/**
 * GraphView
 * Copyright (C) 2014  Jonas Gehring
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * with the "Linking Exception", which can be found at the license.txt
 * file in this program.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * with the "Linking Exception" along with this program; if not,
 * write to the author Jonas Gehring <g.jjoe64@gmail.com>.
 */
package com.jjoe64.graphview.series;

import android.graphics.PointF;

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Basis implementation for series.
 * Used for series that are plotted on
 * a default x/y 2d viewport.
 *
 * Extend this class to implement your own custom
 * graph type.
 *
 * This implementation uses a internal Array to store
 * the data. If you want to implement a custom data provider
 * you may want to implement {@link Series}.
 *
 * @author mmaschino
 * * This particular source code file is licensed per overall GraphView's license
 */
public abstract class BaseMultiSeries<E extends DataPointInterface> implements MultiSeries<E> {
    /**
     * holds the subseries of data; the first subseries holds the sum of Y-values and the X-values that will be used
     */
    final private List mSubseries = new ArrayList<ArrayList<E>>();
    final private ArrayList<String> mSubseriesTitle = new ArrayList<String>();
    final private ArrayList<Integer> mSubseriesColor = new ArrayList<Integer>();

    /**
     * stores the used coordinates to find the
     * corresponding data point on a tap
     *
     * Key => x/y pixel
     * Value => Plotted Datapoint
     *
     * will be filled while drawing via {@link #registerDataPoint(float, float, DataPointInterface)}
     */
    private Map<PointF, E> mDataPoints = new HashMap<PointF, E>();

    /**
     * listener to handle tap events on a data point
     */
    protected OnDataPointTapListener mOnDataPointTapListener;

    /**
     * stores the graphviews where this series is used.
     * Can be more than one.
     */
    private List<GraphView> mGraphViews;

    /**
     * creates series without any data subseries yet
     */
    public BaseMultiSeries() {
        mGraphViews = new ArrayList<GraphView>();
    }

    /**
     * creates series with one initial data subseries
     *
     * @param data  data points
     *              important: array has to be sorted from lowest x-value to the highest
     */
    public BaseMultiSeries(E[] data) {
        mGraphViews = new ArrayList<GraphView>();
        addSubseries(data);
    }

    /**
     * @param data another data subseries to add
     *
     * note that for all subseries other than the first, the X-values are ignored; X-values are always used solely from the first subseries;
     * note that all Y-values must be of the same units-of-measure in all subseries, and need to be additive
     */
    @Override
    public void addSubseries(E[] data) {
        // are there any subseries yet?
        if (mSubseries.size() == 0) {
            // nope, create the Sum-of-Y first subseries; this is also the X-values and index reference
            ArrayList<E> sumSubseries = new ArrayList<E>();
            int j = 0;
            for (E d : data) {
                E sumDP = (E)new DataPoint(d.getIndex(), d.getX(), 0.0d);
                sumDP.setPositionInSeries(j);
                sumSubseries.add(sumDP);
                j++;
            }
            mSubseries.add(sumSubseries);
            mSubseriesTitle.add(null);
            mSubseriesColor.add(new Integer(0xff0077cc));
        }

        // record the new subseries; including summing the new Y-values
        ArrayList<E> subSeries0 = (ArrayList<E>)mSubseries.get(0);
        ArrayList<E> newSubseries = new ArrayList<E>();
        int j = 0;
        for (E d : data) {
            d.setPositionInSeries(j);
            newSubseries.add(d);
            E sd = subSeries0.get(j);
            sd.addToY(d.getY());
            j++;
        }
        mSubseries.add(newSubseries);
        mSubseriesTitle.add(null);
        mSubseriesColor.add(new Integer(0xff0077cc));
    }

    /**
     * @return  number of stored subseries
     *
     */
    @Override
    public int getQtySubseries() {
        if ( mSubseries.size() == 0) { return 0; }
        return mSubseries.size() - 1;
    }

    /**
     * @return whether there are data points
     */
    @Override
    public boolean isEmpty() {
        return mSubseries.isEmpty();
    }

    /**
     * @return quantity of data points for baseline subseries 0
     */
    @Override
    public int size() {
        if ( mSubseries.size() == 0) { return 0; }
        ArrayList<E> subSeries0 = (ArrayList<E>)mSubseries.get(0);
        return subSeries0.size();
    }

    /**
     * @return quantity of data points for specified subseries; should be the same for all subseries and summed-Y subseries0
     */
    public int size(int subseries) {
        if ( mSubseries.size() == 0) { return 0; }
        ArrayList<E> oneSubSeries = (ArrayList<E>)mSubseries.get(subseries + 1);
        return oneSubSeries.size();
    }

