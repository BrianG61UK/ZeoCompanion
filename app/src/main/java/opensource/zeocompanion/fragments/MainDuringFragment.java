package opensource.zeocompanion.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionEventDoingsRec;
import opensource.zeocompanion.utility.Utilities;

import com.android.EvtSpinner;

// fragment within the MainActivity that allows entry of Sleep Journal During-Sleep events
public class MainDuringFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private int mSleepStage = -1;
    private EvtSpinner mSpinner = null;
    private ArrayAdapter<String> mSpinner_Adapter = null;
    private ArrayList<String> mSpinner_List = null;

    // member constants and other static content
    private static final String _CTAG = "MDF";
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
                EvtSpinner theSpinner =  (EvtSpinner)parentView;
                String doing = ((TextView)selectedItemView).getText().toString();
                MainActivity activity = (MainActivity)getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_DID_SOMETHING, doing);
                if (r) { Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show(); }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // not needed for our implementation but must be present
        }
    }
    private SpinnerInteractionListener mListener = new SpinnerInteractionListener();

    // constructor
    public MainDuringFragment() {}

    // instanciator
    public static MainDuringFragment newInstance(int sleepStage) {
        MainDuringFragment fragment = new MainDuringFragment();
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
        }
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG+".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView =  inflater.inflate(R.layout.fragment_main_during, container, false);

        mRootView.findViewById(R.id.button_wokeup).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP, "");
                if (r) {  Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show(); }
            }
        });

        mRootView.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_RETRY_TO_SLEEP, "");
                if (r) { Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show(); }
            }
        });

        mSpinner_List = new ArrayList<String>();
        int defaultPosition = rebuildSpinnerList();

        mSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_didSomething);
        mSpinner.setListenerInfo_DefaultPosition(defaultPosition);
        mSpinner_Adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_dataentry, mSpinner_List);
        mSpinner_Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dataentry);
        mSpinner.setAdapter(mSpinner_Adapter);
        mSpinner.setSelectionWithoutCallback(defaultPosition);

        performEnabledCheck();
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        super.onDestroyView();
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
    }

    // Called when the fragment is visible to the user and actively running
    @Override
    public void onResume () {
        super.onResume();
        //Log.d(_CTAG + ".onResume", "==========FRAG ON-RESUME=====");
        // this logic helps prevent "ghost" clicks of the spinner when tabbing through the MainActivity FragmentPagerAdapter
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_didSomething);
        theSpinner.setOnTouchListener(mListener);
        theSpinner.setOnItemSelectedListener(mListener);
    }

    // Called when the Fragment is no longer resumed
    @Override
    public void onPause () {
        super.onPause();
        //Log.d(_CTAG+".onPause", "==========FRAG ON-PAUSE=====");
        // this logic helps prevent "ghost" clicks of the spinner when tabbing through the MainActivity FragmentPagerAdapter
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_didSomething);
        theSpinner.setOnTouchListener(null);
        theSpinner.setOnItemSelectedListener(null);
    }

    // called by the MainActivity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_DURING_FRAGMENT) == 0){
            ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_DURING_FRAGMENT);
            Utilities.showAlertDialog(getContext(), "Hint", "Hint: If you have woken up in the middle of the night, press the 'Woke Up' button. If you do something before re-trying to sleep, choose the proper activities. Then when you are retrying to sleep, press the 'Retrying To Sleep' button.\n\nLeaving this tab open will cause your display to stay on and dim after 30 seconds, so be sure your device is charging and dim the display.", "Okay");
        }
        performEnabledCheck();
    }

    // called by the MainActivity when handlers or settings have made changes to the database
    // or to settings options, etc
    @Override
    public void needToRefresh() {
        mSpinner_List.clear();
        int defaultPosition = rebuildSpinnerList();
        mSpinner.setListenerInfo_DefaultPosition(defaultPosition);
        mSpinner_Adapter.notifyDataSetChanged();
        mSpinner.setSelectionWithoutCallback(defaultPosition);
    }

    // called by the MainActivity at the behest of the Journal Data Coordinator
    @Override
    public void daypointHasChanged() {
        performEnabledCheck();
    }

    // called by the MainActivity to have Fragments dim their controls during sleep
    @Override
    public void dimControlsForSleep(boolean doDim) {
        //Log.d(_CTAG + ".dimControls", "Auto-dim set to " + doDim);
        Button bt1 = (Button)mRootView.findViewById(R.id.button_wokeup);
        Button bt2 = (Button)mRootView.findViewById(R.id.button_retry);
        TextView sp_tv = (TextView)mSpinner.getChildAt(0);
        if (doDim) {
            bt1.setBackgroundColor(Color.BLACK);
            bt1.setTextColor(Color.GRAY);
            bt2.setBackgroundColor(Color.BLACK);
            bt2.setTextColor(Color.GRAY);
            if (sp_tv != null) { sp_tv.setTextColor(Color.GRAY); }
        } else {
            int colorDE = getResources().getColor(R.color.colorDataEntry);
            bt1.setBackgroundColor(colorDE);
            bt1.setTextColor(Color.BLACK);
            bt2.setBackgroundColor(colorDE);
            bt2.setTextColor(Color.BLACK);
            if (sp_tv != null) { sp_tv.setTextColor(colorDE); }
        }
    }

    // determine from the JDC whether this fragment should be enabled or disabled due to the Daypoint
    private void performEnabledCheck() {
        String msg = ZeoCompanionApplication.mCoordinator.isFragmentDaypointEnabled(mSleepStage);
        if (msg == null) {
            // enabled for data entry
            mRootView.findViewById(R.id.button_wokeup).setEnabled(true);
            mRootView.findViewById(R.id.button_retry).setEnabled(true);
            mRootView.findViewById(R.id.spinner_didSomething).setEnabled(true);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.textView_dimout).setVisibility(View.INVISIBLE);
        } else {
            // disabled
            mRootView.findViewById(R.id.button_wokeup).setEnabled(false);
            mRootView.findViewById(R.id.button_retry).setEnabled(false);
            mRootView.findViewById(R.id.spinner_didSomething).setEnabled(false);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.VISIBLE);
            TextView tv = ((TextView)mRootView.findViewById(R.id.textView_dimout));
            tv.setText(msg);
            tv.setVisibility(View.VISIBLE);
        }
    }

    // rebuild the spinner's list
    private int rebuildSpinnerList() {
        int defaultPosition = 0;
        int defaultRank = 0;
        int i = 0;
        Cursor cursor1 = ZeoCompanionApplication.mDatabaseHandler.getAllEventDoingsRecsSortedDoing();
        if (cursor1 != null) {
            if (cursor1.moveToFirst()) {
                do {
                    CompanionEventDoingsRec rec1 = new CompanionEventDoingsRec(cursor1);
                    if ((rec1.rAppliesToStages & mSleepStage) != 0) {
                        mSpinner_List.add(rec1.rDoing);
                        if (rec1.rIsDefaultPriority > defaultRank) {
                            defaultRank = rec1.rIsDefaultPriority;
                            defaultPosition = i;
                        }
                        i++;
                    }
                } while (cursor1.moveToNext());
            }
            cursor1.close();
        }
        return defaultPosition;
    }

}
