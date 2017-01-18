package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 15/01/2017.
 */

public class WeightDBContract {

    private WeightDBContract (){}

    public static class WeightEntries implements BaseColumns {
        public static final String TABLE_NAME = "weight";
        public static final String ID_COLUMN = "id";
        public static final String WEIGHT_COLUMN = "weight";
        public static final String DATE_TIME_COLUMN = "date_time";
    }

}
