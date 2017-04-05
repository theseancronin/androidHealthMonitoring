package com.android.shnellers.heartrate.database;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.mockdata.PopulateDB;
import com.android.shnellers.heartrate.models.ActivityObject;
import com.android.shnellers.heartrate.models.ActivityStats;
import com.android.shnellers.heartrate.models.DateStepModel;
import com.android.shnellers.heartrate.models.RecognizedActivity;
import com.android.shnellers.heartrate.models.TimeModel;
import com.android.shnellers.heartrate.notifications.IntelligentActivityThresholdReceiver;
import com.google.android.gms.location.DetectedActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.android.shnellers.heartrate.Constants.Const.CYCLING;
import static com.android.shnellers.heartrate.Constants.Const.RUNNING;
import static com.android.shnellers.heartrate.Constants.Const.WALKING;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.MINUTES;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TIME_MILLIS;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TYPE_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;

/**
 * Created by Sean on 22/01/2017.
 */

public class ActivityRecognitionDatabase {

    public static final String WALK_SUM = "walkSum";
    private static final String TAG = "ActivityDatabase";

    private static final boolean DEBUG = true;
    public static final int INITIAL_ACTIVITY = 1;
    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int ACTIVE_FINISHED = 1;
    public static final String SUM = "Sum";
    public static final String COUNT = "Count";
    public static final String WALK_COUNT = "walkCount";
    public static final String CYCLE_SUM = "cycleSum";
    public static final String CYCLE_COUNT = "cycleCount";
    public static final String RUN_SUM = "runSum";
    public static final String RUN_COUNT = "runCount";
    public static final int DEFAULT_HEART_RATE = 0;


    private SQLiteDatabase db;

    private ActivityRecognitionDBHelper mDBHelper;

    private final Context mContext;

    private HeartRateDatabase mHeartDB;

    private HeartRateDBHelper mHeartHelper;



    public ActivityRecognitionDatabase(Context context) {
        mContext = context;

        mDBHelper = new ActivityRecognitionDBHelper(context);

        mHeartDB = new HeartRateDatabase(context);
        mHeartHelper = new HeartRateDBHelper(context);
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
                "AND " + TYPE_COLUMN + " = " + activityType +
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

                    //Log.d(TAG, "CLOSING ACTIVITY ENTRY AND RESTARTING");

                    // Set the active column to inactive and update the record
                    values.put(ActivityContract.ActivityEntries.ACTIVE, ACTIVE_FINISHED);
                    db.update(ActivityContract.ActivityEntries.TABLE_RECOGNITION, values,
                            ActivityContract.ActivityEntries.ID_COLUMN + "= " + id + ";", null);

                    activityNumber++;
                    createNewEntry(activityType, time, activityNumber, timeMilliseconds);

                } else if (secondsDifference >= 60) {
                    //if (DEBUG) Log.d(TAG, "createActivityEntry: updating entry");

                    // If the found activity is inactive then we set it to active, as we then know
                    // the user is walking.
                    if (active == INACTIVE) {
                        active = ACTIVE;
                    }

                    currentMinutes += secondsDifference / 60;

                 //   Log.d(TAG, "CURRENT MINUTES: " + String.valueOf(currentMinutes));

                    if (currentMinutes % 2 == 0) {
                        notifyUserOfTimeDetected(currentMinutes);
                    } else if (currentMinutes % 10 == 0) {
                        Intent intent = new Intent(mContext, IntelligentActivityThresholdReceiver.class);
                        intent.putExtra(WALKING, DetectedActivity.WALKING);
                        intent.putExtra(MINUTES, currentMinutes);

                        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);

                        try {
                            pi.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
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
        values.put(TYPE_COLUMN, activityType);
        values.put(ActivityContract.ActivityEntries.SECONDS, time);
        values.put(ActivityContract.ActivityEntries.ACTIVE, INACTIVE);
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, getTodaysDate());
        values.put(ActivityContract.ActivityEntries.ACTIVITY_NUMBER, activityNumber);
        values.put(TIME_MILLIS, timeMilliseconds);
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
                    "AND " + TYPE_COLUMN + " = " + DetectedActivity.RUNNING + ";";

            String cyclingQuery = "SELECT *" +
                    " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN +
                    " = '" + getTodaysDate() + "' " +
                    "AND " + TYPE_COLUMN + " = " + DetectedActivity.ON_BICYCLE + ";";

            Cursor c = db.rawQuery(walkingQuery, null);

            Log.d(TAG, "getTodaysRecognizedActivities: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {
                while (c.moveToNext()) {
                    list.add(getRecognizedActivity(c));
                }

            }

            db.close();


        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return list;
    }

