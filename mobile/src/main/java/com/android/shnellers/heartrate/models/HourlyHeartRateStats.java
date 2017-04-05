package com.android.shnellers.heartrate.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Sean on 28/03/2017.
 */

public class HourlyHeartRateStats implements Parcelable {

    private int max;
    private int min;
    private int average;
    private int numberOfHeartRates;
    private int hour;
    private int sum;
    private long dateTime;
    private ArrayList<Integer> heartRates;

    public HourlyHeartRateStats() {
    }

    public HourlyHeartRateStats(int hour) {
        setHour(hour);
        max = Integer.MIN_VALUE;
        min = Integer.MAX_VALUE;
        sum = 0;
        average = 0;
        numberOfHeartRates = 0;
        dateTime = 0;
        heartRates = new ArrayList<Integer>();
    }

    public HourlyHeartRateStats(int max, int min, int average) {
        this.max = max;
        this.min = min;
        this.average = average;

    }

    protected HourlyHeartRateStats(Parcel in) {
        max = in.readInt();
        min = in.readInt();
        average = in.readInt();
        numberOfHeartRates = in.readInt();
        hour = in.readInt();
        sum = in.readInt();
        dateTime = in.readLong();
        heartRates = new ArrayList<>();

    }

    public static final Creator<HourlyHeartRateStats> CREATOR = new Creator<HourlyHeartRateStats>() {
        @Override
        public HourlyHeartRateStats createFromParcel(Parcel in) {
            return new HourlyHeartRateStats(in);
        }

        @Override
        public HourlyHeartRateStats[] newArray(int size) {
            return new HourlyHeartRateStats[size];
        }
    };

    public void addHeartRate(final int hr) {
        heartRates.add(hr);

    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public ArrayList<Integer> getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(ArrayList<Integer> heartRates) {
        this.heartRates = heartRates;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getNumberOfHeartRates() {
        return numberOfHeartRates;
    }

    public void setNumberOfHeartRates(int numberOfHeartRates) {
        this.numberOfHeartRates = numberOfHeartRates;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getAverage() {
        if (getSum() > 0 && getNumberOfHeartRates() > 0) {
            return getSum() / getNumberOfHeartRates();
        }
        return average;
    }

    public void setAverage(int average) {

        this.average = average;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(final long dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "HourlyHeartRateStats{" +
                "max=" + max +
                ", min=" + min +
                ", average=" + average +
                ", numberOfHeartRates=" + numberOfHeartRates +
                ", hour=" + hour +
                ", sum=" + sum +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(max);
        dest.writeInt(min);
        dest.writeInt(average);
        dest.writeInt(numberOfHeartRates);
        dest.writeInt(hour);
        dest.writeInt(sum);
        dest.writeLong(dateTime);
    }
}
