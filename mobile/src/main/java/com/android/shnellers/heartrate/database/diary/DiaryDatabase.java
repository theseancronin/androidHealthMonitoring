package com.android.shnellers.heartrate.database.diary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.android.shnellers.heartrate.models.DiaryEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryDatabase {

    public static final String DAY_OF_MONTH = "day_of_month";
    public static final String WEEKDAY = "weekday";
    public static final String MONTH_OF_YEAR = "month_of_year";
    public static final String TIME_OF_DAY = "time_of_day";
    private DiaryDBHelper mDiaryDBHelper;

    private final Context mContext;

    public DiaryDatabase(Context context) {
        mContext = context;
        mDiaryDBHelper = new DiaryDBHelper(context);
    }

    /**
     * Inserts a new entry into the diary log table.
     * @param values
     */
    public void insertEntry(ContentValues values) {
        try {

            String v = values.getAsString(DiaryContract.DiaryEntry.ENTRY);

            Log.d(TAG, "insertEntry: " + v);
            SQLiteDatabase db = mDiaryDBHelper.getWritableDatabase();
            db.insert("diary_logs", null, values);
            db.close();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<DiaryEntry> getDiaryEntries() {

        ArrayList<DiaryEntry> entries = new ArrayList<>();

        String query =
                "SELECT * FROM " + DiaryContract.DiaryEntry.TABLE_NAME + ";";

        SQLiteDatabase db = mDiaryDBHelper.getReadableDatabase();

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {

                HashMap<String, String> entryDetails = getEntryDetails(
                        c.getLong(c.getColumnIndex(DiaryContract.DiaryEntry.DATE_TIME)));

                DiaryEntry entry = new DiaryEntry(
                        c.getString(c.getColumnIndex(DiaryContract.DiaryEntry.ENTRY)),
                        entryDetails.get(MONTH_OF_YEAR),
                        entryDetails.get(WEEKDAY),
                        entryDetails.get(TIME_OF_DAY),
                        entryDetails.get(DAY_OF_MONTH)
                        );
                entries.add(entry);

            } while (c.moveToNext());

            return entries;
        }

        db.close();

        return null;
    }

    private HashMap<String, String> getEntryDetails(long timeLong) {

        HashMap<String, String> map = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String weekday = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
        String month = new SimpleDateFormat("MMMM", Locale.UK).format(calendar.getTime());
        String time = new SimpleDateFormat("HH:mm", Locale.UK).format(calendar.getTime());

        map.put(DAY_OF_MONTH, String.valueOf(day));
        map.put(WEEKDAY, weekday);
        map.put(MONTH_OF_YEAR, month);
        map.put(TIME_OF_DAY, time);

        return map;
    }

    private String getDayOfWeek(int dayOfWeek) {

        String dow = "";

        switch (dayOfWeek) {
            case 1:
                dow =  "Monday";
                break;
            case 2:
                dow = "Tuesday";
                break;
            case 3:
                dow = "Wednesday";
                break;
            case 4:
                dow = "Thursday";
                break;
            case 5:
                dow = "Friday";
                break;
            case 6:
                dow = "Saturday";
                break;
            case 7:
                dow = "Sunday";
                break;

        }

        return dow;
    }

    private String getDateTime(long aLong) {

        String dateFormat = "dd-MM-yyyy";

        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.UK);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(aLong);

        return format.format(calendar.getTime());
    }

    private String getTime(long aLong) {
        String timeFormat = "HH:mm";

        SimpleDateFormat format = new SimpleDateFormat(timeFormat, Locale.UK);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(aLong);

        return format.format(calendar.getTime());
    }

    public void getEntryLogs() {

    }
}
