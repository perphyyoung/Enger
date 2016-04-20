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
 * Created by perphy on 2016/2/28 0028.
 * 词典列表帮助类，对应于DictActivity
 */
public class DictListHelper extends SQLiteOpenHelper {
    public DictListHelper(Context context) {
        super(context, Consts.DB.TABLE_LIST, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + Consts.DB.TABLE_LIST + " ( "
                + Consts.DB._ID + " integer primary key autoincrement, "
                + Consts.DB.COL_DICT_ID + " text unique, "
                + Consts.DB.COL_INTERNAL + " boolean default 0, "
                + Consts.DB.COL_IDX_LOADED + " boolean default 0, "
                + Consts.DB.COL_DICT_DZ_TYPE + " text, "
                + Consts.DB.COL_PURE_NAME + " text, "
                + Consts.DB.COL_ENABLE + " boolean default 1, "
                + Consts.DB.COL_PARENT_PATH + " text)";
        db.execSQL(CREATE_DB);

        ContentValues cv = new ContentValues(5);
        cv.put(Consts.DB.COL_DICT_ID, Consts.DB.INTERNAL_ID);
        cv.put(Consts.DB.COL_INTERNAL, 1);
        cv.put(Consts.DB.COL_IDX_LOADED, 1);
        cv.put(Consts.DB.COL_DICT_DZ_TYPE, "dict");
        cv.put(Consts.DB.COL_ENABLE, 1);
        if (-1 == db.insert(Consts.DB.TABLE_LIST, null, cv)) {
            Log.e(TAG, "DictListHelper.onCreate: insert internal list err", null);
        } else {
            if (DEBUG) Log.i(TAG, "DictListHelper.onCreate: inset internal list success");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
