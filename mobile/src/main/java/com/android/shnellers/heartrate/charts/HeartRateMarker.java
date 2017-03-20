package com.android.shnellers.heartrate.charts;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.helpers.DateHelper;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by Sean on 21/02/2017.
 */

public class HeartRateMarker extends MarkerView {

    @BindView(R.id.heart_marker_rate)
    protected TextView mHeartRate;

    @BindView(R.id.heart_check_activity_type)
    protected TextView mActivityType;

    @BindView(R.id.date)
    protected TextView mDateView;

    private CombinedChart mCombinedChart;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public HeartRateMarker(Context context, int layoutResource, CombinedChart chart) {
        super(context, layoutResource);

        mCombinedChart = chart;

        ButterKnife.bind(this);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        XAxis xAxis = mCombinedChart.getXAxis();

        IAxisValueFormatter formatter = xAxis.getValueFormatter();

        String date = formatter.getFormattedValue(e.getX(), xAxis);

        Log.d(TAG, "refreshContent: ENTRY " + date);

        String bpm = String.valueOf((int)e.getY());

        SimpleDateFormat chartDateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        mHeartRate.setText(bpm);

        mDateView.setText(date);

        try {
            Date dateObject = DateHelper.convertStringToDateTime(date, chartDateFormat);

            HeartRateDatabase db = new HeartRateDatabase(getContext());
            HeartRateObject heartRateObject = db.getDBEntry((int) e.getY(), dbDateFormat.format(dateObject.getTime()));

            if (heartRateObject != null) {
                mActivityType.setText(heartRateObject.getType());
            } else {
                mActivityType.setText("--");
            }

        } catch (ParseException e1) {

            e1.printStackTrace();
        }

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }


}
