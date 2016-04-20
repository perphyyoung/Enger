package edu.perphy.enger.data;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by perphy on 2016/4/17 0017.
 * OxfordHelper
 */
public class OxfordHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "sample_oxford";
    private static final int DATABASE_VERSION = 1;
    public static final int WORD_COUNT = 39429;
    public static final String COL_WORD = "word";

    public OxfordHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
