package com.android.shnellers.heartrate.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityContract;
import com.android.shnellers.heartrate.database.ActivityDatabase;
import com.android.shnellers.heartrate.models.ActivityModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.HashMap;

import static android.os.SystemClock.elapsedRealtime;

/**
 * Starts a new activity timer.
 */
public class ActivityStarted extends Activity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "ActivityStarted";

    private static final int USE_DEVICE_GPS = 1;

    private static final int UNFINISHED = 0;
    private static final int FINISHED = 1;

    public static final String RUNNING = "Running";
    public static final String WALKING = "Walking";
    public static final String CYCLING = "Cycling";
    public static final String PAUSE = "Pause";
    public static final String PLAY = "Play";
    private Chronometer mChronometer;

    private GoogleApiClient mGoogleApiClient;

    private boolean isRunning;

    private FrameLayout mPlayPause;

    private FloatingActionButton mStopBtn;
    private FloatingActionButton mPlayPauseBtn;

    private TextView mType;

    private long timeStopped;

    private String typeStr;
    private String mLongitudeText;
    private String mLatitudeText;

    private String walking;
    private String running;
    private String cycling;

    private int checkPermission;

    private LocationManager mLocationManager;
    private Location mLastLocation;

    private ActivityDatabase mActivityDatabase;

    private ActivityModel mActivityModel;

    private ContentValues values;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_started_layout);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();

        checkPermission = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (checkPermission != PackageManager.PERMISSION_GRANTED) {

        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, locationListener);
        }

        mActivityDatabase = new ActivityDatabase(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // get the timer
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        // get the start and stop button
        mPlayPauseBtn = (FloatingActionButton) findViewById(R.id.play_pause_btn);
        mStopBtn = (FloatingActionButton) findViewById(R.id.stop_btn);

        mPlayPauseBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);

        isRunning = false;

        timeStopped = 0;

        Intent intent = getIntent();

        walking = intent.getStringExtra(WALKING);
        running = intent.getStringExtra(RUNNING);
        cycling = intent.getStringExtra(CYCLING);

        mType = (TextView) findViewById(R.id.type);

        mActivityModel = mActivityDatabase.getLastActivity();

        if (mActivityModel == null) {
            getPermissionToUseDeviceGPS();

            setType();

        } else {

            //Log.d(TAG, "onCreate: FINISHED: " + String.valueOf(mActivityModel.getFinished()));
          //  Log.d(TAG, "onCreate: TYPE: " + String.valueOf(mActivityModel.getType()));
            //Log.d(TAG, "onCreate: ID: " + String.valueOf(mActivityModel.getID()));
            if (mActivityModel.getFinished() == UNFINISHED) {
                typeStr = mActivityModel.getType();
                mType.setText(mActivityModel.getType());

                continueActivity();
            } else {
                typeStr = mActivityModel.getType();
                mType.setText(mActivityModel.getType());
            }
        }
    }

    private void setType() {

        if (walking != null) {
            typeStr = walking;
            mType.setText(walking);
        } else if (running != null) {
            typeStr = running;
            mType.setText(running);
        } else if (cycling != null) {
            typeStr = cycling;
            mType.setText(cycling);
        }
    }

    private void continueActivity() {

       // isRunning = true;
        Log.d(TAG, "continueActivity: ");

        timeStopped = elapsedRealtime() - mActivityModel.getStartTime();
        playOrPauseChronometer();

    }

    /**
     *
     */
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: PAUSE");
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Request permission to use GPS.
     */
    private void getPermissionToUseDeviceGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            }

            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    USE_DEVICE_GPS);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == USE_DEVICE_GPS) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "GPS PERMISSION GRANTED");
            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION
                );

                if (showRationale) {

                } else {
                    Log.d(TAG, "onRequestPermissionsResult: DENIED");
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause_btn:
                Log.d(TAG, "onClick: ");
                playOrPauseChronometer();
                break;
            case R.id.stop_btn:
                stopChronometer();
                break;


        }
    }

    /**
     * Stop the timer.
     */
    private void stopChronometer() {
        isRunning = false;
        mChronometer.stop();

        long timeElapsed = 0;

        HashMap<String, Integer> timeMap;

        long endTime = SystemClock.elapsedRealtime();

      //  if (mActivityModel == null) {
            mActivityModel = latestActivity();
        //}

       // if (mActivityModel == null || mActivityModel.getFinished() == FINISHED) {
        //    timeElapsed = endTime - mChronometer.getBase();

          //  timeMap = Calculations.convertMillisecondsToTime(timeElapsed);
        //} else {
            timeElapsed = endTime - mActivityModel.getEndTime();
            timeMap = Calculations.convertMillisecondsToTime(timeElapsed);
       // }



        finishDBActivity(endTime, timeElapsed);

        showSummary(timeMap);

    }

    private void showSummary(HashMap<String, Integer> timeMap) {
        Intent intent = new Intent(this, ActivityEndSummary.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(typeStr, typeStr);
        intent.putExtra(ActivityContract.ActivityEntries.TIME_MAP, timeMap);
        startActivity(intent);
        finish();
    }

    private void finishDBActivity(final long endTime, final long timeElapsed) {
        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntries.END_TIME_COLUMN, endTime);
        values.put(ActivityContract.ActivityEntries.TIME_TAKEN_COLUMN, timeElapsed);
        values.put(ActivityContract.ActivityEntries.FINISHED_COLUMN, FINISHED);
        mActivityDatabase.finishActivity(mActivityModel.getID(), values);
    }

    /**
     * Play or pause the timer.
     */
    private void playOrPauseChronometer() {
        if (isRunning) {
            isRunning = false;
            // Keep track of the time when stopped
            timeStopped = elapsedRealtime() - mChronometer.getBase();
            // stop the timer
            mChronometer.stop();
            // Set button text to play

            mPlayPauseBtn.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp));
        } else {
            isRunning = true;
            // restart the timer from the exact position when stopped.
            if (timeStopped > 0) {
                mChronometer.setBase(elapsedRealtime() - timeStopped);
            } else {
                mChronometer.setBase(elapsedRealtime());
            }
            mChronometer.start();
            mPlayPauseBtn.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp));

            if (mActivityModel == null) {
                storeNewActivity();
            }
        }
    }

    private ActivityModel latestActivity() {
        return mActivityDatabase.getLastActivity();
    }

    private void storeNewActivity() {
        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntries.TYPE_COLUMN, typeStr);
        values.put(ActivityContract.ActivityEntries.START_TIME_COLUMN, elapsedRealtime());
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());
        values.put(ActivityContract.ActivityEntries.FINISHED_COLUMN, UNFINISHED);
        mActivityDatabase.storeActivity(values);
    }

    /**
     *
     * @return
     */
    private String getTodaysDate() {
        Calendar mCalendar = Calendar.getInstance();

        String todaysDate = String.valueOf(mCalendar.get(Calendar.MONTH)) + "/" +
                String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)) + "/" +
                String.valueOf(mCalendar.get(Calendar.YEAR));

        return todaysDate;
    }

    /**
     *
     */
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                    mGoogleApiClient);
//            if (mLastLocation != null) {
//                // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//                //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
//            }
//        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
        //    editLocation.setText("");
          //  pb.setVisibility(View.INVISIBLE);
            //Toast.makeText(
             //       getBaseContext(),
               //     "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                //            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
//            String longitude = "Longitude: " + loc.getLongitude();
//            Log.v(TAG, longitude);
//            String latitude = "Latitude: " + loc.getLatitude();
//            Log.v(TAG, latitude);
//
//        /*------- To get city name from coordinates -------- */
//            String cityName = null;
//            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
//            List<Address> addresses;
//            try {
//                addresses = gcd.getFromLocation(loc.getLatitude(),
//                        loc.getLongitude(), 1);
//                if (addresses.size() > 0) {
//                    System.out.println(addresses.get(0).getLocality());
//                    cityName = addresses.get(0).getLocality();
//                }
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
//                    + cityName;
            //editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public String getLongitudeText() {
            return mLongitudeText;
        }

        public String getLatitudeText() {
            return mLatitudeText;
        }
    }
}
