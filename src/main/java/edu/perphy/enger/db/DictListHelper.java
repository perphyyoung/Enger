package edu.perphy.enger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/28 0028.
 * 词典列表帮助类，对应于DictActivity
 */
public class DictListHelper extends SQLiteOpenHelper {
    public static DictListHelper instance;
    public static final String TABLE_NAME = "list";
    public static final String _ID = "_id";
    public static final String COL_DICT_ID = "id";
    public static final String COL_INTERNAL = "internal";// 1:yes 0:no
    public static final String COL_IDX_LOADED = "loaded";// 1:yes 0:no
    public static final String COL_PARENT_PATH = "parentPath";
    public static final String COL_DICT_DZ_TYPE = "type";
    public static final String COL_PURE_NAME = "pureName";
    public static final String COL_ENABLE = "enable";

    private DictListHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public static synchronized DictListHelper getInstance(Context context) {
        return instance == null ? instance = new DictListHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + TABLE_NAME + " ( "
                + _ID + " integer primary key autoincrement, "
                + COL_DICT_ID + " text unique, "
                + COL_INTERNAL + " boolean default 0, "
                + COL_IDX_LOADED + " boolean default 0, "
                + COL_DICT_DZ_TYPE + " text, "
                + COL_PURE_NAME + " text, "
                + COL_ENABLE + " boolean default 1, "
                + COL_PARENT_PATH + " text)";
        db.execSQL(CREATE_DB);

        ContentValues cv = new ContentValues(5);
        cv.put(COL_DICT_ID, InternalHelper.TABLE_NAME);
        cv.put(COL_INTERNAL, 1);
        cv.put(COL_IDX_LOADED, 1);
        cv.put(COL_DICT_DZ_TYPE, "dict");
        cv.put(COL_ENABLE, 1);
        if (-1 == db.insert(TABLE_NAME, null, cv)) {
            Log.e(TAG, "DictListHelper.onCreate: insert internal list err", null);
        } else {
            if (DEBUG) Log.i(TAG, "DictListHelper.onCreate: inset internal list success");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
