package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Sean on 27/11/2016.
 */

public class KinDatabase {

    private SQLiteDatabase db;

    // context of application using the database
    private final Context mContext;

    private KinDatabaseHelper mKinDatabaseHelper;

    /**
     *
     * @param context
     */
    public KinDatabase (final Context context) {
        mContext = context;
        mKinDatabaseHelper = new KinDatabaseHelper(context);

    }

    /**
     * Open the medication Database.
     *
     * @return
     * @throws SQLException
     */
    public KinDatabase open () throws SQLException {
        db = mKinDatabaseHelper.getWritableDatabase();
        return this;
    }

    /**
     * Close the database.
     */
    public void close () {
        db.close();
    }

    /**
     * Get the medication Database.
     *
     * @return
     */
    public SQLiteDatabase getDB () {
        return db;
    }

    public Cursor getKinDetails() {
        try {

            open();

            Cursor kin = db.rawQuery("SELECT * " +
                    "FROM kin_details WHERE id = 1;", null);

            if (kin.getCount() > 0) {
                Log.d(TAG, "record found");
             //   Log.d(TAG, kin.getString(1));
                return kin;
            }

        } catch (SQLException ex) {

            ex.printStackTrace();

        } finally {

            close();

        }

        return null;
    }

    public void saveKinDetails (final ContentValues values) {

        try {
            open();

            // Get all the details of the logged user from the database
            Cursor kin = db.rawQuery("SELECT * " +
                    "FROM kin_details;", null);

            // Move to the first retrieved row (Should only be one)
            kin.moveToFirst();

            if (kin.getCount() > 0) {


                db.update("kin_details", values, "id = " + 1, null);

            } else {

                Log.d(TAG, "inserting kin details");
                db.insert("kin_details", null, values);

            }

        } catch (SQLException sqlException) {

            sqlException.printStackTrace();

        } finally {

            close();

        }
    }

}
