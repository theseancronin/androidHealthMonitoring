package com.android.shnellers.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ActivitySummary extends AppCompatActivity {

    private static final String TYPE = "type";
    private static final String TIME = "time";
    private static final String DISTANCE = "distance";
    private static final String CALORIES = "calories";

    private TextView mType;
    private TextView mTime;
    private TextView mDistance;
    private TextView mCalories;

    private FloatingActionButton mFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mType = (TextView) findViewById(R.id.type);
        mTime = (TextView) findViewById(R.id.time);
        mDistance = (TextView) findViewById(R.id.distance);
        mCalories = (TextView) findViewById(R.id.calories);

        Intent intent = getIntent();

        String type = intent.getStringExtra(TYPE);
        String time = intent.getStringExtra(TIME);
        String distance = intent.getStringExtra(DISTANCE);
        String calories = intent.getStringExtra(CALORIES);

        setInfoText(mType, String.valueOf(type));
        setInfoText(mTime, String.valueOf(time));
        setInfoText(mDistance, String.valueOf(distance));
        setInfoText(mCalories, String.valueOf(calories));

        mFinish = (FloatingActionButton) findViewById(R.id.finish);
        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReturn = new Intent(ActivitySummary.this, MainActivity.class);
                startActivity(intentReturn);
            }
        });

    }

    private void setInfoText(TextView view, String value) {
        if (value != null) {
            view.setText(value);
        }
    }
}
