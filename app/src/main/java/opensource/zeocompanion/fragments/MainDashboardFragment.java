package opensource.zeocompanion.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.StatsActivity;
import opensource.zeocompanion.database.CompanionAttributesRec;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionSleepEpisodeInfoParsedRec;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.views.AttrValsSleepDatasetRec;
import opensource.zeocompanion.views.AttributeEffectsGraphView;
import opensource.zeocompanion.views.AttributesHeatmapGraphView;
import opensource.zeocompanion.views.DaysHoursGraphView;
import opensource.zeocompanion.views.SleepDatasetRec;
import opensource.zeocompanion.views.TrendsGraphView;

// fragment within the MainActivity that displays simple non-configurable statistical graphs
public class MainDashboardFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private boolean mLayoutDone = false;
    ArrayList<SleepDatasetRec> mSleepData = null;
    ArrayList<AttrValsSleepDatasetRec> mAttrValsData = null;

    // member constants and other static content
    private static final String _CTAG = "M1F";
    private static final SimpleDateFormat mSDF = new SimpleDateFormat("MM/dd/yyyy");

    // common listener for presses on the trends radio buttons
    View.OnClickListener mTrendsRadioButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton)view).isChecked();
            TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radioButton_trends_deep:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleDeep(true);
                    }
                    break;
                case R.id.radioButton_trends_rem:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleREM(true);
                    }
                    break;
                case R.id.radioButton_trends_light:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleLight(true);
                    }
                    break;
                case R.id.radioButton_trends_awake:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleAwake(true);
                    }
                    break;
                case R.id.radioButton_trends_time2z:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleTimeToZ(true);
                    }
                    break;
                case R.id.radioButton_trends_total:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleTotalSleep(true);
                    }
                    break;
                case R.id.radioButton_trends_zq:
                    if (checked) {
                        theTrendsGraph.toggleAllOff();
                        theTrendsGraph.toggleZQ(true);
                    }
                    break;
            }
        }
    };

    // common listener for presses on the attributes heatmap checkbox buttons
    CheckBox.OnCheckedChangeListener mAttrsHeatmapCheckboxChangedListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);

            // Check which checkbox was checked
            switch(buttonView.getId()) {
                case R.id.checkBox_deep_attrsHeatmap:
                    theAttrsHeatmapGraph.toggleDeep(isChecked);
                    break;
                case R.id.checkBox_rem_attrsHeatmap:
                    theAttrsHeatmapGraph.toggleREM(isChecked);
                    break;
                case R.id.checkBox_awake_attrsHeatmap:
                    theAttrsHeatmapGraph.toggleAwake(isChecked);
                    break;
                case R.id.checkBox_awakenings_attrsHeatmap:
                    theAttrsHeatmapGraph.toggleAwakenings(isChecked);
                    break;
                case R.id.checkBox_total_attrsHeatmap:
                    theAttrsHeatmapGraph.toggleTotalSleep(isChecked);
                    break;
            }
        }
    };

    // common listener for presses on the days/hours graph checkbox buttons
    CheckBox.OnCheckedChangeListener mDaysHoursCheckboxChangedListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);

            // Check which checkbox was checked
            switch(buttonView.getId()) {
                case R.id.checkBox_deep_dayHour:
                    theDaysHoursGraph.toggleDeep(isChecked);
                    break;
                case R.id.checkBox_rem_dayHour:
                    theDaysHoursGraph.toggleREM(isChecked);
                    break;
                case R.id.checkBox_awake_dayHour:
                    theDaysHoursGraph.toggleAwake(isChecked);
                    break;
                case R.id.checkBox_awakenings_dayHour:
                    theDaysHoursGraph.toggleAwakenings(isChecked);
                    break;
                case R.id.checkBox_total_dayHour:
                    theDaysHoursGraph.toggleTotalSleep(isChecked);
                    break;
            }
        }
    };

    // common listener for presses on the radio buttons for the Days/Hours graph
    View.OnClickListener mDaysHoursRadioButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton)view).isChecked();
            DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radioButton_dayHour_DOW:
                    if (checked) {
                        theDaysHoursGraph.toggleDays(true);
                    }
                    break;
                case R.id.radioButton_dayHour_SH:
                    if (checked) {
                        theDaysHoursGraph.toggleDays(false);
                    }
                    break;
            }
        }
    };

    // listeners for the seekbars for the Attributes Heatmap
    SeekBar.OnSeekBarChangeListener mAttrsHeatmapSeekBar1ChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);
            double pct = (double)progress / 100.0;
            theAttrsHeatmapGraph.setThresholdZQpct(pct);
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_seekbarTitle_attrsHeatmap);
            int zq = (int)((theAttrsHeatmapGraph.mHighestZQ - theAttrsHeatmapGraph.mLowestZQ) * pct + theAttrsHeatmapGraph.mLowestZQ);
            tv.setText("Good ZQ Cutoff: "+String.valueOf(zq));
        }
    };
    SeekBar.OnSeekBarChangeListener mAttrsHeatmapSeekBar2ChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);
            double pct = (double)progress / 100.0;
            theAttrsHeatmapGraph.setThresholdDatePct(pct);
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_seekbar2Title_attrsHeatmap);
            long cutoffTimestamp = (long)((double)(theAttrsHeatmapGraph.mHighestTimestamp - theAttrsHeatmapGraph.mLowestTimestamp) * pct) + theAttrsHeatmapGraph.mLowestTimestamp - 43200000;   // less 12 hours
            tv.setText("Start Date Cutoff: "+mSDF.format(new Date(cutoffTimestamp)));
        }
    };
    SeekBar.OnSeekBarChangeListener mDaysHoursSeekBar2ChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);
            double pct = (double)progress / 100.0;
            theDaysHoursGraph.setThresholdDatePct(pct);
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_seekbar2Title_dayHour);
            long cutoffTimestamp = (long)((double)(theDaysHoursGraph.mHighestTimestamp - theDaysHoursGraph.mLowestTimestamp) * pct) + theDaysHoursGraph.mLowestTimestamp - 43200000;   // less 12 hours
            tv.setText("Start Date Cutoff: "+mSDF.format(new Date(cutoffTimestamp)));
        }
    };

    // common listener for presses on the attribute effects radio buttons
    /*View.OnClickListener mAttrEffectsRadioButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton)view).isChecked();
            AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radioButton_attrEffects_deep:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleDeep(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_rem:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleREM(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_light:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleLight(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_awake:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleAwake(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_time2z:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleTimeToZ(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_total:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleTotalSleep(true);
                    }
                    break;
                case R.id.radioButton_attrEffects_zq:
                    if (checked) {
                        theAttrEffectsGraph.toggleAllOff();
                        theAttrEffectsGraph.toggleZQ(true);
                    }
                    break;
            }
        }
    };*/

    private View.OnClickListener mTrendsGraphClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), StatsActivity.class);
            intent.putExtra("startTab", 0);
            startActivity(intent);
        }
    };

    /*private View.OnClickListener mAttrEffectsGraphClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), StatsActivity.class);
            intent.putExtra("startTab", 1);
            startActivity(intent);
        }
    };*/

    // setup a spinner listener;
    // this class's methods block non-user initiated onItemSelected callbacks; the Spinner MUST be touched first
    /*private class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
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
                AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);
                String str = theAttrEffectsGraph.mAttributes.get(position);
                theAttrEffectsGraph.toggleAttribute(str);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // not needed for our implementation but must be present
        }
    }
    private SpinnerInteractionListener mListener = new SpinnerInteractionListener();*/

    // constructor
    public MainDashboardFragment() {}

    // instanciator
    public static MainDashboardFragment newInstance() { return new MainDashboardFragment(); }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG+".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_main_dashboard, container, false);

        // setup all the radio buttons for trends
        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_deep);
        rb.setChecked(true);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_rem);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_light);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_awake);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_time2z);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_total);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_zq);
        rb.setOnClickListener(mTrendsRadioButtonOnClickListener);

        // setup all the checkboxes and sliders for attributes heatmap
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total_attrsHeatmap);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mAttrsHeatmapCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep_attrsHeatmap);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mAttrsHeatmapCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem_attrsHeatmap);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mAttrsHeatmapCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake_attrsHeatmap);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mAttrsHeatmapCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awakenings_attrsHeatmap);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mAttrsHeatmapCheckboxChangedListener);

        SeekBar sb1 = (SeekBar)mRootView.findViewById(R.id.seekbar_attrsHeatmap);
        sb1.setMax(100);
        sb1.setProgress(66);
        sb1.setOnSeekBarChangeListener(mAttrsHeatmapSeekBar1ChangeListener);
        SeekBar sb2 = (SeekBar)mRootView.findViewById(R.id.seekbar2_attrsHeatmap);
        sb2.setMax(100);
        sb2.setProgress(0);
        sb2.setOnSeekBarChangeListener(mAttrsHeatmapSeekBar2ChangeListener);

        // setup all the checkboxes, radio buttons, and sliders for Days/Hours graph
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total_dayHour);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mDaysHoursCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep_dayHour);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mDaysHoursCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem_dayHour);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mDaysHoursCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake_dayHour);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mDaysHoursCheckboxChangedListener);
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awakenings_dayHour);
        cb.setChecked(true);
        cb.setOnCheckedChangeListener(mDaysHoursCheckboxChangedListener);

        // setup all the radio buttons
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_dayHour_DOW);
        rb.setOnClickListener(mDaysHoursRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_dayHour_SH);
        rb.setChecked(true);
        rb.setOnClickListener(mDaysHoursRadioButtonOnClickListener);

        sb2 = (SeekBar)mRootView.findViewById(R.id.seekbar2_dayHour);
        sb2.setMax(100);
        sb2.setProgress(0);
        sb2.setOnSeekBarChangeListener(mDaysHoursSeekBar2ChangeListener);

        /*rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_deep);
        rb.setChecked(true);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_rem);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_light);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_awake);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_time2z);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_total);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);
        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_zq);
        rb.setOnClickListener(mAttrEffectsRadioButtonOnClickListener);*/

        // select the entire dataset needed for all graphs
        selectNeededData();

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
        TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theTrendsGraph.releaseDataset();
        AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);
        theAttrsHeatmapGraph.releaseDataset();
        DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);
        theDaysHoursGraph.releaseDataset();
        //AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);
        //theAttrEffectsGraph.releaseDataset();
        if (mSleepData != null) { mSleepData.clear(); }
        if (mAttrValsData != null) { mAttrValsData.clear(); }
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

    // called by the MainActivity when handlers or settings have made changes to the database
    // or to settings options, etc
    @Override
    public void needToRefresh() {
        selectNeededData();
        createGraphs();
    }

    // select the entire dataset needed for all graphs
    private void selectNeededData() {
        // determine if the Sleep Journal is enabled
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean journal_enabled = prefs.getBoolean("journal_enable", true);

        // prepare empty dataset arrays
        ArrayList<JournalDataCoordinator.IntegratedHistoryRec> theIrecs = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        if (mSleepData != null) { mSleepData.clear(); }
        else { mSleepData = new ArrayList<SleepDatasetRec>(); }
        if (mAttrValsData != null) { mAttrValsData.clear(); }
        else { mAttrValsData = new ArrayList<AttrValsSleepDatasetRec>(); }

        // obtain all integrated sleep data
        ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecs(theIrecs); // sorted newest to oldest

        // parse through the entire integrated database
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: theIrecs) {
            if (iRec.theZAH_SleepRecord != null) {
                int excluded = 0;
                if (iRec.theCSErecord != null) {
                    excluded = (iRec.theCSErecord.rStatesFlag & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_STATESFLAG_EXCLUDE_FROM_GRAPHS);
                }
                if (excluded == 0) {
                    // have an iRec that has Zeo App Sleep Session data;
                    // compose dateset for the time and trends graphs
                    SleepDatasetRec tds = new SleepDatasetRec(iRec.theZAH_SleepRecord.rStartOfNight, iRec.theZAH_SleepRecord.rTime_to_Z_min,
                            iRec.theZAH_SleepRecord.rTime_Total_Z_min, iRec.theZAH_SleepRecord.rTime_REM_min, iRec.theZAH_SleepRecord.rTime_Awake_min,
                            iRec.theZAH_SleepRecord.rTime_Light_min, iRec.theZAH_SleepRecord.rTime_Deep_min, iRec.theZAH_SleepRecord.rCountAwakenings,
                            iRec.theZAH_SleepRecord.rZQ_Score);
                    mSleepData.add(tds);

                    if (journal_enabled && iRec.theCSErecord != null) {
                        if (iRec.theCSErecord.doAttributesExist()) {
                            // have an iRec that also has ZeoCompanion sleep data, which contains attributes, and the Sleep Journal is enabled
                            iRec.theCSErecord.unpackInfoCSVstrings();
                            // compose dataset for the various attribute-based graphs
                            for (CompanionSleepEpisodeInfoParsedRec avr: iRec.theCSErecord.mAttribs_Fixed_array) {
                                if (avr != null) {
                                    if (avr.rSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) {
                                        AttrValsSleepDatasetRec ads = new AttrValsSleepDatasetRec(avr.rAttributeExportName, avr.rLikert, avr.rValue,
                                                iRec.theZAH_SleepRecord.rStartOfNight, iRec.theZAH_SleepRecord.rTime_to_Z_min,
                                                iRec.theZAH_SleepRecord.rTime_Total_Z_min, iRec.theZAH_SleepRecord.rTime_REM_min, iRec.theZAH_SleepRecord.rTime_Awake_min,
                                                iRec.theZAH_SleepRecord.rTime_Light_min, iRec.theZAH_SleepRecord.rTime_Deep_min, iRec.theZAH_SleepRecord.rCountAwakenings,
                                                iRec.theZAH_SleepRecord.rZQ_Score);
                                        mAttrValsData.add(ads);
                                    }
                                }
                            }
                            for (CompanionSleepEpisodeInfoParsedRec avr: iRec.theCSErecord.mAttribs_Vari_array) {
                                if (avr != null) {
                                    if (avr.rSleepStage == CompanionDatabaseContract.SLEEP_EPISODE_STAGE_BEFORE) {
                                        AttrValsSleepDatasetRec ads = new AttrValsSleepDatasetRec(avr.rAttributeExportName, avr.rLikert, avr.rValue,
                                                iRec.theZAH_SleepRecord.rStartOfNight, iRec.theZAH_SleepRecord.rTime_to_Z_min,
                                                iRec.theZAH_SleepRecord.rTime_Total_Z_min, iRec.theZAH_SleepRecord.rTime_REM_min, iRec.theZAH_SleepRecord.rTime_Awake_min,
                                                iRec.theZAH_SleepRecord.rTime_Light_min, iRec.theZAH_SleepRecord.rTime_Deep_min, iRec.theZAH_SleepRecord.rCountAwakenings,
                                                iRec.theZAH_SleepRecord.rZQ_Score);
                                        mAttrValsData.add(ads);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // attempt to locate the attribute display names for all the found data's attribute export names
        if (!mAttrValsData.isEmpty()) {
            Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAllAttributeRecsSortedInvSleepStageDisplayOrder();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int qty = cursor.getCount();
                    String[] attrsDisplay = new String[qty];
                    String[] attrsShort = new String[qty];
                    int i = 0;
                    do {
                        CompanionAttributesRec aRec = new CompanionAttributesRec(cursor);
                        attrsDisplay[i] = aRec.rAttributeDisplayName;
                        attrsShort[i] = aRec.rExportSlotName;
                        i++;
                    } while (cursor.moveToNext());

                    for (AttrValsSleepDatasetRec ads:  mAttrValsData) {
                        for (i = 0; i < qty; i++) {
                            if (ads.rAttributeShortName.equals(attrsShort[i])) {
                                ads.rAttributeDisplayName = attrsDisplay[i];
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // create the various graphs; should have completed layout
    private void createGraphs() {
        // get user's sleep goals (if any)
        double goalTotalSleepMin = 480.0;
        double goalDeepPct = 15.0;
        double goalREMpct = 20.0;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean journal_enabled = prefs.getBoolean("journal_enable", true);
        double d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_hours_per_night", 8.0);
        if (d > 0.0) { goalTotalSleepMin = d * 60.0; }
        d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_percent_deep", 15.0);
        if (d > 0.0 && d <= 100.0) { goalDeepPct = d; }
        d = Utilities.getPrefsEncryptedDouble(prefs, "profile_goal_percent_REM", 20.0);
        if (d > 0.0 && d <= 100.0) { goalREMpct = d; }

        // get current screen orientation sizes
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        // prepare the trends graph
        TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theTrendsGraph.prepareForDashboard(screenSize);
        whichIsCheckedTrends();
        boolean hasAnyData = theTrendsGraph.setDataset(mSleepData, goalTotalSleepMin, goalREMpct, goalDeepPct);
        if (theTrendsGraph.mDatasetLen == 0) {
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_trendtitle);
            tv.setText("Last 7 Session Trend; there is no data to display");
        } else if (theTrendsGraph.mDatasetLen == 1) {
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_trendtitle);
            tv.setText("Last 7 Session Trend; only one sleep session; line-graph will not be useful");
        }
        if (hasAnyData) { theTrendsGraph.setOnClickListener(mTrendsGraphClickListener); }
        else { theTrendsGraph.setOnClickListener(null); }

        // prepare the Days/Hours graph
        DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);
        theDaysHoursGraph.prepareForDashboard(screenSize);
        whichIsCheckedDaysHours();
        hasAnyData = theDaysHoursGraph.setDataset(mSleepData);
        if (!hasAnyData) {
            TextView tv = (TextView)mRootView.findViewById(R.id.textView_dayHourTitle);
            tv.setText("Start-Hour or Day-of-Week ZQ Spread Clustering; there is no data to display");
        } else {
            TextView tv2 = (TextView)mRootView.findViewById(R.id.textView_seekbar2Title_dayHour);
            long cutoffTimestamp = (long)((double)(theDaysHoursGraph.mHighestTimestamp - theDaysHoursGraph.mLowestTimestamp) * theDaysHoursGraph.mTimestampThresholdPct) + theDaysHoursGraph.mLowestTimestamp - 43200000;   // less 12 hours
            tv2.setText("Start Date Cutoff: "+mSDF.format(new Date(cutoffTimestamp)));
        }

        // prepare various attribute-based graphs
        RelativeLayout rl1 = (RelativeLayout)mRootView.findViewById(R.id.relativeLayout_attrsHeatmapTitle);
        //RelativeLayout rl2 = (RelativeLayout)mRootView.findViewById(R.id.relativeLayout_attrEffectsTitle);
        if (journal_enabled) {
            // sleep journal is enabled, so do show attribute-based graphs
            rl1.setVisibility(View.VISIBLE);
            //rl2.setVisibility(View.VISIBLE);

            // prepare the attributes-heatmap graph
            AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);
            theAttrsHeatmapGraph.prepareForDashboard(screenSize);
            whichIsCheckedAttrsHeatmap();
            hasAnyData = theAttrsHeatmapGraph.setDataset(mAttrValsData);
            if (!hasAnyData) {
                TextView tv = (TextView)mRootView.findViewById(R.id.textView_attrsHeatmapTitle);
                tv.setText("Attributes Usefulness HeatMap; there is no data to display");
            } else {
                TextView tv1 = (TextView)mRootView.findViewById(R.id.textView_seekbarTitle_attrsHeatmap);
                int zq = (int)((theAttrsHeatmapGraph.mHighestZQ - theAttrsHeatmapGraph.mLowestZQ) * theAttrsHeatmapGraph.mGoodThresholdPct + theAttrsHeatmapGraph.mLowestZQ);
                tv1.setText("Good ZQ Cutoff: "+String.valueOf(zq));
                TextView tv2 = (TextView)mRootView.findViewById(R.id.textView_seekbar2Title_attrsHeatmap);
                long cutoffTimestamp = (long)((double)(theAttrsHeatmapGraph.mHighestTimestamp - theAttrsHeatmapGraph.mLowestTimestamp) * theAttrsHeatmapGraph.mTimestampThresholdPct) + theAttrsHeatmapGraph.mLowestTimestamp - 43200000;   // less 12 hours
                tv2.setText("Start Date Cutoff: "+mSDF.format(new Date(cutoffTimestamp)));
            }

            // prepare the attribute-effects graph
            /*AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);
            theAttrEffectsGraph.prepareForDashboard(screenSize);
            whichIsCheckedAttrEffects();
            hasAnyData = theAttrEffectsGraph.setDataset(mAttrValsData, goalTotalSleepMin, goalREMpct, goalDeepPct);
            if (theAttrEffectsGraph.mShownDatasetLen == 0) {
                TextView tv = (TextView)mRootView.findViewById(R.id.textView_attrEffectsTitle);
                tv.setText("Last 7 Session Attribute Effects; there is no data to display");
            } else if (theAttrEffectsGraph.mShownDatasetLen == 1) {
                TextView tv = (TextView)mRootView.findViewById(R.id.textView_attrEffectsTitle);
                tv.setText("Last 7 Session Attribute Effects; only one sleep session; graph will not be useful");
            }
            theAttrEffectsGraph.setOnClickListener(mAttrEffectsGraphClickListener);

            // setup the attribute-effects spinner with the found attributes
            Spinner theSpinner = (Spinner)mRootView.findViewById(R.id.spinner_attrEffects);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, theAttrEffectsGraph.mAttributes);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            theSpinner.setAdapter(adapter);
            whichIsSelectedAttrEffects();
            theSpinner.setOnTouchListener(mListener);
            theSpinner.setOnItemSelectedListener(mListener);*/
        } else {
            // sleep journal is disabled, so show no attribute-based graphs
            rl1.setVisibility(View.GONE);
            //rl2.setVisibility(View.GONE);
        }
    }

    // determine which radio button is already checked
    private void whichIsCheckedTrends() {
        TrendsGraphView theTrendsGraph = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theTrendsGraph.toggleAllOff();
        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_deep);
        if (rb.isChecked()) { theTrendsGraph.mShowDeep = true; }
        else {
            rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_rem);
            if (rb.isChecked()) { theTrendsGraph.mShowREM = true; }
            else {
                rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_light);
                if (rb.isChecked()) { theTrendsGraph.mShowLight = true; }
                else {
                    rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_awake);
                    if (rb.isChecked()) { theTrendsGraph.mShowAwake = true; }
                    else {
                        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_time2z);
                        if (rb.isChecked()) { theTrendsGraph.mShowTimeToZ = true; }
                        else {
                            rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_total);
                            if (rb.isChecked()) { theTrendsGraph.mShowTotalSleep = true; }
                            else {
                                rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_zq);
                                if (rb.isChecked()) { theTrendsGraph.mShowZQscore = true; }
                                else {
                                    rb = (RadioButton)mRootView.findViewById(R.id.radioButton_trends_deep);
                                    rb.setChecked(true);
                                    theTrendsGraph.mShowDeep = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // determine which check boxes and sliders are already set for the Attributes Heatmap
    private void whichIsCheckedAttrsHeatmap() {
        AttributesHeatmapGraphView theAttrsHeatmapGraph = (AttributesHeatmapGraphView)mRootView.findViewById(R.id.graph_attrsHeatmap);
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total_attrsHeatmap);
        theAttrsHeatmapGraph.mIncludeTotalSleep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep_attrsHeatmap);
        theAttrsHeatmapGraph.mIncludeDeep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem_attrsHeatmap);
        theAttrsHeatmapGraph.mIncludeREM = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake_attrsHeatmap);
        theAttrsHeatmapGraph.mIncludeAwake = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awakenings_attrsHeatmap);
        theAttrsHeatmapGraph.mIncludeAwakenings = cb.isChecked();

        SeekBar sb1 = (SeekBar)mRootView.findViewById(R.id.seekbar_attrsHeatmap);
        theAttrsHeatmapGraph.mGoodThresholdPct = (double)sb1.getProgress() / 100.0;
        SeekBar sb2 = (SeekBar)mRootView.findViewById(R.id.seekbar2_attrsHeatmap);
        theAttrsHeatmapGraph.mTimestampThresholdPct = (double)sb2.getProgress() / 100.0;
    }

    // determine which check boxes, radio buttons, and sliders are already set for the Days/Hours graph
    private void whichIsCheckedDaysHours() {
        DaysHoursGraphView theDaysHoursGraph = (DaysHoursGraphView)mRootView.findViewById(R.id.graph_dayHour);
        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_total_dayHour);
        theDaysHoursGraph.mIncludeTotalSleep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_deep_dayHour);
        theDaysHoursGraph.mIncludeDeep = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_rem_dayHour);
        theDaysHoursGraph.mIncludeREM = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awake_dayHour);
        theDaysHoursGraph.mIncludeAwake = cb.isChecked();
        cb = (CheckBox)mRootView.findViewById(R.id.checkBox_awakenings_dayHour);
        theDaysHoursGraph.mIncludeAwakenings = cb.isChecked();

        SeekBar sb2 = (SeekBar)mRootView.findViewById(R.id.seekbar2_dayHour);
        theDaysHoursGraph.mTimestampThresholdPct = (double)sb2.getProgress() / 100.0;

        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_dayHour_DOW);
        if (rb.isChecked()) { theDaysHoursGraph.mShowDays = true; }
        else { theDaysHoursGraph.mShowDays = false; }
    }

    // determine which radio button is already checked
    private void whichIsCheckedAttrEffects() {
        /*AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);
        theAttrEffectsGraph.toggleAllOff();
        RadioButton rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_deep);
        if (rb.isChecked()) { theAttrEffectsGraph.mShowDeep = true; }
        else {
            rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_rem);
            if (rb.isChecked()) { theAttrEffectsGraph.mShowREM = true; }
            else {
                rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_light);
                if (rb.isChecked()) { theAttrEffectsGraph.mShowLight = true; }
                else {
                    rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_awake);
                    if (rb.isChecked()) { theAttrEffectsGraph.mShowAwake = true; }
                    else {
                        rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_time2z);
                        if (rb.isChecked()) { theAttrEffectsGraph.mShowTimeToZ = true; }
                        else {
                            rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_total);
                            if (rb.isChecked()) { theAttrEffectsGraph.mShowTotalSleep = true; }
                            else {
                                rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_zq);
                                if (rb.isChecked()) { theAttrEffectsGraph.mShowZQscore = true; }
                                else {
                                    rb = (RadioButton)mRootView.findViewById(R.id.radioButton_attrEffects_deep);
                                    rb.setChecked(true);
                                    theAttrEffectsGraph.mShowDeep = true;
                                }
                            }
                        }
                    }
                }
            }
        }*/
    }

    // determine which radio button is already checked
    private void whichIsSelectedAttrEffects() {
        /*AttributeEffectsGraphView theAttrEffectsGraph = (AttributeEffectsGraphView)mRootView.findViewById(R.id.graph_attrEffects);
        Spinner theSpinner = (Spinner)mRootView.findViewById(R.id.spinner_attrEffects);
        int position = theSpinner.getSelectedItemPosition();
        theAttrEffectsGraph.toggleAttribute(theAttrEffectsGraph.mAttributes.get(position));*/
    }
}
