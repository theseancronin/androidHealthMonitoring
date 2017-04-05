package com.android.shnellers.heartrate.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Sean on 05/04/2017.
 */

public class TimeAxisFormatter implements IAxisValueFormatter {

    long[] values;
    private SimpleDateFormat mFormat;

    public TimeAxisFormatter(long[] values) {
        mFormat = new SimpleDateFormat("HH:mm", Locale.UK); // use one decimal
        this.values = values;
    }

//    @Override
//    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
//        // write your logic here
//
//    }

    @Override
    public String getFormattedValue(final float value, final AxisBase axis) {
        return mFormat.format(values[(int) value]);
    }
}
