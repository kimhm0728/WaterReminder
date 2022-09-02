package com.example.WaterCollect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.Locale;

public class InputActivity extends AppCompatActivity {

    private Button button;
    private EditText edit;
    private TextView text;
    private String weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        edit = findViewById(R.id.edit);
        text = findViewById(R.id.text);
        text.setText("");

        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(edit.getText())) {
                    weight = edit.getText().toString();
                    String day = Integer.toString(Integer.parseInt(weight)*30);
                    double amount = Double.parseDouble(day);

                    DecimalFormat formatter = new DecimalFormat("#,###");
                    String formatted = formatter.format(amount);

                    text.setText(String.format(Locale.KOREA,"일일 섭취량은 %smL입니다.", formatted));
                }
                else {
                    Toast.makeText(InputActivity.this, "몸무게가 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }

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