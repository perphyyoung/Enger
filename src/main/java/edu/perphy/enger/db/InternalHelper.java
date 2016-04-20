package edu.perphy.enger.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import edu.perphy.enger.util.Consts;

/**
 * Created by perphy on 2016/3/12 0012.
 * InternalHelper
 */
public class InternalHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = Consts.DB.INTERNAL_DICT;
    private static final int DATABASE_VERSION = 1;

    public InternalHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
