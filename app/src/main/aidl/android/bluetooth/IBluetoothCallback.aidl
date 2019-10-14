// IBluetoothCallback.aidl
package android.bluetooth;

// Declare any non-default types here with import statements

interface IBluetoothCallback
{
    void onRfcommChannelFound(int channel);
}
