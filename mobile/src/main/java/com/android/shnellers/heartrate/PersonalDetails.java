package com.android.shnellers.heartrate;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.shnellers.heartrate.database.GPContract;
import com.android.shnellers.heartrate.database.GPDatabase;
import com.android.shnellers.heartrate.database.KinContract;
import com.android.shnellers.heartrate.database.KinDatabase;
import com.android.shnellers.heartrate.database.UserContract;
import com.android.shnellers.heartrate.database.UserDatabase;
import com.android.shnellers.heartrate.database.UserDatabaseHelper;
import com.android.shnellers.heartrate.fragments.DetailFragment;
import com.android.shnellers.heartrate.fragments.PassportInfoFragment;

import java.util.ArrayList;
import java.util.HashMap;

import static com.android.shnellers.heartrate.PassportContract.Passport.EDIT;
import static com.android.shnellers.heartrate.PassportContract.Passport.IS_BLOCK_HEADING;
import static com.android.shnellers.heartrate.PassportContract.Passport.PHONE_NUMBER;
import static com.android.shnellers.heartrate.PassportContract.Passport.WEIGHT;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NAME;
import static com.android.shnellers.heartrate.database.UserContract.UserEntry.PHARMACY_NUMBER;


/**
 * Created by Sean on 23/10/2016.
 */

public class PersonalDetails extends Fragment {

    private static final String TAG = "Personal.Details";
    public static final String CONDITION = "Condition";

    private TextInputEditText mName, mAge, mCondition, mWeight, mLocation,
                     mEmail, mPhonenumber;

    private UserDatabaseHelper mUserDatabaseHelper;

    private UserDatabase db;
    private GPDatabase mGPDatabase;
    private KinDatabase mKinDatabase;

    private User user;

    private SessionManager session;

   // private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    private LinearLayout rootLayout;

    private ArrayList<DetailFragment> info;

    private View view;

    private  String name, age, condition, email, location, phoneNumber, weight;

    private boolean clickable;

    public PersonalDetails () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.display_personal_details, container, false);

        mName = (TextInputEditText) view.findViewById(R.id.detailsName);
        mAge = (TextInputEditText) view.findViewById(R.id.detailsAge);
        mCondition = (TextInputEditText) view.findViewById(R.id.detailsCondition);
        mWeight = (TextInputEditText) view.findViewById(R.id.detailsWeight);
        mLocation = (TextInputEditText) view.findViewById(R.id.detailsLocation);
        mEmail = (TextInputEditText) view.findViewById(R.id.detailsEmail);
        mPhonenumber = (TextInputEditText) view.findViewById(R.id.detailsPhoneNumber);

        info = new ArrayList<>();

        mGPDatabase = new GPDatabase(getActivity());
        mKinDatabase = new KinDatabase(getActivity());

        clickable = false;

        if (view.findViewById(R.id.mainContainer) != null) {
            rootLayout = (LinearLayout) view.findViewById(R.id.passportBasicContainer);

            setupBasicInfo();
            setNextOfKin();
            setCondition();
            setGPDetails();
            setPharmacyDetails();
        }

        session = new SessionManager(getActivity().getApplicationContext());

        Log.d(TAG, "onCreateView");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

