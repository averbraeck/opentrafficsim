package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.Break;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;

/**
 * Incentive that lets drivers queue in an adjacent lane as soon as the speed is low in the adjacent lane, and stopping in the
 * current lane might block traffic towards other directions.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveGetInLane implements MandatoryIncentive
{

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {

        Break.on(perception, "472", 28 * 60, true);

        Speed vCong = behavioralCharacteristics.getParameter(ParameterTypes.VCONG);
        double hierarchy = behavioralCharacteristics.getParameter(LmrsParameters.HIERARCHY);
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        SortedSet<InfrastructureLaneChangeInfo> info = infra.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT);
        double left = 0;
        double right = 0;
        double vCur = Double.POSITIVE_INFINITY;
        for (RelativeLane lane : new RelativeLane[] { RelativeLane.LEFT, RelativeLane.RIGHT })
        {
            if (infra.getCrossSection().contains(lane))
            {
                SortedSet<InfrastructureLaneChangeInfo> adjInfo = infra.getInfrastructureLaneChangeInfo(lane);
                if (!info.isEmpty() && !info.first().isDeadEnd())
                {
                    if (adjInfo.isEmpty()
                            || (adjInfo.first().getRemainingDistance().le(info.first().getRemainingDistance()) && adjInfo
                                    .first().getRequiredNumberOfLaneChanges() <= info.first().getRequiredNumberOfLaneChanges()))
                    {
                        double v = Double.POSITIVE_INFINITY;
                        for (HeadwayGTU neighbor : neighbors.getLeaders(lane))
                        {
                            v = Math.min(v, neighbor.getSpeed().si);
                        }
                        if (lane.isLeft())
                        {
                            double d = Math.max(0.0, 1.0 - v / vCong.si);
                            left += d;
                            right -= d;
                        }
                        else
                        {
                            double d = Math.max(0.0, 1.0 - v / vCong.si);
                            right += d;
                            left -= d;
                        }
                    }
                }
                if (!adjInfo.isEmpty() && !adjInfo.first().isDeadEnd())
                {
                    if (info.isEmpty()
                            || (info.first().getRemainingDistance().le(adjInfo.first().getRemainingDistance()) && info.first()
                                    .getRequiredNumberOfLaneChanges() <= adjInfo.first().getRequiredNumberOfLaneChanges()))
                    {
                        if (Double.isInfinite(vCur))
                        {
                            for (HeadwayGTU neighbor : neighbors.getLeaders(RelativeLane.CURRENT))
                            {
                                vCur = Math.min(vCur, neighbor.getSpeed().si);
                            }
                        }
                        if (lane.isLeft())
                        {
                            left -= Math.max(0.0, 1.0 - vCur / vCong.si);
                        }
                        else
                        {
                            right -= Math.max(0.0, 1.0 - vCur / vCong.si);
                        }
                    }
                }
            }
        }
        return new Desire(left * hierarchy, right * hierarchy);
    }

}
