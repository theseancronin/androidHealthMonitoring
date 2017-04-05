package com.android.shnellers.heartrate.activities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.LatestObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 19/03/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CardHolder> {

    private ArrayList<LatestObject> mActivities;
    private Context mContext;

    public RecyclerAdapter(ArrayList<LatestObject> activities, Context context) {
        mContext = context;
        mActivities = activities;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.latest_item, parent, false);


        return new CardHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {

       holder.mType.setText(String.valueOf(mActivities.get(position).getType()));
       holder.mValue.setText(String.valueOf(mActivities.get(position).getValue()));


    }

    private void setTextView(TextView textView, String value, int color) {
        textView.setText(value);
        textView.setTextColor(color);
    }


    @Override
    public int getItemCount() {
        return mActivities.size();
    }

    static class CardHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.value)
        TextView mValue;

        @BindView(R.id.type)
        TextView mType;



        public CardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
