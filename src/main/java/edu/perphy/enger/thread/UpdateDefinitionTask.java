package edu.perphy.enger.thread;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.perphy.enger.NoteDetailActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.data.Def;
import edu.perphy.enger.db.DictInfoHelper;
import edu.perphy.enger.db.DictListHelper;
import edu.perphy.enger.db.InternalHelper;
import edu.perphy.enger.db.NoteHelper;
import edu.perphy.enger.db.ReviewHelper;
import edu.perphy.enger.db.StarDictHelper;
import edu.perphy.enger.fragment.LoadingDialogFragment;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.TimeUtils;

import static edu.perphy.enger.util.Consts.DEBUG;
import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;
import static edu.perphy.enger.util.FileUtils.isFileExists;

/**
 * Created by perphy on 2016/2/29 0029.
 * 更新词典释义
 */
public class UpdateDefinitionTask extends AsyncTask<String, Void, Boolean> {
    private String word; // 要查的单词
    private ArrayList<Def> mDefList;

    private Context mContext;
    private AssetManager am;
    private HandlerThread handlerThread;
    private MyDefinitionHandler myDefinitionHandler;
    private RecyclerView rvDefinitionContainer;
    LoadingDialogFragment loadingDialogFragment;

    private InternalHelper internalHelper;
    private DictListHelper listHelper;
    private StarDictHelper dictHelper;
    private DictInfoHelper infoHelper;
    private SQLiteDatabase internalReader, listReader, dictReader, infoReader;
    private RvAdapterDefinition rvAdapterDefinition;

    public UpdateDefinitionTask(Context context) {
        this.mContext = context;
        am = context.getAssets();
        internalHelper = new InternalHelper(context);
        listHelper = new DictListHelper(mContext);
        infoHelper = new DictInfoHelper(mContext);
        rvAdapterDefinition = new RvAdapterDefinition();
        mDefList = new ArrayList<>(); // dictName and definition

        rvDefinitionContainer = (RecyclerView) ((AppCompatActivity) context).findViewById(R.id.rvDefinitionContainer);
        rvDefinitionContainer.setLayoutManager(new LinearLayoutManager(mContext));

        handlerThread = new HandlerThread(Consts.HANDLER_THREAD_DEFINITION);
        handlerThread.start();
        myDefinitionHandler = new MyDefinitionHandler(handlerThread.getLooper());
    }

