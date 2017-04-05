package com.android.shnellers.heartrate;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.shnellers.heartrate.activities.CalendarFragment;
import com.android.shnellers.heartrate.database.RemindersContract;
import com.android.shnellers.heartrate.database.RemindersDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Reminder extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener  {

    private static final String HEART_RATE_REMINDER = "heart_rate";
    private static final String MEDICATION_REMINDER = "Medication";
    public static final String HOUR_TIME = "hour_time";
    public static final String MINUTE_TIME = "minute_time";
    public static final String EVERYDAY = "everyday";

    private Button mSaveBtn;
    private Button mMonBtn, mTuesBtn, mWedBtn, mThursBtn, mFriBtn,
                    mSatBtn, mSunBtn;

    private boolean monBtnPressed, tuesBtnPressed, wedBtnPressed,
                    thursBtnPressed, friBtnPressed, satBtnPressed,
                    sunBtnPressed;

    private static final String SEPARATOR = "_,_";

    private AppCompatCheckBox mEverydayCheck;

    private TimePicker mTimePicker;

    private Calendar calendar;

    private List<ReminderTime> reminders;

    private RemindersDatabase db;

    private Map<String, String> daysMap;

    private ArrayList<String> selectedDays;

    private Spinner mSpinner;

    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    private String reminderTypeSelected;

    private Calendar mCalendar;

    private long mDateTime;

    @BindView(R.id.datePicker)
    protected Button mDatePicker;

    @BindView(R.id.date)
    protected TextView mCurrentDate;

    private int year;
    private int month;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        ButterKnife.bind(this);

        // Get the time picker
        mTimePicker = (TimePicker) findViewById(R.id.time_picker);

        // Initialize the buttons
        mSaveBtn = (Button) findViewById(R.id.save_btn);


        // Set the button listeners
        mSaveBtn.setOnClickListener(this);

        reminders = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        setDate(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        db = new RemindersDatabase(this);

        daysMap = new HashMap<>();

        selectedDays = new ArrayList<>();

        mSpinner = (Spinner) findViewById(R.id.reminder_type);
        mSpinner.setOnItemSelectedListener(this);

        mSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.reminder_types, android.R.layout.simple_spinner_item);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(mSpinnerAdapter);

        reminderTypeSelected = "";

    }

    private void setDate(final int day, final int month, final int year) {

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.UK);

        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);

        setDateTime(mCalendar.getTimeInMillis());

        mCurrentDate.setText(format.format(mCalendar.getTimeInMillis()));
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_btn:
                saveReminder();
                break;
//            case R.id.monday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    monBtnPressed = setButtonColor(mMonBtn, monBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.monday), monBtnPressed);
//                }
//                break;
//            case R.id.tuesday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    tuesBtnPressed = setButtonColor(mTuesBtn, tuesBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.tuesday), tuesBtnPressed);
//                }
//                break;
//            case R.id.wednesday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    wedBtnPressed = setButtonColor(mWedBtn, wedBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.wednesday), wedBtnPressed);
//                }
//                break;
//            case R.id.thursday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    thursBtnPressed = setButtonColor(mThursBtn, thursBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.thursday), thursBtnPressed);
//                }
//                break;
//            case R.id.friday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    friBtnPressed = setButtonColor(mFriBtn, friBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.friday), friBtnPressed);
//                }
//                break;
//            case R.id.saturday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    satBtnPressed = setButtonColor(mSatBtn, satBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.saturday), satBtnPressed);
//                }
//                break;
//            case R.id.sunday_btn:
//                if (!mEverydayCheck.isChecked()) {
//                    sunBtnPressed = setButtonColor(mSunBtn, sunBtnPressed);
//                    addOrRemoveDayFromSelected(getString(R.string.sunday), sunBtnPressed);
//                }
//                break;
        }
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
       // fragment.st
        //fragment.setTargetFragment(, 123);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "dialogFragment");
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

        }

    }

//    @Override
//    public void onDateSet(final DatePicker view, final int year, final int month, final int dayOfMonth) {
//        System.out.println("YEAR: " + year);
//    }

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

    private void addOrRemoveDayFromSelected(String dayStr, boolean pressed) {
        if (pressed) {
            selectedDays.add(dayStr);
        } else {
            selectedDays.remove(dayStr);
        }
    }

    private boolean setButtonColor(final Button btn, final boolean isPressed) {
        boolean pressed = false;
        if (isPressed) {
            pressed = false;
            Log.d("Button", "active");
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.activeButton));
        } else {
            Log.d("Button", "inactive");
            pressed = true;
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.inactiveButton));
        }

        return pressed;
    }

    /**
     * Set the reminder time
     */
    private void saveReminder() {

        int hour = 0, minute = 0;
        String days = "";

        // Must check min sdk version to determine whether we must use
        // old or new methods fro extracting hour and minute.
        if (Build.VERSION.SDK_INT >= 23) {
            hour = mTimePicker.getHour();
            minute = mTimePicker.getMinute();
        } else {
            hour = mTimePicker.getCurrentHour();
            minute = mTimePicker.getCurrentMinute();
        }

        if (mEverydayCheck.isEnabled()) {
            days = EVERYDAY;
        } else {
            days = convertArrayToString();
        }

        Log.d("Reminder", Integer.toString(hour));

        ContentValues values = new ContentValues();
        values.put(RemindersContract.Columns.HOUR_COLUMN, hour);
        values.put(RemindersContract.Columns.MINUTE_COLUMN, minute);
        values.put(RemindersContract.Columns.DAYS_COLUMN, days);
        values.put(RemindersContract.Columns.TYPE_COLUMN, reminderTypeSelected);
        values.put(RemindersContract.Columns.ACTIVE_COLUMN, RemindersContract.Columns.ALARM_ON);

        db.saveReminder(values);

        returnToReminders();

    }

    public String convertArrayToString() {
        String str = "";

        for (int i = 0; i < selectedDays.size(); i++) {
            str += selectedDays.get(i);
            if (i < selectedDays.size() - 1) {
                str += SEPARATOR;
            }
        }

        return str;
    }

    public ArrayList<String> convertStringToArrayList(final String str) {
        String[] arr = str.split(SEPARATOR);
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(arr));
        return arrayList;
    }

    private void returnToReminders() {
        Intent intent = new Intent(this, Reminders.class);
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
//            case R.id.everyday_check:
//                if (mEverydayCheck.isChecked()) {
//                    disableSelectDaysButtons();
//                } else {
//                    enableSelectDaysButtons();
//                }
//                break;
        }
    }

//    private void enableSelectDaysButtons() {
//        mSaveBtn.setEnabled(true);
//        mMonBtn.setEnabled(true);
//        mTuesBtn.setEnabled(true);
//        mWedBtn.setEnabled(true);
//        mThursBtn.setEnabled(true);
//        mFriBtn.setEnabled(true);
//        mSatBtn.setEnabled(true);
//        mSunBtn.setEnabled(true);
//    }
//
//    private void disableSelectDaysButtons() {
//        mSaveBtn.setEnabled(false);
//        mMonBtn.setEnabled(false);
//        mTuesBtn.setEnabled(false);
//        mWedBtn.setEnabled(false);
//        mThursBtn.setEnabled(false);
//        mFriBtn.setEnabled(false);
//        mSatBtn.setEnabled(false);
//        mSunBtn.setEnabled(false);
//    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        reminderTypeSelected = parent.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
