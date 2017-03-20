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
import java.util.Locale;

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

            String q  = "SELECT * FROM " + WeightDBContract.WeightEntries.TABLE_NAME +
                    " WHERE " + WeightDBContract.WeightEntries.DATE + " > " +
                    " (SELECT DATETIME('now', '-7 day'));";

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

}
