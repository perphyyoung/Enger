package edu.perphy.enger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import edu.perphy.enger.fragment.ChartFragment;
import edu.perphy.enger.fragment.ReviewFragment;
import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.INTENT_TAB_POSITION;

public class ReviewActivity extends AppCompatActivity
        implements ReviewFragment.OnReviewFragmentInteractionListener,
        ChartFragment.OnChartFragmentInteractionListener {
    private Context mContext;
    private SharedPreferences settings;
    private int max_progress;
    private int pre_max_progress = 0;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        mContext = this;
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        pre_max_progress = Integer.parseInt(settings.getString(Consts.Setting.LP_MAX_WORD_COUNT, "30"));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        max_progress = Integer.parseInt(settings.getString(Consts.Setting.LP_MAX_WORD_COUNT, "30"));
        if (max_progress != pre_max_progress) {
            pre_max_progress = max_progress;
            new AlertDialog.Builder(mContext)
                    .setTitle("Notice")
                    .setMessage("You have just change the review schedule. " +
                            "Would you like to reload, or continue current review?")
                    .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recreate();
                        }
                    }).setNegativeButton("Continue", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (0 == mViewPager.getCurrentItem()) {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Notice")
                            .setMessage("Are You sure to reload?")
                            .setPositiveButton("Reload", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    recreate();
                                }
                            }).setNegativeButton("Cancel", null).show();
                }
                return true;
            case R.id.action_stars:
                Intent intent = new Intent(mContext, StarActivity.class);
                intent.putExtra(INTENT_TAB_POSITION, 1);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReviewFragmentInteraction(Uri uri) {

    }

    @Override
    public void onChartFragmentInteraction(Uri uri) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return ReviewFragment.newInstance(max_progress);
                default:
                    return ChartFragment.newInstance("", "");
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Review";
                case 1:
                    return "Chart";
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
