package com.android.shnellers.heartrate.heart_rate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.CurrentMedication;
import com.android.shnellers.heartrate.LoginActivity;
import com.android.shnellers.heartrate.PersonalDetails;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.SessionManager;
import com.android.shnellers.heartrate.User;
import com.android.shnellers.heartrate.ViewPagerAdapter;
import com.android.shnellers.heartrate.activities.ActivityRecognitionService;
import com.android.shnellers.heartrate.activities.StepDetectorEvent;
import com.android.shnellers.heartrate.alerts.FeelingsDialogFragment;
import com.android.shnellers.heartrate.charts.TimeAxisFormatter;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.database.FeelingDatabaseContract;
import com.android.shnellers.heartrate.database.FeelingDatabaseHelper;
import com.android.shnellers.heartrate.database.HeartDBReceiver;
import com.android.shnellers.heartrate.database.HeartRateContract;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.fragments.HomeFragment;
import com.android.shnellers.heartrate.models.ActivityStats;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.android.shnellers.heartrate.models.HourlyHeartRateStats;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.android.shnellers.heartrate.servicealarms.ServiceAlarms;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static com.android.shnellers.heartrate.Constants.Const.LESS_THAN_40;
import static com.android.shnellers.heartrate.Constants.Const.OVER_100;
import static com.android.shnellers.heartrate.Constants.Const.RESTING;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TYPE;

/**
 * Created by Sean on 03/04/2017.
 */

public class MainFrag extends Fragment implements View.OnClickListener,
            GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String NAME_EXTRA = "com.dev.prestigious.heartmonitor.name";

    private static final String EMAIL_EXTRA = "com.dev.prestigious.heartmonitor.email";

    private static final String TAG = "MainFrag";

    private static final boolean DEBUG = true;

    public static final String REMINDERS = "Reminders";
    public static final String SETTINGS = "Settings";
    public static final String MIN = "Min Heart Rate";
    public static final String MAX = "Max Heart Rate";
    public static final String AVG = "Avg Heart Rate";

    private static final String ALL_DAY = "All Day";
    private static final String MORNING = "Morning 5am - 12pm";
    private static final String AFTERNOON = "Afternoon 12pm - 17pm";
    private static final String EVENING = "Evening 17pm - 21pm";
    private static final String NIGHT = "Night 21am - 4am";

    private static final int IS_SET = 1;
    private static final int NOT_SET = 0;
    private static final int DEFAULT = -1;

    private User _loggedUser;

    private UserDatabase _db;

    private ActivityRecognitionDatabase mRecognitionDatabase;

    private SessionManager session;

    private String[] mMenuItems = {"hello", "world"};

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;

    private ImageButton activitiesBtn;

    private ViewPagerAdapter adapter;

    private String[] mDrawerTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private FeelingDatabaseHelper mFeelingHelper;

    private HeartRateDatabase mHeartRateDatabase;

    private View mView;

    @BindView(R.id.btn_activity)
    protected Button mActivityBtn;

    @BindView(R.id.btn_history)
    protected Button mHistoryBtn;

    @BindView(R.id.btn_extra)
    protected Button mExtraBtn;

    @BindView(R.id.btn_passport)
    protected Button mPassportBtn;

    @BindView(R.id.feeling_icon)
    protected ImageButton mFeelingBtn;

    @BindView(R.id.latest_heart_rate)
    protected TextView mLatestReading;

    @BindView(R.id.status)
    protected TextView mStatus;
