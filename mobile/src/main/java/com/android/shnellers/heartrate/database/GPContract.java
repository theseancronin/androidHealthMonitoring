package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 27/11/2016.
 */

public class GPContract {

    private GPContract() {}

    public static class GPEntry implements BaseColumns {
        public static final String TABLE_NAME = "gp_details";
        public static final String _ID = "id";
        public static final String COLUMN_GP_NAME = "name";
        public static final String COLUMN_GP_PRACTICE = "practice";
        public static final String COLUMN_GP_TELEPHONE = "telephone";
    }

}
