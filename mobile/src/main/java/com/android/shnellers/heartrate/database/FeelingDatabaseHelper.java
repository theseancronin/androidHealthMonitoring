package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 12/02/2017.
 */

public class FeelingDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "feelings.db";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            FeelingDatabaseContract.FeelingConsts.TABLE_NAME + " ( " +
            FeelingDatabaseContract.FeelingConsts.ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FeelingDatabaseContract.FeelingConsts.TYPE_COLUMN + " TEXT, " +
            HeartRateContract.Entry.DATE + " DATE," +
            FeelingDatabaseContract.FeelingConsts.DATE_TIME + " INTEGER);";

    public FeelingDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + FeelingDatabaseContract.FeelingConsts.TABLE_NAME);
            onCreate(db);
        }
    }
}
