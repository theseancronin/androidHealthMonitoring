package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 26/01/2017.
 */

public class RecognizedActivity {

    private int hours;
    private int minutes;
    private int seconds;
    private int type;
    private String date;

    public RecognizedActivity(int hours, int minutes, final int type, final String date) {
        this.hours = hours;
        this.minutes = minutes;
        this.type = type;
        this.date = date;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
