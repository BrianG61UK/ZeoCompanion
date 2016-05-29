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

import android.graphics.Canvas;

import com.jjoe64.graphview.GraphView;

import java.util.Iterator;

/**
 * Basis interface for a series with mulitple Y values that can be plotted
 * on the graph usually via a stacked bar graph.
 *
 * @author mmaschino
 * * This particular source code file is licensed per overall GraphView's license
 */
public interface MultiSeries<E extends DataPointInterface> extends Series<E> {
    /**
     * @param data another data subseries to add
     *
     * note that for all subseries other than the first, the X-values are ignored; X-values are always used solely from the first subseries;
     * note that all Y-values must be of the same units-of-measure in all subseries, and need to be additive;
     * note as well that if there are more data points in a subsequent subseries, those data points will be ignored;
     *      similarly if a subseries has fewer data points than the first subseries, then the Y-summations will obviously be inconsistent
     */
    public void addSubseries(E[] data);

    /**
     * clear all the subseries usually in preparation for re-adding new subseries
     */
    public void clearAllSubseries();

    public float getDrawY(GraphView graphView, int subseries, int position);

    /**
     * @param subseries the subseries number to set (starts at 0)
     * @param color the color to set
     * @return  the color of the series. Used in the legend and should
     *          be used for the plotted points or lines.
     */
    public void setColor(int subseries, int color);

    /**
     * @param subseries the subseries number to get (starts at 0)
     * @return  the color of the subseries. Used in the legend and should
     *          be used for the plotted points or lines.
     */
    public int getColor(int subseries);

    /**
     * @param subseries the subseries number to set (starts at 0)
     * @param title the text string to set
     * @return  the color of the series. Used in the legend and should
     *          be used for the plotted points or lines.
     */
    public void setTitle(int subseries, String title);

    /**
     * @param subseries the subseries number to get (starts at 0)
     * @return  the text title of the subseries. Used in the legend.
     */
    public String getTitle(int subseries);
}
