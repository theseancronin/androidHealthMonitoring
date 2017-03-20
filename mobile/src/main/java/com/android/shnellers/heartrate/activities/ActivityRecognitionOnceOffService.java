package com.android.shnellers.heartrate.activities;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

/**
 * Created by Sean on 20/01/2017.
 */

public class ActivityRecognitionOnceOffService extends IntentService {

    private static final String TAG = "ActivityRecognition";

    public static final String DATA_ACTION = "date_action";

    private int mCurrentActivity;

    private Handler mHandler;

    private Timer timer;

    private GoogleApiClient mGoogleApiClient;

    private ActivityRecognitionDatabase db;

    public ActivityRecognitionOnceOffService() {
        super(TAG);

    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ActivityRecognitionOnceOffService(String name) {
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

            Log.d(TAG, "handleDetectedActivities: " + String.valueOf(activity.getType()));

            Intent intent = new Intent();
            intent.setAction(DATA_ACTION);

            switch(activity.getType()) {
                case DetectedActivity.WALKING:
                    if (activity.getConfidence() >= 75) {
                       // createActivityEntry(DetectedActivity.WALKING);
                        intent.putExtra("walking", "activity_detected");
                        sendBroadcast(intent);
                    }
                    break;
                case DetectedActivity.RUNNING:
                    if (activity.getConfidence() >= 75) {
                        //createActivityEntry(DetectedActivity.RUNNING);
                        intent.putExtra("running", "activity_detected");
                        sendBroadcast(intent);
                    }
                    break;
                case DetectedActivity.ON_BICYCLE:
                    if (activity.getConfidence() >= 75) {
                       // createActivityEntry(DetectedActivity.ON_BICYCLE);
                        intent.putExtra("on_bicycle", "activity_detected");
                        sendBroadcast(intent);
                    }
                    break;
                case DetectedActivity.STILL:
                    if (activity.getConfidence() >= 75) {
                        // createActivityEntry(DetectedActivity.ON_BICYCLE);
                        intent.putExtra("still", "activity_detected");
                        sendBroadcast(intent);
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
}
