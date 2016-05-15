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
import edu.perphy.enger.adapter.RvAdapterDailyStar;
import edu.perphy.enger.data.Daily;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnDailyFragmentInteractionListener}
 * interface.
 */
public class DailyStarFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnDailyFragmentInteractionListener mListener;
    public RecyclerView rvDailyList;
    public TextView emptyList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DailyStarFragment() {
    }

    public static DailyStarFragment newInstance(int columnCount) {
        DailyStarFragment fragment = new DailyStarFragment();
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
        View view = inflater.inflate(R.layout.fragment_daily_list, container, false);

        Context context = view.getContext();
        rvDailyList = (RecyclerView) view.findViewById(R.id.rvDailyList);
        emptyList = (TextView) view.findViewById(R.id.emptyList);
        if (mColumnCount <= 1) {
            rvDailyList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            rvDailyList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        rvDailyList.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(context).showLastDivider().build());
        rvDailyList.setHasFixedSize(true);
        rvDailyList.setAdapter(new RvAdapterDailyStar(this, mListener));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDailyFragmentInteractionListener) {
            mListener = (OnDailyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDailyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnDailyFragmentInteractionListener {
        void onDailyFragmentInteraction(Daily daily);
    }
}
