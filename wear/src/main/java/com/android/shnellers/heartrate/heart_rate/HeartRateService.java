package com.android.shnellers.heartrate.heart_rate;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;

import com.android.shnellers.heartrate.R;
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

/**
 * Created by Sean on 12/02/2017.
 */

public class HeartRateService extends Service implements SensorEventListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener {

    public static final String TAG = "HeartRateService";

    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";
    public static final String HEART_RATE = "heart_rate";
    public static final String DATE_TIME = "date_time";

    private final IBinder mServiceBinder = new ServiceBinder();

    private SensorManager mSensorManager;

    private Sensor mHeartSensor;

    private List<Integer> sensorReadings;

    private GoogleApiClient mGoogleApiClient;

    private boolean heartSensorActive;

    private CountDownTimer mCountDownTimer;

    private Node mNode;

//    /**
//     * Creates an IntentService.  Invoked by your subclass's constructor.
//     *
//     * @param name Used to name the worker thread, important only for debugging.
//     */
//    public HeartRateService(String name) {
//        super(name);
//    }

    public HeartRateService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mHeartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        sensorReadings = new ArrayList<>();

        heartSensorActive = false;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();


            if (mSensorManager != null) {
            mSensorManager.registerListener(this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        NotificationCompat.Builder ncb = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart)
                .setContentTitle("Checking Heart Rate")
                .setContentText("Remain Still");

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);

            nm.notify(1, ncb.build());



        startHeartMonitor();

        return Service.START_NOT_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//
//    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged: ");

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE ) {

            float heartRate = event.values[0];
            int rounded = Math.round(heartRate);
            if (rounded > 0) {
                storeHeartSensorValue(rounded);
            }
            Log.d("HeartRateActivity", "Received heart rate: " + Integer.toString(rounded));
        }
    }

    /**
     *
     */
    private void startHeartMonitor() {
        heartSensorActive = true;

        boolean sensorRegistered = mSensorManager.registerListener(
                this, mHeartSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Log.d("Sensor Status: ", "Sensor registered: " + (sensorRegistered ? "yes" : "no"));

        mCountDownTimer = new CountDownTimer(15000, 500) {
            @Override
            public void onTick(long milliseconds) {
                Log.d(TAG, "onTick: ");
            }

            @Override
            public void onFinish() {
                stopHeartMonitor();

            }
        }.start();
    }

    /**
     *
     */
    private void stopHeartMonitor() {
        heartSensorActive = false;


        mSensorManager.unregisterListener(this);

        Log.d("HearRateSensor", "Stopped");

        if (!getSensorReadings().isEmpty()) {
            calculateAverageHeartRate();
        }
    }
    /**
     *
     * @return
     */
    private List<Integer> getSensorReadings() {
        return sensorReadings;
    }


    /**
     *
     */
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

        if (excessiveHeartRateDetected(heartRate)) {
            alertUserOfExcessiveHeartRate(heartRate, dateTime);
        }

        sendMessage(heartRate, dateTime);
    }

    private void alertUserOfExcessiveHeartRate(int heartRate, long dateTime) {

        Intent intent = new Intent(this, UserAlertCheck.class);
        intent.putExtra(HEART_RATE, heartRate);
        intent.putExtra(DATE_TIME, dateTime);
        PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart)
                .setContentTitle("Warning, Excessive heart rate detected")
                .setContentText(String.valueOf(heartRate) + " bpm detected")
                .setContentIntent(pi);

        NotificationManagerCompat nm = NotificationManagerCompat.from(this);

        nm.notify(2, notification.build());
    }

    private boolean excessiveHeartRateDetected(int heartRate) {

        boolean warning = false;

        if (heartRate >= 120) {
            warning = true;
        }

        return warning;
    }

    /**
     *
     * @param heartRate
     * @param dateTime
     */
    private void sendMessage(int heartRate, long dateTime) {
        Log.d(TAG, "sendMessage: ");
        PutDataMapRequest dataMap = PutDataMapRequest.create(WEARABLE_DATA_PATH);
        dataMap.getDataMap().putInt("reading", heartRate);
        dataMap.getDataMap().putLong("date_time", dateTime);
        dataMap.getDataMap().putInt("sensor_type", mHeartSensor.getType());
        PutDataRequest dataRequest = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        new HeartRateService.SendMessageToDataLayer(WEARABLE_DATA_PATH, dataRequest).start();

        stopSelf();
    }

    /**
     *
     * @param sensorValue
     */
    private void storeHeartSensorValue(final int sensorValue) {
        sensorReadings.add(sensorValue);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    /**
     * The type Send message to data layer.
     */
    public class SendMessageToDataLayer extends Thread {
        /**
         * The Path.
         */
        String path;
        /**
         * The Put data request.
         */
        PutDataRequest putDataRequest;
        /**
         * Instantiates a new Send message to data layer.
         *
         * @param path           the path
         * @param putDataRequest the put data request
         */
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
                        //Log.v(TAG, "Data Sent to: " + n.getDisplayName());
                        //Log.v(TAG, "Data Node ID: " + n.getId());
                        //Log.v(TAG, "Data Nodes Size: " + nodes.getNodes().size());
                    }
                });

            }
        }
    }

    public class ServiceBinder extends Binder{
        HeartRateService getService() {
            return HeartRateService.this;
        }
    }
}
