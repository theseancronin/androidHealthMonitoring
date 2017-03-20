package com.android.shnellers.heartrate;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.shnellers.heartrate.activities.HeartCheckActivityRecognitionService;
import com.android.shnellers.heartrate.database.HeartRateContract;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.WeightDBContract;
import com.android.shnellers.heartrate.database.diary.DiaryContract;
import com.android.shnellers.heartrate.database.diary.DiaryDatabase;
import com.android.shnellers.heartrate.heart_rate.UserAlertCheck;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sean on 15/01/2017.
 */

public class SensorReceiveService extends WearableListenerService implements MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "SensorServiceReceive";
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";
    private GoogleApiClient mGoogleApiClient;

    private MessageApi.MessageListener mMessageListener;

    private HeartRateDatabase mHeartRateDatabase;

    private DiaryDatabase mDiaryDatabase;

    private String mRecogizedActivity;

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand: ");
//
//        return START_STICKY;
//    }

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
      //  Log.d(TAG, "onDataChanged: " + dataEventBuffer);

        Log.d(TAG, "onDataChanged: ");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

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

         //   Log.d(TAG, "path: " + path);
            if (path.equals(WEARABLE_DATA_PATH)) {
             //   Log.d(TAG, "onDataChanged: Contains path");

                unpack(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            }
        }
    }

    private void unpack(DataMap dataMap) {

        String diaryEntry = dataMap.getString(Constants.Const.DIARY_ENTRY);

        if (dataMap.getInt("sensor_type") == Sensor.TYPE_HEART_RATE) {
            storeHeartRate(dataMap);
        }

        if (diaryEntry != null) {
            storeDiaryEntry(dataMap);
        }
    }

    /**
     * Saves the received diary log to the database.
     *
     * @param dataMap
     */
    private void storeDiaryEntry(DataMap dataMap) {

        mDiaryDatabase = new DiaryDatabase(this);

        Log.d(TAG, "storeDiaryEntry: " + dataMap.getString(Constants.Const.DIARY_ENTRY));

        // Create and add the log information to the content values.
        ContentValues values = new ContentValues();
        values.put(DiaryContract.DiaryEntry.ENTRY, dataMap.getString(Constants.Const.DIARY_ENTRY));
        values.put(DiaryContract.DiaryEntry.DATE_TIME, dataMap.getLong(Constants.Const.ENTRY_TIME));

        mDiaryDatabase.insertEntry(values);

    }

    /**
     * Stores the heart rate to the database.
     *
     * @param dataMap
     */
    private void storeHeartRate(DataMap dataMap) {
        int heartRate = dataMap.getInt("reading");

        long dateTime = dataMap.getLong("date_time");

        String dateFormat = "dd/MM/yyyy hh:mm:ss.SSS";

        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        String activity = getRecognizedActivity();

        Log.d(TAG, "storeHeartRate: AFTER");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.UK);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(dateTime);

        mHeartRateDatabase = new HeartRateDatabase(this);

        String status = determineStatus(heartRate, dateTime);

        ContentValues values = new ContentValues();
        values.put(HeartRateContract.Entry.BPM_COLUMN, heartRate);
        values.put(HeartRateContract.Entry.DATE_TIME_COLUMN, dateTime);
        values.put(Constants.Const.STATUS, status);
        values.put(WeightDBContract.WeightEntries.DATE, dateFormater.format(System.currentTimeMillis()));
        checkForWarnings(heartRate);

        mHeartRateDatabase.storeHeartRate(values);

        // Log.d(TAG, "Date & Time: " + simpleDateFormat.format(calendar.getTime()));
        //Log.d(TAG, "Heart_Rate: " + Integer.toString(heartRate));
    }

    private String determineStatus(int heartRate, long dateTime) {

        String status = "OK";

        if (heartRate > 100) {
            status = "High";
        }else if (heartRate <= 40 || heartRate >= 120) {
            status = "Extreme";

            Intent intent = new Intent(this, UserAlertCheck.class);
            intent.putExtra("heart_rate", heartRate);
            intent.putExtra("date_time", dateTime);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_heart_white)
                    .setContentTitle("Abnormal heart rate detected")
                    .setContentText(String.valueOf(heartRate) + " bpm detected")
                    .setContentIntent(pi);

            NotificationManagerCompat nm = NotificationManagerCompat.from(this);

            nm.notify(2, notification.build());
        }

        return status;
    }

    private void checkForWarnings(int heartRate) {

        int age = 32;

        if (heartRate >= 120) {
            notifyUserOfAbnormalHeartRate();
        }

    }

    private void notifyUserOfAbnormalHeartRate() {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.d(TAG, "onMessageReceived: ");
        String event = messageEvent.getPath();

        showToast(event);

        //Log.d(TAG, "MessageReceived");
        super.onMessageReceived(messageEvent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public String getRecognizedActivity() {

        initializeGoogleAPIClient();

        Intent myIntent = new Intent(this, HeartCheckActivityRecognitionService.class);
        myIntent.putExtra("intentType", "HeartCheckActivity");
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient, 60000, pendingIntent
        );

       ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, pendingIntent);

        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initializeGoogleAPIClient() {

    }
}


