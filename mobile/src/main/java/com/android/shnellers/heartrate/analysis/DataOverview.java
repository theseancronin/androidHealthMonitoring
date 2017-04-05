package com.android.shnellers.heartrate.analysis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.charts.MyAxisValueFormatter;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.helpers.DateHelper;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 28/02/2017.
 */

public class DataOverview extends AppCompatActivity {

    public static final String TAG = "DataOverview";

    @BindView(R.id.activity_spinner)
    protected Spinner mActivitySpinner;

    @BindView(R.id.total_beats)
    protected TextView mTotalBeats;

    @BindView(R.id.average_beats)
    protected TextView mAverageBeats;

    @BindView(R.id.lowest_bpm)
    protected TextView mLowestBpm;

    @BindView(R.id.lowest_bpm_activity)
    protected TextView mLowestBpmActivity;

    @BindView(R.id.highest_bpm)
    protected TextView mHighestBpm;

    @BindView(R.id.highest_bpm_activity)
    protected TextView mHighestBpmActivity;

    @BindView(R.id.day_of_highest_average)
    protected TextView mDayHighestAvg;

    @BindView(R.id.week_of_highest_average)
    protected TextView mWeekHighestAvg;

    @BindView(R.id.pie_chart)
    protected PieChart mPieChart;

    @BindView(R.id.line_chart)
    protected LineChart mLineChart;

    @BindView(R.id.timeline_type)
    protected Spinner mTimeline;

    @BindView(R.id.warning_text)
    protected TextView mWarningText;

    @BindView(R.id.highest_avg_day)
    protected TextView mHighestDayAvg;

    @BindView(R.id.highest_avg_week)
    protected TextView mHighestWeekAvg;

    @BindView(R.id.activity_rate_range)
    protected TextView mActivityRateRange;

    @BindView(R.id.post_activity_resting_rate_count)
    protected TextView mRestingRateCount;

    @BindView(R.id.bar_chart)
    protected BarChart mBarChart;

    @BindView(R.id.download_report)
    protected ImageView mDownloadReport;

    @BindView(R.id.percentage_resting_rates)
    protected TextView mPercentageTxt;

    @BindView(R.id.percentage_activity)
    protected TextView mPercentageActivity;

    @BindView(R.id.activity_rate_count)
    protected TextView mActivityTxt;

    private HeartRateDatabase mHeartRateDatabase;

    private HashMap<String, ArrayList<HeartRateObject>> records;

    private HashMap<String, ArrayList<HeartRateObject>> datesMap;

    private HashMap<String, ArrayList<HeartRateObject>> datesMap30Days;

    private HashMap<String, ArrayList<HeartRateObject>> allActivityRates;

    private HashMap<String, ArrayList<HeartRateObject>> allActivityRates30Days;

    ArrayList<String> dates;
    ArrayList<String> dates30Days;

    private boolean mSevenSelected;
    private boolean mThirtySelected;
    private boolean mAllSelected;

    private String mTimelineToShow;

    private HashMap<String, ArrayList<HeartRateObject>> recordings;
    private HashMap<String, ArrayList<HeartRateObject>> recordings30Days;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_analysis_overview);

        ButterKnife.bind(this);

        mHeartRateDatabase = new HeartRateDatabase(this);

        recordings = new HashMap<>();

        verifyStoragePermissions(this);
//
//        mHeartRateDatabase.clearDatabase();
//        mHeartRateDatabase.populateDB();
//            mHeartRateDatabase.populateDB();


