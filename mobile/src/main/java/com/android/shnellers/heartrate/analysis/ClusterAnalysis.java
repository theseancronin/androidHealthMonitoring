package com.android.shnellers.heartrate.analysis;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 07/03/2017.
 */

public class ClusterAnalysis extends AppCompatActivity {

    private static final String TAG = "ClusterAnalysis";

    private static final boolean DEBUG = true;

    @BindView(R.id.scatter_chart)
    protected ScatterChart mScatterChart;

    @BindView(R.id.timeline_type)
    protected Spinner mTimeline;

    @BindView(R.id.activity_display)
    protected Spinner mActivityToDisplay;



    private HeartRateDatabase mHeartRateDatabase;

    private HashMap<String, ArrayList<Entry>> entriesMap;

    private ArrayList<Entry> xVals;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_analysis_cluster);

        ButterKnife.bind(this);

        entriesMap = new HashMap<>();

        // int[] xVals = new int[180];
        xVals = new ArrayList<Entry>();
        for (int i = 0; i < 160; i++) {
            if (i > 0) {
                if (i % 2 == 0) {
                    xVals.add(new Entry(0, i));
                } else {

                }
            } else {
                xVals.add(new Entry(24, i));
            }
        }

        setTimelineSpinner();

        setActivityToDisplaySpinner();

        mHeartRateDatabase = new HeartRateDatabase(this);
    }



    /**
     * Sets up the timeline spinner.
     */
    private void setTimelineSpinner() {

        ArrayAdapter<CharSequence> timelineList = ArrayAdapter.createFromResource(
                this, R.array.date_selection, android.R.layout.simple_spinner_item
        );

        timelineList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTimeline.setAdapter(timelineList);

        mTimeline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (getSpinnerValue().equals(Constants.Const.TODAY)) {
                    //setChartDates();
                    //setChartData();
                    setClusterData(Constants.Const.TODAY);
                } else if (getSpinnerValue().equals(Constants.Const.PREVIOUS_SEVEN_DAYS)) {
                   setClusterData(Constants.Const.PREVIOUS_SEVEN_DAYS);
                } else if (getSpinnerValue().equals(Constants.Const.PREVIOUS_THIRTY_DAYS)) {
                    setClusterData(Constants.Const.PREVIOUS_THIRTY_DAYS);
                } else {

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setClusterData(final String timeline) {

        HashMap<String, ArrayList<HeartRateObject>> ratesMap = new HashMap<>();
        HashMap<String, ScatterData> entriesMap = new HashMap<>();
        ArrayList<String> dates;

        // Initialize a scatter data object
        ScatterData data = new ScatterData();

        // Setup the different date formats
        DateFormat parseFormat = new SimpleDateFormat("dd/MM/yy", Locale.UK);


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat f = new SimpleDateFormat("dd MMM", Locale.UK);

        Calendar sd = Calendar.getInstance();
        Calendar ed = Calendar.getInstance();

        Date startDate = null;
        Date endDate  = null;


        String activityToSelect;


        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        if (timeline.equals(Constants.Const.PREVIOUS_SEVEN_DAYS)) {
            c1.add(Calendar.DAY_OF_YEAR, -7);
        } else if (timeline.equals(Constants.Const.PREVIOUS_THIRTY_DAYS)) {
            c1.add(Calendar.DAY_OF_YEAR, -30);
        } else {

        }

        if (getSpinnerValue().equals(Constants.Const.ALL_TIME)) {

        } else {
            ratesMap = mHeartRateDatabase.getRecordsBetweenDates(c1.getTimeInMillis(),
                    c2.getTimeInMillis(), Constants.Const.SELECT_ALL);
        }


//            dates = DateHelper.getDatesBetween(format.format(c1.getTime()),
//                    format.format(c2.getTime()), format);

        setClusterChart(ratesMap);



    }

    private void setClusterChart(final HashMap<String, ArrayList<HeartRateObject>> readings) {



        entriesMap.put(Constants.Const.WALKING, new ArrayList<Entry>());
        entriesMap.put(Constants.Const.CYCLING, new ArrayList<Entry>());
        entriesMap.put(Constants.Const.RUNNING, new ArrayList<Entry>());
        entriesMap.put(Constants.Const.RESTING, new ArrayList<Entry>());
        entriesMap.put(Constants.Const.GENERAL, new ArrayList<Entry>());

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<ScatterData> scatterData = new ArrayList<>();
        ArrayList<IScatterDataSet> dataSets = new ArrayList<IScatterDataSet>();

        HashMap<String, ScatterData> entryData = new HashMap<>();

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yy", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        Iterator iterator = readings.entrySet().iterator();

        if (!readings.isEmpty()) {

            // Loop through all the different activity heart readings
            for (Map.Entry<String, ArrayList<HeartRateObject>> entry : readings.entrySet()) {
                if (DEBUG) Log.d(TAG, "ITERATING MAP");
                // Get the list of hear rates for particular activity
                ArrayList<HeartRateObject> heartRates = entry.getValue();

                // Loo through he list of heart rates

                for (int reading = 0; reading < heartRates.size(); reading++) {

                    HeartRateObject heartObject = heartRates.get(reading);

                    int y = heartObject.getHeartRate() - 76;
                    calendar.setTimeInMillis(heartObject.getDateTime());
                    // Create new cluster entry
                    addEntryToTypeMap(
                            entry.getKey(),
                            entriesMap,
                            new Entry(calendar.get(Calendar.HOUR_OF_DAY), heartObject.getHeartRate()));
                }
            }
        }




      //  mScatterChart.setData(new ScatterData(dt));

        //Log.d(TAG, "setScatterChart: ENTRIES " + String.valueOf(entries));

        ArrayList<IScatterDataSet> ds = new ArrayList<>();

      //  for (Map.Entry<String, ArrayList<Entry>> entry : entriesMap.entrySet()) {
            //Log.d(TAG, "setScatterChart: COUNT" + String.valueOf(entry.getValue().size()));
            if (!entriesMap.isEmpty()) {
                if (getActivitySpinnerItemSelected().equals(Constants.Const.GENERAL)) {
                    ds.add(createScatterData(Constants.Const.GENERAL, entriesMap.get(Constants.Const.GENERAL)));
                } else if (getActivitySpinnerItemSelected().equals(Constants.Const.RESTING)) {
                    ds.add(createScatterData(Constants.Const.RESTING, entriesMap.get(Constants.Const.RESTING)));
                } else if (getActivitySpinnerItemSelected().equals(Constants.Const.WALKING)) {
                    ds.add(createScatterData(Constants.Const.WALKING, entriesMap.get(Constants.Const.WALKING)));
                } else if (getActivitySpinnerItemSelected().equals(Constants.Const.RUNNING)) {
                    ds.add(createScatterData(Constants.Const.RUNNING, entriesMap.get(Constants.Const.RUNNING)));
                } else if (getActivitySpinnerItemSelected().equals(Constants.Const.CYCLING)) {
                    ds.add(createScatterData(Constants.Const.CYCLING, entriesMap.get(Constants.Const.CYCLING)));
                }

            }
        //}
        ScatterDataSet dt = new ScatterDataSet(xVals, "");
        dt.setVisible(false);
        ds.add(dt);

        if (!ds.isEmpty()) {
            ScatterData data = new ScatterData(ds);
            mScatterChart.setData(data);
            mScatterChart.invalidate();
        }

        //displayClusterData(data);

    }

    private void displayClusterData(ScatterData data) {

    }

    private ScatterDataSet createScatterData(String key, ArrayList<Entry> values) {

        ScatterDataSet dataSet = new ScatterDataSet(values, key);

        Log.d(TAG, "createScatterData: " + key + " " + values.size());

        if (key.equals(Constants.Const.CYCLING)) {
            dataSet.setColor(ContextCompat.getColor(this, R.color.cycling));
        } else if (key.equals(Constants.Const.RUNNING)) {
            dataSet.setColor(ContextCompat.getColor(this, R.color.running));
        } else if (key.equals(Constants.Const.RESTING)) {
            dataSet.setColor(ContextCompat.getColor(this, R.color.resting));
        } else if (key.equals(Constants.Const.GENERAL)) {
            dataSet.setColor(ContextCompat.getColor(this, R.color.general));
        } else if (key.equals(Constants.Const.WALKING)) {
            dataSet.setColor(ContextCompat.getColor(this, R.color.walking));
        }

        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(10f);
        dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        dataSet.setScatterShapeSize(30);

        return dataSet;
    }

    /**
     * Adds the chart entry to the correct map entry list.
     *
     * @param key
     * @param entriesMap
     * @param entry
     */
    private void addEntryToTypeMap(String key, HashMap<String, ArrayList<Entry>> entriesMap, Entry entry) {
        switch (key) {
            case Constants.Const.CYCLING:
                entriesMap.get(key).add(entry);
                break;
            case Constants.Const.WALKING:
                entriesMap.get(key).add(entry);
                break;
            case Constants.Const.RUNNING:
                entriesMap.get(key).add(entry);
                break;
            case Constants.Const.RESTING:
                entriesMap.get(key).add(entry);
                break;
            case Constants.Const.GENERAL:
                entriesMap.get(key).add(entry);
                break;
        }
    }

    private void setupChart() throws ParseException {

        mScatterChart.getDescription().setEnabled(false);
        mScatterChart.setBackgroundColor(Color.WHITE);
        mScatterChart.setDrawGridBackground(false);


        YAxis rightAxis = mScatterChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = mScatterChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = mScatterChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);

        mScatterChart.setVisibleXRange(0, 180);

    }


    /**
     * Sets up the spinner menu for the activities to be shown.
     */
    private void setActivityToDisplaySpinner() {
        ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                this, R.array.activity_selection, android.R.layout.simple_spinner_item
        );

        activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActivityToDisplay.setAdapter(activityList);

        mActivityToDisplay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ArrayList<IScatterDataSet> ds = new ArrayList<>();
              //  mScatterChart.clear();
                if (!entriesMap.isEmpty()) {
                    if (getActivitySpinnerItemSelected().equals(Constants.Const.GENERAL)) {
                        ds.add(createScatterData(Constants.Const.GENERAL, entriesMap.get(Constants.Const.GENERAL)));
                    } else if (getActivitySpinnerItemSelected().equals(Constants.Const.RESTING)) {
                        ds.add(createScatterData(Constants.Const.RESTING, entriesMap.get(Constants.Const.RESTING)));
                    } else if (getActivitySpinnerItemSelected().equals(Constants.Const.WALKING)) {
                        ds.add(createScatterData(Constants.Const.WALKING, entriesMap.get(Constants.Const.WALKING)));
                    } else if (getActivitySpinnerItemSelected().equals(Constants.Const.RUNNING)) {
                        ds.add(createScatterData(Constants.Const.RUNNING, entriesMap.get(Constants.Const.RUNNING)));
                    } else if (getActivitySpinnerItemSelected().equals(Constants.Const.CYCLING)) {
                        ds.add(createScatterData(Constants.Const.CYCLING, entriesMap.get(Constants.Const.CYCLING)));
                    }

                }
                ScatterDataSet dt = new ScatterDataSet(xVals, "");
                dt.setVisible(false);
                ds.add(dt);

                if (!ds.isEmpty()) {

                    ScatterData data = new ScatterData(ds);
                    mScatterChart.setData(data);
                    mScatterChart.invalidate();
                }



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

    /**
     * Return the item in the activity spinner that is selected.
     *
     * @return
     */
    private String getActivitySpinnerItemSelected() {
        return mActivityToDisplay.getSelectedItem().toString();
    }

}
