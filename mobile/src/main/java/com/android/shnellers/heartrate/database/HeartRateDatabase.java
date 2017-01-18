package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.shnellers.heartrate.HeartReading;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 15/01/2017.
 */

public class HeartRateDatabase {

    public static final String TAG = "HeartRateDatabase";

    private static final boolean DEBUG = true;

    private SQLiteDatabase _db;

    private HeartRateDBHelper mHelper;

    private HeartReading mHeartReading;

    private final Context context;

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

                    Log.i("BPM", Integer.toString(readings.getInt(bpmIndex)));

                    rates.add(new HeartReading(
                            readings.getInt(bpmIndex),
                            readings.getInt(dateTimeIndex)));
                } while (readings.moveToNext());

            }

            close();
            return rates;
        }
    }
}
