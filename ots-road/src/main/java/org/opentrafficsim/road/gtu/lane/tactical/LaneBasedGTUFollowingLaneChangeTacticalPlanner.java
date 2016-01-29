package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;

/**
 * Lane-based tactical planner that implements car following behavior and rule-based lane change. This tactical planner
 * retrieves the car following model from the strategical planner and will generate an operational plan for the GTU.
 * <p>
 * A lane change occurs when:
 * <ol>
 * <li>The route indicates that the current lane does not lead to the destination; main choices are the time when the GTU
 * switches to the "right" lane, and what should happen when the split gets closer and the lane change has failed. Observations
 * indicate that vehicles if necessary stop in their current lane until they can go to the desired lane. A lane drop is
 * automatically part of this implementation, because the lane with a lane drop will not lead to the GTU's destination.</li>
 * <li>The desired speed of the vehicle is a particular delta-speed higher than its predecessor, the headway to the predecessor
 * in the current lane has exceeded a certain value, it is allowed to change to the target lane, the target lane lies on the
 * GTU's route, and the gap in the target lane is acceptable (including the evaluation of the perceived speed of a following GTU
 * in the target lane).</li>
 * <li>The current lane is not the optimum lane given the traffic rules (for example, to keep right), the headway to the
 * predecessor on the target lane is greater than a certain value, the speed of the predecessor on the target lane is greater
 * than or equal to our speed, the target lane is on the route, it is allowed to switch to the target lane, and the gap at the
 * target lane is acceptable (including the perceived speed of any vehicle in front or behind on the target lane).</li>
 * </ol>
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
public class LaneBasedGTUFollowingLaneChangeTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20160129L;

    /**
     * Instantiated a tactical planner with just GTU following behavior and no lane changes.
     */
    public LaneBasedGTUFollowingLaneChangeTacticalPlanner()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, NetworkException, GTUException
    {
        // ask Perception for the local situation
        LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
        LanePerceptionFull perception = laneBasedGTU.getPerception();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, new Time.Rel(1.0, TimeUnit.SECOND));
        }

        // perceive every time step... This is the 'classical' way of tactical planning.
        perception.perceive();

        // Step 1: Do we want to change lanes because of the current lane not leading to our destination?
        

        // Step 2. Do we want to change lanes because of our predecessor on the current lane?

        
        // Step 3. Do we want to change lanes because of traffic rules?

        
        // get some models to help us make a plan
        GTUFollowingModel gtuFollowingModel =
            laneBasedGTU.getStrategicalPlanner().getDrivingCharacteristics().getGTUFollowingModel();

        // look at the conditions for headway
        HeadwayGTU headwayGTU = perception.getForwardHeadwayGTU();
        AccelerationStep accelerationStep = null;
        if (headwayGTU.getGTU() == null)
        {
            accelerationStep =
                gtuFollowingModel.computeAccelerationWithNoLeader(laneBasedGTU, perception.getSpeedLimit());
        }
        else
        {
            // TODO do not use the velocity of the other GTU, but the PERCEIVED velocity
            accelerationStep =
                gtuFollowingModel.computeAcceleration(laneBasedGTU, headwayGTU.getGTU().getVelocity(),
                    headwayGTU.getDistance(), perception.getSpeedLimit());
        }

        // TODO put this in the AccelerationStep class
        Time.Rel duration = accelerationStep.getValidUntil().minus(gtu.getSimulator().getSimulatorTime().getTime());

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6
            && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, duration);
        }

        // build a list of lanes forward, with a maximum headway.
        LanePathInfo lpi =
            buildLaneListForward(laneBasedGTU, laneBasedGTU.getDrivingCharacteristics().getForwardHeadwayDistance());
        OTSLine3D path = lpi.getPath();

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
        // CHECK start
        double t = accelerationStep.getValidUntil().minus(gtu.getSimulator().getSimulatorTime().get()).si;
        double s = gtu.getVelocity().si * t + 0.5 * accelerationStep.getAcceleration().si * t * t;
        if (path.getLengthSI() < s)
        {
            System.err.println("path for GTU " + laneBasedGTU + " is too short: path length is " + path.getLengthSI()
                + ", s is " + s + ", lanes = " + lpi.getLaneList());
        }
        // CHECK end
        OperationalPlan op = new OperationalPlan(path, startTime, gtu.getVelocity(), operationalPlanSegmentList);
        return op;
    }
}
