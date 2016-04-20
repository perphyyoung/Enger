package edu.perphy.enger.util;

/**
 * Created by perphy on 2016/4/13 0013.
 * 字符工具类
 */
public class CharUtils {
    public static final char EN_DASH = '\u2013';
    public static final String REGEX_EN_DASH = "\\p{Pd}"; // en dash regex

    /**
     * 将连字符(-,hyphen)转换为连接号(–, en dash, 0x2013)，用于日期的连接
     *
     * @param src 包含连字符的字符串
     * @return 转换后的结果
     */
    public static String hyphen2en_dash(String src) {
        return src.replace('-', EN_DASH);
    }

    public static String en_dash2hyphen(String src) {
        return src.replaceAll(REGEX_EN_DASH, "-");
    }
}
