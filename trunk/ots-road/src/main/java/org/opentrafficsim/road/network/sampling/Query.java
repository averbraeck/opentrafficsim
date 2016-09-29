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

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.immutablecollections.ImmutableIterator;
import org.opentrafficsim.road.network.lane.LaneDirection;

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

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion> spaceTimeRegions = new ArrayList<>();

    /**
     * @param sampling sampling
     * @param description description
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaDataSet meta data
     */
    public Query(final Sampling sampling, final String description, final boolean connected, final MetaDataSet metaDataSet)
    {
        this.sampling = sampling;
        this.connected = connected;
        this.metaDataSet = new MetaDataSet(metaDataSet);
        this.description = description;
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
     * Defines a region in space and time for which this query is valid.
     * @param simulator simulator
     * @param laneDirection lane direction
     * @param xStart start position
     * @param xEnd end position
     * @param tStart start time
     * @param tEnd end time
     */
    public void addSpaceTimeRegion(final OTSSimulatorInterface simulator, final LaneDirection laneDirection,
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
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if its meta data
     * complies with the meta data of this query.
     * @param <T> underlying class of meta data type and its value
     * @return list of trajectories in accordance with the query
     * @throws RuntimeException if a meta data type returned a boolean array with incorrect length
     */
    public <T> List<Trajectories> getTrajectories()
    {
        // Step 1) gather trajectories per GTU
        Map<String, List<Trajectory>> trajectoryMap = new HashMap<>();
        Map<String, List<Trajectories>> trajectoriesMap = new HashMap<>();
        for (SpaceTimeRegion spaceTimeRegion : this.spaceTimeRegions)
        {
            Trajectories trajectories = this.sampling.getTrajectories(spaceTimeRegion.getLaneDirection());
            for (Trajectory trajectory : trajectories.getTrajectories())
            {
                if (!trajectoryMap.containsKey(trajectory.getGtuId()))
                {
                    trajectoryMap.put(trajectory.getGtuId(), new ArrayList<>());
                    trajectoriesMap.put(trajectory.getGtuId(), new ArrayList<>());
                }
                trajectoryMap.get(trajectory.getGtuId()).add(trajectory);
                trajectoriesMap.get(trajectory.getGtuId()).add(trajectories);
            }
        }
        // Step 2) accept per GTU
        Map<String, Boolean[]> booleanMap = new HashMap<>();
        Iterator<String> iterator = trajectoryMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String gtuId = iterator.next();
            Boolean[] accepted = new Boolean[trajectoryMap.get(gtuId).size()];
            for (MetaDataType<?> metaDataType : this.metaDataSet.getMetaDataTypes())
            {
                // create safe copies as meta data types may use the list to throw out rejected trajectories etc.
                @SuppressWarnings("unchecked")
                boolean[] acceptedTmp =
                    ((MetaDataType<T>) metaDataType).accept(new ArrayList<>(trajectoryMap.get(gtuId)), new ArrayList<>(
                        trajectoriesMap.get(gtuId)), (Set<T>) new HashSet<>(this.metaDataSet.get(metaDataType)));
                Throw.when(acceptedTmp.length != accepted.length, RuntimeException.class,
                    "Meta data type %s returned a boolean array with incorrect length.", metaDataType);
                for (int i = 0; i < accepted.length; i++)
                {
                    accepted[i] = accepted[i] && acceptedTmp[i];
                }
            }
            booleanMap.put(gtuId, accepted);
        }
        // Step 3) filter Trajectories
        List<Trajectories> out = new ArrayList<>();
        for (SpaceTimeRegion spaceTimeRegion : this.spaceTimeRegions)
        {
            Trajectories full = this.sampling.getTrajectories(spaceTimeRegion.getLaneDirection());
            Duration startTime =
                spaceTimeRegion.getStartTime().lt(full.getStartTime()) ? spaceTimeRegion.getStartTime() : full
                    .getStartTime();
            Trajectories filtered = new Trajectories(startTime, spaceTimeRegion.getLaneDirection());
            for (Trajectory trajectory : full.getTrajectories())
            {
                String gtuId = trajectory.getGtuId();
                if (booleanMap.get(gtuId)[trajectoryMap.get(gtuId).indexOf(trajectory)])
                {
                    // TODO include points on boundaries with some input into the subSet method, there are 4 possibilities
                    // if trajectory is cut: add point on edge
                    // if first point and traj was longitudinally started, add point on edge interpolated to previous point
                    // if last point and traj was longitudinally ended, add point on edge interpolated with next point
                    Trajectory trajectoryTmp =
                        trajectory.subSet(spaceTimeRegion.getStartPosition(), spaceTimeRegion.getEndPosition(),
                            spaceTimeRegion.getStartTime(), spaceTimeRegion.getEndTime());
                    if (trajectoryTmp.size() > 0)
                    {
                        filtered.addTrajectory(trajectoryTmp);
                    }
                }
            }
            out.add(filtered);
        }
        return out;
    }

}
