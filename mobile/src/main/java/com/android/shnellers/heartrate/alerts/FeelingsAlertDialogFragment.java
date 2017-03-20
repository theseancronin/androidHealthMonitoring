package com.android.shnellers.heartrate.alerts;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 12/02/2017.
 */

public class FeelingsAlertDialogFragment extends DialogFragment {

    public static FeelingsAlertDialogFragment newInstance() {
        FeelingsAlertDialogFragment frag = new FeelingsAlertDialogFragment();
//        Bundle args = new Bundle();
//        args.putInt("title", "frag");
//        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_happy)
                .setTitle("How are you feeling today?")
                .setMessage("Message")
                .create();
    }
}
