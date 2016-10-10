package org.opentrafficsim.road.network.sampling;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.meta.MetaData;
import org.opentrafficsim.road.network.sampling.meta.MetaDataType;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

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
public class Sampler implements EventListenerInterface
{

    /** Map with all sampling data. */
    private final Map<LaneDirection, TrajectoryGroup> trajectories = new HashMap<>();

    /** End times of active samplings. */
    private final Map<LaneDirection, Duration> endTimes = new HashMap<>();

    /** Simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<Lane, Trajectory>> trajectoryPerGtu = new HashMap<>();

    /** Registration of sampling events of each GTU per lane, if interval based. */
    private final Map<String, Map<Lane, SimEvent<OTSSimTimeDouble>>> eventPerGtu = new HashMap<>();

    /** Set of registered meta data types. */
    private Set<MetaDataType<?>> registeredMetaDataTypes = new HashSet<>();

    /** Sampling interval. */
    private final Duration samplingInterval;

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param simulator simulator
     * @throw {@link NullPointerException} if the simulator is {@code null}
     */
    public Sampler(final OTSDEVSSimulatorInterface simulator)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        this.simulator = simulator;
        this.samplingInterval = null;
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param simulator simulator
     * @param frequency sampling frequency
     * @throw {@link NullPointerException} if an input is {@code null}
     * @throw {@link IllegalArgumentException} if frequency is negative or zero
     */
    public Sampler(final OTSDEVSSimulatorInterface simulator, final Frequency frequency)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(frequency, "Frequency may not be null.");
        Throw.when(frequency.le(Frequency.ZERO), IllegalArgumentException.class,
                "Negative or zero sampling frequency is not permitted.");
        this.simulator = simulator;
        this.samplingInterval = new Duration(1.0 / frequency.si, TimeUnit.SI);
    }

    /**
     * @param spaceTimeRegion space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion spaceTimeRegion)
    {
        Duration firstPossibleDataTime;
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            firstPossibleDataTime = this.trajectories.get(spaceTimeRegion.getLaneDirection()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = new Duration(this.simulator.getSimulatorTime().getTime().si, TimeUnit.SI);
        }
        Throw.when(spaceTimeRegion.getStartTime().lt(firstPossibleDataTime), IllegalStateException.class,
                "Space time region with start time %s is defined while data is available from %s onwards.",
                spaceTimeRegion.getStartTime(), firstPossibleDataTime);
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(),
                    Duration.max(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), spaceTimeRegion.getEndTime());
            try
            {
                this.simulator.scheduleEventNow(this, this, "startRecording",
                        new Object[] { spaceTimeRegion.getStartTime(), spaceTimeRegion.getLaneDirection() });
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException("Cannot start recording.", exception);
            }
        }
        try
        {
            this.simulator.scheduleEventAbs(
                    this.simulator.getSimulatorTime().plus(this.endTimes.get(spaceTimeRegion.getLaneDirection())), this, this,
                    "stopRecording",
                    new Object[] { this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getLaneDirection() });
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot stop recording.", exception);
        }
    }

    /**
     * Registers meta data types that will be stored with the trajectories.
     * @param metaDataTypes meta data types to register
     */
    public final void registerMetaDataTypes(final Set<MetaDataType<?>> metaDataTypes)
    {
        this.registeredMetaDataTypes.addAll(metaDataTypes);
    }

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param time current time
     * @param laneDirection lane direction
     */
    public final void startRecording(final Duration time, final LaneDirection laneDirection)
    {
        if (this.trajectories.containsKey(laneDirection))
        {
            return;
        }
        this.trajectories.put(laneDirection, new TrajectoryGroup(time, laneDirection));
        laneDirection.getLane().addListener(this, Lane.GTU_ADD_EVENT, true);
        laneDirection.getLane().addListener(this, Lane.GTU_REMOVE_EVENT, true);
    }

    /**
     * Stop recording at given lane direction.
     * @param time to stop
     * @param laneDirection lane direction
     */
    public final void stopRecording(final Duration time, final LaneDirection laneDirection)
    {
        if (!this.trajectories.containsKey(laneDirection) || this.endTimes.get(laneDirection).gt(time))
        {
            return;
        }
        laneDirection.getLane().removeListener(this, Lane.GTU_ADD_EVENT);
        laneDirection.getLane().removeListener(this, Lane.GTU_REMOVE_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
        {
            // Payload: [String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
            // turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane]
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            if (this.trajectoryPerGtu.containsKey(gtuId) && this.trajectoryPerGtu.get(gtuId).containsKey(payload[6]))
            {
                this.trajectoryPerGtu.get(gtuId).get(payload[6]).add((Length) payload[7], (Speed) payload[2],
                        (Acceleration) payload[3], new Duration(this.simulator.getSimulatorTime().get().si, TimeUnit.SI));
            }
        }
        else if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_addition}
            Lane lane = (Lane) event.getSource();
            // TODO GTUDirectionality from Lane.GTU_ADD_EVENT
            LaneDirection laneDirection = new LaneDirection(lane, GTUDirectionality.DIR_PLUS);
            if (!this.trajectories.containsKey(laneDirection))
            {
                // we are not sampling this LaneDirection
                return;
            }
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            Length distance;
            try
            {
                // TODO Length from Lane.GTU_ADD_EVENT
                distance = gtu.position(lane, RelativePosition.REFERENCE_POSITION);
            }
            catch (GTUException exception)
            {
                throw new RuntimeException(exception);
            }
            Speed speed = gtu.getSpeed();
            Acceleration acceleration = gtu.getAcceleration();
            Duration time = new Duration(this.simulator.getSimulatorTime().getTime().si, TimeUnit.SI);
            boolean longitudinalEntry = false;
            Trajectory trajectory = new Trajectory(gtu, longitudinalEntry, makeMetaData(gtu));
            trajectory.add(distance, speed, acceleration, time);
            if (!this.trajectoryPerGtu.containsKey(gtuId))
            {
                Map<Lane, Trajectory> map = new HashMap<>();
                this.trajectoryPerGtu.put(gtuId, map);
            }
            this.trajectoryPerGtu.get(gtuId).put(lane, trajectory);
            this.trajectories.get(laneDirection).addTrajectory(trajectory);
            if (isIntervalBased())
            {
                scheduleSamplingEvent(gtu, lane);
            }
            else
            {
                gtu.addListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT, true);
            }
        }
        else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_removal}
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            Lane lane = (Lane) event.getSource();
            if (this.trajectoryPerGtu.get(gtuId) != null)
            {
                this.trajectoryPerGtu.get(gtuId).remove(lane);
                if (this.trajectoryPerGtu.get(gtuId).isEmpty())
                {
                    this.trajectoryPerGtu.remove(gtuId);
                }
            }
            if (isIntervalBased())
            {
                if (this.eventPerGtu.get(gtuId) != null)
                {
                    if (this.eventPerGtu.get(gtuId).containsKey(lane))
                    {
                        this.simulator.cancelEvent(this.eventPerGtu.get(gtuId).get(lane));
                    }
                    this.eventPerGtu.get(gtuId).remove(lane);
                    if (this.eventPerGtu.get(gtuId).isEmpty())
                    {
                        this.eventPerGtu.remove(gtuId);
                    }
                }
            }
            else
            {
                gtu.removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
            }
        }

    }

    /**
     * @return whether sampling is interval based
     */
    private boolean isIntervalBased()
    {
        return this.samplingInterval != null;
    }
    
    /**
     * Schedules a sampling event for the given gtu on the given lane for the samlping interval from the current time.
     * @param gtu gtu to sample
     * @param lane lane where the gtu is at
     */
    private void scheduleSamplingEvent(final LaneBasedGTU gtu, final Lane lane)
    {
        OTSSimTimeDouble simTime = this.simulator.getSimulatorTime().copy();
        simTime.add(this.samplingInterval);
        SimEvent<OTSSimTimeDouble> simEvent = new SimEvent<>(simTime, this, this, "notifySample", new Object[] { gtu, lane });
        try
        {
            this.simulator.scheduleEvent(simEvent);
        }
        catch (SimRuntimeException exception)
        {
            // should not happen with getSimulatorTime.add()
            throw new RuntimeException("Scheduling sampling in the past.", exception);
        }
        String gtuId = gtu.getId();
        if (!this.eventPerGtu.containsKey(gtuId))
        {
            Map<Lane, SimEvent<OTSSimTimeDouble>> map = new HashMap<>();
            this.eventPerGtu.put(gtuId, map);
        }
        this.eventPerGtu.get(gtuId).put(lane, simEvent);
    }

    /**
     * Samples a gtu and schedules the next sampling event.
     * @param gtu gtu to sample
     * @param lane lane where the gtu is at
     */
    public final void notifySample(final LaneBasedGTU gtu, final Lane lane)
    {
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.containsKey(gtuId) && this.trajectoryPerGtu.get(gtuId).containsKey(lane))
        {
            // Payload: [String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
            // turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane]
            try
            {
                this.trajectoryPerGtu.get(gtuId).get(lane).add(gtu.position(lane, RelativePosition.REFERENCE_POSITION),
                        gtu.getSpeed(), gtu.getAcceleration(),
                        new Duration(this.simulator.getSimulatorTime().get().si, TimeUnit.SI));
            }
            catch (GTUException exception)
            {
                // should not happen, as remove event should prevent it
                throw new RuntimeException("Could not execute sampling event.", exception);
            }
        }
        scheduleSamplingEvent(gtu, lane);
    }

    /**
     * @param gtu gtu to return meta data for
     * @param <T> underlying type of a meta data type
     * @return meta data for the given gtu
     */
    @SuppressWarnings("unchecked")
    private <T> MetaData makeMetaData(final GTU gtu)
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
     * Returns the trajectory group of given lane direction.
     * @param laneDirection lane direction
     * @return trajectory group of given lane direction, {@code null} if none
     */
    public final TrajectoryGroup getTrajectoryGroup(final LaneDirection laneDirection)
    {
        return this.trajectories.get(laneDirection);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endTimes == null) ? 0 : this.endTimes.hashCode());
        result = prime * result + ((this.trajectories == null) ? 0 : this.trajectories.hashCode());
        result = prime * result + ((this.trajectoryPerGtu == null) ? 0 : this.trajectoryPerGtu.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Sampling [simulator=" + this.simulator + "]";
    }

}
