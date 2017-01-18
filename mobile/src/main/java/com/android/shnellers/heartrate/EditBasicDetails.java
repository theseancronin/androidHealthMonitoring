package com.android.shnellers.heartrate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Sean on 16/11/2016.
 */

public class EditBasicDetails extends Fragment {

    public static final String MOTHER = "Mother";
    public static final String FATHER = "Father";

    private TextView relationship;

    private EditText mEditName, mEditKinName, mEditGPName, mEditPharmacyName;
    private EditText mTelNumber, mKinTelNumber, mGPNumber, mPharmacyNumber, mGPPractice;
    private EditText mLocation, mWeight, mKinRelationship, mDOB, mEmail, mPatientNumber;

    private Button cancelButton;




    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = null;



        setHasOptionsMenu(true);

        Log.i("Edit", getArguments().getString(PassportContract.Passport.EDIT_DETAIL));

        if (getArguments().getString(PassportContract.Passport.EDIT_DETAIL) ==
                PassportContract.Passport.EDIT_NEXT_OF_KIN) {
            view = inflater.inflate(R.layout.passport_edit_kin, container, false);


            registerForContextMenu(relationship);

            relationship.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.showContextMenu();
                }
            });

        } else if (getArguments().getString(PassportContract.Passport.EDIT_DETAIL) ==
                PassportContract.Passport.EDIT_GP_DETAILS) {

            view = inflater.inflate(R.layout.passport_edit_gp_details, container, false);

            mEditGPName = (EditText) view.findViewById(R.id.gpName);
            mGPNumber = (EditText) view.findViewById(R.id.gpTelephone);
            mGPPractice = (EditText) view.findViewById(R.id.gpPractice);

            setEditGPLayout();

        } else {
            view = inflater.inflate(R.layout.passport_edit_basic_details, container, false);
        }

        return view;
    }

    /**
     * Set the edit GP layout;
     */
    private void setEditGPLayout() {

    }

    public void setGPDetails(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.relationship_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.mother:
                relationship.setText(MOTHER);
                return true;
            case R.id.father:
                relationship.setText(FATHER);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Cancel edit.
     *
     * @param view
     */
    public void cancelEditKin(View view) {
        getFragmentManager().popBackStack();
    }
}
