package com.android.shnellers.heartrate.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.android.shnellers.heartrate.models.HourlyHeartRateStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.android.shnellers.heartrate.Constants.Const.ALL;
import static com.android.shnellers.heartrate.Constants.Const.RESTING;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TABLE_NAME;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TYPE;
import static com.android.shnellers.heartrate.servicealarms.HourlyAnalysis.AVERAGE_RESTING_BPM;
import static com.android.shnellers.heartrate.servicealarms.HourlyAnalysis.RESTING_BPM_ABOVE_100;

/**
 * Created by Sean on 28/03/2017.
 */

public class HourMonitor extends IntentService {

    private static final String TAG = "HourMonitor";
    public static final String HOUR_OF_HIGHEST_AVG = "Hour of Highest Avg: ";
    public static final String TIME_OF_HIGHEST_AVG = "Time of Highest Avg: ";
    public static final String CLICK_FOR_MORE_DETAILS = "Click for more details...";
    public static final String YOUR_DAILY_RESTING_RATE_STATS = "Your Daily Resting Rate Stats";

    /**
     *
     */
    public HourMonitor() {
        super(TAG);

    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        determineHourlyAverages();
        Log.d(TAG, "HOUR MONITOR");
    }

    /**
     *
     */
    private void determineHourlyAverages() {
        HashMap<Integer, HourlyHeartRateStats> heartRates = initializeHourMap();

        SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

        String query = getQuery(ALL);

        Cursor c = db.rawQuery(query, null);

        Calendar calendar = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(System.currentTimeMillis());
        c2.add(Calendar.DAY_OF_YEAR, -1);
        c2.set(Calendar.HOUR_OF_DAY, 21);
        c2.set(Calendar.MINUTE, 0);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {

                // Initialize calendar with time of record and get the hour
                calendar.setTimeInMillis(c.getInt(c.getColumnIndex(DATE_TIME_COLUMN)));
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

                // Get the heart rate and hourly stats object
                int hr = c.getInt(c.getColumnIndex(BPM_COLUMN));
                HourlyHeartRateStats stats = heartRates.get(hourOfDay);

                stats.getHeartRates().add(c.getColumnIndex(BPM_COLUMN));

                // Set the max hr for the hour
                if (hr > stats.getMax()) {
                    stats.setMax(hr);
                }

                // Set the minimum hr for the hour
                if (hr < stats.getMin() && hr != 0) {
                    stats.setMin(hr);
                }

                // Add hr to the total sum of hrs for this hour
                stats.setSum(stats.getSum() + hr);

                // Increment the number of heart rates detected
                stats.setNumberOfHeartRates(stats.getNumberOfHeartRates() + 1);
            }
        }

        c.close();
        db.close();

