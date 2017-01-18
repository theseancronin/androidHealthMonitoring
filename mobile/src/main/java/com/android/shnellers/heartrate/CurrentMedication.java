package com.android.shnellers.heartrate;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.shnellers.heartrate.database.MedicationDatabase;

import java.util.ArrayList;
import java.util.List;


public class CurrentMedication extends AppCompatActivity {

    public static final String EXTRA_MED = "com.dev.prestigious.heartmonitor.currentmedication";

    private SessionManager session;

    private List<Medication> mMedications;

    private MedicationDatabase db;

    private ListAdapter adapter;

    private ListView medsList;

    private RecyclerView recList;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_medication);

        mMedications = new ArrayList<>();

        session = new SessionManager(getApplicationContext());

        db = new MedicationDatabase(this);

        user = null;

        recList = (RecyclerView) findViewById(R.id.recycleView);

        // Set the manager for our list layout
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);


    }

    @Override
    public void onStart() {
        super.onStart();

        displayCurrentMedication();


    }

    /**
     *
     * @param view
     */
    public void addNewMedication (View view) throws SQLException {
        Intent intent = new Intent(CurrentMedication.this, MedicationActivity.class);
        startActivity(intent);
    }

    private void displayCurrentMedication() {

        user = session.getLoggedInUser();

        db.open();

        mMedications = db.getMedication();

       // MedicationAdapter ad = new MedicationAdapter(mMedications);

        //recList.setAdapter(ad);

        db.close();
    }

    /**
     * Cancel the viewing of medication.
     *
     * @param view
     */
    public void cancel (View view) {

        Intent intent = new Intent(CurrentMedication.this, MainActivity.class);
        startActivity(intent);

    }
}