    /**
     * clear all the subseries usually in preparation for re-adding new subseries
     */
    @Override
    public void clearAllSubseries() {
        for (int i = 0; i < mSubseries.size(); i++) {
            ArrayList<E> oneSubseries = (ArrayList<E>)mSubseries.get(i);
            oneSubseries.clear();
        }
        mSubseries.clear();
    }

    /**
     * called when the series was added to a graph
     *
     * @param graphView graphview that is using this multi-series
     */
    @Override
    public void onGraphViewAttached(GraphView graphView) {
        mGraphViews.add(graphView);
    }

    /**
     * informs all linked GraphViews to refresh themselves
     */
    public void updateAllGraphViews() {
        for (GraphView gv : mGraphViews) {
            gv.onDataChanged(true, false);
        }
    }

    /**
     * @param subseries the subseries number to set (starts at 0; -1 will access the summed-Y series)
     * @param color the color to set
     */
    @Override
    public void setColor(int subseries, int color) {
        mSubseriesColor.set(subseries + 1, new Integer(color));
    }

    /**
     * @param subseries the subseries number to get (starts at 0; -1 will access the summed-Y series)
     * @return  the color of the subseries. Used in the legend and should
     *          be used for the plotted points or lines.
     */
    @Override
    public int getColor(int subseries) {
        Integer val = mSubseriesColor.get(subseries + 1);
        return val;
    }

    /**
     * @param subseries the subseries number to set (starts at 0; -1 will access the summed-Y series)
     * @param title the text string to set
     */
    @Override
    public void setTitle(int subseries, String title) {
        mSubseriesTitle.set(subseries + 1, title);
    }

    /**
     * @param subseries the subseries number to get (starts at 0; -1 will access the summed-Y series)
     * @return  the text title of the subseries. Used in the legend. Can be null.
     */
    @Override
    public String getTitle(int subseries) {
        return mSubseriesTitle.get(subseries + 1);
    }

    /**
     * @return the individual Y values at the indicated position
     */
    public double[] getValueYs(int position) {
        if (mSubseries.isEmpty()) return null;
        double[] yValues = new double[getQtySubseries()];
        for (int i = 1; i < mSubseries.size(); i++) {
            ArrayList<E> oneSubseries = (ArrayList<E>)mSubseries.get(i);
            if (position < oneSubseries.size()) { yValues[i - 1] = oneSubseries.get(position).getY(); }
            else {  yValues[i - 1] = 0.0; }
        }
        return yValues;
    }

