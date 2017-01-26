package com.android.shnellers.heartrate;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 02/11/2016.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "ViewPagerAdapter";

    private final List<Fragment> mFragmentList;
    private final List<String> mFragmentTitleList;

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);

        mFragmentList = new ArrayList<>();
        mFragmentTitleList = new ArrayList<>();
    }

    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, "getItemPosition: ");
        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem: " + String.valueOf(position));
        Log.d(TAG, "getItem: " + mFragmentTitleList.get(position));
        Log.d(TAG, "getItem: " + mFragmentList.get(position).getTag());
        Log.d(TAG, "number of fragment: " + String.valueOf(getCount()));

        return mFragmentList.get(position);
    }

    public void addFragment(final Fragment fragment, final String title) {
        mFragmentList.add(fragment);
        Log.d(TAG, "addFragment: " + String.valueOf(mFragmentList.size()));
        mFragmentTitleList.add(title);

    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle (final int position) {
        Log.d(TAG, "getPageTitle: ");
        return mFragmentTitleList.get(position);

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.d(TAG, "notifyDataSetChanged: ");
    }

    public void swapFragment(Fragment fragment) {
        mFragmentList.add(0, fragment);
        Log.d(TAG, "swapFragment: ");
        notifyDataSetChanged();
    }
}
