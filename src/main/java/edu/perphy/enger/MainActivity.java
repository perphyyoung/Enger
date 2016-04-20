package edu.perphy.enger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.perphy.enger.adapter.WordAutoCompleteArrayAdapter;
import edu.perphy.enger.data.OxfordHelper;
import edu.perphy.enger.fragment.AboutDialogFragment;
import edu.perphy.enger.thread.UpdateDefinitionTask;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.FileUtils;
import edu.perphy.enger.widget.XAutoCompleteTextView;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MAX_HISTORY_LENGTH = 100;
    private static final int INTENT_AVATAR = 17;
    private Context mContext;
    private SharedPreferences sp;
    ArrayList<String> wordList;
    private WordAutoCompleteArrayAdapter mNormalAdapter;

    private Toolbar toolbar;
    private View headerView;
    private CircleImageView civAvatar;
    private TextView tvName, tvEmail;
    private InputMethodManager imm;
    private DrawerLayout mDrawer;

    private long lastClickBackTime = 0; // 上次点击返回键的时间，用于控制返回键的行为
    private String lastWord;
    private TextToSpeech tts;

    private XAutoCompleteTextView mactv;
    private WebView wvNothingToShow;
    private RecyclerView rvDefinitionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        sp = getSharedPreferences(Consts.SP_NAME, MODE_PRIVATE);

        //判断是不是首次运行
        if (sp.getBoolean(Consts.SP_FIRST_RUN, true)) {
            // 创建词典的根目录
            if (!FileUtils.createDir(new File(Consts.ROOT_PATH_STR + File.separator + "dic"))) {
                Toast.makeText(mContext, "External storage is not writable", Toast.LENGTH_LONG).show();
            }

            startActivity(new Intent(mContext, IntroActivity.class));
            sp.edit().putBoolean(Consts.SP_FIRST_RUN, false).apply();
        }

        initialToolbar();
        initialViews();
        initialTextToSpeech();
    }

    private void initialToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initialViews() {
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        initialNavigationView();
        initialHeaderView();
        initialFab();
        initialCustomViews();
    }

    private void initialNavigationView() {
        // 监听导航视图
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
            headerView = mNavigationView.getHeaderView(0);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                mDrawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (imm.isActive()) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initialHeaderView() {
        // 用户信息
        civAvatar = (CircleImageView) headerView.findViewById(R.id.civAvatar);
        String avatarStr = sp.getString(Consts.SP_AVATAR, null);
        if(avatarStr != null) {
            Picasso.with(mContext)
                    .load(avatarStr)
                    .into(civAvatar);
        }
        civAvatar.setClickable(true);
        civAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, INTENT_AVATAR);
            }
        });

        tvName = (TextView) headerView.findViewById(R.id.tvName);
        tvEmail = (TextView) headerView.findViewById(R.id.tvEmail);

        // 夜晚模式
        final boolean notNightMode = sp.getBoolean(Consts.SP_NOT_NIGHT_MODE, true);
        final ImageButton ibNightMode = (ImageButton) headerView.findViewById(R.id.ibNightMode);
        ibNightMode.setImageResource(notNightMode
                ? R.drawable.ic_crescent
                : R.drawable.ic_sun_black);
        ibNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notNightMode) {// 当前不是夜晚模式，要设置为夜晚模式
                    //noinspection WrongConstant Could be compiler error
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    ibNightMode.setImageResource(R.drawable.ic_sun_black);
                    sp.edit().putBoolean(Consts.SP_NOT_NIGHT_MODE, false).apply();
                    mDrawer.closeDrawer(GravityCompat.START);
                    Log.i(TAG, "MainActivity.onClick: 改为夜晚模式");
                    recreate();
                } else {
                    //noinspection WrongConstant Could be compiler error
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    ibNightMode.setImageResource(R.drawable.ic_crescent);
                    sp.edit().putBoolean(Consts.SP_NOT_NIGHT_MODE, true).apply();
                    mDrawer.closeDrawer(GravityCompat.START);
                    Log.i(TAG, "MainActivity.onClick: 取消夜晚模式");
                    recreate();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == INTENT_AVATAR) {
            Uri imageUri = data.getData();
            sp.edit().putString(Consts.SP_AVATAR, imageUri.toString()).apply();
            Picasso.with(mContext)
                    .load(imageUri)
                    .into(civAvatar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        tvName.setText(settings.getString(Consts.Setting.ETP_NAME, getString(R.string.name)));
        tvEmail.setText(settings.getString(Consts.Setting.ETP_EMAIL, getString(R.string.email)));
    }

    private void initialFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }
                    // 单词详情界面
                    Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                    intent.putExtra(Consts.DB.COL_TOBE_SAVE, true);
                    intent.putExtra(Consts.DB.COL_TITLE, mactv.getText().toString().trim());
                    startActivity(intent);
                }
            });
        }
    }

    private void initialCustomViews() {
        mactv = (XAutoCompleteTextView) findViewById(R.id.mactv);
        mactv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (EditorInfo.IME_ACTION_SEARCH == actionId) {
                    final String word = mactv.getText().toString().trim();
                    mactv.dismissDropDown();
                    executeQuery(word);
                    handled = true;
                }
                return handled;
            }
        });
        new InitialAutoCompleteTextViewTask().execute();

        wvNothingToShow = (WebView) findViewById(R.id.wvNothingToShow);
        rvDefinitionContainer = (RecyclerView) findViewById(R.id.rvDefinitionContainer);
    }


    private class InitialAutoCompleteTextViewTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            initialWordList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            initialAutoCompleteTextView();
        }
    }

    private void initialAutoCompleteTextView() {
        mactv.setDrawableLeftListener(new XAutoCompleteTextView.DrawableLeftListener() {
            @Override
            public void onDrawableLeftClick(View view) {
                mactv.setText("");// fixme: 2016/4/2 0002 auto focus, seem to be a bug
                String longHistory = getSharedPreferences(Consts.SP_NAME, MODE_PRIVATE).getString(Consts.SP_HISTORY, "");
                String[] limitedHistory, history = longHistory.split(",");
                int historyLength = history.length;
                if (history.length > MAX_HISTORY_LENGTH) {
                    limitedHistory = new String[MAX_HISTORY_LENGTH];
                    System.arraycopy(history, 0, limitedHistory, 0, MAX_HISTORY_LENGTH);
                } else {
                    limitedHistory = new String[historyLength];
                    System.arraycopy(history, 0, limitedHistory, 0, historyLength);
                }
                final ArrayAdapter<String> mHistoryAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        limitedHistory);
                mactv.setAdapter(mHistoryAdapter);
                mactv.setCompletionHint("Recent search history");
                mactv.showDropDown();
            }
        });

        mNormalAdapter = new WordAutoCompleteArrayAdapter(
                mContext,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                wordList);
        mactv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mactv.setAdapter(mNormalAdapter);
                mactv.setCompletionHint("Related word of phrase");
            }
        });

        mactv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word = ((TextView) view).getText().toString();
                executeQuery(word);
            }
        });
    }

    /**
     * 初始化AutoCompleteTextView的数据集
     */
    private void initialWordList() {
        wordList = new ArrayList<>(OxfordHelper.WORD_COUNT);
        OxfordHelper oxfordHelper = new OxfordHelper(mContext);
        SQLiteDatabase oxfordReader = oxfordHelper.getReadableDatabase();
        oxfordReader.beginTransaction();
        try (Cursor c = oxfordReader.query(OxfordHelper.DATABASE_NAME,
                new String[]{OxfordHelper.COL_WORD},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                wordList.add(c.getString(c.getColumnIndex(OxfordHelper.COL_WORD)));
            }
            oxfordReader.setTransactionSuccessful();
        }catch (Exception e){
            Log.e(TAG, "MainActivity.initialWordList: ", e);
            e.printStackTrace();
        }finally {
            oxfordReader.endTransaction();
            oxfordReader.close();
        }
    }

    private void saveSearchHistory(String word) {
        if (!TextUtils.isEmpty(word)) {
            SharedPreferences sp = getSharedPreferences(Consts.SP_NAME, MODE_PRIVATE);
            String longHistory = sp.getString(Consts.SP_HISTORY, "");
            StringBuilder sb = new StringBuilder(sp.getString(Consts.SP_HISTORY, ""));
            if (longHistory.contains(word + ",")) {// 如果历史记录里面包含此word，则调整到最开始位置
                int offset = sb.indexOf(word + ",");
                sb.delete(offset, offset + word.length() + 1);
            }
            sb.insert(0, word + ",");
            sp.edit().putString(Consts.SP_HISTORY, sb.toString()).apply();
        }
    }

    private void initialTextToSpeech() {
        if (tts == null) {
            tts = new TextToSpeech(mContext.getApplicationContext(),
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (TextToSpeech.SUCCESS == status) {
                                tts.setLanguage(Locale.US); // 设置为美式英语发音
                            }
                        }
                    });
        }
        ImageButton ibSpeaker = (ImageButton) findViewById(R.id.ibSpeaker);
        if (ibSpeaker != null) {
            ibSpeaker.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onClick(View v) {
                    String word = mactv.getText().toString().trim();
                    if (TextUtils.isEmpty(word)) return;
                    tts.speak(word, TextToSpeech.QUEUE_ADD, null);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    private void executeQuery(String word) {
        if (DEBUG) Log.i(TAG, "MainActivity.executeQuery: " + word);

        if (!TextUtils.isEmpty(word)) {
            if (imm.isActive())
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

            if (TextUtils.equals(word, lastWord)) return;

            wvNothingToShow.setVisibility(View.GONE);

            if (rvDefinitionContainer.getVisibility() != View.VISIBLE) {
                rvDefinitionContainer.removeAllViews();
                rvDefinitionContainer.setVisibility(View.VISIBLE);
            }
            // 使用AsyncTask查询单词的解释
            new UpdateDefinitionTask(mContext).execute(word);

            boolean idxLoaded = sp.getBoolean(Consts.SP_IDX_LOADED, false);
            if (!idxLoaded) {
                Log.e(TAG, "MyEditorActionListener.onEditorAction: 没有更多的词典", null);
                Snackbar.make(mactv, "Load more dictionaries  ---->", Snackbar.LENGTH_INDEFINITE)
                        .setAction("LOAD", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(mContext, DictActivity.class));
                            }
                        }).show();
            }
            saveSearchHistory(word);
            lastWord = word;
        } else {
            rvDefinitionContainer.setVisibility(View.GONE);
            wvNothingToShow.setVisibility(View.VISIBLE);
            wvNothingToShow.loadData(getString(R.string.nothing_to_show), "text/plain", "utf-8");
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            long currentClickBackTime = System.currentTimeMillis();
            if (currentClickBackTime - lastClickBackTime < 1000) {
                finish();
            } else {
                Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
                lastClickBackTime = currentClickBackTime;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && !mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_daily_sentence:
                startActivity(new Intent(mContext, DailyActivity.class));
                return true;
            case R.id.action_clear_history:
                final SharedPreferences sp = getSharedPreferences(Consts.SP_NAME, MODE_PRIVATE);
                final String history = sp.getString(Consts.SP_HISTORY, "");
                new AlertDialog.Builder(mContext)
                        .setTitle("ARE YOU SURE?")
                        .setMessage("This action will delete all the search history. Continue?")
                        .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sp.edit().putString(Consts.SP_HISTORY, "").apply();
                                Snackbar.make(mactv, "The search history has been clear successfully", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                sp.edit().putString(Consts.SP_HISTORY, history).apply();
                                                Toast.makeText(mContext, "Recover successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        }).show();
                            }
                        }).setNegativeButton("CANCEL", null).show();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawer.closeDrawer(GravityCompat.START);

        switch (item.getItemId()) {
            case R.id.nav_dict:
                startActivity(new Intent(mContext, DictActivity.class));
                break;
            case R.id.nav_note:
                startActivity(new Intent(mContext, NoteListActivity.class));
                break;
            case R.id.nav_review:
                startActivity(new Intent(mContext, ReviewActivity.class));
                break;
            case R.id.nav_star:
                startActivity(new Intent(mContext, StarActivity.class));
                break;

            case R.id.nav_setting:
                startActivity(new Intent(mContext, SettingsActivity.class));
                break;
            case R.id.nav_tutorial:
                startActivity(new Intent(mContext, IntroActivity.class));
                break;
            case R.id.nav_about:
                AboutDialogFragment dialog = new AboutDialogFragment();
                dialog.show(getSupportFragmentManager(), TAG_LOADING_DIALOG);
                dialog.setCancelable(true);
                break;
            case R.id.nav_quit:
                finish();
        }
        return true;
    }
}
