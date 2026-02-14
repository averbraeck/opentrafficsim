package org.opentrafficsim.road.gtu.perception.mental.channel;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
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
import org.opentrafficsim.road.gtu.perception.categories.DirectIntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.PerceivedGtuType.AnticipationPerceivedGtuType;
import org.opentrafficsim.road.gtu.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLightChannel;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;

/**
 * This class is highly similar to {@link DirectIntersectionPerception} but takes perception delay for conflicting GTUs and
 * traffic light colors from perception channels.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IntersectionPerceptionChannel extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements IntersectionPerception
{

    /** Mental module. */
    private final ChannelMental mental;

    /** Estimation. */
    private final Estimation estimation;

    /** Anticipation. */
    private final Anticipation anticipation;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /**
     * Constructor.
     * @param perception perception.
     * @param estimation estimation.
     * @param anticipation anticipation.
     */
    public IntersectionPerceptionChannel(final LanePerception perception, final Estimation estimation,
            final Anticipation anticipation)
    {
        super(perception);
        Throw.when(!(getPerception().getMental().get() instanceof ChannelMental), IllegalArgumentException.class,
                "Mental module is not channel based.");
        this.mental = (ChannelMental) getPerception().getMental().get();
        this.estimation = estimation;
        this.anticipation = anticipation;
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
        Route route =
                Try.assign(() -> getPerception().getGtu().getStrategicalPlanner().getRoute().get(), "Unable to obtain route");
        return new PerceptionReiterable<>(getGtu(), iterable, (trafficLight, distance) ->
        {
            return new PerceivedTrafficLightChannel(trafficLight, distance,
                    trafficLight.canTurnOnRed(route, getGtu().getType()),
                    () -> IntersectionPerceptionChannel.this.mental.getPerceptionDelay(ChannelTask.FRONT));
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
        return new PerceptionReiterable<>(getGtu(), iterable, (conflict, distance) ->
        {
            Length lookAhead =
                    Try.assign(() -> getGtu().getParameters().getParameter(LOOKAHEAD), "Parameter LOOKAHEAD not present.");
            // TODO visibility
            Length conflictingVisibility = lookAhead;
            PerceivedConflict perceivedConflict;
            PerceivedGtuType perceivedGtuType = new AnticipationPerceivedGtuType(IntersectionPerceptionChannel.this.estimation,
                    IntersectionPerceptionChannel.this.anticipation,
                    () -> IntersectionPerceptionChannel.this.mental.getPerceptionDelay(conflict));
            perceivedConflict = PerceivedConflict.of(getGtu(), conflict, perceivedGtuType, distance, conflictingVisibility);
            return perceivedConflict;
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
            throw new RuntimeException("Unexpected exception while computing conflict alongside.", exception);
        }
    }

    @Override
    public final String toString()
    {
        return "IntersectionPerceptionChannel " + cacheAsString();
    }

}
