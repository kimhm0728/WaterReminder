package com.example.WaterCollect;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

public class WeekActivity extends Fragment {

    private BarChart barChart;
    private TextView sum;
    private TextView liter;
    private TextView average;
    private TextView liter2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_week, container, false);

        ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
        barChart = v.findViewById(R.id.chart);

        BarData barData = new BarData(); // 차트에 담길 데이터

        entry_chart.add(new BarEntry(1, 1)); //entry_chart1에 좌표 데이터를 담는다.
        entry_chart.add(new BarEntry(2, 2));
        entry_chart.add(new BarEntry(3, 3));
        entry_chart.add(new BarEntry(4, 4));
        entry_chart.add(new BarEntry(5, 2));
        entry_chart.add(new BarEntry(6, 8));

        BarDataSet barDataSet = new BarDataSet(entry_chart, "물 섭취량"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.

        barDataSet.setValueFormatter(new MyValueFormatter());
        barDataSet.setColor(Color.parseColor("#99ffffff"));
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(13f);
        barData.addDataSet(barDataSet);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.setTouchEnabled(false); // 차트 터치 막기
        barChart.setDrawValueAboveBar(true); // 값이 차트 위or아래에 그려질 건지
        barChart.setPinchZoom(false); // 두손가락으로 줌 설정
        barChart.setDrawGridBackground(false); // 격자구조
        barChart.setMaxVisibleValueCount(7); // 그래프 최대 갯수
        barChart.setDescription(null);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setLabelCount(3, true);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGridColor(Color.WHITE);

        YAxis yAxisLeft = barChart.getAxisLeft(); // Y축의 왼쪽면 설정
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setGridColor(Color.parseColor("#4Dffffff"));

        YAxis yAxisRight = barChart.getAxisRight(); // Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        // y축의 활성화를 제거함

        Legend legend = barChart.getLegend(); // 레전드 설정
        legend.setTextColor(Color.WHITE); // 레전드 컬러 설정

        barChart.invalidate(); // 차트 업데이트

        return v;

    }
}













