package com.example.WaterCollect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);

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

    private static String water;
    public static int waterSum = 0; // 총 섭취량
    private static int day = 0; // 하루 권장 섭취량
    private static int ratio = 0; // 권장량 달성비율
    private static int weight = 0;

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; //블루투스 소켓
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 스레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼  내 문자 저장 위치
    private int pairedDeviceCount;  //페어링 된 기기의 크기를 지정할 변수
    private int bluetoothCheck = 3;
    // 0: 블루투스 미지원 1: 블루투스 off 2: 블루투스 on, 연결필요 3: 연결완료

    public final static String IP_ADDRESS = "10.0.2.2"; // 에뮬레이터에서 테스트 시 10.0.2.2
    public static String device; // 디바이스 시리얼 넘버 SSAID

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

        device = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // 블루투스 상태 변화에 따른 Broadcast Receiver 등록
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED); //연결 확인
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED); //연결 끊김 확인
        registerReceiver(mBluetoothStateReceiver, stateFilter);

        //위치권한 허용 코드
        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        //블루투스 활성화 코드
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터를 디폴트 어댑터로 설정

        if (bluetoothAdapter == null) { //기기가 블루투스를 지원하지 않을때
            bluetoothCheck = 0;
            Toast.makeText(getApplicationContext(), "Bluetooth 미지원 기기입니다.", Toast.LENGTH_SHORT).show();
        } else { // 기기가 블루투스를 지원할 때
            if (bluetoothAdapter.isEnabled()) { // 기기의 블루투스 기능이 켜져있을 경우
                bluetoothCheck = 2;
            } else { // 기기의 블루투스 기능이 꺼져있을 경우
                bluetoothCheck = 1;
                // 사용자에게 활성화 요청
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

        }

        IntakeResetter.resetAlarm(this);
        windowSet();

        bluetooth_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bluetoothCheck){
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
                // 입력한 몸무게 데이터 받기
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

    } // onCreate() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 액티비티에서 빠져나올 때 실행됨
        if(requestCode == 101 && !TextUtils.isEmpty(data.getStringExtra("weight"))) {
            // 액티비티에서 받은 값이 있는 경우
            weight = Integer.parseInt(data.getStringExtra("weight"));
            day = weight * 30;
        }
        else { } // 액티비티에서 받은 값이 없는 경우
        windowSet();
    }

    // 블루투스 상태 변화에 따른 리시버
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            Log.d("Bluetooth action", action);

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED: //블루투스의 연결 상태 변경
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF: // 블루투스 비활성화
                            bluetoothCheck = 1;
                            break;
                        case BluetoothAdapter.STATE_ON: // 블루투스 활성화
                            bluetoothCheck = 2;
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:  //블루투스 기기 연결
                    bluetoothCheck = 3;
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:   //블루투스 기기 끊어짐
                    bluetoothCheck = 2;
                    device_text.setText("연결된 기기가 존재하지 않습니다");
                    break;
            }
            windowSet();
        }
    };

    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        //페어링 된 디바이스 크기 저장
        pairedDeviceCount = devices.size();
        //페어링 된 장치가 없는 경우
        if (pairedDeviceCount == 0) {
            bluetoothCheck = 2;
            windowSet();
            //페어링 하기 위한 함수 호출
            Toast.makeText(getApplicationContext(), "페어링 되어있는 장치가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
        //페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 대화상자 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("페어링 된 블루투스 디바이스 목록");
            //페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            //모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("닫기");

            //list를 Charsequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bluetoothCheck = 3;
                    //해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });
            //뒤로가기 버튼 누를때 창이 안닫히도록 설정
            builder.setCancelable(false);
            //다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

    }

    //연결 함수
    @SuppressLint("MissingPermission")
    public void connectDevice(String deviceName) {
        if(deviceName.equals("닫기"))
            return;

        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }

        }
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " 연결 완료", Toast.LENGTH_SHORT).show();
        //UUID생성
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            receiveData();

        } catch (IOException e) {
            e.printStackTrace();
        }

        device_text.setText(bluetoothDevice.getName());
    }

    public void receiveData() {
        final Handler handler = new Handler();
        //데이터 수신을 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        final DataInserter[] task = new DataInserter[1];

        //데이터 수신을 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //데이터 수신 확인
                        int byteAvailable = inputStream.available();
                        //데이터 수신 된 경우
                        if (byteAvailable > 0) {
                            //입력 스트림에서 바이트 단위로 읽어옴
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            //입력 스트림 바이트를 한 바이트씩 읽어옴
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                //개행문자를 기준으로 받음 (한줄)
                                if (tempByte == '\n') {
                                    //readBuffer 배열을 encodeBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    //인코딩 된 바이트 배열을 문자열로 변환
                                    water = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            water = water.replace("\r", "");
                                            waterSum += Integer.parseInt(water);
                                            windowSet();
                                            // 받은 센서 값을 서버에 전송
                                            task[0] = new DataInserter();
                                            task[0].execute("http://" + IP_ADDRESS + "/insert.php", device, water, "send");
                                        }
                                    });
                                } // 개행문자가 아닐경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                try {
                    //10초 마다 받아옴
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workerThread.start();
    }

    @Override
    public void onBackPressed() { backKeyHandler.onBackPressed(); }

    public void windowSet() {
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
            case 3:
                if (weight == 0) {
                    day_water.setTextSize(Dimension.SP, 13);
                    day_text.setTextSize(Dimension.SP, 13);
                    day_water.setText(String.format("몸무게를"));
                    day_text.setText("입력해주세요");
                } else {
                    day_water.setTextSize(Dimension.SP, 14);
                    day_text.setTextSize(Dimension.SP, 16);
                    day_water.setText(String.format("하루 권장량"));
                    ratio = (int) (((double) waterSum / day) * 100); // 권장량 달성비율
                    percent.setText(String.format(Locale.KOREA, "%d%%", Math.min(ratio, 100)));
                    day_text.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(day)));
                }
                break;
        }
        left_text.setText(String.format(Locale.KOREA, "%smL", (day - waterSum < 0) ? 0 : StringChanger.decimalComma(day - waterSum)));
        ratio_pBar.setProgress(ratio); // 하루 권장량 달성비율만큼 progressBar에 적용
    }

    @Override
    protected void onDestroy() {
        // Activity 소멸 시 호출
        unregisterReceiver(mBluetoothStateReceiver);
        super.onDestroy();
    }

}