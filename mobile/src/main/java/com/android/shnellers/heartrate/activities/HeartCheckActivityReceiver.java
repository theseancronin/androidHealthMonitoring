package com.android.shnellers.heartrate.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Sean on 26/02/2017.
 */

public class HeartCheckActivityReceiver extends BroadcastReceiver {

    private static final String TAG = "HeartCheckRECV";


    private int result;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        result = intent.getIntExtra("activityRecognized", -1);
    }

    public int getResult() {
        return result;
    }
}
