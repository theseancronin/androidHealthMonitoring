package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.shnellers.heartrate.models.DateStepModel;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sean on 22/01/2017.
 */

public class ActivityRecognitionDatabase {

    private static final String TAG = "ActivityDatabase";

    private static int DB_VERSION = 6;

    private static final String DB_NAME = "HealthMonitor.db";



    private SQLiteDatabase db;

    private ActivityDBHelper mDBHelper;

    private final Context mContext;

    public ActivityRecognitionDatabase(Context context) {
        mContext = context;

        mDBHelper = new ActivityDBHelper(context, DB_VERSION,
                 ActivityContract.ActivityEntries.TABLE_RECOGNITION,
                DB_NAME);
    }

    private ActivityRecognitionDatabase open() {
        db = mDBHelper.getWritableDatabase();
        return this;
    }

    private void close() {
        db.close();
    }

    public void storeRecognizedActivity(final ContentValues values) {

    }

    public synchronized boolean createActivityEntry(final int activityType, final long time) {

        Log.d(TAG, "createActivityEntry: ");
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;

        int currentMinutes = 0;
        int milliseconds = 0;

        String todaysDate = getTodaysDate();
        open();
        String selectQuery = "SELECT " + ActivityContract.ActivityEntries.MINUTES_DETECTED +
                ", " + ActivityContract.ActivityEntries.MILLISECONDS +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + todaysDate + "' " +
                "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + activityType + ";";

        // This first try and catch checks to see if a record for todays steps
        // exists. If it does we increment the value for that date, otherwise
        // we insert a new entry into the table.
        try {

            // Get read data as we don't want to write to the DB
            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                    Log.d(TAG, "createActivityEntry: ALREADY PRESENT");
                    isDateAlreadyPresent = true;

                    currentMinutes = c.getInt(
                            c.getColumnIndex(ActivityContract.ActivityEntries.MINUTES_DETECTED));
                    milliseconds = c.getInt(
                            c.getColumnIndex(ActivityContract.ActivityEntries.MILLISECONDS));
                } while (c.moveToNext());

                db.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            SQLiteDatabase db = mDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, todaysDate);

            if (isDateAlreadyPresent) {
                Log.d(TAG, "createActivityEntry: UPDATING");

                long seconds = (time - milliseconds) / 1000;
                int row = 0;

                if (seconds >= 28000 && seconds <= 33000) {
                    values.put(ActivityContract.ActivityEntries.MINUTES_DETECTED, (currentMinutes + 30000));
                    values.put(ActivityContract.ActivityEntries.MILLISECONDS, time);
                    row = db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
                            ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + todaysDate + "' " +
                                    "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN +
                                    " = " + activityType + ";", null);


                } else {
                    values.put(ActivityContract.ActivityEntries.MILLISECONDS, time);
                    row = db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
                            ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + todaysDate + "' " +
                                    "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN +
                                    " = " + activityType + ";", null);
                }

                if (row == 1) {
                    createSuccessful = true;
                }

                db.close();
            } else {
                Log.d(TAG, "createActivityEntry: NEW ENTRY");
                values.put(ActivityContract.ActivityEntries.MINUTES_DETECTED, 30000);
                values.put(ActivityContract.ActivityEntries.TYPE_COLUMN, activityType);
                values.put(ActivityContract.ActivityEntries.MILLISECONDS, time);
                long row = db.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

                if (row != -1) {
                    createSuccessful = true;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return createSuccessful;
    }

    public ArrayList<DateStepModel> readStepsEntries() {
        ArrayList<DateStepModel> stepCountList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + StepsDBContract.Entry.TABLE_NAME;

        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {

                    DateStepModel model = new DateStepModel(
                            c.getInt(c.getColumnIndex(StepsDBContract.Entry.STEPS_COUNT)),
                            c.getString(c.getColumnIndex(StepsDBContract.Entry.CREATION_DATE)));

                    stepCountList.add(model);

                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stepCountList;
    }

    /**
     *
     * @return
     */
    private String getTodaysDate() {
        Calendar mCalendar = Calendar.getInstance();

        String todaysDate = String.valueOf(mCalendar.get(Calendar.MONTH)) + "/" +
                String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)) + "/" +
                String.valueOf(mCalendar.get(Calendar.YEAR));

        return todaysDate;
    }
}
