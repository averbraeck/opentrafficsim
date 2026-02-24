package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.SortedSet;

import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Incentive for trucks to remain on the two slowest lanes, unless the route requires otherwise.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveStayOnSlowLanes implements VoluntaryIncentive, Stateless<IncentiveStayOnSlowLanes>
{

    /** Singleton instance. */
    public static final IncentiveStayOnSlowLanes SINGLETON = new IncentiveStayOnSlowLanes();

    @Override
    public IncentiveStayOnSlowLanes get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveStayOnSlowLanes()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        if (context.getSpeed().lt(context.getParameters().getParameter(ParameterTypes.VCONG)))
        {
            return new Desire(0.0, 0.0);
        }
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        // start at fastest lane
        SortedSet<RelativeLane> rootCrossSection = context.getPerception().getLaneStructure().getRootCrossSection();
        RelativeLane lane = rootCrossSection.first();
        // move to slow lane until we find 'the slowest lane', defined by the last lane where the urgency does not increase
        double curUrgency = IncentiveRoute.getDesireToLeave(context, lane);
        double slowLaneUrgency;
        RelativeLane slow = lane.getRight();
        while (rootCrossSection.contains(slow)
                && (slowLaneUrgency = IncentiveRoute.getDesireToLeave(context, slow)) <= curUrgency)
        {
            curUrgency = slowLaneUrgency;
            lane = slow;
            slow = slow.getRight();
        }
        boolean legalLeft = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).ge0();
        if (lane.getLateralDirectionality().isRight() && lane.getNumLanes() > 1)
        {
            // must change right
            return new Desire(legalLeft ? -1.0 : 0.0, context.getParameters().getParameter(LmrsParameters.DSYNC));
        }
        if (lane.isRight())
        {
            // must not change left
            return new Desire(legalLeft ? -1.0 : 0.0, 0.0);
        }
        return new Desire(0.0, 0.0);
    }

    @Override
    public String toString()
    {
        return "IncentiveStayOnSlowLanes";
    }

}
