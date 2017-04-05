package com.android.shnellers.heartrate.servicealarms;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.shnellers.heartrate.Calculations;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;
import com.android.shnellers.heartrate.database.diary.DiaryContract;
import com.android.shnellers.heartrate.database.diary.DiaryDBHelper;
import com.android.shnellers.heartrate.models.HourlyHeartRateStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.android.shnellers.heartrate.Constants.Const.RESTING;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.BPM_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.DATE_TIME_COLUMN;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TABLE_NAME;
import static com.android.shnellers.heartrate.database.HeartRateContract.Entry.TYPE;

/**
 * Created by Sean on 28/03/2017.
 */

public class HourlyAnalysis extends IntentService {

    public static final String COUNT = "count";
    private static final String TAG = "HourlyAnalysis";
    public static final String HIGH_RESTING_RATES_DETECTED = "High Resting Rates Detected";
    public static final String HIGHEST_RESTING_BPM = "Highest Resting BPM: ";
    public static final String AVERAGE_RESTING_BPM = "Average Resting BPM: ";
    public static final String RESTING_BPM_ABOVE_100 = "Resting BPM Above 100: ";
    public static final String ASSISTANCE_SUMMARY = "Please seek assistance if feeling unwell!";
    public static final String LOOK_AFTER_HEALTH = "Please look after your health!";
    public static final int THRESHOLD = 5;
    public static final String HEART_RATES_OVER_100_IN_THE_LAST_HOUR = " heart rates over 100 in the last hour...";
    public static final String HOW_ARE_YOU_FEELING = "How are you Feeling?";
    public static final String PERCENTAGE = "percentage";

    private int overOneDeviation;
    private int overTwoDeviation;
    private int overThreeDeviation;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HourlyAnalysis(final String name) {
        super(name);
    }

    public HourlyAnalysis() {
        super(TAG);

    }


    @Override
    public void onCreate() {
        super.onCreate();
        overOneDeviation = 0;
        overTwoDeviation = 0;
        overThreeDeviation = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {


//        // Setup the notification
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_heart_white)
//                .setContentTitle("Testing")
//                ;
//        mBuilder.setVibrate(new long[] { 1000, 1000 });
//        mBuilder.setLights(Color.RED, 3000, 3000);
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        mBuilder.setSound(alarmSound);
//
//
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Notify the user
//        mNotificationManager.notify(111, mBuilder.build());
        analyzeLastHour();

    }

    /**
     * Analyses the the last hours resting rates.
     *
     */
    private void analyzeLastHour() {
        Calendar calendar = Calendar.getInstance();
        ArrayList<HourlyHeartRateStats> morningStats;
        
        // Initialize calendar with last hour
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Calendar recordCal = Calendar.getInstance();

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);

        // Get the hour to review
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Collect all heart rates in the same hour over the last two weeks
        ArrayList<HourlyHeartRateStats> twoWeekStats = getTwoWeekHourStats(hour);

        // If the hour is twelve do a deep analysis of the morning data as that's
        // when most heart attacks occur.
        if (hour == 12) {
            morningStats = getMorningHourlyStats(hour);
        }

        HourlyHeartRateStats stats = new HourlyHeartRateStats(hour);
        stats.setHeartRates(new ArrayList<Integer>());
        stats.setDateTime(calendar.getTimeInMillis());

        SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + TYPE + " = '" + RESTING +
                        "' AND " + DATE_TIME_COLUMN + " BETWEEN " + calendar.getTimeInMillis() +
                " AND " + endTime.getTimeInMillis() + ";";
        Cursor cursor = db.rawQuery(query, null);

        Log.d(TAG, "COUNT " + String.valueOf(cursor.getCount()));

