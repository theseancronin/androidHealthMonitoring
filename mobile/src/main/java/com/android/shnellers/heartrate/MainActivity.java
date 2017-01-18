package com.android.shnellers.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.fragments.HomeFragment;
import com.android.shnellers.heartrate.fragments.MedicationFragment;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String NAME_EXTRA = "com.dev.prestigious.heartmonitor.name";
    private static final String EMAIL_EXTRA = "com.dev.prestigious.heartmonitor.email";
    private static final String TAG = "MainActivity";



    private User _loggedUser;

    private UserDatabase _db;

    private SessionManager session;

    private DrawerLayout mDrawerLayout;

    private ListView mDrawerList;

    private String[] mMenuItems = {"hello", "world"};

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());

        _db = new UserDatabase(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup the pager so we can flick between the activities
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);

        // Setup the layout for the tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        // set the adapter for the list
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.drawer_list_item, mMenuItems));

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();

        Log.i("create is Logged", Boolean.toString(_db.getIsUserLoggedIn()));

    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), "Home");
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
        Log.i("Cancelling", "Cncel");
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

        }
    }
}