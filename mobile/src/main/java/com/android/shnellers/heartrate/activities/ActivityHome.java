package com.android.shnellers.heartrate.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityDatabase;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.StepsDBHelper;
import com.android.shnellers.heartrate.models.ActivityModel;
import com.android.shnellers.heartrate.models.ActivityStats;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.android.shnellers.heartrate.Constants.Const.CYCLING;
import static com.android.shnellers.heartrate.Constants.Const.RUNNING;
import static com.android.shnellers.heartrate.Constants.Const.WALKING;
import static com.android.shnellers.heartrate.R.id.combined_chart;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TIME_MILLIS;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.DATE;

/**
 * Created by Sean on 18/01/2017.
 */

public class ActivityHome extends Fragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, OnChartValueSelectedListener {

    private static final String TAG = "ActivityHomePage";

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

    @BindView(combined_chart)
    protected CombinedChart mCombinedChart;

    @BindView(R.id.activity_spinner)
    protected Spinner mActivitySpinner;

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

    private ArrayList<ActivityStats> mActivityStats;

    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, final Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activities_layout_home, container, false);

        ButterKnife.bind(this, mView);
        findViews();

        setupChart();

        mDayActive = false;
        mSevenActive = true;
        mThirtyActive = false;
        mYearActive = false;

        setActiveButton(mSevenBtn);
        setCurrentActiveBtn(mSevenBtn);

        mActivityStats = mActivityRecognitionDB.getActivityStats(getQueryString());

        setActivityCards();

        setActivitySpinner();


        return mView;
    }

    private void setActivityCards() {



        ArrayList<ActivityStats> totalStats = new ArrayList<>();
        ActivityStats walk = new ActivityStats(WALKING);
        ActivityStats run = new ActivityStats(RUNNING);
        ActivityStats cycle = new ActivityStats(CYCLING);



        int walkMins = 0;
        int walkSum = 0;
        int walkCount = 0;
        int walkDistance = 0;
        int walkCalories = 0;

        int runMins = 0;
        int runSum = 0;
        int runCount = 0;
        int runDistance = 0;
        int runCalories = 0;

        int cycleMins = 0;
        int cycleSum = 0;
        int cycleCount = 0;
        int cycleDistance = 0;
        int cycleCalories = 0;

        for (ActivityStats stat : mActivityStats) {

            if (stat.getActivityName().equals(WALKING)) {

                walkMins += stat.getMinutes();
                walkSum += stat.getAvgHeartRate();
                walkDistance += stat.getDistance();
                walkCalories += stat.getCalories();
                walkCount++;

            } else if (stat.getActivityName().equals(RUNNING)) {

                runMins += stat.getMinutes();
                runSum += stat.getAvgHeartRate();
                runDistance += stat.getDistance();
                runCalories += stat.getCalories();
                runCount++;

            } else {

                cycleMins += stat.getMinutes();
                cycleCount += stat.getAvgHeartRate();
                cycleDistance += stat.getDistance();
                cycleCalories += stat.getCalories();
                cycleCount++;

            }

        }

        walk.setMinutes(walkMins);
        walk.setDistance(walkDistance);
        walk.setCalories(walkCalories);

        if (walkCount > 0) {
            walk.setAvgHeartRate(walkSum / walkCount);
        }


        run.setMinutes(runMins);
        run.setDistance(runDistance);
        run.setCalories(runCalories);
        if (runCount > 0) {
            run.setAvgHeartRate(runSum / runCalories);
        }

        cycle.setMinutes(cycleMins);
        cycle.setDistance(cycleDistance);
        cycle.setCalories(cycleCalories);
        if (cycleCount > 0) {
            cycle.setAvgHeartRate(cycleSum / cycleCount);
        }

        totalStats.add(walk);
        totalStats.add(run);
        totalStats.add(cycle);

        RecyclerView rv = (RecyclerView) mView.findViewById(R.id.activity_card_list);
        rv.setHasFixedSize(true);

        ActivityCardRecycler adapter = new ActivityCardRecycler(totalStats, getActivity());
        rv.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);

    }

    private void setupChart() {


       // IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(mCombinedChart);
//
//        XAxis xAxis = mCombinedChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setLabelRotationAngle(45);
//        //xAxis.setValueFormatter(new MyAxisValueFormatter(DateHelper.getLast7DaysAsDate()));
//        // xAxis.setTypeface(mTfLight);
//        xAxis.setDrawGridLines(false);
//        xAxis.setGranularity(1f); // only intervals of 1 day
//        xAxis.setLabelCount(7);
//       // xAxis.setValueFormatter(xAxisFormatter);
//
//        YAxis leftAxis = mCombinedChart.getAxisLeft();
//        //leftAxis.setTypeface(mTfLight);
//        leftAxis.setLabelCount(8, false);
//        // leftAxis.setValueFormatter(custom);
//        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        leftAxis.setSpaceTop(15f);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)


       // setData(3, 60);


    }

    private void findViews() {

        db = new HeartRateDatabase(getActivity());

        mDBHelper = new StepsDBHelper(getActivity());
        mActivityRecognitionDB = new ActivityRecognitionDatabase(getActivity());
        mActivityDB = new ActivityDatabase(getActivity());


        mWalkTime = (TextView) mView.findViewById(R.id.walking_time);
        mRunTime = (TextView) mView.findViewById(R.id.running_time);
        mCycleTime = (TextView) mView.findViewById(R.id.cycling_time);
        mTotalDistance = (TextView) mView.findViewById(R.id.total_distance);
        mTotalCalories = (TextView) mView.findViewById(R.id.total_calories);
        mTotalTime = (TextView) mView.findViewById(R.id.total_time);

       // mDashboardView = (Button) mView.findViewById(R.id.dashboard);
        mNewActivity = (Button) mView.findViewById(R.id.new_activity);
        mAnalysisView = (Button) mView.findViewById(R.id.analysis);
        mWalkingCard = (CardView) mView.findViewById(R.id.walking_card);
        mRunningCard = (CardView) mView.findViewById(R.id.running_card);
        mCyclingCard = (CardView) mView.findViewById(R.id.cycling_card);

    }

    private void setData() {

        mCombinedChart.clear();

        CombinedData data = new CombinedData();


        updateChartData();

        BarData activity = getBarData();
        LineData stats = getHeartRateData();

        if (activity != null) {
            data.setData(activity);
        }

        if (stats != null) {
            data.setData(stats);
        }
       // data.setData(getCalorieData())

        if (data.getDataSetCount() > 0) {
            mCombinedChart.setData(data);
        }


        mCombinedChart.invalidate();

    }

    private void updateChartData() {


    }

    private LineData getCalorieData() {
        return null;
    }

    /**
     * Set the line chart for the heart rate.
     *
     * @return
     */
    private LineData getHeartRateData() {
        // Create entries list for the graph
        ArrayList<Entry> heartRates = new ArrayList<>();
        ArrayList<Entry> calories = new ArrayList<>();
        ArrayList<Entry> distance = new ArrayList<>();

        // Go through each of the activities and get the activity data
        if (mActivityStats != null && mActivityStats.size() > 0) {
            for (int x = 0; x < mActivityStats.size(); x++) {

                ActivityStats stat = mActivityStats.get(x);

                Log.d(TAG, "NAME: " + stat.getActivityName());
                // We only want to add to the chart the heart rates that are over
                // 0.
                if (stat.getActivityName().equals(getActivitySpinnerValue())) {
                    Log.d(TAG, "HR: " + String.valueOf(stat.getAvgHeartRate()));
                    if (stat.getAvgHeartRate() > 0) {
                        heartRates.add(new Entry(x, stat.getAvgHeartRate()));
                    }

                    if (stat.getCalories() > 0) {
                        calories.add(new Entry(x, stat.getCalories()));
                    }
                    if (stat.getDistance() > 0) {
                        distance.add(new Entry(x, (float) stat.getDistance()));
                    }


                }
            }
        }


        if (!calories.isEmpty() || !heartRates.isEmpty()) {

            List<ILineDataSet> sets = new ArrayList<>();

            LineDataSet hrDataSet = new LineDataSet(heartRates, "Heart Rate");
            hrDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.over_120));
            hrDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.over_120));
            hrDataSet.setCircleColorHole(ContextCompat.getColor(getActivity(), R.color.over_120));
            hrDataSet.setLineWidth(2);

            if (hrDataSet.getEntryCount() > 0) {
                sets.add(hrDataSet);
            }

            LineDataSet caloriesDataSet = new LineDataSet(calories, "Calories");
            caloriesDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.over_70));
            hrDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.over_70));
            hrDataSet.setCircleColorHole(ContextCompat.getColor(getActivity(), R.color.over_70));
            caloriesDataSet.setLineWidth(2);

            if (caloriesDataSet.getEntryCount() > 0) {
                sets.add(caloriesDataSet);
            }

            LineDataSet distanceDataSet = new LineDataSet(distance, "Distance");
            distanceDataSet.setColor(ContextCompat.getColor(getContext(), R.color.over_40));
            hrDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.over_40));
            hrDataSet.setCircleColorHole(ContextCompat.getColor(getActivity(), R.color.over_40));
            distanceDataSet.setLineWidth(2);

            if (caloriesDataSet.getEntryCount() > 0) {
                sets.add(distanceDataSet);
            }
            return new LineData(sets);

        }

        return null;
    }