//
//    @BindView(R.id.low_value)
//    protected TextView mLowReading;
//
//    @BindView(R.id.high_value)
//    protected TextView mHighReading;

    @BindView(R.id.heart_check_activity_type)
    protected TextView mHeartActivityType;

    @BindView(R.id.pie_chart)
    protected PieChart mPieChart;

    @BindView(R.id.time)
    protected TextView mTime;

    @BindView(R.id.txt_walking_mma)
    protected TextView mWalkingTxt;

    @BindView(R.id.txt_running_mma)
    protected TextView mRunningTxt;

    @BindView(R.id.txt_cycling_mma)
    protected TextView mCyclingTxt;

    @BindView(R.id.txt_resting_mma)
    protected TextView mRestingTxt;

    @BindView(R.id.resting_concerns)
    protected TextView mConcerns;

    @BindView(R.id.min_max_avg)
    protected Spinner mMinMaxAvgSpinner;

    @BindView(R.id.top_spinner)
    protected Spinner mTopSpinner;

    @BindView(R.id.line_chart)
    protected LineChart mLineChart;

    @BindView(R.id.resting_rate_count)
    protected TextView mRestingRateCount;

    @BindView(R.id.average)
    protected TextView mAverageResting;

    @BindView(R.id.over_100)
    protected TextView mOver100;

    @BindView(R.id.over_117)
    protected TextView mOver117;

    @BindView(R.id.over_135)
    protected TextView mOver135;

    @BindView(R.id.under_40)
    protected TextView mUnder40;

    private HashMap<String, ArrayList<HeartRateObject>> todaysRecords;

    private ArrayList<HourlyHeartRateStats> mHourlyList;

    private ArrayList<ActivityStats> stats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.heart_rate_home, container, false);

        ButterKnife.bind(this, mView);

        session = new SessionManager(getActivity());

        _db = new UserDatabase(getActivity());

        mFeelingHelper = new FeelingDatabaseHelper(getActivity());

        mHeartRateDatabase = new HeartRateDatabase(getActivity());

        mRecognitionDatabase = new ActivityRecognitionDatabase(getActivity());

        //mRecognitionDatabase = new ActivityRecognitionDatabase(this);
        //mRecognitionDatabase.populateWithMockData();

