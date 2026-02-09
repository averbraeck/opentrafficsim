package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;

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
    public Acceleration accelerate(final TacticalContextEgo context, final RelativeLane lane, final Length mergeDistance)
            throws ParameterException, OperationalPlanException
    {
        return TrafficLightUtil.respondToTrafficLights(context, lane, mergeDistance, true);
    }

    @Override
    public String toString()
    {
        return "AccelerationTrafficLights";
    }

}
