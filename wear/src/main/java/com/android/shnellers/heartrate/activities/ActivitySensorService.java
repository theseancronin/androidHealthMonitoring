package com.android.shnellers.heartrate.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Sean on 18/01/2017.
 */

public class ActivitySensorService extends Fragment implements SensorEventListener,
        View.OnClickListener {

    private SensorManager mSensorManager;

    private Sensor mStepCounter;

    private Sensor mStepDetector;

    private int stepsCount;
    private int stepsDetected;

    private boolean mRunning;

    private View mView;

    private TextView mCountedView, mDetectedView;

    private Button mStartBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_start_layout, container, false);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        stepsCount = 0;

        stepsDetected = 0;

        mRunning = false;

        mCountedView = (TextView) mView.findViewById(R.id.counted);

        mDetectedView = (TextView) mView.findViewById(R.id.detected);

        mStartBtn = (Button) mView.findViewById(R.id.start_stepper);
        mStartBtn.setOnClickListener(this);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void incrementStepsCounted(final float value) {
        stepsCount += Math.round(value);
        Log.d(TAG, "incrementStepsCounted: Counted " + Integer.toString(stepsCount));
    }

    private void incrementStepsDetected() {
        stepsDetected++;
        Log.d(TAG, "incrementStepsCounted: Counted " + Integer.toString(stepsDetected));
    }

    private int getStepsCount() {
        return stepsCount;
    }

    private int getStepsDetected () {
        return stepsDetected;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            incrementStepsCounted(event.values[0]);
            Log.d(TAG, "onSensorChanged: Counted " + Integer.toString(Math.round(event.values[0])));
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            incrementStepsDetected();
            Log.d(TAG, "onSensorChanged: Detected " + Integer.toString(Math.round(event.values[0])));
        }

        updateCounterViews();

    }

    private void updateCounterViews() {
        String val = "Counted: " + Integer.toString(getStepsCount());
        mCountedView.setText(val);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String val = "Detected: " + Integer.toString(getStepsDetected());
        mDetectedView.setText(val);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        switch (v.getId()) {
            case R.id.start_stepper:
                startStepCounter();
                break;
        }
    }

    private void startStepCounter() {
        mRunning = true;
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_NORMAL);

        mStartBtn.setText("Started");
    }
}
