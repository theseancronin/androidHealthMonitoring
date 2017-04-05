package com.android.shnellers.heartrate.views.weight;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.database.WeightDBContract;
import com.android.shnellers.heartrate.database.WeightDBHelper;
import com.android.shnellers.heartrate.weight.WeightView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.ContentValues.TAG;
import static com.android.shnellers.heartrate.weight.WeightView.YESTERDAYS_WEIGHT_LOG;
import static com.android.shnellers.heartrate.weight.WeightView.YESTERDAY_OR_TODAY_LOG;

/**
 * Created by Sean on 03/02/2017.
 */

public class WeightLog extends Activity {

    @BindView(R.id.weight_value)
    EditText mWeightValue;

    @BindView(R.id.weight_type)
    Spinner mWeightType;

    @BindView(R.id.save_btn)
    protected Button mSaveButton;

    @BindView(R.id.cancel_btn)
    protected Button mCancelBtn;

    private WeightDBHelper mWeightDBHelper;

    private int mYesterdayOrTodayLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_log_layout);

        ButterKnife.bind(this);

        mWeightDBHelper = new WeightDBHelper(this);

        mYesterdayOrTodayLog = getIntent().getIntExtra(YESTERDAY_OR_TODAY_LOG, -1);

        setTypeSpinner();
    }

    private void setTypeSpinner() {

        ArrayAdapter<CharSequence> weightTypesList = ArrayAdapter.createFromResource(
                this, R.array.weight_types, android.R.layout.simple_spinner_item
        );

        weightTypesList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mWeightType.setAdapter(weightTypesList);

    }

    /**
     * Saves the inputted weight by the user.
     *
     * @throws SQLiteException
     */

    @OnClick(R.id.save_btn)
    protected void saveWeight() throws SQLiteException {

        SQLiteDatabase db = mWeightDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        // Create the format for both date and time to be entered into the database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat timeFormat = new SimpleDateFormat(("HH:mm"), Locale.UK);

        // Initialize tDate object to get the date
        Date dateTime = new Date();

        Calendar calendar = Calendar.getInstance();

        Log.d(TAG, "saveWeight: " + String.valueOf(mYesterdayOrTodayLog));
        if (mYesterdayOrTodayLog == YESTERDAYS_WEIGHT_LOG) {
            Log.d(TAG, "YESTERDAYS: ");
            calendar.add(Calendar.DATE, -1);
        }

        String weightStr = mWeightValue.getText().toString();

        if (!weightStr.isEmpty()) {
            double weight = Double.parseDouble(weightStr);
            String type = mWeightType.getSelectedItem().toString();

            // Build the content values
            values.put(WeightDBContract.WeightEntries.WEIGHT_COLUMN, weight);
            values.put(WeightDBContract.WeightEntries.TYPE, type);
            values.put(WeightDBContract.WeightEntries.DATE_TIME_COLUMN, calendar.getTimeInMillis());
            values.put(WeightDBContract.WeightEntries.DATE, dateFormat.format(calendar.getTimeInMillis()));

            // Insert the weight into the database
            long row = db.insert(WeightDBContract.WeightEntries.TABLE_NAME, null, values);

            // Display toast on success
            if (row != -1) {
                Toast.makeText(getApplicationContext(), "Weight Logged", Toast.LENGTH_SHORT).show();
            }

            mWeightValue.getText().clear();

            // Close the db
            db.close();

            finishIntent(RESULT_OK);


        } else {
            // Close the db
            db.close();
            finishIntent(RESULT_CANCELED);
        }











    }

    @OnClick(R.id.cancel_btn)
    protected void cancelWeightLog() {
        finishIntent(RESULT_CANCELED);
    }

    private void finishIntent(final int result) {
        Intent intent = new Intent(this, WeightView.class);
        setResult(result, intent);
        finish();
    }
}
