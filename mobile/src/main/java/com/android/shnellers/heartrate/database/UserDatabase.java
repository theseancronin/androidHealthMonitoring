package com.android.shnellers.heartrate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.User;

import java.util.HashMap;

import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_AGE;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_CONDITION;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_DATE_OF_BIRTH;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_EMAIL;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_LOCATION;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_PASSWORD;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_PHONE_NUMBER;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.HEIGHT;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PATIENT_NUMBER;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NUMBER;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.TABLE_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry._ID;


/**
 * Created by Sean on 18/10/2016.
 */

public class UserDatabase {

    // Implement methods that create and maintain the database.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Users.db";
    private static final String TAG = UserDatabaseHelper.class.getSimpleName();

    public static final String SQL_CREATE_USERS =
            "CREATE TABLE IF NOT EXISTS " + UserContract.UserEntry.TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserContract.UserEntry.COLUMN_NAME + " VARCHAR(70) NOT NULL," +
                    UserContract.UserEntry.COLUMN_EMAIL + " VARCHAR(70) NOT NULL," +
                    UserContract.UserEntry.COLUMN_PASSWORD + " VARCHAR(70) NOT NULL," +
                    UserContract.UserEntry.COLUMN_AGE + " INTEGER(3), " +
                    COLUMN_PHONE_NUMBER + " INTEGER, " +
                    PATIENT_NUMBER + " VARCHAR, " +
                    HEIGHT + " INTEGER, " +
                    COLUMN_DATE_OF_BIRTH + " DATETIME, " +
                    COLUMN_LOCATION + " VARCHAR, " +
                    PHARMACY_NAME + " VARCHAR, " +
                    PHARMACY_NUMBER + " INTEGER, " +
                    UserContract.UserEntry.COLUMN_CONDITION + " VARCHAR(1000)," +
                    UserContract.UserEntry.COLUMN_LOGGED_IN + " INTEGER(1));";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME;

    public SQLiteDatabase db;

    // context of application using the database
    private final Context mContext;

    private UserDatabaseHelper mUserDatabaseHelper;

    private User _loggedInUser;

    private boolean _userIsLoggedIn = false;

    public UserDatabase (Context context) {
        mContext = context;
        mUserDatabaseHelper = new UserDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    /**
     * Open a new instance of the database.
     *
     * @return
     * @throws SQLException
     */
    public UserDatabase open () throws SQLException {
        db = mUserDatabaseHelper.getWritableDatabase();
        Log.d("OpeningDB", "OPENING");
        return this;
    }

    /**
     * Close the db.
     */
    public void close() {
        db.close();
    }

    /**
     * Get the instance of the database.
     * @return
     */
    public SQLiteDatabase getDB () {
        return db;
    }

    /**
     * Insert a new user into the users table.
     *
     * @param name
     * @param email
     * @param password
     */
    public void insert(final String name, final String email, final String password) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);

        open();
        long insterted = db.insert(UserContract.UserEntry.TABLE_NAME, null, values);

