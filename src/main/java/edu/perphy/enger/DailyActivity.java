package edu.perphy.enger;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.perphy.enger.data.Daily;
import edu.perphy.enger.db.DailyHelper;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.fragment.LoadingDialogFragment;
import edu.perphy.enger.util.CharUtils;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.FileUtils;
import edu.perphy.enger.util.NetworkUtils;
import edu.perphy.enger.util.TimeUtils;
import edu.perphy.enger.util.Toaster;

import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;

public class DailyActivity extends AppCompatActivity {
    private static final int CONNECTION_TIMEOUT = 5000; // 5 sec
    private static final int READ_TIMEOUT = 5000;
    private Context mContext;
    private SharedPreferences settings;
    private DailyHelper dailyHelper;
    private MediaPlayer player;
    private Toolbar mToolbar;
    private boolean wifiOnly;
    private int screenWidth;
    private Daily daily;
    private ImageView ivPicture, ibStar, ibShare, ibSound;
    private TextView tvEnglish, tvChinese, tvComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        mContext = this;
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        dailyHelper = DailyHelper.getInstance(mContext);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ivPicture = (ImageView) findViewById(R.id.ivPicture);
        ibStar = (ImageView) findViewById(R.id.ibStar);
        ibShare = (ImageView) findViewById(R.id.ibShare);
        ibSound = (ImageView) findViewById(R.id.ibSound);
        tvEnglish = (TextView) findViewById(R.id.tvEnglish);
        tvChinese = (TextView) findViewById(R.id.tvChinese);
        tvComment = (TextView) findViewById(R.id.tvComment);

        // 获取宽度
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;

        ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean starStatus = daily.isStarred();
                setStarImageResource(!starStatus); //notice: reverse star status

