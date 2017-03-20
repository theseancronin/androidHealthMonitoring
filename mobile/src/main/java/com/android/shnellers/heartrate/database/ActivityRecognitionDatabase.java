package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.ActivityStats;
import com.android.shnellers.heartrate.models.DateStepModel;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Sean on 22/01/2017.
 */

public class ActivityRecognitionDatabase {

    private static final String TAG = "ActivityDatabase";

    private static final boolean DEBUG = true;
    public static final int INITIAL_ACTIVITY = 1;
    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int ACTIVE_FINISHED = 1;

    private static int DB_VERSION = 11;

    private static final String DB_NAME = "ActivityRecognition.db";

    private SQLiteDatabase db;

    private ActivityRecognitionDBHelper mDBHelper;

    private final Context mContext;

    public ActivityRecognitionDatabase(Context context) {
        mContext = context;

        mDBHelper = new ActivityRecognitionDBHelper(context, DB_VERSION,
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

    /**
     * Creates or updates a recognized activity.
     *
     * @param activityType
     * @param time
     * @return
     */
    public synchronized boolean createActivityEntry(final int activityType, final int time,
                                                    final long timeMilliseconds) {
        // booleans to check if data already present
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;

        int currentMinutes = 0;
        int seconds = 0;
        int id = 0;

        int active = 0;

        int activityNumber = 0;

        String todaysDate = getTodaysDate();
        open();

        // query to look for todays activities detected
        String selectQuery = "SELECT *" +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + todaysDate + "' " +
                "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + activityType +
                " ORDER BY id DESC LIMIT 1;";

        // This first try and catch checks to see if a record for todays steps
        // exists. If it does we increment the value for that date, otherwise
        // we insert a new entry into the table.
        try {

            // Get read data as we don't want to write to the DB
            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            Cursor c = db.rawQuery(selectQuery, null);

           // Log.d(TAG, "createActivityEntry: " + String.valueOf(c.getCount()));

            // move to the first record
            if (c.moveToFirst()) {

                if (DEBUG) Log.d(TAG, "createActivityEntry: ALREADY PRESENT");
                isDateAlreadyPresent = true;

                // get the minutes and seconds of the detected activity if it is already
                // present.
                currentMinutes = c.getInt(
                        c.getColumnIndex(ActivityContract.ActivityEntries.MINUTES_DETECTED));

                seconds = c.getInt(
                        c.getColumnIndex(ActivityContract.ActivityEntries.SECONDS));

                active = c.getInt(
                        c.getColumnIndex(ActivityContract.ActivityEntries.ACTIVE)
                );

                activityNumber = c.getInt(
                        c.getColumnIndex(ActivityContract.ActivityEntries.ACTIVITY_NUMBER)
                );

                id = c.getInt(
                        c.getColumnIndex(ActivityContract.ActivityEntries.ID_COLUMN)
                );

                c.close();
                db.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            SQLiteDatabase db = mDBHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, todaysDate);

            // If a record for today is already present then we update it
            if (isDateAlreadyPresent) {

                int row = 0;

                // This calculate the difference between the current time and the time of
                // the activity logged in the database.
                int secondsDifference = time - seconds;

                //Log.d(TAG, "createActivityEntry: DIFFERENCE " + String.valueOf(secondsDifference));

                // If the difference is greater than 300 seconds (5 minutes), then we assume the
                // user stopped the activity, so we stop the activity and create a new entry.
                if (secondsDifference >= 420) {

                    Log.d(TAG, "CLOSING ACTIVITY ENTRY AND RESTARTING");

                    // Set the active column to inactive and update the record
                    values.put(ActivityContract.ActivityEntries.ACTIVE, ACTIVE_FINISHED);
                    db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
                            ActivityContract.ActivityEntries.ID_COLUMN + "= " + id + ";", null);

                    activityNumber++;
                    createNewEntry(activityType, time, activityNumber, timeMilliseconds);

                } else if (secondsDifference >= 60) {
                    if (DEBUG) Log.d(TAG, "createActivityEntry: updating entry");

                    // If the found activity is inactive then we set it to active, as we then know
                    // the user is walking.
                    if (active == INACTIVE) {
                        active = ACTIVE;
                    }

                    currentMinutes += secondsDifference / 60;

                 //   Log.d(TAG, "CURRENT MINUTES: " + String.valueOf(currentMinutes));

                    if (currentMinutes % 2 == 0) {
                        notifyUserOfTimeDetected(currentMinutes);
                    }

                    values.put(ActivityContract.ActivityEntries.MINUTES_DETECTED, currentMinutes);
                    values.put(ActivityContract.ActivityEntries.SECONDS, time);
                    values.put(ActivityContract.ActivityEntries.ACTIVE, active);

                    row = db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
                            ActivityContract.ActivityEntries.ID_COLUMN + " = " + id + ";", null);

                }
// else {
//
//                    if (DEBUG) Log.d(TAG, "createActivityEntry: replacing: ");
//
//                    values.put(ActivityContract.ActivityEntries.SECONDS, time);
//                    row = db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
//                            ActivityContract.ActivityEntries.ID_COLUMN + " = " + id + ";", null);
//                }

                if (row == 1) {
                    createSuccessful = true;
                }


                db.close();
            } else {
                if (DEBUG) Log.d(TAG, "createActivityEntry: NEW ENTRY");
                createNewEntry(activityType, time, INITIAL_ACTIVITY, timeMilliseconds);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return createSuccessful;
    }

    /**
     * Sends a notification to the user of the number of minutes detected for an
     * activity.
     *
     * @param currentMinutes
     */
    private void notifyUserOfTimeDetected(int currentMinutes) {

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_running_white)
                .setContentTitle("Great Stuff!")
                .setContentText(String.valueOf(currentMinutes) + " minutes of walking detected");

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(mContext);
        // Issue the notification
        managerCompat.notify(1, builder.build());

    }

    /**
     * Creates a new entry into the table.
     *
     * @param activityType
     * @param time
     */
    private void createNewEntry(int activityType, int time, int activityNumber, long timeMilliseconds) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        // Create the format for both date and time to be entered into the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);


        // Create a new content values and add the default settings
        // for a new DB entry.
        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntries.TYPE_COLUMN, activityType);
        values.put(ActivityContract.ActivityEntries.SECONDS, time);
        values.put(ActivityContract.ActivityEntries.ACTIVE, INACTIVE);
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());
        values.put(ActivityContract.ActivityEntries.ACTIVITY_NUMBER, activityNumber);
        values.put(WeightDBContract.WeightEntries.DATE, dateFormat.format(timeMilliseconds));


        long row = db.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        if (row != -1) {
            //createSuccessful = true;
            Log.d(TAG, "createNewEntry: INSERTED");
        }

        db.close();
    }

    /**
     *
     * @return
     */
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

    public ArrayList<RecognizedActivity> getTodaysRecognizedActivities() {
        ArrayList<RecognizedActivity> list = new ArrayList<>();

        Log.d(TAG, "getTodaysRecognizedActivities: ");
        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            String walkingQuery = "SELECT *" +
                    " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN +
                    " = '" + getTodaysDate() + "';";

            String runningQuery = "SELECT *" +
                    " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN +
                    " = '" + getTodaysDate() + "' " +
                    "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + DetectedActivity.RUNNING + ";";

            String cyclingQuery = "SELECT *" +
                    " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN +
                    " = '" + getTodaysDate() + "' " +
                    "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + DetectedActivity.ON_BICYCLE + ";";

            Cursor c = db.rawQuery(walkingQuery, null);

            Log.d(TAG, "getTodaysRecognizedActivities: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {
                RecognizedActivity activity = getRecognizedActivity(c);
                list.add(activity);
            }

            db.close();


        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return list;
    }

    private RecognizedActivity getRecognizedActivity(Cursor c) {

        int seconds = 0;
        int hours = 0;
        int minutes = 0;
        int minutesRecorded = 0;

        int type = c.getInt(
                c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN));
        String date = c.getString(
                c.getColumnIndex(ActivityContract.ActivityEntries.DATE_TIME_COLUMN));

        do {
            minutesRecorded += c.getInt(c.getColumnIndex(
                    ActivityContract.ActivityEntries.MINUTES_DETECTED));
          //  Log.d(TAG, "getTodaysRecognizedActivities: TYPE: " + String.valueOf(type));


        } while (c.moveToNext());

       // Log.d(TAG, "getTodaysRecognizedActivities: SECONDS: " + String.valueOf(minutesRecorded));

        if (minutesRecorded >= 60) {
            hours = minutesRecorded / 60;

            minutes = minutesRecorded % 60;

        } else {
            hours = 0;
            minutes = minutesRecorded;
        }


        RecognizedActivity activity = new RecognizedActivity(hours, minutes, type, date);

        return activity;
    }

    public void printAllRecords() {

        if (DEBUG) Log.d(TAG, "printAllRecords: ");
        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            String query = "SELECT * " +
                    "FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION + ";";
            Cursor c = db.rawQuery(query, null);

          //  if (DEBUG) Log.d(TAG, "printAllRecords: Cursor Size: " + String.valueOf(c.getCount()));

            int typeColumn = c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN);
            int typeID = 0;

            String typeStr = "";

            if (c.moveToFirst()) {
                do {
                    typeID = c.getInt(typeColumn);
                    if (typeID == DetectedActivity.WALKING) {
                        typeStr = "Walking";
                    } else if (typeID == DetectedActivity.STILL) {
                        typeStr = "Still";
                    }

                    Log.d(TAG, typeStr);
                } while (c.moveToNext());


            } else {

            }

            db.close();


        } catch (SQLiteException e) {
            e.printStackTrace();
        }

    }

    public void populateWithMockData() throws SQLiteException {
        ContentValues walkValues = new ContentValues();
        ContentValues runValues = new ContentValues();
        ContentValues cycleValues = new ContentValues();

        String selectWalk = "SELECT * " +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + getTodaysDate() + "' " +
                "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + DetectedActivity.WALKING + ";";

        String selectRun = "SELECT * " +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + getTodaysDate() + "' " +
                "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + DetectedActivity.RUNNING + ";";

        String selectCycle = "SELECT * " +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " = '" + getTodaysDate() + "' " +
                "AND " + ActivityContract.ActivityEntries.TYPE_COLUMN + " = " + DetectedActivity.ON_BICYCLE + ";";

        walkValues.put(ActivityContract.ActivityEntries.TYPE_COLUMN, DetectedActivity.WALKING);
        runValues.put(ActivityContract.ActivityEntries.TYPE_COLUMN, DetectedActivity.RUNNING);
        cycleValues.put(ActivityContract.ActivityEntries.TYPE_COLUMN, DetectedActivity.ON_BICYCLE);

        walkValues.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());
        runValues.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());
        cycleValues.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());

        walkValues.put(ActivityContract.ActivityEntries.MINUTES, 33);
        walkValues.put(ActivityContract.ActivityEntries.SECONDS, 1980);

        runValues.put(ActivityContract.ActivityEntries.MINUTES, 20);
        runValues.put(ActivityContract.ActivityEntries.SECONDS, 1200);

        cycleValues.put(ActivityContract.ActivityEntries.MINUTES, 44);
        cycleValues.put(ActivityContract.ActivityEntries.SECONDS, 2640);

        SQLiteDatabase sdb = mDBHelper.getWritableDatabase();

        Cursor cw = sdb.rawQuery(selectWalk, null);
        Cursor cr = sdb.rawQuery(selectRun, null);
        Cursor cc = sdb.rawQuery(selectCycle, null);

        boolean exits = false;

        if (cw.moveToFirst()) {

        } else {
            sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, walkValues);
        }

        if (cw.moveToFirst()) {

        } else {
            sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, runValues);
        }

        if (cw.moveToFirst()) {

        } else {
            sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, cycleValues);
        }

        sdb.close();
    }

    public void populateRecognitionDB() throws SQLiteException {

        SQLiteDatabase sdb = mDBHelper.getWritableDatabase();

        ContentValues values;

        values = getContentValues(DetectedActivity.WALKING, 900, 900, "2/7/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 2500, 2500, "2/6/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.RUNNING, 2000, 2000, "2/6/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 1500, 1500, "2/5/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 1000, 1000, "2/4/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.RUNNING, 900, 900, "2/4/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 1200, 1200, "2/3/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 900, 900, "2/2/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.WALKING, 600, 600, "2/1/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        values = getContentValues(DetectedActivity.ON_BICYCLE, 3000, 3000, "2/1/2017");
        sdb.insert(ActivityContract.ActivityEntries.TABLE_RECOGNITION, null, values);

        sdb.close();

    }

    private ContentValues getContentValues(int type, int minutes, int seconds, String date) {

        ContentValues values = new ContentValues();
        values.put(ActivityContract.ActivityEntries.TYPE_COLUMN, type);
        values.put(ActivityContract.ActivityEntries.MINUTES, minutes);
        values.put(ActivityContract.ActivityEntries.SECONDS, seconds);
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, date);

        return values;

    }

    public ArrayList<RecognizedActivity> getLast7DaysRecords() {

        String query  = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + WeightDBContract.WeightEntries.DATE + " > " +
                " (SELECT DATETIME('now', '-7 day')) " +
                " ORDER BY " + ActivityContract.ActivityEntries.TYPE_COLUMN + ";";

//        String query = "select * from (select * from " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
//         " ORDER BY + " + ActivityContract.ActivityEntries.ID_COLUMN + " DESC LIMIT 10) ";

        ArrayList<RecognizedActivity> list = new ArrayList<>();


        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            Cursor c = db.rawQuery(query, null);

           // Log.d(TAG, "getTodaysRecognizedActivities: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {
                do {
                    int minutesInSeconds = c.getInt(c.getColumnIndex(
                            ActivityContract.ActivityEntries.MINUTES_DETECTED));

                    int seconds = 0;
                    int hours = 0;
                    int minutes = 0;
                    int type = c.getInt(
                            c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN));
                    String date = c.getString(
                            c.getColumnIndex(ActivityContract.ActivityEntries.DATE_TIME_COLUMN));

                   // Log.d(TAG, "getTodaysRecognizedActivities: TYPE: " + String.valueOf(type));
                   // Log.d(TAG, "getTodaysRecognizedActivities: SECONDS: " + String.valueOf(minutesInSeconds));
                    if (minutesInSeconds > 3600) {
                        hours = minutesInSeconds / 3600;

                        minutes = (minutesInSeconds % 3600) / 60;

                        seconds = ((minutesInSeconds % 3600) % 60);
                    } else if (minutesInSeconds > 60) {

                        minutes = minutesInSeconds / 60;
                        seconds = minutesInSeconds % 60;

                    } else {
                        seconds = minutesInSeconds;
                    }

                    list.add(new RecognizedActivity(hours, minutes, type, date));
                } while (c.moveToNext());
            }

            db.close();


        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return list;

    }

    /**
     * Retrieves the recognised activities between the given dates and returns a hash map
     * for the individual activity types, with a corresponding array of activity objects.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public HashMap<String, ArrayList<RecognizedActivity>> getRecordsBetweenDates(String startDate, String endDate) {

        HashMap<String, ArrayList<RecognizedActivity>> activities = new HashMap<>();

        ArrayList<RecognizedActivity> walkingList = new ArrayList<>();
        ArrayList<RecognizedActivity> runningList = new ArrayList<>();
        ArrayList<RecognizedActivity> bicycleList = new ArrayList<>();

        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        String q  = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " BETWEEN '" + startDate + "' AND  '" + endDate + "' ;";

        Cursor c = db.rawQuery(q, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                int type = c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN));
                RecognizedActivity activity = getRecognizedActivity(c);

                if (type == DetectedActivity.WALKING) {
                    walkingList.add(activity);
                } else if (type == DetectedActivity.RUNNING) {
                    runningList.add(activity);
                } else {
                    bicycleList.add(activity);
                }
            }
        }

        activities.put(Constants.Const.WALKING, walkingList);
        activities.put(Constants.Const.RUNNING, runningList);
        activities.put(Constants.Const.CYCLING, bicycleList);

        c.close();
        db.close();

        return activities;
    }

    public ArrayList<ActivityStats> getActivityStats(String today) {

        open();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();


        String q = getStatsQuesryString(today);

        Cursor c = db.rawQuery(q, null);

        int walkMins = 0;
        int runMins = 0;
        int cycleMins = 0;

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                int type = c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.ACTIVITY_NUMBER));
                int minutes = c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.MINUTES));

                if (type == DetectedActivity.WALKING) {
                    walkMins += minutes;
                } else if (type == DetectedActivity.RUNNING) {
                    runMins += minutes;
                } else if (type == DetectedActivity.ON_BICYCLE) {
                    cycleMins += minutes;
                }
            }
        }

        ArrayList<ActivityStats> activityStats = createActivityStatsList(
                walkMins, runMins, cycleMins,
                DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_BICYCLE
        );

        c.close();
        db.close();
        return activityStats;
    }

    private ArrayList<ActivityStats> createActivityStatsList(
            int walkMins, int runMins, int cycleMins,
            int walking, int running, int onBicycle) {

        ArrayList<ActivityStats> stats = new ArrayList<>();
        stats.add(new ActivityStats(Constants.Const.WALKING));
        stats.add(new ActivityStats(Constants.Const.RUNNING));
        stats.add(new ActivityStats(Constants.Const.CYCLING));

        for (ActivityStats activity : stats) {
            if (activity.getActivityName().equals(Constants.Const.WALKING)) {

                updateActivityStats(activity, walkMins, DetectedActivity.WALKING);

            } else if (activity.getActivityName().equals(Constants.Const.RUNNING)) {

                updateActivityStats(activity, runMins, DetectedActivity.RUNNING);

            } else {

                updateActivityStats(activity, cycleMins, DetectedActivity.ON_BICYCLE);

            }

        }

        Log.d(TAG, "WALK " + String.valueOf(stats.get(0).getMinutes()));

        return stats;
    }

    private void updateActivityStats(ActivityStats activity, int minutes, int type) {
        double distance = 0;
        int calories = 0;


        if (minutes > 0) {
            distance = Calculations.convertTimeToKM(type, minutes);
            calories = Calculations.caloriesBurnedMen(32, 70, 88, minutes);
        }

        activity.setAvgHeartRate(88);
        activity.setDistance(distance);
        activity.setCalories(calories);
        activity.setMinutes(minutes);
    }

    private String getStatsQuesryString(String date) {

        String query = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        Calendar c1 = Calendar.getInstance();


        if (date.equals(Constants.Const.TODAY)) {
            query = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + WeightDBContract.WeightEntries.DATE + " = '" + format.format(c1.getTime()) + "';";
        }

        return query;
    }
}
