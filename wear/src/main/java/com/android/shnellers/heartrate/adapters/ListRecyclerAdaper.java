package com.android.shnellers.heartrate.adapters;

import android.support.wearable.view.WearableRecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Sean on 23/02/2017.
 */

public class ListRecyclerAdaper extends WearableRecyclerView.Adapter<ListRecyclerAdaper.MyHolder> {

    private ArrayList<String> list;

    public ListRecyclerAdaper(final ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyHolder extends WearableRecyclerView.ViewHolder {

        public MyHolder(View itemView) {
            super(itemView);
        }
    }

}
