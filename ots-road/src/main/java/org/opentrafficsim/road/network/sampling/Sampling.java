package org.opentrafficsim.road.network.sampling;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.meta.MetaData;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO set this class as a property of Network
public class Sampling implements EventListenerInterface
{

    /** Map with all sampling data. */
    private final Map<LaneDirection, Trajectories> trajectories = new HashMap<>();

    /** End times of active samplings. */
    private final Map<LaneDirection, Duration> endTimes = new HashMap<>();

    private OTSDEVSSimulatorInterface simulator;

    /**
     * @param simulator simulator
     * @param spaceTimeRegion space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final OTSDEVSSimulatorInterface simulator, final SpaceTimeRegion spaceTimeRegion)
    {
        this.simulator = simulator;
        Duration firstDataTime;
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            firstDataTime = this.trajectories.get(spaceTimeRegion.getLaneDirection()).getStartTime();
        }
        else
        {
            firstDataTime = new Duration(simulator.getSimulatorTime().getTime().si, TimeUnit.SI);
        }
        Throw.when(spaceTimeRegion.getStartTime().lt(firstDataTime), IllegalStateException.class,
                "Space time region with start time %s is defined while data is available from %s onwards.");
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(),
                    Duration.max(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.trajectories.put(spaceTimeRegion.getLaneDirection(),
                    new Trajectories(firstDataTime, spaceTimeRegion.getLaneDirection()));
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), spaceTimeRegion.getEndTime());
            // TODO schedule event for startRecording at spaceTimeRegion.gettStart()
            try
            {
                simulator.scheduleEventNow(this, this, "startRecording",
                        new Object[] { Duration.ZERO, spaceTimeRegion.getLaneDirection(), simulator });
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException("Cannot start recording.", exception);
            }
        }
        // TODO schedule event for stopRecording at this.endTimes.get(spaceTimeRegion.getLaneDirection())
        // note, before then, additional space-time regions with a later end time may have been registered
        try
        {
            simulator.scheduleEventAbs(simulator.getSimulatorTime().plus(this.endTimes.get(spaceTimeRegion.getLaneDirection())),
                    this, this, "stopRecording", new Object[] { spaceTimeRegion.getLaneDirection(), simulator });
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot stop recording.", exception);
        }
    }

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param time current time
     * @param laneDirection lane direction
     * @param simulator simulator
     */
    public final void startRecording(final Duration time, final LaneDirection laneDirection,
            final OTSDEVSSimulatorInterface simulator)
    {
        this.trajectories.put(laneDirection, new Trajectories(time, laneDirection));
        // TODO subscribe for Lane/GTU events to append trajectories
        laneDirection.getLane().addListener(this, Lane.GTU_ADD_EVENT, true);
        laneDirection.getLane().addListener(this, Lane.GTU_REMOVE_EVENT, true);
    }

    private Map<String, Trajectory> trajectoryPerGtu = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_addition}
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            Lane lane = gtu.getLanes().keySet().iterator().next();
            LaneDirection laneDirection = new LaneDirection(lane, gtu.getLanes().get(lane));
            if (this.trajectories.containsKey(laneDirection))
            {
                gtu.addListener(this, GTU.MOVE_EVENT, true);
                boolean longitudinalEntry = false;
                Trajectory trajectory = new Trajectory(gtu, longitudinalEntry, new MetaData());
                this.trajectoryPerGtu.put(gtuId, trajectory);
                this.trajectories.get(laneDirection).addTrajectory(trajectory);
            }
        }
        if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_removal}
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            gtu.removeListener(this, GTU.MOVE_EVENT);
            Lane lane = gtu.getLanes().keySet().iterator().next();
            if (this.trajectoryPerGtu.get(gtuId) != null)
            {
                this.trajectoryPerGtu.remove(gtu);
            }
        }
        if (event.getType().equals(GTU.MOVE_EVENT))
        {
            // Payload: [String id, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
            // * turnIndicatorStatus, Length odometer]
            Object[] payload = (Object[]) event.getContent();
            String gtuId = (String) payload[0];
            // get trajectories
            this.trajectoryPerGtu.get(gtuId).add(new Length(((DirectedPoint) payload[1]).getX(), LengthUnit.SI),
                    (Speed) payload[2], (Acceleration) payload[3],
                    new Duration(this.simulator.getSimulatorTime().get().si, TimeUnit.SI));
        }

    }

    /**
     * Stop recording at given lane direction.
     * @param laneDirection lane direction
     * @param simulator simulator
     */
    public final void stopRecording(final LaneDirection laneDirection, final OTSDEVSSimulatorInterface simulator)
    {
        this.trajectories.remove(laneDirection);
        // TODO unsubscribe for Lane/GTU events to append trajectories
    }

    /**
     * Returns the trajectories of given lane direction.
     * @param laneDirection lane direction
     * @return trajectories of given lane direction, {@code null} if none
     */
    public final Trajectories getTrajectories(final LaneDirection laneDirection)
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
        Sampling other = (Sampling) obj;
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
        return true;
    }

}
