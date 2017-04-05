package com.android.shnellers.heartrate.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.HeartRateDBHelper;

import butterknife.BindView;
import butterknife.OnClick;

import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_CONDITION;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_LOCATION;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_PHONE_NUMBER;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.COLUMN_WEIGHT;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PATIENT_NUMBER;


/**
 * Created by Sean on 21/11/2016.
 */

public class EditPassportBasicDetails extends Activity {

    @BindView(R.id.edit_name)
    protected EditText mName;

    @BindView(R.id.patientNum)
    protected EditText mPatientNum;

    @BindView(R.id.dateOfBirth)
    protected EditText mDOB;

    @BindView(R.id.weight)
    protected EditText mWeight;

    @BindView(R.id.location)
    protected EditText mLocation;

    @BindView(R.id.phoneNumber)
    protected EditText mPhoneNumber;

    @BindView(R.id.condition_value)
    protected EditText mCondition;

    @BindView(R.id.save_btn)
    protected Button mSaveBtn;

    @BindView(R.id.cancelButton)
    protected Button mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_edit_basic_details);

    }

    @OnClick(R.id.save_btn)
    protected void save() {

        String name = mName.getText().toString();
        String condition = mCondition.getText().toString();
        String phoneNum = mPhoneNumber.getText().toString();
        String patientNum = mPatientNum.getText().toString();
        String location = mLocation.getText().toString();
        String weight = mWeight.getText().toString();

        ContentValues values = new ContentValues();

        int pNum = -1;
        if (!phoneNum.isEmpty()) {
            pNum = Integer.parseInt(phoneNum);
        }

        if (!weight.isEmpty()) {
            values.put(COLUMN_WEIGHT, Integer.parseInt(weight));
        }

        if (!patientNum.isEmpty()) {
            values.put(COLUMN_PHONE_NUMBER, pNum);
        }




        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONDITION, condition);
        values.put(COLUMN_LOCATION, location);
        values.put(PATIENT_NUMBER, patientNum);

        sendToDB(values);

    }

    private void sendToDB(final ContentValues values) {
        SQLiteDatabase db = new HeartRateDBHelper(this).getReadableDatabase();

        //db.update(TABLE_NAME, values, )

        db.close();
    }

    @OnClick(R.id.cancelButton)
    public void cancelEditKin(View view) {

        onBackPressed();

    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