    @Override
    protected void onPreExecute() {
        mDefList.clear();
        loadingDialogFragment = new LoadingDialogFragment();
        loadingDialogFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), TAG_LOADING_DIALOG);
    }

    @Override
    protected Boolean doInBackground(String... words) {
        word = words[0];
        boolean isInternalEnabled = true;
        int offset = 0, length = 0;

        listReader = listHelper.getReadableDatabase();
        listReader.beginTransaction();
        String sql = "select " + Consts.DB.COL_ENABLE
                + " from " + Consts.DB.TABLE_LIST
                + " where " + Consts.DB.COL_INTERNAL + " = ?";
        try {
            SQLiteStatement stmt = listReader.compileStatement(sql);
            stmt.bindString(1, "1");
            isInternalEnabled = stmt.simpleQueryForLong() == 1;
            listReader.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "UpdateDefinitionTask.doInBackground: ", e);
            e.printStackTrace();
        } finally {
            listReader.endTransaction();
            listReader.close();
        }

        if (isInternalEnabled) {
            boolean hasInternalDefinition = true;
            internalReader = internalHelper.getReadableDatabase();
            try (Cursor c = internalReader.query(Consts.DB.INTERNAL_ID,
                    new String[]{Consts.DB.COL_OFFSET, Consts.DB.COL_LENGTH},
                    Consts.DB.COL_WORD + " = ?",
                    new String[]{word}, null, null, null)) {
                if (c.moveToFirst()) {
                    offset = c.getInt(c.getColumnIndex(Consts.DB.COL_OFFSET));
                    length = c.getInt(c.getColumnIndex(Consts.DB.COL_LENGTH));
                    internalReader.close();
                } else {
                    hasInternalDefinition = false;
                }
            }

            if (hasInternalDefinition) {
                try (InputStream is = am.open("databases" + File.separator + Consts.DB.INTERNAL_DICT + ".dict")) {
                    //noinspection ResultOfMethodCallIgnored
                    is.skip(offset);
                    byte[] bytes = new byte[length];
                    if (is.read(bytes) == -1) {
                        if (DEBUG) Log.i(TAG, "ParseDictCallable.call: Arrive at the end of file!");
                    }
                    String definition = new String(bytes, "utf-8");
                    Def def = new Def();
                    def.setInternal(true);
                    def.setDictName(Consts.DB.INTERNAL_DICT_NAME);
                    def.setDef(definition.replaceAll("\n", "<br>"));
                    mDefList.add(def);
                } catch (IOException e) {
                    Log.e(TAG, "UpdateDefinitionTask.onPreExecute: input stream err", e);
                    e.printStackTrace();
                }
            }
        }

        addNote2def(); // 查看自定义词典（即Note）的内容

        // 从list中获取父目录(parentPath)和文件名(pureName)
        listReader = listHelper.getReadableDatabase();
        try (Cursor cList = listReader.query(Consts.DB.TABLE_LIST,
                new String[]{Consts.DB.COL_PARENT_PATH, Consts.DB.COL_PURE_NAME},
                Consts.DB.COL_INTERNAL + " = ? and " + Consts.DB.COL_ENABLE + " = ?",
                new String[]{"0", "1"}, null, null, null)) {
            while (cList.moveToNext()) {
                String parentPath = cList.getString(cList.getColumnIndex(Consts.DB.COL_PARENT_PATH));
                String pureName = cList.getString(cList.getColumnIndex(Consts.DB.COL_PURE_NAME));
                String dictId = "dict" + Math.abs(pureName.hashCode());
                // 如果对应的dict文件存在
                if (isFileExists(new File(parentPath), pureName + ".dict")) {
                    int[] offsetAndLength = getOffsetAndLength(dictId);
                    if (offsetAndLength != null) {
                        offset = offsetAndLength[0];
                        length = offsetAndLength[1];
                        getDictNameAndDefinition(parentPath, pureName, dictId, offset, length);
                    }
                } else {
                    Log.e(TAG, "UpdateDefinitionTask.doInBackground: " + pureName + ".dict文件不存在", null);

                    Message msg = myDefinitionHandler.obtainMessage();
                    msg.what = Consts.PARSE_DICT_NOT_EXISTS;
                    msg.obj = pureName;
                    msg.sendToTarget();
                }
            }
        }

        return mDefList.size() > 0;
    }

    private void addNote2def() {
        NoteHelper noteHelper = NoteHelper.getInstance(mContext);
        SQLiteDatabase noteReader = noteHelper.getReadableDatabase();
        noteReader.beginTransaction();
        try (Cursor c = noteReader.query(Consts.DB.TABLE_NOTE,
                new String[]{Consts.DB.COL_CONTENT},
                Consts.DB.COL_TITLE + " = ?",
                new String[]{word},
                null, null, null)) {
            if (c.moveToFirst()) {
                String definition = c.getString(c.getColumnIndex(Consts.DB.COL_CONTENT));
                Def def = new Def();
                def.setInternal(false);
                def.setDictName(Consts.DB.CUSTOM_DICT_NAME);
                def.setDef(definition.replaceAll("\n", "<br>"));
                mDefList.add(def);
            }
            noteReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "UpdateDefinitionTask.addNote2def: ", e);
            e.printStackTrace();
        } finally {
            noteReader.endTransaction();
            noteReader.close();
            noteHelper.close();
        }
    }

    /**
     * 获取offset, length
     *
     * @return 是否有对应词条
     */
    @Nullable
    private int[] getOffsetAndLength(String dictId) {
        int[] offsetAndLength = new int[2];
        dictHelper = new StarDictHelper(mContext, dictId);
        dictReader = dictHelper.getReadableDatabase();
        // 如果有对应词条
        try (Cursor cDict = dictReader.query(dictId,
                new String[]{Consts.DB.COL_OFFSET, Consts.DB.COL_LENGTH},
                Consts.DB.COL_WORD + "= ?",
                new String[]{word}, null, null, null)) {
            if (cDict.moveToFirst()) {
                offsetAndLength[0] = Integer.parseInt(cDict.getString(cDict.getColumnIndex(Consts.DB.COL_OFFSET)));
                offsetAndLength[1] = Integer.parseInt(cDict.getString(cDict.getColumnIndex(Consts.DB.COL_LENGTH)));
            } else {
                Log.w(TAG, "UpdateDefinitionTask.getOffsetAndLength: 没有对应词条", null);
                return null;
            }
        }
        return offsetAndLength;
    }

    /**
     * 获取词典名和单词释义
     *
     * @param parentPath 父目录
     * @param pureName   文件名
     */
    private void getDictNameAndDefinition(String parentPath, String pureName, String dictId,
                                          int offset, int length) {
        try (InputStream is = new BufferedInputStream(
                new FileInputStream(parentPath + File.separator + pureName + ".dict"))) {
            //noinspection ResultOfMethodCallIgnored
            is.skip(offset);
            byte[] bytes = new byte[length];
            if (is.read(bytes) == -1) {
                if (DEBUG)
                    Log.i(TAG, "UpdateDefinitionTask.parseDictFile: Arrive at the end of file!");
            }

            String definition = new String(bytes, "utf-8");

            if (!TextUtils.isEmpty(definition)) {
                parseDictNameAndDefinition(definition, dictId);
            }
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "UpdateDefinitionTask.onClick: idxFile inputStream err", e);
            e.printStackTrace();
        }
    }

    /**
     * 解析词典名和词典释义
     *
     * @param definition 词典释义
     */
    private void parseDictNameAndDefinition(String definition, String dictId) {
        // 高亮当前单词
        definition = definition.replaceAll(" " + word + " ", " <font color=\"red\">" + word + "</font> ");
        String[] dictNameAndContentType = getDictNameAndContentType(dictId);
        String dictName = dictNameAndContentType[0];
        String contentType = dictNameAndContentType[1];
        if (TextUtils.equals(contentType, "m")) { // 词典的内容类型不是html
            definition = definition.replaceAll("\n", "<br>");
        }
        Def def = new Def();
        def.setInternal(false);
        def.setDictName(dictName);
        def.setDef(definition);
        mDefList.add(def);
    }


    /**
     * 获取词典名和内容类型
     *
     * @param dictId 词典标识
     * @return 词典名和内容类型
     */
    private String[] getDictNameAndContentType(String dictId) {
        String[] dictNameAndContentType = new String[2];
        infoReader = infoHelper.getReadableDatabase();
        try (Cursor c = infoReader.query(Consts.DB.TABLE_INFO,
                new String[]{Consts.DB.COL_BOOK_NAME, Consts.DB.COL_CONTENT_TYPE},
                Consts.DB.COL_DICT_ID + " = ? ",
                new String[]{dictId}, null, null, null)) {
            if (c.moveToFirst()) {
                dictNameAndContentType[0] = c.getString(c.getColumnIndex(Consts.DB.COL_BOOK_NAME));
                dictNameAndContentType[1] = c.getString(c.getColumnIndex(Consts.DB.COL_CONTENT_TYPE));
            }
        }
        infoReader.close();
        return dictNameAndContentType;
    }

    @Override
    protected void onPostExecute(Boolean hasDefinition) {
        loadingDialogFragment.dismiss();
        if (hasDefinition) {
            rvDefinitionContainer.setAdapter(rvAdapterDefinition);
        } else {
            rvDefinitionContainer.setVisibility(View.GONE);
            WebView wvNothingToShow = (WebView) ((AppCompatActivity) mContext).findViewById(R.id.wvNothingToShow);
            if (wvNothingToShow != null) {
                wvNothingToShow.setVisibility(View.VISIBLE);
                wvNothingToShow.loadData(mContext.getString(R.string.no_result), "text/plain", "utf-8");
            }
        }
    }

    public class RvAdapterDefinition extends RecyclerView.Adapter<RvAdapterDefinition.ViewHolder> {
        private ReviewHelper reviewHelper;

        public RvAdapterDefinition() {
            reviewHelper = new ReviewHelper(mContext);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_definition, parent, false));
        }

        @Override
        public void onBindViewHolder(final RvAdapterDefinition.ViewHolder holder, int position) {
            final Def def = mDefList.get(position);
            if (!def.isInternal()) holder.llButtonGroup.setVisibility(View.GONE);

            final String defStr = def.getDef();
            holder.tvDictName.setText(def.getDictName());
            holder.wvDef.loadDataWithBaseURL(null, defStr, "text/html", "utf-8", null);
            holder.ibHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.llContent.getVisibility() == View.VISIBLE) {
                        holder.llContent.setVisibility(View.GONE);
                        holder.ibHide.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                    } else {
                        holder.llContent.setVisibility(View.VISIBLE);
                        holder.ibHide.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                    }
                }
            });
            holder.ibStar.setImageResource(isExists()
                    ? R.drawable.ic_star_black_24dp
                    : R.drawable.ic_star_border_black_24dp);
            holder.ibStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean exists = isExists();

                    // reverse star status
                    holder.ibStar.setImageResource(exists
                            ? R.drawable.ic_star_border_black_24dp
                            : R.drawable.ic_star_black_24dp);

                    if (exists) { // 当前存在，准备移除
                        SQLiteDatabase reviewWriter = reviewHelper.getWritableDatabase();
                        reviewWriter.beginTransaction();
                        try {
                            // query _id
                            String sql = "select " + ReviewHelper.COL_ID
                                    + " from " + ReviewHelper.TABLE_NAME
                                    + " where " + ReviewHelper.COL_WORD + " = ?";
                            SQLiteStatement stmt = reviewWriter.compileStatement(sql);
                            stmt.bindString(1, word);
                            long _id = stmt.simpleQueryForLong();
                            // delete
                            reviewWriter.delete(ReviewHelper.TABLE_NAME,
                                    ReviewHelper.COL_ID + " = ?",
                                    new String[]{_id + ""});
                            // update the ids which is greater than _id
                            sql = "update " + ReviewHelper.TABLE_NAME
                                    + " set " + ReviewHelper.COL_ID + " = " + ReviewHelper.COL_ID + " - 1"
                                    + " where " + ReviewHelper.COL_ID + " > ?";
                            stmt = reviewWriter.compileStatement(sql);
                            stmt.bindLong(1, _id);
                            stmt.executeUpdateDelete();
                            reviewWriter.setTransactionSuccessful();
                        } catch (Exception e) {
                            Log.e(TAG, "RvAdapterDefinition.onClick: ", e);
                            e.printStackTrace();
                        } finally {
                            reviewWriter.endTransaction();
                            reviewWriter.close();
                        }
                    } else {//当前不存在，准备插入
                        SQLiteDatabase reviewWriter = reviewHelper.getWritableDatabase();
                        // query for row count
                        String sql = "select count(*) from " + ReviewHelper.TABLE_NAME;
                        SQLiteStatement stmt = reviewWriter.compileStatement(sql);
                        long rowCount = stmt.simpleQueryForLong();
                        ContentValues cv = new ContentValues(3);
                        cv.put(ReviewHelper.COL_ID, rowCount + 1); //notice: manually increment
                        cv.put(ReviewHelper.COL_WORD, word);
                        cv.put(ReviewHelper.COL_DEF, defStr);
                        cv.put(ReviewHelper.COL_DATE_ADD, TimeUtils.getSimpleDate());
                        reviewWriter.beginTransaction();
                        try {
                            reviewWriter.insertOrThrow(ReviewHelper.TABLE_NAME, null, cv);
                            reviewWriter.setTransactionSuccessful();
                        } catch (SQLException e) {
                            Log.e(TAG, "RvAdapterDefinition.onClick: ", e);
                            e.printStackTrace();
                        } finally {
                            reviewWriter.endTransaction();
                            reviewWriter.close();
                        }
                    }
                }
            });
            holder.ibAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NoteDetailActivity.class);
                    intent.putExtra(Consts.DB.COL_TOBE_SAVE, true);
                    intent.putExtra(Consts.DB.COL_TITLE, word);
                    intent.putExtra(Consts.DB.COL_CONTENT, defStr);
                    mContext.startActivity(intent);
                }
            });
        }

        // 查询review中是否已存在此word，等同于star
        private boolean isExists() {
            boolean exists = false;
            SQLiteDatabase reviewReader = reviewHelper.getReadableDatabase();
            reviewReader.beginTransaction();
            String sql = "select count(*) from " + ReviewHelper.TABLE_NAME
                    + " where " + ReviewHelper.COL_WORD + " = ?";
            SQLiteStatement stmt = reviewReader.compileStatement(sql);
            stmt.bindString(1, word);
            try {
                exists = stmt.simpleQueryForLong() == 1;
                reviewReader.setTransactionSuccessful();
            } catch (SQLiteDoneException e) {
                Log.e(TAG, "RvAdapterDefinition.onClick: ", e);
            } finally {
                reviewReader.endTransaction();
                reviewReader.close();
            }
            return exists;
        }

        @Override
        public int getItemCount() {
            return mDefList.size();
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tvDictName;
            private ImageButton ibHide, ibStar, ibAdd;
            private WebView wvDef;
            private LinearLayout llContent, llButtonGroup;

            public ViewHolder(View itemView) {
                super(itemView);
                tvDictName = (TextView) itemView.findViewById(R.id.tvDictName);
                wvDef = (WebView) itemView.findViewById(R.id.wvDef);
                ibHide = (ImageButton) itemView.findViewById(R.id.ibHide);
                ibStar = (ImageButton) itemView.findViewById(R.id.ibStar);
                ibAdd = (ImageButton) itemView.findViewById(R.id.ibAdd);
                llContent = (LinearLayout) itemView.findViewById(R.id.llContent);
                llButtonGroup = (LinearLayout) itemView.findViewById(R.id.llButtonGroup);
            }
        }
    }

    private class MyDefinitionHandler extends Handler {
        public MyDefinitionHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String pureName;
            switch (msg.what) {
                case Consts.PARSE_DICT_NOT_EXISTS:
                    pureName = (String) msg.obj;
                    Toast.makeText(mContext, pureName + ".dict文件不存在", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
