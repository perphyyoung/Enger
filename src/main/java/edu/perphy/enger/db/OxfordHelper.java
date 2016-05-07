package edu.perphy.enger.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by perphy on 2016/4/17 0017.
 * OxfordHelper
 */
public class OxfordHelper extends SQLiteAssetHelper {
    private static OxfordHelper instance;
    public static final String TABLE_NAME = "sample_oxford";
    public static final int WORD_COUNT = 39429;
    public static final String COL_ID = "_id";
    public static final String COL_WORD = "word";

    private OxfordHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    public static synchronized OxfordHelper getInstance(Context context) {
        return instance == null ? instance = new OxfordHelper(context) : instance;
    }
}
