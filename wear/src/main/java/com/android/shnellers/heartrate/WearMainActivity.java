package com.android.shnellers.heartrate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.android.shnellers.heartrate.heart_rate.HeartRateHome;
import com.android.shnellers.heartrate.heart_rate.HeartRateServiceStarter;
import com.android.shnellers.heartrate.voice_recorder.DiaryLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * The type Wear main activity.
 */
public class WearMainActivity extends WearableActivity implements View.OnClickListener,
        MenuItem.OnMenuItemClickListener{

    private static final String TAG = System.getProperties().getClass().getSimpleName();

    private static final int INTERVAL_SET = 1;
    private static final int INTERVAL_NOT_SET = 0;
    private static final int DEFAULT = -1;

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;

    private Button mHeartBtn;

    private ViewAdapter mViewAdapter;

    private ViewPager mViewPager;

    private SharedPreferences mPreferences;

    private String mNodeId;

    @BindView(R.id.btn_heart_health)
    ImageButton mHeartHealthBtn;

    @BindView(R.id.btn_activity)
    ImageButton mActivityBtn;

    @BindView(R.id.btn_diary)
    ImageButton mDiaryBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        //setContentView(R.layout.grid);

        //final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.pager);
        //mGridPager.setAdapter(new GridPagerAdapter(this, getFragmentManager()));

        ButterKnife.bind(this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);

        setAutomaticHeartRateCheck();

        String message = getIntent().getStringExtra("message");
        if (message == null || message.equalsIgnoreCase("")) {
            message = "Hello World";
        }

        mPreferences = getPreferences(MODE_PRIVATE);

        //setTimeIntervalKey();
        startAllDayHeartMonitoring();

    }

    private void setTimeIntervalKey() {
        int intervalSet = mPreferences.getInt(getString(R.string.time_interval_set), DEFAULT);

        if (intervalSet == DEFAULT) {
            Log.d(TAG, "setTimeIntervalKey: SETTING INTERVAL NOT SET KEY");
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(getString(R.string.time_interval_set), INTERVAL_NOT_SET);
            editor.apply();
        }

    }

    private void startAllDayHeartMonitoring() {
//        int intervalSet = mPreferences.getInt(getString(R.string.time_interval_set), INTERVAL_NOT_SET);
//        if (intervalSet == INTERVAL_NOT_SET) {
//            Log.d(TAG, "SETTING INTERVAL TO TRUE");
//            SharedPreferences.Editor editor = mPreferences.edit();
//            editor.putInt(getString(R.string.time_interval_set), INTERVAL_SET);
//            editor.apply();

            startHeartRateMonitoring();
      //  }
    }

    private void startHeartRateMonitoring() {
        Log.d(TAG, "SETTING ALL DAY MONITORING");
        Intent myIntent = new Intent(this, HeartRateServiceStarter.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,  0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long interval = 60000 * 7;

        /* Set the alarm to start at 10:30 AM */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);

        /* Repeating on every 20 minutes interval */
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                interval, pendingIntent);

        long frequency= 60 * 500; // in ms
//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                (SystemClock.elapsedRealtime() + frequency),
//                frequency, pendingIntent);

    }




    private void setAutomaticHeartRateCheck() {

        Log.d(TAG, "setAutomaticHeartRateCheck: ");
        Intent intent = new Intent(this, HeartRateServiceStarter.class);

        intent.setAction("packagename.ACTION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 40);
        calendar.set(Calendar.SECOND, 0);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);


    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

        }
    }

    @OnClick(R.id.btn_diary)
    public void diaryActivity() {
        Intent intent = new Intent(this, DiaryLog.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_heart_health)
    public void checkHeartRate() {
        Intent intent = new Intent(this, HeartRateHome.class);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private static class ViewAdapter extends FragmentPagerAdapter {

        /**
         * The Pages list.
         */
        public List<Fragment> pagesList;

        /**
         * Instantiates a new View adapter.
         *
         * @param fm the fm
         */
        public ViewAdapter(FragmentManager fm) {
            super(fm);

            pagesList = new ArrayList<>();
        }

        /**
         * Add fragment.
         *
         * @param fragment the fragment
         */
        public void addFragment(final Fragment fragment) {
            pagesList.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return pagesList.get(position);
        }

        @Override
        public int getCount() {
            return pagesList.size();
        }
    }
}
