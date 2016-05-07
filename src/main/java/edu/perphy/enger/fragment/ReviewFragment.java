package edu.perphy.enger.fragment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.perphy.enger.R;
import edu.perphy.enger.data.Word;
import edu.perphy.enger.db.InternalHelper;
import edu.perphy.enger.db.OxfordHelper;
import edu.perphy.enger.db.ReviewHelper;
import edu.perphy.enger.db.StarDictHelper;
import edu.perphy.enger.util.RandomUtils;
import edu.perphy.enger.util.TimeUtils;

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
    private ReviewHelper reviewHelper;
    private ArrayList<Word> mWordList;
    private ProgressBar pb;
    private TextView tvPercent;
    private Button btnWord;
    private WebView wvDef;
    private FloatingActionButton fabDone, fabForget;

    private static final String ARG_MAX_PROGRESS = "param1";

    private int maxProgress;
    private int reviewCount;

    private OnReviewFragmentInteractionListener mListener;

    public ReviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param maxProgress 复习的单词总数
     * @return A new instance of fragment ReviewFragment.
     */
    public static ReviewFragment newInstance(int maxProgress) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MAX_PROGRESS, maxProgress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        am = getContext().getAssets();
        reviewHelper = ReviewHelper.getInstance(getContext());
        mWordList = new ArrayList<>(maxProgress);

        if (getArguments() != null) {
            maxProgress = getArguments().getInt(ARG_MAX_PROGRESS);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        pb = (ProgressBar) view.findViewById(R.id.pb);
        pb.setMax(maxProgress);
        tvPercent = (TextView) view.findViewById(R.id.tvPercent);
        tvPercent.setText(pb.getProgress() + "/" + pb.getSecondaryProgress() + "/" + maxProgress);
        wvDef = (WebView) view.findViewById(R.id.wvDef);
        btnWord = (Button) view.findViewById(R.id.btnWord);
        btnWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnWord.setText(mWordList.get(pb.getSecondaryProgress()).getWord());
                btnWord.setClickable(false);
            }
        });

        fabDone = (FloatingActionButton) view.findViewById(R.id.fabDone);
        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = pb.getProgress();
                int secondProgress = pb.getSecondaryProgress();
                if (secondProgress < maxProgress - 1) {
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
                if (secondProgress < maxProgress - 1) {
                    progressUpdater(secondProgress + 1);
                } else {
                    reviewSum(false);
                }
            }
        });

        SQLiteDatabase reviewReader = reviewHelper.getReadableDatabase();
        reviewReader.beginTransaction();
        String sql = "select count(*) from " + ReviewHelper.TABLE_NAME;
        try {
            SQLiteStatement stmt = reviewReader.compileStatement(sql);
            reviewCount = (int) stmt.simpleQueryForLong();
            reviewReader.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "ReviewFragment.onCreateView: ", e);
            e.printStackTrace();
        } finally {
            reviewReader.endTransaction();
            reviewReader.close();
        }
        boolean isEnoughCount = reviewCount >= maxProgress;
        if (isEnoughCount) {
            new RandomTask().execute(reviewCount);
        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Generate words to review?")
                    .setMessage("Your review goal is " + maxProgress
                            + ", yet your current review library is " + reviewCount
                            + ". Would you like to generate 50 entries randomly? "
                            + "Or continue review with current library?")
                    .setPositiveButton("Generate", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new GenerateTask().execute();
                        }
                    }).setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (reviewCount == 0) {
                        hideButtons();
                    } else {
                        maxProgress = reviewCount;
                        new RandomTask().execute(reviewCount);
                    }
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    hideButtons();
                }
            }).show();
        }

        return view;
    }

    private void hideButtons() {
        btnWord.setVisibility(View.INVISIBLE);
        wvDef.loadData("<b>Nothing to review!</b>", "text/html", "utf-8");
        fabDone.setVisibility(View.GONE);
        fabForget.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void progressUpdater(int previewProgress) {
        wvDef.loadDataWithBaseURL(null, mWordList.get(previewProgress).getDef().replaceAll("\n", "<br>"), "text/html", "utf-8", null);
        btnWord.setText(getString(R.string.tap_to_see));
        btnWord.setClickable(true);
        pb.setSecondaryProgress(previewProgress);
        tvPercent.setText(pb.getProgress() + "/" + pb.getSecondaryProgress() + "/" + maxProgress);
    }

    private class RandomTask extends AsyncTask<Integer, Void, Void> {
        LoadingDialogFragment loadingDialogFragment;

        @Override
        protected void onPreExecute() {
            loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), TAG_LOADING_DIALOG);
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int maxBoundary = params[0];
            int[] randomIds = RandomUtils.getUniqueRandoms(0, maxBoundary, maxProgress);
            SQLiteDatabase reviewReader = reviewHelper.getReadableDatabase();
            reviewReader.beginTransaction();
            try {
                for (int i = 0; i < maxProgress; i++) {
                    Cursor c = reviewReader.query(ReviewHelper.TABLE_NAME,
                            null,
                            ReviewHelper.COL_ID + " = ?",
                            new String[]{(randomIds[i] + 1) + ""},
                            null, null, null);
                    if (c.moveToFirst()) {
                        Word w = new Word();
                        w.setWord(c.getString(c.getColumnIndex(ReviewHelper.COL_WORD)));
                        w.setDef(c.getString(c.getColumnIndex(ReviewHelper.COL_DEF)));
                        mWordList.add(w);
                        c.close();
                    }
                }
                reviewReader.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(TAG, "RandomTask.doInBackground: ", e);
                e.printStackTrace();
            } finally {
                reviewReader.endTransaction();
                reviewReader.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingDialogFragment.dismiss();
            if (mWordList.size() > 0) {
                wvDef.loadDataWithBaseURL(null, mWordList.get(0).getDef().replaceAll("\n", "<br>"), "text/html", "utf-8", null);
            }
        }
    }

    private class GenerateTask extends AsyncTask<Void, Void, Boolean> {
        LoadingDialogFragment loadingDialogFragment;
        ArrayList<Word> wordList;

        @Override
        protected void onPreExecute() {
            loadingDialogFragment = new LoadingDialogFragment();
            loadingDialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), TAG_LOADING_DIALOG);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Boolean doInBackground(Void... params) {
            int generateCount = 50;
            // get words
            String[] randomWords = new String[generateCount];
            int[] randomIds = RandomUtils.getUniqueRandoms(0, OxfordHelper.WORD_COUNT, generateCount);
            SQLiteDatabase oxfordReader = OxfordHelper.getInstance(getContext()).getReadableDatabase();
            String sql = "select " + OxfordHelper.COL_WORD
                    + " from " + OxfordHelper.TABLE_NAME
                    + " where " + OxfordHelper.COL_ID + " = ?";
            oxfordReader.beginTransaction();
            try {
                SQLiteStatement stmt = oxfordReader.compileStatement(sql);
                for (int i = 0; i < generateCount; i++) {
                    stmt.bindString(1, (randomIds[i] + 1) + "");
                    randomWords[i] = stmt.simpleQueryForString();
                }
                oxfordReader.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(TAG, "GenerateTask.doInBackground: ", e);
                e.printStackTrace();
            } finally {
                oxfordReader.endTransaction();
                oxfordReader.close();
            }

            // get offset and length
            wordList = new ArrayList<>(generateCount);
            SQLiteDatabase internalReader = InternalHelper.getInstance(getContext()).getReadableDatabase();
            internalReader.beginTransaction();
            try {
                for (int i = 0; i < generateCount; i++) {
                    Word w = new Word();
                    String word = randomWords[i];
                    w.setWord(word);
                    Cursor c = internalReader.query(InternalHelper.TABLE_NAME,
                            null,
                            StarDictHelper.COL_WORD + " = ?",
                            new String[]{word}, null, null, null);
                    if (c.moveToFirst()) {
                        w.setOffset(c.getInt(c.getColumnIndex(StarDictHelper.COL_OFFSET)));
                        w.setLength(c.getInt(c.getColumnIndex(StarDictHelper.COL_LENGTH)));
                        c.close();
                        wordList.add(w);
                    }
                }
                internalReader.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(TAG, "GenerateTask.doInBackground: ", e);
                e.printStackTrace();
            } finally {
                internalReader.endTransaction();
                internalReader.close();
            }

            // get def
            try (InputStream is = am.open("databases" + File.separator + InternalHelper.FILE_NAME + ".dict")) {
                wordList.trimToSize(); // trim

                for (Word w : wordList) {
                    is.mark(is.available());
                    is.skip(w.getOffset());
                    byte[] bytes = new byte[w.getLength()];
                    is.read(bytes);
                    w.setDef(new String(bytes, "utf-8"));
                    is.reset();
                }
            } catch (IOException e) {
                Log.e(TAG, "GenerateTask.doInBackground: ", e);
                e.printStackTrace();
            }

            // insert into review
            SQLiteDatabase reviewWriter = reviewHelper.getWritableDatabase();
            reviewWriter.beginTransaction();
            int currentMaxId = reviewCount;
            try {
                for (Word w : wordList) {
                    ContentValues cv = new ContentValues(4);
                    cv.put(ReviewHelper.COL_ID, currentMaxId++);
                    cv.put(ReviewHelper.COL_WORD, w.getWord());
                    cv.put(ReviewHelper.COL_DEF, w.getDef().replaceAll("\n", "<br>"));
                    cv.put(ReviewHelper.COL_DATE_ADD, TimeUtils.getSimpleDate());
                    reviewWriter.insertWithOnConflict(ReviewHelper.TABLE_NAME, null, cv,
                            SQLiteDatabase.CONFLICT_IGNORE);
                }
                reviewWriter.setTransactionSuccessful();
                return true;
            } catch (SQLException e) {
                Log.e(TAG, "GenerateTask.doInBackground: ", e);
                e.printStackTrace();
                return false;
            } finally {
                reviewWriter.endTransaction();
                reviewWriter.close();
            }
        }

        @Override
        protected void onPostExecute(Boolean isGenerateSuccess) {
            loadingDialogFragment.dismiss();
            if (isGenerateSuccess) {
                new RandomTask().execute(reviewCount + wordList.size());
            } else {
                Toast.makeText(getContext(), "Something is wrong while generating", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void reviewSum(boolean correct) {
        fabDone.setVisibility(View.GONE);
        fabForget.setVisibility(View.GONE);
        btnWord.setVisibility(View.INVISIBLE);

        int progress = pb.getProgress();
        int secondProgress = pb.getSecondaryProgress();
        if (correct) {
            progress++;
            pb.setProgress(progress);
            pb.setSecondaryProgress(secondProgress + 1);
        } else {
            pb.setSecondaryProgress(secondProgress + 1);
        }
        tvPercent.setText(progress + "/" + maxProgress + "/" + maxProgress);

        boolean doneAll = progress == maxProgress;
        String comment;
        String reviewed = "You have just reviewed " + maxProgress + " words, ";
        if (maxProgress < 10) {
            comment = "Too few to sum up.";
        } else if (doneAll) {
            comment = "Congratulation! " + reviewed
                    + "and all of them are well remembered.";
        } else if (progress > (int) (maxProgress * 0.75)) {
            comment = "Fortunately. " + reviewed
                    + "and the majority of them are well remembered.";
        } else {
            comment = "Unfortunately. " + reviewed
                    + "and you may not achieve your goal. "
                    + "Are you really try your best?";
        }
        comment = "<b>" + comment + "</b>";
        wvDef.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        wvDef.loadData(comment, "text/html", "utf-8");
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
        void onReviewFragmentInteraction(Uri uri);
    }
}
