package com.android.shnellers.heartrate.heart_rate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.HourlyHeartRateStats;
import com.android.shnellers.heartrate.servicealarms.HourlyAnalysisReceiver;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.android.shnellers.heartrate.Constants.Const.LESS_THAN_40;
import static com.android.shnellers.heartrate.Constants.Const.OVER_100;
import static com.android.shnellers.heartrate.Constants.Const.RESTING;

/**
 * Created by Sean on 01/04/2017.
 */

public class HourlyAnalysisFragment extends Fragment {

    private static final String TAG = "HourlyAnalysisFragment";

    @BindView(R.id.line_chart)
    protected LineChart mLineChart;

    @BindView(R.id.min)
    protected TextView mMin;

    @BindView(R.id.max)
    protected TextView mMax;

    @BindView(R.id.avg)
    protected TextView mAvg;

    @BindView(R.id.hour_value)
    protected TextView mHourValue;

    @BindView(R.id.over_100)
    protected TextView mOver100;

    @BindView(R.id.count_value)
    protected TextView mCount;

    @BindView(R.id.under_40)
    protected TextView mUnder40;

    private View mView;

    private HourlyHeartRateStats mHourStats;
    private ArrayList<HourlyHeartRateStats> mStatsList;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.home_hour_analysis_layout, container, false);

        ButterKnife.bind(this, mView);

        try {
            setLineChart();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return mView;
    }

    /**
     *
     */
    private HourlyAnalysisReceiver mHourlyAnalysisReceiver = new HourlyAnalysisReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);

            Log.d(TAG, "onReceive: ");
            HourlyHeartRateStats stats = intent.getParcelableExtra("stats");
            ArrayList<HourlyHeartRateStats> statsList = intent.getParcelableArrayListExtra("two_week_stats");
            System.out.println("LIST: " + statsList.size());
            ArrayList<Integer> heartRates = intent.getIntegerArrayListExtra("heart_rates");
            updateFragment(stats, statsList, heartRates);
            updateLineChart(statsList);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mHourlyAnalysisReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mHourlyAnalysisReceiver, new IntentFilter(HourlyAnalysisReceiver.HOURLY_STATS_UPDATED));
    }

    /**
     *
     * @param stats
     * @param statsList
     * @param heartRates
     */
    private void updateFragment(HourlyHeartRateStats stats, ArrayList<HourlyHeartRateStats> statsList, final ArrayList<Integer> heartRates) {

        SimpleDateFormat hourFormat = new SimpleDateFormat("HH a", Locale.UK);
        Calendar calendar = Calendar.getInstance();

        if (stats != null) {
            // Set the hourly analysis text views
            calendar.setTimeInMillis(stats.getDateTime());
            calendar.add(Calendar.HOUR_OF_DAY, 1);

            if (stats.getNumberOfHeartRates() > 0) {
                setTextView(mHourValue, hourFormat.format(stats.getDateTime()) + " - " +
                        hourFormat.format(calendar.getTimeInMillis()));
                setTextView(mCount, String.valueOf(stats.getNumberOfHeartRates()));
                setTextView(mMin, String.valueOf(stats.getMin()));
                setTextView(mMax, String.valueOf(stats.getMax()));
                setTextView(mAvg, String.valueOf(stats.getAverage()));

                setTextView(mOver100, String.valueOf(getCountHeartRatesOver100(heartRates)));
                setTextView(mUnder40, String.valueOf(getCountHeartRatesUnder40(heartRates)));
            }

        }

        if (statsList != null) {

        }

    }

    private int getCountHeartRatesOver100(final ArrayList<Integer> hrs) {
        int count = 0;

        if (hrs.size() > 0) {
            for (Integer hr : hrs) {
                if (hr >= 100) {
                    count++;
                }
            }
        }


        return count;
    }

    /**
     * Heart rates under 40.
     *
     * @param hrs
     * @return
     */
    private int getCountHeartRatesUnder40(final ArrayList<Integer> hrs) {
        int count = 0;

        for (Integer hr : hrs) {
            if (hr < 40) {
                count++;
            }
        }

        return count;
    }

    /**
     * Set text view value.
     *
     * @param textView
     * @param value
     */
    private void setTextView(final TextView textView, String value) {
        textView.setText(value);
    }

    /**
     * Setup the line chart.
     *
     * @throws ParseException
     */
    private void setLineChart() throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);


        mLineChart.getDescription().setEnabled(false);
        mLineChart.setVisibleYRangeMaximum(190, YAxis.AxisDependency.LEFT);
        mLineChart.getXAxis().setEnabled(false);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getAxisLeft().setDrawGridLines(false);

    }

    /**
     * Update the line chart.
     *
     * @param statsList
     */
    private void updateLineChart(final ArrayList<HourlyHeartRateStats> statsList) {
        ArrayList<Entry> lineEntries = new ArrayList<>();

        if (!statsList.isEmpty()) {
            for (int i = 0; i < statsList.size(); i++) {
                // int average = statsArrayList.get(i).getSum() / statsArrayList.get(i).getNumberOfHeartRates();
                HourlyHeartRateStats stat = statsList.get(i);
                if (stat != null) {
                    lineEntries.add(new Entry(i, statsList.get(i).getAverage()));
                }
            }

            LineDataSet restingDataSet = createLineDataSet(lineEntries, RESTING);

            LineData set = new LineData(restingDataSet);
            mLineChart.setData(set);

            mLineChart.invalidate();
        }
    }

    /**
     * Create the line chart data set.
     *
     * @param entries
     * @param val
     * @return
     */
    private LineDataSet createLineDataSet(ArrayList<Entry> entries, final String val) {

        String type;

        if (val.equals(OVER_100)) {
            type = "Maximum";
        } else if (val.equals(LESS_THAN_40)) {
            type = "Minimum";
        } else {
            type = "Average Resting Rates for Hour Over Last Two Weeks";
        }

        int color = ContextCompat.getColor(getContext(), R.color.resting);

        LineDataSet lineDataSet = new LineDataSet(entries, type);
        lineDataSet.setColor(color);
        lineDataSet.setFillColor(color);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(true);

        lineDataSet.setValueTextSize(9f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setDrawValues(false);
        //lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        lineDataSet.setFormSize(15.f);

        return lineDataSet;
    }
}
