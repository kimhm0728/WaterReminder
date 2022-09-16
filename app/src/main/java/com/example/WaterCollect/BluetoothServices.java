package com.example.WaterCollect;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class BluetoothServices extends Service {
    private BluetoothAdapter mBluetoothAdapter;
    public static final String B_DEVICE = "MY DEVICE";
    public static final String B_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    // 00000000-0000-1000-8000-00805f9b34fb

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private ConnectBtThread mConnectThread;
    private static ConnectedBtThread mConnectedThread;

    private static Handler mHandler = null;
    public static int mState = STATE_NONE;
    public static String deviceName;
    public static BluetoothDevice sDevice = null;
    public Vector<Byte> packData = new Vector<>(2048);

    //IBinder mIBinder = new LocalBinder();

    private BluetoothDevice device;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private OutputStream outputStream = null; //블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; //블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; //문자열 수신에 사용되는 스레드
    private byte[] readBuffer; //수신된 문자열 저장 버퍼
    private int readBufferPosition; //버퍼  내 문자 저장 위치
    private int pairedDeviceCount;  //페어링 된 기기의 크기를 지정할 변수
    private List<String> list;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //mHandler = getApplication().getHandler();
        return mBinder;
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터를 디폴트 어댑터로 설정

//이미 페어링 되어있는 블루투스 기기를 탐색
        devices = mBluetoothAdapter.getBondedDevices();
        //페어링 된 디바이스 크기 저장
        pairedDeviceCount = devices.size();
        //페어링 된 장치가 없는 경우

        String deviceg = intent.getStringExtra("bluetooth_device");

        connectToDevice(deviceg);

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private synchronized void connectToDevice(String macAddress){
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (macAddress.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }

        }

        //device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING){
            if (mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectBtThread(bluetoothDevice);
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
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null){
            mBluetoothAdapter.cancelDiscovery();
        }

        stopSelf();
    }

    public void sendData(String message){
        if (mConnectedThread!= null){
            mConnectedThread.write(message.getBytes());
        }else {
            Toast.makeText(BluetoothServices.this,"Failed to send data",Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    /*private synchronized void connected(BluetoothSocket mmSocket){

        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedBtThread(mmSocket);
        mConnectedThread.start();


        setState(STATE_CONNECTED);
    }*/

    private class ConnectBtThread extends Thread{
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        //UUID생성
        private final ParcelUuid list[];
        private final UUID uuid;
        //Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        @SuppressLint("MissingPermission")
        public ConnectBtThread(@NonNull BluetoothDevice device) {
            list = device.getUuids();
            uuid = UUID.fromString(list[0].toString());
            mDevice = device;
            BluetoothSocket bluetoothSocket = null; //블루투스 소켓
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                //receiveData();
                Log.e("connect", "done");

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
                pre.edit().putString("bluetooth_connected",mDevice.getName()).apply();

            } catch (IOException e) {

                try {
                    mSocket.close();
                    Log.d("service","connect thread run method ( close function)");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            //connected(mSocket);
            mConnectedThread = new ConnectedBtThread(mSocket);
            mConnectedThread.start();
        }

        public void cancel(){
            try {
                mSocket.close();
                Log.d("service","connect thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedBtThread extends Thread{
        private final BluetoothSocket cSocket;
        private final InputStream inS;
        private final OutputStream outS;

        private byte[] buffer;

        public ConnectedBtThread(BluetoothSocket socket){
            cSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inS = tmpIn;
            outS = tmpOut;
        }

        @Override
        public void run() {
            buffer = new byte[1024];
            int mByte;
            try {
                mByte= inS.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("service","connected thread run method");

        }


        public void write(byte[] buff){
            try {
                outS.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void cancel(){
            try {
                cSocket.close();
                Log.d("service","connected thread cancel method");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        this.stop();
        super.onDestroy();
    }
}