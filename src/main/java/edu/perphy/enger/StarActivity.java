package edu.perphy.enger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import edu.perphy.enger.data.Daily;
import edu.perphy.enger.data.Note;
import edu.perphy.enger.data.Word;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.DailyStarFragment;
import edu.perphy.enger.fragment.NoteStarFragment;
import edu.perphy.enger.fragment.ReviewStarFragment;

import static edu.perphy.enger.util.Consts.INTENT_TAB_POSITION;

public class StarActivity extends AppCompatActivity
        implements NoteStarFragment.OnNoteFragmentInteractionListener,
        ReviewStarFragment.OnWordFragmentInteractionListener,
        DailyStarFragment.OnDailyFragmentInteractionListener {
    private Context mContext;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);

        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(mViewPager);

            Intent intent = getIntent();
            int pos = intent.getIntExtra(INTENT_TAB_POSITION, 0);
            tabLayout.getTabAt(pos).select();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_star, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNoteFragmentInteraction(Note note) {
        Intent intent = new Intent(mContext, NoteDetailActivity.class);
        intent.putExtra(NoteHelper.COL_TITLE, note.getTitle());
        intent.putExtra(NoteHelper.COL_CONTENT, note.getContent());
        startActivity(intent);
    }

    @Override
    public void onDailyFragmentInteraction(Daily daily) {
    }

    @Override
    public void onWordFragmentInteraction(Word item) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * getItem is called to instantiate the fragment for the given page.
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return NoteStarFragment.newInstance(1);//notice 参数为List的列数
                case 1:
                    return ReviewStarFragment.newInstance(1);
                default:
                    return DailyStarFragment.newInstance(1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Notes";
                case 1:
                    return "Review";
                case 2:
                    return "Daily";
                default:
                    return null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!onSupportNavigateUp()) {
            super.onBackPressed();
        }
    }
}
