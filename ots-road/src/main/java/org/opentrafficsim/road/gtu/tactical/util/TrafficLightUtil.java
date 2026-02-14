package org.opentrafficsim.road.gtu.tactical.util;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.lmrs.AccelerationIncentive;

/**
 * Static methods regarding traffic lights for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class TrafficLightUtil
{

    /** Maximum deceleration for stopping for yellow traffic light. */
    public static final ParameterTypeAcceleration BCRIT = ParameterTypes.BCRIT;

    /** Car-following stopping distance. */
    public static final ParameterTypeLength S0 = ParameterTypes.S0;

    /**
     * Do not instantiate.
     */
    private TrafficLightUtil()
    {
        //
    }

    /**
     * Returns an acceleration as response to a set of traffic lights, being positive infinity if ignored. The response is
     * governed by the car-following model in case a traffic light is yellow or red. A constant deceleration to stop is also
     * calculated, and the highest acceleration of both is used. If this value is below -bYellow (B_YELLOW), the traffic light
     * is ignored, which usually occurs only during the yellow phase. By using the highest acceleration of the car-following
     * model and the constant deceleration, it is ensured that comfortable deceleration is applied if approaching a red traffic
     * light from far away, while strong deceleration is only applied if required and appropriately represents stopping for
     * yellow.
     * @param context tactical information such as parameters and car-following model
     * @param lane lane
     * @param mergeDistance distance along which no lane changes can be performed towards the lane
     * @param onRoute filter conflicts to only include conflict on the route
     * @return acceleration as response to a traffic light, being positive infinity if ignored
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException if intersection perception category does not exist
     * @throws NullPointerException if any input is {@code null}
     * @throws IllegalArgumentException if the traffic light is not downstream
     */
    public static Acceleration respondToTrafficLights(final TacticalContextEgo context, final RelativeLane lane,
            final Length mergeDistance, final boolean onRoute) throws ParameterException, OperationalPlanException
    {
        Throw.whenNull(context, "context");
        Iterable<PerceivedTrafficLight> headwayTrafficLights = context.getPerception()
                .getPerceptionCategory(IntersectionPerception.class).getTrafficLights(RelativeLane.CURRENT);
        headwayTrafficLights = AccelerationIncentive.onRoad(headwayTrafficLights, lane, mergeDistance);
        if (onRoute)
        {
            headwayTrafficLights = AccelerationIncentive.onRoute(headwayTrafficLights, context.getGtu());
        }
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (PerceivedTrafficLight headwayTrafficLight : headwayTrafficLights)
        {
            Acceleration aLight = respondToTrafficLight(context, headwayTrafficLight);
            a = Acceleration.min(a, aLight);
        }
        return a;
    }

    /**
     * Returns an acceleration as response to a traffic light, being positive infinity if ignored. The response is governed by
     * the car-following model in case the traffic light is yellow or red. A constant deceleration to stop is also calculated,
     * and the highest acceleration of both is used. If this value is below -bYellow (B_YELLOW), the traffic light is ignored,
     * which usually occurs only during the yellow phase. By using the highest acceleration of the car-following model and the
     * constant deceleration, it is ensured that comfortable deceleration is applied if approaching a red traffic light from far
     * away, while strong deceleration is only applied if required and appropriately represents stopping for yellow.
     * @param context tactical information such as parameters and car-following model
     * @param headwayTrafficLight headway traffic light
     * @return acceleration as response to a traffic light, being positive infinity if ignored
     * @throws ParameterException if a parameter is not defined
     * @throws NullPointerException if any input is {@code null}
     * @throws IllegalArgumentException if the traffic light is not downstream
     */
    // @docs/06-behavior/tactical-planner/#modular-utilities
    public static Acceleration respondToTrafficLight(final TacticalContextEgo context,
            final PerceivedTrafficLight headwayTrafficLight) throws ParameterException
    {
        Throw.whenNull(context, "context");
        Throw.whenNull(headwayTrafficLight, "headwayTrafficLight");
        if ((headwayTrafficLight.getTrafficLightColor().isRed() || headwayTrafficLight.getTrafficLightColor().isYellow())
                && !headwayTrafficLight.canTurnOnRed())
        {
            // deceleration from car-following model
            Acceleration a = CarFollowingUtil.followSingleLeader(context, headwayTrafficLight);
            // compare to constant deceleration
            Length s0 = context.getParameters().getParameter(S0);
            if (headwayTrafficLight.getDistance().gt(s0)) // constant acceleration not applicable if within s0
            {
                // constant acceleration is -.5*v^2/s, where s = distance-s0 > 0
                Acceleration aConstant = CarFollowingUtil.constantAccelerationStop(context, headwayTrafficLight.getDistance());
                a = Acceleration.max(a, aConstant);
            }
            // return a if a > -bCrit
            if (a.gt(context.getParameters().getParameter(BCRIT).neg()))
            {
                return a;
            }
        }
        // ignore traffic light
        return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
    }

}
