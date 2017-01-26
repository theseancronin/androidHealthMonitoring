package com.android.shnellers.heartrate.activities;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;

/**
 * Created by Sean on 20/01/2017.
 */

public class ActivityRecognitionService extends IntentService {

    private static final String TAG = "ActivityRecognition";

    private int mCurrentActivity;

    private Handler mHandler;

    private Timer timer;

    private GoogleApiClient mGoogleApiClient;

    private ActivityRecognitionDatabase db;

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
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult
                    .extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        Log.d(TAG, "handleDetectedActivities: ");
        for (DetectedActivity activity : probableActivities) {
            switch(activity.getType()) {
                case DetectedActivity.WALKING:
                    if (activity.getConfidence() >= 75) {
                        Log.d(TAG, "DetectedActivities: CONFIDENCE " + String.valueOf(activity.getConfidence()));
                        Log.d(TAG, "handleDetectedActivities: WALKING");

                        createActivityEntry(DetectedActivity.WALKING);

                    }
                    break;
                case DetectedActivity.STILL:
                    if (activity.getConfidence() >= 75) {
                        Log.d(TAG, "DetectedActivities: CONFIDENCE " + String.valueOf(activity.getConfidence()));
                        Log.d(TAG, "handleDetectedActivities: STILL");

                        mCurrentActivity = DetectedActivity.STILL;
                        createActivityEntry(DetectedActivity.STILL);
                        //new Thread(new ActivityTimerThread(DetectedActivity.STILL)).start();
                    }
                    break;
            }
        }
    }

    private void createActivityEntry(int activityType) {

        long time = Calendar.getInstance().getTimeInMillis();
        db.createActivityEntry(activityType, time);

    }

//    public class ActivityTimerThread implements Runnable {
//
//        public int activityType;
//
//        Handler mHandler;
//
//        public ActivityTimerThread(final int activityType) {
//            this.activityType = activityType;
//            mHandler = new Handler();
//        }
//
//        @Override
//        public void run() {
//            Log.d(TAG, "run: A second has passed");
//            mHandler.postDelayed(this, 1000);
//        }
//
//
//    }
}