    public HashMap<String, ArrayList<RecognizedActivity>> getTodaysActivities() {
        HashMap<String, ArrayList<RecognizedActivity>> map = createActivityHashMap();

        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            String activities = "SELECT *" +
                    " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                    " WHERE " + WeightDBContract.WeightEntries.DATE +
                    " > (SELECT DATETIME('now', '-7 day'));";

            Cursor c = db.rawQuery(activities, null);


            if (c.moveToFirst()) {
                while (c.moveToNext()) {
                    String type = getType(c.getInt(c.getColumnIndex(TYPE_COLUMN)));
                   // Log.d(TAG, "TYPE: " + type);
                    //Log.d(TAG, "TYPE: " + String.valueOf(c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN))));
                    if (type.equals(WALKING) || type.equals(RUNNING) || type.equals(CYCLING)) {
                        map.get(type).add(getRecognizedActivity(c));
                    }
                }
            }

            db.close();


        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return map;
    }

    public void activityHeartRateThresholdNotification() throws ParseException {
        HashMap<String, HashMap<Integer, TimeModel>> map = new HashMap<>();
        map.put(WALKING, new HashMap<Integer, TimeModel>());
        map.put(RUNNING, new HashMap<Integer, TimeModel>());
        map.put(CYCLING, new HashMap<Integer, TimeModel>());

        // Counts for the heart rates
        int wCount = 0;
        int rCOunt = 0;
        int cCOunt = 0;

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor c = db.rawQuery(previousSeven(), null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {

                // Get the type of activity
                String type = getType(c.getInt(c.getColumnIndex(TYPE_COLUMN)));

                // Get the minutes detected
                int minutes = c.getInt(c.getColumnIndex(MINUTES));
                long time = c.getInt(c.getColumnIndex(TIME_MILLIS));
                // Get the time model
                if (type.equals(WALKING) || type.endsWith(RUNNING) || type.equals(CYCLING)) {
                    TimeModel model = map.get(type).get(minutes);

                    // Get the values of the activity
                    HashMap<String, Integer> values = getSumOfHeartRatesForDuration(
                            minutes,
                            time,
                            c.getString(c.getColumnIndex(ActivityContract.ActivityEntries.DATE_TIME_COLUMN)));

                    // Update model or create a new one depending if exists
                    if (model != null)
                    {
                        model.setCount(model.getCount() + values.get(COUNT));
                        model.setSum(model.getSum() + values.get(SUM));
                    }
                    else
                    {
                        TimeModel timeModel = new TimeModel();
                        timeModel.setCount(values.get(COUNT));
                        timeModel.setSum(values.get(SUM));
                        timeModel.setMinute(minutes);
                    }

                    switch (type)
                    {
                        case WALKING:
                            wCount += values.get(COUNT);
                            break;
                        case RUNNING:
                            rCOunt += values.get(COUNT);
                            break;
                        case CYCLING:
                            cCOunt += values.get(COUNT);
                            break;
                    }

                }
                }

        }

        calculateAverages(map);

        c.close();
        db.close();

        //int walkAvg = map.get(WALKING).

    }

