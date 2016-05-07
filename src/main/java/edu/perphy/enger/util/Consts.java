package edu.perphy.enger.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by perphy on 2016/2/18 0018.
 * 常数类
 */
public class Consts {
    public static final String TAG = "PY";
    public static final boolean DEBUG = false;
    public static final String ROOT_PATH_STR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "Enger";
    public static final String PATH_DIC_STR = ROOT_PATH_STR + File.separator + "dic";
    public static final String PATH_NOTE_STR = ROOT_PATH_STR + File.separator + "note";

    public static class Setting {
        // notice 与SettingsActivity必须一致
        public static final String CBP_WIFI_ONLY = "cbpWifiOnly";
        public static final String LP_MAX_WORD_COUNT = "lpMaxWordCount";
        public static final String ETP_NAME = "etpName";
        public static final String ETP_EMAIL = "etpEmail";
    }

    public static final String SP_NAME = "pyPreferences";
    public static final String SP_HISTORY = "history";
    public static final String SP_FIRST_RUN = "firstRun";
    public static final String SP_NOT_NIGHT_MODE = "nightMode";
    public static final String SP_IDX_LOADED = "idxLoaded";
    public static final String SP_AVATAR = "avatar";

    public static final String HANDLER_THREAD_UPDATE_DICT_LIST = "handlerThreadUpdateDictList";
    public static final String HANDLER_THREAD_IDX = "handlerThreadIdx";
    public static final String HANDLER_THREAD_DICT = "handlerThreadDict";
    public static final String HANDLER_THREAD_DEFINITION = "handlerThreadDefinition";

    public static final int PARSE_IFO_PATH_NOT_NEW_LOAD = 1;

    public static final int PARSE_IFO_SUCCESS = 11;
    public static final int PARSE_IFO_NOT_NEW_LOAD = 12;
    public static final int PARSE_IFO_CONTENT_ERR = 13;
    public static final int PARSE_IFO_VERSION_NOT_SUPPORT = 14;
    public static final int PARSE_IFO_NOT_EXISTS = 15;
    public static final int PARSE_IFO_ALREADY_LOADED = 16;
    public static final int PARSE_IFO_GET_ERROR = 17;

    public static final int PARSE_IDX_SUCCESS = 21;
    public static final int PARSE_IDX_NOT_NEW_LOAD = 22;
    public static final int PARSE_IDX_READ_CONTENT_ERR = 23;
    public static final int PARSE_IDX_ALREADY_LOADED = 24;
    public static final int PARSE_IDX_NOT_EXISTS = 25;
    public static final int PARSE_IDX_INPUT_SCREAM_ERR = 26;

    public static final String DICT_SEPARATOR = "###";

    public static final int PARSE_DICT_SUCCESS = 31;
    public static final int PARSE_DICT_NOT_EXISTS = 32;
    public static final int PARSE_DICT_INPUT_SCREAM_ERR = 33;

    public static final String TAG_LOADING_DIALOG = "loadingDialog";
}


