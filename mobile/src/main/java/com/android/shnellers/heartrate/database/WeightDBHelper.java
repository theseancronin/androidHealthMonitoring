package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 15/01/2017.
 */

public class WeightDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "Weight.db";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RemindersContract.Columns.TABLE_NAME + " (" +
                    WeightDBContract.WeightEntries.ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WeightDBContract.WeightEntries.WEIGHT_COLUMN + " INTEGER, " +
                    WeightDBContract.WeightEntries.DATE_TIME_COLUMN + " INTEGER);";

    public WeightDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF NOT EXISTS " + WeightDBContract.WeightEntries.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
