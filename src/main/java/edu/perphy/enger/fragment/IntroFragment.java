package edu.perphy.enger.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.perphy.enger.R;

public class IntroFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DRAWABLE = "drawable";

    private String title;
    private int drawable;

    public IntroFragment() {
    }

    public static IntroFragment newInstance(String title, int imageDrawer) {
        IntroFragment fragment = new IntroFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_DRAWABLE, imageDrawer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            drawable = getArguments().getInt(ARG_DRAWABLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro, container, false);
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        ImageView ivDrawable = (ImageView) v.findViewById(R.id.ivDrawable);
        ivDrawable.setImageResource(drawable);
        return v;
    }

}
