package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 09/02/2017.
 *
 * Weight class for logged weights.
 *
 */
public class WeightObject {

    private double weight;

    private String type;

    private long dateTime;

    private String warning;

    public WeightObject(double weight, String type, long dateTime) {
        this.weight = weight;
        this.type = type;
        this.dateTime = dateTime;
    }

    public double getWeight() {
        return weight;
    }

    public String getType() {
        return type;
    }

    public long getDateTime() {
        return dateTime;
    }

}
