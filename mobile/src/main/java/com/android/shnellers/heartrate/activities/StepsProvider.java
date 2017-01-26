package com.android.shnellers.heartrate.activities;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Sean on 22/01/2017.
 */

public class StepsProvider extends ContentProvider {

    public static final String AUTHORITY = "com.android.shnellers.heartrate.provider";
    public static final String SCHEME = "content://";

    public static final String STEPS = SCHEME + AUTHORITY +"/steps";
    public static final Uri URI_STEPS = Uri.parse(STEPS);
    public static final String STEPS_BASE = STEPS + "/";

    public StepsProvider(){}

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
