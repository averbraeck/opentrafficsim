package org.opentrafficsim.road.gtu.tactical.util;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Utility for tactical planners so they can deal with dead-ens situations.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DeadEnUtil
{

    /**
     * Constructor.
     */
    private DeadEnUtil()
    {
        //
    }

    /**
     * Adjusts the simple operational plan to deal with dead-end situations.
     * @param context tactical information such as parameters and car-following model
     * @param plan simple operational plan
     * @return adjusted simple operational plan to deal with dead-end situations
     * @throws OperationalPlanException if there is no infrastructure perception
     * @throws ParameterException if a required parameter is not present
     */
    public static SimpleOperationalPlan dealWithDeadEnd(final TacticalContextEgo context, final SimpleOperationalPlan plan)
            throws OperationalPlanException, ParameterException
    {
        SimpleOperationalPlan simplePlan = plan;
        // deal with dead-end situations
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        SortedSet<LaneChangeInfo> lcInfo = infra.getPhysicalLaneChangeInfo(RelativeLane.CURRENT);
        if (!lcInfo.isEmpty())
        {
            Length remainingDist = lcInfo.first().remainingDistance();
            /*
             * Once the model has initiated a lane change, we no longer limit acceleration by the dead-end. This is because
             * either the lane change is instantaneous and the dead-end is no longer relevant, or because movement is required
             * for the lane change and we do not want the dead-end to keep the GTU in a stand-still dead-lock.
             */
            if (!simplePlan.isLaneChange())
            {
                boolean edge = context.getGtu().getBookkeeping().isEdge();
                if (edge)
                {
                    int n = lcInfo.first().numberOfLaneChanges();
                    remainingDist = remainingDist.minus(context.getGtu().getLength().times(n));
                }
                if (edge && remainingDist.lt0() && !context.getGtu().getLaneChangeDirection().isNone())
                {
                    Logger.ots().info("Forced continuation of lane change for GTU {}.", context.getGtu().getId());
                    /*
                     * We cannot allow the model to cancel a lane change so close to the dead-end. The model can decide when the
                     * lane change is acceptable to initiate, but once initiated there is no more space to cancel.
                     */
                    simplePlan = new SimpleOperationalPlan(simplePlan.getAcceleration(), simplePlan.getDuration(),
                            context.getGtu().getLaneChangeDirection());
                }
                else
                {
                    Acceleration a = CarFollowingUtil.stop(context, remainingDist);
                    a = Acceleration.max(a, context.getGtu().getParameters().getParameter(ParameterTypes.BCRIT).neg());
                    simplePlan.minimizeAcceleration(a);
                }
            }
            else if (remainingDist.lt0())
            {
                // change lane instantaneously
                context.getGtu().changeLaneInstantaneously(simplePlan.getLaneChangeDirection());
                simplePlan = new SimpleOperationalPlan(simplePlan.getAcceleration(), simplePlan.getDuration());
            }
        }
        return simplePlan;
    }

}
