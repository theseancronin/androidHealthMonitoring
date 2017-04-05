package com.android.shnellers.heartrate.heart_rate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.android.shnellers.heartrate.R;
import com.github.mikephil.charting.charts.LineChart;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 04/04/2017.
 */

public class HourAnalysisFragment extends Fragment {

    @BindView(R.id.top_spinner)
    protected Spinner mTopSpinner;

    @BindView(R.id.line_chart)
    protected LineChart mLineChart;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_hour_analysis_layout, container, false);

        ButterKnife.bind(this, view);

        return view;
    }
}
