package edu.perphy.enger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/26 0026.
 * 管理所有的词典，主要存储词典的编号及ifo文件信息
 */
public class DictInfoHelper extends SQLiteOpenHelper {
    public DictInfoHelper(Context context) {
        super(context, Consts.DB.TABLE_INFO, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + Consts.DB.TABLE_INFO + " ("
                + Consts.DB._ID + " integer primary key autoincrement, "
                + Consts.DB.COL_DICT_ID + " text unique, "
                + Consts.DB.COL_VERSION + " text, "
                + Consts.DB.COL_WORD_COUNT + " text, "
                + Consts.DB.COL_IDX_FILE_SIZE + " text, "
                + Consts.DB.COL_AUTHOR + " text, "
                + Consts.DB.COL_BOOK_NAME + " text unique, "
                + Consts.DB.COL_CONTENT_TYPE + " text, "
                + Consts.DB.COL_DATE + " text, "
                + Consts.DB.COL_DESCRIPTION + " text, "
                + Consts.DB.COL_EMAIL + " text, "
                + Consts.DB.COL_IDX_OFFSET_BITS + " text, "
                + Consts.DB.COL_SYN_WORD_COUNT + " text, "
                + Consts.DB.COL_WEBSITE + " text, "
                + Consts.DB.COL_DICT_TYPE + " text)";
        db.execSQL(CREATE_DB);

        //插入内置词典的ifo信息
        ContentValues cv = new ContentValues(9);
        cv.put(Consts.DB.COL_DICT_ID, Consts.DB.INTERNAL_ID);
        cv.put(Consts.DB.COL_VERSION, "2.4.2");
        cv.put(Consts.DB.COL_WORD_COUNT, Consts.DB.INTERNAL_DICT_COUNT + "");
        cv.put(Consts.DB.COL_IDX_FILE_SIZE, "2652772");
        cv.put(Consts.DB.COL_BOOK_NAME, Consts.DB.INTERNAL_DICT_NAME);
        cv.put(Consts.DB.COL_AUTHOR, "GMX");
        cv.put(Consts.DB.COL_DESCRIPTION, "胡正制作");
        cv.put(Consts.DB.COL_DATE, "2006.5.17");
        cv.put(Consts.DB.COL_CONTENT_TYPE, "m");
        if (-1 == db.insert(Consts.DB.TABLE_INFO, null, cv)) {
            Log.e(TAG, "DictInfoHelper.onCreate: insert internal ifo file err", null);
        } else {
            if (DEBUG) Log.i(TAG, "DictInfoHelper.onCreate: insert internal ifo file success");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
