package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.HeartReading;
import com.android.shnellers.heartrate.models.HeartRateObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Sean on 15/01/2017.
 */

public class HeartRateDatabase {

    public static final String TAG = "HeartRateDatabase";

    private static final boolean DEBUG = false;
    public static final String HEART_RATE = "heart_rate";
    public static final String STATUS = "status";

    private SQLiteDatabase _db;

    private HeartRateDBHelper mHelper;

    private HeartReading mHeartReading;

    private final Context context;
    private HashMap<String, Integer> mMinAndMaxReadings;

    public HeartRateDatabase(Context context) {
        this.context = context;

        mHelper = new HeartRateDBHelper(context);
    }

    public HeartRateDatabase open () throws SQLException {
        _db = mHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        _db.close();
    }

    public SQLiteDatabase getDB() {
        return _db;
    }

    public void storeHeartRate(final ContentValues values) throws SQLException {
        open();
        _db.insert(HeartRateContract.Entry.TABLE_NAME, null, values);
        close();

        if (DEBUG) Log.d(TAG, "storeHeartRate: ");
    }

    public List<HeartReading> getRecentReadings() {
        open();

        Cursor readings = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " ORDER BY date(" + HeartRateContract.Entry.DATE_TIME_COLUMN + ") " +
                "LIMIT 3", null);

        if (readings == null) {
            close();
            return null;
        } else {
            List<HeartReading> rates = new ArrayList<>();
            int bpmIndex = readings.getColumnIndex(HeartRateContract.Entry.BPM_COLUMN);
            int dateTimeIndex = readings.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN);

            if (readings.moveToFirst() && readings.getCount() >= 1) {
                do {

                    if (DEBUG) Log.i("BPM", Integer.toString(readings.getInt(bpmIndex)));

                    rates.add(new HeartReading(
                            readings.getInt(bpmIndex),
                            readings.getInt(dateTimeIndex)));
                } while (readings.moveToNext());

            }

