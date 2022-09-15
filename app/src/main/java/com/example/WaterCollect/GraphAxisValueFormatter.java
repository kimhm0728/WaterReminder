package com.example.WaterCollect;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class GraphAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter {
    private String[] mValues;

    GraphAxisValueFormatter(String[] values){ this.mValues = values; }

    @Override
    public String getFormattedValue(float value) {
        return mValues[(int) value];
    }
}