package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.shnellers.heartrate.models.DateStepModel;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sean on 19/01/2017.
 */

public class StepsDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "StepsDBHelper";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "Steps.db";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + StepsDBContract.Entry.TABLE_NAME + "( " +
                    StepsDBContract.Entry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    StepsDBContract.Entry.STEPS_COUNT + " INTEGER, " +
                    StepsDBContract.Entry.CREATION_DATE + " TEXT);";

    public StepsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + StepsDBContract.Entry.TABLE_NAME);
            onCreate(db);
        }

    }

    public synchronized boolean createStepsEntry() {

        //Log.d(TAG, "createStepsEntry: ");
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;

        int currentDateStepsCount = 0;

        String todaysDate = getTodaysDate();

        String selectQuery = "SELECT " + StepsDBContract.Entry.STEPS_COUNT +
                " FROM " + StepsDBContract.Entry.TABLE_NAME +
                " WHERE " + StepsDBContract.Entry.CREATION_DATE + " = '" + todaysDate + "';";

        // This first try and catch checks to see if a record for todays steps
        // exists. If it does we increment the value for that date, otherwise
        // we insert a new entry into the table.
        try {

            // Get read data as we don't want to write to the DB
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor c = db.rawQuery(selectQuery, null);

            if (c.moveToFirst()) {
                do {
                   // Log.d(TAG, "createStepsEntry: ALREADY PRESENT");
                    isDateAlreadyPresent = true;

                    currentDateStepsCount = c.getInt(
                            c.getColumnIndex(StepsDBContract.Entry.STEPS_COUNT));
                } while (c.moveToNext());

                db.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            
            values.put(StepsDBContract.Entry.CREATION_DATE, todaysDate);

            if (isDateAlreadyPresent) {
             //   Log.d(TAG, "createStepsEntry: UPDATING");
                values.put(StepsDBContract.Entry.STEPS_COUNT, ++currentDateStepsCount);

                int row = db.update(StepsDBContract.Entry.TABLE_NAME, values,
                        StepsDBContract.Entry.CREATION_DATE + " = '" + todaysDate + "'", null);

                if (row == 1) {
                    createSuccessful = true;
                }

                db.close();
            } else {
               // Log.d(TAG, "createStepsEntry: NEW ENTRY");
                values.put(StepsDBContract.Entry.STEPS_COUNT, 1);
                long row = db.insert(StepsDBContract.Entry.TABLE_NAME, null, values);

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

            SQLiteDatabase db = getReadableDatabase();

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

    /**
     *
     * @return
     */
    public DateStepModel getTodaysStepDetails() {

       // Log.d(TAG, "getTodaysStepDetails: ");
        String selectQuery = "SELECT " + StepsDBContract.Entry.STEPS_COUNT +
                " FROM " + StepsDBContract.Entry.TABLE_NAME +
                " WHERE " + StepsDBContract.Entry.CREATION_DATE + " = '" + getTodaysDate() + "';";

        try {

            // Get read data as we don't want to write to the DB
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor c = db.rawQuery(selectQuery, null);

         //   Log.d(TAG, "getTodaysStepDetails: SIZE: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {

                int steps = c.getInt(c.getColumnIndex(StepsDBContract.Entry.STEPS_COUNT));
                Log.d(TAG, "getTodaysStepDetails: STEPS: " + String.valueOf(steps));

                DateStepModel model = new DateStepModel(
                        c.getInt(c.getColumnIndex(StepsDBContract.Entry.STEPS_COUNT)),
                        getTodaysDate());


                db.close();
                return model;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
