package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.function.Predicate;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedLaneBasedObject;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface AccelerationIncentive
{

    /**
     * Determine acceleration.
     * @param simplePlan simple plan to set the acceleration
     * @param lane lane on which to consider the acceleration
     * @param mergeDistance distance over which a lane change is impossible
     * @param gtu gtu
     * @param perception perception
     * @param carFollowingModel car-following model
     * @param speed current speed
     * @param params parameters
     * @param speedLimitInfo speed limit info
     * @throws ParameterException on missing parameter
     * @throws GtuException when there is a problem with the state of the GTU when planning a path
     */
    void accelerate(SimpleOperationalPlan simplePlan, RelativeLane lane, Length mergeDistance, LaneBasedGtu gtu,
            LanePerception perception, CarFollowingModel carFollowingModel, Speed speed, Parameters params,
            SpeedLimitInfo speedLimitInfo) throws ParameterException, GtuException;

    /**
     * Returns an iterable with only those lane-based objects that are on the route, accounting for longitudinal direction of
     * the GTU type.
     * @param iterable iterable
     * @param gtu gtu
     * @param <T> type of lane-based object
     * @return iterable with only those lane-based objects that are on the route
     */
    default <T extends PerceivedLaneBasedObject> Iterable<T> onRoute(final Iterable<T> iterable, final LaneBasedGtu gtu)
    {
        Route route = gtu.getStrategicalPlanner().getRoute();
        return new FilteredIterable<>(iterable, new Predicate<T>()
        {
            @Override
            public boolean test(final T t)
            {
                if (route == null)
                {
                    return true; // when there is no route, we are always on it...
                }
                Link link = t.getLane().getLink();
                if (route.contains(link.getStartNode()) && route.contains(link.getEndNode()))
                {
                    return route.indexOf(link.getEndNode()) - route.indexOf(link.getStartNode()) == 1;
                }
                return false;
            }
        });
    }

}
