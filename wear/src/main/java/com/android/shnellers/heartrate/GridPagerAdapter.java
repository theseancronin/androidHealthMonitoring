package com.android.shnellers.heartrate;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.android.shnellers.heartrate.activities.ActivityRecognitionSummary;
import com.android.shnellers.heartrate.activities.NewActivity;

import java.util.ArrayList;

/**
 * Created by Sean on 24/01/2017.
 */

public class GridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private ArrayList<GridPagerRow> mPages;


    public GridPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        initPages();
    }

    private void initPages() {
        mPages = new ArrayList<>();

        GridPagerRow row1 = new GridPagerRow();
//        row1.addPages(
//                new DiaryLog());
       // row1.addPages(new HeartRateActivity());
       // row1.addPages(new LatestHeartReadings());


        GridPagerRow row2 = new GridPagerRow();
        row2.addPages(new ActivityRecognitionSummary());
        row2.addPages(new NewActivity());

//
//        SimpleRow row3 = new SimpleRow();
//        row3.addPages(new SimplePage("Title4", "Text4"));
//
//        SimpleRow row4 = new SimpleRow();
//        row4.addPages(new SimplePage("Title5", "Text5"));
//        row4.addPages(new SimplePage("Title6", "Text6"));

        mPages.add(row1);
        mPages.add(row2);
//        mPages.add(row3);
//        mPages.add(row4);
    }

    @Override
    public Fragment getFragment(int row, int col) {
       // GridPagerRow page = ((GridPagerRow) mPages.get(row)).getPages(col);
        //CardFragment fragment = CardFragment.create(page.mTitle, page.mText, page.mIconId);

        return mPages.get(row).getPages(col);
    }

//    @Override
//    public ImageReference getBackground(int row, int col) {
//        SimplePage page = ((SimpleRow)mPages.get(row)).getPages(col);
//        return ImageReference.forDrawable(page.mBackgroundId);
//    }

    @Override
    public int getRowCount() {
        return mPages.size();
    }

    @Override
    public int getColumnCount(int row) {
        return mPages.get(row).size();
    }
}