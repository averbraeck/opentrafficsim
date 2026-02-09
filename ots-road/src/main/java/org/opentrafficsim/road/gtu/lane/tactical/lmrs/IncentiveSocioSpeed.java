package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Lane change incentive based on social pressure. Drivers may refrain from changing left to not hinder faster traffic, or
 * drivers may change right to get out of the way. When drivers are on the left lane, this is considered 'overtaking', and
 * related to this the desired speed could be increased by using {@code SocioDesiredSpeedModel}.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// TODO keep left or right rules
public final class IncentiveSocioSpeed implements VoluntaryIncentive, Stateless<IncentiveSocioSpeed>
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

    /** Singleton instance. */
    public static final IncentiveSocioSpeed SINGLETON = new IncentiveSocioSpeed();

    @Override
    public IncentiveSocioSpeed get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveSocioSpeed()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        double dLeft = 0;
        double dRight = 0;
        if (context.getSpeed().gt(context.getParameters().getParameter(VCONG)))
        {
            double sigma = context.getParameters().getParameter(SOCIO);
            NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
            boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
            boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
            // change right to get out of the way
            if (rightLane && mandatoryDesire.right() >= 0.0)
            {
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers = neighbors.getFollowers(RelativeLane.CURRENT);
                if (!followers.isEmpty())
                {
                    double rho = context.getParameters().getParameter(RHO);
                    PerceivedGtu follower = followers.first();
                    double rhoFollower = follower.getBehavior().getParameters().getParameter(RHO);
                    if (rhoFollower * sigma > rho)
                    {
                        dRight = rhoFollower * sigma;
                    }
                }
            }
            // stay right to keep out of the way
            if (leftLane && mandatoryDesire.left() <= 0.0)
            {
                PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers = neighbors.getFollowers(RelativeLane.LEFT);
                if (followers != null && !followers.isEmpty())
                {
                    double rho;
                    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.LEFT);
                    if (leaders != null && !leaders.isEmpty())
                    {
                        PerceivedGtu leader = leaders.first();
                        Speed vDes = context.getGtu().getDesiredSpeed();
                        Speed vGain = context.getParameters().getParameter(VGAIN);
                        Length x0 = context.getParameters().getParameter(LOOKAHEAD);
                        rho = Tailgating.socialPressure(vDes, leader.getSpeed(), vGain, leader.getDistance(), x0);
                    }
                    else
                    {
                        rho = 0.0;
                    }
                    PerceivedGtu follower = followers.first();
                    Speed vGainFollower = follower.getBehavior().getParameters().getParameter(VGAIN);
                    Length x0Follower = follower.getBehavior().getParameters().getParameter(LOOKAHEAD);
                    double rhoFollower = Tailgating.socialPressure(follower.getBehavior().getDesiredSpeed(), context.getSpeed(),
                            vGainFollower, follower.getDistance(), x0Follower);
                    if (rhoFollower * sigma > rho)
                    {
                        dLeft = -rhoFollower * sigma;
                    }
                }
            }
        }
        return new Desire(dLeft, dRight);
    }

    @Override
    public String toString()
    {
        return "IncentiveSocioSpeed";
    }

}
