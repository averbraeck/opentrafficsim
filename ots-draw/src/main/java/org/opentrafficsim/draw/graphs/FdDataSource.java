package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.FdPaintState;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.FdSeries;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Data source for a fundamental diagram.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class FdDataSource extends PlotDelegate<FdPaintState, FundamentalDiagram>
{

    /** Updates per period. */
    public static final EventType UPDATES_PER_PERIOD = new EventType("UPDATES_PER_PERIOD", new MetaData("Updates per period",
            "Updates per period", new ObjectDescriptor("Updates per period", "Updates per period", Integer.class)));

    /** Aggregation period. */
    public static final EventType AGGREGATION_PERIOD = new EventType("AGGREGATION_PERIOD", new MetaData("Aggregation period",
            "Aggregation period", new ObjectDescriptor("Aggregation period", "Aggregation period", Duration.class)));

    /** Aggregation periods. */
    static final PlotSetting<Duration> AGGREGATION_PERIODS =
            PlotSetting.of(new double[] {5.0, 10.0, 30.0, 60.0, 120.0, 300.0, 900.0}, Duration::ofSI, 3);

    /** Updates per period. */
    static final PlotSetting<Integer> UPDATES = PlotSetting.of(List.of(1, 2, 3, 5, 10), 0);

    /** Updates per period. */
    private int updatesPerPeriod = 1;

    /** Aggregation period. */
    private Duration aggregationPeriod;

    /** Aggregation period setting. */
    private final PlotSetting<Duration> aggregationPeriodSetting;

    /** Updates per period setting. */
    private final PlotSetting<Integer> updatesPerPeriodSetting;

    /**
     * Constructor.
     * @param aggregationInterval aggregation interval
     * @param delay delay
     * @param plotScheduler plot scheduler
     */
    public FdDataSource(final Duration aggregationInterval, final Duration delay, final PlotScheduler plotScheduler)
    {
        this(aggregationInterval, delay, plotScheduler, AGGREGATION_PERIODS, UPDATES);
    }

    /**
     * Constructor.
     * @param aggregationInterval aggregation interval
     * @param delay delay
     * @param plotScheduler plot scheduler
     * @param aggregationPeriodSetting setting for aggregation period
     * @param updatesPerPeriodSetting setting for updates per period
     */
    public FdDataSource(final Duration aggregationInterval, final Duration delay, final PlotScheduler plotScheduler,
            final PlotSetting<Duration> aggregationPeriodSetting, final PlotSetting<Integer> updatesPerPeriodSetting)
    {
        super(aggregationInterval, delay, plotScheduler);
        this.aggregationPeriod = aggregationInterval;
        this.aggregationPeriodSetting = aggregationPeriodSetting;
        this.updatesPerPeriodSetting = updatesPerPeriodSetting;
    }

    /**
     * Returns the aggregation period setting.
     * @return aggregation period setting
     */
    public PlotSetting<Duration> getAggregationPeriodSetting()
    {
        return this.aggregationPeriodSetting;
    }

    /**
     * Returns the updates per period setting.
     * @return updates per period setting
     */
    public PlotSetting<Integer> getUpdatesPerPeriodSetting()
    {
        return this.updatesPerPeriodSetting;
    }

    /**
     * The updates per period.
     * @return updates per period
     */
    public int getUpdatesPerPeriod()
    {
        return this.updatesPerPeriod;
    }

    /**
     * Changes the updates per period.
     * @param n updates per period
     */
    public void setUpdatesPerPeriod(final int n)
    {
        synchronized (this)
        {
            this.updatesPerPeriod = n;
            Duration update = this.aggregationPeriod.divide(this.updatesPerPeriod);
            getPlots().forEach((p) -> p.offerUpdateInterval(update));
            invalidateTimeSpan();
        }
        fireEvent(UPDATES_PER_PERIOD, new Object[] {n});
    }

    /**
     * The aggregation period.
     * @return aggregation period
     */
    public Duration getAggregationPeriod()
    {
        return this.aggregationPeriod;
    }

    /**
     * Changes the aggregation period.
     * @param aggregationPeriod aggregation period
     */
    public void setAggregationPeriod(final Duration aggregationPeriod)
    {
        synchronized (this)
        {
            this.aggregationPeriod = aggregationPeriod;
            Duration update = this.aggregationPeriod.divide(this.updatesPerPeriod);
            getPlots().forEach((p) -> p.offerUpdateInterval(update));
            invalidateTimeSpan();
        }
        fireEvent(AGGREGATION_PERIOD, new Object[] {aggregationPeriod});
    }

    /**
     * Returns the update interval.
     * @return update interval
     */
    public Duration getUpdateInterval()
    {
        return getAggregationPeriod().divide(getUpdatesPerPeriod());
    }

    @Override
    public void calculatePaintStateUnsafe(final Duration time)
    {
        FdPaintState paintState = getPaintState(time);
        for (FundamentalDiagram plot : getPlots())
        {
            plot.offerPaintState(paintState);
        }
    }

    /**
     * Hook for a meta-source that combines fundamental diagram sources.
     * @param time current time
     * @return paint state
     */
    abstract FdPaintState getPaintState(Duration time);

    /**
     * Returns the number of series (i.e. lanes or 1 for aggregated).
     * @return number of series
     */
    abstract int getNumberOfSeries();

    /**
     * Returns a name of the series.
     * @param series series number
     * @return name of the series
     */
    abstract String getName(int series);

    /**
     * Returns whether this source aggregates lanes.
     * @return whether this source aggregates lanes
     */
    abstract boolean isAggregate();

    /**
     * Sets the name of the series when aggregated, e.g. for legend. Default is "Aggregate".
     * @param aggregateName name of the series when aggregated
     */
    public abstract void setAggregateName(String aggregateName);

    /**
     * Creates a {@link FdDataSource} from a sampler and positions.
     * @param sampler sampler
     * @param plotScheduler plot scheduler
     * @param crossSection cross section
     * @param aggregateLanes whether to aggregate the positions
     * @param aggregationTime aggregation time (and update time)
     * @param harmonic harmonic mean
     * @return source for a fundamental diagram from a sampler and positions
     * @param <L> LaneData
     */
    public static <L extends LaneData<L>> FdDataSource sourceFromSampler(final Sampler<?, L> sampler,
            final PlotScheduler plotScheduler, final GraphCrossSection<L> crossSection, final boolean aggregateLanes,
            final Duration aggregationTime, final boolean harmonic)
    {
        return new CrossSectionFdDataSource<>(sampler, plotScheduler, crossSection, aggregateLanes, aggregationTime, harmonic);
    }

    /**
     * Creates a {@link FdDataSource} from a sampler and positions.
     * @param sampler sampler
     * @param plotScheduler plot scheduler
     * @param path cross section
     * @param aggregateLanes whether to aggregate the positions
     * @param aggregationTime aggregation time (and update time)
     * @return source for a fundamental diagram from a sampler and positions
     * @param <L> LaneData
     */
    public static <L extends LaneData<L>> FdDataSource sourceFromSampler(final Sampler<?, L> sampler,
            final PlotScheduler plotScheduler, final GraphPath<L> path, final boolean aggregateLanes,
            final Duration aggregationTime)
    {
        return new PathFdDataSource<>(sampler, plotScheduler, path, aggregateLanes, aggregationTime);
    }

    /**
     * Combines multiple sources in to one source.
     * @param sources sources coupled to their names for in the legend
     * @return combined source
     */
    public static FdDataSource combinedSource(final Map<String, FdDataSource> sources)
    {
        return new MultiFdSource(sources);
    }

    /**
     * Fundamental diagram source based on a cross section.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private static final class CrossSectionFdDataSource<L extends LaneData<L>, S extends GraphCrossSection<L>>
            extends AbstractFdDataSource<L, S>
    {

        /** Margin to check GTU has passed the location. */
        private static final Length EPS = Length.ofSI(1e-9);

        /** Harmonic mean. */
        private final boolean harmonic;

        /**
         * Constructor.
         * @param sampler sampler
         * @param plotScheduler plot scheduler
         * @param crossSection cross section
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation period
         * @param harmonic harmonic mean
         */
        private CrossSectionFdDataSource(final Sampler<?, L> sampler, final PlotScheduler plotScheduler, final S crossSection,
                final boolean aggregateLanes, final Duration aggregationPeriod, final boolean harmonic)
        {
            super(sampler, plotScheduler, crossSection, aggregateLanes, aggregationPeriod);
            this.harmonic = harmonic;
        }

        @Override
        protected void getMeasurements(final Trajectory<?> trajectory, final Duration startTime, final Duration endTime,
                final Length length, final int series, final double[] measurements)
        {
            Length x = getSpace().position(series);
            if (GraphUtil.considerTrajectory(trajectory, x.minus(EPS), x.plus(EPS)))
            {
                // detailed check
                if (trajectory.getSpeedAtPosition(x).si != 0.0) // prevent edge case
                {
                    Duration t = trajectory.getTimeAtPosition(x);
                    if (t.si >= startTime.si && t.si < endTime.si)
                    {
                        measurements[0] = 1; // first = count
                        measurements[1] = // second = sum of (inverted) speeds
                                this.harmonic ? 1.0 / trajectory.getSpeedAtPosition(x).si : trajectory.getSpeedAtPosition(x).si;
                    }
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
            return "CrossSectionFdDataSource [harmonic=" + this.harmonic + "]";
        }

    }

    /**
     * Fundamental diagram source based on a path. Density, speed and flow over the entire path are calculated per lane.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private static final class PathFdDataSource<L extends LaneData<L>, S extends GraphPath<L>>
            extends AbstractFdDataSource<L, S>
    {

        /**
         * Constructor.
         * @param sampler sampler
         * @param plotScheduler plot scheduler
         * @param path path
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation period
         */
        private PathFdDataSource(final Sampler<?, L> sampler, final PlotScheduler plotScheduler, final S path,
                final boolean aggregateLanes, final Duration aggregationPeriod)
        {
            super(sampler, plotScheduler, path, aggregateLanes, aggregationPeriod);
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
            return "PathFdDataSource []";
        }

    }

    /**
     * Abstract class that deals with updating and recalculating the fundamental diagram.
     * @param <L> lane data type
     * @param <S> underlying source type
     */
    private abstract static class AbstractFdDataSource<L extends LaneData<L>, S extends AbstractGraphSpace<L>>
            extends FdDataSource
    {

        /** Period number of last calculated period. */
        private int periodNumber = -1;

        /** Last update time. */
        private Duration lastUpdateTime;

        /** Number of series. */
        private final int nSeries;

        /** First data. */
        private double[][] firstMeasurement;

        /** Second data. */
        private double[][] secondMeasurement;

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
         * @param plotScheduler plot scheduler
         * @param space space
         * @param aggregateLanes whether to aggregate the lanes
         * @param aggregationPeriod initial aggregation period
         */
        private AbstractFdDataSource(final Sampler<?, L> sampler, final PlotScheduler plotScheduler, final S space,
                final boolean aggregateLanes, final Duration aggregationPeriod)
        {
            super(aggregationPeriod, Duration.ONE, plotScheduler);
            this.sampler = sampler;
            this.space = space;
            this.aggregateLanes = aggregateLanes;
            this.nSeries = aggregateLanes ? 1 : space.getNumberOfSeries();
            // create and register kpi lane directions
            for (L laneDirection : space)
            {
                sampler.registerSpaceTimeRegion(new SpaceTimeRegion<>(laneDirection, Length.ZERO, laneDirection.getLength(),
                        sampler.now(), Duration.ofSI(Double.MAX_VALUE)));

                // info per kpi lane direction
                this.lastConsecutivelyAssignedTrajectories.put(laneDirection, -1);
                this.assignedTrajectories.put(laneDirection, new TreeSet<>());
            }
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
        public Duration getDelay()
        {
            return Duration.ONE;
        }

        @Override
        public FdPaintState getPaintState(final Duration time)
        {
            boolean redo;
            Duration aggregationPeriod;
            synchronized (AbstractFdDataSource.this)
            {
                redo = getAndResetInvalidTimeSpan();
                aggregationPeriod = getAggregationPeriod();
            }

            if (redo)
            {
                this.periodNumber = -1;
                this.firstMeasurement = new double[AbstractFdDataSource.this.nSeries][10];
                this.secondMeasurement = new double[AbstractFdDataSource.this.nSeries][10];
                this.lastConsecutivelyAssignedTrajectories.clear();
                this.assignedTrajectories.clear();
                for (L lane : AbstractFdDataSource.this.space)
                {
                    AbstractFdDataSource.this.lastConsecutivelyAssignedTrajectories.put(lane, -1);
                    AbstractFdDataSource.this.assignedTrajectories.put(lane, new TreeSet<>());
                }
                this.lastUpdateTime = null;
            }

            while ((AbstractFdDataSource.this.periodNumber + 2) * aggregationPeriod.si <= time.si)
            {
                increaseTime(Duration.ofSI((this.periodNumber + 2) * aggregationPeriod.si), aggregationPeriod);
            }

            return buildPaintState(aggregationPeriod);
        }

        /**
         * Add time slice to data.
         * @param time end time of slice
         * @param aggregationPeriod aggregation period
         */
        private void increaseTime(final Duration time, final Duration aggregationPeriod)
        {
            if (time.si < getAggregationPeriod().si)
            {
                // skip periods that fall below 0.0 time
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
            Duration startTime = time.minus(aggregationPeriod);
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
                    TrajectoryGroup<?> trajectoryGroup = this.sampler.getSamplerData().getTrajectoryGroup(lane).get();
                    int last = this.lastConsecutivelyAssignedTrajectories.get(lane);
                    SortedSet<Integer> assigned = this.assignedTrajectories.get(lane);
                    if (!this.aggregateLanes)
                    {
                        first = 0.0;
                        second = 0.0;
                    }

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

        /**
         * Builds the current state of data for painting.
         * @param aggregationPeriod aggregation period
         * @return current paint state
         */
        private FdPaintState buildPaintState(final Duration aggregationPeriod)
        {
            FdSeries[] series = new FdSeries[getNumberOfSeries()];
            int n = this.periodNumber + 1;
            for (int i = 0; i < getNumberOfSeries(); i++)
            {
                float[] q = new float[n];
                float[] v = new float[n];
                float[] k = new float[n];
                for (int j = 0; j < n; j++)
                {
                    q[j] = (float) (3600 * getItemFlow(i, j, aggregationPeriod));
                    v[j] = (float) (3.6 * getItemSpeed(i, j));
                    k[j] = q[j] / v[j];
                }
                FdSeries serie = new FdSeries(q, v, k);
                series[i] = serie;
            }
            return new FdPaintState(series, this.lastUpdateTime);
        }

        @Override
        public int getNumberOfSeries()
        {
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
        public final boolean isAggregate()
        {
            return this.aggregateLanes;
        }

        /**
         * Returns the flow value for the given item.
         * @param series series
         * @param item item in series
         * @param aggregationPeriod aggregation period
         * @return flow value for item
         */
        private double getItemFlow(final int series, final int item, final Duration aggregationPeriod)
        {
            return getVehicleCount(this.firstMeasurement[series][item], this.secondMeasurement[series][item])
                    / aggregationPeriod.si;
        }

        /**
         * Returns the speed value for the given item.
         * @param series series
         * @param item item in series
         * @return speed value for item
         */
        private double getItemSpeed(final int series, final int item)
        {
            return getSpeed(this.firstMeasurement[series][item], this.secondMeasurement[series][item]);
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
    private static final class MultiFdSource extends FdDataSource
    {

        /** Sources. */
        private FdDataSource[] sources;

        /** Source names. */
        private String[] sourceNames;

        /**
         * Constructor.
         * @param sources sources
         */
        private MultiFdSource(final Map<String, FdDataSource> sources)
        {
            super(sources.values().iterator().next().getAggregationPeriod(), Duration.ONE,
                    sources.values().iterator().next().getPlotScheduler());
            Throw.when(sources == null || sources.size() == 0, IllegalArgumentException.class,
                    "At least 1 source is required.");
            this.sources = new FdDataSource[sources.size()];
            this.sourceNames = new String[sources.size()];
            int index = 0;
            for (Entry<String, FdDataSource> entry : sources.entrySet())
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
        public void setAggregationPeriod(final Duration period)
        {
            for (FdDataSource source : this.sources)
            {
                source.setAggregationPeriod(period);
            }
        }

        @Override
        public void setUpdatesPerPeriod(final int n)
        {
            for (FdDataSource source : this.sources)
            {
                source.setUpdatesPerPeriod(n);
            }
        }

        @Override
        public Duration getDelay()
        {
            return this.sources[0].getDelay();
        }

        @Override
        public int getNumberOfSeries()
        {
            int numberOfSeries = 0;
            for (FdDataSource source : this.sources)
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
        public FdPaintState getPaintState(final Duration time)
        {
            List<FdPaintState> paintStates = new ArrayList<>();
            int n = 0;
            for (FdDataSource source : this.sources)
            {
                FdPaintState state = source.getPaintState(time);
                paintStates.add(state);
                n += state.getSeriesCount();
            }
            FdSeries[] series = new FdSeries[n];
            int i = 0;
            for (FdPaintState state : paintStates)
            {
                System.arraycopy(state.fdSeries(), 0, series, i, state.getSeriesCount());
                i += state.getSeriesCount();
            }
            return new FdPaintState(series, time);
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

}
