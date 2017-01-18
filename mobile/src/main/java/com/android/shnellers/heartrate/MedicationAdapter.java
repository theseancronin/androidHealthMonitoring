package com.android.shnellers.heartrate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Sean on 31/10/2016.
 */

public class MedicationAdapter extends ArrayAdapter<Medication> {

    private List<Medication> medList;

    private int resource;
    private LayoutInflater mInflater;
    private Context mContext;

    public MedicationAdapter (final Context ctx,
                              final int resourceID,
                              final List<Medication> objects) {
        super(ctx, resourceID, objects);

        resource = resourceID;
        mContext = ctx;
        mInflater = LayoutInflater.from(ctx);

        this.medList = medList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = (RelativeLayout) mInflater.inflate(resource, null);
        Medication med = getItem(position);

            TextView medName = (TextView) convertView.findViewById(R.id.medication_name);
            medName.setText(med.getName());

            TextView strength = (TextView) convertView.findViewById(R.id.strength);
            strength.setText("Strength: " + Integer.toString(med.getStrength()) + " mg");

            TextView frequecy = (TextView) convertView.findViewById(R.id.frequency);
            frequecy.setText("Frequecy: " + Integer.toString(med.getFrequency()));

        return convertView;
    }

    //    @Override
//    public MedicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        View itemView = LayoutInflater
//                .from(parent.getContext())
//                .inflate(R.layout.card_view, parent, false);
//
//        return new MedicationViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(MedicationViewHolder holder, int position) {
//
//        Medication med = medList.get(position);
//
//
//        Log.i("HOLDER NAME", med.getName());
//        Log.i("POSITION", Integer.toString(position));
//        holder.mName.setText(med.getName());
//        holder.mStrength.setText(Integer.toString(med.getStrength()));
//        holder.mFrequency.setText(Integer.toString(med.getFrequency()));
//    }
//
//    @Override
//    public int getItemCount() {
//        return medList.size();
//    }
//
//    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
//        protected TextView mName;
//        protected TextView mStrength;
//        protected TextView mFrequency;
//
//        public MedicationViewHolder (View v) {
//            super(v);
//            mName = (TextView) v.findViewById(R.id.cardTitle);
//            mStrength = (TextView) v.findViewById(R.id.cardStrength);
//            mFrequency = (TextView) v.findViewById(R.id.cardFrequency);
//        }
//
//    }
}
