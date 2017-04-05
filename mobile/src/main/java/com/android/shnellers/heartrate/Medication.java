package com.android.shnellers.heartrate;

import java.io.Serializable;

/**
 * Created by Sean on 30/10/2016.
 */

public class Medication implements Serializable {

    private String mName;
    private int mFrequency, mStrength, id;

    public Medication (final int id, final String name,
                       final int strength,
                       final int frequency) {
        setId(id);
        setName(name);
        setFrequency(frequency);
        setStrength(strength);
    }

    public Medication(final String name, final int frequency, final int strength) {
        mName = name;
        mFrequency = frequency;
        mStrength = strength;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getFrequency() {
        return mFrequency;
    }

    public void setFrequency(int frequency) {
        mFrequency = frequency;
    }

    public int getStrength() {
        return mStrength;
    }

    public void setStrength(int strength) {
        mStrength = strength;
    }
}
