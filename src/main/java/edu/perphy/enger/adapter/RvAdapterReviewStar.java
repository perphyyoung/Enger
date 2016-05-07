package edu.perphy.enger.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.perphy.enger.R;
import edu.perphy.enger.data.Word;
import edu.perphy.enger.db.ReviewHelper;
import edu.perphy.enger.fragment.ReviewStarFragment.OnWordFragmentInteractionListener;

import static edu.perphy.enger.util.Consts.TAG;

public class RvAdapterReviewStar extends RecyclerView.Adapter<RvAdapterReviewStar.ViewHolder> {
    private final List<Word> mReviewList;
    private final ReviewHelper reviewHelper;
    private final OnWordFragmentInteractionListener mListener;

    public RvAdapterReviewStar(Context context, OnWordFragmentInteractionListener listener) {
        mListener = listener;
        reviewHelper = ReviewHelper.getInstance(context);
        mReviewList = new ArrayList<>();

        SQLiteDatabase reviewReader = reviewHelper.getReadableDatabase();
        reviewReader.beginTransaction();
        try (Cursor c = reviewReader.query(ReviewHelper.TABLE_NAME,
                null, null, null, null, null, ReviewHelper.COL_DATE_ADD)) {
            while (c.moveToNext()) {
                Word review = new Word();
                review.setWord(c.getString(c.getColumnIndex(ReviewHelper.COL_WORD)));
                review.setDef(c.getString(c.getColumnIndex(ReviewHelper.COL_DEF)));
                mReviewList.add(review);
            }
            reviewReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "RvAdapterReviewStar.RvAdapterReviewStar: ", e);
            e.printStackTrace();
        } finally {
            reviewReader.endTransaction();
            reviewReader.close();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_word, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Word review = mReviewList.get(position);
        holder.mItem = review;
        final String word = review.getWord();
        holder.tvWord.setText(word);
        holder.tvDef.setText(review.getDef());

        holder.ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase reviewWriter = reviewHelper.getWritableDatabase();
                reviewWriter.beginTransaction();
                try {
                    // query _id
                    String sql = "select " + ReviewHelper.COL_ID
                            + " from " + ReviewHelper.TABLE_NAME
                            + " where " + ReviewHelper.COL_WORD + " = ?";
                    SQLiteStatement stmt = reviewWriter.compileStatement(sql);
                    stmt.bindString(1, word);
                    long _id = stmt.simpleQueryForLong();
                    // delete
                    reviewWriter.delete(ReviewHelper.TABLE_NAME,
                            ReviewHelper.COL_ID + " = ?",
                            new String[]{_id + ""});
                    // update the ids which is greater than _id
                    sql = "update " + ReviewHelper.TABLE_NAME
                            + " set " + ReviewHelper.COL_ID + " = " + ReviewHelper.COL_ID + " - 1"
                            + " where " + ReviewHelper.COL_ID + " > ?";
                    stmt = reviewWriter.compileStatement(sql);
                    stmt.bindLong(1, _id);
                    stmt.executeUpdateDelete();
                    reviewWriter.setTransactionSuccessful();

                    mReviewList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } catch (Exception e) {
                    Log.e(TAG, "RvAdapterReviewStar.onClick: ", e);
                    e.printStackTrace();
                } finally {
                    reviewWriter.endTransaction();
                    reviewWriter.close();
                }
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onWordFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvWord, tvDef;
        private final ImageButton ibStar;
        private Word mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvWord = (TextView) view.findViewById(R.id.tvWord);
            tvDef = (TextView) view.findViewById(R.id.tvDef);
            ibStar = (ImageButton) view.findViewById(R.id.ibStar);
        }
    }
}
