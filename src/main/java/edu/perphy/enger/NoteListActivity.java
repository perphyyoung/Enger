package edu.perphy.enger;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tubb.smrv.SwipeMenuRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import edu.perphy.enger.adapter.RvAdapterNoteList;
import edu.perphy.enger.thread.ExportNoteTask;
import edu.perphy.enger.thread.ImportNoteTask;

public class NoteListActivity extends AppCompatActivity {
    private Context mContext;
    public SwipeMenuRecyclerView rvNoteList;
    RvAdapterNoteList adapterNoteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(mContext, NoteDetailActivity.class));
                }
            });

        rvNoteList = (SwipeMenuRecyclerView) findViewById(R.id.rvNoteList);
        rvNoteList.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(mContext).showLastDivider().build());
        rvNoteList.setHasFixedSize(true);
        rvNoteList.setLayoutManager(new LinearLayoutManager(mContext));
        adapterNoteList = new RvAdapterNoteList(mContext);
        rvNoteList.setAdapter(adapterNoteList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_import:
                new AlertDialog.Builder(mContext)
                        .setTitle("Import notes")
                        .setMessage("This action will discard the note entries in json file " +
                                "which has identical title with current list. Continue?")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ImportNoteTask(mContext).execute();
                            }
                        }).setNegativeButton("Cancel", null).show();
                return true;
            case R.id.action_export:
                new ExportNoteTask(mContext).execute();
                return true;
            case R.id.action_stars:
                startActivity(new Intent(mContext, StarActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapterNoteList = new RvAdapterNoteList(mContext);
        rvNoteList.setAdapter(adapterNoteList);
    }

    @Override
    public void onBackPressed() {
        if (!onSupportNavigateUp()) {
            super.onBackPressed();
        }
    }
}
