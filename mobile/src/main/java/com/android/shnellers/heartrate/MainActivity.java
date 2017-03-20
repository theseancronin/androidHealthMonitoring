package com.android.shnellers.heartrate;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.shnellers.heartrate.activities.ActivityHome;
import com.android.shnellers.heartrate.activities.ActivityRecognitionService;
import com.android.shnellers.heartrate.activities.StepDetectorEvent;
import com.android.shnellers.heartrate.alerts.FeelingsDialogFragment;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.database.FeelingDatabaseContract;
import com.android.shnellers.heartrate.database.FeelingDatabaseHelper;
import com.android.shnellers.heartrate.database.HeartDBReceiver;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.fragments.HomeFragment;
import com.android.shnellers.heartrate.heart_rate.History;
import com.android.shnellers.heartrate.models.HeartRateObject;
import com.android.shnellers.heartrate.passport.PassportMain;
import com.android.shnellers.heartrate.settings.SettingsView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

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

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String NAME_EXTRA = "com.dev.prestigious.heartmonitor.name";
    private static final String EMAIL_EXTRA = "com.dev.prestigious.heartmonitor.email";
    private static final String TAG = "MainActivity";

    private static final boolean DEBUG = true;

    public static final String REMINDERS = "Reminders";
    public static final String SETTINGS = "Settings";
    public static final String MIN = "Min Heart Rate";
    public static final String MAX = "Max Heart Rate";
    public static final String AVG = "Avg Heart Rate";

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

    @BindView(R.id.low_value)
    protected TextView mLowReading;

    @BindView(R.id.high_value)
    protected TextView mHighReading;


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

    private HashMap<String, ArrayList<HeartRateObject>> todaysRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_home);

        ButterKnife.bind(this);

        session = new SessionManager(getApplicationContext());

        _db = new UserDatabase(this);

        mFeelingHelper = new FeelingDatabaseHelper(this);

        mHeartRateDatabase = new HeartRateDatabase(this);

        //mRecognitionDatabase = new ActivityRecognitionDatabase(this);
        //mRecognitionDatabase.populateWithMockData();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerTitles = getResources().getStringArray(R.array.extrasListMenu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        Log.d(TAG, "onCreate: ");
        Intent stepService = new Intent (getApplicationContext(), StepDetectorEvent.class);
        startService(stepService);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        todaysRecords = mHeartRateDatabase.getLast24Records();

        ActivityRecognitionDatabase db = new ActivityRecognitionDatabase(this);

        db.populateRecognitionDB();

        setAutomaticHeartRateCheck();

        setLatestHeartRate();

        setHighLowReadings();

        setFeelingIcon();

        setPieChart();

        setMinMaxAvgSpinner();
        setMinMaxAvgValues();
    }

    /**
     * Set the min/max/avg for each activity.
     */
    private void setMinMaxAvgValues() {

        // Loop through the last 24 records
        for (Map.Entry<String, ArrayList<HeartRateObject>> entry : todaysRecords.entrySet()) {
            if (!entry.getKey().equals("General")) {
                ArrayList<HeartRateObject> heartRateObjects = entry.getValue();

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

                        if (entry.getKey().equals(Constants.Const.RESTING) && (hr >= 100 || hr <= 40)) {
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

        String warningTxt = String.valueOf(restingConcerns) + " of the last 24 resting rates were unusually high/low.";
        String severeTxt = String.valueOf(restingConcerns) + " of the last 24 resting rates were unusually high/low, please consider having your heart health checked.";

        if (restingConcerns > 12) {
            mConcerns.setText(severeTxt);
            mConcerns.setTextColor(ContextCompat.getColor(this, R.color.over_120));
        } else if (restingConcerns > 0) {
            mConcerns.setText(warningTxt);
            mConcerns.setTextColor(ContextCompat.getColor(this, R.color.over_85));
        } else {
            mConcerns.setText(R.string.no_concerns);
            mConcerns.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
        }
    }

    /**
     * Sets the value of activity text view.
     *
     * @param type
     * @param value
     */
    private void setTextViewValue(String type, int value) {
        if (type.equals(Constants.Const.RESTING)) {
            mRestingTxt.setText(String.valueOf(value));

            if (value >= 100 || value <= 40) {
                mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.over_120));
            } else if (value > 80) {
                mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.over_85));
            } if (value >= 68 && value <= 80) {
                mRestingTxt.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
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
                this, R.array.min_max_avg, R.layout.spinner_item
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
     * Get the value selected in the dropdown.
     *
     * @return
     */
    private String getActivitySpinnerValue() {
        return mMinMaxAvgSpinner.getSelectedItem().toString();
    }

    /**
     * Setup the pie chart settings
     */
    private void setPieChart() {

        mPieChart.setDrawSlicesUnderHole(false);
        mPieChart.setDrawEntryLabels(false);



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
                    Constants.Const.RESTING,
                    Constants.Const.GENERAL
            ));

            PieDataSet dataSet = new PieDataSet(entries, "");

            ArrayList<Integer> colors = new ArrayList<>(
                    Arrays.asList(
                            ContextCompat.getColor(this, R.color.walking),
                            ContextCompat.getColor(this, R.color.running),
                            ContextCompat.getColor(this, R.color.cycling),
                            ContextCompat.getColor(this, R.color.resting),
                            ContextCompat.getColor(this, R.color.general)
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
        if(min != null) {
            mLowReading.setText(String.valueOf(min.getHeartRate()));
        }

        if (max != null) {
            mHighReading.setText(String.valueOf(max.getHeartRate()));
        }

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
            mHeartActivityType.setText(heartRate.getType());
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
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
       // alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, null);


    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
       // Log.i("Start isLogged", Boolean.toString(session.isLoggedIn()));

        if (!session.isLoggedIn()) {
            //Log.i("If is Logged", Boolean.toString(_db.getIsUserLoggedIn()));
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            displayUserDetails();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        unregisterReceiver(mHeartDBReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        registerReceiver(mHeartDBReceiver, new IntentFilter(HeartDBReceiver.HEART_DB_CHANGED));
    }

    @Override
    protected void onPause() {
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
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Edit the users details.
     *
     * @param view
     */
    public void editUserDetails(View view) {
        Intent intent = new Intent(MainActivity.this, PersonalDetails.class);
        startActivity(intent);

    }

    /**
     * Edit the users medication.
     *
     * @param view
     */
    public void editMedicalDetails(View view) {
        Intent intent = new Intent(MainActivity.this, CurrentMedication.class);
        startActivity(intent);
    }

    /**
     * Cancel edit.
     *
     * @param view
     */
    public void cancelEdit(View view) {
        Log.i("Cancelling", "Cancel");
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

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

    @OnClick(R.id.btn_history)
    public void launchHeartRateHistory() {
        Intent intent = new Intent(this, History.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_passport)
    public void launchPassport() {
        Intent intent = new Intent(this, PassportMain.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_extra)
    public void inflateMenuDialog() {
        Intent intent = new Intent(this, ExtrasView.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_activity)
    public void launchActivityHome() {
        Intent intent = new Intent(this, ActivityHome.class);
        startActivity(intent);
    }

    @OnClick(R.id.feeling_icon)
    public void checkUserFeelings() {

        DialogFragment fragment = FeelingsDialogFragment.newInstance();
        fragment.show(getSupportFragmentManager(), "dialog");

//        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
//        android.support.v4.app.Fragment frag = manager.findFragmentByTag("fragment_edit_name");
//        if (frag != null) {
//            manager.beginTransaction().remove(frag).commit();
//        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent myIntent = new Intent(this, ActivityRecognitionService.class);
        myIntent.putExtra("intentType", "AutoRecognition");
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String item = mDrawerList.getItemAtPosition(position).toString();

            Log.d(TAG, "onItemClick: " + item);
            if (item.equals(REMINDERS)) {
                Intent intent = new Intent(getApplicationContext(), Reminders.class);
                startActivity(intent);
            } else if (item.equals(SETTINGS)) {
                Intent intent = new Intent(getApplicationContext(), SettingsView.class);
                startActivity(intent);
            }
            selectItem(position);
        }
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

    @Override
    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }

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
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }



}