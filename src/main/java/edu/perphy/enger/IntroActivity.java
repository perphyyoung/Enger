package edu.perphy.enger;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro;

import edu.perphy.enger.fragment.IntroFragment;

/**
 * Created by perphy on 2016/4/20 0020.
 * Intro
 */
public class IntroActivity extends AppIntro {
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        addSlide(IntroFragment.newInstance("Main Layout", R.drawable.main));
        addSlide(IntroFragment.newInstance("Drawer Menu", R.drawable.drawer));
        addSlide(IntroFragment.newInstance("Manage Dictionaries", R.drawable.dict));
        addSlide(IntroFragment.newInstance("Note List", R.drawable.note));
        addSlide(IntroFragment.newInstance("Note Detail", R.drawable.note_detail));
        addSlide(IntroFragment.newInstance("Daily Sentence", R.drawable.daily));
        addSlide(IntroFragment.newInstance("Star Lists", R.drawable.star));
        addSlide(IntroFragment.newInstance("Review Layout", R.drawable.review));
    }

    @Override
    public void onSkipPressed() {
        finish();
    }

    @Override
    public void onNextPressed() {
    }

    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onSlideChanged() {
    }
}
