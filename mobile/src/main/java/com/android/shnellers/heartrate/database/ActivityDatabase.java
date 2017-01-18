package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityDatabase {

    private SQLiteDatabase db;

    private ActivityContract.ActivityEntries mContract;

    private ActivityDBHelper mDBHelper;

    private final Context mContext;

    public ActivityDatabase(Context context) {
        mContext = context;
        mDBHelper = new ActivityDBHelper(context);
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
