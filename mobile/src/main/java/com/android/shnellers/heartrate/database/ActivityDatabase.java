package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.models.ActivityModel;

import static android.content.ContentValues.TAG;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityDatabase {

    private static final int DB_VERSION = 2;

    private static final String DB_NAME = "HealthMonitor.db";

    private SQLiteDatabase db;

    private ActivityContract.ActivityEntries mContract;

    private ActivityDBHelper mDBHelper;

    private final Context mContext;

    public ActivityDatabase(Context context) {
        mContext = context;
        mDBHelper = new ActivityDBHelper(context, DB_VERSION,
                ActivityContract.ActivityEntries.TABLE_NAME,
                DB_NAME);
    }

    public ActivityDatabase open() {
        db = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDb () {
        return db;
    }

    public void storeActivity(final ContentValues values) throws SQLiteException {
       // Log.d(TAG, "storeActivity: ");
        open();
        db.insert(ActivityContract.ActivityEntries.TABLE_NAME, null, values);
        close();
    }

    public ActivityModel getLastActivity() throws SQLiteException {

        //Log.d(TAG, "getLastActivity: ");
        SQLiteDatabase dba = mDBHelper.getReadableDatabase();

        // A simple query to get the last record put into the database
        String query = "SELECT * FROM " + ActivityContract.ActivityEntries.TABLE_NAME +
                " WHERE " + ActivityContract.ActivityEntries.ID_COLUMN + " = ( " +
                "SELECT MAX(" + ActivityContract.ActivityEntries.ID_COLUMN + ") FROM " +
                ActivityContract.ActivityEntries.TABLE_NAME + ");";

        Cursor c = dba.rawQuery(query, null);

        // If the last entry exists, we create a model for that entry and return it
        if (c.moveToFirst()) {
            ActivityModel model = new ActivityModel(
                c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.ID_COLUMN)),
                c.getString(c.getColumnIndex(ActivityContract.ActivityEntries.TYPE_COLUMN)),
                c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.START_TIME_COLUMN)),
                c.getInt(c.getColumnIndex(ActivityContract.ActivityEntries.FINISHED_COLUMN)),
                c.getString(c.getColumnIndex(ActivityContract.ActivityEntries.DATE_TIME_COLUMN))
            );

            return model;
        }

        dba.close();
        return null;
    }

    /**
     * When an activity has completed we must update the activityRecord in the database.
     *
     * @param id
     * @param values
     */
    public void finishActivity(final int id, final ContentValues values) {

       // Log.d(TAG, "finishActivity: ");
        SQLiteDatabase dba = mDBHelper.getWritableDatabase();
        try {

            int row = dba.update(ActivityContract.ActivityEntries.TABLE_NAME, values,
                    ActivityContract.ActivityEntries.ID_COLUMN + " = " + id + ";", null);

            if (row != -1) {
                Log.d(TAG, "finishActivity: SUCCESS");
            } else {
                Log.d(TAG, "finishActivity: UNSUCCESSFUL");
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        dba.close();


    }
}
