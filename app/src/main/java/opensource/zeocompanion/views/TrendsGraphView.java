package opensource.zeocompanion.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import opensource.zeocompanion.utility.Utilities;

public class TrendsGraphView extends GraphView {    // TODO V1.1 Dashboard Tab
    // member variables
    private Context mContext = null;
    private  ArrayList<Trends_dataSet> mOrigDataSet = null;
    private int mDatasetLen = 0;
    private int mQtySeries = 0;
    LineGraphSeries<DataPoint> mSeries_TimeToZ = null;
    LineGraphSeries<DataPoint> mSeries_TotalSleep = null;
    LineGraphSeries<DataPoint> mSeries_Awake = null;
    LineGraphSeries<DataPoint> mSeries_REM = null;
    LineGraphSeries<DataPoint> mSeries_Light = null;
    LineGraphSeries<DataPoint> mSeries_Deep = null;
    LineGraphSeries<DataPoint> mSeries_Awakenings = null;
    LineGraphSeries<DataPoint> mSeries_ZQscore = null;

    public boolean mShowTimeToZ = false;
    public boolean mShowTotalSleep = true;
    public boolean mShowAwake = false;
    public boolean mShowREM = true;
    public boolean mShowLight = false;
    public boolean mShowDeep = true;
    public boolean mShowZQscore = false;

    public boolean mShowAwakenings = false;

    // member constants and other static content
    private static final int MAXLINES = 8;

    // original dataset record
    public static class Trends_dataSet {
        public long mTimestamp = 0;
        public double mDataArray[] = new double[MAXLINES];

        // constructor
        public Trends_dataSet(long timestamp, double timeToZMin, double totalSleepMin, double remMin, double awakeMin, double lightMin, double deepMin, int awakeningsQty, int zq_score) {
            mTimestamp = timestamp;
            mDataArray[0] = timeToZMin;
            mDataArray[1] = totalSleepMin;
            mDataArray[2] = awakeMin;
            mDataArray[3] = remMin;
            mDataArray[4] = lightMin;
            mDataArray[5] = deepMin;
            mDataArray[6] = awakeningsQty;
            mDataArray[7] = zq_score;
        }
    }

