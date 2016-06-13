package opensource.zeocompanion.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.MultiSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.StackedBarGraphSeries;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAttributeValuesRec;

// displays an attribute-values line chart or stacked bar chart
public class AttributeEffectsGraphView extends GraphView {
    // member variables
    private Context mContext = null;
    public  ArrayList<String> mAttributes = null;
    private  ArrayList<AttrEffects_dataSet> mOriginalDataSet = null;
    private  ArrayList<AttrEffects_dataSet> mShowAttrDataSet = null;
    private ArrayList<CompanionAttributeValuesRec> mXaxisValues = null;
    public int mDatasetLen = 0;
    public int mShownDatasetLen = 0;
    private int mShowAsMode = 0;
    private Point mScreenSize = null;
    private int mQtySeries = 0;
    private boolean firstTime = true;
    private double mGoalTotalSleepMin = 480.0;  // 8 hours
    private double mGoalREMpct = 20.0;
    private double mGoalDeepPct = 15.0;
    private double mGoalLightPct = 70.0;
    PointsGraphSeries<DataPoint> mPointsSeries_TimeToZ = null;
    PointsGraphSeries<DataPoint> mPointsSeries_TotalSleep = null;
    PointsGraphSeries<DataPoint> mPointsSeries_Awake = null;
    PointsGraphSeries<DataPoint> mPointsSeries_REM = null;
    PointsGraphSeries<DataPoint> mPointsSeries_Light = null;
    PointsGraphSeries<DataPoint> mPointsSeries_Deep = null;
    PointsGraphSeries<DataPoint> mPointsSeries_Awakenings = null;
    PointsGraphSeries<DataPoint> mPointsSeries_ZQscore = null;
    LineGraphSeries<DataPoint> mLineSeries_Goal = null;
    LineGraphSeries<DataPoint> mLineSeries_Trend = null;
    StackedBarGraphSeries<DataPoint> mStackedBarSeries = null;

    public String mShowAttribute = null;
    public boolean mShowBarsAndLines = false;
    public boolean mShowGoalLine = false;
    public boolean mShowTrendLine = false;
    public boolean mShowTimeToZ = false;
    public boolean mShowTotalSleep = false;
    public boolean mShowAwake = false;
    public boolean mShowREM = false;
    public boolean mShowLight = false;
    public boolean mShowDeep = false;
    public boolean mShowZQscore = false;
    public boolean mShowAwakenings = false;

    // member constants and other static content
    private static final String _CTAG = "AEG";
    private static final int MAXFIELDS = 9;
    //private SimpleDateFormat mDF1 = new SimpleDateFormat("MM/dd/yy");

    // data record
    public static class AttrEffects_dataSet {
        public String mAttributeName;
        public String mValueString;
        public float mLikertValue;
        public long mTimestamp = 0;
        public double mDataArray[] = new double[MAXFIELDS];

        // constructor
        public AttrEffects_dataSet(String attribute, float likert, String valueString, long timestamp, double timeToZMin, double totalSleepMin, double remMin, double awakeMin, double lightMin, double deepMin, int awakeningsQty, int zq_score) {
            mAttributeName = attribute;
            mValueString = valueString;
            mLikertValue = likert;
            mTimestamp = timestamp;
            mDataArray[0] = timeToZMin;
            mDataArray[1] = totalSleepMin;
            mDataArray[2] = awakeMin;
            mDataArray[3] = remMin;
            mDataArray[4] = lightMin;
            mDataArray[5] = deepMin;
            mDataArray[6] = awakeningsQty;
            mDataArray[7] = zq_score;
            mDataArray[8] = timeToZMin + totalSleepMin +  awakeMin;
        }
    }

    // custom legend renderer
    public class TGV_LegendRenderer extends LegendRenderer {
        // constructor
        public TGV_LegendRenderer(GraphView graphView) {
            super(graphView);
        }

