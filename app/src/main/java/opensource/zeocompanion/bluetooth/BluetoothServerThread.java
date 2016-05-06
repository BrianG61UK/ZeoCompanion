package opensource.zeocompanion.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;

import opensource.zeocompanion.ZeoCompanionApplication;

public class BluetoothServerThread extends Thread {
    private static final String _CTAG = "BST";
    public BluetoothServerSocket mmServerSocket = null;
    private ZeoMobileHB_BluetoothHandler mZeoMobileHBBluetoothHandler = null;
    public boolean mThreadIsPreparing = true;
    public boolean mThreadIsRunning = false;

    // thread context:  Activity
    public BluetoothServerThread(ZeoMobileHB_BluetoothHandler zeoMobileHBBluetoothHandler, String theName, UUID theUUID) {
        mZeoMobileHBBluetoothHandler = zeoMobileHBBluetoothHandler;
        BluetoothServerSocket tmp = null;
        try {
            tmp = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord(theName, theUUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    // thread context:  BluetoothServerThread
    public void run() {
        Thread.setDefaultUncaughtExceptionHandler(ZeoCompanionApplication.mMasterAbortHandler); // set the master abort handler for this thread
        mThreadIsRunning = true;
        mThreadIsPreparing = false;
        if (mmServerSocket == null) {
            mZeoMobileHBBluetoothHandler.manageConnectedSocketError(ZeoMobileHB_BluetoothHandler.THREAD_SERVER, ZeoMobileHB_BluetoothHandler.ERROR_SOCKET_IS_NULL);
            mThreadIsRunning = false;
            return;
        }

        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                Log.d(_CTAG+".run","Starting Accept");
                socket = mmServerSocket.accept();
                // If a
                if (socket != null) {
                    Log.d(_CTAG+".run", "Accept succeeded");
                    mZeoMobileHBBluetoothHandler.manageConnectedSocket(socket);
                    mmServerSocket.close();
                    mmServerSocket = null;
                    mThreadIsRunning = false;
                    return;
                }
            } catch (IOException connectException) {
                Log.d(_CTAG+".run", "Accept failed: " + connectException.getMessage());
                break;
            }
        }
        mZeoMobileHBBluetoothHandler.manageConnectedSocketError(ZeoMobileHB_BluetoothHandler.THREAD_SERVER, ZeoMobileHB_BluetoothHandler.ERROR_SOCKET_CONNECT_FAILED);
        mThreadIsRunning = false;
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    // thread context:  Activity
    public void disconnect() {
        if (mmServerSocket != null) {
            try {mmServerSocket.close(); } catch (IOException e) {}
            mmServerSocket = null;
        }
    }
}
