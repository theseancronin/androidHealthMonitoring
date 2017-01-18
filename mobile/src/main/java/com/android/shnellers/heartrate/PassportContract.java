package com.android.shnellers.heartrate;

import android.provider.BaseColumns;

/**
 * Created by Sean on 15/11/2016.
 */

public class PassportContract {

    private PassportContract () {}

    public class Passport implements BaseColumns {
        public static final String BASIC_INFO = "Basic Info";
        public static final String NAME = "Name";
        public static final String DOB = "DOB";
        public static final String WEIGHT = "Weight";
        public static final String LOCATION = "Location";
        public static final String PHONE_NUMBER = "Telephone";
        public static final String EMAIL = "Email";
        public static final String PATIENT_NUMBER = "Patient Number";
        public static final String EDIT = "Edit";
        public static final String NEXT_OF_KIN = "Next of Kin";
        public static final String ADD_MARGIN = "addMargin";
        public static final String IS_HEADING = "isHeading";
        public static final String RELATIONSHIP = "Relationship";
        public static final String CLICKABLE = "clickable";
        public static final String ON_CLICK_METHOD = "onClick";
        public static final boolean IS_BLOCK_HEADING = true;

        //Editing method calls
        public static final String EDIT_DETAIL = "editDetails";
        public static final String EDIT_BASIC_DETAILS = "editBasicDetails";
        public static final String EDIT_NEXT_OF_KIN = "editNextOfKin";
        public static final String EDIT_GP_DETAILS= "editGPDetails";
        public static final String EDIT_PHARMACY_DETAILS = "editPharmacyDetails";
    }

}
