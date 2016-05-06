package opensource.zeocompanion.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import opensource.zeocompanion.MainActivity;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.utility.Utilities;

// fragment within the MainActivity that allows entry of Sleep Journal Going-to-Sleep events
public class MainGoingFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private int mSleepStage = -1;

    // member constants and other static content
    private static final String _CTAG = "MGF";
    private static final String ARG_PARAM1 = "param1";

    // constructor
    public MainGoingFragment() {}

    // instanciator
    public static MainGoingFragment newInstance(int sleepStage) {
        MainGoingFragment fragment = new MainGoingFragment();
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
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView =  inflater.inflate(R.layout.fragment_main_going, container, false);

        mRootView.findViewById(R.id.button_stillAwake).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                boolean r = ZeoCompanionApplication.mCoordinator.recordDaypointEvent(mSleepStage, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_STILL_AWAKE, "");
                if (r) { Toast.makeText(activity, "Event Recorded", Toast.LENGTH_SHORT).show(); }
            }
        });

        performEnabledCheck();
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        super.onDestroyView();
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        // nothing to destroy
    }

    // called by the Activity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_GOING_FRAGMENT) == 0){
            ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_GOING_FRAGMENT);
            Utilities.showAlertDialog(getContext(), "Hint", "Hint: If you have trouble falling asleep, keep this tab open, and press the screen-sized 'Press If Still Awake' button throughout the night if still awake.\n\nLeaving this tab open will cause your display to stay on and dim after 30 seconds, so be sure your device is charging and dim the display.", "Okay");
        }
        performEnabledCheck();
    }

    // called by the Activity when handlers or settings have made changes to the database
    // or to settings options, etc
    @Override
    public void needToRefresh() {
        // not needed for this fragment
    }

    // called by the Activity at the behest of the Journal Data Coordinator
    @Override
    public void daypointHasChanged() {
        performEnabledCheck();
    }

    // called by the Activity to have Fragments dim their controls during sleep
    @Override
    public void dimControlsForSleep(boolean doDim) {
        //Log.d(_CTAG + ".dimControls", "Auto-dim set to "+doDim);
        Button bt = (Button)mRootView.findViewById(R.id.button_stillAwake);
        if (doDim) {
            bt.setBackgroundColor(Color.BLACK);
            bt.setTextColor(Color.GRAY);
        } else {
            bt.setBackgroundColor(getResources().getColor(R.color.colorDataEntry));
            bt.setTextColor(Color.BLACK);
        }
    }

    // determine from the JDC whether this fragment should be enabled or disabled due to the Daypoint
    private void performEnabledCheck() {
        String msg = ZeoCompanionApplication.mCoordinator.isFragmentDaypointEnabled(mSleepStage);
        if (msg == null) {
            // enabled for data entry
            mRootView.findViewById(R.id.button_stillAwake).setEnabled(true);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.INVISIBLE);
            mRootView.findViewById(R.id.textView_dimout).setVisibility(View.INVISIBLE);
        } else {
            // disabled
            mRootView.findViewById(R.id.button_stillAwake).setEnabled(false);
            mRootView.findViewById(R.id.imageView_dimout).setVisibility(View.VISIBLE);
            TextView tv = ((TextView)mRootView.findViewById(R.id.textView_dimout));
            tv.setText(msg);
            tv.setVisibility(View.VISIBLE);
        }
    }
}
