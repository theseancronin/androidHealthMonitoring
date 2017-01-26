package com.android.shnellers.heartrate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.shnellers.heartrate.ExtrasView;
import com.android.shnellers.heartrate.HeartReading;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDatabase;
import com.android.shnellers.heartrate.recyclers.BPMReadingsAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;
import java.util.Random;

/**
 * Created by Sean on 02/11/2016.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MyDataMap...";
    // Start with forward slash
    private static final String WEARABLE_DATA_PATH = "/wearable/data/path";

    private TextView latestHeartRate;

    private FloatingActionButton newReadingButton;

    private LineChart mLineChart;

    private LineDataSet mDataSet;

    private LinearLayout heartDisplay;

    private CountDownTimer mCountDownTimer;

    private long timeCountInMilliseconds;
    private long blinkInMilliseconds;

    private TextView mLatestHeartRate;

    private boolean blink;

    private boolean animationActive;

    private Random mRandom;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private View mView;

    private HeartRateDatabase mHeartRateDatabase;

    private ImageButton extrasBtn;

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_home_page, container, false);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_readings);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        timeCountInMilliseconds = 60 * 1000;
        blinkInMilliseconds     = 5 * 1000;

        animationActive = false;

        mHeartRateDatabase = new HeartRateDatabase(getActivity());

//

        Description desc = new Description();
        desc.setText("");

        displayHeartRateCards();

        Log.d(TAG, "onCreateView: ");
        return mView;
    }


    private void displayHeartRateCards() {

        List<HeartReading> readings = mHeartRateDatabase.getRecentReadings();

        if (readings.size() != 3) {
            for (int reading = readings.size(); reading < 3; reading++) {
                readings.add(new HeartReading(0, 0));
            }
        }

        RecyclerView rv = (RecyclerView) mView.findViewById(R.id.recycler_readings);
        rv.setHasFixedSize(true);

        BPMReadingsAdapter adapter = new BPMReadingsAdapter(readings);
        rv.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(manager);

        Log.d(TAG, "displayHeartRateCards: Readings " + Integer.toString(readings.size()));

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.heart_view:

                break;
            case R.id.activity_view:

                break;
            case R.id.dietary_view:

                break;
            case R.id.extras:
               Intent intent = new Intent(getActivity(), ExtrasView.class);
                startActivity(intent);
                Log.d(TAG, "onClick: Extras");
                break;
        }
    }

}
