package edu.perphy.enger;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.material.widget.FloatingEditText;

import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.TimeUtils;
import edu.perphy.enger.util.NullUtils;
import edu.perphy.enger.util.Toaster;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

public class NoteDetailActivity extends AppCompatActivity {
    private static final int CHANGE_NOTHING = 0;
    private static final int CHANGE_TITLE = 1;
    private static final int CHANGE_CONTENT = 2;
    private static final int CHANGE_BOTH = 3;
    private static final int EMPTY_TITLE = 4;
    private boolean somethingChanged = false;
    private boolean tobeSave;
    private Context mContext;
    private NoteHelper noteHelper;
    private SQLiteDatabase noteWriter;
    //    private boolean readOnly;
    private FloatingEditText fetTitle, fetContent;
    private String title, content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        mContext = this;
        noteHelper = NoteHelper.getInstance(mContext);
        fetTitle = (FloatingEditText) findViewById(R.id.fetTitle);
        fetContent = (FloatingEditText) findViewById(R.id.fetContent);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        tobeSave = intent.getBooleanExtra(Consts.DB.COL_TOBE_SAVE, false);
        title = NullUtils.null2empty(intent.getStringExtra(Consts.DB.COL_TITLE));
        content = NullUtils.null2empty(intent.getStringExtra(Consts.DB.COL_CONTENT));

        fetTitle.setText(title);
        fetContent.setText(content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                insertIntoNote(detectChangeType());
                return true;
            case R.id.action_clear: // 清空
                fetTitle.setText("");
                fetContent.setText("");
                return true;
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 检测标题和内容是否有变化
     */
    private int detectChangeType() {
        if (tobeSave) return CHANGE_BOTH;
        String newTitle = fetTitle.getText().toString().trim();
        String newContent = fetContent.getText().toString().trim();
        if (TextUtils.isEmpty(newTitle) && !TextUtils.isEmpty(newContent)) {
            new Toaster(mContext).showCenterToast("Title cannot be empty!");
            return EMPTY_TITLE;
        }

        boolean titleChanged = !TextUtils.equals(title, newTitle);
        boolean contentChanged = !TextUtils.equals(content, newContent);

        if (titleChanged && contentChanged) {
            somethingChanged = true;
            return CHANGE_BOTH;
        } else if (contentChanged) {
            somethingChanged = true;
            return CHANGE_CONTENT;
        } else if (titleChanged) {
            somethingChanged = true;
            return CHANGE_TITLE;
        } else {
            somethingChanged = false;
            return CHANGE_NOTHING;
        }
    }

    private void insertIntoNote(int changeType) {
        String newTitle = fetTitle.getText().toString().trim();
        String newContent = fetContent.getText().toString().trim();
        switch (changeType) {
            case EMPTY_TITLE:
                return;
            case CHANGE_NOTHING:
                break;
            case CHANGE_BOTH:
                if (!insertNewToNote()) {
                    new Toaster(mContext).showCenterToast("Notes cannot have identical TITLE!");
                    return;
                }
                break;
            case CHANGE_TITLE:
                if (!updateSpecificNote(newTitle, CHANGE_TITLE)) {
                    new Toaster(mContext).showCenterToast("Notes cannot have identical TITLE!");
                    return;
                }
                break;
            case CHANGE_CONTENT:
                if (!updateSpecificNote(newContent, CHANGE_CONTENT)) {
                    new Toaster(mContext).showCenterToast("Fail to update the content.");
                    return;
                }
                break;
        }
        startActivity(new Intent(mContext, NoteListActivity.class));
        finish();
    }

    /**
     * 更新特定的笔记
     *
     * @param newStr     新的title或者content
     * @param changeType 要更改的类型
     * @return 是否更改成功
     */
    private boolean updateSpecificNote(String newStr, int changeType) {
        noteWriter = noteHelper.getWritableDatabase();
        noteWriter.beginTransaction();

        try {
            String sql = "select " + Consts.DB._ID
                    + " from " + Consts.DB.TABLE_NOTE
                    + " where " + Consts.DB.COL_TITLE + " = ?";
            SQLiteStatement statement = noteWriter.compileStatement(sql);
            statement.bindString(1, title);
            int id = (int) statement.simpleQueryForLong();

            ContentValues cv = new ContentValues(2);
            cv.put(changeType == CHANGE_CONTENT ? Consts.DB.COL_CONTENT : Consts.DB.COL_TITLE, newStr);
            cv.put(Consts.DB.COL_MODIFY_TIME, TimeUtils.getSimpleDateTime());
            noteWriter.update(Consts.DB.TABLE_NOTE, cv, Consts.DB._ID + "= ?", new String[]{id + ""});
            noteWriter.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "NoteDetailActivity.updateSpecificNote: update err", e);
            e.printStackTrace();
            return false;
        } finally {
            noteWriter.endTransaction();
            noteWriter.close();
        }
        return true;
    }

    private boolean insertNewToNote() {
        noteWriter = noteHelper.getWritableDatabase();
        noteWriter.beginTransaction();

        String simpleDateTime = TimeUtils.getSimpleDateTime();
        try {
            ContentValues cv = new ContentValues(4);
            cv.put(Consts.DB.COL_TITLE, fetTitle.getText().toString().trim());
            cv.put(Consts.DB.COL_CONTENT, fetContent.getText().toString().trim());
            cv.put(Consts.DB.COL_CREATE_TIME, simpleDateTime);
            cv.put(Consts.DB.COL_MODIFY_TIME, simpleDateTime);

            noteWriter.insertOrThrow(Consts.DB.TABLE_NOTE, null, cv);
            noteWriter.setTransactionSuccessful();
        } catch (SQLException e) {
            if (DEBUG) Log.e(TAG, "WordDetailActivity.insertIntoDetailDb: insert into note err", e);
            return false;
        } finally {
            noteWriter.endTransaction();
            noteWriter.close();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        final int changeType = detectChangeType();
        if (changeType == EMPTY_TITLE) return;
        if (tobeSave || somethingChanged) {
            new AlertDialog.Builder(mContext)
                    .setMessage("You have unsaved change.")
                    .setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            insertIntoNote(tobeSave ? CHANGE_BOTH : changeType);
                        }
                    }).setNeutralButton(R.string.action_cancel, null)
                    .setNegativeButton(R.string.action_discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        } else {
            super.onBackPressed();
        }
    }
}
