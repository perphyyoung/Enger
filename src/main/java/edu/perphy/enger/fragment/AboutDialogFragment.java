package edu.perphy.enger.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.perphy.enger.R;

/**
 * Created by perphy on 2016/4/2 0002.
 * http://stackoverflow.com/questions/34936801/android-dialogfragment-no-title-feature-affects-layout
 * AboutDialogFragment
 */
public class AboutDialogFragment extends AppCompatDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_dialog, container);
    }
}
