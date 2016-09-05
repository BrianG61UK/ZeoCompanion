package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAlertRec;

// Fragment for showing and managing the alerts
public class AlertsActivityFragment extends Fragment {
    // member variables
    View mRootView = null;
    private ListView mListView = null;
    private AlertsAdapter mListView_Adapter = null;
    private ArrayList<CompanionAlertRec> mListView_Array = null;

    // member constants and other static content
    private static final String _CTAG = "AAF";

    // listener to the delete button click; just invoke a yes/no confirmation dialog
    private Button.OnClickListener mDeleteButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View button) {
            CompanionAlertRec aRec = (CompanionAlertRec)button.getTag();
            ZeoCompanionApplication.deleteAlertLine(aRec.rID);
            refreshList();
        }
    };

    // constructor
    public AlertsActivityFragment() { }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_alerts, container, false);

        mListView_Array = new ArrayList<CompanionAlertRec>();
        ZeoCompanionApplication.getAllAlerts(mListView_Array);

        mListView = (ListView)mRootView.findViewById(R.id.listView_alerts);
        mListView_Adapter = new AlertsAdapter(getActivity(), R.layout.fragment_alerts_row, mListView_Array);
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

    private void refreshList() {
        mListView_Array.clear();
        ZeoCompanionApplication.getAllAlerts(mListView_Array);
        mListView_Adapter.notifyDataSetChanged();
    }

    // common simple date format used in all the rows
    private static SimpleDateFormat AlertsAdapter_dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a");

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes OutboxEntry as its list entries
    private class AlertsAdapter extends ArrayAdapter<CompanionAlertRec> {
        // member variables
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<CompanionAlertRec> mArrayList = null;

        // constructor
        public AlertsAdapter(Context context, int layoutResourceId, ArrayList<CompanionAlertRec> list) {
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

            CompanionAlertRec aRec = mArrayList.get(position);
            TextView tv = (TextView)rowView.findViewById(R.id.rowtextView_info);
            tv.setText("#"+aRec.rID+" "+AlertsAdapter_dateFormat.format(new Date(aRec.rTimestamp))+": "+aRec.rMessage);

            Button bt1 = (Button)rowView.findViewById(R.id.rowbutton_delete);
            bt1.setTag(aRec);
            bt1.setOnClickListener(mDeleteButtonListener);

            return rowView;
        }
    }
}