        if (cursor.moveToFirst()) {

            while (cursor.moveToNext()) {

                // Set calendar to the records time
                recordCal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DATE_TIME_COLUMN)));

                int rHour = recordCal.get(Calendar.HOUR_OF_DAY);

                // If the record was in the specific hour we append the data to the stats
                // object.
                if (rHour == hour || (hour == 12 && rHour >=6 && rHour <= 12)) {
                    int hr = cursor.getInt(cursor.getColumnIndex(BPM_COLUMN));
                    stats.addHeartRate(hr);

                    // Set the max hr for the hour
                    if (hr > stats.getMax()) {
                        stats.setMax(hr);
                    }

                    // Set the minimum hr for the hour
                    if (hr < stats.getMin() && hr != 0) {
                        stats.setMin(hr);
                    }

                    int sum = stats.getSum();
                    sum += hr;
                    // Add hr to the total sum of hrs for this hour
                    stats.setSum(sum);

                    // Increment the number of heart rates detected
                    stats.setNumberOfHeartRates(stats.getNumberOfHeartRates() + 1);
                }
            }
        }

        cursor.close();
        db.close();

        // If resting rates have been detected, we determine if we should notify the user
        if (stats.getNumberOfHeartRates() > 0) {
            getStandardDeviation(stats);
            notifyAnomalies(stats, twoWeekStats);

        }
        notifyAnomalies(stats, twoWeekStats);
    }

    /**
     * Get the standard deviation of the current heart rates.
     *
     * @param stats
     */
    private void getStandardDeviation(final HourlyHeartRateStats stats) {

        ArrayList<Integer> hrs = stats.getHeartRates();

        int sum = 0;
        int lowSum = 0;

        // This calculates the sum of all the values difference between the MAX (normally average)
        // heart rate, which is then squared.
        for (Integer hr : hrs) {
            sum += Math.pow((double) hr - 100, 2);
            lowSum += Math.pow((double) hr - 40, 2);
        }

        // get the standard deviation.
        if (sum > 0) {
            int sd = (int) Math.sqrt((1.0 / stats.getNumberOfHeartRates()) * sum);
            int lowDeviation = (int) Math.sqrt((1.0 / stats.getNumberOfHeartRates()) * lowSum);

            System.out.println("LOW DEVIATION: " + lowDeviation);

            // Within deviation ranges
            setOverOneDeviationCount(getHeartRatesOverStandardDeviation(stats.getHeartRates(), sd + 100));
            setOverTwoDeviationCount(getHeartRatesOverStandardDeviation(stats.getHeartRates(), (sd * 2) + 100));
            setOverThreeDeviationCount(getHeartRatesOverStandardDeviation(stats.getHeartRates(), (sd * 3) + 100));

        }

    }

    private int getOneDeviationCount() {
        return overOneDeviation;
    }

    private void setOverOneDeviationCount(final int count) {
        overOneDeviation = count;
    }

    private int getOverTwoDeviationCount() {
        return overOneDeviation;
    }

    private void setOverTwoDeviationCount(final int count) {
        overTwoDeviation = count;
    }

    private int getOverThreeDeviationCount() {
        return overThreeDeviation;
    }

    private void setOverThreeDeviationCount(final int count) {
        overThreeDeviation = count;
    }


    /**
     * This method counts the number of heart rates over the number of deviations limit.
     *
     * @param heartRates
     * @param limit
     * @return
     */
    private int getHeartRatesOverStandardDeviation(final ArrayList<Integer> heartRates, final int limit) {
        int count = 0;
        for (Integer hr : heartRates) {
            if (hr > limit) {
                count++;
            }
        }
        return count;
    }

    private ArrayList<HourlyHeartRateStats> getMorningHourlyStats(final int hour) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        ArrayList<HourlyHeartRateStats> stats = new ArrayList<>();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -14);
        start.set(Calendar.HOUR_OF_DAY, hour);

        // Go through all days up until yesterday
        while (start.getTime().before(end.getTime())) {

            // Gets the heart rates for each day and hour
            String query = "SELECT * FROM " + TABLE_NAME +
                    " WHERE " + TYPE + " = '" + RESTING +
                    "' AND " + DATE + " = '" + format.format(start.getTimeInMillis()) + "';";

            SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

            Cursor cursor = db.rawQuery(query, null);

            stats.add(getHourlyHeartStats(cursor, start));

            cursor.close();
            start.add(Calendar.DAY_OF_YEAR, 1);
            start.set(Calendar.HOUR_OF_DAY, hour);
        }



        if (!stats.isEmpty()) {
            return stats;
        }

        return null;
    }

    /**
     * Retrieve the previous two week information within the given hour.
     *
     * @return
     */
    private ArrayList<HourlyHeartRateStats> getTwoWeekHourStats(final int hour) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        ArrayList<HourlyHeartRateStats> stats = new ArrayList<>();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -14);
        start.set(Calendar.HOUR_OF_DAY, hour);

        while (start.getTime().before(end.getTime())) {

            String query = "SELECT * FROM " + TABLE_NAME +
                    " WHERE " + TYPE + " = '" + RESTING +
                    "' AND " + DATE + " = '" + format.format(start.getTimeInMillis()) + "';";

            SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

            Cursor cursor = db.rawQuery(query, null);

            stats.add(getHourlyHeartStats(cursor, start));

            cursor.close();
            start.add(Calendar.DAY_OF_YEAR, 1);
            start.set(Calendar.HOUR_OF_DAY, hour);
        }



        if (!stats.isEmpty()) {
            return stats;
        }

        return null;
    }

    /**
     * Create and return an hourly statistic object.
     *
     * @param cursor
     * @param calendar
     * @return
     */
    private HourlyHeartRateStats getHourlyHeartStats(final Cursor cursor, final Calendar calendar) {

        Calendar recordCal = Calendar.getInstance();

        // Initialize hourly stats object
        HourlyHeartRateStats stats = new HourlyHeartRateStats(calendar.get(Calendar.HOUR_OF_DAY));
        // Initialize a new array list to store the heart rates
        stats.setHeartRates(new ArrayList<Integer>());
        // initialize the time
        stats.setDateTime(calendar.getTimeInMillis());

        // Get the hour of the day
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (cursor.moveToFirst()) {

            while (cursor.moveToNext()) {

                // Set calendar to the records time
                recordCal.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DATE_TIME_COLUMN)));

                int rHour = recordCal.get(Calendar.HOUR_OF_DAY);

                // If the record was in the specific hour we append the data to the stats
                // object.
                if (rHour == hour) {

                    int hr = cursor.getInt(cursor.getColumnIndex(BPM_COLUMN));
                    stats.addHeartRate(hr);

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
        }

        if (stats.getHeartRates().size() > 0) {
            return stats;
        } else {
            return null;
        }

    }

    /**
     * When anomalies are detected the user is notified.
     *
     * @param stats
     * @param twoWeekStats
     */
    private void notifyAnomalies(final HourlyHeartRateStats stats, final ArrayList<HourlyHeartRateStats> twoWeekStats) {

        // Check if we have high heart rates
        int heartRatesOver100 = calculateHighHeartRates(stats, 110);

        // Retrieve data for specific hour over the last two weeks
        HashMap<String, Integer> twoWeeksOver100 = calculateHighRatesOverTwoWeeks(twoWeekStats);

        // Count how many heart rates over 120 beats per minute
        int over120 = calculateHighHeartRates(stats, 130);

        // Count heart rates less than 40
        int under40 = calculateLowHeartRates(stats, 40);

        // Deviation Count

        Log.d(TAG, "1D " + String.valueOf(getOneDeviationCount()) +
                "2D " + String.valueOf(getOverTwoDeviationCount()) + "3D " + String.valueOf(getOverThreeDeviationCount()));

        // Broadcast the values
        Intent intent = new Intent(HourlyAnalysisReceiver.HOURLY_STATS_UPDATED);
        intent.putExtra("stats", stats);
        intent.putParcelableArrayListExtra("two_week_stats", twoWeekStats);
        intent.putIntegerArrayListExtra("heart_rates", stats.getHeartRates());
        getApplicationContext().sendBroadcast(intent);

        // Count the number of diary entries
        int symptomsRecorded = getSymptomsRecorded(stats.getHour());

        // If there have been resting rates over 120 notify the user
        if (over120 > 0)
        {
            notifyExtremeRestingRates(over120, symptomsRecorded);
        }
        // Notify if heart rates over 100 beats per minute.
        else if (heartRatesOver100 > 0) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_heart_white)
                    .setContentTitle(HIGH_RESTING_RATES_DETECTED)
                    .setContentText("Suspiciously high resting rates have been recorded");

            // We initialize an inbox style, so that we have a large notification layout.
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            // Notify the user of data comparison over the last two weeks
            if (twoWeeksOver100 != null) {
                int percentage = Calculations.calculatePercentage(heartRatesOver100, stats.getNumberOfHeartRates());
                notifyUserOfPastInformation(twoWeeksOver100, percentage, heartRatesOver100, symptomsRecorded);
            }

            // Sett notification title and add relevant lines
            inboxStyle.setBigContentTitle(HIGH_RESTING_RATES_DETECTED);
            inboxStyle.addLine(HOW_ARE_YOU_FEELING);
            inboxStyle.addLine(RESTING_BPM_ABOVE_100 + String.valueOf(heartRatesOver100));
            inboxStyle.addLine(HIGHEST_RESTING_BPM + String.valueOf(stats.getMax()));
            inboxStyle.addLine(AVERAGE_RESTING_BPM + String.valueOf(stats.getAverage()));
            inboxStyle.setSummaryText(ASSISTANCE_SUMMARY);

            // Add the large notification to the builder
            mBuilder.setStyle(inboxStyle);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Notify the user
            mNotificationManager.notify(111, mBuilder.build());
       }
    }

    /**
     * Count low heart rates.
     *
     * @param stats
     * @param threshold
     * @return
     */
    private int calculateLowHeartRates(final HourlyHeartRateStats stats, final int threshold) {
        int count = 0;

        ArrayList<Integer> hrs = stats.getHeartRates();

        // Count and return the number of heart rates over the given threshold
        for (Integer hr : hrs) {
            if (hr < threshold) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the number of symptoms recorded in the last two weeks during
     * the given hour.
     *
     * @param hour
     * @return
     */
    private int getSymptomsRecorded(final int hour) {

        Calendar calendar = Calendar.getInstance();

        // initialize the calendar to hold the record date
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_YEAR, -14);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);

        int count = 0;

        SQLiteDatabase db = new DiaryDBHelper(this).getReadableDatabase();

        // Get all diary entries in the last two weeks
        String q = "SELECT * FROM " + DiaryContract.DiaryEntry.TABLE_NAME +
                " WHERE " + DiaryContract.DiaryEntry.DATE_TIME + " >= " +
                start.getTimeInMillis() + ";";

        Cursor c = db.rawQuery(q, null);

        if (c.moveToFirst()) {
            while (c.moveToNext()) {

                // Set the time of the current record
                calendar.setTimeInMillis(c.getLong(c.getColumnIndex(DiaryContract.DiaryEntry.DATE_TIME)));

                // get the hour of the current record
                int currentHour = start.get(Calendar.HOUR_OF_DAY);

                // Determine if the hour is within the current hour being evaluated
                if (currentHour == hour) {
                    count ++;
                }

            }
        }

        c.close();
        db.close();

        return count;
    }

    /**
     * Notifies the user when extremely high heart rates are detected.
     *
     * @param over120
     * @param symptomsRecorded
     */
    private void notifyExtremeRestingRates(final int over120, final int symptomsRecorded) {
        // Setup the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart_white)
                .setContentTitle("Extremely High Resting Heart Rate Detected")
                .setContentText(String.valueOf(over120) + " very high heart rates recorded in the last hour.");

        // Setup the big text notification
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

        // Extended notification text to be shown to the user
        bigStyle.bigText("In the last hour, " + String.valueOf(over120) + " heart rate were recorded that " +
                "were extremely high. If you weren't performing an activity, then please log how your  feeling " +
                "and keep a close watch on your resting heart rate in the coming hours."
        );

        // Summary text for motivation
        bigStyle.setSummaryText(LOOK_AFTER_HEALTH);

        mBuilder.setVibrate(new long[] { 1000, 1000 });
        mBuilder.setLights(Color.RED, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notify the user
        mNotificationManager.notify(111, mBuilder.build());
    }

    /**
     *  @param twoWeeksOver100
     * @param percentage
     * @param symptomsRecorded
     */
    private void notifyUserOfPastInformation(final HashMap<String, Integer> twoWeeksOver100, final int percentage, final int over100, final int symptomsRecorded) {
        // Initialize the builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart_white)
                .setContentTitle(HIGH_RESTING_RATES_DETECTED);

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

        String symTxt;

        // create a string telling the user about the symptoms that were recorded in this time,
        // otherwise they are informed to log any symptoms they maybe feeling.
        if (symptomsRecorded > 0) {
            symTxt = "In the the past, you recorded " + String.valueOf(symptomsRecorded) + " symptoms " +
                    "during this hour.";
        } else {
            symTxt = "If you are feeling any unusual symptoms, please log them in your diary.";
        }

        bigStyle.bigText("How are you feeling? " + String.valueOf(over100) + " resting heart rates over 100 beats per minute " +
                "were recorded in the last hour. That's " + String.valueOf(percentage) + "% of the resting rates recorded. " +
                "Over the last two weeks " + String.valueOf(twoWeeksOver100.get(PERCENTAGE)) + "% of the heart rates were recorded over 100 beats per minute," +
                "during the same time. " + symTxt
        );
        bigStyle.setSummaryText(LOOK_AFTER_HEALTH);


        mBuilder.setStyle(bigStyle);

        mBuilder.setVibrate(new long[] { 1000, 1000 });
        mBuilder.setLights(Color.RED, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notify the user
        mNotificationManager.notify(112, mBuilder.build());
    }

    /**
     * Calculate the hourly rates for the lest two weeks.
     *
     * @param twoWeekStats
     * @return
     */
    private HashMap<String, Integer> calculateHighRatesOverTwoWeeks(final List<HourlyHeartRateStats> twoWeekStats) {

        HashMap<String, Integer> map = new HashMap<>();

        int count = 0;

        int totalHeartRates = 0;

        for (HourlyHeartRateStats entry : twoWeekStats) {
            if (entry != null) {
                count += calculateHighHeartRates(entry, 100);
                totalHeartRates += entry.getHeartRates().size();
            }
        }

        if (count > 0) {
            int percentage = Calculations.calculatePercentage(count, totalHeartRates);
            map.put(COUNT, count);
            map.put(PERCENTAGE, percentage);
            return map;
        }

        return null;
    }

    /**
     *
     * @param stats
     * @param threshold
     * @return
     */
    private int calculateHighHeartRates(final HourlyHeartRateStats stats, final int threshold) {
        int count = 0;

        ArrayList<Integer> hrs = stats.getHeartRates();

        // Count and return the number of heart rates over the given threshold
        for (Integer hr : hrs) {
            if (hr >= threshold) {
                count++;
            }
        }

        return count;
    }
}
