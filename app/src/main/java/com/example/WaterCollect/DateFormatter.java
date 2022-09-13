package com.example.WaterCollect;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatter {
    // 날짜를 문자열의 형태로 반환하는 메소드
    // check 1:yyyy-MM-dd, 2: MM/dd
    @NonNull
    public static String DateString(int day, int check) {
        Date todayDate = new Date();
        SimpleDateFormat dateFormat;

        if(check == 1) // MySQL 에 전달할 문자열 format
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        else // Chart 의 x축이 될 문자열 format
            dateFormat = new SimpleDateFormat("MM/dd");
        String todayString = dateFormat.format(todayDate);

        if(day == 0)
            return todayString;
        else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE , -day);
            String beforeDate = dateFormat.format(cal.getTime());
            return beforeDate;
        }

    }

    @NonNull
    public static String DateString(int day) {
        Date todayDate = new Date();
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM");
        String todayString = dateFormat.format(todayDate);

        if(day == 0)
            return todayString;
        else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE , -day);
            String beforeDate = dateFormat.format(cal.getTime());
            return beforeDate;
        }
    }
}
