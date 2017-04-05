package com.android.shnellers.heartrate.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Sean on 03/04/2017.
 */

public class CalendarFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    // TODO: FIX default calendar date after selection
    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Bundle bundle = getArguments();

        if (bundle != null) {
            year = bundle.getInt("year", year);
            month = bundle.getInt("month", month);
            day = bundle.getInt("day", day);
        }

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {

        Log.d("CALENDAR", "onDateSet: ");
        Intent intent = new Intent();
        intent.putExtra("year", year);
        intent.putExtra("dayOfMonth", dayOfMonth);
        intent.putExtra("month", month);
        getTargetFragment().onActivityResult(getTargetRequestCode(), 123, intent);

    }
}
