package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 19/10/2016.
 */

public final class UserContract {

    /**
     * We make the constructor private to prevent someone
     * accidentally instantiating it.
     */
    private UserContract () {}

    /**
     * This is an inner class that defines the table contents.
     */
    public static class UserEntry implements BaseColumns {
        public static final String _ID = "id";
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";

        public static final String COLUMN_CONDITION = "condition";
        public static final String COLUMN_LOGGED_IN = "logged_in";
    }

}
