package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sean on 27/11/2016.
 */

public class KinDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "Kin.db";

    private static final String TAG = "KIN_DB";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS kin_details (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name VARCHAR(200) NOT NULL, " +
            "relationship VARCHAR(150)," +
            "telephone INTEGER(25));";

    public KinDatabaseHelper (final Context context) {
        // cursor factory of null
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(TAG, "new DB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d(TAG, "create table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading");
        if (newVersion > oldVersion) {
            //db.execSQL(CREATE_TABLE);

        }

    }
}
