package com.android.shnellers.heartrate.weight;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.WeightDBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.DATE;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.TABLE_NAME;
import static com.android.shnellers.heartrate.database.WeightDBContract.WeightEntries.WEIGHT_COLUMN;
import static com.android.shnellers.heartrate.servicealarms.HourlyAnalysis.LOOK_AFTER_HEALTH;

/**
 * Created by Sean on 04/04/2017.
 */
public class WeightAnalysis extends IntentService {

    private static final String TAG = "WeightAnalysis";
    public static final String FIFTH_DAY = "FifthDay";
    public static final String THIRD_DAY = "thirdDay";
    public static final String TODAY = "today";
    public static final String SEVEN_DAY = "Seven Day";
    public static final String YESTERDAY = "Yesterday";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WeightAnalysis(final String name) {
        super(name);
    }

    /**
     *
     */
    public WeightAnalysis() {
        super(TAG);
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {

        if (intent != null) {
            int id = intent.getIntExtra("id", -1);

            if (id == 145) {
                checkWeightIsLogged();
            } else if (id == 541) {
                analyseWeight();
            }


        }
    }

    /**
     *
     */
    private void checkWeightIsLogged() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        SQLiteDatabase db = new WeightDBHelper(this).getReadableDatabase();

        // Get weight logged for today
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + DATE + " = SELECT(DATETIME('now'));";

        Cursor c = db.rawQuery(query, null);

        if (c.getCount() == 0) {
            notifyWeightNotLogged();
        }

        c.close();
        db.close();
    }

    private void notifyWeightNotLogged() {

        // Setup the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart_white)
                .setContentTitle("Weight Not Logged")
                .setContentText("There is no record of todays weight.");

        // Set the notification vibrate
        mBuilder.setVibrate(new long[] { 1000, 1000 });
        mBuilder.setLights(Color.RED, 3000, 3000);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        // Intent to inflate when notification clicked
        Intent resultIntent = new Intent(this, WeightView.class);

        // Pending intent to hold our intent
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        // Set the notification click event
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notify the user
        mNotificationManager.notify(111, mBuilder.build());


    }

    /**
     *
     */
    private void analyseWeight() {
        HashMap<String, Float> weightDayMap = getWeights();

        float todaysWeight = weightDayMap.get(TODAY);
        float yesterday = weightDayMap.get(YESTERDAY);
        float sevenDay = weightDayMap.get(SEVEN_DAY);

        float yesterdayChange = 0;
        float weeklyChange = 0;

        // First check if todays weight was logged so we can compare
        if (todaysWeight != -1) {

            // Check if yesterdays weight was logged
            if (yesterday != -1) {
                // Get the difference in weight since yesterday
                yesterdayChange = todaysWeight - yesterday;

                // Notify the user if weight change is excessive
                if (yesterdayChange >= 2.0) {
                    notifyHighWeight(yesterdayChange, "in the last 24 hours");
                }

            }

            // Check if weight recorded at start of week
            if (sevenDay != -1){
                // Get weight change over the last seven days
                weeklyChange = todaysWeight - sevenDay;

                // Notify the user if weight change is excessive
                if (weeklyChange >= 2.2) {
                    notifyHighWeight(weeklyChange, "in the last week");

                }
            }

        }

    }

    /**
     * Notify the user of high weight.
     *
     * @param weightChange
     * @param daysString
     */
    private void notifyHighWeight(final float weightChange, final String daysString) {
        // Setup the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_heart_white)
                .setContentTitle("High Weight Change")
                .setContentText("Your weight has increases by " + String.valueOf(weightChange) +
                " in the last " + daysString);

        // Setup the big text notification
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();

        // Extended notification text to be shown to the user
        bigStyle.bigText("Your weight has increases by " + String.valueOf(weightChange) +
                " " + daysString + ". This an unusual increase and could be the sign " +
                        "of an underlying condition. If you are feeling unwell, then please seek " +
                        "medical advice."
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
        mNotificationManager.notify(146, mBuilder.build());
    }

    /**
     *
     * @return
     */
    private HashMap<String, Float> getWeights() {

        HashMap<String, Float> map = new HashMap<>();

        Calendar day7 = Calendar.getInstance();
        day7.add(Calendar.DAY_OF_YEAR, -7);

        Calendar yest = Calendar.getInstance();
        yest.add(Calendar.DAY_OF_YEAR, -1);

        Calendar today = Calendar.getInstance();

        float d7Value = getWeightOnDay(day7);
        float yestValue = getWeightOnDay(yest);
        float todaysWeight = getWeightOnDay(today);

        map.put(SEVEN_DAY, d7Value);
        map.put(YESTERDAY, yestValue);
        map.put(TODAY, todaysWeight);

        return map;
    }

    /**
     *
     * @param time
     * @return
     */
    private float getWeightOnDay(final Calendar time) {

        float weight = -1;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        SQLiteDatabase db = new WeightDBHelper(this).getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + DATE + " = " + format.format(time.getTimeInMillis());

        Cursor c = db.rawQuery(query, null);

        if (c.moveToLast()) {
            weight = c.getInt(c.getColumnIndex(WEIGHT_COLUMN));
        }

        c.close();
        db.close();

        return weight;
    }
}
