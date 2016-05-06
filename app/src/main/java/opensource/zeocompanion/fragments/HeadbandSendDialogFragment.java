package opensource.zeocompanion.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.activities.HeadbandActivity;
import opensource.zeocompanion.bluetooth.ZeoMobileHB_Msg;

public class HeadbandSendDialogFragment extends DialogFragment {
    // note: the Headband Activity and its Fragments will not be destroyed/recreated upon rotation
    private View mRootView = null;
    private ListView mListView = null;
    private ArrayAdapter<String> mListView_Adapter = null;
    private ArrayList<String> mListView_List = null;
    private boolean mIncludeAcqReq = false;
    private int mSubpage = -1;
    private int mSubpageItem = -1;
    private short mNextCommand = ZeoMobileHB_Msg.ZEOMOB_HB_CMD_NMAX;
    private short[] mCommands = null;

    public interface OnHeadbandSendListener {
        public void onMsgReadyToSend(ZeoMobileHB_Msg theMsgBuf);
    }

    public HeadbandSendDialogFragment() {}

    public static HeadbandSendDialogFragment newInstance() {
        return new HeadbandSendDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_headband_send, container, false);

        mSubpage = -1;
        mListView = (ListView) mRootView.findViewById(R.id.listView_main);
        mListView_List = new ArrayList<String>();
        fillListView();
        mListView_Adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.listview_checkmarkrow, mListView_List);
        mListView.setAdapter(mListView_Adapter);
        mSubpage = 0;
        clearAllCheckmarks();

        mRootView.findViewById(R.id.checkBox_AcqReq).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mIncludeAcqReq = ((CheckBox)v).isChecked();
            }
        });

        mRootView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        mRootView.findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSubpage = 0;
                fillListView();
                Button b = (Button)mRootView.findViewById(R.id.button_back);
                b.setEnabled(false);
            }
        });

        mRootView.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ZeoMobileHB_Msg newMsg = new ZeoMobileHB_Msg();
                switch (mSubpage) {
                    case 0:
                        // not applicable
                        break;
                    case 1:
                        // Command... submenu was selected to send
                        short cmdNo = mCommands[mSubpageItem];
                        /*if (mSubpageItem == 0) {
                            // next command menu item
                            if (mNextCommand >= 0 && mNextCommand < ZeoMobileHB_Msg.ZEOMOB_HB_CMD_NMAX) {
                                cmdNo = ZeoMobileHB_Msg.ZEOMOB_HB_CMD_NMAX;
                                mNextCommand = (short)(cmdNo + 1);
                            } else if (mNextCommand >= 256 || mNextCommand < 0) {
                                cmdNo = ZeoMobileHB_Msg.ZEOMOB_HB_CMD_NMAX;
                                mNextCommand = (short)(cmdNo + 1);
                            } else if (mNextCommand == ZeoMobileHB_Msg.ZEOMOB_HB_CMD_REBOOT) {
                                cmdNo = ZeoMobileHB_Msg.ZEOMOB_HB_CMD_REBOOT + 1;
                                mNextCommand = (short) (cmdNo + 1);
                            } else {
                                cmdNo = mNextCommand;
                                mNextCommand++;
                            }
                            newMsg.makeCommand(cmdNo);
                        } else {*/
                            // normal menu item
                            newMsg.makeCommand(cmdNo);
                        //}
                        newMsg.setAcqReq(mIncludeAcqReq);
                        ((HeadbandActivity)getActivity()).onMsgReadyToSend(newMsg);
                        break;
                    case 2:
                        // Time Query selected to send
                        newMsg.makeTimeQuery();
                        newMsg.setAcqReq(mIncludeAcqReq);
                        ((HeadbandActivity)getActivity()).onMsgReadyToSend(newMsg);
                        break;
                    case 3:
                        // ?!? not implemented due to suspended investigation of the headband's protocol
                        break;
                    case 4:
                        // Test... submenu was selected to send
                        short msgNo = mCommands[mSubpageItem];
                        /*if (mSubpageItem == 0) {
                            // next message menu item
                            if (mNextCommand >= 0 && mNextCommand <= ZeoMobileHB_Msg.ZEOMOB_HB_MSG_WAKEUP_WINDOW) {
                                msgNo = ZeoMobileHB_Msg.ZEOMOB_HB_MSG_WAKEUP_WINDOW + 1;
                                mNextCommand = (short)(msgNo + 1);
                            } else if (mNextCommand >= ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_FIRST && mNextCommand <= ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_WAVEFORM_REPLY) {
                                msgNo = ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_WAVEFORM_REPLY + 1;
                                mNextCommand = (short)(msgNo + 1);
                            } else if (mNextCommand >= 256 || mNextCommand < 0) {
                                msgNo = ZeoMobileHB_Msg.ZEOMOB_HB_MSG_WAKEUP_WINDOW + 1;
                                mNextCommand = (short) (msgNo + 1);
                            } else if (msgNo == 173) {
                                // DANGER: sending message #173 will brick the headband
                                // Note:  messages #174 to #255 have NOT been tested
                                mNextCommand++;
                                msgNo = mNextCommand;
                            } else {
                                msgNo = mNextCommand;
                                mNextCommand++;
                            }
                            newMsg.makeTestQuery(msgNo, (short)2);*/
                        //} else
                        if (msgNo == ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_LED) {
                            newMsg.makeTestLEDON(true);
                        } else if (msgNo == -ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_LED) {
                            newMsg.makeTestLEDON(false);
                        } else if (msgNo == ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE) {
                            newMsg.makePCBTestMode(true);
                        } else if (msgNo == -ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE) {
                            newMsg.makePCBTestMode(false);
                        } else {
                            newMsg.makeTestQuery(msgNo, (short)2);
                        }
                        newMsg.setAcqReq(mIncludeAcqReq);
                        ((HeadbandActivity) getActivity()).onMsgReadyToSend(newMsg);
                        break;
                }
                dismiss();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clearAllCheckmarks();
                switch (mSubpage) {
                    case 2:
                    case 0:
                        // root menu is showing, or root menu is showing with the Time Query selected
                        if (id >= 0 && id <= 3) { mSubpage = (int)id + 1; }
                        switch (mSubpage) {
                            case 1:
                                fillListView();
                                Button b1 = (Button)mRootView.findViewById(R.id.button_back);
                                b1.setEnabled(true);
                                break;
                            case 2:
                                mSubpageItem = (int)id;
                                mListView.setItemChecked(position, true);
                                mListView_Adapter.notifyDataSetChanged();
                                Button b2 = (Button)mRootView.findViewById(R.id.button_send);
                                b2.setEnabled(true);
                                break;
                            case 3:
                                // ?!? code never implemented for this command type
                                break;
                            case 4:
                                fillListView();
                                Button b5 = (Button)mRootView.findViewById(R.id.button_back);
                                b5.setEnabled(true);
                                break;
                        }
                        break;
                    case 1:
                        // Command... subpage is showing, so user selected an item from this submenu
                        mSubpageItem = (int)id;
                        mListView.setItemChecked(position, true);
                        mListView_Adapter.notifyDataSetChanged();
                        Button b3 = (Button)mRootView.findViewById(R.id.button_send);
                        b3.setEnabled(true);
                        break;
                    case 3:
                        // not applicable
                        break;
                    case 4:
                        // Test... subpage is showing, so user selected an item from this submenu
                        mSubpageItem = (int)id;
                        mListView.setItemChecked(position, true);
                        mListView_Adapter.notifyDataSetChanged();
                        Button b4 = (Button)mRootView.findViewById(R.id.button_send);
                        b4.setEnabled(true);
                        break;
                }
            }
        });

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle("Send Message");
    }

    private void clearAllCheckmarks() {
        int m = mListView.getCount();
        for (int i=0; i < m; i++) { mListView.setItemChecked(i, false); }
    }

    private void fillListView() {
        mCommands = null;
        switch (mSubpage) {
            case -1:
                mListView_List.add("Command...");
                mListView_List.add("Time Query");
                mListView_List.add("Wakeup Window...");
                mListView_List.add("Test...");
                mListView.setVisibility(View.VISIBLE);
                break;
            case 0:
                mListView_List.clear();
                mListView_List.add("Command...");
                mListView_List.add("Time Query");
                mListView_List.add("Wakeup Window...");
                mListView_List.add("Test...");
                mListView_Adapter.notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                break;
            case 1:
                mListView_List.clear();
                mListView_List.add("Request State Report");
                mListView_List.add("Request Sleep Report");
                mListView_List.add("Set Bluetooth Lock");
                mListView_List.add("Set Bluetooth Unlock");
                mListView_List.add("Set Demo Mode On");
                mListView_List.add("Set Demo Mode Off");
                mListView_List.add("Set Sleep Start");
                mListView_List.add("Set Sleep Stop");
                mListView_List.add("Factory Reset");
                mListView_List.add("Reset Sensor Use");
                mListView_List.add("Reboot Headband");
                mListView_Adapter.notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                mCommands = new short[] {
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_QUERY_STATE, ZeoMobileHB_Msg.ZEOMOB_HB_CMD_SLEEP_SEND,
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_BLUETOOTH_LOCK, ZeoMobileHB_Msg.ZEOMOB_HB_CMD_BLUETOOTH_UNLOCK,
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_DEMO_MODE_ON, ZeoMobileHB_Msg.ZEOMOB_HB_CMD_DEMO_MODE_OFF,
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_SLEEP_START, ZeoMobileHB_Msg.ZEOMOB_HB_CMD_SLEEP_STOP,
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_FACTORY_RESET, ZeoMobileHB_Msg.ZEOMOB_HB_CMD_RESET_SENSOR_USE,
                        ZeoMobileHB_Msg.ZEOMOB_HB_CMD_REBOOT };
                break;
            case 2:
                mListView.setVisibility(View.VISIBLE);
                break;
            case 3:
                mListView.setVisibility(View.INVISIBLE);
                break;
            case 4:
                mListView_List.clear();
                mListView_List.add("Test LED ON=true");
                mListView_List.add("Test LED ON=false");
                mListView_List.add("Test Voltage Query");
                mListView_List.add("Test Cal Data Query");
                mListView_List.add("Test First");
                mListView_List.add("Test Impedance");
                mListView_List.add("Test Waveform");
                mListView_List.add("Test Freq Tim Set");
                mListView_List.add("Test Analog *DISCON*");
                mListView_List.add("Test PCB Test Mode=true *DISCON*");
                mListView_List.add("Test PCB Test Mode=false *DISCON*");
                mListView_List.add("Test Accel Query *DISCON*");
                mListView_List.add("Test ACP Query *DISCON*");
                mListView_Adapter.notifyDataSetChanged();
                mListView.setVisibility(View.VISIBLE);
                mCommands = new short[] { ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_LED, -ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_LED,
                        ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_VOLTAGE_QUERY, ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_CAL_DATA_QUERY,
                        ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_FIRST, ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_IMPEDANCE,
                        ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_WAVEFORM, ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_FREQ_TRIM_SET,
                        ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_ANALOG, ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE, -ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_PCB_TEST_MODE,
                        ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_ACCEL_QUERY, ZeoMobileHB_Msg.ZEOMOB_HB_MSG_TEST_ACP_QUERY };
                break;
        }
    }
}