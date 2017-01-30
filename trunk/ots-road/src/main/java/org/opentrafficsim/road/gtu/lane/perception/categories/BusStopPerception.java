package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructure.Entry;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class BusStopPerception extends LaneBasedAbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20170127;

    /** Bus stops. */
    private TimeStampedObject<SortedSet<HeadwayBusStop>> busStops;

    /**
     * @param perception perception
     */
    public BusStopPerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateBusStops();
    }

    /**
     * Updates the bus stops.
     * @throws GTUException if the GTU has not been initialized
     * @throws ParameterException if lane structure cannot be made due to missing parameter
     */
    public final void updateBusStops() throws GTUException, ParameterException
    {
        SortedSet<HeadwayBusStop> stops = new TreeSet<>();
        Map<RelativeLane, SortedSet<Entry<BusStop>>> map = getPerception().getLaneStructure().getDownstreamObjectsOnRoute(
                BusStop.class, getGtu(), RelativePosition.FRONT, getGtu().getStrategicalPlanner().getRoute());
        for (RelativeLane relativeLane : map.keySet())
        {
            for (Entry<BusStop> entry : map.get(relativeLane))
            {
                stops.add(new HeadwayBusStop(entry.getLaneBasedObject(), entry.getDistance(), relativeLane));
            }
        }
        this.busStops = new TimeStampedObject<>(stops, getTimestamp());
    }

    /**
     * Returns the bus stops.
     * @return bus stops
     */
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

}
