package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.immutablecollections.ImmutableIterator;

/**
 * Set of trajectories to be accepted or rejected for a query.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 sep. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TrajectoryAcceptList
{

    /** GTU id of the contained trajectories. */
    private String gtuId;

    /** List of trajectory's. */
    private final List<Trajectory> trajectoryList = new ArrayList<>();

    /** List of trajectories. */
    private final List<Trajectories> trajectoriesList = new ArrayList<>();

    /** Map of trajectory's and acceptance boolean. */
    private final Map<Trajectory, Boolean> trajectoryMap = new HashMap<>();

    /**
     * Adds a {@code Trajectory} with the {@code Trajectories} it is from to the accept list.
     * @param trajectory {@code Trajectory} trajectory
     * @param trajectories {@code Trajectories} trajectories
     * @throws IllegalArgumentException if the {@code Trajectory} is not within the {@code Trajectories}
     * @throws IllegalArgumentException if the {@code Trajectory} belongs to a different GTU than an earlier provided
     *             {@code Trajectory}
     */
    public final void addTrajectory(final Trajectory trajectory, final Trajectories trajectories)
    {
        Throw.whenNull(trajectory, "Trajectory may not be null.");
        Throw.whenNull(trajectories, "Trajectories may not be null.");
        Throw.when(!trajectories.contains(trajectory), IllegalArgumentException.class,
                "The trajectory should be contained within the trajectories.");
        Throw.when(this.gtuId != null && !this.gtuId.equals(trajectory.getGtuId()), IllegalArgumentException.class,
                "Trajectories of different GTU's may not be in a single trajectory accept list.");
        this.gtuId = trajectory.getGtuId();
        this.trajectoryList.add(trajectory);
        this.trajectoriesList.add(trajectories);
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
     * @param i number of {@code trajectory} to get
     * @return i'th {@code trajectory}
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public final Trajectory getTrajectory(final int i)
    {
        return this.trajectoryList.get(i);
    }

    /**
     * @param i number of {@code trajectories} to get
     * @return i'th {@code trajectories}
     * @throws IndexOutOfBoundsException if the index is out of range (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public final Trajectories getTrajectories(final int i)
    {
        return this.trajectoriesList.get(i);
    }

    /**
     * @return iterator over {@code trajectory}'s, does not allow removal
     */
    public final Iterator<Trajectory> getTrajectoryIterator()
    {
        return new ImmutableIterator<>(this.trajectoryList.iterator());
    }

    /**
     * @return iterator over {@code Trajectories}'s, does not allow removal
     */
    public final Iterator<Trajectories> getTrajectoriesIterator()
    {
        return new ImmutableIterator<>(this.trajectoriesList.iterator());
    }

    /**
     * Accept given trajectory.
     * @param trajectory trajectory to accept
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void acceptTrajectory(final Trajectory trajectory)
    {
        acceptTrajectory(trajectory, true);
    }

    /**
     * Reject given trajectory.
     * @param trajectory trajectory to reject
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void rejectTrajectory(final Trajectory trajectory)
    {
        acceptTrajectory(trajectory, false);
    }
    
    /**
     * Accept or reject given trajectory.
     * @param trajectory trajectory to accept or reject
     * @param accept whether to accept the trajectory
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final void acceptTrajectory(final Trajectory trajectory, final boolean accept)
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
        for (Trajectory trajectory : this.trajectoryList)
        {
            this.trajectoryMap.put(trajectory, true);
        }
    }

    /**
     * Reject all trajectories.
     */
    public final void rejectAll()
    {
        for (Trajectory trajectory : this.trajectoryList)
        {
            this.trajectoryMap.put(trajectory, false);
        }
    }

    /**
     * Returns whether the given trajectory is accepted or not. If this was not determined, it is {@code false} by default.
     * @param trajectory trajectory
     * @return whether the given trajectory is accepted or not
     * @throws IllegalArgumentException if the trajectory is not part of the trajectory accept list
     */
    public final boolean isAccepted(final Trajectory trajectory)
    {
        Throw.when(!this.trajectoryList.contains(trajectory), IllegalArgumentException.class,
                "The trajectory is not part of the trajectory accept list.");
        return this.trajectoryMap.get(trajectory);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TrajectoryAcceptList [gtuId=" + this.gtuId + ", trajectoryList=" + this.trajectoryList + ", trajectoriesList="
                + this.trajectoriesList + ", trajectoryMap=" + this.trajectoryMap + "]";
    }
    
    

}
