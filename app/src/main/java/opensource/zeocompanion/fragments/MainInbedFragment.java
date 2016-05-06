package opensource.zeocompanion.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

// fragment within the MainActivity that allows entry of Sleep Journal In-Bed events
public class MainInbedFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private int mSleepStage = -1;
    private EvtSpinner mSpinner = null;
    private ArrayAdapter<String> mSpinner_Adapter = null;
    private ArrayList<String> mSpinner_List = null;

    // member constants and other static content
    private static final String _CTAG = "MIF";
    private static final String ARG_PARAM1 = "sleepStage";

    // setup a common listener for every spinner in the ListView;
    // this class's methods block non-user initiated onItemSelected callbacks; the Spinner MUST be touched first
    private class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
        private boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(_CTAG+".onTouch", "Touch on view "+v);
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
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_NOT_YET_SLEEPING, doing);
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
    public MainInbedFragment() {}

    // instanciator
    public static MainInbedFragment newInstance(int sleepStage) {
        MainInbedFragment fragment = new MainInbedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, sleepStage);
        fragment.setArguments(args);
        return fragment;
    }

    // called by the framework to create the fragment (typically used to reload passed Fragment parameters)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(_CTAG + ".onCreate", "==========FRAG ON-CREATE=====");
        if (getArguments() != null) {
            mSleepStage = getArguments().getInt(ARG_PARAM1);
        }
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG+".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView =  inflater.inflate(R.layout.fragment_main_inbed, container, false);
        MainActivity activity = (MainActivity)getActivity();

        mRootView.findViewById(R.id.button_inbed).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOT_INTO_BED, "");
                if (r) Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show();
            }
        });

        mRootView.findViewById(R.id.button_gotoSleep).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP, "");
                if (r) Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show();
            }
        });

        mSpinner_List = new ArrayList<String>();
        int defaultPosition = rebuildSpinnerList();

        mSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_doing);
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
    public void onResume() {
        super.onResume();
        //Log.d(_CTAG + ".onResume", "==========FRAG ON-RESUME=====");
        // this logic helps prevent "ghost" clicks of the spinner when tabbing through the MainActivity FragmentPagerAdapter
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_doing);
        theSpinner.setOnItemSelectedListener(mListener);
    }

    // Called when the Fragment is no longer resumed
    @Override
    public void onPause () {
        super.onPause();
        //Log.d(_CTAG+".onPause","==========FRAG ON-PAUSE=====");
        // this logic helps prevent "ghost" clicks of the spinner when tabbing through the MainActivity FragmentPagerAdapter
        EvtSpinner theSpinner = (EvtSpinner)mRootView.findViewById(R.id.spinner_doing);
        theSpinner.setOnItemSelectedListener(null);
        mListener = null;
    }

    // called by the MainActivity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_INBED_FRAGMENT) == 0){
            ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_INBED_FRAGMENT);
            Utilities.showAlertDialog(getContext(), "Hint", "Hint: Press the 'Got Into Bed' button when you get into bed. Optionally pick activities if you are doing something other than sleeping for awhile. Then press the 'Now Trying To Sleep' button when you are now trying to sleep.", "Okay");
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

    // determine from the JDC whether this fragment should be enabled or disabled due to the Daypoint
    private void performEnabledCheck() {
        String msg = ZeoCompanionApplication.mCoordinator.isFragmentDaypointEnabled(mSleepStage);
        if (msg == null) {
            // enabled for data entry
            mRootView.findViewById(R.id.button_inbed).setEnabled(true);
            mRootView.findViewById(R.id.button_gotoSleep).setEnabled(true);
            mRootView.findViewById(R.id.spinner_doing).setEnabled(true);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.textView_dimout).setVisibility(View.INVISIBLE);
        } else {
            // disabled
            mRootView.findViewById(R.id.button_inbed).setEnabled(false);
            mRootView.findViewById(R.id.button_gotoSleep).setEnabled(false);
            mRootView.findViewById(R.id.spinner_doing).setEnabled(false);
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
