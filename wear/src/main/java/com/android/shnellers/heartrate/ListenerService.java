package com.android.shnellers.heartrate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.shnellers.heartrate.heart_rate.HeartRateServiceStarter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Calendar;

import static com.android.shnellers.heartrate.Constants.Const.ACTIVITY;

/**
 * Created by Sean on 16/01/2017.
 */

public class ListenerService extends WearableListenerService implements MessageApi.MessageListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "MyDataMap...";
    // Start with forward slash
    private static final String WEARABLE_DATA_PATH = "/reminders/data/path";

    private static final String LATEST_STATS = "/wear/latest/heart/data";

    private static final String HEART_RATE_DATA_PATH = "/heart/data/path";
    public static final int PATH = 0;
    public static final int ACTIVITY_INDEX = 1;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        Log.d(TAG, "onDataChanged: ");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

//        ConnectionResult connectionResult =
//                googleApiClient .blockingConnect(30, TimeUnit.SECONDS);
//
//        if (!connectionResult.isSuccess()) {
//            Log.e(TAG, "onDataChanged: Failed to connect to API client");
//            return;
//        }

        for (DataEvent event : dataEventBuffer) {
            DataItem dataItem = event.getDataItem();

            Uri uri = dataItem.getUri();

            String path = uri.getPath();

               Log.d(TAG, "path: " + path);
            if (path.equals(WEARABLE_DATA_PATH)) {
                Log.d(TAG, "onDataChanged: Contains path");

                unpack(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            }

            if (path.equals(HEART_RATE_DATA_PATH)) {
                startHeartRateService(null);
            }

            if (path.equals(LATEST_STATS)) {

                Log.d(TAG, "RECEIVED");

            }
        }
    }

    private void unpack(DataMap dataMap) {

        Log.d(TAG, "unpack: ");

        ArrayList<DataMap> remindersMap = dataMap.getDataMapArrayList("reminders_map");

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        ArrayList<PendingIntent> intentsArray = new ArrayList<>();
        if (!remindersMap.isEmpty()) {

            for (int i = 0; i < remindersMap.size(); i++) {

                NotificationCompat.Builder n1 = new NotificationCompat.Builder(this)
                        .setContentTitle("Heart Monitor Reminder")
                        .setContentText("Time to check Heart Rate")
                        .setSmallIcon(R.mipmap.ic_launcher);

                Intent intent = new Intent(this, NotificationPublisher.class);
                intent.putExtra(NotificationPublisher.NOTIFICATION_ID, i);
                intent.putExtra(NotificationPublisher.NOTIFICATION, n1.build());

                // Loop counter alarm is used as request code
                PendingIntent pi = PendingIntent.getBroadcast(this, i, intent, 0);

                DataMap map = remindersMap.get(i);

                Log.d(TAG, "unpack: ALARM SETTING: " + String.valueOf(map.getInt("hour")) + ":" +
                                                        String.valueOf(map.getInt("minute")));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, map.getInt("hour"));
                calendar.set(Calendar.MINUTE, map.getInt("minute"));
                calendar.set(Calendar.SECOND, 0);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                    Log.d("Reminders", "Alarm set: " + Integer.toString(time.getHour()) + " : " + Integer.toString(time.getMinute()));
//                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
//
//                } else {
//                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
//                }

            }
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String[] msgSplit = messageEvent.getPath().split(",");

        Log.d(TAG, "onMessageReceived: ACTIVITY: " + msgSplit[ACTIVITY_INDEX]);

        if (msgSplit[PATH].equals(HEART_RATE_DATA_PATH)) {
            startHeartRateService(msgSplit[ACTIVITY_INDEX]);
        }

        // Compare the paths
        if (messageEvent.getPath().equals(WEARABLE_DATA_PATH)) {
            final String message = new String(messageEvent.getData());

            Intent startIntent = new Intent (this, WearMainActivity.class);
            startIntent.putExtra("message", message);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear history
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "onMessageReceived: Main Activity Started");
        }

        if (messageEvent.getPath().equals(LATEST_STATS)) {

            Log.d(TAG, "RECEIVED");

        }

        super.onMessageReceived(messageEvent);
    }

    private void startHeartRateService(String activity) {
        Intent intent = new Intent(this, HeartRateServiceStarter.class);

        if (activity != null) {
            intent.putExtra(ACTIVITY, activity);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
