package edu.perphy.enger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.perphy.enger.util.Consts;

/**
 * Created by perphy on 2016/2/18 0018.
 * StarDict词典解析
 */
public class StarDictHelper extends SQLiteOpenHelper {
    String CREATE_DB;

    public StarDictHelper(Context context, String tableName) {
        super(context, tableName, null, 1);
        CREATE_DB = "create table " + tableName + " ("
                + Consts.DB._ID + " integer primary key autoincrement, "
                + Consts.DB.COL_WORD + " text, "
                + Consts.DB.COL_OFFSET + " text, "
                + Consts.DB.COL_LENGTH + " text)";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
