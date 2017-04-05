package com.android.shnellers.heartrate;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

/**
 * Created by Sean on 05/04/2017.
 */

public class StartDetailsForm extends AppCompatActivity implements VerticalStepperForm{

    @BindView(R.id.vertical_stepper_form)
    protected VerticalStepperFormLayout verticalStepperForm;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_form);
;
        ButterKnife.bind(this);

        String[] mySteps = {"Name", "Email", "Phone Number"};
        int colorPrimary = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
        int colorPrimaryDark = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);

        // Finding the view
      //  verticalStepperForm = (VerticalStepperFormLayout) findViewById(R.id.vertical_stepper_form);

        // Setting up and initializing the form
        VerticalStepperFormLayout.Builder.newInstance(verticalStepperForm, mySteps, this, this)
                .primaryColor(colorPrimary)
                .primaryDarkColor(colorPrimaryDark)
                .displayBottomNavigation(true) // It is true by default, so in this case this line is not necessary
                .init();


    }

    @Override
    public View createStepContentView(final int stepNumber) {
        View view = null;
        switch (stepNumber) {
            case 0:
                view = createNameStep();
                break;
            case 1:
                view = createEmailStep();
                break;
            case 2:
                view = createPhoneNumberStep();
                break;
        }
        return view;
    }
    private View createNameStep() {
        // Here we generate programmatically the view that will be added by the system to the step content layout
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint("Your name");
        return name;
    }

    private View createEmailStep() {
        // In this case we generate the view by inflating a XML file
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        LinearLayout emailLayoutContent = (LinearLayout) inflater.inflate(R.layout.email_layout, null, false);
        EditText email = (EditText) emailLayoutContent.findViewById(R.id.email);
        return emailLayoutContent;
    }

    private View createPhoneNumberStep() {
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        //LinearLayout phoneLayoutContent = (LinearLayout) inflater.inflate(R.layout.phone_step_layout, null, false);
        return null;
    }


    @Override
    public void onStepOpening(final int stepNumber) {

    }

    @Override
    public void sendData() {

    }
}
