package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.draw.egtf.Converter;
import org.opentrafficsim.draw.egtf.DataSource;
import org.opentrafficsim.draw.egtf.DataStream;
import org.opentrafficsim.draw.egtf.Egtf;
import org.opentrafficsim.draw.egtf.EgtfEvent;
import org.opentrafficsim.draw.egtf.EgtfListener;
import org.opentrafficsim.draw.egtf.Filter;
import org.opentrafficsim.draw.egtf.Quantity;
import org.opentrafficsim.draw.egtf.typed.TypedQuantity;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.SamplerData;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.Trajectory.SpaceTimeView;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Class that contains data for contour plots. One data source can be shared between contour plots, in which case the
 * granularity, path, sampler, update interval, and whether the data is smoothed (EGTF) are equal between the plots.
 * <p>
 * By default the source contains traveled time and traveled distance per cell.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ContourDataSource
{

    // *************************
    // *** GLOBAL PROPERTIES ***
    // *************************

    /** Space granularity values. */
    protected static final double[] DEFAULT_SPACE_GRANULARITIES = {10, 20, 50, 100, 200, 500, 1000};

    /** Index of the initial space granularity. */
    protected static final int DEFAULT_SPACE_GRANULARITY_INDEX = 3;

    /** Time granularity values. */
    protected static final double[] DEFAULT_TIME_GRANULARITIES = {1, 2, 5, 10, 20, 30, 60, 120, 300, 600};

    /** Index of the initial time granularity. */
    protected static final int DEFAULT_TIME_GRANULARITY_INDEX = 3;

    /** Initial lower bound for the time scale. */
    protected static final Time DEFAULT_LOWER_TIME_BOUND = Time.ZERO;

    /**
     * Total kernel size relative to sigma and tau. This factor is determined through -log(1 - p) with p ~= 99%. This means that
     * the cumulative exponential distribution has 99% at 5 times sigma or tau. Note that due to a coordinate change in the
     * Adaptive Smoothing Method, the actual cumulative distribution is slightly different. Hence, this is just a heuristic.
     */
    private static final int KERNEL_FACTOR = 5;

    /** Spatial kernel size. Larger value may be used when using a large granularity. */
    private static final Length SIGMA = Length.instantiateSI(300);

    /** Temporal kernel size. Larger value may be used when using a large granularity. */
    private static final Duration TAU = Duration.instantiateSI(30);

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

    /** Sampler data. */
    private final SamplerData<?> samplerData;

    /** Update interval. */
    private final Duration updateInterval;

    /** Delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories. */
    private final Duration delay;

    /** Path. */
    private final GraphPath<? extends LaneData<?>> path;

    /** Space axis. */
    final Axis spaceAxis;

    /** Time axis. */
    final Axis timeAxis;

    /** Registered plots. */
    private Set<AbstractContourPlot<?>> plots = new LinkedHashSet<>();

    // *****************
    // *** PLOT DATA ***
    // *****************

    /** Total distance traveled per cell. */
    private float[][] distance;

    /** Total time traveled per cell. */
    private float[][] time;

    /** Data of other types. */
    private final Map<ContourDataType<?, ?>, float[][]> additionalData = new LinkedHashMap<>();

    // ****************************
    // *** SMOOTHING PROPERTIES ***
    // ****************************

    /** Free flow propagation speed. */
    private Speed cFree;

    /** Flip-over speed between congestion and free flow. */
    private Speed vc;

    /** Smoothing filter. */
    private Egtf egtf;

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
    private Map<ContourDataType<?, ?>, DataStream<?>> additionalStreams = new LinkedHashMap<>();

    // *****************************
    // *** CONTINUITY PROPERTIES ***
    // *****************************

    /** Updater for update times. */
    private final GraphUpdater<Time> graphUpdater;

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

    /** Whether to smooth data. */
    private boolean smooth = false;

    // ********************
    // *** CONSTRUCTORS ***
    // ********************

    /**
     * Constructor using default granularities.
     * @param samplerData SamplerData&lt;?&gt;; sampler data
     * @param path GraphPath&lt;? extends LaneData&gt;; path
     */
    public ContourDataSource(final SamplerData<?> samplerData, final GraphPath<? extends LaneData<?>> path)
    {
        this(samplerData, Duration.instantiateSI(1.0), path, DEFAULT_SPACE_GRANULARITIES, DEFAULT_SPACE_GRANULARITY_INDEX,
                DEFAULT_TIME_GRANULARITIES, DEFAULT_TIME_GRANULARITY_INDEX, DEFAULT_LOWER_TIME_BOUND,
                AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND);
    }

    /**
     * Constructor for non-default input.
     * @param samplerData SamplerData&lt;?&gt;; sampler data
     * @param delay Duration; delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     * @param path GraphPath&lt;? extends LaneData&gt;; path
     * @param spaceGranularity double[]; granularity options for space dimension
     * @param initSpaceIndex int; initial selected space granularity
     * @param timeGranularity double[]; granularity options for time dimension
     * @param initTimeIndex int; initial selected time granularity
     * @param start Time; start time
     * @param initialEnd Time; initial end time of plots, will be expanded if simulation time exceeds it
     */
    @SuppressWarnings("parameternumber")
    public ContourDataSource(final SamplerData<?> samplerData, final Duration delay, final GraphPath<? extends LaneData<?>> path,
            final double[] spaceGranularity, final int initSpaceIndex, final double[] timeGranularity, final int initTimeIndex,
            final Time start, final Time initialEnd)
    {
        this.samplerData = samplerData;
        this.updateInterval = Duration.instantiateSI(timeGranularity[initTimeIndex]);
        this.delay = delay;
        this.path = path;
        this.spaceAxis = new Axis(0.0, path.getTotalLength().si, spaceGranularity[initSpaceIndex], spaceGranularity);
        this.timeAxis = new Axis(start.si, initialEnd.si, timeGranularity[initTimeIndex], timeGranularity);

        // get length-weighted mean speed limit from path to determine cFree and Vc for smoothing
        this.cFree = Speed.min(path.getSpeedLimit(), MAX_C_FREE);
        this.vc = Speed.min(path.getSpeedLimit().times(VC_FACRTOR), MAX_C_FREE);

        // setup updater to do the actual work in another thread
        this.graphUpdater = new GraphUpdater<>("Contour Data Source worker", Thread.currentThread(), (t) -> update(t));
    }

    // ************************************
    // *** PLOT INTERFACING AND GETTERS ***
    // ************************************

    /**
     * Returns the sampler data for an {@code AbstractContourPlot} using this {@code ContourDataSource}.
     * @return SamplerData&lt;?&gt;; the sampler
     */
    public final SamplerData<?> getSamplerData()
    {
        return this.samplerData;
    }

    /**
     * Returns the update interval for an {@code AbstractContourPlot} using this {@code ContourDataSource}.
     * @return Duration; update interval
     */
    final Duration getUpdateInterval()
    {
        return this.updateInterval;
    }

    /**
     * Returns the delay for an {@code AbstractContourPlot} using this {@code ContourDataSource}.
     * @return Duration; delay
     */
    final Duration getDelay()
    {
        return this.delay;
    }

    /**
     * Returns the path for an {@code AbstractContourPlot} using this {@code ContourDataSource}.
     * @return GraphPath&lt;? extends LaneData&gt;; the path
     */
    final GraphPath<? extends LaneData<?>> getPath()
    {
        return this.path;
    }

    /**
     * Register a contour plot to this data pool. The contour constructor will do this.
     * @param contourPlot AbstractContourPlot&lt;?&gt;; contour plot
     */
    final void registerContourPlot(final AbstractContourPlot<?> contourPlot)
    {
        ContourDataType<?, ?> contourDataType = contourPlot.getContourDataType();
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
    public final double[] getGranularities(final Dimension dimension)
    {
        return dimension.getAxis(this).granularities;
    }

    /**
     * Returns the selected granularity that a linked plot should use.
     * @param dimension Dimension; space or time
     * @return double; granularity that a linked plot should use
     */
    @SuppressWarnings("synthetic-access")
    public final double getGranularity(final Dimension dimension)
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
            for (AbstractContourPlot<?> plot : this.plots)
            {
                plot.setUpperDomainBound(updateTime.si);
            }
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
    public final synchronized void setGranularity(final Dimension dimension, final double granularity)
    {
        if (dimension.equals(Dimension.DISTANCE))
        {
            this.desiredSpaceGranularity = granularity;
            for (AbstractContourPlot<?> contourPlot : ContourDataSource.this.plots)
            {
                contourPlot.setSpaceGranularity(granularity);
            }
        }
        else
        {
            this.desiredTimeGranularity = granularity;
            for (AbstractContourPlot<?> contourPlot : ContourDataSource.this.plots)
            {
                contourPlot.setUpdateInterval(Duration.instantiateSI(granularity));
                contourPlot.setTimeGranularity(granularity);
            }
        }
        invalidate(null);
    }

    /**
     * Sets bi-linear interpolation enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param interpolate boolean; whether to enable interpolation
     */
    @SuppressWarnings("synthetic-access")
    public final void setInterpolate(final boolean interpolate)
    {
        if (this.timeAxis.interpolate != interpolate)
        {
            synchronized (this)
            {
                this.timeAxis.setInterpolate(interpolate);
                this.spaceAxis.setInterpolate(interpolate);
                for (AbstractContourPlot<?> contourPlot : ContourDataSource.this.plots)
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
    public final void setSmooth(final boolean smooth)
    {
        if (this.smooth != smooth)
        {
            synchronized (this)
            {
                this.smooth = smooth;
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
    @SuppressWarnings({"synthetic-access", "methodlength"})
    private void update(final Time t)
    {
        Throw.when(this.plots.isEmpty(), IllegalStateException.class, "ContourDataSource is used, but not by a contour plot!");

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
            for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
            {
                this.additionalData.put(contourDataType, new float[nSpace][nTime]);
            }

            // setup the smoothing filter
            if (smooth0)
            {
                // create the filter
                this.egtf = new Egtf(C_CONG.si, this.cFree.si, DELTA_V.si, this.vc.si);

                // create data source and its data streams for speed, distance traveled, time traveled, and additional
                DataSource generic = this.egtf.getDataSource("generic");
                generic.addStream(TypedQuantity.SPEED, Speed.instantiateSI(1.0), Speed.instantiateSI(1.0));
                generic.addStreamSI(this.travelTimeQuantity, 1.0, 1.0);
                generic.addStreamSI(this.travelDistanceQuantity, 1.0, 1.0);
                this.speedStream = generic.getStream(TypedQuantity.SPEED);
                this.travelTimeStream = generic.getStream(this.travelTimeQuantity);
                this.travelDistanceStream = generic.getStream(this.travelDistanceQuantity);
                for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
                {
                    this.additionalStreams.put(contourDataType, generic.addStreamSI(contourDataType.getQuantity(), 1.0, 1.0));
                }

                // in principle we use sigma and tau, unless the data is so rough, we need more (granularity / 2).
                double tau2 = Math.max(TAU.si, timeGranularity / 2);
                double sigma2 = Math.max(SIGMA.si, spaceGranularity / 2);
                // for maximum space and time range, increase sigma and tau by KERNEL_FACTOR, beyond which both kernels diminish
                this.egtf.setGaussKernelSI(sigma2 * KERNEL_FACTOR, tau2 * KERNEL_FACTOR, sigma2, tau2);

                // add listener to provide a filter status update and to possibly stop the filter when the plot is invalidated
                this.egtf.addListener(new EgtfListener()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void notifyProgress(final EgtfEvent event)
                    {
                        // check stop (explicit use of property, not locally stored value)
                        if (ContourDataSource.this.redo)
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
            this.distance[i] = GraphUtil.ensureCapacity(this.distance[i], toTimeIndex + 1);
            this.time[i] = GraphUtil.ensureCapacity(this.time[i], toTimeIndex + 1);
            for (float[][] additional : this.additionalData.values())
            {
                additional[i] = GraphUtil.ensureCapacity(additional[i], toTimeIndex + 1);
            }
        }

        // loop cells to update data
        for (int j = fromTimeIndex; j <= toTimeIndex; j++)
        {
            Time tFrom = Time.instantiateSI(timeTicks[j]);
            Time tTo = Time.instantiateSI(timeTicks[j + 1]);

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
                Length xFrom = Length.instantiateSI(spaceTicks[i]);
                Length xTo = Length.instantiateSI(Math.min(spaceTicks[i + 1], this.path.getTotalLength().si));

                // init cell data
                double totalDistance = 0.0;
                double totalTime = 0.0;
                Map<ContourDataType<?, ?>, Object> additionalIntermediate = new LinkedHashMap<>();
                for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
                {
                    additionalIntermediate.put(contourDataType, contourDataType.identity());
                }

                // aggregate series in cell
                for (int series = 0; series < this.path.getNumberOfSeries(); series++)
                {
                    // obtain trajectories
                    List<TrajectoryGroup<?>> trajectories = new ArrayList<>();
                    for (Section<? extends LaneData<?>> section : getPath().getSections())
                    {
                        TrajectoryGroup<?> trajectoryGroup = this.samplerData.getTrajectoryGroup(section.getSource(series));
                        if (null == trajectoryGroup)
                        {
                            CategoryLogger.always().error("trajectoryGroup {} is null", series);
                        }
                        trajectories.add(trajectoryGroup);
                    }

                    // filter groups (lanes) that overlap with section i
                    List<TrajectoryGroup<?>> included = new ArrayList<>();
                    List<Length> xStart = new ArrayList<>();
                    List<Length> xEnd = new ArrayList<>();
                    for (int k = 0; k < trajectories.size(); k++)
                    {
                        TrajectoryGroup<?> trajectoryGroup = trajectories.get(k);
                        LaneData<?> lane = trajectoryGroup.getLane();
                        Length startDistance = this.path.getStartDistance(this.path.get(k));
                        if (startDistance.si + this.path.get(k).getLength().si > spaceTicks[i]
                                && startDistance.si < spaceTicks[i + 1])
                        {
                            included.add(trajectoryGroup);
                            double scale = this.path.get(k).getLength().si / lane.getLength().si;
                            // divide by scale, so we go from base length to section length
                            xStart.add(Length.max(xFrom.minus(startDistance).divide(scale), Length.ZERO));
                            xEnd.add(Length.min(xTo.minus(startDistance).divide(scale), trajectoryGroup.getLane().getLength()));
                        }
                    }

                    // accumulate distance and time of trajectories
                    for (int k = 0; k < included.size(); k++)
                    {
                        TrajectoryGroup<?> trajectoryGroup = included.get(k);
                        for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                        {
                            // for optimal operations, we first do quick-reject based on time, as by far most trajectories
                            // during the entire time span of simulation will not apply to a particular cell in space-time
                            if (GraphUtil.considerTrajectory(trajectory, tFrom, tTo))
                            {
                                // again for optimal operations, we use a space-time view only (we don't need more)
                                SpaceTimeView spaceTimeView;
                                try
                                {
                                    spaceTimeView = trajectory.getSpaceTimeView(xStart.get(k), xEnd.get(k), tFrom, tTo);
                                }
                                catch (IllegalArgumentException exception)
                                {
                                    CategoryLogger.always().debug(exception,
                                            "Unable to generate space-time view from x = {} to {} and t = {} to {}.",
                                            xStart.get(k), xEnd.get(k), tFrom, tTo);
                                    continue;
                                }
                                totalDistance += spaceTimeView.getDistance().si;
                                totalTime += spaceTimeView.getTime().si;
                            }
                        }
                    }

                    // loop and set any additional data
                    for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
                    {
                        addAdditional(additionalIntermediate, contourDataType, included, xStart, xEnd, tFrom, tTo);
                    }

                }

                // scale values to the full size of a cell on a single lane, so the EGTF is interpolating comparable values
                double norm = spaceGranularity / (xTo.si - xFrom.si) / this.path.getNumberOfSeries();
                totalDistance *= norm;
                totalTime *= norm;
                this.distance[i][j] = (float) totalDistance;
                this.time[i][j] = (float) totalTime;
                for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
                {
                    this.additionalData.get(contourDataType)[i][j] =
                            finalizeAdditional(additionalIntermediate, contourDataType);
                }

                // add data to EGTF (yes it's a copy, but our local data will be overwritten with smoothed data later)
                if (smooth0)
                {
                    // center of cell
                    double xDat = (xFrom.si + xTo.si) / 2.0;
                    double tDat = (tFrom.si + tTo.si) / 2.0;
                    // speed data is implicit as totalDistance/totalTime, but the EGTF needs it explicitly
                    this.egtf.addPointDataSI(this.speedStream, xDat, tDat, totalDistance / totalTime);
                    this.egtf.addPointDataSI(this.travelDistanceStream, xDat, tDat, totalDistance);
                    this.egtf.addPointDataSI(this.travelTimeStream, xDat, tDat, totalTime);
                    for (ContourDataType<?, ?> contourDataType : this.additionalStreams.keySet())
                    {
                        ContourDataSource.this.egtf.addPointDataSI(
                                ContourDataSource.this.additionalStreams.get(contourDataType), xDat, tDat,
                                this.additionalData.get(contourDataType)[i][j]);
                    }
                }

                // check stop (explicit use of properties, not locally stored values)
                if (this.redo)
                {
                    // plots need to be redone, or time has increased meaning that a next call may continue further just as well
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
            Set<Quantity<?, ?>> quantities = new LinkedHashSet<>();
            quantities.add(this.travelDistanceQuantity);
            quantities.add(this.travelTimeQuantity);
            for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
            {
                quantities.add(contourDataType.getQuantity());
            }
            Filter filter = this.egtf.filterFastSI(spaceTicks[0] + 0.5 * spaceGranularity, spaceGranularity,
                    spaceTicks[0] + (-1.5 + spaceTicks.length) * spaceGranularity, tFromEgtf, timeGranularity, t.si,
                    quantities.toArray(new Quantity<?, ?>[quantities.size()]));
            if (filter != null) // null if interrupted
            {
                overwriteSmoothed(this.distance, nFromEgtf, filter.getSI(this.travelDistanceQuantity));
                overwriteSmoothed(this.time, nFromEgtf, filter.getSI(this.travelTimeQuantity));
                for (ContourDataType<?, ?> contourDataType : this.additionalData.keySet())
                {
                    overwriteSmoothed(this.additionalData.get(contourDataType), nFromEgtf,
                            filter.getSI(contourDataType.getQuantity()));
                }
                this.plots.forEach((plot) -> plot.notifyPlotChange());
            }
        }
    }

    /**
     * Add additional data to stored intermediate result.
     * @param additionalIntermediate Map&lt;ContourDataType&lt;?, ?&gt;, Object&gt;; intermediate storage map
     * @param contourDataType ContourDataType&lt;?, ?&gt;; additional data type
     * @param included List&lt;TrajectoryGroup&lt;?&gt;&gt;; trajectories
     * @param xStart List&lt;Length&gt;; start distance per trajectory group
     * @param xEnd List&lt;Length&gt;; end distance per trajectory group
     * @param tFrom Time; start time
     * @param tTo Time; end time
     * @param <I> intermediate data type
     */
    @SuppressWarnings("unchecked")
    private <I> void addAdditional(final Map<ContourDataType<?, ?>, Object> additionalIntermediate,
            final ContourDataType<?, ?> contourDataType, final List<TrajectoryGroup<?>> included, final List<Length> xStart,
            final List<Length> xEnd, final Time tFrom, final Time tTo)
    {
        additionalIntermediate.put(contourDataType, ((ContourDataType<?, I>) contourDataType)
                .processSeries((I) additionalIntermediate.get(contourDataType), included, xStart, xEnd, tFrom, tTo));
    }

    /**
     * Stores a finalized result for additional data.
     * @param additionalIntermediate Map&lt;ContourDataType&lt;?, ?&gt;, Object&gt;; intermediate storage map
     * @param contourDataType ContourDataType&lt;?, ?&gt;; additional data type
     * @return float; finalized results for a cell
     * @param <I> intermediate data type
     */
    @SuppressWarnings("unchecked")
    private <I> float finalizeAdditional(final Map<ContourDataType<?, ?>, Object> additionalIntermediate,
            final ContourDataType<?, ?> contourDataType)
    {
        return ((ContourDataType<?, I>) contourDataType).finalize((I) additionalIntermediate.get(contourDataType)).floatValue();
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
        for (AbstractContourPlot<?> plot : ContourDataSource.this.plots)
        {
            // TODO what shall we do this this? plot.setStatusLabel(status);
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
     * @param contourDataType ContourDataType&lt;?, ?&gt;; contour data type
     * @return data of the given {@code ContourDataType} for a specific item
     */
    public double get(final int item, final ContourDataType<?, ?> contourDataType)
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
        Throw.when(item < 0 || item >= this.spaceAxis.getBinCount() * this.timeAxis.getBinCount(),
                IndexOutOfBoundsException.class, "Item out of range");
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
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum Dimension
    {
        /** Distance axis. */
        DISTANCE
        {
            /** {@inheritDoc} */
            @Override
            protected Axis getAxis(final ContourDataSource dataPool)
            {
                return dataPool.spaceAxis;
            }
        },

        /** Time axis. */
        TIME
        {
            /** {@inheritDoc} */
            @Override
            protected Axis getAxis(final ContourDataSource dataPool)
            {
                return dataPool.timeAxis;
            }
        };

        /**
         * Returns the {@code Axis} object.
         * @param dataPool ContourDataSource; data pool
         * @return Axis; axis
         */
        protected abstract Axis getAxis(ContourDataSource dataPool);
    }

    /**
     * Class to store and determine axis information such as granularity, ticks, and range.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    static class Axis
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

        /**
         * Retrieve the interpolate flag.
         * @return boolean; true if interpolation is on; false if interpolation is off
         */
        public boolean isInterpolate()
        {
            return this.interpolate;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "Axis [minValue=" + this.minValue + ", maxValue=" + this.maxValue + ", granularity=" + this.granularity
                    + ", granularities=" + Arrays.toString(this.granularities) + ", interpolate=" + this.interpolate
                    + ", ticks=" + Arrays.toString(this.ticks) + "]";
        }

    }

    /**
     * Interface for data types of which a contour plot can be made. Using this class, the data pool can determine and store
     * cell values for a variable set of additional data types (besides total distance, total time and speed).
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <Z> value type
     * @param <I> intermediate data type
     */
    public interface ContourDataType<Z extends Number, I>
    {
        /**
         * Returns the initial value for intermediate result.
         * @return I, initial intermediate value
         */
        I identity();

        /**
         * Calculate value from provided trajectories that apply to a single grid cell on a single series (lane).
         * @param intermediate I; intermediate value of previous series, starts as the identity
         * @param trajectories List&lt;TrajectoryGroup&lt;?&gt;&gt;; trajectories, all groups overlap the requested space-time
         * @param xFrom List&lt;Length&gt;; start location of cell on the section
         * @param xTo List&lt;Length&gt;; end location of cell on the section.
         * @param tFrom Time; start time of cell
         * @param tTo Time; end time of cell
         * @return I; intermediate value
         */
        I processSeries(I intermediate, List<TrajectoryGroup<?>> trajectories, List<Length> xFrom, List<Length> xTo, Time tFrom,
                Time tTo);

        /**
         * Returns the final value of the intermediate result after all lanes.
         * @param intermediate I; intermediate result after all lanes
         * @return Z; final value
         */
        Z finalize(I intermediate);

        /**
         * Returns the quantity that is being plotted on the z-axis for the EGTF filter.
         * @return Quantity&lt;Z, ?&gt;; quantity that is being plotted on the z-axis for the EGTF filter
         */
        Quantity<Z, ?> getQuantity();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContourDataSource [samplerData=" + this.samplerData + ", updateInterval=" + this.updateInterval + ", delay="
                + this.delay + ", path=" + this.path + ", spaceAxis=" + this.spaceAxis + ", timeAxis=" + this.timeAxis
                + ", plots=" + this.plots + ", distance=" + Arrays.toString(this.distance) + ", time="
                + Arrays.toString(this.time) + ", additionalData=" + this.additionalData + ", smooth=" + this.smooth
                + ", cFree=" + this.cFree + ", vc=" + this.vc + ", egtf=" + this.egtf + ", speedStream=" + this.speedStream
                + ", travelTimeStream=" + this.travelTimeStream + ", travelDistanceStream=" + this.travelDistanceStream
                + ", travelTimeQuantity=" + this.travelTimeQuantity + ", travelDistanceQuantity=" + this.travelDistanceQuantity
                + ", additionalStreams=" + this.additionalStreams + ", graphUpdater=" + this.graphUpdater + ", redo="
                + this.redo + ", toTime=" + this.toTime + ", readyItems=" + this.readyItems + ", desiredSpaceGranularity="
                + this.desiredSpaceGranularity + ", desiredTimeGranularity=" + this.desiredTimeGranularity + "]";
    }

}
