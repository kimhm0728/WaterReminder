package com.example.WaterReminder;

import android.app.Activity;
import android.widget.Toast;

public class BackKeyHandler {
    private long backKeyPressedTime = 0;
    private Activity activity;
    private Toast toast;

    public BackKeyHandler(Activity activity) {
        this.activity = activity;
    }

    public void onBackPressed() { // 뒤로가기 버튼을 2초 이상 간격으로 누르면 종료되지 않음
        if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
            // 뒤로가기 버튼 한 번 눌렀을 시 종료되지 않고 Toast만 출력
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish();
            toast.cancel();
        }
    }
    private void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT);
        toast.show();
    }

}
