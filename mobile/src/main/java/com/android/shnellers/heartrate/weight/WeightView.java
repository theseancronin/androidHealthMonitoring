package com.android.shnellers.heartrate.weight;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.shnellers.heartrate.R;
import com.android.shnellers.heartrate.charts.MyAxisValueFormatter;
import com.android.shnellers.heartrate.database.WeightDatabase;
import com.android.shnellers.heartrate.helpers.DateHelper;
import com.android.shnellers.heartrate.models.WeightObject;
import com.android.shnellers.heartrate.views.weight.WeightLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Sean on 09/02/2017.
 */

public class WeightView extends AppCompatActivity {

    private static final String TAG = "WeightView";

    private static final int WEIGHT_LOGGED = 1;

    public static final int TODAYS_WEIGHT_LOG = 1;

    public static final int YESTERDAYS_WEIGHT_LOG = 2;

    private static final boolean DEBUG = true;

    public static final String YESTERDAY_OR_TODAY_LOG = "yesterday_or_today_log";
    public static final String SEVERE = "Status: Severe Weight change detected";
    public static final String OK = "Status: OK";
    public static final String SUSPICIOUS = "Status: Suspicious weight increase detected";

    @BindView(R.id.log_weight)
    Button mLogWeight;

    @BindView(R.id.loss_or_gain)
    TextView mLossOrGain;

    @BindView(R.id.weight_today)
    TextView mTodaysWeight;

    @BindView(R.id.weight_yesterday)
    TextView mYesterdaysWeight;

    @BindView(R.id.bmi_value)
    TextView mBmiValue;

    @BindView(R.id.bmi_status)
    TextView mBmiStatus;

    @BindView(R.id.fluid_status)
    TextView mFluidStatus;

    @BindView(R.id.weight_chart)
    LineChart mWeightChart;

    @BindView(R.id.yesterday_weight_log)
    LinearLayout mLinearLayout;

    private WeightDatabase mWeightDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_main_layout);

        ButterKnife.bind(this);

        mWeightDB = new WeightDatabase(this);

        mWeightChart.getDescription().setText("7 Day Outlook");
        mWeightChart.setTouchEnabled(false);
        XAxis xAxis = mWeightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setValueFormatter(new MyAxisValueFormatter(DateHelper.getLast7DaysAsDate()));
        xAxis.setTextSize(10f);
        //xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//
