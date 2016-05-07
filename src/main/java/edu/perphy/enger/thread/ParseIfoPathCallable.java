package edu.perphy.enger.thread;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Callable;

import edu.perphy.enger.db.DictListHelper;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.FileUtils;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/29 0029.
 * ifo文件的路径
 */
public class ParseIfoPathCallable implements Callable<Integer> {
    private static final int MAX_RECURSIVE_LAYER = 5;
    private DictListHelper listHelper;

    private int layer = 0;
    private int successfulFindCount = 0;
    private int successfulInsertCount = 0;

    public ParseIfoPathCallable(Context context) {
        listHelper = new DictListHelper(context);
    }

    @Override
    public Integer call() throws Exception {
        recursiveFindIfoFile(new File(Consts.PATH_DIC_STR));
        if (successfulFindCount == 0) {
            if (DEBUG) Log.i(TAG, "ParseIfoPathCallable.call: 没有发现新的ifo文件");
            return 0;
        } else {
            if (DEBUG) Log.i(TAG, "ParseIfoPathCallable.call: 成功发现" +
                    successfulFindCount + "个新ifo文件");
            return successfulInsertCount;
        }
    }

    /**
     * 递归寻找ifo文件
     *
     * @param rootPath 根目录
     */
    public void recursiveFindIfoFile(File rootPath) {
        layer++;
        File[] files = rootPath.listFiles(new FileUtils.PostfixFileFilter("ifo"));
        // 没有相应的ifo文件，并且此文件夹下也没有子文件夹
        if (files.length == 0) {
            layer--;
            return;
        }
        for (File file : files) {
            if (file.isDirectory() && layer < MAX_RECURSIVE_LAYER) {
                recursiveFindIfoFile(file); // recursive search
            } else {
                if (updateListDb(file)) {
                    successfulInsertCount++;
                }
            }
        }
    }

    /**
     * 将ifo文件的相应目录等信息插入到list数据库
     *
     * @param ifoFile ifo文件
     */
    public boolean updateListDb(File ifoFile) {
        SQLiteDatabase listWriter = listHelper.getWritableDatabase();
        Thread idleThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        idleThread.start();

        String ifoFileStr = ifoFile.getName();
        File parentPath = new File(ifoFile.getParent());

        if (DEBUG) Log.i(TAG, "ParseIfoPathCallable.updateListDb: " + layer +
                " 父目录：" + parentPath.toString());
        String pureName = ifoFileStr.substring(0, ifoFileStr.length() - 4);
        String dictId = "dict" + Math.abs(pureName.hashCode());

        if(idleThread.isAlive()){
            try {
                idleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 检查是否已插入此词典信息到list
        String sql = "select count(*) from " + Consts.DB.TABLE_LIST + " where " + Consts.DB.COL_DICT_ID + " = ?";
        SQLiteStatement statement = listWriter.compileStatement(sql);
        statement.bindString(1, dictId);
        int result = (int) statement.simpleQueryForLong();
        if (result > 0) {
            if (DEBUG) Log.i(TAG, "ParseIfoPathCallable.updateListDb: " + pureName + "已插入到list");
            return false; // 已插入到list
        } else {
            successfulFindCount++;
            ContentValues cv = new ContentValues(5);
            cv.put(Consts.DB.COL_DICT_ID, dictId);
            cv.put(Consts.DB.COL_IDX_LOADED, 0);
            cv.put(Consts.DB.COL_PARENT_PATH, parentPath.getAbsolutePath());
            cv.put(Consts.DB.COL_DICT_DZ_TYPE, "dict");
            cv.put(Consts.DB.COL_PURE_NAME, pureName);

            boolean success = -1 != listWriter.insertOrThrow(Consts.DB.TABLE_LIST, null, cv);
            cv.clear();
            listWriter.close();
            if (!success) {
                Log.e(TAG, "ParseIfoPathCallable.updateListDb: " + pureName + " 插入list失败", null);
                return false;//插入list失败
            } else {
                return true;//插入list成功
            }
        }
    }
}
