package edu.perphy.enger.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/4/7 0007.
 * 笔记帮助类
 */
public class NoteHelper extends SQLiteOpenHelper {
    private static NoteHelper instance;
    public static final String TABLE_NAME = "note";
    public static final String _ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_STAR = "star";
    public static final String COL_CREATE_TIME = "createTime";
    public static final String COL_MODIFY_TIME = "modifyTime";
    public static final String COL_TOBE_SAVE = "tobeSave";
    public static final String CUSTOM_DICT_NAME = "Custom Dictionary";

    private NoteHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public static synchronized NoteHelper getInstance(Context context) {
        return instance == null ? instance = new NoteHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + TABLE_NAME + " ( "
                + _ID + " integer primary key autoincrement, "
                + COL_TITLE + " text unique not null, "
                + COL_CONTENT + " text, "
                + COL_STAR + " boolean default 0, "
                + COL_CREATE_TIME + " text, "
                + COL_MODIFY_TIME + " text)";
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static void delete(Context context, ArrayList<Integer> toRemoveList) {
        SQLiteDatabase noteWriter = getInstance(context).getWritableDatabase();
        noteWriter.beginTransaction();
        try {
            String sql = "delete from " + TABLE_NAME
                    + " where " + _ID + " in (" + TextUtils.join(",", toRemoveList) + ")";
            noteWriter.execSQL(sql);
            noteWriter.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "NoteHelper.delete: ", e);
            e.printStackTrace();
        } finally {
            noteWriter.endTransaction();
            noteWriter.close();
        }
    }
}
