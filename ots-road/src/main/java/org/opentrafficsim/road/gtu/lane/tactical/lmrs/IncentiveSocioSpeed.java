package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Lane change incentive based on social pressure. Drivers may refrain from changing left to not hinder faster traffic, or
 * drivers may change right to get out of the way. When drivers are on the left lane, this is considered 'overtaking', and
 * related to this the desired speed could be increased by using {@code SocioDesiredSpeedModel}.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
// TODO keep left or right rules
public class IncentiveSocioSpeed implements VoluntaryIncentive
{

    /** Social pressure applied to the leader. */
    protected static final ParameterTypeDouble RHO = Tailgating.RHO;

    /** Hierarchy parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** Speed threshold below which traffic is considered congested. */
    protected static final ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** Vgain parameter; ego-speed sensitivity. */
    protected static final ParameterTypeSpeed VGAIN = LmrsParameters.VGAIN;

    /** Look-ahead distance. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        double dLeft = 0;
        double dRight = 0;
        Speed vCong = parameters.getParameter(VCONG);
        Speed ownSpeed = perception.getPerceptionCategoryOrNull(EgoPerception.class).getSpeed();
        if (ownSpeed.gt(vCong))
        {
            double sigma = parameters.getParameter(SOCIO);
            NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
            InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
            boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
            boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
            // change right to get out of the way
            if (rightLane && mandatoryDesire.getRight() >= 0.0)
            {
                PerceptionCollectable<HeadwayGTU, LaneBasedGTU> followers = neighbors.getFollowers(RelativeLane.CURRENT);
                if (!followers.isEmpty())
                {
                    double rho = parameters.getParameter(RHO);
                    HeadwayGTU follower = followers.first();
                    double rhoFollower = follower.getParameters().getParameter(RHO);
                    if (rhoFollower * sigma > rho)
                    {
                        dRight = rhoFollower * sigma;
                    }
                }
            }
            // stay right to keep out of the way
            if (leftLane && mandatoryDesire.getLeft() <= 0.0)
            {
                PerceptionCollectable<HeadwayGTU, LaneBasedGTU> followers = neighbors.getFollowers(RelativeLane.LEFT);
                if (followers != null && !followers.isEmpty())
                {
                    double rho;
                    PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders = neighbors.getLeaders(RelativeLane.LEFT);
                    if (leaders != null && !leaders.isEmpty())
                    {
                        HeadwayGTU leader = leaders.first();
                        Speed vDes = Try.assign(() -> perception.getGtu().getDesiredSpeed(),
                                "Could not obtain GTU from perception.");
                        Speed vGain = parameters.getParameter(VGAIN);
                        Length x0 = parameters.getParameter(LOOKAHEAD);
                        rho = Tailgating.socialPressure(ownSpeed, vCong, vDes, leader.getSpeed(), vGain, leader.getDistance(),
                                x0);
                    }
                    else
                    {
                        rho = 0.0;
                    }
                    HeadwayGTU follower = followers.first();
                    Speed vGainFollower = follower.getParameters().getParameter(VGAIN);
                    Length x0Follower = follower.getParameters().getParameter(LOOKAHEAD);
                    double rhoFollower = Tailgating.socialPressure(follower.getSpeed(), vCong, follower.getDesiredSpeed(),
                            ownSpeed, vGainFollower, follower.getDistance(), x0Follower);
                    if (rhoFollower * sigma > rho)
                    {
                        dLeft = -rhoFollower * sigma;
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
