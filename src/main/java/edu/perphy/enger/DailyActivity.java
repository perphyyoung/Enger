package edu.perphy.enger;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.perphy.enger.db.DailyHelper;
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
    private final String TODAY = TimeUtils.getSimpleDate();
    private final String TODAY_DIR = Consts.PATH_NOTE_STR + File.separator + TODAY;
    private boolean hasNewContent = false;
    private int screenWidth;
    private String sid, tts, content, note, love, translation, picture, picture2,
            caption, dateline, s_pv, sp_pv, tags, fenxiang_img;
    private ImageView ivPicture, ibStar, ibShare, ibSound;
    private TextView tvContent, tvNote, tvComment;

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

        dateline = getString(R.string.daily_date);
        content = getString(R.string.daily_content);
        note = getString(R.string.daily_note);

        ivPicture = (ImageView) findViewById(R.id.ivPicture);
        ibStar = (ImageView) findViewById(R.id.ibStar);
        ibShare = (ImageView) findViewById(R.id.ibShare);
        ibSound = (ImageView) findViewById(R.id.ibSound);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvNote = (TextView) findViewById(R.id.tvNote);
        tvComment = (TextView) findViewById(R.id.tvComment);

        // 获取宽度
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenWidth = point.x;

        ibStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int starStatus = getStarStatus();
                if (-1 != starStatus) {
                    setStarImageResource(starStatus == 0); //notice: reverse star status
                } else {return;}

                final String date = mToolbar.getTitle().toString();
                if (!hasNewContent) { // 旧内容
                    //notice: reverse star status in database
                    SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                    dailyWriter.beginTransaction();
                    try {
                        ContentValues cv = new ContentValues(1);
                        cv.put(Consts.DB.COL_STAR, starStatus == 0 ? "1" : "0");
                        dailyWriter.update(Consts.DB.TABLE_DAILY,
                                cv,
                                Consts.DB.COL_DATE + " = ?",
                                new String[]{date});
                        dailyWriter.setTransactionSuccessful();
                        new Toaster(mContext).showSingletonToast(starStatus == 0 ? "Star" : "Unstar");
                    } catch (SQLException e) {
                        Log.e(TAG, "DailyActivity.onClick: ", e);
                        e.printStackTrace();
                    } finally {
                        dailyWriter.endTransaction();
                        dailyWriter.close();
                    }
                } else if (starStatus == 0) {// make new star, aka insert
                    SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                    dailyWriter.beginTransaction();
                    ContentValues cv = new ContentValues(4);
                    cv.put(Consts.DB.COL_DATE, date);
                    cv.put(Consts.DB.COL_CONTENT, tvContent.getText().toString());
                    cv.put(Consts.DB.COL_NOTE, tvNote.getText().toString());
                    cv.put(Consts.DB.COL_STAR, "1");
                    try {
                        dailyWriter.insertOrThrow(Consts.DB.TABLE_DAILY, null, cv);
                        new Toaster(mContext).showSingletonToast("Star");
                        dailyWriter.setTransactionSuccessful();
                    } catch (SQLException e) {
                        Log.e(TAG, "DailyActivity.onClick: ", e);
                        e.printStackTrace();
                    } finally {
                        dailyWriter.endTransaction();
                        dailyWriter.close();
                    }
                } else { // make new unstar, aka delete
                    SQLiteDatabase dailyWriter = dailyHelper.getWritableDatabase();
                    dailyWriter.beginTransaction();
                    try {
                        dailyWriter.delete(Consts.DB.TABLE_DAILY,
                                Consts.DB.COL_DATE + " = ?",
                                new String[]{date});
                        new Toaster(mContext).showSingletonToast("Unstar");
                        dailyWriter.setTransactionSuccessful();
                    } catch (Exception e) {
                        Log.e(TAG, "DailyActivity.onClick: ", e);
                        e.printStackTrace();
                    } finally {
                        dailyWriter.endTransaction();
                        dailyWriter.close();
                    }
                }
            }
        });
        ibShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2016/4/4 0004 分享到。。。
                Toast.makeText(mContext, "Developing...", Toast.LENGTH_SHORT).show();
            }
        });
        ibSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tts == null) return;

                Toast.makeText(mContext, "The sound will play when prepared.", Toast.LENGTH_SHORT).show();
                if (player == null) {
                    player = new MediaPlayer();
                }
                try {
                    if (!player.isPlaying()) {
                        player.setDataSource(tts);
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

    @Override
    protected void onResume() {
        super.onResume();
        mToolbar.setTitle(dateline);
        wifiOnly = settings.getBoolean(Consts.Setting.CBP_WIFI_ONLY, true);
        if (new File(TODAY_DIR).exists()) {
            hasNewContent = true;
            loadLocalDaily();
        } else {
            refreshLayout();
        }
    }

    /**
     * 从本地更新界面
     */
    private void loadLocalDaily() {
        File pictureFile = new File(TODAY_DIR + File.separator + TODAY + ".jpg");
        if (pictureFile.exists()) { // 保存图片
            Picasso.with(mContext)
                    .load(pictureFile)
                    .transform(normalFormation)
                    .into(ivPicture);
        }
        File jsonFile = new File(TODAY_DIR + File.separator + TODAY + ".json");
        if (jsonFile.exists()) {
            try {
                JSONObject obj = new JSONObject(FileUtils.get5json(jsonFile));
                mToolbar.setTitle((String) obj.get(Consts.DB.COL_DATE));
                tvContent.setText((String) obj.get(Consts.DB.COL_CONTENT));
                tvNote.setText((String) obj.get(Consts.DB.COL_NOTE));
                tvComment.setText((String) obj.get(Consts.DB.COL_COMMENT));
            } catch (JSONException e) {
                Log.e(TAG, "DailyActivity.loadLocalDaily: ", e);
                e.printStackTrace();
            }
        }
    }

    private void refreshLayout() {
        if (TextUtils.equals(mToolbar.getTitle(), TimeUtils.getSimpleDate()))
            return; // title为日期，与今天相同说明已加载最新内容

        // 判断网络连接情况
        if (NetworkUtils.isWifiEnabled(mContext)) {
            new DailySentenceTask().execute();
            return;
        }
        int starStatus = getStarStatus();
        if (-1 != starStatus) {
            setStarImageResource(starStatus == 1);
        }
        if (NetworkUtils.isMobileEnabled(mContext)) {
            if (wifiOnly) {
                Snackbar.make(tvContent, "WIFI ONLY is enabled.", Snackbar.LENGTH_INDEFINITE)
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
            Snackbar.make(tvContent, "Network is not available.", Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {// 跳转到手机的网络设置界面
                            mContext.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }).show();
        }
    }

    /**
     * 获取star状态
     *
     * @return 1:star 0:unstar -1:error
     */
    private int getStarStatus() {
        SQLiteDatabase dailyReader = dailyHelper.getReadableDatabase();
        dailyReader.beginTransaction();
        try {
            String sql = "select " + Consts.DB.COL_STAR
                    + " from " + Consts.DB.TABLE_DAILY
                    + " where " + Consts.DB.COL_DATE + " = ?";
            SQLiteStatement statement = dailyReader.compileStatement(sql);
            statement.bindString(1, dateline);
            // notice: throw exception if return 0 rows
            int starStatus = Integer.parseInt(statement.simpleQueryForString());
            dailyReader.setTransactionSuccessful();
            return starStatus;
        } catch (SQLiteDoneException e) {
            e.printStackTrace();
            return 0; // new content, thus unstar
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // err
        } finally {
            dailyReader.endTransaction();
            dailyReader.close();
        }
    }

    private void setStarImageResource(boolean starred) {
        ibStar.setImageResource(starred ? R.drawable.ic_star_black_24dp
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
                if (hasNewContent) {
                    saveSentence2note();
                } else {
                    new AlertDialog.Builder(mContext)
                            .setTitle("Save to note")
                            .setMessage("You are about saving the content of 2016-03-29. Continue?")
                            .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveSentence2note();
                                }
                            }).setNegativeButton("Cancel", null).show();
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(mContext, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSentence2note() {
        String tmpTitle = "day_" + dateline;
        String tmpContent = content + "\n" + note;
        Intent intent = new Intent(mContext, NoteDetailActivity.class);
        intent.putExtra(Consts.DB.COL_TOBE_SAVE, true);
        intent.putExtra(Consts.DB.COL_TITLE, tmpTitle);
        intent.putExtra(Consts.DB.COL_CONTENT, tmpContent);
        startActivity(intent);
    }

    private class DailySentenceTask extends AsyncTask<Void, Void, Integer> {
        private static final int SUCCESS = 1;
        private static final int ERR_NETWORK = 2;
        private static final int ERR_JSON = 3;
        LoadingDialogFragment loadingDialogFragment;
        String jsonStr = "";
        JSONObject json = null;

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
                jsonStr = FileUtils.get5json(is);

                json = new JSONObject(jsonStr);
                sid = json.getString("sid");//每日一句ID
                tts = json.getString("tts");//音频地址
                content = json.getString("content");//英文内容
                note = json.getString("note"); // 中文内容
                love = json.getString("love");//喜欢个数
                translation = json.getString("translation");//词霸小编
                picture = json.getString("picture");//图片地址
                picture2 = json.getString("picture2");//大图片地址
                caption = json.getString("caption");//标题
                dateline = CharUtils.hyphen2en_dash(json.getString("dateline"));//时间
                s_pv = json.getString("s_pv");//浏览数
                sp_pv = json.getString("sp_pv");//语音评测浏览数
                JSONArray array = json.getJSONArray("tags");//相关标签
                tags = "";
                for (int i = 0; i < array.length(); i++) {
                    JSONObject tag = (JSONObject) array.get(i);
                    tags += tag.getString("name") + ",";
                }
                fenxiang_img = json.getString("fenxiang_img");//分享图片

                // 下载必要文本到本机
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String savePath = Consts.PATH_NOTE_STR + File.separator + dateline;
                        if (FileUtils.createDir(savePath)) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put(Consts.DB.COL_DATE, dateline);
                                obj.put(Consts.DB.COL_CONTENT, content);
                                obj.put(Consts.DB.COL_NOTE, note);
                                obj.put(Consts.DB.COL_COMMENT, translation);
                                FileUtils.save2json(obj.toString(), savePath, dateline + ".json");
                            } catch (JSONException e) {
                                Log.e(TAG, "DailySentenceTask.run: save json err", e);
                                e.printStackTrace();
                            }
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
                    hasNewContent = false;
                    new Toaster(mContext).showCenterToast("Parse json error");
                    return;
                case ERR_NETWORK:
                    hasNewContent = false;
                    Snackbar.make(ivPicture, "Network timeout.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Reload", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new DailySentenceTask().execute();
                                }
                            }).show();
                    return;
                case SUCCESS:
                    hasNewContent = true;
                    break;
            }

            tvComment.setText(translation);
            ibStar.setImageResource(R.drawable.ic_star_border_black_24dp);

            mToolbar.setTitle(dateline);
            tvContent.setText(content);
            tvNote.setText(note);
            if (picture != null) {
                loadingDialogFragment.dismiss();
                Picasso.with(mContext)
                        .load(picture)
                        .transform(normalFormation)
                        .into(ivPicture);
                // 保存图片
                Picasso.with(mContext)
                        .load(picture)
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
                    String savePath = Consts.PATH_NOTE_STR + File.separator + dateline;
                    if (FileUtils.createDir(savePath)) {
                        try (FileOutputStream fos = new FileOutputStream(new File(savePath, dateline + ".jpg"))) {
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
            return TODAY;
        }
    };
}
