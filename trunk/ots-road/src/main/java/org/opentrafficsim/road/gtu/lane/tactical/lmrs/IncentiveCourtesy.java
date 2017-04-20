package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
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
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveCourtesy implements VoluntaryIncentive, LmrsParameters
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Desire mandatoryDesire,
            final Desire voluntaryDesire) throws ParameterException, OperationalPlanException
    {

        double dLeftYes = 0;
        double dRightYes = 0;
        double dLeftNo = 0;
        double dRightNo = 0;
        double courtesy = behavioralCharacteristics.getParameter(COURTESY);
        Acceleration b = behavioralCharacteristics.getParameter(ParameterTypes.B);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        Speed ownSpeed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        SpeedLimitInfo sli = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        boolean leftLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).si > 0.0;
        boolean rightLane = infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).si > 0.0;
        for (LateralDirectionality dir : new LateralDirectionality[] { LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT })
        {
            Set<HeadwayGTU> leaders = neighbors.getLeaders(new RelativeLane(dir, 1));
            if (leaders != null)
            {
                for (HeadwayGTU leader : leaders)
                {
                    BehavioralCharacteristics bc = leader.getBehavioralCharacteristics();
                    double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                    if (desire > 0)
                    {
                        Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire,
                                bc, sli, carFollowingModel);
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
            Set<HeadwayGTU> followers = neighbors.getFollowers(new RelativeLane(dir, 2));
            if (followers != null)
            {
                for (HeadwayGTU follower : followers)
                {
                    BehavioralCharacteristics bc = follower.getBehavioralCharacteristics();
                    double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                    Acceleration a = follower.getDistance().lt0() ? b.neg()
                            : LmrsUtil.singleAcceleration(follower.getDistance(), follower.getSpeed(), ownSpeed, desire, bc,
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
                    BehavioralCharacteristics bc = leader.getBehavioralCharacteristics();
                    double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                    if (desire > 0)
                    {
                        Acceleration a = LmrsUtil.singleAcceleration(leader.getDistance(), ownSpeed, leader.getSpeed(), desire,
                                bc, sli, carFollowingModel);
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
        dLeftYes *= courtesy;
        dRightYes *= courtesy;
        return new Desire(dLeftYes - dLeftNo, dRightYes - dRightNo);

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveCourtesy";
    }

}
