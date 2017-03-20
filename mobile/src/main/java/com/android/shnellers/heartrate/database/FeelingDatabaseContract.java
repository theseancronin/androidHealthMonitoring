package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 12/02/2017.
 */

public class FeelingDatabaseContract {

    private FeelingDatabaseContract(){}

    public static class FeelingConsts implements BaseColumns {

        public static final String TABLE_NAME = "feelings";
        public static final String ID_COLUMN = "id";
        public static final String TYPE_COLUMN = "type";
        public static final String DATE_TIME = "date_time";

    }

}
