package com.example.WaterCollect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;


public class SetActivity extends AppCompatActivity {

    private ImageView alarmImage;
    private ImageView aiImage;
    private TextView alarmText;
    private TextView aiText;
    private Switch alarmSwitch;
    private Switch aiSwitch;
    private ImageButton xBtn;

    private PackageManager pm;
    private ComponentName receiver;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        pm = this.getPackageManager();
        receiver = new ComponentName(this, DeviceBootReceiver.class);
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmImage = (ImageView) findViewById(R.id.alarmImage);
        alarmText = (TextView) findViewById(R.id.alarmText);
        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
        aiImage = (ImageView) findViewById(R.id.aiImage);
        aiText = (TextView) findViewById(R.id.aiText);
        aiSwitch = (Switch) findViewById(R.id.aiSwitch);
        xBtn = (ImageButton) findViewById(R.id.x_btn);

        //스위치 클릭 이벤트
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Toast.makeText(SetActivity.this, "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    alarmImage.setImageResource(R.drawable.on);
                    alarmText.setText("정기 알림 on");

                    // 현재 지정된 시간으로 알람 시간 설정
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, 18);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);

                    // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    diaryNotification(calendar);

                } else {
                    Toast.makeText(SetActivity.this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    alarmImage.setImageResource(R.drawable.off);
                    alarmText.setText("정기 알림 off");

                    // 모든 알림 삭제
                    if (PendingIntent.getBroadcast(SetActivity.this, 0, alarmIntent, 0) != null && alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                        //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
                    }
                    pm.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
            }
        });

        //스위치 클릭 이벤트
        aiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Toast.makeText(SetActivity.this, "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    aiImage.setImageResource(R.drawable.on);
                    aiText.setText("인공지능 알림 on");

                    // 인공지능 관련 기능 추가
                }
                else {
                    Toast.makeText(SetActivity.this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    aiImage.setImageResource(R.drawable.off);
                    aiText.setText("인공지능 알림 off");

                    // 인공지능 관련 기능 해제
                }
            }
        });

        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    // 매일 특정 시간에 알림
    void diaryNotification(Calendar calendar) {

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

}