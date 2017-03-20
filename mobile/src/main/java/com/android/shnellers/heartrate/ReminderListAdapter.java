package com.android.shnellers.heartrate;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.database.RemindersDatabase;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Sean on 08/01/2017.
 */

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ReminderHolder> {

    private static final String TAG = "RemindersAdapter";

    private ArrayList<ReminderTime> reminders;
    private final PublishSubject<ReminderTime> onClickSubject;
    private RemindersDatabase db;


    /**#
     * Initialize the view for the holder.
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_card_view, parent, false);

        db = new RemindersDatabase(itemView.getContext());

        return new ReminderHolder(itemView);
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

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: YOU CLICKED ME");
            }
        });

//        holder.mSwitchCompat.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                Log.d("Clicked Reminder", Boolean.toString(holder.mSwitchCompat.isChecked()));
//                if (holder.mSwitchCompat.isChecked()) {
//                    Log.d("ac", "checked");
//                    db.activateAlarm(reminders.get(position).getId());
//                } else {
//                    db.deactivateAlarm(reminders.get(position).getId());
//
//                }
//            }
//        });
    }

    public Observable<ReminderTime> onClickPosition() {
        return null;
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

        public ReminderHolder(View itemView) {
            super(itemView);

            mCardView = (CardView)  itemView.findViewById(R.id.card_view);
            time = (TextView) itemView.findViewById(R.id.time_text);
            mSwitchCompat = (SwitchCompat) itemView.findViewById(R.id.on_off_toggle);
            typeView = (TextView) itemView.findViewById(R.id.reminder_type_txt);
        }
    }
}
