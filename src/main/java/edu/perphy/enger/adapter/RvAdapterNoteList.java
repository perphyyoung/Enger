package edu.perphy.enger.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tubb.smrv.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.HashSet;

import edu.perphy.enger.NoteDetailActivity;
import edu.perphy.enger.NoteListActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.data.Note;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.util.Toaster;

import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/4/7 0007.
 * 笔记列表的适配器
 */
public class RvAdapterNoteList
        extends RecyclerView.Adapter<RvAdapterNoteList.NoteListViewHolder> {
    private NoteListActivity act;
    private NoteHelper noteHelper;
    final private ArrayList<Note> mNoteList;
    private ActionMode mActionMode;
    private ActionMode.Callback mCallback;
    private HashSet<Integer> selectedSet;

    public RvAdapterNoteList(Context context) {
        this.act = (NoteListActivity) context;
        noteHelper = NoteHelper.getInstance(act);
        SQLiteDatabase noteReader = noteHelper.getReadableDatabase();
        mNoteList = new ArrayList<>();

        noteReader.beginTransaction();
        try (Cursor c = noteReader.query(NoteHelper.TABLE_NAME,
                null, null, null, null, null,
                NoteHelper.COL_MODIFY_TIME)) {
            while (c.moveToNext()) {
                Note n = new Note();
                n.setId(c.getLong(c.getColumnIndex(NoteHelper._ID)));
                n.setTitle(c.getString(c.getColumnIndex(NoteHelper.COL_TITLE)));
                n.setContent(c.getString(c.getColumnIndex(NoteHelper.COL_CONTENT)));
                n.setModifyTime(c.getString(c.getColumnIndex(NoteHelper.COL_MODIFY_TIME)));
                n.setStarred(c.getString(c.getColumnIndex(NoteHelper.COL_STAR)));
                mNoteList.add(n);
            }
            noteReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "RvAdapterNoteList.RvAdapterNoteList: ", e);
            e.printStackTrace();
        } finally {
            noteReader.endTransaction();
            noteReader.close();
        }

        updateView();

        selectedSet = new HashSet<>();
        setupActionModeCallback();
    }

    private void updateView() {
        if (mNoteList.isEmpty()) {
            act.rvNoteList.setVisibility(View.GONE);
            act.emptyList.setVisibility(View.VISIBLE);
        } else {
            act.rvNoteList.setVisibility(View.VISIBLE);
            act.emptyList.setVisibility(View.GONE);
        }
    }

    @Override
    public NoteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(final NoteListViewHolder holder, int position) {
        final Note n = mNoteList.get(position);
        final String title = n.getTitle();
        final String content = n.getContent();
        final String modifyTime = n.getModifyTime();
        boolean starred = TextUtils.equals(n.getStarred(), "1");

        holder.tvTitle.setText(title);
        holder.tvContent.setText(content);
        holder.tvModifyTime.setText(modifyTime);
        holder.ibStar.setImageResource(starred
                ? R.drawable.ic_star_black_24dp
                : R.drawable.ic_star_border_black_24dp);
        holder.setSelected(position);

        final SwipeMenuLayout itemView = (SwipeMenuLayout) holder.itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionMode == null) {
                    Intent intent = new Intent(act, NoteDetailActivity.class);
                    intent.putExtra(NoteHelper.COL_TITLE, title);
                    intent.putExtra(NoteHelper.COL_CONTENT, content);
                    act.startActivity(intent);
                } else {
                    addOrRemove(holder.getAdapterPosition());
                }
            }

            @SuppressWarnings("deprecation")
            private void addOrRemove(int position) {
                if (selectedSet.contains(position)) {
                    itemView.setBackgroundResource(0);
                    selectedSet.remove(position);
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_selected);
                    selectedSet.add(position);
                }
                if (selectedSet.size() == 0) {
                    mActionMode.finish();
                } else {
                    mActionMode.setTitle(selectedSet.size() + " selected");
                    notifyDataSetChanged();
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mActionMode == null) {
                    mActionMode = act.startSupportActionMode(mCallback);
                    itemView.setBackgroundResource(R.drawable.bg_selected);
                    selectedSet.add(holder.getAdapterPosition());
                }
                return true;
            }
        });
        holder.ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
                noteWriter.beginTransaction();
                try {
                    // 查询当前是否已经 star
                    String sql = "select " + NoteHelper.COL_STAR
                            + " from " + NoteHelper.TABLE_NAME
                            + " where " + NoteHelper.COL_TITLE + " = ?";
                    SQLiteStatement statement = noteWriter.compileStatement(sql);
                    statement.bindString(1, title);
                    boolean isStarred = statement.simpleQueryForLong() > 0;

                    ContentValues cv = new ContentValues(1);
                    cv.put(NoteHelper.COL_STAR, isStarred ? 0 : 1);

                    //notice change star to unstar, vice versa
                    noteWriter.update(NoteHelper.TABLE_NAME,
                            cv,
                            NoteHelper.COL_TITLE + " = ?",
                            new String[]{holder.tvTitle.getText().toString()});
                    noteWriter.setTransactionSuccessful();

                    //notice Initially, isStarred is false, then currentStar is true
                    boolean currentStar = !isStarred;
                    ((ImageButton) v).setImageResource(currentStar
                            ? R.drawable.ic_star_black_24dp
                            : R.drawable.ic_star_border_black_24dp);
                    new Toaster(act).showSingletonToast(currentStar ? "Star" : "Unstar");
                } catch (Exception e) {
                    Toast.makeText(act, "Failed to star or unstar", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } finally {
                    noteWriter.endTransaction();
                    noteWriter.close();
                }
            }
        });
        holder.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2016/4/7 0007  笔记信息
                Toast.makeText(act, "笔记信息", Toast.LENGTH_SHORT).show();
            }
        });
        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                updateView();

                SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
                noteWriter.beginTransaction();
                try {
                    noteWriter.delete(NoteHelper.TABLE_NAME,
                            NoteHelper.COL_TITLE + " = ?",
                            new String[]{title});
                    noteWriter.setTransactionSuccessful();
                    Snackbar.make(v, "Delete successfully.", Snackbar.LENGTH_INDEFINITE).show();
                } catch (Exception e) {
                    Toast.makeText(act, "delete err", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    noteWriter.endTransaction();
                    noteWriter.close();
                }
            }
        });
    }

    /**
     * http://blog.csdn.net/srtianxia/article/details/50369898
     */
    private void setupActionModeCallback() {
        mCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if (mActionMode == null) {
                    mActionMode = mode;
                    mode.getMenuInflater().inflate(R.menu.action_mode, menu);
                }
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        new AlertDialog.Builder(act)
                                .setTitle("Delete Notes")
                                .setMessage("Are your sure to delete " + selectedSet.size() + " note(s)." +
                                        " This action cannot be revert.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteNotes();
                                        mActionMode.finish();
                                    }
                                }).setNegativeButton("Cancel", null).show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
                selectedSet.clear();
                notifyDataSetChanged();
            }
        };
    }

    private void deleteNotes() {
        ArrayList<Note> toRemoveNoteList = new ArrayList<>(selectedSet.size());
        for (int position : selectedSet) {
            Note n = mNoteList.get(position);
            toRemoveNoteList.add(n);
        }
        ArrayList<Integer> toRemoveIdList = new ArrayList<>(toRemoveNoteList.size());
        for (Note n : toRemoveNoteList) {
            toRemoveIdList.add((int) n.getId());
            mNoteList.remove(n);
        }
        updateView();
        NoteHelper.delete(act, toRemoveIdList);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public class NoteListViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvContent, tvModifyTime;
        private ImageButton ibStar, ibInfo, ibDelete;

        public NoteListViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvContent = (TextView) itemView.findViewById(R.id.tvContent);
            tvModifyTime = (TextView) itemView.findViewById(R.id.tvModifyTime);
            ibStar = (ImageButton) itemView.findViewById(R.id.ibStar);
            ibInfo = (ImageButton) itemView.findViewById(R.id.ibInfo);
            ibDelete = (ImageButton) itemView.findViewById(R.id.ibDelete);
        }

        private void setSelected(int position) {
            if (selectedSet.contains(position)) {
                itemView.setBackgroundResource(R.drawable.bg_selected);
            } else {
                itemView.setBackgroundResource(0);
            }
        }
    }
}