    /**
     * Go through the time models and calculate the averages;
     * @param map
     */
    private void calculateAverages(HashMap<String, HashMap<Integer, TimeModel>> map) {
        Log.d(TAG, "calculateAverages: ");
        for (Map.Entry<String, HashMap<Integer, TimeModel>> entry : map.entrySet()) {

            HashMap<Integer, TimeModel> timeModelHashMap = entry.getValue();

            for (Map.Entry<Integer, TimeModel> timeModelEntry : timeModelHashMap.entrySet()) {
                int sum = timeModelEntry.getValue().getSum();
                int count = timeModelEntry.getValue().getSum();
                timeModelEntry.getValue().setAvg(sum / count);

                Log.d(TAG, "Minutes: " + String.valueOf(timeModelEntry.getValue().getMinute()) +
                            " Average: " + String.valueOf(timeModelEntry.getValue().getAvg()));
            }
        }
    }

    /**
     * Retrieves all the hard rates that occurred within the given time.
     * @param minutes
     * @param time
     * @return
     */
    private HashMap<String, Integer> getSumOfHeartRatesForDuration(int minutes, long time, String dt) throws ParseException {

        HashMap<String, Integer> values = new HashMap<>();
        int sum = 0;
        int count = 0;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.UK);

        // Get three calendars
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        Calendar c3 = Calendar.getInstance();

        // Set min time
        c1.setTimeInMillis(time);

        // set max time
        c2.setTimeInMillis(time);
        c2.add(Calendar.MINUTE, minutes);
        Log.d(TAG, df.parse(dt).toString());

        SQLiteDatabase db = new HeartRateDBHelper(mContext).getReadableDatabase();

        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + HeartRateContract.Entry.DATE +
                    " > (SELECT DATETIME('now', '-20 day'))";

        Cursor c = db.rawQuery(query, null);

