package opensource.zeocompanion.fragments;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import java.util.ArrayList;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.views.TrendsGraphView;

// fragment within the StatsActivity that displays configurable statistical trends graph
public class StatsActivityTrendsFragment extends Fragment {
    // member variables
    private View mRootView = null;
    private boolean mLayoutDone = false;
    private RectF lastViewport = null;

    // member constants and other static content
    private static final String _CTAG = "M1F";

    // common listener for presses on the radio buttons
    CheckBox.OnCheckedChangeListener mCheckboxChangedListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);

            // Check which checkbox was checked
            switch(buttonView.getId()) {
                case R.id.checkBox_deep:
                    theTrendsGraph.toggleDeep(isChecked);
                    break;
                case R.id.checkBox_rem:
                    theTrendsGraph.toggleREM(isChecked);
                    break;
                case R.id.checkBox_light:
                    theTrendsGraph.toggleLight(isChecked);
                    break;
                case R.id.checkBox_awake:
                    theTrendsGraph.toggleAwake(isChecked);
                    break;
                case R.id.checkBox_time2z:
                    theTrendsGraph.toggleTimeToZ(isChecked);
                    break;
                case R.id.checkBox_total:
                    theTrendsGraph.toggleTotalSleep(isChecked);
                    break;
                case R.id.checkBox_zq:
                    theTrendsGraph.toggleZQ(isChecked);
                    break;
            }
        }
    };

    // common listener for presses on the radio buttons
    View.OnClickListener mRadioButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton)view).isChecked();
            TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radioButton_lines:
                    if (checked) {
                        theTrendsGraph.toggleBarsAndLines(false);
                    }
                    break;
                case R.id.radioButton_barsAndLines:
                    if (checked) {
                        theTrendsGraph.toggleBarsAndLines(true);
                    }
                    break;
            }
        }
    };

    // constructor
    public StatsActivityTrendsFragment() {}

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG+".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_stats_activity_trends, container, false);

        // setup all the checkboxes
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_light);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_time2z);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_zq);
        cb.setOnCheckedChangeListener(mCheckboxChangedListener);

        // setup all the radio buttons
        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_lines);
        rb.setChecked(true);
        rb.setOnClickListener(mRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_barsAndLines);
        rb.setOnClickListener(mRadioButtonOnClickListener);

        final TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theTrendsGraph.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    theTrendsGraph.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    theTrendsGraph.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mLayoutDone = true;
                createGraphs();
            }
        });
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        mLayoutDone = false;
        super.onDestroyView();
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
    }

    // Called when the fragment is visible to the user and actively running
    @Override
    public void onResume() {
        super.onResume();
        //Log.d(_CTAG + ".onResume", "==========FRAG ON-RESUME=====");
        if (mLayoutDone) { createGraphs(); }
    }

    // Called when the Fragment is no longer resumed
    @Override
    public void onPause () {
        super.onPause();
        //Log.d(_CTAG + ".onPause", "==========FRAG ON-PAUSE=====");
    }

    // create the various graphs; should have completed layout
    private void createGraphs() {
        // get user's sleep goals (if any)
        double goalTotalSleepMin = 480.0;
        double goalDeepPct = 15.0;
        double goalREMpct = 20.0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        double d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_hours_per_night", 8.0);
        if (d > 0.0) { goalTotalSleepMin = d * 60.0; }
        d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_percent_deep", 15.0);
        if (d > 0.0 && d <= 100.0) { goalDeepPct = d; }
        d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_percent_REM", 20.0);
        if (d > 0.0 && d <= 100.0) { goalREMpct = d; }

        ArrayList<JournalDataCoordinator.IntegratedHistoryRec> theIrecs = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        ArrayList<TrendsGraphView.Trends_dataSet> theData = new ArrayList<TrendsGraphView.Trends_dataSet>();
        ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecs(theIrecs); // sorted newest to oldest
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: theIrecs) {
            if (iRec.theZAH_SleepRecord != null) {
                TrendsGraphView.Trends_dataSet ds = new TrendsGraphView.Trends_dataSet(iRec.theZAH_SleepRecord.rStartOfNight, iRec.theZAH_SleepRecord.rTime_to_Z_min,
                        iRec.theZAH_SleepRecord.rTime_Total_Z_min, iRec.theZAH_SleepRecord.rTime_REM_min, iRec.theZAH_SleepRecord.rTime_Awake_min,
                        iRec.theZAH_SleepRecord.rTime_Light_min, iRec.theZAH_SleepRecord.rTime_Deep_min, iRec.theZAH_SleepRecord.rCountAwakenings,
                        iRec.theZAH_SleepRecord.rZQ_Score);
                theData.add(ds);
            }
        }

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theTrendsGraph.prepareForStats(screenSize);
        whichIsChecked();
        theTrendsGraph.setDataset(theData, goalTotalSleepMin, goalREMpct, goalDeepPct);

        //theTrendsGraph.setScrollScaleListener(1L, mScrollScaleListener);
    }

    // determine which check boxes and radio buttons are already checked
    private void whichIsChecked() {
        TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep);
        theTrendsGraph.mShowDeep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_light);
        theTrendsGraph.mShowLight = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem);
        theTrendsGraph.mShowREM = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake);
        theTrendsGraph.mShowAwake = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_time2z);
        theTrendsGraph.mShowTimeToZ = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total);
        theTrendsGraph.mShowTotalSleep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_zq);
        theTrendsGraph.mShowZQscore = cb.isChecked();

        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_barsAndLines);
        if (rb.isChecked()) { theTrendsGraph.mShowBarsAndLines = true; };
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_lines);
        if (rb.isChecked()) { theTrendsGraph.mShowBarsAndLines = false; };
    }
}
