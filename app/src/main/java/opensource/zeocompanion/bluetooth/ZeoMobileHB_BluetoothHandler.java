package opensource.zeocompanion.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.HeadbandActivity;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.zeo.ZAH_HeadbandRecord;

import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ZeoMobileHB_BluetoothHandler {
    private static final String _CTAG = "ZHH";
    public static final int STATE_ADAPT_DISABLED = 0;
    public static final int STATE_ADAPT_PENDING = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    public static final int STATE_DISCONNECTING = 5;

    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_BT_ADAPTOR = 1;
    public static final int ERROR_BT_NOT_ENABLED = 2;
    public static final int ERROR_ZEOAPP_NO_HB_REC = 3;
    public static final int ERROR_ZEO_HB_NOT_PAIRED = 4;
    public static final int ERROR_SYSTEM_SOCKET_ALLOC_FAILED = 5;
    public static final int ERROR_SOCKET_IS_NULL = 6;
    public static final int ERROR_SOCKET_CONNECT_FAILED = 7;
    public static final int ERROR_SOCKET_INSTREAM_NULL = 8;
    public static final int ERROR_SOCKET_OUTSTREAM_NULL = 9;

    public static final int THREAD_SERVER = 1;
    public static final int THREAD_CLIENT = 2;
    public static final int THREAD_IO = 3;

    public static final UUID ZEO_BT_ANDROID_UUID        = UUID.fromString("56b32a76-479b-43d4-99ff-42d79823d0a5");
    public static final UUID ZEO_BT_HEADBAND_UUID       = UUID.fromString("56b32a76-479b-43d4-99ff-42d79823d0a6");
    public static final UUID ZEO_BT_HEADBAND_ALT_UUID   = UUID.fromString("00000000-deca-fade-deca-deafdecacaff");
    public static final String SERVER_SERVICENAME = "Zeo Android";

    private HeadbandActivity mActivity = null;
    public BluetoothAdapter mBluetoothAdapter = null;
    //private BluetoothClientThread mClientThread = null;
    private BluetoothServerThread mServerThread = null;
    private BluetoothIOThread mIOThread = null;
    private short mNextSendSeqNo = 0;
    private boolean mAdaptPending = false;
    private int mReassembly_state = 0;
    private int mReassembly_currLen = 0;
    private byte[] mReassembly_bytes = null;

    // thread context:  MainActivity
    public ZeoMobileHB_BluetoothHandler(Activity activity)
    {
        mActivity = (HeadbandActivity)activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // thread context:  MainActivity
    public int initialize() {
        // now ensure bluetooth is enabled on the Android device
        if (mBluetoothAdapter == null) { return ERROR_NO_BT_ADAPTOR; }

        mNextSendSeqNo = (short)(new Random().nextInt(256));

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, HeadbandActivity.ACTIVITY_RESULT_REQUEST_ENABLE_BT);
            mAdaptPending = true;
            return ERROR_BT_NOT_ENABLED;
        }
        return ERROR_NONE;
    }

    // thread context:  MainActivity
    public int getState() {
        if (mBluetoothAdapter == null) { return STATE_ADAPT_DISABLED; }
        if (!mBluetoothAdapter.isEnabled()) {
            if (mAdaptPending) { return STATE_ADAPT_PENDING; }
            return STATE_ADAPT_DISABLED;
        }
        if (mIOThread != null) {
            if (mIOThread.mThreadIsRunning) { return STATE_CONNECTED; }
            else if (mIOThread.mThreadIsPreparing) { return STATE_CONNECTING; }
            else { return STATE_DISCONNECTING; }
        }
        if (mServerThread != null) {
            if (mServerThread.mThreadIsRunning) { return STATE_CONNECTING; }
            else if (mServerThread.mThreadIsPreparing) { return STATE_CONNECTING; }
            else { return STATE_DISCONNECTED; }
        }
        return STATE_DISCONNECTED;
    }

    // thread context:  MainActivity
    public void setPendingOff() {
        mAdaptPending = false;
    }

    // thread context:  MainActivity
    public int connectToHeadband() {
        if (mBluetoothAdapter == null) { return ERROR_NO_BT_ADAPTOR; }
        if (!mBluetoothAdapter.isEnabled()) { return ERROR_BT_NOT_ENABLED; }
        mReassembly_state = 0;
        mReassembly_currLen = 0;
        mReassembly_bytes = null;

        // obtain the ZeoApp's information about the headband
        ZAH_HeadbandRecord hdRec = mActivity.getActiveHeadbandRecord();
        if (hdRec == null) { return ERROR_ZEOAPP_NO_HB_REC; }

        // locate the Zeo headband in the existing list of bonded bluetooth devices
        BluetoothDevice foundDevice = null;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(hdRec.rBluetooth_address)) { foundDevice = device; }
            }
        }
        if (foundDevice == null) { return ERROR_ZEO_HB_NOT_PAIRED; }

        // we know the proper bluetooth device to use and it is still paired to the Android device;
        // now attempt a connection to the headband;
        // we are going to act as a client
        // Cancel any client thread currently running a connection
        //if (mClientThread != null) {
            //mClientThread.cancel();
            //mClientThread = null;
        //}

        // Start the thread to connect with the given device; the class initializer sets the mmSocket
        //mClientThread = new BluetoothClientThread(this, foundDevice, ZEO_BT_HEADBAND_UUID);
        //mClientThread.setName("BluetoothClientThread");
        //if (mClientThread.mmSocket == null) { return ERROR_SYSTEM_SOCKET_ALLOC_FAILED; }
        //mClientThread.start();

        // we know the proper bluetooth device to use and it is still paired to the Android device;
        // now attempt a connection to the headband;
        // we are going to act as a server
        // Cancel any server thread currently running a connection
        if (mServerThread != null) { mServerThread.disconnect(); mServerThread = null; }

        // Start the server thread; the class initializer sets the mmServerSocket
        mServerThread = new BluetoothServerThread(this, SERVER_SERVICENAME, ZEO_BT_ANDROID_UUID);
        mServerThread.setName("BluetoothServerThread");
        if (mServerThread.mmServerSocket == null) { return ERROR_SYSTEM_SOCKET_ALLOC_FAILED; }
        mServerThread.start();

        // return to the GUI
        return ERROR_NONE;
    }

    // thread context:  MainActivity
    // this comes from the BroadcastReceiver() in the MainActivity
    public boolean reassessBluetoothState(Intent intent){
        // received a global bluetooth notification message
        String action = intent.getAction();
        if (action == null) { return false; }
        if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: some device is requesting disconnect");
            if (mIOThread == null) { return false; }
            BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String theDevAddr = device.getAddress();
            if (!theDevAddr.equals(mIOThread.getDeviceMAC())) { return false; }
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: our headband is requesting disconnect");
            disconnectHeadband();
            return true;
        }
        else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: some device has disconnected");
            if (mIOThread == null) { return false; }
            BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String theDevAddr = device.getAddress();
            if (!theDevAddr.equals(mIOThread.getDeviceMAC())) { return false; }
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: our headband has disconnected");
            disconnectHeadband();
            return true;
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(_CTAG+".reassess", "Bluetooth adaptor is now disabled");
                disconnectHeadband();
                return true;
            } else {
                // ?!? bluetooth adaptor is now enabled; need to inform the fragment
            }
        } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: some device has connected");
            if (mIOThread == null) { return false; }
            BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String theDevAddr = device.getAddress();
            if (!theDevAddr.equals(mIOThread.getDeviceMAC())) { return false; }
            Log.d(_CTAG+".reassess", "Bluetooth broadcast: our headband has connected");
        }
        return false;
    }

    // thread context:  MainActivity
    public void disconnectHeadband() {
        // disconnect the IO then disconnect any pending connect attempts
        if (mIOThread != null) { mIOThread.disconnect(); mIOThread = null; }
        if (mServerThread != null) { mServerThread.disconnect(); mServerThread = null; }
    }

    public String getHeadbandName() {
        if (mIOThread == null) return "*none*";
        return mIOThread.getDeviceName();
    }
    public String getHeadbandMACString() {
        if (mIOThread == null) return "*none*";
        return mIOThread.getDeviceMAC();
    }

    // thread context:  BluetoothServerThread, BluetoothClientThread
    public void manageConnectedSocket(BluetoothSocket theSocket) {
        int msg = 0;
        int err = 0;

        // connection attempt was successful;
        // cancel any existing IO thread, then allocate a new one
        if (mIOThread != null) { mIOThread.disconnect(); mIOThread = null; }
        mIOThread = new BluetoothIOThread(this, theSocket);
        mIOThread.setName("BluetoothIOThread");

        // detect any errors in preparing the IO thread
        if (mIOThread.mmInStream == null) {
            err = ERROR_SOCKET_INSTREAM_NULL;
            msg = ZeoCompanionApplication.MESSAGE_HEADBAND_BLUETOOTH_HNDLR_ERR;
        }
        if (mIOThread.mmOutStream == null) {
            err = ERROR_SOCKET_OUTSTREAM_NULL;
            msg = ZeoCompanionApplication.MESSAGE_HEADBAND_BLUETOOTH_HNDLR_ERR;
        } else {
            // no errors, so start the IO thread
            msg = ZeoCompanionApplication.MESSAGE_HEADBAND_HB_CONNECT_OK;
            mIOThread.start();
        }

        // send the final results of the connect attempt to the MainActivity
        mActivity.mHandler.obtainMessage(msg, err, 0, null).sendToTarget();
    }

    // thread context:  BluetoothServerThread, BluetoothClientThread, BluetoothIOThread
    public void manageConnectedSocketError(int theThread, int theResult) {
        // send the error message to the MainActivity
        mActivity.mHandler.obtainMessage(ZeoCompanionApplication.MESSAGE_HEADBAND_BLUETOOTH_HNDLR_ERR, theResult, theThread, null).sendToTarget();
    }

    // thread context:  BluetoothIOThread
    public void message_received(int theLen, byte[] theBytes) {
        // Bluetooth RFCOMM message packets will have to be re-combined into PDUs;
        // it is possble that more than one PDU could be in a packet, or a packet can end a PDU then start a next;
        // remember the buffer from BluetoothIOThread is re-used, always 1024 bytes, and so must always System.arraycopy for the msg.obj
        if (theLen == 0) { return; }    // ignore zero-length packets

        switch (mReassembly_state) {
            case 0:
                // first attempt at reassembly; ignore everything until a message header is found
                if (theLen >= 4) {
                    if (theBytes[0] == 0x48 && theBytes[1] == 0x4d && theBytes[2] == 0x53 && theBytes[3] == 0x47) {
                        // simple case achieved; the packet is starting a PDU
                        Log.d(_CTAG+".msgrcv", mReassembly_state + ": Packet (" + theLen + ") is start of 1st PDU: " + Utilities.bytesToHex(theBytes, 0, 12));
                        mReassembly_state = 1;
                    }
                }
                if (mReassembly_state == 0) {
                    int m = theLen - 3;
                    int i = 1;
                    while (i < m) {
                        if (theBytes[i] == 0x48 && theBytes[i + 1] == 0x4d && theBytes[i + 2] == 0x53 && theBytes[i + 3] == 0x47) {
                            // found a PDU header in the packet after byte 0
                            Log.d(_CTAG+".msgrcv", mReassembly_state + ": Packet (" + theLen + ") is start of 1st PDU at " + i + ": " + Utilities.bytesToHex(theBytes, i, 12));
                            mReassembly_state = 1;
                        }
                        i++;
                    }
                }
                if (mReassembly_state == 0) {
                    return;  // ignore initial mid-split packets
                }
                // deliberately allow flow to pass to the next case

            case 1:
                // reassembly is synced and in-progress;
                // move or merge the new packet bytes into the stream buffer
                if (mReassembly_currLen == 0) {
                    byte[] newBytes1 = new byte[theLen];
                    System.arraycopy(theBytes, 0, newBytes1, 0, theLen);
                    mReassembly_bytes = null;   // help garbage collection
                    mReassembly_bytes = newBytes1;
                    mReassembly_currLen = theLen;
                    Log.d(_CTAG+".msgrcv", mReassembly_state + ": Packet (" + theLen + ") placed into buffer (" + mReassembly_currLen + "): " + Utilities.bytesToHex(theBytes, 0, 12));

                } else {
                    byte[] newBytes2 = new byte[mReassembly_currLen + theLen];
                    System.arraycopy(mReassembly_bytes, 0, newBytes2, 0, mReassembly_currLen);
                    System.arraycopy(theBytes, 0, newBytes2, mReassembly_currLen, theLen);
                    mReassembly_bytes = null;   // help garbage collection
                    mReassembly_bytes = newBytes2;
                    mReassembly_currLen = mReassembly_currLen + theLen;
                    Log.d(_CTAG+".msgrcv", mReassembly_state + ": Packet (" + theLen + ") merged into buffer(" + mReassembly_currLen + "): " + Utilities.bytesToHex(theBytes, 0, 12));
                }

                int msgHdrAt = 0;
                int someFoundAt = -1;
                while (msgHdrAt < mReassembly_currLen) {
                    if (mReassembly_bytes[msgHdrAt] == 0x48) {
                        // possibility of a message header
                        int remainingLen = mReassembly_currLen - msgHdrAt;
                        if (remainingLen >= 4) {
                            if (mReassembly_bytes[msgHdrAt + 1] == 0x4d && mReassembly_bytes[msgHdrAt + 2] == 0x53 && mReassembly_bytes[msgHdrAt + 3] == 0x47) {
                                // found a message header
                                someFoundAt = msgHdrAt;
                                if (remainingLen >= 12) {
                                    // and it has a complete base PDU
                                    int neededLen = 12 + (mReassembly_bytes[msgHdrAt + 11] << 8 | mReassembly_bytes[msgHdrAt + 10]);
                                    if (neededLen <= remainingLen) {
                                        // complete PDU is present; send the PDU to the MainActivity
                                        byte[] newBytes3 = new byte[neededLen];
                                        System.arraycopy(mReassembly_bytes, msgHdrAt, newBytes3, 0, neededLen);
                                        Log.d(_CTAG+".msgrcv", mReassembly_state + ": Found complete PDU (" + neededLen + ") at " + msgHdrAt + ": " + Utilities.bytesToHex(newBytes3, 0, 12));
                                        mActivity.mHandler.obtainMessage(ZeoCompanionApplication.MESSAGE_HEADBAND_RECV_HB_MSG, neededLen, 0, newBytes3).sendToTarget();

                                        // and remove the PDU from the reassembly buffer
                                        int newLen = mReassembly_currLen - neededLen;
                                        if (newLen <= 0) {
                                            // buffer is completely empty; reset and wait for more packets
                                            mReassembly_bytes = null;   // help garbage collection
                                            mReassembly_currLen = 0;
                                            Log.d(_CTAG+".msgrcv", mReassembly_state + ": Buffer now empty; waiting");
                                            return;
                                        } else {
                                            // there is residual data in the reassembly buffer; shrink the buffer;
                                            // then reset the main loop to search this smaller reassembly buffer from the start
                                            byte[] newBytes4 = new byte[newLen];
                                            System.arraycopy(mReassembly_bytes, msgHdrAt + neededLen, newBytes4, 0, newLen);
                                            mReassembly_bytes = null;   // help garbage collection
                                            mReassembly_bytes = newBytes4;
                                            mReassembly_currLen = newLen;
                                            msgHdrAt = -1;
                                        }
                                    } else {
                                        // base PDU is present but data is incomplete; wait for more packets
                                        Log.d(_CTAG+".msgrcv", mReassembly_state + ": Found incomplete PDU (" + remainingLen + " of " + neededLen + ") at " + msgHdrAt + "; waiting: " + Utilities.bytesToHex(mReassembly_bytes, msgHdrAt, 12));
                                        return;
                                    }
                                } else {
                                    // base PDU is incomplete; wait for more packets
                                    Log.d(_CTAG+".msgrcv", mReassembly_state + ": Found incomplete base PDU (" + remainingLen + " of unknown) at " + msgHdrAt + "; waiting: " + Utilities.bytesToHex(mReassembly_bytes, msgHdrAt, 12));
                                    return;
                                }
                            }
                        }
                    }
                    msgHdrAt++; // continue looking for a message header
                }
                // no further message headers were found in the reassembly buffer
                if (someFoundAt < 0) {
                    // entire reassembly buffer has no possible message header;
                    // flush out all but the last 3 bytes
                    if (mReassembly_currLen > 3) {
                        int flushLen = mReassembly_currLen - 3;
                        Log.d(_CTAG+".msgrcv", mReassembly_state + ": Residual in buffer flushed of "+flushLen+" bytes; waiting");
                        byte[] newBytes5 = new byte[3];
                        System.arraycopy(mReassembly_bytes, mReassembly_currLen - 3, newBytes5, 0, 3);
                        mReassembly_bytes = null;   // help garbage collection
                        mReassembly_bytes = newBytes5;
                        mReassembly_currLen = 3;
                    }
                }
        }
    }

    // thread context:  MainActivity
    public String getErrorMessageString(int theError) {
        switch (theError) {
            case ERROR_NONE:
                return "No error";
            case ERROR_NO_BT_ADAPTOR:
                return "No Bluetooth adapter";
            case ERROR_BT_NOT_ENABLED:
                return "Bluetooth not enabled";
            case ERROR_ZEOAPP_NO_HB_REC:
                return "ZeoApp is not installed or has not yet been used";
            case ERROR_ZEO_HB_NOT_PAIRED:
                return "Zeo Headband is not currently paired";
            case ERROR_SYSTEM_SOCKET_ALLOC_FAILED:
                return "No Bluetooth adapter";
            case ERROR_SOCKET_IS_NULL:
                return "Bluetooth not enabled";
            case ERROR_SOCKET_CONNECT_FAILED:
                return "ZeoApp is not installed or has not yet been used";
            case ERROR_SOCKET_INSTREAM_NULL:
                return "Zeo Headband is not currently paired";
            case ERROR_SOCKET_OUTSTREAM_NULL:
                return "Zeo Headband is not currently paired";
            default:
                return "LOGIC-ERROR: Unknown ZeoMobileHB_BluetoothHandler Error";
        }
    }

    // thread context:  MainActivity
    public void send_message_to_HB(ZeoMobileHB_Msg theMsg) {
        theMsg.setSeqNo(mNextSendSeqNo);
        mNextSendSeqNo++;
        if (mNextSendSeqNo >= 256) { mNextSendSeqNo = 0; }
        if (theMsg.rMsgType == ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TIME_QUERY)  { theMsg.setTimeQuery(); };

        theMsg.addCRC();

        if (mIOThread == null) return;
        mIOThread.write(theMsg.rBytes);
        int showLen = theMsg.mRecLen;
        if (showLen > 40) showLen = 40;
        Log.d(_CTAG+".sendMsg", "Sent Message (" + theMsg.mRecLen + ") to headband: " + Utilities.bytesToHex(theMsg.rBytes, 0, showLen));
    }
}