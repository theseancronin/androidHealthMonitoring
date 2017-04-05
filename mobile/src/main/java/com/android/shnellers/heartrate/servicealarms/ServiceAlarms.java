package com.android.shnellers.heartrate.servicealarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.shnellers.heartrate.notifications.HourMonitor;

import java.util.Calendar;

import static android.content.ContentValues.TAG;

/**
 * Created by Sean on 28/03/2017.
 */

public class ServiceAlarms {

    /**
     *
     * @param context
     */
    public static void setHourlyAnalysisCheck(final Context context) {
        Log.d("ALARM", "setHourlyAnalysisCheck: ");
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the calendar to start checking at next hour.
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        Log.d(TAG, "setHourlyAnalysisCheck: " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));

        // Set the intent to start the hourly analysis service
        Intent intent = new Intent(context, HourlyAnalysis.class);
        //intent.putExtra("calendar", calendar.getTimeInMillis());

        // Set the pending service to start
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, intent, 0);

        // Set the repeating alarm
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR, pendingIntent);

    }

    /**
     *
     * @param context
     */
    public static void setDailyAnalysisCheck(final Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the calendar to start checking at next hour.
        calendar.add(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);

        // Set the intent to start the hourly analysis service
        Intent intent = new Intent(context, HourMonitor.class);
        intent.putExtra("calendar", calendar.getTimeInMillis());

        // Set the pending service to start
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, intent, 0);

        // Set the repeating alarm
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

    }
}
