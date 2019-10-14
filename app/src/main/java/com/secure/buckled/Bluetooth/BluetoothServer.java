package com.secure.buckled.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.net.wifi.WifiInfo;
import android.net.wifi.SupplicantState;
import android.util.Log;
import android.net.NetworkInfo.DetailedState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.UUID;


/**
 * Created by ajalal on 4/17/18.
 */

public class BluetoothServer extends Thread{
    public static BluetoothSocket bTSocket;
    public static final String TAG = "BuckledUp:Bluetooth";
    public BluetoothAdapter mBluetoothAdapter;
    public UUID mUUID;
    boolean connecting = true;
    public BluetoothServer(BluetoothAdapter mBluetoothAdapter, UUID mUUID) {
        //this.mBluetoothAdapter = mBluetoothAdapter;
        this.mUUID = mUUID;
    }

    public void run(){
        acceptConnect();
    }

    public void acceptConnect() {
        BluetoothServerSocket temp = null;
        InputStream mmInStream;
        OutputStream mmOutStream;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Log.i(TAG, "Bluetooth Listening");
            temp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("VbusConnection", mUUID);
        } catch(Exception e) {
            Log.i("SERVERCONNECT", "Could not get a BluetoothServerSocket:", e);
            closeConnect();
        }
        while(connecting) {
            try {
                Log.i(TAG, "Bluetooth accepting");
                bTSocket = temp.accept();
            } catch (Exception e) {
                Log.i("SERVERCONNECT", "Could not accept an incoming connection.");
                closeConnect();
                break;
            }
            if (bTSocket != null && bTSocket.isConnected()) {
                try {
                    Log.i(TAG, "Device connected");
                    connecting = false;
                    temp.close();
                } catch (IOException e) {
                    Log.i("SERVERCONNECT", "Could not close ServerSocket:" + e.toString());
                    closeConnect();
                }
            }
        }
    }

    public void closeConnect() {
        try {
            bTSocket.close();
        } catch(IOException e) {
            Log.i("SERVERCONNECT", "Could not close connection:" + e.toString());
        }

        this.interrupt();
    }
}