//            private SimpleDateFormat mFormat = new SimpleDateFormat("dd/mm/yyyy", Locale.UK);
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//
//                long millis = TimeUnit.HOURS.toMillis((long) value);
//                return mFormat.format(new Date(millis));
//            }
//        });

        YAxis leftAxis = mWeightChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(170f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mWeightChart.getAxisRight();
        rightAxis.setEnabled(false);

        setChartDisplay();

        setupWeightDisplay();

    }

    private ArrayList<String> getLast7DaysAsDate() {

        ArrayList<String> dates = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DAY_OF_YEAR, -7);
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);

            dates.add(format.format(calendar.getTime()));
            Log.d(TAG, "displayLast7Days: DATE: " + format.format(calendar.getTime()));
        }

        return dates;

    }

    /**
     * Sets the display of the chart.
     */
    private void setChartDisplay() {
        ArrayList<String> dates = DateHelper.getLast7DaysAsDate();
        ArrayList<WeightObject> weights = mWeightDB.getLast7Days();
        ArrayList<Entry> values = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd MMM", Locale.UK);

        Calendar calendar = Calendar.getInstance();

        if (!weights.isEmpty()) {
            for (int i = 0; i < dates.size(); i++) {

                String date = dates.get(i);

                for (WeightObject weight : weights) {

                    calendar.setTimeInMillis(weight.getDateTime());

                    String dt;

                    if (date.equals(format.format(calendar.getTime()))) {
                        values.add(new Entry((float) i, (float) weight.getWeight()));
                    }
                }
            }
        }


        if (!values.isEmpty()) {
//            for (int x = 0; x < weights.size(); x++) {
//                values.add(new Entry(x, (float) weights.get(x).getWeight()));
//            }

            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(values, "DataSet 1");
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setValueTextColor(ColorTemplate.getHoloBlue());
            set1.setLineWidth(1.5f);
            set1.setDrawCircles(false);
            set1.setDrawValues(false);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);

//            // create a data object with the datasets
//            ArrayList<String> weeklyDates = getLast7DaysAsDate();

            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mWeightChart.setData(data);
        }

    }


    /**
     * This sets up the activity display for the wights.
     */
    private void setupWeightDisplay() {

        WeightObject tWeight = mWeightDB.getTodaysWeight();
        WeightObject yWeight = mWeightDB.getYesterdaysWeight();

        double yesterdayWeight = 0;
        double todaysWeight = 0;

        if (tWeight != null) {
            todaysWeight = tWeight.getWeight();
            setWeightViewValue(mTodaysWeight, todaysWeight);
        } else {
            mTodaysWeight.setText(getString(R.string.no_value));
        }

        if (yWeight != null) {
            yesterdayWeight = yWeight.getWeight();
            setWeightViewValue(mYesterdaysWeight, yesterdayWeight);
        } else {
            mYesterdaysWeight.setText(getString(R.string.no_value));
        }



        // If there is a weight for today and yesterday then we set the weights and get
        // the loss or gained value. This will help monitor fluid build up.
        if (tWeight != null && yWeight != null) {
            DecimalFormat oneDigit = new DecimalFormat("#,##0.0");//format to 1 decimal place
            double lossOrGain = Double.valueOf(oneDigit.format(todaysWeight - yesterdayWeight));
            setWeightViewValue(mLossOrGain, lossOrGain);
            if (lossOrGain >= 1.5) {
                setFluidAccumulationStatus(SEVERE);
            } else if (lossOrGain >= 1) {
                setFluidAccumulationStatus(SUSPICIOUS);
            } else {
                setFluidAccumulationStatus(OK);
            }
        } else {
            mLossOrGain.setText(getString(R.string.no_value));
        }
    }

    private void setFluidAccumulationStatus(String warningType) {

        mFluidStatus.setText(warningType);

        String errorTxt;

        if (warningType.equals(SEVERE) || warningType.equals(SUSPICIOUS)) {

            if (warningType.equals(SEVERE)) {
                errorTxt = SEVERE;
            } else {
                errorTxt = SUSPICIOUS;
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_error_white)
                    .setContentTitle("Weight Warning")
                    .setContentText(errorTxt);
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.notify(1, builder.build());
        }

    }

    private void setWeightViewValue(TextView lossOrGain, double weight) {

        String weightStr = String.valueOf(weight) + " ";
        lossOrGain.setText(weightStr);

    }

    /**
     * Checks to see if the passed dateTime parameter is yesterdays date.
     *
     * @param dateTime
     * @return
     */
    private boolean isYesterdaysDate(long dateTime) {

        boolean isYesterdaysDate = false;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        long yDate = calendar.getTimeInMillis();
        String yesterdaysDate = convertLongToDate(yDate);

        String testDate = convertLongToDate(dateTime);

        Log.d(TAG, "isYesterdaysDate: " + yesterdaysDate);
        Log.d(TAG, "isYesterdaysDate: " + testDate);

        if (yesterdaysDate.equals(testDate)) {
            isYesterdaysDate = true;
            Log.d(TAG, "isYesterdaysDate: IS YESTERDAY");
        }

        return isYesterdaysDate;

    }

    /**
     * Takes a long value as argument and converts into a date.
     *
     * @param dateTime
     * @return
     */
    private String convertLongToDate(long dateTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);

        String todaysDate = calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR);

        return todaysDate;
    }

    /**
     * Tests whether the passed dateTime parameter is todays date.
     *
     * @param dateTime long value to be used to determine date.
     * @return
     */
    private boolean isTodaysDate(long dateTime) {

        boolean isTodaysDate = false;

        String todaysDate = convertLongToDate(System.currentTimeMillis());

        String testDate = convertLongToDate(dateTime);

        Log.d(TAG, "isTodaysDate: " + todaysDate);
        Log.d(TAG, "isTodaysDate: " + testDate);

        if (todaysDate.equals(testDate)) {
            isTodaysDate = true;
        }

        return isTodaysDate;
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @OnClick(R.id.log_weight)
    public void logWeight() {
        Intent intent = new Intent(this, WeightLog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(YESTERDAY_OR_TODAY_LOG, TODAYS_WEIGHT_LOG);
        startActivityForResult(intent, WEIGHT_LOGGED);
    }

    @OnClick(R.id.yesterday_weight_log)
    protected void logYesterdaysWeight() {
        Intent intent = new Intent(this, WeightLog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(YESTERDAY_OR_TODAY_LOG, YESTERDAYS_WEIGHT_LOG);
        startActivityForResult(intent, WEIGHT_LOGGED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult: ");
        if (requestCode == WEIGHT_LOGGED) {
            if (resultCode == RESULT_OK) {
                setupWeightDisplay();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
