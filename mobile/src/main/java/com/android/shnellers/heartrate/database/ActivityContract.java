package com.android.shnellers.heartrate.database;

import android.provider.BaseColumns;

/**
 * Created by Sean on 15/01/2017.
 */

public class ActivityContract {

    private ActivityContract (){}

    public static class ActivityEntries implements BaseColumns {

        public static final String TABLE_NAME = "activities";
        public static final String ID_COLUMN = "id";
        public static final String TYPE_COLUMN = "type";
        public static final String DISTANCE_TRAVELLED_COLUMN = "distance";
        public static final String START_TIME_COLUMN = "start_time";
        public static final String END_TIME_COLUMN = "end_time";
        public static final String TIME_TAKEN_COLUMN = "time_taken";
        public static final String DATE_TIME_COLUMN = "date_time";
        public static final String CALORIES_BURNED_COLUMN = "calories_burned";
        public static final String STEPS_COLUMN = "steps";

        public static final String TABLE_RECOGNITION = "activities_recognised";
        public static final String MINUTES_DETECTED = "minutes";
        public static final String SECONDS = "seconds";

        public static final String FINISHED_COLUMN = "finished";

        public static final String TIME_MAP = "timeMap";
        public static final String HOURS = "hours";
        public static final String MINUTES = "minutes";

        public static final String ACTIVE = "active";
        public static final String ACTIVITY_NUMBER = "activity_number";
    }

}
