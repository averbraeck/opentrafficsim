package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.FilteredIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class AccelerationConflicts implements AccelerationIncentive, Blockable
{

    /** Set of yield plans at conflicts with priority. Remembering for static model. */
    // @docs/06-behavior/tactical-planner/#modular-utilities
    private final ConflictPlans yieldPlans = new ConflictPlans();

    /** {@inheritDoc} */
    @Override
    public final void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final Length mergeDistance,
            final LaneBasedGtu gtu, final LanePerception perception, final CarFollowingModel carFollowingModel,
            final Speed speed, final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GtuException
    {
        EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);
        Acceleration acceleration = ego.getAcceleration();
        Length length = ego.getLength();
        Length width = ego.getWidth();
        Iterable<HeadwayConflict> conflicts = perception.getPerceptionCategory(IntersectionPerception.class).getConflicts(lane);
        PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane);
        if (!lane.isCurrent())
        {
            conflicts = new FilteredIterable<>(conflicts, (conflict) ->
            {
                return conflict.getDistance().gt(mergeDistance);
            });
        }
        conflicts = onRoute(conflicts, gtu);
        Acceleration a = ConflictUtil.approachConflicts(params, conflicts, leaders, carFollowingModel, length, width, speed,
                acceleration, speedLimitInfo, this.yieldPlans, gtu, lane);
        simplePlan.minimizeAcceleration(a);
        if (this.yieldPlans.getIndicatorIntent().isLeft())
        {
            simplePlan.setIndicatorIntentLeft(this.yieldPlans.getIndicatorObjectDistance());
        }
        else if (this.yieldPlans.getIndicatorIntent().isRight())
        {
            simplePlan.setIndicatorIntentRight(this.yieldPlans.getIndicatorObjectDistance());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBlocking()
    {
        return this.yieldPlans.isBlocking();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "AccelerationConflicts";
    }

}
