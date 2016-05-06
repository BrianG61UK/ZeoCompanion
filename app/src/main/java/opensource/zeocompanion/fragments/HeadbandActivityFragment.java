package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.HeadbandActivity;
import opensource.zeocompanion.bluetooth.ZeoMobileHB_BluetoothHandler;
import opensource.zeocompanion.bluetooth.ZeoMobileHB_Msg;
import opensource.zeocompanion.utility.Utilities;

public class HeadbandActivityFragment extends Fragment {
    // note: the Headband Activity and its Fragments will not be destroyed/recreated upon rotation
    private static final String _CTAG = "HAF";

    private View mRootView = null;
    private ListView mListView = null;
    private ZeoMobHBMsgAdapter mListView_Adapter = null;
    private ArrayList<ZeoMobileHB_Msg> mListView_List = null;
    private ZeoMobileHB_BluetoothHandler mBluetoothHandler = null;
    private HeadbandSendDialogFragment mHBsendFrag = null;

    public HeadbandActivityFragment() {}

    public static HeadbandActivityFragment newInstance() {
        return new HeadbandActivityFragment();
    }

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);
        mBluetoothHandler = new ZeoMobileHB_BluetoothHandler(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_headband, container, false);

        mListView = (ListView) mRootView.findViewById(R.id.listView_info);
        mListView_List = new ArrayList<ZeoMobileHB_Msg>();
        mListView_Adapter = new ZeoMobHBMsgAdapter(this.getActivity(), R.layout.listview_rightleftrow, mListView_List);
        mListView.setAdapter(mListView_Adapter);

        mHBsendFrag = new HeadbandSendDialogFragment();

        mRootView.findViewById(R.id.button_connect).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBluetoothHandler.getState() == ZeoMobileHB_BluetoothHandler.STATE_DISCONNECTED) {
                    // we are not connected to the headband, so start a connect
                    int result  = mBluetoothHandler.connectToHeadband();
                    configureUI();
                    if (result != ZeoMobileHB_BluetoothHandler.ERROR_NONE) { display_BTHDLR_Error(result); }
                } else {
                    // for all other states, just attempt a disconnect
                    mBluetoothHandler.disconnectHeadband();
                    configureUI();
                }
            }
        });

        mRootView.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // popup a send message dialog;
                // note the result of the dialog will be sent to the MainActivity which will then get passed to this Fragment for handling
                mHBsendFrag.show(getFragmentManager(),"DiagHBS");
            }
        });

        mBluetoothHandler.initialize();
        configureUI();
        return mRootView;
    }

    public void configureUI() {
        Button butConnect = (Button) mRootView.findViewById(R.id.button_connect);
        Button butSend = (Button) mRootView.findViewById(R.id.button_send);
        TextView tv = (TextView)mRootView.findViewById(R.id.textView_headband);
        int state = mBluetoothHandler.getState();
        switch (state) {
            case ZeoMobileHB_BluetoothHandler.STATE_ADAPT_DISABLED:
                butConnect.setText("BT disabled");
                butConnect.setEnabled(false);
                butSend.setEnabled(false);
                tv.setText("Adaptor is disabled");
                break;
            case ZeoMobileHB_BluetoothHandler.STATE_ADAPT_PENDING:
                butConnect.setText("BT pending...");
                butConnect.setEnabled(false);
                butSend.setEnabled(false);
                tv.setText("Adaptor is pending user enablement...");
                break;
            case ZeoMobileHB_BluetoothHandler.STATE_DISCONNECTED:
                butConnect.setText("Connect");
                butConnect.setEnabled(true);
                butSend.setEnabled(false);
                tv.setText("Not connected");
                break;
            case ZeoMobileHB_BluetoothHandler.STATE_CONNECTING:
                butConnect.setText("Connecting...");
                butConnect.setEnabled(false);
                butSend.setEnabled(false);
                tv.setText("Connecting to headband...");
                break;
            case ZeoMobileHB_BluetoothHandler.STATE_CONNECTED:
                butConnect.setText("Disconnect");
                butConnect.setEnabled(true);
                butSend.setEnabled(true);
                break;
            case ZeoMobileHB_BluetoothHandler.STATE_DISCONNECTING:
                butConnect.setText("Disconnecting...");
                butConnect.setEnabled(false);
                butSend.setEnabled(false);
                tv.setText("Disconnecting from headband...");
                break;
        }
    }

    // callback from any sub-Activity invoked using startActivityForResult()
    public void handleActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == AppCompatActivity.RESULT_CANCELED) { mBluetoothHandler.setPendingOff(); }
        configureUI();
    }

    public void handleMsgReadyToSend(ZeoMobileHB_Msg theMsgBuf) {
        // a ZeoMobile Headband Message is composed and nearly ready to send
        mBluetoothHandler.send_message_to_HB(theMsgBuf);
        theMsgBuf.mWasSent = true;
        mListView_List.add(0, theMsgBuf);
        mListView_Adapter.notifyDataSetChanged();
    }

    public void reassessBluetoothState(Intent intent){
        boolean isOurs = mBluetoothHandler.reassessBluetoothState(intent);
        if (isOurs) { configureUI(); }
    }

    public void receivedThreadMessage(Message msg) {
        switch (msg.what) {
            case ZeoCompanionApplication.MESSAGE_HEADBAND_BLUETOOTH_HNDLR_ERR:
                display_BTHDLR_Error(msg.arg1);
                configureUI();
                break;
            case ZeoCompanionApplication.MESSAGE_HEADBAND_HB_CONNECT_OK:
                configureUI();
                TextView tv = (TextView)mRootView.findViewById(R.id.textView_headband);
                tv.setText("Name="+mBluetoothHandler.getHeadbandName()+"\nMac="+mBluetoothHandler.getHeadbandMACString());
                break;
            case ZeoCompanionApplication.MESSAGE_HEADBAND_RECV_HB_MSG:
                byte[] theBytes = (byte[])msg.obj;
                int showLen = msg.arg1;
                if (showLen > 40) { showLen = 40; }
                Log.d("HBFrag.rcvThreadMsg", "HB Message received (" + msg.arg1 + "): " + Utilities.bytesToHex(theBytes, 0, showLen));
                ZeoMobileHB_Msg theMsg = new ZeoMobileHB_Msg(msg.arg1, theBytes);
                if (theMsg.mIsValid) { mListView_List.add(0, theMsg); mListView_Adapter.notifyDataSetChanged(); }
                break;
            default:
                break;
        }
    }

    private void display_BTHDLR_Error(int theError) {
        String theString = mBluetoothHandler.getErrorMessageString(theError);
        Toast.makeText(getContext().getApplicationContext(), "ERROR: "+theString, Toast.LENGTH_SHORT).show();
    }
}

