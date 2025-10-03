package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.time.Period;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Fundamental diagram from various sources.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FundamentalDiagram extends AbstractBoundedPlot implements XYDataset
{

    /** Aggregation periods. */
    public static final double[] DEFAULT_PERIODS = new double[] {5.0, 10.0, 30.0, 60.0, 120.0, 300.0, 900.0};

    /** Update frequencies (n * 1/period). */
    public static final int[] DEFAULT_UPDATE_FREQUENCIES = new int[] {1, 2, 3, 5, 10};

    /** Source providing the data. */
    private final FdSource source;

    /** Fundamental diagram line. */
    private final FdLine fdLine;

    /** Quantity on domain axis. */
    private Quantity domainQuantity;

    /** Quantity on range axis. */
    private Quantity rangeQuantity;

    /** The other, 3rd quantity. */
    private Quantity otherQuantity;

    /** Labels of series. */
    private final List<String> seriesLabels = new ArrayList<>();

    /** Updater for update times. */
    private final GraphUpdater<Duration> graphUpdater;

    /** Property for chart listener to provide time info for status label. */
    private String timeInfo = "";

    /** Legend to change text color to indicate visibility. */
    private LegendItemCollection legend;

    /** Whether each lane is visible or not. */
    private final List<Boolean> laneVisible = new ArrayList<>();

    /**
     * Constructor.
     * @param caption caption
     * @param domainQuantity initial quantity on the domain axis
     * @param rangeQuantity initial quantity on the range axis
     * @param scheduler scheduler.
     * @param source source providing the data
     * @param fdLine fundamental diagram line, may be {@code null}
     */
    public FundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final PlotScheduler scheduler, final FdSource source, final FdLine fdLine)
    {
        super(scheduler, caption, source.getUpdateInterval(), source.getDelay());
        Throw.when(domainQuantity.equals(rangeQuantity), IllegalArgumentException.class,
                "Domain and range quantity should not be equal.");
        this.fdLine = fdLine;
        this.setDomainQuantity(domainQuantity);
        this.setRangeQuantity(rangeQuantity);
        Set<Quantity> quantities = EnumSet.allOf(Quantity.class);
        quantities.remove(domainQuantity);
        quantities.remove(rangeQuantity);
        this.setOtherQuantity(quantities.iterator().next());
        this.source = source;
        int d = 0;
        if (fdLine != null)
        {
            d = 1;
            this.seriesLabels.add(fdLine.getName());
            this.laneVisible.add(true);
        }
        for (int series = 0; series < source.getNumberOfSeries(); series++)
        {
            this.seriesLabels.add(series + d, source.getName(series));
            this.laneVisible.add(true);
        }
        setChart(createChart());
        setLowerDomainBound(0.0);
        setLowerRangeBound(0.0);

        // setup updater to do the actual work in another thread
        this.graphUpdater = new GraphUpdater<>("Fundamental diagram worker", Thread.currentThread(), (t) ->
        {
            if (this.getSource() != null)
            {
                this.getSource().increaseTime(t);
                notifyPlotChange();
            }
        });

        // let this diagram be notified by the source
        source.addFundamentalDiagram(this);
    }

    /**
     * Create a chart.
     * @return chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis(this.getDomainQuantity().label());
        NumberAxis yAxis = new NumberAxis(this.getRangeQuantity().label());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer()
        {
            /** */
            private static final long serialVersionUID = 20181022L;

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean isSeriesVisible(final int series)
            {
                return FundamentalDiagram.this.laneVisible.get(series);
            }
        }; // XYDotRenderer doesn't support different markers
        renderer.setDefaultLinesVisible(false);
        if (hasLineFD())
        {
            int series = this.getSource().getNumberOfSeries();
            renderer.setSeriesLinesVisible(series, true);
            renderer.setSeriesPaint(series, Color.BLACK);
            renderer.setSeriesShapesVisible(series, false);
        }
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        boolean showLegend = true;
        if (!hasLineFD() && this.getSource().getNumberOfSeries() < 2)
        {
            plot.setFixedLegendItems(null);
            showLegend = false;
        }
        else
        {
            this.legend = new LegendItemCollection();
            for (int i = 0; i < this.getSource().getNumberOfSeries(); i++)
            {
                LegendItem li = new LegendItem(this.getSource().getName(i));
                li.setSeriesKey(i); // lane series, not curve series
                li.setShape(renderer.lookupLegendShape(i));
                li.setFillPaint(renderer.lookupSeriesPaint(i));
                this.legend.add(li);
            }
            if (hasLineFD())
            {
                LegendItem li = new LegendItem(this.fdLine.getName());
                li.setSeriesKey(-1);
                this.legend.add(li);
            }
            plot.setFixedLegendItems(this.legend);
            showLegend = true;
        }
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
    }

    @Override
    protected void increaseTime(final Duration time)
    {
        if (this.graphUpdater != null && time.si >= this.getSource().getAggregationPeriod().si) // null during construction
        {
            this.graphUpdater.offer(time);
        }
    }

    @Override
    public int getSeriesCount()
    {
        if (this.getSource() == null)
        {
            return 0;
        }
        return this.getSource().getNumberOfSeries() + (hasLineFD() ? 1 : 0);
    }

    @Override
    public Comparable<String> getSeriesKey(final int series)
    {
        return this.seriesLabels.get(series);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        int index = this.seriesLabels.indexOf(seriesKey);
        return index < 0 ? 0 : index;
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(final int series)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.domainQuantity).length;
        }
        return this.getSource().getItemCount(series);
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.domainQuantity)[item];
        }
        return this.getDomainQuantity().getValue(this.getSource(), series, item);
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.rangeQuantity)[item];
        }
        return this.getRangeQuantity().getValue(this.getSource(), series, item);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.FUNDAMENTAL_DIAGRAM;
    }

    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return this.getDomainQuantity().format(domainValue) + ", " + this.getRangeQuantity().format(rangeValue) + ", "
                + this.getOtherQuantity()
                        .format(this.getDomainQuantity().computeOther(this.getRangeQuantity(), domainValue, rangeValue))
                + this.getTimeInfo();
    }

    /**
     * Quantity enum defining density, flow and speed.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum Quantity
    {
        /** Density. */
        DENSITY
        {
            @Override
            public String label()
            {
                return "Density [veh/km] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/km", value);
            }

            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 1000 * src.getDensity(series, item);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // .......................... speed = flow / density .. flow = density * speed
                return pairing.equals(FLOW) ? pairedValue / thisValue : thisValue * pairedValue;
            }
        },

        /** Flow. */
        FLOW
        {
            @Override
            public String label()
            {
                return "Flow [veh/h] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/h", value);
            }

            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 3600 * src.getFlow(series, item);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // speed = flow * density ... density = flow / speed
                return thisValue / pairedValue;
            }
        },

        /** Speed. */
        SPEED
        {
            @Override
            public String label()
            {
                return "Speed [km/h] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.1f km/h", value);
            }

            @Override
            public double getValue(final FdSource src, final int series, final int item)
            {
                return 3.6 * src.getSpeed(series, item);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // ............................. flow = speed * density .. density = flow / speed
                return pairing.equals(DENSITY) ? thisValue * pairedValue : pairedValue / thisValue;
            }
        };

        /**
         * Returns an axis label of the quantity.
         * @return axis label of the quantity
         */
        public abstract String label();

        /**
         * Formats a value for status display.
         * @param value value
         * @return formatted string including quantity
         */
        public abstract String format(double value);

        /**
         * Get scaled value in presentation unit.
         * @param src the data source
         * @param series series number
         * @param item item number in series
         * @return scaled value in presentation unit
         */
        public abstract double getValue(FdSource src, int series, int item);

        /**
         * Compute the value of the 3rd quantity.
         * @param pairing quantity on other axis
         * @param thisValue value of this quantity
         * @param pairedValue value of the paired quantity on the other axis
         * @return value of the 3rd quantity
         */
        public abstract double computeOther(Quantity pairing, double thisValue, double pairedValue);

    }

    /**
     * Data source for a fundamental diagram.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface FdSource
    {
        /**
         * Returns the possible intervals.
         * @return possible intervals
         */
        default double[] getPossibleAggregationPeriods()
        {
            return DEFAULT_PERIODS;
        }

        /**
         * Returns the possible frequencies, as a factor on 1 / 'aggregation interval'.
         * @return possible frequencies
         */
        default int[] getPossibleUpdateFrequencies()
        {
            return DEFAULT_UPDATE_FREQUENCIES;
        }

        /**
         * Add fundamental diagram. Used to notify diagrams when data has changed.
         * @param fundamentalDiagram fundamental diagram
         */
        void addFundamentalDiagram(FundamentalDiagram fundamentalDiagram);

        /**
         * Clears all connected fundamental diagrams.
         */
        void clearFundamentalDiagrams();

        /**
         * Returns the diagrams.
         * @return ImmutableSet&lt;FundamentalDiagram&gt; diagrams
         */
        ImmutableSet<FundamentalDiagram> getDiagrams();

        /**
         * The update interval.
         * @return update interval
         */
        Duration getUpdateInterval();

        /**
         * Changes the update interval.
         * @param interval update interval
         * @param time time until which data has to be recalculated
         */
        void setUpdateInterval(Duration interval, Duration time);

        /**
         * The aggregation period.
         * @return aggregation period
         */
        Duration getAggregationPeriod();

        /**
         * Changes the aggregation period.
         * @param period aggregation period
         */
        void setAggregationPeriod(Duration period);

        /**
         * Recalculates the data after the aggregation or update time was changed.
         * @param time time up to which recalculation is required
         */
        void recalculate(Duration time);

        /**
         * Return the delay for graph updates so future influencing events have occurred, e.d. GTU move's.
         * @return graph delay
         */
        Duration getDelay();

        /**
         * Increase the time span.
         * @param time time to increase to
         */
        void increaseTime(Duration time);

        /**
         * Returns the number of series (i.e. lanes or 1 for aggregated).
         * @return number of series
         */
        int getNumberOfSeries();

        /**
         * Returns a name of the series.
         * @param series series number
         * @return name of the series
         */
        String getName(int series);

        /**
         * Returns the number of items in the series.
         * @param series series number
         * @return number of items in the series
         */
        int getItemCount(int series);

        /**
         * Return the SI flow value of item in series.
         * @param series series number
         * @param item item number in the series
         * @return SI flow value of item in series
         */
        double getFlow(int series, int item);

        /**
         * Return the SI density value of item in series.
         * @param series series number
         * @param item item number in the series
         * @return SI density value of item in series
         */
        double getDensity(int series, int item);

        /**
         * Return the SI speed value of item in series.
         * @param series series number
         * @param item item number in the series
         * @return SI speed value of item in series
         */
        double getSpeed(int series, int item);

        /**
         * Returns whether this source aggregates lanes.
         * @return whether this source aggregates lanes
         */
        boolean isAggregate();

        /**
         * Sets the name of the series when aggregated, e.g. for legend. Default is "Aggregate".
         * @param aggregateName name of the series when aggregated
         */
        void setAggregateName(String aggregateName);
    }

    /**
     * Abstract implementation to link to fundamental diagrams.
     */
    abstract static class AbstractFdSource implements FdSource
    {

        /** Fundamental diagrams. */
        private Set<FundamentalDiagram> fundamentalDiagrams = new LinkedHashSet<>();

        /**
         * Constructor.
         */
        public AbstractFdSource()
        {
            //
        }

        @Override
        public void addFundamentalDiagram(final FundamentalDiagram fundamentalDiagram)
        {
            this.fundamentalDiagrams.add(fundamentalDiagram);
        }

        @Override
        public void clearFundamentalDiagrams()
        {
            this.fundamentalDiagrams.clear();
        }

        @Override
        public ImmutableSet<FundamentalDiagram> getDiagrams()
        {
            return new ImmutableLinkedHashSet<>(this.fundamentalDiagrams);
        }

    }

    /**
     * Creates a {@code Source} from a sampler and positions.
     * @param sampler sampler
     * @param crossSection cross section
     * @param aggregateLanes whether to aggregate the positions
     * @param aggregationTime aggregation time (and update time)
     * @param harmonic harmonic mean
     * @return source for a fundamental diagram from a sampler and positions
     * @param <L> LaneData
     */
    @SuppressWarnings("methodlength")
    public static <L extends LaneData<L>> FdSource sourceFromSampler(final Sampler<?, L> sampler,
            final GraphCrossSection<L> crossSection, final boolean aggregateLanes, final Duration aggregationTime,
            final boolean harmonic)
    {
        return new CrossSectionSamplerFdSource<>(sampler, crossSection, aggregateLanes, aggregationTime, harmonic);
    }

    /**
     * Creates a {@code Source} from a sampler and positions.
     * @param sampler sampler
     * @param path cross section
     * @param aggregateLanes whether to aggregate the positions
     * @param aggregationTime aggregation time (and update time)
     * @return source for a fundamental diagram from a sampler and positions
     * @param <L> LaneData
     */
    public static <L extends LaneData<L>> FdSource sourceFromSampler(final Sampler<?, L> sampler, final GraphPath<L> path,
            final boolean aggregateLanes, final Duration aggregationTime)
    {
        return new PathSamplerFdSource<>(sampler, path, aggregateLanes, aggregationTime);
    }

    /**
     * Combines multiple sources in to one source.
     * @param sources sources coupled to their names for in the legend
     * @return combined source
     */
    public static FdSource combinedSource(final Map<String, FdSource> sources)
    {
        return new MultiFdSource(sources);
    }

    /**
     * Fundamental diagram source based on a cross section.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private static class CrossSectionSamplerFdSource<L extends LaneData<L>, S extends GraphCrossSection<L>>
            extends AbstractSpaceSamplerFdSource<L, S>
    {
        /** Harmonic mean. */
        private final boolean harmonic;

        /**
         * Constructor.
         * @param sampler Sampler<?, ?>; sampler
         * @param crossSection cross section
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation {@link Period}
         * @param harmonic harmonic mean
         */
        CrossSectionSamplerFdSource(final Sampler<?, L> sampler, final S crossSection, final boolean aggregateLanes,
                final Duration aggregationPeriod, final boolean harmonic)
        {
            super(sampler, crossSection, aggregateLanes, aggregationPeriod);
            this.harmonic = harmonic;
        }

        @Override
        protected void getMeasurements(final Trajectory<?> trajectory, final Duration startTime, final Duration endTime,
                final Length length, final int series, final double[] measurements)
        {
            Length x = getSpace().position(series);
            if (GraphUtil.considerTrajectory(trajectory, x, x.plus(Length.instantiateSI(1.0e-3))))
            {
                // detailed check
                Duration t = trajectory.getTimeAtPosition(x);
                if (t.si >= startTime.si && t.si < endTime.si)
                {
                    measurements[0] = 1; // first = count
                    measurements[1] = // second = sum of (inverted) speeds
                            this.harmonic ? 1.0 / trajectory.getSpeedAtPosition(x).si : trajectory.getSpeedAtPosition(x).si;
                }
            }
        }

        @Override
        protected double getVehicleCount(final double first, final double second)
        {
            return first; // is divided by aggregation period by caller
        }

        @Override
        protected double getSpeed(final double first, final double second)
        {
            return this.harmonic ? first / second : second / first;
        }

        @Override
        public String toString()
        {
            return "CrossSectionSamplerFdSource [harmonic=" + this.harmonic + "]";
        }

    }

    /**
     * Fundamental diagram source based on a path. Density, speed and flow over the entire path are calculated per lane.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private static class PathSamplerFdSource<L extends LaneData<L>, S extends GraphPath<L>>
            extends AbstractSpaceSamplerFdSource<L, S>
    {
        /**
         * Constructor.
         * @param sampler Sampler<?, ?>; sampler
         * @param path path
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation period
         */
        PathSamplerFdSource(final Sampler<?, L> sampler, final S path, final boolean aggregateLanes,
                final Duration aggregationPeriod)
        {
            super(sampler, path, aggregateLanes, aggregationPeriod);
        }

        @Override
        protected void getMeasurements(final Trajectory<?> trajectory, final Duration startTime, final Duration endTime,
                final Length length, final int series, final double[] measurements)
        {
            SpaceTimeView stv = trajectory.getSpaceTimeView(Length.ZERO, length, startTime, endTime);
            measurements[0] = stv.distance().si; // first = total traveled distance
            measurements[1] = stv.time().si; // second = total traveled time
        }

        @Override
        protected double getVehicleCount(final double first, final double second)
        {
            return first / getSpace().getTotalLength().si; // is divided by aggregation period by caller
        }

        @Override
        protected double getSpeed(final double first, final double second)
        {
            return first / second;
        }

        @Override
        public String toString()
        {
            return "PathSamplerFdSource []";
        }

    }

    /**
     * Abstract class that deals with updating and recalculating the fundamental diagram.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private abstract static class AbstractSpaceSamplerFdSource<L extends LaneData<L>, S extends AbstractGraphSpace<L>>
            extends AbstractFdSource
    {
        /** Period number of last calculated period. */
        private int periodNumber = -1;

        /** Update interval. */
        private Duration updateInterval;

        /** Aggregation period. */
        private Duration aggregationPeriod;

        /** Last update time. */
        private Duration lastUpdateTime;

        /** Number of series. */
        private final int nSeries;

        /** First data. */
        private double[][] firstMeasurement;

        /** Second data. */
        private double[][] secondMeasurement;

        /** Whether the plot is in a process such that the data is invalid for the current draw of the plot. */
        private boolean invalid = false;

        /** The sampler. */
        private final Sampler<?, L> sampler;

        /** Space. */
        private final S space;

        /** Whether to aggregate the lanes. */
        private final boolean aggregateLanes;

        /** Name of the series when aggregated. */
        private String aggregateName = "Aggregate";

        /** For each series (lane), the highest trajectory number (n) below which all trajectories were also handled (0:n). */
        private Map<L, Integer> lastConsecutivelyAssignedTrajectories = new LinkedHashMap<>();

        /** For each series (lane), a list of handled trajectories above n, excluding n+1. */
        private Map<L, SortedSet<Integer>> assignedTrajectories = new LinkedHashMap<>();

        /**
         * Constructor.
         * @param sampler Sampler<?, ?>; sampler
         * @param space space
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation period
         */
        AbstractSpaceSamplerFdSource(final Sampler<?, L> sampler, final S space, final boolean aggregateLanes,
                final Duration aggregationPeriod)
        {
            this.sampler = sampler;
            this.space = space;
            this.aggregateLanes = aggregateLanes;
            this.nSeries = aggregateLanes ? 1 : space.getNumberOfSeries();
            // create and register kpi lane directions
            for (L laneDirection : space)
            {
                sampler.registerSpaceTimeRegion(new SpaceTimeRegion<>(laneDirection, Length.ZERO, laneDirection.getLength(),
                        sampler.now(), Duration.instantiateSI(Double.MAX_VALUE)));

                // info per kpi lane direction
                this.lastConsecutivelyAssignedTrajectories.put(laneDirection, -1);
                this.assignedTrajectories.put(laneDirection, new TreeSet<>());
            }

            this.updateInterval = aggregationPeriod;
            this.aggregationPeriod = aggregationPeriod;
            this.firstMeasurement = new double[this.nSeries][10];
            this.secondMeasurement = new double[this.nSeries][10];
        }

        /**
         * Returns the space.
         * @return space
         */
        protected S getSpace()
        {
            return this.space;
        }

        @Override
        public Duration getUpdateInterval()
        {
            return this.updateInterval;
        }

        @Override
        public void setUpdateInterval(final Duration interval, final Duration time)
        {
            if (this.updateInterval != interval)
            {
                this.updateInterval = interval;
                recalculate(time);
            }
        }

        @Override
        public Duration getAggregationPeriod()
        {
            return this.aggregationPeriod;
        }

        @Override
        public void setAggregationPeriod(final Duration period)
        {
            if (this.aggregationPeriod != period)
            {
                this.aggregationPeriod = period;
            }
        }

        @Override
        public void recalculate(final Duration time)
        {
            new Thread(new Runnable()
            {
                @Override
                @SuppressWarnings("synthetic-access")
                public void run()
                {
                    synchronized (AbstractSpaceSamplerFdSource.this)
                    {
                        // an active plot draw will now request data on invalid items
                        AbstractSpaceSamplerFdSource.this.invalid = true;
                        AbstractSpaceSamplerFdSource.this.periodNumber = -1;
                        AbstractSpaceSamplerFdSource.this.updateInterval = getUpdateInterval();
                        AbstractSpaceSamplerFdSource.this.firstMeasurement =
                                new double[AbstractSpaceSamplerFdSource.this.nSeries][10];
                        AbstractSpaceSamplerFdSource.this.secondMeasurement =
                                new double[AbstractSpaceSamplerFdSource.this.nSeries][10];
                        AbstractSpaceSamplerFdSource.this.lastConsecutivelyAssignedTrajectories.clear();
                        AbstractSpaceSamplerFdSource.this.assignedTrajectories.clear();
                        for (L lane : AbstractSpaceSamplerFdSource.this.space)
                        {
                            AbstractSpaceSamplerFdSource.this.lastConsecutivelyAssignedTrajectories.put(lane, -1);
                            AbstractSpaceSamplerFdSource.this.assignedTrajectories.put(lane, new TreeSet<>());
                        }
                        AbstractSpaceSamplerFdSource.this.lastUpdateTime = null; // so the increaseTime call is not skipped
                        while ((AbstractSpaceSamplerFdSource.this.periodNumber + 1) * getUpdateInterval().si
                                + AbstractSpaceSamplerFdSource.this.aggregationPeriod.si <= time.si)
                        {
                            increaseTime(Duration
                                    .instantiateSI((AbstractSpaceSamplerFdSource.this.periodNumber + 1) * getUpdateInterval().si
                                            + AbstractSpaceSamplerFdSource.this.aggregationPeriod.si));
                            // TODO: if multiple plots are coupled to the same source, other plots are not invalidated
                            // TODO: change of aggregation period / update freq, is not updated in the GUI on other plots
                            // for (FundamentalDiagram diagram : getDiagrams())
                            // {
                            // }
                        }
                        AbstractSpaceSamplerFdSource.this.invalid = false;
                    }
                }
            }, "Fundamental diagram recalculation").start();
        }

        @Override
        public Duration getDelay()
        {
            return Duration.instantiateSI(1.0);
        }

        @Override
        public synchronized void increaseTime(final Duration time)
        {
            if (time.si < this.aggregationPeriod.si)
            {
                // skip periods that fall below 0.0 time
                return;
            }

            if (this.lastUpdateTime != null && time.le(this.lastUpdateTime))
            {
                // skip updates from different graphs at the same time
                return;
            }
            this.lastUpdateTime = time;

            // ensure capacity
            int nextPeriod = this.periodNumber + 1;
            if (nextPeriod >= this.firstMeasurement[0].length - 1)
            {
                for (int i = 0; i < this.nSeries; i++)
                {
                    this.firstMeasurement[i] = GraphUtil.ensureCapacity(this.firstMeasurement[i], nextPeriod + 1);
                    this.secondMeasurement[i] = GraphUtil.ensureCapacity(this.secondMeasurement[i], nextPeriod + 1);
                }
            }

            // loop positions and trajectories
            Duration startTime = time.minus(this.aggregationPeriod);
            double first = 0;
            double second = 0.0;
            for (int series = 0; series < this.space.getNumberOfSeries(); series++)
            {
                Iterator<L> it = this.space.iterator(series);
                while (it.hasNext())
                {
                    L lane = it.next();
                    if (!this.sampler.getSamplerData().contains(lane))
                    {
                        // sampler has not yet started to record on this lane
                        continue;
                    }
                    TrajectoryGroup<?> trajectoryGroup = this.sampler.getSamplerData().getTrajectoryGroup(lane);
                    int last = this.lastConsecutivelyAssignedTrajectories.get(lane);
                    SortedSet<Integer> assigned = this.assignedTrajectories.get(lane);
                    if (!this.aggregateLanes)
                    {
                        first = 0.0;
                        second = 0.0;
                    }

                    // Length x = this.crossSection.position(series);
                    int i = 0;
                    for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                    {
                        // we can skip all assigned trajectories, which are all up to and including 'last' and all in 'assigned'
                        if (i > last && !assigned.contains(i))
                        {
                            // quickly filter
                            if (GraphUtil.considerTrajectory(trajectory, startTime, time))
                            {
                                double[] measurements = new double[2];
                                getMeasurements(trajectory, startTime, time, lane.getLength(), series, measurements);
                                first += measurements[0];
                                second += measurements[1];
                            }
                            if (trajectory.getT(trajectory.size() - 1) < startTime.si - getDelay().si)
                            {
                                assigned.add(i);
                            }
                        }
                        i++;
                    }
                    if (!this.aggregateLanes)
                    {
                        this.firstMeasurement[series][nextPeriod] = first;
                        this.secondMeasurement[series][nextPeriod] = second;
                    }

                    // consolidate list of assigned trajectories in 'all up to n' and 'these specific ones beyond n'
                    if (!assigned.isEmpty())
                    {
                        int possibleNextLastAssigned = assigned.first();
                        while (possibleNextLastAssigned == last + 1) // consecutive or very first
                        {
                            last = possibleNextLastAssigned;
                            assigned.remove(possibleNextLastAssigned);
                            possibleNextLastAssigned = assigned.isEmpty() ? -1 : assigned.first();
                        }
                        this.lastConsecutivelyAssignedTrajectories.put(lane, last);
                    }
                }
            }
            if (this.aggregateLanes)
            {
                // whatever we measured, it was summed and can be normalized per line like this
                this.firstMeasurement[0][nextPeriod] = first / this.space.getNumberOfSeries();
                this.secondMeasurement[0][nextPeriod] = second / this.space.getNumberOfSeries();
            }
            this.periodNumber = nextPeriod;
        }

        @Override
        public int getNumberOfSeries()
        {
            // if there is an active plot draw as the data is being recalculated, data on invalid items is requested
            // a call to getSeriesCount() indicates a new draw, and during a recalculation the data is limited but valid
            this.invalid = false;
            return this.nSeries;
        }

        @Override
        public void setAggregateName(final String aggregateName)
        {
            this.aggregateName = aggregateName;
        }

        @Override
        public String getName(final int series)
        {
            if (this.aggregateLanes)
            {
                return this.aggregateName;
            }
            return this.space.getName(series);
        }

        @Override
        public int getItemCount(final int series)
        {
            return this.periodNumber + 1;
        }

        @Override
        public final double getFlow(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return getVehicleCount(this.firstMeasurement[series][item], this.secondMeasurement[series][item])
                    / this.aggregationPeriod.si;
        }

        @Override
        public final double getDensity(final int series, final int item)
        {
            return getFlow(series, item) / getSpeed(series, item);
        }

        @Override
        public final double getSpeed(final int series, final int item)
        {
            if (this.invalid)
            {
                return Double.NaN;
            }
            return getSpeed(this.firstMeasurement[series][item], this.secondMeasurement[series][item]);
        }

        @Override
        public final boolean isAggregate()
        {
            return this.aggregateLanes;
        }

        /**
         * Returns the first and the second measurement of a trajectory. For a cross-section this is 1 and the vehicle speed if
         * the trajectory crosses the location, and for a path it is the traveled distance and the traveled time. If the
         * trajectory didn't cross the cross section or space-time range, both should be 0.
         * @param trajectory trajectory
         * @param startTime start time of aggregation period
         * @param endTime end time of aggregation period
         * @param length length of the section (to cut off possible lane overshoot of trajectories)
         * @param series series number in the section
         * @param measurements array with length 2 to place the first and second measurement in
         */
        protected abstract void getMeasurements(Trajectory<?> trajectory, Duration startTime, Duration endTime, Length length,
                int series, double[] measurements);

        /**
         * Returns the vehicle count of two related measurement values. For a cross section: vehicle count & sum of speeds (or
         * sum of inverted speeds for the harmonic mean). For a path: total traveled distance & total traveled time.
         * <p>
         * The value will be divided by the aggregation time to calculate flow. Hence, for a cross section the first measurement
         * should be returned, while for a path the first measurement divided by the section length should be returned. That
         * will end up to equate to {@code q = sum(x)/XT}.
         * @param first first measurement value
         * @param second second measurement value
         * @return flow
         */
        protected abstract double getVehicleCount(double first, double second);

        /**
         * Returns the speed of two related measurement values. For a cross section: vehicle count & sum of speeds (or sum of
         * inverted speeds for the harmonic mean). For a path: total traveled distance & total traveled time.
         * @param first first measurement value
         * @param second second measurement value
         * @return speed
         */
        protected abstract double getSpeed(double first, double second);

    }

    /**
     * Class to group multiple sources in plot.
     */
    // TODO: when sub-sources recalculate responding to a click in the graph, they notify only their coupled plots, which are
    // none
    private static class MultiFdSource extends AbstractFdSource
    {

        /** Sources. */
        private FdSource[] sources;

        /** Source names. */
        private String[] sourceNames;

        /**
         * Constructor.
         * @param sources sources
         */
        MultiFdSource(final Map<String, FdSource> sources)
        {
            Throw.when(sources == null || sources.size() == 0, IllegalArgumentException.class,
                    "At least 1 source is required.");
            this.sources = new FdSource[sources.size()];
            this.sourceNames = new String[sources.size()];
            int index = 0;
            for (Entry<String, FdSource> entry : sources.entrySet())
            {
                this.sources[index] = entry.getValue();
                this.sourceNames[index] = entry.getKey();
                index++;
            }
        }

        /**
         * Returns from a series number overall, the index of the sub-source and the series index in that source.
         * @param series overall series number
         * @return index of the sub-source and the series index in that source
         */
        private int[] getSourceAndSeries(final int series)
        {
            int source = 0;
            int sourceSeries = series;
            while (sourceSeries >= this.sources[source].getNumberOfSeries())
            {
                sourceSeries -= this.sources[source].getNumberOfSeries();
                source++;
            }
            return new int[] {source, sourceSeries};
        }

        @Override
        public Duration getUpdateInterval()
        {
            return this.sources[0].getUpdateInterval();
        }

        @Override
        public void setUpdateInterval(final Duration interval, final Duration time)
        {
            for (FdSource source : this.sources)
            {
                source.setUpdateInterval(interval, time);
            }
        }

        @Override
        public Duration getAggregationPeriod()
        {
            return this.sources[0].getAggregationPeriod();
        }

        @Override
        public void setAggregationPeriod(final Duration period)
        {
            for (FdSource source : this.sources)
            {
                source.setAggregationPeriod(period);
            }
        }

        @Override
        public void recalculate(final Duration time)
        {
            for (FdSource source : this.sources)
            {
                source.recalculate(time);
            }
        }

        @Override
        public Duration getDelay()
        {
            return this.sources[0].getDelay();
        }

        @Override
        public void increaseTime(final Duration time)
        {
            for (FdSource source : this.sources)
            {
                source.increaseTime(time);
            }
        }

        @Override
        public int getNumberOfSeries()
        {
            int numberOfSeries = 0;
            for (FdSource source : this.sources)
            {
                numberOfSeries += source.getNumberOfSeries();
            }
            return numberOfSeries;
        }

        @Override
        public String getName(final int series)
        {
            int[] ss = getSourceAndSeries(series);
            return this.sourceNames[ss[0]]
                    + (this.sources[ss[0]].isAggregate() ? "" : ": " + this.sources[ss[0]].getName(ss[1]));
        }

        @Override
        public int getItemCount(final int series)
        {
            int[] ss = getSourceAndSeries(series);
            return this.sources[ss[0]].getItemCount(ss[1]);
        }

        @Override
        public double getFlow(final int series, final int item)
        {
            int[] ss = getSourceAndSeries(series);
            return this.sources[ss[0]].getFlow(ss[1], item);
        }

        @Override
        public double getDensity(final int series, final int item)
        {
            int[] ss = getSourceAndSeries(series);
            return this.sources[ss[0]].getDensity(ss[1], item);
        }

        @Override
        public double getSpeed(final int series, final int item)
        {
            int[] ss = getSourceAndSeries(series);
            return this.sources[ss[0]].getSpeed(ss[1], item);
        }

        @Override
        public boolean isAggregate()
        {
            return false;
        }

        @Override
        public void setAggregateName(final String aggregateName)
        {
            // invalid for this source type
        }

    }

    /**
     * Defines a line plot for a fundamental diagram.
     */
    public interface FdLine
    {
        /**
         * Return the values for the given quantity. For two quantities, this should result in a 2D fundamental diagram line.
         * @param quantity quantity to return value for.
         * @return values for quantity
         */
        double[] getValues(Quantity quantity);

        /**
         * Returns the name of the line, as shown in the legend.
         * @return name of the line, as shown in the legend
         */
        String getName();
    }

    @Override
    public String toString()
    {
        return "FundamentalDiagram [source=" + this.getSource() + ", domainQuantity=" + this.getDomainQuantity()
                + ", rangeQuantity=" + this.getRangeQuantity() + ", otherQuantity=" + this.getOtherQuantity()
                + ", seriesLabels=" + this.seriesLabels + ", graphUpdater=" + this.graphUpdater + ", timeInfo="
                + this.getTimeInfo() + ", legend=" + this.legend + ", laneVisible=" + this.laneVisible + "]";
    }

    /**
     * Get the data source.
     * @return the data source
     */
    public FdSource getSource()
    {
        return this.source;
    }

    /**
     * Retrievee the legend of this FundamentalDiagram.
     * @return the legend
     */
    public LegendItemCollection getLegend()
    {
        return this.legend;
    }

    /**
     * Return the list of lane visibility flags.
     * @return the list of lane visibility flags
     */
    public List<Boolean> getLaneVisible()
    {
        return this.laneVisible;
    }

    /**
     * Return the domain quantity.
     * @return the domain quantity
     */
    public Quantity getDomainQuantity()
    {
        return this.domainQuantity;
    }

    /**
     * Set the domain quantity.
     * @param domainQuantity the new domain quantity
     */
    public void setDomainQuantity(final Quantity domainQuantity)
    {
        this.domainQuantity = domainQuantity;
    }

    /**
     * Get the other (non domain; vertical axis) quantity.
     * @return the quantity for the vertical axis
     */
    public Quantity getOtherQuantity()
    {
        return this.otherQuantity;
    }

    /**
     * Set the other (non domain; vertical axis) quantity.
     * @param otherQuantity the quantity for the vertical axis
     */
    public void setOtherQuantity(final Quantity otherQuantity)
    {
        this.otherQuantity = otherQuantity;
    }

    /**
     * Get the range quantity.
     * @return the range quantity
     */
    public Quantity getRangeQuantity()
    {
        return this.rangeQuantity;
    }

    /**
     * Set the range quantity.
     * @param rangeQuantity the new range quantity
     */
    public void setRangeQuantity(final Quantity rangeQuantity)
    {
        this.rangeQuantity = rangeQuantity;
    }

    /**
     * Retrieve the time info.
     * @return the time info
     */
    public String getTimeInfo()
    {
        return this.timeInfo;
    }

    /**
     * Set the time info.
     * @param timeInfo the new time info
     */
    public void setTimeInfo(final String timeInfo)
    {
        this.timeInfo = timeInfo;
    }

    /**
     * Return whether the plot has a fundamental diagram line.
     * @return whether the plot has a fundamental diagram line
     */
    public boolean hasLineFD()
    {
        return this.fdLine != null;
    }

}
