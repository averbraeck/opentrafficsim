package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.math.means.ArithmeticMean;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.draw.egtf.Converter;
import org.opentrafficsim.draw.egtf.DataSource;
import org.opentrafficsim.draw.egtf.DataStream;
import org.opentrafficsim.draw.egtf.Egtf;
import org.opentrafficsim.draw.egtf.Filter;
import org.opentrafficsim.draw.egtf.Quantity;
import org.opentrafficsim.draw.egtf.typed.TypedQuantity;
import org.opentrafficsim.draw.graphs.AbstractContourPlot.ContourPaintState;
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
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ContourDataSource extends PlotDelegate<ContourPaintState, AbstractContourPlot<?>>
{

    // *******************
    // *** EVENT TYPES ***
    // *******************

    /** Granularity changed. */
    public static final EventType GRANULARITY = new EventType("GRANULARITY",
            new MetaData("Granularity", "Granularity changed.", new ObjectDescriptor("Axis", "Axis", Dimension.class),
                    new ObjectDescriptor("Granularity", "Granularity", Double.class)));

    /** Interpolation changed. */
    public static final EventType INTERPOLATE = new EventType("INTERPOLATE", new MetaData("Interpolate", "Interpolate changed.",
            new ObjectDescriptor("Interpolate", "Interpolate", Boolean.class)));

    /** Smooth changed. */
    public static final EventType SMOOTH = new EventType("SMOOTH",
            new MetaData("Smooth", "Smooth changed.", new ObjectDescriptor("Smooth", "Smooth", Boolean.class)));

    // *************************
    // *** GLOBAL PROPERTIES ***
    // *************************

    /** Space granularities. */
    protected static final PlotSetting<Length> SPACE_GRANULARITIES =
            PlotSetting.of(new double[] {10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0}, Length::ofSI, 3);

    /** Time granularities. */
    protected static final PlotSetting<Duration> TIME_GRANULARITIES =
            PlotSetting.of(new double[] {1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0, 600.0}, Duration::ofSI, 3);

    /** Initial lower bound for the time scale. */
    protected static final Duration DEFAULT_LOWER_TIME_BOUND = Duration.ZERO;

    /**
     * Total kernel size relative to sigma and tau. This factor is determined through -log(1 - p) with p ~= 99%. This means that
     * the cumulative exponential distribution has 99% at 5 times sigma or tau. Note that due to a coordinate change in the
     * Adaptive Smoothing Method, the actual cumulative distribution is slightly different. Hence, this is just a heuristic.
     */
    private static final int KERNEL_FACTOR = 5;

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

    /** Path. */
    private final GraphPath<? extends LaneData<?>> path;

    /** Space axis. */
    private final Axis spaceAxis;

    /** Time axis. */
    private final Axis timeAxis;

    /** Data types. */
    private final Set<ContourDataType<?>> dataTypes = new LinkedHashSet<>();

    // *****************
    // *** PLOT DATA ***
    // *****************

    /** Total distance traveled per cell. */
    private float[][] distance;

    /** Total time traveled per cell. */
    private float[][] time;

    /** Data of other types. */
    private final Map<ContourAdditionalDataType<?, ?>, float[][]> additionalData = new LinkedHashMap<>();

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
    private Map<ContourAdditionalDataType<?, ?>, DataStream<?>> additionalStreams = new LinkedHashMap<>();

    // *****************************
    // *** CONTINUITY PROPERTIES ***
    // *****************************

    /** Time up to which to determine data. This is a multiple of the update interval, which is now, or recent on a redo. */
    private double toTime = 0.0;

    /** Number of items that are ready. To return NaN values if not ready, and for operations between consecutive updates. */
    private int readyItems = -1;

    /** Whether to smooth data. */
    private boolean smooth = false;

    // ********************
    // *** CONSTRUCTORS ***
    // ********************

    /**
     * Constructor using default granularities.
     * @param samplerData sampler data
     * @param path path
     * @param plotScheduler plot scheduler
     */
    public ContourDataSource(final SamplerData<?> samplerData, final GraphPath<? extends LaneData<?>> path,
            final PlotScheduler plotScheduler)
    {
        this(samplerData, Duration.ofSI(1.0), path, plotScheduler, SPACE_GRANULARITIES, TIME_GRANULARITIES,
                DEFAULT_LOWER_TIME_BOUND, AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND);
    }

    /**
     * Constructor for non-default input.
     * @param samplerData sampler data
     * @param delay delay so critical future events have occurred, e.g. GTU's next move's to extend trajectories
     * @param path path
     * @param plotScheduler plot scheduler
     * @param spaceGranularities granularity options for space dimension
     * @param timeGranularities granularity options for time dimension
     * @param start start time
     * @param initialEnd initial end time of plots, will be expanded if simulation time exceeds it
     */
    @SuppressWarnings("parameternumber")
    public ContourDataSource(final SamplerData<?> samplerData, final Duration delay,
            final GraphPath<? extends LaneData<?>> path, final PlotScheduler plotScheduler,
            final PlotSetting<Length> spaceGranularities, final PlotSetting<Duration> timeGranularities, final Duration start,
            final Duration initialEnd)
    {
        super(Duration.ofSI(timeGranularities.defaultValueIndex()), delay, plotScheduler);
        this.samplerData = samplerData;
        this.path = path;
        this.spaceAxis = new Axis(0.0, path.getTotalLength().si, spaceGranularities.getDefaultValue().si,
                spaceGranularities.values().stream().mapToDouble((len) -> len.si).toArray());
        this.timeAxis = new Axis(start.si, initialEnd.si, timeGranularities.getDefaultValue().si,
                timeGranularities.values().stream().mapToDouble((len) -> len.si).toArray());

        // get length-weighted mean speed limit from path to determine cFree and Vc for smoothing
        this.cFree = Speed.min(path.getSpeedLimit(), MAX_C_FREE);
        this.vc = Speed.min(path.getSpeedLimit().times(VC_FACRTOR), MAX_C_FREE);
    }

    @Override
    public String toString()
    {
        return "ContourDataSource []";
    }

    // ************************************
    // *** PLOT INTERFACING AND GETTERS ***
    // ************************************

    /**
     * Returns the path for an {@link AbstractContourPlot} using this {@link ContourDataSource}.
     * @return the path
     */
    GraphPath<? extends LaneData<?>> getPath()
    {
        return this.path;
    }

    @Override
    public void addPlot(final AbstractContourPlot<?> contourPlot)
    {
        ContourDataType<?> contourDataType = contourPlot.getContourDataType();
        if (contourDataType instanceof ContourAdditionalDataType<?, ?> type)
        {
            this.additionalData.put(type, null);
        }
        this.dataTypes.add(contourDataType);
        super.addPlot(contourPlot);
    }

    /**
     * Returns the available granularities that a linked plot may use.
     * @param dimension space or time
     * @return available granularities that a linked plot may use
     */
    public double[] getGranularities(final Dimension dimension)
    {
        return dimension.getAxis(this).granularities;
    }

    /**
     * Returns the selected granularity that a linked plot should use.
     * @param dimension space or time
     * @return granularity that a linked plot should use
     */
    public double getGranularity(final Dimension dimension)
    {
        return dimension.getAxis(this).granularity;
    }

    /**
     * Sets the granularity of the plot. This will invalidate the plot triggering a redraw.
     * @param dimension space or time
     * @param granularity granularity in space or time (SI unit)
     */
    public void setGranularity(final Dimension dimension, final double granularity)
    {
        synchronized (this)
        {
            dimension.getAxis(this).setGranularity(granularity);
            if (dimension.equals(Dimension.TIME))
            {
                getPlots().forEach((p) -> p.offerUpdateInterval(Duration.ofSI(granularity)));
            }
            invalidateTimeSpan();
        }
        fireEvent(GRANULARITY, new Object[] {dimension, granularity});
    }

    /**
     * Sets bi-linear interpolation enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param interpolate whether to enable interpolation
     */
    public void setInterpolate(final boolean interpolate)
    {
        boolean did = false;
        synchronized (this)
        {
            if (this.timeAxis.interpolate != interpolate)
            {
                did = true;
                this.timeAxis.setInterpolate(interpolate);
                this.spaceAxis.setInterpolate(interpolate);
            }
            invalidateTimeSpan();
        }
        if (did)
        {
            fireEvent(INTERPOLATE, interpolate);
        }
    }

    /**
     * Sets the adaptive smoothing enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param smooth whether to smooth the plot
     */
    public void setSmooth(final boolean smooth)
    {
        boolean did = false;
        synchronized (this)
        {
            if (this.smooth != smooth)
            {
                did = true;
                this.smooth = smooth;
            }
            invalidateTimeSpan();
        }
        if (did)
        {
            fireEvent(SMOOTH, smooth);
        }
    }

    // ************************
    // *** UPDATING METHODS ***
    // ************************

    /**
     * Heart of the data pool. This method is invoked regularly by the worker thread of a plot, either for a scheduled update or
     * due to user input. No two invocations can happen at the same time.
     * <p>
     * This method regularly checks conditions that indicate the update should be interrupted as for example a setting has
     * changed and repainting is required. Whenever a new invalidation causes {@link #invalidateTimeSpan} to be invoked, this
     * method can stop as the full data needs to be recalculated. This can be set by any change of e.g. granularity or
     * smoothing, during the update.
     * @param t time up to which to show data
     */
    @Override
    protected void calculatePaintStateUnsafe(final Duration t)
    {
        Throw.when(getPlots().isEmpty(), IllegalStateException.class, "ContourDataSource is used, but not by a contour plot!");

        // Get consistent update context
        UpdateContext uc = snapshotAndPrepare(t);

        // Reset data arrays and clear filter upon a redo
        resetDataOnRedo(uc);

        // Setup or clear filter data
        configureFilter(uc);

        // Ensure capacity
        ensureTimeCapacity(uc.toTimeIndex());

        // Process the data
        for (int j = uc.fromTimeIndex(); j <= uc.toTimeIndex(); j++)
        {
            if (!processTimeSlice(j, uc))
            {
                return; // early stop requested
            }
        }

        // Smoothing filter
        applySmoothingIfNeeded(uc);
    }

    /**
     * Takes a snapshot of the update context with which the worker thread can run. Then other threads can set properties while
     * the worker is running with a consistent context.
     * @param now current time
     * @return update context
     */
    private UpdateContext snapshotAndPrepare(final Duration now)
    {
        /**
         * This method is executed once at a time by a plot worker thread. Many properties, such as the data, are maintained by
         * this method. Other properties, which other methods can change, are read first in a synchronized block, while those
         * methods are also synchronized.
         */
        boolean redo;
        double timeGranularity;
        double spaceGranularity;
        boolean smooth0;
        boolean interpolate0;
        double timeKernelSize;
        double spaceKernelSize;
        double[] spaceTicks;
        double[] timeTicks;
        int fromSpaceIndex = 0;
        int fromTimeIndex = 0;
        int toTimeIndex;
        double tFromEgtf = 0;
        int skipTime = 0;
        int tSliceFromEgtf = 0;
        double snappedToTime;
        synchronized (this)
        {
            timeGranularity = this.timeAxis.granularity;
            spaceGranularity = this.spaceAxis.granularity;

            redo = getAndResetInvalidTimeSpan();
            // snap to granularity
            this.toTime = (double) (timeGranularity * ((int) (now.si / timeGranularity)));
            snappedToTime = this.toTime;
            if (snappedToTime > this.timeAxis.maxValue)
            {
                this.timeAxis.setMaxValue(snappedToTime);
            }

            // save local copies so commands given during this execution can change it for the next execution
            smooth0 = this.smooth && snappedToTime > timeGranularity;
            interpolate0 = this.timeAxis.interpolate;
            // kernel size based on granularity
            timeKernelSize = timeGranularity * 2 * KERNEL_FACTOR;
            spaceKernelSize = spaceGranularity * 2 * KERNEL_FACTOR;
            spaceTicks = Arrays.copyOf(this.spaceAxis.getTicks(), this.spaceAxis.getTicks().length);
            timeTicks = Arrays.copyOf(this.timeAxis.getTicks(), this.timeAxis.getTicks().length);
            if (!redo)
            {
                // remember where we started, readyItems will be updated but we need to know where we started during the update
                fromSpaceIndex = (this.readyItems + 1) % this.spaceAxis.getSliceCount();
                fromTimeIndex = (this.readyItems + 1) / this.spaceAxis.getSliceCount();
            }
            toTimeIndex = ((int) (snappedToTime / timeGranularity)) - (interpolate0 ? 0 : 1);
            if (smooth0)
            {
                // time of current slice - kernel size, get slice of that time, get time (middle) of that slice
                tFromEgtf = this.timeAxis.getSliceValue(redo ? 0
                        : this.timeAxis.getValueSlice(this.timeAxis.getSliceValue(fromTimeIndex) - 2 * timeKernelSize));
                tSliceFromEgtf = this.timeAxis.getValueSlice(tFromEgtf);

                /*
                 * The above time is based on twice the kernel size because the fast implementation only accounts for data on
                 * (and within range of) the output grid. To make sure all data within the kernel size (now-kernel : now) is
                 * correct (given the data available up to now), we need all data in twice that size (now-2*kernel : now). Only
                 * the second half of that (now-kernel : now) should be written in the output data. The value of skipTime makes
                 * overwriteSmoothed() skip the first half (now-2*kernel : now-kernel).
                 */
                double tFromEgtf2 = this.timeAxis.getSliceValue(
                        redo ? 0 : this.timeAxis.getValueSlice(this.timeAxis.getSliceValue(fromTimeIndex) - timeKernelSize));
                int nFromEgtf2 = this.timeAxis.getValueSlice(tFromEgtf2);
                skipTime = nFromEgtf2 - tSliceFromEgtf;
            }

            if (redo)
            {
                this.readyItems = -1;
            }
        }
        return new UpdateContext(redo, timeGranularity, spaceGranularity, smooth0, interpolate0, timeKernelSize,
                spaceKernelSize, spaceTicks, timeTicks, fromSpaceIndex, fromTimeIndex, toTimeIndex, tFromEgtf, tSliceFromEgtf,
                skipTime, snappedToTime);
    }

    /**
     * Resets data and filter upon a redo.
     * @param uc update context
     */
    private void resetDataOnRedo(final UpdateContext uc)
    {
        if (!uc.redo())
        {
            return;
        }
        int nSpace = uc.spaceTicks().length - 1;
        int nTime = uc.timeTicks().length - 1;
        this.distance = new float[nSpace][nTime];
        this.time = new float[nSpace][nTime];
        for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
        {
            this.additionalData.put(type, new float[nSpace][nTime]);
        }
        this.egtf = null;
    }

    /**
     * Configure filter; setting up a filter or clearing any existent filter objects.
     * @param uc update context
     */
    private void configureFilter(final UpdateContext uc)
    {
        if (uc.smooth() && this.egtf == null)
        {
            setupFilter(uc.timeGranularity(), uc.timeKernelSize(), uc.spaceKernelSize());
        }
        else if (!uc.smooth())
        {
            // discard smoothing state
            this.egtf = null;
            this.speedStream = null;
            this.travelTimeStream = null;
            this.travelDistanceStream = null;
            this.additionalStreams.clear();
        }
    }

    /**
     * Setup the filter.
     * @param timeGranularity time granularity
     * @param timeKernelSize time kernel size
     * @param spaceKernelSize space kernel size
     */
    private void setupFilter(final double timeGranularity, final double timeKernelSize, final double spaceKernelSize)
    {
        // create the filter
        this.egtf = new Egtf(C_CONG.si, this.cFree.si, DELTA_V.si, this.vc.si);

        // create data source and its data streams for speed, distance traveled, time traveled, and additional
        DataSource generic = this.egtf.getDataSource("generic");
        generic.addStream(TypedQuantity.SPEED, Speed.ofSI(1.0), Speed.ofSI(1.0));
        generic.addStreamSI(this.travelTimeQuantity, 1.0, 1.0);
        generic.addStreamSI(this.travelDistanceQuantity, 1.0, 1.0);
        this.speedStream = generic.getStream(TypedQuantity.SPEED);
        this.travelTimeStream = generic.getStream(this.travelTimeQuantity);
        this.travelDistanceStream = generic.getStream(this.travelDistanceQuantity);
        for (ContourAdditionalDataType<?, ?> contourDataType : this.additionalData.keySet())
        {
            this.additionalStreams.put(contourDataType, generic.addStreamSI(contourDataType.getQuantity(), 1.0, 1.0));
        }

        // for maximum space and time range, increase sigma and tau by KERNEL_FACTOR, beyond which both kernels diminish
        this.egtf.setKernelSI(spaceKernelSize / KERNEL_FACTOR, timeKernelSize / KERNEL_FACTOR, spaceKernelSize,
                timeGranularity);

        // add listener to provide a filter status update and to possibly stop the filter when the plot is invalidated
        this.egtf.addListener((event) ->
        {
            // check stop (explicit use of property, not locally stored value)
            if (isInvalidTimeSpan())
            {
                // plots need to be redone
                Logger.ots().debug("Interrupting EGTF");
                event.interrupt(); // stop the EGTF
            }
        });
    }

    /**
     * Ensure capacity.
     * @param toTimeIndex to time index
     */
    private void ensureTimeCapacity(final int toTimeIndex)
    {
        for (int i = 0; i < this.distance.length; i++)
        {
            this.distance[i] = GraphUtil.ensureCapacity(this.distance[i], toTimeIndex + 1);
            this.time[i] = GraphUtil.ensureCapacity(this.time[i], toTimeIndex + 1);
            for (float[][] add : this.additionalData.values())
            {
                add[i] = GraphUtil.ensureCapacity(add[i], toTimeIndex + 1);
            }
        }
    }

    /**
     * Processing a time slice.
     * @param j time slice index
     * @param uc update context
     * @return false if the processing should be aborted
     */
    private boolean processTimeSlice(final int j, final UpdateContext uc)
    {
        final Duration tFrom = Duration.ofSI(uc.timeTicks()[j]);
        final Duration tTo = Duration.ofSI(uc.timeTicks()[j + 1]);

        int fromSpaceIndex = uc.fromSpaceIndex(); // local copy; set to 0 after first cell

        for (int i = fromSpaceIndex; i < uc.spaceTicks().length - 1; i++)
        {
            if (handleInterpolationEdges(i, j, uc.interpolate()))
            {
                this.readyItems++;
                if (isInvalidTimeSpan())
                {
                    return false;
                }
                continue;
            }

            // in next time slice, all of space needs to be processed
            fromSpaceIndex = 0;

            // define cell
            Length xFrom = Length.ofSI(uc.spaceTicks()[i]);
            Length xTo = Length.ofSI(Math.min(uc.spaceTicks()[i + 1], this.path.getTotalLength().si));
            CellWindow window = new CellWindow(i, j, xFrom, xTo, tFrom, tTo);

            // compute cell totals
            CellTotals totals = aggregateCell(window);

            // write cell data
            this.distance[i][j] = (float) totals.distance();
            this.time[i][j] = (float) totals.time();
            for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
            {
                this.additionalData.get(type)[i][j] = finalizeAdditional(totals.additional(), type);
            }

            feedFilterIfNeeded(window, totals, uc);

            if (isInvalidTimeSpan())
            {
                return false; // early stop
            }
            this.readyItems++;
        }

        // offer time slice result
        offerPaintState(uc);

        return true;
    }

    /**
     * Handle edge cases for interpolation.
     * @param i space slice index
     * @param j time slice index
     * @param interpolate whether the data will be interpolated
     * @return whether the cell was processed as an edge case for interpolation
     */
    private boolean handleInterpolationEdges(final int i, final int j, final boolean interpolate)
    {
        if ((j == 0 || i == 0) && interpolate)
        {
            this.distance[i][j] = Float.NaN;
            this.time[i][j] = Float.NaN;
            for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
            {
                this.additionalData.get(type)[i][j] = Float.NaN;
            }
            return true;
        }
        return false;
    }

    /**
     * Aggregate data in a single cell.
     * @param cell cell window
     * @return cell totals
     */
    private CellTotals aggregateCell(final CellWindow cell)
    {
        double totalDistance = 0.0;
        double totalTime = 0.0;

        Map<ContourAdditionalDataType<?, ?>, Object> additionalIntermediate = new LinkedHashMap<>();
        for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
        {
            additionalIntermediate.put(type, type.identity());
        }

        int nSeries = this.path.getNumberOfSeries();
        for (int series = 0; series < nSeries; series++)
        {
            // gather groups for series
            List<TrajectoryGroup<?>> groups = groupsForSeries(series);

            // filter groups for cell
            List<TrajectoryGroup<?>> included = new ArrayList<>();
            List<Length> xStart = new ArrayList<>();
            List<Length> xEnd = new ArrayList<>();
            includedGroupsForCell(groups, cell, included, xStart, xEnd);

            // accumulate data
            DistTime distTime = accumulateDistanceAndTime(cell, included, xStart, xEnd);
            totalDistance += distTime.distance();
            totalTime += distTime.time();
            for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
            {
                addAdditional(additionalIntermediate, type, included, xStart, xEnd, cell.tFrom(), cell.tTo());
            }
        }

        // normalize to full cell on single lane so EGTF compares apples to apples
        double length = cell.xTo().si - cell.xFrom().si;
        double norm = this.spaceAxis.granularity / length / nSeries;
        totalDistance *= norm;
        totalTime *= norm;

        return new CellTotals(totalDistance, totalTime, additionalIntermediate);
    }

    /**
     * Returns trajectory groups for the series.
     * @param series series index
     * @return trajectory groups for the series
     */
    private List<TrajectoryGroup<?>> groupsForSeries(final int series)
    {
        List<TrajectoryGroup<?>> groups = new ArrayList<>();
        for (Section<? extends LaneData<?>> section : getPath().getSections())
        {
            TrajectoryGroup<?> group = this.samplerData.getTrajectoryGroup(section.getSource(series)).orElse(null);
            if (group == null)
            {
                Logger.ots().error("trajectoryGroup {} is null", series);
            }
            groups.add(group);
        }
        return groups;
    }

    /**
     * Filter the trajectory groups regarding the cell. The results are added to the last three input parameters.
     * @param trajectories trajectory groups
     * @param cell cell window
     * @param included list for included trajectories to be stored in
     * @param xStart list of start coordinates for included trajectories
     * @param xEnd list of end coordinates for included trajectories
     */
    private void includedGroupsForCell(final List<TrajectoryGroup<?>> trajectories, final CellWindow cell,
            final List<TrajectoryGroup<?>> included, final List<Length> xStart, final List<Length> xEnd)
    {
        for (int k = 0; k < trajectories.size(); k++)
        {
            TrajectoryGroup<?> tg = trajectories.get(k);
            LaneData<?> lane = tg.getLane();
            Length startDistance = this.path.getStartDistance(this.path.get(k));
            double secStart = startDistance.si;
            double secEnd = secStart + this.path.get(k).length().si;

            if (secEnd > cell.xFrom().si && secStart < cell.xTo().si)
            {
                included.add(tg);
                double scale = this.path.get(k).length().si / lane.getLength().si;
                xStart.add(Length.max(cell.xFrom().minus(startDistance).divide(scale), Length.ZERO));
                xEnd.add(Length.min(cell.xTo().minus(startDistance).divide(scale), tg.getLane().getLength()));
            }
        }
    }

    /**
     * Accumulate distance and time of included trajectories.
     * @param cell cell window
     * @param included included trajectories
     * @param xStart list of start coordinates for included trajectories
     * @param xEnd list of end coordinates for included trajectories
     * @return accumulated distance and time
     */
    private DistTime accumulateDistanceAndTime(final CellWindow cell, final List<TrajectoryGroup<?>> included,
            final List<Length> xStart, final List<Length> xEnd)
    {
        double dist = 0.0;
        double tim = 0.0;
        for (int k = 0; k < included.size(); k++)
        {
            TrajectoryGroup<?> tg = included.get(k);
            for (Trajectory<?> tr : tg.getTrajectories())
            {
                if (!GraphUtil.considerTrajectory(tr, cell.tFrom(), cell.tTo()))
                {
                    continue;
                }
                try
                {
                    SpaceTimeView v = tr.getSpaceTimeView(xStart.get(k), xEnd.get(k), cell.tFrom(), cell.tTo());
                    dist += v.distance().si;
                    tim += v.time().si;
                }
                catch (IllegalArgumentException ex)
                {
                    Logger.ots().debug(ex, "Unable to generate space-time view x={}..{}, t={}..{}", xStart.get(k), xEnd.get(k),
                            cell.tFrom(), cell.tTo());
                }
            }
        }
        return new DistTime(dist, tim);
    }

    /**
     * Add additional data to stored intermediate result.
     * @param additionalIntermediate intermediate storage map
     * @param contourDataType additional data type
     * @param included trajectories
     * @param xStart start distance per trajectory group
     * @param xEnd end distance per trajectory group
     * @param tFrom start time
     * @param tTo end time
     * @param <I> intermediate data type
     */
    @SuppressWarnings("unchecked")
    private <I> void addAdditional(final Map<ContourAdditionalDataType<?, ?>, Object> additionalIntermediate,
            final ContourAdditionalDataType<?, ?> contourDataType, final List<TrajectoryGroup<?>> included,
            final List<Length> xStart, final List<Length> xEnd, final Duration tFrom, final Duration tTo)
    {
        additionalIntermediate.put(contourDataType, ((ContourAdditionalDataType<?, I>) contourDataType)
                .processSeries((I) additionalIntermediate.get(contourDataType), included, xStart, xEnd, tFrom, tTo));
    }

    /**
     * Stores a finalized result for additional data.
     * @param additionalIntermediate intermediate storage map
     * @param contourDataType additional data type
     * @return finalized results for a cell
     * @param <I> intermediate data type
     */
    @SuppressWarnings("unchecked")
    private <I> float finalizeAdditional(final Map<ContourAdditionalDataType<?, ?>, Object> additionalIntermediate,
            final ContourAdditionalDataType<?, ?> contourDataType)
    {
        return ((ContourAdditionalDataType<?, I>) contourDataType).finalize((I) additionalIntermediate.get(contourDataType))
                .floatValue();
    }

    /**
     * Feed data into the filter if we are smoothing.
     * @param cell cell window
     * @param totals totals in cell
     * @param uc update context
     */
    private void feedFilterIfNeeded(final CellWindow cell, final CellTotals totals, final UpdateContext uc)
    {
        if (!uc.smooth())
        {
            return;
        }
        double xDat = (cell.xFrom().si + cell.xTo().si) / 2.0;
        double tDat = (cell.tFrom().si + cell.tTo().si) / 2.0;

        if (this.path.isCircular())
        {
            double pathLength = this.path.getTotalLength().si;
            if (xDat < uc.spaceKernelSize())
            {
                setDataInEgtf(pathLength + xDat, tDat, totals.distance(), totals.time(), cell.i(), cell.j());
            }
            if (xDat > pathLength - uc.spaceKernelSize())
            {
                setDataInEgtf(xDat - pathLength, tDat, totals.distance(), totals.time(), cell.i(), cell.j());
            }
        }
        setDataInEgtf(xDat, tDat, totals.distance(), totals.time(), cell.i(), cell.j());
    }

    /**
     * Sets data in the EGTF for filtering.
     * @param xDat position of data
     * @param tDat time of data
     * @param totalDistance total distance traveled
     * @param totalTime total time traveled
     * @param i space index in data grid
     * @param j time index in data grid
     */
    private void setDataInEgtf(final double xDat, final double tDat, final double totalDistance, final double totalTime,
            final int i, final int j)
    {
        // speed data is implicit as totalDistance/totalTime, but the EGTF needs it explicitly
        this.egtf.addPointDataSI(this.speedStream, xDat, tDat, totalDistance / totalTime);
        this.egtf.addPointDataSI(this.travelDistanceStream, xDat, tDat, totalDistance);
        this.egtf.addPointDataSI(this.travelTimeStream, xDat, tDat, totalTime);
        for (ContourAdditionalDataType<?, ?> contourDataType : this.additionalStreams.keySet())
        {
            this.egtf.addPointDataSI(this.additionalStreams.get(contourDataType), xDat, tDat,
                    this.additionalData.get(contourDataType)[i][j]);
        }
    }

    /**
     * Apply smoothing filter.
     * @param uc update context
     */
    private void applySmoothingIfNeeded(final UpdateContext uc)
    {
        if (!uc.smooth())
        {
            return;
        }

        // gather quantities
        Set<Quantity<?, ?>> quantities = new LinkedHashSet<>();
        quantities.add(this.travelDistanceQuantity);
        quantities.add(this.travelTimeQuantity);
        this.additionalData.keySet().forEach((type) -> quantities.add(type.getQuantity()));

        // size of space to skip as this space was only used to provide data around edges
        int skipSpace = this.path.isCircular() ? (int) Math.ceil(uc.spaceKernelSize() / uc.spaceGranularity()) : 0;

        // do the filtering
        double tTo = uc.snappedToTime();
        if (tTo <= uc.tSliceFromEgtf())
        {
            return;
        }
        Optional<Filter> filter = this.egtf.filterFastSI(uc.spaceTicks()[0] + (0.5 - skipSpace) * uc.spaceGranularity(),
                uc.spaceGranularity(), uc.spaceTicks()[0] + (-1.5 + uc.spaceTicks().length + skipSpace) * uc.spaceGranularity(),
                uc.tSliceFromEgtf(), uc.timeGranularity(), tTo, quantities.toArray(new Quantity<?, ?>[0]));
        if (filter.isEmpty())
        {
            return;
        }

        // overwrite data with smoothed data
        overwriteSmoothed(this.distance, uc.nFromEgtf(), filter.get().getSI(this.travelDistanceQuantity), uc.skipTime(),
                skipSpace);
        overwriteSmoothed(this.time, uc.nFromEgtf(), filter.get().getSI(this.travelTimeQuantity), uc.skipTime(), skipSpace);
        for (ContourAdditionalDataType<?, ?> type : this.additionalData.keySet())
        {
            overwriteSmoothed(this.additionalData.get(type), uc.nFromEgtf(), filter.get().getSI(type.getQuantity()),
                    uc.skipTime(), skipSpace);
        }

        // notify filter result
        offerPaintState(uc);
    }

    /**
     * Offers the current state of data for painting.
     * @param uc update context
     */
    private void offerPaintState(final UpdateContext uc)
    {
        final int nTimeSlices = uc.timeTicks().length - 1;
        final int nSpaceSlices = uc.spaceTicks().length - 1;
        final int n = nTimeSlices * nSpaceSlices;
        final Map<ContourDataType<?>, float[]> dataMap = new LinkedHashMap<>();
        final double area = uc.timeGranularity() * uc.spaceGranularity();
        final int limit = Math.min(this.readyItems + 1, n);

        for (ContourDataType<?> dataType : this.dataTypes)
        {
            float[] data = new float[n];
            int i = 0;
            if (dataType instanceof ContourEdieDataType edieType)
            {
                OUTER: for (int timeSlice = 0; timeSlice < nTimeSlices; timeSlice++)
                {
                    for (int spaceSlice = 0; spaceSlice < nSpaceSlices; spaceSlice++, i++)
                    {
                        if (i == limit)
                        {
                            break OUTER;
                        }
                        else
                        {
                            data[i] = (float) edieType.calculate(this.time[spaceSlice][timeSlice],
                                    this.distance[spaceSlice][timeSlice], area);
                        }
                    }
                }
            }
            else
            {
                ContourAdditionalDataType<?, ?> additionalType = (ContourAdditionalDataType<?, ?>) dataType;
                float[][] matrix = this.additionalData.get(dataType);
                float scale = (float) (additionalType.normalize() ? area : 1.0);
                OUTER: for (int timeSlice = 0; timeSlice < nTimeSlices; timeSlice++)
                {
                    for (int spaceSlice = 0; spaceSlice < nSpaceSlices; spaceSlice++, i++)
                    {
                        if (i == limit)
                        {
                            break OUTER;
                        }
                        else
                        {
                            data[i] = matrix[i % nSpaceSlices][i / nSpaceSlices] / scale;
                        }
                    }
                }
            }
            if (i < n - 1)
            {
                Arrays.fill(data, i, n, Float.NaN);
            }
            dataMap.put(dataType, data);
        }

        for (AbstractContourPlot<?> plot : getPlots())
        {
            plot.offerPaintState(new ContourPaintState(dataMap.get(plot.getContourDataType()), uc.spaceGranularity(),
                    uc.timeGranularity(), nSpaceSlices, uc.interpolate(), Duration.ofSI(uc.snappedToTime())));
        }
    }

    /**
     * Helper method to fill smoothed data in to raw data.
     * @param raw the raw non-smoothed data
     * @param rawCol column from which onward to fill smoothed data in to the raw data which is used for plotting
     * @param smoothed smoothed data returned by {@code EGTF}
     * @param skipTime slices to skip as this was only part of the smoothed data to include the kernel size
     * @param skipSpace slices to ignore at start and end because of circular graph path (i.e. this was only included for data)
     */
    private void overwriteSmoothed(final float[][] raw, final int rawCol, final double[][] smoothed, final int skipTime,
            final int skipSpace)
    {
        for (int i = 0; i < raw.length; i++)
        {
            int ii = i + skipSpace;
            // can't use System.arraycopy due to float vs double
            for (int j = skipTime; j < smoothed[ii].length; j++)
            {
                raw[i][j + rawCol] = (float) smoothed[ii][j];
            }
        }
    }

    // **********************
    // *** HELPER CLASSES ***
    // **********************

    /**
     * Update context.
     * @param redo redo whole time window
     * @param timeGranularity time granularity
     * @param spaceGranularity space granularity
     * @param smooth smooth data
     * @param interpolate interpolate data
     * @param timeKernelSize time kernel size
     * @param spaceKernelSize space kernel size
     * @param spaceTicks space ticks
     * @param timeTicks time ticks
     * @param fromSpaceIndex from space index
     * @param fromTimeIndex from time index
     * @param toTimeIndex tom time index
     * @param tSliceFromEgtf start slice for filter; only meaningful if {@code smooth==true}
     * @param nFromEgtf n start filter; only meaningful if {@code smooth==true}
     * @param skipTime skip time for filter; only meaningful if {@code smooth==true}
     * @param snappedToTime to time that adheres time granularity
     */
    private record UpdateContext(boolean redo, double timeGranularity, double spaceGranularity, boolean smooth,
            boolean interpolate, double timeKernelSize, double spaceKernelSize, double[] spaceTicks, double[] timeTicks,
            int fromSpaceIndex, int fromTimeIndex, int toTimeIndex, double tSliceFromEgtf, int nFromEgtf, int skipTime,
            double snappedToTime)
    {
    }

    /**
     * Defines a cell for processing.
     * @param i space slice index
     * @param j time slice index
     * @param xFrom space start coordinate
     * @param xTo space end coordinate
     * @param tFrom time start coordinate
     * @param tTo time end coordinate
     */
    private record CellWindow(int i, int j, Length xFrom, Length xTo, Duration tFrom, Duration tTo)
    {
    }

    /**
     * Totals computed in a cell.
     * @param distance distance in cell
     * @param time time in cell
     * @param additional additional data in cell
     */
    private record CellTotals(double distance, double time, Map<ContourAdditionalDataType<?, ?>, Object> additional)
    {
    }

    /**
     * Intermediate data storage to accumulate values in cell.
     * @param distance distance in cell from one lane
     * @param time time in cell from one lane
     */
    private record DistTime(double distance, double time)
    {
    }

    /**
     * Enum to refer to either the distance or time axis.
     */
    public enum Dimension
    {
        /** Distance axis. */
        DISTANCE
        {
            @Override
            protected Axis getAxis(final ContourDataSource dataPool)
            {
                return dataPool.spaceAxis;
            }
        },

        /** Time axis. */
        TIME
        {
            @Override
            protected Axis getAxis(final ContourDataSource dataPool)
            {
                return dataPool.timeAxis;
            }
        };

        /**
         * Returns the {@code Axis} object.
         * @param dataPool data pool
         * @return axis
         */
        protected abstract Axis getAxis(ContourDataSource dataPool);
    }

    /**
     * Class to store and determine axis information such as granularity, ticks, and range.
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
         * @param minValue minimum value
         * @param maxValue maximum value
         * @param granularity initial granularity
         * @param granularities possible granularities
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
         * @param maxValue maximum value
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
         * @param granularity granularity
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
         * @return ticks
         */
        double[] getTicks()
        {
            if (this.ticks == null)
            {
                int n = getSliceCount() + 1;
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
         * Calculates the number of slices.
         * @return number of slices
         */
        int getSliceCount()
        {
            return (int) Math.ceil((this.maxValue - this.minValue) / this.granularity) + (this.interpolate ? 1 : 0);
        }

        /**
         * Calculates the center value of a slices.
         * @param slice slice number
         * @return center value of the slice
         */
        double getSliceValue(final int slice)
        {
            return this.minValue + (0.5 + slice - (this.interpolate ? 1 : 0)) * this.granularity;
        }

        /**
         * Looks up the slice number of the value.
         * @param value value
         * @return slice number
         */
        int getValueSlice(final double value)
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
         * @param interpolate interpolation
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
         * @return true if interpolation is on; false if interpolation is off
         */
        public boolean isInterpolate()
        {
            return this.interpolate;
        }

        @Override
        public String toString()
        {
            return "Axis [minValue=" + this.minValue + ", maxValue=" + this.maxValue + ", granularity=" + this.granularity
                    + ", granularities=" + Arrays.toString(this.granularities) + ", interpolate=" + this.interpolate
                    + ", ticks=" + Arrays.toString(this.ticks) + "]";
        }

    }

    /**
     * Contour data type.
     * @param <Z> value type
     */
    public sealed interface ContourDataType<Z extends Number> permits ContourEdieDataType, ContourAdditionalDataType
    {

        /**
         * Adds weighted values to the mean, where the weight of each value in {@code values} is equal to the respective delta
         * in {@code weightDimension}. The last value in {@code values} is ignored. Argument {@code weightDimension} is
         * typically space or time to produce a space-mean or a time-mean.
         * @param values values
         * @param weightDimension weight dimension
         * @param mean mean
         */
        static void weighted(final float[] values, final float[] weightDimension, final ArithmeticMean<Double, Double> mean)
        {
            for (int i = 0; i < values.length - 1; i++)
            {
                mean.add((double) values[i], (double) (weightDimension[i + 1] - weightDimension[i]));
            }
        }

        /**
         * Adds weighted values to the mean, where the weight of each value in {@code values} is equal to the respective delta
         * in {@code weightDimension}. NaN values and the last value in {@code values} are ignored. Argument
         * {@code weightDimension} is typically space or time to produce a space-mean or a time-mean.
         * @param values values
         * @param weightDimension weight dimension
         * @param mean mean
         */
        static void weightedNaN(final float[] values, final float[] weightDimension, final ArithmeticMean<Double, Double> mean)
        {
            for (int i = 0; i < values.length - 1; i++)
            {
                if (!Float.isNaN(values[i]))
                {
                    mean.add((double) values[i], (double) (weightDimension[i + 1] - weightDimension[i]));
                }
            }
        }

    }

    /**
     * Edie's contour data source types. These are the standard flow, density and speed, calculated based on total distance,
     * total time, and area "time x space" of a cell.
     * @param <Z> value type
     */
    public non-sealed interface ContourEdieDataType<Z extends Number> extends ContourDataType<Z>
    {

        /** Contour data type for flow. */
        ContourEdieDataType<Frequency> FLOW = new ContourEdieDataType<>()
        {
            @Override
            public double calculate(final double totalTime, final double totalDistance, final double area)
            {
                return totalDistance / area;
            }
        };

        /** Contour data type for density. */
        ContourEdieDataType<LinearDensity> DENSITY = new ContourEdieDataType<>()
        {
            @Override
            public double calculate(final double totalTime, final double totalDistance, final double area)
            {
                return totalTime / area;
            }
        };

        /** Contour data type for speed. */
        ContourEdieDataType<Speed> SPEED = new ContourEdieDataType<>()
        {
            @Override
            public double calculate(final double totalTime, final double totalDistance, final double area)
            {
                return totalDistance / totalTime;
            }
        };

        /**
         * Calculate the value.
         * @param totalTime total trajectory time in area
         * @param totalDistance total trajectory distance in area
         * @param area area "time x space" of space-time cell
         * @return calculated value
         */
        double calculate(double totalTime, double totalDistance, double area);

    }

    /**
     * Interface for data types of which a contour plot can be made. Using this class, the data pool can determine and store
     * cell values for a variable set of additional data types (besides total distance, total time and speed).
     * @param <Z> value type
     * @param <I> intermediate data type
     */
    public non-sealed interface ContourAdditionalDataType<Z extends Number, I> extends ContourDataType<Z>
    {

        /**
         * Returns the initial value for intermediate result.
         * @return I, initial intermediate value
         */
        I identity();

        /**
         * Calculate value from provided trajectories that apply to a single grid cell on a single series (lane).
         * @param intermediate intermediate value of previous series, starts as the identity
         * @param trajectories trajectories, all groups overlap the requested space-time
         * @param xFrom start location of cell on the section
         * @param xTo end location of cell on the section
         * @param tFrom start time of cell
         * @param tTo end time of cell
         * @return intermediate value
         */
        I processSeries(I intermediate, List<TrajectoryGroup<?>> trajectories, List<Length> xFrom, List<Length> xTo,
                Duration tFrom, Duration tTo);

        /**
         * Returns the final value of the intermediate result after all lanes.
         * @param intermediate intermediate result after all lanes
         * @return final value
         */
        Z finalize(I intermediate);

        /**
         * Returns the quantity that is being plotted on the z-axis for the EGTF filter.
         * @return quantity that is being plotted on the z-axis for the EGTF filter
         */
        Quantity<Z, ?> getQuantity();

        /**
         * Returns whether the data type needs normalization by the area "space x time".
         * @return whether the data type needs normalization by the area "space x time"
         */
        boolean normalize();

    }

}
