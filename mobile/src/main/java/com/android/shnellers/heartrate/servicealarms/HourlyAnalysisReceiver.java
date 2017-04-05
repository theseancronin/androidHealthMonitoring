package com.android.shnellers.heartrate.servicealarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Sean on 01/04/2017.
 */

public class HourlyAnalysisReceiver extends BroadcastReceiver{

    private static final String TAG = "HourlyAnalysisReceiver";

    public static final String HOURLY_STATS_UPDATED = "com.android.shnellers.heartrate.servicealarms.HOURLY_ANALYSIS";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
    }
}