        Log.d(TAG, "getSumOfHeartRatesForDuration: " + format.format(time));
        if (c.moveToFirst())
        {
            while (c.moveToNext())
            {
                long hrt = c.getInt(c.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN));
                Log.d(TAG, "THIS FORMAT " +  format.format(hrt));
                if (hrt != -1) {
                    c3.setTimeInMillis(hrt);

                    Date date = c3.getTime();

                    if (date.after(c1.getTime()) && date.before(c2.getTime()))
                    {
                        sum += c.getInt(c.getColumnIndex(BPM_COLUMN));
                        count++;
                    }
                }


            }
        }

        c.close();
        db.close();

        values.put(SUM, sum);
        values.put(COUNT, count);

        return values;
    }

    private String previousSeven() {
        return "SELECT *" +
                " FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " > (SELECT DATETIME('now', '-7 day'));";
    }

    private String getType(int type) {

        String typeStr = "";

        if (type == DetectedActivity.WALKING) {
            typeStr = WALKING;
        } else if (type == DetectedActivity.RUNNING) {
            typeStr = RUNNING;
        } else if (type == DetectedActivity.ON_BICYCLE) {
            typeStr = CYCLING;
        }


        return typeStr;
    }

    private HashMap<String, ArrayList<RecognizedActivity>> createActivityHashMap() {

        HashMap<String, ArrayList<RecognizedActivity>> map = new HashMap<>();
        map.put(WALKING, new ArrayList<RecognizedActivity>());
        map.put(RUNNING, new ArrayList<RecognizedActivity>());
        map.put(CYCLING, new ArrayList<RecognizedActivity>());

        return map;
    }

    private RecognizedActivity getRecognizedActivity(Cursor c) {

        int seconds = 0;
        int hours = 0;
        int minutes = 0;
        int minutesRecorded = 0;

        int type = c.getInt(c.getColumnIndex(TYPE_COLUMN));
        String date = c.getString(c.getColumnIndex(ActivityContract.ActivityEntries.DATE_TIME_COLUMN));

        minutesRecorded = c.getInt(c.getColumnIndex(MINUTES));

//        do {
//            minutesRecorded += c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.MINUTES_DETECTED));
//
//        } while (c.moveToNext());


        Log.d(TAG, "minutes: " + String.valueOf(minutesRecorded) + " type: " + type + " date: " + date);

        if (minutesRecorded >= 60) {
            hours = minutesRecorded / 60;

            minutes = minutesRecorded % 60;

        } else {
            hours = 0;
            minutes = minutesRecorded;
        }


        return new RecognizedActivity(hours, minutes, type, date, c.getLong(c.getColumnIndex(TIME_MILLIS)));
    }

    public void printAllRecords() {

        if (DEBUG) Log.d(TAG, "printAllRecords: ");
        try {

            SQLiteDatabase db = mDBHelper.getReadableDatabase();

            String query = "SELECT * " +
                    "FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION + ";";
            Cursor c = db.rawQuery(query, null);

          //  if (DEBUG) Log.d(TAG, "printAllRecords: Cursor Size: " + String.valueOf(c.getCount()));

            int typeColumn = c.getColumnIndex(TYPE_COLUMN);
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

    private ContentValues getContentValues(int type, int minutes, int seconds, String date) {

        ContentValues values = new ContentValues();
        values.put(TYPE_COLUMN, type);
        values.put(MINUTES, minutes);
        values.put(ActivityContract.ActivityEntries.SECONDS, seconds);
        values.put(ActivityContract.ActivityEntries.DATE_TIME_COLUMN, date);

        return values;

    }

    public ArrayList<RecognizedActivity> getLast7DaysRecords() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        String query  = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
                " WHERE " + TIME_MILLIS + " >= " + calendar.getTimeInMillis() +
                " ORDER BY " + TYPE_COLUMN + ";";

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
                    int type = c.getInt(c.getColumnIndex(TYPE_COLUMN));
                    String date = c.getString(c.getColumnIndex(WeightDBContract.WeightEntries.DATE));

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

                    list.add(new RecognizedActivity(hours, minutes, type, date, c.getLong(c.getColumnIndex(TIME_MILLIS))));
                } while (c.moveToNext());
            }

            c.close();
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

        Log.d(TAG, "ACTIVITES: " + String.valueOf(c.getCount()));
        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                int type = c.getInt(c.getColumnIndex(TYPE_COLUMN));
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

        activities.put(WALKING, walkingList);
        activities.put(Constants.Const.RUNNING, runningList);
        activities.put(Constants.Const.CYCLING, bicycleList);

        c.close();
        db.close();

        return activities;
    }

    /**
     *
     * @return
     */
    public ArrayList<ActivityStats> getActivityStats(String query) {

        // Stats array
        ArrayList<ActivityStats> stats = new ArrayList<>();

        open();

        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // Get the query string

        // Retrieve the query objects
        Cursor c = db.rawQuery(query, null);

        int walkMins = 0;
        int runMins = 0;
        int cycleMins = 0;

        // A map that will keep track of the sums and counts for eah heart rate
        // so we can keep work out the average heart rates.
        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put("walkSum", 0);
                map.put("walkCount", 0);
                map.put("runSum", 0);
                map.put("runCount", 0);
                map.put("cycleSum", 0);
                map.put("cycleCount", 0);
                int type = c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.ACTIVITY_NUMBER));
                int minutes = c.getInt(c.getColumnIndex(MINUTES));
                long startTime = c.getLong(c.getColumnIndex(TIME_MILLIS));

                // Depending on the activity we update the heart rates for he duration and the number
                // or minutes.
                if (type == DetectedActivity.WALKING) {
                    System.out.println("MINUTES: " + minutes);
                   // getHeartRatesForActivity(map, WALKING, startTime, minutes * 60000);
                    stats.add(
                            new ActivityStats(
                                    minutes, 0, WALKING, Calculations.caloriesBurnedMen(32, 155, 88, minutes),
                                    getHeartRatesForActivity(map, WALKING, startTime, minutes * 60000),
                                    Calculations.convertTimeToKM(type, minutes), startTime
                            )
                    );
                    walkMins += minutes;
                } else if (type == DetectedActivity.RUNNING) {
                  //  getHeartRatesForActivity(map, RUNNING, startTime, minutes * 60000);
                    stats.add(
                            new ActivityStats(
                                    minutes, 0, RUNNING, Calculations.caloriesBurnedMen(32, 155, 88, minutes),
                                    getHeartRatesForActivity(map, RUNNING, startTime, minutes * 60000),
                                    Calculations.convertTimeToKM(type, minutes), startTime
                            )
                    );
                    runMins += minutes;
                } else if (type == DetectedActivity.ON_BICYCLE) {
                  //  getHeartRatesForActivity(map, CYCLING, startTime, minutes * 60000);
                    stats.add(
                            new ActivityStats(
                                    minutes, 0, CYCLING, Calculations.caloriesBurnedMen(32, 155, 88, minutes),
                                    getHeartRatesForActivity(map, CYCLING, startTime, minutes * 60000),
                                    Calculations.convertTimeToKM(type, minutes), startTime
                            )
                    );
                    cycleMins += minutes;
                }
            }
        }

