package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines desire out of hierarchal courtesy. For right-hand driving this is towards the right if the follower has a higher
 * desired speed. If the left follower has a higher desired speed, a negative desire towards the left exists. For left-hand
 * driving it is the other way around. Hierarchal desire depends on the level of hierarchal courtesy.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO keep left or right rules
public class IncentiveHierarchal implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Desire mandatoryDesire,
            final Desire voluntaryDesire) throws ParameterException, OperationalPlanException
    {
        double dLeft = 0;
        double dRight = 0;
        double hierarchy = behavioralCharacteristics.getParameter(LmrsParameters.HIERARCHY);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        Speed vDes = carFollowingModel.desiredSpeed(behavioralCharacteristics,
                perception.getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT)
                        .getSpeedLimitInfo(Length.ZERO));
        Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
        boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
        // change right to get out of the way
        if (rightLane && mandatoryDesire.getRight() >= 0.0)
        {
            SortedSet<HeadwayGTU> followers = neighbors.getFollowers(RelativeLane.CURRENT);
            if (!followers.isEmpty())
            {
                HeadwayGTU follower = followers.first();
                Speed vDesFollower = follower.getCarFollowingModel().desiredSpeed(follower.getBehavioralCharacteristics(),
                        follower.getSpeedLimitInfo());
                if (vDes.lt(vDesFollower)
                        && CarFollowingUtil
                                .followSingleLeader(follower.getCarFollowingModel(), follower.getBehavioralCharacteristics(),
                                        follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), ownSpeed)
                                .le0())
                {
                    dRight = hierarchy;
                }
            }
        }
        // stay right to keep out of the way
        if (leftLane && mandatoryDesire.getLeft() <= 0.0)
        {
            SortedSet<HeadwayGTU> followers = neighbors.getFollowers(RelativeLane.LEFT);
            if (followers != null && !followers.isEmpty())
            {
                HeadwayGTU follower = followers.first();
                Speed vDesFollower = follower.getCarFollowingModel().desiredSpeed(follower.getBehavioralCharacteristics(),
                        follower.getSpeedLimitInfo());
                if (vDes.lt(vDesFollower)
                        && CarFollowingUtil
                                .followSingleLeader(follower.getCarFollowingModel(), follower.getBehavioralCharacteristics(),
                                        follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), ownSpeed)
                                .le0())
                {
                    dLeft = -hierarchy;
                }
            }
        }
        return new Desire(dLeft, dRight);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveHierarchal";
    }

}
