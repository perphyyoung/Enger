package edu.perphy.enger.adapter;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.perphy.enger.DictActivity;
import edu.perphy.enger.R;
import edu.perphy.enger.data.Dict;
import edu.perphy.enger.db.DictInfoHelper;
import edu.perphy.enger.db.DictListHelper;
import edu.perphy.enger.fragment.DictDialogFragment;

import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.TAG_DICT_DIALOG;

/**
 * Created by perphy on 2016/2/27 0027.
 * 从info数据库中读取词典列表信息，并填充到词典列表
 */
public class RvAdapterDictList extends RecyclerView.Adapter<RvAdapterDictList.DictListViewHolder> {
    private DictActivity act;
    private DictListHelper listHelper;
    private ArrayList<Dict> mDictList;

    public RvAdapterDictList(DictActivity act) {
        this.act = act;
        listHelper = DictListHelper.getInstance(act);
        DictInfoHelper infoHelper = DictInfoHelper.getInstance(act);
        mDictList = new ArrayList<>();
        SQLiteDatabase infoReader = infoHelper.getReadableDatabase();
        infoReader.beginTransaction();

        // 从info中读取词典名
        try (Cursor c = infoReader.query(DictInfoHelper.TABLE_NAME,
                new String[]{DictInfoHelper.COL_DICT_ID, DictInfoHelper.COL_BOOK_NAME, DictInfoHelper.COL_WORD_COUNT},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                Dict d = new Dict();
                d.setDictId(c.getString(c.getColumnIndex(DictInfoHelper.COL_DICT_ID)));
                d.setBookName(c.getString(c.getColumnIndex(DictInfoHelper.COL_BOOK_NAME)));
                d.setWordCount(Integer.parseInt(c.getString(c.getColumnIndex(DictInfoHelper.COL_WORD_COUNT))));
                mDictList.add(d);
            }
            infoReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "RvAdapterDictList.RvAdapterDictList: ", e);
            e.printStackTrace();
        } finally {
            infoReader.endTransaction();
            infoReader.close();
        }
    }

    @Override
    public DictListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DictListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dict, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final DictListViewHolder holder, int position) {
        final Dict d = mDictList.get(position);
        holder.tvName.setText(d.getBookName());
        holder.tvCount.setText("Count: " + d.getWordCount());
        holder.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DictDialogFragment dialog = DictDialogFragment.newInstance(d.getDictId());
                dialog.show(act.getSupportFragmentManager(), TAG_DICT_DIALOG);
                dialog.setCancelable(true);
            }
        });

        boolean isChecked = isChecked(d.getDictId());
        setTvEnabledText(holder.tvEnabled, isChecked);
        holder.cbEnable.setChecked(isChecked);
        holder.cbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTvEnabledText(holder.tvEnabled, isChecked);
                SQLiteDatabase listWriter = listHelper.getWritableDatabase();
                listWriter.beginTransaction();
                try {
                    ContentValues cv = new ContentValues(1);
                    cv.put(DictListHelper.COL_ENABLE, isChecked ? "1" : "0");
                    listWriter.update(DictListHelper.TABLE_NAME,
                            cv,
                            DictListHelper.COL_DICT_ID + " = ?",
                            new String[]{d.getDictId()});
                    listWriter.setTransactionSuccessful();
                    Toast.makeText(act, isChecked ? "Enable" : "Disable", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    Log.e(TAG, "RvAdapterDictList.onCheckedChanged: ", e);
                    e.printStackTrace();
                } finally {
                    listWriter.endTransaction();
                    listWriter.close();
                }
            }
        });
    }

    private boolean isChecked(String dictId) {
        SQLiteDatabase listReader = listHelper.getReadableDatabase();
        listReader.beginTransaction();
        String sql = "select " + DictListHelper.COL_ENABLE
                + " from " + DictListHelper.TABLE_NAME
                + " where " + DictListHelper.COL_DICT_ID + " = ?";
        try {
            SQLiteStatement stmt = listReader.compileStatement(sql);
            stmt.bindString(1, dictId);
            boolean isChecked = stmt.simpleQueryForLong() == 1;
            listReader.setTransactionSuccessful();
            return isChecked;
        } catch (SQLException e) {
            Log.e(TAG, "RvAdapterDictList.isChecked: ", e);
            e.printStackTrace();
            return true;// enable if error occur
        } finally {
            listReader.endTransaction();
            listReader.close();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setTvEnabledText(TextView tvEnabled , boolean isChecked){
        if(isChecked){
            tvEnabled.setTextColor(Color.GREEN);
            tvEnabled.setText("Enabled");
        } else {
            tvEnabled.setTextColor(Color.RED);
            tvEnabled.setText("Disabled");
        }
    }

    @Override
    public int getItemCount() {
        return mDictList.size();
    }

    protected static class DictListViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCount, tvEnabled;
        private ImageButton ibInfo;
        private CheckBox cbEnable;

        public DictListViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvCount = (TextView) itemView.findViewById(R.id.tvCount);
            tvEnabled = (TextView) itemView.findViewById(R.id.tvEnabled);
            ibInfo = (ImageButton) itemView.findViewById(R.id.ibInfo);
            cbEnable = (CheckBox) itemView.findViewById(R.id.cbEnable);
        }
    }
}