//        ArrayList<ActivityStats> activityStats = createActivityStatsList(
//                walkMins, runMins, cycleMins,
//                DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_BICYCLE,
//
//        );

        c.close();
        db.close();
        return stats;
    }

    /**
     *
     * @param map
     * @param type
     * @param dateTime
     * @param mls
     * @return
     */
    private int getHeartRatesForActivity(final HashMap<String, Integer> map, final String type, final long dateTime, final int mls) {

        SQLiteDatabase db = mHeartHelper.getReadableDatabase();

        int count = 0;
        int sum = 0;

        long endTime = dateTime + mls;

        // String that will get all of the heart rates during an activity
        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                       " WHERE " + HeartRateContract.Entry.DATE_TIME_COLUMN + " >= " + dateTime +
                       " AND " + HeartRateContract.Entry.DATE_TIME_COLUMN + " <= " + endTime + ";";

        // Query the database
        Cursor c = db.rawQuery(query, null);

        System.out.println("HEART RATE FOUND: " + c.getCount());

        // If heart rates have been found, get the sum and count
        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                sum += c.getInt(c.getColumnIndex(BPM_COLUMN));
                System.out.println(" HR: " + c.getInt(c.getColumnIndex(BPM_COLUMN)));
                count++;
            }
        }

        System.out.println("SUM HR: " + sum);
        // Get and update the map contents
        if (type.equals(WALKING)) {
            map.put(WALK_SUM, map.get("walkSum") + sum);
            map.put(WALK_COUNT, map.get("walkCount") + count);
        } else if (type.equals(CYCLING)) {
            map.put(CYCLE_SUM, map.get("cycleSum") + sum);
            map.put(CYCLE_COUNT, map.get("cycleCount") + count);
        } else {
            Log.d(TAG, "getHeartRatesForActivity: RUNNNINg");
            map.put(RUN_SUM, map.get("runSum") + sum);
            map.put(RUN_COUNT, map.get("runCount") + count);
        }

        c.close();
        db.close();

        if (count > 0) {
            return sum / count;
        }

        return 0;

    }

//    /**
//     *
//     * @param walkMins
//     * @param runMins
//     * @param cycleMins
//     * @param walking
//     * @param running
//     * @param onBicycle
//     * @param map
//     * @return
//     */
//    private ArrayList<ActivityStats> createActivityStatsList(
//            int walkMins, int runMins, int cycleMins,
//            int walking, int running, int onBicycle, final HashMap<String, Integer> map) {
//
//        ArrayList<ActivityStats> stats = new ArrayList<>();
//        stats.add(new ActivityStats(WALKING));
//        stats.add(new ActivityStats(Constants.Const.RUNNING));
//        stats.add(new ActivityStats(Constants.Const.CYCLING));
//
//        for (ActivityStats activity : stats) {
//            if (activity.getActivityName().equals(WALKING)) {
//                updateActivityStats(activity, walkMins, DetectedActivity.WALKING, map, WALKING);
//
//            } else if (activity.getActivityName().equals(Constants.Const.RUNNING)) {
//
//                updateActivityStats(activity, runMins, DetectedActivity.RUNNING, map, RUNNING);
//
//            } else {
//
//                updateActivityStats(activity, cycleMins, DetectedActivity.ON_BICYCLE, map, CYCLING);
//
//            }
//
//        }
//
//        Log.d(TAG, "WALK " + String.valueOf(stats.get(0).getMinutes()));
//
//        return stats;
//    }

