package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Acceleration incentive for traffic lights.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AccelerationTrafficLights implements AccelerationIncentive, Stateless<AccelerationTrafficLights>
{

    /** Singleton instance. */
    public static final AccelerationTrafficLights SINGLETON = new AccelerationTrafficLights();

    @Override
    public AccelerationTrafficLights get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private AccelerationTrafficLights()
    {
        //
    }

    @Override
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final Length mergeDistance,
            final LaneBasedGtu gtu, final LanePerception perception, final CarFollowingModel carFollowingModel,
            final Speed speed, final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException, OperationalPlanException
    {
        Iterable<PerceivedTrafficLight> it =
                perception.getPerceptionCategory(IntersectionPerception.class).getTrafficLights(lane);
        if (!lane.isCurrent() && mergeDistance.gt0())
        {
            it = new FilteredIterable<>(it, (trafficLight) ->
            {
                return trafficLight.getDistance().gt(mergeDistance);
            });
        }
        it = onRoute(it, gtu);
        simplePlan.minimizeAcceleration(
                TrafficLightUtil.respondToTrafficLights(params, it, carFollowingModel, speed, speedLimitInfo));
    }

    @Override
    public String toString()
    {
        return "AccelerationTrafficLights";
    }

}
