package com.android.shnellers.heartrate.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.StepsDBHelper;
import com.android.shnellers.heartrate.models.DateStepModel;

/**
 * Created by Sean on 18/01/2017.
 */

public class ActivityHomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ActivityHomePage";

    private View mView;

    private ImageButton extrasBtn;

    private StepsDBHelper mDBHelper;

    private TextView mStepsView;
    private TextView mCaloriesView;
    private TextView mDistanceView;

    private CardView mLogsView;
    private CardView mNewActivity;
    private CardView mAnalysisView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");
        mView = inflater.inflate(R.layout.activities_layout_home, container, false);

//        extrasBtn = (ImageButton) mView.findViewById(R.id.extras);
//        extrasBtn.setOnClickListener(this);

        mDBHelper = new StepsDBHelper(getActivity());

        mStepsView = (TextView) mView.findViewById(R.id.steps_value);

        mLogsView = (CardView) mView.findViewById(R.id.logs_view);
        mNewActivity = (CardView) mView.findViewById(R.id.new_activity);
        mAnalysisView = (CardView) mView.findViewById(R.id.analysis);

        mLogsView.setOnClickListener(this);
        mNewActivity.setOnClickListener(this);
        mAnalysisView.setOnClickListener(this);

        setActivityDisplay();

        return mView;
    }

    private void setActivityDisplay() {

        Log.d(TAG, "setActivityDisplay: ");
        DateStepModel model = mDBHelper.getTodaysStepDetails();
        StringBuilder builder = new StringBuilder();
        String stepsStr = "";

        if (model != null) {
            stepsStr = String.valueOf(model.getStepCount()) + "/6000";

        } else {
            stepsStr = "0/6000";
        }

        mStepsView.setText(stepsStr);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.logs_view:
                viewActivityLogs();
                break;
            case R.id.new_activity:
                startNewActivity();
                break;
            case R.id.analysis:
                viewActivityAnalysis();
                break;
            case R.id.extras:

                break;
        }
    }

    private void viewActivityAnalysis() {

    }

    private void startNewActivity() {
        Intent intent = new Intent(getActivity(), ActivityTypeDialog.class);
        startActivity(intent);
    }

    private void viewActivityLogs() {

    }

    public class DatabaseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
