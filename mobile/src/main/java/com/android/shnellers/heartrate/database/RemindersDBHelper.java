package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 09/01/2017.
 */

public class RemindersDBHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + RemindersContract.Columns.TABLE_NAME + " (" +
            RemindersContract.Columns.ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RemindersContract.Columns.HOUR_COLUMN + " INTEGER, " +
            RemindersContract.Columns.MINUTE_COLUMN + " INTEGER, " +
            RemindersContract.Columns.DAYS_COLUMN + " VARCHAR(200), " +
            RemindersContract.Columns.TYPE_COLUMN + " VARCHAR(100)," +
            RemindersContract.Columns.ACTIVE_COLUMN + " INTEGER(1));";

    private static final String DELETE_TABLE = "" +
            "DROP TABLE IF EXISTS " + RemindersContract.Columns.TABLE_NAME;

    private static final String DB_NAME = "Reminder.db";

    private static final int DATABASE_VERSION = 1;



    public RemindersDBHelper(Context context) {
        super(context, RemindersContract.Columns.TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            sqLiteDatabase.execSQL(DELETE_TABLE);
            onCreate(sqLiteDatabase);
        }

    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        onUpgrade(db, oldVersion, newVersion);
    }


}