        if (insterted != -1) {
            Log.d(TAG, "insert: INSERT WORKED");
        } else {
            Log.d(TAG, "insert: NOT INSERTED");
        }
        close();
        Log.d(TAG, "insert: " + name);
       // printDB();
    }

    /**
     * FInd the user by the given email and password.
     *
     * @param email
     * @param password
     * @return
     * @throws SQLException
     */
    public Cursor getUser(final String email, final String password) throws SQLException {
        open();
        Cursor user = db.rawQuery("SELECT * FROM users WHERE email = '" + email +
                "' AND password = '" + password + "'", null);

        int e = user.getColumnIndex("name");
        user.moveToFirst();
        if (user != null) {
//            db.rawQuery("UPDATE users SET " + UserContract.UserEntry.COLUMN_LOGGED_IN +
//                       " = 1 WHERE id = " +
//                       Integer.parseInt(user.getString(user.getColumnIndex("id"))) + ";", null);
            close();
            return user;
        } else {
            close();
            return null;
        }

    }

    /**
     * Set the logged in user object.
     *
     * @param user
     */
    public void setLoggedInUser(final User user) {
        _loggedInUser = user;
    }

    /**
     * Get the logged in user object.
     *
     * @return
     */
    public User getLoggedInUser() {
        return _loggedInUser;
    }

    /**
     * Set if the user is logged in.
     *
     * @param loggedIn
     */
    public void setUserIsLoggedIn(final boolean loggedIn) {

        _userIsLoggedIn = loggedIn;
    }

    /**
     * Check if the user is logged in.
     *
     * @return
     */
    public boolean getIsUserLoggedIn() {
        return _userIsLoggedIn;
    }

    /**
     * Print users of the database.
     *
     * @throws SQLException
     */
    public void printDB () throws SQLException {


        Cursor c = getDB().rawQuery("SELECT * FROM users", null);
        int nameIndex = c.getColumnIndex("name");
        int emailIndex = c.getColumnIndex("email");
        int passwordIndex = c.getColumnIndex("password");

        c.moveToFirst();

        while (c.moveToNext()) {
            Log.i("Users", c.getString(nameIndex) + " : " +
                    c.getString(emailIndex) + " : " +
                    c.getString(passwordIndex));

        }

        c.close();
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {

        // Delete All Rows
        db.delete(UserContract.UserEntry.TABLE_NAME, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }



    /**
     * Update the user details.
     */
    public void updateUserDetails(final String name,
                                  final String age,
                                  final String condition,
                                  final String email,
                                  final String weight,
                                  final String location,
                                  final String phoneNumber,
                                  final ContentValues values) {

        Log.i("EMAIL", email);

        try {
            open();

            db.update("users",
                    values,
                    "email = ? ", new String[]{email});
        } catch (SQLiteException e) {

            Log.e("SQLite ERROR", e.toString());

            e.printStackTrace();
        }



//        db.rawQuery("UPDATE users " +
//                "SET name = '" + name + "', " +
//                    "age = '" + age + "', " +
//                    "condition = '" + condition + "', " +
//                "WHERE email = '" + email + "';", null);
    }

    /**
     * Update the pharmacy details for the user.
     */
    public void savePharmacyDetails(final String name, final int number) {

        ContentValues values = new ContentValues();
        values.put(PHARMACY_NAME, name);
        values.put(PHARMACY_NUMBER, number);

        open();

        long insert = db.update(TABLE_NAME, values, _ID + " = " + 1, null);

        if (insert == -1 ) {
            Log.d(TAG, "FAILED");
        }

        close();

    }

    /**
     *
     * @return
     */
    public HashMap<String, String> getPharmacyDetails() {

        HashMap<String, String> fields = new HashMap<>();

        open();

        String q = "SELECT * FROM " +
                 TABLE_NAME;

        Cursor c = db.rawQuery(q, null);

        if (c.moveToFirst()) {

            String name = c.getString(c.getColumnIndex(PHARMACY_NAME));
            String number = c.getString(c.getColumnIndex(PHARMACY_NUMBER));

            if (name != null) {
                fields.put(PHARMACY_NAME, c.getString(c.getColumnIndex(PHARMACY_NAME)));
            }
            if (name != null) {
                fields.put(PHARMACY_NUMBER, String.valueOf(c.getInt(c.getColumnIndex(PHARMACY_NUMBER))));
            }

            return fields;
        }

        c.close();
        close();

        return null;
    }

    /**
     * Inserts a dummy user
     */
    public void insertUser() {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, "Sean Cronin");
        values.put(COLUMN_LOCATION, "Cork");
        values.put(COLUMN_AGE, 32);
        values.put(COLUMN_DATE_OF_BIRTH, "20/01/1985");
        values.put(COLUMN_CONDITION, "Suffered cardiac arrest during summer 2014");
        values.put(COLUMN_EMAIL, "user@hearthealth.com");
        values.put(PATIENT_NUMBER, "PT1234567");
        values.put(PHARMACY_NUMBER, 0214321567);
        values.put(PHARMACY_NAME, "O'Sullivan's");
        values.put(COLUMN_PASSWORD, "password");

        open();

        long insert = db.insert(TABLE_NAME, null, values);

        if (insert != -1) {
            Log.d(TAG, "OK");
        } else  {
            Log.d(TAG, "FAILED");
        }

        close();

    }
}
