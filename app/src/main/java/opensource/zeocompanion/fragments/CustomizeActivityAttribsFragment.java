package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.myzeo.android.api.data.MyZeoExportDataContract;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.CustomizeActivity;
import opensource.zeocompanion.database.CompanionAttributeValuesRec;
import opensource.zeocompanion.database.CompanionAttributesRec;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.utility.Utilities;

// fragment class that creates the UI for managing attributes;
// this fragment is normally shown "split-screen" on only 1/2 the available display
public class CustomizeActivityAttribsFragment extends Fragment implements CustomizeActivityAttribsEditDialogFragment.OnAttribEditFragListener {
    // member variables
    private View mRootView = null;
    private CustomizeActivity mActivity = null;
    private int mMode = 0;
    private ListView mListView_Attribs = null;
    private CAF_Attribs_Adapter mListView_Attribs_Adapter = null;
    private ArrayList<CompanionAttributesRec> mListView_Attribs_List = null;
    private int mLastSelectedItem_attribs = -1;

    // member constants and other static content
    private String _CTAG = "CAF";
    private static final String ARG_PARAM1 = "mode";

    // listener for any changes in any of the Attribute Visible checkboxes in the entire ListView;
    // note that non-end-user initiated "changes" will invoke this listener with no change in state of the checkbox
    private CheckBox.OnCheckedChangeListener mCheckboxListener_Attribs = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
            // locate the applicable record; it may be null or its mDoing member null to indicate an in-process delete of the record
            CompanionAttributesRec aRec = (CompanionAttributesRec)buttonView.getTag();
            if (aRec == null) { return; }

