package com.example.WaterCollect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;

import java.util.Locale;

public class WeekActivity extends Fragment {

    private BarChart barChart;
    private TextView sum;
    private TextView average;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_week, container, false);

        barChart = v.findViewById(R.id.chart);
        sum = v.findViewById(R.id.sum_liter);
        average = v.findViewById(R.id.average_liter);

        ChartSetter chartSetter = new ChartSetter(7);
        barChart = chartSetter.barChartSet(barChart);
        sum.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(chartSetter.getSum())));
        average.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(chartSetter.getAvg())));

        barChart.invalidate(); // 차트 업데이트
        return v;
    }
}













