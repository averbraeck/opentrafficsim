package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;

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
public class LaneBasedCarFollowingTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /**
     * @param strategicalPlanner the strategic planner that has instantiated this tactical planner
     */
    public LaneBasedCarFollowingTacticalPlanner(final LaneBasedStrategicalPlanner strategicalPlanner)
    {
        super(strategicalPlanner);
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, NetworkException, GTUException
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
        LanePerception perception = laneBasedGTU.getPerception();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, new Time.Rel(1.0, TimeUnit.SECOND));
        }

        // perceive every time step... This is the 'classical' way of tactical planning.
        perception.perceive();

        // get some models to help us make a plan
        GTUFollowingModel gtuFollowingModel =
            getStrategicalPlanner().getDrivingCharacteristics().getGTUFollowingModel();

        // look at the conditions for headway
        HeadwayGTU headwayGTU = perception.getForwardHeadwayGTU();
        AccelerationStep accelerationStep = null;
        if (headwayGTU.getOtherGTU() == null)
        {
            accelerationStep =
                gtuFollowingModel.computeAccelerationWithNoLeader(laneBasedGTU, perception.getSpeedLimit());
        }
        else
        {
            // TODO do not use the velocity of the other GTU, but the PERCEIVED velocity
            accelerationStep =
                gtuFollowingModel.computeAcceleration(laneBasedGTU, headwayGTU.getOtherGTU().getVelocity(),
                    headwayGTU.getDistance(), perception.getSpeedLimit());
        }

        Time.Rel duration = accelerationStep.getValidUntil().minus(gtu.getSimulator().getSimulatorTime().getTime());

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6
            && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, duration);
        }

        // build a list of lanes forward, with a maximum headway.
        OTSLine3D path;
        try
        {
            path =
                buildLaneListForward(laneBasedGTU, laneBasedGTU.getDrivingCharacteristics().getForwardHeadwayDistance())
                    .getPath();
        }
        catch (OTSGeometryException exception)
        {
            throw new GTUException(exception);
        }
        List<Segment> operationalPlanSegmentList = new ArrayList<>();
        if (accelerationStep.getAcceleration().si == 0.0)
        {
            Segment segment = new OperationalPlan.SpeedSegment(duration);
            operationalPlanSegmentList.add(segment);
        }
        else
        {
            Segment segment = new OperationalPlan.AccelerationSegment(duration, accelerationStep.getAcceleration());
            operationalPlanSegmentList.add(segment);
        }
        OperationalPlan op = new OperationalPlan(path, startTime, gtu.getVelocity(), operationalPlanSegmentList);
        return op;
    }
}