//        Calendar c1 = Calendar.getInstance();
//        Calendar c2 = Calendar.getInstance();
//        Calendar c3 = Calendar.getInstance();
//
//        c1.add(Calendar.DAY_OF_YEAR, -7);
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
//
//        recordings = mHeartRateDatabase.getRecordsBetweenDates(
//                c1.getTimeInMillis(),
//                c2.getTimeInMillis(), Constants.Const.SELECT_ALL);
//
//        // Get all the records between the dates
//        HashMap<String, ArrayList<HeartRateObject>> allActivityRates =
//                mHeartRateDatabase.getActivityRecordsBetweenDates(
//                        c1.getTimeInMillis(),
//                        c2.getTimeInMillis(),
//                        getActivitySpinnerValue());
//         allActivityRates =
//                mHeartRateDatabase.getActivityRecordsBetweenDates(
//                c1.getTimeInMillis(),
//                c2.getTimeInMillis(),
//                getActivitySpinnerValue());
//
//
//        try {
//            dates = DateHelper.getDatesBetween(format.format(c1.getTimeInMillis()),
//                    format.format(c2.getTimeInMillis()), format);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//
//         datesMap = mHeartRateDatabase.getAllRecordsBetweenDates(
//                c1.getTimeInMillis(),
//                c2.getTimeInMillis());
//
//        c1.setTimeInMillis(System.currentTimeMillis());
//        c1.add(Calendar.DAY_OF_YEAR, -30);
//
//        try {
//            dates30Days = DateHelper.getDatesBetween(format.format(c1.getTimeInMillis()),
//                    format.format(c2.getTimeInMillis()), format);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        allActivityRates30Days =
//                mHeartRateDatabase.getActivityRecordsBetweenDates(
//                        c1.getTimeInMillis(),
//                        c2.getTimeInMillis(),
//                        getActivitySpinnerValue());
//
//        recordings30Days = mHeartRateDatabase.getRecordsBetweenDates(
//                c1.getTimeInMillis(),
//                c2.getTimeInMillis(), Constants.Const.SELECT_ALL);
//        datesMap30Days = mHeartRateDatabase.getAllRecordsBetweenDates(
//                c1.getTimeInMillis(),
//                c2.getTimeInMillis());


        setActivityToDisplaySpinner();

        setBarChart();
        try {
            setLineChart();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setTimelineSpinner();
        setGeneralData();
        setBreakdownData();
        setPieChart();
        setWarningText();
        setActivityRateRangeTxt();
    }

    private HashMap<String, ArrayList<HeartRateObject>> getRecordings() {
        return recordings;
    }

    private HashMap<String, ArrayList<HeartRateObject>> getRecordings30Days() {
        return recordings30Days;
    }

    public ArrayList<String> getDates() {
        return dates;
    }

    public ArrayList<String> getDates30Days() {
        return dates30Days;
    }

    public HashMap<String, ArrayList<HeartRateObject>> getAllActivityRates() {
        return allActivityRates;
    }

    public HashMap<String, ArrayList<HeartRateObject>> getAllActivityRates30Days() {
        return allActivityRates30Days;
    }

    private void setActivityRateRangeTxt() {

        String txt  = "--";

        int peak = 220;
        int age = 32;
        int minRange = 0;
        int maxRange = 0;

        double fiftyPerc = 0.5;
        double seventyPerc = 0.7;
        double eightyFive = 0.85;

        int maxHeartRate = peak - age;

        double fp = maxHeartRate * fiftyPerc;
        double sp = maxHeartRate * seventyPerc;
        double efp = maxHeartRate * eightyFive;

        if (getActivitySpinnerValue().equals(Constants.Const.WALKING)) {
            txt = "Based on your age, your Walking heart rate should rise between " +
                    String.valueOf((int) fp) + " and " + String.valueOf((int) sp) + " beats per minute.";
        } else {
            txt = "Based on your age, your " + getActivitySpinnerValue() + " heart rate should rise between " +
                    String.valueOf((int) sp) + " and " + String.valueOf((int) efp) + " beats per minute.";
        }

        mActivityRateRange.setText(txt);
    }

    private void setBarChart() {

        mBarChart.getDescription().setEnabled(false);

        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
    }

    /**
     * Sets the warning text depending if on abnormal resting heart rate.
     */
    private void setWarningText() {
        int sum = 0;
        int count = 0;
        int totalCount = 0;
        int days = 0;
        // Loop through the heart rate records
        for (Map.Entry<String, ArrayList<HeartRateObject>> entry : recordings.entrySet()) {

            ArrayList<HeartRateObject> rates = entry.getValue();

            if ((!rates.isEmpty())) {
                // initialize the first object
                HeartRateObject current = rates.get(0);
                for (int x = 1; x < rates.size(); x++) {

                    HeartRateObject next = rates.get(x);

                    // If the same date, update the count value for resting over 100
                    if (current.getDate().equals(next.getDate())) {
                        if (current.getType().equals(Constants.Const.RESTING) &&
                                current.getHeartRate() >= 100) {
                            count++;
                            totalCount++;
                        }
                    } else {
                        // IF rate of 100 detected more than 3 times in one day increment
                        // the number of days
                        if (count >= 3) {
                            days++;
                        }

                        count = 0;

                    }
                    current = next;
                }



            }
        }

        if (days > 0) {
            updateWarningText(days, totalCount);
        } else {
            String warning = "Status: OK, Nothing to report";
            mWarningText.setText(warning);
            mWarningText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

    /**
     * Sets up the timeline spinner.
     */

    private void updateWarningText(int days, int totalCount) {
        Log.d(TAG, "setWarningText: UPDATING DAYS: " + String.valueOf(days));
        if (getSpinnerValue().equals(Constants.Const.PREVIOUS_SEVEN_DAYS)) {
            String warning = "Abnormal resting heart rate was detected " + String.valueOf(totalCount) +
                    " times, in the last seven days.";
            mWarningText.setText(warning);
            mWarningText.setTextColor(ContextCompat.getColor(this, R.color.over_120));

        } else if (getSpinnerValue().equals(Constants.Const.PREVIOUS_THIRTY_DAYS)) {

            String warning = "Abnormal resting heart rate was detected " + String.valueOf(totalCount) +
                    " times in the last 30 days.";
            mWarningText.setText(warning);
            mWarningText.setTextColor(ContextCompat.getColor(this, R.color.over_120));
        }
    }

    private void setTimelineSpinner() {

        ArrayAdapter<CharSequence> timelineList = ArrayAdapter.createFromResource(
                this, R.array.timeline_spinner, R.layout.spinner_item
        );

        timelineList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTimeline.setAdapter(timelineList);

        mTimeline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBarChart.clear();
                try {
                    updateBarChart();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setWarningText();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * Returns the currently selected date type in the spinner.
     *
     * @return
     */
    private String getSpinnerValue() {
        return mTimeline.getSelectedItem().toString();
    }


    private void setLineChart() throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);

        String startDate = mHeartRateDatabase.getFirstRecordDate();
        String endDate = mHeartRateDatabase.getLastRecordDate();

        mLineChart.getDescription().setEnabled(false);


        //updateBarChart();
    }

    private void setChartXAxisDates(final String startDate, final String endDate) throws ParseException {
        mBarChart.getXAxis().setValueFormatter(
            new MyAxisValueFormatter(
                    DateHelper.getDatesBetween(startDate, endDate,
                          new SimpleDateFormat("dd/MM/yyyy", Locale.UK))));
    }

    private void updateBarChart() throws ParseException {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<BarEntry> activityEntry = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> dates;
        HashMap<String, ArrayList<HeartRateObject>> allActivityRates;


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat axisFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy", Locale.UK);

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Calendar c3 = Calendar.getInstance();

        // Reset the calendar to the correct start day
        if (getSpinnerValue().equals(Constants.Const.PREVIOUS_SEVEN_DAYS)) {
            c1.add(Calendar.DAY_OF_YEAR, -7);
           // dates = getDates();
            //allActivityRates = getAllActivityRates();
        }else if (getSpinnerValue().equals(Constants.Const.PREVIOUS_THIRTY_DAYS)) {
            c1.add(Calendar.DAY_OF_YEAR, -30);
           // dates = getDates30Days();
           // allActivityRates = getAllActivityRates30Days();
        }

        recordings = mHeartRateDatabase.getRecordsBetweenDates(
                        c1.getTimeInMillis(),
                        c2.getTimeInMillis(), Constants.Const.SELECT_ALL);

        // Get all the records between the dates
        allActivityRates =
                mHeartRateDatabase.getActivityRecordsBetweenDates(
                c1.getTimeInMillis(),
                c2.getTimeInMillis(),
                getActivitySpinnerValue());

        dates = DateHelper.getDatesBetween(format.format(c1.getTimeInMillis()),
                format.format(c2.getTimeInMillis()), format);


        HashMap<String, ArrayList<HeartRateObject>> datesMap =
                mHeartRateDatabase.getAllRecordsBetweenDates(
                c1.getTimeInMillis(),
                c2.getTimeInMillis());

//        ActivityRecognitionDatabase db = new ActivityRecognitionDatabase(this);
//        HashMap<String, ArrayList<ActivityObject>> activityMap = db.getAllRecordsBetweenDates(
//                c1.getTimeInMillis());


        try {
            setChartXAxisDates(axisFormat.format(c1.getTimeInMillis()), axisFormat.format(c2.getTimeInMillis()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int sum = 0;
        int count = 0;
        int avgSum = 0;
        int avgCount = 0;
        int restSum = 0;
        int restCount = 0;
        int totalCount = 0;
        int min = 0;
        int max = 0;

        int goodRangeCount = 0;

        int activityCount = 0;

        int totalResting = 0;

        ArrayList<Integer> averageHeartRates = new ArrayList<>();
        ArrayList<Integer> restingHeartRates = new ArrayList<>();

        int allRates = 0;

        if (!getActivitySpinnerValue().equals(Constants.Const.RESTING)) {
            int day = 0;

            if ((!datesMap.isEmpty())) {

               // Log.d(TAG, "DATES: " + String.valueOf(dates.size()));
                // Get the first object
                //HeartRateObject current = rates.get(0);

                for (int date = 0; date < dates.size(); date++) {

                    String currentDate = dates.get(date);

                    Date d = sdf.parse(currentDate);

                    ArrayList<HeartRateObject> rates = datesMap.get(format.format(d.getTime()));
//                   / ArrayList<ActivityObject> activityObjects = activityMap.get(format.format(d.getTime()));


                    if (rates != null) {

                        Log.d(TAG, "HEART OBJECTS " + String.valueOf(rates.size()));

                        int entryPosition = 0;

                        for (int record = 0; record < rates.size(); record++) {
//                            for (int act = 0; act < activityObjects.size(); act++) {
//
//                            }
                            // Get the next object
                            //HeartRateObject next = rates.get(record);
                            HeartRateObject current = rates.get(record);

                            Log.d(TAG, "FOUND: " + current.getType());

                            allRates++;

                            if (current.getType().equals(Constants.Const.RESTING)) {
                                restCount++;
                                restSum += current.getHeartRate();
                            }

                            // If the current type ==
                            if (current.getType().equals(getActivitySpinnerValue())) {

                                avgCount++;
                                activityCount++;

                                avgSum += current.getHeartRate();
                                activityEntry.add(new BarEntry(record, current.getHeartRate()));

                                // Ensures we don't over-run the array size
                                if (record + 1 < rates.size()) {

                                    // Set starting position to the next record
                                    int position = record + 1;

                                    // Get the next records
                                    HeartRateObject next = rates.get(position);

                                    boolean notActivity = false;

                                    // We loop while position is less than array size, the dates differ,
                                    // or until the type is not equal to the selected activity.
                                    // or until the type is not equal to the selected activity.
                                    while (!notActivity && position != rates.size()) {

                                        c3.setTimeInMillis(next.getDateTime());

                                        // We check if the heart rate post the activity is resting and
                                        // if so increment the sum and count of heart rates detected.
                                        // We only look at values before 12 when the user is presumed asleep.
                                        if (next.getType().equals(Constants.Const.RESTING) && c3.get(Calendar.HOUR_OF_DAY) < 21) {

                                            if (next.getHeartRate() >= 60 && next.getHeartRate() <= 100) {
                                                goodRangeCount++;
                                            }
                                            sum += next.getHeartRate();
                                           // lineEntries.add(new Entry(entryPosition, next.getHeartRate()));
                                           // entryPosition++;
                                            count++;
                                            totalCount++;
                                        } else {
                                            if (getActivitySpinnerValue().equals(Constants.Const.RUNNING)) {

                                                if (next.getType().equals(Constants.Const.CYCLING) ||
                                                        next.getType().equals(Constants.Const.WALKING)) {

                                                    notActivity = true;
                                                }

                                            } else if (getActivitySpinnerValue().equals(Constants.Const.CYCLING)) {

                                                if (next.getType().equals(Constants.Const.WALKING) ||
                                                        next.getType().equals(Constants.Const.RUNNING)) {

                                                    notActivity = true;
                                                }
                                            } else if (getActivitySpinnerValue().equals(Constants.Const.WALKING)) {

                                                if (next.getType().equals(Constants.Const.CYCLING) ||
                                                        next.getType().equals(Constants.Const.RUNNING)) {

                                                    notActivity = true;
                                                }
                                            }
                                        }

                                        // Increment position
                                        position++;

                                        if (position != rates.size() ) {
                                            // Log.d(TAG, "updateBarChart: GETTING NEXT");
                                            next = rates.get(position);
                                        } else {
                                            // Log.d(TAG, "updateBarChart: NOT GETTING");
                                        }
                                    }
                                    record = position;

                                }
                            }
                        }

                        if (count > 0) {
                            int average = sum / count;
                            sum = 0;
                            count = 0;
                            entries.add(new BarEntry(date, average));
                        }

                        if (avgCount > 0) {
                            int avg = avgSum / avgCount;
                            averageHeartRates.add(avg);
                            avgSum = 0;
                            avgCount = 0;
                        }
                        if (restCount > 0) {
                            int avg = restSum / restCount;
                            restingHeartRates.add(avg);
                            restSum = 0;
                            restCount = 0;
                        }

                    }
                }

            }

            String rest= String.valueOf(totalCount) + " resting heart rates recorded after " + getActivitySpinnerValue();
            int restGoodPerc = Calculations.calculatePercentage(goodRangeCount, totalCount);
            String restPerc = String.valueOf(restGoodPerc) + "% were between 60 and 100 beats per minute";

            String activity = String.valueOf(activityCount) + " " + getActivitySpinnerValue() + " heart rates detected";
            int activityPercentage = Calculations.calculatePercentage(activityCount, allRates);
            String activityStr = "This was " + String.valueOf(activityPercentage) + "% of all heart rates recorded";

            mRestingRateCount.setText(rest);
            mPercentageTxt.setText(restPerc);

            mActivityTxt.setText(activity);
            mPercentageActivity.setText(activityStr);

            Log.d(TAG, "ENTRIES: " + String.valueOf(entries.size()));
            updateLineDataSet(entries, activityEntry, dates);
            updateLineChart(allActivityRates.get(getActivitySpinnerValue()), averageHeartRates, restingHeartRates);
        }
    }

    /**
     * Update the line chart
     *
     *  @param rates
     * @param averageHeartRates
     * @param restingHeartRates*/
    private void updateLineChart(ArrayList<HeartRateObject> rates, ArrayList<Integer> averageHeartRates, ArrayList<Integer> restingHeartRates) {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<Entry> restingEntries = new ArrayList<>();
      //  ArrayList<HeartRateObject> heartRateObjects = rates.get("Resting");
       // Log.d(TAG, "updateLineChart: SIZE: " + String.valueOf(heartRateObjects.size()));

//        for (int rate = 0; rate < rates.size(); rate++) {
//            entries.add(
//                    new Entry(rate, rates.get(rate).getHeartRate())
//            );
//        }
        for (int rate = 0; rate < averageHeartRates.size(); rate++) {
            entries.add(
                    new Entry(rate, averageHeartRates.get(rate))
            );
        }

        for (int rate = 0; rate < restingHeartRates.size(); rate++) {
            restingEntries.add(
                    new Entry(rate, restingHeartRates.get(rate))
            );
        }


        LineDataSet activityDataSet = createLineDataSet(entries, getActivitySpinnerValue());
        LineDataSet restingDataSet = createLineDataSet(restingEntries, Constants.Const.RESTING);

        LineData set = new LineData(activityDataSet, restingDataSet);
        mLineChart.setData(set);

        mLineChart.invalidate();

    }

    private LineDataSet createLineDataSet(ArrayList<Entry> entries, final String activity) {
        LineDataSet lineDataSet = new LineDataSet(entries, activity + " average");
        lineDataSet.setColor(getActivityColor(activity));
        lineDataSet.setFillColor(getActivityColor(activity));
        lineDataSet.setFillAlpha(65);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setDrawValues(false);
        //lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        lineDataSet.setFormSize(15.f);

        return lineDataSet;
    }

    /**
     * Returns the integer value of a color for an activity.
     *
     * @param activity
     * @return
     */
    private int getActivityColor(String activity) {

        int color = 0;

        switch (activity) {
            case Constants.Const.CYCLING:
                color = ContextCompat.getColor(this, R.color.cycling);
                break;
            case Constants.Const.WALKING:
                color = ContextCompat.getColor(this, R.color.walking);
                break;
            case Constants.Const.RUNNING:
                color = ContextCompat.getColor(this, R.color.running);
                break;
            case Constants.Const.RESTING:
                color = ContextCompat.getColor(this, R.color.resting);
                break;
            case Constants.Const.GENERAL:
                color = ContextCompat.getColor(this, R.color.general);
                break;
        }

        return color;
    }

    private void updateLineDataSet(
            ArrayList<BarEntry> entries, ArrayList<BarEntry> activityEntry, ArrayList<String> dates) {

        String activity = getActivitySpinnerValue();


        Log.d(TAG, "COUNT: " + String.valueOf(activityEntry.size()));
        //LineDataSet lineDataSet = createLineDataSet(activity, entries);
        BarDataSet barDataSet = createEntryDataSet(activity, "Post " + activity + " resting rate averages", entries);
       // BarDataSet activitySet = createEntryDataSet(activity, activityEntry);

      //  LineData lineData = new LineData(lineDataSet);

       // mLineChart.setData(lineData);

//        BarData barData = new BarData(barDataSet);
//        BarData activityData = new BarData(activitySet);

        BarData data = new BarData(barDataSet);
//        data.setData(barData);
//        data.setData(activityData);
        mBarChart.setData(data);
        mBarChart.invalidate();

    }

    private BarDataSet createEntryDataSet(String activity, String key, ArrayList<BarEntry> entries) {
        BarDataSet set = new BarDataSet(entries, key);

        // set.setAxisDependency(XAxis.XAxisPosition.BOTTOM);
        if (activity.equals(Constants.Const.RESTING)) {
            set.setColor(ContextCompat.getColor(this, R.color.resting));
        } else if (activity.equals(Constants.Const.WALKING)) {
            set.setColor(ContextCompat.getColor(this, R.color.walking));
        } else if (activity.equals(Constants.Const.RUNNING)) {
            set.setColor(ContextCompat.getColor(this, R.color.running));
        } else if (activity.equals(Constants.Const.CYCLING)) {
            set.setColor(ContextCompat.getColor(this, R.color.cycling));
        }

//        set.setDrawCircles(true);
//        set.setDrawCircleHole(true);
//        set.setDrawValues(false);
//        // set.setCircleColor(Color.WHITE);
//        set.setLineWidth(2f);
//        set.setCircleRadius(3f);
//        set.setFillAlpha(65);
//        set.setFillColor(ColorTemplate.getHoloBlue());
//        set.setHighLightColor(Color.rgb(244, 117, 117));
//        set.setDrawCircleHole(false);

        return set;
    }

    private void setLineData(LineDataSet set) {
        LineData lineData = new LineData();
    }

    private LineDataSet createLineDataSet(String key, ArrayList<Entry> entries) {

        LineDataSet set = new LineDataSet(entries, key);

       // set.setAxisDependency(XAxis.XAxisPosition.BOTTOM);
        if (key.equals(Constants.Const.RESTING)) {
            set.setColor(ContextCompat.getColor(this, R.color.resting));
        } else if (key.equals(Constants.Const.WALKING)) {
            set.setColor(ContextCompat.getColor(this, R.color.walking));
        } else if (key.equals(Constants.Const.RUNNING)) {
            set.setColor(ContextCompat.getColor(this, R.color.running));
        } else if (key.equals(Constants.Const.CYCLING)) {
            set.setColor(ContextCompat.getColor(this, R.color.cycling));
        }

        set.setDrawCircles(true);
        set.setDrawCircleHole(true);
        set.setDrawValues(false);
       // set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);

        return set;
    }

    /**
     * Setup the pie chart.
     */
    private void setPieChart() {

        mPieChart.setDrawHoleEnabled(false);
        mPieChart.setDrawSlicesUnderHole(false);

    }

    /**
     * Updates the display of the pie chart.
     */
    private void updatePieChartDisplay(ArrayList<Integer> values) {

        int sum = 0;



        if (!values.isEmpty()) {
            for (Integer v : values) {
                sum += v;
            }
        }
        Log.d(TAG, "updatePieChartDisplay: SUM " + String.valueOf(sum));
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {

            entries.add(new PieEntry(
                    calculatePercentage(values.get(i), sum),
                    getRange(values.get(i)),
                    i
            ));
        }




        PieDataSet dataSet = new PieDataSet(entries, "Entries");

        ArrayList<Integer> colors = new ArrayList<>(
            Arrays.asList(
                R.color.less_than_40,
                R.color.over_40,
                R.color.over_55,
                R.color.over_70,
                R.color.over_85,
                R.color.over_100,
                R.color.over_120
            )
        );

        dataSet.setColors(colors);

        //dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
       // dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private String getRange(int value) {
        if (value > 120) {
            return "> 120";
        } else if (value >= 100) {
            return "100 - 119";
        } else if (value >= 85) {
            return "85 - 99";
        } else if (value >= 70) {
            return "70 - 85";
        } else if (value >= 55) {
            return "55 - 69";
        } else if (value >= 40) {
            return "40 - 54";
        } else {
            return "< 40";
        }

    }

    private int calculatePercentage(final int value, final int sum) {

        double v = (double) value;
        double s = (double) sum;

        double percentage = (v / s) * 100;

        return (int) percentage;
    }

    /**
     * Set the breakdown data.
     */
    private void setBreakdownData() {

        records = mHeartRateDatabase.getAllRecords();
//        Log.d(TAG, "RECORDINGS: " + String.valueOf(re.size()));
        if (!records.isEmpty()) {

            String activity = getActivitySpinnerValue();
            // Depending on the spinner value, set the display
            if (activity.equals(Constants.Const.CYCLING)) {
                updateBreakdownDisplay(Constants.Const.CYCLING, records.get(Constants.Const.CYCLING));

            } else if (activity.equals(Constants.Const.RUNNING)) {

                updateBreakdownDisplay(Constants.Const.RUNNING, records.get(Constants.Const.RUNNING));

            } else if (activity.equals(Constants.Const.RESTING)) {

                updateBreakdownDisplay(Constants.Const.RESTING, records.get(Constants.Const.RESTING));

            } else if (activity.equals(Constants.Const.GENERAL)) {

                updateBreakdownDisplay(Constants.Const.GENERAL, records.get(Constants.Const.GENERAL));

            } else if (activity.equals(Constants.Const.WALKING)) {

                updateBreakdownDisplay(Constants.Const.WALKING, records.get(Constants.Const.WALKING));

            }
        }
    }

    /**
     *  Update the breakdown display to the new data.
     *
     * @param activity
     * @param recordings
     */
    private void updateBreakdownDisplay(String activity, ArrayList<HeartRateObject> recordings) {

        // Initialize the counts
        int over120 = 0;
        int over100 = 0;
        int over85 = 0;
        int over70 = 0;
        int over55 = 0;
        int over40 = 0;
        int lessThan40 = 0;

        //SimpleDateFormat format = new SimpleDateFormat("")



        long dayOfHighestAverage = 0;

        int highestWeeklyAverage = 0;
        int weeklySum = 0;
        int highestWeekNumber = 0;
        int weekBpmCount = 0;

        int highestAverage = 0;
        int sum = 0;
        int count = 0;

        HeartRateObject current;
        HeartRateObject previous = null;

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        // Go through the heart rate objects
        for (int recording = 1; recording < recordings.size(); recording++) {
            current = recordings.get(recording);
            int bpm = current.getHeartRate();

            if (bpm > 120) {
                over120++;
            } else if (bpm >= 100) {
                over100++;
            } else if (bpm >= 85) {
                over85++;
            } else if (bpm >= 70) {
                over70++;
            } else if (bpm >= 55) {
                over55++;
            } else if (bpm >= 40) {
                over40++;
            } else {
                lessThan40++;
            }

            //previous = current;

            if (recording > 0) {
                // Set the times of current and previous records calendars
                c1.setTimeInMillis(current.getDateTime());
                c2.setTimeInMillis(recordings.get(recording - 1).getDateTime());


                // If the the two objects are in the same week, update bpm sum and number of
                // rates detected.
                if (c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)) {
                    weekBpmCount++;
                    weeklySum += bpm;

                } else {
                    if (weekBpmCount > 0) {
                        // Get the weeks average heart rate for activity
                        int weeklyAverage = weeklySum / weekBpmCount;

                        // Reset the count of weekly heart rates
                        weekBpmCount = 0;
                        weeklySum = 0;

                        if (weeklyAverage > highestWeeklyAverage) {
                            highestWeeklyAverage = weeklyAverage;
                            highestWeekNumber = c2.get(Calendar.WEEK_OF_YEAR);
                        }
                    }
                }

                if (current.getDate().equals(recordings.get(recording - 1).getDate())) {
                    count++;
                    sum += bpm;
                } else {
                    if (count > 0) {
                        int average = sum / count;

                        count = 0;
                        sum = 0;

                        if (average > highestAverage) {
                            highestAverage = average;
                            dayOfHighestAverage = recordings.get(recording - 1).getDateTime();
                        }
                    }
                }
            }

        }

        ArrayList<Integer> values = new ArrayList<>(Arrays.asList(lessThan40, over40, over55, over70,
                over85, over100, over120));
        updatePieChartDisplay(values);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);
        String date = dateFormat.format(dayOfHighestAverage);

        // Set the day of highest average
        setStringTextViewValue(mDayHighestAvg, date);
        setStringTextViewValue(mHighestDayAvg, highestAverage + "BPM");
        setStringTextViewValue(mWeekHighestAvg, "Week " + highestWeekNumber);
        setStringTextViewValue(mHighestWeekAvg, highestWeeklyAverage + "BPM");

    }

    /**
     * Set the value of a text view.
     *
     * @param textView
     * @param value
     */
    private void setTextViewValue(TextView textView, int value) {
        textView.setText(String.valueOf(value));
    }

    /**
     * Set the value of a text view.
     *  @param textView
     * @param value
     */
    private void setStringTextViewValue(TextView textView, String value) {
        textView.setText(value);
    }

    /**
     * Set the general information.
     */
    private void setGeneralData() {

        int dbSize = mHeartRateDatabase.getDBSize();
        int average = mHeartRateDatabase.averageHeartRate();
        HeartRateObject max = mHeartRateDatabase.getMaxHeartRate();
        HeartRateObject min = mHeartRateDatabase.getMinHeartRate();


        setBpmCount(dbSize);
        setMaxBpm(max);
        setMinBpm(min);
        setTotalAverageHeartRate(average);

    }

    /**
     * Set the average heart rate recorded
     *
     * @param average
     */
    private void setTotalAverageHeartRate(int average) {
        mAverageBeats.setText(String.valueOf(average));
    }

    /**
     * Set the minimum heart rate recorded.
     *
     * @param min
     */
    private void setMinBpm(HeartRateObject min) {
        if (min != null) {
            mLowestBpm.setText(String.valueOf(min.getHeartRate()));
            mLowestBpmActivity.setText(min.getType());
        }
    }

    /**
     * Set the maximum heart rate reoorded.
     *
     * @param max
     */
    private void setMaxBpm(HeartRateObject max) {
        if (max != null) {
            mHighestBpm.setText(String.valueOf(max.getHeartRate()));
            mHighestBpmActivity.setText(max.getType());
        }
    }

    /**
     * Set the value of the total beats recorded.
     *
     * @param size
     */
    private void setBpmCount(int size) {
        mTotalBeats.setText(String.valueOf(size));
    }


    /**
     * Sets up the spinner menu for the activities to be shown.
     */
    private void setActivityToDisplaySpinner() {
        ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                this, R.array.activity_selection, R.layout.spinner_item
        );

        activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActivitySpinner.setAdapter(activityList);

        mActivitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBarChart.clear();
                try {
                    updateBarChart();
                    setActivityRateRangeTxt();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setBreakdownData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    /**
     * Get the activity selected in the dropdown.
     *
     * @return
     */
    private String getActivitySpinnerValue() {
        return mActivitySpinner.getSelectedItem().toString();
    }

    @OnClick(R.id.download_report)
    public void downloadReport() {

        try {
            File pdfFile = PDFReport.createPDFReport(mHeartRateDatabase.getDBSize(), mHeartRateDatabase.getRestingData());
            viewPdf(pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void viewPdf(File myFile){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(myFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void emailNote(File myFile)
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse(myFile.getAbsolutePath());
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }

    /**
     * Creates our first table
     * @return our first table
     */
    public static PdfPTable createFirstTable() {
        // a table with three columns
        PdfPTable table = new PdfPTable(4);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        cell = new PdfPCell();
        table.addCell(new Phrase("Total Detected", boldFont));
        table.addCell("11678");
        table.addCell(new Phrase("Resting Rates", boldFont));
        table.addCell("9876");

        cell = new PdfPCell();
        table.addCell(new Phrase("Total Detected", boldFont));
        table.addCell("11678");
        table.addCell(new Phrase("Resting Rates", boldFont));
        table.addCell("9876");

        cell = new PdfPCell();
        table.addCell(new Phrase("Total Detected", boldFont));
        table.addCell("11678");
        table.addCell(createNewCell("Resting", true));
        table.addCell("9876");

        cell = new PdfPCell();
        table.addCell(new Phrase("Total Detected", boldFont));
        table.addCell("11678");
        table.addCell("Resting Rates");
        table.addCell("9876");

        // now we add a cell with rowspan 2
        cell = new PdfPCell(new Phrase("Cell with rowspan 2"));
        cell.setRowspan(2);
        table.addCell(cell);
        // we add the four remaining cells with addCell()
        table.addCell("row 1; cell 1");
        table.addCell("row 1; cell 2");
        table.addCell("row 2; cell 1");
        table.addCell("row 2; cell 2");
        return table;
    }

    private static PdfPCell createNewCell(String cellText, boolean isHeading) {
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.addElement(new Phrase(cellText, isHeading ? boldFont : font));

        return cell;
    }

}
