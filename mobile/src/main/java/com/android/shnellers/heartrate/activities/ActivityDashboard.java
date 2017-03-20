package com.android.shnellers.heartrate.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 07/02/2017.
 */

public class ActivityDashboard extends AppCompatActivity {

    @BindView(R.id.chart_calories)
    LineChart mCaloriesChart;

    @BindView(R.id.chart_distance)
    BarChart mDistanceChart;

    @BindView(R.id.chart_steps)
    BarChart mStepsChart;

    @BindView(R.id.chart_time)
    LineChart mTimeChart;

    private ActivityRecognitionDatabase mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ButterKnife.bind(this);

        mCaloriesChart.getLegend().setEnabled(false);
        mCaloriesChart.getDescription().setEnabled(false);



        mDistanceChart.getLegend().setEnabled(false);
        mDistanceChart.getDescription().setEnabled(false);

        mStepsChart.getLegend().setEnabled(false);
        mStepsChart.getDescription().setEnabled(false);

        mTimeChart.getLegend().setEnabled(false);
        mTimeChart.getDescription().setEnabled(false);

        mDatabase = new ActivityRecognitionDatabase(this);

       // setCaloriesChartData();

      //  setDistanceChartData();
    }

    private void setDistanceChartData() {

        ArrayList<RecognizedActivity> activities = mDatabase.getLast7DaysRecords();

        ArrayList<BarEntry> values = new ArrayList<>();

        if (!activities.isEmpty()) {

            for (int i = 0; i < activities.size(); i++) {

                RecognizedActivity activity = activities.get(i);

                double km = Calculations.convertTimeToKM(activity.getType(), activity.getMinutes());

                values.add(new BarEntry(i, (int)km));
            }

            BarDataSet set1;

            if (mDistanceChart.getData() != null &&
                    mDistanceChart.getData().getDataSetCount() > 0) {
                set1 = (BarDataSet) mDistanceChart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                mDistanceChart.getData().notifyDataChanged();
                mDistanceChart.notifyDataSetChanged();
            } else {
                set1 = new BarDataSet(values, "Distance");
                //set1.setColors(ColorTemplate.MATERIAL_COLORS);

                ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                dataSets.add(set1);

                BarData data = new BarData(dataSets);
                data.setValueTextSize(10f);
                //data.setValueTypeface(mTfLight);
                data.setBarWidth(0.9f);

                mDistanceChart.setData(data);
            }
        }
    }

    private void setCaloriesChartData() {

        ArrayList<RecognizedActivity> activities = mDatabase.getLast7DaysRecords();

        ArrayList<Entry> values = new ArrayList<>();

        if (!activities.isEmpty()) {

            for (int i = 0; i < activities.size(); i++) {

                RecognizedActivity activity = activities.get(i);

                int burned = Calculations.caloriesBurnedMen(32, 154, 84, activity.getMinutes());

                values.add(new Entry(i, burned));

            }

            LineDataSet set1;

            if (mCaloriesChart.getData() != null &&
                    mCaloriesChart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet)mCaloriesChart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                mCaloriesChart.getData().notifyDataChanged();
                mCaloriesChart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values, "DataSet 1");

                // set the line to be drawn like this "- - - - - -"
//                set1.enableDashedLine(10f, 5f, 0f);
//                set1.enableDashedHighlightLine(10f, 5f, 0f);
//                set1.setColor(Color.BLACK);
//                set1.setCircleColor(Color.BLACK);
//                set1.setLineWidth(1f);
//                set1.setCircleRadius(3f);
//                set1.setDrawCircleHole(false);
//                set1.setValueTextSize(9f);
//                set1.setDrawFilled(true);
//                set1.setFormLineWidth(1f);
//                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//                set1.setFormSize(15.f);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                   // set1.setFillDrawable(drawable);
                }
                else {
                    set1.setFillColor(Color.BLACK);
                }

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1); // add the datasets

                // create a data object with the datasets
                LineData data = new LineData(dataSets);

                // set data
                mCaloriesChart.setData(data);
            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
