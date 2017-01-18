package com.android.shnellers.heartrate;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Sean on 16/01/2017.
 */

public class ListenerService extends WearableListenerService {

    private static final String TAG = "MyDataMap...";
    // Start with forward slash
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: Outside if");
        // Compare the paths
        if (messageEvent.getPath().equals(WEARABLE_DATA_PATH)) {
            final String message = new String(messageEvent.getData());

            Intent startIntent = new Intent (this, WearMainActivity.class);
            startIntent.putExtra("message", message);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // cleat history
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "onMessageReceived: Main Activity Started");
        } else {

        }

        super.onMessageReceived(messageEvent);
    }
}
