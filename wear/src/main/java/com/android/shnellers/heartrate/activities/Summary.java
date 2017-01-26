package com.android.shnellers.heartrate.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 24/01/2017.
 */

public class Summary extends Fragment {

    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_latest_layout, container, false);

        return mView;
    }
}
