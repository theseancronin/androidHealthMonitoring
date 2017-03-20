package com.android.shnellers.heartrate.diary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.diary.DiaryDatabase;
import com.android.shnellers.heartrate.models.DiaryEntry;

import java.util.ArrayList;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntries extends Fragment {

    private static final String TAG = "DiaryEntries";

    private DiaryDatabase mDiaryDatabase;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDiaryDatabase = new DiaryDatabase(getActivity());


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.diary_entries_layout, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.diary_recycler);

        displayDiaryEntries();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void displayDiaryEntries() {

        ArrayList<DiaryEntry> diaryEntries = mDiaryDatabase.getDiaryEntries();

        DiaryEntryAdapter adapter = new DiaryEntryAdapter(diaryEntries, getActivity());
        mRecyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);

    }
}
