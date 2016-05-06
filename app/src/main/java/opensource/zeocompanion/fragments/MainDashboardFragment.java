package opensource.zeocompanion.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import opensource.zeocompanion.R;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.JournalDataCoordinator;
import opensource.zeocompanion.views.DailyResultGraphView;
import opensource.zeocompanion.views.TrendsGraphView;

public class MainDashboardFragment extends MainFragmentWrapper {
    private static final String _CTAG = "M1F";
    private View mRootView = null;

    public MainDashboardFragment() {}

    public static MainDashboardFragment newInstance() { return new MainDashboardFragment(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_main_dashboard, container, false);
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO V1.1 Dashboard Tab
        /*ArrayList<JournalDataCoordinator.IntegratedHistoryRec> theIrecs = new ArrayList<JournalDataCoordinator.IntegratedHistoryRec>();
        ArrayList<TrendsGraphView.Trends_dataSet> theData = new ArrayList<TrendsGraphView.Trends_dataSet>();
        ZeoCompanionApplication.mCoordinator.getAllIntegratedHistoryRecs(theIrecs);
        for (JournalDataCoordinator.IntegratedHistoryRec iRec: theIrecs) {
            if (iRec.theZAH_SleepRecord != null) {
                TrendsGraphView.Trends_dataSet ds = new TrendsGraphView.Trends_dataSet(iRec.theZAH_SleepRecord.mStartOfNight, iRec.theZAH_SleepRecord.mTime_to_Z_min,
                        iRec.theZAH_SleepRecord.mTime_Total_Z_min, iRec.theZAH_SleepRecord.mTime_REM_min, iRec.theZAH_SleepRecord.mTime_Awake_min,
                        iRec.theZAH_SleepRecord.mTime_Light_min, iRec.theZAH_SleepRecord.mTime_Deep_min, iRec.theZAH_SleepRecord.mCountAwakenings,
                        iRec.theZAH_SleepRecord.mZQ_Score);
                theData.add(ds);
            }
        }

        TrendsGraphView theDailyResults = (TrendsGraphView)mRootView.findViewById(R.id.graph_trends);
        theDailyResults.setDataset(theData);*/
    }

    @Override
    public void needToRefresh() {
        // TODO V1.1 Dashboard tab
    }

}
