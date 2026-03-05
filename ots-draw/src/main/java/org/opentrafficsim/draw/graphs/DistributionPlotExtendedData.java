package org.opentrafficsim.draw.graphs;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.IntervalXYDataset;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Distribution data plot for any numerical extended trajectory data. Data is collected cumulatively; each interval adds to the
 * totals of the previous interval.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU data type that needs to be consistent between the sampler data and the extended data type
 * @param <Z> value type that needs to be consistent between extended data type and label data
 */
public class DistributionPlotExtendedData<G extends GtuData, Z extends Number> extends AbstractPlot implements IntervalXYDataset
{

    /** Sampler data. */
    private final SamplerData<G> samplerData;

    /** KPI lane directions registered in the sampler. */
    private final GraphPath<? extends LaneData<?>> path;

    /** Data type. */
    private final ExtendedDataType<Z, ?, ?, ?> dataType;

    /** Time of most recent update. */
    private double lastUpdateTime = Double.NEGATIVE_INFINITY;

    /** X-values. */
    private final double[] x;

    /** Step size. */
    private final double dx;

    /** Y-values. */
    private final long[] y;

    /**
     * Constructor.
     * @param samplerData sampler data
     * @param path path
     * @param dataType data type
     * @param plotScheduler plot scheduler
     * @param labelData label data
     */
    public DistributionPlotExtendedData(final SamplerData<G> samplerData, final GraphPath<? extends LaneData<?>> path,
            final ExtendedDataType<Z, ?, ?, G> dataType, final PlotScheduler plotScheduler, final LabelData<Z> labelData)
    {
        super(plotScheduler, labelData.caption(), Duration.ofSI(10.0), Duration.ZERO);
        int n = 1 + (int) Math.round(
                (labelData.maximum().doubleValue() - labelData.minimum().doubleValue()) / labelData.step().doubleValue());
        this.x = new double[n];
        for (int i = 0; i < n; i++)
        {
            this.x[i] = labelData.minimum().doubleValue() + i * labelData.step().doubleValue();
        }
        this.dx = labelData.step().doubleValue();
        this.y = new long[n];
        this.samplerData = Throw.whenNull(samplerData, "samplerData");
        this.path = Throw.whenNull(path, "path");
        this.dataType = Throw.whenNull(dataType, "dataType");
        setChart(createChart(labelData.xLabel()));
    }

    /**
     * Create a chart.
     * @param xLabel label on x-axis
     * @return JFreeChart; chart
     */
    private JFreeChart createChart(final String xLabel)
    {
        NumberAxis xAxis = new NumberAxis(xLabel);
        xAxis.setRange(this.x[0], this.x[this.x.length - 1]);
        NumberAxis yAxis = new NumberAxis("Count [-]");
        yAxis.setAutoRangeIncludesZero(true);
        XYBarRenderer renderer = new XYBarRenderer();
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);
    }

    @Override
    public int getSeriesCount()
    {
        return 1;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparable getSeriesKey(final int series)
    {
        return Integer.valueOf(1);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    @Override
    public int getItemCount(final int series)
    {
        return this.x.length - 1;
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return this.x[item];
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        return this.x[item];
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return this.y[item];
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        return this.y[item];
    }

    @Override
    public Number getStartX(final int series, final int item)
    {
        return this.x[item];
    }

    @Override
    public double getStartXValue(final int series, final int item)
    {
        return this.x[item];
    }

    @Override
    public Number getEndX(final int series, final int item)
    {
        return this.x[item + 1];
    }

    @Override
    public double getEndXValue(final int series, final int item)
    {
        return this.x[item + 1];
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
        return " ";
    }

    @Override
    protected void increaseTime(final Duration time)
    {
        if (this.path == null)
        {
            return; // initializing
        }
        for (Section<? extends LaneData<?>> section : this.path.getSections())
        {
            for (LaneData<?> lane : section.sections())
            {
                processLane(lane);
            }
        }
        this.lastUpdateTime = time.si;
    }

    /**
     * Processes a single lane while updating the time.
     * @param lane lane
     */
    private void processLane(final LaneData<?> lane)
    {
        Optional<TrajectoryGroup<G>> trajectories = this.samplerData.getTrajectoryGroup(lane);
        if (trajectories.isPresent())
        {
            for (Trajectory<G> trajectory : trajectories.get())
            {
                int n = trajectory.size() - 1;
                while (n >= 0 && trajectory.getT(n) > this.lastUpdateTime)
                {
                    Z z = trajectory.getExtendedData(this.dataType, n);
                    double value = z == null ? Double.NaN : z.doubleValue();
                    if (!Double.isNaN(value) && value >= this.x[0] && value < this.x[this.x.length - 1])
                    {
                        int index = (int) Math.floor((value - this.x[0]) / this.dx);
                        this.y[index]++;
                    }
                    n--;
                }
            }
        }
    }

    /**
     * Input container for label input.
     * @param <Z> value type
     * @param caption caption
     * @param xLabel label on x-axis
     * @param minimum minimum x-value
     * @param step step value
     * @param maximum maximum x-value
     */
    public record LabelData<Z extends Number>(String caption, String xLabel, Z minimum, Z step, Z maximum)
    {
        /**
         * Constructor.
         * @param caption caption
         * @param xLabel label on x-axis
         * @param minimum minimum x-value
         * @param step step value
         * @param maximum maximum x-value
         */
        public LabelData
        {
            Throw.when(maximum.doubleValue() <= minimum.doubleValue(), IllegalArgumentException.class,
                    "maximum must be greater than minimum");
            Throw.when(step.doubleValue() <= 0.0, IllegalArgumentException.class, "step must be greater than 0");
        }
    }

}
