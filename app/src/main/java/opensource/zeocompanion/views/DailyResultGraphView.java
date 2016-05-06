package opensource.zeocompanion.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import opensource.zeocompanion.utility.Utilities;
import opensource.zeocompanion.zeo.ZAH_SleepRecord;

// displays a daily results graph
public class DailyResultGraphView extends GraphView {
    // member variables
    private  ArrayList<DRG_dataSet> mOrigDataSet = null;
    private BarGraphSeries<DataPoint> mDataBarSeries = null;
    private int mDatasetLen = 0;

    // member constants and other static content
    private static final String _CTAG = "DRG";

    // dataset record definition
    public static class DRG_dataSet {
        public String mParameter = null;
        public double mValue = 0;
        public double mValueToDivide = 0;
        public double mGoalValue = 0;
        public double mGoalPct = 0;

        public DRG_dataSet(String param, double value, double divisor, double goalValue, double goalPct) {
            mParameter = param;
            mValue = value;
            mValueToDivide = divisor;
            mGoalValue = goalValue;
            mGoalPct = goalPct;
        }
    }

    // constructors
    public DailyResultGraphView(Context context) {
        super(context);
        prepare();
    }
    public DailyResultGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepare();
    }
    public DailyResultGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        prepare();
    }

    // pass the dataset into the graphView for display
    public void setDataset( ArrayList<DRG_dataSet> theData) {
        // retain the dataset
        mOrigDataSet = theData;
        mDatasetLen = theData.size();

        // convert the dataset into points needed for the graphing library; Y-axis data is a percentage that can be greater than 100% is the case of the Total-Z time
        DataPoint[] theDataPoints = new DataPoint[mDatasetLen];
        double maxY = 0.0;
        for (int i = 0; i < mDatasetLen; i++) {
            DRG_dataSet item = theData.get(i);
            double pct = 100.0;
            if (item.mValueToDivide != 0) { pct = item.mValue / item.mValueToDivide * 100.0; }
            theDataPoints[i] = new DataPoint(i, i, pct);
            if (pct > maxY) { maxY = pct; }
        }

        // prepare the bargraph series
        this.removeAllSeries();
        mDataBarSeries = new BarGraphSeries<DataPoint>(theDataPoints);
        mDataBarSeries.setSpacing(10); // expressed as a percentage
        mDataBarSeries.setDrawValuesOnTop(true);
        mDataBarSeries.setValuesOnTopColor(Color.BLACK);

        // setup the callback that will be used to choose the color of each bar
        mDataBarSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                // pick the proper color depending upon the end-user's achievement toward their goals
                DRG_dataSet item = mOrigDataSet.get(data.getIndex());
                if (item.mParameter.equals("Awake")) { return Color.RED; }  // awake-time is always bad (therefore RED)
                double y = data.getY();
                if (y >= item.mGoalPct * .97) { return Color.rgb(0, 153, 0); }  // within 3% of goal?  then light Green
                else if (y < item.mGoalPct * .75) { return Color.RED; }  // less than 75% of goal?  then RED
                return Color.YELLOW;    // otherwise Yellow
            }
        });

        // add the bargaph series to the graph
        this.addSeries(mDataBarSeries);

        // setup variable parameters; first the X-axis labeling
        GridLabelRenderer render = this.getGridLabelRenderer();
        Viewport viewport = this.getViewport();
        double maxX = (double) (mDatasetLen - 1);
        viewport.setMaxX(maxX + .5);
        render.setHorizontalLabelsStartX(0.0);
        render.setHorizontalLabelsEndX(maxX);
        render.setNumHorizontalLabels(mDatasetLen);

        // setup the y-axis label formatter; the y-axis labels are not specific to the particular datapoint even when shown above the bar
        DefaultLabelFormatter yAxisFormatter = new DefaultLabelFormatter() {
            @Override
            public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
                if (isValueX) { return ""; }    // should never get called
                else { return super.formatLabelEx(reason, index, value, isValueX)+"%"; } // show normal y values with a percent sign
            }
        };

        // setup the Static Labeling formatter; only the x-axis will have static labels;
        // the static x-axis label shows the parameter name along with the total amount of time in that parameter
        StaticLabelsFormatter slFormatter = new StaticLabelsFormatter(this);
        String[] xAxisStrings = new String[mDatasetLen];
        for (int i=0; i<mDatasetLen; i++) {
            DRG_dataSet item = mOrigDataSet.get(i);
            String str = item.mParameter;
            if (i == 0) { str = str + "\n" + Utilities.showTimeInterval(item.mValue, false); }
            else { str = str + "\n" + String.format("%.0fm", item.mValue); }
            xAxisStrings[i] = str;
        }
        slFormatter.setHorizontalLabels(xAxisStrings);
        slFormatter.setDynamicLabelFormatter(yAxisFormatter);
        render.setLabelFormatter(slFormatter);

        // adjust the Y-axis for both ensuring the upper-label is on-graph and if the Total-Z percentage is greater than 100%
        double endY = 100.0;
        int numLabels = 5;
        if (maxY <= 90.0) {
            maxY = 110;     // account for extra space needed at top so the label "100%" does not overflow
        }  else if (maxY <= 100.0) {
            maxY = 115.0;   // account for extra space needed at top so the text-on-top does not overflow
        } else {
            // maxY is greater than 100%; want to compute the proper quarter segment
            double pctFloor = Math.floor(maxY / 100.0) * 100.0;
            double subPct = Math.floor(maxY - pctFloor);
            double subQtr = Math.floor((subPct - 1.0) / 25.0) + 1.0;
            endY = pctFloor + subQtr * 25.0;
            maxY = endY + 15.0;   // account for extra space needed at top so the "100%" does not overflow
            numLabels = (int)((Math.floor(endY / 25.0)) + 1.0);
        }
        viewport.setMaxY(maxY);
        render.setVerticalLabelsStartY(0.0);
        render.setVerticalLabelsEndY(endY);
        render.setNumVerticalLabels(numLabels);
    }

    // pre-defined non-variable settings for the graph
    private void prepare() {
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setLabelsSpace(10);
        render.setHorizontalLabelsVisible(true);
        render.setVerticalLabelsVisible(true);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(-0.5);
        viewport.setMaxX(4.5);
        viewport.setMinY(0.0);
        viewport.setMaxY(110.0);
    }

    // prepare to create a bitmap rather than a view
    public void prepDrawToCanvas(int width, int height) {
        this.setLeft(0);
        this.setRight(width);
        this.setTop(0);
        this.setBottom(height);
        return;
    }
}

