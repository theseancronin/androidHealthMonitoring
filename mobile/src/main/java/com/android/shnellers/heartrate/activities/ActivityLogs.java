package com.android.shnellers.heartrate.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityRecognitionDatabase;
import com.android.shnellers.heartrate.models.ActivityStats;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TIME_MILLIS;

/**
 * Created by Sean on 03/04/2017.
 */

public class ActivityLogs extends Fragment implements DatePickerDialog.OnDateSetListener {


    @BindView(R.id.datePicker)
    protected Button mDatePicker;

    @BindView(R.id.activity_card_list)
    protected RecyclerView mRecyclerView;

    @BindView(R.id.no_records)
    protected TextView mNoRecords;

    private int year;
    private int month;
    private int day;

    private long mDateTime;

    private Calendar mCalendar;

    private ActivityRecognitionDatabase mActivityRecognitionDB;

    private ArrayList<ActivityStats> mActivityStats;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_logs, container, false);

        ButterKnife.bind(this, view);

        Calendar calendar = Calendar.getInstance();
        setDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        mActivityRecognitionDB = new ActivityRecognitionDatabase(getActivity());

        mActivityStats = mActivityRecognitionDB.getActivityStats(getQueryString());

        setRecordsText();

        setActivityLogs();

        return view;
    }

    private void setRecordsText() {
        if (mActivityStats.size() > 0) {
            mNoRecords.setVisibility(View.INVISIBLE);
        } else {
            mNoRecords.setVisibility(View.VISIBLE);
        }
    }

    private void setDate(final int day, final int month, final int year) {
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);

        setDateTime(mCalendar.getTimeInMillis());
    }

    /**
     *
     * @param timeInMillis
     */
    private void setDateTime(final long timeInMillis) {

        mDateTime = timeInMillis;

    }

    /**
     *
     * @return
     */
    private long getDateTime() {
        return mDateTime;
    }

    /**
     *
     * @return
     */
    private Calendar getCalendar() {
        return mCalendar;
    }

    /**
     *
     * @return
     */
    private String getQueryString() {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTimeInMillis(getDateTime());
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);

        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.DAY_OF_YEAR, 1);
        ;

        System.out.println("START: " + start.getTimeInMillis() + " END: " + end.getTimeInMillis());

        return "SELECT * FROM " + TABLE_RECOGNITION +
                " WHERE " + TIME_MILLIS + " >= " + start.getTimeInMillis() +
                " AND " + TIME_MILLIS + " <= " + end.getTimeInMillis() +
                " ORDER BY " + TIME_MILLIS + " ASC;";

    }

    private void setActivityLogs() {



        mRecyclerView.setHasFixedSize(true);

        ActivityCardRecyclerDate adapter = new ActivityCardRecyclerDate(mActivityStats, getActivity());
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);

    }


    /**
     * On click to display calendar.
     */
    @OnClick(R.id.datePicker)
    protected void dateSelection() {

        Bundle bundle = new Bundle();
        bundle.putInt("day", getCalendar().get(Calendar.DAY_OF_MONTH));
        bundle.putInt("month", getCalendar().get(Calendar.MONTH));
        bundle.putInt("year", getCalendar().get(Calendar.YEAR));

        CalendarFragment fragment = new CalendarFragment();
        fragment.setTargetFragment(this, 123);
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "dialogFragment");
    }

    /**
     * This is used to to get the date picked by the user.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            setDay(data.getIntExtra("day", -1));
            setMonth(data.getIntExtra("dayOfMonth", -1));
            setYear(data.getIntExtra("year", -1));

            setDate(data.getIntExtra("day", -1), data.getIntExtra("dayOfMonth", -1), data.getIntExtra("year", -1));

            updateActivityStats();
        }

    }

    private void updateActivityStats() {

        mActivityStats = mActivityRecognitionDB.getActivityStats(getQueryString());

        setActivityLogs();
    }

    @Override
    public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {
        System.out.println("YEAR: " + year);
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(final int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(final int day) {
        this.day = day;
    }
}
