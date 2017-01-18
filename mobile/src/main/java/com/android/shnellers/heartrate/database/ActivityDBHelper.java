package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "HealthMonitor.db";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ActivityContract.ActivityEntries.TABLE_NAME + "( " +
                    "id INTEGER " + ActivityContract.ActivityEntries.ID_COLUMN + " NOT NULL AUTOINCREMENT, " +
                    "type VARCHAR " + ActivityContract.ActivityEntries.TYPE_COLUMN + ", " +
                    "distance INTEGER " + ActivityContract.ActivityEntries.DISTANCE_TRAVELLED_COLUMN + ", " +
                    "start_time int " + ActivityContract.ActivityEntries.START_TIME_COLUMN + ", " +
                    "end_time int " + ActivityContract.ActivityEntries.END_TIME_COLUMN + ", " +
                    "time_taken int " + ActivityContract.ActivityEntries.TIME_TAKEN_COLUMN + ", " +
                    "date_time INTEGER " + ActivityContract.ActivityEntries.DATE_TIME_COLUMN + ", " +
                    "calories_burned INTEGER " + ActivityContract.ActivityEntries.CALORIES_BURNED_COLUMN + ", " +
                    "steps INTEGER " + ActivityContract.ActivityEntries.STEPS_COLUMN + ");";


    public ActivityDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ActivityContract.ActivityEntries.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