//    private ArrayList<Entry> getLineDataEntryList() {
//        ArrayList<Entry> list = new ArrayList<>();
//
//        for (int x = 0; x < mActivityStats.size(); x++) {
//
//            ActivityStats stat = mActivityStats.get(x);
//
//            // We only want to add to the chart the heart rates that are over
//            // 0.
//            if (stat.getActivityName().equals(WALKING)) {
//
//                if (stat.getAvgHeartRate() > 0) {
//                    heartRates.add(new Entry(x, stat.getAvgHeartRate()));
//                }
//
//                if (stat.getCalories() > 0) {
//                    calories.add(new Entry(x, stat.getCalories()));
//                }
//
//            }
//        }
//    }

    private BarData getBarData() {

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        if (mActivityStats != null && mActivityStats.size() > 0) {
            for (int x = 0; x < mActivityStats.size(); x++) {

                ActivityStats stat = mActivityStats.get(x);

                System.out.println("X: " + x + " Y: " + stat.getMinutes());
                if (stat.getActivityName().equals(WALKING)) {
                    barEntries.add(new BarEntry(x, stat.getMinutes()));
                }
            }
        }

        if (!barEntries.isEmpty()) {

            BarDataSet data = new BarDataSet(barEntries, getActivitySpinnerValue());
            data.setColor(getBarColor());

            return new BarData(data);

        }

        return null;
    }

    /**
     * Gets and returns the color for the bar chart bars, depending on the
     * activity.
     *
     * @return
     */
    private int getBarColor() {

        int color;

        if (getActivitySpinnerValue().equals(WALKING)) {
            color =ContextCompat.getColor(getContext(), R.color.walking);
        } else if ( getActivitySpinnerValue().equals(RUNNING)) {
            color = ContextCompat.getColor(getContext(), R.color.running);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.cycling);
        }

        return color;
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

        }
    }

    private void viewActivityAnalysis() {

    }

    private void startNewActivity() {
        Intent intent = new Intent(getContext(), ActivityTypeDialog.class);
        startActivity(intent);
    }

    private void viewActivityDashboard() {

        Intent intent = new Intent(getContext(), ActivityDashboard.class);
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

    /**
     * Sets up the spinner to determine what values to show.
     */
    private void setActivitySpinner() {
        ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                getContext(), R.array.activity, R.layout.spinner_item
        );

        activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActivitySpinner.setAdapter(activityList);

        mActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Get the value selected in the dropdown.
     *
     * @return
     */
    private String getActivitySpinnerValue() {
        return mActivitySpinner.getSelectedItem().toString();
    }
    private String getQueryString() {

        String query;

        Calendar starttime = Calendar.getInstance();
        Calendar endtime = Calendar.getInstance();

        if (mDayActive) {

            query = "SELECT * FROM " + TABLE_RECOGNITION +
                    " WHERE " + DATE + " = (SELECT DATETIME('now')) ORDER BY " + TIME_MILLIS + " ASC;";


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

            setActivityData();
        } else {
            mDayActive = false;
            deactivateButton(mDayBtn);
        }

    }

    private void setActivityData() {
        mActivityStats = mActivityRecognitionDB.getActivityStats(getQueryString());
        setData();
        setActivityCards();
    }


    @OnClick(R.id.seven_day)
    public void sevenView() {
        if (!mSevenActive) {
            mDayActive = false;
            mSevenActive = true;
            mThirtyActive = false;
            mYearActive = false;

            setActiveButton(mSevenBtn);
            setActivityData();
        } else {
            mSevenActive = false;
            deactivateButton(mSevenBtn);
        }
    }

    @OnClick(R.id.thirty_day)
    public void thirtyView() {
        if (!mThirtyActive) {
            mDayActive = false;
            mSevenActive = false;
            mThirtyActive = true;
            mYearActive = false;

            setActiveButton(mThirtyBtn);
            setActivityData();
        } else {
            mThirtyActive = false;
            deactivateButton(mThirtyBtn);
        }
    }

    @OnClick(R.id.year)
    public void yearView() {
        if (!mYearActive) {
            mDayActive = false;
            mSevenActive = false;
            mThirtyActive = false;
            mYearActive = true;

            setActiveButton(mYearBtn);
            setActivityData();
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

    public Button getCurrentActiveBtn() {
        return currentActiveBtn;
    }

    public void setCurrentActiveBtn(final Button currentActiveBtn) {
        this.currentActiveBtn = currentActiveBtn;
    }

}
