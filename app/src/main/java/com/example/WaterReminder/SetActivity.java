package com.example.WaterReminder;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SetActivity extends AppCompatActivity {
    private ImageView alarmImage;
    private ImageView aiImage;
    private TextView alarmText;
    private TextView aiText;
    private Switch alarmSwitch;
    private Switch aiSwitch;
    private ImageButton xBtn;
    private SharedPreferences pref;

    private PackageManager pm;
    private ComponentName receiver;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;

    private WorkManager workManager;
    private WorkRequest aiWorkRequest;
    private AlarmSetter alarmSetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        pm = this.getPackageManager();
        receiver = new ComponentName(this, DeviceBootReceiver.class);
        alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        alarmImage = (ImageView) findViewById(R.id.alarmImage);
        alarmText = (TextView) findViewById(R.id.alarmText);
        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
        aiImage = (ImageView) findViewById(R.id.aiImage);
        aiText = (TextView) findViewById(R.id.aiText);
        aiSwitch = (Switch) findViewById(R.id.aiSwitch);
        xBtn = (ImageButton) findViewById(R.id.x_btn);

        pref = getSharedPreferences("", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        alarmSwitch.setChecked(pref.getBoolean("alarm",false));
        aiSwitch.setChecked(pref.getBoolean("ai", false));

        alarmText.setText(pref.getString("alarmString", "정기 알림 off"));
        aiText.setText(pref.getString("aiString", "인공지능 알림 off"));

        workManager = WorkManager.getInstance(getApplicationContext());
        alarmSetter = new AlarmSetter(getApplicationContext());

        //스위치 클릭 이벤트
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Toast.makeText(SetActivity.this, "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    alarmImage.setImageResource(R.drawable.on);
                    alarmText.setText("정기 알림 on");
                    editor.putBoolean("alarm",true);
                    editor.putString("alarmString", alarmText.getText().toString());

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

                    alarmSetter.diaryNotification(calendar, pm, receiver, pendingIntent);

                } else {
                    Toast.makeText(SetActivity.this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    alarmImage.setImageResource(R.drawable.off);
                    alarmText.setText("정기 알림 off");
                    editor.putBoolean("alarm",false);
                    editor.putString("alarmString", alarmText.getText().toString());

                    alarmSetter.stopNotification(SetActivity.this, pm, alarmIntent, receiver, pendingIntent);
                }
                editor.commit();
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
                    editor.putBoolean("ai",true);
                    editor.putString("aiString", aiText.getText().toString());

                    aiWorkRequest =
                            new PeriodicWorkRequest.Builder(AiWorker.class, 1, TimeUnit.HOURS)
                                    .addTag("WORK_TAG")
                                    .build();

                    workManager.enqueue(aiWorkRequest);

                }
                else {
                    Toast.makeText(SetActivity.this, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                    aiImage.setImageResource(R.drawable.off);
                    aiText.setText("인공지능 알림 off");
                    editor.putBoolean("ai",false);
                    editor.putString("aiString", aiText.getText().toString());

                    if(isWorkScheduled("WORK_TAG")) {
                        // 예정된 알림 해제 후 work cancel
                        if(AiWorker.getContext() != null)
                            alarmSetter.stopNotification(AiWorker.getContext(), AiWorker.getPm(),
                                AiWorker.getAlarmIntent(), AiWorker.getReceiver(), AiWorker.getPendingIntent());
                        workManager.cancelAllWorkByTag("WORK_TAG");
                    }

                }
                editor.commit();
            }
        });

        xBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance(getApplicationContext());
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(tag);
        try {
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                Log.d("work state", String.valueOf(state));
                if(state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED)
                    return true;
            }
            return false;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
