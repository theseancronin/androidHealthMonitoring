package com.android.shnellers.heartrate.heart_rate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 04/04/2017.
 */

public class HeartRateHome extends WearableActivity {

    private static final String TAG = "HeartRateHome";

    @BindView(R.id.check_heart_rate)
    protected TextView mCheckHeart;

    @BindView(R.id.view_stats)
    protected TextView mViewStats;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_home);

        ButterKnife.bind(this);

    }


    @OnClick(R.id.check_heart_rate)
    protected void checkHeartRate() {
        Intent intent = new Intent(this, HeartRateHome.class);
        startActivity(intent);
    }

    @OnClick(R.id.view_stats)
    protected void viewTodaysStats() {
        Intent intent = new Intent(this, LatestHeartReadings.class);
        startActivity(intent);
    }

}
