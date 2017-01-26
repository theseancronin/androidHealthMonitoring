package com.android.shnellers.heartrate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The type Wear main activity.
 */
public class WearMainActivity extends FragmentActivity implements View.OnClickListener{

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;

    private Button mHeartBtn;

    private ViewAdapter mViewAdapter;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);

        //setContentView(R.layout.grid);

        final GridViewPager mGridPager = (GridViewPager) findViewById(R.id.pager);
        mGridPager.setAdapter(new GridPagerAdapter(this, getFragmentManager()));

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
//        mHeartBtn = (Button) findViewById(R.id.heartBtn);
//        mHeartBtn.setOnClickListener(this);

      //  mViewPager = (ViewPager) findViewById(R.id.wear_view_pager);

       // mViewAdapter = new ViewAdapter(getSupportFragmentManager());

       // setupWearablePages();

//        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
//        pager.setAdapter(new SensorFragmentPagerAdapter(getFragmentManager()));
//
//        DotsPageIndicator indicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
//        indicator.setPager(pager);

        String message = getIntent().getStringExtra("message");
        if (message == null || message.equalsIgnoreCase("")) {
            message = "Hello World";
        }

        //mHeartBtn.setText(message);
    }

    private void setupWearablePages() {
       // mViewAdapter.addFragment(new HeartRateActivity());
       // mViewAdapter.addFragment(new ActivitySensorService());
       //    mViewPager.setAdapter(mViewAdapter);
    }

//    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
//        super.onEnterAmbient(ambientDetails);
//        updateDisplay();
//    }
//
//    @Override
//    public void onUpdateAmbient() {
//        super.onUpdateAmbient();
//        updateDisplay();
//    }
//
//    @Override
//    public void onExitAmbient() {
//        updateDisplay();
//        super.onExitAmbient();
//    }
//
//    private void updateDisplay() {
//        if (isAmbient()) {
//            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//        } else {
//            mContainerView.setBackground(null);
//        }
//    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
//            case R.id.heartBtn:
//                checkHeartRate();
//                break;
        }
    }

    private void checkHeartRate() {
        Intent intent = new Intent(this, HeartRateActivity.class);
        startActivity(intent);
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
