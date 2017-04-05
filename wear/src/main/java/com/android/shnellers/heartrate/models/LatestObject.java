package com.android.shnellers.heartrate.models;

/**
 * Created by Sean on 05/04/2017.
 */

public class LatestObject {

    private int value;
    private String type;

    public LatestObject(final int value, final String type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
