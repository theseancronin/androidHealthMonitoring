package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 09/01/2017.
 */

public final class RemindersContract {

    private RemindersContract(){}

    // Inner class that defines table properties
    public static class Columns implements BaseColumns {
        public static final String TABLE_NAME = "reminders";
        public static final String ID_COLUMN = "id";
        public static final String HOUR_COLUMN = "hour";
        public static final String MINUTE_COLUMN = "minute";
        public static final String TYPE_COLUMN = "type";
        public static final String DAYS_COLUMN = "days";
        public static final String ACTIVE_COLUMN = "active";

        public static final int ALARM_ON = 1;
        public static final int ALARM_OFF = 0;
    }

}
