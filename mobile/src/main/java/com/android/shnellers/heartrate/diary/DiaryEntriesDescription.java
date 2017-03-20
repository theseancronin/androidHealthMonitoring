package com.android.shnellers.heartrate.diary;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.diary.DiaryDatabase;
import com.android.shnellers.heartrate.models.DiaryEntry;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntriesDescription extends Activity {

    public static final String KEY_POSITION = "position";

    private int currentPosition = -1;

    private ArrayList<DiaryEntry> mEntryLogs;

    private DiaryDatabase mDiaryDatabase;

    private DiaryEntry mDiaryEntry;

    @BindView(R.id.entry_text)
    TextView mEntryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_entry);
        mDiaryDatabase = new DiaryDatabase(this);

        mEntryLogs = mDiaryDatabase.getDiaryEntries();

        ButterKnife.bind(this);

        mDiaryEntry = getIntent().getParcelableExtra("diaryEntryClicked");

        if (mDiaryEntry != null) {
            mEntryText.setText(mDiaryEntry.getEntry());
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
