package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.Headway;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * Lane-based tactical planner that implements car following behavior. This tactical planner retrieves the car following model
 * from the strategical planner and will generate an operational plan for the GTU.
 * <p>
 * This lane-based tactical planner makes decisions based on headway (GTU following model). It can ask the strategic planner for
 * assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedGTUFollowingTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /**
     * Instantiated a tactical planner with just GTU following behavior and no lane changes.
     * @param carFollowingModel Car-following model.
     */
    public LaneBasedGTUFollowingTacticalPlanner(final GTUFollowingModelOld carFollowingModel)
    {
        super(carFollowingModel);
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
            final DirectedPoint locationAtStartTime) throws OperationalPlanException, NetworkException, GTUException,
            ParameterException
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
        LanePerceptionFull perception = laneBasedGTU.getPerception();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(gtu, locationAtStartTime, startTime, new Time.Rel(1.0, TimeUnit.SECOND));
        }

        // perceive every time step... This is the 'classical' way of tactical planning.
        perception.perceive();

        // see how far we can drive
        Length.Rel maxDistance = laneBasedGTU.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, maxDistance);

        // look at the conditions for headway
        Headway headway = perception.getForwardHeadway();
        AccelerationStep accelerationStep = null;
        if (headway.getDistance().ge(maxDistance))
        {
            // TODO I really don't like this -- if there is a lane drop at 20 m, the GTU should stop...
            accelerationStep =
                    ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStepWithNoLeader(laneBasedGTU,
                            lanePathInfo.getPath().getLength(), perception.getSpeedLimit());
        }
        else
        {
            accelerationStep =
                    ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStep(laneBasedGTU, headway.getSpeed(),
                            headway.getDistance(), lanePathInfo.getPath().getLength(), perception.getSpeedLimit());
        }

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6 && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(gtu, locationAtStartTime, startTime, accelerationStep.getDuration());
        }

        List<Segment> operationalPlanSegmentList = new ArrayList<>();
        if (accelerationStep.getAcceleration().si == 0.0)
        {
            Segment segment = new OperationalPlan.SpeedSegment(accelerationStep.getDuration());
            operationalPlanSegmentList.add(segment);
        }
        else
        {
            Segment segment =
                    new OperationalPlan.AccelerationSegment(accelerationStep.getDuration(), accelerationStep.getAcceleration());
            operationalPlanSegmentList.add(segment);
        }
        OperationalPlan op =
                new OperationalPlan(gtu, lanePathInfo.getPath(), startTime, gtu.getVelocity(), operationalPlanSegmentList);
        return op;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGTUFollowingTacticalPlanner [carFollowingModel=" + getCarFollowingModel() + "]";
    }
}
