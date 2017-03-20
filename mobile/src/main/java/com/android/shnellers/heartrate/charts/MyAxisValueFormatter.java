package com.android.shnellers.heartrate.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Sean on 13/02/2017.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter {

    private ArrayList<String> dates;
    private DecimalFormat mFormat;

    public MyAxisValueFormatter(ArrayList<String> dates) {

        // format values to 1 decimal digit

        this.dates = dates;

        mFormat = new DecimalFormat("###,###,##0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
       // Log.d(TAG, "getFormattedValue: " + String.valueOf(value));
        return dates.get((int) value);
    }

//    /** this is only needed if numbers are returned, else return 0 */
//    @Override
//    public int getDecimalDigits() { return 1; }
}
