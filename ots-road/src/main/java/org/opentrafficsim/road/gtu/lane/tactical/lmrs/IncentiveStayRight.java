package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Incentive for trucks to remain on the two right-hand lanes, unless the route requires otherwise.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IncentiveStayRight implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        // start at left-most lane
        SortedSet<RelativeLane> rootCrossSection = perception.getLaneStructure().getRootCrossSection();
        RelativeLane lane = rootCrossSection.first();
        // move right until we find 'the right-hand lane', which is defined by the last lane where the urgency does not increase
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        double curUrgency = urgency(infra.getLegalLaneChangeInfo(lane), parameters, speed);
        double rightUrgency;
        RelativeLane right;
        while (rootCrossSection.contains(right = lane.getRight())
                && (rightUrgency = urgency(infra.getLegalLaneChangeInfo(right), parameters, speed)) <= curUrgency)
        {
            curUrgency = rightUrgency;
            lane = right;
        }
        boolean legalLeft = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).ge0();
        if (lane.getLateralDirectionality().isRight() && lane.getNumLanes() > 1)
        {
            // must change right
            return new Desire(legalLeft ? -1.0 : 0.0, parameters.getParameter(LmrsParameters.DSYNC));
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

}
