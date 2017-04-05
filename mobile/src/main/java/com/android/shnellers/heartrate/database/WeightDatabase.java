package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.models.WeightObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.android.shnellers.heartrate.Constants.Const.SEVEN_DAYS_BEFORE;
import static com.android.shnellers.heartrate.Constants.Const.TODAY;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.DATE;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.ID_COLUMN;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.TABLE_NAME;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.WEIGHT_COLUMN;
import static com.android.shnellers.heartrate.weight.WeightAnalysis.SEVEN_DAY;
import static com.android.shnellers.heartrate.weight.WeightAnalysis.YESTERDAY;

/**
 * Created by Sean on 15/01/2017.
 */

public class WeightDatabase {

    private static final String TAG = "WeightDatabase";

    private SQLiteDatabase db;

    private WeightDBHelper mWeightDBHelper;

    private final Context mContext;

    public WeightDatabase(Context context) {
        mContext = context;
        mWeightDBHelper = new WeightDBHelper(context);
    }

    public WeightDatabase open () {
        db = mWeightDBHelper.getWritableDatabase();
        return this;
    }

    public void close () {
        db.close();
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void storeWeight(final ContentValues values) {
        open();
        db.insert(WeightDBContract.WeightEntries.TABLE_NAME, null, values);
        close();
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(cal.getTimeInMillis());
    }

    private String getYesterdaysDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(cal.getTimeInMillis());

    }

    /**
     *
     * @return
     */
    public WeightObject getTodaysWeight() {
        WeightObject weight = null;
        Log.d(TAG, "getTodaysWeight: ");

        try {

            SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

            String query = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                    " WHERE " + WeightDBContract.WeightEntries.DATE + " = " +
                    " '" + getTodaysDate() + "'" +
                    " ORDER BY id DESC LIMIT 1;";

            Cursor c = db.rawQuery(query, null);

            Log.d(TAG, "getTodaysWeights: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {
                do {
                    Log.d(TAG, "getTodaysWeight: " + c.getString(c.getColumnIndex("date")));
                    Log.d(TAG, "getTodaysWeight: " + c.getString(c.getColumnIndex("weight")));
                    weight = createWeightObject(c);
                } while (c.moveToNext());

            }

            db.close();
            c.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return weight;
    }

    /**
     *
     * @return
     */
    public WeightObject getYesterdaysWeight() {
        WeightObject weight = null;

        try {

            SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

            String query = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                    " WHERE " + WeightDBContract.WeightEntries.DATE + " = " +
                    " '" + getYesterdaysDate() + "'" +
                    " ORDER BY id DESC LIMIT 1;";

            Cursor c = db.rawQuery(query, null);

            Log.d(TAG, "getYeste: " + String.valueOf(c.getCount()));

            if (c.moveToFirst()) {
                Log.d(TAG, "getTodaysWeight: " + c.getString(c.getColumnIndex("date")));
                Log.d(TAG, "getTodaysWeight: " + c.getString(c.getColumnIndex("weight")));
                weight = createWeightObject(c);
            }

            db.close();
            c.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return weight;
    }

    private WeightObject createWeightObject(final Cursor c) {
        return new WeightObject(
                c.getDouble(c.getColumnIndex(WeightDBContract.WeightEntries.WEIGHT_COLUMN)),
                c.getString(c.getColumnIndex(WeightDBContract.WeightEntries.TYPE)),
                c.getLong(c.getColumnIndex(WeightDBContract.WeightEntries.DATE_TIME_COLUMN)));
    }

    /**
     *
     * @return
     */
    public ArrayList<WeightObject> getLast7Days() {

        ArrayList<WeightObject> weights = new ArrayList<>();

        try {

            SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -30);

            String q  = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                    " WHERE " + WeightDBContract.WeightEntries.DATE_TIME_COLUMN + " >= " +
                    calendar.getTimeInMillis();

            Cursor c = db.rawQuery(q, null);

            if (c.moveToFirst()) {
                do {

                    weights.add(
                            new WeightObject(
                                    c.getDouble(c.getColumnIndex(WeightDBContract.WeightEntries.WEIGHT_COLUMN)),
                                    c.getString(c.getColumnIndex(WeightDBContract.WeightEntries.TYPE)),
                                    c.getLong(c.getColumnIndex(WeightDBContract.WeightEntries.DATE_TIME_COLUMN))
                            )
                    );

                } while (c.moveToNext());
            }

            c.close();
            db.close();

        } catch (SQLiteException e) {

            e.printStackTrace();

        }

        return weights;
    }

    /**
     *
     * @return
     * @throws SQLiteException
     */
    public int getWeekStartWeight() throws SQLiteException {

        SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

        String q  = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                " WHERE " + WeightDBContract.WeightEntries.DATE + " > " +
                " (SELECT DATETIME('now', '-7 day')) ORDER BY " +
                WeightDBContract.WeightEntries.DATE + " DESC;";

        Cursor cursor = db.rawQuery(q, null);

        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(WeightDBContract.WeightEntries.WEIGHT_COLUMN));
        }