        if (!heartRates.isEmpty()) {
            calculateHourlyReadings(heartRates);
        }
    }

    /**
     *
     * @param heartRates
     */
    private void calculateHourlyReadings(HashMap<Integer, HourlyHeartRateStats> heartRates) {

        Log.d(TAG, "calculateHourlyReadings: ");
        // We go through each hourly stats and work out the average for that hour
        for (Map.Entry<Integer, HourlyHeartRateStats> entry : heartRates.entrySet()) {

            // Get the object and list of heart rates
            HourlyHeartRateStats stats = entry.getValue();
            ArrayList<Integer> hrs = entry.getValue().getHeartRates();

            // Only if hrs have been recorded for that hour we work out the heart rate,
            // otherwise we would get a divide by zero.
            if (!hrs.isEmpty()) {
                stats.setAverage(stats.getSum() / hrs.size());
            }
            // Log.d(TAG, stats.toString());
        }

        dailyNotification(heartRates);
    }

    /**
     * When anomalies are detected the user is notified.
     *
     * @param stats
     */
    private void dailyNotification(final HashMap<Integer, HourlyHeartRateStats> stats) {

        Log.d(TAG, "dailyNotification: ");
        // Check if we have high heart rates
        int heartRatesOver100 = calculateHighHeartRates(stats);
        HourlyHeartRateStats highestHour = getHourOfHighestAvg(stats);
        String timeOfHighestAvg = getPartOfDayWithHighesAverage(stats);

        String startHour = "";
        String endHour = "";

        if (highestHour.getHour() < 10) {
            startHour = "0" + String.valueOf(highestHour.getHour()) + ":00";
        } else {
            startHour = String.valueOf(highestHour.getHour()) + ":00";
        }

        int nextHour = highestHour.getHour() + 1;
        if (nextHour < 10) {
            endHour = "0" + String.valueOf(nextHour) + ":00";
        } else {
            endHour = String.valueOf(nextHour) + ":00";
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart_white)
                .setContentTitle(YOUR_DAILY_RESTING_RATE_STATS);

        // We initialize an inbox style, so that we have a large notification layout.
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        // Sett notification title and add relevant lines
        inboxStyle.setBigContentTitle(YOUR_DAILY_RESTING_RATE_STATS);
        inboxStyle.addLine(HOUR_OF_HIGHEST_AVG + startHour + " - " + endHour);
        inboxStyle.addLine(AVERAGE_RESTING_BPM + String.valueOf(highestHour.getAverage()));
        inboxStyle.addLine(TIME_OF_HIGHEST_AVG + timeOfHighestAvg);
        inboxStyle.addLine(RESTING_BPM_ABOVE_100 + String.valueOf(heartRatesOver100));
        inboxStyle.setSummaryText(CLICK_FOR_MORE_DETAILS);

        // Add the large notification to the builder
        mBuilder.setStyle(inboxStyle);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notify the user
        mNotificationManager.notify(111, mBuilder.build());

    }

    /**
     * Determines what part of the day had the highest average.
     *
     * @param stats
     * @return Morning, Afternoon, Evening, Night
     */
    private String getPartOfDayWithHighesAverage(final HashMap<Integer, HourlyHeartRateStats> stats) {

        int morningAvg = 0;
        int morningSum = 0;
        int morningCount = 0;
        int morningOver100 = 0;

        int afternoonAvg = 0;
        int afternoonSum = 0;
        int afternoonCount = 0;
        int afternoonOver100 = 0;

        int eveningAvg = 0;
        int eveningSum = 0;
        int eveningCount = 0;
        int eveningOver100 = 0;

        int nightAvg = 0;
        int nightSum = 0;
        int nightCount = 0;
        int nightOver100 = 0;

        for (Map.Entry<Integer, HourlyHeartRateStats> entry : stats.entrySet()) {

            HourlyHeartRateStats stat = entry.getValue();
            int hour = stat.getHour();

            if (!stat.getHeartRates().isEmpty()) {

               if (hour >= 5 && hour < 12) {

                   morningSum += stat.getAverage();
                   morningOver100 = getHeartRatesOver100(stat.getHeartRates());
                   morningCount++;

               } else if (hour >= 12 && hour < 17) {

                   afternoonSum += stat.getAverage();
                   afternoonOver100 = getHeartRatesOver100(stat.getHeartRates());
                   afternoonCount++;

                } else if (hour >= 17 && hour < 21) {

                   eveningSum += stat.getAverage();
                   eveningOver100 = getHeartRatesOver100(stat.getHeartRates());
                   eveningCount++;

               } else if (hour >= 21 && hour < 5) {

                   nightSum += stat.getAverage();
                   nightOver100 = getHeartRatesOver100(stat.getHeartRates());
                   nightCount++;
               }
            }
        }

        String worst = getWorst(morningAvg, afternoonAvg, eveningAvg, nightAvg);getString(R.string.morning);

        return worst;
    }

    /**
     *
     * @param morningAvg
     * @param afternoonAvg
     * @param eveningAvg
     * @param nightAvg
     * @return
     */
    private String getWorst(final int morningAvg, final int afternoonAvg, final int eveningAvg, final int nightAvg) {
        int max = morningAvg;

        String worst = getString(R.string.morning);

        int[] vals = {afternoonAvg, eveningAvg, nightAvg};

        for (int i = 0; i < vals.length; i++) {
            if (vals[i] > max) {
                max = vals[i];
                if (i == 0) {
                    worst = getString(R.string.afternoon);
                } else if (i == 1) {
                    worst = getString(R.string.evening);
                } else {
                    worst = getString(R.string.nighttime
                    );
                }
            }
        }
        return worst;
    }

    /**
     *
     * @param heartRates
     * @return
     */
    private int getHeartRatesOver100(final ArrayList<Integer> heartRates) {
        int count = 0;
        // Count the heart rates over 100 beats per minute
        for (Integer hr : heartRates) {
            if (hr >= 100) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the hour with the highest average.
     *
     * @param stats
     * @return
     */
    private HourlyHeartRateStats getHourOfHighestAvg(final HashMap<Integer, HourlyHeartRateStats> stats) {

        int highestHour = 0;
        int highestAvg = 0;

        HourlyHeartRateStats highest = null;

        for (Map.Entry<Integer, HourlyHeartRateStats> entry : stats.entrySet()) {

            HourlyHeartRateStats stat = entry.getValue();

            if (!stat.getHeartRates().isEmpty()) {

                // Simply determines the highest average of all heart rates.
                if (stat.getAverage() > highestAvg) {
                    highestAvg = stat.getAverage();
                    highestHour = stat.getHour();
                    highest = stat;
                }
            }
        }

        return highest;
    }

    /**
     *
     * @param stats
     * @return
     */
    private int calculateHighHeartRates(final HashMap<Integer, HourlyHeartRateStats> stats) {
        int count = 0;

        for (Map.Entry<Integer, HourlyHeartRateStats> entry : stats.entrySet()) {
            ArrayList<Integer> hrs = entry.getValue().getHeartRates();

            if (!hrs.isEmpty()) {
                for (Integer hr : hrs) {
                    if (hr >= 100) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Get the query type to search for records.
     *
     * @param type all or today
     * @return
     */
    private String getQuery(final String type) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (type.equals(ALL)) {
            return "SELECT * FROM " + TABLE_NAME +
                    " WHERE " + TYPE + " = '" + RESTING + "'" +
                    " AND " + DATE + " BETWEEN '" + format.format(yesterday.getTimeInMillis()) +
                    "' AND '" + format.format(today.getTimeInMillis()) + "';";
        } else {
            return "SELECT * FROM " + TABLE_NAME +
                    " WHERE " + TYPE + " = '" + RESTING + "'" +
                    " AND " + DATE + " BETWEEN '" + format.format(yesterday.getTimeInMillis()) +
                    "' AND '" + format.format(today.getTimeInMillis()) + "';";
        }
    }

    /**
     *
     *
     * @return
     */
    private HashMap<Integer, HourlyHeartRateStats> initializeHourMap() {

        HashMap<Integer, HourlyHeartRateStats> map = new HashMap<>();

        for (int hour = 0; hour <= 23; hour++) {
            map.put(hour, new HourlyHeartRateStats(hour));
            map.get(hour).setHeartRates(new ArrayList<Integer>());
        }

        return map;
    }
}