    /**
     * @return the Y value at the indicated position in the indicated subseries
     */
    public double getValueY(int subseries, int position) {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> oneSubseries = (ArrayList<E>)mSubseries.get(subseries + 1);
        return oneSubseries.get(position).getY();
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // the following methods are from the Series interface and should represent the sum of the Y values in all the subseries;
    // most of these will not be used by StackedBarGraphSeries but are required;
    // these will also allow single-Y-value series to be based upon MultiSeries if so desired,
    // allowing re-use of the MultiSeries simultaneously for single-series and multi-series implementations
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return the summed-Y value at the indicated position
     */
    public double getValueY(int position) {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        return subseries0.get(position).getY();
    }

    /**
     * @return the lowest x value, or 0 if there is no data
     */
    public double getLowestValueX() {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        return subseries0.get(0).getX();
    }

    /**
     * @return the highest x value, or 0 if there is no data
     */
    public double getHighestValueX() {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        return subseries0.get(subseries0.size()-1).getX();
    }

    /**
     * @return the lowest summed-Y value, or 0 if there is no data
     */
    public double getLowestValueY() {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        double low = subseries0.get(0).getY();
        for (int j = 1; j < subseries0.size(); j++) {
            double ySum = subseries0.get(j).getY();
            if (ySum < low) { low = ySum; }
        }
        return low;
    }

    /**
     * @return the highest summed-Y value, or 0 if there is no data
     */
    public double getHighestValueY() {
        if (mSubseries.isEmpty()) return 0d;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        double high = subseries0.get(0).getY();
        for (int j = 1; j < subseries0.size(); j++) {
            double ySum = subseries0.get(j).getY();
            if (ySum > high) { high = ySum; }
        }
        return high;
    }

    /**
     * @return array of data points based upon the summed-Y values
     */
    public DataPoint[] toArray() {
        if (mSubseries.isEmpty()) return null;
        ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        DataPoint[] points = new DataPoint[subseries0.size()];
        return (DataPoint[])subseries0.toArray(points);
    }

    /**
     * get the summed-Y values for a given x range. if from and until are bigger or equal than
     * all the data, the original data is returned.
     * If it is only a part of the data, the range is returned plus one datapoint
     * before and after to get a nice scrolling.
     *
     * @param from minimal x-value
     * @param until maximal x-value
     * @return data for the range +/- 1 datapoint
     */
    @Override
    public Iterator<E> getValues(final double from, final double until) {
        if (mSubseries.isEmpty()) return null;
        final ArrayList<E> subseries0 = (ArrayList<E>)mSubseries.get(0);
        if (from <= getLowestValueX() && until >= getHighestValueX()) {
            return subseries0.iterator();
        } else {
            return new Iterator<E>() {
                Iterator<E> org = subseries0.iterator();
                E nextValue = null;
                E nextNextValue = null;
                boolean plusOne = true;

                {
                    // go to first
                    boolean found = false;
                    E prevValue = null;
                    if (org.hasNext()) {
                        prevValue = org.next();
                    }
                    if (prevValue.getX() >= from) {
                        nextValue = prevValue;
                        found = true;
                    } else {
                        while (org.hasNext()) {
                            nextValue = org.next();
                            if (nextValue.getX() >= from) {
                                found = true;
                                nextNextValue = nextValue;
                                nextValue = prevValue;
                                break;
                            }
                            prevValue = nextValue;
                        }
                    }
                    if (!found) {
                        nextValue = null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public E next() {
                    if (hasNext()) {
                        E r = nextValue;
                        if (r.getX() > until) {
                            plusOne = false;
                        }
                        if (nextNextValue != null) {
                            nextValue = nextNextValue;
                            nextNextValue = null;
                        } else if (org.hasNext()) nextValue = org.next();
                        else nextValue = null;
                        return r;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextValue != null && (nextValue.getX() <= until || plusOne);
                }
            };
        }
    }

    /**
     * @return the title of the overall set of multi-data; can be null
     */
    public String getTitle() {
        return mSubseriesTitle.get(0);
    }

    /**
     * set the title of the series. This will be used in
     * the legend.
     *
     * @param mTitle title of the overall set of multi-data; can be null
     */
    public void setTitle(String mTitle) {
        mSubseriesTitle.set(0, mTitle);
    }

    /**
     * @return color of the overall set of multi-data
     */
    public int getColor() {
        Integer val = mSubseriesColor.get(0);
        return val;
    }

    /**
     * set the color of the overall set of multi-data
     *
     * @param mColor
     */
    public void setColor(int mColor) {
        mSubseriesColor.set(0, new Integer(mColor));
    }

    /**
     * set a listener for tap on a data point.
     *
     * @param l listener
     */
    public void setOnDataPointTapListener(OnDataPointTapListener l) {
        this.mOnDataPointTapListener = l;
    }

    /**
     * called by the tap detector in order to trigger
     * the on tap on datapoint event.
     *
     * @param x pixel
     * @param y pixel
     */
    @Override
    public void onTap(float x, float y) {
        if (mOnDataPointTapListener != null) {
            E p = findDataPoint(x, y);
            if (p != null) {
                mOnDataPointTapListener.onTap(this, p);
            }
        }
    }

    /**
     * find the data point which is next to the
     * coordinates
     *
     * @param x pixel
     * @param y pixel
     * @return the data point or null if nothing was found
     */
    protected E findDataPoint(float x, float y) {
        float shortestDistance = Float.NaN;
        E shortest = null;
        for (Map.Entry<PointF, E> entry : mDataPoints.entrySet()) {
            float x1 = entry.getKey().x;
            float y1 = entry.getKey().y;
            float x2 = x;
            float y2 = y;

            float distance = (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
            if (shortest == null || distance < shortestDistance) {
                shortestDistance = distance;
                shortest = entry.getValue();
            }
        }
        if (shortest != null) {
            if (shortestDistance < 120) {
                return shortest;
            }
        }
        return null;
    }

    /**
     * register the datapoint to find it at a tap
     *
     * @param x pixel
     * @param y pixel
     * @param dp the data point to save
     */
    protected void registerDataPoint(float x, float y, E dp) {
        mDataPoints.put(new PointF(x, y), dp);
    }

    /**
     * clears the cached data point coordinates
     */
    public void resetDataPoints() {     // CHANGE NOTICE: garbage collection
        mDataPoints.clear();
    }

}
