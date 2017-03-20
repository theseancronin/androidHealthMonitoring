package com.android.shnellers.heartrate.voice_recorder;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 23/02/2017.
 */

public class DiaryMain extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recycler);
    }
}
