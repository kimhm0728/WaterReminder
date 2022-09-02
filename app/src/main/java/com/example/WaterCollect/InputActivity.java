package com.example.WaterCollect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class InputActivity extends AppCompatActivity {

    private TextView text;
    private String weight;
    private NumberPicker weight_picker;
    private ImageButton xBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        text = findViewById(R.id.text);
        weight_picker = findViewById(R.id.weight);
        xBtn = findViewById(R.id.x_btn);
        weight_picker.setMaxValue(200);
        weight_picker.setMinValue(0);
        weight_picker.setValue(0);

        weight_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
                weight = Integer.toString(newValue);
                String day = StringChanger.decimalComma(newValue*30);

                text.setText(String.format(Locale.KOREA,"하루 권장량은 %smL입니다.", day));
            }
        });

        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("weight", weight);
                setResult(Activity.RESULT_OK, intent);
                finish();
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