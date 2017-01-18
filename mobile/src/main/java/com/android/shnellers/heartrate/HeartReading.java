package com.android.shnellers.heartrate;

/**
 * Created by Sean on 15/01/2017.
 */

public class HeartReading {

    private int mBPM;

    private long mDateTime;

    public HeartReading(int BPM, long dateTime) {
        mBPM = BPM;
        mDateTime = dateTime;
    }

    public long getDateTime() {
        return mDateTime;
    }

    public int getBPM() {
        return mBPM;
    }



}
