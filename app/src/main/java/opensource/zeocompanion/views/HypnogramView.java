package opensource.zeocompanion.views;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import opensource.zeocompanion.ZeoCompanionApplication;
import opensource.zeocompanion.database.CompanionDatabaseContract;
import opensource.zeocompanion.database.CompanionSleepEpisodeEventsParsedRec;
import opensource.zeocompanion.zeo.ZAH_SleepRecord;

// displays a hypnogram, or creates a bitmap of a hypnogram
public class HypnogramView extends GraphView {
    // member variables
    private byte[] mTheData = null;
    private ArrayList<CompanionSleepEpisodeEventsParsedRec> mTheEvents = null;
    private BarGraphSeries<DataPoint> mHypnoSeries = null;
    private PointsGraphSeries<DataPoint> mEventSeries = null;
    private int[] mBarsGraphIndexMap = null;
    private int[] mPointsGraphIndexMap = null;
    private int mShowAsMode = 0;
    private long mDisplayStart_Timestamp = 0L;
    private int mHypnoSeriesSpacing = 0;
    private int mTheData_len = 0;
    private int mDataPoints_len = 0;
    private int mEpochInSec = 0;
    private double mScaledSpan = 10.167;

    // member constants and other static content
    private static final String _CTAG = "HG";
    private SimpleDateFormat mDF1 = new SimpleDateFormat("h:mm");
    private SimpleDateFormat mDF1s = new SimpleDateFormat("h:mm:ss");
    private SimpleDateFormat mDF2 = new SimpleDateFormat("h:mm a");
    private SimpleDateFormat mDF2s = new SimpleDateFormat("h:mm:ss a");

