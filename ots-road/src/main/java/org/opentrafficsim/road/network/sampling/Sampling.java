package org.opentrafficsim.road.network.sampling;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.lane.LaneDirection;

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
public class Sampling
{

    /** Map with all sampling data. */
    private final Map<LaneDirection, Trajectories> trajectories = new HashMap<>();

    /** End times of active samplings. */
    private final Map<LaneDirection, Duration> endTimes = new HashMap<>();

    /**
     * @param simulator simulator
     * @param spaceTimeRegion space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final OTSSimulatorInterface simulator, final SpaceTimeRegion spaceTimeRegion)
    {
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
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), Duration.max(this.endTimes.get(spaceTimeRegion
                .getLaneDirection()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), spaceTimeRegion.getEndTime());
            // TODO schedule event for startRecording at spaceTimeRegion.gettStart()
        }
        // TODO schedule event for stopRecording at this.endTimes.get(spaceTimeRegion.getLaneDirection())
        // note, before then, additional space-time regions with a later end time may have been registered
    }
    
    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param time current time
     * @param laneDirection lane direction
     */
    public final void startRecording(final Duration time, final LaneDirection laneDirection)
    {
        this.trajectories.put(laneDirection, new Trajectories(time, laneDirection));
        // TODO subscribe for Lane/GTU events to append trajectories
    }
    
    /**
     * Stop recording at given lane direction.
     * @param laneDirection lane direction
     */
    public final void stopRecording(final LaneDirection laneDirection)
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
