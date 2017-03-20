package com.android.shnellers.heartrate;

import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import com.android.shnellers.heartrate.database.ActivityContract;
import com.google.android.gms.location.DetectedActivity;

import java.text.DecimalFormat;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by Sean on 27/01/2017.
 */

public class Calculations {

    public static int convertPixelsToInt(final int px) {

       return (int) TypedValue.applyDimension(
               TypedValue.COMPLEX_UNIT_DIP,
               px,
               Resources.getSystem().getDisplayMetrics());
    }

    /**
     * Converts time to an estimated kilometer.
     *
     * @param activity
     * @param minutes
     * @return
     */
    public static double convertTimeToKM(final int activity, final int minutes) {


        double km = 0;
        double miles = 0;
        double milesAMinute = 0;

        if (activity == DetectedActivity.WALKING) {

            milesAMinute = 1 / 15D;

            miles = milesAMinute * minutes;
            km = 1.6 * miles;

        } else if (activity == DetectedActivity.ON_BICYCLE || activity == DetectedActivity.RUNNING) {
            milesAMinute = 2 / 15D;
            miles = milesAMinute * minutes;
            km = 1.6 * miles;
        }

        if (km > 0) {
            DecimalFormat oneDigit = new DecimalFormat("#,##0.0");//format to 1 decimal place

            Log.d(TAG, "convertTimeToKM: " + String.valueOf(Double.valueOf(oneDigit.format(km))));

            return Double.valueOf(oneDigit.format(km));
        } else {
            return 0;
        }
    }

    /**
     * Works out the length of the activity line depending on the minutes
     *
     * @param minutes
     * @return
     */
    public static int calculateLineWidth(final int minutes) {
        return (int) ((220 / 60) * minutes);
    }

    /**
     * Calculates the number of calories burned for a man
     */
    public static int caloriesBurnedMen(final int age, final int weight,
                                        final int heartRate, final int time) {
       // double burned =  ((age * 0.2017) - (weight * 0.09036) +
       //         (heartRate * 0.6309) - 55.0969) * time / 4.184;
        double burned = ((age * 0.2017) - (weight * 0.09036) + (heartRate * 0.6309) - 55.0969)
                * (time / 4.184);
        Log.d(TAG, "caloriesBurnedMen: " + String.valueOf(burned));


        return (int) Math.abs(burned);
    }

    /**
     * Calculates the number of calories burned for a woman
     */
    public static int caloriesBurnedWomen(final int age, final int weight,
                                        final int heartRate, final int time) {
        // double burned =  ((age * 0.2017) - (weight * 0.09036) +
        //         (heartRate * 0.6309) - 55.0969) * time / 4.184;
        double burned = ((age * 0.074) - (weight * 0.05741) + (heartRate * 0.4472) - 55.0969)
                * (time / 4.184);
        Log.d(TAG, "caloriesBurnedMen: " + String.valueOf(burned));
        return (int) burned;
    }

    /**
     * Converts a time in milliseconds to hours, minutes and seconds.
     *
     * @param timeInSeconds
     * @return
     */
    public static HashMap<String, Integer> convertMillisecondsToTime(final long timeInSeconds) {

        HashMap<String, Integer> timeMap = new HashMap<>();

        int hours = (int) (timeInSeconds / 3600000);
        int minutes = (int) (timeInSeconds - hours * 3600000) / 60000;
        int seconds = (int) (timeInSeconds - hours * 3600000 - minutes * 60000) / 1000;

        timeMap.put(ActivityContract.ActivityEntries.HOURS, hours);
        timeMap.put(ActivityContract.ActivityEntries.MINUTES, minutes);
        timeMap.put(ActivityContract.ActivityEntries.SECONDS, seconds);

        return timeMap;

    }

    public void convertMinutesToTime(final int minutes) {

    }

    /**
     * Determines the target heart rate for the user.
     *
     * @param activityLevel
     * @param age
     * @return
     */
    public HashMap<String, Integer> targetHeartRate(int activityLevel, int age) {

        int maxHeartRate = 220 - age;
        int low = 0;
        int high = 0;

        HashMap<String, Integer> targets = new HashMap<>();

        if (isModerateIntensity(activityLevel)) {
            low = (int) Math.round(maxHeartRate * .5);
            high = (int) Math.round(maxHeartRate * .7);
        } else if (isVigerousIntensity(activityLevel)) {
            low = (int) Math.round(maxHeartRate * .7);
            high = (int) Math.round(maxHeartRate * .85);
        } else {

        }

        targets.put(Constants.Const.MAX, maxHeartRate);
        targets.put(Constants.Const.LOW, low);
        targets.put(Constants.Const.HIGH, high);

        return targets;

    }

    private boolean isVigerousIntensity(int activityLevel) {
        return activityLevel == 2;
    }

    private boolean isModerateIntensity(int activityLevel) {
        return activityLevel == 1;
    }

    /**
     * Calculates the percentage for a given value and sum.
     *
     * @param value
     * @param sum
     * @return
     */
    public static int calculatePercentage(final int value, final int sum) {

        double v = (double) value;
        double s = (double) sum;

        double percentage = (v / s) * 100;

        return (int) percentage;
    }
}
