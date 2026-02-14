package org.opentrafficsim.road.gtu.perception.categories;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.MultiLanePerceptionIterable;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.PerceptionReiterable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.object.PerceivedBusStop;
import org.opentrafficsim.road.gtu.perception.structure.LaneRecord;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.object.BusStop;

/**
 * Bus stop perception.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectBusStopPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements BusStopPerception
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /**
     * Constructor.
     * @param perception perception
     */
    public DirectBusStopPerception(final LanePerception perception)
    {
        super(perception);
    }

    @Override
    public final PerceptionCollectable<PerceivedBusStop, BusStop> getBusStops()
    {
        return this.computeIfAbsent("busStops", () -> computeBusStops());
    }

    /**
     * Returns bus stops.
     * @return bus stops
     */
    public final PerceptionCollectable<PerceivedBusStop, BusStop> computeBusStops()
    {
        try
        {
            MultiLanePerceptionIterable<LaneBasedGtu, PerceivedBusStop, BusStop> stops =
                    new MultiLanePerceptionIterable<>(getGtu());
            for (RelativeLane lane : getPerception().getLaneStructure().getRootCrossSection())
            {
                Iterable<Entry<BusStop>> busStops = getPerception().getLaneStructure().getDownstreamObjects(lane, BusStop.class,
                        RelativePosition.FRONT, true);

                LaneRecord record = getPerception().getLaneStructure().getRootRecord(lane);
                Length pos = record.getStartDistance().neg();
                pos = pos.plus(getGtu().getFront().dx());

                PerceptionReiterable<LaneBasedGtu, PerceivedBusStop, BusStop> it =
                        new PerceptionReiterable<>(getGtu(), busStops, (object, distance) ->
                        {
                            Set<String> conflictIds = new LinkedHashSet<>();
                            for (Conflict conflict : object.getConflicts())
                            {
                                conflictIds.add(conflict.getId());
                            }
                            return new PerceivedBusStop(object, distance, lane, conflictIds, object.getLane());
                        });

                stops.addIterable(lane, it);
            }
            return stops;
        }
        catch (ParameterException exception)
        {
            throw new OtsRuntimeException("Unexpected exception while perceiving bus stops.");
        }
    }

    @Override
    public final String toString()
    {
        return "DirectBusStopPerception";
    }

}
