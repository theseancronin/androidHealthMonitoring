package com.android.shnellers.heartrate;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.database.WeightDBContract;
import com.android.shnellers.heartrate.database.diary.DiaryContract;
import com.android.shnellers.heartrate.database.diary.DiaryDatabase;
import com.android.shnellers.heartrate.heart_rate.UserAlertCheck;
import com.android.shnellers.heartrate.receivers.ActivityRecognitionReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.android.shnellers.heartrate.Constants.Const.ACTIVITY;
import static com.android.shnellers.heartrate.Constants.Const.RESTING;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TABLE_NAME;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TYPE;

/**
 * Created by Sean on 15/01/2017.
 */

public class SensorReceiveService extends WearableListenerService implements MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "SensorServiceReceive";
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    private static final String LATEST_STATS = "/wear/latest/heart/data";

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mActivityApiClient;

    private MessageApi.MessageListener mMessageListener;

    private HeartRateDatabase mHeartRateDatabase;

    private DiaryDatabase mDiaryDatabase;

    private String mActivity;

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStartCommand: ");
//
//        return START_STICKY;
//    }

    private ActivityRecognitionReceiver mActivityReceiver = new ActivityRecognitionReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            mActivity = intent.getStringExtra(ACTIVITY);
            Log.d(TAG, "onReceive: " + mActivity);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mActivityApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mActivityApiClient.connect();

        registerReceiver(mActivityReceiver, new IntentFilter(ActivityRecognitionReceiver.ACTIVITY_RECOGNIZED));
    }


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

        initializeGoogleAPIClient();

//        ConnectionResult connectionResult =
//                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
//
//        if (!connectionResult.isSuccess()) {
//            Log.e(TAG, "onDataChanged: Failed to connect to API client");
//            return;
//        }

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
            initializeGoogleAPIClient();
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

        String activity = "";

        mActivity = dataMap.getString(ACTIVITY);

        Log.d(TAG, "storeHeartRate: AFTER");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.UK);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(dateTime);

        mHeartRateDatabase = new HeartRateDatabase(this);

        Log.d(TAG, "storeHeartRate: " + mActivity);


        String status = determineStatus(heartRate, dateTime);

        ContentValues values = new ContentValues();
        values.put(HeartRateContract.Entry.BPM_COLUMN, heartRate);
        values.put(HeartRateContract.Entry.DATE_TIME_COLUMN, dateTime);
        values.put(Constants.Const.STATUS, status);
        values.put(TYPE, activity);
        values.put(WeightDBContract.WeightEntries.DATE, dateFormater.format(System.currentTimeMillis()));
        checkForWarnings(heartRate);

        mHeartRateDatabase.storeHeartRate(values);

        if (mActivity == null) {
            Log.d(TAG, "storeHeartRate: Check Activity");
            getRecognizedActivity(calendar);
        }

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

        Log.d(TAG, "onMessageReceived: " + messageEvent.getPath());
        String event = messageEvent.getPath();

        if (event.equals(LATEST_STATS)) {
            sendLatestHeartDataToWear();
        }

        //showToast(event);

        //Log.d(TAG, "MessageReceived");
        super.onMessageReceived(messageEvent);
    }

    private void sendLatestHeartDataToWear() {

        Log.d(TAG, "sendLatestHeartDataToWear: ");
        int count =0;
        int sum = 0;
        int avg = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + TYPE + " = '" + RESTING  +
                "' AND " + DATE_TIME_COLUMN + " >= " + calendar.getTimeInMillis() + ";";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {

                int hr = cursor.getInt(cursor.getColumnIndex(BPM_COLUMN));

                sum += hr;
                count++;

                if (hr > max) {
                    max = hr;
                }

                if (hr < min) {
                    min = hr;
                }

            }
        }

        cursor.close();
        db.close();

        if (count > 0) {
            avg = sum / count;
        }

        sendToWear(count, min, max, avg);

    }

    private void sendToWear(final int count, final int min, final int max, final int avg) {
        Log.d(TAG, "sendToWear: ");
        PutDataMapRequest dataMap = PutDataMapRequest.create(LATEST_STATS);
        dataMap.getDataMap().putInt("count", count);
        dataMap.getDataMap().putInt("min", min);
        dataMap.getDataMap().putInt("max", max);
        dataMap.getDataMap().putInt("avg", avg);

        PutDataRequest dataRequest = dataMap.asPutDataRequest();

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        new SendMessageToDataLayer(LATEST_STATS, dataRequest).start();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public String getRecognizedActivity(Calendar calendar) {
        Intent myIntent = new Intent(this, HeartCheckActivityRecognitionService.class);
        myIntent.putExtra("intentType", "HeartCheckActivity");
        myIntent.putExtra("dateOfCheck", calendar.getTimeInMillis());
        PendingIntent pendingIntent = PendingIntent.getService(
                this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mActivityApiClient, 60000, pendingIntent
        );

        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mActivityApiClient, pendingIntent);

        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initializeGoogleAPIClient();
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void initializeGoogleAPIClient() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mActivityReceiver);
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
            Log.d(TAG, "SendMessageToDataLayer: ");
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


