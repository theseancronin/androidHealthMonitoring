package com.android.shnellers.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class WearMainActivity extends WearableActivity implements View.OnClickListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;

    private Button mHeartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mHeartBtn = (Button) findViewById(R.id.heartBtn);
        mHeartBtn.setOnClickListener(this);

//        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
//        pager.setAdapter(new SensorFragmentPagerAdapter(getFragmentManager()));
//
//        DotsPageIndicator indicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
//        indicator.setPager(pager);

        String message = getIntent().getStringExtra("message");
        if (message == null || message.equalsIgnoreCase("")) {
            message = "Hello World";
        }

        mHeartBtn.setText(message);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.heartBtn:
                checkHeartRate();
                break;
        }
    }

    private void checkHeartRate() {
        Intent intent = new Intent(this, HeartRateActivity.class);
        startActivity(intent);
    }
}
