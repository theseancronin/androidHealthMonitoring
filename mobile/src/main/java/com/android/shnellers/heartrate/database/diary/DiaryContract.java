package com.android.shnellers.heartrate.database.diary;

import android.provider.BaseColumns;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryContract {

    private DiaryContract () {}

    public class DiaryEntry implements BaseColumns {

        public static final String TABLE_NAME = "diary_logs";
        public static final String ID = "id";
        public static final String ENTRY = "entry";
        public static final String DATE_TIME = "date_time";

    }

}
