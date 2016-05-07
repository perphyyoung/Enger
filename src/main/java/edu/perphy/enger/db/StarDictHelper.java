package edu.perphy.enger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by perphy on 2016/2/18 0018.
 * StarDict词典解析
 */
public class StarDictHelper extends SQLiteOpenHelper {
    private static String TABLE_NAME;
    public static final String _ID = "_id";
    public static final String COL_WORD = "word";
    public static final String COL_OFFSET = "offset";
    public static final String COL_LENGTH = "length";

    public StarDictHelper(Context context, String tableName) {
        super(context, tableName, null, 1);
        TABLE_NAME = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DB = "create table " + TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + COL_WORD + " text, "
                + COL_OFFSET + " text, "
                + COL_LENGTH + " text)";
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
