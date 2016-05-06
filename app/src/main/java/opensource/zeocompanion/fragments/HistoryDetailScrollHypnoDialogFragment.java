package opensource.zeocompanion.fragments;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;

import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.views.HypnogramView;

public class HistoryDetailScrollHypnoDialogFragment extends DialogFragment {
    // remember that no class member variable will be preserved across an orientation change
    private View mRootView = null;
    private HypnogramView mGraph_Hypno1 = null;
    private HypnogramView mGraph_Hypno2 = null;
    private boolean mHas_hypno1 = false;
    private boolean mHas_hypno2 = false;
    private boolean mShowAmended;
    private boolean mIsAmended = false;

    // member constants and other static content
    private static final String _CTAG = "HHF";

    // setup a listener for scrolling activities in either hypnogram
    private Viewport.ScrollScaleListener mScrollScaleListener = new Viewport.ScrollScaleListener() {
        // scrolling is occurring
        public void onScrolling(GraphView graphView, RectF newViewport) {
            if (graphView.mParentNumber == 1L) {
                if (mHas_hypno2) { mGraph_Hypno2.getViewport().setScrollTo(newViewport); }
            } else if (graphView.mParentNumber == 2L) {
                if (mHas_hypno1) { mGraph_Hypno1.getViewport().setScrollTo(newViewport); }
            }
        }
        public void onScaling(GraphView graphView, RectF newViewport) {
            if (graphView.mParentNumber == 1L) {
                mGraph_Hypno1.setLabelsPerScale();
            } else if (graphView.mParentNumber == 2L) {
                mGraph_Hypno2.setLabelsPerScale();
            }

            if (graphView.mParentNumber == 1L) {
                if (mHas_hypno2) { mGraph_Hypno2.getViewport().setScaleTo(newViewport); }
            } else if (graphView.mParentNumber == 2L) {
                if (mHas_hypno1) { mGraph_Hypno1.getViewport().setScaleTo(newViewport); }
            }
        }
    };

    // constructor
    public HistoryDetailScrollHypnoDialogFragment() {}

    // instanciator
    public static HistoryDetailScrollHypnoDialogFragment newInstance(String param1, String param2) {
        HistoryDetailScrollHypnoDialogFragment fragment = new HistoryDetailScrollHypnoDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Log.d(_CTAG + ".onCreate", "==========FRAG ON-CREATE=====");
        mRootView = inflater.inflate(R.layout.fragment_history_detail_scroll_hypno, container, false);

        // determine whether the record's CSE portion is amended
        mIsAmended = false;
        if (ZeoCompanionApplication.mIrec_HDAonly != null) {
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
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
        // setup a click listener on the done button
        mRootView.findViewById(R.id.button_done).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        mGraph_Hypno1 = (HypnogramView)mRootView.findViewById(R.id.graph_hypnoScroll1);
        mGraph_Hypno2 = (HypnogramView)mRootView.findViewById(R.id.graph_hypnoScroll2);
        if (ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord != null) {
            mGraph_Hypno1.showAsExpanded();
            if (ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
                mGraph_Hypno1.setDataset(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime, 30, 30, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rBase_Hypnogram, false, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
            } else {
                mGraph_Hypno1.setDataset(ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rDisplayHypnogramStartTime, 30, 30, ZeoCompanionApplication.mIrec_HDAonly.theZAH_SleepRecord.rBase_Hypnogram, false, null);
            }
            mHas_hypno1 = true;
        } else {
            mHas_hypno1 = false;
        }
        if (mIsAmended && ZeoCompanionApplication.mIrec_HDAonly.theCSErecord != null) {
            mGraph_Hypno2.showAsExpanded();
            mGraph_Hypno2.setDataset(ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Display_Hypnogram_Starttime, 30, 30, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.rAmend_Base_Hypnogram, false, ZeoCompanionApplication.mIrec_HDAonly.theCSErecord.mEvents_array);
            mHas_hypno2 = true;
        } else {
            mHas_hypno2 = false;
        }
        if (mHas_hypno1) { mGraph_Hypno1.setScrollScaleListener(1L, mScrollScaleListener); }
        if (mHas_hypno2) { mGraph_Hypno2.setScrollScaleListener(2L, mScrollScaleListener); }
        showProperImage();

        CheckBox cb = (CheckBox)mRootView.findViewById(R.id.checkBox_showAmended);
        if (mIsAmended) { cb.setVisibility(View.VISIBLE); }
        else { cb.setVisibility(View.INVISIBLE); }
        cb.setChecked(mShowAmended);
        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mShowAmended = isChecked;
                showProperImage();
            }
        });

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Log.d(_CTAG + ".onViewCreated", "==========FRAG ON-VIEWCREATED=====");
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle("30 Sec Hypnogram Scrollable");
    }

    @Override
    public void onResume() {
        //Log.d(_CTAG + ".onResume", "==========FRAG ON-RESUME=====");
        super.onResume();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        Window window = getDialog().getWindow();
        int newWidth =  (int)(((double)screenSize.x) * 0.95);
        window.setLayout(newWidth, (int) (450.0 * ZeoCompanionApplication.mScreenDensity));
        window.setGravity(Gravity.CENTER);
    }

    // show the proper image of the two that were built
    private void showProperImage() {
        if (mIsAmended && mShowAmended) {
            if (mHas_hypno2) {
                mGraph_Hypno1.setVisibility(View.INVISIBLE);
                mGraph_Hypno2.setVisibility(View.VISIBLE);
            } else if (mHas_hypno1) {
                mGraph_Hypno1.setVisibility(View.VISIBLE);
                mGraph_Hypno2.setVisibility(View.INVISIBLE);
            }
        } else {
            if (mHas_hypno1) {
                mGraph_Hypno1.setVisibility(View.VISIBLE);
                mGraph_Hypno2.setVisibility(View.INVISIBLE);
            } else if (mHas_hypno2) {
                mGraph_Hypno1.setVisibility(View.INVISIBLE);
                mGraph_Hypno2.setVisibility(View.VISIBLE);
            }
        }
    }
}
