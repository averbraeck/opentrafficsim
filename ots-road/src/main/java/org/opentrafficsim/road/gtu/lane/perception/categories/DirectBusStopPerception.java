package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.LaneBasedObjectIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneRecord;
import org.opentrafficsim.road.gtu.lane.perception.MultiLanePerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Bus stop perception.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
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

    /**
     * @param perception LanePerception; perception
     */
    public DirectBusStopPerception(final LanePerception perception)
    {
        super(perception);
    }

    /**
     * Returns bus stops.
     * @return PerceptionCollectable&lt;HeadwayBusStop, BusStop&gt;; bus stops
     */
    public final PerceptionCollectable<HeadwayBusStop, BusStop> computeBusStops()
    {
        try
        {
            Route route = getGtu().getStrategicalPlanner().getRoute();
            MultiLanePerceptionIterable<HeadwayBusStop, BusStop> stops = new MultiLanePerceptionIterable<>(getGtu());
            for (RelativeLane lane : getPerception().getLaneStructure().getExtendedCrossSection())
            {
                LaneRecord<?> record = getPerception().getLaneStructure().getFirstRecord(lane);
                Length pos = record.getStartDistance().neg();
                pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                        : pos.minus(getGtu().getFront().getDx());
                AbstractPerceptionIterable<HeadwayBusStop, BusStop,
                        ?> it = new LaneBasedObjectIterable<HeadwayBusStop, BusStop>(getGtu(), BusStop.class, record,
                                Length.max(Length.ZERO, pos), true, getGtu().getParameters().getParameter(LOOKAHEAD),
                                getGtu().getFront(), route)
                        {
                            /** {@inheritDoc} */
                            @Override
                            public HeadwayBusStop perceive(final LaneBasedGTU perceivingGtu, final BusStop busStop,
                                    final Length distance)
                            {
                                Set<String> conflictIds = new LinkedHashSet<>();
                                for (Conflict conflict : busStop.getConflicts())
                                {
                                    conflictIds.add(conflict.getId());
                                }
                                return Try.assign(
                                        () -> new HeadwayBusStop(busStop, distance, lane, conflictIds, busStop.getLane()),
                                        "Exception while creating bus stop headway.");
                            }
                        };
                stops.addIterable(lane, it);
            }
            return stops;
        }
        catch (GTUException | ParameterException exception)
        {
            throw new RuntimeException("Unexpected exception while perceiving bus stops.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayBusStop, BusStop> getBusStops()
    {
        return this.computeIfAbsent("busStops", () -> computeBusStops());
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        // lazy evaluation
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectBusStopPerception";
    }

}
