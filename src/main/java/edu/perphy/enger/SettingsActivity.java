package edu.perphy.enger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import edu.perphy.enger.util.Consts;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Toolbar mToolbar;
    private EditTextPreference etpName, etpEmail;
    private ListPreference lpMaxWordCount;
    private CheckBoxPreference cbpWifiOnly;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        etpName = (EditTextPreference) findPreference(getString(R.string.etpName));
        etpName.setSummary(etpName.getText());

        etpEmail = (EditTextPreference) findPreference(getString(R.string.etpEmail));
        etpEmail.setSummary(etpEmail.getText());

        lpMaxWordCount = (ListPreference) findPreference(getString(R.string.lpMaxWordCount));
        lpMaxWordCount.setSummary(getString(R.string.lpMaxWordCountSummary, Integer.parseInt(lpMaxWordCount.getValue())));

        cbpWifiOnly = (CheckBoxPreference) findPreference(getString(R.string.cbpWifiOnly));
        cbpWifiOnly.setSummaryOn(R.string.summary_wifi_only);
        cbpWifiOnly.setSummaryOff(R.string.summary_wifi_both);
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.activity_settings, new LinearLayout(this), false);

        mToolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        mToolbar.setTitle(getTitle());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        // Allow super to try and create a view first
        final View result = super.onCreateView(name, context, attrs);
        if (result != null) {
            return result;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // If we're running pre-L, we need to 'inject' our tint aware Views in place of the
            // standard framework versions
            switch (name) {
                case "EditText":
                    return new AppCompatEditText(this, attrs);
                case "Spinner":
                    return new AppCompatSpinner(this, attrs);
                case "CheckBox":
                    return new AppCompatCheckBox(this, attrs);
                case "RadioButton":
                    return new AppCompatRadioButton(this, attrs);
                case "CheckedTextView":
                    return new AppCompatCheckedTextView(this, attrs);
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        switch (key) {
            case Consts.Setting.ETP_NAME:
                etpName.setSummary(etpName.getText());
                break;
            case Consts.Setting.ETP_EMAIL:
                etpEmail.setSummary(etpEmail.getText());
                break;
            case Consts.Setting.LP_MAX_WORD_COUNT:
                lpMaxWordCount.setSummary(getString(R.string.lpMaxWordCountSummary, Integer.parseInt(lpMaxWordCount.getValue())));
                break;
            case Consts.Setting.CBP_WIFI_ONLY:
                break;
        }
    }
}
