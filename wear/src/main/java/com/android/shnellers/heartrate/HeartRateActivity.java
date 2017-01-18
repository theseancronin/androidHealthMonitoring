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
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeartRateActivity extends Activity implements SensorEventListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener{

    private static final boolean DEBUG = true;

    public static final String TAG = "HeartRateActivity";

    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    public static final String CHECKING = "Checking...";
    private SensorManager mSensorManager;

    private Sensor mHeartSensor;

    private TextView mResultLabel, mTopLabel;

    private ImageButton mHeartBtn;
    private boolean heartSensorActive;

    private List<Integer> sensorReadings;

    private CountDownTimer mCountDownTimer;
    private GoogleApiClient mGoogleApiClient;

    private Node mNode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mResultLabel = (TextView) findViewById(R.id.heart_rate);
        mTopLabel = (TextView) findViewById(R.id.top_label);

        mHeartBtn = (ImageButton) findViewById(R.id.check_heart_rate);
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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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
        mGoogleApiClient.disconnect();
        Log.d("HearRateActivity", "onPause");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            float heartRate = sensorEvent.values[0];
            int rounded = Math.round(heartRate);
            if (rounded > 0) {
                storeHeartSensorValue(rounded);
            }
            Log.d("HeartRateActivity", "Received heart rate: " + Integer.toString(rounded));
        }

    }

    private void storeHeartSensorValue(final int sensorValue) {
        sensorReadings.add(sensorValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // called when accuracy of sensor has changed
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.check_heart_rate:
                if (heartSensorActive) {
                    stopHeartMonitor();
                } else {
                    startHeartMonitor();
                }
                break;
        }
    }

    private void startHeartMonitor() {
        mTopLabel.setText(CHECKING);
        heartSensorActive = true;
        boolean sensorRegistered = mSensorManager.registerListener(
                this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("Sensor Status: ", "Sensor registered: " + (sensorRegistered ? "yes" : "no"));

        mCountDownTimer = new CountDownTimer(15000, 500) {
            @Override
            public void onTick(long milliseconds) {

            }

            @Override
            public void onFinish() {
                stopHeartMonitor();

            }
        }.start();
    }

    private void stopHeartMonitor() {
        heartSensorActive = false;
        mResultLabel.setText("--");
        mSensorManager.unregisterListener(this);
        Log.d("HearRateSensor", "Stopped");
        if (!getSensorReadings().isEmpty()) {
            calculateAverageHeartRate();
        } else {
            resetWithError();
        }
    }

    private void calculateAverageHeartRate() {
        List<Integer> readings = getSensorReadings();
        int sum = 0;
        int heartRate = 0;

        for (Integer reading : readings) {
            sum += reading;
        }

        heartRate = sum / readings.size();

        Log.d("HeartRate: ", Integer.toString(heartRate));

        // Get the date and time of the reading
        long dateTime = System.currentTimeMillis();


        sendMessage(heartRate, dateTime);
        displayResult(heartRate);
    }

    private void sendMessage(int heartRate, long dateTime) {
        Log.d(TAG, "sendMessage: ");
        PutDataMapRequest dataMap = PutDataMapRequest.create(WEARABLE_DATA_PATH);
        dataMap.getDataMap().putInt("reading", heartRate);
        dataMap.getDataMap().putLong("date_time", dateTime);
        dataMap.getDataMap().putInt("sensor_type", mHeartSensor.getType());
        PutDataRequest dataRequest = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        new SendMessageToDataLayer(WEARABLE_DATA_PATH, dataRequest).start();
    }


    private boolean validateConnection() {
        if (mGoogleApiClient.isConnected()) {
            return true;
        }

        ConnectionResult result = mGoogleApiClient.blockingConnect(15000, TimeUnit.MILLISECONDS);

        return result.isSuccess();
    }

    private void resetWithError() {
        mTopLabel.setText("");
    }

    private void displayResult(final int heartRate) {
        mHeartBtn.setVisibility(View.INVISIBLE);
        mResultLabel.setVisibility(View.VISIBLE);

        if(heartRate == 0) {
            mResultLabel.setText("Reading Failed: Try Again");
        } else {
            mResultLabel.setText(Integer.toString(heartRate));
        }

    }
    private List<Integer> getSensorReadings() {
        return sensorReadings;
    }

    private void resetSensorReadings() {
        sensorReadings.clear();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (DEBUG) Log.d(TAG, "onConnected: " + bundle);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
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

    public class SendMessageToDataLayer extends Thread {
        String path;
        PutDataRequest putDataRequest;

        public SendMessageToDataLayer(String path, PutDataRequest putDataRequest) {
            this.path = path;
            this.putDataRequest = putDataRequest;
        }

        @Override
        public void run() {
            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient).await();

            // GEt the node we are sending message to
            for (Node node : nodes.getNodes()) {

                final Node n = nodes.getNodes().get(0);
                PendingResult<DataItemBuffer> dataResult =
                        Wearable.DataApi.getDataItems(mGoogleApiClient);


                dataResult.setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(@NonNull DataItemBuffer dataItems) {
                        Log.d(TAG, "onResult: Item send: " + dataItems.getStatus().isSuccess());
                        Log.v(TAG, "Data Sent to: " + n.getDisplayName());
                        Log.v(TAG, "Data Node ID: " + n.getId());
                        //Log.v(TAG, "Data Nodes Size: " + nodes.getNodes().size());
                    }
                });

            }
        }
    }
}