package com.android.shnellers.heartrate;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class HeartRateActivity extends Activity implements SensorEventListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener{

    private static final boolean DEBUG = true;
    public static final String TAG = "HeartRateActivity";

    private SensorManager mSensorManager;

    private Sensor mHeartSensor;

    private TextView mTextView;

    private Button mHeartBtn;
    private boolean heartSensorActive;

    private List<Integer> sensorReadings;

    private CountDownTimer mCountDownTimer;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mTextView = (TextView) findViewById(R.id.heart_rate);
        mTextView.setText("--");

        mHeartBtn = (Button) findViewById(R.id.heart);
        mHeartBtn.setOnClickListener(this);

        heartSensorActive = false;

        sensorReadings = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // start listening for events on the heart sensor
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        mGoogleApiClient.connect();
        Log.d("HearRateActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mHeartSensor);
        Log.d("HearRateActivity", "onPause");
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float heartRate = sensorEvent.values[0];
            int rounded = Math.round(heartRate);
            sensorReadings.add(rounded);
            mTextView.setText(Float.toString(heartRate));

            if (DEBUG) Log.d("HeartRateActivity", "Received heart rate: " + Integer.toString(rounded));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // called when accuracy of sensor has changed
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.heart:
                //  if (heartSensorActive) {
                //   stopHeartMonitor();
                // } else {
                startHeartMonitor();
                //  }

                break;
        }
    }

    private void startHeartMonitor() {
        mTextView.setText("Please wait...");
        heartSensorActive = true;
        boolean sensorRegistered = mSensorManager.registerListener(
                this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Sensor Status: ", "Sensor registered: " + (sensorRegistered ? "yes" : "no"));

        mCountDownTimer = new CountDownTimer(10000, 500) {
            @Override
            public void onTick(long milliseconds) {
                //  if (mHeartBtn.getVisibility() == View.VISIBLE) {
                //     mHeartBtn.setVisibility(View.INVISIBLE);
                Log.d("Seconds Left: ", Long.toString(milliseconds / 1000));
                // } else {
                //    mHeartBtn.setVisibility(View.VISIBLE);
                //}
            }

            @Override
            public void onFinish() {
                stopHeartMonitor();
            }
        }.start();
    }

    private void stopHeartMonitor() {
        heartSensorActive = false;
        mTextView.setText("--");
        mSensorManager.unregisterListener(this);
    }

    private int count = 0;

    private void increaseCounter() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/count");
        // Obtain a data map that you can put values on
        putDataMapRequest.getDataMap().putInt("counterKey", count++);
        // obtains a put data request object
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        // DataApi.putDataItem requests the system to create the data item
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (DEBUG) Log.d(TAG, "onConnected: " + bundle);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (DEBUG) Log.d(TAG, "onConnectionSuspended: " + cause);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (DEBUG) Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }
}
