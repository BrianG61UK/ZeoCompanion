package opensource.zeocompanion.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.myzeo.android.api.data.ZeoDataContract;
import com.obscuredPreferences.ObscuredPrefs;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.HistoryDetailActivity;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionSleepEpisodeEventsParsedRec;
import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.views.DailyResultGraphView.DRG_dataSet;
import opensource.zeocompanion.views.DailyResultGraphView;
import opensource.zeocompanion.views.HypnogramView;

// fragment for the History Detail Activity that display all the content
public class HistoryDetailActivityFragment extends Fragment {
    // member variables
    private View mRootView = null;
    private HistoryDetailScrollHypnoDialogFragment mBigHypnoFrag = null;
    private boolean mShowAmended = false;
    private boolean mIsAmended = false;

    // member constants and other static content
    private static final String _CTAG = "HDF";
    SimpleDateFormat mDf1 = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
    SimpleDateFormat mDf1a = new SimpleDateFormat("MMM d, ''yy hh:mm a");
    SimpleDateFormat mDf2 = new SimpleDateFormat("hh:mm a");
    SimpleDateFormat mDf2s = new SimpleDateFormat("hh:mm:ss a");

    // listener for presses on the tableView cells for both Zeo and Amended datasets
    private TextView.OnClickListener mTextViewListener = new CheckBox.OnClickListener() {
        @Override
        public void onClick (View view) {
            if (view.getId() == R.id.textView_content22) {
                mShowAmended = false;
                CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_showAmended);
                cb.setChecked(false);
                //refreshDisplay(); // do not need to call this; the setChecked auto-triggers the checkbox changed listener
            } else if (view.getId() == R.id.textView_content23) {
                mShowAmended = true;
                CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_showAmended);
                cb.setChecked(true);
                //refreshDisplay(); // do not need to call this; the setChecked auto-triggers the checkbox changed listener
            }
        }
    };

    // listener for presses on the tableView cells for both Zeo and Amended datasets
    private TextView.OnLongClickListener mTextViewLongListener = new CheckBox.OnLongClickListener() {
        @Override
        public boolean onLongClick (View view) {
            if (view.getId() == R.id.textView_content23) {
                if (ZeoCompanionApplication.mIrec_HDAonly != null) {
                    boolean changed = ZeoCompanionApplication.mCoordinator.amendTheSleepRecord(ZeoCompanionApplication.mIrec_HDAonly, true);
                    if (changed && ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                        ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.saveToDB();
                        mShowAmended = true;
                        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_showAmended);
                        cb.setChecked(true);
                        //refreshDisplay(); // do not need to call this; the setChecked auto-triggers the checkbox changed listener
                    }
                }
            }
            return true;
        }
    };

    // listener for taps on the 30-second hypnogram
    private View.OnClickListener m30SecHypnoClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mBigHypnoFrag != null) {
                if (mBigHypnoFrag.isAdded()) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(mBigHypnoFrag);
                    ft.commit();
                }
                mBigHypnoFrag.show(getFragmentManager(), "DiagBHF");
            }
        }
    };

    // constructor
    public HistoryDetailActivityFragment() {}

    // instanciator
    public static HistoryDetailActivityFragment newInstance() {
        HistoryDetailActivityFragment fragment = new HistoryDetailActivityFragment();
        return fragment;
    }

    // called by the framework to create the fragment (typically used to reload passed Fragment parameters)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(_CTAG + ".onCreate", "==========FRAG ON-CREATE=====");
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {   // master Exception catcher
            //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
            // Inflate the layout for this fragment
            mRootView = inflater.inflate(R.layout.fragment_history_detail, container, false);
            if (mBigHypnoFrag == null) { mBigHypnoFrag = new HistoryDetailScrollHypnoDialogFragment(); }

            // if the ZSE or the CSE in the iRec are still active, then do a reload of them both because their contents may have changed
            // after the History Tab built its list of records
            boolean reloadNeeded = false;
            if (ZeoCompanionApplication.mIrec_HDAonly != null) {
                if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) {
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rEndReason == ZeoDataContract.SleepRecord.END_REASON_ACTIVE) {
                        reloadNeeded = true;
                    }
                }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                    if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.getStatusCode() != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_DONE &&
                            ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.getStatusCode() != CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_STATUSCODE_SOFTDONE) {
                        reloadNeeded = true;
                    }
                }
                if (reloadNeeded) { ZeoCompanionApplication.mCoordinator.refreshIntegratedHistoryRec(ZeoCompanionApplication.mIrec_HDAonly); }
            }

            // determine whether the record's CSE portion is amended and unpack the events and attributes
            mIsAmended = false;
            if (ZeoCompanionApplication.mIrec_HDAonly != null) {
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                    ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.unpackEventCSVstring();
                    ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.unpackInfoCSVstrings();
                    mIsAmended = ((ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0);
                }
            }

            // determine whether to initially show amended content (if present)
            if (ZeoCompanionApplication.mIrec_HDAonly != null) {
                if (mIsAmended) {
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord == null) {
                        mShowAmended = true;
                    } else {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        mShowAmended = prefs.getBoolean("history_detail_amended_showFirst", false);
                    }
                } else {
                    mShowAmended = false;
                }
            }

            // setup the checkbox to toggle between Zeo and amended views;
            // setup a listener for presses of the checkbox
            CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_showAmended);
            if (mIsAmended) {
                cb.setVisibility(View.VISIBLE);
            } else {
                cb.setVisibility(View.INVISIBLE);
            }
            cb.setChecked(mShowAmended);
            cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mShowAmended = isChecked;
                    refreshDisplay();
                }
            });
        } catch (Exception e) {
            String eMsg = "For";
            if (ZeoCompanionApplication.mIrec_HDAonly == null) { eMsg = eMsg + " iRec=null"; }
            else {
                if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord == null) { eMsg = eMsg + " ZSE=null"; }
                else { eMsg = eMsg + " ZSE ID=" + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rSleepEpisodeID; }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord == null) { eMsg = eMsg + " CSE=null"; }
                else { eMsg = eMsg + " CSE ID=" + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rID; }
            }
            ZeoCompanionApplication.postToErrorLog(_CTAG+".onCreateView", e, eMsg);
            ((HistoryDetailActivity)getActivity()).goBack();
        }

        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView () {
        mBigHypnoFrag = null;
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        super.onDestroyView();
    }

    // Called when the fragment's activity has been created and this fragment's view hierarchy instantiated
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onActivityCreated", "==========FRAG ON-ACTIVITYCREATED=====");
        super.onActivityCreated(savedInstanceState);
        refreshDisplay();
    }

    // create all the Fragment's details
    public void refreshDisplay() {
        try {   // master Exception catcher
            // obtain the screen size in its current orientation
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getSize(screenSize);

            // obtain handles to all the root view's components
            TextView theDate = (TextView)mRootView.findViewById(R.id.TextView_date);
            TextView theHeaderInfo = (TextView)mRootView.findViewById(R.id.TextView_header_info);
            HypnogramView theHypno1_graph = (HypnogramView)mRootView.findViewById(R.id.graph_hypnogram_summary);
            HypnogramView theHypno2_graph = (HypnogramView)mRootView.findViewById(R.id.graph_hypnogram_detailed);
            TextView theHypno2_tv = (TextView)mRootView.findViewById(R.id.textView_hypnogram_detailed);
            DailyResultGraphView theDailyResults = (DailyResultGraphView)mRootView.findViewById(R.id.graph_dailyResult);
            TextView theDailyResults_tv = (TextView)mRootView.findViewById(R.id.textView_dailyResult);
            TextView theJournalSummary = (TextView)mRootView.findViewById(R.id.textView_summary);

            TextView tvh1 = (TextView)mRootView.findViewById(R.id.textView_header1);
            TextView tvh2 = (TextView)mRootView.findViewById(R.id.textView_header2);
            TextView tvh3 = (TextView)mRootView.findViewById(R.id.textView_header3);

            TextView tvc21 = (TextView)mRootView.findViewById(R.id.textView_content21);
            TextView tvc22 = (TextView)mRootView.findViewById(R.id.textView_content22);
            TextView tvc23 = (TextView)mRootView.findViewById(R.id.textView_content23);

            TextView tvc31 = (TextView)mRootView.findViewById(R.id.textView_content31);
            TextView tvc32 = (TextView)mRootView.findViewById(R.id.textView_content32);
            TextView tvc33 = (TextView)mRootView.findViewById(R.id.textView_content33);

            // if somehow we've been given a bad record, then show nothing
            if (ZeoCompanionApplication.mIrec_HDAonly == null) {
                theDate.setText("INTERNAL ERROR: no record passed to this view");
                theHypno1_graph.setVisibility(View.INVISIBLE);
                theHypno2_graph.setVisibility(View.INVISIBLE);
                theHypno2_tv.setVisibility(View.INVISIBLE);
                theDailyResults.setVisibility(View.INVISIBLE);
                theDailyResults_tv.setVisibility(View.INVISIBLE);
                tvh2.setVisibility(View.INVISIBLE);
                tvh3.setVisibility(View.INVISIBLE);
                tvc22.setVisibility(View.INVISIBLE);
                tvc23.setVisibility(View.INVISIBLE);
                tvc32.setVisibility(View.INVISIBLE);
                tvc33.setVisibility(View.INVISIBLE);
                theJournalSummary.setVisibility(View.INVISIBLE);
                return;
            }

            // change background colorations and listeners depending upon amended and show amended states
            if (mShowAmended && mIsAmended) {
                tvc22.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2));
                tvc23.setBackgroundColor(getResources().getColor(R.color.colorDeepPurple));
                tvc22.setOnClickListener(mTextViewListener);
                tvc23.setOnClickListener(mTextViewListener);
            } else if (mIsAmended) {
                tvc22.setBackgroundColor(getResources().getColor(R.color.colorDeepPurple));
                tvc23.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2));
                tvc22.setOnClickListener(mTextViewListener);
                tvc23.setOnClickListener(mTextViewListener);
            } else {
                tvc22.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2));
                tvc23.setBackgroundColor(getResources().getColor(R.color.colorOffBlack2));
                tvc22.setOnClickListener(null);
                tvc23.setOnClickListener(null);
            }
            tvc23.setOnLongClickListener(mTextViewLongListener);

            // get user's sleep goals (if any)
            double goalTotalSleepMin = 480.0;
            double goalDeepPct = 15.0;
            double goalREMpct = 20.0;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String wStr = ObscuredPrefs.decryptString(prefs.getString("profile_goal_hours_per_night", "8"));
            if (!wStr.isEmpty()) {
                double d = Double.parseDouble(wStr);
                if (d > 0.0) { goalTotalSleepMin = d * 60.0; }
            }
            wStr = ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_deep", "15"));
            if (!wStr.isEmpty()) {
                double d = Double.parseDouble(wStr);
                if (d > 0.0 && d <= 100.0) { goalDeepPct = d;  }
            }
            wStr  = ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_REM", "20"));
            if (!wStr.isEmpty()) {
                double d = Double.parseDouble(wStr);
                if (d > 0.0 && d <= 100.0) { goalREMpct = d;  }
            }

            // calculate the time offset the ZeoApp applied to the headband's data
            long clockOffset = 0;
            if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) {
                clockOffset = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight - ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNightOrig;
            }

            // set the date TextView
            if (screenSize.x < 600) { theDate.setText(mDf1a.format(new Date(ZeoCompanionApplication.mIrec_HDAonly.mTimestamp))); }
            else { theDate.setText(mDf1.format(new Date(ZeoCompanionApplication.mIrec_HDAonly.mTimestamp))); }

            // set any header info TextView
            if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null && ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                CompanionSleepEpisodeEventsParsedRec eRec = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.getEventOldest(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_INBED, CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_ZEO_RECORDING);
                if (eRec != null) {
                    long delta = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight - eRec.rTimestamp;
                    Log.d(_CTAG+".refreshDisplay","delta="+delta);
                    if (delta > 15000 || delta < -15000) {
                        String msg = "HB clock maybe out-of-sync";
                        if (screenSize.x / ZeoCompanionApplication.mScreenDensity >= 1024) {
                            if (delta > 0) { msg = "Warning: " + msg + " by " + ((delta-5000)/1000) + " sec";  }
                            else { msg = "Warning: " + msg + " by " + ((delta+5000)/1000) + " sec";  }
                        }
                        theHeaderInfo.setText(msg);
                        theHeaderInfo.setTextColor(Color.YELLOW);
                    }
                }
            }

            // prepare for showing the hypnograms
            int showAsEpoch = 30;
            if (screenSize.x < 750) {
                showAsEpoch = 90;
                theHypno2_tv.setText("90 sec resolution Hypnogram; click to expand to 30 sec:");
            } else if (screenSize.x < 1500) {
                showAsEpoch = 60;
                theHypno2_tv.setText("60 sec resolution Hypnogram; click to expand to 30 sec:");
            }

            // display the hypnograms
            switch (showWhich()) {
                default:
                    // neither
                    theHypno1_graph.setVisibility(View.GONE);
                    theHypno2_graph.setVisibility(View.GONE);
                    theHypno2_tv.setVisibility(View.GONE);
                    theHypno2_graph.setOnClickListener(null);
                    break;
                case 1:
                    // Zeo record
                    Log.d(_CTAG+".refreshDisplay","HB DispStart="+mDf2s.format(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime-clockOffset)+", RecStart="+mDf2s.format(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNightOrig)+", clockOffsetSec="+((double)clockOffset/1000.0));
                    Log.d(_CTAG+".refreshDisplay","APP DispStart="+mDf2s.format(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime)+", RecStart="+mDf2s.format(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight));
                    theHypno1_graph.setVisibility(View.VISIBLE);
                    theHypno2_graph.setVisibility(View.VISIBLE);
                    theHypno2_tv.setVisibility(View.VISIBLE);
                    theHypno1_graph.showAsDetailed();
                    long displayStart1 = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight;
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.mHasExtended && ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime > 0) {
                        displayStart1 = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime;
                    }
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplay_Hypnogram_Count > 1) {
                        if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                            theHypno1_graph.setDataset(displayStart1, 300, 300, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplay_Hypnogram, true, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
                        } else {
                            theHypno1_graph.setDataset(displayStart1, 300, 300, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplay_Hypnogram, true, null);
                        }
                    }
                    theHypno2_graph.showAsDetailed();
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rBase_Hypnogram_Count > 1) {
                        if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                            theHypno2_graph.setDataset(displayStart1, 30, showAsEpoch, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rBase_Hypnogram, true, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
                        } else {
                            theHypno2_graph.setDataset(displayStart1, 30, showAsEpoch, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rBase_Hypnogram, true, null);
                        }
                        theHypno2_graph.setOnClickListener(m30SecHypnoClickListener);
                    }
                    break;
                case 2:
                    // CSE record
                    theHypno1_graph.setVisibility(View.VISIBLE);
                    theHypno2_graph.setVisibility(View.VISIBLE);
                    theHypno2_tv.setVisibility(View.VISIBLE);
                    theHypno1_graph.showAsDetailed();
                    long displayStart2 = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_StartOfNight;
                    if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Display_Hypnogram_Starttime > 0) {
                        displayStart2 = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Display_Hypnogram_Starttime;
                    }
                    if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Display_Hypnogram.length > 1) {
                        theHypno1_graph.setDataset(displayStart2, 300, 300, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Display_Hypnogram, true, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
                    }
                    theHypno2_graph.showAsDetailed();
                    if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Base_Hypnogram.length > 1) {
                        theHypno2_graph.setDataset(displayStart2, 30, showAsEpoch, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Base_Hypnogram, true, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
                        theHypno2_graph.setOnClickListener(m30SecHypnoClickListener);
                    }
                    break;
            }

            // now compose and prepare the Zeo vs Amended table
            tvh1.setVisibility(View.VISIBLE);
            tvc21.setVisibility(View.VISIBLE);
            tvc21.setText("From/To:\nZQ:\n#Awakens:\nTotal Duration:\nRecording Duration:\n  Still awake until:\n  Awake (%total):\n  Total sleep (%goal):\n    REM (%total): \n    Light (total):\n    Deep (%total):\nHB Battery:");
            if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) {
                tvh2.setVisibility(View.VISIBLE);
                tvc22.setVisibility(View.VISIBLE);

                double pctSleep = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min / goalTotalSleepMin * 100.0;
                double pctAwake = 0.0;
                double pctREM = 0.0;
                double pctLight = 0.0;
                double pctDeep = 0.0;
                if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min > 0.0) {
                    pctAwake = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min / (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min) * 100.0;
                    pctREM = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_REM_min / ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;
                    pctLight = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Light_min / ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;
                    pctDeep = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Deep_min / ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;
                }

                long stillAwake = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight + (((long)ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_to_Z_min) * 60000);
                double totalDurMin = (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rEndOfNight - ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight) / 60000;
                double recDurMin = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_to_Z_min;

                tvh2.setText("Zeo-ID# " + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rSleepEpisodeID + " (" + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.getStatusString() + ")");

                String str =    mDf2.format(new Date(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight)) + " to " + mDf2.format(new Date(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rEndOfNight)) + "\n";
                str = str +     ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rZQ_Score + "\n";
                str = str +     ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rCountAwakenings + "\n";
                str = str +     Utilities.showTimeInterval(totalDurMin, true) + "\n";
                str = str +     Utilities.showTimeInterval(recDurMin, true) + "\n";
                str = str +     "  " + mDf2.format(new Date(stillAwake)) + " (" + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_to_Z_min, true) + ")\n";
                str = str +     "  " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min, true) + " (" + String.format("%.1f", pctAwake) + "%)\n";
                str = str +     "  " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min, true) + " (" + String.format("%.1f", pctSleep) + "%)\n";
                str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_REM_min, true) + " (" + String.format("%.1f", pctREM) + "%)\n";
                str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Light_min, true) + " (" + String.format("%.1f", pctLight) + "%)\n";
                str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Deep_min, true) + " (" + String.format("%.1f", pctDeep) + "%)\n";
                if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.mHasExtended) {
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rVoltageBattery > 0) {
                        if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rVoltageBattery > 150) { str = str + "Start: "; }
                        else { str = str + "End: "; }
                        str = str + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rVoltageBattery;
                    }
                }
                tvc22.setText(str);
                tvc32.setVisibility(View.INVISIBLE);
            } else {
                tvh2.setVisibility(View.INVISIBLE);
                tvc22.setVisibility(View.INVISIBLE);
                tvc32.setVisibility(View.INVISIBLE);
            }
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                tvh3.setVisibility(View.VISIBLE);
                tvc23.setVisibility(View.VISIBLE);
                tvc33.setVisibility(View.VISIBLE);

                ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.unpackEventCSVstring();
                ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.unpackInfoCSVstrings();

                double totalDurMin = 0.0;
                double recDurMin = 0.0;
                long starting = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rEvent_GotIntoBed_Timestamp;
                if (starting == 0) { starting = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rEvent_TryingToSleep_Timestamp; }
                if (starting == 0 && ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) { starting = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rStartOfNight; }
                long ending = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rEvent_OutOfBedDoneSleeping_Timestamp;
                if (ending == 0 && ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) { ending = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rEndOfNight; }
                if (starting > 0 && ending > 0) { totalDurMin = (double)((ending / 60000) - (starting / 60000)); }
                if (mIsAmended) {
                    recDurMin = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_to_Z_min;
                }

                String str =    "Journal-ID# "+ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rID;
                str = str + " ("+ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.getStatusString()+")";
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoSleepEpisode_ID != 0) {
                    str = str + "\n  linked Zeo-ID# " + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoSleepEpisode_ID;
                    if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord == null) { str = str + " (missing)"; }
                }
                tvh3.setText(str);

                str = "";
                if (starting > 0 && ending > 0) {
                    str = str + mDf2.format(new Date(starting)) + " to " + mDf2.format(new Date(ending)) + "\n";
                } else if (ending > 0) {
                    str = str + "Ending: " + mDf2.format(new Date(ending)) + "\n";
                } else if (starting > 0) {
                    str = str + "Starting: " + mDf2.format(new Date(starting)) + "\n";
                }
                if (mIsAmended) { str = str + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_ZQ_Score + "\n"; }
                else { str = str + "\n"; }
                if (mIsAmended) { str = str + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_CountAwakenings + "\n"; }
                else { str = str + "\n"; }
                if (totalDurMin > 0) { str = str + Utilities.showTimeInterval(totalDurMin, true) + "\n"; }
                else { str = str + "\n"; }
                if (recDurMin > 0) { str = str + Utilities.showTimeInterval(recDurMin, true) + "\n"; }
                else { str = str + "\n"; }
                if (mIsAmended) {
                    long stillAwake = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_StartOfNight + (((long)ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_to_Z_min) * 60000);
                    str = str + "  " + mDf2.format(new Date(stillAwake)) + " (" + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_to_Z_min, true) + ")\n";
                } else { str = str + "\n"; }
                if (mIsAmended) {
                    double pctSleep = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min / goalTotalSleepMin * 100.0;
                    double pctAwake = 0.0;
                    double pctREM = 0.0;
                    double pctLight = 0.0;
                    double pctDeep = 0.0;
                    if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min > 0.0) {
                        pctAwake = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min / (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min) * 100.0;
                        pctREM = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_REM_min / ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min * 100.0;
                        pctLight = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Light_min / ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min * 100.0;
                        pctDeep = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Deep_min / ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min * 100.0;
                    }

                    str = str +     "  " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min, true) + " (" + String.format("%.1f", pctAwake) + "%)\n";
                    str = str +     "  " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min, true) + " (" + String.format("%.1f", pctSleep) + "%)\n";
                    str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_REM_min, true) + " (" + String.format("%.1f", pctREM) + "%)\n";
                    str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Light_min, true) + " (" + String.format("%.1f", pctLight) + "%)\n";
                    str = str +     "    " + Utilities.showTimeInterval(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Deep_min, true) + " (" + String.format("%.1f", pctDeep) + "%)\n";
                } else { str = str + "\n\n\n\n\n"; }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoHeadbandBattery_High > 0) {
                    str = str + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoHeadbandBattery_High;
                }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoHeadbandBattery_Low > 0) {
                    str = str + " to " + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rZeoHeadbandBattery_Low;
                }
                tvc23.setText(str);

                str = "";
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array != null) {
                    str = str + "#Events: " + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array.size();
                }
                int c = 0;
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mAttribs_Fixed_array != null) {
                    for (int p = 0; p < ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mAttribs_Fixed_array.size(); p++) {
                        if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mAttribs_Fixed_array.get(p) != null) { c++; }
                    }
                }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mAttribs_Vari_array != null) {
                    c = c + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mAttribs_Vari_array.size();
                }
                if (c > 0) {
                    if (str.length() > 0) { str = str + ", "; }
                    str = str + "#Attributes: " + c;
                }
                tvc33.setText(str);
            } else {
                tvh3.setVisibility(View.INVISIBLE);
                tvc23.setVisibility(View.INVISIBLE);
                tvc33.setVisibility(View.INVISIBLE);
            }

            // compose and show the daily results graph
            ArrayList<DRG_dataSet> dailyData = new ArrayList<DRG_dataSet>();
            switch (showWhich()) {
                default:
                    // neither
                    theDailyResults.setVisibility(View.GONE);
                    break;
                case 1:
                    // Zeo record
                    theDailyResults.setVisibility(View.VISIBLE);
                    dailyData.add(new DRG_dataSet("Total", ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min, goalTotalSleepMin, goalTotalSleepMin, 100.0));
                    dailyData.add(new DRG_dataSet("Awake", ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min, (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min), 0, 0.00));
                    dailyData.add(new DRG_dataSet("REM", ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_REM_min, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min, 0, goalREMpct));
                    dailyData.add(new DRG_dataSet("Light", ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Light_min, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min, 0, 0.0));
                    dailyData.add(new DRG_dataSet("Deep", ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Deep_min, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min, 0, goalDeepPct));
                    theDailyResults.setDataset(dailyData);
                    break;
                case 2:
                    // CSE record
                    theDailyResults.setVisibility(View.VISIBLE);
                    dailyData.add(new DRG_dataSet("Total", ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min, goalTotalSleepMin, goalTotalSleepMin, 100.0));
                    dailyData.add(new DRG_dataSet("Awake", ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min, (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min), 0, 0.00));
                    dailyData.add(new DRG_dataSet("REM", ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_REM_min, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min, 0, goalREMpct));
                    dailyData.add(new DRG_dataSet("Light", ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Light_min, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min, 0, 0.0));
                    dailyData.add(new DRG_dataSet("Deep", ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Deep_min, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min, 0, goalDeepPct));
                    theDailyResults.setDataset(dailyData);
                    break;
            }

            // compose and show the daily results textual summary
            String drStr = "";
            double deltaTotal = 0.0;
            double deltaDeep = 0.0;
            double deltaREM = 0.0;
            double goalDeepMin = 0.0;
            double goalREMmin = 0.0;
            double shouldHaveTotalZ = 0.0;
            switch (showWhich()) {
                default:
                    // neither
                    break;
                case 1:
                    // Zeo record
                    drStr = drStr + "Deltas from Goals:\n";
                    deltaTotal = goalTotalSleepMin - ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min;

                    shouldHaveTotalZ = ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Awake_min;
                    goalDeepMin = goalDeepPct / 100.0 * shouldHaveTotalZ;
                    deltaDeep = goalDeepMin - ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_Deep_min;

                    goalREMmin = goalREMpct / 100.0 * shouldHaveTotalZ;
                    deltaREM = goalREMmin - ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rTime_REM_min;
                    break;
                case 2:
                    // CSE record
                    drStr = drStr + "Deltas from Goals:\n";
                    deltaTotal = goalTotalSleepMin - ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min;

                    shouldHaveTotalZ = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Total_Z_min + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Awake_min;
                    goalDeepMin = goalDeepPct / 100.0 * shouldHaveTotalZ;
                    deltaDeep = goalDeepMin - ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_Deep_min;

                    goalREMmin = goalREMpct / 100.0 * shouldHaveTotalZ;
                    deltaREM = goalREMmin - ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Time_REM_min;
                    break;
            }

            if (deltaTotal < 0.0) { drStr = drStr + "  Total: "+String.format("%.0f", -deltaTotal) + "m > goal "+Utilities.showTimeInterval(goalTotalSleepMin, false)+"\n"; }
            else if (deltaTotal > 0.0) { drStr = drStr + "  Total: "+String.format("%.0f", deltaTotal) + "m < goal "+Utilities.showTimeInterval(goalTotalSleepMin, false)+"\n"; }
            if (deltaDeep < 0.0) { drStr = drStr +   "  Deep:  "+String.format("%.0f", -deltaDeep) + "m > goal "+String.format("%.0f", goalDeepMin)+"m\n"; }
            else if (deltaDeep > 0.0) { drStr = drStr + "  Deep:  "+String.format("%.0f", deltaDeep) + "m < goal "+String.format("%.0f", goalDeepMin)+"m\n"; }
            if (deltaREM < 0.0) { drStr = drStr + "  REM:   "+String.format("%.0f", -deltaREM) + "m > goal "+String.format("%.0f", goalREMmin)+"m\n"; }
            else if (deltaREM > 0.0) { drStr = drStr + "  REM:   "+String.format("%.0f", deltaREM) + "m < goal "+String.format("%.0f", goalREMmin)+"m\n"; }

            // include end-of-sleep attributes if available
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                ArrayList<String> afterAttribs = ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.getAttribsSummaryStrings(CompanionDatabaseContract.SLEEP_EPISODE_STAGE_AFTER);
                if (afterAttribs.size() > 0) {
                    drStr = drStr + "Morning Results:\n";
                    for (String aStr : afterAttribs) {
                        drStr = drStr + "  " + aStr + "\n";
                    }
                }
            }
            if (!drStr.isEmpty()) { theDailyResults_tv.setText(drStr); }

            // compose and show the sleep journal summary
            String summText = "";
            theJournalSummary.setVisibility(View.VISIBLE);
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                ArrayList<String> summaryText = new ArrayList<String>();
                ZeoCompanionApplication.mCoordinator.createSummaryList(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord, summaryText);
                for (String string: summaryText) {
                    summText = summText + string + "\n";
                }
            }
            if (!summText.isEmpty()) { theJournalSummary.setText(summText); }
        } catch (Exception e) {
            String eMsg = "For";
            if (ZeoCompanionApplication.mIrec_HDAonly == null) { eMsg = eMsg + " iRec=null"; }
            else {
                if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord == null) { eMsg = eMsg + " ZSE=null"; }
                else { eMsg = eMsg + " ZSE ID=" + ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rSleepEpisodeID; }
                if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord == null) { eMsg = eMsg + " CSE=null"; }
                else { eMsg = eMsg + " CSE ID=" + ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rID; }
            }
            ZeoCompanionApplication.postToErrorLog(_CTAG+".refreshDisplay", e, eMsg);
            ((HistoryDetailActivity)getActivity()).goBack();
        }
    }

    // determines what to show (1=Zeo, 2=CSE, or 0=neither)
    private int showWhich() {
        if (mShowAmended && mIsAmended) {
            // should show the CSE record
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) { return 2; }
            if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) { return 1; }
        } else {
            // should show the Zeo record
            if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) { return 1; }
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null && mIsAmended) { return 2; }
        }
        return 0;
    }
}