            // the Visible checkbox was "changed"; compute the new value and only change the database if the checkbox truly changed
            int newFlags = aRec.rFlags;
            if (isChecked) { newFlags = newFlags | CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_VISIBLE;  }
            else { newFlags = newFlags & ~CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_VISIBLE; }
            if (newFlags != aRec.rFlags) {
                aRec.rFlags = newFlags;
                aRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
            }
        }
    };

    // listen for a click on any one of the TextView fields in the entire ListView;
    // invoke the EditTextDialogFragment to allow the end-user to change the text;
    // this will only occur during Mode 1 for Custom Attributes
    private TextView.OnClickListener mTextViewListener_Attribs = new CheckBox.OnClickListener() {
        @Override
        public void onClick (View view) {
            CompanionAttributesRec aRec = (CompanionAttributesRec)view.getTag();
            if (aRec == null) { return; }
            if (aRec.rAttributeDisplayName == null) { return; }

            CustomizeActivityAttribsEditDialogFragment frag = CustomizeActivityAttribsEditDialogFragment.newInstance(aRec.rAttributeDisplayName, aRec.rExportSlotName, aRec.rAppliesToStage, "Change existing Custom Attribute", "c");
            frag.setTargetFragment(CustomizeActivityAttribsFragment.this, 1);
            frag.show(getFragmentManager(),"DiagCEF");
        }
    };

    // define a callback listener for a response to the YesNoDialog which is used when the end-user signals to delete an entry;
    // if the confirmation answer was Yes, then delete the record from the database, and from the ListView
    final Utilities.ShowYesNoDialogInterface mYesNoResponseListener = new Utilities.ShowYesNoDialogInterface() {
        @Override
        public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String ignored) {
            if (callbackAction == 1 && theResult) {
                // end-user confrmed yes to remove the entry; so remove it from the datbase
                CompanionAttributesRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, callbackString1);
                CompanionAttributeValuesRec.removeAllWithAttribute(ZeoCompanionApplication.mDatabaseHandler, callbackString1);
                mLastSelectedItem_attribs = -1;
                showValuesList();
                // search for it in the ListView
                for (int p = 0; p < mListView_Attribs_List.size(); p++ ) {
                    CompanionAttributesRec aRec = mListView_Attribs_List.get(p);
                    if (aRec.rAttributeDisplayName.equals(callbackString1)) {
                        // found the entry in the ListView
                        if (p + 1 == mListView_Attribs_List.size()) { mLastSelectedItem_attribs = -1; } // if the item in the last row of the ListView is removed (since its selected) the selection state will change to "nothing selected"
                        aRec.rAttributeDisplayName = null;                 // need to do this to prevent re-adding of the record by onCheckedChanged
                        mListView_Attribs_List.remove(p);   // remove it from the ListView
                        break;
                    }
                }
                mListView_Attribs_Adapter.notifyDataSetChanged();
                mActivity.informAttributesChanged();
            }
        }
    };

    // constructor
    public CustomizeActivityAttribsFragment() {}

    // instanciator for one edit field
    public static CustomizeActivityAttribsFragment newInstance(int mode) {
        CustomizeActivityAttribsFragment fragment = new CustomizeActivityAttribsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, mode);
        fragment.setArguments(args);
        return fragment;
    }

    // create the Fragment object
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMode = getArguments().getInt(ARG_PARAM1);
            _CTAG = _CTAG + mMode;
        }
        mActivity = (CustomizeActivity)getActivity();
    }

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_customize_attribs, container, false);
        View containerView = mRootView.findViewById(R.id.values_container);
        int newContainerID = generateViewId();
        containerView.setId(newContainerID);

        // if working with the Custom Attributes, show and listen to the add/delete buttons
        if (mMode == 1) {
            mRootView.findViewById(R.id.button_attrib_add).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.button_attrib_remove).setVisibility(View.VISIBLE);

            // setup a listener for end-user presses of the Attributes delete button
            mRootView.findViewById(R.id.button_attrib_remove).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // make sure something is selected in the Values ListView, then invoke a YesNoDialog to ask end-user for confirmation
                    if (mLastSelectedItem_attribs >= 0) {
                        CompanionAttributesRec aRec = mListView_Attribs_List.get(mLastSelectedItem_attribs);
                        if (aRec.rDisplay_order < 0) { Toast.makeText(getActivity(), "No Custom additions item is selected", Toast.LENGTH_SHORT).show(); }
                        else if (aRec.rExportSlot >= 0) { Toast.makeText(getActivity(), "Selected item is not a Custom addition; this item cannot be deleted (it can be hidden)", Toast.LENGTH_SHORT).show(); }
                        else {
                            Utilities.showYesNoDialog(getContext(), "Confirm", "Are you sure you want to delete Custom additions Attribute item: " + aRec.rAttributeDisplayName, "Remove", "Cancel", mYesNoResponseListener, 1, aRec.rAttributeDisplayName, "");
                        }
                    } else {
                        Toast.makeText(getActivity(), "No Custom additions item is selected", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // setup a listener for end-user presses of the Attributes add button
            mRootView.findViewById(R.id.button_attrib_add).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CustomizeActivityAttribsEditDialogFragment frag = CustomizeActivityAttribsEditDialogFragment.newInstance("", "", CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE, "Add new Custom addition Attribute", "a");
                    frag.setTargetFragment(CustomizeActivityAttribsFragment.this, 1);
                    frag.show(getFragmentManager(),"DiagCEF");
                }
            });

        } else {
            mRootView.findViewById(R.id.button_attrib_add).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.button_attrib_remove).setVisibility(View.INVISIBLE);
        }

        // build the list for the ListView from the database depending upon mode
        mListView_Attribs_List = new ArrayList<CompanionAttributesRec>();
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAllAttributeRecsSortedInvSleepStageDisplayOrder();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                switch (mMode) {
                    case 1:
                        CompanionAttributesRec fRef1 = new CompanionAttributesRec("Predefined slots:", 0, -2, 0, -1, "");    // insert a false record to act as a section header
                        mListView_Attribs_List.add(fRef1);
                        int flagsNeeded1 = CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT + CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT;
                        int flagsNot1 = CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED + CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME;
                        do {
                            CompanionAttributesRec aRec = new CompanionAttributesRec(cursor);
                            if ((aRec.rFlags & flagsNot1) == 0) {
                                if ((aRec.rFlags & flagsNeeded1) == flagsNeeded1) {
                                    mListView_Attribs_List.add(aRec);
                                }
                            }
                        } while (cursor.moveToNext());

                        fRef1 = new CompanionAttributesRec("Custom additions:", 0, -3, 0, -1, "");    // insert a false record to act as a section header
                        mListView_Attribs_List.add(fRef1);
                        cursor.moveToFirst();
                        do {
                            CompanionAttributesRec aRec = new CompanionAttributesRec(cursor);
                            if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED) == 0) {
                                if ((aRec.rFlags & flagsNeeded1) == 0) {
                                    mListView_Attribs_List.add(aRec);
                                }
                            }
                        } while (cursor.moveToNext());

                        break;

                    default:
                        // Mode zero, show only fixed-slot non-custom attributes, organized into before and after sleep stages
                        // select attributes that are not disabled, and are fixed-slot and noRename
                        int flagsNeeded2 = CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DEFAULT + CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT + CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME;
                        int prior_SleepStage = CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER;
                        CompanionAttributesRec fRef2 = new CompanionAttributesRec("After Done Sleeping:", prior_SleepStage, -1, 0, -1, "");    // insert a false record to act as a section header
                        mListView_Attribs_List.add(fRef2);
                        do {
                            CompanionAttributesRec aRec = new CompanionAttributesRec(cursor);
                            if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED) == 0) {
                                if ((aRec.rFlags & flagsNeeded2) == flagsNeeded2) {
                                    if (prior_SleepStage != aRec.rAppliesToStage) {
                                        prior_SleepStage = aRec.rAppliesToStage;
                                        fRef2 = new CompanionAttributesRec("Before Sleeping:", prior_SleepStage, -1, 0, -1, "");    // insert a false record to act as a section header
                                        mListView_Attribs_List.add(fRef2);
                                    }
                                    mListView_Attribs_List.add(aRec);
                                }
                            }
                        } while (cursor.moveToNext());
                        break;
                }
            }
            cursor.close();
        }

        // proportionally size the left-side RelativeLayout depending upon the orientation of the device
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        //if (width > height) { width = (int)(width * 0.5); }
        //else { width = (int)(width * 0.333333); }
        width = (int)(width * 0.5);
        RelativeLayout relativeV = (RelativeLayout)mRootView.findViewById(R.id.relative_main);
        LinearLayout.LayoutParams list1 = (LinearLayout.LayoutParams)relativeV.getLayoutParams();
        list1.width = width;
        relativeV.setLayoutParams(list1);

        // setup the Attributes ListView; proportionally size it same as its parent RelativeLayout
        mListView_Attribs = (ListView)mRootView.findViewById(R.id.listView_attributes);
        RelativeLayout.LayoutParams list2 = (RelativeLayout.LayoutParams)mListView_Attribs.getLayoutParams();
        list2.width = width;
        mListView_Attribs.setLayoutParams(list2);
        mListView_Attribs_Adapter = new CAF_Attribs_Adapter(getActivity(), R.layout.fragment_customize_attribs_row, mListView_Attribs_List);
        mListView_Attribs.setAdapter(mListView_Attribs_Adapter);

        // setup a listener for end-user selection of an Attributes ListView row;
        // cannot use .getSelectedItemPosition() per Google Docs for end-user touchs of the ListView: http://android-developers.blogspot.com/2008/12/touch-mode.html
        /*  "A very common problem with new Android developers is to rely on ListView.getSelectedItemPosition().
            In touch mode, this method will return INVALID_POSITION.
            You should instead use click listeners or the choice mode." */
        mListView_Attribs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastSelectedItem_attribs = position;
                showValuesList();
            }
        });

        // now setup the companion Values Fragment after the prior resizing;
        // when instanciating duplicate base Fragments its important that the container added into be different
        // for each different companion fragment; otherwise each companion fragment will appear in the
        // first base fragment's view
        FragmentManager fm = getFragmentManager();
        CustomizeActivityValuesFragment frag = CustomizeActivityValuesFragment.newInstance(mMode);
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(newContainerID, frag, "CVF."+mMode);
        ft.commit();

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

    // the below View API method is available only in SDK 17 and higher
    /**
     * Generate a value suitable for use in setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * return a generated ID value
     */
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) { return result; }
        }
    }

    // an Attributes row has been selected in the ListView, so have the Values Fragment show the values for the attribute
    private void showValuesList() {
        CustomizeActivityValuesFragment frag = (CustomizeActivityValuesFragment)getFragmentManager().findFragmentByTag("CVF."+mMode);
        if (mLastSelectedItem_attribs < 0)  { frag.showValuesListForAttribute(mMode, null);  }
        else {
            CompanionAttributesRec aRec = mListView_Attribs_List.get(mLastSelectedItem_attribs);
            if (frag != null) {
                if (aRec.rDisplay_order < 0) { frag.showValuesListForAttribute(mMode, null);   }
                else { frag.showValuesListForAttribute(mMode, aRec.rAttributeDisplayName); }
            }
        }
    }

    // callback handler for the final text from the CustomizeActivityAttribsEditDialogFragment for adding or changing a record;
    // this is only allowed in Mode 1 with Custom fields
    public void editedResults(String theNewAttributeText, String theNewShortText, int theNewSleepStage, String action, String theOrigAttributeText, String theOrigShortText, int theOrigSleepStage) {
        // initial checks of the results for non-changed entries or blank results
        char actionChar = action.toCharArray()[0];
        if (theNewAttributeText.isEmpty()) { Toast.makeText(getActivity(), "Cannot leave the Attribute name blank", Toast.LENGTH_LONG).show(); return; }
        if (theNewShortText.isEmpty()) { Toast.makeText(getActivity(), "Cannot leave the Short export Attribute name blank", Toast.LENGTH_LONG).show(); return; }

        // ensure the new or changed Doing name does not already exist in another record
        if (actionChar == 'a' || !theNewAttributeText.equals(theOrigAttributeText)) {
            for (CompanionAttributesRec oldRec : mListView_Attribs_List) {
                if (oldRec.rAttributeDisplayName.equals(theNewAttributeText)) {
                    if (actionChar == 'c') {
                        Toast.makeText(getActivity(), "Cannot rename this entry to an existing Attribute name", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Cannot add with an existing Attribute name", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        }

        // for an Add, just construct the new record, add it to the database and the ListView
        if (actionChar == 'a') {
                                                                        // String attributeDisplayName, int appliesToStage, int displayOrder, int flags, int exportSlot, String exportSlotName
            CompanionAttributesRec newRec = new CompanionAttributesRec(theNewAttributeText, theNewSleepStage, 99, CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_VISIBLE, -1, theNewShortText);
            newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
            mListView_Attribs_List.add(newRec);
            mListView_Attribs_Adapter.notifyDataSetChanged();
            mLastSelectedItem_attribs = mListView_Attribs_List.size()-1;
            mListView_Attribs.setSelection(mLastSelectedItem_attribs);
            showValuesList();
            mActivity.informAttributesChanged();
            return;
        }

        // for a Change, first locate the original entry in the ListView list
        for (int p = 0; p < mListView_Attribs_List.size(); p++ ) {
            CompanionAttributesRec oldRec = mListView_Attribs_List.get(p);
            if (oldRec.rAttributeDisplayName.equals(theOrigAttributeText)) {
                if (theNewAttributeText.equals(theOrigAttributeText)) {
                    // the attribute name did not change
                    if (!theNewShortText.equals(theOrigShortText) || theNewSleepStage != theOrigSleepStage) {
                        oldRec.rExportSlotName = theNewShortText;
                        oldRec.rAppliesToStage = theNewSleepStage;
                        oldRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
                    }
                    return;
                } else {
                    // the attribute name did change, so have to do a delete/add process
                    // create and copy a new entry based upon the old entry
                    CompanionAttributesRec newRec = new CompanionAttributesRec(oldRec);
                    newRec.rAttributeDisplayName = theNewAttributeText;
                    newRec.rExportSlotName = theNewShortText;
                    newRec.rAppliesToStage = theNewSleepStage;
                    CompanionAttributesRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, oldRec.rAttributeDisplayName);  // remove the old entry from the database
                    newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);  // add the new entry to the database
                    CompanionAttributeValuesRec.renameAllWithAttribute(ZeoCompanionApplication.mDatabaseHandler, oldRec.rAttributeDisplayName, newRec.rAttributeDisplayName);
                    oldRec.rAttributeDisplayName = newRec.rAttributeDisplayName;      // rename the existing record in the ListView list
                    mListView_Attribs_Adapter.notifyDataSetChanged();
                    mActivity.informAttributesChanged();
                    return;
                }
            }
        }
    }

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes CompanionAttributesRec as its list entries
    class CAF_Attribs_Adapter extends ArrayAdapter {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<CompanionAttributesRec> mArrayList = null;

        // constructor
        public CAF_Attribs_Adapter(Context context, int layoutResourceId, ArrayList<CompanionAttributesRec> list) {
            super(context, layoutResourceId, list);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mArrayList = list;
        }

        // we'll be providing section views and data views
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        // indicate to the framework what view to use for each position
        @Override
        public int getItemViewType(int position) {
            CompanionAttributesRec aRec = mListView_Attribs_List.get(position);
            if (aRec.rDisplay_order < 0) { return 1; }
            return 0;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // for large ListViews a ViewHolder Tag should be used
        // the widget views in the row have Tags that contain a pointer to their relevant CompanionAttributesRec entry in the ListView list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template if the view is brand new (not recycled);
            // however must also determine if this is a section header row or a data row
            View rowView = convertView;
            CompanionAttributesRec aRec = mListView_Attribs_List.get(position);
            if (aRec.rDisplay_order < 0) {
                // section header row
                if (rowView == null) {
                    LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                    rowView = inflater.inflate(R.layout.listview_header_row, parent, false);
                }
                TextView tv = (TextView)rowView.findViewById(R.id.rowtextView_header);
                tv.setText(aRec.rAttributeDisplayName);
                return rowView;
            }

            // data row
            if (rowView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            } else {
                // this is necessary to prevent false triggers of the checkbox
                CheckBox cb1 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_visible);
                cb1.setOnCheckedChangeListener(null);
            }

            // now properly configure the row's data and attributes
            // setup the TextView with the current text of the Attribute record;
            // for slotted non-custom attributes, include slot#; for slotted custom attributes, include the customSlot#
            // if in Mode 1 for Custom attributes, also activate a click listener for the textView
            TextView tv1 = (TextView)rowView.findViewById(R.id.rowtextView_attribute);
            if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FIXED_SLOT) != 0) {
                if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_NORENAME) != 0) { tv1.setText((aRec.rExportSlot + 1) + ": " + aRec.rAttributeDisplayName); }
                else { tv1.setText((aRec.rExportSlot + 1 - MyZeoExportDataContract.EXPORT_FIELD_SLOTS_FIRST_CUSTOM) + ": " + aRec.rAttributeDisplayName); }
            } else { tv1.setText(aRec.rAttributeDisplayName); }
            if (mMode == 1) {
                tv1.setBackgroundColor(getResources().getColor(R.color.colorDataEntry));
                tv1.setTextColor(getResources().getColor(R.color.colorDataEntryInsideText));
                tv1.setTag(aRec);
                tv1.setClickable(true);
                tv1.setOnClickListener(mTextViewListener_Attribs);
            }

            TextView tv2 = (TextView)rowView.findViewById(R.id.rowtextView_attributeShort);
            String str = CompanionDatabaseContract.getSleepStageString(aRec.rAppliesToStage)+": '"+aRec.rExportSlotName+"'";
            tv2.setText(str);

            // setup the Checkbox with shown/not shown state and point to a common Listener that handles changes to the checkbox
            CheckBox cb1 = (CheckBox)rowView.findViewById(R.id.rowcheckBox_visible);
            if ((aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_VISIBLE) != 0) { cb1.setChecked(true); }
            else { cb1.setChecked(false); }
            cb1.setTag(aRec);
            cb1.setOnCheckedChangeListener(mCheckboxListener_Attribs);

            return rowView;
        }
    }
}
