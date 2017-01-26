package com.android.shnellers.heartrate;

import android.app.Fragment;

import java.util.ArrayList;

/**
 * Created by Sean on 24/01/2017.
 */

public class GridPagerRow {

    ArrayList<Fragment> mPagesRow = new ArrayList<>();

    public void addPages(Fragment fragment) {
        mPagesRow.add(fragment);
    }

    public Fragment getPages(int index) {
        return mPagesRow.get(index);
    }

    public int size(){
        return mPagesRow.size();
    }
}
