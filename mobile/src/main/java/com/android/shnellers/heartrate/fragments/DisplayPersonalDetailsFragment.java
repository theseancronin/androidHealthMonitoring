package com.android.shnellers.heartrate.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;


/**
 * Created by Sean on 04/11/2016.
 */

public class DisplayPersonalDetailsFragment extends Fragment {

    private TextView mNameView, mAgeView, mEmailView, mWeightView,
                    mConditionView, mLocationView, mPhoneNumberView;

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.display_personal_details, container, false);

        setVariables(view);

        return view;

    }

    /**
     * Set the various views for the fragment.
     */
    private void setVariables(final View view) {
//        mNameView = (TextView) view.findViewById(R.id.displayName);
//        mAgeView = (TextView) view.findViewById(R.id.displayDOB);
//        mEmailView = (TextView) view.findViewById(R.id.displayEmail);
//        mWeightView = (TextView) view.findViewById(R.id.displayWeight);
//        mConditionView = (TextView) view.findViewById(R.id.displayCondition);
//        mPhoneNumberView = (TextView) view.findViewById(R.id.displayPhoneNumber);
//        mLocationView = (TextView) view.findViewById(R.id.displayLocation);
    }

}
