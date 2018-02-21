package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
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
 * Determines desire out of social speed courtesy. For right-hand driving this is towards the right if the follower has a higher
 * desired speed. If the left follower has a higher desired speed, a negative desire towards the left exists. For left-hand
 * driving it is the other way around. Socio-speed desire depends on the level of courtesy.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
// TODO keep left or right rules
public class IncentiveSocioSpeed implements VoluntaryIncentive
{

    /** Hierarchy parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        double dLeft = 0;
        double dRight = 0;
        double c = parameters.getParameter(SOCIO);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        Speed vDes = carFollowingModel.desiredSpeed(parameters,
                infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO));
        Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
        boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
        // change right to get out of the way
        if (rightLane && mandatoryDesire.getRight() >= 0.0)
        {
            PerceptionIterable<HeadwayGTU> followers =
                    (PerceptionIterable<HeadwayGTU>) neighbors.getFollowers(RelativeLane.CURRENT);
            if (!followers.isEmpty())
            {
                HeadwayGTU follower = followers.first();
                Speed vDesFollower =
                        follower.getCarFollowingModel().desiredSpeed(follower.getParameters(), follower.getSpeedLimitInfo());
                if (vDesFollower.gt(vDes))
                {
                    Acceleration aFol =
                            CarFollowingUtil.followSingleLeader(follower.getCarFollowingModel(), follower.getParameters(),
                                    follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), ownSpeed);
                    Acceleration aFree = CarFollowingUtil.freeAcceleration(follower.getCarFollowingModel(),
                            follower.getParameters(), follower.getSpeed(), follower.getSpeedLimitInfo());
                    if (aFol.lt(aFree))
                    {
                        dRight = c;
                    }
                }
            }
        }
        // stay right to keep out of the way
        if (leftLane && mandatoryDesire.getLeft() <= 0.0)
        {
            PerceptionIterable<HeadwayGTU> followers =
                    (PerceptionIterable<HeadwayGTU>) neighbors.getFollowers(RelativeLane.LEFT);
            if (followers != null && !followers.isEmpty())
            {
                HeadwayGTU follower = followers.first();
                Speed vDesFollower =
                        follower.getCarFollowingModel().desiredSpeed(follower.getParameters(), follower.getSpeedLimitInfo());
                if (vDesFollower.gt(vDes))
                {
                    Acceleration aFol =
                            CarFollowingUtil.followSingleLeader(follower.getCarFollowingModel(), follower.getParameters(),
                                    follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), ownSpeed);
                    Acceleration aFree = CarFollowingUtil.freeAcceleration(follower.getCarFollowingModel(),
                            follower.getParameters(), follower.getSpeed(), follower.getSpeedLimitInfo());
                    if (aFol.lt(aFree))
                    {
                        dLeft = -c;
                    }
                }
            }
        }
        return new Desire(dLeft, dRight);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSocioSpeed";
    }

}
