package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 19/10/2016.
 */

public final class MedicationContract {

    /**
     * We make the constructor private to prevent someone
     * accidentally instantiating it.
     */
    private MedicationContract() {}

    /**
     * This is an inner class that defines the table contents.
     */
    public static class MedicationEntry implements BaseColumns {
        public static final String _ID = "id";
        public static final String USER_ID = "user_id";
        public static final String MEDICATION_ID = "medication_id";
        public static final String TABLE_NAME = "medication";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_STRENGTH = "strength";
        public static final String COLUMN_FREQUENCY = "frequency";
    }

}
