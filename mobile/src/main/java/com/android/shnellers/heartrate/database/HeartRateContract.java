package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 15/01/2017.
 */

public class HeartRateContract {

    private HeartRateContract(){}

    public static class Entry implements BaseColumns {

        public static final String TABLE_NAME = "heart_rate";
        public static final String ID_COLUMN = "id";
        public static final String BPM_COLUMN = "bpm";
        public static final String DATE_TIME_COLUMN = "date_time";
    }
}
