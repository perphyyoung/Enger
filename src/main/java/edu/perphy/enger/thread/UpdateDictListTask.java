package edu.perphy.enger.thread;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.FutureTask;

import edu.perphy.enger.R;
import edu.perphy.enger.adapter.RvAdapterDictList;
import edu.perphy.enger.util.Consts;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;

/**
 * Created by perphy on 2016/3/3 0003.
 * 更新词典列表的AsyncTask
 */
public class UpdateDictListTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private SharedPreferences preferences;
    private FloatingActionButton fab;
    private RecyclerView rvDict;
    private RvAdapterDictList mAdapter;

    private HandlerThread handlerThread;
    private MyUpdateDictListHandler myUpdateDictListHandler;

    public UpdateDictListTask(Context context) {
        this.mContext = context;
        this.preferences = context.getSharedPreferences(Consts.SP_NAME, Context.MODE_PRIVATE);
        this.rvDict = (RecyclerView) ((AppCompatActivity) context).findViewById(R.id.rvDict);

        handlerThread = new HandlerThread(Consts.HANDLER_THREAD_UPDATE_DICT_LIST);
        handlerThread.start();
        myUpdateDictListHandler = new MyUpdateDictListHandler(handlerThread.getLooper());
    }

    @Override
    protected void onPreExecute() {
        fab = (FloatingActionButton) ((AppCompatActivity) mContext).findViewById(R.id.fab);
        if (fab != null)
            fab.setImageResource(android.R.drawable.ic_popup_sync);
    }

    /**
     * 1. 搜索ifo文件
     * 2. 解析ifo文件
     * 3. 解析idx文件
     *
     * @return 解析的状态
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        return parseIfoPath(mContext) && parseIfoFile(mContext) && parseIdxFile(mContext);
    }

    /**
     * 寻找ifo文件, 并插入到ist数据库
     *
     * @return 是否加载并插入了新的ifo文件到list
     */
    public boolean parseIfoPath(Context context) {
        //寻找ifo文件
        ParseIfoPathCallable pipc = new ParseIfoPathCallable(context);
        FutureTask<Integer> ft = new FutureTask<>(pipc);
        new Thread(ft).start();

        try {
            int successfulInsertCount = ft.get();
            if (successfulInsertCount > 0) {
                if (DEBUG)
                    Log.i(TAG, "UpdateDictListTask.parseIfoPath: 成功插入" + successfulInsertCount + "个ifo文件");
                return true;
            } else {//没有发现新的ifo文件
                Message msg = myUpdateDictListHandler.obtainMessage();
                msg.what = Consts.PARSE_IFO_PATH_NOT_NEW_LOAD;
                msg.sendToTarget();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "UpdateDictListTask.parseIfoPath: ParseIfoPathCallable get err", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析ifo文件
     */
    public boolean parseIfoFile(Context context) {
        ParseIfoCallable pic = new ParseIfoCallable(context);
        FutureTask<Integer> ft = new FutureTask<>(pic);
        new Thread(ft).start();
        try {
            int successfulInsertCount = ft.get();
            if (successfulInsertCount > 0) {
                Log.i(TAG, "UpdateDictListTask.parseIfoFile: 成功解析" + successfulInsertCount + "个ifo文件");
                return true;
            } else {//没有加载新的ifo文件
                Message msg = myUpdateDictListHandler.obtainMessage();
                msg.what = Consts.PARSE_IFO_NOT_NEW_LOAD;
                msg.sendToTarget();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "UpdateDictListTask.parseIfoFile: FutureTask get err", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析idx文件
     */
    public boolean parseIdxFile(Context context) {
        ParseIdxCallable pic = new ParseIdxCallable(context);
        FutureTask<Integer> ft = new FutureTask<>(pic);
        new Thread(ft).start();

        try {
            int successfulLoadCount = ft.get();
            if (successfulLoadCount > 0) {
                if (DEBUG)
                    Log.i(TAG, "UpdateDictListTask.parseIdxFile: 成功加载" + successfulLoadCount + "个idx文件");
                Message msg = myUpdateDictListHandler.obtainMessage();
                msg.what = Consts.PARSE_IDX_SUCCESS;
                msg.arg1 = successfulLoadCount;
                msg.sendToTarget();
                return true;
            } else {//没有加载新的idx文件
                Message msg = myUpdateDictListHandler.obtainMessage();
                msg.what = Consts.PARSE_IDX_NOT_NEW_LOAD;
                msg.sendToTarget();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "FileUtils.parseIdxFile: parseIdxCallable get err", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 1. 如果解析成功，则更新词典列表
     *
     * @param success 解析标识
     */
    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            preferences.edit().putBoolean(Consts.SP_IDX_LOADED, true).apply();

            mAdapter = new RvAdapterDictList(mContext);
            rvDict.setAdapter(mAdapter);
        }

        fab.setImageResource(R.drawable.ic_popup_sync_1);
    }

    private class MyUpdateDictListHandler extends Handler {
        public MyUpdateDictListHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Consts.PARSE_IFO_PATH_NOT_NEW_LOAD:
                    Toast.makeText(mContext, "No new dictionary to load!", Toast.LENGTH_SHORT).show();
                    break;
                case Consts.PARSE_IFO_NOT_NEW_LOAD:
                case Consts.PARSE_IDX_NOT_NEW_LOAD:
                    Toast.makeText(mContext, "The list is the latest already!", Toast.LENGTH_SHORT).show();
                    break;
                case Consts.PARSE_IDX_SUCCESS:
                    int successfulLoadCount = msg.arg1;
                    Toast.makeText(mContext, "Load " + successfulLoadCount + " new dictionary successfully",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
