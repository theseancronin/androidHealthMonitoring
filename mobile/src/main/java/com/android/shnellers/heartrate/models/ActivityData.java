package com.android.shnellers.heartrate.models;

/**
 * This class is used to hold and activities length and average heart
 * rate for the duration of the activity.
 */
public class ActivityData {

    private int time;
    private int averageHeartRate;

    /**
     * Simple constructor.
     *
     * @param time
     * @param averageHeartRate
     */
    public ActivityData(int time, int averageHeartRate) {
        this.time = time;
        this.averageHeartRate = averageHeartRate;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(int averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }
}
