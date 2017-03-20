package com.android.shnellers.heartrate.database.diary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryDBHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "diary.db";
    private static final int DB_VERSION = 3;

    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + DiaryContract.DiaryEntry.TABLE_NAME + " (" +
            DiaryContract.DiaryEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DiaryContract.DiaryEntry.ENTRY + " TEXT, " +
            DiaryContract.DiaryEntry.DATE_TIME + " INTEGER);";


    public DiaryDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DiaryContract.DiaryEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