            close();
            return rates;
        }
    }

    /**
     * Get the average heart rate
     */
    public int averageHeartRate() {
        List<HeartReading> readings = getRecentReadings();

        if (!readings.isEmpty()) {
            int sum = 0;

            for (HeartReading reading : readings) {
                sum += reading.getBPM();
            }

            int avg = sum / readings.size();

            return avg;

        } else {
            return 0;
        }


    }

    /**
     * Retrieve the last inserted record from the database.
     *
     * @return
     */
    public HeartRateObject getLatestHeartRate() {

        open();

       HeartRateObject heartRate = null;

        // Query to get the last entered record from the database
        Cursor readings = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MAX(id) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        if (readings.moveToFirst()) {
            heartRate = new HeartRateObject(
                    readings.getInt(readings.getColumnIndex("id")),
                    readings.getInt(readings.getColumnIndex("bpm")),
                    readings.getLong(readings.getColumnIndex("date_time")),
                    readings.getString(readings.getColumnIndex("status")),
                    readings.getString(readings.getColumnIndex("date")),
                    readings.getString(readings.getColumnIndex(HeartRateContract.Entry.TYPE))
            );

        }

        readings.close();
        close();

        return heartRate;
    }

    public ArrayList<HeartRateObject> get7DayReadings() throws SQLiteException {
        open();

        ArrayList<HeartRateObject> readings = new ArrayList<>();


        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE + " > " +
                " (SELECT DATETIME('now', '-7 day'));";

        Cursor c = _db.rawQuery(q, null);

        if (c.moveToFirst()) {
            do {

                readings.add(
                    new HeartRateObject(
                            c.getInt(c.getColumnIndex("id")),
                            c.getInt(c.getColumnIndex("bpm")),
                            c.getLong(c.getColumnIndex("date_time")),
                            c.getString(c.getColumnIndex("status")),
                            c.getString(c.getColumnIndex("date")),
                            c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE))
                    )
                );

            } while (c.moveToNext());
        }

        c.close();
        close();

        return readings;
    }

    public ArrayList<HeartRateObject> getRecords(
            String start, String end) {

        open();

        ArrayList<HeartRateObject> heartRates = new ArrayList<>();

        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " BETWEEN '" + start + "' AND  '" + end + "' ;";

        Cursor c = _db.rawQuery(q, null);

        if (c.moveToFirst()) {
            do {

                heartRates.add(createHeartRateObject(c));

            } while (c.moveToNext());
        }

        c.close();
        close();

        return heartRates;
    }

    public HashMap<String, ArrayList<HeartRateObject>> getRecordsBetweenDates(
            String start, String end, String activityToSelect) {

        open();

        HashMap<String, ArrayList<HeartRateObject>> heartRates = getActivityHeartObjectMap();

        String q  = "SELECT " + activityToSelect + " FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " BETWEEN '" + start + "' AND  '" + end + "' ;";

        Cursor c = _db.rawQuery(q, null);

        if (c.moveToFirst()) {
            do {

                String type = c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE));

                if (type != null) {
                    switch (type) {
                        case Constants.Const.CYCLING:
                            heartRates.get(Constants.Const.CYCLING).add(createHeartRateObject(c));
                            break;
                        case Constants.Const.WALKING:
                            heartRates.get(Constants.Const.WALKING).add(createHeartRateObject(c));
                            break;
                        case Constants.Const.RUNNING:
                            heartRates.get(Constants.Const.RUNNING).add(createHeartRateObject(c));
                            break;
                        case Constants.Const.RESTING:
                            heartRates.get(Constants.Const.RESTING).add(createHeartRateObject(c));
                            break;
                        case Constants.Const.GENERAL:
                            heartRates.get(Constants.Const.GENERAL).add(createHeartRateObject(c));
                            break;
                    }
                }


            } while (c.moveToNext());
        }

        c.close();
        close();

        return heartRates;
    }

    /**
     * Populates the database with mock data.
     *
     */
    public void populateDB() {



        open();

        int startHour = 0;
        int endHour = 23;

        int count = 50;

        Calendar c = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_YEAR,1);

        c.add(Calendar.DAY_OF_MONTH, -count);

        Date date = new Date();

        while (c.getTime().before(endDate.getTime())) {

            c.set(Calendar.HOUR_OF_DAY, startHour);

            int hourOfDay = c.get(Calendar.HOUR_OF_DAY);

            while (hourOfDay != endHour) {

                addRecordToDB(c);

                hourOfDay++;

                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        c.add(Calendar.DAY_OF_MONTH, 1);

        for (int i = 0; i <= 3; i++) {
            addRecordToDB(c);
        }

        close();

    }

    private void addRecordToDB(Calendar c) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        int min = 100;
        int max = 135;

        ArrayList<Integer> hours = new ArrayList<>(Arrays.asList(9, 13, 14, 18, 21));
        ArrayList<Integer> minutes = new ArrayList<>(Arrays.asList(0, 10, 15, 30, 45));

        Random random = new Random();

        int heartRate = random.nextInt(max - min + 1) + min;
        int hour = random.nextInt(hours.size());
        int minute = random.nextInt(minutes.size());

        //c.set(Calendar.HOUR_OF_DAY, hours.get(hour));
        //c.set(Calendar.MINUTE, minutes.get(minute));

        ContentValues values = new ContentValues();
        values.put(HeartRateContract.Entry.BPM_COLUMN, heartRate);
        values.put(HeartRateContract.Entry.DATE_TIME_COLUMN, c.getTimeInMillis());
        values.put(Constants.Const.STATUS, "OK");
        values.put(WeightDBContract.WeightEntries.DATE, format.format(c.getTimeInMillis()));
        values.put(HeartRateContract.Entry.TYPE,  (c.get(Calendar.HOUR_OF_DAY)));

        _db.insert(HeartRateContract.Entry.TABLE_NAME, null, values);
    }

    /**
     * Get the string value of an activity.
     *
     * @return
     * @param hour, the hour of day
     */
    private String getActivityType(int hour) {

        String str = "";

        Random random = new Random();

        int type = random.nextInt(4 - 1 + 1) + 1;

        if ((hour >= 0 && hour <= 9) || hour > 21) {
            str = Constants.Const.RESTING;
            return str;
        }

        switch (type) {
            case 1:
                str = Constants.Const.WALKING;
                break;
            case 2:
                str = Constants.Const.RUNNING;
                break;
            case 3:
                str = Constants.Const.CYCLING;
                break;
            case 4:
                str = Constants.Const.RESTING;
                break;
        }

        if (str.isEmpty()) {
            str = "General";
        }

        return str;
    }

    /**
     * Stores the activity of the most recent heart rate recording.
     *
     * @param activity
     */
    public void storeHeartRateActivityType(final String activity) {

        open();

        Cursor c = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MAX(id) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        if (c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndex(HeartRateContract.Entry.ID_COLUMN));

            ContentValues values = new ContentValues();
            values.put(HeartRateContract.Entry.TYPE, activity);

            long row = _db.update(HeartRateContract.Entry.TABLE_NAME, values,
                    ActivityContract.ActivityEntries.ID_COLUMN + " = " + id + ";", null);

            if (row != -1) {
                Log.d(TAG, "storeHeartRateActivityType: UPDATED");

                context.sendBroadcast(new Intent(HeartDBReceiver.HEART_DB_CHANGED));
            }
            
        }

        c.close();
        close();

    }

    public HeartRateObject getDBEntry(int bpm, String date) {

        HeartRateObject heartRateObject = null;

        open();

        Log.d(TAG, "getDBEntry: " + date);

        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + HeartRateContract.Entry.BPM_COLUMN + " = " + bpm +
                " AND " + WeightDBContract.WeightEntries.DATE + "= '" + date + "';";

        Cursor c = _db.rawQuery(query, null);

        Log.d(TAG, "getDBEntry: " + String.valueOf(c.getCount()));

        if (c.moveToNext()) {
            heartRateObject = createHeartRateObject(c);
        }

        c.close();
        close();

        return heartRateObject;
    }

    private HeartRateObject createHeartRateObject(Cursor c) {



        return new HeartRateObject(
                c.getInt(c.getColumnIndex("id")),
                c.getInt(c.getColumnIndex("bpm")),
                c.getLong(c.getColumnIndex("date_time")),
                c.getString(c.getColumnIndex("status")),
                c.getString(c.getColumnIndex("date")),
                c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE))
        );
    }

    /**
     * Remove all elements from the database;
     */
    public void clearDatabase() {
        open();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        close();
    }

    /**
     * Gets the minimum and maximum heart rates recorded.
     *
     * @return
     */
    public HashMap<String, HeartRateObject> getMinAndMaxReadings() {

        HashMap<String, HeartRateObject> minMax = new HashMap<>();

        open();

        Cursor max = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MAX(bpm) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        Cursor min = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MIN(bpm) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        if (min.moveToFirst()) {
            minMax.put("Min", createHeartRateObject(min));
        }

        if (max.moveToFirst()) {
            minMax.put("Max", createHeartRateObject(max));
        }

        max.close();
        min.close();
        close();

        return minMax;
    }

    public HashMap<String, Date> getDaysOfAllReadings() {

        HashMap<String, Date> dates = new HashMap<>();

        open();

        Cursor startDate = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MAX(id) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        Cursor endDate = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                        " WHERE id = ( SELECT MIN(id) FROM " +
                        HeartRateContract.Entry.TABLE_NAME + ");", null);

        if (startDate.moveToFirst()) {
            dates.put("startDate", new Date(startDate.getLong(
                    startDate.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN))));
        }

        if (endDate.moveToFirst()) {
            dates.put("endDate", new Date(endDate.getLong(
                    endDate.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN))));
        }

        startDate.close();
        endDate.close();
        close();

        return dates;
    }

    /**
     * Returns the table size.
     *
     * @return
     */
    public int getDBSize() {

        int size = 0;

        open();

        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME + ";";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            size = c.getCount();
        }
        c.close();
        close();

        return size;
    }

    /**
     * Returns the table size.
     *
     * @return
     */
    public HashMap<String, Integer> getRestingData() {

        int size = 0;

        HashMap<String, Integer> restingDetails;

        open();

        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME + " WHERE " +
                HeartRateContract.Entry.TYPE + " = " + Constants.Const.RESTING + ";";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            size = c.getCount();
        }
        c.close();
        close();

        return size;
    }

    /**
     * Get the average heart rate detected of all heart rates.
     *
     * @return
     */
    public int getAverageHeartRate() {

        int average = 0;
        int total = 0;

        open();

        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME + ";";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                total += c.getInt(c.getColumnIndex(HeartRateContract.Entry.BPM_COLUMN));
            }
            average = total / c.getCount();
        }
        c.close();
        close();

        return average;
    }

    /**
     * Returns the table size.
     *
     * @return
     */
    public HeartRateObject getMaxHeartRate() {

        HeartRateObject maxHeartRate = null;

        open();

        String query = "SELECT *" +
                " FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + HeartRateContract.Entry.BPM_COLUMN + " = " +
                " (SELECT MAX(" + HeartRateContract.Entry.BPM_COLUMN + ") " +
                " FROM " + HeartRateContract.Entry.TABLE_NAME + ");";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            maxHeartRate = createHeartRateObject(c);
        }
        c.close();
        close();

        return maxHeartRate;
    }

    /**
     * Returns the table size.
     *
     * @return
     */
    public HeartRateObject getMinHeartRate() {

        HeartRateObject minHeartRate = null;

        open();

        String query = "SELECT *" +
                " FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + HeartRateContract.Entry.BPM_COLUMN + " = " +
                " (SELECT MIN(" + HeartRateContract.Entry.BPM_COLUMN + ") " +
                " FROM " + HeartRateContract.Entry.TABLE_NAME + ");";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            minHeartRate = createHeartRateObject(c);
        }
        c.close();
        close();

        return minHeartRate;
    }

    /**
     * Returns all hash map of all heart rates and their corresponding activity.
     *
     * @return
     */
    public HashMap<String, ArrayList<HeartRateObject>> getAllRecords() {
        open();

        // Setup a hash map and fill with activities as keys and list of heart rate objects.

        HashMap<String, ArrayList<HeartRateObject>> heartRates = getActivityHeartObjectMap();

        String query = "SELECT *" +
                " FROM " + HeartRateContract.Entry.TABLE_NAME + ";";

        Cursor c = _db.rawQuery(query, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                String type = c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE));

                // Depending on the type, add the recording to the list of that activity
                switch (type) {
                    case Constants.Const.CYCLING:
                        heartRates.get(Constants.Const.CYCLING).add(createHeartRateObject(c));
                        break;
                    case Constants.Const.WALKING:
                        heartRates.get(Constants.Const.WALKING).add(createHeartRateObject(c));
                        break;
                    case Constants.Const.RUNNING:
                        heartRates.get(Constants.Const.RUNNING).add(createHeartRateObject(c));
                        break;
                    case Constants.Const.RESTING:
                        heartRates.get(Constants.Const.RESTING).add(createHeartRateObject(c));
                        break;
                    case Constants.Const.GENERAL:
                        heartRates.get(Constants.Const.GENERAL).add(createHeartRateObject(c));
                        break;
                }
            }
        }

        c.close();
        close();

        return heartRates;
    }

    /**
     * Get the date of the first record.
     *
     * @return
     */
    public String getFirstRecordDate() {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        open();

        Cursor c = _db.rawQuery("SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME + ";", null);

        if (c.moveToFirst()) {
            date = format.format(c.getLong(c.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN)));
        }

        c.close();
        close();

        return date;
    }

    /**
     * Get the date of the last record.
     *
     * @return
     */
    public String getLastRecordDate() {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        open();

        Cursor c = _db.rawQuery("SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + HeartRateContract.Entry.ID_COLUMN + " = " +
                " (SELECT MAX(id) FROM " + HeartRateContract.Entry.TABLE_NAME + ");", null);

        if (c.moveToFirst()) {
            date = format.format(c.getLong(c.getColumnIndex(HeartRateContract.Entry.DATE_TIME_COLUMN)));
        }

        c.close();
        close();

        return date;
    }

    /**
     * Retrieves all records between the given dates in an array list.
     *
     * @param start
     * @param end
     * @return
     */
    public HashMap<String, ArrayList<HeartRateObject>> getAllRecordsBetweenDates(String start, String end) {
        open();
        HashMap<String, ArrayList<HeartRateObject>> datesMap = new HashMap<>();

        // Get cursor object of all records
        Cursor c = getRecordsBetween(start, end);

        if (c.moveToFirst()) {
            do {

                String date = c.getString(c.getColumnIndex(HeartRateContract.Entry.DATE));

                if (!datesMap.containsKey(date)) {

                    Log.d(TAG, "KEY " + date);
                    datesMap.put(date, new ArrayList<HeartRateObject>());
                }


                ArrayList<HeartRateObject> heartRates = datesMap.get(date);
                heartRates.add(createHeartRateObject(c));

            } while (c.moveToNext());
        }

        c.close();
        close();

        return datesMap;

    }

    /**
     * Returns cursor data structure that contains all records between the given
     * dates.
     *
     * @param start
     * @param end
     * @return
     */
    private Cursor getRecordsBetween(String start, String end) {

        // All records query
        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " BETWEEN '" + start + "' AND  '" + end + "' ;";

        // Perform the query
        return  _db.rawQuery(q, null);

    }

    /**
     * Gets todays records.
     *
     * @return
     */
    public HashMap<String, ArrayList<HeartRateObject>> getTodaysRecords() {

        HashMap<String, ArrayList<HeartRateObject>> todaysRecords = getActivityHeartObjectMap();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        Calendar calendar = Calendar.getInstance();

        open();

        String q = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " = '" + format.format(calendar.getTimeInMillis()) + "' ;";

        Cursor c = _db.rawQuery(q, null);

        Log.d(TAG, "getTodaysRecords: COUNT: " + String.valueOf(c.getCount()));
        Log.d(TAG, "getTodaysRecords: DATE: " + format.format(calendar.getTimeInMillis()));

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                todaysRecords.get(
                        c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)))
                .add(createHeartRateObject(c));
            }
        }

        c.close();
        close();

        return null;
    }

    public HashMap<String, ArrayList<HeartRateObject>> getLast24Records() {
        HashMap<String, ArrayList<HeartRateObject>> records = getActivityHeartObjectMap();

        open();

        String q = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " ORDER BY " + HeartRateContract.Entry.ID_COLUMN + " ASC LIMIT 24;";

        Cursor c = _db.rawQuery(q, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                records.get(c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)))
                        .add(createHeartRateObject(c));
            }
        }

        close();

        return records;
    }

    /**
     * Returns a hash map of an activity and list to store all heart rate objects.
     * @return
     */
    private HashMap<String, ArrayList<HeartRateObject>> getActivityHeartObjectMap() {

        HashMap<String, ArrayList<HeartRateObject>> heartRates = new HashMap<>();

        heartRates.put(Constants.Const.WALKING, new ArrayList<HeartRateObject>());

        heartRates.put(Constants.Const.CYCLING, new ArrayList<HeartRateObject>());

        heartRates.put(Constants.Const.RUNNING, new ArrayList<HeartRateObject>());

        heartRates.put(Constants.Const.RESTING, new ArrayList<HeartRateObject>());

        heartRates.put(Constants.Const.GENERAL, new ArrayList<HeartRateObject>());

        return heartRates;
    }

    public HashMap<String,ArrayList<HeartRateObject>> getActivityRecordsBetweenDates(
            String start, String end, String activityType) {

        HashMap<String, ArrayList<HeartRateObject>> records = getActivityHeartObjectMap();

        open();

        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE +
                " BETWEEN '" + start + "' AND  '" + end + "' " +
                ";";

        Cursor c = _db.rawQuery(q, null);

        Log.d(TAG, "getActivityRecordsBetweenDates: " + String.valueOf(records.get(Constants.Const.WALKING).size()));

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                records.get(c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)))
                        .add(createHeartRateObject(c));
            }
        }

        close();

        return records;
    }
}
