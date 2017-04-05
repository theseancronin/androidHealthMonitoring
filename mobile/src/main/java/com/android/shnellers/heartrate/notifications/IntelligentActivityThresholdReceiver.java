package com.android.shnellers.heartrate.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import static android.content.ContentValues.TAG;
import static com.android.shnellers.heartrate.Constants.Const.CYCLING;
import static com.android.shnellers.heartrate.Constants.Const.RUNNING;
import static com.android.shnellers.heartrate.Constants.Const.WALKING;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.MINUTES;

/**
 * Created by Sean on 27/03/2017.
 */

public class IntelligentActivityThresholdReceiver extends WakefulBroadcastReceiver {

    public static final String INTELLIGENT_ACTIVITY =
            "com.android.shnellers.heartrate.notifications.INTELLIGENT_ACTIVITY";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: BROADCAST RECEIVED");
        int activity = -1;
        if (intent.getIntExtra(WALKING, -1) != -1)  {
            activity = DetectedActivity.WALKING;
        } else if (intent.getIntExtra(RUNNING, -1) != -1) {
            activity = DetectedActivity.RUNNING;
        } else if (intent.getIntExtra(CYCLING, -1) != -1) {
            activity = DetectedActivity.ON_BICYCLE;
        }

        new IntelligentActivityThresholdTask(context, activity, intent.getIntExtra(MINUTES, -1)).execute();
    }
}
