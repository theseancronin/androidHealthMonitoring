package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 19/01/2017.
 */

public class StepsDBContract {

    private StepsDBContract(){}

    public class Entry implements BaseColumns {

        public static final String TABLE_NAME = "StepsSummary";
        public static final String ID = "id";
        public static final String STEPS_COUNT = "steps_count";
        public static final String CREATION_DATE = "creation_date";


    }

}
