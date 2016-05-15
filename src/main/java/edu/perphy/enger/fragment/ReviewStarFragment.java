package edu.perphy.enger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import edu.perphy.enger.R;
import edu.perphy.enger.adapter.RvAdapterReviewStar;
import edu.perphy.enger.data.Word;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnWordFragmentInteractionListener}
 * interface.
 */
public class ReviewStarFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnWordFragmentInteractionListener mListener;
    public RecyclerView rvReviewList;
    public TextView emptyList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReviewStarFragment() {
    }

    public static ReviewStarFragment newInstance(int columnCount) {
        ReviewStarFragment fragment = new ReviewStarFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_list, container, false);

        Context context = view.getContext();
        rvReviewList = (RecyclerView) view.findViewById(R.id.rvReviewList);
        emptyList = (TextView) view.findViewById(R.id.emptyList);
        if (mColumnCount <= 1) {
            rvReviewList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            rvReviewList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        rvReviewList.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(context).showLastDivider().build());
        rvReviewList.setHasFixedSize(true);
        rvReviewList.setAdapter(new RvAdapterReviewStar(this, mListener));
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWordFragmentInteractionListener) {
            mListener = (OnWordFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnWordFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnWordFragmentInteractionListener {
        void onWordFragmentInteraction(Word item);
    }
}
