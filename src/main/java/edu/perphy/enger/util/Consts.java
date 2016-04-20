package edu.perphy.enger.util;

import android.graphics.PixelFormat;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.design.widget.CoordinatorLayout;
import android.view.WindowManager;

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

    /**
     * 数据库相关常数
     */
    public static class DB implements BaseColumns {
        // internal
        public static final String INTERNAL_DICT = "internalDict";
        public static final String INTERNAL_ID = "dict1857041860";
        public static final String INTERNAL_DICT_NAME = "牛津简明英汉袖珍辞典";
        public static final int INTERNAL_DICT_COUNT = 142367;

        // 词典的ifo信息 与ifo内的定义名称一致
        public static final String TABLE_INFO = "info";
        public static final String COL_DICT_ID = "id";
        public static final String COL_BOOK_NAME = "bookname";
        public static final String COL_WORD_COUNT = "wordcount";
        public static final String COL_IDX_FILE_SIZE = "idxfilesize";
        public static final String COL_AUTHOR = "author";
        public static final String COL_EMAIL = "email";
        public static final String COL_WEBSITE = "website";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_DATE = "date";
        public static final String COL_CONTENT_TYPE = "sametypesequence";
        public static final String COL_VERSION = "version";
        public static final String COL_SYN_WORD_COUNT = "synwordcount";
        public static final String COL_IDX_OFFSET_BITS = "idxoffsetbits";
        public static final String COL_DICT_TYPE = "dicttype";

        // list, 词典在app中的相关配置信息
        public static final String TABLE_LIST = "list";
        public static final String COL_INTERNAL = "internal";// 1:yes 0:no
        public static final String COL_IDX_LOADED = "loaded";// 1:yes 0:no
        public static final String COL_PARENT_PATH = "parentPath";
        public static final String COL_DICT_DZ_TYPE = "type"; //todo dict or dict.dz
        public static final String COL_PURE_NAME = "pureName";
        public static final String COL_ENABLE = "enable";
        public static final String COL_STAR = "star";

        // idx
        public static final String COL_WORD = "word";
        public static final String COL_OFFSET = "offset";
        public static final String COL_LENGTH = "length";

        // note 笔记数据库
        public static final String TABLE_NOTE = "note";
        // col_id
        public static final String COL_TITLE = "title";
        public static final String COL_CONTENT = "content";
        public static final String COL_CREATE_TIME = "createTime";
        public static final String COL_MODIFY_TIME = "modifyTime";
        // col_star
        public static final String COL_TOBE_SAVE = "tobeSave";
        public static final String CUSTOM_DICT_NAME = "Custom Dictionary";

        // daily 每日一句数据库
        public static final String TABLE_DAILY = "daily";
        // col_date
        // col_content
        public static final String COL_NOTE = "note";
        // col_star
        public static final String COL_COMMENT = "comment";
    }

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
    public static final String SP_HAS_DIY_DICT = "hasDiyDict";
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

    public static final String NOTE_READ_ONLY = "readOnly";

    public static final String TAG_LOADING_DIALOG = "loadingDialog";


    // TODO: 2016/3/20 0020 to be delete
    public static final WindowManager.LayoutParams PARAMS = new WindowManager.LayoutParams(
            CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
}


