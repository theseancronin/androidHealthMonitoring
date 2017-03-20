package com.android.shnellers.heartrate.alerts;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.FeelingDatabaseContract;
import com.android.shnellers.heartrate.database.FeelingDatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sean on 12/02/2017.
 *
 * This is a simple dialog fragment that allows the user to record their current feelings.
 */
public class FeelingsDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String SAD = "Sad";
    public static final String NOT_GOOD = "Not Good";
    public static final String NEUTRAL = "Neutral";
    public static final String GOOD = "Good";
    public static final String HAPPY = "Happy";
    @BindView(R.id.icon_sad)
    ImageButton mSadBtn;

    @BindView(R.id.icon_not_good)
    ImageButton mNotGoodBtn;

    @BindView(R.id.icon_neutral)
    ImageButton mNeutralBtn;

    @BindView(R.id.icon_good)
    ImageButton mGoodBtn;

    @BindView(R.id.icon_happy)
    ImageButton mHappyBtn;

    private FeelingDatabaseHelper mDatabaseHelper;

    /**
     * Create a new instance of the dialog
     *
     * @return
     */
    public static FeelingsDialogFragment newInstance() {
        return new FeelingsDialogFragment();
    }

    /**
     * Inflates the dialog view.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feeling_today_layout, container);

        ButterKnife.bind(this, view);

        mSadBtn.setOnClickListener(this);
        mNotGoodBtn.setOnClickListener(this);
        mNeutralBtn.setOnClickListener(this);
        mGoodBtn.setOnClickListener(this);
        mHappyBtn.setOnClickListener(this);

        mDatabaseHelper = new FeelingDatabaseHelper(getActivity());

        return view;
    }


    @Override

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon_sad:
                storeFeelingInDatabase(SAD);
                setFeelingIconInActivity(R.drawable.ic_sad, R.id.icon_sad);
                getDialog().dismiss();
                getActivity().recreate();
                break;
            case R.id.icon_not_good:
                storeFeelingInDatabase(NOT_GOOD);
                setFeelingIconInActivity(R.drawable.ic_not_good, R.id.icon_not_good);
                getDialog().dismiss();
                getActivity().recreate();
                break;
            case R.id.icon_neutral:
                storeFeelingInDatabase(NEUTRAL);
                setFeelingIconInActivity(R.drawable.ic_not_good, R.id.icon_neutral);
                getDialog().dismiss();
                getActivity().recreate();
                break;
            case R.id.icon_good:
                storeFeelingInDatabase(GOOD);
                setFeelingIconInActivity(R.drawable.ic_good, R.id.icon_good);
                getDialog().dismiss();
                getActivity().recreate();
                break;
            case R.id.icon_happy:
                storeFeelingInDatabase(HAPPY);
                setFeelingIconInActivity(R.drawable.ic_happy, R.id.icon_happy);
                getDialog().dismiss();
                getActivity().recreate();
                break;
        }


    }

    private void setFeelingIconInActivity(int iconDrawable, int viewId) {
        //ImageButton btn = (ImageButton) getActivity().findViewById(viewId);

    }

    /**
     * Store the feeling type to the database.
     *
     * @param feelingType
     * @throws SQLiteException
     */
    private void storeFeelingInDatabase(String feelingType) throws SQLiteException {

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeelingDatabaseContract.FeelingConsts.TYPE_COLUMN, feelingType);
        values.put(FeelingDatabaseContract.FeelingConsts.DATE_TIME, System.currentTimeMillis());

        long row = db.insert(FeelingDatabaseContract.FeelingConsts.TABLE_NAME, null, values);

        db.close();

        if (row != -1) {
            Toast.makeText(getActivity(), "Feeling Saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Failed, try again!", Toast.LENGTH_SHORT).show();
        }

    }
}
