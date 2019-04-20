package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class AccelerationConflicts implements AccelerationIncentive
{

    /** Set of yield plans at conflicts with priority. Remembering for static model. */
    private final ConflictPlans yieldPlans = new ConflictPlans();

    /** {@inheritDoc} */
    @Override
    public void accelerate(final SimpleOperationalPlan simplePlan, final RelativeLane lane, final LaneBasedGTU gtu,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Speed speed,
            final BehavioralCharacteristics bc, final SpeedLimitInfo speedLimitInfo)
            throws OperationalPlanException, ParameterException, GTUException
    {
        Acceleration acceleration = perception.getPerceptionCategory(EgoPerception.class).getAcceleration();
        Length length = perception.getPerceptionCategory(EgoPerception.class).getLength();
        SortedSet<HeadwayConflict> conflicts =
                perception.getPerceptionCategory(IntersectionPerception.class).getConflicts(lane);
        SortedSet<HeadwayGTU> leaders = perception.getPerceptionCategory(NeighborsPerception.class).getLeaders(lane);
        simplePlan.minimizeAcceleration(ConflictUtil.approachConflicts(bc, conflicts, leaders, carFollowingModel, length, speed,
                acceleration, speedLimitInfo, this.yieldPlans, gtu));
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
    public String toString()
    {
        return "AccelerationConflicts";
    }

}