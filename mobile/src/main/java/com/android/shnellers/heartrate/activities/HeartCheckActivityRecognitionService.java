package com.android.shnellers.heartrate.activities;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;
import java.util.Timer;

/**
 * Created by Sean on 20/01/2017.
 */

public class HeartCheckActivityRecognitionService extends IntentService {

    private static final String TAG = "HeartCheckActivity";

    private int mCurrentActivity;

    private Handler mHandler;

    private Timer timer;

    private GoogleApiClient mGoogleApiClient;

    private HeartRateDatabase db;

    public HeartCheckActivityRecognitionService() {
        super(TAG);

    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HeartCheckActivityRecognitionService(String name) {
        super(name);


    }

    @Override
    public void onCreate() {
        super.onCreate();

        db = new HeartRateDatabase(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        String activity;
        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            if (intent.getStringExtra("intentType").equals("HeartCheckActivity")) {
                Log.d(TAG, "onHandleIntent: HEART CHECK");
                activity = handleHeartRateCheckActivity(result.getProbableActivities());

                db.storeHeartRateActivityType(activity);
            }
        }
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
    private String handleHeartRateCheckActivity(List<DetectedActivity> probableActivities) {
        for (DetectedActivity activity : probableActivities) {
            switch(activity.getType()) {
                case DetectedActivity.WALKING:
                    if (activity.getConfidence() >= 50) {
                        Log.d(TAG, "handleHeartRateCheckActivity: WALKING");
                        return Constants.Const.WALKING;

                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (activity.getConfidence() >= 50) {

                        Log.d(TAG, "handleHeartRateCheckActivity: RUNNING");
                        return Constants.Const.RUNNING;
                    }
                    break;
                case DetectedActivity.ON_BICYCLE:
                    if (activity.getConfidence() >= 50) {

                        Log.d(TAG, "handleHeartRateCheckActivity: CYCLING");
                        return Constants.Const.CYCLING;
                    }
                    break;
                case DetectedActivity.STILL:
                    if (activity.getConfidence() >= 50) {

                        Log.d(TAG, "handleHeartRateCheckActivity: RESTING");
                        return Constants.Const.RESTING;
                    }
                    break;
            }
        }
        return null;
    }
}
