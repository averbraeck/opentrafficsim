package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.MultiLanePerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedBusStop;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneRecord;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Bus stop perception.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectBusStopPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements BusStopPerception
{

    /** */
    private static final long serialVersionUID = 20170127;

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
                Iterable<
                        Entry<BusStop>> busStops = getPerception().getLaneStructure().getDownstreamObjects(lane, BusStop.class,
                        RelativePosition.FRONT, true);

                LaneRecord record = getPerception().getLaneStructure().getRootRecord(lane);
                Length pos = record.getStartDistance().neg();
                pos = pos.plus(getGtu().getFront().dx());

                AbstractPerceptionReiterable<LaneBasedGtu, PerceivedBusStop, BusStop> it =
                        new AbstractPerceptionReiterable<LaneBasedGtu, PerceivedBusStop, BusStop>(getGtu())
                        {
                            @Override
                            protected Iterator<AbstractPerceptionReiterable<LaneBasedGtu, PerceivedBusStop,
                                    BusStop>.PrimaryIteratorEntry> primaryIterator()
                            {
                                Iterator<Entry<BusStop>> iterator = busStops.iterator();
                                return new Iterator<>()
                                {
                                    @Override
                                    public boolean hasNext()
                                    {
                                        return iterator.hasNext();
                                    }

                                    @Override
                                    public AbstractPerceptionReiterable<LaneBasedGtu, PerceivedBusStop,
                                            BusStop>.PrimaryIteratorEntry next()
                                    {
                                        Entry<BusStop> entry = iterator.next();
                                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                                    }
                                };
                            }

                            @Override
                            protected PerceivedBusStop perceive(final BusStop object, final Length distance)
                                    throws GtuException, ParameterException
                            {
                                Set<String> conflictIds = new LinkedHashSet<>();
                                for (Conflict conflict : object.getConflicts())
                                {
                                    conflictIds.add(conflict.getId());
                                }
                                return Try.assign(
                                        () -> new PerceivedBusStop(object, distance, lane, conflictIds, object.getLane()),
                                        "Exception while creating bus stop headway.");
                            }
                        };
                stops.addIterable(lane, it);
            }
            return stops;
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Unexpected exception while perceiving bus stops.");
        }
    }

    @Override
    public final String toString()
    {
        return "DirectBusStopPerception";
    }

}
