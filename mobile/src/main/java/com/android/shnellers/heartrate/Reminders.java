package com.android.shnellers.heartrate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.shnellers.heartrate.database.RemindersContract;
import com.android.shnellers.heartrate.database.RemindersDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class Reminders extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Reminders";

    private FloatingActionButton fab;

    private ArrayList<ReminderTime> reminders;

    private Bundle mBundle;

    private int count;

    private RemindersDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_list_view);

        getSupportActionBar().show();

        fab = (FloatingActionButton) findViewById(R.id.add_reminder);
        fab.setOnClickListener(this);

        // Initialize the list of reminders
        reminders = new ArrayList<>();

        db = new RemindersDatabase(this);

        Log.d(TAG, "onCreate: ");

        displayRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Reminders", "onStart");
    }

    private void displayRecyclerView() {

        Cursor c = db.getReminders();

        int hourIndex, minuteIndex, daysIndex, typeIndex, activeIndex, idIndex;

        ReminderTime reminder;

        Log.d("Cursor Count: ", Integer.toString(c.getCount()));

        if (c != null) {
            hourIndex = c.getColumnIndex(RemindersContract.Columns.HOUR_COLUMN);
            minuteIndex = c.getColumnIndex(RemindersContract.Columns.MINUTE_COLUMN);
            daysIndex = c.getColumnIndex(RemindersContract.Columns.DAYS_COLUMN);
            typeIndex = c.getColumnIndex(RemindersContract.Columns.TYPE_COLUMN);
            activeIndex = c.getColumnIndex(RemindersContract.Columns.ACTIVE_COLUMN);
            idIndex = c.getColumnIndex(RemindersContract.Columns.ID_COLUMN);

            c.moveToFirst();

            while (c.moveToNext()) {
                reminder = new ReminderTime(c.getInt(hourIndex), c.getInt(minuteIndex),
                        c.getInt(activeIndex), c.getInt(idIndex), c.getString(typeIndex));
                Log.d("Active: Type: ", c.getString(typeIndex));
                reminders.add(reminder);
            }

            setAlarms(reminders);



            Log.d("Reminders Size: ", Integer.toString(reminders.size()));

            RecyclerView rv = (RecyclerView) findViewById(R.id.reminder_list);
            rv.setHasFixedSize(true);

            ReminderListAdapter adapter = new ReminderListAdapter(reminders);
            rv.setAdapter(adapter);

            RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
            rv.setLayoutManager(manager);

//            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
//                    LinearLayoutManager.VERTICAL);
//            rv.addItemDecoration(dividerItemDecoration);
        }


    }

    private void setAlarms(ArrayList<ReminderTime> reminders) {

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        ArrayList<PendingIntent> intentsArray = new ArrayList<>();

        if (!reminders.isEmpty()) {
            for (int alarm = 0; alarm < reminders.size(); alarm++) {

                NotificationCompat.Builder n1 = new NotificationCompat.Builder(this)
                        .setContentTitle("Heart Monitor Reminder")
                        .setContentText("Time to check Heart Rate")
                        .setSmallIcon(R.mipmap.ic_launcher);

                Intent intent = new Intent(this, NotificationPublisher.class);
                intent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
                intent.putExtra(NotificationPublisher.NOTIFICATION, n1.build());

                // Loop counter alarm is used as request code
                PendingIntent pi = PendingIntent.getBroadcast(this, alarm, intent, 0);

                ReminderTime time = reminders.get(alarm);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
                calendar.set(Calendar.MINUTE, time.getMinute());
                calendar.set(Calendar.SECOND, 0);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    Log.d("Reminders", "Alarm set: " + Integer.toString(time.getHour()) + " : " + Integer.toString(time.getMinute()));
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                }

                intentsArray.add(pi);
            }
        }

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.add_reminder:
                addReminder();
                break;
        }

    }

    private void addReminder() {
        Intent intent = new Intent(this, Reminder.class);
        intent.putParcelableArrayListExtra("parce", reminders);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", reminders);
       // outState.putBundle("bundle", bundle);
       // outState.putParcelableArrayList("remind", reminders);
        outState.putInt("save_count", count);
        Log.d("Reminders Count: ", Integer.toString(count));
        Log.d("Reminders", "saveState");
        Log.d("Reminders list size", Integer.toString(reminders.size()));

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

}
