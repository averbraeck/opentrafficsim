package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire for speed, where the slowest vehicle in the current and adjacent lanes are assessed. The larger
 * the speed differences between these vehicles, the larger the desire. Negative speed differences result in negative lane
 * change desire. Only vehicles within a limited anticipation range are considered. The considered speed difference with an
 * adjacent lane is reduced as the slowest leader in the adjacent lane is further ahead. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveSpeed implements VoluntaryIncentive, Stateless<IncentiveSpeed>
{

    /** Singleton instance. */
    public static final IncentiveSpeed SINGLETON = new IncentiveSpeed();

    @Override
    public IncentiveSpeed get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveSpeed()
    {
        //
    }

    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire,
            final ImmutableMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        // TODO: SpeedWithCourtesy now uses TrafficPerception, which embeds the courtesy part. How to do this?
        return new Desire(0, 0);
    }

    @Override
    public String toString()
    {
        return "IncentiveSpeed []";
    }

}
