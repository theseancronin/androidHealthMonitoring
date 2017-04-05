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

import static com.android.shnellers.heartrate.Constants.Const.AVG;
import static com.android.shnellers.heartrate.Constants.Const.DEFAULT_NO_VALUE;
import static com.android.shnellers.heartrate.Constants.Const.GENERAL;
import static com.android.shnellers.heartrate.Constants.Const.LESS_THAN_40;
import static com.android.shnellers.heartrate.Constants.Const.MAX_CAPITAL_M;
import static com.android.shnellers.heartrate.Constants.Const.MIN;
import static com.android.shnellers.heartrate.Constants.Const.OVER_100;
import static com.android.shnellers.heartrate.Constants.Const.RESTING;
import static com.android.shnellers.heartrate.Constants.Const.RESTING_RATES;
import static com.android.shnellers.heartrate.Constants.Const.TOTAL_COUNT;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;

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

        context.sendBroadcast(new Intent(HeartDBReceiver.HEART_DB_CHANGED));

        if (DEBUG) Log.d(TAG, "storeHeartRate: ");
    }

    public List<HeartReading> getRecentReadings() {
        open();

        Cursor readings = _db.rawQuery(
                "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " ORDER BY date(" + DATE_TIME_COLUMN + ") " +
                "LIMIT 3", null);

        if (readings == null) {
            close();
            return null;
        } else {
            List<HeartReading> rates = new ArrayList<>();
            int bpmIndex = readings.getColumnIndex(BPM_COLUMN);
            int dateTimeIndex = readings.getColumnIndex(DATE_TIME_COLUMN);

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
                " (SELECT DATETIME('now', '-7 day')) ORDER BY " + HeartRateContract.Entry.DATE + "  DESC;";

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

//        /Log.d(TAG, "get7DayReadings: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK).format(readings.get(readings.size() - 1).getDateTime()));

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
            long start, long end, String activityToSelect) {

        open();

        HashMap<String, ArrayList<HeartRateObject>> heartRates = getActivityHeartObjectMap();

        String q  = "SELECT " + activityToSelect + " FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + DATE_TIME_COLUMN +
                " >= " + start +  " ORDER BY " + DATE_TIME_COLUMN + " ASC ;";

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

        int startHour = 6;
        int endHour = 23;

        int count = 20;

        Calendar c = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        //endDate.add(Calendar.DAY_OF_YEAR, 1);

        c.add(Calendar.DAY_OF_YEAR, -count);
        c.set(Calendar.HOUR_OF_DAY, 0);


        Date date = new Date();

        while (c.getTimeInMillis() < endDate.getTimeInMillis()) {

            //c.set(Calendar.HOUR_OF_DAY, startHour);

            //int hourOfDay = c.get(Calendar.HOUR_OF_DAY);

            //while (hourOfDay != endHour) {

                addRecordToDB(c);

                c.add(Calendar.MINUTE, 7);

              //  hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            //}
           // c.add(Calendar.DAY_OF_MONTH, 1);
        }

//        c.add(Calendar.DAY_OF_YEAR, 1);
//        c.set(Calendar.HOUR_OF_DAY, startHour);
//
//        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
//
//        while (hourOfDay != endHour) {
//            addRecordToDB(c);
//            c.add(Calendar.MINUTE, 10);
//
//            hourOfDay = c.get(Calendar.HOUR_OF_DAY);
//        }

        close();

    }

    private void addRecordToDB(Calendar c) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        int min = 50;
        int max = 135;
        int hour = c.get(Calendar.HOUR_OF_DAY);

        ArrayList<Integer> hours = new ArrayList<>(Arrays.asList(9, 13, 14, 18, 21));
        ArrayList<Integer> minutes = new ArrayList<>(Arrays.asList(0, 10, 15, 30, 45));

        Random random = new Random();

        if (hour < 10) {
            max = 120;
        } else if (hour < 16) {
            max = 130;
        }



        int heartRate = random.nextInt(max - min + 1) + min;


        int minute = random.nextInt(minutes.size());

        //c.set(Calendar.HOUR_OF_DAY, hours.get(hour));
        //c.set(Calendar.MINUTE, minutes.get(minute));

        ContentValues values = new ContentValues();
        values.put(BPM_COLUMN, heartRate);
        values.put(DATE_TIME_COLUMN, c.getTimeInMillis());
        values.put(Constants.Const.STATUS, "OK");
        values.put(WeightDBContract.WeightEntries.DATE, format.format(c.getTimeInMillis()));
        values.put(HeartRateContract.Entry.TYPE,  RESTING);

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
                " WHERE " + BPM_COLUMN + " = " + bpm +
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
                !c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)).isEmpty() ?
                        c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)) : GENERAL
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
                    startDate.getColumnIndex(DATE_TIME_COLUMN))));
        }

        if (endDate.moveToFirst()) {
            dates.put("endDate", new Date(endDate.getLong(
                    endDate.getColumnIndex(DATE_TIME_COLUMN))));
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

        HashMap<String, Integer> restingDetails = createRestingDetailsHashMap();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        open();

        Calendar st = Calendar.getInstance();
        Calendar ed = Calendar.getInstance();

        st.add(Calendar.DAY_OF_YEAR, - 7);
//        String query = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME + " WHERE " +
//                HeartRateContract.Entry.TYPE + " = " + Constants.Const.RESTING + ";";

        String query  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE + " > " +
                " (SELECT DATETIME('now', '-7 day'));";

        Cursor c = _db.rawQuery(query, null);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int avg = 0;
        int sum = 0;
        int countRestingRates = 0;
        int over100 = 0;
        int lessThan40 = 0;


        if (c.moveToFirst()) {
            size = c.getCount();
            while (c.moveToNext()) {
                if (c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE)).equals(
                        Constants.Const.RESTING
                )) {
                    countRestingRates++;
                    int bpm = c.getInt(c.getColumnIndex(BPM_COLUMN));

                    sum += bpm;

                    if (bpm > max) {
                        max = bpm;
                    }

                    if (bpm < min) {
                        min = bpm;
                    }

                    if (bpm >= 100) {
                        over100++;
                    } else if (bpm < 40) {
                        lessThan40++;
                    }
                }
            }

            avg = sum / countRestingRates;
            restingDetails.put(Constants.Const.TOTAL_COUNT, countRestingRates);
            restingDetails.put(RESTING_RATES, countRestingRates > 0 ? countRestingRates : DEFAULT_NO_VALUE);
            restingDetails.put(Constants.Const.MIN, min > 0 ? min : DEFAULT_NO_VALUE);
            restingDetails.put(MAX_CAPITAL_M, max > 0 ? max : DEFAULT_NO_VALUE);
            restingDetails.put(AVG, avg > 0 ? avg : DEFAULT_NO_VALUE);
            restingDetails.put(Constants.Const.OVER_100, over100 > 0 ? over100 : DEFAULT_NO_VALUE);
            restingDetails.put(Constants.Const.LESS_THAN_40, lessThan40 > 0 ? over100 : DEFAULT_NO_VALUE);

        }


        c.close();
        close();

        return restingDetails;
    }



    private HashMap<String, Integer> createRestingDetailsHashMap() {

        HashMap<String, Integer> restingDetails = new HashMap<>();

        restingDetails.put(TOTAL_COUNT, DEFAULT_NO_VALUE);

        restingDetails.put(RESTING_RATES, DEFAULT_NO_VALUE);

        restingDetails.put(MIN, DEFAULT_NO_VALUE);

        restingDetails.put(MAX_CAPITAL_M, DEFAULT_NO_VALUE);

        restingDetails.put(OVER_100, DEFAULT_NO_VALUE);

        restingDetails.put(LESS_THAN_40, DEFAULT_NO_VALUE);

        restingDetails.put(AVG, DEFAULT_NO_VALUE);

        return restingDetails;
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
                total += c.getInt(c.getColumnIndex(BPM_COLUMN));
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
                " WHERE " + BPM_COLUMN + " = " +
                " (SELECT MAX(" + BPM_COLUMN + ") " +
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
                " WHERE " + BPM_COLUMN + " = " +
                " (SELECT MIN(" + BPM_COLUMN + ") " +
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
            date = format.format(c.getLong(c.getColumnIndex(DATE_TIME_COLUMN)));
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
            date = format.format(c.getLong(c.getColumnIndex(DATE_TIME_COLUMN)));
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
    public HashMap<String, ArrayList<HeartRateObject>> getAllRecordsBetweenDates(long start, long end) {
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
    private Cursor getRecordsBetween(long start, long end) {

        // All records query
        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + DATE_TIME_COLUMN + " > " + start + ";";

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

    /**
     * Retrieve the last 24 records.
     *
     * @return
     */
    public HashMap<String, ArrayList<HeartRateObject>> getLast24Records() {
        HashMap<String, ArrayList<HeartRateObject>> records = getActivityHeartObjectMap();

        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, -3);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);

        open();

        String q = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + DATE_TIME_COLUMN + " > " + c1.getTimeInMillis();

        Cursor c = _db.rawQuery(q, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {
//                Log.d(TAG, "BPM: " + String.valueOf(c.getInt(c.getColumnIndex(BPM_COLUMN))) +
//                        " Type: " + String.valueOf(c.getString(c.getColumnIndex(TYPE))) +
//                        " Status: " + c.getString(c.getColumnIndex(STATUS)) +
//                        " Date: " + c.getString(c.getColumnIndex(DATE)));
                String type = c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE));
                if (type != null && !type.isEmpty()) {
                    records.get(type).add(createHeartRateObject(c));
                }

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
            long start, long end, String activityType) {

        HashMap<String, ArrayList<HeartRateObject>> records = getActivityHeartObjectMap();

        open();

        String q  = "SELECT * FROM " + HeartRateContract.Entry.TABLE_NAME +
                " WHERE " + DATE_TIME_COLUMN +
                " > " + start + ";";

        Cursor c = _db.rawQuery(q, null);

        Log.d(TAG, "getActivityRecordsBetweenDates: " + String.valueOf(records.get(Constants.Const.WALKING).size()));

        if (c.moveToFirst()) {
            while (c.moveToNext()) {

                String type = c.getString(c.getColumnIndex(HeartRateContract.Entry.TYPE));

                if (!type.isEmpty()) {
                    records.get(type).add(createHeartRateObject(c));
                }

            }
        }

        close();

        return records;
    }
}
