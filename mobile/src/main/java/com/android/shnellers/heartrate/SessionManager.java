package com.android.shnellers.heartrate;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Sean on 24/10/2016.
 */

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AndroidHiveLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String USER_EMAIL = "email";
    private static final String USER_NAME = "name";
    private static final String USER_AGE = "age";
    private static final String USER_CONDITION = "condition";
    private static final String USER_WEIGHT = "weight";
    private static final String USER_LOCATION = "location";
    private static final String USER_PHONE_NUMBER = "phone_number";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    /**
     * Store details of logged user.
     *
     * @param user
     */
    public void storeUserData(final User user) {
        editor.putString(USER_NAME, user.get_name());
        editor.putString(USER_EMAIL, user.get_email());
        editor.putString(USER_AGE, user.get_age());
        editor.putString(USER_CONDITION, user.get_condition());
        editor.putString(USER_WEIGHT, user.get_weight());
        editor.putString(USER_LOCATION, user.get_location());
        editor.putString(USER_PHONE_NUMBER, user.get_phoneNumber());
        editor.commit();
    }

    public User getLoggedInUser() {
        String name = pref.getString(USER_NAME, "");
        String email = pref.getString(USER_EMAIL, "");
        String age = pref.getString(USER_AGE, "");
        String condition = pref.getString(USER_CONDITION, "");
        String weight = pref.getString(USER_WEIGHT, "");
        String location = pref.getString(USER_LOCATION, "");
        String phoneNumber = pref.getString(USER_PHONE_NUMBER, "");

        Log.i("EMAIL", email);


        User user = new User(name, age, email, condition,
                             weight, location, phoneNumber);
        return user;
    }
}
