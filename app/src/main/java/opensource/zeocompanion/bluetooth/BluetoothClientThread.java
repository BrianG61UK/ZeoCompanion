package opensource.zeocompanion.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import opensource.zeocompanion.ZeoCompanionApplication;

public class BluetoothClientThread extends Thread {
    private static final String _CTAG = "BCT";
    public BluetoothSocket mmSocket = null;
    private BluetoothDevice mmDevice = null;
    private ZeoMobileHB_BluetoothHandler mZeoMobileHBBluetoothHandler = null;

    // thread context:  HeadbandActivity
    public BluetoothClientThread(ZeoMobileHB_BluetoothHandler zeoMobileHBBluetoothHandler, BluetoothDevice device, UUID theUUID) {
        BluetoothSocket tmp = null;
        mZeoMobileHBBluetoothHandler = zeoMobileHBBluetoothHandler;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(theUUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    // thread context:  BluetoothClientThread
    public void run() {
        Thread.setDefaultUncaughtExceptionHandler(ZeoCompanionApplication.mMasterAbortHandler); // set the master abort handler for this thread

        // Cancel discovery because it will slow down the connection
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        if (mmSocket == null) {
            mZeoMobileHBBluetoothHandler.manageConnectedSocketError(ZeoMobileHB_BluetoothHandler.THREAD_CLIENT, ZeoMobileHB_BluetoothHandler.ERROR_SOCKET_IS_NULL);
            return;
        }

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.d(_CTAG+".run","Starting Connect");
            mmSocket.connect();
            Log.d(_CTAG+".run", "Connect succeeded");
            mZeoMobileHBBluetoothHandler.manageConnectedSocket(mmSocket);
            return;
        } catch (IOException connectException) {
            // Unable to connect; do a retry
            Log.d(_CTAG+".run","Connect failed: "+connectException.getMessage()+"; retry");
            try {
                Class<?> clazz = mmDevice.getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = clazz.getMethod("createInsecureRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(4)};
                mmSocket  = (BluetoothSocket) m.invoke(mmDevice, params);
                mmSocket.connect();
                Log.d(_CTAG+".run", "Connect-retry succeeded");
                mZeoMobileHBBluetoothHandler.manageConnectedSocket(mmSocket);
                return;
            } catch (Exception connectException2) {
                Log.d(_CTAG+".run","Connect-retry failed: "+connectException2.getMessage()+"; retry");
                try {
                    // Unable to connect; close the socket and get out
                    mmSocket.close();
                } catch (IOException closeException) {}
            }

        }
        mZeoMobileHBBluetoothHandler.manageConnectedSocketError(ZeoMobileHB_BluetoothHandler.THREAD_CLIENT, ZeoMobileHB_BluetoothHandler.ERROR_SOCKET_CONNECT_FAILED);
    }

    /** Will cancel an in-progress connection, and close the socket */
    // thread context:  HeadbandActivity
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
        mmSocket = null;
    }
}
