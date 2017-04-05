package com.android.shnellers.heartrate.fragments;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.passport.PassportMain;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NUMBER;

/**
 * Created by Sean on 05/04/2017.
 */

public class EditPharmacy extends AppCompatActivity{

    private static final String TAG = "GP_DETAILS";


    private UserDatabase mDB;

    @BindView(R.id.pharmacy_name)
    protected EditText mName;

    @BindView(R.id.pharmacy_tel)
    protected EditText mPhone;

    @BindView(R.id.save_btn)
    protected Button mSave;

    @BindView(R.id.cancelButton)
    protected Button mCancel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_edit_pharmacy);

        ButterKnife.bind(this);

        mDB = new UserDatabase(this);

        fillEditTextFields();

    }

    private void fillEditTextFields() {

        HashMap<String, String> fields = mDB.getPharmacyDetails();

        String name;
        String number;

        if (fields != null) {

            if (fields.get(PHARMACY_NAME) != null) {
                name = fields.get(PHARMACY_NAME);
                mName.setText(name != null ? name : "");
            }

            if (fields.get(PHARMACY_NUMBER) != null) {
                number = fields.get(PHARMACY_NUMBER);
                mPhone.setText(number != null ? number : "");
            }
        }
    }


    @OnClick(R.id.save_btn)
    protected void savePharmacyDetails() throws SQLException {

        Log.d(TAG, "savePharmacyDetails: " + mName.getText().toString());

        int num = 0;

        if (mPhone.getText().toString().length() > 0) {
            num = Integer.parseInt(mPhone.getText().toString());
        }
        mDB.savePharmacyDetails(mName.getText().toString(), num);

        Intent intent = new Intent(this, PassportMain.class);
        startActivity(intent);
    }

    @OnClick(R.id.cancelButton)
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
