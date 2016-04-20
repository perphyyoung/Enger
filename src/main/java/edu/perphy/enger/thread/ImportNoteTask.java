package edu.perphy.enger.thread;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import edu.perphy.enger.data.Note;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.LoadingDialogFragment;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.FileUtils;

import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;

/**
 * Created by perphy on 2016/4/11 0011.
 * 导入笔记
 */
public class ImportNoteTask extends AsyncTask<Void, Void, Integer> {
    private final int EMPTY = 1;
    private final int SUCCESS = 2;
    private final int ERROR_JSON = 3;
    private final int ERROR_SQL = 4;
    private final int ERROR_IO = 5;
    private Context mContext;
    private ArrayList<Note> mNoteList;
    private NoteHelper noteHelper;
    LoadingDialogFragment loadingDialogFragment;
    private int importCount = 0;

    public ImportNoteTask(Context context) {
        mContext = context;
        mNoteList = new ArrayList<>();
        noteHelper = NoteHelper.getInstance(mContext);
    }

    @Override
    protected void onPreExecute() {
        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), TAG_LOADING_DIALOG);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        File rootNotePath = new File(Consts.PATH_NOTE_STR);
        // 创建笔记的根目录
        if (FileUtils.createDir(rootNotePath)) {
            File[] jsonFiles = rootNotePath.listFiles(new FileUtils.PostfixFileFilter("json"));
            if (jsonFiles.length == 0) {
                return EMPTY;
            } else {
                try {
                    String data = FileUtils.get5json(jsonFiles[0]);
                    JSONArray array = new JSONArray(data); // null to throw json exception
                    importCount = array.length();
                    for (int i = 0; i < importCount; ++i) {
                        Note note = new Note((JSONObject) array.get(i));
                        mNoteList.add(note);
                    }
                    return insert2note() ? SUCCESS : ERROR_SQL;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return ERROR_JSON;
                }
            }
        } else {
            return ERROR_IO;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        loadingDialogFragment.dismiss();
        switch (status) {
            case EMPTY:
                Toast.makeText(mContext, "Nothing to import!", Toast.LENGTH_SHORT).show();
                break;
            case SUCCESS:
                Toast.makeText(mContext, "Import " + importCount + " notes successfully!", Toast.LENGTH_SHORT).show();
                ((AppCompatActivity)mContext).recreate();
                break;
            case ERROR_JSON:
                Toast.makeText(mContext, "Not a valid json file for note", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_SQL:
                Toast.makeText(mContext, "Error occur when insert into database", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_IO:
                Toast.makeText(mContext, "Error occur with IO", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean insert2note() {
        SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
        try {
            noteWriter.beginTransaction();
            String sql = "insert into " + Consts.DB.TABLE_NOTE
                    + " ( " + Consts.DB.COL_TITLE + ", "
                    + Consts.DB.COL_CONTENT + ", "
                    + Consts.DB.COL_STAR + ", "
                    + Consts.DB.COL_CREATE_TIME + ", "
                    + Consts.DB.COL_MODIFY_TIME + " ) values (?, ?, ?, ?, ? )";
            SQLiteStatement statement = noteWriter.compileStatement(sql);
            for (Note note : mNoteList) {
                statement.bindAllArgsAsStrings(new String[]
                        {note.getTitle(), note.getContent(), note.getStarred(), note.getCreateTime(), note.getModifyTime()});
                statement.executeInsert();
            }
            noteWriter.setTransactionSuccessful();
            return true;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            noteWriter.endTransaction();
            noteWriter.close();
        }
    }
}
