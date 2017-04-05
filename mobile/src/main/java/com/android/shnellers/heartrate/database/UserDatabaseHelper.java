package com.android.shnellers.heartrate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.android.shnellers.heartrate.database.UserContract.UserEntry.TABLE_NAME;


/**
 * Created by Sean on 19/10/2016.
 */

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "UserDBHelper";

    private int mDBVersion;

    private SQLiteDatabase db;


    public UserDatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d("dbHelper", "helper");
        mDBVersion = version;
    }

    /**
     * Creates the database on initial start.
     *
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: CREATING DATABASE");
        Log.w(TAG, "Current db version is " + db.getVersion());
        db.execSQL(UserDatabase.SQL_CREATE_USERS);
    }


    /**
     * Whenever the database updates, we simply discard ot and
     * create a new one using on create. This is only temporary.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

     //   db.execSQL(UserDatabase.SQL_DELETE_ENTRIES);
       // onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
