package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.LaneBasedObjectIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.MultiLanePerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Bus stop perception.
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

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Bus stops. */
    private TimeStampedObject<PerceptionIterable<HeadwayBusStop>> busStops;

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
        Route route = getPerception().getGtu().getStrategicalPlanner().getRoute();
        MultiLanePerceptionIterable<HeadwayBusStop> stops = new MultiLanePerceptionIterable<>();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
        {
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos = record.getStartDistance().neg();
            pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                    : pos.minus(getGtu().getFront().getDx());
            Iterable<HeadwayBusStop> it = new LaneBasedObjectIterable<HeadwayBusStop, BusStop, LaneStructureRecord>(
                    BusStop.class, record, Length.max(Length.ZERO, pos), getGtu().getParameters().getParameter(LOOKAHEAD),
                    getGtu().getFront(), route)
            {
                /** {@inheritDoc} */
                @Override
                protected HeadwayBusStop createHeadway(final BusStop busStop, final Length distance)
                        throws ParameterException, GTUException
                {
                    Set<String> conflictIds = new HashSet<>();
                    for (Conflict conflict : busStop.getConflicts())
                    {
                        conflictIds.add(conflict.getId());
                    }
                    return new HeadwayBusStop(busStop, distance, lane, conflictIds);
                }
            };
            stops.addIterable(lane, it);
        }
        this.busStops = new TimeStampedObject<>(stops, getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionIterable<HeadwayBusStop> getBusStops()
    {
        return this.busStops.getObject();
    }

    /**
     * Returns the time stamped bus stops.
     * @return time stamped bus stops
     */
    public final TimeStampedObject<PerceptionIterable<HeadwayBusStop>> getTimeStampedBusStops()
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
