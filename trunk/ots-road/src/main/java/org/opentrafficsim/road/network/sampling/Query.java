package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.immutablecollections.ImmutableIterator;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.sampling.meta.MetaDataSet;
import org.opentrafficsim.road.network.sampling.meta.MetaDataType;

/**
 * A query defines which subset of trajectory information should be included. This is in terms of space-time regions, and in
 * terms of meta data of trajectories, e.g. only include trajectories of trucks.
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
public final class Query
{
    /** unique id. */
    private UUID uniqueId = UUID.randomUUID();

    /** Sampling. */
    private final Sampling sampling;

    /** Description. */
    private final String description;

    /** Whether the space-time regions are longitudinally connected. */
    private final boolean connected;

    /** Meta data set. */
    private final MetaDataSet metaDataSet;

    /** Update frequency. */
    private final Frequency updateFrequency;

    /** Interval to gather statistics over. */
    private final Duration interval;

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion> spaceTimeRegions = new ArrayList<>();

    /**
     * @param sampling sampling
     * @param description description
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaDataSet meta data
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampling sampling, final String description, final boolean connected, final MetaDataSet metaDataSet)
    {
        this(sampling, description, connected, metaDataSet, null, null);
    }

    /**
     * @param sampling sampling
     * @param description description
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaDataSet meta data
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampling sampling, final String description, final boolean connected, final MetaDataSet metaDataSet,
            final Duration interval)
    {
        this(sampling, description, connected, metaDataSet, null, interval);
    }

    /**
     * @param sampling sampling
     * @param description description
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampling sampling, final String description, final boolean connected, final MetaDataSet metaDataSet,
            final Frequency updateFrequency)
    {
        this(sampling, description, connected, metaDataSet, updateFrequency, null);
    }

    /**
     * @param sampling sampling
     * @param description description
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampling sampling, final String description, final boolean connected, final MetaDataSet metaDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        Throw.whenNull(sampling, "Sampling may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(metaDataSet, "Meta data may not be null.");
        this.sampling = sampling;
        this.connected = connected;
        this.metaDataSet = new MetaDataSet(metaDataSet);
        this.description = description;
        this.updateFrequency = updateFrequency;
        this.interval = interval;
    }

    /**
     * return the unique id for the query.
     * @return String; the unique id for the query
     */
    public String getId()
    {
        return this.uniqueId.toString();
    }

    /**
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return connected whether the space-time regions are longitudinally connected
     */
    public boolean isConnected()
    {
        return this.connected;
    }

    /**
     * @return updateFrequency.
     */
    public Frequency getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    /**
     * @return interval.
     */
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * @return number of meta data entries
     */
    public int metaDataSize()
    {
        return this.metaDataSet.size();
    }

    /**
     * @return iterator over meta data entries, removal is not allowed
     */
    public Iterator<Entry<MetaDataType<?>, Set<?>>> getMetaDataSetIterator()
    {
        return this.metaDataSet.getMetaDataSetIterator();
    }

    /**
     * Defines a region in space and time for which this query is valid. All lanes in the link are included.
     * @param simulator simulator
     * @param link link
     * @param direction direction
     * @param xStart start position
     * @param xEnd end position
     * @param tStart start time
     * @param tEnd end time
     */
    public void addSpaceTimeRegionLink(final OTSDEVSSimulatorInterface simulator, final CrossSectionLink link,
            final GTUDirectionality direction, final Length xStart, final Length xEnd, final Duration tStart,
            final Duration tEnd)
    {
        for (Lane lane : link.getLanes())
        {
            Length x0 = new Length(lane.getLength().si * xStart.si / link.getLength().si, LengthUnit.SI);
            Length x1 = new Length(lane.getLength().si * xEnd.si / link.getLength().si, LengthUnit.SI);
            addSpaceTimeRegion(simulator, new LaneDirection(lane, direction), x0, x1, tStart, tEnd);
        }
    }

    /**
     * Defines a region in space and time for which this query is valid.
     * @param simulator simulator
     * @param laneDirection lane direction
     * @param xStart start position
     * @param xEnd end position
     * @param tStart start time
     * @param tEnd end time
     */
    public void addSpaceTimeRegion(final OTSDEVSSimulatorInterface simulator, final LaneDirection laneDirection,
            final Length xStart, final Length xEnd, final Duration tStart, final Duration tEnd)
    {
        SpaceTimeRegion spaceTimeRegion = new SpaceTimeRegion(laneDirection, xStart, xEnd, tStart, tEnd);
        this.sampling.registerSpaceTimeRegion(simulator, spaceTimeRegion);
        this.spaceTimeRegions.add(spaceTimeRegion);
        // TODO check on connectivity of laneDirections if connected==true, they should be given in order and be connected
        // through equal nodes
    }

    /**
     * @return number of space-time regions
     */
    public int spaceTimeRegionSize()
    {
        return this.spaceTimeRegions.size();
    }

    /**
     * @return iterator over space-time regions, removal is not allowed
     */
    public Iterator<SpaceTimeRegion> getSpaceTimeIterator()
    {
        return new ImmutableIterator<>(this.spaceTimeRegions.iterator());
    }

    /**
     * Returns a list of trajectories in accordance with the query. Each {@code Trajectories} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the meta
     * data of this query accepts the trajectory.
     * @param startTime start time of interval to get trajectories for
     * @param endTime start time of interval to get trajectories for
     * @param <T> underlying class of meta data type and its value
     * @return list of trajectories in accordance with the query
     * @throws RuntimeException if a meta data type returned a boolean array with incorrect length
     */
    @SuppressWarnings("unchecked")
    public <T> List<Trajectories> getTrajectories(final Duration startTime, final Duration endTime)
    {
        // Step 1) gather trajectories per GTU, truncated over space and time
        Map<String, TrajectoryAcceptList> trajectoryAcceptListMap = new HashMap<>();
        List<Trajectories> trajectoriesSet = new ArrayList<>();
        for (SpaceTimeRegion spaceTimeRegion : this.spaceTimeRegions)
        {
            Duration start = startTime.gt(spaceTimeRegion.getStartTime()) ? startTime : spaceTimeRegion.getStartTime();
            Duration end = endTime.lt(spaceTimeRegion.getEndTime()) ? endTime : spaceTimeRegion.getEndTime();
            Trajectories trajectories = this.sampling.getTrajectories(spaceTimeRegion.getLaneDirection())
                    .getTrajectories(spaceTimeRegion.getStartPosition(), spaceTimeRegion.getEndPosition(), start, end);
            for (Trajectory trajectory : trajectories.getTrajectorySet())
            {
                if (!trajectoryAcceptListMap.containsKey(trajectory.getGtuId()))
                {
                    trajectoryAcceptListMap.put(trajectory.getGtuId(), new TrajectoryAcceptList());
                }
                trajectoryAcceptListMap.get(trajectory.getGtuId()).addTrajectory(trajectory, trajectories);
            }
            trajectoriesSet.add(trajectories);
        }
        // Step 2) accept per GTU
        Iterator<String> iterator = trajectoryAcceptListMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String gtuId = iterator.next();
            TrajectoryAcceptList trajectoryAcceptListCombined = null;
            if (this.metaDataSet.size() == 0)
            {
                trajectoryAcceptListCombined = trajectoryAcceptListMap.get(gtuId);
                trajectoryAcceptListCombined.acceptAll();
                
            }
            for (MetaDataType<?> metaDataType : this.metaDataSet.getMetaDataTypes())
            {
                // create safe copy per meta data type, with defaults accepts = false
                TrajectoryAcceptList trajectoryAcceptList = trajectoryAcceptListMap.get(gtuId);
                TrajectoryAcceptList trajectoryAcceptListCopy = new TrajectoryAcceptList();
                for (int i = 0; i < trajectoryAcceptList.size(); i++)
                {
                    trajectoryAcceptListCopy.addTrajectory(trajectoryAcceptList.getTrajectory(i),
                            trajectoryAcceptList.getTrajectories(i));
                }
                // request meta data type to accept or reject
                ((MetaDataType<T>) metaDataType).accept(trajectoryAcceptListCopy,
                        (Set<T>) new HashSet<>(this.metaDataSet.get(metaDataType)));
                // combine acceptance/rejection of meta data type so far
                if (trajectoryAcceptListCombined == null)
                {
                    trajectoryAcceptListCombined = trajectoryAcceptListCopy;
                }
                else
                {
                    for (int i = 0; i < trajectoryAcceptListCopy.size(); i++)
                    {
                        Trajectory trajectory = trajectoryAcceptListCopy.getTrajectory(i);
                        trajectoryAcceptListCombined.acceptTrajectory(trajectory,
                                trajectoryAcceptListCombined.isAccepted(trajectory)
                                        && trajectoryAcceptListCopy.isAccepted(trajectory));
                    }
                }
            }
        }
        // Step 3) filter Trajectories
        List<Trajectories> out = new ArrayList<>();
        for (Trajectories full : trajectoriesSet)
        {
            Trajectories filtered = new Trajectories(full.getStartTime(), full.getLaneDirection());
            for (Trajectory trajectory : full.getTrajectorySet())
            {
                String gtuId = trajectory.getGtuId();
                if (trajectoryAcceptListMap.get(gtuId).isAccepted(trajectory))
                {
                    // TODO include points on boundaries with some input into the subSet method, there are 4 possibilities
                    // if trajectory is cut: add point on edge
                    // if first point and traj was longitudinally started, add point on edge interpolated to previous point
                    // if last point and traj was longitudinally ended, add point on edge interpolated with next point
                    filtered.addTrajectory(trajectory);
                }
            }
            out.add(filtered);
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.connected ? 1231 : 1237);
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.interval == null) ? 0 : this.interval.hashCode());
        result = prime * result + ((this.metaDataSet == null) ? 0 : this.metaDataSet.hashCode());
        result = prime * result + ((this.sampling == null) ? 0 : this.sampling.hashCode());
        result = prime * result + ((this.spaceTimeRegions == null) ? 0 : this.spaceTimeRegions.hashCode());
        result = prime * result + ((this.uniqueId == null) ? 0 : this.uniqueId.hashCode());
        result = prime * result + ((this.updateFrequency == null) ? 0 : this.updateFrequency.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
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
        Query other = (Query) obj;
        if (this.connected != other.connected)
        {
            return false;
        }
        if (this.description == null)
        {
            if (other.description != null)
            {
                return false;
            }
        }
        else if (!this.description.equals(other.description))
        {
            return false;
        }
        if (this.interval == null)
        {
            if (other.interval != null)
            {
                return false;
            }
        }
        else if (!this.interval.equals(other.interval))
        {
            return false;
        }
        if (this.metaDataSet == null)
        {
            if (other.metaDataSet != null)
            {
                return false;
            }
        }
        else if (!this.metaDataSet.equals(other.metaDataSet))
        {
            return false;
        }
        if (this.sampling == null)
        {
            if (other.sampling != null)
            {
                return false;
            }
        }
        else if (!this.sampling.equals(other.sampling))
        {
            return false;
        }
        if (this.spaceTimeRegions == null)
        {
            if (other.spaceTimeRegions != null)
            {
                return false;
            }
        }
        else if (!this.spaceTimeRegions.equals(other.spaceTimeRegions))
        {
            return false;
        }
        if (this.uniqueId == null)
        {
            if (other.uniqueId != null)
            {
                return false;
            }
        }
        else if (!this.uniqueId.equals(other.uniqueId))
        {
            return false;
        }
        if (this.updateFrequency == null)
        {
            if (other.updateFrequency != null)
            {
                return false;
            }
        }
        else if (!this.updateFrequency.equals(other.updateFrequency))
        {
            return false;
        }
        return true;
    }

}
