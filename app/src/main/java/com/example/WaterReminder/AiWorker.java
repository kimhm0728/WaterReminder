package com.example.WaterReminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class AiWorker extends Worker {
    private static PackageManager pm;
    private static ComponentName receiver;
    private static Intent alarmIntent;
    private static PendingIntent pendingIntent;
    private static AlarmManager alarmManager;
    private static Context context;
    private AlarmSetter alarmSetter;

    private final static String TAG = "phptest";

    public AiWorker (@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        context = appContext;

        pm = appContext.getPackageManager();
        receiver = new ComponentName(context, AiDeviceBootReceiver.class);
        alarmIntent = new Intent(context, AiAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmSetter = new AlarmSetter(appContext);

    }

    @NonNull
    @Override
    public Result doWork() {
        String predict = null;

        try {
            String email = IntroActivity.getEmail();
            String postParameters = "email=" + email;

            String serverURL = "http://" + MainActivity.getIpAddress() + "/ai.php";

            // 인공지능 모델 생성(이상치면 1, 정상이면 0 반환)
            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                predict = sb.toString().trim();
                Log.d("ai predicted result", predict);

                if(!TextUtils.isEmpty(predict) && (Integer.parseInt(predict) == 1)) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, 10);
                    alarmSetter.diaryNotification(cal, pm, receiver, pendingIntent); // 10분 후 알림 발생
                }

            } catch (Exception e) {
                Log.d(TAG, "AiModelGenerate: Error ", e);
                return Result.retry();
            }

            return Result.success();

        } catch (Throwable throwable) {
            return Result.failure();
        }
    }

    public static Context getContext() { return context; }
    public static PackageManager getPm() { return pm; }
    public static ComponentName getReceiver() { return receiver; }
    public static Intent getAlarmIntent() { return alarmIntent; }
    public static PendingIntent getPendingIntent() { return pendingIntent; }

}
