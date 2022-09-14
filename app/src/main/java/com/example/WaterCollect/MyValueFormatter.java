package com.example.WaterCollect;

import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

// 소숫점 한 자리까지 보이기 위한 Formatter
public class MyValueFormatter extends ValueFormatter implements IAxisValueFormatter {
    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("#,###,###");
    }

    @Override
    public String getFormattedValue(float value) {
        return mFormat.format((int) value) + "mL";
    }
}