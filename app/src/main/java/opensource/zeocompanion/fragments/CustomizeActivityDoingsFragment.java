package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionEventDoingsRec;
import opensource.zeocompanion.utility.Utilities;

// fragment class that creates the UI for managing event doings
public class CustomizeActivityDoingsFragment extends Fragment implements EditTextDialogFragment.OnExitTextFragListener {
    // member variables
    private View mRootView = null;
    private ListView mListView = null;
    private CDF_Adapter mListView_Adapter = null;
    private ArrayList<CompanionEventDoingsRec> mListView_List = null;
    private int mLastSelectedItem = -1;

    // member constants and other static content
    private static final String _CTAG = "CDF";

    // listener for any changes in any of the checkboxes in the entire ListView;
    // note that non-end-user initiated "changes" will invoke this listener with no change in state of the checkbox
    private CheckBox.OnCheckedChangeListener mCheckboxListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
            // locate the applicable record; it may be null or its mDoing member null to indicate an in-process delete of the record
            CompanionEventDoingsRec dRec = (CompanionEventDoingsRec)buttonView.getTag();
            if (dRec == null) { return; }
            if (dRec.rDoing == null) { return; }
            String field = buttonView.getText().toString();

            int newAppliesToStages = dRec.rAppliesToStages;
            if (field.equals("In-bed")) {
                // the In-bed checkbox was "changed"; compute the new value and only change the database if the checkbox truly changed
                if (isChecked) { newAppliesToStages = newAppliesToStages | CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED;  }
                else { newAppliesToStages = newAppliesToStages & ~CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED; }
                if (newAppliesToStages != dRec.rAppliesToStages) {
                    dRec.rAppliesToStages = newAppliesToStages;
                    dRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
                }
            }
            else if (field.equals("During")) {
                // the During checkbox was "changed"; compute the new value and only change the database if the checkbox truly changed
                if (isChecked) { newAppliesToStages = newAppliesToStages | CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING;  }
                else { newAppliesToStages = newAppliesToStages & ~CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING; }
                if (newAppliesToStages != dRec.rAppliesToStages) {
                    dRec.rAppliesToStages = newAppliesToStages;
                    dRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
                }
            }
            else if (field.equals("Default")) {
                // the Default checkbox was "changed"; compute the new value and only change the database if the checkbox truly changed
                int defaultPriority = dRec.rIsDefaultPriority;
                if (isChecked) { defaultPriority = 1; }
                else { defaultPriority = 0; }
                if (defaultPriority != dRec.rIsDefaultPriority) {
                    dRec.rIsDefaultPriority = defaultPriority;
                    dRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
                }
            }
        }
    };

    // listen for a click on any one of the TextView fields in the entire ListView;
    // invoke the EditTextDialogFragment to allow the end-user to change the text
    private TextView.OnClickListener mTextViewListener = new CheckBox.OnClickListener() {
        @Override
        public void onClick (View view) {
            CompanionEventDoingsRec dRec = (CompanionEventDoingsRec)view.getTag();

            EditTextDialogFragment editFrag = EditTextDialogFragment.newInstance1(dRec.rDoing, "Change existing Value", "c", "", dRec.rDoing);
            editFrag.setTargetFragment(CustomizeActivityDoingsFragment.this, 1);
            editFrag.show(getFragmentManager(), "DiagETF");
        }
    };

    // constructor
    public CustomizeActivityDoingsFragment() {
    }

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_customize_doings, container, false);

        // build the list for the ListView from the database
        mListView_List = new ArrayList<CompanionEventDoingsRec>();
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAllEventDoingsRecsSortedDoing();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanionEventDoingsRec dRec = new CompanionEventDoingsRec(cursor);
                    mListView_List.add(dRec);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // setup the ListView
        mListView = (ListView)mRootView.findViewById(R.id.listView_doings);
        mListView_Adapter = new CDF_Adapter(getActivity(), R.layout.fragment_customize_doings_row, mListView_List);
        mListView.setAdapter(mListView_Adapter);

        // define a callback listener for a response to the YesNoDialog which is used when the end-user signals to delete an entry;
        // if the confirmation answer was Yes, then delete the record from the database, and from the ListView
        final Utilities.ShowYesNoDialogInterface yesNoResponseListener = new Utilities.ShowYesNoDialogInterface() {
            @Override
            public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
                if (callbackAction == 1 && theResult) {
                    // end-user confrmed yes to remove the entry; so remove it from the datbase
                    CompanionEventDoingsRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, callbackString1);
                    // search for it in the ListView
                    for (int p = 0; p < mListView_List.size(); p++ ) {
                        CompanionEventDoingsRec dRec = mListView_List.get(p);
                        if (dRec.rDoing.equals(callbackString1)) {
                            // found the entry in the ListView
                            if (p + 1 == mListView_List.size()) { mLastSelectedItem = -1; } // if the item in the last row of the ListView is removed (since its selected) the selection state will change to "nothing selected"
                            dRec.rDoing = null;             // need to do this to prevent re-adding of the record by onCheckedChanged
                            mListView_List.remove(p);   // remove it from the ListView
                            break;
                        }
                    }
                    mListView_Adapter.notifyDataSetChanged();
                }
            }
        };

        // setup a listener for end-user presses of the delete button;
        // make sure something is selected in the ListView, then invoke a YesNoDialog to ask end-user for confirmation
        mRootView.findViewById(R.id.button_remove).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mLastSelectedItem >= 0) {
                    CompanionEventDoingsRec dRec = mListView_List.get(mLastSelectedItem);
                    Utilities.showYesNoDialog(getContext(), "Confirm", "Are you sure you want to delete Doing item: " + dRec.rDoing, "Remove", "Cancel", yesNoResponseListener, 1, dRec.rDoing, "");
                } else {
                    Toast.makeText(getActivity(), "No Doing item is selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // setup a listener for end-user presses of the add button;
        // if pressed, invoke the EditTextDialogFragment to allow the end-user to enter the new Doing name
        mRootView.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditTextDialogFragment editFrag = EditTextDialogFragment.newInstance1("", "Add new Doing", "a", "", "");
                editFrag.setTargetFragment(CustomizeActivityDoingsFragment.this, 1);
                editFrag.show(getFragmentManager(), "DiagETF");
            }
        });

        // setup a listener for end-user selection of a ListView row (likely in preparation for a delete button press);
        // cannot use .getSelectedItemPosition() per Google Docs for end-user touchs of the ListView: http://android-developers.blogspot.com/2008/12/touch-mode.html
        /*  "A very common problem with new Android developers is to rely on ListView.getSelectedItemPosition().
            In touch mode, this method will return INVALID_POSITION.
            You should instead use click listeners or the choice mode." */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastSelectedItem = position;
            }
        });

        return mRootView;
    }

    // Called when the fragment's view has been detached from the fragment
    @Override
    public void onDestroyView () {
        mListView_Adapter.clear();
        mListView_List.clear();
        mListView_List = null;
        mListView.setAdapter(null);
        mListView_Adapter = null;
        mListView = null;
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        super.onDestroyView();
    }

    // callback handler for the final text from the EditTextDialogFragment for adding or changing a record
    public void editedText(int qtyOfFields, String newText, String ignored1, String action, String ignored0, String origDoingText, String ignored2) {
        // initial checks of the results for non-changed entries or blank results
        char actionChar = action.toCharArray()[0];
        if (newText.isEmpty()) { Toast.makeText(getActivity(), "Cannot leave the Doing name blank", Toast.LENGTH_LONG).show(); return; }
        if (actionChar == 'c' && newText.equals(origDoingText)) { return; }

        // ensure the new or changed Doing name does not already exist in another record
        for (CompanionEventDoingsRec oldRec: mListView_List) {
            if (oldRec.rDoing.equals(newText)) {
                if (actionChar == 'c') { Toast.makeText(getActivity(), "Cannot rename this entry to an existing Doing name", Toast.LENGTH_LONG).show(); }
                else { Toast.makeText(getActivity(), "Cannot add with an existing Doing name", Toast.LENGTH_LONG).show(); }
                return;
            }
        }

        // for an Add, just construct the new record, add it to the database and the ListView
        if (actionChar == 'a') {
            CompanionEventDoingsRec newRec = new CompanionEventDoingsRec(newText, (CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED | CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING), 0);
            newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
            mListView_List.add(newRec);
            mListView_Adapter.notifyDataSetChanged();
            return;
        }

        // for a Change (essentially a rename), first locate the original entry in the ListView list
        for (int p = 0; p < mListView_List.size(); p++ ) {
            CompanionEventDoingsRec oldRec = mListView_List.get(p);
            if (oldRec.rDoing.equals(origDoingText)) {
                // found the old entry; create and copy a new entry based upon the old entry
                CompanionEventDoingsRec newRec = new CompanionEventDoingsRec(oldRec);
                newRec.rDoing = newText;
                CompanionEventDoingsRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, oldRec.rDoing);  // remove the old entry from the database
                newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);  // add the new entry to the database
                oldRec.rDoing = newRec.rDoing;      // rename the existing record in the ListView list
                mListView_Adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes CompanionEventDoingsRec as its list entries
    class CDF_Adapter extends ArrayAdapter {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<CompanionEventDoingsRec> mArrayList = null;

        // constructor
        public CDF_Adapter(Context context, int layoutResourceId, ArrayList<CompanionEventDoingsRec> list) {
            super(context, layoutResourceId, list);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // for large ListViews a ViewHolder Tag should be used
        // the widget views in the row have Tags that contain a pointer to their relevant CompanionEventDoingsRec entry in the ListView list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template if the view is brand new (not recycled)
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            } else {
                // this is necessary to prevent false triggers of the checkbox
                CheckBox cb1 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_before);
                CheckBox cb2 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_during);
                CheckBox cb3 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_default);
                cb1.setOnCheckedChangeListener(null);
                cb2.setOnCheckedChangeListener(null);
                cb3.setOnCheckedChangeListener(null);
            }

            // now properly configure the row's data and attributes
            CompanionEventDoingsRec dRec = mListView_List.get(position);

            // setup the TextView with the current text of the Doing record;
            // set every textview to use the common Listener defined in the Fragment mainline
            TextView tv = (TextView)rowView.findViewById(R.id.rowtextView_doing);
            tv.setText(dRec.rDoing);
            tv.setTag(dRec);
            tv.setOnClickListener(mTextViewListener);

            // proper set each checkbox's full state conditions;
            // set every checkbox to use the common Listener defined in the Fragment mainline
            CheckBox cb1 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_before);
            CheckBox cb2 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_during);
            CheckBox cb3 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_default);
            if ((dRec.rAppliesToStages & CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED) != 0) { cb1.setChecked(true); }
            else { cb1.setChecked(false); }
            cb1.setTag(dRec);
            cb1.setOnCheckedChangeListener(mCheckboxListener);
            if ((dRec.rAppliesToStages & CompanionDatabaseContract.SLEEP_EPISODE_STAGE_DURING) != 0) { cb2.setChecked(true); }
            else { cb2.setChecked(false); }
            cb2.setTag(dRec);
            cb2.setOnCheckedChangeListener(mCheckboxListener);
            if (dRec.rIsDefaultPriority == 0) { cb3.setChecked(false); }
            else { cb3.setChecked(true); }
            cb3.setTag(dRec);
            cb3.setOnCheckedChangeListener(mCheckboxListener);

            return rowView;
        }
    }
}
