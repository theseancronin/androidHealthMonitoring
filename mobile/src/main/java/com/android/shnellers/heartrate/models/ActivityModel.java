package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 29/01/2017.
 */

public class ActivityModel {

    private String mType;
    private String mDate;

    private int mID;
    private int mStartTime;
    private int mEndTime;
    private int mTimeTaken;
    private int mCaloriesBurned;
    private int mSteps;
    private int mFinished;
    private int mDistance;

    public ActivityModel () {

    }

    public ActivityModel(final int id, final String type, final int startTime, final int finished,
                         final String date) {
        setID(id);
        setType(type);
        setStartTime(startTime);
        setFinished(finished);
        setDate(date);
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public void setStartTime(int startTime) {
        mStartTime = startTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public void setEndTime(int endTime) {
        mEndTime = endTime;
    }

    public int getTimeTaken() {
        return mTimeTaken;
    }

    public void setTimeTaken(int timeTaken) {
        mTimeTaken = timeTaken;
    }

    public int getCaloriesBurned() {
        return mCaloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        mCaloriesBurned = caloriesBurned;
    }

    public int getSteps() {
        return mSteps;
    }

    public void setSteps(int steps) {
        mSteps = steps;
    }

    public int getFinished() {
        return mFinished;
    }

    public void setFinished(int finished) {
        mFinished = finished;
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }
}
