package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class AccelerationConflicts implements AccelerationIncentive, Blockable
{

    /** Set of yield plans at conflicts with priority. Remembering for static model. */
    private final ConflictPlans yieldPlans = new ConflictPlans();

    /** {@inheritDoc} */
    @Override
    public final void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final LaneBasedGTU gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final Parameters params, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GTUException
    {
        // TODO consider adjacent lanes before and during lane change
        EgoPerception ego = perception.getPerceptionCategory(EgoPerception.class);
        Acceleration acceleration = ego.getAcceleration();
        Length length = ego.getLength();
        Length width = ego.getWidth();
        PerceptionCollectable<HeadwayConflict, Conflict> conflicts =
                perception.getPerceptionCategory(IntersectionPerception.class).getConflicts(lane);
        PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders =
                perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane);

        Acceleration a;
        if (lane.isCurrent())
        {
            a = ConflictUtil.approachConflicts(params, conflicts, leaders, carFollowingModel, length, width, speed,
                    acceleration, speedLimitInfo, this.yieldPlans, gtu);
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
        else if (!conflicts.isEmpty() && conflicts.first().getDistance().gt0())
        {
            // TODO this is too simple, needs to be consistent with gap-acceptance or GTU's may not change
            Length lcDistance = perception.getPerceptionCategory(InfrastructurePerception.class)
                    .getLegalLaneChangePossibility(RelativeLane.CURRENT, lane.getLateralDirectionality()).neg();
            HeadwayConflict conflict = null;
            for (HeadwayConflict c : conflicts)
            {
                if (c.getDistance().gt(lcDistance))
                {
                    conflict = c;
                    break;
                }
            }
            if (conflict != null)
            {
                a = CarFollowingUtil.followSingleLeader(carFollowingModel, params, speed, speedLimitInfo,
                        conflicts.first().getDistance(), Speed.ZERO);
                // limit deceleration on adjacent lanes
                a = Acceleration.max(a, params.getParameter(ParameterTypes.BCRIT).neg());
                simplePlan.minimizeAcceleration(a);
            }
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