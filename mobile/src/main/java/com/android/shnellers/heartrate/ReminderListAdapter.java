package com.android.shnellers.heartrate;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.shnellers.heartrate.database.RemindersDBHelper;
import com.android.shnellers.heartrate.database.RemindersDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.android.shnellers.heartrate.database.RemindersContract.Columns.ID_COLUMN;
import static com.android.shnellers.heartrate.database.RemindersContract.Columns.TABLE_NAME;
import static com.android.shnellers.heartrate.diary.DiaryEntryAdapter.CANCEL;
import static com.android.shnellers.heartrate.diary.DiaryEntryAdapter.DELETE_DIARY_ENTRY;

/**
 * Created by Sean on 08/01/2017.
 */

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ReminderHolder> {

    private static final String TAG = "RemindersAdapter";

    private ArrayList<ReminderTime> reminders;
    private final PublishSubject<ReminderTime> onClickSubject;
    private RemindersDatabase db;

    private View mView;

    /**#
     * Initialize the view for the holder.
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_card_view, parent, false);

        db = new RemindersDatabase(mView.getContext());

        return new ReminderHolder(mView);
    }

    /**
     * A simple constructor.
     *
     * @param reminders
     */
    public ReminderListAdapter(final ArrayList<ReminderTime> reminders) {
        this.reminders = reminders;
        onClickSubject = PublishSubject.create();
    }

    /**
     * Bind the data to the view holder.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ReminderHolder holder, final int position) {

        String time;
        String hrStr;
        String minStr;

        final ReminderTime reminder = reminders.get(position);

        int hour = reminders.get(position).getHour();

        int minute = reminders.get(position).getMinute();

        if (hour < 10) {
            hrStr = "0" + String.valueOf(hour);
        } else {
            hrStr = String.valueOf(hour);
        }

        if (minute < 10) {
            minStr = "0" + String.valueOf(minute);
        } else {
            minStr = String.valueOf(minute);
        }
        holder.time.setText(hrStr + ":" + minStr);

        holder.typeView.setText(reminders.get(position).getType());

        Log.d("ReminderAdapter active", Integer.toString(reminders.get(position).getActive()));

        boolean active = (reminders.get(position).getActive() != 0);

        holder.mSwitchCompat.setChecked(active);



       // Log.d("Clicked Reminder", Boolean.toString(holder.mSwitchCompat.isChecked()));

//        holder.mCardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: YOU CLICKED ME");
//            }
//        });

        holder.mRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(mView.getRootView().getContext());
                alert.setTitle(DELETE_DIARY_ENTRY);
                alert.setMessage("Are you sure you want to delete the diary entry?");
                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeEntry(reminder.getId());
                        reminders.remove(position);
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
    }

    public Observable<ReminderTime> onClickPosition() {
        return null;
    }

    private void removeEntry(final int id) throws SQLiteException {

        SQLiteDatabase db = new RemindersDBHelper(mView.getContext()).getReadableDatabase();

        db.delete(TABLE_NAME, ID_COLUMN + "=" + id, null);

        db.close();

    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ReminderHolder extends RecyclerView.ViewHolder {

        public TextView typeView;
        public TextView time;
        public CardView mCardView;
        public SwitchCompat mSwitchCompat;

        @BindView(R.id.reminder_layout)
        RelativeLayout mRelativeLayout;

        public ReminderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

         //   mCardView = (CardView)  itemView.findViewById(R.id.card_view);
            time = (TextView) itemView.findViewById(R.id.time_text);
            mSwitchCompat = (SwitchCompat) itemView.findViewById(R.id.on_off_toggle);
            typeView = (TextView) itemView.findViewById(R.id.reminder_type_txt);
        }
    }
}
