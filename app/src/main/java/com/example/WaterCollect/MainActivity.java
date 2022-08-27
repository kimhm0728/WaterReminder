package com.example.WaterCollect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

/* 디버깅 해야될 것
1. 메뉴 버튼 눌렀을 때 이벤트(지금은 드래그했을 때만 됨)
2. 메뉴 각각 눌렀을 때 화면 넘어가는 거(구현은 다 했는데 안넘어감)
아마 메뉴가 구현이 안되어서 그런듯 그리고 화면끼리 데이터보내는것도해야함
3. weight 입력 안됐을 때 입력하라는 텍스트 안 나옴
4. 뒤로가기 눌렀을 때 메뉴 꺼지는 거

* */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main_Activity";

    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    private ImageView ivMenu;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private ProgressBar pBar;
    private int weight = 0;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivMenu=findViewById(R.id.iv_menu);
        drawerLayout=findViewById(R.id.drawer);
        toolbar=findViewById(R.id.toolbar);

        int water = 100; // DB에서 가져온 무게센서 데이터
        int day = 0; // 하루 권장 섭취량
        int ratio = 0; // 권장량 달성비율
        text1 = findViewById(R.id.text1);
        text1.setText(String.format(Locale.KOREA,"오늘의 물 섭취량 %smL", decimalChange(water)));

        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);

        if(weight == 0) {
            text2.setText(String.format("하루 권장량을 설정해주세요"));
            text3.setText(" ");
        }
        else {
            day = weight * 30; // 몸무게 * 30 -> 하루 수분 권장량
            ratio = (int) (((double) water / day) * 100); // 권장량 달성비율
            text2.setText(String.format(Locale.KOREA, "하루 권장량 %smL 중 %d%% 달성", decimalChange(day), ratio));
            text3.setText(String.format(Locale.KOREA, "남은 섭취량 %smL", decimalChange(day - water)));
        }
        pBar = findViewById(R.id.progressBar);
        pBar.setProgress(ratio); // 하루 권장량 달성비율만큼 progressBar에 적용

        // 메뉴바 클릭 이벤트

        setSupportActionBar(toolbar);

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: 클릭됨");
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        button = findViewById(R.id.button3); // 입력
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inputIntent = new Intent(getApplicationContext(), inputActivity.class);
                weight = inputIntent.getIntExtra("WEIGHT",0);
                startActivity(inputIntent);
            }
        });

        // 현재 지정된 시간으로 알람 시간 설정
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0);

        // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        diaryNotification(calendar);

    } // Oncreate() end

    // 매일 특정 시간에 알림
    void diaryNotification(Calendar calendar) {
        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }

        // 부팅 후 실행되는 리시버 사용가능하게 설정
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    @Override
    public void onBackPressed() { backKeyHandler.onBackPressed(); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main,menu);
        //return false;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    static String decimalChange(int s) { // 천단위로 콤마 붙이는 함수
        String number = Integer.toString(s);
        double amount = Double.parseDouble(number);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formatted = formatter.format(amount);
        return formatted;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_input:
                //Intent inputIntent = new Intent(this, inputActivity.class);
                //weight = inputIntent.getIntExtra("WEIGHT",0);
                //startActivity(inputIntent);
                return true;
            case R.id.menu_stat:
                // 통계 보여주는 화면으로 이동
                // Intent statIntent = new Intent(this, statActivity.class);
                // startActivity(statIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}