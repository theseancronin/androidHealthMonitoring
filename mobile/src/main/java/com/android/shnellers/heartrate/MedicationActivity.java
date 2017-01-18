package com.android.shnellers.heartrate;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.android.shnellers.heartrate.database.MedicationContract;
import com.android.shnellers.heartrate.database.MedicationDatabase;
import com.android.shnellers.heartrate.database.MedicationDatabaseHelper;
import com.android.shnellers.heartrate.fragments.MedicationFragment;

import java.io.Serializable;


public class MedicationActivity extends AppCompatActivity implements Serializable, View.OnClickListener {

    private TextInputEditText mMedName, mFrequency, mStrength;
    private MedicationDatabaseHelper mMedicationDatabaseHelper;
    private MedicationDatabase db;

    private Medication medication;

    private String name, frequency, strength;

    private Button saveBtn, cancelBtn;

    private boolean allowRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMedName = (TextInputEditText) findViewById(R.id.medName);
        mFrequency = (TextInputEditText) findViewById(R.id.medFrequency);
        mStrength = (TextInputEditText) findViewById(R.id.medStrength);

        allowRefresh = true;

        saveBtn = (Button) findViewById(R.id.saveButton);
        cancelBtn = (Button) findViewById(R.id.cancelButton);

        saveBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        db = new MedicationDatabase(this);
    }

    /**
     * Save a new medication.
     *
     */
    private void saveMedicationDetails() {

        String name = mMedName.getText().toString();
        String frequency = mFrequency.getText().toString();
        String strength = mStrength.getText().toString();

        if (validated(name, frequency, strength) == true) {
            db.open();

            ContentValues values = new ContentValues();

            values.put(MedicationContract.MedicationEntry.COLUMN_NAME, name);
            values.put(MedicationContract.MedicationEntry.COLUMN_STRENGTH, strength);
            values.put(MedicationContract.MedicationEntry.COLUMN_FREQUENCY, frequency);

            db.addNewMedication(
                    name,
                    Integer.parseInt(strength),
                    Integer.parseInt(frequency));

            Medication med = new Medication(
                    name,
                    Integer.parseInt(strength),
                    Integer.parseInt(frequency));

            db.close();

            Bundle bundle = new Bundle();
            bundle.putString("str", "Hello World");
            MedicationFragment fragment = new MedicationFragment();
            fragment.setArguments(bundle);


            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Validates the inputs by the user.
     *
     * @param name
     * @param frequency
     * @param strength
     * @return
     */
    private boolean validated(String name, String frequency, String strength) {

        boolean valid = true;

        if (name.isEmpty() || frequency.isEmpty() || strength.isEmpty()) {
            valid = false;
        }

        return valid;

    }

    /**
     * Cancel the editing of the medication details.
     *
     */
    private void cancelEditMedication () {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton:
                saveMedicationDetails();
                break;

            case R.id.cancelButton:
                cancelEditMedication();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
