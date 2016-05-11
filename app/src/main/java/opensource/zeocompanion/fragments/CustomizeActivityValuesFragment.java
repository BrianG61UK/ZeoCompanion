package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAttributeValuesRec;
import opensource.zeocompanion.utility.Utilities;

// fragment class that creates the UI for managing values for an attribute;
// this fragment is normally shown "split-screen" on only 1/2 the available display
public class CustomizeActivityValuesFragment extends Fragment implements EditTextDialogFragment.OnExitTextFragListener {
    // member variables
    private View mRootView = null;
    private int mMode = 0;
    private ListView mListView_Values = null;
    private CVF_Values_Adapter mListView_Values_Adapter = null;
    private ArrayList<CompanionAttributeValuesRec> mListView_Values_List = null;
    private String mCurrentAttributeDisplayName = null;
    private int mLastSelectedItem_values = -1;
    private String _CTAG = "CVF";

    // member constants and other static content
    private static final String ARG_PARAM1 = "mode";

    // listen for a click on any one of the TextView fields in the entire ListView;
    // invoke the EditTextDialogFragment to allow the end-user to change the text
    private TextView.OnClickListener mTextViewListener_Values = new CheckBox.OnClickListener() {
        @Override
        public void onClick (View view) {
            CompanionAttributeValuesRec vRec = (CompanionAttributeValuesRec)view.getTag();
            if (vRec == null) { return; }
            if (vRec.rValue == null) { return; }

            EditTextDialogFragment editFrag = EditTextDialogFragment.newInstance2(vRec.rValue, String.valueOf(vRec.rLikert), "Change existing Value", "c", mCurrentAttributeDisplayName, vRec.rValue, String.valueOf(vRec.rLikert));
            editFrag.setTargetFragment(CustomizeActivityValuesFragment.this, 1);
            editFrag.show(getFragmentManager(), "DiagETF");
        }
    };

