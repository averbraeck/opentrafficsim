package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

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
        Length x0 = behavioralCharacteristics.getParameter(ParameterTypes.LOOKAHEAD);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        for (LateralDirectionality dir : new LateralDirectionality[] { LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT })
        {
            for (HeadwayGTU leader : neighbors.getLeaders(new RelativeLane(dir, 1)))
            {
                BehavioralCharacteristics bc = leader.getBehavioralCharacteristics();
                double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                if (desire > 0)
                {
                    double d = (1 - leader.getDistance().si / x0.si) * desire;
                    if (dir.isLeft())
                    {
                        dLeftYes = dLeftYes > d ? dLeftYes : d;
                    }
                    else
                    {
                        dRightYes = dRightYes > d ? dRightYes : d;
                    }
                }
            }
            for (HeadwayGTU follower : neighbors.getFollowers(new RelativeLane(dir, 2)))
            {
                if (follower.getDistance().lt0())
                {
                    BehavioralCharacteristics bc = follower.getBehavioralCharacteristics();
                    double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                    if (desire > 0)
                    {
                        if (dir.isLeft())
                        {
                            dLeftNo = dLeftNo > desire ? dLeftNo : desire;
                        }
                        else
                        {
                            dRightNo = dRightNo > desire ? dRightNo : desire;
                        }
                    }
                }
                else
                {
                    break;
                }
            }
            for (HeadwayGTU leader : neighbors.getLeaders(new RelativeLane(dir, 2)))
            {
                BehavioralCharacteristics bc = leader.getBehavioralCharacteristics();
                double desire = dir.isLeft() ? bc.getParameter(DRIGHT) : bc.getParameter(DLEFT);
                if (desire > 0)
                {
                    double d = (1 - leader.getDistance().si / x0.si) * desire;
                    if (dir.isLeft())
                    {
                        dLeftNo = dLeftNo > d ? dLeftNo : d;
                    }
                    else
                    {
                        dRightNo = dRightNo > d ? dRightNo : d;
                    }
                }
            }
        }
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
