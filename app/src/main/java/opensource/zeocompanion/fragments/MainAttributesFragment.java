package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAttributeValuesRec;
import opensource.zeocompanion.database.CompanionAttributesRec;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.utility.LLL_Adapter;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.views.ListLinearLayout;
import com.android.AttrSpinner;

// primary UI for collecting before sleep and after sleep attribute values
public class MainAttributesFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private ListLinearLayout mLLL = null;
    private AV_LLL_Adapter mLLL_Adapter = null;
    private ArrayList<CompanionAttributesRec> mLLL_ListAttributes = null;
    private ArrayList<CompanionAttributeValuesRec> mLLL_ListAttributeValues = null;
    private int mSleepStage = -1;
    private boolean mShowHidden = false;

    // member constants and other static content
    private String _CTAG = "MAF";
    private static final String ARG_PARAM1 = "sleepStage";

    // setup a common listener for every spinner in the ListView;
    // this class's methods block non-user initiated onItemSelected callbacks; the Spinner MUST be touched first
    private class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        private boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Log.d(_CTAG+".onTouch", "Touch on view "+v);
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            // a spinner item was selected; note this will also get called when setting the default value
            //Log.d(_CTAG+".onItemSelected", "View="+parentView+", userSelect="+userSelect);
            if (userSelect) {
                userSelect = false;

                // a spinner item was selected due only to user touch
                AttrSpinner theSpinner =  (AttrSpinner)parentView;
                CompanionAttributesRec aRec = mLLL_ListAttributes.get(theSpinner.mAttributesArrayPosition);
                if (position == theSpinner.mDefaultPosition) {
                    // since the default position is the current value position; do nothing so as to ignore false selections
                    //Log.d(_CTAG+".onItemSelected", "attribute="+aRec.mAttributeDisplayName+", spinner at default position="+position+"; IGNORED");
                    return;
                }
                String value = ((TextView)selectedItemView).getText().toString();

                if (position == 0) {
                    // end-user selected to remove a value from the attribute
                    //Log.d(_CTAG+".onItemSelected", "attribute="+aRec.mAttributeDisplayName+", spinner position="+position+", value="+value);
                    ZeoCompanionApplication.mCoordinator.removeDaypointInfoAttributeValueOfType(mSleepStage, aRec);
                    mLLL_Adapter.rowValueUpdated(theSpinner, aRec, null);
                    return;
                }

                // end user selected to add or replace a value to the attribute;
                // first need to locate value's record so we can get the matching likert value
                //Log.d(_CTAG+".onItemSelected", "attribute="+aRec.mAttributeDisplayName+", spinner position="+position+", value="+value);
                Float likert = (float)0.0;
                for (CompanionAttributeValuesRec vRec: mLLL_ListAttributeValues) {
                    if (vRec.rAttributeDisplayName.equals(aRec.rAttributeDisplayName) && vRec.rValue.equals(value)) {
                        likert = vRec.rLikert;
                        break;
                    }
                }
                ZeoCompanionApplication.mCoordinator.recordDaypointAttributeValueOfType(mSleepStage, aRec, value, likert);
                mLLL_Adapter.rowValueUpdated(theSpinner, aRec, value);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // not needed for our implementation but must be present
        }
    }
    private SpinnerInteractionListener mListener = new SpinnerInteractionListener();

    // constructor
    public MainAttributesFragment() {}

    // instanciator
    public static MainAttributesFragment newInstance(int sleepStage) {
        MainAttributesFragment fragment = new MainAttributesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, sleepStage);
        fragment.setArguments(args);
        return fragment;
    }

    // called by the framework to create the fragment (typically used to reload passed Fragment parameters)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSleepStage = getArguments().getInt(ARG_PARAM1);
            _CTAG = "MAF" + mSleepStage;
        }
        //Log.d(_CTAG + ".onCreate", "==========FRAG ON-CREATE=====");
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");

        // construct the proper view based upon passed sleep stage from the MainActivity
        if (mSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) {
            mRootView = inflater.inflate(R.layout.fragment_main_attributes_after, container, false);

            // setup a listener for end-user presses of the Done Sleeping button; have the JournalDataCoordinator create an event
            mRootView.findViewById(R.id.button_doneSleep).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING, "");
                    if (r) { Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show(); }
                }
            });
        } else {
            mRootView = inflater.inflate(R.layout.fragment_main_attributes_before, container, false);
        }

        // obtain the screen size in its current orientation
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        // handle the Show Hidden checkbox
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_showHidden);
        if (screenSize.x < 600) { cb.setText("Show\nhidden"); }
        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                mShowHidden = isChecked;
                mLLL.refreshVisibilities();
            }
        });

        // build the attributes and values lists for the ListView and the Spinners from the database
        mLLL_ListAttributes = new ArrayList<CompanionAttributesRec>();
        mLLL_ListAttributeValues = new ArrayList<CompanionAttributeValuesRec>();
        buildLists();

        // setup the ListView
        mLLL = (ListLinearLayout) mRootView.findViewById(R.id.list_container_attributes);
        mLLL_Adapter = new AV_LLL_Adapter(getActivity(), this, mLLL, R.layout.fragment_main_attributes_row, mLLL_ListAttributes, mLLL_ListAttributeValues);
        mLLL.setAdapter(mLLL_Adapter);

        /*mLLL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    mLLL.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mLLL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mLLL.refreshVisibilities();
            }
        });*/

        performEnabledCheck();
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        // though disputed, assist garbage collection by clearing out the ListView contents and views
        mLLL.clearAllViews();
        mLLL_Adapter.clear();
        mLLL_ListAttributes.clear();
        mLLL_ListAttributeValues.clear();

        super.onDestroyView();
    }

    // Called when the fragment is visible to the user and actively running
    @Override
    public void onResume () {
        super.onResume();
        //Log.d(_CTAG+".onResume", "==========FRAG ON-RESUME=====");
    }

    // Called when the Fragment is no longer resumed
    @Override
    public void onPause () {
        super.onPause();
        //Log.d(_CTAG + ".onPause", "==========FRAG ON-PAUSE=====");
    }

    // called by the Activity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        //Log.d(_CTAG + ".fragmentBecameShown", "==========FRAG BECAME-SHOWN=====");
        // show a one-time hint
        if (mSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER) {
            if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_ATTRIBUTES_FRAGMENT_AFTER) == 0){
                ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_ATTRIBUTES_FRAGMENT_AFTER);
                Utilities.showAlertDialog(getContext(), "Hint", "Hint: Remember to press the Done Sleeping button when you get up in the morning to let the journal know you are done sleeping.\n\nTo set a value for a morning after-sleep results attribute, press the blue 'spinner' text and select the value desired.", "Okay");
            }
        } else {
            if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_ATTRIBUTES_FRAGMENT_BEFORE) == 0){
                ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_ATTRIBUTES_FRAGMENT_BEFORE);
                Utilities.showAlertDialog(getContext(), "Hint", "Hint: You can thin down and even fully customize this list in the drop-menu under Customize Attributes.\n\nTo set a value for a before-sleep attribute, press the blue 'spinner' text and select the value desired.", "Okay");
            }
        }

        // perform refreshes and display checks
        needToRefresh();
        performEnabledCheck();
    }

    // the definitions for attributes and values have been changed in the database;
    // need to rebuild everything
    @Override
    public void needToRefresh() {
        mLLL_ListAttributes.clear();
        mLLL_ListAttributeValues.clear();
        mLLL_Adapter.clear();                   // this invokes an onChange to the LLL
        buildLists();
        mLLL_Adapter.notifyDataSetChanged();    // via the onChange, automatically performs mLLL.clearAllViews()
    }

    // called by the Activity at the behest of the Journal Data Coordinator
    @Override
    public void daypointHasChanged() {
        //Log.d(_CTAG + ".daypointHasChanged", "==========FRAG DAYPOINT-CHANGE=====");
        performEnabledCheck();
        needToRefresh();
    }

    // determine from the JDC whether this fragment should be enabled or disabled due to the Daypoint
    private void performEnabledCheck() {
        if (mSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) { return; }
        String msg = ZeoCompanionApplication.mCoordinator.isFragmentDaypointEnabled(mSleepStage);
        if (msg == null) {
            // enabled for data entry
            mRootView.findViewById(R.id.button_doneSleep).setEnabled(true);
            mRootView.findViewById(R.id.list_container_attributes).setEnabled(true);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.textView_dimout).setVisibility(View.INVISIBLE);
        } else {
            // disabled
            mRootView.findViewById(R.id.button_doneSleep).setEnabled(false);
            mRootView.findViewById(R.id.list_container_attributes).setEnabled(false);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.VISIBLE);
            TextView tv = ((TextView)mRootView.findViewById(R.id.textView_dimout));
            tv.setText(msg);
            tv.setVisibility(View.VISIBLE);
        }
        boolean show = ZeoCompanionApplication.mCoordinator.isFragmentDaypointButtonDoneSleepingEnabled();
        if (show) { mRootView.findViewById(R.id.button_doneSleep).setVisibility(View.VISIBLE); }
        else { mRootView.findViewById(R.id.button_doneSleep).setVisibility(View.INVISIBLE); }
    }

    // construct or re-construct the lists that drive the ListView and the Spinners
    private void buildLists() {
        // build the attributes list for the ListView from the database
        Cursor cursor1 = ZeoCompanionApplication.mDatabaseHandler.getAllAttributeRecsSortedInvSleepStageDisplayOrder();
        if (cursor1 != null) {
            if (cursor1.moveToFirst()) {
                do {
                    CompanionAttributesRec rec1 = new CompanionAttributesRec(cursor1);
                    if ((rec1.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_FACTORY_DISABLED) == 0) {
                        if (rec1.rAppliesToStage == mSleepStage) { mLLL_ListAttributes.add(rec1); }
                    }
                } while (cursor1.moveToNext());
            }
            cursor1.close();
        }

        // build the values list for the ListView from the database
        Cursor cursor2 = ZeoCompanionApplication.mDatabaseHandler.getAllAttributeValuesRecsSortedLikert();
        if (cursor2 != null) {
            if (cursor2.moveToFirst()) {
                do {
                    CompanionAttributeValuesRec rec2 = new CompanionAttributeValuesRec(cursor2);
                    for (CompanionAttributesRec rec3: mLLL_ListAttributes) {
                        if (rec2.rAttributeDisplayName.equals(rec3.rAttributeDisplayName)) {
                            mLLL_ListAttributeValues.add(rec2);
                            break;
                        }
                    }
                } while (cursor2.moveToNext());
            }
            cursor2.close();
        }
    }

    // ViewHolder class to hold all the child views within a row to speed up processing
    private static class AV_LLL_Adapter_ViewTag {
        CompanionAttributesRec aRec = null;
        AttrSpinner sp_Spinner_NewValue = null;
        ArrayAdapter<String> aa_Spinner_NewValue = null;
        ArrayList<String> al_Spinner_NewValue = null;
    }

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes both CompanionAttributesRec and CompanionAttributeValuesRec as its list entries
    class AV_LLL_Adapter extends LLL_Adapter {
        // member variables
        private Context mContext = null;
        private int mLayoutResourceId = -1;
        private ListLinearLayout mListLinearLayout = null;
        private MainAttributesFragment mFragment;
        private ArrayList<CompanionAttributesRec> mArrayList_Attributes = null;
        private ArrayList<CompanionAttributeValuesRec> mArrayList_AttributeValues = null;

        // constructor
        public AV_LLL_Adapter(Context context, MainAttributesFragment frag, ListLinearLayout listLinearLayout, int layoutResourceId, ArrayList<CompanionAttributesRec> list1, ArrayList<CompanionAttributeValuesRec> list2) {
            super(context, layoutResourceId, list1);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mListLinearLayout = listLinearLayout;
            mArrayList_Attributes = list1;
            mArrayList_AttributeValues = list2;
            mFragment = frag;
        }

        @Override
        public void notifyDataSetChanged() {
            mListLinearLayout.onChanged();
            super.notifyDataSetChanged();
        }

        @Override
        public void destroyView(View view) {
            AV_LLL_Adapter_ViewTag tag = (AV_LLL_Adapter_ViewTag)view.getTag();
            if (tag != null) {
                tag.sp_Spinner_NewValue.setOnTouchListener(null);
                tag.sp_Spinner_NewValue.setOnItemSelectedListener(null);
                tag.al_Spinner_NewValue.clear();
                tag.aa_Spinner_NewValue.clear();
                tag.sp_Spinner_NewValue.setAdapter(null);
                tag.al_Spinner_NewValue = null;
                tag.aa_Spinner_NewValue = null;
                tag.sp_Spinner_NewValue = null;
                tag.aRec = null;
            }
        }

        // populate a row's View; these views are NOT recycled; convertView will always be NULL
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            View rowView = inflater.inflate(mLayoutResourceId, parent, false);

            // get the attribute for this position, and the attribute's current value
            CompanionAttributesRec aRec = mArrayList_Attributes.get(position);
            String currValue = ZeoCompanionApplication.mCoordinator.getDaypointCurrentInfoAttributeValue(mFragment.mSleepStage, aRec);

            // setup the callback tag with pointers to things that will need changing upon user presses of the Spinner
            AV_LLL_Adapter_ViewTag newTag = new AV_LLL_Adapter_ViewTag();
            newTag.aRec = aRec;
            newTag.sp_Spinner_NewValue = (AttrSpinner)rowView.findViewById(R.id.rowSpinnerValues);
            newTag.al_Spinner_NewValue = new ArrayList<String>();

            // set the attribute name in the view
            TextView tv_Attribute = (TextView)rowView.findViewById(R.id.rowTextViewAttribute);
            tv_Attribute.setText(aRec.rAttributeDisplayName);

            // show the current value for this attribute as recorded in the database
            TextView tv_CurrValue = (TextView)rowView.findViewById(R.id.rowTextViewValue);
            if (currValue == null) { tv_CurrValue.setText("-none-"); }
            else if (currValue.isEmpty()) {tv_CurrValue.setText("-none-"); }
            else { tv_CurrValue.setText(currValue); }

            // create the list of possible values for this attribute to populate the spinner
            newTag.al_Spinner_NewValue.add("-none-");
            int i = 1;
            int currValuePosition = -1;
            for (CompanionAttributeValuesRec avRec: mArrayList_AttributeValues) {
                if (avRec.rAttributeDisplayName.equals(aRec.rAttributeDisplayName)) {
                    newTag.al_Spinner_NewValue.add(avRec.rValue);
                    if (currValue != null) {
                        if (currValue.equals(avRec.rValue)) { currValuePosition = i; }
                    }
                    i++;
                }
            }

            // setup the spinner
            newTag.aa_Spinner_NewValue = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_dataentry, newTag.al_Spinner_NewValue);
            newTag.aa_Spinner_NewValue.setDropDownViewResource(R.layout.spinner_dropdown_item_dataentry);
            newTag.sp_Spinner_NewValue.setAdapter(newTag.aa_Spinner_NewValue);
            newTag.aa_Spinner_NewValue.notifyDataSetChanged();

            // set the initial value of the spinner
            if (currValue == null) {
                // there is no daypoint CSE yet
                newTag.sp_Spinner_NewValue.setListenerInfo(rowView, position, 0);   // current value therefore is none
                newTag.sp_Spinner_NewValue.setSelection(0, false);  // OnItemSelectedListener will get called; must do setListenerInfo before this call
            } else if (currValuePosition <= 0) {
                // there is a daypoint CSE, but the attribute has no defined value yet, OR
                // there is a daypoint CSE, and the value is explicitly none
                newTag.sp_Spinner_NewValue.setListenerInfo(rowView, position, 0);   // current value therefore is none
                newTag.sp_Spinner_NewValue.setSelection(0, false);  // OnItemSelectedListener will get called; must do setListenerInfo before this call
            } else {
                // there is a daypoint CSE, and a defined value as well
                newTag.sp_Spinner_NewValue.setListenerInfo(rowView, position, currValuePosition);
                newTag.sp_Spinner_NewValue.setSelection(currValuePosition, false);  // OnItemSelectedListener will get called; must do setListenerInfo before this call
            }

            // finally allow the spinner listener to receive touches
            newTag.sp_Spinner_NewValue.setOnTouchListener(mListener);
            newTag.sp_Spinner_NewValue.setOnItemSelectedListener(mListener);

            rowView.setTag(newTag);
            return rowView;
        }

        @Override
        public boolean getShouldBeVisible(int position, View view) {
            AV_LLL_Adapter_ViewTag tag = (AV_LLL_Adapter_ViewTag)view.getTag();
            return getShouldBeVisible(position, tag);
        }
        public boolean getShouldBeVisible(int position, AV_LLL_Adapter_ViewTag tag) {
            if (mShowHidden) { return true; }
            if (tag == null) { return true; }
            if (tag.aRec == null) { return true; }
            if  ((tag.aRec.rFlags & CompanionDatabaseContract.CompanionAttributes.COMPANION_ATTRIBUTES_FLAG_VISIBLE) != 0) { return true; }
            return false;
        }

        // update the row when the end-user has made a selection from the spinner
        public void rowValueUpdated(AttrSpinner theSpinner, CompanionAttributesRec aRec, String newValue) {
            // change the current value field to what is newly placed into the database
            View rowView = theSpinner.mInRowView;
            TextView tv_CurrValue = (TextView)rowView.findViewById(R.id.rowTextViewValue);

            int currValuePosition = 0;
            if (newValue == null) {
                // being removed aka set to -none-
                tv_CurrValue.setText("-none-");
            } else {
                // being changed
                tv_CurrValue.setText(newValue);
                int i = 1;
                for (CompanionAttributeValuesRec avRec: mArrayList_AttributeValues) {
                    if (avRec.rAttributeDisplayName.equals(aRec.rAttributeDisplayName)) {
                        if (newValue.equals(avRec.rValue)) { currValuePosition = i; break; }
                        i++;
                    }
                }
            }

            theSpinner.setListenerInfo_DefaultPosition(currValuePosition);
            theSpinner.setSelection(currValuePosition, false);  // OnItemSelectedListener will get called; must do setListenerInfo_DefaultPosition before this call
        }
    }
}
