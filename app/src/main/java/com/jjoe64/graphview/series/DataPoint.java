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

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.Date;

/**
 * default data point implementation.
 * This stores the x and y values.
 *
 * @author jjoe64
 */
public class DataPoint implements DataPointInterface, Serializable {
    private static final long serialVersionUID=1428263322645L;

    private int positionInSeries = -1;                                   // CHANGE NOTICE: support StackedBarGraphSeries
    private int index;                                                  // CHANGE NOTICE: include index# in the callback
    private double x;
    private double y;

    public DataPoint(int index, double x, double y) {                   // CHANGE NOTICE: include index# in the callback
        this.index = index;                                             // CHANGE NOTICE: include index# in the callback
        this.x=x;
        this.y=y;
    }

    public DataPoint(int index, Date x, double y) {                     // CHANGE NOTICE: include index# in the callback
        this.index = index;                                             // CHANGE NOTICE: include index# in the callback
        this.x = x.getTime();
        this.y = y;
    }

    @Override
    public int getIndex() { return this.index; }                        // CHANGE NOTICE: include index# in the callback

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double addToY(double y) { this.y += y; return this.y; }      // CHANGE NOTICE: support StackedBarGraphSeries

    @Override
    public String toString() { return "[#"+index+":"+x+"/"+y+"]"; }     // CHANGE NOTICE: include index# in the callback

    @Override
    public void setPositionInSeries(int positionInSeries) { this.positionInSeries = positionInSeries; }     // CHANGE NOTICE: support StackedBarGraphSeries

    @Override
    public int getPositionInSeries() { return this.positionInSeries; }        // CHANGE NOTICE: support StackedBarGraphSeries
}
