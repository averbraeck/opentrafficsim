package org.opentrafficsim.animation.graphs;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.djutils.data.Column;
import org.djutils.data.Row;
import org.djutils.data.Table;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;

/**
 * Presents the plot as tabular data.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public interface TabularDataset extends XYDataset
{

    /**
     * Returns the chart.
     * @return the chart
     */
    JFreeChart getChart();

    /**
     * Returns the dataset in table form.
     * @return dataset in table form
     */
    default Table asTable()
    {
        XYPlot xyPlot = getChart().getXYPlot();
        XYZDataset datasetZ = this instanceof XYZDataset set ? set : null;
        String x = xyPlot.getDomainAxis().getLabel();
        x = x == null || x.isEmpty() ? "x" : x;
        String y = xyPlot.getRangeAxis().getLabel();
        y = y == null || y.isEmpty() ? "y" : y;
        Collection<Column<?>> columns = datasetZ == null
                ? List.of(new Column<String>("series", "series identifier", String.class),
                        new Column<Double>(x, x.equals("x") ? "x-value" : x, Double.class),
                        new Column<Double>(y, y.equals("y") ? "y-value" : y, Double.class))
                : List.of(new Column<String>("series", "series identifier", String.class),
                        new Column<Double>(x, x.equals("x") ? "x-value" : x, Double.class),
                        new Column<Double>(y, y.equals("y") ? "y-value" : y, Double.class),
                        new Column<Double>("z", "z-value", Double.class));
        boolean empty = true;
        for (int i = 0; i < getSeriesCount(); i++)
        {
            if (getItemCount(i) > 0)
            {
                empty = false;
                break;
            }
        }
        boolean isEmpty = empty;
        return new Table(getChart().getTitle().getText(), getChart().getTitle().getText(), columns)
        {
            private final Table thisTable = this;

            @Override
            public Iterator<Row> iterator()
            {
                return new Iterator<>()
                {
                    private int series = 0;

                    private int item = 0;

                    @Override
                    public boolean hasNext()
                    {
                        while (this.series < getSeriesCount() && this.item >= getItemCount(this.series))
                        {
                            this.item = 0;
                            this.series++;
                        }
                        return this.series < getSeriesCount() && this.item < getItemCount(this.series);
                    }

                    @Override
                    public Row next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "No more element in plot data table.");
                        int i = this.item++;
                        return new Row(thisTable,
                                datasetZ == null
                                        ? new Object[] {getSeriesKey(this.series).toString(), getXValue(this.series, i),
                                                getYValue(this.series, i)}
                                        : new Object[] {getSeriesKey(this.series).toString(), getXValue(this.series, i),
                                                getYValue(this.series, i), datasetZ.getZValue(this.series, i)});
                    }
                };
            }

            @Override
            public boolean isEmpty()
            {
                return isEmpty;
            }
        };
    }

}
