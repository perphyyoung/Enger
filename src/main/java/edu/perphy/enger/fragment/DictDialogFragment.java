package edu.perphy.enger.fragment;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.perphy.enger.R;
import edu.perphy.enger.data.Dict;
import edu.perphy.enger.db.DictInfoHelper;

import static edu.perphy.enger.util.Consts.TAG;

public class DictDialogFragment extends AppCompatDialogFragment {
    private static final String ARG_DICT_ID = "dictId";
    private String dictId;
    private Dict d;

    private TextView tvDictName;
    private TextView tvWordCount;
    private TextView tvVersion;
    private TextView tvAuthor;
    private TextView tvDescription;

    public DictDialogFragment() {
    }

    public static DictDialogFragment newInstance(String dictId) {
        DictDialogFragment fragment = new DictDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DICT_ID, dictId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dictId = getArguments().getString(ARG_DICT_ID);
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialog);

        SQLiteDatabase infoReader = DictInfoHelper.getInstance(getContext()).getReadableDatabase();
        infoReader.beginTransaction();
        try (Cursor c = infoReader.query(DictInfoHelper.TABLE_NAME,
                null,
                DictInfoHelper.COL_DICT_ID + " = ?",
                new String[]{dictId},
                null, null, null)) {
            if (c.moveToFirst()) {
                d = new Dict();
                d.setBookName(c.getString(c.getColumnIndex(DictInfoHelper.COL_BOOK_NAME)));
                d.setWordCount(Integer.parseInt(c.getString(c.getColumnIndex(DictInfoHelper.COL_WORD_COUNT))));
                d.setVersion(c.getString(c.getColumnIndex(DictInfoHelper.COL_VERSION)));
                d.setAuthor(c.getString(c.getColumnIndex(DictInfoHelper.COL_AUTHOR)));
                d.setDescription(c.getString(c.getColumnIndex(DictInfoHelper.COL_DESCRIPTION)));
            }
            infoReader.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "DictDialogFragment.onCreate: ", e);
            e.printStackTrace();
        } finally {
            infoReader.endTransaction();
            infoReader.close();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dict_dialog, container);
        tvDictName = (TextView) v.findViewById(R.id.tvDictName);
        tvWordCount = (TextView) v.findViewById(R.id.tvWordCount);
        tvVersion = (TextView) v.findViewById(R.id.tvVersion);
        tvAuthor = (TextView) v.findViewById(R.id.tvAuthor);
        tvDescription = (TextView) v.findViewById(R.id.tvDescription);

        tvDictName.setText("Dict Name: " + d.getBookName());
        tvWordCount.setText("Entries Count: " + d.getWordCount());
        tvVersion.setText("Version: " + d.getVersion());
        tvAuthor.setText("Author: " + d.getAuthor());
        tvDescription.setText("Description: " + d.getDescription());
        return v;
    }
}