//        mHeartRateDatabase.clearDatabase();
//         mRecognitionDatabase.clearDatabase();
//        PopulateDB.PopulateActivityRecognitionDB(new ActivityRecognitionDBHelper(getActivity()), new HeartRateDBHelper(getActivity()));
//        mHeartRateDatabase.populateDB();

        Log.d(TAG, "onCreate: ");
        Intent stepService = new Intent (getActivity(), StepDetectorEvent.class);
        getActivity().startService(stepService);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        todaysRecords = mHeartRateDatabase.getLast24Records();

        mHourlyList = new ArrayList<>();

        setAutomaticHeartRateCheck();

        setLatestHeartRate();

        setHighLowReadings();

        setFeelingIcon();

        setPieChart();

        setTopSpinner();

        setMinMaxAvgSpinner();
        setMinMaxAvgValues();

        testTodaysData();

        setAnalysisAlarms();

        try {
            setLineChart();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //inflateHourlyAnalysisFragment();

        return mView;
    }

    private void inflateHourlyAnalysisFragment() {

        FragmentManager fm = getActivity().getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        HourlyAnalysisFragment haf = new HourlyAnalysisFragment();

        ft.add(R.id.hourly_analysis_card, haf);

        ft.commit();

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
            mLineChart.getXAxis().setEnabled(true);
            mLineChart.getXAxis().setDrawGridLines(false);
            mLineChart.getAxisLeft().setDrawGridLines(false);
//            /mLineChart.getXAxis().setValueFormatter(new TimeAxisFormatter());

            updateLineChart(getTopSpinnerValue());
        }

        /**
         * Updates the line chart values;
         *
         * @param topSpinnerValue
         */
        private void updateLineChart(final String topSpinnerValue) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            ArrayList<HeartRateObject> rates = new ArrayList<>();
            ArrayList<HourlyHeartRateStats> statsArrayList = new ArrayList<>();
            SQLiteDatabase db = new HeartRateDBHelper(getActivity()).getReadableDatabase();



            long now = System.currentTimeMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, - 3);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);

            String query = "SELECT *" +
                    " FROM " + HeartRateContract.Entry.TABLE_NAME +
                    " WHERE " + TYPE + " = '" + RESTING + "' AND " + DATE_TIME_COLUMN + " > " +
                    calendar.getTimeInMillis() + " ORDER BY " + DATE_TIME_COLUMN + " ASC;";

            Cursor c = db.rawQuery(query, null);
            Log.d(TAG, "updateLineChart: " + String.valueOf(c.getCount()));

            long[] axisValues = new long[200];
            int x = 0;

            while (calendar.getTimeInMillis() < now) {

                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                axisValues[x] = calendar.getTimeInMillis();
                HourlyHeartRateStats stats = createHourObjects(c, currentHour, dateFormat.format(calendar.getTimeInMillis()));
                if (stats != null) {
                    mHourlyList.add(stats);
                }

                calendar.add(Calendar.HOUR_OF_DAY, 1);
                x++;
            }


            c.close();
            db.close();

            mLineChart.getXAxis().setValueFormatter(new TimeAxisFormatter(axisValues));

            displayLineGraphDetails();
        }

        private HourlyHeartRateStats createHourObjects(final Cursor c, final int currentHour, final String date) {

            HourlyHeartRateStats stats = new HourlyHeartRateStats(currentHour);
            stats.setHeartRates(new ArrayList<Integer>());

            Calendar calendar = Calendar.getInstance();

            if (c.moveToFirst()) {
                while (c.moveToNext()) {

                    calendar.setTimeInMillis(c.getLong(c.getColumnIndex(DATE_TIME_COLUMN)));
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    if (date.equals(c.getString(c.getColumnIndex(DATE))) && hour == currentHour) {
                        // Get the heart rate and hourly stats object
                        int hr = c.getInt(c.getColumnIndex(BPM_COLUMN));
                        stats.getHeartRates().add(hr);

                        // Set the max hr for the hour
                        if (hr > stats.getMax()) {
                            stats.setMax(hr);
                        }

                        // Set the minimum hr for the hour
                        if (hr < stats.getMin() && hr != 0) {
                            stats.setMin(hr);
                        }

                        // Add hr to the total sum of hrs for this hour
                        stats.setSum(stats.getSum() + hr);

                        // Increment the number of heart rates detected
                        stats.setNumberOfHeartRates(stats.getNumberOfHeartRates() + 1);
                    }
                }
            }

            if (!stats.getHeartRates().isEmpty()) {
                return stats;
            }
            return null;
        }

        private void displayLineGraphDetails() {
            ArrayList<Entry> lineEntries = new ArrayList<>();
            ArrayList<Entry> dummy = new ArrayList<>();
            ArrayList<Entry> over100List = new ArrayList<>();
            ArrayList<Entry> minList = new ArrayList<>();
            ArrayList<ArrayList<Entry>> entries = new ArrayList<>();

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
            String current;
            String next;

            int count = 0;
            int sum = 0;
            int over100 = 0;
            int overt117 = 0;
            int over135 = 0;
            int under40 = 0;

            if (!mHourlyList.isEmpty()) {
                for (int i = 0; i < mHourlyList.size(); i++) {
                    // int average = statsArrayList.get(i).getSum() / statsArrayList.get(i).getNumberOfHeartRates();


                    HourlyHeartRateStats stat = mHourlyList.get(i);

                    current = format.format(stat.getDateTime());

                    if (getTopSpinnerValue().equals(MORNING)) {

                        if (stat.getHour() >= 6 && stat.getHour() < 12) {
                            addValueToArray(lineEntries, over100List, minList, stat, i);
                            for (int v = 0; v < stat.getHeartRates().size(); v++) {
                                int hr = stat.getHeartRates().get(v);

                                if (hr >= 135) {
                                    over135 ++;
                                }

                                if (hr >= 117) {
                                    overt117++;
                                }

                                if (hr < 40) {
                                    under40++;
                                }

                                if (hr >= 100) {
                                    over100++;
                                }

                                if (hr > 0) {
                                    sum += hr;
                                    count++;
                                }
                            }
                        }

                    } else if (getTopSpinnerValue().equals(AFTERNOON)) {
                        if (stat.getHour() >= 12 && stat.getHour() < 17) {
                            addValueToArray(lineEntries, over100List, minList, stat, i);
                            for (int v = 0; v < stat.getHeartRates().size(); v++) {
                                int hr = stat.getHeartRates().get(v);

                                if (hr >= 135) {
                                    over135 ++;
                                }

                                if (hr >= 117) {
                                    overt117++;
                                }

                                if (hr >= 100) {
                                    over100++;
                                }

                                if (hr < 40) {
                                    under40++;
                                }

                                if (hr > 0) {
                                    sum += hr;
                                    count++;
                                }
                            }
                        }
                    } else if (getTopSpinnerValue().equals(EVENING)) {
                        if (stat.getHour() >= 17 && stat.getHour() < 21) {
                            addValueToArray(lineEntries, over100List, minList, stat, i);
                            for (int v = 0; v < stat.getHeartRates().size(); v++) {
                                int hr = stat.getHeartRates().get(v);

                                if (hr >= 135) {
                                    over135 ++;
                                }

                                if (hr >= 117) {
                                    overt117++;
                                }

                                if (hr >= 100) {
                                    over100++;
                                }
                                if (hr < 40) {
                                    under40++;
                                }


                                if (hr > 0) {
                                    sum += hr;
                                    count++;
                                }
                            }
                        }
                    } else if (getTopSpinnerValue().equals(NIGHT)) {
                        if (stat.getHour() >= 21 && stat.getHour() < 5) {
                            addValueToArray(lineEntries, over100List, minList, stat, i);
                            for (int v = 0; v < stat.getHeartRates().size(); v++) {
                                int hr = stat.getHeartRates().get(v);

                                if (hr >= 135) {
                                    over135 ++;
                                }

                                if (hr >= 117) {
                                    overt117++;
                                }

                                if (hr >= 100) {
                                    over100++;
                                }

                                if (hr < 40) {
                                    under40++;
                                }

                                if (hr > 0) {
                                    sum += hr;
                                    count++;
                                }
                            }
                        }
                    } else {
                        addValueToArray(lineEntries, over100List, minList, stat, i);
                        for (int v = 0; v < stat.getHeartRates().size(); v++) {
                            int hr = stat.getHeartRates().get(v);

                            if (hr >= 135) {
                                over135 ++;
                            }

                            if (hr >= 117) {
                                overt117++;
                            }

                            if (hr >= 100) {
                                over100++;
                            }

                            if (hr < 40) {
                                under40++;
                            }

                            if (hr > 0) {
                                sum += hr;
                                count++;
                            }
                        }
                    }

                }


                mRestingRateCount.setText(count > 0 ? String.valueOf(count) : String.valueOf(0));
                mOver100.setText(over100 > 0 ? String.valueOf(over100) : String.valueOf(0));
                mOver117.setText(overt117 > 0 ? String.valueOf(overt117) : String.valueOf(0));
                mOver135.setText(over135 > 0 ? String.valueOf(over135) : String.valueOf(0));
                mUnder40.setText(under40 > 0 ? String.valueOf(under40) : String.valueOf(0));
                mAverageResting.setText(count > 0 ? String.valueOf(sum / count) : String.valueOf(0));

                LineDataSet restingDataSet = createLineDataSet(lineEntries, RESTING);
                LineDataSet highSet = createLineDataSet(over100List, OVER_100);
                LineDataSet minSet = createLineDataSet(minList, LESS_THAN_40);

                LineData set = new LineData(restingDataSet, highSet, minSet);
                mLineChart.setData(set);

                mLineChart.invalidate();
            }
        }

        private void addValueToArray(ArrayList<Entry> avgList,
                                     ArrayList<Entry> maxList,
                                     ArrayList<Entry> minList,
                                     HourlyHeartRateStats stat,
                                     int x) {
            avgList.add(new Entry(x, stat.getAverage()));
            maxList.add(new Entry(x, stat.getMax()));
            if (stat.getMin() > 0) {
                minList.add(new Entry(x, stat.getMin()));
            }

        }


        private LineDataSet createLineDataSet(ArrayList<Entry> entries, final String val) {

            String legend = " Resting BPM";
            String type;

            if (val.equals(OVER_100)) {
                type = "Maximum";
            } else if (val.equals(LESS_THAN_40)) {
                type = "Minimum";
            } else {
                type = "Average";
            }

            int color = 0;
            if (val.equals(RESTING)) {
                color = ContextCompat.getColor(getActivity(), R.color.resting);
            } else if (val.equals(OVER_100)) {
                color = ContextCompat.getColor(getActivity(), R.color.over_120);
            } else if (val.equals(LESS_THAN_40)) {
                color = ContextCompat.getColor(getActivity(), R.color.over_40);
            }

            LineDataSet lineDataSet = new LineDataSet(entries, type);
            lineDataSet.setColor(color);
            lineDataSet.setFillColor(color);
            lineDataSet.setFillAlpha(65);
            lineDataSet.setCircleColor(color);
            lineDataSet.setLineWidth(1f);
            lineDataSet.setCircleRadius(3f);
            lineDataSet.setDrawCircles(true);
            lineDataSet.setDrawCircleHole(true);

            if (!getTopSpinnerValue().equals(ALL_DAY)) {
               lineDataSet.enableDashedLine(5, 5, 0);
            } else {
                //lineDataSet.setVisible(false);
            }

            lineDataSet.setValueTextSize(9f);
            lineDataSet.setDrawFilled(false);
            lineDataSet.setFormLineWidth(1f);
            lineDataSet.setDrawValues(false);
            //lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            lineDataSet.setFormSize(15.f);

            return lineDataSet;
        }

        public ArrayList<HourlyHeartRateStats> getHourlyList() {
            return mHourlyList;
        }

        /**
         *
         */
        private void setAnalysisAlarms() {

            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//        int isSet = sharedPref.getInt(getString(R.string.alarms_set), NOT_SET);
//
//        if (isSet == NOT_SET) {
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putInt(getString(R.string.alarms_set), IS_SET);
//
//            // Set the alarm managers for the applications analysis
            ServiceAlarms.setHourlyAnalysisCheck(getActivity());
            ServiceAlarms.setDailyAnalysisCheck(getActivity());

            //    editor.apply();
            //}

        }

        private void testTodaysData() {


            HashMap<String, ArrayList<RecognizedActivity>> activities = mRecognitionDatabase.getTodaysActivities();

            Log.d(TAG, "testTodaysData: " + String.valueOf(activities.size()));

            for (Map.Entry<String, ArrayList<RecognizedActivity>> entry : activities.entrySet()) {
                ArrayList<RecognizedActivity> acts = entry.getValue();

                if (!acts.isEmpty()) {
                    for (RecognizedActivity act : acts) {
                        System.out.println("Activity: " + act.getType() + " Min: " + String.valueOf(act.getMinutes()));
                    }
                }

            }

        }

        /**
         * Set the min/max/avg for each activity.
         */
        private void setMinMaxAvgValues() {

            // Loop through the last 24 records
            for (Map.Entry<String, ArrayList<HeartRateObject>> entry : todaysRecords.entrySet()) {
                if (!entry.getKey().equals("General")) {
                    ArrayList<HeartRateObject> heartRateObjects = entry.getValue();
                    Log.d(TAG, "setMinMaxAvgValues: " + String.valueOf(heartRateObjects.size()));

                    if (!heartRateObjects.isEmpty()) {
                        // Initialize the min, max, avg and sum variables
                        int min = Integer.MAX_VALUE;
                        int max = 0;
                        int avg = 0;
                        int sum = 0;
                        int restingConcerns = 0;

                        // Loop the heart rate objects for activities and work out the min, max and
                        // avg heart rates.
                        for (HeartRateObject heartRateObject : heartRateObjects) {
                            int hr = heartRateObject.getHeartRate();

                            if (hr < min) {
                                min = hr;
                            }

                            if (hr > max) {
                                max = hr;
                            }

                            sum += hr;

                            if (entry.getKey().equals(RESTING) && (hr >= 100 || hr <= 40)) {
                                restingConcerns++;
                            }

                        }

                        avg = sum / heartRateObjects.size();

                        if (getActivitySpinnerValue().equals(MIN)) {
                            setTextViewValue(entry.getKey(), min);
                        } else if (getActivitySpinnerValue().equals(MAX)) {
                            setTextViewValue(entry.getKey(), max);
                        } else {
                            setTextViewValue(entry.getKey(), avg);
                        }

                        setRestingConcerTxt(restingConcerns);
                    }



                }
            }
        }

        private void setRestingConcerTxt(int restingConcerns) {

            String warningTxt = String.valueOf(restingConcerns) + " suspicious resting rates recorded in the last 3 days. " +
                    "If you are feeling unwell please log symptoms in your diary.";
            String severeTxt = String.valueOf(restingConcerns) +
                    " suspicious resting rates recorded in the last 3 days. If you are feeling unwell " +
                    "please log symptoms in your diary and seek medical advice if necessary.";

            if (restingConcerns > 30) {
                mConcerns.setText(severeTxt);
                mConcerns.setTextColor(ContextCompat.getColor(getActivity(), R.color.over_120));
            } else if (restingConcerns > 20) {
                mConcerns.setText(warningTxt);
                mConcerns.setTextColor(ContextCompat.getColor(getActivity(), R.color.over_85));
            } else {
                mConcerns.setText(R.string.no_concerns);
                mConcerns.setTextColor(ContextCompat.getColor(getActivity(), R.color.textPrimary));
            }
        }

        /**
         * Sets the value of activity text view.
         *
         * @param type
         * @param value
         */
        private void setTextViewValue(String type, int value) {
            if (type.equals(RESTING)) {
                mRestingTxt.setText(String.valueOf(value));

                if (value >= 100 || value <= 40) {
                    // mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.over_120));
                } else if (value > 80) {
                    // mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.over_85));
                } if (value >= 68 && value <= 80) {
                    // mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                }

            } else if (type.equals(Constants.Const.WALKING)) {
                mWalkingTxt.setText(String.valueOf(value));
            } else if (type.equals(Constants.Const.RUNNING)) {
                mRunningTxt.setText(String.valueOf(value));
            } else {
                mCyclingTxt.setText(String.valueOf(value));
            }
        }

        /**
         * Sets up the spinner to determine what values to show.
         */
        private void setMinMaxAvgSpinner() {
            ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                    getActivity(), R.array.min_max_avg, R.layout.spinner_item
            );

            activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mMinMaxAvgSpinner.setAdapter(activityList);

            mMinMaxAvgSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    setMinMaxAvgValues();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        /**
         * Sets up the spinner to determine what values to show.
         */
        private void setTopSpinner() {
            ArrayAdapter<CharSequence> activityList = ArrayAdapter.createFromResource(
                    getActivity(), R.array.time_of_day, R.layout.spinner_item
            );

            activityList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mTopSpinner.setAdapter(activityList);

            mTopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    displayLineGraphDetails();
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
            return mMinMaxAvgSpinner.getSelectedItem().toString();
        }
        /**
         * Get the value selected in the dropdown.
         *
         * @return
         */
        private String getTopSpinnerValue() {
            return mTopSpinner.getSelectedItem().toString();
        }


        /**
         * Setup the pie chart settings
         */
        private void setPieChart() {

            mPieChart.setDrawSlicesUnderHole(false);
            mPieChart.setDrawEntryLabels(false);
            mPieChart.setDrawHoleEnabled(false);


            Legend legend = mPieChart.getLegend();
            legend.setEnabled(true);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
            legend.setXEntrySpace(5f);
            legend.setYEntrySpace(0f);
//        legend.setYOffset(-10f);
            updatePieChart();
        }

        /**
         * Update the pie chart display
         */
        private void updatePieChart() {

            ArrayList<PieEntry> entries = new ArrayList<>();

            int sum = 0;

            if (todaysRecords != null) {
                for (Map.Entry<String, ArrayList<HeartRateObject>> entry : todaysRecords.entrySet()) {
                    ArrayList<HeartRateObject> heartRateObjects = entry.getValue();

                    sum += heartRateObjects.size();
                }

                Log.d(TAG, "updatePieChart: SUM " + String.valueOf(sum));
                int position = 0;

                for (Map.Entry<String, ArrayList<HeartRateObject>> entry : todaysRecords.entrySet()) {
                    ArrayList<HeartRateObject> heartRateObjects = entry.getValue();

                    if (!entry.getKey().equals("General")) {
                        entries.add(new PieEntry(
                                Calculations.calculatePercentage(heartRateObjects.size(), sum),
                                entry.getKey(),
                                position
                        ));
                    }


                    position++;
                }

                ArrayList<String> labels = new ArrayList<>(Arrays.asList(
                        Constants.Const.WALKING,
                        Constants.Const.RUNNING,
                        Constants.Const.CYCLING,
                        RESTING,
                        Constants.Const.GENERAL
                ));

                PieDataSet dataSet = new PieDataSet(entries, "");

                ArrayList<Integer> colors = new ArrayList<>(
                        Arrays.asList(
                                ContextCompat.getColor(getActivity(), R.color.walking),
                                ContextCompat.getColor(getActivity(), R.color.running),
                                ContextCompat.getColor(getActivity(), R.color.cycling),
                                ContextCompat.getColor(getActivity(), R.color.resting),
                                ContextCompat.getColor(getActivity(), R.color.general)
                        )
                );



                dataSet.setColors(colors);

                //dataSet.setDrawIcons(false);

                dataSet.setSliceSpace(3f);
                // dataSet.setIconsOffset(new MPPointF(0, 40));
                dataSet.setSelectionShift(5f);
                dataSet.setDrawValues(true);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);

                mPieChart.setData(data);

                // undo all highlights
                mPieChart.highlightValues(null);

                mPieChart.invalidate();
            }

        }

        /**
         * This method sets the high and low heart rate readings.
         */
        private void setHighLowReadings() {

            HashMap<String, HeartRateObject> lowHigh = mHeartRateDatabase.getMinAndMaxReadings();
            HeartRateObject min = lowHigh.get("Min");
            HeartRateObject max = lowHigh.get("Max");
//            if(min != null) {
//                mLowReading.setText(String.valueOf(min.getHeartRate()));
//            }
//
//            if (max != null) {
//                mHighReading.setText(String.valueOf(max.getHeartRate()));
//            }

        }

        private HeartDBReceiver mHeartDBReceiver = new HeartDBReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                setLatestHeartRate();
            }
        };



