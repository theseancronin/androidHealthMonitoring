package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 22/03/2017.
 */

public class TimeModel {

    private int minute;
    private int sum;
    private int count;
    private int avg;

    public TimeModel() {
    }

    public TimeModel(int minute, int sum, int count, int avg) {
        this.minute = minute;
        this.sum = sum;
        this.count = count;
        this.avg = avg;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAvg() {
        return avg;
    }

    public void setAvg(int avg) {
        this.avg = avg;
    }
}
