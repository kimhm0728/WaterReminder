package com.example.WaterCollect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Locale;

public class InputActivity extends AppCompatActivity {

    private TextView text;
    private String weight;
    private NumberPicker number_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        text = findViewById(R.id.text);
        text.setText("");
        number_picker=findViewById(R.id.number_picker);
        number_picker.setMaxValue(200);
        number_picker.setMinValue(0);
        number_picker.setValue(0);


        number_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
                String day = Integer.toString(newValue*30);
                double amount = Double.parseDouble(day);

                DecimalFormat formatter = new DecimalFormat("#,###");
                String formatted = formatter.format(amount);

                text.setText(String.format(Locale.KOREA,"일일 섭취량은 %smL입니다.", formatted));
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("weight", weight);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}