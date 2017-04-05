package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Sean on 15/01/2017.
 */

public class HeartRateDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 68;

    private static final String DB_NAME = "HeartRate.db";

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + HeartRateContract.Entry.TABLE_NAME + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "bpm INTEGER, " +
            "type TEXT," +
            "status TEXT, " +
            "date DATETIME, " +
            "date_time INTEGER);";

    private Context mContext;

    public HeartRateDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            Toast.makeText(mContext, "DROPPING", Toast.LENGTH_LONG).show();
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeartRateContract.Entry.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

}