class ZeoMobHBMsgAdapter extends ArrayAdapter<ZeoMobileHB_Msg> {

    private Context mContext;
    private int mLayoutResourceId;
    private ArrayList<ZeoMobileHB_Msg> mArrayList = null;

    public ZeoMobHBMsgAdapter(Context context, int layoutResourceId, ArrayList<ZeoMobileHB_Msg> list) {
        super(context, layoutResourceId, list);
        this.mLayoutResourceId = layoutResourceId;
        this.mContext = context;
        this.mArrayList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // first construct the row's template
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            rowView = inflater.inflate(mLayoutResourceId, parent, false);
        }

        // now properly configure the row's data and attributes
        ZeoMobileHB_Msg theMsg = mArrayList.get(position);
        String theMsgString = "#"+theMsg.rSeqNo+": "+theMsg.getMessageTypeString()+": "+theMsg.getDataString();
        TextView tr = (TextView)rowView.findViewById(R.id.rowTextViewRight);
        TextView tl = (TextView)rowView.findViewById(R.id.rowTextViewLeft);
        if (theMsg.mWasSent) {
            tr.setText(theMsgString);
            tr.setVisibility(View.VISIBLE);
            tl.setVisibility(View.INVISIBLE);
        } else {
            tl.setText(theMsgString);
            tl.setVisibility(View.VISIBLE);
            tr.setVisibility(View.INVISIBLE);
        }
        return rowView;
    }
}

