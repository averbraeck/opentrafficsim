package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;

/**
 * Contains all trajectories pertaining to a certain space-time region.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public class TrajectoryGroup<G extends GtuData> implements Iterable<Trajectory<G>>
{

    /** Start time of trajectories. */
    private final Time startTime;

    /** Start position of the section. */
    private final Length startPosition;

    /** End position of the section. */
    private final Length endPosition;

    /** Direction for which the trajectories have been sampled. */
    private final LaneData lane;

    /** Trajectories. */
    private final List<Trajectory<G>> trajectories = new ArrayList<>();

    /**
     * Constructor without length specification. The complete lane will be used.
     * @param startTime Time; start time of trajectories
     * @param lane LaneData; lane
     */
    public TrajectoryGroup(final Time startTime, final LaneData lane)
    {
        this(startTime, Length.ZERO, lane == null ? null : lane.getLength(), lane);
    }

    /**
     * @param startTime Time; start time of trajectory group
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param lane LaneData; the lane
     */
    public TrajectoryGroup(final Time startTime, final Length startPosition, final Length endPosition,
            final LaneData lane)
    {
        Throw.whenNull(startTime, "Start time may not be null.");
        // keep before position check; prevents "End position may not be null" due to missing direction in other constructor
        Throw.whenNull(lane, "Lane direction time may not be null.");
        Throw.whenNull(startPosition, "Start position may not be null");
        Throw.whenNull(endPosition, "End position may not be null");
        Throw.when(startPosition.gt(endPosition), IllegalArgumentException.class,
                "Start position should be smaller than end position in the direction of travel");
        this.startTime = startTime;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.lane = lane;
    }

    /**
     * Add trajectory.
     * @param trajectory Trajectory&lt;G&gt;; trajectory to add
     */
    public final synchronized void addTrajectory(final Trajectory<G> trajectory)
    {
        // System.out.println("Adding trajectory " + trajectory + " to " + this.toString());
        this.trajectories.add(trajectory);
    }

    /**
     * @return startTime.
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return length.
     */
    public final Length getLength()
    {
        return this.endPosition.minus(this.startPosition);
    }

    /**
     * Whether this {@code TrajectoryGroup} holds the given trajectory. Note that this is false if the given trajectory is
     * derived from a trajectory in this {@code TrajectoryGroup}.
     * @param trajectory Trajectory&lt;?&gt;; trajectory
     * @return whether this {@code TrajectoryGroup} holds the given trajectory.
     */
    public final boolean contains(final Trajectory<?> trajectory)
    {
        return this.trajectories.contains(trajectory);
    }

    /**
     * Returns the number of trajectories in this group.
     * @return number of trajectories in this group
     */
    public final int size()
    {
        return this.trajectories.size();
    }

    /**
     * Returns a list of trajectories.
     * @return list of trajectories
     */
    public final List<Trajectory<G>> getTrajectories()
    {
        return new ArrayList<>(this.trajectories);
    }

    /**
     * Returns trajectory group between two locations.
     * @param x0 Length; start length
     * @param x1 Length; end length
     * @return list of trajectories
     */
    public final synchronized TrajectoryGroup<G> getTrajectoryGroup(final Length x0, final Length x1)
    {
        Length minLenght = Length.max(x0, this.startPosition);
        Length maxLenght = Length.min(x1, this.endPosition);
        TrajectoryGroup<G> out = new TrajectoryGroup<>(this.startTime, minLenght, maxLenght, this.lane);
        for (Trajectory<G> trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(x0, x1));
        }
        return out;
    }

    /**
     * Returns trajectory group between two times.
     * @param t0 Time; start time
     * @param t1 Time; end time
     * @return list of trajectories
     */
    public final synchronized TrajectoryGroup<G> getTrajectoryGroup(final Time t0, final Time t1)
    {
        TrajectoryGroup<G> out = new TrajectoryGroup<>(this.startTime.lt(t0) ? t0 : this.startTime, this.lane);
        for (Trajectory<G> trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(t0, t1));
        }
        return out;
    }

    /**
     * Returns trajectory group between two locations and between two times.
     * @param x0 Length; start length
     * @param x1 Length; end length
     * @param t0 Time; start time
     * @param t1 Time; end time
     * @return list of trajectories
     */
    public final synchronized TrajectoryGroup<G> getTrajectoryGroup(final Length x0, final Length x1, final Time t0,
            final Time t1)
    {
        TrajectoryGroup<G> out = new TrajectoryGroup<>(this.startTime.lt(t0) ? t0 : this.startTime, this.lane);
        for (Trajectory<G> trajectory : this.trajectories)
        {
            out.addTrajectory(trajectory.subSet(x0, x1, t0, t1));
        }
        return out;
    }

    /**
     * Returns the lane.
     * @return lane
     */
    public final LaneData getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.lane == null) ? 0 : this.lane.hashCode());
        result = prime * result + ((this.endPosition == null) ? 0 : this.endPosition.hashCode());
        result = prime * result + ((this.startPosition == null) ? 0 : this.startPosition.hashCode());
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
        TrajectoryGroup<?> other = (TrajectoryGroup<?>) obj;
        if (this.lane == null)
        {
            if (other.lane != null)
            {
                return false;
            }
        }
        else if (!this.lane.equals(other.lane))
        {
            return false;
        }
        if (this.endPosition == null)
        {
            if (other.endPosition != null)
            {
                return false;
            }
        }
        else if (!this.endPosition.equals(other.endPosition))
        {
            return false;
        }
        if (this.startPosition == null)
        {
            if (other.startPosition != null)
            {
                return false;
            }
        }
        else if (!this.startPosition.equals(other.startPosition))
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
        return "TrajectoryGroup [startTime=" + this.startTime + ", minLength=" + this.startPosition + ", maxLength="
                + this.endPosition + ", laneDirection=" + this.lane + ", collected "
                + (this.trajectories == null ? "null" : this.trajectories.size()) + " trajectories]";
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<Trajectory<G>> iterator()
    {
        return this.trajectories.iterator();
    }

}
