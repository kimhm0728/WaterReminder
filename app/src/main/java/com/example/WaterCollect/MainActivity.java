package com.example.WaterCollect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Thread workerThread = null; //문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼  내 문자 저장 위치
    private int pairedDeviceCount;  //페어링 된 기기의 크기를 지정할 변수
    private boolean connect_status;
    private int bluetoothCheck = 3;
    // 0: 블루투스 미지원 1: 블루투스 off 2: 블루투스 on, 연결필요 3: 연결완료

    private final static String IP_ADDRESS = "10.0.2.2";
    private String device; // 디바이스 시리얼 넘버 SSAID

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
                windowSet();
            } else { // 기기의 블루투스 기능이 꺼져있을 경우
                bluetoothCheck = 1;
                windowSet();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택 값이 onActivityResult 함수에서 콜백
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

        }

        resetAlarm(this);
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
                //Intent statIntent = new Intent(this, StatActivity.class);
                //startActivity(statIntent);
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
        connect_status = true;
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
        DataInserter task = new DataInserter();

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
                                            waterSum += Integer.parseInt(water);
                                            windowSet();
                                            // 받은 센서 값을 서버에 전송
                                            task.execute("http://" + IP_ADDRESS + "/insert.php", device, water);
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
                    //1분 마다 받아옴
                    Thread.sleep(60000);
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
                day_water.setText(String.format("블루투스 기능을"));
                day_text.setText("켜주세요");
                return;
            case 2:
                day_water.setText(String.format("아래의 블루투스 버튼을"));
                day_text.setText("클릭하여 장치와 연결해주세요");
                return;
        }
        if (weight == 0) {
            day_water.setText(String.format("몸무게를"));
            day_text.setText("입력해주세요");
        } else {
            day_water.setText(String.format("하루 권장량"));
            ratio = (int) (((double) waterSum / day) * 100); // 권장량 달성비율
            percent.setText(String.format(Locale.KOREA, "%d%%", ratio));
            day_text.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(day)));
            left_text.setText(String.format(Locale.KOREA, "%smL", StringChanger.decimalComma(day - waterSum)));
        }

        ratio_pBar.setProgress(ratio); // 하루 권장량 달성비율만큼 progressBar에 적용
    }

    public void resetAlarm(@NonNull Context context) {
        // 자정이 되면 물 섭취량을 0으로 다시 초기화
        AlarmManager resetAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, IntakeReceiver.class);
        PendingIntent resetSender = PendingIntent.getBroadcast(context, 0, resetIntent, 0);

        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE, 0);
        resetCal.set(Calendar.SECOND, 0);

        //다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis()
                +AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender);
    }

    // post 방식으로 php->MySQL 데이터 전송
    class DataInserter extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        private final static String TAG = "phptest";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            // mTextViewResult.setText(result);
            Log.d(TAG, "POST response  - " + result);
        }

        @Override
        protected String doInBackground(@NonNull String... params) {

            String device = (String)params[1];
            String intake = (String)params[2];

            String serverURL = (String)params[0];
            String postParameters = "device=" + device + "&intake=" + intake;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
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

                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }

        }
    }

}