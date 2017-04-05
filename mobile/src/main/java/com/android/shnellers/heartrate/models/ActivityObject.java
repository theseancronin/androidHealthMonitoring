package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 05/04/2017.
 */

public class ActivityObject {

    long start;
    long finish;
    int minutes;
    String type;

    public ActivityObject() {

    }

    public ActivityObject(final long start, final long finish, final int minutes) {
        this.start = start;
        this.finish = finish;
        this.minutes = minutes;
    }

    public ActivityObject(final long start, final long finish, final int minutes, final String type) {
        this.start = start;
        this.finish = finish;
        this.minutes = minutes;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public long getStart() {
        return start;
    }

    public void setStart(final long start) {
        this.start = start;
    }

    public long getFinish() {
        return finish;
    }

    public void setFinish(final long finish) {
        this.finish = finish;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(final int minutes) {
        this.minutes = minutes;
    }
}
