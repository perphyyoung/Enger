package edu.perphy.enger.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tubb.smrv.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.HashMap;

import edu.perphy.enger.NoteDetailActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.Toaster;

/**
 * Created by perphy on 2016/4/7 0007.
 * 笔记列表的适配器
 */
public class RvAdapterNoteList
        extends RecyclerView.Adapter<RvAdapterNoteList.NoteListViewHolder> {
    private Context mContext;
    private NoteHelper noteHelper;
    final private ArrayList<HashMap<String, String>> mNoteList;

    public RvAdapterNoteList(Context context) {
        this.mContext = context;
        noteHelper = NoteHelper.getInstance(mContext);
        SQLiteDatabase noteReader = noteHelper.getReadableDatabase();
        mNoteList = new ArrayList<>();

        try (Cursor c = noteReader.query(Consts.DB.TABLE_NOTE,
                null, null, null, null, null,
                Consts.DB.COL_MODIFY_TIME)) {
            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndex(Consts.DB.COL_TITLE));
                String content = c.getString(c.getColumnIndex(Consts.DB.COL_CONTENT));
                String modifyTime = c.getString(c.getColumnIndex(Consts.DB.COL_MODIFY_TIME));
                String starred = c.getString(c.getColumnIndex(Consts.DB.COL_STAR));

                HashMap<String, String> hm = new HashMap<>(4);
                hm.put(Consts.DB.COL_TITLE, title);
                hm.put(Consts.DB.COL_CONTENT, content);
                hm.put(Consts.DB.COL_MODIFY_TIME, modifyTime);
                hm.put(Consts.DB.COL_STAR, starred);
                mNoteList.add(hm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            noteReader.close();
        }
    }

    @Override
    public NoteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NoteListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(final NoteListViewHolder holder, int position) {
        final HashMap<String, String> hm = mNoteList.get(position);
        final String title = hm.get(Consts.DB.COL_TITLE);
        final String content = hm.get(Consts.DB.COL_CONTENT);
        final String modifyTime = hm.get(Consts.DB.COL_MODIFY_TIME);
        boolean starred = TextUtils.equals(hm.get(Consts.DB.COL_STAR), "1");

        holder.tvTitle.setText(title);
        holder.tvContent.setText(content);
        holder.tvModifyTime.setText(modifyTime);
        holder.ibStar.setImageResource(starred ? R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp);

        SwipeMenuLayout itemView = (SwipeMenuLayout) holder.itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NoteDetailActivity.class);
//                intent.putExtra(Consts.NOTE_READ_ONLY, true);
                intent.putExtra(Consts.DB.COL_TITLE, title);
                intent.putExtra(Consts.DB.COL_CONTENT, content);
                mContext.startActivity(intent);
            }
        });
        holder.ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
                noteWriter.beginTransaction();
                try {
                    // 查询当前是否已经 star
                    String sql = "select " + Consts.DB.COL_STAR
                            + " from " + Consts.DB.TABLE_NOTE
                            + " where " + Consts.DB.COL_TITLE + " = ?";
                    SQLiteStatement statement = noteWriter.compileStatement(sql);
                    statement.bindString(1, title);
                    boolean isStarred = statement.simpleQueryForLong() > 0;

                    ContentValues cv = new ContentValues(1);
                    cv.put(Consts.DB.COL_STAR, isStarred ? 0 : 1);

                    //notice change star to unstar, vice versa
                    noteWriter.update(Consts.DB.TABLE_NOTE,
                            cv,
                            Consts.DB.COL_TITLE + " = ?",
                            new String[]{holder.tvTitle.getText().toString()});
                    noteWriter.setTransactionSuccessful();

                    //notice Initially, isStarred is false, then currentStar is true
                    boolean currentStar = !isStarred;
                    ((ImageButton) v).setImageResource(currentStar
                            ? R.drawable.ic_star_black_24dp
                            : R.drawable.ic_star_border_black_24dp);
                    new Toaster(mContext).showSingletonToast(currentStar ? "Star" : "Unstar");
                } catch (Exception e) {
                    Toast.makeText(mContext, "Failed to star or unstar", Toast.LENGTH_LONG).show();
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
                Toast.makeText(mContext, "笔记信息", Toast.LENGTH_SHORT).show();
            }
        });
        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteList.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());

                SQLiteDatabase noteWriter = noteHelper.getWritableDatabase();
                noteWriter.beginTransaction();
                try {
                    noteWriter.delete(Consts.DB.TABLE_NOTE,
                            Consts.DB.COL_TITLE + " = ?",
                            new String[]{title});
                    noteWriter.setTransactionSuccessful();
                    Snackbar.make(v, "Delete successfully.", Snackbar.LENGTH_INDEFINITE).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, "delete err", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } finally {
                    noteWriter.endTransaction();
                    noteWriter.close();
                }
            }
        });
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
    }
}
