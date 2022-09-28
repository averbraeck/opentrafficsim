package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.function.Predicate;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayLaneBasedObject;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface AccelerationIncentive
{

    /**
     * Determine acceleration.
     * @param simplePlan SimpleOperationalPlan; simple plan to set the acceleration
     * @param lane RelativeLane; lane on which to consider the acceleration
     * @param mergeDistance Length; distance over which a lane change is impossible
     * @param gtu LaneBasedGTU; gtu
     * @param perception LanePerception; perception
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param speed Speed; current speed
     * @param params Parameters; parameters
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @throws OperationalPlanException in case of an error
     * @throws ParameterException on missing parameter
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     */
    void accelerate(SimpleOperationalPlan simplePlan, RelativeLane lane, Length mergeDistance, LaneBasedGTU gtu,
            LanePerception perception, CarFollowingModel carFollowingModel, Speed speed, Parameters params,
            SpeedLimitInfo speedLimitInfo) throws OperationalPlanException, ParameterException, GTUException;

    /**
     * Returns an iterable with only those lane-based objects that are on the route, accounting for longitudinal direction of
     * the GTU type.
     * @param iterable Iterable&lt;T&gt;; iterable
     * @param gtu LaneBasedGTU; gtu
     * @param <T> type of lane-based object
     * @return Iterable&lt;T&gt;; iterable with only those lane-based objects that are on the route
     */
    default <T extends HeadwayLaneBasedObject> Iterable<T> onRoute(final Iterable<T> iterable, final LaneBasedGTU gtu)
    {
        Route route = gtu.getStrategicalPlanner().getRoute();
        return new FilteredIterable<>(iterable, new Predicate<T>()
        {
            /** {@inheritDoc} */
            @Override
            public boolean test(final T t)
            {
                if (route == null)
                {
                    return true; // when there is no route, we are always on it...
                }
                OTSLink link = t.getLane().getParentLink();
                if (route.contains(link.getStartNode()) && route.contains(link.getEndNode()))
                {
                    LongitudinalDirectionality dir = link.getDirectionality(gtu.getGTUType());
                    if (dir.isNone())
                    {
                        return false;
                    }
                    if (dir.isBoth())
                    {
                        return Math.abs(route.indexOf(link.getStartNode()) - route.indexOf(link.getEndNode())) == 1;
                    }
                    if (dir.isForward())
                    {
                        return route.indexOf(link.getEndNode()) - route.indexOf(link.getStartNode()) == 1;
                    }
                    return route.indexOf(link.getStartNode()) - route.indexOf(link.getEndNode()) == 1;
                }
                return false;
            }
        });
    }

}
