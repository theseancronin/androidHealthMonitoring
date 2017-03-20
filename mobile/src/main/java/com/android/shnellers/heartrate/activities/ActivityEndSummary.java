package com.android.shnellers.heartrate.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.shnellers.heartrate.MainActivity;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityContract;
import com.android.shnellers.heartrate.database.ActivityDatabase;
import com.android.shnellers.heartrate.models.ActivityModel;

import java.util.HashMap;

/**
 * Created by Sean on 30/01/2017.
 */
public class ActivityEndSummary extends Activity implements View.OnClickListener{

    private TextView mTime;
    private TextView mDistance;
    private TextView mCalories;
    private TextView mType;
    private TextView mHeartRate;

    private ImageButton mImageButton;

    private HashMap<String, Integer> mTimeMap;
    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        mTime = (TextView) findViewById(R.id.time);
        mType = (TextView) findViewById(R.id.type);
        mCalories = (TextView) findViewById(R.id.calories);
        mDistance = (TextView) findViewById(R.id.distance);

        Intent intent = getIntent();

        if (intent != null) {
            mTimeMap = (HashMap<String, Integer>) intent.getSerializableExtra(
                            ActivityContract.ActivityEntries.TIME_MAP);
            setSummaryData(mTimeMap);
        }



        mImageButton = (ImageButton) findViewById(R.id.finish);
        mImageButton.setOnClickListener(this);

    }

    private void setSummaryData(HashMap<String, Integer> timeMap) {

        int totalMinutes = 0;
        double km = 0;

        ActivityDatabase db = new ActivityDatabase(this);
        ActivityModel model = db.getLastActivity();

        int hours = timeMap.get(ActivityContract.ActivityEntries.HOURS);
        int minutes = timeMap.get(ActivityContract.ActivityEntries.MINUTES);
        int seconds = timeMap.get(ActivityContract.ActivityEntries.SECONDS);

        String time = String.valueOf(hours) + " hr " +
                String.valueOf(minutes) + " min " +
                String.valueOf(seconds) + "sec";

        if (hours >= 1) {
            totalMinutes += hours * 60;
        }

        totalMinutes += minutes;
        if (totalMinutes >= 1) {
            int type = 0;

            if (model.getType().equals("Walking")) {
                type = 7;
            } else if (model.getType().equals("Running")) {
                type = 8;
            } else if (model.getType().equals("Cycling")) {
                type = 1;
            }

            //km = Calculations.convertTimeToKM(type, totalMinutes);

           // String distance = String.valueOf(km) + " km";
           // mDistance.setText(distance);
        }

        mType.setText(model.getType());
        mTime.setText(time);

    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.finish:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }
}
