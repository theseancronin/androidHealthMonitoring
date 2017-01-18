package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Sean on 15/01/2017.
 */

public class WeightDatabase {

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
}
