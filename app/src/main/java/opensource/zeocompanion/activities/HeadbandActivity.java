package opensource.zeocompanion.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.bluetooth.ZeoMobileHB_Msg;
import opensource.zeocompanion.fragments.HeadbandActivityFragment;
import opensource.zeocompanion.zeo.ZAH_HeadbandRecord;

// Activity for providing the Headband Commander
public class HeadbandActivity extends AppCompatActivity {
    // note: the Headband Activity and its Fragments will not be destroyed/recreated upon rotation
    public static final String _CTAG = "HBA";
    public static final int ACTIVITY_RESULT_REQUEST_ENABLE_BT = 100;

    // receive system-wide broadcasts about changes in Bluetooth state
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HeadbandActivityFragment frag= (HeadbandActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_headband);
            frag.reassessBluetoothState(intent);
        }
    };

    // receive inter-process messages from our sub-threads
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what >= ZeoCompanionApplication.MESSAGE_HEADBAND_HBFRAG_LOW && msg.what <= ZeoCompanionApplication.MESSAGE_HEADBAND_HBFRAG_HIGH) {
                // message# is in the range for the bluetooth threads used by the headband fragment
                HeadbandActivityFragment frag= (HeadbandActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_headband);
                frag.receivedThreadMessage(msg);
            }
        }
    };

    // called then the Activity is first created or upon return of some other Activity back to this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(_CTAG + ".onCreate", "=====ON-CREATE=====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_headband);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    // setup the action bar to have a back arrow
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // called when the App is being terminated
    @Override
    public void onDestroy() {
        Log.d(_CTAG + ".onDestroy", "=====ON-DESTROY=====");
        super.onDestroy();
        unregisterReceiver(mBluetoothReceiver);
    }

    // obtain the active headband's record in the Zeo App database
    public ZAH_HeadbandRecord getActiveHeadbandRecord() {
        return ZeoCompanionApplication.mZeoAppHandler.getActiveHeadbandRecord();
    }

    // callback from any sub-Activity invoked using startActivityForResult()
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_RESULT_REQUEST_ENABLE_BT) {
            HeadbandActivityFragment frag= (HeadbandActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_headband);
            frag.handleActivityResult(requestCode, resultCode, data);
        }
    }

    // callback from HeadbandSendDialogFragment results
    public void onMsgReadyToSend(ZeoMobileHB_Msg theMsgBuf) {
        HeadbandActivityFragment frag= (HeadbandActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_headband);
        frag.handleMsgReadyToSend(theMsgBuf);
    }
}
