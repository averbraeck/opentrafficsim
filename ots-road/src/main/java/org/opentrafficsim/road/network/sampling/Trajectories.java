package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Trajectories
{

    /** Start time of trajectories. */
    private final Duration startTime;
    
    /** Direction for which the trajectories have been sampled. */
    private final LaneDirection laneDirection;

    /** Trajectories. */
    private final List<Trajectory> trajectories = new ArrayList<>();

    /**
     * @param startTime start time of trajectories
     * @param laneDirection lane direction
     */
    public Trajectories(final Duration startTime, final LaneDirection laneDirection)
    {
        this.startTime = startTime;
        this.laneDirection = laneDirection;
    }

    /**
     * Add trajectory.
     * @param trajectory trajectory to add
     */
    public final void addTrajectory(final Trajectory trajectory)
    {
        this.trajectories.add(trajectory);
    }
    
    /**
     * @return startTime.
     */
    public final Duration getStartTime()
    {
        return this.startTime;
    }

    /**
     * Returns a list of trajectories.
     * @return list of trajectories
     */
    public final List<Trajectory> getTrajectorySet()
    {
        return new ArrayList<>(this.trajectories);
    }

    /**
     * Returns trajectories between two locations.
     * @param minLength minimum length
     * @param maxLength maximum length
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Length minLength, final Length maxLength)
    {
        Trajectories out = new Trajectories(this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(minLength, maxLength));
        }
        return out;
    }

    /**
     * Returns trajectories between two times.
     * @param minDuration minimum duration
     * @param maxDuration maximum duration
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Duration minDuration, final Duration maxDuration)
    {
        Trajectories out = new Trajectories(this.startTime.lt(minDuration) ? minDuration : this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(minDuration, maxDuration));
        }
        return out;
    }

    /**
     * Returns trajectories between two locations and between two times.
     * @param minLength minimum length
     * @param maxLength maximum length
     * @param minDuration minimum duration
     * @param maxDuration maximum duration
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Length minLength, final Length maxLength,
        final Duration minDuration, final Duration maxDuration)
    {
        Trajectories out = new Trajectories(this.startTime.lt(minDuration) ? minDuration : this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(minLength, maxLength, minDuration, maxDuration));
        }
        return out;
    }

    /**
     * Returns the lane direction.
     * @return lane direction
     */
    public final LaneDirection getLaneDirection()
    {
        return this.laneDirection;
    }

}
