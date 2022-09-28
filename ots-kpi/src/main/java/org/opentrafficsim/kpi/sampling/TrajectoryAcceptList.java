package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;

/**
 * Set of trajectories to be accepted or rejected for a query.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrajectoryAcceptList
{

    /** GTU id of the contained trajectories. */
    private String gtuId;

    /** List of trajectory's. */
    private final List<Trajectory<?>> trajectoryList = new ArrayList<>();

    /** List of trajectory groups. */
    private final List<TrajectoryGroup> trajectoryGroupList = new ArrayList<>();

    /** Map of trajectory's and acceptance boolean. */
    private final Map<Trajectory<?>, Boolean> trajectoryMap = new LinkedHashMap<>();

    /**
     * Adds a {@code Trajectory} with the {@code TrajectoryGroup} it is from to the accept list.
     * @param trajectory Trajectory&lt;?&gt;; {@code Trajectory} trajectory
     * @param trajectoryGroup TrajectoryGroup; {@code TrajectoryGroup} trajectories
     * @throws IllegalArgumentException if the {@code Trajectory} is not within the {@code TrajectoryGroup}
     * @throws IllegalArgumentException if the {@code Trajectory} belongs to a different GTU than an earlier provided
     *             {@code Trajectory}
     */
    public final void addTrajectory(final Trajectory<?> trajectory, final TrajectoryGroup trajectoryGroup)
    {
        Throw.whenNull(trajectory, "Trajectory may not be null.");
        Throw.whenNull(trajectoryGroup, "Trajectory group may not be null.");
        // This is quite a costly check
        // Throw.when(!trajectoryGroup.contains(trajectory), IllegalArgumentException.class,
        // "The trajectory should be contained within the trajectory group.");
        Throw.when(this.gtuId != null && !this.gtuId.equals(trajectory.getGtuId()), IllegalArgumentException.class,
                "Trajectories of different GTU's may not be in a single trajectory accept list.");
        this.gtuId = trajectory.getGtuId();
        this.trajectoryList.add(trajectory);
        this.trajectoryGroupList.add(trajectoryGroup);
        this.trajectoryMap.put(trajectory, false);
    }

    /**
     * @return number of trajectories
     */
    public final int size()
    {
        return this.trajectoryList.size();
    }

    /**
     * @param i int; number of {@code trajectory} to get
     * @return i'th {@code trajectory}
     * @throws IndexOutOfBoundsException if the index is out of range (<code>index &lt; 0 || index &gt;= size()</code>)
     */
    public final Trajectory<?> getTrajectory(final int i)
    {
        return this.trajectoryList.get(i);
    }

    /**
     * @param i int; number of {@code TrajectoryGroup} to get
     * @return i'th {@code TrajectoryGroup}
     * @throws IndexOutOfBoundsException if the index is out of range (<code>index &lt; 0 || index &gt;= size()</code>)
     */
    public final TrajectoryGroup getTrajectoryGroup(final int i)
    {
        return this.trajectoryGroupList.get(i);
    }

    /**
     * @return iterator over {@code trajectory}'s, does not allow removal
     */
    public final Iterator<Trajectory<?>> getTrajectoryIterator()
    {
        return new ImmutableIterator<>(this.trajectoryList.iterator());
    }

    /**
     * @return iterator over {@code TrajectoryGroup}'s, does not allow removal
     */
    public final Iterator<TrajectoryGroup> getTrajectoryGroupIterator()
    {
        return new ImmutableIterator<>(this.trajectoryGroupList.iterator());
    }

    /**
     * Accept given trajectory.
     * @param trajectory Trajectory&lt;?&gt;; trajectory to accept
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void acceptTrajectory(final Trajectory<?> trajectory)
    {
        acceptTrajectory(trajectory, true);
    }

    /**
     * Reject given trajectory.
     * @param trajectory Trajectory&lt;?&gt;; trajectory to reject
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void rejectTrajectory(final Trajectory<?> trajectory)
    {
        acceptTrajectory(trajectory, false);
    }

    /**
     * Accept or reject given trajectory.
     * @param trajectory Trajectory&lt;?&gt;; trajectory to accept or reject
     * @param accept boolean; whether to accept the trajectory
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void acceptTrajectory(final Trajectory<?> trajectory, final boolean accept)
    {
        Throw.when(!this.trajectoryList.contains(trajectory), IllegalArgumentException.class,
                "The trajectory is not part of the trajectory accept list.");
        this.trajectoryMap.put(trajectory, accept);
    }

    /**
     * Accept all trajectories.
     */
    public final void acceptAll()
    {
        for (Trajectory<?> trajectory : this.trajectoryList)
        {
            this.trajectoryMap.put(trajectory, true);
        }
    }

    /**
     * Reject all trajectories.
     */
    public final void rejectAll()
    {
        for (Trajectory<?> trajectory : this.trajectoryList)
        {
            this.trajectoryMap.put(trajectory, false);
        }
    }

    /**
     * Returns whether the given trajectory is accepted or not. If this was not determined, it is {@code false} by default.
     * @param trajectory Trajectory&lt;?&gt;; trajectory
     * @return whether the given trajectory is accepted or not
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final boolean isAccepted(final Trajectory<?> trajectory)
    {
        Boolean out = this.trajectoryMap.get(trajectory);
        Throw.when(out == null, IllegalArgumentException.class, "The trajectory is not part of the trajectory accept list.");
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrajectoryAcceptList [gtuId=" + this.gtuId + ", " + this.trajectoryList.size() + " trajectories]";
    }

}
