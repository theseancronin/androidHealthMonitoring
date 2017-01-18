package com.android.shnellers.heartrate.recyclers;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.shnellers.heartrate.HeartReading;
import com.android.shnellers.heartrate.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.android.shnellers.heartrate.database.HeartRateDatabase.TAG;

/**
 * Created by Sean on 15/01/2017.
 */

public class BPMReadingsAdapter extends RecyclerView.Adapter<BPMReadingsAdapter.ViewHolder>{

    public static final String N_A = "n/a";

    private static final String DATE_TIME_STRING_FORMAT = "hh:mm";

    private List<HeartReading> readings;

    public BPMReadingsAdapter(final List<HeartReading> readings) {
        this.readings = readings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.heart_rate_card_view, parent, false);



        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        int bpm = readings.get(position).getBPM();

        Long dateTime = new Long(readings.get(position).getDateTime());

        if (bpm > 0) {
            holder.heartRate.setText(Integer.toString(bpm));
        } else {
            holder.heartRate.setText(N_A);
        }

        Log.d(TAG, "onBindViewHolder: Time: " + Long.toString(dateTime));

        if (dateTime != 0) {

            Log.d(TAG, "onBindViewHolder: Not 0");
            Log.d(TAG, "onBindViewHolder: Timt" + Long.toString(dateTime));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);

            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(dateTime);

            Date time = calendar.getTime();

            
            holder.time.setText("Time: " + simpleDateFormat.format(calendar.getTime()));

        } else {
            Log.d(TAG, "onBindViewHolder: is zro");
            holder.time.setText(N_A);
        }
    }

    @Override
    public int getItemCount() {
        if (!readings.isEmpty()) {
            return readings.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CardView mCardView;
        public TextView heartRate;
        public TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.bpm_card);
            heartRate = (TextView) itemView.findViewById(R.id.heart_rate_value);
            time = (TextView) itemView.findViewById(R.id.time_txt);
        }
    }

}
