package opensource.zeocompanion.views;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.jjoe64.graphview.series.StackedBarGraphSeries;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionAttributeValuesRec;

// displays an attribute-values heatmap chart
public class AttributesHeatmapGraphView extends GraphView {
    // member variables
    private Context mContext = null;
    private  ArrayList<AttrRec> mAttrRecs = null;
    private ArrayList<AttrValueRec> mAttrValueRecs = null;
    private  ArrayList<AttrValsSleepDatasetRec> mOriginalDataSet = null;
    public int mDatasetLen = 0;
    public double mLowestZQ = 0;
    public double mHighestZQ = 0;
    public long mLowestTimestamp = 0L;
    public long mHighestTimestamp = 0L;
    private int mHighestAttrValueQtyRecs = 0;
    private int mNumLetters = 1;
    private Point mScreenSize = null;
    PointsGraphSeries<DataPoint> mPointsSeries = null;

    public double mGoodThresholdPct = 0.666666;
    public double mTimestampThresholdPct = 0.0;
    public boolean mIncludeTotalSleep = true;
    public boolean mIncludeDeep = true;
    public boolean mIncludeREM = true;
    public boolean mIncludeAwake = true;
    public boolean mIncludeAwakenings = true;

    // member constants and other static content
    private static final String _CTAG = "AHG";

    // internal records for bucketing the Attributes and Values
    private class AttrRec {
        int rID = 0;
        String rAttributeShortName = null;
        String rAttributeDisplayName = null;
        double rHighestIntensityPct = 0.0;
        double rX = 0.0;
        int rValuesQtyActive = 0;
        ArrayList<Integer> rValuesInx = null;
    }
    private class AttrValueRec {
        int rAttrID = 0;
        String rValueName = null;
        double rLikert = 0.0;
        double rY = 0.0;
        double rIntensityAvg = 0.0;
        double rIntensityPct = 0.0;
        int rOrigRecsQtyActive = 0;
        ArrayList<AttrValsSleepDatasetRec> rOrigRecs = null;
    }

