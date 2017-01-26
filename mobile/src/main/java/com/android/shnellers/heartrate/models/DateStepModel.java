package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 18/01/2017.
 */

public class DateStepModel {
    private String mDate;
    private int mStepCount;

    public DateStepModel(int stepCount, String date) {
        mDate = date;
        mStepCount = stepCount;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public int getStepCount() {
        return mStepCount;
    }

    public void setStepCount(int stepCount) {
        mStepCount = stepCount;
    }
}
