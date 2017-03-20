package com.android.shnellers.heartrate.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Sean on 27/02/2017.
 */

public class HeartDBReceiver extends BroadcastReceiver {

    private static final String TAG = "HeartReceiver";
    
    public static final String HEART_DB_CHANGED = "com.android.shnellers.heartrate.DB_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
    }
}
