package edu.perphy.enger.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import edu.perphy.enger.data.Review;
import edu.perphy.enger.db.ReviewHelper;
import edu.perphy.enger.fragment.WordFragment.OnWordFragmentInteractionListener;

import static edu.perphy.enger.util.Consts.TAG;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Review} and makes a call to the
 * specified {@link OnWordFragmentInteractionListener}.
 */
public class RvAdapterWordStar extends RecyclerView.Adapter<RvAdapterWordStar.ViewHolder> {
    private final List<Review> mReviewList;
    private final ReviewHelper reviewHelper;
    private final OnWordFragmentInteractionListener mListener;

    public RvAdapterWordStar(Context context, OnWordFragmentInteractionListener listener) {
        mListener = listener;
        reviewHelper = new ReviewHelper(context);
        mReviewList = new ArrayList<>();

        SQLiteDatabase reviewReader = reviewHelper.getReadableDatabase();
        reviewReader.beginTransaction();
        try (Cursor c = reviewReader.query(ReviewHelper.TABLE_NAME,
                null, null, null, null, null, ReviewHelper.COL_DATE_ADD)) {
            while (c.moveToNext()) {
                Review review = new Review();
                review.setWord(c.getString(c.getColumnIndex(ReviewHelper.COL_WORD)));
                review.setDef(c.getString(c.getColumnIndex(ReviewHelper.COL_DEF)));
                mReviewList.add(review);
            }
            reviewReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "RvAdapterWordStar.RvAdapterWordStar: ", e);
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
        final Review review = mReviewList.get(position);
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
                    reviewWriter.delete(ReviewHelper.TABLE_NAME,
                            ReviewHelper.COL_WORD + " = ?",
                            new String[]{word});
                    reviewWriter.setTransactionSuccessful();
                    mReviewList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } catch (Exception e) {
                    Log.e(TAG, "RvAdapterWordStar.onClick: ", e);
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
        private Review mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvWord = (TextView) view.findViewById(R.id.tvWord);
            tvDef = (TextView) view.findViewById(R.id.tvDef);
            ibStar = (ImageButton) view.findViewById(R.id.ibStar);
        }
    }
}
