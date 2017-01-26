package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "ActivityDBHelper";



    private String mCreateTable;

    private String mTableName;

    private static final String CREATE_RECOGNITION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ActivityContract.ActivityEntries.TABLE_RECOGNITION + "( " +
                    ActivityContract.ActivityEntries.ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    ActivityContract.ActivityEntries.TYPE_COLUMN  + " INTEGER, " +
                    ActivityContract.ActivityEntries.MINUTES_DETECTED + " INTEGER, " +
                    ActivityContract.ActivityEntries.MILLISECONDS + " INTEGER," +
                    ActivityContract.ActivityEntries.DATE_TIME_COLUMN  + " INTEGER);";

    private static final String CREATE_ACTIVITIES_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ActivityContract.ActivityEntries.TABLE_NAME + "( " +
                    ActivityContract.ActivityEntries.ID_COLUMN +" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    ActivityContract.ActivityEntries.TYPE_COLUMN + " VARCHAR, " +
                    ActivityContract.ActivityEntries.DISTANCE_TRAVELLED_COLUMN + " INTEGER , " +
                    ActivityContract.ActivityEntries.START_TIME_COLUMN + " INTEGER, " +
                    ActivityContract.ActivityEntries.END_TIME_COLUMN + " INTEGER, " +
                    ActivityContract.ActivityEntries.TIME_TAKEN_COLUMN + " INTEGER, " +
                    ActivityContract.ActivityEntries.DATE_TIME_COLUMN + " INTEGER, " +
                    ActivityContract.ActivityEntries.CALORIES_BURNED_COLUMN +  " INTEGER, " +
                    ActivityContract.ActivityEntries.STEPS_COLUMN  + "INTEGER);";

    public ActivityDBHelper(Context context, final int version,
                            final String tableName, final String dbName) {
        super(context, dbName, null, version);
        mCreateTable = tableName;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: CREATING TABLE");
        sqLiteDatabase.execSQL(CREATE_RECOGNITION_TABLE);
        sqLiteDatabase.execSQL(CREATE_ACTIVITIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            Log.d(TAG, "onUpgrade: DROPPING");
            if (mTableName != null) {
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + mTableName);
                onCreate(sqLiteDatabase);
            }
        }
    }


}
