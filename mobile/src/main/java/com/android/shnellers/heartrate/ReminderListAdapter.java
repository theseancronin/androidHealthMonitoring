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

/**
 * Created by Sean on 08/01/2017.
 */

public class ReminderListAdapter extends RecyclerView.Adapter<ReminderListAdapter.ReminderHolder> {

    private ArrayList<ReminderTime> reminders;
    private RemindersDatabase db;


    @Override
    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_card_view, parent, false);

        db = new RemindersDatabase(itemView.getContext());

        return new ReminderHolder(itemView);
    }

    public ReminderListAdapter(final ArrayList<ReminderTime> reminders) {
        this.reminders = reminders;
    }

    @Override
    public void onBindViewHolder(final ReminderHolder holder, final int position) {
        holder.time.setText(Integer.toString(reminders.get(position).getHour()) + " : " +
                        Integer.toString(reminders.get(position).getMinute()));

        holder.typeView.setText(reminders.get(position).getType());

        Log.d("ReminderAdapter active", Integer.toString(reminders.get(position).getActive()));

        boolean active = (reminders.get(position).getActive() != 0);

        holder.mSwitchCompat.setChecked(active);

       // Log.d("Clicked Reminder", Boolean.toString(holder.mSwitchCompat.isChecked()));

        holder.mSwitchCompat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Log.d("Clicked Reminder", Boolean.toString(holder.mSwitchCompat.isChecked()));
                if (holder.mSwitchCompat.isChecked()) {
                    Log.d("ac", "checked");
                    db.activateAlarm(reminders.get(position).getId());
                } else {
                    db.deactivateAlarm(reminders.get(position).getId());

                }
            }
        });
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
