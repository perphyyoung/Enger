package edu.perphy.enger;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.BounceInterpolator;

import com.tubb.smrv.SwipeMenuRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import edu.perphy.enger.adapter.RvAdapterDictList;
import edu.perphy.enger.thread.UpdateDictListTask;

public class DictActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dict);

        mContext = this;
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SwipeMenuRecyclerView rvDict = (SwipeMenuRecyclerView) findViewById(R.id.rvDict);
        if (rvDict != null) {
            rvDict.addItemDecoration(new HorizontalDividerItemDecoration.
                    Builder(this).showLastDivider().build());
            rvDict.setHasFixedSize(true);
            rvDict.setLayoutManager(new LinearLayoutManager(mContext));
            rvDict.setOpenInterpolator(new BounceInterpolator());
            rvDict.setCloseInterpolator(new BounceInterpolator());
            rvDict.setAdapter(new RvAdapterDictList(this));
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 更新词典列表
                    new UpdateDictListTask(DictActivity.this).execute();
                }
            });
        }

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (!onSupportNavigateUp()) {
            super.onBackPressed();
        }
    }
}