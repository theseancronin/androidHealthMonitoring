package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.shnellers.heartrate.Medication;

import java.util.ArrayList;
import java.util.List;

import static com.android.shnellers.heartrate.database.MedicationContract.MedicationEntry._ID;

/**
 * Created by Sean on 29/10/2016.
 */

public class MedicationDatabase {

    private SQLiteDatabase db;

    // context of application using the database
    private final Context mContext;

    private MedicationDatabaseHelper mMedicationDatabaseHelper;

    /**
     *
     * @param context
     */
    public MedicationDatabase(final Context context) {
        mContext = context;
        mMedicationDatabaseHelper = new MedicationDatabaseHelper(context);
    }

    /**
     * Open the medication Database.
     *
     * @return
     * @throws SQLException
     */
    public MedicationDatabase open () throws SQLException {
        db = mMedicationDatabaseHelper.getWritableDatabase();
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

    /**
     * Insert a medication into the database.
     *
     * @param name
     * @param strength
     */
    public void addNewMedication (
                        final String name,
                        final int strength,
                        final int frequency) {

        ContentValues values = new ContentValues();

        values.put(
                MedicationContract.MedicationEntry.COLUMN_NAME,
                name);
        values.put(
                MedicationContract.MedicationEntry.COLUMN_STRENGTH,
                strength);
        values.put(
                MedicationContract.MedicationEntry.COLUMN_FREQUENCY,
                frequency);

        getDB().insert("medication", null, values);
    }

    public void removeMedication(final int id) {
        String table = "medication";
        String whereClause = "id=?";
        String[] whereArgs = new String[] { String.valueOf(id) };
        db.delete(table, whereClause, whereArgs);
    }

    /**
     * Returns all current medication.
     *
     * @return
     * @throws SQLException
     */
    public List<Medication> getMedication () throws SQLException {

        open();

        ArrayList<Medication> m = new ArrayList<>();

        Cursor meds = db.rawQuery("SELECT * FROM medication", null);

        int name = meds.getColumnIndex(
                MedicationContract.MedicationEntry.COLUMN_NAME);
        int strength = meds.getColumnIndex(
                MedicationContract.MedicationEntry.COLUMN_STRENGTH);
        int frequency = meds.getColumnIndex(
                MedicationContract.MedicationEntry.COLUMN_FREQUENCY);

        if (meds == null) {
            meds.close();
            return null;
        }

        if (meds.moveToFirst() && meds.getCount() >= 1) {
            do {

                Log.i("MED NAME", meds.getString(name));

                m.add(new Medication(
                        meds.getInt(meds.getColumnIndex(_ID)),
                        meds.getString(name),
                        meds.getInt(strength),
                        meds.getInt(frequency)));
            } while (meds.moveToNext());


        }

        meds.close();

        return m;
    }

}
