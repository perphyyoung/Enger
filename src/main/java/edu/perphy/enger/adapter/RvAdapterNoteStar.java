package edu.perphy.enger.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.perphy.enger.R;
import edu.perphy.enger.data.Note;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.NoteStarFragment.OnNoteFragmentInteractionListener;
import edu.perphy.enger.util.Consts;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Note} and makes a call to the
 * specified {@link OnNoteFragmentInteractionListener}.
 */
public class RvAdapterNoteStar extends RecyclerView.Adapter<RvAdapterNoteStar.ViewHolder> {
    private final OnNoteFragmentInteractionListener mListener;
    private final NoteHelper noteHelper;
    private final ArrayList<Note> mNoteList;

    public RvAdapterNoteStar(Context context, OnNoteFragmentInteractionListener listener) {
        mListener = listener;
        noteHelper = NoteHelper.getInstance(context);
        mNoteList = new ArrayList<>();

        SQLiteDatabase noteReader = noteHelper.getReadableDatabase();
        noteReader.beginTransaction();
        try (Cursor c = noteReader.query(Consts.DB.TABLE_NOTE, null,
                Consts.DB.COL_STAR + " = ?", new String[]{1 + ""}, null, null, Consts.DB.COL_TITLE)) {
            while (c.moveToNext()) {
                Note note = new Note();
                note.setTitle(c.getString(c.getColumnIndex(Consts.DB.COL_TITLE)));
                note.setContent(c.getString(c.getColumnIndex(Consts.DB.COL_CONTENT)));
                note.setStarred(c.getString(c.getColumnIndex(Consts.DB.COL_STAR)));
                note.setModifyTime(c.getString(c.getColumnIndex(Consts.DB.COL_MODIFY_TIME)));
                mNoteList.add(note);
            }
            noteReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "RvAdapterNoteStar.RvAdapterNoteStar: ", e);
            e.printStackTrace();
        } finally {
            noteReader.endTransaction();
            noteReader.close();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_content, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Note note = mNoteList.get(position);
        final String title = note.getTitle();
        holder.mItem = note;
        holder.tvTitle.setText(title);
        holder.tvContent.setText(note.getContent());
        holder.tvModifyTime.setText(note.getModifyTime());
        holder.ibStar.setImageResource(TextUtils.equals(note.getStarred(), 1 + "")
                ? R.drawable.ic_star_black_24dp
                : R.drawable.ic_star_border_black_24dp);
        holder.ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                mNoteList.remove(pos);
                notifyItemRemoved(pos);

                SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
                noteWriter.beginTransaction();
                try {
                    String sql = "update " + Consts.DB.TABLE_NOTE
                            + " set " + Consts.DB.COL_STAR + " = 0 "
                            + " where " + Consts.DB.COL_TITLE + " = ?";
                    SQLiteStatement statement = noteWriter.compileStatement(sql);
                    statement.bindString(1, title);
                    statement.executeUpdateDelete();
                    noteWriter.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    noteWriter.endTransaction();
                    noteWriter.close();
                }
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onNoteFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView tvTitle, tvContent, tvModifyTime;
        private final ImageButton ibStar;
        private Note mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            tvContent = (TextView) view.findViewById(R.id.tvContent);
            tvModifyTime = (TextView) view.findViewById(R.id.tvModifyTime);
            ibStar = (ImageButton) view.findViewById(R.id.ibStar);
        }
    }
}
