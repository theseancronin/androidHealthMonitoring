package com.android.shnellers.heartrate.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.android.shnellers.heartrate.ActivitySummary;
import com.android.shnellers.heartrate.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Starts a new activity timer.
 */
public class ActivityStarted extends Activity implements View.OnClickListener {

    private static final String TAG = "ActivityStarted";

    public static final String RUNNING = "Running";
    public static final String WALKING = "Walking";
    public static final String CYCLING = "Cycling";
    public static final String PAUSE = "Pause";
    public static final String PLAY = "Play";

    private Chronometer mChronometer;

    private boolean isRunning;

    private CardView mPlayPause;

    private CardView mStop;

    private TextView mPlayPauseText;

    private TextView mType;

    private long timeStopped;

    private String typeStr;

    private LocationManager mLocationManager;

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

        int check = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (check != PackageManager.PERMISSION_GRANTED) {

        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, locationListener);
        }


        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mPlayPauseText = (TextView) findViewById(R.id.play_pause_txt);

        mPlayPause = (CardView) findViewById(R.id.play_pause);
        mStop = (CardView) findViewById(R.id.stop);

        mPlayPause.setOnClickListener(this);
        mStop.setOnClickListener(this);

        isRunning = false;

        timeStopped = 0;

        Intent intent = getIntent();

        String walking = intent.getStringExtra(WALKING);
        String running = intent.getStringExtra(RUNNING);
        String cycling = intent.getStringExtra(CYCLING);

        mType = (TextView) findViewById(R.id.type);

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

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pause:
                Log.d(TAG, "onClick: ");
                playOrPauseChronometer();
                break;
            case R.id.stop:
                stopChronometer();
                break;


        }
    }

    /**
     * Stop the timer.
     */
    private void stopChronometer() {
        mChronometer.stop();

        long timeElapsed = SystemClock.elapsedRealtime() - mChronometer.getBase();

        int hours = (int) (timeElapsed / 3600000);
        int minutes = (int) (timeElapsed - hours * 3600000) / 60000;
        int seconds = (int) (timeElapsed - hours * 3600000 - minutes * 60000) / 1000;

        StringBuffer buffer = new StringBuffer();


        Intent intent = new Intent(this, ActivitySummary.class);
        intent.putExtra(typeStr, typeStr);
        intent.putExtra("time", timeElapsed);

    }

    /**
     * Play or pause the timer.
     */
    private void playOrPauseChronometer() {
        if (isRunning) {
            isRunning = false;
            // Keep track of the time when stopped
            timeStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
            mPlayPauseText.setText(PLAY);
        } else {
            Log.d(TAG, "playOrPauseChronometer: " + String.valueOf(timeStopped));
            isRunning = true;
            // restart the timer from the exact position when stopped.
            if (timeStopped > 0) {
                Log.d(TAG, "playOrPauseChronometer: GREATER");
                mChronometer.setBase(SystemClock.elapsedRealtime() + timeStopped);
            } else {
                mChronometer.setBase(SystemClock.elapsedRealtime());
            }
            mChronometer.start();
            mPlayPauseText.setText(PAUSE);

        }
    }

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

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
        //    editLocation.setText("");
          //  pb.setVisibility(View.INVISIBLE);
            Toast.makeText(
                    getBaseContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);

        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;
            //editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
