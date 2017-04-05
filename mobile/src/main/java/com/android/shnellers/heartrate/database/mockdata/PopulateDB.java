package com.android.shnellers.heartrate.database.mockdata;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.database.ActivityContract;
import com.android.shnellers.heartrate.database.ActivityRecognitionDBHelper;
import com.android.shnellers.heartrate.database.HeartRateContract;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static com.android.shnellers.heartrate.Constants.Const.CYCLING;
import static com.android.shnellers.heartrate.Constants.Const.RUNNING;
import static com.android.shnellers.heartrate.Constants.Const.STATUS;
import static com.android.shnellers.heartrate.Constants.Const.WALKING;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TYPE;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.DATE;

/**
 * Created by Sean on 24/03/2017.
 */

public class PopulateDB {

    private static final String TAG = "PopulateDB";

    public static void PopulateActivityRecognitionDB(ActivityRecognitionDBHelper helper, HeartRateDBHelper heartHelper) {

        Calendar sd = Calendar.getInstance();
        Calendar ed = Calendar.getInstance();

        sd.add(Calendar.DAY_OF_YEAR, -20);

        while (sd.getTimeInMillis() < ed.getTimeInMillis()) {

            for (int count = 0; count < 6; count++) {
                updateDatabase(helper, heartHelper, sd, count);
            }

            sd.add(Calendar.DAY_OF_YEAR, 1);
        }

        sd.add(Calendar.DAY_OF_YEAR, 1);

        for (int i = 0; i < 6; i++) {
            updateDatabase(helper, heartHelper, sd, i);
        }
    }

    private static void updateDatabase(ActivityRecognitionDBHelper helper, HeartRateDBHelper heartHelper, Calendar sd, int count) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat dFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.UK);

        int[] activities = {1, 7, 8};

        int minHour = 0;
        int maxHour = 0;

        if (count == 0) {
            minHour = 6;
            maxHour = 11;
        } else if (count == 1) {
            minHour = 12;
            maxHour = 16;
        } else {
            minHour = 17;
            maxHour = 21;
        }

        int minminutes = 5;
        int maxminutes = 50;

        int[] activityMinutes = {5, 10, 15, 20, 25, 30, 40, 50, 60};

        Random random = new Random();

        // randomly pick hour of day
        int rHour = random.nextInt(maxHour - minHour + 1) + minHour;
        // randomly pick the number of minutes the activity was performed for
        int minutes = random.nextInt(60 - 5) + 5;
        // Set the activity type
        int activityNumber = random.nextInt(activities.length);
        // Seconds the activity was performed for
        int seconds = minutes * 60;

        sd.set(Calendar.HOUR_OF_DAY, rHour);

        //Log.d(TAG, "populateDB: " + format.format(sd.getTimeInMillis()));
        String sqliteDate = format.format(sd.getTimeInMillis());
        String dDate = dFormat.format(sd.getTimeInMillis());

        long ml = sd.getTimeInMillis();

        addRecordToDB(sd, minutes, activities[activityNumber], seconds, helper, sqliteDate, dDate, ml);
        addHeartRateData(heartHelper, minutes, sd, activities[activityNumber], ml);
    }

    private static void addHeartRateData(HeartRateDBHelper heartHelper, int minutes, Calendar sd, int activityNumber, long ml) throws SQLiteException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        // open the database
        SQLiteDatabase db = heartHelper.getWritableDatabase();

        // min and max heart rate range
        int walkMin = 98;
        int walkMax = 140;

        int runMin = 131;
        int runMax = 185;

        long endTime = sd.getTimeInMillis() + (minutes * 60000);

        Random rand = new Random();

        if (minutes <= 10) {
            walkMax = 108;
        } else if (minutes <= 20) {
            walkMax = 120;
        } else {
            walkMax = 137;
        }

        while (sd.getTimeInMillis() < endTime ) {

            // get a random heart rate

            int heartRate;
            if (activityNumber == DetectedActivity.WALKING) {
                heartRate = rand.nextInt(walkMax - walkMin) + walkMin;
            } else {
                heartRate = rand.nextInt(runMax - runMin) + runMin;
            }

            String activity = getActivityType(activityNumber);

            //Log.d(TAG, "BPM: " + String.valueOf(heartRate) + " DATE: " + dateFormat.format(sd.getTimeInMillis())
             //           + " status: " + determineStatus(heartRate) + " Activity: " + activity);

            long mls = sd.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put(BPM_COLUMN, heartRate);
            values.put(DATE_TIME_COLUMN, sd.getTimeInMillis());
            values.put(STATUS, determineStatus(heartRate));
            values.put(TYPE, activity);
            values.put(DATE, dateFormat.format(sd.getTimeInMillis()));

            sd.add(Calendar.MINUTE, 1);
          //  Log.d(TAG, "DATE HR: " + dateFormat.format(sd.getTimeInMillis()));

            // Add the new record to the database
            db.insert(HeartRateContract.Entry.TABLE_NAME, null, values);

        }
        db.close();

    }

    private static String getActivityType(int type) {
        String typeStr = "";

        Log.d(TAG, "getActivityType: " + String.valueOf(type));
        if (type == DetectedActivity.WALKING) {
            typeStr = WALKING;
        } else if (type == DetectedActivity.RUNNING) {
            typeStr = RUNNING;
        } else if (type == DetectedActivity.ON_BICYCLE) {
            typeStr = CYCLING;
        }


        return typeStr;
    }

    private static String determineStatus(int heartRate) {

        String status = "OK";

        if (heartRate > 100) {
            status = "High";
        }else if (heartRate <= 40 || heartRate >= 120) {
            status = "Extreme";

        }

        return status;
    }

    private static void addRecordToDB(Calendar sd, int minutes,
                                      int activityNumber, int seconds, ActivityRecognitionDBHelper helper,
                                      String sqliteDate, String dDate, long ml) {

        // Create the format for both date and time to be entered into the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);

        SQLiteDatabase dbs = helper.getWritableDatabase();
        // Create a new content values and add the default settings
        // for a new DB entry.
        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntries.TYPE_COLUMN, activityNumber);
        values.put(ActivityContract.ActivityEntries.SECONDS, seconds);
        values.put(ActivityContract.ActivityEntries.MINUTES, minutes);
        values.put(ActivityContract.ActivityEntries.ACTIVITY_NUMBER, activityNumber);
        values.put(DATE, dateFormat.format(sd.getTimeInMillis()));
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, format.format(sd.getTimeInMillis()));
        values.put(ActivityContract.ActivityEntries.TIME_MILLIS, sd.getTimeInMillis());

        Log.d(TAG, "TYPE: " + String.valueOf(activityNumber) + " Minutes: " + String.valueOf(minutes) + " " + dateFormat.format(sd.getTimeInMillis()) + " : " + format.format(sd.getTimeInMillis()));

        long row = dbs.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        //if (row != -1) {
            //createSuccessful = true;
           // Log.d(TAG, "createNewEntry: INSERTED");
        //}

        dbs.close();
    }

}
