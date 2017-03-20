package com.android.shnellers.heartrate.activities;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

/**
 * Created by Sean on 20/01/2017.
 */

public class ActivityRecognitionService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
                            GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener  {

    private static final String TAG = "ActivityRecognition";

    private static final String HEART_RATE_DATA_PATH = "/heart/data/path";

    private static final String CHECK_HEART_RATE = "check_activity_heart_rate";

    private int mCurrentActivity;

    private Handler mHandler;

    private Timer timer;

    private ActivityRecognitionDatabase db;

    private GoogleApiClient mGoogleApiClient;

    private Node mNode;

    public ActivityRecognitionService() {
        super(TAG);

    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActivityRecognitionService(String name) {
        super(name);


    }

    @Override
    public void onCreate() {
        super.onCreate();

        db = new ActivityRecognitionDatabase(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (intent.getStringExtra("intentType").equals("AutoRecognition")) {
                Log.d(TAG, "onHandleIntent: AUTO RECOGNITION");
                handleDetectedActivities(result.getProbableActivities());
            }

            if (intent.getStringExtra("intentType").equals("HeartCheckActivity")) {
                Log.d(TAG, "onHandleIntent: HEART CHECK");
                handleHeartRateCheckActivity(result.getProbableActivities());
            }
        }
    }

    private void handleHeartRateCheckActivity(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities)
            switch (activity.getType()) {
                case DetectedActivity.WALKING:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.WALKING);

                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.RUNNING);
                    }
                    break;
                case DetectedActivity.ON_BICYCLE:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.ON_BICYCLE);
                    }
                    break;
                case DetectedActivity.STILL:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.ON_BICYCLE);
                       // checkHeartRate(Constants.Const.RESTING);
                    }
                    break;
            }
    }

    private void checkHeartRate (String activity) {

        Log.d(TAG, "CHECK HEART RATE: ");

        PutDataMapRequest dataMap = PutDataMapRequest.create(HEART_RATE_DATA_PATH);

        dataMap.getDataMap().putString(CHECK_HEART_RATE, activity);

        PutDataRequest dataRequest = dataMap.asPutDataRequest();

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

        new SendMessageToDataLayer(HEART_RATE_DATA_PATH, dataRequest).start();
    }

    /**
     * Takes as argument a list of activities that have been detected and their
     * confidence score, which is used to determine how confident the system is
     * that it predicted the right activity. High Confidence is better with a
     * score of 75 and over. Anything less than that and could have inaccurate
     * results.
     *
     * @param probableActivities
     */
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities) {
            switch(activity.getType()) {
                case DetectedActivity.WALKING:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.WALKING);
                        checkHeartRate(Constants.Const.WALKING);
                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.RUNNING);
                    }
                    break;
                case DetectedActivity.ON_BICYCLE:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.ON_BICYCLE);
                    }
                    break;
                case DetectedActivity.STILL:
                    if (activity.getConfidence() >= 50) {
                        createActivityEntry(DetectedActivity.STILL);
                        checkHeartRate(Constants.Const.RESTING);
                    }
                    break;
            }
        }
    }

    private void createActivityEntry(int activityType) {

        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        int hourInSeconds = calendar.get(Calendar.HOUR) * 3600;
        int minInSeconds = calendar.get(Calendar.MINUTE) * 60;
        int seconds = calendar.get(Calendar.SECOND);


        Date date = new Date(System.currentTimeMillis());
        date.getTime();

        int timeInSeconds = hourInSeconds + minInSeconds + seconds;

        Log.d(TAG, "createActivityEntry: CALENDAR:" + String.valueOf(timeInSeconds));
        long time = System.currentTimeMillis();
        db.createActivityEntry(activityType, timeInSeconds, time);

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
            Log.d(TAG, "SendMessageToDataLayer: ");
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
