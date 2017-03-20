package com.android.shnellers.heartrate.activities;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.shnellers.heartrate.Constants;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.models.ActivityStats;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 19/03/2017.
 */

public class ActivityCardRecycler extends RecyclerView.Adapter<ActivityCardRecycler.CardHolder> {

    private ArrayList<ActivityStats> mActivities;
    private Context mContext;

    public ActivityCardRecycler(ArrayList<ActivityStats> activities, Context context) {
        mActivities = activities;
        mContext = context;
    }

    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_card, parent, false);


        return new CardHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        ActivityStats activity = mActivities.get(position);

        setTextView(holder.mActivityName, activity.getActivityName(), getActivityColor(activity.getActivityName()));
        setTextView(holder.mAvgHeartRate, String.valueOf(activity.getAvgHeartRate()), getActivityColor(activity.getActivityName()));
        setTextView(holder.mCalories, String.valueOf(activity.getCalories()), getActivityColor(activity.getActivityName()));
        setTextView(holder.mSteps, String.valueOf(activity.getSteps()), getActivityColor(activity.getActivityName()));
        setTextView(holder.mDistance, String.valueOf(activity.getDistance()), getActivityColor(activity.getActivityName()));
        setTextView(holder.mMinutes, String.valueOf(activity.getMinutes()), getActivityColor(activity.getActivityName()));

        if (activity.getActivityName().equals(Constants.Const.RUNNING)) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_run));
            holder.mIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_running));
        } else if (activity.getActivityName().equals(Constants.Const.CYCLING)) {
            holder.mIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cycling));
            holder.mIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_cycling));
        }
    }

    private void setTextView(TextView textView, String value, int color) {
        textView.setText(value);
        textView.setTextColor(color);
    }

    private int getActivityColor(String activityName) {

        int color = -1;

        if (activityName.equals(Constants.Const.WALKING)) {
            color = ContextCompat.getColor(mContext, R.color.walking);
        } else if (activityName.equals(Constants.Const.RUNNING)) {
            color = ContextCompat.getColor(mContext, R.color.running);
        } else if (activityName.equals(Constants.Const.CYCLING)) {
            color = ContextCompat.getColor(mContext, R.color.cycling);
        }
        return color;
    }

    @Override
    public int getItemCount() {
        return mActivities.size();
    }

    static class CardHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.activity_name)
        TextView mActivityName;

        @BindView(R.id.icon)
        ImageView mIcon;

        @BindView(R.id.minutes)
        TextView mMinutes;

        @BindView(R.id.distance)
        TextView mDistance;

        @BindView(R.id.steps)
        TextView mSteps;

        @BindView(R.id.calories)
        TextView mCalories;

        @BindView(R.id.avg_heart_rate)
        TextView mAvgHeartRate;


        public CardHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
