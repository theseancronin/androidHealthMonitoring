package com.android.shnellers.heartrate.analysis;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.charts.HeartRateMarker;
import com.android.shnellers.heartrate.charts.MyAxisValueFormatter;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.helpers.DateHelper;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 20/02/2017.
 */

public class DataAnalysis extends Fragment {

    private static final String TAG = "DataAnalysis";

    public static final String CUSTOM_DATE = "Custom Date";
    public static final String DAYS = "Days";

    private static final String SELECT_ALL = "*";
    public static final String ALL = "All";
    public static final String WEEK_TIMELINE = "Week";
    public static final String MONTH_TIMELINE = "Month";

    @BindView(R.id.combined_chart)
    protected CombinedChart mCombinedChart;

    @BindView(R.id.start_date)
    protected EditText mStartDate;

    @BindView(R.id.end_date)
    protected EditText mEndDate;

    @BindView(R.id.timeline_type)
    protected Spinner mTimeline;

    @BindView(R.id.activity_display)
    protected Spinner mActivityToDisplay;

    @BindView(R.id.days_layout)
    protected LinearLayout mDaysLayout;

    @BindView(R.id.previous_btn)
    protected ImageButton mPreviousBtn;

    @BindView(R.id.next_btn)
    protected ImageButton mNextBtn;

    @BindView(R.id.day_view)
    protected TextView mDayView;

    private String mSpinnerSelected;

    private HeartRateDatabase mDatabase;

    private ActivityRecognitionDatabase mRecognitionDatabase;

    final Calendar myCalendar = Calendar.getInstance();

