package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;

/**
 * Incentive that lets drivers queue in an adjacent lane as soon as the speed is low in the adjacent lane, and stopping in the
 * current lane might block traffic towards other directions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 mrt. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveGetInLane implements MandatoryIncentive
{

    /** Congestion speed threshold parameter type. */
    protected static final ParameterTypeSpeed VCONG = ParameterTypes.VCONG;

    /** Hierarchy parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {

        Speed vCong = parameters.getParameter(VCONG);
        double socio = parameters.getParameter(SOCIO);
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        SortedSet<InfrastructureLaneChangeInfo> info = infra.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT);
        double dCur = info.isEmpty() ? Double.POSITIVE_INFINITY
                : info.first().getRemainingDistance().si / info.first().getRequiredNumberOfLaneChanges();
        double left = 0;
        double right = 0;
        double vCur = Double.POSITIVE_INFINITY;

        for (RelativeLane lane : new RelativeLane[] {RelativeLane.LEFT, RelativeLane.RIGHT})
        {
            if (infra.getCrossSection().contains(lane))
            {
                SortedSet<InfrastructureLaneChangeInfo> adjInfo = infra.getInfrastructureLaneChangeInfo(lane);
                double dAdj = adjInfo.isEmpty() ? Double.POSITIVE_INFINITY
                        : adjInfo.first().getRemainingDistance().si / adjInfo.first().getRequiredNumberOfLaneChanges();
                if (!info.isEmpty() && !info.first().isDeadEnd() && dCur < dAdj)
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
                        // right -= d;
                    }
                    else
                    {
                        double d = Math.max(0.0, 1.0 - v / vCong.si);
                        right += d;
                        // left -= d;
                    }
                }
                if (!adjInfo.isEmpty() && !adjInfo.first().isDeadEnd()
                        && (info.isEmpty() || (!info.isEmpty() && !info.first().isDeadEnd())) && dCur > dAdj)
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
        return new Desire(left * socio, right * socio);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveGetInLane";
    }

}
