package org.opentrafficsim.road.gtu.perception.categories;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.PerceptionReiterable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.object.LaneBasedObject;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;

/**
 * Perceives traffic lights and intersection conflicts.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Iterable<Entry<TrafficLight>> iterable = Try.assign(() ->
        {
            return getPerception().getLaneStructure().getDownstreamObjects(lane, TrafficLight.class, RelativePosition.FRONT,
                    true);
        }, "Unable to get downstream traffic lights from LaneStructure");
        LaneBasedGtu gtu = getPerception().getGtu();
        Route route = gtu.getStrategicalPlanner().getRoute().orElse(null);
        return new PerceptionReiterable<>(gtu, iterable, (trafficLight, distance) ->
        {
            return new PerceivedTrafficLight(trafficLight, distance, trafficLight.canTurnOnRed(route, gtu.getType()));
        });
    }

    /**
     * Compute conflicts.
     * @param lane lane
     * @return PerceptionCollectable of conflicts
     */
    private PerceptionCollectable<PerceivedConflict, Conflict> computeConflicts(final RelativeLane lane)
    {
        Iterable<Entry<Conflict>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamObjects(lane,
                Conflict.class, RelativePosition.FRONT, true), "Unable to get downstream conflicts from LaneStructure");
        return new PerceptionReiterable<LaneBasedObject, PerceivedConflict, Conflict>(getGtu(), iterable,
                (conflict, distance) ->
                {
                    Length lookAhead = getGtu().getParameters().getOptionalParameter(LOOKAHEAD)
                            .orElseThrow(() -> new OtsRuntimeException("Parameter Look-ahead not present."));
                    return PerceivedConflict.of(getGtu(), conflict, DirectIntersectionPerception.this.perceptionGtuType,
                            distance, lookAhead);
                });
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
            throw new OtsRuntimeException("Unexpected exception while computing conflict alongside.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "DirectIntersectionPerception " + cacheAsString();
    }

}
