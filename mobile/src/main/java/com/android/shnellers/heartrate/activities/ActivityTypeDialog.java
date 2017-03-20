package com.android.shnellers.heartrate.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 22/01/2017.
 */

public class ActivityTypeDialog extends Activity implements View.OnClickListener{

    public static final String WALKING = "Walking";
    public static final String RUNNING = "Running";
    private static final String CYCLING = "Cycling";

    private CardView mWalking;
    private CardView mRunnig;
    private CardView mCycling;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_dialog);

        mWalking = (CardView) findViewById(R.id.walking_card);
        mRunnig = (CardView) findViewById(R.id.running_card);
        mCycling = (CardView) findViewById(R.id.cycling_card);

        mWalking.setOnClickListener(this);
        mRunnig.setOnClickListener(this);
        mCycling.setOnClickListener(this);


    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.walking_card:
                Intent walkingIntent = new Intent(this, ActivityStarted.class);
                walkingIntent.putExtra(WALKING, WALKING);
                startActivity(walkingIntent);
                finish();
                break;
            case R.id.running_card:
                Intent runningIntent = new Intent(this, ActivityStarted.class);
                runningIntent.putExtra(RUNNING, RUNNING);
                startActivity(runningIntent);
                finish();
                break;
            case R.id.cycling_card:
                Intent cyclingIntent = new Intent(this, ActivityStarted.class);
                cyclingIntent.putExtra(CYCLING, CYCLING);
                startActivity(cyclingIntent);
                finish();
                break;
        }

    }
}
