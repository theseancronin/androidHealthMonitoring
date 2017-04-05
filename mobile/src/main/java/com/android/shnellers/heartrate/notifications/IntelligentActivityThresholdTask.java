package com.android.shnellers.heartrate.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.ActivityRecognitionDBHelper;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.android.shnellers.heartrate.models.ActivityData;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.ACTIVITY_NUMBER;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.MINUTES;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TABLE_RECOGNITION;
import static com.android.shnellers.heartrate.database.ActivityContract.ActivityEntries.TIME_MILLIS;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TABLE_NAME;
import static com.android.shnellers.heartrate.servicealarms.HourlyAnalysis.LOOK_AFTER_HEALTH;

/**
 * Created by Sean on 27/03/2017.
 */

public class IntelligentActivityThresholdTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "IntelligentActivity";
    public static final String ACTIVITY_UPDATE = "Activity Update";

    private ActivityRecognitionDBHelper mRecognitionDBHelper;

    private static final String LESS_THAN_EQUAL_MINUTES = "<=";
    private static final String MORE_THAN_MINUTES = ">";

    private Context mContext;

    private int mActivity;

    private int mMinutes;



    public IntelligentActivityThresholdTask(Context context, int activity, int minutes) {
        mContext = context;
        mRecognitionDBHelper = new ActivityRecognitionDBHelper(mContext);
        mActivity = activity;
        mMinutes = minutes;

        //Log.d(TAG, "ACTIVITY: " + String.valueOf(mActivity));
    }

    @Override
    protected Void doInBackground(Void... params) {
        List<ActivityData> activityDatas = getRecentRecords(mMinutes, LESS_THAN_EQUAL_MINUTES);
        List<ActivityData> laterTimes = getRecentRecords(mMinutes, MORE_THAN_MINUTES);

        long start = System.currentTimeMillis();

        Calendar sd = Calendar.getInstance();

        Calendar ed = Calendar.getInstance();
        ed.add(Calendar.MINUTE, -mMinutes);
        int currentAverage = calculateAvgHeartRate(start, ed.getTimeInMillis());

        HashMap<String, Integer> threshold = Calculations.targetHeartRate(getThreshold(), 32);

        int sum = 0;
        int avg = 0;
        int count = 0;
        ActivityData best = null;

        for (ActivityData activity : activityDatas) {
            if (activity.getAverageHeartRate() != -1 && activity.getAverageHeartRate() > 0) {
                sum += activity.getAverageHeartRate();
                count++;
            }

        }


        ActivityData bestTime = null;
        int maxMins = 0;
        int min = Integer.MIN_VALUE;

        ArrayList<ActivityData> hrs = new ArrayList<>();

        for (ActivityData activity : laterTimes) {
            if (activity.getAverageHeartRate() != -1) {
                if (activity.getTime() > maxMins && activity.getAverageHeartRate() <= 131
                        && activity.getAverageHeartRate() > 0) {
                    maxMins = activity.getTime();
                    bestTime = activity;
                }
            }

        }



        if (activityDatas.size() > 0) {
            avg = sum / count;
        }

        if (bestTime != null) {
            Log.d(TAG, "Best Time: " + String.valueOf(bestTime.getTime()) +
                    " BEST AVG: " + String.valueOf(bestTime.getAverageHeartRate()));

            sendNotification(bestTime);
        }

        Log.d(TAG, "PAST AVG: " + String.valueOf(avg) + " CURRENT AVG: " + String.valueOf(currentAverage));

        return null;
    }

    private List<ActivityData> getRecentRecords(final int activityMinutes, final String sign) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        List<ActivityData> activityDatas = new ArrayList<>();

        SQLiteDatabase db = mRecognitionDBHelper.getReadableDatabase();

        // Initialize first calendar to go back two weeks
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DATE, -14);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);

        Calendar sd = Calendar.getInstance();

        // Second calendar to hold time up to the end of the previous day
        Calendar ed = Calendar.getInstance();
        ed.set(Calendar.HOUR_OF_DAY, 0);
        ed.set(Calendar.MINUTE, 0);

        // Select all resting rates between the given dates for specific times and minutes
        String query = "SELECT * FROM " + TABLE_RECOGNITION +
                " WHERE " + ACTIVITY_NUMBER + " = " + mActivity +
                " AND " + MINUTES + sign + activityMinutes +
                " AND " + TIME_MILLIS + " BETWEEN " + c1.getTimeInMillis() +
                " AND " + ed.getTimeInMillis() + ";";

        Cursor cursor = db.rawQuery(query, null);

        Log.d(TAG, "getRecentRecords: COUNT: " + String.valueOf(cursor.getCount()));

        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                // Get the time and minutes of the the activity
                long timeMillis = cursor.getLong(cursor.getColumnIndex(TIME_MILLIS));
                int minutes = cursor.getInt(cursor.getColumnIndex(MINUTES));
                long endTime = (60 * 1000) + timeMillis;

                // Calculate the average heart rate in that time
                int avgHeartRate = calculateAvgHeartRate(timeMillis, endTime);

                //Log.d(TAG, "AVG: " + String.valueOf(avgHeartRate));
