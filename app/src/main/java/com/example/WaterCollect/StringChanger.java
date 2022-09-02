package com.example.WaterCollect;

import java.text.DecimalFormat;

public class StringChanger {
    public static String decimalComma(int s) { // 천단위로 콤마 붙이는 함수
        String number = Integer.toString(s);
        double amount = Double.parseDouble(number);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(amount);
        return formatted;
    }
}
