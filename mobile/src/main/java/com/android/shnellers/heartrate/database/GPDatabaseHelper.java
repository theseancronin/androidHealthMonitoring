package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sean on 27/11/2016.
 */

public class GPDatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 5;

    private static final String DB_NAME = "gp_details.db";

    private static final String TAG = "GP_DB";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS gp_details (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "name VARCHAR(200) NOT NULL, " +
            "practice VARCHAR(150)," +
            "telephone INTEGER(25));";

    public GPDatabaseHelper (final Context context) {
        // cursor factory of null
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("gpDB", "new DB");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        Log.d("gpDB", "create table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading");
        if (newVersion > oldVersion) {
            //db.execSQL(CREATE_TABLE);
            Log.d(TAG, "New Version");
        }

    }
}
