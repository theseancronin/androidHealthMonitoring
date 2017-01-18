package com.android.shnellers.heartrate.fragments;

import android.content.ContentValues;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.SessionManager;
import com.android.shnellers.heartrate.User;
import com.android.shnellers.heartrate.database.GPContract;
import com.android.shnellers.heartrate.database.GPDatabase;
import com.android.shnellers.heartrate.database.GPDatabaseHelper;


/**
 * Created by Sean on 27/11/2016.
 */

public class EditPassportGP extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GP_DETAILS";

    private GPDatabaseHelper mGPDatabaseHelper;

    private GPDatabase db;

    private User user;

    private SessionManager session;

    private Button cancelButton, saveButton;

    private EditText _gpName, _gpPractice, _gpTelephone;

    private User loggedUser;

    private String mUserEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_edit_gp_details);

        saveButton = (Button) findViewById(R.id.saveButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        _gpName = (EditText) findViewById(R.id.gpName);
        _gpPractice = (EditText) findViewById(R.id.gpPractice);
        _gpTelephone = (EditText) findViewById(R.id.gpTelephone);

        db = new GPDatabase(this);

        session = new SessionManager(this.getApplicationContext());

        loggedUser = session.getLoggedInUser();

        mUserEmail = loggedUser.get_email();

        Log.d(TAG, "create");
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.saveButton:
                Log.d(TAG, "save button clicked");
                saveGPDetails();
                break;

            case R.id.cancelButton:
                Log.d(TAG, "cancel button clicked");
                onBackPressed();
                break;
        }

    }

    private void saveGPDetails() throws SQLException {
        ContentValues values = new ContentValues();
        values.put(GPContract.GPEntry.COLUMN_GP_NAME, _gpName.getText().toString());
        values.put(GPContract.GPEntry.COLUMN_GP_PRACTICE, _gpPractice.getText().toString());
        values.put(GPContract.GPEntry.COLUMN_GP_TELEPHONE, _gpTelephone.getText().toString());

        db.saveGPDetails(mUserEmail, values);

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
