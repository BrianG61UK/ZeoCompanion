package opensource.zeocompanion.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.Utilities;

public class BluetoothIOThread extends Thread {
    private static final String _CTAG = "BIT";
    private ZeoMobileHB_BluetoothHandler mHandler = null;
    private BluetoothSocket mmSocket = null;
    public InputStream mmInStream = null;
    public OutputStream mmOutStream = null;
    public boolean mThreadIsPreparing = true;
    public boolean mThreadIsRunning = false;

    // thread context:  HeadbandActivity
    public BluetoothIOThread(ZeoMobileHB_BluetoothHandler theHandler, BluetoothSocket theSocket) {
        mHandler = theHandler;
        mmSocket = theSocket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    // thread context:  BluetoothIOThread
    public void run() {
        Thread.setDefaultUncaughtExceptionHandler(ZeoCompanionApplication.mMasterAbortHandler); // set the master abort handler for this thread
        mThreadIsRunning = true;
        mThreadIsPreparing = false;
        if (mmInStream == null) {
            mHandler.manageConnectedSocketError(ZeoMobileHB_BluetoothHandler.THREAD_IO, ZeoMobileHB_BluetoothHandler.ERROR_SOCKET_IS_NULL);
            mThreadIsRunning = false;
            return;
        }

        byte[] buffer = new byte[1024];  // buffer store for the stream
        int len; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (mmInStream != null) {
            try {
                // Read from the InputStream
                len = mmInStream.read(buffer);
                Log.d(_CTAG + ".run", "Received packet (" + len + "): " + Utilities.bytesToHex(buffer, 0, 12));
                // Send the obtained bytes to the UI activity
                mHandler.message_received(len, buffer);
            } catch (IOException e) {
                break;
            }
        }
        mThreadIsRunning = false;
    }

    /* Call this from the main activity to send data to the remote device */
    // thread context:  HeadbandActivity
    public void write(byte[] bytes) {
        if (mmOutStream != null) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {}
        }
    }

    /* Call this from the main activity to shutdown the connection */
    // thread context:  HeadbandActivity
    public void disconnect() {
        // must disconnect the IO streams first
        if (mmInStream != null) {
            try { mmInStream.close(); } catch (IOException e) {}
            mmInStream = null;
        }
        if (mmOutStream != null) {
            try { mmOutStream.close(); } catch (IOException e) {}
            mmOutStream = null;
        }
        if (mmSocket != null) {
            try { mmSocket.close(); } catch (IOException e) {}
            mmSocket = null;
        }
    }

    // thread context:  HeadbandActivity
    public String getDeviceName() {
        if (mmSocket == null) return "*none*";
        return mmSocket.getRemoteDevice().getName();
    }
    // thread context:  Activity
    public String getDeviceMAC() {
        if (mmSocket == null) return "*none*";
        return mmSocket.getRemoteDevice().getAddress();
    }
}
