package edu.perphy.enger.thread;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import edu.perphy.enger.NoteListActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.adapter.RvAdapterNoteList;
import edu.perphy.enger.data.Note;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.BaseDialogFragment;
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
    private NoteListActivity act;
    private ArrayList<Note> mNoteList;
    private NoteHelper noteHelper;
    BaseDialogFragment dialog;
    private int importCount = 0;

    public ImportNoteTask(Context context) {
        act = (NoteListActivity) context;
        mNoteList = new ArrayList<>();
        noteHelper = NoteHelper.getInstance(act);
    }

    @Override
    protected void onPreExecute() {
        dialog = BaseDialogFragment.newInstance(R.layout.fragment_loading_dialog);
        dialog.show(act.getSupportFragmentManager(), TAG_LOADING_DIALOG);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        File rootNotePath = new File(Consts.PATH_NOTE_STR);
        // 创建笔记的根目录
        if (FileUtils.createDir(rootNotePath)) {
            File[] jsonFiles = rootNotePath.listFiles(new FileUtils.PostfixFileFilter("json", true));
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
        dialog.dismiss();
        switch (status) {
            case EMPTY:
                Toast.makeText(act, "Nothing to import!", Toast.LENGTH_SHORT).show();
                break;
            case SUCCESS:
                Toast.makeText(act, "Import " + importCount + " notes successfully!", Toast.LENGTH_SHORT).show();
                act.rvNoteList.setAdapter(new RvAdapterNoteList(act));
                break;
            case ERROR_JSON:
                Toast.makeText(act, "Not a valid json file for note", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_SQL:
                Toast.makeText(act, "Error occur when insert into database", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_IO:
                Toast.makeText(act, "Error occur with IO", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean insert2note() {
        SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
        noteWriter.beginTransaction();
        try {
            for (Note n : mNoteList) {
                ContentValues cv = new ContentValues();
                cv.put(NoteHelper.COL_TITLE, n.getTitle());
                cv.put(NoteHelper.COL_CONTENT, n.getContent());
                cv.put(NoteHelper.COL_STAR, n.getStarred());
                cv.put(NoteHelper.COL_CREATE_TIME, n.getCreateTime());
                cv.put(NoteHelper.COL_MODIFY_TIME, n.getModifyTime());
                noteWriter.insertWithOnConflict(NoteHelper.TABLE_NAME,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE); // ignore (not insert or change) if conflict occur
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
