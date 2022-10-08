package com.example.WaterReminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;

public class AlarmSetter {
    private AlarmManager alarmManager;

    public AlarmSetter(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    // 특정 시간에 알림
    public void diaryNotification(Calendar calendar, PackageManager pm, ComponentName receiver, PendingIntent pendingIntent) {
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

    public void stopNotification(Context context, PackageManager pm, Intent intent, ComponentName receiver, PendingIntent pendingIntent) {
        // 모든 알림 삭제
        if (PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE) != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
        }
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
