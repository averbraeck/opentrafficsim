package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire for courtesy lane changes, which are performed to supply space for other drivers. In case
 * drivers in adjacent lanes have desire to change to the current lane, the driver has desire to change to the other adjacent
 * lane. The level of desire depends on lane change courtesy, as well as the distance of the leading vehicle for which desire
 * exists. This desire exists for only a single vehicle, i.e. the one giving maximum desire. A negative desire may also result
 * for leaders in the 2nd adjacent lane desiring to change to the 1st adjacent lane. By not changing to the 1st adjacent lane,
 * room is reserved for the leader on the 2nd adjacent lane.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveCourtesy implements VoluntaryIncentive, Stateless<IncentiveCourtesy>
{

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Socio-speed sensitivity parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** Singleton instance. */
    public static final IncentiveCourtesy SINGLETON = new IncentiveCourtesy();

    @Override
    public IncentiveCourtesy get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveCourtesy()
    {
        //
    }

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        double dLeftYes = 0;
        double dRightYes = 0;
        double dLeftNo = 0;
        double dRightNo = 0;
        double socio = context.getParameters().getParameter(SOCIO);
        Acceleration b = context.getParameters().getParameter(B);
        NeighborsPerception neighbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
        boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
        for (LateralDirectionality dir : LateralDirectionality.LEFT_AND_RIGHT)
        {
            Iterable<PerceivedGtu> leaders = neighbors.getLeaders(new RelativeLane(dir, 1));
            if (leaders != null)
            {
                for (PerceivedGtu leader : leaders)
                {
                    double desire = dir.isLeft() ? leader.getBehavior().rightLaneChangeDesire()
                            : leader.getBehavior().leftLaneChangeDesire();
                    if (desire > 0)
                    {
                        // TODO factor -a/b as influence factor is heavy in calculation, consider v<vEgo & 1-s/x0
                        Acceleration a = CarFollowingUtil.followSingleLeader(context, leader);
                        if (a.lt0())
                        {
                            double d = desire * Math.min(-a.si / b.si, 1.0);
                            if (dir.isLeft() && rightLane)
                            {
                                // leader in left lane wants to change right, so we change right
                                dRightYes = dRightYes > d ? dRightYes : d;
                            }
                            else if (leftLane)
                            {
                                // leader in right lane wants to change left, so we change left
                                dLeftYes = dLeftYes > d ? dLeftYes : d;
                            }
                        }
                    }
                }
            }
            // consider close followers on 2 lanes away
            Iterable<PerceivedGtu> followers = neighbors.getFollowers(new RelativeLane(dir, 2));
            if (followers != null)
            {
                for (PerceivedGtu follower : followers)
                {
                    double desire = dir.isLeft() ? follower.getBehavior().rightLaneChangeDesire()
                            : follower.getBehavior().leftLaneChangeDesire();
                    Acceleration a = follower.getDistance().lt0() ? b.neg()
                            : LmrsUtil.singleAcceleration(follower, follower.getDistance(), context.getSpeed(), desire);
                    if (a.lt0())
                    {
                        if (desire > 0)
                        {
                            double d = desire * Math.min(-a.si / b.si, 1.0);
                            if (dir.isLeft() && leftLane)
                            {
                                // follower in second left lane wants to change right, so we do not change left
                                dLeftNo = dLeftNo > d ? dLeftNo : d;
                            }
                            else if (rightLane)
                            {
                                // follower in second right lane wants to change left, so we do not change right
                                dRightNo = dRightNo > d ? dRightNo : d;
                            }
                        }
                    }
                    else
                    {
                        // ignore further followers
                        break;
                    }
                }
            }
            leaders = neighbors.getLeaders(new RelativeLane(dir, 2));
            if (leaders != null)
            {
                for (PerceivedGtu leader : leaders)
                {
                    double desire = dir.isLeft() ? leader.getBehavior().rightLaneChangeDesire()
                            : leader.getBehavior().leftLaneChangeDesire();
                    if (desire > 0)
                    {
                        Acceleration a = LmrsUtil.singleAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                        if (a.lt0())
                        {
                            double d = desire * Math.min(-a.si / b.si, 1.0); // (1 - leader.getDistance().si / x0.si) * desire;
                            if (dir.isLeft() && leftLane)
                            {
                                // leader in second left lane wants to change right, so we do not change left
                                dLeftNo = dLeftNo > d ? dLeftNo : d;
                            }
                            else if (rightLane)
                            {
                                // leader in second right lane wants to change left, so we do not change right
                                dRightNo = dRightNo > d ? dRightNo : d;
                            }
                        }
                    }
                }
            }
        }
        // note: noLeft and noRight weighted with 1 always
        dLeftYes *= socio;
        dRightYes *= socio;
        return new Desire(dLeftYes - dLeftNo, dRightYes - dRightNo);

    }

    @Override
    public String toString()
    {
        return "IncentiveCourtesy";
    }

}
