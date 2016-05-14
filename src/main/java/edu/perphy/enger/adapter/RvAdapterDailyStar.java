package edu.perphy.enger.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import edu.perphy.enger.NoteDetailActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.data.Daily;
import edu.perphy.enger.db.DailyHelper;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.DailyStarFragment.OnDailyFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Daily} and makes a call to the
 * specified {@link OnDailyFragmentInteractionListener}.
 */
public class RvAdapterDailyStar extends RecyclerView.Adapter<RvAdapterDailyStar.ViewHolder> {
    private final Context mContext;
    private final OnDailyFragmentInteractionListener mListener;
    private final ArrayList<Daily> mDailyList;
    private final DailyHelper dailyHelper;

    public RvAdapterDailyStar(Context context, OnDailyFragmentInteractionListener listener) {
        mContext = context;
        mListener = listener;
        mDailyList = new ArrayList<>();
        dailyHelper = DailyHelper.getInstance(mContext);

        SQLiteDatabase dailyReader = dailyHelper.getReadableDatabase();
        dailyReader.beginTransaction();
        try (Cursor c = dailyReader.query(DailyHelper.TABLE_NAME, null,
                DailyHelper.COL_STAR + " = ?",
                new String[]{1 + ""}, null, null,
                DailyHelper.COL_DATE)) {
            while (c.moveToNext()) {
                Daily daily = new Daily();
                daily.setDate(c.getString(c.getColumnIndex(DailyHelper.COL_DATE)));
                daily.setEnglish(c.getString(c.getColumnIndex(DailyHelper.COL_ENGLISH)));
                daily.setChinese(c.getString(c.getColumnIndex(DailyHelper.COL_CHINESE)));
                daily.setStarred(TextUtils.equals(c.getString(c.getColumnIndex(DailyHelper.COL_STAR)), "1"));
                mDailyList.add(daily);
            }
            dailyReader.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dailyReader.endTransaction();
            dailyReader.close();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_daily, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Daily daily = mDailyList.get(position);
        holder.mItem = daily;
        holder.tvDate.setText(daily.getDate());
        holder.tvEnglish.setText(daily.getEnglish());
        holder.tvChinese.setText(daily.getChinese());
        holder.ibStar.setImageResource(R.drawable.ic_star_black_24dp);

        holder.ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Notice")
                        .setMessage("Unstar will delete this card from the database. " +
                                "Otherwise, you can make this card as a note. " +
                                "Do you still want to unstar?")
                        .setPositiveButton("Unstar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                                dailyWriter.beginTransaction();
                                try {
                                    dailyWriter.delete(DailyHelper.TABLE_NAME,
                                            DailyHelper.COL_DATE + " = ?",
                                            new String[]{daily.getDate()});
                                    dailyWriter.setTransactionSuccessful();
                                    int pos = holder.getAdapterPosition();
                                    mDailyList.remove(pos);
                                    notifyItemRemoved(pos);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    dailyWriter.endTransaction();
                                    dailyWriter.close();
                                }
                            }
                        }).setNegativeButton("Cancel", null)
                        .setNeutralButton("Make a note", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(mContext, NoteDetailActivity.class);
                                intent.putExtra(NoteHelper.COL_TOBE_SAVE, true);
                                intent.putExtra(NoteHelper.COL_TITLE, "day_" + daily.getDate());
                                intent.putExtra(NoteHelper.COL_CONTENT,
                                        daily.getEnglish() + "\n" + daily.getChinese());
                                mContext.startActivity(intent);
                            }
                        }).show();
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onDailyFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDailyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvDate, tvEnglish, tvChinese;
        private final ImageButton ibStar;
        public Daily mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvEnglish = (TextView) view.findViewById(R.id.tvEnglish);
            tvChinese = (TextView) view.findViewById(R.id.tvChinese);
            ibStar = (ImageButton) view.findViewById(R.id.ibStar);
        }
    }
}
