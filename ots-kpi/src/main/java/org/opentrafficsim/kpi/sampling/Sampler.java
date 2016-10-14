package org.opentrafficsim.kpi.sampling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.MetaData;
import org.opentrafficsim.kpi.sampling.meta.MetaDataType;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Sampler
{

    /** Map with all sampling data. */
    private final Map<KpiLaneDirection, TrajectoryGroup> trajectories = new HashMap<>();

    /** End times of active samplings. */
    private final Map<KpiLaneDirection, Time> endTimes = new HashMap<>();

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<KpiLaneDirection, Trajectory>> trajectoryPerGtu = new HashMap<>();

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?>> extendedDataTypes = new HashSet<>();

    /** Set of registered meta data types. */
    private Set<MetaDataType<?>> registeredMetaDataTypes = new HashSet<>();

    /**
     * @param spaceTimeRegion space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion spaceTimeRegion)
    {
        Time firstPossibleDataTime;
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            firstPossibleDataTime = this.trajectories.get(spaceTimeRegion.getLaneDirection()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = now();
        }
        // TODO Throw
        // Throw.when(spaceTimeRegion.getStartTime().lt(firstPossibleDataTime), IllegalStateException.class,
        // "Space time region with start time %s is defined while data is available from %s onwards.",
        // spaceTimeRegion.getStartTime(), firstPossibleDataTime);
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(),
                    Time.max(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), spaceTimeRegion.getEndTime());
            scheduleStartRecording(spaceTimeRegion.getStartTime(), spaceTimeRegion.getLaneDirection());
        }
        scheduleStopRecording(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getLaneDirection());
    }

    /**
     * Returns the current simulation time.
     * @return current simulation time
     */
    public abstract Time now();

    /**
     * Schedules the start of recording for a given lane-direction.
     * @param time time to start recording
     * @param kpiLaneDirection lane-direction to start recording
     */
    public abstract void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Schedules the stop of recording for a given lane-direction.
     * @param time time to stop recording
     * @param kpiLaneDirection lane-direction to stop recording
     */
    public abstract void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Registers meta data types that will be stored with the trajectories.
     * @param metaDataTypes meta data types to register
     */
    public final void registerMetaDataTypes(final Set<MetaDataType<?>> metaDataTypes)
    {
        this.registeredMetaDataTypes.addAll(metaDataTypes);
    }

    /**
     * Registers extended data type that will be stored with the trajectories.
     * @param extendedDataType extended data type to register
     */
    public final void registerExtendedDataType(final ExtendedDataType<?> extendedDataType)
    {
        this.extendedDataTypes.add(extendedDataType);
    }

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param time current time
     * @param kpiLaneDirection lane direction
     */
    public final void startRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        if (this.trajectories.containsKey(kpiLaneDirection))
        {
            return;
        }
        this.trajectories.put(kpiLaneDirection, new TrajectoryGroup(time, kpiLaneDirection));
        addListeners(kpiLaneDirection);
    }

    /**
     * Adds listeners to start recording.
     * @param kpiLaneDirection lane direction to add listeners for
     */
    public abstract void addListeners(final KpiLaneDirection kpiLaneDirection);

    /**
     * Stop recording at given lane direction.
     * @param time to stop
     * @param kpiLaneDirection lane direction
     */
    public final void stopRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        if (!this.trajectories.containsKey(kpiLaneDirection) || this.endTimes.get(kpiLaneDirection).gt(time))
        {
            return;
        }
        removeListeners(kpiLaneDirection);
    }

    /**
     * Remove listeners to stop recording.
     * @param kpiLaneDirection lane direction to remove listeners for
     */
    public abstract void removeListeners(final KpiLaneDirection kpiLaneDirection);

    /**
     * Creates a trajectory with the current snapshot of a GTU.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuAddEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
    {
        String gtuId = gtu.getId();
        Trajectory trajectory = new Trajectory(gtu, makeMetaData(gtu), this.extendedDataTypes, kpiLaneDirection);
        if (!this.trajectoryPerGtu.containsKey(gtuId))
        {
            Map<KpiLaneDirection, Trajectory> map = new HashMap<>();
            this.trajectoryPerGtu.put(gtuId, map);
        }
        this.trajectoryPerGtu.get(gtuId).put(kpiLaneDirection, trajectory);
        this.trajectories.get(kpiLaneDirection).addTrajectory(trajectory);
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
    }

    /**
     * Adds a new snapshot of a GTU to its recording trajectory, if recorded. This method may be invoked on GTU that are not
     * being recorded; the event will then be ignored.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuMoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
    {
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.containsKey(gtuId) && this.trajectoryPerGtu.get(gtuId).containsKey(kpiLaneDirection))
        {
            this.trajectoryPerGtu.get(gtuId).get(kpiLaneDirection).add(position, speed, acceleration, time, gtu);
        }
    }

    /**
     * Finalizes a trajectory with the current snapshot of a GTU.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuRemoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
    {
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.get(gtuId) != null)
        {
            this.trajectoryPerGtu.get(gtuId).remove(kpiLaneDirection);
            if (this.trajectoryPerGtu.get(gtuId).isEmpty())
            {
                this.trajectoryPerGtu.remove(gtuId);
            }
        }
    }

    /**
     * @param gtu gtu to return meta data for
     * @param <T> underlying type of a meta data type
     * @return meta data for the given gtu
     */
    @SuppressWarnings("unchecked")
    private <T> MetaData makeMetaData(final GtuDataInterface gtu)
    {
        MetaData metaData = new MetaData();
        for (MetaDataType<?> metaDataType : this.registeredMetaDataTypes)
        {
            T value = (T) metaDataType.getValue(gtu);
            if (value != null)
            {
                metaData.put((MetaDataType<T>) metaDataType, value);
            }
        }
        return metaData;
    }

    /**
     * Returns whether there is data for the give lane direction.
     * @param kpiLaneDirection lane direction
     * @return whether there is data for the give lane direction
     */
    public final boolean contains(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.containsKey(kpiLaneDirection);
    }

    /**
     * Returns the trajectory group of given lane direction.
     * @param kpiLaneDirection lane direction
     * @return trajectory group of given lane direction, {@code null} if none
     */
    public final TrajectoryGroup getTrajectoryGroup(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.get(kpiLaneDirection);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endTimes == null) ? 0 : this.endTimes.hashCode());
        result = prime * result + ((this.extendedDataTypes == null) ? 0 : this.extendedDataTypes.hashCode());
        result = prime * result + ((this.registeredMetaDataTypes == null) ? 0 : this.registeredMetaDataTypes.hashCode());
        result = prime * result + ((this.trajectories == null) ? 0 : this.trajectories.hashCode());
        result = prime * result + ((this.trajectoryPerGtu == null) ? 0 : this.trajectoryPerGtu.hashCode());
        return result;
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
        Sampler other = (Sampler) obj;
        if (this.endTimes == null)
        {
            if (other.endTimes != null)
            {
                return false;
            }
        }
        else if (!this.endTimes.equals(other.endTimes))
        {
            return false;
        }
        if (this.extendedDataTypes == null)
        {
            if (other.extendedDataTypes != null)
            {
                return false;
            }
        }
        else if (!this.extendedDataTypes.equals(other.extendedDataTypes))
        {
            return false;
        }
        if (this.registeredMetaDataTypes == null)
        {
            if (other.registeredMetaDataTypes != null)
            {
                return false;
            }
        }
        else if (!this.registeredMetaDataTypes.equals(other.registeredMetaDataTypes))
        {
            return false;
        }
        if (this.trajectories == null)
        {
            if (other.trajectories != null)
            {
                return false;
            }
        }
        else if (!this.trajectories.equals(other.trajectories))
        {
            return false;
        }
        if (this.trajectoryPerGtu == null)
        {
            if (other.trajectoryPerGtu != null)
            {
                return false;
            }
        }
        else if (!this.trajectoryPerGtu.equals(other.trajectoryPerGtu))
        {
            return false;
        }
        return true;
    }

}
