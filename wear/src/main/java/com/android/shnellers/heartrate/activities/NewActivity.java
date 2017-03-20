package com.android.shnellers.heartrate.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 24/01/2017.
 */

public class NewActivity extends Fragment implements View.OnClickListener {

    public static final String WALKING = "Walking";
    public static final String RUNNING = "Running";
    public static final String CYCLING = "Cycling";
    private View mView;

    private RelativeLayout mWalkingLayout;
    private RelativeLayout mRunningLayout;
    private RelativeLayout mCyclingLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.new_activity_layout, container, false);

        mWalkingLayout = (RelativeLayout) mView.findViewById(R.id.layout_walking);
        mRunningLayout = (RelativeLayout) mView.findViewById(R.id.layout_running);
        mCyclingLayout = (RelativeLayout) mView.findViewById(R.id.layout_cycling);

        mWalkingLayout.setOnClickListener(this);
        mRunningLayout.setOnClickListener(this);
        mCyclingLayout.setOnClickListener(this);

        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_walking:
                startNewActivity(WALKING);
                break;
            case R.id.layout_running:
                startNewActivity(RUNNING);
                break;
            case R.id.layout_cycling:
                startNewActivity(CYCLING);
                break;
        }
    }

    private void startNewActivity(String activity) {

    }
}
