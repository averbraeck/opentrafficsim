package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * Lane-based tactical planner that implements car following behavior. This tactical planner retrieves the car following model
 * from the strategical planner and will generate an operational plan for the GTU.
 * <p>
 * This lane-based tactical planner makes decisions based on headway (GTU following model). It can ask the strategic planner for
 * assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Instantiate a tactical planner with just GTU following behavior and no lane changes.
     * @param carFollowingModel Car-following model.
     * @param gtu GTU
     */
    public LaneBasedGTUFollowingTacticalPlanner(final GTUFollowingModelOld carFollowingModel, final LaneBasedGTU gtu)
    {
        super(carFollowingModel, gtu);
        getPerception().addPerceptionCategory(new DefaultSimplePerception(getPerception()));
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, NetworkException, GTUException, ParameterException
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = getGtu();
        LanePerception perception = getPerception();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(getGtu(), locationAtStartTime, startTime, new Duration(1.0, TimeUnit.SECOND));
        }

        // perceive every time step... This is the 'classical' way of tactical planning.
        perception.perceive();

        // see how far we can drive
        Length maxDistance = laneBasedGTU.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, maxDistance);

        // look at the conditions for headway from a GTU in front
        Headway headwayGTU = perception.getPerceptionCategory(DefaultSimplePerception.class).getForwardHeadwayGTU();
        AccelerationStep accelerationStepGTU = null;
        if (headwayGTU.getDistance().ge(maxDistance))
        {
            // TODO I really don't like this -- if there is a lane drop at 20 m, the GTU should stop...
            accelerationStepGTU = ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStepWithNoLeader(
                    laneBasedGTU, lanePathInfo.getPath().getLength(),
                    perception.getPerceptionCategory(DefaultSimplePerception.class).getSpeedLimit());
        }
        else
        {
            accelerationStepGTU = ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStep(laneBasedGTU,
                    headwayGTU.getSpeed(), headwayGTU.getDistance(), lanePathInfo.getPath().getLength(),
                    perception.getPerceptionCategory(DefaultSimplePerception.class).getSpeedLimit());
        }

        // look at the conditions for headway from an object in front
        Headway headwayObject = perception.getPerceptionCategory(DefaultSimplePerception.class).getForwardHeadwayObject();
        AccelerationStep accelerationStepObject = null;
        if (headwayObject.getDistance().ge(maxDistance))
        {
            accelerationStepObject = ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStepWithNoLeader(
                    laneBasedGTU, lanePathInfo.getPath().getLength(),
                    perception.getPerceptionCategory(DefaultSimplePerception.class).getSpeedLimit());
        }
        else
        {
            accelerationStepObject = ((GTUFollowingModelOld) getCarFollowingModel()).computeAccelerationStep(laneBasedGTU,
                    headwayObject.getSpeed(), headwayObject.getDistance(), lanePathInfo.getPath().getLength(),
                    perception.getPerceptionCategory(DefaultSimplePerception.class).getSpeedLimit());
        }

        // see which one is most limiting
        AccelerationStep accelerationStep = accelerationStepGTU.getAcceleration().lt(accelerationStepObject.getAcceleration())
                ? accelerationStepGTU : accelerationStepObject;

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6 && laneBasedGTU.getSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(getGtu(), locationAtStartTime, startTime, accelerationStep.getDuration());
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
        OperationalPlan op = new OperationalPlan(getGtu(), lanePathInfo.getPath(), startTime, getGtu().getSpeed(),
                operationalPlanSegmentList);
        return op;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedGTUFollowingTacticalPlanner [carFollowingModel=" + getCarFollowingModel() + "]";
    }
}
