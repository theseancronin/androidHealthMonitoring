package com.android.shnellers.heartrate.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.diary.DiaryDBHelper;
import com.android.shnellers.heartrate.models.DiaryEntry;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.PublishSubject;

import static com.android.shnellers.heartrate.database.diary.DiaryContract.DiaryEntry.ID;
import static com.android.shnellers.heartrate.database.diary.DiaryContract.DiaryEntry.TABLE_NAME;

/**
 * Created by Sean on 05/02/2017.
 */

public class DiaryEntryAdapter extends RecyclerView.Adapter<DiaryEntryAdapter.EntryHolder> {

    public static final String DELETE_DIARY_ENTRY = "Delete Diary Entry";
    public static final String CANCEL = "Cancel";
    private ArrayList<DiaryEntry> mDiaryEntries;

    private PublishSubject<String> onClickSubject = PublishSubject.create();

    private final View.OnClickListener mOnClickListener = new DiaryOnClickListener();

    private int mCurrentIndex;

    private FragmentActivity mFragmentActivity;
    private ViewGroup mParent;

    private View mView;

    public DiaryEntryAdapter(final ArrayList<DiaryEntry> diaryEntries, FragmentActivity activity) {
        mDiaryEntries = diaryEntries;
        mFragmentActivity = activity;
    }

    @Override
    public EntryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_entry_container, parent, false);

        return new EntryHolder(mView);
    }

    @Override
    public void onBindViewHolder(EntryHolder holder, final int position) {

        final String entryTxt = mDiaryEntries.get(position).getEntry();

        final DiaryEntry diaryEntry = mDiaryEntries.get(position);
        mCurrentIndex = position;

        holder.mEntryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDiaryEntry(diaryEntry);
            }
        });

        holder.mEntryCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(mView.getRootView().getContext());
                alert.setTitle(DELETE_DIARY_ENTRY);
                alert.setMessage("Are you sure you want to delete the diary entry?");
                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeEntry(diaryEntry.getId());
                        mDiaryEntries.remove(position);
                        notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert.show();
                return true;
            }
        });

        holder.mSpeechText.setText(mDiaryEntries.get(position).getEntry());
        holder.mDay.setText(mDiaryEntries.get(position).getDay());
        holder.mMonth.setText(mDiaryEntries.get(position).getMonth());
        holder.mWeekday.setText(mDiaryEntries.get(position).getWeekday());
        holder.mTime.setText(mDiaryEntries.get(position).getTime());

    }

    private void removeEntry(final int id) throws SQLiteException {

        SQLiteDatabase db = new DiaryDBHelper(mFragmentActivity).getReadableDatabase();

        db.delete(TABLE_NAME, ID + "=" + id, null);

        db.close();

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
