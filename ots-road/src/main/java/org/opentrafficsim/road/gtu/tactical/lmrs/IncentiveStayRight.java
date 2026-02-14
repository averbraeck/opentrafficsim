package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Incentive for trucks to remain on the two right-hand lanes, unless the route requires otherwise.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveStayRight implements VoluntaryIncentive, Stateless<IncentiveStayRight>
{

    /** Singleton instance. */
    public static final IncentiveStayRight SINGLETON = new IncentiveStayRight();

    @Override
    public IncentiveStayRight get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveStayRight()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        // start at left-most lane
        SortedSet<RelativeLane> rootCrossSection = context.getPerception().getLaneStructure().getRootCrossSection();
        RelativeLane lane = rootCrossSection.first();
        // move right until we find 'the right-hand lane', which is defined by the last lane where the urgency does not increase
        double curUrgency = urgency(infra.getLegalLaneChangeInfo(lane), context.getParameters(), context.getSpeed());
        double rightUrgency;
        RelativeLane right;
        while (rootCrossSection.contains(right = lane.getRight()) && (rightUrgency =
                urgency(infra.getLegalLaneChangeInfo(right), context.getParameters(), context.getSpeed())) <= curUrgency)
        {
            curUrgency = rightUrgency;
            lane = right;
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

    /**
     * Returns the urgency to leave a lane.
     * @param laneChangeInfo lane change info on the lane
     * @param parameters parameters
     * @param speed current speed
     * @return urgency to leave the lane
     * @throws ParameterException if parameter is not given
     */
    private double urgency(final SortedSet<LaneChangeInfo> laneChangeInfo, final Parameters parameters, final Speed speed)
            throws ParameterException
    {
        double urgency = 0.0;
        for (LaneChangeInfo info : laneChangeInfo)
        {
            double nextUrgency =
                    IncentiveRoute.getDesireToLeave(parameters, info.remainingDistance(), info.numberOfLaneChanges(), speed);
            urgency = urgency > nextUrgency ? urgency : nextUrgency;
        }
        return urgency;
    }

    @Override
    public String toString()
    {
        return "IncentiveStayRight";
    }

}
