package edu.perphy.enger.thread;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

import edu.perphy.enger.db.InternalHelper;
import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/2/28 0028.
 * ParseDictCallable
 */
public class ParseDictCallable implements Callable<String> {
    private Context context;
    private String word;
    private AssetManager am;

    private SQLiteAssetHelper interHelper;
    private SQLiteDatabase interReader;

    public ParseDictCallable(Context context, String word) {
        this.context = context;
        this.word = word;
        am = context.getAssets();
    }

    @Override
    public String call() throws Exception {
        interHelper = new InternalHelper(context);
        interReader = interHelper.getReadableDatabase();
        Cursor c = interReader.query(Consts.DB.INTERNAL_ID,
                new String[]{Consts.DB.COL_OFFSET, Consts.DB.COL_LENGTH},
                Consts.DB.COL_WORD + " = ?",
                new String[]{word}, null, null, null);
        int offset, length;
        if (c.moveToFirst()) {
            offset = c.getInt(c.getColumnIndex(Consts.DB.COL_OFFSET));
            length = c.getInt(c.getColumnIndex(Consts.DB.COL_LENGTH));
            c.close();
            interReader.close();
        } else {
            return null;
        }

        try (InputStream is = am.open("databases" + File.separator + Consts.DB.INTERNAL_DICT + ".dict")) {
            is.skip(offset);
            byte[] bytes = new byte[length];
            if (is.read(bytes) == -1) {
                if (DEBUG)
                    Log.i(TAG, "ParseDictCallable.call: Arrive at the end of file!");
            }
            return new String(bytes, "utf-8");
        }
    }
}
