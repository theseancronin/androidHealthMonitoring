package com.android.shnellers.heartrate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HeartRateActivityResult extends AppCompatActivity {

    private TextView mHeartRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_result);

        Intent intent = getIntent();
        int heartRate = intent.getIntExtra(SensorsContract.SensorValues.HEART_RATE_RESULT, 0);

        mHeartRate = (TextView) findViewById(R.id.heart_rate_result);

        mHeartRate.setText(Integer.toString(heartRate));
    }

}
