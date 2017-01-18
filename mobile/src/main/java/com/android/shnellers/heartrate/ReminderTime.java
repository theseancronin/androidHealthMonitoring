package com.android.shnellers.heartrate;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sean on 08/01/2017.
 */

public class ReminderTime implements Parcelable {

    private int hour, minute, active, id;

    private String type;

    public ReminderTime(int hour, int minute, int active, int id, String type) {
        setHour(hour);
        setMinute(minute);
        setActive(active);
        setId(id);
        setType(type);
    }

    protected ReminderTime(Parcel in) {
        hour = in.readInt();
        minute = in.readInt();
        active = in.readInt();
        id = in.readInt();
        type = in.readString();
    }

    public static final Creator<ReminderTime> CREATOR = new Creator<ReminderTime>() {
        @Override
        public ReminderTime createFromParcel(Parcel in) {
            return new ReminderTime(in);
        }

        @Override
        public ReminderTime[] newArray(int size) {
            return new ReminderTime[size];
        }
    };

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(hour);
        parcel.writeInt(minute);
        parcel.writeInt(active);
        parcel.writeInt(id);
        parcel.writeString(type);
    }


}
