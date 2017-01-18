package com.android.shnellers.heartrate;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import ernestoyaquello.com.verticalstepperform.*;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

public class DetailStepForm extends AppCompatActivity implements VerticalStepperForm{

    private VerticalStepperFormLayout _stepForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_step_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        _stepForm = (VerticalStepperFormLayout) findViewById(R.id.detail_step_form);

        String[] steps = getResources().getStringArray(R.array.steps_titles);
        int colorPrimary = (ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        int colorPrimaryDark = (ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));

        VerticalStepperFormLayout.Builder.newInstance(_stepForm, steps, this, this)
                .displayBottomNavigation(true)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .init();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public View createStepContentView(int stepNumber) {
        View view = null;

        switch(stepNumber) {
            case 0:
                view = createPersonalDetails();
                break;

        }
        return view;
    }

    private View createPersonalDetails() {
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout l = (LinearLayout) inflater.inflate(
                R.layout.details, null, false
        );


        return l;
    }

    @Override
    public void onStepOpening(int stepNumber) {

    }

    @Override
    public void sendData() {

    }
}
