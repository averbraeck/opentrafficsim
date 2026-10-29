package org.opentrafficsim.animation.graphs;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.DomainOrder;
import org.opentrafficsim.animation.graphs.AbstractPlot.PaintState;
import org.opentrafficsim.animation.graphs.Scatterplot.ScatterState;

/**
 * Scatter plot.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class Scatterplot extends AbstractPlot<ScatterState> implements TabularDataset
{

    /** Data source. */
    private final Supplier<List<ScatterPoint>[]> source;

    /** Series labels. */
    private final String[] labels;

    /**
     * Constructor.
     * @param scheduler scheduler
     * @param title title
     * @param updateInterval update interval
     * @param scatterDecoration decoration
     * @param source data source, which should supply an increasing list
     */
    public Scatterplot(final PlotScheduler scheduler, final String title, final Duration updateInterval,
            final ScatterDecoration scatterDecoration, final Supplier<List<ScatterPoint>[]> source)
    {
        super(scheduler, title, updateInterval, Duration.ZERO,
                createInitialStateSupplier(scatterDecoration.seriesLabels.length));
        Throw.whenNull(scatterDecoration, "scatterPlotInfo");
        Throw.whenNull(source, "source");
        this.source = source;
        this.labels = scatterDecoration.seriesLabels();
        setChart(createChart(scatterDecoration));
    }

    /**
     * Build initial empty state.
     * @param n number of series
     * @return empty state
     */
    private static Supplier<ScatterState> createInitialStateSupplier(final int n)
    {
        @SuppressWarnings("unchecked")
        List<ScatterPoint>[] points = new List[n];
        for (int i = 0; i < n; i++)
        {
            points[i] = Collections.emptyList();
        }
        return () -> new ScatterState(Duration.ZERO, points);
    }

    /**
     * Create a chart.
     * @param scatterDecoration decoration
     * @return JFreeChart; chart
     */
    private JFreeChart createChart(final ScatterDecoration scatterDecoration)
    {
        Bounds bounds = scatterDecoration.bounds();
        NumberAxis xAxis = new AutoBoundedNumberAxis(scatterDecoration.xLabel(), bounds.xMin(), bounds.xMax());
        if (bounds.xMin() != null)
        {
            if (bounds.xMax() != null && bounds.xMin() <= 0.0 && bounds.xMax() >= 0.0)
            {
                xAxis.setAutoRangeIncludesZero(true);
            }
        }
        NumberAxis yAxis = new AutoBoundedNumberAxis(scatterDecoration.yLabel(), bounds.yMin(), bounds.yMax());
        if (bounds.yMin() != null)
        {
            if (bounds.yMax() != null && bounds.yMin() <= 0.0 && bounds.yMax() >= 0.0)
            {
                yAxis.setAutoRangeIncludesZero(true);
            }
        }

        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        xAxis.setAutoRange(true);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setAutoRange(true);

        XYShapeRenderer renderer = new XYShapeRenderer();
        int s = scatterDecoration.dotSize();
        renderer.setDefaultShape(new Ellipse2D.Double(-s / 2, -s / 2, s, s));
        int n = scatterDecoration.seriesLabels().length;
        renderer.setLegendItemLabelGenerator((dataset, series) -> scatterDecoration.seriesLabels()[series]);
        if (scatterDecoration.seriesColors() != null)
        {
            IntStream.range(0, n).forEach((i) -> renderer.setSeriesPaint(i, scatterDecoration.seriesColors()[i]));
        }
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, true);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.OTHER;
    }

    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return " ";
    }

    @Override
    protected void calculatePaintState(final Duration time)
    {
        offerPaintState(new ScatterState(time, this.source.get()));
    }

    @Override
    public int getSeriesCount()
    {
        return getPaintState().scatter().length;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Comparable getSeriesKey(final int series)
    {
        return this.labels[series];
    }

    @Override
    @SuppressWarnings("rawtypes")
    public int indexOf(final Comparable seriesKey)
    {
        return Arrays.asList(this.labels).indexOf(seriesKey);
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(final int series)
    {
        return getPaintState().scatter()[series].size();
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getPaintState().scatter()[series].get(item).x();
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        return getPaintState().scatter()[series].get(item).x();
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getPaintState().scatter()[series].get(item).y();
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        return getPaintState().scatter()[series].get(item).y();
    }

    @Override
    public void setAutoBoundDomain(final XYPlot plot)
    {
        // override to prevent UnsupportedOperationException
    }

    @Override
    public void setAutoBoundRange(final XYPlot plot)
    {
        // override to prevent UnsupportedOperationException
    }

    /**
     * Axes bounds.
     * @param xMin x-axis minimum
     * @param xMax x-axis maximum
     * @param yMin y-axis minimum
     * @param yMax y-axis maximum
     */
    public record Bounds(Double xMin, Double xMax, Double yMin, Double yMax)
    {
    }

    /**
     * Scatter plot decoration.
     * @param xLabel x-axis label
     * @param yLabel y-axis label
     * @param seriesLabels label per series
     * @param seriesColors color per series, may be {@code null} for default colors
     * @param dotSize dot size
     * @param bounds axes bounds
     */
    public record ScatterDecoration(String xLabel, String yLabel, String[] seriesLabels, Color[] seriesColors, int dotSize,
            Bounds bounds)
    {
    }

    /**
     * Scatter paint state.
     * @param getAvailableTime available time for data
     * @param scatter scatter point data per series
     */
    public record ScatterState(Duration getAvailableTime, List<ScatterPoint>[] scatter) implements PaintState
    {
    }

    /**
     * Scatter point.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public record ScatterPoint(float x, float y)
    {
    }

}
