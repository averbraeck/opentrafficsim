package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.object.PerceivedLaneBasedObject;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;

/**
 * Interface for acceleration incentives.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface AccelerationIncentive
{

    /** Field to return when there is no reason to accelerate/decelerate. */
    Acceleration NO_REASON = Acceleration.POS_MAXVALUE;

    /**
     * Determine acceleration.
     * @param context tactical information such as parameters and car-following model
     * @param lane lane on which to consider the acceleration
     * @param mergeDistance distance over which a lane change is impossible
     * @return acceleration
     * @throws ParameterException on missing parameter
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     */
    Acceleration accelerate(TacticalContextEgo context, RelativeLane lane, Length mergeDistance)
            throws ParameterException, GtuException;

    /**
     * Returns an iterable with only those lane-based objects that are on the same road, i.e. only downstream of a possible
     * merge.
     * @param <T> type of lane-based object
     * @param iterable iterable
     * @param lane lane of objects
     * @param mergeDistance distance within which the lane and current lane merge
     * @return iterable with only those lane-based objects that are on the route
     * @throws NullPointerException when any input is {@code null}
     */
    static <T extends PerceivedLaneBasedObject> Iterable<T> onRoad(final Iterable<T> iterable, final RelativeLane lane,
            final Length mergeDistance)
    {
        Throw.whenNull(lane, "lane");
        Throw.whenNull(mergeDistance, "mergeDistance");
        if (!lane.isCurrent() && mergeDistance.gt0())
        {
            return new FilteredIterable<>(iterable, (trafficLight) ->
            {
                return trafficLight.getDistance().gt(mergeDistance);
            });
        }
        return iterable;
    }

    /**
     * Returns an iterable with only those lane-based objects that are on the route, accounting for longitudinal direction of
     * the GTU type.
     * @param <T> type of lane-based object
     * @param iterable iterable
     * @param gtu gtu
     * @return iterable with only those lane-based objects that are on the route
     * @throws NullPointerException when any input is {@code null}
     */
    static <T extends PerceivedLaneBasedObject> Iterable<T> onRoute(final Iterable<T> iterable, final LaneBasedGtu gtu)
    {
        Throw.whenNull(gtu, "gtu");
        Optional<Route> route = gtu.getStrategicalPlanner().getRoute();
        return new FilteredIterable<>(iterable, (t) ->
        {
            if (route.isEmpty())
            {
                return true; // when there is no route, we are always on it...
            }
            Link link = t.getLane().getLink();
            if (route.get().contains(link.getStartNode()) && route.get().contains(link.getEndNode()))
            {
                return route.get().indexOf(link.getEndNode()) - route.get().indexOf(link.getStartNode()) == 1;
            }
            return false;
        });
    }

}
