package com.android.shnellers.heartrate.diary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.shnellers.heartrate.R;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntriesContainer extends FragmentActivity {

    private static final String TAG = System.getProperties().getClass().getSimpleName();
    public static final String DIARY_ENTRY_ADAPTER = "DiaryEntryAdapter";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_fragment_container);

        Log.d(TAG, "onCreate: DIARY ENTRIES");

        if (savedInstanceState == null) {
            if (findViewById(R.id.diary_fragment_container) != null) {

                setDiaryRecyclerList();

            }
        }

    }

    private void setDiaryRecyclerList() {
        DiaryEntries diaryEntries = new DiaryEntries();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.diary_fragment_container, diaryEntries, DIARY_ENTRY_ADAPTER)
                .commit();
    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();
    }
}
