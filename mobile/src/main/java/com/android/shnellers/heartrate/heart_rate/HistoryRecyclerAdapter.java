package com.android.shnellers.heartrate.heart_rate;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.HeartRateObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 13/03/2017.
 */

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryHolder> {

    private ArrayList<HeartRateObject> mHeartRateObjects;

    private Context mContext;

    public HistoryRecyclerAdapter(ArrayList<HeartRateObject> heartRateObjects) {
        mHeartRateObjects = heartRateObjects;
    }

    @Override
    public HistoryRecyclerAdapter.HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.heart_rate_history_card, parent, false);

        mContext = parent.getContext();

        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryRecyclerAdapter.HistoryHolder holder, int position) {

        HeartRateObject heartRateObject = mHeartRateObjects.get(position);

        SimpleDateFormat format = new SimpleDateFormat("dd MMM yy", Locale.UK);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.UK);

        holder.mHeartRate.setText(String.valueOf(heartRateObject.getHeartRate()));

        if (heartRateObject.getHeartRate() >= 100 || heartRateObject.getHeartRate() < 40) {
            holder.mHeartRate.setTextColor(ContextCompat.getColor(mContext, R.color.over_120));
        } else {
            holder.mHeartRate.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

        holder.mDate.setText(format.format(heartRateObject.getDateTime()));
        holder.mTime.setText(timeFormat.format(heartRateObject.getDateTime()));
        holder.mActivity.setText(heartRateObject.getType());

    }

    @Override
    public int getItemCount() {
        return mHeartRateObjects.size();
    }

    public static class HistoryHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.heart_rate)
        TextView mHeartRate;

        @BindView(R.id.date_view)
        TextView mDate;

        @BindView(R.id.time_view)
        TextView mTime;

        @BindView(R.id.activity_type)
        TextView mActivity;

        public HistoryHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

}
