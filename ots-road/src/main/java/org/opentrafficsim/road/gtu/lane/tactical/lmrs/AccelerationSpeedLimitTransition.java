package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Acceleration incentive for speed limit transitions.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class AccelerationSpeedLimitTransition
        implements AccelerationIncentive, Stateless<AccelerationSpeedLimitTransition>
{

    /** Singleton instance. */
    public static final AccelerationSpeedLimitTransition SINGLETON = new AccelerationSpeedLimitTransition();

    @Override
    public AccelerationSpeedLimitTransition get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private AccelerationSpeedLimitTransition()
    {
        //
    }

    @Override
    public Acceleration accelerate(final RelativeLane lane, final Length mergeDistance, final LaneBasedGtu gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final Parameters params, final SpeedLimitInfo speedLimitInfo) throws OperationalPlanException, ParameterException
    {
        SpeedLimitProspect slp = perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(lane);
        return SpeedLimitUtil.considerSpeedLimitTransitions(params, speed, slp, carFollowingModel);
    }

    @Override
    public String toString()
    {
        return "AccelerationSpeedLimitTransition";
    }

}
