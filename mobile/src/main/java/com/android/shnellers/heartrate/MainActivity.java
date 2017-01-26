package com.android.shnellers.heartrate;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.shnellers.heartrate.activities.ActivityHomeFragment;
import com.android.shnellers.heartrate.activities.ActivityRecognitionService;
import com.android.shnellers.heartrate.activities.StepDetectorEvent;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.fragments.HomeFragment;
import com.android.shnellers.heartrate.fragments.MedicationFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final String NAME_EXTRA = "com.dev.prestigious.heartmonitor.name";
    private static final String EMAIL_EXTRA = "com.dev.prestigious.heartmonitor.email";
    private static final String TAG = "MainActivity";

    private static final boolean DEBUG = true;

    public static final String REMINDERS = "Reminders";

    private User _loggedUser;

    private UserDatabase _db;

    private SessionManager session;

    private String[] mMenuItems = {"hello", "world"};

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;

    private ImageButton activitiesBtn;

    private ViewPagerAdapter adapter;

    private String[] mDrawerTitles;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());

        _db = new UserDatabase(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerTitles = getResources().getStringArray(R.array.extrasListMenu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Setup the pager so we can flick between the activities
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);

        // Setup the layout for the tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        Log.d(TAG, "onCreate: ");
        Intent stepService = new Intent (getApplicationContext(), StepDetectorEvent.class);
        //startService(stepService);

        // set the adapter for the list
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mMenuItems));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();




        Log.i("create is Logged", Boolean.toString(_db.getIsUserLoggedIn()));


    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment.equals(new HomeFragment())) {
            Log.d(TAG, "onAttachFragment: confirmed");
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new ActivityHomeFragment(), "A");
        adapter.addFragment(new PersonalDetails(), "Passport");
        adapter.addFragment(new MedicationFragment(), "Meds");
//        adapter.addFragment(new Reminders(), "Reminders");

        //adapter.addFragment(new PersonalDetails(), "Passport");

        mViewPager.setAdapter(adapter);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        _db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("Start isLogged", Boolean.toString(session.isLoggedIn()));

        if (!session.isLoggedIn()) {
            Log.i("If is Logged", Boolean.toString(_db.getIsUserLoggedIn()));
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            displayUserDetails();
        }

    }

    /**
     * Authenticate that the user is actually logged in.
     *
     * @return
     */
    private boolean authenticate() {
        return _db.getIsUserLoggedIn();
    }

    /**
     * Display the user details.
     */
    private void displayUserDetails() {

        User user = session.getLoggedInUser();


    }

    /**
     * Log the user out of the application.
     *
     * @param view
     */
    public void applicationLogout(View view) {
        session.setLogin(false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Edit the users details.
     *
     * @param view
     */
    public void editUserDetails(View view) {
        Intent intent = new Intent(MainActivity.this, PersonalDetails.class);
        startActivity(intent);

    }

    /**
     * Edit the users medication.
     *
     * @param view
     */
    public void editMedicalDetails(View view) {
        Intent intent = new Intent(MainActivity.this, CurrentMedication.class);
        startActivity(intent);
    }

    /**
     * Cancel edit.
     *
     * @param view
     */
    public void cancelEdit(View view) {
        Log.i("Cancelling", "Cancel");
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }

    public void sendBtnClicked (View v){
        Log.d(TAG, "sendBtnClicked: ");

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.activity_view:
                adapter.swapFragment(new ActivityHomeFragment());
                Log.d(TAG, "onClick: activity");
                break;
            case R.id.extras:
                Log.d(TAG, "onClick: ");
                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");

        Intent myIntent = new Intent(this, ActivityRecognitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient, 5000, pendingIntent
        );
//
    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String item = mDrawerList.getItemAtPosition(position).toString();

            Log.d(TAG, "onItemClick: " + item);
            if (item.equals("Reminders")) {
                Intent intent = new Intent(getApplicationContext(), Reminders.class);
                startActivity(intent);
            }
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
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
    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
    }
}