package com.android.shnellers.heartrate.diary;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.DiaryEntry;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.EntryHolder> {

    private ArrayList<DiaryEntry> mDiaryEntries;

    private PublishSubject<String> onClickSubject = PublishSubject.create();

    private final View.OnClickListener mOnClickListener = new DiaryOnClickListener();

    private int mCurrentIndex;

    private FragmentActivity mFragmentActivity;

    public DiaryEntryAdapter(final ArrayList<DiaryEntry> diaryEntries, FragmentActivity activity) {
        mDiaryEntries = diaryEntries;
        mFragmentActivity = activity;
    }

    @Override
    public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View entryView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_entry_container, parent, false);

        return new EntryHolder(entryView);
    }

    @Override
    public void onBindViewHolder(EntryHolder holder, int position) {

        final String entryTxt = mDiaryEntries.get(position).getEntry();

        final DiaryEntry diaryEntry = mDiaryEntries.get(position);
        mCurrentIndex = position;

        holder.mEntryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDiaryEntry(diaryEntry);
            }
        });

        holder.mSpeechText.setText(mDiaryEntries.get(position).getEntry());
        holder.mDay.setText(mDiaryEntries.get(position).getDay());
        holder.mMonth.setText(mDiaryEntries.get(position).getMonth());
        holder.mWeekday.setText(mDiaryEntries.get(position).getWeekday());
        holder.mTime.setText(mDiaryEntries.get(position).getTime());

    }

    private void openDiaryEntry(DiaryEntry diaryEntry) {

        DiaryEntriesDescription description = new DiaryEntriesDescription();

        Intent intent = new Intent(mFragmentActivity, DiaryEntriesDescription.class);
        intent.putExtra("diaryEntryClicked", diaryEntry);
        mFragmentActivity.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return mDiaryEntries.size();
    }


    public static class EntryHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.entry_card)
        CardView mEntryCard;

        @BindView(R.id.speech_txt_portion)
        TextView mSpeechText;

        @BindView(R.id.entry_day)
        TextView mDay;

        @BindView(R.id.entry_month)
        TextView mMonth;

        @BindView(R.id.entry_weekday)
        TextView mWeekday;

        @BindView(R.id.entry_time)
        TextView mTime;

        public EntryHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
