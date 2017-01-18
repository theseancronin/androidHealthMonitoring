package com.android.shnellers.heartrate;

import android.app.Fragment;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Sean on 10/01/2017.
 */
public class SensorFragment extends Fragment implements SensorEventListener {

    private static final float SHAKE_THRESHOLD = 1.1f;
    private static final int SHAKE_WAIT_TIME = 250;
    private static final float ROTATION_THRESHOLD = 2.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    public static final String SENSOR_TYPE = "sensorType";

    private View mView;
    private TextView mTextTitle, mTextValues;
    private SensorManager mSensorManger;
    private Sensor mSensor;
    private int mSensorType;
    private long mShakeTime;
    private long mRotationTime;

    public static SensorFragment newInstance(int sensorType) {
        SensorFragment sf = new SensorFragment();

        // Supply sensor type as argument
        Bundle args = new Bundle();
        args.putInt(SENSOR_TYPE, sensorType);
        sf.setArguments(args);

        return sf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        if (args != null) {
            Log.d("args", "not Null");
            mSensorType = args.getInt(SENSOR_TYPE);
        }
        else {
            Log.d("args", "Null");
        }
        Log.d("sensor", Integer.toString(mSensorType));

        Log.d("sensorFragment", "onCreate");


        mSensorManger = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.sensor, container, false);

        mTextTitle = (TextView) mView.findViewById(R.id.text_title);

        Log.d("sensorFragment", "onCreateView");

        if (Build.VERSION.SDK_INT >= 24) {
          //  Log.d("sensorFragment", Integer.toString(mSensor.getId()));
        }


        if (mSensor != null) {
            mTextTitle.setText(mSensor.getStringType());
        }

        mTextValues = (TextView) mView.findViewById(R.id.text_values);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManger.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManger.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        Log.d("StepCounter", sensorEvent.toString());

        mTextValues.setText(
                "x = " + Float.toString(sensorEvent.values[0]) + "\n" +
                "y = " + Float.toString(sensorEvent.values[1]) + "\n" +
                "z = " + Float.toString(sensorEvent.values[2]) + "\n"
        );

        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(sensorEvent);
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            detectRotation(sensorEvent);
        }
    }

    private void detectRotation(SensorEvent sensorEvent) {
        long now = System.currentTimeMillis();



        if((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            // Change background color if rate of rotation around any
            // axis and in any direction exceeds threshold;
            // otherwise, reset the color
            if(Math.abs(sensorEvent.values[0]) > ROTATION_THRESHOLD ||
                    Math.abs(sensorEvent.values[1]) > ROTATION_THRESHOLD ||
                    Math.abs(sensorEvent.values[2]) > ROTATION_THRESHOLD) {
                mView.setBackgroundColor(Color.rgb(0, 100, 0));
            }
            else {
                mView.setBackgroundColor(Color.BLACK);
            }
        }
    }

    private void detectShake(SensorEvent sensorEvent) {
        long now = System.currentTimeMillis();

        if ((now - mShakeTime) > SHAKE_WAIT_TIME) {
            mShakeTime = now;

            float gX = sensorEvent.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = sensorEvent.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = sensorEvent.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if (gForce > SHAKE_THRESHOLD) {
                mView.setBackgroundColor(Color.rgb(0, 100, 0));
            } else {
                mView.setBackgroundColor(Color.BLACK);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