//    private void setScatterChart() {
//
//        ArrayList<String> dates = DateHelper.getLast7DaysAsDate();
//
//        ArrayList<Entry> entries = new ArrayList<>();
//
//        ArrayList<HeartRateObject> readings = mHeartRateDatabase.get7DayReadings();
//
//        SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.UK);
//
//        Calendar calendar = Calendar.getInstance();
//
//        if (!readings.isEmpty()) {
//            for (int i = 0; i < dates.size(); i++) {
//
//                String date = dates.get(i);
//
//                for (HeartRateObject reading : readings) {
//
//                    calendar.setTimeInMillis(reading.getDateTime());
//
//                    String dt;
//
//                    if (date.equals(format.format(calendar.getTime()))) {
//                        entries.add(setEntry(i, reading.getHeartRate()));
//                    }
//                }
//            }
//        }
//
//        if (!entries.isEmpty()) {
//            ScatterDataSet dataSet = new ScatterDataSet(entries, "# of calls");
//            dataSet.setDrawValues(false);
//            dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
//            dataSet.setColor(Color.MAGENTA);
//
//            ScatterData data = new ScatterData(dataSet);
//
//            mScatterChart.setData(data);
//        }
//
//    }

        private Entry setEntry(int index, int value) {
            if (index == 0) {
                return new Entry((float) index, value);
            } else if (index == 1) {
                return new Entry((float) index, value);
            } else if (index == 2) {
                return new Entry((float) index, value);
            } else if (index == 3) {
                return new Entry((float) index, value);
            } else if (index == 4) {
                return new Entry((float) index, value);
            } else if (index == 5) {
                return new Entry((float) index, value);
            } else {
                return new Entry((float) index, value);
            }
        }

        /**
         * Sets the latest heart rate obtained by the user.
         */
        private void setLatestHeartRate() {
            HeartRateObject heartRate = mHeartRateDatabase.getLatestHeartRate();

            if (heartRate != null) {
                mLatestReading.setText(String.valueOf(heartRate.getHeartRate()));
                mStatus.setText(heartRate.getStatus());
                if (!heartRate.getType().isEmpty()) {
                    mHeartActivityType.setText(heartRate.getType());
                } else {
                    mHeartActivityType.setText("General");
                }

                mTime.setText(new SimpleDateFormat("HH:mm", Locale.UK).format(heartRate.getDateTime()));
            } else {
                mLatestReading.setText(String.valueOf(0));
                mStatus.setText("-");
                mHeartActivityType.setText("-");
            }

        }

        private void setAutomaticHeartRateCheck() {

            Log.d(TAG, "setAutomaticHeartRateCheck: ");
            // Intent intent = new Intent(this, HeartRateServiceStarter.class);

            //intent.setAction("packagename.ACTION");

            //PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
            //        0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.set(Calendar.HOUR_OF_DAY, 19);
            calendar.set(Calendar.MINUTE, 18);
            calendar.set(Calendar.SECOND, 0);
            AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            // alarm.cancel(pendingIntent);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, null);


        }

    /* Called whenever we call invalidateOptionsMenu() */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