    // custom label formatter (used for the X-axis)
    public class AHV_DefaultLabelFormatter extends DefaultLabelFormatter {
        @Override
        public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
            switch (reason) {
                case SIZING:
                case SIZING_MAX:
                case SIZING_MIN:
                    // return the largest sized label
                    if (isValueX) {
                        return StringUtils.repeat("W", mNumLetters);
                    } else {
                        return "";
                    }

                case AXIS_STEP:
                case AXIS_STEP_SECONDSCALE:
                case DATA_POINT:
                default:
                    if (isValueX) {
                        // show the attribute name; index in this case will be sequential with the mAttrRecs
                        int inx = (int)value - 1;
                        if (inx >= mAttrRecs.size()) return "";
                        AttrRec atRec = mAttrRecs.get(inx);
                        int end = mNumLetters;
                        if (atRec.rAttributeDisplayName != null) {
                            if (!atRec.rAttributeDisplayName.isEmpty()) {
                                int len = atRec.rAttributeDisplayName.length();
                                if (end > len) { end = len; }
                                return atRec.rAttributeDisplayName.substring(0, end);
                            }
                        }
                        int len = atRec.rAttributeShortName.length();
                        if (end > len) { end = len; }
                        return atRec.rAttributeShortName.substring(0, end);
                    } else {
                        // show the Y value
                        return "";
                    }
            }
        }
    }

    // setup a listener for scrolling and scaling activities
    private Viewport.ScrollScaleListener mScrollScaleListener = new Viewport.ScrollScaleListener() {
        // scrolling is occurring
        public void onScrolling(GraphView graphView, RectF newViewport) {
            // nothing needed
        }
        public void onScaling(GraphView graphView, RectF newViewport) {
            recomputeXlabeling(newViewport);
        }
    };

    // constructors
    public AttributesHeatmapGraphView(Context context) { super(context); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public AttributesHeatmapGraphView(Context context, AttributeSet attrs) { super(context, attrs); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public AttributesHeatmapGraphView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); mContext = context; setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

    // toogle methods for the various lines available to show
    public void toggleTotalSleep(boolean show) { mIncludeTotalSleep = show; refresh(); }
    public void toggleREM(boolean show) { mIncludeREM = show; refresh(); }
    public void toggleAwake(boolean show) { mIncludeAwake = show; refresh(); }
    public void toggleDeep(boolean show) { mIncludeDeep = show; refresh(); }
    public void toggleAwakenings(boolean show) { mIncludeAwakenings = show; refresh(); }
    public void setThresholdZQpct(double thresholdZQpct) { mGoodThresholdPct = thresholdZQpct; refresh(); }
    public void setThresholdDatePct(double thresholdDatePct) { mTimestampThresholdPct = thresholdDatePct; refresh(); }

    // show just one data field plus goal and trend (Dashboard Tab)
    public void prepareForDashboard(Point screenSize) {
        mScreenSize = screenSize;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(false);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0.0);
        viewport.setAxisMinY(0.0);
        viewport.setMaxY(1.0);
        viewport.setAxisMaxY(1.0);
        viewport.setXAxisBoundsManual(true);
        render.setLabelFormatter(new AHV_DefaultLabelFormatter());
        viewport.setScalable(true);
        viewport.setScrollable(true);

        mIncludeTotalSleep = false;
        mIncludeAwake = false;
        mIncludeREM = false;
        mIncludeDeep = true;
        mIncludeAwakenings = false;
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
        if (mAttrValueRecs != null) { mAttrValueRecs.clear(); }
        mAttrValueRecs = null;
        clearAttrRecs();
        mAttrRecs = null;
    }

    // set the data for the trends graph; note that the passed dataset is in descending date order;
    // however GraphView mandates that X-values be in ascending value order; this will be handled in the buildSeries methods
    public boolean setDataset(ArrayList<AttrValsSleepDatasetRec> theData) {
        mOriginalDataSet = theData;
        mDatasetLen = theData.size();

        if (mAttrValueRecs != null) { mAttrValueRecs.clear();  mAttrValueRecs = null; }
        if (mAttrRecs != null) { clearAttrRecs(); mAttrRecs = null; }
        if (mDatasetLen == 0) return false;
        mAttrRecs = new ArrayList<AttrRec>();
        mAttrValueRecs = new ArrayList<AttrValueRec>();
        mLowestTimestamp = 0L;
        mHighestTimestamp = 0L;

        // first parse the original dataset into attribute and value buckets
        int maxRows = 1;
        for (int i = 0; i < mDatasetLen; i++ ) {
            AttrValsSleepDatasetRec odRec = mOriginalDataSet.get(i);
            boolean foundAT = false;
            if (mLowestTimestamp == 0L) { mLowestTimestamp = odRec.rTimestamp; }
            else if (odRec.rTimestamp < mLowestTimestamp) { mLowestTimestamp = odRec.rTimestamp; }
            if (mHighestTimestamp == 0L) { mHighestTimestamp = odRec.rTimestamp; }
            else if (odRec.rTimestamp > mHighestTimestamp) { mHighestTimestamp = odRec.rTimestamp; }

            for (AttrRec atRec: mAttrRecs) {
                if (odRec.rAttributeShortName.equals(atRec.rAttributeShortName)) {
                    foundAT = true;
                    boolean foundAV = false;
                    for (Integer index: atRec.rValuesInx) {
                        AttrValueRec avRec = mAttrValueRecs.get(index);
                        if (odRec.rLikertValue == avRec.rLikert) {
                            foundAV = true;
                            avRec.rOrigRecs.add(odRec);
                            avRec.rOrigRecsQtyActive++;
                            break;
                        }
                    }
                    if (!foundAV) {
                        AttrValueRec avNewRec = new AttrValueRec();
                        avNewRec.rAttrID = atRec.rID;
                        avNewRec.rValueName = odRec.rValueString;
                        avNewRec.rLikert = odRec.rLikertValue;;
                        avNewRec.rOrigRecs = new ArrayList<AttrValsSleepDatasetRec>();
                        avNewRec.rOrigRecs.add(odRec);
                        avNewRec.rOrigRecsQtyActive = 1;
                        mAttrValueRecs.add(avNewRec);

                        atRec.rValuesInx.add(new Integer(mAttrValueRecs.size() - 1));
                        atRec.rValuesQtyActive++;

                        if (atRec.rValuesInx.size() > maxRows) { maxRows = atRec.rValuesInx.size(); }
                    }
                    break;
                }
            }
            if (!foundAT) {
                AttrValueRec avNewRec = new AttrValueRec();
                avNewRec.rAttrID = mAttrRecs.size() + 1;
                avNewRec.rValueName = odRec.rValueString;
                avNewRec.rLikert = odRec.rLikertValue;;
                avNewRec.rOrigRecs = new ArrayList<AttrValsSleepDatasetRec>();
                avNewRec.rOrigRecs.add(odRec);
                avNewRec.rOrigRecsQtyActive = 1;
                mAttrValueRecs.add(avNewRec);

                AttrRec atNewRec = new AttrRec();
                atNewRec.rID = mAttrRecs.size() + 1;
                atNewRec.rAttributeShortName = odRec.rAttributeShortName;
                atNewRec.rAttributeDisplayName = odRec.rAttributeDisplayName;
                atNewRec.rValuesInx = new ArrayList<Integer>();
                atNewRec.rValuesInx.add(new Integer(mAttrValueRecs.size() - 1));
                atNewRec.rValuesQtyActive = 1;
                mAttrRecs.add(atNewRec);

                if (atNewRec.rValuesInx.size() > maxRows) { maxRows = atNewRec.rValuesInx.size(); }
            }
        }

        // now calculate Y-axis positions based upon the highest and lowest found likerts;
        // Y-axis only spans from 0.0 to 1.0
        if (maxRows > 5) { maxRows = 5; }
        mHighestAttrValueQtyRecs = 0;
        for (AttrRec atRec: mAttrRecs) {
            // sort all the found values in ascending Likert order
            Collections.sort(atRec.rValuesInx, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    // configured for ASCENDING sort order
                    double likert1 = mAttrValueRecs.get(o1).rLikert;
                    double likert2 = mAttrValueRecs.get(o2).rLikert;
                    if (likert1 > likert2) {
                        return 1;
                    }
                    if (likert1 < likert2) {
                        return -1;
                    }
                    return 0;
                }
            });

            // calculate the y-point for each Value bucket;
            // also determine the highest quantity of records in any of the buckets
            int count = atRec.rValuesInx.size();
            if (count < maxRows) { count = maxRows; }
            double y = 0.1;
            double dy = 1.0 / (double)(count);
            for (Integer index: atRec.rValuesInx) {
                AttrValueRec avRec = mAttrValueRecs.get(index);
                avRec.rY = y;
                y += dy;

                int qty = avRec.rOrigRecs.size();
                if (qty > mHighestAttrValueQtyRecs) { mHighestAttrValueQtyRecs = qty; }
            }
        }
        //Log.d(_CTAG+".setDataset","Total Recs Cnt="+mDatasetLen+", Found Attributes Cnt="+mAttrRecs.size()+", Found Unique Values Cnt="+mAttrValueRecs.size()+", MaxQty="+mHighestAttrValueQtyRecs);

        // prepare and display the graph
        refresh();

        // setup a scroll/scale listener
        mParentNumber = 1;
        Viewport viewport = this.getViewport();
        viewport.setScrollScaleListener(mScrollScaleListener);
        return true;
    }

    // clear out all the AttrRecs and AttrValueRecs
    private void clearAttrRecs() {
        if (mAttrRecs != null) {
            for (AttrRec atRec: mAttrRecs) {
                if (atRec != null) {
                    if (atRec.rValuesInx != null) { atRec.rValuesInx.clear(); }
                    atRec.rValuesInx = null;
                    atRec.rAttributeDisplayName = null;
                    atRec.rAttributeShortName = null;
                }
            }
            mAttrRecs.clear();
        }
        if (mAttrValueRecs != null) {
            for (AttrValueRec avRec: mAttrValueRecs) {
                if (avRec != null) {
                    if (avRec.rOrigRecs != null) { avRec.rOrigRecs.clear(); }
                    avRec.rValueName = null;
                }
            }
            mAttrValueRecs.clear();
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
        calculateIntensities();

        // layout the attributes along the X-axis and set the viewport properly
        double dinx = 1.0;
        int qtyDatapoints = 0;
        if (mAttrRecs != null) {
            for (AttrRec atRec: mAttrRecs) {
                if (atRec.rValuesQtyActive > 0) {
                    //Log.d(_CTAG+".refresh","Attribute in Order="+atRec.rAttributeShortName);
                    atRec.rX = dinx;
                    dinx = dinx + 1.0;

                    for (Integer index: atRec.rValuesInx) {
                        AttrValueRec avRec = mAttrValueRecs.get(index);
                        if (avRec.rOrigRecsQtyActive > 0) { qtyDatapoints++; }
                    }
                }
                else { atRec.rX = 0.0; }
            }
        }

        // set the viewport
        viewport.setMinX(0.0);
        viewport.setAxisMinX(0.0);
        viewport.setMaxX(dinx);
        viewport.setAxisMaxX(dinx);
        render.setNumHorizontalLabels((int)dinx);
        render.setHorizontalLabelsStartX(1.0);
        render.setHorizontalLabelsEndX(dinx);
        computeXlabeling();

        // setup the datapoints
        int inx = 0;
        if (mDatasetLen > 0 && qtyDatapoints > 0) {
            DataPoint[] theDataPoints = new DataPoint[qtyDatapoints];
            for (AttrRec atRec: mAttrRecs) {
                if (atRec.rValuesQtyActive > 0) {
                    for (Integer index: atRec.rValuesInx) {
                        AttrValueRec avRec = mAttrValueRecs.get(index);
                        if (avRec.rOrigRecsQtyActive > 0) {
                            //Log.d(_CTAG+".refresh","X="+String.format("%.2f",atRec.rX)+", Y="+String.format("%.2f",avRec.rY)+", A="+atRec.rAttributeShortName+", V="+avRec.rValueName+", L="+avRec.rLikert+", I="+String.format("%.2f",avRec.rIntensityAvg)+", I%="+String.format("%.2f",avRec.rIntensityPct)+", Q="+avRec.rOrigRecs.size());
                            DataPoint dp = new DataPoint(index, atRec.rX, avRec.rY);
                            theDataPoints[inx] = dp;
                            inx++;
                        }
                    }
                }
            }
            mPointsSeries = new PointsGraphSeries<DataPoint>(theDataPoints);
            mPointsSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
                @Override
                public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                    int inx = dataPoint.getIndex();
                    AttrValueRec avRec = mAttrValueRecs.get(inx);
                    //Log.d(_CTAG+".custShap.draw","X="+String.format("%.2f",dataPoint.getX())+", Y="+String.format("%.2f",avRec.rY)+", V="+avRec.rValueName+", L="+avRec.rLikert+", I="+String.format("%.2f",avRec.rIntensityAvg)+", I%="+String.format("%.2f",avRec.rIntensityPct)+", Q="+avRec.rOrigRecs.size());
                    float size = 5.0f + 15.0f * ((float)avRec.rOrigRecsQtyActive / (float)mHighestAttrValueQtyRecs) * ZeoCompanionApplication.mScreenDensity;
                    paint.setColor(determineColor(avRec));
                    canvas.drawCircle(x, y, size, paint);
                }
            });
            mPointsSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    int inx = dataPoint.getIndex();
                    AttrValueRec avRec = mAttrValueRecs.get(inx);
                    for (AttrRec atRec: mAttrRecs) {
                        if (atRec.rID == avRec.rAttrID) {
                            String attr = atRec.rAttributeShortName;
                            if (atRec.rAttributeDisplayName != null) {
                                if (!atRec.rAttributeDisplayName.isEmpty()) {
                                    attr = atRec.rAttributeDisplayName;
                                }
                            }

                            SpannableStringBuilder builder = new SpannableStringBuilder();
                            SpannableString str1 = new SpannableString("Attribute: "+attr+", Value="+avRec.rValueName+"\nQty: "+avRec.rOrigRecsQtyActive+", Usefulness: ");
                            builder.append(str1);
                            SpannableString str2 = null;
                            if (avRec.rIntensityPct >= .8) {
                                str2 = new SpannableString("HIGH");
                            } else if (avRec.rIntensityPct >= .65) {
                                str2 = new SpannableString("GOOD");
                            } else if (avRec.rIntensityPct >= .50) {
                                str2 = new SpannableString("FAIR");
                            } else if (avRec.rIntensityPct >= .35) {
                                str2 = new SpannableString("NOT HELPFUL");
                            } else if (avRec.rIntensityPct >= .20) {
                                str2 = new SpannableString("NOT HELPFUL/POOR");
                            } else {
                                str2 = new SpannableString("NOT HELPFUL/DETRIMENTAL");
                            }
                            str2.setSpan(new ForegroundColorSpan(determineColor(avRec)), 0, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            builder.append(str2);

                            Toast toast = Toast.makeText(mContext, builder, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                            break;
                        }
                    }
                }
            });
            addSeries(mPointsSeries);
        }

        // now redraw the entire graph
        onDataChanged(false, false);
    }

    // determine the proper color to use
    private int determineColor(AttrValueRec avRec) {
        if (avRec.rIntensityPct >= .8) {
           return Color.RED;
        } else if (avRec.rIntensityPct >= .65) {
            return Color.YELLOW;
        } else if (avRec.rIntensityPct >= .50) {
            return Color.GREEN;
        } else if (avRec.rIntensityPct >= .35) {
            return Color.BLUE;
        } else if (avRec.rIntensityPct >= .20) {
            return Color.rgb(75,0,130);
        }
        return Color.BLACK;
    }

    // calculate the intensities of each attribute-value bucket based upon the current toggles
    private void calculateIntensities() {
        if (mAttrRecs == null) { return; }
        if (mAttrRecs.isEmpty()) { return; }

        // calculate the cutoff timestamp
        long cutoffTimestamp = (long)((double)(mHighestTimestamp - mLowestTimestamp) * mTimestampThresholdPct) + mLowestTimestamp - 43200000;   // less 12 hours
        //Log.d(_CTAG+".calcInten","LowestTimestamp="+mLowestTimestamp+", HighestTimestamp="+mHighestTimestamp+", CutoffTimestamp="+cutoffTimestamp);

        // stage 1: calculate all the new "fractional" ZQs for the entire bucketed dataset; note the "fractional" ZQ could be negative
        double lowestZQrecomp = 999999.0;
        double highestZQrecomp = -999999.0;
        mLowestZQ = 999999.0;
        mHighestZQ = -999999.0;
        mHighestAttrValueQtyRecs = 0;
        for (AttrRec atRec: mAttrRecs) {
            atRec.rValuesQtyActive = 0;
            for (Integer index: atRec.rValuesInx) {
                AttrValueRec avRec = mAttrValueRecs.get(index);
                avRec.rOrigRecsQtyActive = 0;
                for (AttrValsSleepDatasetRec odRec: avRec.rOrigRecs) {
                    // calculate the new "fractional" ZQ based upon the desired elements to include
                    odRec.rWorkingArray[0] = 0.0;   // per-record storage slot for the "fractional" ZQ
                    if (odRec.rTimestamp >= cutoffTimestamp) {
                        avRec.rOrigRecsQtyActive++;
                        if (mIncludeTotalSleep) { odRec.rWorkingArray[0] += (odRec.rDataArray[1] / 60.0); }
                        if (mIncludeREM) { odRec.rWorkingArray[0] += (odRec.rDataArray[3] / 60.0 / 2.0); }
                        if (mIncludeDeep) { odRec.rWorkingArray[0] += (odRec.rDataArray[5] / 60.0 * 1.5); }
                        if (mIncludeAwake) { odRec.rWorkingArray[0] -= (odRec.rDataArray[2] / 60.0 / 2.0); }
                        if (mIncludeAwakenings) { odRec.rWorkingArray[0] -= (odRec.rDataArray[6] / 15.0); }
                        odRec.rWorkingArray[0] = odRec.rWorkingArray[0] * 8.5;
                        if (odRec.rWorkingArray[0] < lowestZQrecomp) { lowestZQrecomp = odRec.rWorkingArray[0]; }
                        if (odRec.rWorkingArray[0] > highestZQrecomp) { highestZQrecomp = odRec.rWorkingArray[0]; }
                        if (odRec.rDataArray[7] < mLowestZQ) { mLowestZQ = odRec.rDataArray[7]; }
                        if (odRec.rDataArray[7] > mHighestZQ) { mHighestZQ = odRec.rDataArray[7]; }
                    }
                }
                if (avRec.rOrigRecsQtyActive > 0) {
                    atRec.rValuesQtyActive++;
                    if (avRec.rOrigRecsQtyActive > mHighestAttrValueQtyRecs) { mHighestAttrValueQtyRecs = avRec.rOrigRecsQtyActive; }
                }
            }
        }

        // calculate the "goodZQ" cutoff threshold; note again the ZQ range could span into the negatives
        double goodZQrecomp = (highestZQrecomp - lowestZQrecomp) * mGoodThresholdPct + lowestZQrecomp;
        //Log.d(_CTAG+".calcInten","LowZQ="+String.format("%.1f",lowestZQrecomp)+", HighZQ="+String.format("%.1f",highestZQrecomp)+", GoodZQ="+String.format("%.1f",goodZQrecomp));

        // stage 2: calculate IntensityAvgs for each attribute value bucket
        double lowestIntensityAvg = 999999.0;
        double highestIntensityAvg = -999999.0;
        for (AttrRec atRec: mAttrRecs) {
            if (atRec.rValuesQtyActive > 0) {
                for (Integer index: atRec.rValuesInx) {
                    AttrValueRec avRec = mAttrValueRecs.get(index);
                    if (avRec.rOrigRecsQtyActive > 0) {
                        avRec.rIntensityAvg = 0.0;
                        for (AttrValsSleepDatasetRec odRec: avRec.rOrigRecs) {
                            if (odRec.rTimestamp >= cutoffTimestamp) {
                                odRec.rWorkingArray[1] = (odRec.rWorkingArray[0] - goodZQrecomp);   // per-record storage slot for the intensity; it can be negative
                                avRec.rIntensityAvg += odRec.rWorkingArray[1];
                                //Log.d(_CTAG+".calcInten","Orig Rec: A="+atRec.rAttributeShortName+", V="+avRec.rValueName+", L="+avRec.rLikert+", ZQrecomp="+String.format("%.1f",odRec.rWorkingArray[0])+", Iavg="+String.format("%.1f",odRec.rWorkingArray[1]));
                            }
                        }
                        avRec.rIntensityAvg = avRec.rIntensityAvg / avRec.rOrigRecsQtyActive;
                        if (avRec.rIntensityAvg < lowestIntensityAvg) { lowestIntensityAvg = avRec.rIntensityAvg; }
                        if (avRec.rIntensityAvg > highestIntensityAvg) { highestIntensityAvg = avRec.rIntensityAvg; }
                    }
                }
            }
        }

        // calculate the entire range of intensity sums (ranges from negative to positive)
        double positiveIntensityAvgRange = 0.0;
        if (highestIntensityAvg > 0.0) {
            if (lowestIntensityAvg <= 0.0) { positiveIntensityAvgRange = highestIntensityAvg; }
            else { positiveIntensityAvgRange = highestIntensityAvg - lowestIntensityAvg; }
        }
        double negativeIntensityAvgRange = 0.0;
        if (lowestIntensityAvg < 0.0) {
            if (highestIntensityAvg >= 0.0) { negativeIntensityAvgRange = -lowestIntensityAvg; }
            else { negativeIntensityAvgRange = highestIntensityAvg - lowestIntensityAvg; }
        }
        //Log.d(_CTAG+".calcInten","LowIavg="+String.format("%.2f",lowestIntensityAvg)+", HighIavg="+String.format("%.2f",highestIntensityAvg)+", negIrange="+String.format("%.2f",negativeIntensityAvgRange)+", posIrange="+String.format("%.2f",positiveIntensityAvgRange));

        // stage 3: calculate the final intensityPct for each attribute balue bucket
        for (AttrRec atRec: mAttrRecs) {
            atRec.rHighestIntensityPct = -999999;
            if (atRec.rValuesQtyActive > 0) {
                for (Integer index: atRec.rValuesInx) {
                    AttrValueRec avRec = mAttrValueRecs.get(index);
                    if (avRec.rOrigRecsQtyActive > 0) {
                        if (mDatasetLen == 1) { avRec.rIntensityPct = 0.5; }
                        else if (avRec.rIntensityAvg >= 0.0) {
                            if (positiveIntensityAvgRange == 0.0) { avRec.rIntensityPct = 0.5; }
                            else {
                                double low = 0.0;
                                if (lowestIntensityAvg > 0.0) { low = lowestIntensityAvg; }
                                avRec.rIntensityPct = 0.5 + ((avRec.rIntensityAvg - low) / positiveIntensityAvgRange / 2.0);
                            }
                        } else {
                            if (negativeIntensityAvgRange == 0.0) { avRec.rIntensityPct = 0.5; }
                            else {
                                avRec.rIntensityPct = ((avRec.rIntensityAvg - lowestIntensityAvg) / negativeIntensityAvgRange / 2.0);
                            }
                        }
                        if (avRec.rIntensityPct > atRec.rHighestIntensityPct) { atRec.rHighestIntensityPct = avRec.rIntensityPct; }
                        //Log.d(_CTAG+".calcInten","A="+atRec.rAttributeShortName+", V="+avRec.rValueName+", L="+avRec.rLikert+", Iavg="+String.format("%.1f",avRec.rIntensityAvg)+", Ipct="+String.format("%.1f",avRec.rIntensityPct));
                    }
                }
            }
        }

        // stage 4: sort the attributes in descending rHighestIntensityPct order
        Collections.sort(mAttrRecs, new Comparator<AttrRec>() {
            @Override
            public int compare(AttrRec o1, AttrRec o2) {
                // configured for DESCENDING sort order
                if (o1.rHighestIntensityPct < o2.rHighestIntensityPct) {
                    return 1;
                }
                if (o1.rHighestIntensityPct > o2.rHighestIntensityPct) {
                    return -1;
                }
                return 0;
            }
        });
    }

    // determine how many letters of the Attribute names can be shown along the X-axis
    private void computeXlabeling() {
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
    }
}

