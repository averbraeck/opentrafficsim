package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;

/**
 * Lane-based tactical planner that implements car following behavior. This tactical planner retrieves the car following model
 * from the strategical planner and will generate an operational plan for the GTU.
 * <p>
 * This lane-based tactical planner makes decisions based on headway (GTU following model). It can ask the strategic planner for
 * assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class LaneBasedGtuFollowingTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /**
     * Instantiate a tactical planner with just GTU following behavior and no lane changes.
     * @param carFollowingModel GtuFollowingModelOld; Car-following model.
     * @param gtu LaneBasedGtu; GTU
     */
    public LaneBasedGtuFollowingTacticalPlanner(final GtuFollowingModelOld carFollowingModel, final LaneBasedGtu gtu)
    {
        super(carFollowingModel, gtu, new CategoricalLanePerception(gtu));
        getPerception().addPerceptionCategory(new DirectDefaultSimplePerception(getPerception()));
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, NetworkException, GtuException, ParameterException
    {
        // ask Perception for the local situation
        LaneBasedGtu laneBasedGTU = getGtu();
        LanePerception perception = getPerception();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(getGtu(), locationAtStartTime, startTime, new Duration(1.0, DurationUnit.SECOND));
        }

        // see how far we can drive
        Length maxDistance = laneBasedGTU.getParameters().getParameter(LOOKAHEAD);
        LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, maxDistance);

        // look at the conditions for headway from a GTU in front
        DefaultSimplePerception simplePerception = perception.getPerceptionCategory(DefaultSimplePerception.class);
        Headway headwayGTU = simplePerception.getForwardHeadwayGtu();
        AccelerationStep accelerationStepGTU = null;
        if (headwayGTU.getDistance().ge(maxDistance))
        {
            // TODO I really don't like this -- if there is a lane drop at 20 m, the GTU should stop...
            accelerationStepGTU = ((GtuFollowingModelOld) getCarFollowingModel()).computeAccelerationStepWithNoLeader(
                    laneBasedGTU, lanePathInfo.getPath().getLength(), simplePerception.getSpeedLimit());
        }
        else
        {
            accelerationStepGTU =
                    ((GtuFollowingModelOld) getCarFollowingModel()).computeAccelerationStep(laneBasedGTU, headwayGTU.getSpeed(),
                            headwayGTU.getDistance(), lanePathInfo.getPath().getLength(), simplePerception.getSpeedLimit());
        }

        // look at the conditions for headway from an object in front
        Headway headwayObject = simplePerception.getForwardHeadwayObject();
        AccelerationStep accelerationStepObject = null;
        if (headwayObject.getDistance().ge(maxDistance))
        {
            accelerationStepObject = ((GtuFollowingModelOld) getCarFollowingModel()).computeAccelerationStepWithNoLeader(
                    laneBasedGTU, lanePathInfo.getPath().getLength(), simplePerception.getSpeedLimit());
        }
        else
        {
            accelerationStepObject = ((GtuFollowingModelOld) getCarFollowingModel()).computeAccelerationStep(laneBasedGTU,
                    headwayObject.getSpeed(), headwayObject.getDistance(), lanePathInfo.getPath().getLength(),
                    simplePerception.getSpeedLimit());
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
        return "LaneBasedGtuFollowingTacticalPlanner [carFollowingModel=" + getCarFollowingModel() + "]";
    }
}
