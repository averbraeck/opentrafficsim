package org.opentrafficsim.road.network.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
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
    /** unique id.  */
    private UUID uniqueId = UUID.randomUUID();
    
    /** Sampling. */
    private final Sampling sampling;

    /** Whether the space-time regions are longitudinally connected. */
    private final boolean connected;

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion> spaceTimeRegions = new ArrayList<>();

    /** Meta data. */
    private final MetaData metaData;

    /**
     * @param sampling sampling
     * @param connected whether the space-time regions are longitudinally connected
     * @param metaData meta data
     */
    public Query(final Sampling sampling, final boolean connected, final MetaData metaData)
    {
        this.sampling = sampling;
        this.connected = connected;
        this.metaData = new MetaData(metaData);
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
     * @return connected whether the space-time regions are longitudinally connected
     */
    public boolean isConnected()
    {
        return this.connected;
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
     * Accepts a trajectory if the values of meta data types specified in this query are equal to the values of the meta data
     * types in the trajectory. If the trajectory does not contain a certain meta data type specified in this query, the
     * trajectory is not accepted. Any additional meta data types in the trajectory are ignored.
     * @param trajectory trajectory
     * @return whether the trajectory is accepted for this query
     */
    public boolean accept(final Trajectory trajectory)
    {
        for (MetaDataType<?> metaDataType : this.metaData.getMetaDataTypes())
        {
            if (!trajectory.contains(metaDataType)
                || !trajectory.getMetaData(metaDataType).equals(this.metaData.get(metaDataType)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of trajectories in accordance with the query. Each {@code Trajectories} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if its meta data
     * complies with the meta data of this query.
     * @return list of trajectories in accordance with the query
     */
    public List<Trajectories> getTrajectories()
    {
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
                if (accept(trajectory))
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
