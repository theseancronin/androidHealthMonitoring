package com.android.shnellers.heartrate.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.android.shnellers.heartrate.PassportContract;
import com.android.shnellers.heartrate.R;

import static com.android.shnellers.heartrate.R.drawable.border_bottom;
import static com.android.shnellers.heartrate.R.drawable.border_bottom_2;


/**
 * Created by Sean on 24/10/2016.
 */

public class DetailFragment extends Fragment {

    private static final String TAG = "Detail.Fragment";

    public static final String KEY = "key";
    public static final String VALUE = "value";
    private static final String EDIT = "edit";
    public static final String IS_HEADING = "isHeading";
    public static final String ADD_MARGIN = "addMargin";


    private TextView keyTextView;
    private TextView valueTextView;

    private String key;
    private String value;


    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.passport_detail_fragment, container, false);

        keyTextView = (TextView) view.findViewById(R.id.keyTextView);
        valueTextView = (TextView) view.findViewById(R.id.valueTextView);

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

        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.passportChildFragment);

        if (!getArguments().getBoolean(IS_HEADING)) {

            layout.setBackgroundResource(border_bottom);

        } else {
            layout.setBackgroundResource(border_bottom_2);
            //layout.setBackgroundResource(R.drawable.text_view_heading);

        }

        if (getArguments().getBoolean(ADD_MARGIN)) {
           // setTopMargin(layout);
        }

        if (getArguments().getBoolean(PassportContract.Passport.CLICKABLE)) {
            setOnClickListener(getArguments().getString(PassportContract.Passport.ON_CLICK_METHOD));
        }

    }

    private void setOnClickListener(final String val) {

        if (getArguments().getBoolean(PassportContract.Passport.CLICKABLE)) {
            valueTextView.setOnClickListener(new View.OnClickListener() {

                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                Bundle args = new Bundle();



                @Override
                public void onClick(View v) {
                    System.out.println("S: " + val);
                    switch (val) {

                        case PassportContract.Passport.EDIT_BASIC_DETAILS:

                            Intent editBasicIntent = new Intent(getActivity(), EditPassportBasicDetails.class);
                            startActivity(editBasicIntent);

                            Log.i("Editing Details", "Worked!!");
                            break;
                        case PassportContract.Passport.EDIT_NEXT_OF_KIN:
                            Intent editKin = new Intent(getActivity(), EditPassportKin.class);
                            startActivity(editKin);
//
                            break;

                        case PassportContract.Passport.EDIT_GP_DETAILS:
                            Intent gpIntent = new Intent(getActivity(), EditPassportGP.class);
                            startActivity(gpIntent);

                            Log.i("Editing GP", "Worked!!");
                            break;

                        case PassportContract.Passport.EDIT_PHARMACY_DETAILS:
                            Intent pharmacy = new Intent(getActivity(), EditPharmacy.class);
                            startActivity(pharmacy);

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
