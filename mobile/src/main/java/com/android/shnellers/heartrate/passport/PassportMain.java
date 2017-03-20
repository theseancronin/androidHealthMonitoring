package com.android.shnellers.heartrate.passport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.android.shnellers.heartrate.PersonalDetails;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.ViewPagerAdapter;
import com.android.shnellers.heartrate.fragments.MedicationView;

/**
 * Created by Sean on 09/02/2017.
 */

public class PassportMain extends AppCompatActivity {

    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_main);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        // Setup the pager so we can flick between the activities
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(mViewPager);

        // Setup the layout for the tabs
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
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
        adapter.addFragment(new PersonalDetails(), "Passport");
        adapter.addFragment(new MedicationView(), "Meds");
//        adapter.addFragment(new Reminders(), "Reminders");

        //adapter.addFragment(new PersonalDetails(), "Passport");

        mViewPager.setAdapter(adapter);
    }
}
