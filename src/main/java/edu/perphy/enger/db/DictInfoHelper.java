package edu.perphy.enger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/26 0026.
 * 管理所有的词典，主要存储词典的编号及ifo文件信息
 */
public class DictInfoHelper extends SQLiteOpenHelper {
    private static DictInfoHelper instance;
    public static final String TABLE_NAME = "info";
    public static final String _ID = "_id";
    public static final String COL_DICT_ID = "id";
    public static final String COL_BOOK_NAME = "bookname";
    public static final String COL_WORD_COUNT = "wordcount";
    public static final String COL_IDX_FILE_SIZE = "idxfilesize";
    public static final String COL_AUTHOR = "author";
    public static final String COL_EMAIL = "email";
    public static final String COL_WEBSITE = "website";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DATE = "date";
    public static final String COL_CONTENT_TYPE = "sametypesequence";
    public static final String COL_VERSION = "version";
    public static final String COL_SYN_WORD_COUNT = "synwordcount";
    public static final String COL_IDX_OFFSET_BITS = "idxoffsetbits";
    public static final String COL_DICT_TYPE = "dicttype";

    private DictInfoHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public static synchronized DictInfoHelper getInstance(Context context) {
        return instance == null ? instance = new DictInfoHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + COL_DICT_ID + " text unique, "
                + COL_VERSION + " text, "
                + COL_WORD_COUNT + " text, "
                + COL_IDX_FILE_SIZE + " text, "
                + COL_AUTHOR + " text, "
                + COL_BOOK_NAME + " text unique, "
                + COL_CONTENT_TYPE + " text, "
                + COL_DATE + " text, "
                + COL_DESCRIPTION + " text, "
                + COL_EMAIL + " text, "
                + COL_IDX_OFFSET_BITS + " text, "
                + COL_SYN_WORD_COUNT + " text, "
                + COL_WEBSITE + " text, "
                + COL_DICT_TYPE + " text)";
        db.execSQL(CREATE_DB);

        //插入内置词典的ifo信息
        ContentValues cv = new ContentValues(9);
        cv.put(COL_DICT_ID, InternalHelper.TABLE_NAME);
        cv.put(COL_VERSION, "2.4.2");
        cv.put(COL_WORD_COUNT, InternalHelper.WORD_COUNT + "");
        cv.put(COL_IDX_FILE_SIZE, "2652772");
        cv.put(COL_BOOK_NAME, InternalHelper.DICT_NAME);
        cv.put(COL_AUTHOR, "GMX");
        cv.put(COL_DESCRIPTION, "胡正制作");
        cv.put(COL_DATE, "2006.5.17");
        cv.put(COL_CONTENT_TYPE, "m");
        if (-1 == db.insert(TABLE_NAME, null, cv)) {
            Log.e(TAG, "DictInfoHelper.onCreate: insert internal ifo file err", null);
        } else {
            if (DEBUG) Log.i(TAG, "DictInfoHelper.onCreate: insert internal ifo file success");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
