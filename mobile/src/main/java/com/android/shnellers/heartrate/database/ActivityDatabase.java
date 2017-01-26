package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityDatabase {

    private static final int DB_VERSION = 1;

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

    public void storeActivity(final ContentValues values) {
        open();
        db.insert(mContract.TABLE_NAME, null, values);
        close();
    }
}
