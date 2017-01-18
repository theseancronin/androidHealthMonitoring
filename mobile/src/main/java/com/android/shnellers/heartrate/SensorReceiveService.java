package com.android.shnellers.heartrate;

import android.content.ContentValues;
import android.hardware.Sensor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.shnellers.heartrate.database.HeartRateContract;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sean on 15/01/2017.
 */

public class SensorReceiveService extends WearableListenerService implements MessageApi.MessageListener {

    private static final String TAG = "SensorServiceReceive";
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";
    private GoogleApiClient mGoogleApiClient;

    private MessageApi.MessageListener mMessageListener;

    private HeartRateDatabase mHeartRateDatabase;


    @Override
    public void onPeerConnected(Node node) {
        super.onPeerConnected(node);

        Log.d(TAG, "onPeerConnected");
    }

    @Override
    public void onPeerDisconnected(Node node) {
        super.onPeerDisconnected(node);
        Log.d(TAG, "onPeerDisconnected");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "onDataChanged: Failed to connect to API client");
            return;
        }

        for (DataEvent event : dataEventBuffer) {
            DataItem dataItem = event.getDataItem();

            Uri uri = dataItem.getUri();

            String path = uri.getPath();

            Log.d(TAG, "path: " + path);
            if (path.equals(WEARABLE_DATA_PATH)) {
                Log.d(TAG, "onDataChanged: Contains path");

                unpack(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            }
        }
    }

    private void unpack(DataMap dataMap) {
        if (dataMap.getInt("sensor_type") == Sensor.TYPE_HEART_RATE) {
            storeHeartRate(dataMap);
        }
    }

    private void storeHeartRate(DataMap dataMap) {
        int heartRate = dataMap.getInt("reading");

        long dateTime = dataMap.getLong("date_time");

        String dateFormat = "dd/MM/yyyy hh:mm:ss.SSS";

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(dateTime);

        mHeartRateDatabase = new HeartRateDatabase(this);

        ContentValues values = new ContentValues();
        values.put(HeartRateContract.Entry.BPM_COLUMN, heartRate);
        values.put(HeartRateContract.Entry.DATE_TIME_COLUMN, dateTime);

        mHeartRateDatabase.storeHeartRate(values);

        Log.d(TAG, "Date & Time: " + simpleDateFormat.format(calendar.getTime()));
        Log.d(TAG, "Heart_Rate: " + Integer.toString(heartRate));
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String event = messageEvent.getPath();

        showToast(event);

        Log.d(TAG, "MessageReceived");
        super.onMessageReceived(messageEvent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}


