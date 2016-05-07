package edu.perphy.enger.db;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by perphy on 2016/3/12 0012.
 * InternalHelper
 */
public class InternalHelper extends SQLiteAssetHelper {
    private static InternalHelper instance;
    public static final String FILE_NAME = "internalDict";
    public static final String TABLE_NAME = "dict1857041860";
    public static final String DICT_NAME = "牛津简明英汉袖珍辞典";
    public static final int WORD_COUNT = 142367;

    private InternalHelper(Context context) {
        super(context, FILE_NAME, null, 1);
    }

    public static synchronized InternalHelper getInstance(Context context) {
        return instance == null ? instance = new InternalHelper(context) : instance;
    }
}
