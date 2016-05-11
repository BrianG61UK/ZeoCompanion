package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAttributesRec;
import opensource.zeocompanion.database.CompanionDatabaseContract;

// fragment class that creates the UI for re-ordering the Attributes
public class CustomizeActivityAttribsOrderFragment extends Fragment {
    // member variables
    private View mRootView = null;
    private ListView mListView_Attribs = null;
    private COF_Adapter mListView_Attribs_Adapter = null;
    private ArrayList<CompanionAttributesRec> mListView_Attribs_List = null;
    private int mLastSelectedItem_attribs = -1;

    // member constants and other static content
    private String _CTAG = "COF";

    // constructor
    public CustomizeActivityAttribsOrderFragment() {}

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_customize_attribs_order, container, false);

        // setup a listener for end-user presses of the Attributes up button;
        // make sure something is selected in the ListView, then invoke a move item up/down handler
        mRootView.findViewById(R.id.button_attrib_up).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveAttributeUpDown(true);
            }
        });

        // setup a listener for end-user presses of the Attributes down button;
        // make sure something is selected in the ListView, then invoke a move item up/down handler
        mRootView.findViewById(R.id.button_attrib_down).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                moveAttributeUpDown(false);
            }
        });

        // build the list for the ListView from the database depending upon mode
        mListView_Attribs_List = new ArrayList<CompanionAttributesRec>();
        buildList();

        // setup the Attributes ListView; proportionally size it same as its parent RelativeLayout
        mListView_Attribs = (ListView)mRootView.findViewById(R.id.listView_attributes);
        mListView_Attribs_Adapter = new COF_Adapter(getActivity(), R.layout.fragment_customize_attribs_order_row, mListView_Attribs_List);
        mListView_Attribs.setAdapter(mListView_Attribs_Adapter);

        // setup a listener for end-user selection of an Attributes ListView row;
        // cannot use .getSelectedItemPosition() per Google Docs for end-user touchs of the ListView: http://android-developers.blogspot.com/2008/12/touch-mode.html
        /*  "A very common problem with new Android developers is to rely on ListView.getSelectedItemPosition().
            In touch mode, this method will return INVALID_POSITION.
            You should instead use click listeners or the choice mode." */
        mListView_Attribs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastSelectedItem_attribs = position;
                setSelected(position);
            }
        });

        return mRootView;
    }

    // Called when the fragment's view has been detached from the fragment
    @Override
    public void onDestroyView() {
        mListView_Attribs_Adapter.clear();
        mListView_Attribs_List.clear();
        mListView_Attribs_List = null;
        mListView_Attribs.setAdapter(null);
        mListView_Attribs_Adapter = null;
        mListView_Attribs = null;

        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        super.onDestroyView();
    }

    // refresh the list of attributes
    public void refreshList() {
        if (mListView_Attribs_Adapter == null) { return; }
        mLastSelectedItem_attribs = -1;
        setSelected(-1);
        buildList();
        mListView_Attribs_Adapter.notifyDataSetChanged();
    }

    // refresh the list of attributes
    private void buildList() {
        if (mListView_Attribs_List == null) { return; }
        mListView_Attribs_List.clear();
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAllAttributeRecsSortedInvSleepStageDisplayOrder();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanionAttributesRec aRec = new CompanionAttributesRec(cursor);
                    if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED) == 0) {
                        if (aRec.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) {
                            mListView_Attribs_List.add(aRec);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private void moveAttributeUpDown(boolean moveUp) {
        // get the selected record
        if (mLastSelectedItem_attribs < 0) { Toast.makeText(getActivity(), "No Attribute item is selected", Toast.LENGTH_SHORT).show(); return; }
        CompanionAttributesRec aRec1 = mListView_Attribs_List.get(mLastSelectedItem_attribs);
        if (aRec1.rDisplay_order < 0) { Toast.makeText(getActivity(), "No Attribute item is selected", Toast.LENGTH_SHORT).show(); return; }

        // determine our range within the list for the selected record's sleepStage
        int posSectionStart = -1;
        int posSectionEnd = -1;
        for (int pos = 0; pos < mListView_Attribs_List.size(); pos++) {
            CompanionAttributesRec existing = mListView_Attribs_List.get(pos);
            if (existing.rDisplay_order < 0) {
                if (aRec1.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) {
                    if (existing.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) { posSectionStart = pos; }
                    else if (existing.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) { posSectionEnd = pos; break; }
                } else {
                    if (existing.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) { posSectionStart = pos; break; }
                }
            }
        }
        if (aRec1.rAppliesToStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) { posSectionEnd = mListView_Attribs_List.size(); }

        // has the selected item reached the limits of the range?
        int delta = 1;
        if (moveUp) {
            delta = -1;
            if (mLastSelectedItem_attribs <= posSectionStart + 1) { return; }   // yes, cannot move up any further
        } else {
            if (mLastSelectedItem_attribs >= posSectionEnd - 1) { return; }   // yes, cannot move down any further
        }

        // everything is okay, change the display orders and save the results to the database
        CompanionAttributesRec aRec2 = mListView_Attribs_List.get(mLastSelectedItem_attribs + delta);
        int dispOrder = aRec1.rDisplay_order;
        aRec1.rDisplay_order = aRec2.rDisplay_order;
        aRec2.rDisplay_order = dispOrder;
        aRec1.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
        aRec2.saveToDB(ZeoCompanionApplication.mDatabaseHandler);

        // swap their positions in the List
        mListView_Attribs_List.set(mLastSelectedItem_attribs, aRec2);
        mListView_Attribs_List.set(mLastSelectedItem_attribs + delta, aRec1);
        mListView_Attribs_Adapter.notifyDataSetChanged();
        mLastSelectedItem_attribs = mLastSelectedItem_attribs + delta;
        setSelected(mLastSelectedItem_attribs);
    }

    // because the children hired by Google are too stupid to properly create UI controls
    // perform our own selection indicator that ignores the pathetic "touch mode"
    // position <0 will clear all selections
    public void setSelected(int position) {
        int firstVisible = mListView_Attribs.getFirstVisiblePosition();
        int lastVisible = mListView_Attribs.getLastVisiblePosition();
        int c = mListView_Attribs.getCount();
        if (c == 0) { return; }
        int seek = -1;
        if (position >= 0) {
            if (position >= c) { position = c - 1; }
            if (position <= firstVisible || position >= lastVisible) {
                mListView_Attribs.smoothScrollToPosition(position);
            }
            seek = position - firstVisible;
        }

        for (int i = 0; i < mListView_Attribs.getChildCount(); i++) {
            View v = mListView_Attribs.getChildAt(i);
            if (i == seek) { v.setBackgroundColor(getResources().getColor(R.color.colorListViewRowBackgroundSelected)); }
            else { v.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2)); }
        }
    }

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes CompanionAttributesRec as its list entries
    class COF_Adapter extends ArrayAdapter {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<CompanionAttributesRec> mArrayList = null;

        // constructor
        public COF_Adapter(Context context, int layoutResourceId, ArrayList<CompanionAttributesRec> list) {
            super(context, layoutResourceId, list);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // for large ListViews a ViewHolder Tag should be used
        // the widget views in the row have Tags that contain a pointer to their relevant CompanionAttributesRec entry in the ListView list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template if the view is brand new (not recycled);
            // however must also determine if this is a section header row or a data row
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            }
            CompanionAttributesRec aRec = mListView_Attribs_List.get(position);

            if (position == mLastSelectedItem_attribs) { rowView.setBackgroundColor(getResources().getColor(R.color.colorListViewRowBackgroundSelected)); }
            else { rowView.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2)); }

            TextView tv1 = (TextView)rowView.findViewById(R.id.rowtextView_attribute);
            tv1.setText(aRec.rAttributeDisplayName);
            return rowView;
        }
    }
}
