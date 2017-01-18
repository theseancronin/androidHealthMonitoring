package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Sean on 09/01/2017.
 */

public class RemindersDatabase {

    private SQLiteDatabase db;

    private final Context mContext;

    private RemindersDBHelper mDBHelper;

    public RemindersDatabase(final Context context) {
        mContext = context;
        mDBHelper = new RemindersDBHelper(context);
    }

    private RemindersDatabase open () throws SQLException {
        db = mDBHelper.getWritableDatabase();
        return this;
    }

    private void close() {
        db.close();
    }

    public void saveReminder(final ContentValues values) throws SQLException {

        open();

        Log.d("alarm", Integer.toString(values.getAsInteger(RemindersContract.Columns.ACTIVE_COLUMN)));

        db.insert(RemindersContract.Columns.TABLE_NAME, null, values);

        close();
    }

    public Cursor getReminders() {
        try {

            open();

            Cursor reminders = db.rawQuery(
                    "SELECT * " +
                    "FROM " + RemindersContract.Columns.TABLE_NAME, null);

            if (reminders == null) {
                return null;
            } else {
                return reminders;
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {

        }

        return null;
    }

    public void activateAlarm(final int id) throws SQLException {
        open();

        Log.d("ReminderDB", "Activating alarm");

        ContentValues values = new ContentValues();
        values.put(RemindersContract.Columns.ACTIVE_COLUMN,
                RemindersContract.Columns.ALARM_ON);

        db.update(RemindersContract.Columns.TABLE_NAME, values,
                RemindersContract.Columns.ID_COLUMN + "=" + id, null);

        close();
    }

    public void deactivateAlarm(final int id) throws SQLException {
        open();

        Log.d("ReminderDB", "Deactivating alarm");

        ContentValues values = new ContentValues();
        values.put(RemindersContract.Columns.ACTIVE_COLUMN,
                RemindersContract.Columns.ALARM_OFF);

        db.update(RemindersContract.Columns.TABLE_NAME, values,
                RemindersContract.Columns.ID_COLUMN + "=" + id, null);

        close();
    }
}
