package com.example.WaterCollect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Locale;

public class inputActivity extends AppCompatActivity {

    private Button button;
    private Button button2;
    private EditText edit;
    private TextView text;
    private TextView text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        edit = findViewById(R.id.edit);
        text = findViewById(R.id.text);

        text.setText("");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int weight = Integer.parseInt(edit.getText().toString());
                String day = Integer.toString(weight*30);
                double amount = Double.parseDouble(day);
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formatted = formatter.format(amount);

                Intent intent = getIntent();
                intent.putExtra("WEIGHT", 0);
                text.setText(String.format(Locale.KOREA,"일일 섭취량은 %smL입니다.", formatted));
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}