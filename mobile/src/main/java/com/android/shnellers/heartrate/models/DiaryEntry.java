package com.android.shnellers.heartrate.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntry implements Parcelable {

    private String entry;
    private String month;
    private String weekday;
    private String time;

    private String day;

    public DiaryEntry (final String entry, final String month,
                       final String weekday, final String time, final String day) {
        setEntry(entry);
        setMonth(month);
        setWeekday(weekday);
        setTime(time);
        setDay(day);
    }

    protected DiaryEntry(Parcel in) {
        entry = in.readString();
        month = in.readString();
        weekday = in.readString();
        time = in.readString();
        day = in.readString();
    }

    public static final Creator<DiaryEntry> CREATOR = new Creator<DiaryEntry>() {
        @Override
        public DiaryEntry createFromParcel(Parcel in) {
            return new DiaryEntry(in);
        }

        @Override
        public DiaryEntry[] newArray(int size) {
            return new DiaryEntry[size];
        }
    };

    public String getEntry() {
        return entry;
    }

    private void setEntry(String entry) {
        this.entry = entry;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entry);
        dest.writeString(month);
        dest.writeString(weekday);
        dest.writeString(time);
        dest.writeString(day);
    }
}
