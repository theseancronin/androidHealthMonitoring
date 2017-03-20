package com.android.shnellers.heartrate.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.shnellers.heartrate.Medication;
import com.android.shnellers.heartrate.MedicationActivity;
import com.android.shnellers.heartrate.MedicationAdapter;
import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.MedicationDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Sean on 28/11/2016.
 */

public class MedicationView extends Fragment implements View.OnClickListener {

    private static final String TAG = "Medication.Fragment";

    private ListView medicationListView;

    private Context context;

    private Button newMedicationBtn;

    private List<Medication> medList;

    private MedicationDatabase db;

    private ArrayAdapter<Medication> adapter;

    private boolean allowRefresh;

    @BindView(R.id.add_medication)
    FloatingActionButton mAddMedication;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.medication_view, container, false);

        db = new MedicationDatabase(getActivity());

        ButterKnife.bind(this, view);

        allowRefresh = false;




        medList = new ArrayList<>();

        medList = db.getMedication();

        if (!medList.isEmpty()) {

            medicationListView = (ListView) view.findViewById(R.id.medication_list);

            adapter = new MedicationAdapter(getActivity(), R.layout.medication_list, medList);
            medicationListView.setAdapter(adapter);

            medicationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });

            medicationListView.setLongClickable(true);
            medicationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Delete Medication");
                    alert.setMessage("Are you sure you want to delete the medication");
                    alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.removeMedication(position);
                            refreshMedicationList();
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    alert.show();

                    return true;
                }
            });

        }

        Log.d(TAG, "onCreateView");

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_medication:

                break;
        }
    }

    @OnClick(R.id.add_medication)
    public void addMedication() {
        Intent intent = new Intent(getActivity(), MedicationActivity.class);
        startActivityForResult(intent, 500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle = getArguments();

        if (bundle != null) {
            String str = getArguments().getString("str");

            Log.d(TAG, str);
        }

        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshMedicationList();


        Bundle bundle = getArguments();

        if (bundle != null) {
            String str = getArguments().getString("str");

            Log.d(TAG, str);
        }
        Log.d(TAG, "onResume");
        if (allowRefresh) {
            allowRefresh = false;
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    private void refreshMedicationList() {
        medList.clear();
        medList.addAll(db.getMedication());
   //     adapter.notifyDataSetChanged();
    }
}
