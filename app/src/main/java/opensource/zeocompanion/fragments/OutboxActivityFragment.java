package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.DirectEmailerOutbox;
import opensource.zeocompanion.utility.DirectEmailerThread;
import opensource.zeocompanion.utility.Utilities;

// Fragment for showing and managing the email outbox
public class OutboxActivityFragment extends Fragment implements EditTextDialogFragment.OnExitTextFragListener {
    // member variables
    View mRootView = null;
    private ListView mListView = null;
    private OutboxAdapter mListView_Adapter = null;
    private ArrayList<DirectEmailerOutbox.OutboxEntry> mListView_Array = null;

    // member constants and other static content
    private static final String _CTAG = "OAF";

    // internal handler to move state change detections from the Email thread into the main thread
    private Handler mEmailResendHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ZeoCompanionApplication.MESSAGE_OUTBOX_EMAILRESEND_RESULTS) {
                DirectEmailerOutbox.OutboxEntry rec = ZeoCompanionApplication.mEmailOutbox.readOutboxFile((String)msg.obj);
                if (rec != null) {
                    ZeoCompanionApplication.mEmailOutbox.deleteOutboxEntry(rec);
                    refresh();
                    informMainActivity();
                }
            }
        }
    };

    // thread context:  DirectEmailerThread
    // Outbox resend email result is available for the Outbox; but has to be passed via a message
    DirectEmailerThread.DirectEmailerThreadResponse mEmailResult = new DirectEmailerThread.DirectEmailerThreadResponse() {
        @Override
        public void emailResults(String callback, boolean successFail, String ignored) {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_OUTBOX_EMAILRESEND_RESULTS;
            if (successFail) { msg.arg1 = 1; }
            else { msg.arg1 = 0; }
            msg.obj = callback;
            mEmailResendHandler.sendMessage(msg);
        }
    };

    // listener to the answer to the Yes/No Confirmation to delete an Outbox entry
    private Utilities.ShowYesNoDialogInterface mYesNoListener = new Utilities.ShowYesNoDialogInterface() {
        @Override
        public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
            if (theResult) {
                DirectEmailerOutbox.OutboxEntry rec = ZeoCompanionApplication.mEmailOutbox.readOutboxFile(callbackString1);
                if (rec != null) {
                    ZeoCompanionApplication.mEmailOutbox.deleteOutboxEntry(rec);
                    refresh();
                    informMainActivity();
                }
            }
        }
    };

    // listener to the delete button click; just invoke a yes/no confirmation dialog
    private Button.OnClickListener mDeleteButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View button) {
            DirectEmailerOutbox.OutboxEntry rec = (DirectEmailerOutbox.OutboxEntry)button.getTag();
            Utilities.showYesNoDialog(getContext(), "Confirm", "Are you sure you want to permanently delete this Outbox entry", "Delete", "Cancel", mYesNoListener, 1, rec.rFilePath, null);
        }
    };

    // listener to the resend button click; setup the proper handlers to resend the email
    private Button.OnClickListener mResendButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View button) {
            DirectEmailerOutbox.OutboxEntry rec = (DirectEmailerOutbox.OutboxEntry)button.getTag();
            DirectEmailerThread de = new DirectEmailerThread(getContext());
            de.setName("DirectEmailerThread via "+_CTAG+".OnClickListener.Resend");
            File attachment = null;
            if (rec.rAttachmentPath != null) {
                if (!rec.rAttachmentPath.isEmpty()) { attachment = new File(rec.rAttachmentPath); }
            }
            int result = de.configure(rec.rSubject, rec.rBody, attachment, false);
            if (result < 0) {
                Utilities.showAlertDialog(getContext(), "Settings", "The necessary Settings are not in-place to be able to send a direct email; please configure you email account and destinations in the Settings.", "Okay");
            } else {
                if (!rec.rToAll) { de.configureToAddress(rec.rToAddress); }
                de.setResultsCallback(mEmailResult, rec.rFilePath, true);
                de.start();
            }
        }
    };

    // listener to the ToAddress button; invoke the EditText dialog to let the end-user change the To Address
    private Button.OnClickListener mChangeToAddrButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View button) {
            DirectEmailerOutbox.OutboxEntry rec = (DirectEmailerOutbox.OutboxEntry)button.getTag();
            if (rec.rToAll) { Toast.makeText(getContext(), "Notice: cannot add or alter the To Address for this 'to all' email.", Toast.LENGTH_SHORT).show(); return; }

            EditTextDialogFragment editFrag = EditTextDialogFragment.newInstance1(rec.rToAddress, "Change To Email Address", "c", rec.rFilePath, rec.rToAddress);
            editFrag.setTargetFragment(OutboxActivityFragment.this, 1);
            editFrag.show(getFragmentManager(), "DiagETF");
        }
    };

    // constructor
    public OutboxActivityFragment() {}

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_outbox, container, false);

        mListView_Array = new ArrayList<DirectEmailerOutbox.OutboxEntry>();
        String msg = ZeoCompanionApplication.mEmailOutbox.getAllOutboxEntries(mListView_Array);
        if (msg != null) {
            if (!msg.isEmpty()) { Utilities.showAlertDialog(getContext(), "Error", msg, "Okay"); }
        }

        mListView = (ListView)mRootView.findViewById(R.id.listView_outbox);
        mListView_Adapter = new OutboxAdapter(getActivity(), R.layout.fragment_outbox_row, mListView_Array);
        mListView.setAdapter(mListView_Adapter);
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        mListView_Array.clear();
        mListView_Adapter.notifyDataSetChanged();

        super.onDestroyView();
    }

    // refresh the Outbox list after actions that may have changed its contents
    private void refresh() {
        mListView_Array.clear();
        String msg = ZeoCompanionApplication.mEmailOutbox.getAllOutboxEntries(mListView_Array);
        if (msg != null) {
            if (!msg.isEmpty()) { Utilities.showAlertDialog(getContext(), "Error", msg, "Okay"); }
        }
        mListView_Adapter.notifyDataSetChanged();
    }

    // callback handler for the final text from the EditTextDialogFragment for adding or changing a Value record
    public void editedText(int qtyOfFields, String newToAddress, String ignored1, String action, String filePath, String origToAddress, String ignored2) {
        // initial checks of the results for non-changed entries or blank results
        if (newToAddress.isEmpty()) { Toast.makeText(getActivity(), "Cannot set the To Address blank", Toast.LENGTH_LONG).show(); return; }
        if (newToAddress.equals(origToAddress)) { return; }

        // for a Change, first locate the original entry in the ListView list
        ZeoCompanionApplication.mEmailOutbox.changeToAddress(filePath, newToAddress);
        refresh();
    }

    // inform the Main Activity so it can changes its menus; must be done via messaging
    private void informMainActivity() {
        if (MainActivity.instance != null) {
            Message msg = new Message();
            msg.what = ZeoCompanionApplication.MESSAGE_MAIN_UPDATE_MENU;
            MainActivity.instance.mHandler.sendMessage(msg);
        }
    }

    // predefined date formater for the list row entries
    private static SimpleDateFormat mOutboxcAdapter_dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a");

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes OutboxEntry as its list entries
    private class OutboxAdapter extends ArrayAdapter<DirectEmailerOutbox.OutboxEntry> {
        // member variables
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<DirectEmailerOutbox.OutboxEntry> mArrayList = null;

        // constructor
        public OutboxAdapter(Context context, int layoutResourceId, ArrayList<DirectEmailerOutbox.OutboxEntry> list) {
            super(context, layoutResourceId, list);
            this.mLayoutResourceId = layoutResourceId;
            this.mContext = context;
            this.mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // for large ListViews a ViewHolder Tag should be used
        // the widget views in the row have Tags that contain a pointer to their relevant OutboxEntry entry in the ListView list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            }

            DirectEmailerOutbox.OutboxEntry rec = mArrayList.get(position);
            TextView tv = (TextView)rowView.findViewById(R.id.rowtextView_info);
            String str = mOutboxcAdapter_dateFormat.format(new Date(rec.rTimestamp)) + " \n";
            if (rec.rToAll) { str = str + "To: (all), "; }
            else { str = str + "To: "+rec.rToAddress+", "; }
            str = str + "Subject: " + rec.rSubject;
            if (rec.rShortErrorMessage != null) { str = str + "\nError: "+rec.rShortErrorMessage; }
            if (rec.rLongErrorMessage != null) { str = str + "\nDetails: "+rec.rLongErrorMessage; }
            tv.setText(str);

            Button bt1 = (Button)rowView.findViewById(R.id.rowbutton_delete);
            bt1.setTag(rec);
            bt1.setOnClickListener(mDeleteButtonListener);

            Button bt2 = (Button)rowView.findViewById(R.id.rowbutton_resend);
            bt2.setTag(rec);
            bt2.setOnClickListener(mResendButtonListener);

            Button bt3 = (Button) rowView.findViewById(R.id.rowbutton_change_toaddress);
            if (!rec.rToAll) {
                bt3.setTag(rec);
                bt3.setOnClickListener(mChangeToAddrButtonListener);
                bt3.setVisibility(View.VISIBLE);
            } else { bt3.setVisibility(View.INVISIBLE); }

            return rowView;
        }
    }
}
