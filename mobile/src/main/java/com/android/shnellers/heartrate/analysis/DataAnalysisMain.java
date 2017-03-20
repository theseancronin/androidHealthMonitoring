package com.android.shnellers.heartrate.analysis;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.ViewPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 28/02/2017.
 */

public class DataAnalysisMain extends AppCompatActivity {

  //  @BindView(R.id.analysis_container)
  //  protected LinearLayout mLinearLayout;

//    @BindView(R.id.tab_layout)
//    protected TabLayout mTabLayout;
//
//    @BindView(R.id.view_pager)
//    protected ViewPager mViewPager;

    @BindView(R.id.overview_container)
    protected RelativeLayout mOverviewLayout;

    @BindView(R.id.activity_container)
    protected RelativeLayout mActivityLayout;

    @BindView(R.id.frequency_container)
    protected RelativeLayout mFrequencyLayout;

    @BindView(R.id.scatter_container)
    protected RelativeLayout mScatterLayout;



    private ViewPagerAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_analysis_main);
        ButterKnife.bind(this);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());


       // setupViewPager();

       // mTabLayout.setupWithViewPager(mViewPager);
    }



//    private void setupViewPager() {
//        adapter.addFragment(new DataOverview(), "Overview");
//        adapter.addFragment(new DataAnalysis(), "Graph");
//        adapter.addFragment(new ClusterAnalysis(), "Cluster");
//
//
//        mViewPager.setAdapter(adapter);
//    }

    @OnClick(R.id.overview_container)
    protected void inflateOverviewLayout() {
        Intent intent = new Intent(this, DataOverview.class);
        startActivity(intent);
    }

    @OnClick(R.id.activity_container)
    protected void inflateHeartActivityLayout() {
        Intent intent = new Intent(this, DataOverview.class);
        startActivity(intent);
    }

    @OnClick(R.id.scatter_container)
    protected void inflateScatterLayout() {
        Intent intent = new Intent(this, ClusterAnalysis.class);
        startActivity(intent);
    }
}
