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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU data type
 */
public class TrajectoryGroup<G extends GtuData> implements Iterable<Trajectory<G>>
{

    /** Start time of trajectories. */
    private final Time startTime;

    /** Start position of the section. */
    private final Length startPosition;

    /** End position of the section. */
    private final Length endPosition;

    /** Lane for which the trajectories have been sampled. */
    private final LaneData<?> lane;

    /** Trajectories. */
    private final List<Trajectory<G>> trajectories = new ArrayList<>();

    /**
     * Constructor without length specification. The complete lane will be used.
     * @param startTime start time of trajectories
     * @param lane lane
     */
    public TrajectoryGroup(final Time startTime, final LaneData<?> lane)
    {
        this(startTime, Length.ZERO, lane == null ? null : lane.getLength(), lane);
    }

    /**
     * Constructor.
     * @param startTime start time of trajectory group
     * @param startPosition start position
     * @param endPosition end position
     * @param lane the lane
     */
    public TrajectoryGroup(final Time startTime, final Length startPosition, final Length endPosition, final LaneData<?> lane)
    {
        Throw.whenNull(startTime, "Start time may not be null.");
        // keep before position check; prevents "End position may not be null" due to missing direction in other constructor
        Throw.whenNull(lane, "Lane time may not be null.");
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
     * @param trajectory trajectory to add
     */
    public final synchronized void addTrajectory(final Trajectory<G> trajectory)
    {
        this.trajectories.add(trajectory);
    }

    /**
     * Returns the start time.
     * @return start time
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Returns the length.
     * @return length
     */
    public final Length getLength()
    {
        return this.endPosition.minus(this.startPosition);
    }

    /**
     * Whether this {@code TrajectoryGroup} holds the given trajectory. Note that this is false if the given trajectory is
     * derived from a trajectory in this {@code TrajectoryGroup} (e.g. a subset of).
     * @param trajectory trajectory
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
     * @param x0 start length
     * @param x1 end length
     * @return list of trajectories
     */
    public final synchronized TrajectoryGroup<G> getTrajectoryGroup(final Length x0, final Length x1)
    {
        Length minLenght = Length.max(x0, this.startPosition);
        Length maxLenght = Length.min(x1, this.endPosition);
        TrajectoryGroup<G> out = new TrajectoryGroup<>(this.startTime, minLenght, maxLenght, this.lane);
        for (Trajectory<G> trajectory : this.trajectories)
        {
            Trajectory<G> sub = trajectory.subSet(x0, x1);
            if (sub.size() > 0)
            {
                out.addTrajectory(sub);
            }
        }
        return out;
    }

    /**
     * Returns trajectory group between two times.
     * @param t0 start time
     * @param t1 end time
     * @return list of trajectories
     */
    public final synchronized TrajectoryGroup<G> getTrajectoryGroup(final Time t0, final Time t1)
    {
        TrajectoryGroup<G> out = new TrajectoryGroup<>(this.startTime.lt(t0) ? t0 : this.startTime, this.lane);
        for (Trajectory<G> trajectory : this.trajectories)
        {
            Trajectory<G> sub = trajectory.subSet(t0, t1);
            if (sub.size() > 0)
            {
                out.addTrajectory(sub);
            }
        }
        return out;
    }

    /**
     * Returns trajectory group between two locations and between two times.
     * @param x0 start length
     * @param x1 end length
     * @param t0 start time
     * @param t1 end time
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
    public final LaneData<?> getLane()
    {
        return this.lane;
    }

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.lane.hashCode();
        result = prime * result + this.endPosition.hashCode();
        result = prime * result + this.startPosition.hashCode();
        result = prime * result + this.startTime.hashCode();
        result = prime * result + this.trajectories.hashCode();
        return result;
    }

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
        if (!this.lane.equals(other.lane))
        {
            return false;
        }
        if (!this.endPosition.equals(other.endPosition))
        {
            return false;
        }
        if (!this.startPosition.equals(other.startPosition))
        {
            return false;
        }
        if (!this.startTime.equals(other.startTime))
        {
            return false;
        }
        if (!this.trajectories.equals(other.trajectories))
        {
            return false;
        }
        return true;
    }

    @Override
    public final String toString()
    {
        return "TrajectoryGroup [startTime=" + this.startTime + ", minLength=" + this.startPosition + ", maxLength="
                + this.endPosition + ", lane=" + this.lane + ", collected "
                + (this.trajectories == null ? "null" : this.trajectories.size()) + " trajectories]";
    }

    @Override
    public Iterator<Trajectory<G>> iterator()
    {
        return this.trajectories.iterator();
    }

}
