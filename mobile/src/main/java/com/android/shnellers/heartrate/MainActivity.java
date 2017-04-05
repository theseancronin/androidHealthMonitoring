package com.android.shnellers.heartrate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.heart_rate.HourlyAnalysisFragment;
import com.android.shnellers.heartrate.heart_rate.MainFrag;
import com.android.shnellers.heartrate.settings.SettingsView;
import com.android.shnellers.heartrate.weight.WeightAnalysis;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String REMINDERS = "Reminders";
    private static final String SETTINGS = "Settings";
    private static final String TAG = "MainActivity";

    private String[] mDrawerTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private ViewPagerAdapter adapter;

    private CharSequence mTitle;

    private CharSequence mDrawerTitle;

    @BindView(R.id.navigation)
    protected FloatingActionButton mActionButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate_tab_layout);

        ButterKnife.bind(this);

        mTitle = mDrawerTitle = getTitle();

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Setup the pager so we can flick between the activities
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);

        // Setup the layout for the tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setIcon(R.drawable.ic_menu);
        }



        mDrawerTitles = getResources().getStringArray(R.array.drawer_items);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {


            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                Log.d(TAG, "onDrawerClosed: ");
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mDrawerTitle);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        setWeightCheckService();

       // addUser();

    }

    private void addUser() {

        UserDatabase db = new UserDatabase(this);
        db.insertUser();

    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: ");
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Once a day check that the user has logged his weight and also that
     * do a once off check.
     */
    private void setWeightCheckService() {

        int[] alarmIds = {145, 541};



        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        for (int id = 0; id < alarmIds.length; id++) {
            int current = alarmIds[id];

            if (current == 145){
                calendar.set(Calendar.HOUR_OF_DAY, 20);
                calendar.set(Calendar.MINUTE, 0);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 20);
                calendar.set(Calendar.MINUTE, 0);
            }

            Intent intent = new Intent(this, WeightAnalysis.class);

            PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

            manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);


        }



    }

    @OnClick(R.id.navigation)
    protected void navigationClick() {
        Intent intent = new Intent(this, ExtrasView.class);
        startActivity(intent);
    }
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        String item = mDrawerList.getItemAtPosition(position).toString();
        if (item.equals(REMINDERS)) {
            Intent intent = new Intent(this, Reminders.class);
            startActivity(intent);
        } else if (item.equals(SETTINGS)) {
            Intent intent = new Intent(this, SettingsView.class);
            startActivity(intent);
        }

        // Create a new fragment and specify the planet to show based on position
//        Fragment fragment = new PlanetFragment();
//        Bundle args = new Bundle();
//        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
//        fragment.setArguments(args);
//
//        // Insert the fragment by replacing any existing fragment
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.content_frame, fragment)
//                .commit();
//
//        // Highlight the selected item, update the title, and close the drawer
//        mDrawerList.setItemChecked(position, true);
//        setTitle(mPlanetTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter.addFragment(new MainFrag(), "Home");
        adapter.addFragment(new HourlyAnalysisFragment(), "Hourly Analysis");
        mViewPager.setAdapter(adapter);
    }
}