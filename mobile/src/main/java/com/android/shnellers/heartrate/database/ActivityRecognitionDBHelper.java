package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityRecognitionDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "ActivityDBHelper";

    private String mCreateTable;

    private String mTableName;

    private int mVersion;

    private static final String CREATE_RECOGNITION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + ActivityContract.ActivityEntries.TABLE_RECOGNITION + "( " +
                    ActivityContract.ActivityEntries.ID_COLUMN + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    ActivityContract.ActivityEntries.TYPE_COLUMN  + " INTEGER, " +
                    ActivityContract.ActivityEntries.MINUTES_DETECTED + " INTEGER, " +
                    ActivityContract.ActivityEntries.SECONDS + " INTEGER," +
                    ActivityContract.ActivityEntries.ACTIVE + " INTEGER," +
                    ActivityContract.ActivityEntries.ACTIVITY_NUMBER + " INTEGER," +
                    WeightDBContract.WeightEntries.DATE + " DATETIME, " +
                    ActivityContract.ActivityEntries.DATE_TIME_COLUMN  + " VARCHAR);";


    public ActivityRecognitionDBHelper(Context context, final int version,
                            final String tableName, final String dbName) {
        super(context, dbName, null, version);
        mCreateTable = tableName;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate: CREATING TABLE");
        sqLiteDatabase.execSQL(CREATE_RECOGNITION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            Log.d(TAG, "onUpgrade: DROPPING TABLE");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ActivityContract.ActivityEntries.TABLE_RECOGNITION);
            onCreate(sqLiteDatabase);
        }
    }


    public ContentValues getAllActivityRecogitionRecords() {
        return null;
    }
}
