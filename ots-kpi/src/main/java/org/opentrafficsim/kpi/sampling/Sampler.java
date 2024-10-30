package org.opentrafficsim.kpi.sampling;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> gtu data type
 * @param <L> lane data type
 */
public abstract class Sampler<G extends GtuData, L extends LaneData<L>>
{

    /** Sampler data. */
    private final SamplerData<G> samplerData;

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?, ?, ?, ? super G>> extendedDataTypes;

    /** Set of registered filter data types. */
    private final Set<FilterDataType<?, ? super G>> filterDataTypes;

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<L, Trajectory<G>>> trajectoryPerGtu = new LinkedHashMap<>();

    /** End times of active samplings. */
    private final Map<L, Time> endTimes = new LinkedHashMap<>();

    /** Space time regions. */
    private Set<SpaceTimeRegion<L>> spaceTimeRegions = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param extendedDataTypes extended data types.
     * @param filterDataTypes filter data types.
     */
    public Sampler(final Set<ExtendedDataType<?, ?, ?, ? super G>> extendedDataTypes,
            final Set<FilterDataType<?, ? super G>> filterDataTypes)
    {
        this.extendedDataTypes = new LinkedHashSet<>(extendedDataTypes);
        this.filterDataTypes = new LinkedHashSet<>(filterDataTypes);
        this.samplerData = new SamplerData<>(extendedDataTypes, filterDataTypes);
    }

    /**
     * Underlying sampler data.
     * @return underlying sampler data
     */
    public SamplerData<G> getSamplerData()
    {
        return this.samplerData;
    }

    /**
     * Whether this sampler has the given extended data type registered to it.
     * @param extendedDataType extended data type
     * @return whether this sampler has the given extended data type registered to it
     */
    public boolean contains(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        return this.extendedDataTypes.contains(extendedDataType);
    }

    /**
     * Registers a space-time region. Data will be recorded across the entire length of a lane, but only during specified time
     * periods.
     * @param spaceTimeRegion space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion<L> spaceTimeRegion)
    {
        Throw.whenNull(spaceTimeRegion, "SpaceTimeRegion may not be null.");
        Time firstPossibleDataTime;
        if (this.samplerData.contains(spaceTimeRegion.lane()))
        {
            firstPossibleDataTime = this.samplerData.getTrajectoryGroup(spaceTimeRegion.lane()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = now();
        }
        Throw.when(spaceTimeRegion.startTime().lt(firstPossibleDataTime), IllegalStateException.class,
                "Space time region with start time %s is defined while data is available from %s onwards.",
                spaceTimeRegion.startTime(), firstPossibleDataTime);
        if (this.samplerData.contains(spaceTimeRegion.lane()))
        {
            this.endTimes.put(spaceTimeRegion.lane(),
                    Time.max(this.endTimes.get(spaceTimeRegion.lane()), spaceTimeRegion.endTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.lane(), spaceTimeRegion.endTime());
            scheduleStartRecording(spaceTimeRegion.startTime(), spaceTimeRegion.lane());
        }
        scheduleStopRecording(this.endTimes.get(spaceTimeRegion.lane()), spaceTimeRegion.lane());
        this.spaceTimeRegions.add(spaceTimeRegion);
    }

    /**
     * Returns the current simulation time.
     * @return current simulation time
     */
    public abstract Time now();

    /**
     * Schedules the start of recording for a given lane, i.e. the implementation has to invoke {@code startRecording} at the
     * specified time, with the given lane as input. In case multiple space time-regions are registered for the same lane, this
     * method is invoked whenever the next space-time region that is added has an earlier start time than any before.
     * @param time time to start recording
     * @param lane lane to start recording
     */
    public abstract void scheduleStartRecording(Time time, L lane);

    /**
     * Schedules the stop of recording for a given lane, i.e. the implementation has to invoke {@code stopRecording} at the
     * specified time, with the given lane as input. In case multiple space time-regions are registered for the same lane, this
     * method is invoked whenever the next space-time region that is added has a late end time than any before.
     * @param time time to stop recording
     * @param lane lane to stop recording
     */
    public abstract void scheduleStopRecording(Time time, L lane);

