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

    /** Minimum position of the section. */
    private final Length minLength;

    /** Maximum position of the section. */
    private final Length maxLength;

    /** Direction for which the trajectories have been sampled. */
    private final LaneDirection laneDirection;

    /** Trajectories. */
    private final List<Trajectory> trajectories = new ArrayList<>();

    /**
     * Constructor without length specification. The complete lane will be used.
     * @param startTime start time of trajectories
     * @param laneDirection lane direction
     */
    public Trajectories(final Duration startTime, final LaneDirection laneDirection)
    {
        this.startTime = startTime;
        this.minLength = Length.ZERO;
        this.maxLength = laneDirection.getLane().getLength();
        this.laneDirection = laneDirection;
    }

    /**
     * @param startTime start time of trajectories
     * @param minLength length of the section
     * @param maxLength length of the section
     * @param laneDirection lane direction
     */
    public Trajectories(final Duration startTime, final Length minLength, final Length maxLength,
            final LaneDirection laneDirection)
    {
        this.startTime = startTime;
        this.minLength = minLength;
        this.maxLength = maxLength;
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
     * @return length.
     */
    public final Length getLength()
    {
        return this.maxLength.minus(this.minLength);
    }

    /**
     * Whether this {@code Trajectories} holds the given trajectory. Note that this is false if the given trajectory is derived
     * from a trajectory in this {@code Trajectories}.
     * @param trajectory trajectory
     * @return whether this {@code Trajectories} holds the given trajectory.
     */
    public final boolean contains(final Trajectory trajectory)
    {
        return this.trajectories.contains(trajectory);
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
     * @param startLength start length
     * @param endLength end length
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Length startLength, final Length endLength)
    {
        Length minLenght = Length.max(startLength, this.minLength);
        Length maxLenght = Length.min(endLength, this.maxLength);
        Trajectories out = new Trajectories(this.startTime, minLenght, maxLenght, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(startLength, endLength));
        }
        return out;
    }

    /**
     * Returns trajectories between two times.
     * @param startDuration start duration
     * @param endDuration end duration
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Duration startDuration, final Duration endDuration)
    {
        Trajectories out = new Trajectories(this.startTime.lt(startDuration) ? startDuration : this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(startDuration, endDuration));
        }
        return out;
    }

    /**
     * Returns trajectories between two locations and between two times.
     * @param startLength start length
     * @param endLength end length
     * @param startDuration start duration
     * @param endDuration end duration
     * @return list of trajectories
     */
    public final Trajectories getTrajectories(final Length startLength, final Length endLength, final Duration startDuration,
            final Duration endDuration)
    {
        Trajectories out = new Trajectories(this.startTime.lt(startDuration) ? startDuration : this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(startLength, endLength, startDuration, endDuration));
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
