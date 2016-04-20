package edu.perphy.enger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.perphy.enger.util.Consts;

/**
 * Created by perphy on 2016/4/7 0007.
 * 笔记帮助类
 */
public class NoteHelper extends SQLiteOpenHelper {
    private static NoteHelper instance;

    private NoteHelper(Context context) {
        super(context, Consts.DB.TABLE_NOTE, null, 1);
    }

    public static synchronized NoteHelper getInstance(Context context) {
        return instance == null ? instance = new NoteHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + Consts.DB.TABLE_NOTE + " ( "
                + Consts.DB._ID + " integer primary key autoincrement, "
                + Consts.DB.COL_TITLE + " text unique not null, "
                + Consts.DB.COL_CONTENT + " text, "
                + Consts.DB.COL_STAR + " boolean default 0, "
                + Consts.DB.COL_CREATE_TIME + " text, "
                + Consts.DB.COL_MODIFY_TIME + " text)";
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
