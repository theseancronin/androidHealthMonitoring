package com.android.shnellers.heartrate.heart_rate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sean on 12/02/2017.
 */

public class HeartRateServiceStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, HeartRateService.class);
        context.startService(service);
    }
}
