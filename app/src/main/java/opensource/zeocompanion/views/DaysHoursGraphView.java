package opensource.zeocompanion.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.utility.Utilities;

// displays a ZQ by day-of-week or a ZQ by start-of-sleep-hour graph
public class DaysHoursGraphView extends GraphView {
    // member variables
    private Context mContext = null;
    private  ArrayList<SleepDatasetRec> mOriginalDataSet = null;
    private  ArrayList<DayOfWeekRec> mDayOfWeekArray = null;
    private  ArrayList<StartHourRec> mStartHourArray = null;
    public int mDatasetLen = 0;
    public long mLowestTimestamp = 0L;
    public long mHighestTimestamp = 0L;
    private Point mScreenSize = null;
    PointsGraphSeries<DataPoint> mPointsSeries = null;

    public double mTimestampThresholdPct = 0.0;
    public boolean mShowDays = false;
    public boolean mIncludeTotalSleep = true;
    public boolean mIncludeDeep = true;
    public boolean mIncludeREM = true;
    public boolean mIncludeAwake = true;
    public boolean mIncludeAwakenings = true;

    // member constants and other static content
    private static final String _CTAG = "DHG";

    // internal records for bucketing the DOW and SH
    private class DayOfWeekRec {
        int rDayOfWeek = 0;
        String rDayOfWeekString = null;
        double rX = 0.0;
        ArrayList<Integer> rValuesInx = null;
    }
    private class StartHourRec {
        int rStartHour = 0;
        double rX = 0.0;
        ArrayList<Integer> rValuesInx = null;
    }

