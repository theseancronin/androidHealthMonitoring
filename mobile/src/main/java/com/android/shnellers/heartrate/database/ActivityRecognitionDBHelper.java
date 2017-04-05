package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityRecognitionDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "ActivityDBHelper";
    private static final int DB_VERSION = 52;

    private static final String DB_NAME = "ActivityRecognition.db";

    private String mCreateTable;

    private String mTableName;

    private int mVersion;

    private static final String CREATE_RECOGNITION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RECOGNITION + "( " +
                    ActivityContract.ActivityEntries.ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    ActivityContract.ActivityEntries.TYPE_COLUMN  + " INTEGER, " +
                    ActivityContract.ActivityEntries.MINUTES_DETECTED + " INTEGER, " +
                    ActivityContract.ActivityEntries.SECONDS + " INTEGER," +
                    ActivityContract.ActivityEntries.ACTIVE + " INTEGER," +
                    ActivityContract.ActivityEntries.ACTIVITY_NUMBER + " INTEGER," +
                    WeightDBContract.WeightEntries.DATE + " DATETIME, " +
                    ActivityContract.ActivityEntries.TIME_MILLIS + " INTEGER, " +
                    ActivityContract.ActivityEntries.DATE_TIME_COLUMN  + " VARCHAR);";


    public ActivityRecognitionDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCreateTable = TABLE_RECOGNITION;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: CREATING TABLE");
        sqLiteDatabase.execSQL(CREATE_RECOGNITION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            Log.d(TAG, "DROPPING TABLE");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECOGNITION);
            onCreate(sqLiteDatabase);
        }
    }


    public ContentValues getAllActivityRecogitionRecords() {
        return null;
    }
}
