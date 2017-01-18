package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Sean on 27/11/2016.
 */

public class GPDatabase {

    private SQLiteDatabase db;

    // context of application using the database
    private final Context mContext;

    private GPDatabaseHelper mGPDatabaseHelper;

    /**
     *
     * @param context
     */
    public GPDatabase (final Context context) {
        mContext = context;
        mGPDatabaseHelper = new GPDatabaseHelper(context);

    }

    /**
     * Open the medication Database.
     *
     * @return
     * @throws SQLException
     */
    public GPDatabase open () throws SQLException {
        db = mGPDatabaseHelper.getWritableDatabase();
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

    public Cursor getGPDetails() {
        open();

        try {



            Cursor gp = db.rawQuery("SELECT * " +
                    "FROM gp_details WHERE id = 1;", null);

            if (gp.getCount() > 0) {
                Log.d("GP_DATABASE", "record found");
                return gp;
            }

        } catch (SQLException ex) {

            ex.printStackTrace();

        } finally {

            close();

        }

        return null;
    }

    public void saveGPDetails (final String email,
                               final ContentValues values) {

        try {
            open();

            // Get all the details of the logged user from the database
            Cursor user = db.rawQuery("SELECT * " +
                    "FROM gp_details;", null);

            // Move to the first retrieved row (Should only be one)
            user.moveToFirst();

            Log.d("number of rows", Integer.toString(user.getCount()));
            if (user.getCount() > 0) {

                db.update("gp_details", values, "id = " + 1, null);

            } else {
                Log.d("INSERTING", "inserting new gp");
                db.insert("gp_details", null, values);
            }
        } catch (SQLException sqlException) {

            sqlException.printStackTrace();

        } finally {

            close();

        }
    }
}
