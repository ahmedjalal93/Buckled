package com.secure.buckled;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.secure.buckled.Bluetooth.BluetoothServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.secure.buckled.Bluetooth.BluetoothServer.bTSocket;
import static com.secure.buckled.SeatBelts.resetSeatBeltState;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "BuckledUp";
    SparseLongArray seatTime = new SparseLongArray();
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private BluetoothServer mSecureAcceptThread;
    private BluetoothAdapter mBluetoothAdapter;
    private Button mButton;
    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        while (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Method method;
            try {
                Log.i(TAG, "trying to enable discover on bluetooth");
                Thread.sleep(1000);
                method = mBluetoothAdapter.getClass().getMethod("setScanMode", int.class, int.class);
                method.invoke(mBluetoothAdapter,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,600);

                Log.i("invoke","method invoke successfully");
            }
            catch (Exception e){
                Log.i(TAG, "Error: ", e);
            }
        }

       // ensureBluetoothDiscoverability();

        Thread t1 = new Thread(new BluetoothServer(mBluetoothAdapter, MY_UUID_INSECURE));
        t1.start();


        seatStateUpdater();


    }

    public static IBluetooth getIBluetooth() {

        IBluetooth ibt = null;

        try {

            Class<?> c2 = Class.forName("android.os.ServiceManager");

            Method m2 = c2.getDeclaredMethod("getService", String.class);
            IBinder b = (IBinder) m2.invoke(null, BLUETOOTH_SERVICE);

            Class<?> c3 = Class.forName("android.bluetooth.IBluetooth");

            Class[] s2 = c3.getDeclaredClasses();

            Class<?> c = s2[0];
            Log.i(TAG, "print " + c + " sssssssss2[0] " + s2[0]);
            Method m = c.getDeclaredMethod("asInterface", IBinder.class);

            m.setAccessible(true);
            Log.i(TAG, " m " + m.isAccessible());
            ibt = (IBluetooth) m.invoke(null, b);
            Log.i(TAG, "ibt " + ibt.getName() + " ibt22 " + ibt.toString() + " m " + m.isAccessible());

        } catch (Exception e) {

        }
        return ibt;
    }

    private void ensureBluetoothDiscoverability() {


        try {
            IBluetooth mBtService = getIBluetooth();
            Log.i(TAG, "Ensuring bluetoot is discoverable");
            if(mBtService.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                Log.i(TAG, "Device was not in discoverable mode");
                try {
                    mBtService.setDiscoverableTimeout(300);
                    // mBtService.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 1000);
                } catch(Exception e) {
                    Log.e(TAG, "Error setting bt discoverable",e);
                }
                Log.i(TAG, "Device must be discoverable");
            } else {
                Log.e("TESTE", "Device already discoverable");
            }
        } catch(Exception e) {
            Log.i(TAG, "Error ensuring BT discoverability", e);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.i(TAG, "seatbelt " + SeatBelts.SeatBelt.findSeatBelt(keyCode) + " buckled");
        SeatBelts.setSeatBeltState(keyCode, SeatBelts.SeatBeltState.ON.intValue, this);
        if(bTSocket != null && bTSocket.isConnected() ) {
            sendToMonitor(keyCode, SeatBelts.SeatBeltState.ON.intValue);
        }
        seatTime.put(keyCode, System.currentTimeMillis());

        return true;
    }

    public void seatStateUpdater(){

        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SeatBelts.SeatBelt[] allSeats = SeatBelts.SeatBelt.values();
                for (SeatBelts.SeatBelt seat : allSeats ) {
                    if(seat != SeatBelts.SeatBelt.UNKNOWN) {
                        int seatState = SeatBelts.getSeatBeltState(seat.seatID, getApplicationContext());
                        long timeDelay = System.currentTimeMillis()-5000;
                        if(seatTime.get(seat.seatID) < timeDelay && seatState == SeatBelts.SeatBeltState.ON.intValue) {
                            Log.i(TAG, "seat " + seat + " timestamp " + seatTime.get(seat.seatID));
                            SeatBelts.setSeatBeltState(seat.seatID, SeatBelts.SeatBeltState.OFF.intValue, getApplicationContext());

                        }
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void sendToMonitor(int keyCode, int value){
        OutputStream tmpOut = null;
        OutputStream mmOutStream;

        try {
            tmpOut = bTSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }
        mmOutStream = tmpOut;

        String data = keyCode+":"+value;
        Log.i(TAG, "trying data send: " + data);
        byte[] send = data.getBytes();
        try {
            mmOutStream.write(send);
        } catch (Exception e) {
            try {
                bTSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            exec.shutdown();
            resetSeatBeltState(getApplicationContext());
            finish();
            startActivity(getIntent());
            //e.printStackTrace();
        }
        Log.i(TAG, "data send: " + send.toString());
    }

    public class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent myIntent = new Intent(context, MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
    }
}
