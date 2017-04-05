package com.android.shnellers.heartrate.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.shnellers.heartrate.EditBasicDetails;
import com.android.shnellers.heartrate.PassportContract;
import com.android.shnellers.heartrate.R;


/**
 * Created by Sean on 21/11/2016.
 */

public class PassportInfoFragment extends Fragment {

    public static final String KEY = "key";
    public static final String VALUE = "value";
    private static final String EDIT = "edit";
    public static final String IS_HEADING = "isHeading";
    public static final String ADD_MARGIN = "addMargin";


    private TextView keyTextView;
    private TextView valueTextView;

    private String key;
    private String value;

    private Bundle bundle;

    private View view;

    public PassportInfoFragment () {
        bundle = getArguments();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.passport_detail_container, container, false);

        keyTextView = (TextView) view.findViewById(R.id.keyTextView);
        valueTextView = (TextView) view.findViewById(R.id.valueTextView);

        if (bundle != null) {
            valueTextView.setId(bundle.getInt("id"));
        }


        key = getArguments().getString(KEY);
        value = getArguments().getString(VALUE);

        setFragmentKey(key);
        setFragmentValue(value);

        return view;
    }

    /**
     * Set the key value for the display.
     *
     * @param key
     */
    public void setFragmentKey(final String key) {
        keyTextView.setText(key);
    }

    /**
     * Set the value for the piece of data.
     *
     * @param value
     */
    public void setFragmentValue (final String value) {
        valueTextView.setText(value);
    }

    @Override
    public void onStart() {
        super.onStart();

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.passportChildContainer);

        if (!getArguments().getBoolean(IS_HEADING)) {

            layout.setBackgroundResource(R.drawable.border_bottom);

        } else {

            layout.setBackgroundResource(R.drawable.text_view_heading);

        }

        if (getArguments().getBoolean(ADD_MARGIN)) {
            // setTopMargin(layout);
        }

        if (getArguments().getBoolean(PassportContract.Passport.CLICKABLE)) {
            setOnClickListener(getArguments().getString(PassportContract.Passport.ON_CLICK_METHOD));
        }

    }

    private void setOnClickListener(final String onClick) {

        if (getArguments().getBoolean(PassportContract.Passport.CLICKABLE)) {
            valueTextView.setOnClickListener(new View.OnClickListener() {

                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Bundle args = new Bundle();

                @Override
                public void onClick(View v) {
                    switch (onClick) {
                        case PassportContract.Passport.EDIT_BASIC_DETAILS:
                            Intent intent = new Intent(getActivity(), EditPassportBasicDetails.class);
                            getActivity().startActivity(intent);


//                            EditBasicDetails ebd = new EditBasicDetails();
//
//                            args.putString(
//                                    PassportContract.Passport.EDIT_DETAIL,
//                                    PassportContract.Passport.EDIT_BASIC_DETAILS
//                            );
//                            ebd.setArguments(args);
//
//                            ft.add(R.id.passportEditRootFragment, ebd);
//                            ft.addToBackStack(null);
//
//                            ft.commit();

                           // Intent intent = new Intent(getActivity(), EditBasicDetails.class);



                            Log.i("Editing Details", "Worked!!");
                            break;
                        case PassportContract.Passport.EDIT_NEXT_OF_KIN:
                            Intent basicIntent = new Intent(getActivity(), EditPassportKin.class);
                            getActivity().startActivity(basicIntent);
//                            EditBasicDetails kinDetails = new EditBasicDetails();
//
//                            args.putString(
//                                    PassportContract.Passport.EDIT_DETAIL,
//                                    PassportContract.Passport.EDIT_NEXT_OF_KIN);
//
//                            kinDetails.setArguments(args);
//
//                            ft.add(R.id.passportEditRootFragment, kinDetails);
//                            ft.addToBackStack(null);
//                            ft.commit();
//                            Log.i("Editing KIN", "Worked!!");
                            break;

                        case PassportContract.Passport.EDIT_GP_DETAILS:
                            EditBasicDetails gpDetails = new EditBasicDetails();

                            args.putString(
                                    PassportContract.Passport.EDIT_DETAIL,
                                    PassportContract.Passport.EDIT_GP_DETAILS
                            );

                            gpDetails.setArguments(args);

                            ft.add(R.id.passportEditRootFragment, gpDetails);
                            ft.addToBackStack(null);
                            ft.commit();

                            Log.i("Editing GP", "Worked!!");
                            break;

                        case PassportContract.Passport.EDIT_PHARMACY_DETAILS:
                            EditBasicDetails pharmacyDetails = new EditBasicDetails();

                            args.putString(
                                    PassportContract.Passport.EDIT_DETAIL,
                                    PassportContract.Passport.EDIT_PHARMACY_DETAILS
                            );

                            pharmacyDetails.setArguments(args);

                            ft.add(R.id.passportEditRootFragment, pharmacyDetails);
                            ft.addToBackStack(null);
                            ft.commit();

                            Log.i("Editing Pharmacy", "Worked!!");
                            break;
                    }
                }
            });
        }


    }



    /**
     * Adds a top margin to separate sibling details.
     *
     * @param layout
     */
    private void setTopMargin(final RelativeLayout layout) {
        // Initialize the parameters for the layout
        LinearLayout.LayoutParams params = getLinearLayoutParams();

        // This converts a DP value to pixels
        Resources r = view.getResources();

        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                25, // the DP value
                r.getDisplayMetrics()
        );

        // Set the margin values for the layout
        params.setMargins(0, px, 0, 0);

        // Set the parameters for the layout
        layout.setLayoutParams(params);
    }

    /**
     * Initializes the layout params so we can dynamically update
     * the layouts parameters.
     *
     * @return
     */
    private LinearLayout.LayoutParams getLinearLayoutParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Cancel edit.
     *
     * @param view
     */
    public void cancelEdit(View view) {
        getFragmentManager().popBackStack();
    }

}
