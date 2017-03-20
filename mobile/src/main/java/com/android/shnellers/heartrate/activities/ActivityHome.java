package com.android.shnellers.heartrate.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.charts.MyAxisValueFormatter;
import com.android.shnellers.heartrate.database.ActivityDatabase;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.StepsDBHelper;
import com.android.shnellers.heartrate.helpers.DateHelper;
import com.android.shnellers.heartrate.models.ActivityModel;
import com.android.shnellers.heartrate.models.ActivityStats;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sean on 18/01/2017.
 */

public class ActivityHome extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private static final String TAG = "ActivityHomePage";

    private View mView;

    private ImageButton extrasBtn;

    private StepsDBHelper mDBHelper;

    private ActivityRecognitionDatabase mActivityRecognitionDB;

    private HeartRateDatabase db;

    private ActivityDatabase mActivityDB;

    private TextView mStepsView;
    private TextView mCaloriesView;
    private TextView mDistanceView;
    private TextView mWalkTime;
    private TextView mRunTime;
    private TextView mCycleTime;
    private TextView mTotalDistance;
    private TextView mTotalTime;
    private TextView mTotalCalories;

    private Button mDashboardView;
    private Button mNewActivity;
    private Button mAnalysisView;

    private CardView mWalkingCard;
    private CardView mRunningCard;
    private CardView mCyclingCard;

    private LinearLayout mWalkLine;
    private LinearLayout mRunLine;
    private LinearLayout mCycleLine;

    private PieChart mPieChart;

    private BarChart mBarChart;

    private HorizontalBarChart mHorizontalBarChart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_layout_home);

        findViews();

        setupChart();

        mDashboardView.setOnClickListener(this);
        mNewActivity.setOnClickListener(this);
        mAnalysisView.setOnClickListener(this);

        setActivityDisplay();

        //setActivityRecognitionDailyStats();

        setActivityCards();

    }

    private void setActivityCards() {

        ArrayList<ActivityStats> activityStats = mActivityRecognitionDB.getActivityStats(Constants.Const.TODAY);

        RecyclerView rv = (RecyclerView) findViewById(R.id.activity_card_list);
        rv.setHasFixedSize(true);

        ActivityCardRecycler adapter = new ActivityCardRecycler(activityStats, this);
        rv.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        rv.setLayoutManager(manager);

    }

    private void setupChart() {
        mBarChart.setOnChartValueSelectedListener(this);

        mBarChart.getDescription().setEnabled(false);

        mBarChart.setDrawGridBackground(false);
        mBarChart.setDrawBarShadow(false);

        mBarChart.setDrawValueAboveBar(false);
        mBarChart.setHighlightFullBarEnabled(false);

        mBarChart.getLegend().setEnabled(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mBarChart);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(new MyAxisValueFormatter(DateHelper.getLast7DaysAsDate()));
        // xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis leftAxis = mBarChart.getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setLabelCount(8, false);
        // leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)


       // setData(3, 60);


    }

    private void findViews() {

        db = new HeartRateDatabase(this);

        mDBHelper = new StepsDBHelper(this);
        mActivityRecognitionDB = new ActivityRecognitionDatabase(this);
        mActivityDB = new ActivityDatabase(this);

        mStepsView = (TextView) findViewById(R.id.total_steps);
        mWalkTime = (TextView) findViewById(R.id.walking_time);
        mRunTime = (TextView) findViewById(R.id.running_time);
        mCycleTime = (TextView) findViewById(R.id.cycling_time);
        mTotalDistance = (TextView) findViewById(R.id.total_distance);
        mTotalCalories = (TextView) findViewById(R.id.total_calories);
        mTotalTime = (TextView) findViewById(R.id.total_time);

        mDashboardView = (Button) findViewById(R.id.dashboard);
        mNewActivity = (Button) findViewById(R.id.new_activity);
        mAnalysisView = (Button) findViewById(R.id.analysis);
        mWalkingCard = (CardView) findViewById(R.id.walking_card);
        mRunningCard = (CardView) findViewById(R.id.running_card);
        mCyclingCard = (CardView) findViewById(R.id.cycling_card);

        mBarChart = (BarChart) findViewById(R.id.bar_chart);
    }

    private void setData(int count, float range) {
        float spaceForBar = 10f;

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        float start = 1f;

        ArrayList<RecognizedActivity> activities =
                mActivityRecognitionDB.getLast7DaysRecords();

        int totalMinutes = 0;

        // Create the different bars for each recognized activity
        if (!activities.isEmpty()) {
            for (int i = 1; i < activities.size(); i++) {

                RecognizedActivity activity = activities.get(i);

                if (activity.getHours() >= 1) {
                    totalMinutes += activity.getHours() * 60;
                }

                if (activity.getMinutes() >= 1) {
                    totalMinutes += activity.getSeconds();
                }

                if (activity.getType() == DetectedActivity.WALKING) {
                    yVals1.add(new BarEntry(i, activity.getMinutes()));
                } else if (activity.getType() == DetectedActivity.RUNNING) {
                    yVals1.add(new BarEntry(i, activity.getMinutes()));
                } else if (activity.getType() == DetectedActivity.ON_BICYCLE) {
                    yVals1.add(new BarEntry(i, activity.getMinutes()));
                }
            }
        }

        BarDataSet set1;



        if (mBarChart.getData() != null && mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "DataSet 1");
//            set1.setValues(yVals1);
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            //data.setValueTypeface(mTfLight);
            //data.setBarWidth(barWidth);

            mBarChart.setData(data);
        }

        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.colorWalk));
        colors.add(ContextCompat.getColor(this, R.color.colorRun));
        colors.add(ContextCompat.getColor(this, R.color.colorCycle));
        //set1.setColors(colors);
    }

    private ArrayList<ActivityStats> setActivityRecognitionDailyStats(String today) {

        ArrayList<ActivityStats> stats = new ArrayList<>();
        ArrayList<RecognizedActivity> activities =
                mActivityRecognitionDB.getTodaysRecognizedActivities();

        ViewGroup.LayoutParams layoutParams;

        Log.d(TAG, "SIZE: " + String.valueOf(activities.size()));

        // Make sure activities are stored
        if (!activities.isEmpty()) {

            int distance = 0;
            int totalMinutes = 0;

            StringBuilder builder = new StringBuilder();
            String time;


            for (RecognizedActivity activity : activities) {

                // Get the hours and minutes the activity was monitored for, and also
                // the type of activity.
                int hours = activity.getHours();
                int minutes = activity.getMinutes();

                int type = activity.getType();

                if (hours >= 1 || minutes >= 1) {

                    // Get the hours of an activity detected and create a string reference.
                    if (hours >= 1) {
                        time = String.valueOf(hours) + " hr ";
                        totalMinutes += hours * 60;
                        builder.append(time);
                    }
                    // Creates a string reference to the amount of minutes detected
                    if (minutes >= 1) {
                        time = String.valueOf(minutes) + " min";
                        totalMinutes += minutes;
                        builder.append(time);
                    }

                    // Set the line widths for each of the activities
//                    if (type == DetectedActivity.WALKING) {
//                        setMinutes(mWalkTime, builder.toString(), minutes);
//                    } else if (type == DetectedActivity.RUNNING) {
//                        setMinutes(mRunTime, builder.toString(), minutes);
//                    } else {
//                        setMinutes(mCycleTime, builder.toString(), minutes);
//                    }

                    distance += Calculations.convertTimeToKM(activity.getType(), 100);
                }

                totalMinutes +=  activity.getSeconds();

                Log.d(TAG, "SECOND " + String.valueOf(activity.getSeconds()));

                setDistanceCovered(distance);
                setCaloriesBurned(totalMinutes);
                setTotalTimeDetected(totalMinutes);

            }

        }
        return null;
    }

    private void setCaloriesBurned(final int time) {
        int burned = Calculations.caloriesBurnedMen(32, 159, db.averageHeartRate(), time);
       // Log.d(TAG, "setCaloriesBurned: BURNED " + String.valueOf(burned));
        if (burned > 0) {
           // Log.d(TAG, "setCaloriesBurned: MORE THAN 0");
            mTotalCalories.setText(String.valueOf(burned));
        } else {
            mTotalCalories.setText(getResources().getString(R.string.no_calories));
           // Log.d(TAG, "setCaloriesBurned: NOT MORE");
        }
       // Log.d(TAG, "setCaloriesBurned: BURNED" + String.valueOf(burned));
    }

    private void setTotalTimeDetected(final int totalMinutes) {

        if (totalMinutes > 0) {
            mTotalTime.setText(String.valueOf(totalMinutes) + " min");
        } else {
            mTotalTime.setText(getResources().getString(R.string.no_minutes));
        }

    }

    /**
     * Draw the line to visualize the amount of activity and also set the
     *
     * @param cardView
     * @param timeView
     * @param minStr
     * @param width
     * @param height
     * @param minutes
     */
    private void drawActivityLine(CardView cardView, TextView timeView,
                                  String minStr, int width, int height, int minutes) {

        // If the minutes are more than one we draw the line.
        if (minutes >= 1) {

            timeView.setText(minStr);

        } else {

            timeView.setText("0 min");

        }
    }

    private void setMinutes(TextView timeView, String minStr, int minutes) {
        // If the minutes are more than one we draw the line.
        if (minutes >= 1) {

            timeView.setText(minStr);

        } else {

            timeView.setText("0 min");

        }
    }

    /**
     * Set the total amount for distance covered.
     *
     * @param distance
     */
    private void setDistanceCovered(final int distance) {

        Log.d(TAG, "setDistanceCovered: DISTANCE " + String.valueOf(distance));

        if (distance > 0) {
            mTotalDistance.setText(String.valueOf(distance) + " km");
        } else {
            mTotalDistance.setText("0 km");
        }

    }

    private void setActivityDisplay() {
        //DateStepModel model = mDBHelper.getTodaysStepDetails();

        ActivityModel model = mActivityDB.getLastActivity();

        StringBuilder builder = new StringBuilder();
        String stepsStr = "";

        if (model != null) {
            mStepsView.setText(String.valueOf(model.getSteps()));

            int time = model.getTimeTaken();
            int type = getActivityType(model.getType());
            double km = Calculations.convertTimeToKM(type, model.getTimeTaken());
            mDistanceView.setText(String.valueOf(km));
            mCaloriesView.setText(String.valueOf(model.getCaloriesBurned()));
            HashMap<String, Integer> timeMap = Calculations.convertMillisecondsToTime(time);

            String t = timeMap.get("hour") + " hr " + timeMap.get("minutes") + " min";



        } else {
            stepsStr = "0/6000";
            mStepsView.setText(stepsStr);
        }


    }

    private int getActivityType(String  t) {
        if (t.equals("Walking")) {
            return DetectedActivity.WALKING;
        } else if (t.equals("Running")) {
            return DetectedActivity.RUNNING;
        } else {
            return DetectedActivity.ON_BICYCLE;
        }
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
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
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.dashboard:
                viewActivityDashboard();
                break;
            case R.id.new_activity:
                startNewActivity();
                break;
            case R.id.analysis:
                viewActivityAnalysis();
                break;
            case R.id.extras:

                break;
        }
    }

    private void viewActivityAnalysis() {

    }

    private void startNewActivity() {
        Intent intent = new Intent(this, ActivityTypeDialog.class);
        startActivity(intent);
    }

    private void viewActivityDashboard() {

        Intent intent = new Intent(this, ActivityDashboard.class);
        startActivity(intent);

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public class DatabaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }


}
