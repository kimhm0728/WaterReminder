package com.example.WaterCollect;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {
    // 날짜를 문자열의 형태로 반환하는 메소드
    // check 1:yyyy-MM-dd, 2: MM/dd
    @NonNull
    public static String weekString(int day, int check) {
        Date todayDate = new Date();
        SimpleDateFormat sdf;

        if(check == 1) // MySQL 에 전달할 문자열 format
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        else // Chart 의 x축이 될 문자열 format
            sdf = new SimpleDateFormat("M/d");
        String todayString = sdf.format(todayDate);

        if(day == 0)
            return todayString;
        else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE , -day);
            return sdf.format(cal.getTime());
        }

    }

    // check 1:yyyy-MM, 2:MM
    @NonNull
    public static String monthString(int day, int check) {
        Date todayDate = new Date();
        SimpleDateFormat sdf;

        if(check == 1) // MySQL 에 전달할 문자열 format
            sdf = new SimpleDateFormat("yyyy-MM");
        else // Chart 의 x축이 될 문자열 format
            sdf = new SimpleDateFormat("M");
        String todayString = sdf.format(todayDate);

        if(day == 0)
            return todayString;
        else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH , -day);
            return sdf.format(cal.getTime());
        }
    }

    // yyyy-MM-dd HH:mm:ss
    @NonNull
    public static String nowDateString() {
        Date todayDate = new Date();
        SimpleDateFormat dateFormat;

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String todayString = dateFormat.format(todayDate);

        return todayString;
    }

    public static Calendar stringToCalender(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
        Calendar cal = Calendar.getInstance();
        Date date = null;
        try {
            date = sdf.parse(str);
            cal.setTime(date);
            cal.add(Calendar.MINUTE , 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }
}
