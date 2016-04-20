package edu.perphy.enger.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.perphy.enger.R;
import edu.perphy.enger.db.DictInfoHelper;
import edu.perphy.enger.util.Consts;

/**
 * Created by perphy on 2016/2/27 0027.
 * 从info数据库中读取词典列表信息，并填充到词典列表
 */
public class RvAdapterDictList extends RecyclerView.Adapter<RvAdapterDictList.DictListViewHolder> {
    private Context mContext;
    private ArrayList<String> mDictList;

    public RvAdapterDictList(Context context) {
        mContext = context;
        DictInfoHelper infoHelper = new DictInfoHelper(mContext);
        SQLiteDatabase infoReader = infoHelper.getReadableDatabase();
        mDictList = new ArrayList<>();

        // 从info中读取词典名
        Cursor cursor = infoReader.query(Consts.DB.TABLE_INFO,
                new String[]{Consts.DB.COL_BOOK_NAME},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(Consts.DB.COL_BOOK_NAME));
            mDictList.add(name);
        }
        cursor.close();
        infoReader.close();
        infoHelper.close();
    }

    @Override
    public DictListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DictListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dict, parent, false));
    }

    @Override
    public void onBindViewHolder(DictListViewHolder holder, int position) {
        holder.name.setText(mDictList.get(position));
        holder.ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2016/4/6 0006 词典信息
                Toast.makeText(mContext, "info", Toast.LENGTH_SHORT).show();
            }
        });
        holder.cbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO: 2016/4/6 0006 是否查询此词典 
                Toast.makeText(mContext, isChecked + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDictList.size();
    }

    protected static class DictListViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageButton ibInfo;
        private CheckBox cbEnable;

        public DictListViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            ibInfo = (ImageButton) itemView.findViewById(R.id.ibInfo);
            cbEnable = (CheckBox) itemView.findViewById(R.id.cbEnable);
        }
    }
}
