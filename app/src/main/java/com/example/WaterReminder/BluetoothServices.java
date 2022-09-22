package com.example.WaterCollect;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothServices extends Service {
    private BluetoothAdapter mBluetoothAdapter;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private ConnectBtThread mConnectThread;
    private static ConnectedBtThread mConnectedThread;
    private BluetoothDevice mBluetoothDevice; // 블루투스 디바이스
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋

    private static Handler mHandler = null;
    public static int mState = STATE_NONE;

    private Context context = this;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //mHandler = getApplication().getHandler();
        return mBinder;
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "연결을 완료했습니다.", Toast.LENGTH_LONG).show();
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothServices getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothServices.this;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = mBluetoothAdapter.getBondedDevices(); // 페어링 되어있는 블루투스 기기를 탐색

        String device = intent.getStringExtra("bluetooth_device");
        connectToDevice(device);

        return START_REDELIVER_INTENT;
    }

    @SuppressLint("MissingPermission")
    private synchronized void connectToDevice(String macAddress){
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (macAddress.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectBtThread(mBluetoothDevice);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private void setState(int state){
        mState = state;
        if (mHandler != null){
            // mHandler.obtainMessage();
        }
    }

    @SuppressLint("MissingPermission")
    public synchronized void stop(){
        setState(STATE_NONE);
        if (mConnectedThread != null){
            mConnectedThread.interrupt();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }

        stopSelf();
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    private class ConnectBtThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private final ParcelUuid list[];
        private final UUID uuid;

        @SuppressLint("MissingPermission")
        public ConnectBtThread(@NonNull BluetoothDevice device) {
            list = device.getUuids();
            uuid = UUID.fromString(list[0].toString());
            mDevice = device;
            BluetoothSocket bluetoothSocket = null;
            try {
                //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = bluetoothSocket;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();
                Log.d("service","connect thread run method (connected)");
                SharedPreferences pre = getSharedPreferences("BT_NAME",0);
                pre.edit().putString("bluetooth_connected", mDevice.getName()).commit();

            } catch (IOException e) {
                try {
                    mSocket.close();
                    Log.d("service","connect thread run method (close function)");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

            mConnectedThread = new ConnectedBtThread(mSocket);
            mConnectedThread.start();
        }

        public void cancel() {
            try {
                mSocket.close();
                Log.d("service","connect thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedBtThread extends Thread {
        private final BluetoothSocket cSocket;
        private final InputStream inS;

        private byte[] readBuffer = new byte[1024]; // 데이터 수신을 위한 버퍼 생성
        private int readBufferPosition = 0; // 버퍼 내 문자 저장 위치
        final Handler handler = new Handler(Looper.getMainLooper());
        final DataInserter[] task = new DataInserter[1];
        String water;

        public ConnectedBtThread(BluetoothSocket socket) {
            cSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inS = tmpIn;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                if(interrupted()) break;
                try {
                    int byteAvailable = inS.available();
                    if (byteAvailable > 0) {
                        //입력 스트림에서 바이트 단위로 읽어옴
                        byte[] bytes = new byte[byteAvailable];
                        inS.read(bytes);
                        //입력 스트림 바이트를 한 바이트씩 읽어옴
                        for (int i = 0; i < byteAvailable; i++) {
                            byte tempByte = bytes[i];
                            //개행문자를 기준으로 받음
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
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("RECEIVED_DATA"));
                                        // 받은 센서 값을 서버에 전송
                                        task[0] = new DataInserter();
                                        task[0].execute("http://" + MainActivity.getIpAddress() + "/insert.php", IntroActivity.getEmail(), water, "send");
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
                    break;
                }
            }
            try {
                //10초 마다 받아옴
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("service","connected thread run method");
        }

        private void cancel() {
            try {
                cSocket.close();
                Log.d("service","connected thread cancel method");
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "장치와의 연결이 끊어졌습니다.", Toast.LENGTH_LONG).show();
        stop();
    }
}