package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 29/10/2016.
 */

public class MedicationDatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "medication.db";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS medication (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name VARCHAR(200) NOT NULL, " +
                    "strength INTEGER(5)," +
                    "frequency INTEGER(5));";

    public MedicationDatabaseHelper(final Context context) {
        // cursor factory of null
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