    // custom label formatter (used for the X-axis)
    public class DHV_DefaultLabelFormatter extends DefaultLabelFormatter {
        @Override
        public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
            switch (reason) {
                case SIZING:
                case SIZING_MAX:
                case SIZING_MIN:
                    // return the largest sized label
                    if (isValueX) {
                        if (mShowDays) { return "Wed\npm"; }
                        return "23\npm";
                    } else {
                        return "123";
                    }

                case AXIS_STEP:
                case AXIS_STEP_SECONDSCALE:
                case DATA_POINT:
                default:
                    if (isValueX) {
                        if (mShowDays) {
                            DayOfWeekRec dowRec = mDayOfWeekArray.get(index);
                            return dowRec.rDayOfWeekString + "\n ";
                        }
                        int hour = (int)value - 1;
                        String retStr = String.valueOf(hour)+"\n";
                        if (mScreenSize.x < mScreenSize.y) {
                            if (hour == 0) { retStr += "am"; }
                            else if (hour == 12) { retStr += "pm"; }
                            else { retStr += " "; }
                        } else {
                            if (hour >= 12 ) { retStr += "pm"; }
                            else { retStr += "am"; }
                        }
                        return retStr;
                    } else {
                        // show the Y value
                        return String.format("%.0f",value);
                    }
            }
        }
    }

    // setup a listener for scrolling and scaling activities
    /*???private Viewport.ScrollScaleListener mScrollScaleListener = new Viewport.ScrollScaleListener() {
        // scrolling is occurring
        public void onScrolling(GraphView graphView, RectF newViewport) {
            // nothing needed
        }
        public void onScaling(GraphView graphView, RectF newViewport) {
            recomputeXlabeling(newViewport);
        }
    };*/

    // constructors
    public DaysHoursGraphView(Context context) { super(context); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public DaysHoursGraphView(Context context, AttributeSet attrs) { super(context, attrs); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public DaysHoursGraphView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

    // toogle methods for the various lines available to show
    public void toggleDays(boolean showDays) { mShowDays = showDays; refresh(); }
    public void toggleTotalSleep(boolean show) { mIncludeTotalSleep = show; refresh(); }
    public void toggleREM(boolean show) { mIncludeREM = show; refresh(); }
    public void toggleAwake(boolean show) { mIncludeAwake = show; refresh(); }
    public void toggleDeep(boolean show) { mIncludeDeep = show; refresh(); }
    public void toggleAwakenings(boolean show) { mIncludeAwakenings = show; refresh(); }
    public void setThresholdDatePct(double thresholdDatePct) { mTimestampThresholdPct = thresholdDatePct; refresh(); }

    // setup graph for Dashboard use
    public void prepareForDashboard(Point screenSize) {
        mScreenSize = screenSize;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(true);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        render.setVerticalLabelsStartY(0.0);
        render.setHighlightZeroLines(true);

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0.0);
        viewport.setAxisMinY(0.0);
        viewport.setMaxY(100.0);
        viewport.setAxisMaxY(100.0);
        viewport.setXAxisBoundsManual(true);
        render.setLabelFormatter(new DHV_DefaultLabelFormatter());
        //???viewport.setScalable(true);
        //viewport.setScrollable(true);

        mIncludeTotalSleep = false;
        mIncludeAwake = false;
        mIncludeREM = false;
        mIncludeDeep = true;
        mIncludeAwakenings = false;

        if (mDayOfWeekArray != null) { clearDOW(); mDayOfWeekArray = null; }
        if (mStartHourArray != null) { clearSH(); mStartHourArray = null; }
        mDayOfWeekArray = new ArrayList<DayOfWeekRec>();
        mStartHourArray = new ArrayList<StartHourRec>();

        // setup the DOW array
        for (int i = 0; i < 7; i++)
        {
            DayOfWeekRec dowRec = new DayOfWeekRec();
            dowRec.rDayOfWeek = i;
            dowRec.rX = (double)i + 1.0;
            dowRec.rValuesInx = new ArrayList<Integer>();
            switch (i) {
                case 0:
                    dowRec.rDayOfWeekString = "Sun";
                    break;
                case 1:
                    dowRec.rDayOfWeekString = "Mon";
                    break;
                case 2:
                    dowRec.rDayOfWeekString = "Tue";
                    break;
                case 3:
                    dowRec.rDayOfWeekString = "Wed";
                    break;
                case 4:
                    dowRec.rDayOfWeekString = "Thu";
                    break;
                case 5:
                    dowRec.rDayOfWeekString = "Fri";
                    break;
                case 6:
                    dowRec.rDayOfWeekString = "Sat";
                    break;
            }
            mDayOfWeekArray.add(dowRec);
        }

        // setup the SH array
        for (int i = 0; i < 24; i++)
        {
            StartHourRec shRec = new StartHourRec();
            shRec.rStartHour = i;
            shRec.rX = (double)i + 1.0;
            shRec.rValuesInx = new ArrayList<Integer>();
            mStartHourArray.add(shRec);
        }
    }

    // prepare to create a bitmap rather than a view
    public void prepDrawToCanvas(int width, int height) {
        this.setLeft(0);
        this.setRight(width);
        this.setTop(0);
        this.setBottom(height);
        return;
    }

    // release all stored memory datasets (usually because the view is being destroyed)
    public void releaseDataset() {
        mOriginalDataSet = null;
        mDatasetLen = 0;
        removeAllSeries_deferRedraw();
        if (mPointsSeries != null) { mPointsSeries.resetDataPoints(); }
        mPointsSeries = null;
        clearDOW();
        mDayOfWeekArray = null;
        clearSH();
        mStartHourArray = null;
    }

    // set the data for the trends graph; note that the passed dataset is in descending date order;
    // however GraphView mandates that X-values be in ascending value order; this will be handled in the buildSeries methods
    public boolean setDataset(ArrayList<SleepDatasetRec> theData) {
        mOriginalDataSet = theData;
        mDatasetLen = theData.size();
        mLowestTimestamp = 0L;
        mHighestTimestamp = 0L;

        if (mDatasetLen == 0) return false;

        // parse the original dataset into DOW and SH buckets
        Calendar c = Calendar.getInstance();
        for (int i = 0; i < mDatasetLen; i++ ) {
            SleepDatasetRec oslRec = mOriginalDataSet.get(i);
            if (mLowestTimestamp == 0L) { mLowestTimestamp = oslRec.rTimestamp; }
            else if (oslRec.rTimestamp < mLowestTimestamp) { mLowestTimestamp = oslRec.rTimestamp; }
            if (mHighestTimestamp == 0L) { mHighestTimestamp = oslRec.rTimestamp; }
            else if (oslRec.rTimestamp > mHighestTimestamp) { mHighestTimestamp = oslRec.rTimestamp; }

            Date dt = new Date(oslRec.rTimestamp);
            int inx = dt.getHours();
            StartHourRec shRec = mStartHourArray.get(inx);
            shRec.rValuesInx.add(new Integer(i));

            c.setTime(dt);
            inx = c.get(Calendar.DAY_OF_WEEK);
            DayOfWeekRec dowRec = mDayOfWeekArray.get(inx - 1);
            dowRec.rValuesInx.add(new Integer(i));
        }

        // prepare and display the graph
        refresh();

        // setup a scroll/scale listener
        /*???mParentNumber = 1;
        Viewport viewport = this.getViewport();
        viewport.setScrollScaleListener(mScrollScaleListener);*/
        return true;
    }

    // clear out all the DOW and SH buckets
    private void clearDOW() {
        if (mDayOfWeekArray != null) {
            for (DayOfWeekRec dowRec: mDayOfWeekArray) {
                if (dowRec != null) {
                    if (dowRec.rValuesInx != null) { dowRec.rValuesInx.clear(); }
                    dowRec.rValuesInx = null;
                    dowRec.rDayOfWeekString = null;
                }
            }
            mDayOfWeekArray.clear();
        }
    }
    private void clearSH() {
        if (mStartHourArray != null) {
            for (StartHourRec shRec: mStartHourArray) {
                if (shRec != null) {
                    if (shRec.rValuesInx != null) { shRec.rValuesInx.clear(); }
                    shRec.rValuesInx = null;
                }
            }
            mStartHourArray.clear();
        }
    }

    // rebuild the trends graph usually after a change in the line(s) to display
    private void refresh() {
        // preserve the current viewport scale and scroll of the X-axis
        Viewport viewport = this.getViewport();
        GridLabelRenderer render = this.getGridLabelRenderer();

        // first clear out any existing sets of series
        removeAllSeries_deferRedraw();
        if (mPointsSeries != null) { mPointsSeries.resetDataPoints();  mPointsSeries = null; }

        // calculate the intensities of each attribute-value bucket; this also sorts the attributes into proper display order
        // calculate the cutoff timestamp
        long cutoffTimestamp = (long)((double)(mHighestTimestamp - mLowestTimestamp) * mTimestampThresholdPct) + mLowestTimestamp - 43200000;   // less 12 hours
        //Log.d(_CTAG+".calcInten","LowestTimestamp="+mLowestTimestamp+", HighestTimestamp="+mHighestTimestamp+", CutoffTimestamp="+cutoffTimestamp);

        // stage 1: calculate all the new "fractional" ZQs for the entire dataset; note the "fractional" ZQ could be negative
        int qtyActive = 0;
        for (SleepDatasetRec oslRec: mOriginalDataSet) {
            // calculate the new "fractional" ZQ based upon the desired elements to include
            oslRec.rWorkingArray[0] = 0.0;   // per-record storage slot for the "fractional" ZQ
            if (oslRec.rTimestamp >= cutoffTimestamp) {
                if (mIncludeTotalSleep) { oslRec.rWorkingArray[0] += (oslRec.rDataArray[1] / 60.0); }
                if (mIncludeREM) { oslRec.rWorkingArray[0] += (oslRec.rDataArray[3] / 60.0 / 2.0); }
                if (mIncludeDeep) { oslRec.rWorkingArray[0] += (oslRec.rDataArray[5] / 60.0 * 1.5); }
                if (mIncludeAwake) { oslRec.rWorkingArray[0] -= (oslRec.rDataArray[2] / 60.0 / 2.0); }
                if (mIncludeAwakenings) { oslRec.rWorkingArray[0] -= (oslRec.rDataArray[6] / 15.0); }
                oslRec.rWorkingArray[0] = oslRec.rWorkingArray[0] * 8.5;
                qtyActive++;
            }
        }

        // set the viewport
        viewport.setMinX(0.0);
        viewport.setAxisMinX(0.0);
        render.setHorizontalLabelsStartX(1.0);
        if (mShowDays) {
            viewport.setMaxX(8.0);
            viewport.setAxisMaxX(8.0);
            render.setNumHorizontalLabels(7);
            render.setHorizontalLabelsEndX(7.0);
        } else {
            viewport.setMaxX(25.0);
            viewport.setAxisMaxX(25.0);
            render.setNumHorizontalLabels(24);
            render.setHorizontalLabelsEndX(24.0);
        }

        // ??? computeXlabeling();

        // setup the datapoints
        int inx = 0;
        if (mDatasetLen > 0 && qtyActive > 0) {
            DataPoint[] theDataPoints = new DataPoint[qtyActive];
            if (mShowDays) {
                for (DayOfWeekRec dowRec: mDayOfWeekArray) {
                    for (Integer index: dowRec.rValuesInx) {
                        SleepDatasetRec oslRec = mOriginalDataSet.get(index);
                        if (oslRec.rTimestamp >= cutoffTimestamp) {
                            DataPoint dp = new DataPoint(index, dowRec.rX, oslRec.rWorkingArray[0]);
                            theDataPoints[inx] = dp;
                            inx++;
                        }
                    }
                }
            } else {
                for (StartHourRec shRec: mStartHourArray) {
                    for (Integer index: shRec.rValuesInx) {
                        SleepDatasetRec oslRec = mOriginalDataSet.get(index);
                        if (oslRec.rTimestamp >= cutoffTimestamp) {
                            DataPoint dp = new DataPoint(index, shRec.rX, oslRec.rWorkingArray[0]);
                            theDataPoints[inx] = dp;
                            inx++;
                        }
                    }
                }
            }
            mPointsSeries = new PointsGraphSeries<DataPoint>(theDataPoints);
            mPointsSeries.setSize(4.0f * ZeoCompanionApplication.mScreenDensity);
            /*m???PointsSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
                @Override
                public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                    int inx = dataPoint.getIndex();
                    AttrValueRec avRec = mAttrValueRecs.get(inx);
                    //Log.d(_CTAG+".custShap.draw","X="+String.format("%.2f",dataPoint.getX())+", Y="+String.format("%.2f",avRec.rY)+", V="+avRec.rValueName+", L="+avRec.rLikert+", I="+String.format("%.2f",avRec.rIntensityAvg)+", I%="+String.format("%.2f",avRec.rIntensityPct)+", Q="+avRec.rOrigRecs.size());
                    float size = 5.0f + 15.0f * ((float)avRec.rOrigRecsQtyActive / (float)mHighestAttrValueQtyRecs) * ZeoCompanionApplication.mScreenDensity;
                    paint.setColor(determineColor(avRec));
                    canvas.drawCircle(x, y, size, paint);
                }
            });*/
            addSeries(mPointsSeries);
            double minY = mPointsSeries.getLowestValueY();
            double maxY = mPointsSeries.getHighestValueY();
            if (minY < 0.0) {
                viewport.setMinY(minY);
                viewport.setAxisMinY(minY);
            } else {
                viewport.setMinY(0.0);
                viewport.setAxisMinY(0.0);
            }
            int numlbls = 5;
            if (maxY <= 0.0) { maxY = 1.0; numlbls = 2; }
            else if (maxY < 10.0) { maxY = 10.0; numlbls = 6; }
            else if (maxY < 15.0) { maxY = 15.0; numlbls = 4; }
            else if (maxY < 25.0) { maxY = 25.0; numlbls = 6; }
            else if (maxY < 50.0) { maxY = 50.0; numlbls = 3; }
            else if (maxY < 75.0) { maxY = 75.0; numlbls = 4; }
            else if (maxY < 100.0) { maxY = 100.0; numlbls = 5; }
            else if (maxY < 125.0) { maxY = 125.0; numlbls = 6; }
            else if (maxY < 150.0) { maxY = 150.0; numlbls = 7; }
            viewport.setAxisMaxY(maxY);
            if (maxY <= 25) { viewport.setMaxY(maxY + 2.5); }
            else { viewport.setMaxY(maxY + 10.0); }
            render.setVerticalLabelsEndY(maxY);
            render.setNumVerticalLabels(numlbls);
        }

        // now redraw the entire graph
        onDataChanged(false, false);
    }

    // determine how many letters of the Attribute names can be shown along the X-axis
    /*???private void computeXlabeling() {
        Viewport viewport = this.getViewport();
        recomputeXlabeling(viewport.mCurrentViewport);
    }
    private void recomputeXlabeling(RectF newViewport) {
        GridLabelRenderer render = this.getGridLabelRenderer();

        int qtyShown = (int)(newViewport.right - newViewport.left - 1.0);
        if (qtyShown == 0) { qtyShown = 1; }
        int pixelsPerSlot = (getGraphContentWidth() / qtyShown) - 3;
        if (pixelsPerSlot < 5) pixelsPerSlot = 5;

        Rect textBounds = new Rect();
        int n = 0;
        do {
            n++;
            String str = StringUtils.repeat("S", n);
            render.computeTextBounds(str, 0, n, textBounds);
        } while (textBounds.width() < pixelsPerSlot);

        mNumLetters = n - 1;
    }*/
}

