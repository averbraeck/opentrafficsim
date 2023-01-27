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
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 * @param <L> lane data type
 */
public abstract class Sampler<G extends GtuData, L extends LaneData>
{

    /** Sampler data. */
    private final SamplerData<G> samplerData;

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes;

    /** Set of registered filter data types. */
    private final Set<FilterDataType<?>> filterDataTypes;

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<L, Trajectory<G>>> trajectoryPerGtu = new LinkedHashMap<>();

    /** End times of active samplings. */
    private final Map<L, Time> endTimes = new LinkedHashMap<>();

    /** Space time regions. */
    private Set<SpaceTimeRegion<L>> spaceTimeRegions = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param extendedDataTypes Set&lt;ExtendedDataType&lt;?, ?, ?, G&gt;&gt;; extended data types.
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types.
     */
    public Sampler(final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes, final Set<FilterDataType<?>> filterDataTypes)
    {
        this.extendedDataTypes = new LinkedHashSet<>(extendedDataTypes);
        this.filterDataTypes = new LinkedHashSet<>(filterDataTypes);
        this.samplerData = new SamplerData<>(extendedDataTypes, filterDataTypes);
    }

    /**
     * Underlying sampler data.
     * @return SamplerData&lt;G&gt;; underlying sampler data
     */
    public SamplerData<G> getSamplerData()
    {
        return this.samplerData;
    }

    /**
     * Whether this sampler has the given extended data type registered to it.
     * @param extendedDataType ExtendedDataType&lt;?,?,?,?&gt;; extended data type
     * @return whether this sampler has the given extended data type registered to it
     */
    public boolean contains(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        return this.extendedDataTypes.contains(extendedDataType);
    }

    /**
     * Registers a space-time region. Data will be recorded across the entire length of a lane, but only during specified time
     * periods.
     * @param spaceTimeRegion SpaceTimeRegion&lt;L&gt;; space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion<L> spaceTimeRegion)
    {
        Throw.whenNull(spaceTimeRegion, "SpaceTimeRegion may not be null.");
        Time firstPossibleDataTime;
        if (this.samplerData.contains(spaceTimeRegion.getLane()))
        {
            firstPossibleDataTime = this.samplerData.getTrajectoryGroup(spaceTimeRegion.getLane()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = now();
        }
        Throw.when(spaceTimeRegion.getStartTime().lt(firstPossibleDataTime), IllegalStateException.class,
                "Space time region with start time %s is defined while data is available from %s onwards.",
                spaceTimeRegion.getStartTime(), firstPossibleDataTime);
        if (this.samplerData.contains(spaceTimeRegion.getLane()))
        {
            this.endTimes.put(spaceTimeRegion.getLane(),
                    Time.max(this.endTimes.get(spaceTimeRegion.getLane()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.getLane(), spaceTimeRegion.getEndTime());
            scheduleStartRecording(spaceTimeRegion.getStartTime(), spaceTimeRegion.getLane());
        }
        scheduleStopRecording(this.endTimes.get(spaceTimeRegion.getLane()), spaceTimeRegion.getLane());
        this.spaceTimeRegions.add(spaceTimeRegion);
    }

    /**
     * Returns the current simulation time.
     * @return current simulation time
     */
    public abstract Time now();

    /**
     * Schedules the start of recording for a given lane-direction.
     * @param time Time; time to start recording
     * @param lane L; lane-direction to start recording
     */
    public abstract void scheduleStartRecording(Time time, L lane);

    /**
     * Schedules the stop of recording for a given lane.
     * @param time Time; time to stop recording
     * @param lane L; lane to stop recording
     */
    public abstract void scheduleStopRecording(Time time, L lane);

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param lane L; lane
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
     * @param lane L; lane to initialize recording for
     */
    public abstract void initRecording(L lane);

    /**
     * Stop recording at given lane direction.
     * @param lane L; lane
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
     * @param lane L; lane
     */
    public abstract void finalizeRecording(L lane);

    /**
     * Creates a trajectory with the current snapshot of a GTU.
     * @param lane L; lane the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
     */
    public final void processGtuAddEvent(final L lane, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(gtu, "GtuData may not be null.");
        if (lane.getLength().lt(position))
        {
            // ignore event if beyond lane length (may happen during lane change)
            return;
        }
        String gtuId = gtu.getId();
        Trajectory<G> trajectory = new Trajectory<>(gtu, makeFilterData(gtu), this.extendedDataTypes, lane);
        if (!this.trajectoryPerGtu.containsKey(gtuId))
        {
            Map<L, Trajectory<G>> map = new LinkedHashMap<>();
            this.trajectoryPerGtu.put(gtuId, map);
        }
        this.trajectoryPerGtu.get(gtuId).put(lane, trajectory);
        this.samplerData.getTrajectoryGroup(lane).addTrajectory(trajectory);
        processGtuMoveEvent(lane, position, speed, acceleration, time, gtu);
    }

    /**
     * Adds a new snapshot of a GTU to its recording trajectory, if recorded. This method may be invoked on GTU that are not
     * being recorded; the event will then be ignored.
     * @param lane L; lane the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
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
        if (this.trajectoryPerGtu.containsKey(gtuId) && this.trajectoryPerGtu.get(gtuId).containsKey(lane))
        {
            this.trajectoryPerGtu.get(gtuId).get(lane).add(position, speed, acceleration, time, gtu);
        }
    }

    /**
     * Finalizes a trajectory with the current snapshot of a GTU.
     * @param lane L; lane direction the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
     */
    public final void processGtuRemoveEvent(final L lane, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        processGtuMoveEvent(lane, position, speed, acceleration, time, gtu);
        processGtuRemoveEvent(lane, gtu);
    }

    /**
     * Finalizes a trajectory.
     * @param lane L; lane the gtu is at
     * @param gtu G; gtu
     */
    public final void processGtuRemoveEvent(final L lane, final G gtu)
    {
        Throw.whenNull(lane, "LaneData may not be null.");
        Throw.whenNull(gtu, "GtuData may not be null.");
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.containsKey(gtuId))
        {
            this.trajectoryPerGtu.get(gtuId).remove(lane);
            if (this.trajectoryPerGtu.get(gtuId).isEmpty())
            {
                this.trajectoryPerGtu.remove(gtuId);
            }
        }
    }

    /**
     * Gathers the filter data for filter data types.
     * @param gtu G; gtu to return filter data for a GTU
     * @return filter data for the given gtu
     */
    private Map<FilterDataType<?>, Object> makeFilterData(final G gtu)
    {
        Map<FilterDataType<?>, Object> filterData = new LinkedHashMap<>();
        for (FilterDataType<?> filterDataType : this.filterDataTypes)
        {
            filterData.put(filterDataType, filterDataType.getValue(gtu));
        }
        return filterData;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.extendedDataTypes, this.filterDataTypes, this.spaceTimeRegions);
    }

    /** {@inheritDoc} */
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