        // draw the legend to the left of the graph right next to the ending line point
        @Override
        public void draw(Canvas canvas) {
            float top = mGraphView.getGraphContentTop();
            float bottom = mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight();
            float left =  mGraphView.getGraphContentLeft()+mGraphView.getGraphContentWidth();
            float right = left + getLegendRenderLayoutWidth();

            if (mStyles.backgroundColor != Color.TRANSPARENT) {
                mPaint.setColor(mStyles.backgroundColor);
                canvas.drawRect(left, top, right, bottom, mPaint);
            }

            mPaint.setTextSize(mStyles.textSize);
            List<Series> mainSeries = mGraphView.getSeries();
            for (Series s : mainSeries) {
                if (s.getQtySubseries() <= -1) {
                    // this series has no subseries
                    String title = s.getTitle();
                    int len = s.size();
                    if (len > 0 && title != null) {
                        float y = s.getDrawY(mGraphView, len - 1) + mStyles.textSize / (float)2.0;
                        if (y > bottom) { y = bottom; }
                        mPaint.setColor(s.getColor());
                        canvas.drawText(title, left + mStyles.padding, y, mPaint);
                    }
                } else {
                    // this series has zero or more subseries
                    for (int j = 0; j < s.getQtySubseries(); j++) {
                        MultiSeries ms = (MultiSeries)s;
                        String title = ms.getTitle(j);
                        int len = ms.size(j);
                        if (len > 0 && title != null) {
                            float y = ms.getDrawY(mGraphView, j, len - 1) + mStyles.textSize / (float)2.0;
                            if (y > bottom) { y = bottom; }
                            mPaint.setColor(ms.getColor(j));
                            canvas.drawText(title, left + mStyles.padding, y, mPaint);
                        }
                    }
                }
            }
        }

        // get the width needed for the legend (takes away from main graphing area)
        @Override
        public int getLegendRenderLayoutWidth() {
            // width
            int legendWidth = mStyles.width;
            if (legendWidth == 0) {
                // auto
                legendWidth = cachedLegendWidth;
                if (legendWidth == 0) {
                    mPaint.setTextSize(mStyles.textSize);
                    Rect textBounds = new Rect();
                    mPaint.getTextBounds("Time2Z%", 0, 7, textBounds);
                    legendWidth = Math.max(legendWidth, textBounds.width());
                    List<Series> mainSeries = mGraphView.getSeries();
                    for (Series s : mainSeries) {
                        String title = s.getTitle();
                        if (title != null) {
                            mPaint.getTextBounds(title, 0, title.length(), textBounds);
                            legendWidth = Math.max(legendWidth, textBounds.width());
                        }
                    }
                    if (legendWidth > 0) {
                        legendWidth += (mStyles.padding * 2);
                        cachedLegendWidth = legendWidth;
                    }
                }
            }
            return legendWidth;
        }

        // get the height needed for the legend (takes away from main graphing area)
        @Override
        public int getLegendRenderLayoutHeight() {
            return 0;
        }
    }