        cursor.close();
        db.close();

        return 0;
    }

    /**
     *
     * @return
     */
    public int getLastEntry() {

        SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

        String q  = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                " WHERE " + ID_COLUMN  + " = " +
                " (SELECT MAX(" + ID_COLUMN + ") FROM " + TABLE_NAME +");";

        Cursor c = db.rawQuery(q, null);

        if (c.moveToFirst()) {
            return c.getInt(c.getColumnIndex(WEIGHT_COLUMN));
        }

        c.close();
        db.close();

        return -1;
    }

    /**
     *
     * @return
     * @throws SQLiteException
     */
    public HashMap<String,Integer> getWeightChangeOverWeek() throws SQLiteException {

        HashMap<String,Integer> map = getChangeMap();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.add(Calendar.DAY_OF_YEAR, -7);

        String weekStartWeight  = getQuery(start);
        String recentWeight = getQuery(end);

        int startWeight = getWeight(weekStartWeight);
        int todaysWeight = getWeight(recentWeight);


        return map;
    }
//
//    public int getLastWeightEntry() {
//
//    }

    private int getWeight(final String query) {

        SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            return c.getInt(c.getColumnIndex(WEIGHT_COLUMN));
        }

        c.close();
        db.close();

        return -1;
    }

    private String getQuery(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        return "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                " WHERE " + DATE  + " = " + format.format(calendar.getTimeInMillis()) +";";
    }

    private HashMap<String, Integer> getChangeMap() {

        HashMap<String,Integer> map = new HashMap<>();
        map.put(TODAY, 0);
        map.put(SEVEN_DAYS_BEFORE, 0);

        return map;
    }

    public float getWeeklyWeightChange() {

        HashMap<String, Float> weightDayMap = getWeights();

        float todaysWeight = weightDayMap.get(TODAY);

        float sevenDay = weightDayMap.get(SEVEN_DAY);

        float yesterdayChange = 0;

        float weeklyChange = 0;

        if (todaysWeight == -1 || sevenDay == -1) {
            return -1;
        }

        // First check if todays weight was logged so we can compare
        // Check if weight recorded at start of week
        // Get weight change over the last seven days
        weeklyChange = todaysWeight - sevenDay;


        return weeklyChange;

    }

    /**
     *
     * @return
     */
    private HashMap<String, Float> getWeights() {

        HashMap<String, Float> map = new HashMap<>();

        Calendar day7 = Calendar.getInstance();
        day7.add(Calendar.DAY_OF_YEAR, -7);

        Calendar yest = Calendar.getInstance();
        yest.add(Calendar.DAY_OF_YEAR, -1);

        Calendar today = Calendar.getInstance();

        float d7Value = getWeightOnDay(day7);
        float yestValue = getWeightOnDay(yest);
        float todaysWeight = getWeightOnDay(today);

        map.put(SEVEN_DAY, d7Value);
        map.put(YESTERDAY, yestValue);
        map.put(TODAY, todaysWeight);

        return map;
    }

    /**
     *
     * @param time
     * @return
     */
    private float getWeightOnDay(final Calendar time) {

        float weight = -1;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        SQLiteDatabase db = mWeightDBHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + DATE + " = " + format.format(time.getTimeInMillis());

        Cursor c = db.rawQuery(query, null);

        if (c.moveToLast()) {
            weight = c.getInt(c.getColumnIndex(WEIGHT_COLUMN));
        }

        c.close();
        db.close();

        return weight;
    }
}
