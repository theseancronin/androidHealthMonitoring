package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 19/10/2016.
 */

public final class KinContract {

    /**
     * We make the constructor private to prevent someone
     * accidentally instantiating it.
     */
    private KinContract() {}

    /**
     * This is an inner class that defines the table contents.
     */
    public static class KinEntry implements BaseColumns {
        public static final String _ID = "id";
        public static final String COLUMN_KIN_NAME = "name";
        public static final String COLUMN_KIN_RELATIONSHIP = "relationship";
        public static final String COLUMN_KIN_TELEPHONE= "telephone";

    }

}
