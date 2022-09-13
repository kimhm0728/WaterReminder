package com.example.WaterCollect;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ChartSetter {
    private ArrayList<BarEntry> entry_chart = new ArrayList<>(); // 데이터를 담을 Arraylist
    private int intakeSum = 0;
    private String[] days;
    private int type; // 주, 월을 구분

    // MySQL 에서 섭취량을 가져오는 생성자
    public ChartSetter(int type) {
        this.type = type;
        String date; // query 검색을 위한 날짜 문자열
        String result; // MySQL query 결과값
        final DataInserter[] task = new DataInserter[1];
        int intake = 0;
        days = new String[type];

        for(int i=0; i<type; i++) {
            task[0] = new DataInserter();
            if(type == 7) {
                date = DateFormatter.DateString(i, 1); // MySQL 쿼리를 위한 yyyy-MM-dd 문자열
                days[i] = DateFormatter.DateString(i, 2); // x축에 넣기 위한 MM/dd 문자열
            }
            else
                date = DateFormatter.DateString(i);

            try {
                result = task[0].execute("http://" + MainActivity.IP_ADDRESS + "/query.php", MainActivity.device, date, "receive").get();
                intake = Integer.parseInt(result);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            entry_chart.add(new BarEntry(i, intake));
            intakeSum += intake;
        }
    }

    // BarChart 에 대한 설정
    public BarChart barChartSet(BarChart barChart) {
        BarDataSet barDataSet = new BarDataSet(entry_chart, "물 섭취량"); // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환
        barDataSet.setValueFormatter(new MyValueFormatter());
        barDataSet.setColor(Color.parseColor("#99ffffff"));
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(13f);

        BarData barData = new BarData(); // 차트에 담길 데이터
        barData.addDataSet(barDataSet);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(1000);
        barChart.setTouchEnabled(false); // 차트 터치 막기
        barChart.setDrawValueAboveBar(true); // 값이 차트 위or아래에 그려질 건지
        barChart.setPinchZoom(false); // 두손가락으로 줌 설정
        barChart.setDrawGridBackground(false); // 격자구조
        barChart.setMaxVisibleValueCount(type); // 그래프 최대 갯수
        barChart.setDescription(null);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setLabelCount(type, true);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGridColor(Color.WHITE);

        YAxis yAxisLeft = barChart.getAxisLeft(); // Y축의 왼쪽면 설정
        yAxisLeft.setTextColor(Color.WHITE);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setGridColor(Color.parseColor("#4Dffffff"));

        YAxis yAxisRight = barChart.getAxisRight(); // Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        // y축의 활성화를 제거함

        Legend legend = barChart.getLegend(); // 레전드 설정
        legend.setTextColor(Color.WHITE); // 레전드 컬러 설정

        barChart.invalidate(); // 차트 업데이트

        return barChart;

    }
    public int getSum() {
        return intakeSum;
    }
    public int getAvg() { return (int)((double)intakeSum/type); }
}