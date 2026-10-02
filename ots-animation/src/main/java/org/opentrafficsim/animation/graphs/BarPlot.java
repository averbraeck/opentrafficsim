package org.opentrafficsim.animation.graphs;

import java.util.Arrays;

import org.djunits.value.vdouble.scalar.Duration;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.IntervalXYDataset;
import org.opentrafficsim.animation.graphs.BarPlot.BarPaintState;

/**
 * Plot implementation to show a bar plot.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class BarPlot extends AbstractPlot<BarPaintState> implements IntervalXYDataset
{

    /** Plot data. */
    private final BarPlotData data;

    /**
     * Constructor.
     * @param scheduler scheduler
     * @param caption caption
     * @param updateInterval regular update interval (simulation time)
     * @param delay amount of time that chart runs behind simulation to prevent gaps in the charted data
     * @param barPlotData plot data
     */
    public BarPlot(final PlotScheduler scheduler, final String caption, final Duration updateInterval, final Duration delay,
            final BarPlotData barPlotData)
    {
        super(scheduler, caption, updateInterval, delay,
                () -> new BarPaintState(0.0, 1.0, new float[barPlotData.source().seriesLabels().length][0], Duration.ZERO));
        this.data = barPlotData;
        setChart(createChart());
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        Object key = this.data.dataKey();
        NumberAxis xAxis = new NumberAxis(this.data.xLabel());
        BarDataSource source = this.data.source();
        xAxis.setRange(source.getMinX(key),
                source.getMinX(key) + source.getData(this.data.dataKey())[0].length * source.getDx(key));
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("Count [-]");
        yAxis.setAutoRangeIncludesZero(true);
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setLegendItemLabelGenerator((dataset, series) -> source.seriesLabels()[series]);
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, source.seriesLabels().length > 1);
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    @Override
    public int getItemCount(final int series)
    {
        return getPaintState().getItemCount(series);
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        return getPaintState().xMin() + getPaintState().dx() * item;
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        return getPaintState().getValue(series, item);
    }

    @Override
    public int getSeriesCount()
    {
        return getPaintState().getSeriesCount();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable getSeriesKey(final int series)
    {
        return series;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return (Integer) seriesKey;
    }

    @Override
    public Number getStartX(final int series, final int item)
    {
        return getStartXValue(series, item);
    }

    @Override
    public double getStartXValue(final int series, final int item)
    {
        return getPaintState().xMin() + item * getPaintState().dx() + series * seriesWidth();
    }

    @Override
    public Number getEndX(final int series, final int item)
    {
        return getEndXValue(series, item);
    }

    @Override
    public double getEndXValue(final int series, final int item)
    {
        return getStartXValue(series, item) + seriesWidth();
    }

    /**
     * Returns the width for each series, which is the x-axis step divided by the series count.
     * @return width for each series
     */
    private double seriesWidth()
    {
        return getPaintState().dx() / getPaintState().getSeriesCount();
    }

    @Override
    public Number getStartY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getStartYValue(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public Number getEndY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getEndYValue(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.OTHER;
    }

    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        if (getPaintState().data()[0].length == 0)
        {
            return " ";
        }
        double xMin = getPaintState().xMin();
        double dx = getPaintState().dx();
        int item = (int) ((domainValue - xMin) / dx);
        int series = (int) ((domainValue - getStartXValue(0, item)) / seriesWidth());
        if (series >= getSeriesCount() || item >= getItemCount(series))
        {
            return " ";
        }
        double value = getPaintState().getValue(series, item);
        if (value == 0.0)
        {
            return " ";
        }
        String label = String.format("%s [%.1f %.1f] is %.3f", this.data.source().seriesLabels()[series], xMin + dx * item,
                xMin + dx * (item + 1), value);
        return label.endsWith(".000") ? label.substring(0, label.length() - 4) : label;
    }

    @Override
    protected void calculatePaintState(final Duration time)
    {
        Object key = this.data.dataKey();
        BarDataSource source = this.data.source();
        offerPaintState(new BarPaintState(source.getMinX(key), source.getDx(key), this.data.getData(), time));
    }

    @Override
    public void setAutoBoundDomain(final XYPlot plot)
    {
        // don't need to do anything, override to prevent exception thrown in super
    }

    @Override
    public void setAutoBoundRange(final XYPlot plot)
    {
        // don't need to do anything, override to prevent exception thrown in super
    }

    /**
     * Data source for one or more bar plots.
     */
    public interface BarDataSource
    {

        /**
         * Returns the minimum x-axis value of the data for the given key.
         * @param dataKey data key
         * @return the minimum x-axis value of the data for the given key
         */
        double getMinX(Object dataKey);

        /**
         * Returns the x-axis step of the data for the given key.
         * @param dataKey data key
         * @return the x-axis step of the data for the given key
         */
        double getDx(Object dataKey);

        /**
         * Returns the data for the given key.
         * @param dataKey data key
         * @return the data for the given key ({@code float[series][item]})
         */
        float[][] getData(Object dataKey);

        /**
         * Returns an array with series labels for the legend.
         * @return an array with series labels for the legend
         */
        String[] seriesLabels();

    }

    /**
     * Data for a specific bar plot.
     * @param xLabel x-axis label
     * @param dataKey data key
     * @param source data source
     */
    public record BarPlotData(String xLabel, Object dataKey, BarDataSource source)
    {

        /**
         * Returns bar plot data based on an internally created bar data source with only a single set of data.
         * @param xLabel x-axis label
         * @param minX minimum x-axis value
         * @param dx x-axis step
         * @param data data ({@code float[series][item]})
         * @return bar plot data based on an internally created bar data source with only a single set of data
         */
        public static BarPlotData ofSingle(final String xLabel, final double minX, final double dx, final float[][] data)
        {
            return new BarPlotData(xLabel, null, new BarDataSource()
            {
                @Override
                public double getMinX(final Object dataKey)
                {
                    return minX;
                }

                @Override
                public double getDx(final Object dataKey)
                {
                    return dx;
                }

                @Override
                public float[][] getData(final Object dataKey)
                {
                    return data;
                }

                @Override
                public String[] seriesLabels()
                {
                    return new String[] {""};
                }
            });
        }

        /**
         * Returns a copy of the data for the internal data key.
         * @return copy of the data for the internal data key
         */
        public float[][] getData()
        {
            return Arrays.stream(this.source.getData(dataKey())).map(float[]::clone).toArray(float[][]::new);
        }

    }

    /**
     * Paint state for bar plot.
     * @param xMin minimum x-axis value
     * @param dx x-axis step
     * @param data data per series, and per item ({@code float[series][item]})
     * @param getAvailableTime available time
     */
    public record BarPaintState(double xMin, double dx, float[][] data, Duration getAvailableTime)
            implements AbstractPlot.PaintState
    {

        /**
         * Returns the number of series.
         * @return number of series
         */
        private int getSeriesCount()
        {
            return data().length;
        }

        /**
         * Get number of items for series.
         * @param series series
         * @return number of items for series
         */
        private int getItemCount(final int series)
        {
            return data()[series].length;
        }

        /**
         * Returns the value for the item'th bar in the series'th series.
         * @param series series
         * @param item item
         * @return value for the item'th bar in the series'th series
         */
        private double getValue(final int series, final int item)
        {
            return data()[series][item];
        }

    }
}