    // constructors
    public TrendsGraphView(Context context) { super(context); mContext = context; prepare(); }
    public TrendsGraphView(Context context, AttributeSet attrs) { super(context, attrs); mContext = context;  prepare(); }
    public TrendsGraphView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); mContext = context;  prepare(); }

    // show content toggles
    public void toggleTimeToZ(boolean show) { mShowTimeToZ = show; refresh(); }
    public void toggleTotalSleep(boolean show) { mShowTotalSleep = show; refresh(); }
    public void toggleREM(boolean show) { mShowREM = show; refresh(); }
    public void toggleAwake(boolean show) { mShowAwake = show; refresh(); }
    public void toggleLight(boolean show) { mShowLight = show; refresh(); }
    public void toggleDeep(boolean show) { mShowDeep = show; refresh(); }
    public void toggleAwakenings(boolean show) { mShowAwakenings = show; refresh(); }
    public void toggleZQ(boolean show) { mShowZQscore = show; refresh(); }

    public void setDataset( ArrayList<Trends_dataSet> theData) {
        mOrigDataSet = theData;
        mDatasetLen = theData.size();
        refresh();
    }

    private void refresh() {
        // first clear out any existing sets of series
        if (mQtySeries > 0) {
            this.removeAllSeries();
            mSeries_TimeToZ = null;
            mSeries_TotalSleep = null;
            mSeries_Awake = null;
            mSeries_REM = null;
            mSeries_Light = null;
            mSeries_Deep = null;
            mSeries_Awakenings = null;
            mSeries_ZQscore = null;
            mQtySeries = 0;
        }

        // begin building series and adding them to the graph
        if (mShowTimeToZ) {
            mSeries_TimeToZ = buildSeries(0);
            mSeries_TimeToZ.setColor(Color.YELLOW);
            mSeries_TimeToZ.setDrawDataPoints(true);
            mSeries_TimeToZ.setDataPointsRadius(5);
            this.addSeries(mSeries_TimeToZ);
            mQtySeries++;
        }
        if (mShowTotalSleep) {
            mSeries_TotalSleep = buildSeries(1);
            mSeries_TotalSleep.setColor(Color.BLACK);
            mSeries_TotalSleep.setDrawDataPoints(true);
            mSeries_TotalSleep.setDataPointsRadius(5);
            //this.addSeries(mSeries_TotalSleep);
            mQtySeries++;
        }
        if (mShowAwake) {
            mSeries_Awake = buildSeries(2);
            mSeries_Awake.setColor(Color.RED);
            mSeries_Awake.setDrawDataPoints(true);
            mSeries_Awake.setDataPointsRadius(5);
            this.addSeries(mSeries_Awake);
            mQtySeries++;
        }
        if (mShowREM) {
            mSeries_REM = buildSeries(3);
            mSeries_REM.setColor(Color.rgb(0, 153, 0));    // green
            mSeries_REM.setDrawDataPoints(true);
            mSeries_REM.setDataPointsRadius(5);
            this.addSeries(mSeries_REM);
            mQtySeries++;
        }
        if (mShowLight) {
            mSeries_Light = buildSeries(4);
            mSeries_Light.setColor(Color.rgb(102, 178, 255));    // light blue
            mSeries_Light.setDrawDataPoints(true);
            mSeries_Light.setDataPointsRadius(5);
            this.addSeries(mSeries_Light);
            mQtySeries++;
        }
        if (mShowDeep) {
            mSeries_Deep = buildSeries(5);
            mSeries_Deep.setColor(Color.rgb(0, 0, 204));    // dark blue
            mSeries_Deep.setDrawDataPoints(true);
            mSeries_Deep.setDataPointsRadius(5);
            //this.addSeries(mSeries_Deep);
            mQtySeries++;
        }
        /*if (mShowAwakenings) {
            mSeries_Awakenings = buildSeries(6);
            mSeries_Awakenings.setColor(Color.MAGENTA);
            mSeries_Awakenings.setDrawDataPoints(true);
            mSeries_Awakenings.setDataPointsRadius(5);
            this.addSeries(mSeries_Awakenings);
            mQtySeries++;
        }*/
        if (mShowZQscore) {
            mSeries_ZQscore = buildSeries(7);
            mSeries_ZQscore.setColor(Color.WHITE);
            mSeries_ZQscore.setDrawDataPoints(true);
            mSeries_ZQscore.setDataPointsRadius(5);
            this.addSeries(mSeries_ZQscore);
            mQtySeries++;
        }
    }

    private LineGraphSeries<DataPoint> buildSeries(int dataArrayIndex) {
        DataPoint[] theDataPoints = new DataPoint[mDatasetLen];
        for (int i = 0; i < mDatasetLen; i++) {
            Trends_dataSet item = mOrigDataSet.get(i);
            double y = 0.0;
            switch (dataArrayIndex) {
                case 0:
                    // time to Z (min)
                    // ?
                    break;
                case 1:
                    // total sleep (min)
                    // ?
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    // awake, REM, light, deep (all min)
                    if (item.mDataArray[1] == 0.0) { y = 0.0; }
                    else { y = item.mDataArray[dataArrayIndex] / item.mDataArray[1] * 100.0; }
                case 6:
                    // qty awakenings (count)
                    break;
                case 7:
                    // ZQ score is always 0 to 100
                    y = item.mDataArray[dataArrayIndex];
                    break;
            }
            theDataPoints[i] = new DataPoint(i, item.mTimestamp, y);
        }
        return new LineGraphSeries<DataPoint>(theDataPoints);
    }

    private void prepare() {
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(true);
        render.setVerticalLabelsColor(Color.BLACK);
        render.setHorizontalLabelsColor(Color.BLACK);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        render.setNumHorizontalLabels(3);
        render.setLabelFormatter(new DateAsXAxisLabelFormatter(mContext));

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0.0);
        viewport.setMaxY(100.0);
    }

    public void prepDrawToCanvas(int width, int height) {
        this.setLeft(0);
        this.setRight(width);
        this.setTop(0);
        this.setBottom(height);
        return;
    }
}

