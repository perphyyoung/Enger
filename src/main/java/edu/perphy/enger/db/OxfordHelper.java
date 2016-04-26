package edu.perphy.enger.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by perphy on 2016/4/17 0017.
 * OxfordHelper
 */
public class OxfordHelper extends SQLiteAssetHelper {
    public static final String TABLE_NAME = "sample_oxford";
    private static final int DATABASE_VERSION = 1;
    public static final int WORD_COUNT = 39429;
    public static final String COL_ID = "_id";
    public static final String COL_WORD = "word";

    public OxfordHelper(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }
}