                final String date = daily.getDate();
                SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                dailyWriter.beginTransaction();
                try {
                    ContentValues cv = new ContentValues(1);
                    cv.put(DailyHelper.COL_STAR, starStatus ? "0" : "1");
                    dailyWriter.update(DailyHelper.TABLE_NAME,
                            cv,
                            DailyHelper.COL_DATE + " = ?",
                            new String[]{date});
                    dailyWriter.setTransactionSuccessful();
                    Toast.makeText(mContext, starStatus ? "Unstar" : "Star", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Log.e(TAG, "DailyActivity.onClick: ", e);
                    e.printStackTrace();
                } finally {
                    dailyWriter.endTransaction();
                    dailyWriter.close();
                }
            }
        });
        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Developing...", Toast.LENGTH_SHORT).show();
            }
        });
        ibSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkEnabled(mContext)) {
                    Toast.makeText(mContext, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(mContext, "The sound will play when prepared.", Toast.LENGTH_SHORT).show();
                if (player == null) {
                    player = new MediaPlayer();
                }
                try {
                    if (!player.isPlaying()) {
                        player.setDataSource(daily.getTts());
                        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                            }
                        });
                        player.prepareAsync();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                player = null;
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "DailyActivity.onClick: play tts err", e);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * http://blog.csdn.net/jdsjlzx/article/details/8793896
     */
    @Override
    protected void onResume() {
        super.onResume();
        wifiOnly = settings.getBoolean(Consts.Setting.CBP_WIFI_ONLY, true);

        SQLiteDatabase dailyReader = dailyHelper.getReadableDatabase();
        dailyReader.beginTransaction();
        try (Cursor c = dailyReader.query(DailyHelper.TABLE_NAME,
                null, null, null, null, null,
                DailyHelper._ID + " desc ",
                " 0, 1")) {
            if (c.moveToFirst()) {
                daily = new Daily();
                daily.setDate(c.getString(c.getColumnIndex(DailyHelper.COL_DATE)));
                daily.setEnglish(c.getString(c.getColumnIndex(DailyHelper.COL_ENGLISH)));
                daily.setChinese(c.getString(c.getColumnIndex(DailyHelper.COL_CHINESE)));
                daily.setPicture(c.getString(c.getColumnIndex(DailyHelper.COL_PICTURE)));
                daily.setTts(c.getString(c.getColumnIndex(DailyHelper.COL_TTS)));
                daily.setComment(c.getString(c.getColumnIndex(DailyHelper.COL_COMMENT)));
                daily.setStarred(TextUtils.equals(c.getString(c.getColumnIndex(DailyHelper.COL_STAR)), "1"));
            }
            dailyReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "DailyActivity.onResume: ", e);
            e.printStackTrace();
        } finally {
            dailyReader.endTransaction();
            dailyReader.close();
        }

        String date = daily.getDate();
        mToolbar.setTitle(date);
        loadPicture();
        tvEnglish.setText(daily.getEnglish());
        tvChinese.setText(daily.getChinese());
        setStarImageResource(daily.isStarred());
        tvComment.setText(daily.getComment());

        if (!TextUtils.equals(date, TimeUtils.getSimpleDate())) {
            refreshLayout();
        }
    }

    private void loadPicture() {
        if (TextUtils.equals(mToolbar.getTitle(), getString(R.string.daily_date))) { // snapshot
            Picasso.with(mContext)
                    .load(Integer.parseInt(daily.getPicture()))
                    .transform(normalFormation)
                    .into(ivPicture);
        } else { // new content
            File pictureFile = new File(Consts.PATH_NOTE_STR, daily.getDate() + ".jpg");
            if (pictureFile.exists()) {
                Picasso.with(mContext)
                        .load(pictureFile)
                        .transform(normalFormation)
                        .into(ivPicture);
            } else {
                if (NetworkUtils.isNetworkEnabled(mContext)) {
                    Picasso.with(mContext)
                            .load(daily.getPicture())
                            .transform(normalFormation)
                            .into(ivPicture);
                } else {
                    Toast.makeText(mContext, "Network is not available, picture maybe not correspond", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void refreshLayout() {
        // 判断网络连接情况
        if (NetworkUtils.isWifiEnabled(mContext)) {
            new DailySentenceTask().execute();
            return;
        }
        if (NetworkUtils.isMobileEnabled(mContext)) {
            if (wifiOnly) {
                Snackbar.make(tvEnglish, "WIFI ONLY is enabled.", Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {// 跳转到设置界面
                                mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                            }
                        }).show();
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle("No WIFI Connection")
                        .setMessage("Do you want to continue using mobile network retrieve data?")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DailySentenceTask().execute();
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        } else {
            Toast.makeText(mContext, "You are seeing a snapshot of 2016-03-29", Toast.LENGTH_LONG).show();
            Snackbar.make(tvEnglish, getString(R.string.network_not_available), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {// 跳转到手机的网络设置界面
                            mContext.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }).show();
        }
    }

    private void setStarImageResource(boolean starred) {
        ibStar.setImageResource(starred
                ? R.drawable.ic_star_black_24dp
                : R.drawable.ic_star_border_black_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_daily, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshLayout();
                return true;
            case R.id.action_add:
                saveSentence2note();
                return true;
            case R.id.action_stars:
                startActivity(new Intent(mContext, StarActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSentence2note() {
        String tmpTitle = "day_" + mToolbar.getTitle().toString();
        String tmpContent = tvEnglish.getText().toString() + "\n" + tvChinese.getText().toString();
        Intent intent = new Intent(mContext, NoteDetailActivity.class);
        intent.putExtra(NoteHelper.COL_TOBE_SAVE, true);
        intent.putExtra(NoteHelper.COL_TITLE, tmpTitle);
        intent.putExtra(NoteHelper.COL_CONTENT, tmpContent);
        startActivity(intent);
    }

    private class DailySentenceTask extends AsyncTask<Void, Void, Integer> {
        private static final int SUCCESS = 1;
        private static final int ERR_NETWORK = 2;
        private static final int ERR_JSON = 3;
        LoadingDialogFragment loadingDialogFragment;

        @Override
        protected void onPreExecute() {
            loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(getSupportFragmentManager(), TAG_LOADING_DIALOG);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                URL url = new URL("http://open.iciba.com/dsapi/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                httpURLConnection.setReadTimeout(READ_TIMEOUT);
                InputStream is = httpURLConnection.getInputStream();

                JSONObject json = new JSONObject(FileUtils.get5json(is));
                daily.setSid(json.getString("sid"));
                daily.setTts(json.getString("tts"));
                daily.setEnglish(json.getString("content"));
                daily.setChinese(json.getString("note"));
                daily.setLove(json.getString("love"));
                daily.setComment(json.getString("translation"));
                daily.setPicture(json.getString("picture"));
                daily.setPicture2(json.getString("picture2"));
                daily.setCaption(json.getString("caption"));
                daily.setDate(CharUtils.hyphen2en_dash(json.getString("dateline")));
                daily.setS_pv(json.getString("s_pv"));
                daily.setSp_pv(json.getString("sp_pv"));
                daily.setTags(json.getJSONArray("tags"));
                daily.setShare(json.getString("fenxiang_img"));

                daily.setStarred(false);

                // Insert into database
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                        dailyWriter.beginTransaction();
                        try {
                            ContentValues cv = new ContentValues(6);
                            cv.put(DailyHelper.COL_DATE, daily.getDate());
                            cv.put(DailyHelper.COL_ENGLISH, daily.getEnglish());
                            cv.put(DailyHelper.COL_CHINESE, daily.getChinese());
                            cv.put(DailyHelper.COL_PICTURE, daily.getPicture());
                            cv.put(DailyHelper.COL_TTS, daily.getTts());
                            cv.put(DailyHelper.COL_COMMENT, daily.getComment());
                            dailyWriter.insertWithOnConflict(DailyHelper.TABLE_NAME,
                                    null,
                                    cv,
                                    SQLiteDatabase.CONFLICT_IGNORE);
                            dailyWriter.setTransactionSuccessful();
                        } catch (Exception e) {
                            Log.e(TAG, "DailySentenceTask.run: ", e);
                            e.printStackTrace();
                        } finally {
                            dailyWriter.endTransaction();
                            dailyWriter.close();
                        }
                    }
                }).start();
                return SUCCESS;
            } catch (IOException e) {
                Log.e(TAG, "DailySentenceTask.doInBackground: net err", e);
                e.printStackTrace();
                return ERR_NETWORK;
            } catch (Exception e) {
                Log.e(TAG, "DailySentenceTask.doInBackground: json err", e);
                e.printStackTrace();
                return ERR_JSON;
            }
        }

        @Override
        protected void onPostExecute(Integer status) {
            loadingDialogFragment.dismiss();
            switch (status) {
                case ERR_JSON:
                    new Toaster(mContext).showCenterToast("Parse json error");
                    return;
                case ERR_NETWORK:
                    Snackbar.make(ivPicture, "Network timeout.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Reload", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new DailySentenceTask().execute();
                                }
                            }).show();
                    return;
                case SUCCESS:
                    break;
            }

            tvComment.setText(daily.getComment());
            ibStar.setImageResource(R.drawable.ic_star_border_black_24dp);

            mToolbar.setTitle(daily.getDate());
            tvEnglish.setText(daily.getEnglish());
            tvChinese.setText(daily.getChinese());
            String pictureStr = daily.getPicture();
            if (pictureStr != null) {
                loadingDialogFragment.dismiss();
                Picasso.with(mContext)
                        .load(pictureStr)
                        .transform(normalFormation)
                        .into(ivPicture);
                // 保存图片
                Picasso.with(mContext)
                        .load(pictureStr)
                        .transform(normalFormation)
                        .into(target);
            }
        }
    }

    /**
     * @see "http://stackoverflow.com/questions/22533228/downloading-images-with-picasso-android-disck"
     */
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String date = daily.getDate();
                    String savePath = Consts.PATH_NOTE_STR;
                    if (FileUtils.createDir(savePath)) {
                        try (FileOutputStream fos = new FileOutputStream(new File(savePath, date + ".jpg"))) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                        } catch (IOException e) {
                            Log.e(TAG, "DailyActivity.run: ", e);
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.e(TAG, "DailyActivity.onBitmapFailed: failed", null);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    Transformation normalFormation = new Transformation() {
        @Override
        public Bitmap transform(Bitmap source) {
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetWidth = screenWidth;
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                source.recycle(); // Same bitmap is returned if sizes are the same
            }
            return result;
        }

        @Override
        public String key() {
            return daily.getDate();
        }
    };
}
