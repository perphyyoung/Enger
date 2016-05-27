package edu.perphy.enger.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.perphy.enger.R;

public class BaseDialogFragment extends AppCompatDialogFragment {
    private static final String ARG_LAYOUT_ID = "layout_id";
    private int layoutId;

    // Required empty public constructor
    public BaseDialogFragment() {
    }

    public static BaseDialogFragment newInstance(int layoutId) {
        BaseDialogFragment fragment = new BaseDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_ID, layoutId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            layoutId = getArguments().getInt(ARG_LAYOUT_ID);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layoutId, container);
    }
}
