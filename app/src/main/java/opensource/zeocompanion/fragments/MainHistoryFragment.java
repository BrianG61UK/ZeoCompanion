package opensource.zeocompanion.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.obscuredPreferences.ObscuredPrefs;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.activities.HistoryDetailActivity;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.views.HypnogramView;
import opensource.zeocompanion.utility.Utilities;

// fragment within the MainActivity that shows an integrated history of all ZeoApp and ZeoCompanion sleep records
public class MainHistoryFragment extends MainFragmentWrapper {
    // member variables
    private View mRootView = null;
    private boolean mShowAmended = false;
    private boolean mShowTextDetails = true;
    private boolean mAnyAmended = false;
    private int mHypnogramWidth = 0;
    private int mHypnogramHeight = 0;
    private double mGoalTotalSleepMin = 480.0;
    private double mGoalDeepPct = 15.0;
    private double mGoalREMpct = 20.0;
    private Point mScreenSize = null;
    private ListView mListView = null;
    private ZAHSR_Adapter mListView_Adapter = null;
    private ArrayList<JournalDataCoordinator.IntegratedHistoryRec> mListView_List = null;

    // member constants and other static content
    private static final String _CTAG = "MHF";

    // listener for preference changes
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals("main_history_showTextView") ||
                    key.equals("profile_goal_hours_per_night") ||
                    key.equals("profile_goal_percent_deep") ||
                    key.equals("profile_goal_percent_REM")) {
                Log.d(_CTAG+".prefChgListen","History Tab preferences have changed");
                needToRefresh();
            }
        }
    };

    // constructor
    public MainHistoryFragment() {
    }

    // instanciator
    public static MainHistoryFragment newInstance() {
        return new MainHistoryFragment();
    }

    // called by the framework to create the Fragment's view contents
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onCreateView", "==========FRAG ON-CREATEVIEW=====");
        mRootView = inflater.inflate(R.layout.fragment_main_history, container, false);

        // obtain the screen size in its current orientation
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        mScreenSize = new Point();
        display.getSize(mScreenSize);

        mListView_List = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        loadListViewList();

        mListView = (ListView) mRootView.findViewById(R.id.listView_history);
        mListView_Adapter = new ZAHSR_Adapter(getActivity(), R.layout.fragment_main_history_row, mListView_List);
        mListView.setAdapter(mListView_Adapter);

        // add a blank footer to deal with problems with last row not showing
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.fragment_main_history_footer, mListView, false);
        mListView.addFooterView(footer, null, false);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ZeoCompanionApplication.mIrec_HDAonly = mListView_List.get(position);
                Intent intent = new Intent(getContext(), HistoryDetailActivity.class);
                startActivity(intent);
            }
        });

        final ImageView imgView = (ImageView) mRootView.findViewById(R.id.hiderowImageView_hypnogram);
        imgView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    imgView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    imgView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mHypnogramWidth = imgView.getWidth();
                mHypnogramHeight = imgView.getHeight();
                mListView_Adapter.notifyDataSetChanged();
            }
        });

        // get user's sleep goals (if any)
        loadUserPrefs();

        // setup the checkbox to show amended
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_showAmended);
        if (mAnyAmended) {
            cb.setVisibility(View.VISIBLE);
            mShowAmended = prefs.getBoolean("main_history_amended_showFirst", false);    // this preference does not need to be reloaded upon change
            cb.setChecked(mShowAmended);
            cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mShowAmended = isChecked;
                    mListView_Adapter.clearAllBitmaps();
                    mListView_Adapter.notifyDataSetChanged();
                }
            });
        } else {
            cb.setVisibility(View.GONE);
        }

        // listen for changes in the preferences applicable to this tab
        prefs.registerOnSharedPreferenceChangeListener(mPrefsListener);
        return mRootView;
    }

    // Called by the framework when the fragment's view has been detached from the fragment (counterpart to onCreateView)
    @Override
    public void onDestroyView() {
        //Log.d(_CTAG + ".onDestroyView", "==========FRAG ON-DESTROYVIEW=====");
        // because the ArrayList for the ListView contains large Bitmaps, explicitly release them and other records to help out standard garbage collection process
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.unregisterOnSharedPreferenceChangeListener(mPrefsListener);

        for (JournalDataCoordinator.IntegratedHistoryRec iRec : mListView_List) { iRec.destroy(); }
        mListView_Adapter.clear();
        mListView_List.clear();
        mListView_List = null;
        mListView.setAdapter(null);
        mListView_Adapter = null;
        mListView = null;

        super.onDestroyView();
    }

    // create a bitmap of a hypnogram; called by the ListView Adapter's getView as needed
    private void createHypnogramBitmap(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean isAmended) {
        if (iRec == null) { return; }
        if (iRec.theHypnogramBitmap != null) {
            iRec.theHypnogramBitmap.recycle();
            iRec.theHypnogramBitmap = null;
        }
        if (mHypnogramWidth == 0 || mHypnogramHeight == 0) { return; }
        if (iRec.theZAH_SleepRecord == null && iRec.theCSErecord == null) { return; }

        int showWhat = showWhich(iRec, isAmended);
        if (showWhat == 0) { return; }

        HypnogramView theHypno = new HypnogramView(getContext());
        theHypno.showAsCompact();
        theHypno.prepDrawToCanvas(mHypnogramWidth, mHypnogramHeight);
        Bitmap b = Bitmap.createBitmap(mHypnogramWidth, mHypnogramHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        switch (showWhat) {
            case 1:
                // Zeo hypnogram
                long displayStart1 = iRec.theZAH_SleepRecord.rStartOfNight;
                if (iRec.theZAH_SleepRecord.mHasExtended && iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime > 0) {
                    displayStart1 = iRec.theZAH_SleepRecord.rDisplayHypnogramStartTime;
                }
                if (iRec.theZAH_SleepRecord.rDisplay_Hypnogram_Count > 1) {
                    theHypno.setDataset(displayStart1, 300, 300, iRec.theZAH_SleepRecord.rDisplay_Hypnogram, true, null);
                }
                break;
            case 2:
                // CSE hypnogram
                long displayStart2 = iRec.theCSErecord.rAmend_StartOfNight;
                if (iRec.theCSErecord.rAmend_Display_Hypnogram_Starttime > 0) {
                    displayStart2 = iRec.theCSErecord.rAmend_Display_Hypnogram_Starttime;
                }
                if (iRec.theCSErecord.rAmend_Display_Hypnogram.length > 1) {
                    theHypno.setDataset(displayStart2, 300, 300, iRec.theCSErecord.rAmend_Display_Hypnogram, true, null);
                }
                break;
        }

        theHypno.doDraw(c);
        iRec.theHypnogramBitmap = b;
    }

    // determines what to show (1=Zeo, 2=CSE, or 0=neither)
    private int showWhich(JournalDataCoordinator.IntegratedHistoryRec iRec, boolean isAmended) {
        if (mShowAmended && isAmended) {
            // should show the CSE record
            if (iRec.theCSErecord != null) { return 2; }
            if (iRec.theZAH_SleepRecord != null) { return 1; }
        } else {
            // should show the Zeo record
            if (iRec.theZAH_SleepRecord != null) { return 1; }
            if (iRec.theCSErecord != null && isAmended) { return 2; }
        }
        return 0;
    }

    // called by the Activity to a specific Fragment when it becomes actually shown
    @Override
    public void fragmentBecameShown() {
        if ((ZeoCompanionApplication.mFirstTimeHintsShown & ZeoCompanionApplication.APP_HINTS_HISTORY_FRAGMENT) == 0) {
            ZeoCompanionApplication.hintShown(ZeoCompanionApplication.APP_HINTS_HISTORY_FRAGMENT);
            Utilities.showAlertDialog(getContext(), "Hint", "Hint: You can remove specific tabs or the entire journal feature, plus choose the first shown tab in the drop-down Settings under Journal.\n\nClick a row for expanded details.", "Okay");
        }
    }

    // called by the Activity when handlers or settings have made changes to the database
    // or to settings options, etc
    @Override
    public void needToRefresh() {
        loadUserPrefs();
        for (JournalDataCoordinator.IntegratedHistoryRec iRec : mListView_List) { iRec.destroy(); }
        mListView_List.clear();
        loadListViewList();
        mListView_Adapter.clearAllBitmaps();
        mListView_Adapter.notifyDataSetChanged();
        CheckBox cb = (CheckBox) mRootView.findViewById(R.id.checkBox_showAmended);
        if (mAnyAmended) { cb.setVisibility(View.VISIBLE); }
        else { cb.setVisibility(View.GONE); }
    }

    // load all the to-be-shown IntegratedHistoryRecs then check the results
    private void loadListViewList() {
        ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecs(mListView_List);

        mAnyAmended = false;
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: mListView_List) {
            if (iRec.theCSErecord != null) {
                boolean needSave = ZeoCompanionApplication.mCoordinator.amendTheSleepRecord(iRec, false);
                if (needSave) { iRec.theCSErecord.saveToDB(); }
                if ((iRec.theCSErecord.rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0) { mAnyAmended = true; }
            }
        }
    }

    // load the current state of applicable preferences
    private void loadUserPrefs() {
        // get user's preferences and sleep goals (if any)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mShowTextDetails = prefs.getBoolean("main_history_showTextView",true);
        String str = ObscuredPrefs.decryptString(prefs.getString("profile_goal_hours_per_night", "8"));
        if(!str.isEmpty()) {
            double d = Double.parseDouble(str);
            if (d > 0.0) { mGoalTotalSleepMin = d * 60.0; }
        }

        str=ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_deep","15"));
        if(!str.isEmpty()) {
            double d = Double.parseDouble(str);
            if (d > 0.0 && d <= 100.0) { mGoalDeepPct = d; }
        }

        str=ObscuredPrefs.decryptString(prefs.getString("profile_goal_percent_REM","20"));
        if(!str.isEmpty()) {
            double d = Double.parseDouble(str);
            if (d > 0.0 && d <= 100.0) { mGoalREMpct = d; }
        }
    }

    // ViewHolder class to hold all the child views within a row to speed up processing
    private static class ZAHSR_Adapter_ViewHolder {
        TextView theDate;
        TextView theZQ;
        TextView theAddtl;
        TextView theSum;
        ImageView theHypno;
    }

    // common simple date format used in all the rows
    private static SimpleDateFormat mZAHSR_Adapter_dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm a");
    private static SimpleDateFormat mZAHSR_Adapter_dateFormat_small = new SimpleDateFormat("MMM d, ''yy hh:mm a");

    // ListView adaptor specific to this Fragment;
    // the adaptor utilizes IntegratedHistoryRec as its list entries
    class ZAHSR_Adapter extends ArrayAdapter {
        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<JournalDataCoordinator.IntegratedHistoryRec> mArrayList = null;

        // constructor
        public ZAHSR_Adapter(Context context, int layoutResourceId, ArrayList<JournalDataCoordinator.IntegratedHistoryRec> list) {
            super(context, layoutResourceId, list);
            mLayoutResourceId = layoutResourceId;
            mContext = context;
            mArrayList = list;
        }

        // populate a row View; these views ARE recycled; cannot presume that initial contents from the XML are still present
        // since this is a large ListViews a ViewHolder Tag is used
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // first construct the row's template; remember this view can be recycled containing data from another entry
            ZAHSR_Adapter_ViewHolder viewHolder = null;
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                rowView = inflater.inflate(mLayoutResourceId, parent, false);

                viewHolder = new ZAHSR_Adapter_ViewHolder();
                viewHolder.theDate = (TextView)rowView.findViewById(R.id.rowTextViewDate);
                viewHolder.theZQ = (TextView)rowView.findViewById(R.id.rowTextViewZQ);
                viewHolder.theAddtl = (TextView)rowView.findViewById(R.id.rowTextViewAddtl);
                viewHolder.theSum = (TextView)rowView.findViewById(R.id.rowTextViewSum);
                viewHolder.theHypno = (ImageView)rowView.findViewById(R.id.rowImageView_hypnogram);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ZAHSR_Adapter_ViewHolder)rowView.getTag();
            }

            // now properly configure the row's data and attributes
            JournalDataCoordinator.IntegratedHistoryRec iRec = mArrayList.get(position);
            if (mScreenSize.x < 600) { viewHolder.theDate.setText(mZAHSR_Adapter_dateFormat_small.format(new Date(iRec.mTimestamp))); }
            else { viewHolder.theDate.setText(mZAHSR_Adapter_dateFormat.format(new Date(iRec.mTimestamp))); }

            boolean isAmended = false;
            if (iRec.theCSErecord != null) {
                isAmended = ((iRec.theCSErecord.rAmendedFlags & CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_AMENDED_FLAGS_AMENDED) != 0);
            }

            if (mShowTextDetails) {
                double pctDeep, pctREM, pctAwake;
                SpannableString totalSleep, deepSleep, remSleep, awakeSleep, cntAwaken;
                SpannableStringBuilder builder;
                int showWhat = showWhich(iRec, isAmended);
                switch (showWhat) {
                    case 1:
                        // zeo sleep record data
                        viewHolder.theZQ.setText(String.format("%02d", iRec.theZAH_SleepRecord.rZQ_Score));
                        viewHolder.theZQ.setVisibility(View.VISIBLE);

                        pctDeep = iRec.theZAH_SleepRecord.rTime_Deep_min / iRec.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;
                        pctREM = iRec.theZAH_SleepRecord.rTime_REM_min / iRec.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;
                        pctAwake = iRec.theZAH_SleepRecord.rTime_Awake_min / iRec.theZAH_SleepRecord.rTime_Total_Z_min * 100.0;

                        builder = new SpannableStringBuilder();
                        totalSleep = new SpannableString("+Total: " + Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Total_Z_min, false) + "\n");
                        if (iRec.theZAH_SleepRecord.rTime_Total_Z_min < mGoalTotalSleepMin) { totalSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, totalSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(totalSleep);

                        deepSleep = new SpannableString("+%Deep: " + String.format("%.1f", pctDeep) + "% (" + Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Deep_min, false) + ")\n");
                        if (pctDeep < mGoalDeepPct) { deepSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, deepSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(deepSleep);

                        remSleep = new SpannableString("+%REM: " + String.format("%.1f", pctREM) + "% (" + Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_REM_min, false) + ")\n");
                        if (pctREM < mGoalREMpct) { remSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, remSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(remSleep);

                        awakeSleep = new SpannableString("-%Awake: " + String.format("%.1f", pctAwake) + "% (" + Utilities.showTimeInterval(iRec.theZAH_SleepRecord.rTime_Awake_min, false) + ")\n");
                        if (iRec.theZAH_SleepRecord.rTime_Awake_min > 0.0) { awakeSleep.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, awakeSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(awakeSleep);

                        cntAwaken = new SpannableString("-#Awaken: " + iRec.theZAH_SleepRecord.rCountAwakenings);
                        if (iRec.theZAH_SleepRecord.rCountAwakenings > 0) { cntAwaken.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, cntAwaken.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(cntAwaken);

                        viewHolder.theSum.setText(builder);
                        viewHolder.theSum.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        // CSE amended data
                        viewHolder.theZQ.setText(String.format("%02d", iRec.theCSErecord.rAmend_ZQ_Score));
                        viewHolder.theZQ.setVisibility(View.VISIBLE);

                        pctDeep = iRec.theCSErecord.rAmend_Time_Deep_min / iRec.theCSErecord.rAmend_Time_Total_Z_min * 100.0;
                        pctREM = iRec.theCSErecord.rAmend_Time_REM_min / iRec.theCSErecord.rAmend_Time_Total_Z_min * 100.0;
                        pctAwake = iRec.theCSErecord.rAmend_Time_Awake_min / iRec.theCSErecord.rAmend_Time_Total_Z_min * 100.0;

                        builder = new SpannableStringBuilder();
                        totalSleep = new SpannableString("+Total: " + Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Total_Z_min, false) + "\n");
                        if (iRec.theCSErecord.rAmend_Time_Total_Z_min < mGoalTotalSleepMin) { totalSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, totalSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(totalSleep);

                        deepSleep = new SpannableString("+%Deep: " + String.format("%.1f", pctDeep) + "% (" + Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Deep_min, false) + ")\n");
                        if (pctDeep < mGoalDeepPct) { deepSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, deepSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(deepSleep);

                        remSleep = new SpannableString("+%REM: " + String.format("%.1f", pctREM) + "% (" + Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_REM_min, false) + ")\n");
                        if (pctREM < mGoalREMpct) { remSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, remSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(remSleep);

                        awakeSleep = new SpannableString("-%Awake: " + String.format("%.1f", pctAwake) + "% (" + Utilities.showTimeInterval(iRec.theCSErecord.rAmend_Time_Awake_min, false) + ")\n");
                        if (iRec.theCSErecord.rAmend_Time_Awake_min > 0.0) { awakeSleep.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, awakeSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(awakeSleep);

                        cntAwaken = new SpannableString("-#Awaken: " + iRec.theCSErecord.rAmend_CountAwakenings);
                        if (iRec.theCSErecord.rAmend_CountAwakenings > 0) { cntAwaken.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, cntAwaken.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); }
                        builder.append(cntAwaken);

                        viewHolder.theSum.setText(builder);
                        viewHolder.theSum.setVisibility(View.VISIBLE);
                        break;
                    case 0:
                        // show limited CSE data if present
                        viewHolder.theZQ.setVisibility(View.INVISIBLE);
                        if (iRec.theCSErecord != null) {
                            double totalDurMin = 0.0;
                            long starting = iRec.theCSErecord.rEvent_GotIntoBed_Timestamp;
                            if (starting == 0) { starting = iRec.theCSErecord.rEvent_TryingToSleep_Timestamp; }
                            if (starting == 0 && iRec.theZAH_SleepRecord != null) { starting = iRec.theZAH_SleepRecord.rStartOfNight; }
                            long ending = iRec.theCSErecord.rEvent_OutOfBedDoneSleeping_Timestamp;
                            if (ending == 0 && iRec.theZAH_SleepRecord != null) { ending = iRec.theZAH_SleepRecord.rEndOfNight; }
                            if (starting > 0 && ending > 0) { totalDurMin = (double)((ending / 60000) - (starting / 60000)); }

                            if (totalDurMin > 0.0) {
                                builder = new SpannableStringBuilder();
                                totalSleep = new SpannableString("+Total: " + Utilities.showTimeInterval(totalDurMin, false) + "\n");
                                if (totalDurMin < mGoalTotalSleepMin) {
                                    totalSleep.setSpan(new ForegroundColorSpan(Color.rgb(230, 149, 0)), 0, totalSleep.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                builder.append(totalSleep);

                                viewHolder.theSum.setText(builder);
                                viewHolder.theSum.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder.theSum.setVisibility(View.GONE);
                            }
                        } else {
                            viewHolder.theSum.setVisibility(View.GONE);
                        }
                        break;
                }
            } else {
                viewHolder.theSum.setVisibility(View.GONE);
            }

            if (iRec.theHypnogramBitmap == null) { createHypnogramBitmap(iRec, isAmended); }   // note that iRec.theHypnogramBitmap may still be null after this call
            if (iRec.theHypnogramBitmap != null) {
                viewHolder.theHypno.setImageBitmap(iRec.theHypnogramBitmap);
                viewHolder.theHypno.setVisibility(View.VISIBLE);
            } else {
                viewHolder.theHypno.setVisibility(View.INVISIBLE);
            }
            return rowView;
        }

        // clear all the bitmaps so they have to be regenerated
        public void clearAllBitmaps() {
            for (JournalDataCoordinator.IntegratedHistoryRec iRec: mArrayList) {
                if (iRec.theHypnogramBitmap != null) {
                    iRec.theHypnogramBitmap.recycle();
                    iRec.theHypnogramBitmap = null;
                }
            }
        }
    }
}