//



        Log.d(TAG, "onStart");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent dat = getActivity().getIntent();

        Log.d(TAG, "Request Code = " + Integer.toString(requestCode));
        Log.d(TAG, "Result Code = " + Integer.toString(resultCode));
        Bundle extras = dat.getBundleExtra("key");
        if (requestCode == 999){
            Log.d(TAG, "foundExtras");
           // String strtext =   extras.getString("key");

       //     TextView
//            List<Fragment> fragments = getChildFragmentManager().getFragments();
//            if (fragments != null) {
//                for (Fragment fragment : fragments) {
//                    getChildFragmentManager().beginTransaction().remove(fragment).commit();
//                }
//            }
//
//            setupBasicInfo();
//            setNextOfKin();
//            setCondition();
//            setGPDetails();
//            setPharmacyDetails();

            //Log.d(TAG, strtext);
        }

        Log.d(TAG, "onActivityResult");
    }

    /**
     * Set the basic information for the patient.
     *
     */
    private void setupBasicInfo() {

        // Initialize the detail fragments
        DetailFragment basicInfo = new DetailFragment();
        PassportInfoFragment name = new PassportInfoFragment();
        PassportInfoFragment medID = new PassportInfoFragment();
        PassportInfoFragment dob = new PassportInfoFragment();
        PassportInfoFragment weight = new PassportInfoFragment();
        PassportInfoFragment location = new PassportInfoFragment();
        PassportInfoFragment phoneNumber = new PassportInfoFragment();
        PassportInfoFragment email = new PassportInfoFragment();
        PassportInfoFragment condition = new PassportInfoFragment();


        String onClick = "editBasicDetails";

        // Bundle the arguments for the fragments
        basicInfo.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.BASIC_INFO,
                        EDIT,
                        IS_BLOCK_HEADING,
                        false,
                        isClickable(),
                        PassportContract.Passport.EDIT_BASIC_DETAILS));

        name.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.NAME,
                        "Sean Cronin",
                        false,
                        false,
                        isNotClickable(),
                        null));
        medID.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.PATIENT_NUMBER,
                        "PT1234567",
                        false,
                        false,
                        isNotClickable(),
                        null));
        dob.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.DOB,
                        "20/01/1985",
                        false,
                        false,
                        isNotClickable(),
                        null));
        weight.setArguments(
                createBundleOfArguments(
                        WEIGHT,
                        "75kg",
                        false,
                        false,
                        isNotClickable(),
                        null));
        location.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.LOCATION,
                        "Cork",
                        false,
                        false,
                        isNotClickable(),
                        null));
        phoneNumber.setArguments(
                createBundleOfArguments(
                        PHONE_NUMBER,
                        "086 12345676",
                        false,
                        false,
                        isNotClickable(),
                        null));
        email.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.EMAIL,
                        "s@s.com",
                        false,
                        false,
                        isNotClickable(),
                        null));
        condition.setArguments(
                createBundleOfArguments(
                        CONDITION,
                        "Double bypass.",
                        false,
                        false,
                        isNotClickable(),
                        null));


        // Get the fragment manager and begin transaction
        FragmentManager mFragmentManager = getChildFragmentManager();
        FragmentTransaction ft  = mFragmentManager.beginTransaction();

        // Add the fragment to the root fragment
        ft.add(R.id.passportBasicContainer, basicInfo);
        ft.add(R.id.passportBasicContainer, name);
        ft.add(R.id.passportBasicContainer, medID);
        ft.add(R.id.passportBasicContainer, dob);
        ft.add(R.id.passportBasicContainer, weight);
        ft.add(R.id.passportBasicContainer, location);
        ft.add(R.id.passportBasicContainer, phoneNumber);
        ft.add(R.id.passportBasicContainer, email);
        ft.add(R.id.passportBasicContainer, condition);

        ft.addToBackStack(basicInfo.getTag());
        ft.addToBackStack(name.getTag());
        ft.addToBackStack(medID.getTag());
        ft.addToBackStack(dob.getTag());
        ft.addToBackStack(weight.getTag());
        ft.addToBackStack(location.getTag());
        ft.addToBackStack(phoneNumber.getTag());
        ft.addToBackStack(email.getTag());
        ft.addToBackStack(condition.getTag());

        ft.commit();


    }

    /**
     * Set the details of the next of kin.
     */
    private void setNextOfKin() {

        Bundle nameBundle = new Bundle();
       // nameBundle.putInt("id", R.id.kin_name);
        Bundle relBundle = new Bundle();
        //relBundle.putInt("id", R.id.kin_relationship);
        Bundle telBundle = new Bundle();
        // telBundle.putInt("id", R.id.kin_telephone);

        // Initialize the fragments for the kin details
        DetailFragment nextOfKin = new DetailFragment();
        PassportInfoFragment name = new PassportInfoFragment();
        name.setArguments(nameBundle);
        PassportInfoFragment relationship = new PassportInfoFragment();
        PassportInfoFragment telephone = new PassportInfoFragment();

        Cursor kin = mKinDatabase.getKinDetails();

        int telephoneIndex = 0;
        int nameIndex = 0;
        int relationshipIndex = 0;

        if (kin != null) {

            kin.moveToFirst();

            nameIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_NAME);
            relationshipIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_RELATIONSHIP);
            telephoneIndex = kin.getColumnIndex(KinContract.KinEntry.COLUMN_KIN_TELEPHONE);
        }



        // Set the next of kin heading
        nextOfKin.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.NEXT_OF_KIN,
                        EDIT,
                        IS_BLOCK_HEADING,
                        true,
                        isClickable(),
                        PassportContract.Passport.EDIT_NEXT_OF_KIN));

        // set the next of kin name
        name.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.NAME,
                        kin != null ? kin.getString(nameIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        // set the next of kin relationship
        relationship.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.RELATIONSHIP,
                        kin != null ? kin.getString(relationshipIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        telephone.setArguments(
                createBundleOfArguments(
                        PHONE_NUMBER,
                        kin != null ? kin.getString(telephoneIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Add the fragments to the parent node
        ft.add(R.id.passportKinContainer, nextOfKin);
        ft.add(R.id.passportKinContainer, name);
        ft.add(R.id.passportKinContainer, relationship);
        ft.add(R.id.passportKinContainer, telephone);

        ft.commit();

    }

    /**
     * Set the details of the next of kin.
     */
    private void setGPDetails() {

        // Initialize the fragments for the GP details
        DetailFragment gpDetails = new DetailFragment();
        PassportInfoFragment gpPractice = new PassportInfoFragment();
        PassportInfoFragment gpName = new PassportInfoFragment();
        PassportInfoFragment gpTelephone = new PassportInfoFragment();
        PassportInfoFragment consultantsName = new PassportInfoFragment();

        Cursor gp = mGPDatabase.getGPDetails();
        int nameIndex = 0;
        int practiceIndex = 0;
        int telephoneIndex = 0;

        if (gp != null) {

            gp.moveToFirst();

            nameIndex = gp.getColumnIndex(GPContract.GPEntry.COLUMN_GP_NAME);
            practiceIndex = gp.getColumnIndex(GPContract.GPEntry.COLUMN_GP_PRACTICE);
            telephoneIndex = gp.getColumnIndex(GPContract.GPEntry.COLUMN_GP_TELEPHONE);
        }



        //Log.d("Name", );

        // Set the arguments for the GP details
        gpDetails.setArguments(
                createBundleOfArguments(
                        "GP Details",
                        EDIT,
                        IS_BLOCK_HEADING,
                        true,
                        isClickable(),
                        PassportContract.Passport.EDIT_GP_DETAILS));

        gpPractice.setArguments(
                createBundleOfArguments("Practice",
                        gp != null ? gp.getString(practiceIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        gpName.setArguments(
                createBundleOfArguments("Name",
                        gp != null ? gp.getString(telephoneIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        gpTelephone.setArguments(
                createBundleOfArguments(
                        PHONE_NUMBER,
                        gp != null ? gp.getString(nameIndex) : "",
                        false,
                        false,
                        isNotClickable(),
                        null));

        consultantsName.setArguments(
                createBundleOfArguments("Consultants Name", "Dr Peter Kelly", false, false,
                        isNotClickable(),
                        null));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // Add the fragments to the parent node
        ft.add(R.id.passportGPContainer, gpDetails);
        ft.add(R.id.passportGPContainer, gpPractice);
        ft.add(R.id.passportGPContainer, gpName);
        ft.add(R.id.passportGPContainer, gpTelephone);

        ft.commit();

    }

    /**
     * Set the details of the next of kin.
     */
    private void setPharmacyDetails() {

        // Initialize the fragments for the pharmacy details
        DetailFragment pharmDetails = new DetailFragment();
        PassportInfoFragment name = new PassportInfoFragment();
        PassportInfoFragment telephone = new PassportInfoFragment();


        HashMap<String, String> fields = new UserDatabase(getActivity()).getPharmacyDetails();

        String nameStr = "";
        String number = "";

        if (fields != null) {

            if (fields.get(PHARMACY_NAME) != null) {
                nameStr = fields.get(PHARMACY_NAME);
            }

            if (fields.get(PHARMACY_NUMBER) != null) {
                number = fields.get(PHARMACY_NUMBER);
            }
        }


        // Set the arguments for the pharmacy details
        pharmDetails.setArguments(
                createBundleOfArguments(
                        "Pharmacy Details",
                        EDIT,
                        IS_BLOCK_HEADING,
                        true,
                        isClickable(),
                        PassportContract.Passport.EDIT_PHARMACY_DETAILS));
        name.setArguments(
                createBundleOfArguments(
                        PassportContract.Passport.NAME,
                        nameStr,
                        false,
                        false,
                        isNotClickable(),
                        null));
        telephone.setArguments(
                createBundleOfArguments(
                        PHONE_NUMBER,
                        number,
                        false,
                        false,
                        isNotClickable(),
                        null));

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft  = fm.beginTransaction();

        // Add the fragments to the parent node
        ft.add(R.id.passportPharmacyContainer, pharmDetails);
        ft.add(R.id.passportPharmacyContainer, name);
        ft.add(R.id.passportPharmacyContainer, telephone);

        ft.commit();

    }

    private void setCondition() {



    }

    /**
     * This method is responsible for creating a bundle of arguments,
     * which are passed to a fragment.
     *
     * @param key
     * @param value
     * @return
     */
    private Bundle createBundleOfArguments(final String key,
                                           final String value,
                                           final boolean isHeading,
                                           final boolean addMargin,
                                           final boolean clickable,
                                           final String onClick) {

        Bundle args = new Bundle();

        args.putString(DetailFragment.KEY, key);
        args.putString(DetailFragment.VALUE, value);
        args.putBoolean(PassportContract.Passport.IS_HEADING, isHeading);
        args.putBoolean(PassportContract.Passport.ADD_MARGIN, addMargin);
        args.putBoolean("clickable", clickable);
        args.putString("onClick", onClick);

        return args;

    }

    /**
     * Takes the user details from the DB that we already have,
     * and fills the details form.
     */
    private void _displayUserDetails() {
        user = session.getLoggedInUser();
        Log.i("Age USer", user.get_age());

        if (!user.get_name().isEmpty()) {
            mName.setText(user.get_name());
            name = user.get_name();
        }

        if (!user.get_age().toString().isEmpty()) {
            mAge.setText(user.get_age());
            age = user.get_age();
        }

        if (!user.get_condition().isEmpty()) {
            mCondition.setText(user.get_condition());
            condition = user.get_condition();
        }

        if (!user.get_location().isEmpty()) {
            mLocation.setText(user.get_location());
            location = user.get_name();
        }

        if (!user.get_age().toString().isEmpty()) {
            mAge.setText(user.get_age());
            age = user.get_age();
        }

        if (!user.get_weight().isEmpty()) {
            mWeight.setText(user.get_weight());
            weight = user.get_weight();
        }

        if (!user.get_condition().isEmpty()) {
            mPhonenumber.setText(user.get_phoneNumber());
            phoneNumber = user.get_phoneNumber();
        }

        mEmail.setText(user.get_email());
        email = user.get_email();

    }

    /**
     * Save the usr details.
     *
     * @param view
     */
    public void saveUserDetails(View view) {

        // Get the values of the edit text fields
        String name = mName.getText().toString();
        String age = mAge.getText().toString();
        String condition = mCondition.getText().toString();
        String weight = mWeight.getText().toString();
        String location = mLocation.getText().toString();
        String phoneNumber = mPhonenumber.getText().toString();

        // If the entered details are OK, we add the details to the content
        // values collection and then update the users details.
        if (validUserDetails(name, age, condition) == true) {
            db.open();

            ContentValues values = new ContentValues();
            values.put(UserContract.UserEntry.COLUMN_NAME, name);
            values.put(UserContract.UserEntry.COLUMN_AGE, age);
            values.put(UserContract.UserEntry.COLUMN_CONDITION, condition);
            values.put(UserContract.UserEntry.COLUMN_WEIGHT, weight);
            values.put(UserContract.UserEntry.COLUMN_LOCATION, location);
            values.put(UserContract.UserEntry.COLUMN_CONDITION, phoneNumber);

            // update the user details in the database
            db.updateUserDetails(name, age, condition, email,
                                 weight, location, phoneNumber, values);

            // Creates a new user, to overwrite the logged in user as the details have
            // been updated.
            User user = new User(name, age, email, condition,
                                 weight, location, phoneNumber);

            // Store the updated user in the session.
            session.storeUserData(user);

            db.close();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);



        }

    }

    /**
     * Cancel the editing of the user details. This simple returns to
     * the previous screen without making any changes.
     *
     * @param view
     */
    public void cancelEditDetails(View view) {

        if (validUserDetails(name, age, condition) == true) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Validates the user details.
     *
     * @param name
     * @param age
     * @param condition
     * @return
     */
    private boolean validUserDetails(String name, String age, String condition) {
        boolean valid = true;

        Log.i("AGE", age);
        if (age.isEmpty()) {
            age = "0";
        }
        int userAge = Integer.parseInt(age);

        if (name.isEmpty()) {
            valid = false;
            Toast.makeText(getActivity().getBaseContext(), "Name required", Toast.LENGTH_LONG).show();
        } else if (userAge < 0 || userAge > 110) {
            valid = false;
            Toast.makeText(getActivity().getBaseContext(), "Age out of bounds", Toast.LENGTH_LONG).show();
        }

        return valid;
    }

    /**
     * Allow text view to be clickable.
     *
     * @return
     */
    private boolean isClickable() {
        return true;
    }

    /**
     * Don't allow text view to be clickable.
     *
     * @return
     */
    private boolean isNotClickable() {
        return false;
    }

    public void openRelationshipMenu(View view) {
        view.showContextMenu();
    }

    /**
     * Cancel edit.
     *
     * @param view
     */
    public void cancelEdit(View view) {



        getFragmentManager().popBackStack();
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
