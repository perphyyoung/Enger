package edu.perphy.enger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by perphy on 2016/4/18 0018.
 * 复习时的单词列表
 */
public class ReviewHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "review";
    public static final String COL_ID = "_id";
    public static final String COL_WORD = "word";
    public static final String COL_DEF = "def";
    public static final String COL_DATE_ADD = "dateAdd";
    public static final String COL_DATE_REVIEW = "dateReview";
    private static final int VERSION = 1;

    public ReviewHelper(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "create table if not exists " + TABLE_NAME + " ( "
                + COL_ID + " integer primary key, "
                + COL_WORD + " text not null unique, "
                + COL_DEF + " text, "
                + COL_DATE_ADD + " text, "
                + COL_DATE_REVIEW + " text ) ";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
