package com.android.shnellers.heartrate.heart_rate;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.Button;

import com.android.shnellers.heartrate.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 13/02/2017.
 */

public class UserAlertCheck extends WearableActivity {

    public static final String GENERAL = "General";
    public static final String ACTIVE = "Active";
    public static final String RESTING = "Resting";
    @BindView(R.id.btn_resting)
    Button mRestingBtn;

    @BindView(R.id.btn_active)
    Button mActiveBtn;

    @BindView(R.id.btn_general)
    Button mGeneralBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_alert);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_resting)
    public void isResting() {
        storeChoice(RESTING);
    }

    @OnClick(R.id.btn_active)
    public void isActive() {
        storeChoice(ACTIVE);
    }

    @OnClick(R.id.btn_general)
    public void isGeneral() {
        storeChoice(GENERAL);
    }

    private void storeChoice(String choice) {


    }

}