    // constructors
    public HypnogramView(Context context) { super(context); }
    public HypnogramView(Context context, AttributeSet attrs) { super(context, attrs); }
    public HypnogramView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mViewport.onTouchEvent(event);
        boolean a = super.onTouchEvent(event);
        return b || a;
    }

    // shown without labels (History Tab)
    public void showAsCompact() {
        mShowAsMode = 1;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(0);
        render.setHorizontalLabelsVisible(false);
        render.setVerticalLabelsVisible(false);
        render.setLabelsSpace(0);
        render.setGridStyle(GridLabelRenderer.GridStyle.NONE);

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setAxisMinX(0.0);
        viewport.setAxisMaxX(10.167);
        viewport.setAxisMinY(0.0);
        viewport.setAxisMaxY(4.0);

        mHypnoSeriesSpacing = 10;   // expressed as a percent
    }

    // shown with labels (History Details screen)
    public void showAsDetailed() {
        mShowAsMode = 2;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(false);   // this will be overrided in setDataset() call
        render.setVerticalLabelsVisible(true);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setLabelsSpace(10);
        render.setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);
        render.setHorizontalLabelsStartX(0.0);
        render.setHorizontalLabelsEndX(9.0);
        render.setNumHorizontalLabels(10);
        render.setVerticalLabelsStartY(1.0);
        render.setVerticalLabelsEndY(4.0);
        render.setNumVerticalLabels(4);

        render.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
                switch (reason) {
                    case SIZING:
                    case SIZING_MAX:
                    case SIZING_MIN:
                        // return the largest sized label
                        if (isValueX) {
                            return "10h\n00:00";
                        } else {
                            return "Awake";
                        }

                    case AXIS_STEP:
                    case AXIS_STEP_SECONDSCALE:
                    case DATA_POINT:
                    default:
                        double dIvalue = Math.floor(value);
                        int iIvalue = (int) dIvalue;
                        if (isValueX) {
                            // show integral X values along with a time
                            String str = String.valueOf(iIvalue)+"h\n";
                            Date dt = new Date(mDisplayStart_Timestamp + ((long)iIvalue * 3600000L));
                            if (index == 0) { str = str + mDF1s.format(dt); }
                            else { str = str + mDF1.format(dt); }
                            return str;
                        } else {
                            // show the sleep stage name
                            switch (iIvalue) {
                                case 5:
                                    return "Event";
                                case 4:
                                    return "Awake";
                                case 3:
                                    return "REM";
                                case 2:
                                    return "Light";
                                case 1:
                                    return "Deep";
                                default:
                                    return "Unknown";
                            }
                        }
                }
            }
        });

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setAxisMinX(0.0);
        viewport.setAxisMaxX(10.167);
        viewport.setAxisMinY(0.0);
        viewport.setAxisMaxY(4.5);

        mHypnoSeriesSpacing = 10;   // expressed as a percent
    }

    // shown for export via sharing
    public void showAsSharedDetailed(String title) {
        mShowAsMode = 4;
        if (title != null) {
            if (!title.isEmpty()) {
                setTitle(title);
                setTitleTextSize(48);
                setTitleColor(Color.WHITE);
            }
        }

        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(false);   // this will be overrided in setDataset() call
        render.setVerticalLabelsVisible(true);
        render.setTextSize(36);
        render.setHorizontalAxisTitleTextSize(36);
        render.setVerticalAxisTitleTextSize(36);
        render.setVerticalLabelsColor(Color.WHITE);
        render.setHorizontalLabelsColor(Color.WHITE);
        render.setLabelsSpace(5);
        render.setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);
        render.setHorizontalLabelsStartX(0.0);
        render.setHorizontalLabelsEndX(9.0);
        render.setNumHorizontalLabels(10);
        render.setVerticalLabelsStartY(1.0);
        render.setVerticalLabelsEndY(4.0);
        render.setNumVerticalLabels(4);
        render.reloadStyles();

        render.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
                switch (reason) {
                    case SIZING:
                    case SIZING_MAX:
                    case SIZING_MIN:
                        // return the largest sized label
                        if (isValueX) {
                            return "10h";
                        } else {
                            return "Awake";
                        }

                    case AXIS_STEP:
                    case AXIS_STEP_SECONDSCALE:
                    case DATA_POINT:
                    default:
                        double dIvalue = Math.floor(value);
                        int iIvalue = (int) dIvalue;
                        if (isValueX) {
                            return String.valueOf(iIvalue)+"h";
                        } else {
                            // show the sleep stage name
                            switch (iIvalue) {
                                case 5:
                                    return "Event";
                                case 4:
                                    return "Awake";
                                case 3:
                                    return "REM";
                                case 2:
                                    return "Light";
                                case 1:
                                    return "Deep";
                                default:
                                    return "Unknown";
                            }
                        }
                }
            }
        });

        render.setVerticalLabelsDependentColor(new GridLabelRenderer.VerticalLabelsDependentColor() {
            @Override
            public int getColor(int index, double value, boolean isValueX) {
                double dIvalue = Math.floor(value);
                int iIvalue = (int) dIvalue;
                if (isValueX) {
                    return Color.BLACK;
                } else {
                    // show the sleep stage name
                    switch (iIvalue) {
                        case 5:
                            return Color.WHITE;
                        case 4:
                            return Color.RED;
                        case 3:
                            return Color.rgb(0, 153, 0);
                        case 2:
                            return Color.rgb(102, 178, 255);
                        case 1:
                            return Color.rgb(102, 102, 255);
                        default:
                            return Color.BLACK;
                    }
                }
            }
        });

        Viewport viewport = this.getViewport();
        viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setAxisMinX(0.0);
        viewport.setAxisMinY(0.0);
        viewport.setAxisMaxY(4.5);

        mHypnoSeriesSpacing = 10;   // expressed as a percent
    }

    // shown with expanded X-axis labels (30-sec scrollable hypnogram popup)
    public void showAsExpanded() {
        mShowAsMode = 3;
        GridLabelRenderer render = this.getGridLabelRenderer();
        render.setPadding(10);
        render.setHorizontalLabelsVisible(false);   // this will be overrided in setDataset() call
        render.setVerticalLabelsVisible(true);
        render.setLabelsSpace(10);
        render.setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);
        render.setVerticalLabelsStartY(1.0);
        render.setVerticalLabelsEndY(4.0);
        render.setNumVerticalLabels(4);

        render.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabelEx(GridLabelRenderer.LabelFormatterReason reason, int index, double value, boolean isValueX) {
                switch (reason) {
                    case SIZING:
                    case SIZING_MAX:
                    case SIZING_MIN:
                        // return the largest sized label
                        if (isValueX) {
                            return "10h:59m\n00:00 AM";
                        } else {
                            return "Awake";
                        }

                    case AXIS_STEP:
                    case AXIS_STEP_SECONDSCALE:
                    case DATA_POINT:
                    default:
                        double dIvalue = Math.floor(value);
                        int iIvalue = (int) dIvalue;
                        if (isValueX) {
                            // show x values as h:m along with a time
                            String str = "";
                            int minutes = (int) ((value - dIvalue) * 60.0);
                            if (value == 0.0) {
                                str = str + "0m";
                            } else if (value >= 1.0) {
                                str = str + String.format("%dh", iIvalue);
                            }
                            if (minutes > 0) {
                                if (str.length() > 0) {
                                    str = str + ":";
                                }
                                str = str + String.format("%dm", minutes);
                            }
                            str = str + "\n";
                            Date dt = new Date(mDisplayStart_Timestamp + ((((long) iIvalue * 60L) + (long) minutes) * 60000L));
                            if (index == 0) { str = str + mDF2s.format(dt); }
                            else { str = str + mDF2.format(dt); }

                            return str;
                        } else {
                            // show the sleep stage name
                            switch (iIvalue) {
                                case 5:
                                    return "Event";
                                case 4:
                                    return "Awake";
                                case 3:
                                    return "REM";
                                case 2:
                                    return "Light";
                                case 1:
                                    return "Deep";
                                default:
                                    return "Unknown";
                            }
                        }
                }
            }
        });

        // get device current orientation information
        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String spanString;
        if (screenSize.x > screenSize.y) {
            spanString = mPrefs.getString("hypno_30sec_initial_window_landscape", "3 hours");

        } else {
            spanString = mPrefs.getString("hypno_30sec_initial_window_portrait", "2 hours");
        }

        String[] split = spanString.split(" ");
        mScaledSpan = 0.0;
        if (split[1].equals("min")) { mScaledSpan = Double.parseDouble(split[0]) / 60.0; }
        else { mScaledSpan = Double.parseDouble(split[0]); }
        double maxHours =  ((double)screenSize.x - 150.0) / 2.0 / 60.0;
        if (mScaledSpan < 0.15) { mScaledSpan = 0.15; }
        if (mScaledSpan > 6.0) { mScaledSpan = 6.0; }
        if (mScaledSpan > maxHours) {
            double maxHour = Math.floor(maxHours);
            if (maxHours > maxHour + 0.5) { maxHour = maxHour + 0.5; }
            mScaledSpan = maxHour;
        }

        Viewport viewport = this.getViewport();
        //viewport.setBackgroundColor(Color.LTGRAY);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setAxisMinY(0.0);
        viewport.setAxisMaxY(4.5);

        viewport.setAxisMinX(0.0);
        viewport.setAxisMaxX(10.167);
        viewport.setMaxX(mScaledSpan);
        render.setHorizontalLabelsStartX(0.0);
        render.setHorizontalLabelsEndX(9.0);    // recomputed in setDataset call
        render.setNumHorizontalLabels(43);      // recomputed in setDataset call

        viewport.setMinimumScaleWidth((float)0.083333);     // 5 minutes of minimum width
        viewport.setScrollable(true);
        viewport.setScalable(true);
        mHypnoSeriesSpacing = 10;   // expressed as a percent
    }

    // set the data for the hypnogram; theEvents can be null;
    public void setDataset(long displayStartTimestamp, int data_is_epoch_in_sec, int alter_to_epoch_in_sec, byte[] theData, boolean fixAtStdSize, ArrayList<CompanionSleepEpisodeEventsParsedRec> theEvents) {
        mTheData = theData;
        mTheData_len = theData.length;
        mTheEvents = theEvents;
        mEpochInSec = alter_to_epoch_in_sec;
        mDisplayStart_Timestamp = displayStartTimestamp;
        Viewport viewport = this.getViewport();
        GridLabelRenderer render = this.getGridLabelRenderer();
        double hoursCvtr_theData = (double)data_is_epoch_in_sec / 3600.0;

        // trim away trailing "undefined"
        int i = mTheData_len - 1;
        while (i > 0 && theData[i] <= ZAH_SleepRecord.ZAH_HYPNOGRAM_UNDEFINED) { i--; }
        mTheData_len = i + 1;

        // convert the hypnogram data into bargraph series data points;
        // also since the detailed hypnogram' quantity of bars can be smaller than the display size in pixels,
        // may need to reduce to effectively 60-sec or 90-sec epochs/bars by just skipping 1 or 2 bars per shown bar
        int newSize = mTheData_len;
        if (alter_to_epoch_in_sec == 90) { newSize = (mTheData_len / 3) + 1; }
        else if (alter_to_epoch_in_sec == 60) { newSize = (mTheData_len / 2) + 1; }
        double offset =((double)mEpochInSec) / 3600.0 / 2.0;
        DataPoint[] theDataPoints = new DataPoint[newSize];
        mBarsGraphIndexMap = new int[newSize];
        mDataPoints_len = 0;
        i = 0;
        while (i < mTheData_len && mDataPoints_len < newSize) {
            double hour = (hoursCvtr_theData * (double)i) + offset;
            switch (theData[i]) {
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_WAKE:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 4.0);
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_REM:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 3.0);
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 2.0);
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT_TO_DEEP:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 1.5);
                    break;
                case ZAH_SleepRecord.ZAH_HYPNOGRAM_DEEP:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 1.0);
                    break;
                default:
                    theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 0.0);
                    break;
            }
            mBarsGraphIndexMap[mDataPoints_len] = i;
            if (alter_to_epoch_in_sec == 90) { i = i + 2; }
            else if (alter_to_epoch_in_sec == 60) { i++; }
            mDataPoints_len++;
            i++;
        }
        while (mDataPoints_len < newSize) {
            double hour = (hoursCvtr_theData * (double)i) + offset;
            theDataPoints[mDataPoints_len] = new DataPoint(mDataPoints_len, hour, 0.0);
            mDataPoints_len++;
            i++;
        }

        // prepare the bargraph series
        this.removeAllSeries();
        mHypnoSeries = new BarGraphSeries<DataPoint>(theDataPoints);
        mHypnoSeries.setSpacing(mHypnoSeriesSpacing);

        // setup a callback to choose the color of the bar
        mHypnoSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                int inx1 = data.getIndex();
                if (inx1 < mDataPoints_len) {
                    int inx2 = mBarsGraphIndexMap[inx1];
                    switch (mTheData[inx2]) {
                        case ZAH_SleepRecord.ZAH_HYPNOGRAM_WAKE:
                            return Color.RED;
                        case ZAH_SleepRecord.ZAH_HYPNOGRAM_REM:
                            return Color.rgb(0, 153, 0);
                        case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT:
                            return Color.rgb(102, 178, 255);
                        case ZAH_SleepRecord.ZAH_HYPNOGRAM_LIGHT_TO_DEEP:
                            //return Color.rgb(102, 102, 255);
                            // allow to "fall-thru"
                        case ZAH_SleepRecord.ZAH_HYPNOGRAM_DEEP:
                            return Color.rgb(0, 0, 204);
                        default:
                            return Color.WHITE;
                    }
                } else {
                    return Color.WHITE;
                }
            }
        });

        // add the bargraph series to the graph
        this.addSeries(mHypnoSeries);

        // are the events to annotate the hypnogram with?
        if (theEvents != null) {
            if (!theEvents.isEmpty()) {
                // yes; count those events that should be shown
                int c = 0;
                for (CompanionSleepEpisodeEventsParsedRec eRec: theEvents) {
                    if (eRec.rTimestamp >= mDisplayStart_Timestamp) {
                        if (eRec.rEventNo >= CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP &&
                                eRec.rEventNo < CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING) { c++; }
                    }
                }
                if (c > 0) {
                    // there are indeed events to-be-shown;
                    // construct the datapoints that the graphing library can utilize;
                    // as a Y-axis, the events are stored as the fractional component of a "5.x" vertical value so as to be above all the hypnogram bars
                    DataPoint[] evtDataPoints = new DataPoint[c];
                    mPointsGraphIndexMap = new int[c];
                    c = 0;
                    double maxEvtCode = 0.0;
                    for (i = 0; i < theEvents.size(); i++) {
                        CompanionSleepEpisodeEventsParsedRec eRec = theEvents.get(i);
                        if (eRec.rTimestamp >= mDisplayStart_Timestamp) {
                            if (eRec.rEventNo >= CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP &&
                                    eRec.rEventNo < CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_DONE_SLEEPING) {
                                double interval_hour = (double)(eRec.rTimestamp - mDisplayStart_Timestamp) / 3600000.0;
                                double evtCode = 5.0 + ((double) eRec.rEventNo / 100.0);
                                if (evtCode > maxEvtCode) { maxEvtCode = evtCode; }
                                evtDataPoints[c] = new DataPoint(c, interval_hour, evtCode);
                                mPointsGraphIndexMap[c] = i;
                                c++;
                            }
                        }
                    }

                    // construct a points graph series for the events
                    mEventSeries = new PointsGraphSeries<DataPoint>(evtDataPoints);
                    mEventSeries.setColor(Color.RED);   // default color of event text will be red

                    // set a custom shape callback that will place the event's text code letter and color
                    mEventSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            CompanionSleepEpisodeEventsParsedRec eRec = mTheEvents.get(mPointsGraphIndexMap[dataPoint.getIndex()]);
                            String letter = "";
                            switch (eRec.rEventNo) {
                                case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_GOING_TO_SLEEP:
                                    paint.setColor(Color.rgb(0, 0, 204));
                                    letter = "T";
                                    break;
                                case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_STILL_AWAKE:
                                    paint.setColor(Color.RED);
                                    letter = "S";
                                    break;
                                case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP:
                                    paint.setColor(Color.RED);
                                    letter = "W";
                                    break;
                                case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_DID_SOMETHING:
                                    paint.setColor(Color.RED);
                                    letter = "D";
                                    break;
                                case CompanionDatabaseContract.CompanionSleepEpisodes.SLEEP_EPISODE_EVENT_WOKEUP_RETRY_TO_SLEEP:
                                    paint.setColor(Color.rgb(0, 0, 204));
                                    letter = "R";
                                    break;
                            }
                            if (!letter.isEmpty()) {
                                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                                paint.setTextSize(16 * ZeoCompanionApplication.mScreenDensity);
                                canvas.drawText(letter, x, y + (10 * ZeoCompanionApplication.mScreenDensity), paint);
                            }
                        }
                    });

                    // adjust the y-axis labeling
                    viewport.setAxisMaxY(maxEvtCode + 0.25);
                    render.setVerticalLabelsEndY(5.0);
                    render.setNumVerticalLabels(5);

                    // add the points series to the graph
                    this.addSeries(mEventSeries);
                }
            }
        }

        // determine length of the hypnogram
        double maxX = ((double)(mEpochInSec * mDataPoints_len) + ((double)(mEpochInSec / 2))) / 3600.0;
        switch (mShowAsMode) {
            case 1:
                // compact
                render.setHorizontalLabelsVisible(false);
                break;
            case 2:
                // detailed
                render.setHorizontalLabelsVisible(true);
                if (fixAtStdSize && maxX < 10.167) { maxX = 10.167; }
                int maxEnd1 = ((int)Math.floor(maxX)) - 1;
                if (maxEnd1 < 1) { maxEnd1 = 1; }
                render.setHorizontalLabelsEndX(maxEnd1);
                render.setNumHorizontalLabels(maxEnd1 + 1);
                viewport.setAxisMaxX(maxX);
                break;
            case 3:
                // expanded-scrollable
                render.setHorizontalLabelsVisible(true);
                WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = windowManager.getDefaultDisplay();
                Point screenSize = new Point();
                display.getSize(screenSize);

                float maxScaleX = (float)(((double)(mEpochInSec * (screenSize.x - 200)) + ((double)(mEpochInSec / 2))) / 3600.0);
                viewport.setAxisMaxX(maxX);
                viewport.setMaxX(mScaledSpan);
                viewport.setMaximumScaleWidth(maxScaleX);
                setLabelsPerScale();
                break;
            case 4:
                // shared detailed
                render.setHorizontalLabelsVisible(true);
                if (fixAtStdSize && maxX < 10.167) { maxX = 10.167; }
                maxX = Math.ceil(maxX);
                int maxEnd2 = (int)maxX - 1;
                if (maxEnd2 < 1) { maxEnd2 = 1; }
                render.setHorizontalLabelsEndX(maxEnd2);
                render.setNumHorizontalLabels(maxEnd2 + 1);
                viewport.setAxisMaxX(maxX);
                break;
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

    // set a scrolling and scaling callback listener
    public void setScrollScaleListener(long callbackNumber, Viewport.ScrollScaleListener listener) {
        mParentNumber = callbackNumber;
        Viewport viewport = this.getViewport();
        viewport.setScrollScaleListener(listener);
    }

    public void setLabelsPerScale() {
        Viewport viewport = this.getViewport();
        GridLabelRenderer render = this.getGridLabelRenderer();
        double span = viewport.getMaxX(false) - viewport.getMinX(false);
        double maxX = ((double)(mEpochInSec * mDataPoints_len) + ((double)(mEpochInSec / 2))) / 3600.0;

        int maxEnd2 = ((int)Math.floor(maxX));
        if (maxEnd2 < 1) { maxEnd2 = 1; }

        Log.d(_CTAG+".setLabelsPerScale","MinX="+viewport.getMaxX(false)+", MaxX="+viewport.getMinX(false)+" Span="+span+" maxEnd2="+maxEnd2);

        if (span >= 3.0) {
            render.setHorizontalLabelsEndX((double)maxEnd2);
            render.setNumHorizontalLabels(maxEnd2 + 1);
        } else if (span >= 2.0) {
            render.setHorizontalLabelsEndX((double)maxEnd2 + 0.5);
            render.setNumHorizontalLabels((maxEnd2 + 1)*2);
        } else if (span >= 0.50) {
            render.setHorizontalLabelsEndX((double)maxEnd2 + 0.75);
            render.setNumHorizontalLabels((maxEnd2 + 1)*4);
        } else {
            render.setHorizontalLabelsEndX((double)maxEnd2 + 0.917);
            render.setNumHorizontalLabels((maxEnd2 + 1)*12);
        }
    }
}
