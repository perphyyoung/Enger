package edu.perphy.enger.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import edu.perphy.enger.R;
import edu.perphy.enger.util.RandomUtils;

public class ChartFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private LineChart lcCurve;
    private AppCompatSeekBar sbRange, sbCount;
    private TextView tvRange, tvCount;

    private OnChartFragmentInteractionListener mListener;

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        lcCurve = (LineChart) view.findViewById(R.id.lcCurve);

        sbRange = (AppCompatSeekBar) view.findViewById(R.id.sbRange);
        int range = sbRange.getProgress();
        tvRange = (TextView) view.findViewById(R.id.tvRange);
        tvRange.setText(range + "");
        sbRange.setOnSeekBarChangeListener(this);

        sbCount = (AppCompatSeekBar) view.findViewById(R.id.sbCount);
        int count = sbCount.getProgress();
        tvCount = (TextView) view.findViewById(R.id.tvCount);
        tvCount.setText(count + "");
        sbCount.setOnSeekBarChangeListener(this);

        setDate(range, count);
        return view;
    }

    private void setDate(int range, int count) {
        ArrayList<String> xValues = new ArrayList<>();
        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            xValues.add(i + "");

            float val = (float) RandomUtils.getRandom(range / 2, range + 1);
            yValues.add(new Entry(val, i));
        }
        // create a data set and give it a type
        LineDataSet set1 = new LineDataSet(yValues, "Data set");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the dataSets

        // create a data object with the dataSets
        LineData data = new LineData(xValues, dataSets);
        lcCurve.setData(data);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onChartFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChartFragmentInteractionListener) {
            mListener = (OnChartFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChartFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int range = sbRange.getProgress();
        int count = sbCount.getProgress();
        tvRange.setText(range + "");
        tvCount.setText(count + "");

        setDate(range, count);
        lcCurve.invalidate(); // redraw
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface OnChartFragmentInteractionListener {
        void onChartFragmentInteraction(Uri uri);
    }
}
