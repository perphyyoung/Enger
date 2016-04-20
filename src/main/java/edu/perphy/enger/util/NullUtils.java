package edu.perphy.enger.util;

import android.text.TextUtils;

/**
 * Created by perphy on 2016/4/9 0009.
 * null -> ""
 */
public class NullUtils {
    public static String null2empty(String s) {
        return TextUtils.equals(s, null) ? "" : s;
    }
}
