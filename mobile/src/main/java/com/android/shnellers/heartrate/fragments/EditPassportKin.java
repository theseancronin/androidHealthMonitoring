package com.android.shnellers.heartrate.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.KinContract;
import com.android.shnellers.heartrate.database.KinDatabase;
import com.android.shnellers.heartrate.database.KinDatabaseHelper;


public class EditPassportKin extends AppCompatActivity implements View.OnClickListener  {

    private static final String TAG = "EDIT_KIN_DETAILS";

    private KinDatabase db;

    private KinDatabaseHelper helper;

    private Button saveButton, cancelButton;

    private EditText name, telephone;

    private TextView relationship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_edit_kin);

        name = (EditText) findViewById(R.id.kinName);
        telephone = (EditText) findViewById(R.id.kinTelephone);
        relationship = (TextView) findViewById(R.id.kinRelationship);

        saveButton = (Button) findViewById(R.id.save_btn);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        db = new KinDatabase(this);


        setKinInformation();

    }

    /**
     * Set the information to be edited in the edit text views.
     */
    private void setKinInformation() {

        Cursor kin = db.getKinDetails();

        int telephoneIndex = 0;
        int nameIndex = 0;
        int relationshipIndex = 0;

        if (kin != null) {

            kin.moveToFirst();

            nameIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_NAME);
            relationshipIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_RELATIONSHIP);
            telephoneIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_TELEPHONE);
        }

        name.setText(kin != null ? kin.getString(nameIndex) : "");
        relationship.setText(kin != null ? kin.getString(relationshipIndex) : "");
        telephone.setText(kin != null ? kin.getString(telephoneIndex) : "");
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.saveButton:
                _saveKinDetails();
                break;

            case R.id.cancelButton:
                cancelEditKin();
                break;
        }

    }

    private void _saveKinDetails() {

        ContentValues values = new ContentValues();

        values.put(KinContract.KinEntry.COLUMN_KIN_NAME, name.getText().toString());
        values.put(KinContract.KinEntry.COLUMN_KIN_RELATIONSHIP, relationship.getText().toString());
        values.put(KinContract.KinEntry.COLUMN_KIN_TELEPHONE, telephone.getText().toString());

        db.saveKinDetails(values);

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString("key", "hello");
        intent.putExtras(bundle);
        setResult(1, intent);
        finish();
        //onBackPressed();
    }

    public void cancelEditKin() {
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "start");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "pause");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "resume");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "stop");
    }

}
