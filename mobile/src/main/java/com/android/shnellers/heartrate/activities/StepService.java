package com.android.shnellers.heartrate.activities;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.shnellers.heartrate.database.StepsDBHelper;

/**
 * Created by Sean on 19/01/2017.
 */

public class StepService extends Service implements SensorEventListener {

    private static final String TAG = "StepService";

    private SensorManager mSensorManager;

    private Sensor mStepDetector;

    private StepsDBHelper mStepsDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: StepServiceStarted");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
            mStepsDBHelper = new StepsDBHelper(this);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: ");
        Toast.makeText(getApplicationContext(), "Step Detected", Toast.LENGTH_LONG).show();
        mStepsDBHelper.createStepsEntry();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: ");
       //
    }

}
