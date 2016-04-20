package edu.perphy.enger.thread;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.LoadingDialogFragment;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.TimeUtils;
import edu.perphy.enger.util.FileUtils;

import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;

/**
 * Created by perphy on 2016/4/10 0010.
 * 导出笔记
 */
public class ExportNoteTask extends AsyncTask<Void, Integer, Integer> {
    private final int SUCCESS = 1;
    private final int ERROR = 2;
    private final int EMPTY = 3;
    private Context mContext;
    private NoteHelper noteHelper;
    LoadingDialogFragment loadingDialogFragment;
    private int exportCount = 0;

    public ExportNoteTask(Context context) {
        this.mContext = context;
        noteHelper = NoteHelper.getInstance(mContext);
    }

    @Override
    protected void onPreExecute() {
        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), TAG_LOADING_DIALOG);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        SQLiteDatabase noteReader = noteHelper.getReadableDatabase();
        JSONArray noteArray = null;
        noteReader.beginTransaction();
        try (Cursor c = noteReader.query(Consts.DB.TABLE_NOTE,
                null, null, null, null, null, null)) {
            noteArray = new JSONArray();
            while (c.moveToNext()) {
                //notice cannot bind boolean?
                JSONObject note = new JSONObject();
                note.put(Consts.DB.COL_TITLE, c.getString(c.getColumnIndex(Consts.DB.COL_TITLE)));
                note.put(Consts.DB.COL_CONTENT, c.getString(c.getColumnIndex(Consts.DB.COL_CONTENT)));
                note.put(Consts.DB.COL_STAR, c.getString(c.getColumnIndex(Consts.DB.COL_STAR)));
                note.put(Consts.DB.COL_CREATE_TIME, c.getString(c.getColumnIndex(Consts.DB.COL_CREATE_TIME)));
                note.put(Consts.DB.COL_MODIFY_TIME, c.getString(c.getColumnIndex(Consts.DB.COL_MODIFY_TIME)));
                noteArray.put(note);
            }
            noteReader.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            noteReader.endTransaction();
            noteReader.close();
        }

        if (noteArray == null || noteArray.length() == 0) {
            return EMPTY;
        } else {
            exportCount = noteArray.length();
            return FileUtils.save2json(noteArray.toString(),
                    Consts.PATH_NOTE_STR,
                    "notes_" + TimeUtils.getSimpleDateTime() + ".json")
                    ? SUCCESS : ERROR;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        loadingDialogFragment.dismiss();
        switch (status) {
            case EMPTY:
                Toast.makeText(mContext, "Nothing to export!", Toast.LENGTH_SHORT).show();
                break;
            case SUCCESS:
                Toast.makeText(mContext, "Export " + exportCount + " notes successfully!", Toast.LENGTH_SHORT).show();
                break;
            case ERROR:
                Toast.makeText(mContext, "Export occur IOException", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
