package com.android.shnellers.heartrate.helpers;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sean on 13/02/2017.
 */

public class DateHelper {

    private static String TAG = "DATE_HELPER";

    private DateHelper(){}

    public static ArrayList<String> getLast7DaysAsDate() {

        ArrayList<String> dates = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_YEAR, -7);
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            String date = format.format(calendar.getTime());
           // String date = new SimpleDateFormat(
            //        "dd", Locale.UK).format(calendar.getTime()) + " " +
             //       new SimpleDateFormat("MMM", Locale.UK).format(calendar.getTime());
            dates.add(
                    format.format(calendar.getTime()));
            Log.d(TAG, "displayLast7Days: DATE: " + date);
        }
        int count = 0;

        while (count < 3) {
            String date = getStringDateFromCalendar(calendar);
            dates.add(format.format(calendar.getTime()));
            Log.d(TAG, "displayLast7Days: DATE: " + date);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            count++;
        }


        return dates;

    }

    /**
     * Returns all dates in string format between two supplied dates.
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ParseException
     */
    public static ArrayList<String> getDatesBetween(String startDate, String endDate,
                                                    final SimpleDateFormat dateFormat) throws ParseException {
        ArrayList<String> dates = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yy", Locale.UK);
        SimpleDateFormat returnFormat = new SimpleDateFormat("dd MMM yy", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        Date start = convertStringToDateTime(startDate, dateFormat);
        Date end = convertStringToDateTime(endDate, dateFormat);

        calendar.setTimeInMillis(start.getTime());

        while (calendar.getTime().before(end)) {

            String date = getStringDateFromCalendar(calendar);
            dates.add(returnFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Log.d(TAG, "displayLast7Days: DATE: " + date);
        }


        dates.add(returnFormat.format(calendar.getTime()));
        int count = 0;

//        while (count < 3) {
//            String date = getStringDateFromCalendar(calendar);
//            dates.add(returnFormat.format(calendar.getTime()));
//            Log.d(TAG, "displayLast7Days: DATE: " + date);
//            calendar.add(Calendar.DAY_OF_YEAR, 1);
//            count++;
//        }

       // Log.d(TAG, "displayLast7Days: DATE: " + format.format(calendar.getTimeInMillis()));
        return dates;
    }

    public static Date convertStringToDateTime (
            final String timeStr, final SimpleDateFormat format) throws ParseException {
        //DateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        Date startDate = format.parse(timeStr);
        return startDate;

    }

    public static String getStringDateFromCalendar(Calendar calendar) {
        return new SimpleDateFormat("dd MMM yy", Locale.UK).format(calendar.getTime());
    }
}
