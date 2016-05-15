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
import edu.perphy.enger.adapter.RvAdapterNoteStar;
import edu.perphy.enger.data.Note;

/**
 * A fragment representing a list of Items.
 * <br/>
 * Activities containing this fragment MUST implement the {@link OnNoteFragmentInteractionListener}
 * interface.
 */
public class NoteStarFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnNoteFragmentInteractionListener mListener;
    public RecyclerView rvNoteList;
    public TextView emptyList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteStarFragment() {
    }

    public static NoteStarFragment newInstance(int columnCount) {
        NoteStarFragment fragment = new NoteStarFragment();
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
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        Context context = view.getContext();
        rvNoteList = (RecyclerView) view.findViewById(R.id.rvNoteList);
        emptyList = (TextView) view.findViewById(R.id.emptyList);
        if (mColumnCount <= 1) {
            rvNoteList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            rvNoteList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        rvNoteList.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(context).showLastDivider().build());
        rvNoteList.setHasFixedSize(true);
        rvNoteList.setAdapter(new RvAdapterNoteStar(this, mListener));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNoteFragmentInteractionListener) {
            mListener = (OnNoteFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNoteFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnNoteFragmentInteractionListener {
        void onNoteFragmentInteraction(Note note);
    }
}
