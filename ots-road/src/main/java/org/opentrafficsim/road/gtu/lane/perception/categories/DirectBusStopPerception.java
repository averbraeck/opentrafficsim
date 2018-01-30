package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructure.Entry;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectBusStopPerception extends LaneBasedAbstractPerceptionCategory implements BusStopPerception
{

    /** */
    private static final long serialVersionUID = 20170127;

    /** Bus stops. */
    private TimeStampedObject<SortedSet<HeadwayBusStop>> busStops;

    /**
     * @param perception perception
     */
    public DirectBusStopPerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateBusStops() throws GTUException, ParameterException
    {
        SortedSet<HeadwayBusStop> stops = new TreeSet<>();
        Map<RelativeLane, SortedSet<Entry<BusStop>>> map = getPerception().getLaneStructure().getDownstreamObjectsOnRoute(
                BusStop.class, getGtu(), RelativePosition.FRONT, getGtu().getStrategicalPlanner().getRoute());
        for (RelativeLane relativeLane : map.keySet())
        {
            for (Entry<BusStop> entry : map.get(relativeLane))
            {
                Set<String> conflictIds = new HashSet<>();
                for (Conflict conflict : entry.getLaneBasedObject().getConflicts())
                {
                    conflictIds.add(conflict.getId());
                }
                stops.add(new HeadwayBusStop(entry.getLaneBasedObject(), entry.getDistance(), relativeLane, conflictIds));
            }
        }
        this.busStops = new TimeStampedObject<>(stops, getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayBusStop> getBusStops()
    {
        return this.busStops.getObject();
    }

    /**
     * Returns the time stamped bus stops.
     * @return time stamped bus stops
     */
    public final TimeStampedObject<SortedSet<HeadwayBusStop>> getTimeStampedBusStops()
    {
        return this.busStops;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectBusStopPerception";
    }

}
