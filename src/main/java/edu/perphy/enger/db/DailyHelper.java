package edu.perphy.enger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.perphy.enger.R;
import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/4/12 0012.
 * 每日一句帮助类
 */
public class DailyHelper extends SQLiteOpenHelper {
    private static DailyHelper instance;
    private Context mContext;

    private DailyHelper(Context context) {
        super(context, Consts.DB.TABLE_DAILY, null, 1);
        mContext = context;
    }

    public static synchronized DailyHelper getInstance(Context context) {
        return instance == null ? instance = new DailyHelper(context) : instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_DB = "create table if not exists " + Consts.DB.TABLE_DAILY + "("
                + Consts.DB._ID + " integer primary key autoincrement, "
                + Consts.DB.COL_DATE + " text unique not null, "
                + Consts.DB.COL_CONTENT + " text, "
                + Consts.DB.COL_NOTE + " text, "
                + Consts.DB.COL_STAR + " boolean default 0)";
        db.execSQL(CREATE_DB);

        // insert snapshot
        ContentValues cv = new ContentValues();
        cv.put(Consts.DB.COL_DATE, mContext.getString(R.string.daily_date));
        cv.put(Consts.DB.COL_CONTENT, mContext.getString(R.string.daily_content));
        cv.put(Consts.DB.COL_NOTE, mContext.getString(R.string.daily_note));
        db.beginTransaction();
        try {
            db.insertOrThrow(Consts.DB.TABLE_DAILY, null, cv);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "DailyHelper.onCreate: err", e);
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
