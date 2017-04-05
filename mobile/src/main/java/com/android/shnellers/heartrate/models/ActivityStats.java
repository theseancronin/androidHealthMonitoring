package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 19/03/2017.
 */

public class ActivityStats {

    private int minutes;
    private int steps;
    private String activityName;
    private int calories;
    private int avgHeartRate;
    private double distance;

    private long dateTime;

    public ActivityStats(String activityName) {
        this.activityName = activityName;
    }

    public ActivityStats(int minutes, int steps, String activityName,
                         int calories, int avgHeartRate, double distance,
                         long dateTime) {
        this.distance = distance;
        this.minutes = minutes;
        this.steps = steps;
        this.activityName = activityName;
        this.calories = calories;
        this.avgHeartRate = avgHeartRate;
        this.dateTime = dateTime;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(final long dateTime) {
        this.dateTime = dateTime;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setAvgHeartRate(int avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSteps() {
        return steps;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getCalories() {
        return calories;
    }

    public int getAvgHeartRate() {
        return avgHeartRate;
    }
}