//        return super.onPrepareOptionsMenu(menu);
//    }

        @Override
        public void onAttachFragment(Fragment fragment) {
            super.onAttachFragment(fragment);

            if (fragment.equals(new HomeFragment())) {
                Log.d(TAG, "onAttachFragment: confirmed");
            }
        }

        /**
         * Logging out the user. Will set isLoggedIn flag to false in shared
         * preferences Clears the user data from sqlite users table
         * */
        private void logoutUser() {
            session.setLogin(false);

            _db.deleteUsers();

            // Launching the login activity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }


        @Override
        public void onStart() {
            super.onStart();
            // Log.i("Start isLogged", Boolean.toString(session.isLoggedIn()));

            if (!session.isLoggedIn()) {
                //Log.i("If is Logged", Boolean.toString(_db.getIsUserLoggedIn()));
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            } else {
                displayUserDetails();
            }

        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d(TAG, "onStop: ");
            getActivity().unregisterReceiver(mHeartDBReceiver);
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "onResume: ");
            getActivity().registerReceiver(mHeartDBReceiver, new IntentFilter(HeartDBReceiver.HEART_DB_CHANGED));
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause: ");
        }



        public void setFeelingIcon() {
            SQLiteDatabase db = mFeelingHelper.getReadableDatabase();
            String query = "SELECT * FROM " + FeelingDatabaseContract.FeelingConsts.TABLE_NAME +
                    " WHERE id = (SELECT MAX(id) from " +
                    FeelingDatabaseContract.FeelingConsts.TABLE_NAME + " );";
            Cursor c = db.rawQuery(query, null);

            if (c.moveToFirst()) {
                String feelingType = c.getString(
                        c.getColumnIndex(FeelingDatabaseContract.FeelingConsts.TYPE_COLUMN));
                switch (feelingType) {
                    case "Sad":
                        setIcon(R.drawable.ic_sad);
                        break;
                    case "Not Good":
                        setIcon(R.drawable.ic_not_good);
                        break;
                    case "Neutral":
                        setIcon(R.drawable.ic_neutral);
                        break;
                    case "Good":
                        setIcon(R.drawable.ic_good);
                        break;
                    case "Happy":
                        setIcon(R.drawable.ic_happy);
                        break;
                }

            }

            c.close();
            db.close();
        }

        private void setIcon(int icon) {
            mFeelingBtn.setImageResource(icon);
        }

        /**
         * Authenticate that the user is actually logged in.
         *
         * @return
         */
        private boolean authenticate() {
            return _db.getIsUserLoggedIn();
        }

        /**
         * Display the user details.
         */
        private void displayUserDetails() {

            User user = session.getLoggedInUser();


        }

        /**
         * Log the user out of the application.
         *
         * @param view
         */
        public void applicationLogout(View view) {
            session.setLogin(false);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }

        /**
         * Edit the users details.
         *
         * @param view
         */
        public void editUserDetails(View view) {
            Intent intent = new Intent(getActivity(), PersonalDetails.class);
            startActivity(intent);

        }

        /**
         * Edit the users medication.
         *
         * @param view
         */
        public void editMedicalDetails(View view) {
            Intent intent = new Intent(getActivity(), CurrentMedication.class);
            startActivity(intent);
        }

