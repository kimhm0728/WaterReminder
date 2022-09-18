package com.example.WaterCollect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    private Intent mIntent;
    private TextView day_water;
    private TextView total_text;
    private TextView day_text;
    private TextView left_text;
    private TextView device_text;
    private TextView percent;
    private ProgressBar ratio_pBar;
    private ImageButton bluetooth_btn;
    private ImageButton input_btn;
    private ImageButton stat_btn;
    private ImageButton setting_btn;

    public static int waterSum = 0; // 총 섭취량
    private static int day = 0; // 하루 권장 섭취량
    private static int ratio = 0; // 권장량 달성비율
    private static int weight;

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private int pairedDeviceCount;  //페어링 된 기기의 크기를 지정할 변수
    private int bluetoothCheck;
    // 0: 블루투스 미지원 1: 블루투스 off 2: 블루투스 on, 연결필요 3: 연결완료

    private final static String IP_ADDRESS = "192.168.45.134";
    // 에뮬레이터 10.0.2.2, 안드로이드 192.168.45.134
    private static String deviceNumber; // 디바이스 시리얼 넘버 SSAID

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        day_water = findViewById(R.id.dayText);
        total_text = findViewById(R.id.water);
        day_text = findViewById(R.id.day);
        left_text = findViewById(R.id.left);
        device_text = findViewById(R.id.device);
        percent = findViewById(R.id.percent);
        ratio_pBar = findViewById(R.id.water_pBar);
        bluetooth_btn = findViewById(R.id.bluetooth_btn);
        input_btn = findViewById(R.id.input_btn);
        stat_btn = findViewById(R.id.statistics_btn);
        setting_btn = findViewById(R.id.setting_btn);

        deviceNumber = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        mIntent = new Intent(getApplicationContext(), BluetoothServices.class);

        //블루투스 활성화 코드
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터를 디폴트 어댑터로 설정

        if (bluetoothAdapter == null) { //기기가 블루투스를 지원하지 않을때
            bluetoothCheck = 0;
            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
        } else { // 기기가 블루투스를 지원할 때
            if (bluetoothAdapter.isEnabled()) { //기기의 블루투스 기능이 켜져있을 경우
                bluetoothCheck = 2;
            } else { //기기의 블루투스 기능이 꺼져있을 경우
                bluetoothCheck = 1;
                //사용자에게 활성화 요청
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }

        IntakeResetter.resetAlarm(this);
        windowSet();

        bluetooth_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bluetoothCheck) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "휴대폰의 Bluetooth 기능을 켠 후 연결할 장치를 선택해주세요.", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                    case 3:
                        selectBluetoothDevice();
                        break;
                }
            }
        });

        input_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //입력한 몸무게 데이터 받기
                Intent inputIntent = new Intent(getApplicationContext(), InputActivity.class);
                startActivityForResult(inputIntent, 101);
            }
        });

        stat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statIntent = new Intent(getApplicationContext(), StatActivity.class);
                startActivity(statIntent);
            }
        });

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent setIntent = new Intent(getApplicationContext(), SetActivity.class);
               startActivity(setIntent);
            }
        });

    } //onCreate() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //inputActivity 에서 빠져나올 때 실행됨
        if(requestCode == 101 && !TextUtils.isEmpty(data.getStringExtra("weight"))) {
            //액티비티에서 받은 값이 있는 경우
            weight = Integer.parseInt(data.getStringExtra("weight"));
            day = weight * 30;
        }
        else { } //액티비티에서 받은 값이 없는 경우
        windowSet();
        saveState();
    }

    //블루투스 상태 변화에 따른 리시버
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF: //블루투스 비활성화
                            bluetoothCheck = 1;
                            break;
                        case BluetoothAdapter.STATE_ON: //블루투스 활성화
                            bluetoothCheck = 2;
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                    bluetoothCheck = 3;
                    //device_text.setText(bluetoothDevice.getName());
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                    bluetoothCheck = 2;
                    stopService(mIntent);
                    break;
            }
            windowSet();
        }
    };

    //블루투스 데이터 수신에 따른 리시버
    BroadcastReceiver mBluetoothDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if(intent.getAction().equals("RECEIVED_DATA"))
                windowSet();
        }
    };

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        pairedDeviceCount = devices.size();

        if (pairedDeviceCount == 0) { //페어링 된 장치가 없는 경우
            Toast.makeText(getApplicationContext(), "페어링 되어있는 장치가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
        else { //페어링 되어있는 장치가 있는 경우
            //디바이스를 선택하기 위한 대화상자 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 된 블루투스 디바이스 목록");

            List<String> list = new ArrayList<>(); //모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("닫기");

            //list를 Charsequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            //항목을 눌렀을 때 해당 디바이스와 연결
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String deviceName = charSequences[which].toString();
                    if(deviceName.equals("닫기"))
                        return;

                    mIntent.putExtra("bluetooth_device", deviceName);
                    startService(mIntent);
                    //device_text.setText(bluetoothDevice.getName());
                }
            });
            //뒤로가기 버튼 누를때 창이 안닫히도록 설정
            builder.setCancelable(false);

            //다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onBackPressed() { backKeyHandler.onBackPressed(); }

    public void windowSet() {
        final DataInserter task = new DataInserter();
        day = weight * 30;

        try {
            task.execute("http://" + MainActivity.IP_ADDRESS + "/weekquery.php", deviceNumber, DateFormatter.weekString(0, 1), "receive");
            waterSum = Integer.parseInt(task.get());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        total_text.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(waterSum)));

        switch(bluetoothCheck) {
            case 1:
                day_water.setTextSize(Dimension.SP, 13);
                day_text.setTextSize(Dimension.SP, 13);
                day_water.setText(String.format("블루투스 기능을"));
                day_text.setText("켜주세요");
                break;
            case 2:
                day_water.setTextSize(Dimension.SP, 10);
                day_text.setTextSize(Dimension.SP, 10);
                day_water.setText(String.format("아래의 블루투스 버튼을"));
                day_text.setText("클릭하여 장치와 연결해주세요");
                break;
            case 0:
            case 3:
                if (weight == 0) {
                    day_water.setTextSize(Dimension.SP, 13);
                    day_text.setTextSize(Dimension.SP, 13);
                    day_water.setText(String.format("몸무게를"));
                    day_text.setText("입력해주세요");
                    percent.setText("0%");
                    ratio_pBar.setProgress(0);
                    return;
                } else {
                    day_water.setTextSize(Dimension.SP, 14);
                    day_text.setTextSize(Dimension.SP, 16);
                    day_water.setText(String.format("하루 권장량"));
                    day_text.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(day)));
                }
                break;
        }
        ratio = (int) (((double) waterSum / day) * 100); //권장량 달성비율
        percent.setText(String.format(Locale.KOREA, "%d%%", Math.min(ratio, 100)));
        left_text.setText(String.format(Locale.KOREA, "%smL", (day - waterSum < 0) ? 0 : StringChanger.decimalComma(day - waterSum)));
        ratio_pBar.setProgress(ratio); //하루 권장량 달성비율만큼 progressBar에 적용
    }

    public static String getIpAddress() { return IP_ADDRESS; }

    public static String getDeviceNumber() { return deviceNumber; }

    @Override
    protected void onStart() {
        super.onStart();
        restoreState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //블루투스 상태변화
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        registerReceiver(mBluetoothStateReceiver, stateFilter);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBluetoothDataReceiver,
                new IntentFilter("RECEIVED_DATA"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothStateReceiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBluetoothDataReceiver);
    }

    protected void saveState(){
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("weight", weight);

        editor.commit();
    }

    protected void restoreState() {
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if ((pref != null) && (pref.contains("weight"))) {
            weight = pref.getInt("weight", 0);
            windowSet();
        }
    }

}