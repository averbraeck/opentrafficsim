package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.network.lane.LaneDirection;

/**
 * Contains all trajectories pertaining to a certain space-time region.
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
public class TrajectoryGroup
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
    public TrajectoryGroup(final Duration startTime, final LaneDirection laneDirection)
    {
        this.startTime = startTime;
        this.minLength = Length.ZERO;
        this.maxLength = laneDirection.getLane().getLength();
        this.laneDirection = laneDirection;
    }

    /**
     * @param startTime start time of trajectory group
     * @param minLength length of the section
     * @param maxLength length of the section
     * @param laneDirection lane direction
     */
    public TrajectoryGroup(final Duration startTime, final Length minLength, final Length maxLength,
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
     * Whether this {@code TrajectoryGroup} holds the given trajectory. Note that this is false if the given trajectory is
     * derived from a trajectory in this {@code TrajectoryGroup}.
     * @param trajectory trajectory
     * @return whether this {@code TrajectoryGroup} holds the given trajectory.
     */
    public final boolean contains(final Trajectory trajectory)
    {
        return this.trajectories.contains(trajectory);
    }

    /**
     * Returns a list of trajectories.
     * @return list of trajectories
     */
    public final List<Trajectory> getTrajectories()
    {
        return new ArrayList<>(this.trajectories);
    }

    /**
     * Returns trajectory group between two locations.
     * @param startLength start length
     * @param endLength end length
     * @return list of trajectories
     */
    public final TrajectoryGroup getTrajectoryGroup(final Length startLength, final Length endLength)
    {
        Length minLenght = Length.max(startLength, this.minLength);
        Length maxLenght = Length.min(endLength, this.maxLength);
        TrajectoryGroup out = new TrajectoryGroup(this.startTime, minLenght, maxLenght, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(startLength, endLength));
        }
        return out;
    }

    /**
     * Returns trajectory group between two times.
     * @param startDuration start duration
     * @param endDuration end duration
     * @return list of trajectories
     */
    public final TrajectoryGroup getTrajectoryGroup(final Duration startDuration, final Duration endDuration)
    {
        TrajectoryGroup out =
                new TrajectoryGroup(this.startTime.lt(startDuration) ? startDuration : this.startTime, this.laneDirection);
        for (Trajectory trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(startDuration, endDuration));
        }
        return out;
    }

    /**
     * Returns trajectory group between two locations and between two times.
     * @param startLength start length
     * @param endLength end length
     * @param startDuration start duration
     * @param endDuration end duration
     * @return list of trajectories
     */
    public final TrajectoryGroup getTrajectoryGroup(final Length startLength, final Length endLength,
            final Duration startDuration, final Duration endDuration)
    {
        TrajectoryGroup out =
                new TrajectoryGroup(this.startTime.lt(startDuration) ? startDuration : this.startTime, this.laneDirection);
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

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.laneDirection == null) ? 0 : this.laneDirection.hashCode());
        result = prime * result + ((this.maxLength == null) ? 0 : this.maxLength.hashCode());
        result = prime * result + ((this.minLength == null) ? 0 : this.minLength.hashCode());
        result = prime * result + ((this.startTime == null) ? 0 : this.startTime.hashCode());
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
        TrajectoryGroup other = (TrajectoryGroup) obj;
        if (this.laneDirection == null)
        {
            if (other.laneDirection != null)
            {
                return false;
            }
        }
        else if (!this.laneDirection.equals(other.laneDirection))
        {
            return false;
        }
        if (this.maxLength == null)
        {
            if (other.maxLength != null)
            {
                return false;
            }
        }
        else if (!this.maxLength.equals(other.maxLength))
        {
            return false;
        }
        if (this.minLength == null)
        {
            if (other.minLength != null)
            {
                return false;
            }
        }
        else if (!this.minLength.equals(other.minLength))
        {
            return false;
        }
        if (this.startTime == null)
        {
            if (other.startTime != null)
            {
                return false;
            }
        }
        else if (!this.startTime.equals(other.startTime))
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrajectoryGroup [startTime=" + this.startTime + ", minLength=" + this.minLength + ", maxLength="
                + this.maxLength + ", laneDirection=" + this.laneDirection + "]";
    }

}