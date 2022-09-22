package com.example.WaterReminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

public class IntakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull Context context, Intent intent) {
        Log.e("test","test");
        final MainActivity main = new MainActivity();
        main.waterSum = 0;
    }
}
