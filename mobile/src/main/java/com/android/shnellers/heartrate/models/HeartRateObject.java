package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 13/02/2017.
 */

public class HeartRateObject {

    private int id;
    private int heartRate;

    private long dateTime;

    private String status;
    private String date;
    private String type;

    public HeartRateObject(int id, int heartRate, long dateTime,
                           String status, String date, String type) {
        this.id = id;
        this.heartRate = heartRate;
        this.dateTime = dateTime;
        this.status = status;
        this.date = date;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public long getDateTime() {
        return dateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }
}
