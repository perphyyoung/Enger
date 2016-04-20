package edu.perphy.enger.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import edu.perphy.enger.R;
import edu.perphy.enger.data.Word;
import edu.perphy.enger.db.InternalHelper;
import edu.perphy.enger.util.Consts;
import edu.perphy.enger.util.RandomUtils;

import static edu.perphy.enger.util.Consts.TAG;
import static edu.perphy.enger.util.Consts.TAG_LOADING_DIALOG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnReviewFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewFragment extends Fragment {
    private AssetManager am;
    private ArrayList<Word> mWordList;
    private ProgressBar pb;
    private TextView tvPercent;
    private Button pbWord;
    private WebView wvDef;
    private FloatingActionButton fabDone, fabForget;

    private static final String ARG_MAX_PROGRESS = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int max_progress;
    private String mParam2;

    private OnReviewFragmentInteractionListener mListener;

    public ReviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param max_progress 复习的单词总数
     * @param param2       Parameter 2.
     * @return A new instance of fragment ReviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReviewFragment newInstance(int max_progress, String param2) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MAX_PROGRESS, max_progress);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        am = getContext().getAssets();
        mWordList = new ArrayList<>(max_progress);

        if (getArguments() != null) {
            max_progress = getArguments().getInt(ARG_MAX_PROGRESS);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        new RandomTask().execute();

        pb = (ProgressBar) view.findViewById(R.id.pb);
        pb.setMax(max_progress);
        tvPercent = (TextView) view.findViewById(R.id.tvPercent);
        tvPercent.setText(pb.getProgress() + "/" + pb.getSecondaryProgress() + "/" + max_progress);
        wvDef = (WebView) view.findViewById(R.id.wvDef);
        pbWord = (Button) view.findViewById(R.id.pbWord);
        pbWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbWord.setText(mWordList.get(pb.getSecondaryProgress()).getWord());
                pbWord.setClickable(false);
            }
        });

        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = pb.getProgress();
                int secondProgress = pb.getSecondaryProgress();
                if (secondProgress < max_progress - 1) {
                    pb.setProgress(progress + 1);
                    progressUpdater(secondProgress + 1);
                } else {
                    reviewSum(true);
                }
            }
        });
        fabForget = (FloatingActionButton) view.findViewById(R.id.fabForget);
        fabForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int secondProgress = pb.getSecondaryProgress();
                if (secondProgress < max_progress - 1) {
                    progressUpdater(secondProgress + 1);
                } else {
                    reviewSum(false);
                }
            }
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void progressUpdater(int previewProgress) {
        new DefTask().execute(previewProgress); // next def
        pbWord.setText(getString(R.string.tap_to_see));
        pbWord.setClickable(true);
        pb.setSecondaryProgress(previewProgress);
        tvPercent.setText(pb.getProgress() + "/" + pb.getSecondaryProgress() + "/" + max_progress);
    }

    private class RandomTask extends AsyncTask<Void, Void, Void> {
        LoadingDialogFragment loadingDialogFragment;

        @Override
        protected void onPreExecute() {
            loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), TAG_LOADING_DIALOG);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String[] randoms = new String[max_progress];
            int[] randomObjects = RandomUtils.getUniqueRandoms(0, Consts.DB.INTERNAL_DICT_COUNT, max_progress);
            for (int i = 0; i < max_progress; i++) {
                randoms[i] = randomObjects[i] + "";
            }

            InternalHelper internalHelper = new InternalHelper(getContext());
            SQLiteDatabase internalReader = internalHelper.getReadableDatabase();
            internalReader.beginTransaction();
            // http://stackoverflow.com/questions/7418849/in-clause-and-placeholders
            String sql = "select * from " + Consts.DB.INTERNAL_ID
                    + " where " + Consts.DB._ID
                    + " in (" + TextUtils.join(",", Collections.nCopies(max_progress, "?")) + ")";
            try (Cursor c = internalReader.rawQuery(sql, randoms)) {
                while (c.moveToNext()) {
                    Word word = new Word();
                    word.setWord(c.getString(c.getColumnIndex(Consts.DB.COL_WORD)));
                    word.setOffset(c.getInt(c.getColumnIndex(Consts.DB.COL_OFFSET)));
                    word.setLength(c.getInt(c.getColumnIndex(Consts.DB.COL_LENGTH)));
                    mWordList.add(word);
                }
                internalReader.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, "RandomTask.doInBackground: ", e);
                e.printStackTrace();
            } finally {
                internalReader.endTransaction();
                internalReader.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingDialogFragment.dismiss();
            new DefTask().execute(0);
        }
    }

    private class DefTask extends AsyncTask<Integer, Void, String> {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected String doInBackground(Integer... params) {
            int id = params[0];
            Word word = mWordList.get(id);

            try (InputStream is = am.open("databases" + File.separator + Consts.DB.INTERNAL_DICT + ".dict")) {
                is.skip(word.getOffset());
                byte[] bytes = new byte[word.getLength()];
                is.read(bytes);
                String definition = new String(bytes, "utf-8");
                return definition.replaceAll("\n", "<br>");
            } catch (IOException e) {
                Log.e(TAG, "UpdateDefinitionTask.onPreExecute: input stream err", e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String def) {
            if (def != null) {
                wvDef.loadDataWithBaseURL(null, def, "text/html", "utf-8", null);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void reviewSum(boolean correct) {
        fabDone.setVisibility(View.GONE);
        fabForget.setVisibility(View.GONE);
        pbWord.setVisibility(View.INVISIBLE);

        int progress = pb.getProgress();
        if (correct) progress++;
        tvPercent.setText(progress + "/" + max_progress + "/" + max_progress);

        boolean doneAll = progress == max_progress;
        String comment;
        String reviewed = "You have just reviewed " + max_progress + " words, ";
        if (doneAll) {
            comment = "Congratulation! " + reviewed
                    + "and all of them are well remembered.";
        } else if (progress > (int) (max_progress * 0.75)) {
            comment = "Fortunately. " + reviewed
                    + "and the majority of them are well remembered.";
        } else {
            comment = "Unfortunately. " + reviewed
                    + "and you may not achieve your goal. "
                    + "Are you really try your best?";
        }
        wvDef.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        wvDef.loadData(comment, "text/plain", "utf-8");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onReviewFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReviewFragmentInteractionListener) {
            mListener = (OnReviewFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnReviewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnReviewFragmentInteractionListener {
        // TODO: Update argument type and name
        void onReviewFragmentInteraction(Uri uri);
    }
}
