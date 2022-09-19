package com.example.WaterCollect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;
import java.util.Set;

public class BluetoothChecker {
    // parameter 로 들어온 장치가 연결되어 있는지 체크
    public static boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressLint("MissingPermission")
    //연결 중인 장치가 있다면 장치명 반환, 없다면 null 반환
    public static String PairingBluetoothListState(Set<BluetoothDevice> devices) {
        for (BluetoothDevice bluetoothDevice : devices) {
            if (isConnected(bluetoothDevice))
                return bluetoothDevice.getName();
            }
        return null;
    }
}
