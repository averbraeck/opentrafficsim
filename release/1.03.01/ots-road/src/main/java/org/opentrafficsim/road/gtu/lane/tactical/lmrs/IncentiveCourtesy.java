package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Determines lane change desire for courtesy lane changes, which are performed to supply space for other drivers. In case
 * drivers in adjacent lanes have desire to change to the current lane, the driver has desire to change to the other adjacent
 * lane. The level of desire depends on lane change courtesy, as well as the distance of the leading vehicle for which desire
 * exists. This desire exists for only a single vehicle, i.e. the one giving maximum desire. A negative desire may also result
 * for leaders in the 2nd adjacent lane desiring to change to the 1st adjacent lane. By not changing to the 1st adjacent lane,
 * room is reserved for the leader on the 2nd adjacent lane.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveCourtesy implements VoluntaryIncentive
{

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Socio-speed sensitivity parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** Current left lane change desire. */
    protected static final ParameterTypeDouble DLEFT = LmrsParameters.DLEFT;

    /** Current right lane change desire. */
    protected static final ParameterTypeDouble DRIGHT = LmrsParameters.DRIGHT;

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {

        double dLeftYes = 0;
        double dRightYes = 0;
        double dLeftNo = 0;
        double dRightNo = 0;
        double socio = parameters.getParameter(SOCIO);
        Acceleration b = parameters.getParameter(B);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        SpeedLimitInfo sli = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
        boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
        for (LateralDirectionality dir : new LateralDirectionality[] {LateralDirectionality.LEFT, LateralDirectionality.RIGHT})
        {
            Iterable<HeadwayGTU> leaders = neighbors.getLeaders(new RelativeLane(dir, 1));
            if (leaders != null)
            {
                for (HeadwayGTU leader : leaders)
                {
                    Parameters params = leader.getParameters();
                    double desire = dir.isLeft() ? params.getParameter(DRIGHT) : params.getParameter(DLEFT);
                    if (desire > 0)
                    {
                        // TODO factor -a/b as influence factor is heavy in calculation, consider v<vEgo & 1-s/x0
                        Acceleration a =
                                CarFollowingUtil.followSingleLeader(carFollowingModel, parameters, ownSpeed, sli, leader);
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
            Iterable<HeadwayGTU> followers = neighbors.getFollowers(new RelativeLane(dir, 2));
            if (followers != null)
            {
                for (HeadwayGTU follower : followers)
                {
                    Parameters params = follower.getParameters();
                    double desire = dir.isLeft() ? params.getParameter(DRIGHT) : params.getParameter(DLEFT);
                    Acceleration a = follower.getDistance().lt0() ? b.neg()
                            : LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire, params,
                                    follower.getSpeedLimitInfo(), follower.getCarFollowingModel());
                    if (a.lt0())
                    {
                        if (desire > 0)
                        {
                            double d = desire * Math.min(-a.si / b.si, 1.0);
                            if (dir.isLeft() && leftLane)
                            {
                                // follower in second left lane wants to change right, so we do not change left
                                dLeftNo = dLeftNo > desire ? dLeftNo : d;
                            }
                            else if (rightLane)
                            {
                                // follower in second right lane wants to change left, so we do not change right
                                dRightNo = dRightNo > desire ? dRightNo : d;
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
                for (HeadwayGTU leader : leaders)
                {
                    Parameters params = leader.getParameters();
                    double desire = dir.isLeft() ? params.getParameter(DRIGHT) : params.getParameter(DLEFT);
                    if (desire > 0)
                    {
                        Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire,
                                params, sli, carFollowingModel);
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveCourtesy";
    }

}
