package com.android.shnellers.heartrate;

import java.util.ArrayList;

/**
 * Created by Sean on 24/01/2017.
 */

public class SimpleRow {

    ArrayList<SimplePage> mPagesRow = new ArrayList<SimplePage>();

    public void addPages(SimplePage page) {
        mPagesRow.add(page);
    }

    public SimplePage getPages(int index) {
        return mPagesRow.get(index);
    }

    public int size() {
        return mPagesRow.size();
    }
}