    public class TGV_DefaultLabelFormatter extends DefaultLabelFormatter {
        @Override
        public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
            switch (reason) {
                case SIZING:
                case SIZING_MAX:
                case SIZING_MIN:
                    // return the largest sized label
                    if (isValueX) {
                        return "00.00\nString";
                    } else {
                        return "000%";
                    }

                case AXIS_STEP:
                case AXIS_STEP_SECONDSCALE:
                case DATA_POINT:
                default:
                    if (isValueX) {
                        // show the likert value and string version of it
                        return String.format("%.0f",value)+"\n";    // ???
                    } else {
                        // show the Y value
                        return String.format("%.0f",value)+"%";
                    }
            }
        }
    }

    // constructors
    public AttributeEffectsGraphView(Context context) { super(context); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public AttributeEffectsGraphView(Context context, AttributeSet attrs) { super(context, attrs); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public AttributeEffectsGraphView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

    // toogle methods for the various lines available to show
    public void toggleBarsAndLines(boolean bars) { mShowBarsAndLines = bars; refresh(); }
    public void toggleTimeToZ(boolean show) { mShowTimeToZ = show; refresh(); }
    public void toggleTotalSleep(boolean show) { mShowTotalSleep = show; refresh(); }
    public void toggleREM(boolean show) { mShowREM = show; refresh(); }
    public void toggleAwake(boolean show) { mShowAwake = show; refresh(); }
    public void toggleLight(boolean show) { mShowLight = show; refresh(); }
    public void toggleDeep(boolean show) { mShowDeep = show; refresh(); }
    public void toggleAwakenings(boolean show) { mShowAwakenings = show; refresh(); }
    public void toggleZQ(boolean show) { mShowZQscore = show; refresh(); }
    public void toggleAllOff() {    // does not refresh
        mShowTimeToZ = false;
        mShowTotalSleep = false;
        mShowREM = false;
        mShowAwake = false;
        mShowLight = false;
        mShowDeep = false;
        mShowAwakenings = false;
        mShowZQscore = false;
    }
    public void toggleAttribute(String attribute) { mShowAttribute = attribute; refresh(); }

    // show just one data field plus goal and trend (Dashboard Tab)
    public void prepareForDashboard(Point screenSize) {
        mShowAsMode = 1;
        mScreenSize = screenSize;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(true);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        render.setLabelFormatter(new DateAsXAxisLabelFormatter(mContext));

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0.0);
        viewport.setMaxY(105.0);
        render.setNumVerticalLabels(6);
        render.setVerticalLabelsEndY(100.0);
        viewport.setXAxisBoundsManual(true);
        render.setLabelFormatter(new TGV_DefaultLabelFormatter());

        if (mScreenSize.x >= 1024) {
            setLegendRenderer(new TGV_LegendRenderer(this));
            LegendRenderer lr = getLegendRenderer();
            lr.setVisible(true);
            lr.setBackgroundColor(Color.LTGRAY);
            lr.setPadding(5);
        }

        mShowTimeToZ = false;
        mShowTotalSleep = false;
        mShowAwake = false;
        mShowREM = false;
        mShowLight = false;
        mShowDeep = true;
        mShowZQscore = false;
        mShowAwakenings = false;
        mShowGoalLine = true;
        mShowTrendLine = true;
    }

    // full capability graph
    public void prepareForStats(Point screenSize) {
        mShowAsMode = 2;
        mScreenSize = screenSize;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(true);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        render.setLabelFormatter(new DateAsXAxisLabelFormatter(mContext));

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0.0);
        viewport.setMaxY(105.0);
        viewport.setAxisMinY(0.0);
        viewport.setAxisMaxY(105.0);
        render.setNumVerticalLabels(6);
        render.setVerticalLabelsEndY(100.0);
        viewport.setXAxisBoundsManual(true);
        render.setHorizontalLabelsFixedPosition(true);
        render.setLabelFormatter(new TGV_DefaultLabelFormatter());

        if (mScreenSize.x >= 1024) {
            setLegendRenderer(new TGV_LegendRenderer(this));
            LegendRenderer lr = getLegendRenderer();
            lr.setVisible(true);
            lr.setBackgroundColor(Color.LTGRAY);
            lr.setPadding(5);
        }

        //viewport.setMinimumScaleWidth((float)10080.0);     // 7 days of minimum width in minutes
        //viewport.setScrollable(true);
        //viewport.setScalable(true);

        mShowTimeToZ = false;
        mShowTotalSleep = false;
        mShowAwake = false;
        mShowREM = false;
        mShowLight = false;
        mShowDeep = true;
        mShowZQscore = false;
        mShowAwakenings = false;
        mShowGoalLine = true;
        mShowTrendLine = true;
    }

    // prepare to create a bitmap rather than a view
    public void prepDrawToCanvas(int width, int height) {
        this.setLeft(0);
        this.setRight(width);
        this.setTop(0);
        this.setBottom(height);
        return;
    }

    // set the data for the trends graph; note that the passed dataset is in descending date order;
    // however GraphView mandates that X-values be in ascending value order; this will be handled in the buildSeries methods
    public boolean setDataset(ArrayList<AttrEffects_dataSet> theData, double goalTotalSleep, double goalREMpct, double goalDeepPct) {
        mGoalTotalSleepMin = goalTotalSleep;
        mGoalREMpct = goalREMpct;
        mGoalDeepPct = goalDeepPct;
        mGoalLightPct = 100.0 - goalREMpct - goalDeepPct;
        mOriginalDataSet = theData;
        mDatasetLen = theData.size();
        if (mShowAsMode == 1 && mDatasetLen > 7) { mDatasetLen = 7; }
        mShownDatasetLen = mDatasetLen;

        mAttributes = new ArrayList<String>();
        for (int i = 0; i < mDatasetLen; i++ ) {
            AttrEffects_dataSet ads = mOriginalDataSet.get(i);
            boolean found = false;
            for (String existingAttr: mAttributes) {
                if (ads.mAttributeName.equals(existingAttr)) { found = true; }
            }
            if (!found) { mAttributes.add(ads.mAttributeName); }
        }
        Log.d(_CTAG+".setDataset","Total Recs Cnt="+mDatasetLen+", Found Attributes Cnt="+mAttributes.size());

        if (mDatasetLen == 0) return false;
        refresh();
        return true;
    }

    // set a scrolling and scaling callback listener
    public void setScrollScaleListener(long callbackNumber, Viewport.ScrollScaleListener listener) {
        mParentNumber = callbackNumber;
        Viewport viewport = this.getViewport();
        viewport.setScrollScaleListener(listener);
    }

    // rebuild the trends graph usually after a change in the line(s) to display
    private void refresh() {
        // preserve the current viewport scale and scroll of the X-axis
        Viewport viewport = this.getViewport();
        GridLabelRenderer render = this.getGridLabelRenderer();
       // double origMinX = viewport.getMinX(false);
        //double origMaxX = viewport.getMaxX(false);

        // first clear out any existing sets of series
        if (mQtySeries > 0) {
            removeAllSeries_deferRedraw();
            mPointsSeries_TimeToZ = null;
            mPointsSeries_TotalSleep = null;
            mPointsSeries_Awake = null;
            mPointsSeries_REM = null;
            mPointsSeries_Light = null;
            mPointsSeries_Deep = null;
            mPointsSeries_Awakenings = null;
            mPointsSeries_ZQscore = null;
            mLineSeries_Goal = null;
            mStackedBarSeries = null;
            mQtySeries = 0;
        }

        // select the subset of data for the shown attribute
        if (mShowAttribute == null) { mShownDatasetLen = 0; return; }
        if (mShowAttribute.isEmpty()) { mShownDatasetLen = 0; return; }
        if (mShowAttrDataSet != null) { mShowAttrDataSet.clear(); }
        else { mShowAttrDataSet = new ArrayList<AttrEffects_dataSet>(); }
        double lowestFoundLikert = 999999999999.0;
        double highestFoundLikert = 0.0;
        for (int i = 0; i < mDatasetLen; i++) {
            AttrEffects_dataSet item = mOriginalDataSet.get(i);
            if (item.mAttributeName.equals(mShowAttribute)) {
                if (item.mLikertValue > highestFoundLikert) { highestFoundLikert = item.mLikertValue; }
                if (item.mLikertValue < lowestFoundLikert) { lowestFoundLikert = item.mLikertValue; }
                mShowAttrDataSet.add(item);
            }
        }
        Log.d(_CTAG+".refresh","Attribute="+mShowAttribute+", Found Recs Cnt="+mShowAttrDataSet.size()+", Lowest Observed Likert="+lowestFoundLikert+", Highest Observed Likert="+highestFoundLikert);

        // get all the possible X-axis likert values for the shown attribute
        double lowestOfficialLikert = 999999999999.0;
        double highestOfficialLikert = 0.0;
        Cursor cursor = ZeoCompanionApplication.mDatabaseHandler.getAttributeValuesRecsForAttributeSortedLikert(mShowAttribute);
        if (mXaxisValues != null) { mXaxisValues.clear(); }
        else { mXaxisValues = new ArrayList<CompanionAttributeValuesRec>(); }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    CompanionAttributeValuesRec avRec = new CompanionAttributeValuesRec(cursor);
                    if (avRec.rLikert > highestOfficialLikert) { highestOfficialLikert = avRec.rLikert; }
                    if (avRec.rLikert < lowestOfficialLikert) { lowestOfficialLikert = avRec.rLikert; }
                    mXaxisValues.add(avRec);
                } while (cursor.moveToNext());
            }
        }
        Log.d(_CTAG+".refresh","Attribute="+mShowAttribute+", Lowest Official Likert="+lowestOfficialLikert+", Highest Official Likert="+highestOfficialLikert);

        // set the viewport properly (including current scaling and scrolling)
        double xMin = 999999999999.0;
        if (lowestOfficialLikert < 999999999999.0) {
            xMin = lowestOfficialLikert;
        } else if (lowestFoundLikert < 999999999999.0) {
            xMin = lowestFoundLikert;
        } else {
            xMin = 1.0;
        }
        xMin = Math.floor(xMin);
        double xMax = 0.0;
        if (highestOfficialLikert > 0.0) {
            xMax = highestOfficialLikert;
        } else if (highestFoundLikert > 0.0) {
            xMax = highestFoundLikert;
        } else {
            xMax = 5.0;
        }
        xMax = Math.ceil(xMax);
        Log.d(_CTAG+".refresh","Attribute="+mShowAttribute+", Found Recs Cnt="+mShowAttrDataSet.size()+", lowest X="+xMin+", highest X="+xMax);

        viewport.setMinX(xMin);
        //viewport.setAxisMinX(lowestDate);
        viewport.setMaxX(xMax);
        //double addX = viewport.xPixelsToDeltaXvalue(10.0f);
        //viewport.setMaxX(highestDate + addX);
        //viewport.setAxisMaxX(highestDate + addX);
       // render.setHorizontalLabelsEndX(highestDate);
        /*if (!firstTime) {
            viewport.setMinX(origMinX);
            viewport.setMaxX(origMaxX);
        }
        firstTime = false;*/
        int q = (int)(xMax - xMin);
        render.setNumHorizontalLabels(q + 1);

        // is there any data for this attribute?
        mShownDatasetLen = mShowAttrDataSet.size();
        if (mShownDatasetLen == 0) { return; }

        // yes, begin building series and adding them to the graph
        // first up are those series that can be also be shown as a stackedBar
        int qtyFieldsShown = 0;
        double maxY = 0.0;
        DataPoint[] theDataPoints = null;
        if (mShowBarsAndLines) {
            if (mShowDeep || mShowLight || mShowREM || mShowAwake || mShowTimeToZ) {
                mStackedBarSeries = new StackedBarGraphSeries<DataPoint>();
                if (mShowDeep) {
                    theDataPoints = buildDataPoints(5);
                    if (theDataPoints != null) {
                        mStackedBarSeries.addSubseries(theDataPoints);
                        int subseriesNo = mStackedBarSeries.getQtySubseries() - 1;
                        mStackedBarSeries.setColor(subseriesNo,  Color.rgb(0, 0, 204));  // dark blue
                        mStackedBarSeries.setTitle(subseriesNo, "Deep%");
                        qtyFieldsShown++;
                    }
                }
                if (mShowLight) {
                    theDataPoints = buildDataPoints(4);
                    if (theDataPoints != null) {
                        mStackedBarSeries.addSubseries(theDataPoints);
                        int subseriesNo = mStackedBarSeries.getQtySubseries() - 1;
                        mStackedBarSeries.setColor(subseriesNo,  Color.rgb(102, 178, 255));  // light blue
                        mStackedBarSeries.setTitle(subseriesNo, "Light%");
                        qtyFieldsShown++;
                    }
                }
                if (mShowREM) {
                    theDataPoints = buildDataPoints(3);
                    if (theDataPoints != null) {
                        mStackedBarSeries.addSubseries(theDataPoints);
                        int subseriesNo = mStackedBarSeries.getQtySubseries() - 1;
                        mStackedBarSeries.setColor(subseriesNo,  Color.rgb(0, 153, 0));  // green
                        mStackedBarSeries.setTitle(subseriesNo, "REM%");
                        qtyFieldsShown++;
                    }
                }
                if (mShowAwake) {
                    theDataPoints = buildDataPoints(2);
                    if (theDataPoints != null) {
                        mStackedBarSeries.addSubseries(theDataPoints);
                        int subseriesNo = mStackedBarSeries.getQtySubseries() - 1;
                        mStackedBarSeries.setColor(subseriesNo,  Color.RED);
                        mStackedBarSeries.setTitle(subseriesNo, "Awake%");
                        qtyFieldsShown++;
                    }
                }
                if (mShowTimeToZ) {
                    theDataPoints = buildDataPoints(0);
                    if (theDataPoints != null) {
                        mStackedBarSeries.addSubseries(theDataPoints);
                        int subseriesNo = mStackedBarSeries.getQtySubseries() - 1;
                        mStackedBarSeries.setColor(subseriesNo,  Color.rgb(255, 165, 0));   // orange
                        mStackedBarSeries.setTitle(subseriesNo, "Time2Z%");
                        qtyFieldsShown++;
                    }
                }
                double y = mStackedBarSeries.getHighestValueY();
                if (y > maxY) { maxY = y; }

                float pixels = viewport.deltaXvalueToXpixels(960.0);    // 16 hours in minutes
                mStackedBarSeries.setBarWidth(pixels);
                addSeries_deferRedraw(mStackedBarSeries);
                mQtySeries++;
            }
        } else {
            if (mShowDeep) {
                theDataPoints = buildDataPoints(5);
                if (theDataPoints != null) {
                    mPointsSeries_Deep = new PointsGraphSeries<DataPoint>(theDataPoints);
                    double y = mPointsSeries_Deep.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    if (mShowAsMode == 1) { mPointsSeries_Deep.setColor(Color.BLUE); }
                    else { mPointsSeries_Deep.setColor(Color.rgb(0, 0, 204)); }    // dark blue
                    mPointsSeries_Deep.setTitle("Deep%");
                    addSeries_deferRedraw(mPointsSeries_Deep);
                    mQtySeries++;
                    qtyFieldsShown++;

                }
            }
            if (mShowLight) {
                theDataPoints = buildDataPoints(4);
                if (theDataPoints != null) {
                    mPointsSeries_Light = new PointsGraphSeries<DataPoint>(theDataPoints);
                    double y = mPointsSeries_Light.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    if (mShowAsMode == 1) { mPointsSeries_Light.setColor(Color.BLUE); }
                    else { mPointsSeries_Light.setColor(Color.rgb(102, 178, 255)); }    // light blue
                    mPointsSeries_Light.setTitle("Light%");
                    addSeries_deferRedraw(mPointsSeries_Light);
                    mQtySeries++;
                    qtyFieldsShown++;
                }
            }
            if (mShowREM) {
                theDataPoints = buildDataPoints(3);
                if (theDataPoints != null) {
                    mPointsSeries_REM = new PointsGraphSeries<DataPoint>(theDataPoints);
                    double y = mPointsSeries_REM.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    if (mShowAsMode == 1) { mPointsSeries_REM.setColor(Color.BLUE); }
                    else { mPointsSeries_REM.setColor(Color.rgb(0, 153, 0)); }    // green
                    mPointsSeries_REM.setTitle("REM%");
                    addSeries_deferRedraw(mPointsSeries_REM);
                    mQtySeries++;
                    qtyFieldsShown++;
                }
            }
            if (mShowAwake) {
                theDataPoints = buildDataPoints(2);
                if (theDataPoints != null) {
                    mPointsSeries_Awake = new PointsGraphSeries<DataPoint>(theDataPoints);
                    double y = mPointsSeries_Awake.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    if (mShowAsMode == 1) { mPointsSeries_Awake.setColor(Color.BLUE); }
                    else { mPointsSeries_Awake.setColor(Color.RED); }
                    mPointsSeries_Awake.setTitle("Awake%");
                    addSeries_deferRedraw(mPointsSeries_Awake);
                    mQtySeries++;
                    qtyFieldsShown++;
                }
            }
            if (mShowTimeToZ) {
                theDataPoints = buildDataPoints(0);
                if (theDataPoints != null) {
                    mPointsSeries_TimeToZ = new PointsGraphSeries<DataPoint>(theDataPoints);
                    double y = mPointsSeries_TimeToZ.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    if (mShowAsMode == 1) { mPointsSeries_TimeToZ.setColor(Color.BLUE); }
                    else { mPointsSeries_TimeToZ.setColor(Color.rgb(255, 165, 0)); }  // orange
                    mPointsSeries_TimeToZ.setTitle("Time2Z%");
                    addSeries_deferRedraw(mPointsSeries_TimeToZ);
                    mQtySeries++;
                    qtyFieldsShown++;
                }
            }
        }

        // now those series that are always lines
        if (mShowTotalSleep) {
            theDataPoints = buildDataPoints(1);
            if (theDataPoints != null) {
                mPointsSeries_TotalSleep = new PointsGraphSeries<DataPoint>(theDataPoints);
                double y = mPointsSeries_TotalSleep.getHighestValueY();
                if (y > maxY) { maxY = y; }
                if (mShowAsMode == 1) { mPointsSeries_TotalSleep.setColor(Color.BLUE); }
                else { mPointsSeries_TotalSleep.setColor(Color.BLACK); }
                mPointsSeries_TotalSleep.setTitle("Total%");
                addSeries_deferRedraw(mPointsSeries_TotalSleep);
                mQtySeries++;
                qtyFieldsShown++;
            }
        }
        /*if (mShowAwakenings) {
            theDataPoints = buildDataPoints(6);
            if (theDataPoints != null) {
                mSeries_Awakenings = new LineGraphSeries<DataPoint>(theDataPoints);
                double y = mSeries_Awakenings.getHighestValueY();
                if (y > maxY) { maxY = y; }
                if (mShowAsMode == 1) { mSeries_Awakenings.setColor(Color.BLUE); }
                else { mSeries_Awakenings.setColor(Color.MAGENTA); }
                mSeries_Awakenings.setDrawDataPoints(true);
                mSeries_Awakenings.setDataPointsRadius(5);
                mSeries_Awakenings.setTitle("Awaken#");
                addSeries_deferRedraw(mSeries_Awakenings);
                mQtySeries++;
                qtyFieldsShown++;
            }
        }*/
        if (mShowZQscore) {
            theDataPoints = buildDataPoints(7);
            if (theDataPoints != null) {
                mPointsSeries_ZQscore = new PointsGraphSeries<DataPoint>(theDataPoints);
                double y = mPointsSeries_ZQscore.getHighestValueY();
                if (y > maxY) { maxY = y; }
                if (mShowAsMode == 1) { mPointsSeries_ZQscore.setColor(Color.BLUE); }
                else { mPointsSeries_ZQscore.setColor(Color.WHITE); }
                mPointsSeries_ZQscore.setTitle("ZQ");
                addSeries_deferRedraw(mPointsSeries_ZQscore);
                mQtySeries++;
                qtyFieldsShown++;
            }
        }

        // special series (trends and goal)
        /*???if (mShowTrendLine &&  mDatasetLen > 1 && theDataPoints != null) {
            TrendlinePoints[] tps = null;
            String title = "Trend";
            if (mShowAsMode == 2) { title += "(s)"; }
            if (qtyFieldsShown == 1) {
                tps = calculateTrendlines(theDataPoints);
            } else if (mShowBarsAndLines && mStackedBarSeries != null && qtyFieldsShown > 1 && mQtySeries == 1) {
                tps = calculateTrendlines(mStackedBarSeries.toArray());
            }
            if (tps != null) {
                for (int i = 0; i < tps.length; i++) {
                    DataPoint[] trendDataPoints = new DataPoint[2];
                    trendDataPoints[0] = new DataPoint(0, tps[i].mStartValueX, tps[i].mStartValueY);
                    trendDataPoints[1] = new DataPoint(1, tps[i].mEndValueX, tps[i].mEndValueY);
                    mLineSeries_Trend = new LineGraphSeries<DataPoint>(trendDataPoints);
                    double y = mLineSeries_Trend.getHighestValueY();
                    if (y > maxY) { maxY = y; }
                    mLineSeries_Trend.setColor(Color.GRAY);
                    mLineSeries_Trend.setDrawDataPoints(false);
                    if (ZeoCompanionApplication.mScreenDensity > 1.0f) { mLineSeries_Trend.setThickness(5); }
                    else { mLineSeries_Trend.setThickness(3); }
                    if (i == tps.length - 1) { mLineSeries_Trend.setTitle(title); }
                    else { mLineSeries_Trend.setTitle(null); }
                    addSeries_deferRedraw(mLineSeries_Trend);
                    mQtySeries++;
                }
            }
        }*/

        if (mShowGoalLine && mDatasetLen > 0) {
            double goal = getGoal(qtyFieldsShown);
            if (goal > 0.0) {
                DataPoint[] goalDataPoints = new DataPoint[2];
                goalDataPoints[0] = new DataPoint(0, xMin, goal);
                goalDataPoints[1] = new DataPoint(1, xMax, goal);
                mLineSeries_Goal = new LineGraphSeries<DataPoint>(goalDataPoints);
                double y = mLineSeries_Goal.getHighestValueY();
                if (y > maxY) { maxY = y; }
                mLineSeries_Goal.setColor(Color.GRAY);
                mLineSeries_Goal.setDrawDataPoints(false);
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                if (ZeoCompanionApplication.mScreenDensity > 1.0f) { paint.setStrokeWidth(5); }
                else { paint.setStrokeWidth(3); }
                paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
                mLineSeries_Goal.setCustomPaint(paint);
                if (ZeoCompanionApplication.mScreenDensity > 1.0f) { mLineSeries_Goal.setThickness(5); }
                else { mLineSeries_Goal.setThickness(3); }
                mLineSeries_Goal.setTitle("Goal");
                addSeries_deferRedraw(mLineSeries_Goal);
                mQtySeries++;
            }
        }

        // adjust the maximum Y to nice intervals
        if (maxY < 25.0) {
            maxY = 25.0;
        } else if (maxY < 50.0) {
            maxY = 50.0;
        } else if (maxY < 75.0) {
            maxY = 75.0;
        } else if (maxY < 100.0) {
            maxY = 100.0;
        } else if (maxY < 125.0) {
            maxY = 125.0;
        } else if (maxY < 150.0) {
            maxY = 150.0;
        } else if (maxY < 175.0) {
            maxY = 175.0;
        }
        viewport.setMaxY(maxY + 5.0);
        viewport.setAxisMaxY(maxY + 5.0);
        render.setVerticalLabelsEndY(maxY);

        // now redraw the entire graph
        onDataChanged(false, false);
    }

    // get the proper goal value for the shown data field; only used in show-single-line mode
    private double getGoal(int qtyFieldsShown) {
        if (mShowBarsAndLines) {
            if (mShowTotalSleep && qtyFieldsShown == 1) { return 100.0; }
            double sumGoalPct = 0.0;
            int c = 0;
            if (mShowREM) { sumGoalPct += mGoalREMpct; c++; }
            if (mShowLight) { sumGoalPct += mGoalLightPct; c++; }
            if (mShowDeep) { sumGoalPct += mGoalDeepPct; c++; }
            if (mShowAwake) { c++; }
            if (mShowTimeToZ) { c++; }
            if (c == qtyFieldsShown) { return sumGoalPct; }
            return 0.0;
        } else {
            if (qtyFieldsShown > 1) { return 0.0; }
            if (mShowREM) {
                return mGoalREMpct;
            } else if (mShowLight) {
                return mGoalLightPct;
            } else if (mShowDeep) {
                return mGoalDeepPct;
            } else if (mShowTotalSleep) {
                return 100.0;
            } else {
                return 0.0;
            }
        }
    }

    // build the data points for a single data field; note the X-values are in descending order but GraphView must have them in ascending order
    private DataPoint[] buildDataPoints(int dataArrayIndex) {
        if (mDatasetLen <= 0) { return null; }
        DataPoint[] theDataPoints = new DataPoint[mShownDatasetLen];
        int j = 0;
        for (int i = mShownDatasetLen - 1; i >= 0; i--) {
            AttrEffects_dataSet item = mShowAttrDataSet.get(i);
            double y = 0.0;
            switch (dataArrayIndex) {
                case 1:
                    // total sleep (min); percentage to goal
                    if (mGoalTotalSleepMin == 0.0) { y = 0.0; }
                    else { y = item.mDataArray[1] / mGoalTotalSleepMin * 100.0; }
                    break;
                case 0:
                case 2:
                case 3:
                case 4:
                case 5:
                    // time-to-Z, awake, REM, light, deep (all min); percentage to total duration
                    if (item.mDataArray[8] == 0.0) { y = 0.0; }
                    else { y = item.mDataArray[dataArrayIndex] / item.mDataArray[8] * 100.0; }
                    break;
                case 6:
                    // qty awakenings (count)
                    break;
                case 7:
                    // ZQ score is generally 0 to 100, but could go higher than 100
                    y = item.mDataArray[dataArrayIndex];
                    break;
            }
            theDataPoints[j] = new DataPoint(i, (double)item.mLikertValue, y);
            j++;
        }
        return theDataPoints;
    }

    // data points record for showing trendlines
    private class TrendlinePoints {
        public double mStartValueX;
        public double mStartValueY;
        public double mEndValueX;
        public double mEndValueY;
    }

    // calculate one trend line
    private TrendlinePoints calculateOneTrendline(DataPoint[] theDataPoints, int startInx, int endInx) {
        if (endInx - startInx  < 1) { return null; }
        SimpleRegression sr = new SimpleRegression(true);
        for (int i = startInx; i <= endInx; i++) {
            sr.addData(theDataPoints[i].getX(), theDataPoints[i].getY());
        }
        if (!sr.hasIntercept()) { return null; }

        double slope = sr.getSlope();
        double ValueYatXzero = sr.getIntercept();

        TrendlinePoints results = new TrendlinePoints();
        results.mStartValueX = theDataPoints[startInx].getX();
        results.mStartValueY = ValueYatXzero + results.mStartValueX * slope;

        results.mEndValueX = theDataPoints[endInx].getX();
        results.mEndValueY = ValueYatXzero + results.mEndValueX * slope;
        return results;
    }

    // calculate one or more trend lines depending on dataset size and time-gaps in the dataset
    private TrendlinePoints[] calculateTrendlines(DataPoint[] theDataPoints) {
        if (theDataPoints == null) { return null; }
        if (mDatasetLen <= 1) { return null; }
        if (mDatasetLen <= 7) {
            // this is for the dashboard tab, or if there are less than or equal to just 7 sleep sessions to-date
            TrendlinePoints[] tps = new TrendlinePoints[1];
            tps[0] = calculateOneTrendline(theDataPoints, 0, mDatasetLen - 1);
            if (tps[0] == null) { return null; }
            return tps;
        }

        // this is for a larger dataset for the Statistics graph;
        // need to look for time-gaps in the sleep records and form separate trend lines
        ArrayList<TrendlinePoints> tpa = new ArrayList<TrendlinePoints>();
        int startI = 0;
        int endI = 0;
        double priorX = 0.0;
        while (endI < mDatasetLen) {
            double endX = theDataPoints[endI].getX();
            if (endX - priorX > 10080.0) {     // 7 days in minutes
                // greater than 7 days since prior sleep session
                if ((endI - 1) - startI > 0) {
                    // have two or more days of sleep session data
                    TrendlinePoints tp = calculateOneTrendline(theDataPoints, startI, endI - 1);
                    tpa.add(tp);
                }
                startI = endI;
            }
            priorX = endX;
            endI++;
        }
        if ((endI - 1) - startI > 0) {
            // have two or more days of sleep session data
            TrendlinePoints tp = calculateOneTrendline(theDataPoints, startI, endI - 1);
            tpa.add(tp);
        }

        TrendlinePoints[] tps = new TrendlinePoints[tpa.size()];
        return (TrendlinePoints[])tpa.toArray(tps);
    }
}