//    /**
//     * Cancel edit.
//     *
//     * @param view
//     */
//    public void cancelEdit(View view) {
//        Log.i("Cancelling", "Cancel");
//        this.onBackPressed();
//    }

//    @Override
//    public void onBackPressed() {
//        if(getFragmentManager().getBackStackEntryCount() == 0) {
//            super.onBackPressed();
//        }
//        else {
//            getFragmentManager().popBackStack();
//        }
//    }

        public void sendBtnClicked (View v){
            Log.d(TAG, "sendBtnClicked: ");

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.btn_activity:

                    break;
                case R.id.activity_view:
                    //adapter.swapFragment(new ActivityHome());

                    break;
                case R.id.btn_extra:

                    break;

            }
        }

//    @OnClick(R.id.btn_history)
//    public void launchHeartRateHistory() {
//        Intent intent = new Intent(this, History.class);
//        startActivity(intent);
//    }
//
//    @OnClick(R.id.btn_passport)
//    public void launchPassport() {
//        Intent intent = new Intent(this, PassportMain.class);
//        startActivity(intent);
//    }
//
//    @OnClick(R.id.btn_extra)
//    public void inflateMenuDialog() {
//        Intent intent = new Intent(this, ExtrasView.class);
//        startActivity(intent);
//    }
//
//    @OnClick(R.id.btn_activity)
//    public void launchActivityHome() {
//        Intent intent = new Intent(this, ActivityMain.class);
//        startActivity(intent);
//    }

        @OnClick(R.id.feeling_icon)
        public void checkUserFeelings() {

            DialogFragment fragment = FeelingsDialogFragment.newInstance();
            fragment.show(getActivity().getSupportFragmentManager(), "dialog");

//        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
//        android.support.v4.app.Fragment frag = manager.findFragmentByTag("fragment_edit_name");
//        if (frag != null) {
//            manager.beginTransaction().remove(frag).commit();
//        }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Intent myIntent = new Intent(getActivity(), ActivityRecognitionService.class);
            myIntent.putExtra("intentType", "AutoRecognition");
            PendingIntent pendingIntent = PendingIntent.getService(
                    getActivity(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient, 60000, pendingIntent
            );
        }



        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended: ");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed: ");
        }



        /** Swaps fragments in the main content view */
        private void selectItem(int position) {
            // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();
//
//        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
        }

//    @Override
//    public void setTitle(CharSequence title) {
////        mTitle = title;
////        getActionBar().setTitle(mTitle);
//    }

        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: RECIEVED");
                int result = intent.getIntExtra("newHeartRateAdded", -1);

                if (result == RESULT_OK) {
                    toast("Updated");
                } else {
                    toast("not updated");
                }
            }
        };

        private void toast(final String str)  {
            Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
        }



    }

