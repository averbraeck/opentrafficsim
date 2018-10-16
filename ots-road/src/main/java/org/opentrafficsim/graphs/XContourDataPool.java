package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.WeightedMeanAndSum;
import org.opentrafficsim.core.animation.EGTF;
import org.opentrafficsim.core.animation.EGTF.Converter;
import org.opentrafficsim.core.animation.EGTF.DataSource;
import org.opentrafficsim.core.animation.EGTF.DataStream;
import org.opentrafficsim.core.animation.EGTF.EgtfEvent;
import org.opentrafficsim.core.animation.EGTF.EgtfListener;
import org.opentrafficsim.core.animation.EGTF.Filter;
import org.opentrafficsim.core.animation.EGTF.Quantity;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.RoadSampler;

import nl.tudelft.simulation.language.Throw;

/**
 * Class that contains data for contour plots. One data pool can be shared between contour plots, in which case the granularity,
 * path, sampler, update interval, and whether the data is smoothed (EGTF) are equal between the plots.
 * <p>
 * By default the pool contains traveled time and traveled distance per cell.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 5 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class XContourDataPool
{

    // *************************
    // *** GLOBAL PROPERTIES ***
    // *************************

    /** Space granularity values. */
    protected static final double[] DEFAULT_SPACE_GRANULARITIES = { 10, 20, 50, 100, 200, 500, 1000 };

    /** Index of the initial space granularity. */
    protected static final int DEFAULT_SPACE_GRANULARITY_INDEX = 3;

    /** Time granularity values. */
    protected static final double[] DEFAULT_TIME_GRANULARITIES = { 1, 2, 5, 10, 20, 30, 60, 120, 300, 600 };

    /** Index of the initial time granularity. */
    protected static final int DEFAULT_TIME_GRANULARITY_INDEX = 3;

    /** Initial lower bound for the time scale. */
    protected static final Time DEFAULT_LOWER_TIME_BOUND = Time.ZERO;

    /**
     * Total kernel size relative to sigma and tau. This factor is determined through -log(1 - p) with p = 95%. This means that
     * the cumulative exponential distribution has 95% at 3 times sigma or tau. Note that due to a coordinate change in the
     * Adaptive Smoothing Method, the actual cumulative distribution is slightly different. Hence, this is just a heuristic.
     */
    private static final int KERNEL_FACTOR = 3;

    /** Spatial kernel size. Larger value may be used when using a large granularity. */
    private static final Length SIGMA = Length.createSI(100);

    /** Temporal kernel size. Larger value may be used when using a large granularity. */
    private static final Duration TAU = Duration.createSI(10);

    /** Maximum free flow propagation speed. */
    private static final Speed MAX_C_FREE = new Speed(80.0, SpeedUnit.KM_PER_HOUR);

    /** Factor on speed limit to determine vc, the flip over speed between congestion and free flow. */
    private static final double VC_FACRTOR = 0.8;

    /** Congestion propagation speed. */
    private static final Speed C_CONG = new Speed(-18.0, SpeedUnit.KM_PER_HOUR);

    /** Delta v, speed transition region around threshold. */
    private static final Speed DELTA_V = new Speed(10.0, SpeedUnit.KM_PER_HOUR);

    // *****************************
    // *** CONTEXTUAL PROPERTIES ***
    // *****************************

    /** Sampler. */
    private final RoadSampler sampler;

    /** Update interval. */
    private final Duration updateInterval;

    /** Delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories. */
    private final Duration delay;

    /** Path. */
    private final List<LaneDirection> path;

    /** Space axis. */
    private final Axis spaceAxis;

    /** Time axis. */
    private final Axis timeAxis;
    
    /** Registered plots. */
    private Set<XAbstractContourPlot<?>> plots = new LinkedHashSet<>();

    // *****************
    // *** PLOT DATA ***
    // *****************

    /** Total distance traveled per cell. */
    private float[][] distance;

    /** Total time traveled per cell. */
    private float[][] time;

    /** Data of other types. */
    private final Map<ContourDataType<?>, float[][]> additionalData = new LinkedHashMap<>();

    // ****************************
    // *** SMOOTHING PROPERTIES ***
    // ****************************

    /** Whether to smooth data. */
    private boolean smooth = false;

    /** Free flow propagation speed. */
    private Speed cFree;

    /** Flip-over speed between congestion and free flow. */
    private Speed vc;

    /** Smoothing filter. */
    private EGTF egtf;

    /** Data stream for speed. */
    private DataStream<Speed> speedStream;

    /** Data stream for travel time. */
    private DataStream<Duration> travelTimeStream;

    /** Data stream for travel distance. */
    private DataStream<Length> travelDistanceStream;

    /** Quantity for travel time. */
    private final Quantity<Duration, double[][]> travelTimeQuantity = new Quantity<>("travel time", Converter.SI);

    /** Quantity for travel distance. */
    private final Quantity<Length, double[][]> travelDistanceQuantity = new Quantity<>("travel distance", Converter.SI);

    /** Data streams for any additional data. */
    private Map<ContourDataType<?>, DataStream<?>> additionalStreams = new LinkedHashMap<>();

    // *****************************
    // *** CONTINUITY PROPERTIES ***
    // *****************************

    /** Updater for update times. */
    private final XGraphUpdater<Time> graphUpdater;

    /** Whether any command since or during the last update asks for a complete redo. */
    private boolean redo = true;

    /** Time up to which to determine data. This is a multiple of the update interval, which is now, or recent on a redo. */
    private Time toTime;

    /** Number of items that are ready. To return NaN values if not ready, and for operations between consecutive updates. */
    private int readyItems = -1;

    /** Selected space granularity, to be set and taken on the next update. */
    private Double desiredSpaceGranularity = null;

    /** Selected time granularity, to be set and taken on the next update. */
    private Double desiredTimeGranularity = null;

    // ********************
    // *** CONSTRUCTORS ***
    // ********************

    /**
     * Constructor using default granularities.
     * @param sampler RoadSampler; sampler
     * @param path List&lt;LaneDirection&gt;; path
     */
    public XContourDataPool(final RoadSampler sampler, final List<LaneDirection> path)
    {
        this(sampler, Duration.createSI(1.0), path, DEFAULT_SPACE_GRANULARITIES, DEFAULT_SPACE_GRANULARITY_INDEX,
                DEFAULT_TIME_GRANULARITIES, DEFAULT_TIME_GRANULARITY_INDEX, DEFAULT_LOWER_TIME_BOUND,
                XAbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND);
    }

    /**
     * Constructor for non-default input.
     * @param sampler RoadSampler; sampler
     * @param delay Duration; delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     * @param path List&lt;LaneDirection&gt;; path
     * @param spaceGranularity double[]; granularity options for space dimension
     * @param initSpaceIndex int; initial selected space granularity
     * @param timeGranularity double[]; granularity options for time dimension
     * @param initTimeIndex int; initial selected time granularity
     * @param start Time; start time
     * @param initialEnd Time; initial end time of plots, will be expanded if simulation time exceeds it
     */
    @SuppressWarnings("parameternumber")
    public XContourDataPool(final RoadSampler sampler, final Duration delay, final List<LaneDirection> path,
            final double[] spaceGranularity, final int initSpaceIndex, final double[] timeGranularity, final int initTimeIndex,
            final Time start, final Time initialEnd)
    {
        this.sampler = sampler;
        this.updateInterval = Duration.createSI(timeGranularity[initTimeIndex]);
        this.delay = delay;
        this.path = path;
        double length = 0;
        for (LaneDirection lane : path)
        {
            length += lane.getLength().si;
        }
        this.spaceAxis = new Axis(0.0, length, spaceGranularity[initSpaceIndex], spaceGranularity);
        this.timeAxis = new Axis(start.si, initialEnd.si, timeGranularity[initTimeIndex], timeGranularity);

        // get length-weighted mean speed limit from path to determine cFree and Vc for smoothing
        Speed meanSpeedLimit;
        try
        {
            WeightedMeanAndSum<Speed, Length> mean = new WeightedMeanAndSum<>();
            for (LaneDirection lane : getPath())
            {
                mean.add(lane.getLane().getLowestSpeedLimit(), lane.getLength());
            }
            meanSpeedLimit = Speed.createSI(mean.getMean());
        }
        catch (@SuppressWarnings("unused") NetworkException exception)
        {
            meanSpeedLimit = MAX_C_FREE;
        }
        this.cFree = Speed.min(meanSpeedLimit, MAX_C_FREE);
        this.vc = Speed.min(meanSpeedLimit.multiplyBy(VC_FACRTOR), MAX_C_FREE);

        // setup updater to do the actual work in another thread
        this.graphUpdater = new XGraphUpdater<>("DataPool worker", Thread.currentThread(), (t) -> update(t));
    }

    // ************************************
    // *** PLOT INTERFACING AND GETTERS ***
    // ************************************

    /**
     * Returns the sampler for an {@code AbstractContourPlot} using this {@code ContourDataPool}.
     * @return RoadSampler; the sampler
     */
    public final RoadSampler getSampler()
    {
        return this.sampler;
    }

    /**
     * Returns the update interval for an {@code AbstractContourPlot} using this {@code ContourDataPool}.
     * @return Duration; update interval
     */
    final Duration getUpdateInterval()
    {
        return this.updateInterval;
    }

    /**
     * Returns the delay for an {@code AbstractContourPlot} using this {@code ContourDataPool}.
     * @return Duration; delay
     */
    final Duration getDelay()
    {
        return this.delay;
    }

    /**
     * Returns the path for an {@code AbstractContourPlot} using this {@code ContourDataPool}.
     * @return List&lt;LaneDirection&gt;; the path
     */
    final List<LaneDirection> getPath()
    {
        return this.path;
    }

    /**
     * Register a contour plot to this data pool. The contour constructor will do this.
     * @param contourPlot AbstractContourPlot; contour plot
     */
    final void registerContourPlot(final XAbstractContourPlot<?> contourPlot)
    {
        ContourDataType<?> contourDataType = contourPlot.getContourDataType();
        if (contourDataType != null)
        {
            this.additionalData.put(contourDataType, null);
        }
        this.plots.add(contourPlot);
    }

    /**
     * Returns the bin count.
     * @param dimension Dimension; space or time
     * @return int; bin count
     */
    final int getBinCount(final Dimension dimension)
    {
        return dimension.getAxis(this).getBinCount();
    }

    /**
     * Returns the size of a bin. Usually this is equal to the granularity, except for the last which is likely smaller.
     * @param dimension Dimension; space or time
     * @param item int; item number (cell number in contour plot)
     * @return double; the size of a bin
     */
    final synchronized double getBinSize(final Dimension dimension, final int item)
    {
        int n = dimension.equals(Dimension.DISTANCE) ? getSpaceBin(item) : getTimeBin(item);
        double[] ticks = dimension.getAxis(this).getTicks();
        return ticks[n + 1] - ticks[n];
    }

    /**
     * Returns the value on the axis of an item.
     * @param dimension Dimension; space or time
     * @param item int; item number (cell number in contour plot)
     * @return double; the value on the axis of this item
     */
    final double getAxisValue(final Dimension dimension, final int item)
    {
        if (dimension.equals(Dimension.DISTANCE))
        {
            return this.spaceAxis.getBinValue(getSpaceBin(item));
        }
        return this.timeAxis.getBinValue(getTimeBin(item));
    }

    /**
     * Returns the axis bin number of the given value.
     * @param dimension Dimension; space or time
     * @param value double; value
     * @return int; axis bin number of the given value
     */
    final int getAxisBin(final Dimension dimension, final double value)
    {
        if (dimension.equals(Dimension.DISTANCE))
        {
            return this.spaceAxis.getValueBin(value);
        }
        return this.timeAxis.getValueBin(value);
    }

    /**
     * Returns the available granularities that a linked plot may use.
     * @param dimension Dimension; space or time
     * @return double[]; available granularities that a linked plot may use
     */
    @SuppressWarnings("synthetic-access")
    final double[] getGranularities(final Dimension dimension)
    {
        return dimension.getAxis(this).granularities;
    }

    /**
     * Returns the selected granularity that a linked plot should use.
     * @param dimension Dimension; space or time
     * @return double; granularity that a linked plot should use
     */
    @SuppressWarnings("synthetic-access")
    final double getGranularity(final Dimension dimension)
    {
        return dimension.getAxis(this).granularity;
    }

    /**
     * Called by {@code AbstractContourPlot} to update the time. This will invalidate the plot triggering a redraw.
     * @param updateTime Time; current time
     */
    @SuppressWarnings("synthetic-access")
    final synchronized void increaseTime(final Time updateTime)
    {
        if (updateTime.si > this.timeAxis.maxValue)
        {
            this.timeAxis.setMaxValue(updateTime.si);
        }
        if (this.toTime == null || updateTime.si > this.toTime.si) // null at initialization
        {
            invalidate(updateTime);
        }
    }

    /**
     * Sets the granularity of the plot. This will invalidate the plot triggering a redraw.
     * @param dimension Dimension; space or time
     * @param granularity double; granularity in space or time (SI unit)
     */
    final synchronized void setGranularity(final Dimension dimension, final double granularity)
    {
        if (dimension.equals(Dimension.DISTANCE))
        {
            this.desiredSpaceGranularity = granularity;
            for (XAbstractContourPlot<?> contourPlot : XContourDataPool.this.plots)
            {
                contourPlot.setSpaceGranularityRadioButton(granularity);
            }
        }
        else
        {
            this.desiredTimeGranularity = granularity;
            for (XAbstractContourPlot<?> contourPlot : XContourDataPool.this.plots)
            {
                contourPlot.setUpdateInterval(Duration.createSI(granularity));
                contourPlot.setTimeGranularityRadioButton(granularity);
            }
        }
        invalidate(null);
    }

    /**
     * Sets bi-linear interpolation enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param interpolate boolean; whether to enable interpolation
     */
    @SuppressWarnings("synthetic-access")
    final void setInterpolate(final boolean interpolate)
    {
        if (this.timeAxis.interpolate != interpolate)
        {
            synchronized (this)
            {
                this.timeAxis.setInterpolate(interpolate);
                this.spaceAxis.setInterpolate(interpolate);
                for (XAbstractContourPlot<?> contourPlot : XContourDataPool.this.plots)
                {
                    contourPlot.setInterpolation(interpolate);
                }
                invalidate(null);
            }
        }
    }
    
    /**
     * Sets the adaptive smoothing enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param smooth boolean; whether to smooth the plor
     */
    final void setSmooth(final boolean smooth)
    {
        if (this.smooth != smooth)
        {
            synchronized (this)
            {
                this.smooth = smooth;
                for (XAbstractContourPlot<?> contourPlot : XContourDataPool.this.plots)
                {
                    contourPlot.setSmoothing(smooth);
                }
                invalidate(null);
            }
        }
    }

    // ************************
    // *** UPDATING METHODS ***
    // ************************

    /**
     * Each method that changes a setting such that the plot is no longer valid, should call this method after the setting was
     * changed. If time is updated (increased), it should be given as input in to this method. The given time <i>should</i> be
     * {@code null} if the plot is not valid for any other reason. In this case a full redo is initiated.
     * <p>
     * Every method calling this method should be {@code synchronized}, at least for the part where the setting is changed and
     * this method is called. This method will in all cases add an update request to the updater, working in another thread. It
     * will invoke method {@code update()}. That method utilizes a synchronized block to obtain all synchronization sensitive
     * data, before starting the actual work.
     * @param t Time; time up to which to show data
     */
    private synchronized void invalidate(final Time t)
    {
        if (t != null)
        {
            this.toTime = t;
        }
        else
        {
            this.redo = true;
        }
        if (this.toTime != null) // null at initialization
        {
            // either a later time was set, or time was null and a redo is required (will be picked up through the redo field)
            // note that we cannot set {@code null}, hence we set the current to time, which may or may not have just changed
            this.graphUpdater.offer(this.toTime);
        }
    }

    /**
     * Heart of the data pool. This method is invoked regularly by the "DataPool worker" thread, as scheduled in a queue through
     * planned updates at an interval, or by user action changing the plot appearance. No two invocations can happen at the same
     * time, as the "DataPool worker" thread executes this method before the next update request from the queue is considered.
     * <p>
     * This method regularly checks conditions that indicate the update should be interrupted as for example a setting has
     * changed and appearance should change. Whenever a new invalidation causes {@code redo = true}, this method can stop as the
     * full data needs to be recalculated. This can be set by any change of e.g. granularity or smoothing, during the update.
     * <p>
     * During the data recalculation, a later update time may also trigger this method to stop, while the next update will pick
     * up where this update left off. During the smoothing this method doesn't stop for an increased update time, as that will
     * leave a gap in the smoothed data. Note that smoothing either smoothes all data (when {@code redo = true}), or only the
     * last part that falls within the kernel.
     * @param t Time; time up to which to show data
     */
    @SuppressWarnings({ "synthetic-access", "methodlength" })
    private void update(final Time t)
    {
        Throw.when(this.plots.isEmpty(), IllegalStateException.class, "ContourDataPool is used, but not by a contour plot!");

        if (t.si < this.toTime.si)
        {
            // skip this update as new updates were commanded, while this update was in the queue, and a previous was running
            return;
        }

        /**
         * This method is executed once at a time by the worker Thread. Many properties, such as the data, are maintained by
         * this method. Other properties, which other methods can change, are read first in a synchronized block, while those
         * methods are also synchronized.
         */
        boolean redo0;
        boolean smooth0;
        boolean interpolate0;
        double timeGranularity;
        double spaceGranularity;
        double[] spaceTicks;
        double[] timeTicks;
        int fromSpaceIndex = 0;
        int fromTimeIndex = 0;
        int toTimeIndex;
        double tFromEgtf = 0;
        int nFromEgtf = 0;
        synchronized (this)
        {
            // save local copies so commands given during this execution can change it for the next execution
            redo0 = this.redo;
            smooth0 = this.smooth;
            interpolate0 = this.timeAxis.interpolate;
            // timeTicks may be longer than the simulation time, so we use the time bin for the required time of data
            if (this.desiredTimeGranularity != null)
            {
                this.timeAxis.setGranularity(this.desiredTimeGranularity);
                this.desiredTimeGranularity = null;
            }
            if (this.desiredSpaceGranularity != null)
            {
                this.spaceAxis.setGranularity(this.desiredSpaceGranularity);
                this.desiredSpaceGranularity = null;
            }
            timeGranularity = this.timeAxis.granularity;
            spaceGranularity = this.spaceAxis.granularity;
            spaceTicks = this.spaceAxis.getTicks();
            timeTicks = this.timeAxis.getTicks();
            if (!redo0)
            {
                // remember where we started, readyItems will be updated but we need to know where we started during the update
                fromSpaceIndex = getSpaceBin(this.readyItems + 1);
                fromTimeIndex = getTimeBin(this.readyItems + 1);
            }
            toTimeIndex = ((int) (t.si / timeGranularity)) - (interpolate0 ? 0 : 1);
            if (smooth0)
            {
                // time of current bin - kernel size, get bin of that time, get time (middle) of that bin
                tFromEgtf = this.timeAxis.getBinValue(redo0 ? 0 : this.timeAxis.getValueBin(
                        this.timeAxis.getBinValue(fromTimeIndex) - Math.max(TAU.si, timeGranularity / 2) * KERNEL_FACTOR));
                nFromEgtf = this.timeAxis.getValueBin(tFromEgtf);
            }
            // starting execution, so reset redo trigger which any next command may set to true if needed
            this.redo = false;
        }

        // reset upon a redo
        if (redo0)
        {
            this.readyItems = -1;

            // init all data arrays
            int nSpace = spaceTicks.length - 1;
            int nTime = timeTicks.length - 1;
            this.distance = new float[nSpace][nTime];
            this.time = new float[nSpace][nTime];
            for (ContourDataType<?> contourDataType : this.additionalData.keySet())
            {
                this.additionalData.put(contourDataType, new float[nSpace][nTime]);
            }

            // setup the smoothing filter
            if (smooth0)
            {
                // create the filter
                this.egtf = new EGTF(C_CONG, this.cFree, DELTA_V, this.vc);

                // create data source and its data streams for speed, distance traveled, time traveled, and additional
                DataSource generic = this.egtf.getDataSource("generic");
                generic.addSpeedStream(Quantity.SPEED, Speed.ZERO, Speed.ZERO);
                generic.addNonSpeedStream(this.travelTimeQuantity);
                generic.addNonSpeedStream(this.travelDistanceQuantity);
                this.speedStream = generic.getStream(Quantity.SPEED);
                this.travelTimeStream = generic.getStream(this.travelTimeQuantity);
                this.travelDistanceStream = generic.getStream(this.travelDistanceQuantity);
                for (ContourDataType<?> contourDataType : this.additionalData.keySet())
                {
                    this.additionalStreams.put(contourDataType, generic.addNonSpeedStream(contourDataType.getQuantity()));
                }

                // in principle we use sigma and tau, unless the data is so rough, we need more (granularity / 2).
                Duration tau2 = Duration.createSI(Math.max(TAU.si, timeGranularity / 2));
                Length sigma2 = Length.createSI(Math.max(SIGMA.si, spaceGranularity / 2));
                // for maximum space and time range, increase sigma and tau by KERNEL_FACTOR, beyond which both kernels diminish
                this.egtf.setKernel(tau2.multiplyBy(KERNEL_FACTOR), sigma2.multiplyBy(KERNEL_FACTOR), sigma2, tau2);

                // add listener to provide a filter status update and to possibly stop the filter when the plot is invalidated
                this.egtf.addListener(new EgtfListener()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void notifyProgress(final EgtfEvent event)
                    {
                        // check stop (explicit use of property, not locally stored value)
                        if (XContourDataPool.this.redo)
                        {
                            // plots need to be redone
                            event.interrupt(); // stop the EGTF
                            setStatusLabel(" "); // reset status label so no "ASM at 12.6%" remains there
                            return;
                        }
                        String status =
                                event.getProgress() >= 1.0 ? " " : String.format("ASM at %.2f%%", event.getProgress() * 100);
                        setStatusLabel(status);
                    }
                });
            }
        }

        // discard any data from smoothing if we are not smoothing
        if (!smooth0)
        {
            // free for garbage collector to remove the data
            this.egtf = null;
            this.speedStream = null;
            this.travelTimeStream = null;
            this.travelDistanceStream = null;
            this.additionalStreams.clear();
        }

        // ensure capacity
        for (int i = 0; i < this.distance.length; i++)
        {
            this.distance[i] = XPlotUtil.ensureCapacity(this.distance[i], toTimeIndex + 1);
            this.time[i] = XPlotUtil.ensureCapacity(this.time[i], toTimeIndex + 1);
            for (float[][] additional : this.additionalData.values())
            {
                additional[i] = XPlotUtil.ensureCapacity(additional[i], toTimeIndex + 1);
            }
        }

        // obtain trajectories
        // note we need a contour plot for getTotalLength() and getStartDistance(lane), as defined in AbstractSamplerPlot, and
        // hence not also defined in this data pool that has the same path
        XAbstractContourPlot<?> contourPlot = this.plots.iterator().next();
        List<TrajectoryGroup> trajectories = contourPlot.getTrajectories();

        // loop cells to update data
        for (int j = fromTimeIndex; j <= toTimeIndex; j++)
        {
            Time tFrom = Time.createSI(timeTicks[j]);
            Time tTo = Time.createSI(timeTicks[j + 1]);

            // we never filter time, time always spans the entire simulation, it will contain tFrom till tTo

            for (int i = fromSpaceIndex; i < spaceTicks.length - 1; i++)
            {
                // when interpolating, set the first row and column to NaN so colors representing 0 do not mess up the edges
                if ((j == 0 || i == 0) && interpolate0)
                {
                    this.distance[i][j] = Float.NaN;
                    this.time[i][j] = Float.NaN;
                    this.readyItems++;
                    continue;
                }

                // only first loop with offset, later in time, none of the space was done in the previous update
                fromSpaceIndex = 0;

                Length xFrom = Length.createSI(spaceTicks[i]);
                Length xTo = Length.createSI(Math.min(spaceTicks[i + 1], contourPlot.getEndLocation().si));

                // filter groups (lanes) that overlap with section i
                List<TrajectoryGroup> included = new ArrayList<>();
                List<Length> startDistances = new ArrayList<>();
                for (TrajectoryGroup trajectoryGroup : trajectories)
                {
                    KpiLaneDirection lane = trajectoryGroup.getLaneDirection();
                    Length startDistance = contourPlot.getStartDistance(lane);
                    if (startDistance.si + lane.getLaneData().getLength().si > spaceTicks[i]
                            && startDistance.si < spaceTicks[i + 1])
                    {
                        included.add(trajectoryGroup);
                        startDistances.add(startDistance);
                    }
                }

                // accumulate distance and time of trajectories
                double totalDistance = 0.0;
                double totalTime = 0.0;
                for (int k = 0; k < included.size(); k++)
                {
                    TrajectoryGroup trajectoryGroup = included.get(k);
                    Length startDistance = startDistances.get(k);
                    Length x0 = Length.max(xFrom.minus(startDistance), Length.ZERO);
                    Length x1 =
                            Length.min(xTo.minus(startDistance), trajectoryGroup.getLaneDirection().getLaneData().getLength());
                    for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                    {
                        // for optimal operations, we first do quick-reject based on time, as by far most trajectories during
                        // the entire time span of simulation will not apply to a particular cell in space-time
                        if (XPlotUtil.considerTrajectory(trajectory, tFrom, tTo))
                        {
                            // again for optimal operations, we use a space-time view only (we don't need more)
                            SpaceTimeView spaceTimeView = trajectory.getSpaceTimeView(x0, x1, tFrom, tTo);
                            totalDistance += spaceTimeView.getDistance().si;
                            totalTime += spaceTimeView.getTime().si;
                        }
                    }
                }
                // scale values to the full size of a cell, so the EGTF is interpolating comparable values
                double norm = spaceGranularity / (xTo.si - xFrom.si);
                totalDistance *= norm;
                totalTime *= norm;
                this.distance[i][j] = (float) totalDistance;
                this.time[i][j] = (float) totalTime;

                // loop and set any additional data
                for (ContourDataType<?> contourDataType : this.additionalData.keySet())
                {
                    this.additionalData.get(contourDataType)[i][j] =
                            (float) contourDataType.calculateValue(included, startDistances, xFrom, xTo, tFrom, tTo);
                }

                // add data to EGTF (yes it's a copy, but our local data will be overwritten with smoothed data later)
                if (smooth0)
                {
                    // center of cell
                    Length xDat = Length.createSI((xFrom.si + xTo.si) / 2.0);
                    Time tDat = Time.createSI((tFrom.si + tTo.si) / 2.0);
                    // speed data is implicit as totalDistance/totalTime, but the EGTF needs it explicitly
                    this.egtf.addPointData(this.speedStream, xDat, tDat, Speed.createSI(totalDistance / totalTime));
                    this.egtf.addPointData(this.travelDistanceStream, xDat, tDat, Length.createSI(totalDistance));
                    this.egtf.addPointData(this.travelTimeStream, xDat, tDat, Duration.createSI(totalTime));
                    for (ContourDataType<?> contourDataType : this.additionalStreams.keySet())
                    {
                        addAdditionalDataToFilter(contourDataType, xDat, tDat, this.additionalData.get(contourDataType)[i][j]);
                    }
                }

                // check stop (explicit use of properties, not locally stored values)
                if (this.redo)
                {
                    // plots need to be redone, or time has increased meaning that a next call may just as well continue further
                    return;
                }

                // one more item is ready for plotting
                this.readyItems++;
            }

            // notify changes for every time slice
            this.plots.forEach((plot) -> plot.notifyPlotChange());
        }

        // smooth all data that is as old as our kernel includes (or all data on a redo)
        if (smooth0)
        {
            int nTime = toTimeIndex - nFromEgtf + 1;
            int nSpace = spaceTicks.length - 1;
            double[] tFilt = new double[nTime];
            double[] xFilt = new double[nSpace];
            for (int i = 0; i < nTime; i++)
            {
                tFilt[i] = tFromEgtf + i * timeGranularity;
            }
            for (int j = 0; j < nSpace; j++)
            {
                xFilt[j] = (spaceTicks[j] + spaceTicks[j + 1]) / 2.0;
            }
            Set<Quantity<?, ?>> quantities = new LinkedHashSet<>();
            quantities.add(this.travelDistanceQuantity);
            quantities.add(this.travelTimeQuantity);
            for (ContourDataType<?> contourDataType : this.additionalData.keySet())
            {
                quantities.add(contourDataType.getQuantity());
            }
            Filter filter = this.egtf.filterSI(xFilt, tFilt, quantities.toArray(new Quantity<?, ?>[quantities.size()]));
            if (filter != null) // null if interrupted
            {
                overwriteSmoothed(this.distance, nFromEgtf, filter.getSI(this.travelDistanceQuantity));
                overwriteSmoothed(this.time, nFromEgtf, filter.getSI(this.travelTimeQuantity));
                for (ContourDataType<?> contourDataType : this.additionalData.keySet())
                {
                    overwriteSmoothed(this.additionalData.get(contourDataType), nFromEgtf,
                            filter.getSI(contourDataType.getQuantity()));
                }
                this.plots.forEach((plot) -> plot.notifyPlotChange());
            }
        }
    }

    /**
     * Helper method to deal with generics. It sets a value of some additional type in the smoothing filter.
     * @param contourDataType ContourDataType&lt;T&gt;; the type of data, e.g. acceleration, travel time delay, etc.
     * @param x Length; measurement location
     * @param t Time; measurement time
     * @param val Float; measured value
     * @param <T> type of data
     */
    @SuppressWarnings("unchecked") // type and data consistent through additionalStreams
    private <T extends Number> void addAdditionalDataToFilter(final ContourDataType<T> contourDataType, final Length x,
            final Time t, final Float val)
    {
        XContourDataPool.this.egtf.addPointData((DataStream<T>) XContourDataPool.this.additionalStreams.get(contourDataType), x,
                t, (T) val);
    }

    /**
     * Helper method to fill smoothed data in to raw data.
     * @param raw float[][]; the raw unsmoothed data
     * @param rawCol int; column from which onward to fill smoothed data in to the raw data which is used for plotting
     * @param smoothed double[][]; smoothed data returned by {@code EGTF}
     */
    private void overwriteSmoothed(final float[][] raw, final int rawCol, final double[][] smoothed)
    {
        for (int i = 0; i < raw.length; i++)
        {
            // can't use System.arraycopy due to float vs double
            for (int j = 0; j < smoothed[i].length; j++)
            {
                raw[i][j + rawCol] = (float) smoothed[i][j];
            }
        }
    }

    /**
     * Helper method used by an {@code EgtfListener} to present the filter progress.
     * @param status String; progress report
     */
    private void setStatusLabel(final String status)
    {
        for (XAbstractContourPlot<?> plot : XContourDataPool.this.plots)
        {
            plot.setStatusLabel(status);
        }
    }

    // ******************************
    // *** DATA RETRIEVAL METHODS ***
    // ******************************

    /**
     * Returns the speed of the cell pertaining to plot item.
     * @param item int; plot item
     * @return double; speed of the cell, calculated as 'total distance' / 'total space'.
     */
    public double getSpeed(final int item)
    {
        if (item > this.readyItems)
        {
            return Double.NaN;
        }
        return getTotalDistance(item) / getTotalTime(item);
    }

    /**
     * Returns the total distance traveled in the cell pertaining to plot item.
     * @param item int; plot item
     * @return double; total distance traveled in the cell
     */
    public double getTotalDistance(final int item)
    {
        if (item > this.readyItems)
        {
            return Double.NaN;
        }
        return this.distance[getSpaceBin(item)][getTimeBin(item)];
    }

    /**
     * Returns the total time traveled in the cell pertaining to plot item.
     * @param item int; plot item
     * @return double; total time traveled in the cell
     */
    public double getTotalTime(final int item)
    {
        if (item > this.readyItems)
        {
            return Double.NaN;
        }
        return this.time[getSpaceBin(item)][getTimeBin(item)];
    }

    /**
     * Returns data of the given {@code ContourDataType} for a specific item.
     * @param item int; plot item
     * @param contourDataType ContourDataType; contour data type
     * @return data of the given {@code ContourDataType} for a specific item
     */
    public double get(final int item, final ContourDataType<?> contourDataType)
    {
        if (item > this.readyItems)
        {
            return Double.NaN;
        }
        return this.additionalData.get(contourDataType)[getSpaceBin(item)][getTimeBin(item)];
    }

    /**
     * Returns the time bin number of the item.
     * @param item int; item number
     * @return int; time bin number of the item
     */
    private int getTimeBin(final int item)
    {
        return item / this.spaceAxis.getBinCount();
    }

    /**
     * Returns the space bin number of the item.
     * @param item int; item number
     * @return int; space bin number of the item
     */
    private int getSpaceBin(final int item)
    {
        return item % this.spaceAxis.getBinCount();
    }

    // **********************
    // *** HELPER CLASSES ***
    // **********************

    /**
     * Enum to refer to either the distance or time axis.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum Dimension
    {
        /** Distance axis. */
        DISTANCE
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            protected Axis getAxis(final XContourDataPool dataPool)
            {
                return dataPool.spaceAxis;
            }
        },

        /** Time axis. */
        TIME
        {
            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            protected Axis getAxis(final XContourDataPool dataPool)
            {
                return dataPool.timeAxis;
            }
        };

        /**
         * Returns the {@code Axis} object.
         * @param dataPool ContourDataPool; data pool
         * @return Axis; axis
         */
        protected abstract Axis getAxis(XContourDataPool dataPool);
    }

    /**
     * Class to store and determine axis information such as granularity, ticks, and range.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class Axis
    {
        /** Minimum value. */
        private final double minValue;

        /** Maximum value. */
        private double maxValue;

        /** Selected granularity. */
        private double granularity;

        /** Possible granularities. */
        private final double[] granularities;

        /** Whether the data pool is set to interpolate. */
        private boolean interpolate = true;

        /** Tick values. */
        private double[] ticks;

        /**
         * Constructor.
         * @param minValue double; minimum value
         * @param maxValue double; maximum value
         * @param granularity double; initial granularity
         * @param granularities double[]; possible granularities
         */
        Axis(final double minValue, final double maxValue, final double granularity, final double[] granularities)
        {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.granularity = granularity;
            this.granularities = granularities;
        }

        /**
         * Sets the maximum value.
         * @param maxValue double; maximum value
         */
        void setMaxValue(final double maxValue)
        {
            if (this.maxValue != maxValue)
            {
                this.maxValue = maxValue;
                this.ticks = null;
            }
        }

        /**
         * Sets the granularity.
         * @param granularity double; granularity
         */
        void setGranularity(final double granularity)
        {
            if (this.granularity != granularity)
            {
                this.granularity = granularity;
                this.ticks = null;
            }
        }

        /**
         * Returns the ticks, which are calculated if needed.
         * @return double[]; ticks
         */
        double[] getTicks()
        {
            if (this.ticks == null)
            {
                int n = getBinCount() + 1;
                this.ticks = new double[n];
                int di = this.interpolate ? 1 : 0;
                for (int i = 0; i < n; i++)
                {
                    if (i == n - 1)
                    {
                        this.ticks[i] = Math.min((i - di) * this.granularity, this.maxValue);
                    }
                    else
                    {
                        this.ticks[i] = (i - di) * this.granularity;
                    }
                }
            }
            return this.ticks;
        }

        /**
         * Calculates the number of bins.
         * @return int; number of bins
         */
        int getBinCount()
        {
            return (int) Math.ceil((this.maxValue - this.minValue) / this.granularity) + (this.interpolate ? 1 : 0);
        }

        /**
         * Calculates the center value of a bin.
         * @param bin int; bin number
         * @return double; center value of the bin
         */
        double getBinValue(final int bin)
        {
            return this.minValue + (0.5 + bin - (this.interpolate ? 1 : 0)) * this.granularity;
        }

        /**
         * Looks up the bin number of the value.
         * @param value double; value
         * @return int; bin number
         */
        int getValueBin(final double value)
        {
            getTicks();
            if (value > this.ticks[this.ticks.length - 1])
            {
                return this.ticks.length - 1;
            }
            int i = 0;
            while (i < this.ticks.length - 1 && this.ticks[i + 1] < value + 1e-9)
            {
                i++;
            }
            return i;
        }

        /**
         * Sets interpolation, important is it required the data to have an additional row or column.
         * @param interpolate boolean; interpolation
         */
        void setInterpolate(final boolean interpolate)
        {
            if (this.interpolate != interpolate)
            {
                this.interpolate = interpolate;
                this.ticks = null;
            }
        }

    }

    /**
     * Interface for data types of which a contour plot can be made. Using this class, the data pool can determine and store
     * cell values for a variable set of additional data types (besides total distance, total time and speed).
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <Z> value type
     */
    public interface ContourDataType<Z extends Number>
    {
        /**
         * Calculate value from provided trajectories that apply to a single grid cell. The start and end of the cell is given
         * in length on the path. For each {@code TrajectoryGroup} a start distance on the path is provided.
         * @param trajectories List&lt;TrajectoryGroup&gt;; trajectories, all groups overlap the requested space-time
         * @param startDistances List&lt;Length&gt;; start distances of each {@code TrajectoryGroup} on the path.
         * @param xFrom Length; start location of cell on the path (not on any particular {@code TrajectoryGroup}.
         * @param xTo Length; end location of cell on the path (not on any particular {@code TrajectoryGroup}.
         * @param tFrom Time; start time of cell
         * @param tTo Time; end time of cell
         * @return double; calculated value
         */
        double calculateValue(List<TrajectoryGroup> trajectories, List<Length> startDistances, Length xFrom, Length xTo,
                Time tFrom, Time tTo);

        /**
         * Returns the quantity that is being plotted on the z-axis for the EGTF filter.
         * @return Quantity&lt;Z, ?&gt;; quantity that is being plotted on the z-axis for the EGTF filter
         */
        Quantity<Z, ?> getQuantity();
    }

}
