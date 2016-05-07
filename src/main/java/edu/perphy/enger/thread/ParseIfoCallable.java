package edu.perphy.enger.thread;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import edu.perphy.enger.db.DictInfoHelper;
import edu.perphy.enger.db.DictListHelper;
import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.DEBUG;

/**
 * Created by perphy on 2016/2/28 0028.
 * 解析ifo文件的线程
 */
public class ParseIfoCallable implements Callable<Integer> {
    private Context context;
    private MyIfoHandler myIfoHandler;
    private DictListHelper listHelper;
    private DictInfoHelper infoHelper;

    private ArrayList<HashMap<String, String>> al;
    private int successfulInsertCount = 0;

    public ParseIfoCallable(Context context) {
        this.context = context;

        listHelper = DictListHelper.getInstance(context);
        infoHelper = DictInfoHelper.getInstance(context);

        HandlerThread handlerThread = new HandlerThread(Consts.HANDLER_THREAD_DICT);
        handlerThread.start();
        myIfoHandler = new MyIfoHandler(handlerThread.getLooper());
    }

    @Override
    public Integer call() throws Exception {
        al = new ArrayList<>();

        // 从list数据库中读取ifo文件的父目录和具体的文件名
        SQLiteDatabase listReader = listHelper.getReadableDatabase();
        Cursor c = listReader.query(DictListHelper.TABLE_NAME,
                new String[]{DictListHelper.COL_PARENT_PATH, DictListHelper.COL_PURE_NAME},
                DictListHelper.COL_INTERNAL + " = ?", new String[]{0 + ""},
                null, null, null, null);

        while (c.moveToNext()) {
            String parentPath = c.getString(c.getColumnIndex(DictListHelper.COL_PARENT_PATH));
            String pureName = c.getString(c.getColumnIndex(DictListHelper.COL_PURE_NAME));
            String dictId = "dict" + Math.abs(pureName.hashCode());

            // 从info中查询是否已加载此ifo文件
            SQLiteDatabase infoReader = infoHelper.getReadableDatabase();
            String sql = "select count(*) from " + DictInfoHelper.TABLE_NAME +
                    " where " + DictInfoHelper.COL_DICT_ID + " = ?";
            SQLiteStatement statement = infoReader.compileStatement(sql);
            statement.bindString(1, dictId);
            int result = (int) statement.simpleQueryForLong();
            infoReader.close();
            if (1 == result) {
                if (DEBUG) Log.i(TAG, "ParseIfoCallable.call: " + pureName + "已加载");
            } else {
                if (parseIfoHelper(parentPath, pureName, dictId)) {
                    successfulInsertCount++;
                }
            }
        }
        c.close();
        listReader.close();
        return successfulInsertCount;
    }

    /**
     * 具体解析ifo文件
     *
     * @param parentPath 父目录
     * @param pureName   文件名
     * @param dictId     词典ID
     * @return 是否解析成功
     */
    private boolean parseIfoHelper(String parentPath, String pureName, String dictId) {
        try (RandomAccessFile raf = new RandomAccessFile(
                parentPath + File.separator + pureName + ".ifo", "r")) {
            String line = raf.readLine();
            if (line.equals("StarDict's dict ifo file")) {
                HashMap<String, String> hm = new HashMap<>();
                while ((line = raf.readLine()) != null) {
                    String[] ifoPair = line.split("=");
                    ifoPair[1] = new String(ifoPair[1].getBytes("iso-8859-1"), "utf-8");
                    hm.put(ifoPair[0], ifoPair[1]);
                }
                String version = hm.get(DictInfoHelper.COL_VERSION);
                if (version.equals("2.4.2")) {
                    hm.put(DictInfoHelper.COL_DICT_ID, dictId);
                } else {
                    Log.e(TAG, "ParseIfoCallable.parseIfoFile: 版本不支持", null);

                    Message msg = myIfoHandler.obtainMessage();
                    msg.what = Consts.PARSE_IFO_VERSION_NOT_SUPPORT;
                    msg.obj = pureName;
                    msg.sendToTarget();
                    return false;
                }
                al.add(hm);
                return InsertIfoToInfo(pureName);
            } else {
                Log.e(TAG, "ParseIfoCallable.parseIfoFile: 首行不匹配", null);

                Message msg = myIfoHandler.obtainMessage();
                msg.what = Consts.PARSE_IFO_CONTENT_ERR;
                msg.obj = pureName;
                msg.sendToTarget();
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "ParseIfoCallable.parseIfoFile: raf error", e);
            e.printStackTrace();
            return false;
        }
    }

