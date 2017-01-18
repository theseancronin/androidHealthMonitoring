package com.android.shnellers.heartrate.fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.shnellers.heartrate.R;


/**
 * Created by Sean on 21/11/2016.
 */

public class EditPassportBasicDetails extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passport_edit_basic_details);

    }

    public void cancelEditKin(View view) {

        onBackPressed();

    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
