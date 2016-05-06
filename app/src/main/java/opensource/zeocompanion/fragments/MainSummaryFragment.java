package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.Utilities;

// fragment within the MainActivity that shows an list of Sleep Journal entries made tonight
public class MainSummaryFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private ListView mListView = null;
    private MainSummaryAdapter mListView_Adapter = null;
    private ArrayList<String> mListView_StringArray = null;

    // member constants and other static content
    private static final String _CTAG = "MSF";

    // constructor
    public MainSummaryFragment() {}

    // instanciator
    public static MainSummaryFragment newInstance() {
        MainSummaryFragment fragment = new MainSummaryFragment();
        return fragment;
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG+".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_main_summary, container, false);
        MainActivity activity = (MainActivity)getActivity();

        mListView_StringArray = new ArrayList<String>();
        ZeoCompanionApplication.mCoordinator.createDaypointSummaryList(mListView_StringArray);

        mListView = (ListView)mRootView.findViewById(R.id.listView_summary);
        mListView_Adapter = new MainSummaryAdapter(activity, R.layout.listview_rightleftrow, mListView_StringArray);
        mListView.setAdapter(mListView_Adapter);

        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        // though disputed, assist garbage collection by clearing out the ListView contents and views
        mListView_StringArray.clear();
        mListView_Adapter.notifyDataSetChanged();

        super.onDestroyView();
    }

    // called by the Activity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_SUMMARY_FRAGMENT) == 0){
            ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_SUMMARY_FRAGMENT);
            Utilities.showAlertDialog(getContext(), "Hint", "Hint: This tab will show a summary of all events and attributes that you have recorded for this sleep session.", "Okay");
        }
        needToRefresh();
    }

    // called by the Activity when handlers or settings have made changes to the database
    // or to settings options, etc
    @Override
    public void needToRefresh() {
        mListView_StringArray.clear();
        ZeoCompanionApplication.mCoordinator.createDaypointSummaryList(mListView_StringArray);
        mListView_Adapter.notifyDataSetChanged();
    }

    // called by the Activity at the behest of the Journal Data Coordinator
    @Override
    public void daypointHasChanged() {
        needToRefresh();
    }

    // ListView adaptor specific to this fragment
    private class MainSummaryAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<String> mArrayList = null;

        // constructor
        public MainSummaryAdapter(Context context, int layoutResourceId, ArrayList<String> list) {
            super(context, layoutResourceId, list);
            this.mLayoutResourceId = layoutResourceId;
            this.mContext = context;
            this.mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            }

            // now properly configure the row's data and attributes
            String theMsgString = mArrayList.get(position);
            TextView tr = (TextView) rowView.findViewById(R.id.rowTextViewRight);
            TextView tl = (TextView) rowView.findViewById(R.id.rowTextViewLeft);
            if (theMsgString.substring(0, 1).equals("\t")) {
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
}