    /**
     * Start recording at the given time (which should be the current time) on the given lane.
     * @param lane lane
     */
    public final void startRecording(final L lane)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        if (this.samplerData.contains(lane))
        {
            return;
        }
        this.samplerData.putTrajectoryGroup(lane, new TrajectoryGroup<>(now(), lane));
        initRecording(lane);
    }

    /**
     * Adds listeners to start recording.
     * @param lane lane to initialize recording for
     */
    public abstract void initRecording(L lane);

    /**
     * Stop recording at given lane.
     * @param lane lane
     */
    public final void stopRecording(final L lane)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        if (!this.samplerData.contains(lane) || this.endTimes.get(lane).gt(now()))
        {
            return;
        }
        finalizeRecording(lane);
    }

    /**
     * Remove listeners to stop recording.
     * @param lane lane
     */
    public abstract void finalizeRecording(L lane);

    /**
     * Creates a trajectory with the current snapshot of a GTU.
     * @param lane lane the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuAddEventWithMove(final L lane, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(position, "Position may not be null.");
        if (lane.getLength().lt(position))
        {
            // ignore event if beyond lane length (may happen during lane change)
            return;
        }
        processGtuAddEvent(lane, gtu);
        processGtuMoveEvent(lane, position, speed, acceleration, time, gtu);
    }

    /**
     * Creates a trajectory, including filter data.
     * @param lane lane the gtu is at
     * @param gtu gtu
     */
    public final void processGtuAddEvent(final L lane, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(gtu, "GtuData may not be null.");
        String gtuId = gtu.getId();
        Trajectory<G> trajectory = new Trajectory<G>(gtu, makeFilterData(gtu), this.extendedDataTypes, lane);
        this.trajectoryPerGtu.computeIfAbsent(gtuId, (key) -> new LinkedHashMap<>()).put(lane, trajectory);
        this.samplerData.getTrajectoryGroup(lane).addTrajectory(trajectory);
    }

    /**
     * Adds a new snapshot of a GTU to its recording trajectory, if recorded. This method may be invoked on GTU that are not
     * being recorded; the event will then be ignored.
     * @param lane lane the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuMoveEvent(final L lane, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(gtu, "GtuData may not be null.");
        String gtuId = gtu.getId();
        Map<L, Trajectory<G>> trajectoryPerLane = this.trajectoryPerGtu.get(gtuId);
        if (trajectoryPerLane != null)
        {
            Trajectory<G> trajectory = trajectoryPerLane.get(lane);
            if (trajectory != null)
            {
                trajectory.add(position, speed, acceleration, time, gtu);
            }
        }
    }

    /**
     * Finalizes a trajectory with the current snapshot of a GTU.
     * @param lane lane the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuRemoveEventWithMove(final L lane, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        processGtuMoveEvent(lane, position, speed, acceleration, time, gtu);
        processGtuRemoveEvent(lane, gtu);
    }

    /**
     * Finalizes a trajectory.
     * @param lane lane the gtu is at
     * @param gtu gtu
     */
    public final void processGtuRemoveEvent(final L lane, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(gtu, "GtuData may not be null.");
        String gtuId = gtu.getId();
        Map<L, Trajectory<G>> trajectoryPerLane = this.trajectoryPerGtu.get(gtuId);
        if (trajectoryPerLane != null)
        {
            trajectoryPerLane.remove(lane);
            if (trajectoryPerLane.isEmpty())
            {
                this.trajectoryPerGtu.remove(gtuId);
            }
        }
    }

    /**
     * Gathers the filter data for filter data types.
     * @param gtu gtu to return filter data for a GTU
     * @return filter data for the given gtu
     */
    private Map<FilterDataType<?, ? super G>, Object> makeFilterData(final G gtu)
    {
        Map<FilterDataType<?, ? super G>, Object> filterData = new LinkedHashMap<>();
        this.filterDataTypes.forEach((filterDataType) -> filterData.put(filterDataType, filterDataType.getValue(gtu)));
        return filterData;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.extendedDataTypes, this.filterDataTypes, this.spaceTimeRegions);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Sampler<?, ?> other = (Sampler<?, ?>) obj;
        return Objects.equals(this.extendedDataTypes, other.extendedDataTypes)
                && Objects.equals(this.filterDataTypes, other.filterDataTypes)
                && Objects.equals(this.spaceTimeRegions, other.spaceTimeRegions);
    }

}
