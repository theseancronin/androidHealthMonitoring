package com.android.shnellers.heartrate.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityRecognitionDBHelper;
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
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TIME_MILLIS;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.DATE;

/**
 * Created by Sean on 07/02/2017.
 */

public class ActivityDashboard extends Fragment {

    @BindView(R.id.chart_calories)
    LineChart mCaloriesChart;

    @BindView(R.id.chart_distance)
    BarChart mDistanceChart;

    @BindView(R.id.chart_steps)
    BarChart mStepsChart;

    @BindView(R.id.chart_time)
    LineChart mTimeChart;

    @BindView(R.id.day)
    protected Button mDayBtn;

    @BindView(R.id.seven_day)
    protected Button mSevenBtn;

    @BindView(R.id.thirty_day)
    protected Button mThirtyBtn;

    @BindView(R.id.year)
    protected Button mYearBtn;

    private boolean mDayActive;
    private boolean mSevenActive;
    private boolean mThirtyActive;
    private boolean mYearActive;

    private Button currentActiveBtn;

    private ActivityRecognitionDatabase mDatabase;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        ButterKnife.bind(this, view);

        mCaloriesChart.getLegend().setEnabled(false);
        mCaloriesChart.getDescription().setEnabled(false);

        mDistanceChart.getLegend().setEnabled(false);
        mDistanceChart.getDescription().setEnabled(false);

        mStepsChart.getLegend().setEnabled(false);
        mStepsChart.getDescription().setEnabled(false);

        mTimeChart.getLegend().setEnabled(false);
        mTimeChart.getDescription().setEnabled(false);

        mDatabase = new ActivityRecognitionDatabase(getActivity());

        mDayActive = true;
        mSevenActive = false;
        mThirtyActive = false;
        mYearActive = false;

        setActiveButton(mDayBtn);
        setCurrentActiveBtn(mDayBtn);

        return view;
    }

    private void displayCharts() {



        SQLiteDatabase db = new ActivityRecognitionDBHelper(getActivity()).getReadableDatabase();

        String query = getQueryString();

        Cursor c = db.rawQuery(query, null);

        c.close();

        db.close();
    }

    private String getQueryString() {

        String query;

        Calendar starttime = Calendar.getInstance();
        Calendar endtime = Calendar.getInstance();



        if (mDayActive) {

            query = "SELECT * FROM " + TABLE_RECOGNITION +
                    " WHERE " + DATE + " = (SELECT * FROM DATETIME('now')) ORDER BY " + TIME_MILLIS + " ASC;";


        } else if (mSevenActive) {

            starttime.add(Calendar.DAY_OF_YEAR, -7);

            query = "SELECT * FROM " + TABLE_RECOGNITION +
                    " WHERE " + TIME_MILLIS + " >= " + starttime.getTimeInMillis() + " ORDER BY " + TIME_MILLIS + " ASC;";

        } else if (mThirtyActive) {

            starttime.add(Calendar.DAY_OF_YEAR, -30);

            query = "SELECT * FROM " + TABLE_RECOGNITION +
                    " WHERE " + TIME_MILLIS + " >= " + starttime.getTimeInMillis() + " ORDER BY " + TIME_MILLIS + " ASC;";
        } else {
            starttime.add(Calendar.DAY_OF_YEAR, -365);

            query = "SELECT * FROM " + TABLE_RECOGNITION +
                    " WHERE " + TIME_MILLIS + " >= " + starttime.getTimeInMillis() + " ORDER BY " + TIME_MILLIS + " ASC;";
        }

        return query;
    }

    @OnClick(R.id.day)
    public void dayView() {


        if (!mDayActive) {
            mDayActive = true;
            mSevenActive = false;
            mThirtyActive = false;
            mYearActive = false;

            setActiveButton(mDayBtn);

            displayCharts();
        } else {
            mDayActive = false;
            deactivateButton(mDayBtn);
        }

    }



    @OnClick(R.id.seven_day)
    public void sevenView() {
        if (!mDayActive) {
            mDayActive = false;
            mSevenActive = true;
            mThirtyActive = false;
            mYearActive = false;

            setActiveButton(mSevenBtn);
        } else {
            mSevenActive = false;
            deactivateButton(mSevenBtn);
        }
    }

    @OnClick(R.id.thirty_day)
    public void thirtyView() {
        if (!mDayActive) {
            mDayActive = false;
            mSevenActive = false;
            mThirtyActive = true;
            mYearActive = false;

            setActiveButton(mThirtyBtn);
        } else {
            mThirtyActive = false;
            deactivateButton(mThirtyBtn);
        }
    }

    @OnClick(R.id.year)
    public void yearView() {
        if (!mDayActive) {
            mDayActive = false;
            mSevenActive = false;
            mThirtyActive = false;
            mYearActive = true;

            setActiveButton(mYearBtn);
        } else {
            mYearActive = false;
            deactivateButton(mYearBtn);
        }
    }


    private void setActiveButton(final Button btn) {

        if (getCurrentActiveBtn() != null) {
            if (getCurrentActiveBtn().getId() != btn.getId()) {
                deactivateButton(getCurrentActiveBtn());
                btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.layout_box_border_fill));
                getCurrentActiveBtn().setBackground(ContextCompat.getDrawable(getContext(), R.drawable.layout_box_border_3px));
                setCurrentActiveBtn(btn);
            }
        } else {
            btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.layout_box_border_fill));
        }


    }

    private void deactivateButton(final Button btn) {

        btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.layout_box_border_3px));

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

    public Button getCurrentActiveBtn() {
        return currentActiveBtn;
    }

    public void setCurrentActiveBtn(final Button currentActiveBtn) {
        this.currentActiveBtn = currentActiveBtn;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
