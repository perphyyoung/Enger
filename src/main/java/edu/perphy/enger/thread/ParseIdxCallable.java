package edu.perphy.enger.thread;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import edu.perphy.enger.db.DictInfoHelper;
import edu.perphy.enger.db.DictListHelper;
import edu.perphy.enger.db.StarDictHelper;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.FileUtils;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/28 0028.
 * 解析idx文件的线程，并插入到对应的数据库
 */
public class ParseIdxCallable implements Callable<Integer> {
    private static final int MAX_WORD_LEN = 256;

    private DictListHelper listHelper;
    private DictInfoHelper infoHelper;
    private Context context;
    private MyIdxHandler myIdxHandler;
    private int successfulLoadCount = 0;
    private StringBuilder sb;

    public ParseIdxCallable(final Context context) {
        this.context = context;
        listHelper = DictListHelper.getInstance(context);
        infoHelper = DictInfoHelper.getInstance(context);
        sb = new StringBuilder();

        HandlerThread handlerThread = new HandlerThread(Consts.HANDLER_THREAD_IDX);
        handlerThread.start();
        myIdxHandler = new MyIdxHandler(handlerThread.getLooper());
    }

    @Override
    public Integer call() throws Exception {
        // 从list中获取parentPath, pureName
        SQLiteDatabase listReader = listHelper.getReadableDatabase();
        Cursor c = listReader.query(DictListHelper.TABLE_NAME,
                new String[]{DictListHelper.COL_PARENT_PATH, DictListHelper.COL_PURE_NAME},
                DictListHelper.COL_INTERNAL + " = ? and " + DictListHelper.COL_IDX_LOADED + " = ?", // 不是内置，也没有加载过
                new String[]{0 + "", 0 + ""},
                null, null, null, null);

        while (c.moveToNext()) {
            String parentPath = c.getString(c.getColumnIndex(DictListHelper.COL_PARENT_PATH));
            String pureName = c.getString(c.getColumnIndex(DictListHelper.COL_PURE_NAME));

            if (FileUtils.isFileExists(new File(parentPath), pureName + ".idx")) {
                if (parseIdxFile(parentPath, pureName)) {
                    successfulLoadCount++; // 成功加载计数+1
                }
            } else {
                Log.e(TAG, "ParseIdxCallable.call: " + pureName + ".idx 文件不存在", null);
                Message msg = myIdxHandler.obtainMessage();
                msg.obj = pureName;
                msg.what = Consts.PARSE_IDX_NOT_EXISTS;
                msg.sendToTarget();
            }
        }
        c.close();
        listReader.close();

        if (!TextUtils.isEmpty(sb.toString())) {
            String[] dictIds = sb.toString().split(Consts.DICT_SEPARATOR);
            SQLiteDatabase listWriter = listHelper.getWritableDatabase();
            listWriter.beginTransaction();
            for (String dictId : dictIds) {
                if (!TextUtils.isEmpty(dictId)) {
                    String sql = "update " + DictListHelper.TABLE_NAME +
                            " set " + DictListHelper.COL_IDX_LOADED + " = 1" +
                            " where " + DictInfoHelper.COL_DICT_ID + " = ?";
                    SQLiteStatement statement = listWriter.compileStatement(sql);
                    statement.bindString(1, dictId);
                    statement.execute();
                }
            }
            listWriter.setTransactionSuccessful();
            listWriter.endTransaction();
            listWriter.close();
        }

        return successfulLoadCount;
    }

