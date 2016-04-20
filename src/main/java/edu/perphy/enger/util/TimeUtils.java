package edu.perphy.enger.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by perphy on 2016/4/7 0007.
 * 时间和日期的工具类
 */
public class TimeUtils {

    public static String getSimpleDateTime() {
        String pattern = CharUtils.hyphen2en_dash("yyyy-MM-dd HH:mm"); // year, month, day, hour, minute
        CharUtils.hyphen2en_dash(pattern);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static String getSimpleDate() {
        String pattern = CharUtils.hyphen2en_dash("yyyy-MM-dd"); // year, month, day
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