    private boolean InsertIfoToInfo(String pureName) {
        // 将ifo文件信息插入到info数据库中
        ContentValues values = new ContentValues();
        for (HashMap hm : al) {
            values.put(DictInfoHelper.COL_AUTHOR, (String) hm.get(DictInfoHelper.COL_AUTHOR));
            values.put(DictInfoHelper.COL_BOOK_NAME, (String) hm.get(DictInfoHelper.COL_BOOK_NAME));
            values.put(DictInfoHelper.COL_CONTENT_TYPE, (String) hm.get(DictInfoHelper.COL_CONTENT_TYPE));
            values.put(DictInfoHelper.COL_DATE, (String) hm.get(DictInfoHelper.COL_DATE));
            values.put(DictInfoHelper.COL_DESCRIPTION, (String) hm.get(DictInfoHelper.COL_DESCRIPTION));
            values.put(DictInfoHelper.COL_DICT_TYPE, (String) hm.get(DictInfoHelper.COL_DICT_TYPE));
            values.put(DictInfoHelper.COL_EMAIL, (String) hm.get(DictInfoHelper.COL_EMAIL));
            values.put(DictInfoHelper.COL_DICT_ID, (String) hm.get(DictInfoHelper.COL_DICT_ID));
            values.put(DictInfoHelper.COL_IDX_FILE_SIZE, (String) hm.get(DictInfoHelper.COL_IDX_FILE_SIZE));
            values.put(DictInfoHelper.COL_IDX_OFFSET_BITS, (String) hm.get(DictInfoHelper.COL_IDX_OFFSET_BITS));
            values.put(DictInfoHelper.COL_SYN_WORD_COUNT, (String) hm.get(DictInfoHelper.COL_SYN_WORD_COUNT));
            values.put(DictInfoHelper.COL_VERSION, (String) hm.get(DictInfoHelper.COL_VERSION));
            values.put(DictInfoHelper.COL_WEBSITE, (String) hm.get(DictInfoHelper.COL_WEBSITE));
            values.put(DictInfoHelper.COL_WORD_COUNT, (String) hm.get(DictInfoHelper.COL_WORD_COUNT));
        }
        SQLiteDatabase infoWriter = infoHelper.getWritableDatabase();
        boolean success = infoWriter.insertOrThrow(DictInfoHelper.TABLE_NAME, null, values) != -1;
        infoWriter.close();
        if (success) {
            if (DEBUG) Log.i(TAG, "ParseIfoCallable.call: " + pureName + "插入到info成功");
            return true;
        } else {
            Log.e(TAG, "ParseIfoCallable.parseIfoFile: " + pureName + "插入ifo信息失败", null);
            return false;
        }
    }

    private class MyIfoHandler extends Handler {
        public MyIfoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String fileName;
            switch (msg.what) {
                case Consts.PARSE_IFO_CONTENT_ERR:
                    fileName = (String) msg.obj;
                    Toast.makeText(context, fileName + ".ifo content error", Toast.LENGTH_SHORT).show();
                    break;
                case Consts.PARSE_IFO_VERSION_NOT_SUPPORT:
                    fileName = (String) msg.obj;
                    Toast.makeText(context, fileName + " : Not support this version yet", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