    // define a callback listener for a response to the YesNoDialog which is used when the end-user signals to delete an entry;
    // if the confirmation answer was Yes, then delete the record from the database, and from the ListView
    final Utilities.ShowYesNoDialogInterface mYesNoResponseListener = new Utilities.ShowYesNoDialogInterface() {
        @Override
        public void onYesNoDialogDone(boolean theResult, int callbackAction, String callbackString1, String callbackString2) {
            if (callbackAction == 1 && theResult) {
                // end-user confrmed yes to remove the entry; so remove it from the datbase
                CompanionAttributeValuesRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, callbackString1, callbackString2);
                // search for it in the ListView
                for (int p = 0; p < mListView_Values_List.size(); p++ ) {
                    CompanionAttributeValuesRec vRec = mListView_Values_List.get(p);
                    if (vRec.rAttributeDisplayName.equals(callbackString1) && vRec.rValue.equals(callbackString2)) {
                        // found the entry in the ListView
                        if (p + 1 == mListView_Values_List.size()) { mLastSelectedItem_values = -1; } // if the item in the last row of the ListView is removed (since its selected) the selection state will change to "nothing selected"
                        vRec.rValue = null;                 // need to do this to prevent re-adding of the record by onCheckedChanged
                        mListView_Values_List.remove(p);   // remove it from the ListView
                        break;
                    }
                }
                mListView_Values_Adapter.notifyDataSetChanged();
            }
        }
    };

    // constructor
    public CustomizeActivityValuesFragment() {}

    // instanciator for one edit field
    public static CustomizeActivityValuesFragment newInstance(int mode) {
        CustomizeActivityValuesFragment fragment = new CustomizeActivityValuesFragment();
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
    }

    // create the Fragment's view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_customize_values, container, false);

        // setup a listener for end-user presses of the Values delete button
        mRootView.findViewById(R.id.button_value_remove).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // make sure something is selected in the Values ListView, then invoke a YesNoDialog to ask end-user for confirmation
                if (mLastSelectedItem_values >= 0) {
                    CompanionAttributeValuesRec vRec = mListView_Values_List.get(mLastSelectedItem_values);
                    Utilities.showYesNoDialog(getContext(), "Confirm", "Are you sure you want to delete Value item: " + vRec.rValue, "Remove", "Cancel", mYesNoResponseListener, 1, vRec.rAttributeDisplayName, vRec.rValue);
                } else {
                    Toast.makeText(getActivity(), "No Value item is selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // setup a listener for end-user presses of the Values add button
        mRootView.findViewById(R.id.button_value_add).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mCurrentAttributeDisplayName == null) {
                    Toast.makeText(getActivity(), "No Attribute item is selected", Toast.LENGTH_SHORT).show();
                } else {
                    EditTextDialogFragment editFrag = EditTextDialogFragment.newInstance2("", "", "Add Value & Likert for '" + mCurrentAttributeDisplayName + "'", "a", mCurrentAttributeDisplayName, "", "");
                    editFrag.setTargetFragment(CustomizeActivityValuesFragment.this, 1);
                    editFrag.show(getFragmentManager(), "DiagETF");
                }
            }
        });

        // setup the Values Listview as initially empty
        mListView_Values_List = new ArrayList<CompanionAttributeValuesRec>();
        mListView_Values = (ListView)mRootView.findViewById(R.id.listView_values);
        mListView_Values_Adapter = new CVF_Values_Adapter(getActivity(), R.layout.fragment_customize_values_row, mListView_Values_List);
        mListView_Values.setAdapter(mListView_Values_Adapter);

        // setup a listener for end-user selection of a Values ListView row;
        mListView_Values.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLastSelectedItem_values = position;
            }
        });

        return mRootView;
    }

    // Called when the fragment's view has been detached from the fragment
    @Override
    public void onDestroyView () {
        mListView_Values_Adapter.clear();
        mListView_Values_List.clear();
        mListView_Values_List = null;
        mListView_Values.setAdapter(null);
        mListView_Values_Adapter = null;
        mListView_Values = null;
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        super.onDestroyView();
    }

    // invoked by the CustomizeActivityAttribsFragment; passed parameter can be null to signal display nothing
    public void showValuesListForAttribute(int callersMode, String attributeDisplayName) {
        if (callersMode != mMode) {
            Log.e(_CTAG + ".showValuesForAttrib", "INTERNAL ERROR: Callers mode=" + callersMode + "and our mode=" + mMode);
            return;
        }
        mCurrentAttributeDisplayName = attributeDisplayName;
        TextView tv = (TextView) mRootView.findViewById(R.id.textView_attribute);
        mListView_Values_List.clear();
        if (mCurrentAttributeDisplayName == null) {
            tv.setText("");
            mListView_Values_Adapter.notifyDataSetChanged();
            return;
        }
        tv.setText(mCurrentAttributeDisplayName);

        // build the Values list for the selected attribute
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAttributeValuesRecsForAttributeSortedLikert(mCurrentAttributeDisplayName);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanionAttributeValuesRec vRec = new CompanionAttributeValuesRec(cursor);
                    mListView_Values_List.add(vRec);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        mListView_Values_Adapter.notifyDataSetChanged();
    }

    // callback handler for the final text from the EditTextDialogFragment for adding or changing a Value record
    public void editedText(int qtyOfFields, String newTextValue, String newTextLikert, String action, String forAttribute, String origValueText, String origLikertText) {
        // initial checks of the results for non-changed entries or blank results
        char actionChar = action.toCharArray()[0];
        if (newTextValue.isEmpty()) { Toast.makeText(getActivity(), "Cannot leave the Value name blank", Toast.LENGTH_LONG).show(); return; }
        if (newTextLikert.isEmpty()) { Toast.makeText(getActivity(), "Cannot leave the Likert blank", Toast.LENGTH_LONG).show(); return; }

        // ensure the new or changed Value name does not already exist in another record
        if (actionChar == 'a' || !newTextValue.equals(origValueText)) {
            for (CompanionAttributeValuesRec oldRec : mListView_Values_List) {
                if (oldRec.rValue.equals(newTextValue)) {
                    if (actionChar == 'c') {
                        Toast.makeText(getActivity(), "Cannot rename this entry to an existing Value name", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Cannot add with an existing Value name", Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            }
        }

        // for an Add, just construct the new record, add it to the database and the ListView
        if (actionChar == 'a') {
            CompanionAttributeValuesRec newRec = new CompanionAttributeValuesRec(forAttribute, newTextValue, Float.parseFloat(newTextLikert));
            newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
            mListView_Values_List.add(newRec);
            mListView_Values_Adapter.notifyDataSetChanged();
            return;
        }

        // for a Change, first locate the original entry in the ListView list
        for (int p = 0; p < mListView_Values_List.size(); p++ ) {
            CompanionAttributeValuesRec oldRec = mListView_Values_List.get(p);
            if (oldRec.rValue.equals(origValueText)) {
                // found the old entry
                if (newTextValue.equals(origValueText) && !newTextLikert.equals(origLikertText)) {
                    // only the Likert is changed, so can just update the record
                    oldRec.rLikert = Float.parseFloat(newTextLikert);
                    oldRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);
                } else {
                    // the Value name is changed, so have to do a delete/add process
                    // create and copy a new entry based upon the old entry
                    CompanionAttributeValuesRec newRec = new CompanionAttributeValuesRec(oldRec);
                    newRec.rValue = newTextValue;
                    CompanionAttributeValuesRec.removeFromDB(ZeoCompanionApplication.mDatabaseHandler, oldRec.rAttributeDisplayName, oldRec.rValue);  // remove the old entry from the database
                    newRec.saveToDB(ZeoCompanionApplication.mDatabaseHandler);  // add the new entry to the database
                    oldRec.rValue = newRec.rValue;      // rename the existing record in the ListView list
                }
                mListView_Values_Adapter.notifyDataSetChanged();
                return;
            }
        }
    }

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes CompanionAttributeValuesRec as its list entries
    class CVF_Values_Adapter extends ArrayAdapter {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<CompanionAttributeValuesRec> mArrayList = null;

        // constructor
        public CVF_Values_Adapter(Context context, int layoutResourceId, ArrayList<CompanionAttributeValuesRec> list) {
            super(context, layoutResourceId, list);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // for large ListViews a ViewHolder Tag should be used
        // the widget views in the row have Tags that contain a pointer to their relevant CompanionAttributeValuesRec entry in the ListView list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template if the view is brand new (not recycled)
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);
            }

            CompanionAttributeValuesRec vRec = mListView_Values_List.get(position);

            // now properly configure the row's data and attributes
            // setup the TextView with the current text of the AttributeValues record;
            // set every textview to use the common Listener defined in the Fragment mainline
            TextView tv1 = (TextView)rowView.findViewById(R.id.rowtextView_value);
            tv1.setText(vRec.rValue);
            tv1.setTag(vRec);
            tv1.setOnClickListener(mTextViewListener_Values);
            TextView tv2 = (TextView)rowView.findViewById(R.id.rowtextView_likert);
            tv2.setText(String.valueOf(vRec.rLikert));
            tv2.setTag(vRec);
            tv2.setOnClickListener(mTextViewListener_Values);

            return rowView;
        }
    }


}
