package edu.perphy.enger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.perphy.enger.R;

import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/4/12 0012.
 * 每日一句帮助类
 */
public class DailyHelper extends SQLiteOpenHelper {
    private Context mContext;
    private static DailyHelper instance;
    public static final String TABLE_NAME = "daily";
    public static final String _ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_ENGLISH = "english";
    public static final String COL_CHINESE = "chinese";
    public static final String COL_STAR = "star";
    public static final String COL_COMMENT = "comment";

    public static final String COL_TTS = "tts";

    private DailyHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
        mContext = context;
    }

    public static synchronized DailyHelper getInstance(Context context) {
        return instance == null ? instance = new DailyHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + TABLE_NAME + "("
                + _ID + " integer primary key autoincrement, "
                + COL_DATE + " text unique not null, "
                + COL_ENGLISH + " text, "
                + COL_CHINESE + " text, "
                + COL_STAR + " boolean default 0)";
        db.execSQL(CREATE_DB);

        // insert snapshot
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, mContext.getString(R.string.daily_date));
        cv.put(COL_ENGLISH, mContext.getString(R.string.daily_english));
        cv.put(COL_CHINESE, mContext.getString(R.string.daily_chinese));
        db.beginTransaction();
        try {
            db.insertOrThrow(TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "DailyHelper.onCreate: err", e);
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