//    private void updateActivityStats(ActivityStats activity, int minutes, int type,
//                                     final HashMap<String, Integer> map, final String activityType) {
//        double distance = 0;
//        int calories = 0;
//
//
//        if (minutes > 0) {
//            distance = Calculations.convertTimeToKM(type, minutes);
//            calories = Calculations.caloriesBurnedMen(32, 70, 88, minutes);
//        }
//
//        activity.setDistance(distance);
//        activity.setCalories(calories);
//        activity.setMinutes(minutes);
//
//        // For each activity we set the average heart rate
//        if (activityType.equals(WALKING)) {
//            if (map.get(WALK_COUNT) > 0) {
//                activity.setAvgHeartRate(map.get(WALK_SUM) / map.get(WALK_COUNT));
//            } else {
//                activity.setAvgHeartRate(DEFAULT_HEART_RATE);
//            }
//        } else if (activityType.equals(RUNNING)) {
//            if (map.get(RUN_COUNT) > 0) {
//                activity.setAvgHeartRate(map.get(RUN_SUM) / map.get(RUN_COUNT));
//            } else {
//                activity.setAvgHeartRate(DEFAULT_HEART_RATE);
//            }
//        } else {
//            if (map.get(CYCLE_COUNT) > 0) {
//                activity.setAvgHeartRate(map.get(CYCLE_SUM) / map.get(CYCLE_COUNT));
//            } else {
//                activity.setAvgHeartRate(DEFAULT_HEART_RATE);
//            }
//        }
//    }

//    private String getStatsQuesryString(String date) {
//
//        String query = "";
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
//
//        Calendar c1 = Calendar.getInstance();
//        c1.add(Calendar.DAY_OF_YEAR, -7);
//
//        Calendar c2 = Calendar.getInstance();
//
//
//
//        query = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_RECOGNITION +
//                " WHERE " + TIME_MILLIS + " BETWEEN " + c1.getTimeInMillis() +
//                " AND " + c2.getTimeInMillis() + ";";
//
//
//        return query;
//    }

    public void populateDB() {

        PopulateDB.PopulateActivityRecognitionDB(mDBHelper, mHeartHelper);
    }

    /**
     * Remove all elements from the database;
     */
    public void clearDatabase() {
        open();
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        close();
    }

    public HashMap<String, ArrayList<ActivityObject>> getAllRecordsBetweenDates(long start) {
        open();
        HashMap<String, ArrayList<ActivityObject>> datesMap = new HashMap<>();

        // Get cursor object of all records
        Cursor c = getRecordsBetween(start);

        if (c.moveToFirst()) {
            do {

                String date = c.getString(c.getColumnIndex(HeartRateContract.Entry.DATE));

                if (!datesMap.containsKey(date)) {

                    Log.d(TAG, "KEY " + date);
                    datesMap.put(date, new ArrayList<ActivityObject>());
                }


                ArrayList<ActivityObject> heartRates = datesMap.get(date);
                heartRates.add(createActivityObject(c));

            } while (c.moveToNext());
        }

        c.close();
        close();

        return datesMap;

    }

    private ActivityObject createActivityObject(final Cursor c) {

        return new ActivityObject(
            c.getLong(c.getColumnIndex(TIME_MILLIS)),
            c.getLong(c.getColumnIndex(TIME_MILLIS)) + (c.getInt(c.getColumnIndex(MINUTES)) * 60000),
            c.getInt(c.getColumnIndex(MINUTES)),
            Calculations.getType(c.getInt(c.getColumnIndex(TYPE_COLUMN)))
        );

    }


    private Cursor getRecordsBetween(long start) {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // All records query
        String q  = "SELECT * FROM " + TABLE_RECOGNITION +
                " WHERE " + TIME_MILLIS + " >= " + start + " ORDER BY " + TIME_MILLIS + " ASC ;";

        // Perform the query
        return  db.rawQuery(q, null);

    }
}


