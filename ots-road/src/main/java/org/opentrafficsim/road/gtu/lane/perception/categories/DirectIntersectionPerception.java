package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectIntersectionPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements IntersectionPerception
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Headway GTU type that should be used. */
    private final PerceivedGtuType perceptionGtuType;

    /**
     * Constructor.
     * @param perception perception
     * @param headwayGtuType type of headway gtu to generate
     */
    public DirectIntersectionPerception(final LanePerception perception, final PerceivedGtuType headwayGtuType)
    {
        super(perception);
        this.perceptionGtuType = headwayGtuType;
    }

    @Override
    public final PerceptionCollectable<PerceivedTrafficLight, TrafficLight> getTrafficLights(final RelativeLane lane)
    {
        return computeIfAbsent("trafficLights", () -> computeTrafficLights(lane), lane);
    }

    @Override
    public final PerceptionCollectable<PerceivedConflict, Conflict> getConflicts(final RelativeLane lane)
    {
        return computeIfAbsent("conflicts", () -> computeConflicts(lane), lane);
    }

    @Override
    public final boolean isAlongsideConflictLeft()
    {
        return computeIfAbsent("alongside", () -> computeConflictAlongside(LateralDirectionality.LEFT),
                LateralDirectionality.LEFT);
    }

    @Override
    public final boolean isAlongsideConflictRight()
    {
        return computeIfAbsent("alongside", () -> computeConflictAlongside(LateralDirectionality.RIGHT),
                LateralDirectionality.RIGHT);
    }

    /**
     * Compute traffic lights.
     * @param lane lane
     * @return PerceptionCollectable of traffic lights
     */
    private PerceptionCollectable<PerceivedTrafficLight, TrafficLight> computeTrafficLights(final RelativeLane lane)
    {
        Iterable<Entry<TrafficLight>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamObjects(lane,
                TrafficLight.class, RelativePosition.FRONT, true), "");
        Route route = Try.assign(() -> getPerception().getGtu().getStrategicalPlanner().getRoute(), "");
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<TrafficLight>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, PerceivedTrafficLight,
                            TrafficLight>.PrimaryIteratorEntry next()
                    {
                        Entry<TrafficLight> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected PerceivedTrafficLight perceive(final TrafficLight trafficLight, final Length distance)
                    throws GtuException, ParameterException
            {
                return new PerceivedTrafficLight(trafficLight, distance,
                        trafficLight.canTurnOnRed(route, getPerception().getGtu().getType()));
            }
        };
    }

    /**
     * Compute conflicts.
     * @param lane lane
     * @return PerceptionCollectable of conflicts
     */
    private PerceptionCollectable<PerceivedConflict, Conflict> computeConflicts(final RelativeLane lane)
    {
        Iterable<Entry<Conflict>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamObjects(lane,
                Conflict.class, RelativePosition.FRONT, true), "");
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<Conflict>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, PerceivedConflict, Conflict>.PrimaryIteratorEntry next()
                    {
                        Entry<Conflict> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected PerceivedConflict perceive(final Conflict conflict, final Length distance)
                    throws GtuException, ParameterException
            {
                Length lookAhead = Try.assign(() -> getObject().getParameters().getParameter(LOOKAHEAD),
                        "Parameter Look-ahead not present.");
                return PerceivedConflict.of(getGtu(), conflict, DirectIntersectionPerception.this.perceptionGtuType, distance,
                        lookAhead);
            }
        };
    }

    /**
     * Compute whether there is a conflict alongside.
     * @param lat lateral directionality
     * @return whether there is a conflict alongside
     */
    private boolean computeConflictAlongside(final LateralDirectionality lat)
    {
        try
        {
            RelativeLane lane = new RelativeLane(lat, 1);
            if (getPerception().getLaneStructure().exists(lane))
            {
                Iterator<Entry<Conflict>> conflicts = getPerception().getLaneStructure()
                        .getUpstreamObjects(lane, Conflict.class, RelativePosition.FRONT).iterator();
                if (conflicts.hasNext())
                {
                    Entry<Conflict> entry = conflicts.next();
                    return entry.distance().si < entry.object().getLength().si + getGtu().getLength().si;
                }
            }
            return false;
        }
        catch (ParameterException exception)
        {
            throw new RuntimeException("Unexpected exception while computing conflict alongside.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "DirectIntersectionPerception " + cacheAsString();
    }

}