    /**
     * 解析idx文件
     *
     * @param parentPath 父目录
     * @param pureName   文件名（不带后缀）
     * @return 是否解析成功
     */
    public boolean parseIdxFile(final String parentPath, final String pureName) {
        final String dictId = "dict" + Math.abs(pureName.hashCode());

        SQLiteDatabase infoReader = infoHelper.getReadableDatabase();
        StarDictHelper starDictHelper = new StarDictHelper(context, dictId);

        int wordSum;
        // 从info中获取单词的总数目
        String sql4wordCountInIdx = "select " + DictInfoHelper.COL_WORD_COUNT
                + " from " + DictInfoHelper.TABLE_NAME
                + " where " + DictInfoHelper.COL_DICT_ID + " = ?";
        SQLiteStatement statementToCount = infoReader.compileStatement(sql4wordCountInIdx);
        statementToCount.bindString(1, dictId);
        wordSum = (int) statementToCount.simpleQueryForLong();
        infoReader.close();

        if (DEBUG) Log.i(TAG, "ParseIdxCallable.parseIdxFile: wordSum = " + wordSum);
        final int maxCount = wordSum;

        SQLiteDatabase dictWriter = starDictHelper.getWritableDatabase();
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(parentPath + File.separator + pureName + ".idx"))) {
            ArrayList<HashMap> al = new ArrayList<>(maxCount);

            byte[] byteWord = new byte[MAX_WORD_LEN]; // 尝试获取单词的字节数组
            int offset;
            int length;
            int wordCount = 0; // 单词计数
            is.mark(is.available() + 1);

            // 如果没有读取到文件结束
            while (is.read() != -1) {
                is.reset();
                int wordLen = 0; // 已读取的单词长度
                while (true) {
                    try {
                        int index = is.read();
                        if (index == 0) {
                            break;
                        } else if (wordLen < MAX_WORD_LEN) {
                            byteWord[wordLen] = (byte) index; // 将int转换为byte
                            wordLen++;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "ParseIdxCallable.parseIdxFile: " + pureName + ".idx 内容错误", e);
                        e.printStackTrace();
                        Message msg = myIdxHandler.obtainMessage();
                        msg.what = Consts.PARSE_IDX_READ_CONTENT_ERR;
                        msg.obj = pureName;
                        msg.sendToTarget();
                        return false;
                    }
                } // end of inner while

                byte[] byteWord2 = new byte[wordLen]; // 实际的单词字节数组
                System.arraycopy(byteWord, 0, byteWord2, 0, wordLen);
                // notice 解析单词
                String wordBuf = new String(byteWord2);
                wordCount++;

                try {
                    byte[] buffer = new byte[4];
                    // notice 读取偏移量值
                    //noinspection ResultOfMethodCallIgnored
                    is.read(buffer, 0, 4);
                    offset = FileUtils.bytes2Int(buffer);
                    // notice 读取区块大小值
                    //noinspection ResultOfMethodCallIgnored
                    is.read(buffer, 0, 4);
                    length = FileUtils.bytes2Int(buffer);
                    is.mark(is.available() + 1);
                } catch (IOException e) {
                    Log.e(TAG, "ParseIdxCallable.run: read offset or length err", e);
                    e.printStackTrace();
                    Message msg = myIdxHandler.obtainMessage();
                    msg.what = Consts.PARSE_IDX_READ_CONTENT_ERR;
                    msg.obj = pureName;
                    msg.sendToTarget();
                    return false;
                }

                HashMap<String, String> m = new HashMap<>(3);
                m.put(StarDictHelper.COL_WORD, wordBuf);
                m.put(StarDictHelper.COL_OFFSET, offset + "");
                m.put(StarDictHelper.COL_LENGTH, length + "");
                al.add(m);
            }
            if (DEBUG) Log.i(TAG, "ParseIdxCallable.run: 实际读取到: " + wordCount);

            // 插入数据库
            String sql4insert = "insert into " + dictId
                    + "(" + StarDictHelper.COL_WORD + ", "
                    + StarDictHelper.COL_OFFSET + ", "
                    + StarDictHelper.COL_LENGTH + ") values(?,?,?)";
            SQLiteStatement stat = dictWriter.compileStatement(sql4insert);
            dictWriter.beginTransaction();
            for (HashMap hm : al) {
                stat.bindString(1, (String) hm.get(StarDictHelper.COL_WORD));
                stat.bindString(2, (String) hm.get(StarDictHelper.COL_OFFSET));
                stat.bindString(3, (String) hm.get(StarDictHelper.COL_LENGTH));
                stat.executeInsert();
            }
            dictWriter.setTransactionSuccessful();
            dictWriter.endTransaction();
            dictWriter.close();

            if (DEBUG) Log.i(TAG, "MyIdxHandler.handleMessage: " + pureName + " idx插入成功");
            sb.append(dictId);
            sb.append(Consts.DICT_SEPARATOR);

            Message msg = myIdxHandler.obtainMessage();
            msg.what = Consts.PARSE_IDX_SUCCESS;
            msg.obj = pureName;
            msg.sendToTarget();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "ParseIdxCallable.parseIdxFile: " + pureName + ".idx inputStream err", e);
            e.printStackTrace();
            return false;
        }
    }

    private class MyIdxHandler extends Handler {
        public MyIdxHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String pureName;
            switch (msg.what) {
                case Consts.PARSE_IDX_NOT_EXISTS:
                    pureName = (String) msg.obj;
                    Toast.makeText(context, pureName + ".idx not exists, ignore this file", Toast.LENGTH_SHORT).show();
                    break;
                case Consts.PARSE_IDX_READ_CONTENT_ERR:
                    pureName = (String) msg.obj;
                    Toast.makeText(context, pureName + ".idx content error", Toast.LENGTH_SHORT);
                    break;
                case Consts.PARSE_IDX_SUCCESS:
                    pureName = (String) msg.obj;
                    Toast.makeText(context, pureName + ".idx insert successfully", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    if (DEBUG) {
                        Log.e(TAG, "MyIdxHandler.handleMessage: 解析idx文件未知原因错误", null);
                        Toast.makeText(context, "Something unknown cause error", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }
}