//                Log.d(TAG,
//                        "MINS: " + String.valueOf(minutes) +
//                        " AVERAGE: " + String.valueOf(avgHeartRate) +
//                        " DATE: " + cursor.getString(cursor.getColumnIndex(DATE)) +
//                        " FORMAT DATE: " + format.format(timeMillis));
                activityDatas.add(new ActivityData(minutes, avgHeartRate));

            }
        }

        cursor.close();
        db.close();
        return activityDatas;
    }



    /**
     * Calculate the average of heart rates in the time the user was walking.
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private int calculateAvgHeartRate(long startTime, long endTime) {

        int count = 0;
        int avg = -1;
        int sum = 0;

        SQLiteDatabase db = new HeartRateDBHelper(mContext).getReadableDatabase();

        Calendar ca = Calendar.getInstance();
        ca.setTimeInMillis(startTime);
        ca.add(Calendar.MINUTE, 10);

        String query = "SELECT * FROM " + TABLE_NAME +
                       " WHERE " + DATE_TIME_COLUMN + " BETWEEN " + startTime +
                       " AND " + endTime + ";";
        Cursor cursor = db.rawQuery(query, null);

        // Loop through all of the heart rates and accumulate the sum and count.
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                sum += cursor.getInt(cursor.getColumnIndex(BPM_COLUMN));
                count++;
            }
        }

        // Calculate the average heart rate
        if (count > 0) {
            avg = sum / count;
        }

        cursor.close();
        db.close();

        return avg;
    }

    /**
     * Notify the user of problems.
     *
     * @param data
     */
    private void sendNotification(final ActivityData data) {

        Log.d(TAG, "sendNotification: ");
        int icon = mActivity == DetectedActivity.WALKING ? R.drawable.ic_walk_white
                : mActivity == DetectedActivity.RUNNING ? R.drawable.ic_run_white : R.drawable.ic_cycling_white;

        // This string is presented to the user informing them of how long more they should
        // perform the current activity for, in order to have a healthy heart rate.
        String text = "Based on you previous records you can " + Calculations.getType(mActivity) +
                        " for another " + String.valueOf(data.getTime()) + " minutes and maintain average heart rate of " +
                            String.valueOf(data.getAverageHeartRate()) + " bpm.";

        // Set up the standard builder notification for when the notification is small
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(icon)
                .setContentTitle(ACTIVITY_UPDATE)
                .setContentText(text);

        // This big notification will reveal the entire text
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.setBigContentTitle(ACTIVITY_UPDATE);
        bigStyle.bigText(text);
        bigStyle.setSummaryText(LOOK_AFTER_HEALTH);

//            // We initialize an inbox style, so that we have a large notification layout.
//            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//
//            // Sett notification title and add relevant lines
//            inboxStyle.setBigContentTitle(HIGH_RESTING_RATES_DETECTED);
//            inboxStyle.addLine(HIGHEST_RESTING_BPM + String.valueOf(stats.getMax()));
//            inboxStyle.addLine(AVERAGE_RESTING_BPM + String.valueOf(stats.getAverage()));
//            inboxStyle.addLine(RESTING_BPM_ABOVE_100 + String.valueOf(heartRatesOver100));
//            inboxStyle.setSummaryText(ASSISTANCE_SUMMARY);
//
//            // Add the large notification to the builder
//            mBuilder.setStyle(inboxStyle);

        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notify the user
        mNotificationManager.notify(111, mBuilder.build());

    }

    private int getThreshold() {

        int level = 0;

        if (mActivity == DetectedActivity.WALKING) {
            level = 1;
        } else if (mActivity == DetectedActivity.RUNNING || mActivity == DetectedActivity.ON_BICYCLE) {
            level = 2;
        }

        return level;
    }
}
