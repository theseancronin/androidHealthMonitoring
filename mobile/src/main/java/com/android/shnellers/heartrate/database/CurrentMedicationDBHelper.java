package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 30/10/2016.
 */

public class CurrentMedicationDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private static final String DB_NAME = "CurrentMedication.db";

    private static final String CREATE_TABLE = "CREATE TABLE current_medication (" +
            "user_id Integer(9) NOT NULL PRIMARY KEY, " +
            "medication_id Integer(9) NOT NULL PRIMARY KEY, " +
            "frequency INTEGER(4));";

    public CurrentMedicationDBHelper(final Context context) {
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