    private boolean mInit;

    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.data_analysis_layout, container, false);
        ButterKnife.bind(this, mView);

        mInit = false;

        mDatabase = new HeartRateDatabase(getActivity());

        mRecognitionDatabase = new ActivityRecognitionDatabase(getActivity());

        mTimeline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {

                    mCombinedChart.clear();
                    if (getSpinnerValue().equals(CUSTOM_DATE)) {
                        Log.d(TAG, "onItemSelected: CUSTOM");
                        setChartDates();
                        setChartData();
                    } else if (getSpinnerValue().equals(DAYS)) {
                        Log.d(TAG, "onItemSelected: DAYS");
                        updateChartToDays();
                       // setChartData();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initializeDates();

        setDatePickerPopups();

        setTimelineSpinner();

        setActivityToDisplaySpinner();

        try {


            setupChart();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            setChartData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void updateChartToDays() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.UK);
        Calendar calendar = Calendar.getInstance();

        mStartDate.setText(format.format(calendar.getTimeInMillis()));
        mEndDate.setText(format.format(calendar.getTimeInMillis()));
        updateLabel(mStartDate, calendar);
        updateLabel(mEndDate, calendar);
    }

    /**
     * Initializes the calendar with the last 7 days of data.
     */
    private void initializeDates() {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.UK);

        Calendar sc = Calendar.getInstance();
        Calendar ec = Calendar.getInstance();

        sc.add(Calendar.DAY_OF_MONTH, -7);

        mStartDate.setText(format.format(sc.getTimeInMillis()));
        mEndDate.setText(format.format(ec.getTimeInMillis()));
    }

    private void setChartData() throws ParseException {
        setChartDates();
        CombinedData data = new CombinedData();
        data.setData(generateHeartRateLimitLines());

        try {

            data.setData(generateHeartRateHistory());


//
//            ArrayList<LineData> activityData = generateActivityData();
//            if (!activityData.isEmpty()) {
//                for (LineData lineData : activityData) {
//                    data.setData(lineData);
//                }
//            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        mCombinedChart.setData(data);
        mCombinedChart.invalidate();
    }

    /**
     * Generates the data for the activities.
     *
     * @return
     * @throws ParseException
     */
    private ArrayList<LineData> generateActivityData() throws ParseException {

        HashMap<String, ArrayList<RecognizedActivity>> activities;
        ArrayList<String> dates;

        ArrayList<LineData> lineData = new ArrayList<>();

        DateFormat parseFormat = new SimpleDateFormat("dd/MM/yy", Locale.UK);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat f = new SimpleDateFormat("dd MMM", Locale.UK);

        Calendar sd = Calendar.getInstance();
        Calendar ed = Calendar.getInstance();

        Date startDate = null;
        Date endDate  = null;

        String start = getStartDate();
        String end = getEndDate();



        if (getSpinnerValue().equals(CUSTOM_DATE)) {

            startDate = parseFormat.parse(start);

            sd.setTimeInMillis(startDate.getTime());

            endDate = parseFormat.parse(end);

            ed.setTimeInMillis(endDate.getTime());

            activities = mRecognitionDatabase.getRecordsBetweenDates(format.format(startDate.getTime()),
                    format.format(endDate.getTime()));

            dates = DateHelper.getDatesBetween(format.format(startDate.getTime()),
                    format.format(endDate.getTime()), format);

            lineData = setActivityData(activities, dates);

        }

        return lineData;
    }

    /**
     * Set the activity data.
     *
     * @param activities
     * @param dates
     * @return
     * @throws ParseException
     */
    private ArrayList<LineData>  setActivityData(
        HashMap<String, ArrayList<RecognizedActivity>> activities,
        ArrayList<String> dates) throws ParseException {

        LineData walkingData;
        LineData runningData;
        LineData cyclingData;

        ArrayList<Entry> walkingEntries;
        ArrayList<Entry> runningEntries;
        ArrayList<Entry> cyclingEntries;

        ArrayList<LineData> entries = new ArrayList<>();

        Iterator iterator = activities.entrySet().iterator();

        if (!activities.isEmpty()) {

            if (activities.get(Constants.Const.WALKING) != null) {
                walkingEntries = createActivityEntries(dates, activities.get(Constants.Const.WALKING));
                walkingData = createLineDataObject(walkingEntries, Constants.Const.WALKING);
                entries.add(walkingData);
            }

            if (activities.get(Constants.Const.RUNNING) != null) {
                runningEntries = createActivityEntries(dates, activities.get(Constants.Const.RUNNING));
                runningData = createLineDataObject(runningEntries, Constants.Const.RUNNING);
                entries.add(runningData);
            }

            if (activities.get(Constants.Const.CYCLING) != null) {
                cyclingEntries = createActivityEntries(dates, activities.get(Constants.Const.CYCLING));
                cyclingData = createLineDataObject(cyclingEntries, Constants.Const.CYCLING);
                entries.add(cyclingData);
            }
        }

        return entries;
    }

    /**
     * Creates the line data object which will be displayed on the screen.
     *
     * @param entries
     * @param type
     * @return
     */
    private LineData createLineDataObject(ArrayList<Entry> entries, String type) {

        Log.d(TAG, "createLineDataObject: ");

        LineData data = new LineData();

        LineDataSet dataSet = new LineDataSet(entries, type);

        dataSet.setColor(Color.MAGENTA);
        dataSet.setCircleRadius(7.5f);
        dataSet.setDrawValues(false);
        dataSet.setValueTextSize(10f);
        //dataSet.(ScatterChart.ScatterShape.CIRCLE);
        //dataSet.setScatterShapeSize(50);
        data.addDataSet(dataSet);

        return data;
    }

    /**
     * Create graph entry data for the activities.
     *
     * @param dates
     * @param recognizedActivities
     * @return
     * @throws ParseException
     */
    private ArrayList<Entry> createActivityEntries(ArrayList<String> dates,
                                                   ArrayList<RecognizedActivity> recognizedActivities) throws ParseException {

        ArrayList<Entry> entries = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.UK);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < dates.size(); i++) {

            String date = dates.get(i);
            for(RecognizedActivity activity : recognizedActivities) {

                if (dbFormat.toString().equals(activity.getDate())) {
                    Date activityDate = DateHelper.convertStringToDateTime(activity.getDate(), dbFormat);

                    calendar.setTimeInMillis(activityDate.getTime());

                    if (date.equals(format.format(calendar.getTime()))) {

                        entries.add(new Entry(i, activity.getMinutes()));

                    }
                }

            }
        }

        return entries;
    }

    /**
     * Plot the heart rate data onto the graph.
     */
    private ScatterData generateHeartRateHistory() throws ParseException, SQLiteException {

        HashMap<String, ArrayList<HeartRateObject>> ratesMap;
        HashMap<String, ScatterData> entriesMap = new HashMap<>();
        ArrayList<String> dates;

        String timeline = getActivitySpinnerItemSelected();

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

        String start = getStartDate();
        String end = getEndDate();

        String activityToSelect;

        if (getActivitySpinnerItemSelected().equals(ALL)) {
            activityToSelect = SELECT_ALL;
        } else if (getActivitySpinnerItemSelected().equals(ALL)){
            activityToSelect = getActivitySpinnerItemSelected();
        }

        if (getSpinnerValue().equals(CUSTOM_DATE)) {

            startDate = parseFormat.parse(start);

            sd.setTimeInMillis(startDate.getTime());

            endDate = parseFormat.parse(end);

            ed.setTimeInMillis(endDate.getTime());

            ratesMap = mDatabase.getRecordsBetweenDates(startDate.getTime(),
                                            endDate.getTime(), SELECT_ALL);

            dates = DateHelper.getDatesBetween(format.format(startDate.getTime()),
                    format.format(endDate.getTime()), format);

            data = setScatterChart(ratesMap, dates);

        } else if (getSpinnerValue().equals(DAYS)) {
            HashMap<String, Date> allDates = mDatabase.getDaysOfAllReadings();

            Date minDate = null;
            Date maxDate = null;

            if (!allDates.isEmpty()) {
                if (allDates.get("startDate") != null) {
                    minDate = allDates.get("startDate");
                }

                if (allDates.get("endDate") != null) {
                    maxDate = allDates.get("endDate");
                }

                if (minDate != null || maxDate != null) {
                    ratesMap = mDatabase.getRecordsBetweenDates(
                            minDate != null ? minDate.getTime() : null,
                            maxDate != null ? maxDate.getTime() : null,
                            SELECT_ALL
                    );
                }
            }
        }


        return data;
    }

    /**
     * Sets scatter data for a given list and dates.
     *
     * @param readings
     * @param dates
     * @return
     */
    private ScatterData setScatterChart(final HashMap<String, ArrayList<HeartRateObject>> readings,
                                 final ArrayList<String> dates) {
        ScatterData data = new ScatterData();

        HashMap<String, ArrayList<Entry>> entriesMap = new HashMap<>();
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
            for (int i = 0; i < dates.size(); i++) {

                String date = dates.get(i);

                for (Map.Entry<String, ArrayList<HeartRateObject>> entry : readings.entrySet()) {

                    ArrayList<HeartRateObject> heartRates = entry.getValue();

                    setHeartRateTypeColor(entry.getKey());

                    for (HeartRateObject heartObject : heartRates) {
                        calendar.setTimeInMillis(heartObject.getDateTime());

                        String dt;

                        if (date.equals(format.format(calendar.getTime()))) {
                            addEntryToTypeMap(
                                    entry.getKey(),
                                    entriesMap,
                                    new Entry(i, heartObject.getHeartRate()));
                           // entries.add(new Entry(i, heartObject.getHeartRate()));

                        }
                    }


                }
            }
            //entries.add(new Entry(dates.size(), 0));
           // entries.add(new Entry(dates.size() + 2, 0));
            //entries.add(new Entry(dates.size() + 3, 0));
        }

        //Log.d(TAG, "setScatterChart: ENTRIES " + String.valueOf(entries));

        for (Map.Entry<String, ArrayList<Entry>> entry : entriesMap.entrySet()) {
            Log.d(TAG, "setScatterChart: COUNT" + String.valueOf(entry.getValue().size()));
            if (!entry.getValue().isEmpty()) {
                data.addDataSet(createScatterData(entry.getKey(), entry.getValue()));
            }
        }


        if (!entries.isEmpty()) {
//            ScatterDataSet dataSet = new ScatterDataSet(entries, "Heart Rate");
//            dataSet.setColor(Color.MAGENTA);
//            dataSet.setScatterShapeSize(7.5f);
//            dataSet.setDrawValues(false);
//            dataSet.setValueTextSize(10f);
//            dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
//            dataSet.setScatterShapeSize(50);
//            data.addDataSet(dataSet);
//            return data;
        }

        return data;


    }

    private ScatterDataSet createScatterData(String key, ArrayList<Entry> values) {

        ScatterDataSet dataSet = new ScatterDataSet(values, key);

        Log.d(TAG, "createScatterData: " + key + " " + values.size());

        if (key.equals(Constants.Const.CYCLING)) {
            dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.cycling));
        } else if (key.equals(Constants.Const.RUNNING)) {
            dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.running));
        } else if (key.equals(Constants.Const.RESTING)) {
            dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.resting));
        } else if (key.equals(Constants.Const.GENERAL)) {
            dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.general));
        } else if (key.equals(Constants.Const.WALKING)) {
            dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.walking));
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

    /**
     * Changes color of dot depending on the activity the user was doing.
     *
     * @param type
     */
    private void setHeartRateTypeColor(String type) {
        switch (type) {
            case Constants.Const.CYCLING:

                break;
            case Constants.Const.WALKING:

                break;
            case Constants.Const.RUNNING:

                break;
            case Constants.Const.RESTING:

                break;
            case Constants.Const.GENERAL:

                break;
        }
    }

    /**
     * Get the custom start date.
     *
     * @return
     */
    private String getStartDate() {
        return mStartDate.getText().toString();
    }

    /**
     * Get the custom end date.
     *
     * @return
     */
    private String getEndDate() {
        return mEndDate.getText().toString();
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
     * Date picker for the start date
     */
    DatePickerDialog.OnDateSetListener startDatePicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            try {
                updateLabel(mStartDate, myCalendar);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * Date picker for the end date
     */
    DatePickerDialog.OnDateSetListener endDatePicker = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            try {
                updateLabel(mEndDate, myCalendar);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * Start date button click event.
     */
    @OnClick(R.id.start_date)
    protected void pickStartDate() {
        new DatePickerDialog(getActivity(), startDatePicker,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * End date button click event.
     */
    @OnClick(R.id.end_date)
    protected void pickEndDate() {
        new DatePickerDialog(getActivity(), endDatePicker, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }



    private void setDatePickerPopups() {


    }

    private void updateLabel(EditText editText, Calendar myCalendar) throws ParseException {

        int id = editText.getId();

        // Get a new instance of calendar
        Calendar c = Calendar.getInstance();

        // Get todays Date
        Date today = c.getTime();

        // Get the user selected date
        Date userSelected = myCalendar.getTime();

        Log.d(TAG, "updateLabel: " + editText.getText().toString());

        // If the user selected date is after today, we display a toast notifying them
        // that the date is out of bounds
        if (userSelected.after(today)) {
            Toast.makeText(getActivity(), "Date is out of bounds", Toast.LENGTH_LONG).show();
        } else {

            // Set up the date format
            String myFormat = "dd/MM/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            String date = sdf.format(myCalendar.getTime());

            Date d;
            String dateString;

            // Must check whether start date is after end date
            if (id == R.id.start_date) {
                // Get the end date already selected if it exists
                dateString = mEndDate.getText().toString();

                if (!dateString.isEmpty()) {
                    // Parse the date string and convert into a date object
                    d = sdf.parse(dateString);

                    // If the Start date is after the end date, we display an error, otherwise
                    // the date is set to the edit text
                    if (!userSelected.equals(d) && userSelected.after(d)) {

                        Toast.makeText(getActivity(), "Start date out of bounds", Toast.LENGTH_LONG).show();
                    } else {
                        editText.setText(sdf.format(myCalendar.getTime()));
                        mCombinedChart.clear();
                        setChartData();
                    }
                } else {
                    editText.setText(sdf.format(myCalendar.getTime()));
                    mCombinedChart.clear();
                    setChartData();
                    Toast.makeText(getActivity(), "All good!", Toast.LENGTH_LONG).show();
                }
            } else {
                // This section does the opposite of above and checks to see whether the
                // end date selected is before the start date. If it is, we print an error,
                // otherwise we set the text.
                dateString = mStartDate.getText().toString();

                if (!dateString.isEmpty()) {
                    d = sdf.parse(dateString);

                    if (userSelected.before(d)) {
                        Toast.makeText(getActivity(), "End date out of bounds", Toast.LENGTH_LONG).show();
                    } else {
                        editText.setText(sdf.format(myCalendar.getTime()));
                        mCombinedChart.clear();
                        setChartData();
                    }
                } else {
                    editText.setText(sdf.format(myCalendar.getTime()));
                    mCombinedChart.clear();
                    setChartData();
                }
            }
        }
    }

    /**
     * Sets up the timeline spinner.
     */
    private void setTimelineSpinner() {

        ArrayAdapter<CharSequence> timelineList = ArrayAdapter.createFromResource(
                getActivity(), R.array.date_type_selection, android.R.layout.simple_spinner_item
        );

        timelineList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTimeline.setAdapter(timelineList);

    }

    /**
     * Sets up the spinner menu for the activities to be shown.
     */
    private void setActivityToDisplaySpinner() {
        ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                getActivity(), R.array.activity_selection, android.R.layout.simple_spinner_item
        );

        activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActivityToDisplay.setAdapter(activityList);

    }

    /**
     * Return the item in the activity spinner that is selected.
     *
     * @return
     */
    private String getActivitySpinnerItemSelected() {
        return mActivityToDisplay.getSelectedItem().toString();
    }

    private BarData generateHeartRateLimitLines() {
        return null;
    }

    private void setupChart() throws ParseException {

        mCombinedChart.getDescription().setEnabled(false);
        mCombinedChart.setBackgroundColor(Color.WHITE);
        mCombinedChart.setDrawGridBackground(false);
        mCombinedChart.setDrawBarShadow(false);
        mCombinedChart.setHighlightFullBarEnabled(false);

        HeartRateMarker mv = new HeartRateMarker(getActivity(), R.layout.chart_heart_marker, mCombinedChart);
        mv.setChartView(mCombinedChart);
        mCombinedChart.setMarker(mv);

//        // draw bars behind lines
//        mCombinedChart.setDrawOrder(new DrawOrder[]{
//                DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER
//        });

        YAxis rightAxis = mCombinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis leftAxis = mCombinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = mCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);

        if (getSpinnerValue().equals(CUSTOM_DATE)) {
            setChartDates();
        }

//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return mMonths[(int) value % mMonths.length];
//            }
//        });
    }

    /**
     *
     *
     * @throws ParseException
     */
    private void setChartDates() throws ParseException {
        mCombinedChart.getXAxis().setValueFormatter(
                new MyAxisValueFormatter(
                        DateHelper.getDatesBetween(getStartDate(), getEndDate(),
                                new SimpleDateFormat("dd/MM/yyyy", Locale.UK))));
    }

    private void setXAxisToDays() {

        HashMap<String, Date> dates = mDatabase.getDaysOfAllReadings();

        mCombinedChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM HH:mm", Locale.UK);

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });
    }
}
