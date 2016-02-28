package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedAltruistic;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedEgoistic;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.directedlanechange.DirectedLaneMovementStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.network.lane.Lane;

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
public class LaneBasedGTUFollowingChange0TacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20160129L;

    /** Earliest next lane change time. */
    private Time.Abs earliestNexLaneChangeTime = Time.Abs.ZERO;

    /**
     * Instantiated a tactical planner with just GTU following behavior and no lane changes.
     */
    public LaneBasedGTUFollowingChange0TacticalPlanner()
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
        LanePerception perception = laneBasedGTU.getPerception();
        LaneBasedDrivingCharacteristics drivingCharacteristics = laneBasedGTU.getDrivingCharacteristics();

        // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
        if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, new Time.Rel(1.0, TimeUnit.SECOND));
        }

        // perceive the forward headway, accessible lanes and speed limit.
        perception.updateForwardHeadwayGTU();
        perception.updateAccessibleAdjacentLanesLeft();
        perception.updateAccessibleAdjacentLanesRight();
        perception.updateSpeedLimit();

        // find out where we are going
        Length.Rel forwardHeadway = drivingCharacteristics.getForwardHeadwayDistance();
        LanePathInfo lanePathInfo = buildLaneListForward(laneBasedGTU, forwardHeadway);
        NextSplitInfo nextSplitInfo = determineNextSplit(laneBasedGTU, forwardHeadway);
        Set<Lane> correctLanes = laneBasedGTU.getLanes().keySet();
        correctLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());

        // Step 1: Do we want to change lanes because of the current lane not leading to our destination?
        if (lanePathInfo.getPath().getLength().lt(forwardHeadway))
        {
            if (correctLanes.isEmpty())
            {
                LateralDirectionality direction = determineLeftRight(laneBasedGTU, nextSplitInfo);
                if (direction != null)
                {
                    if (canChange(laneBasedGTU, perception, lanePathInfo, direction))
                    {
                        DirectedPoint newLocation = changeLane(laneBasedGTU, direction);
                        lanePathInfo = buildLaneListForward(laneBasedGTU, forwardHeadway);
                        return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                    }
                }
            }
        }

        // Condition, if we have just changed lane, let's not change immediately again.
        if (gtu.getSimulator().getSimulatorTime().getTime().lt(this.earliestNexLaneChangeTime))
        {
            return currentLanePlan(laneBasedGTU, startTime, locationAtStartTime, lanePathInfo);
        }

        // Step 2. Do we want to change lanes to the left because of our predecessor on the current lane?
        // does the lane left of us [TODO: driving direction] bring us to our destination as well?
        Set<Lane> leftLanes = perception.getAccessibleAdjacentLanesLeft().get(lanePathInfo.getReferenceLane());
        if (nextSplitInfo.isSplit())
        {
            leftLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());
        }
        if (!leftLanes.isEmpty() && laneBasedGTU.getVelocity().si > 4.0) // XXX we are driving...
        {
            perception.updateBackwardHeadwayGTU();
            perception.updateParallelGTUsLeft();
            perception.updateLaneTrafficLeft();
            if (perception.getParallelGTUsLeft().isEmpty())
            {
                Collection<HeadwayGTU> sameLaneTraffic = new HashSet<>();
                if (perception.getForwardHeadwayGTU() != null && perception.getForwardHeadwayGTU().getGtuId() != null)
                {
                    sameLaneTraffic.add(perception.getForwardHeadwayGTU());
                }
                if (perception.getBackwardHeadwayGTU() != null && perception.getBackwardHeadwayGTU().getGtuId() != null)
                {
                    sameLaneTraffic.add(perception.getBackwardHeadwayGTU());
                }
                DirectedLaneChangeModel dlcm = new DirectedAltruistic();
                DirectedLaneMovementStep dlms =
                    dlcm.computeLaneChangeAndAcceleration(laneBasedGTU, LateralDirectionality.LEFT, sameLaneTraffic,
                        perception.getNeighboringGTUsLeft(), laneBasedGTU.getDrivingCharacteristics()
                            .getForwardHeadwayDistance(), perception.getSpeedLimit(), new Acceleration(1.0,
                            AccelerationUnit.SI), new Acceleration(0.5, AccelerationUnit.SI), new Time.Rel(
                            0.5, TimeUnit.SECOND));
                if (dlms.getLaneChange() != null)
                {
                    if (canChange(laneBasedGTU, perception, lanePathInfo, LateralDirectionality.LEFT))
                    {
                        DirectedPoint newLocation = changeLane(laneBasedGTU, LateralDirectionality.LEFT);
                        lanePathInfo = buildLaneListForward(laneBasedGTU, forwardHeadway);
                        return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                    }
                }
            }
        }

        // Step 3. Do we want to change lanes to the right because of traffic rules?
        Set<Lane> rightLanes = perception.getAccessibleAdjacentLanesRight().get(lanePathInfo.getReferenceLane());
        if (nextSplitInfo.isSplit())
        {
            rightLanes.retainAll(nextSplitInfo.getCorrectCurrentLanes());
        }
        if (!rightLanes.isEmpty() && laneBasedGTU.getVelocity().si > 4.0) // XXX we are driving...
        {
            perception.updateBackwardHeadwayGTU();
            perception.updateParallelGTUsRight();
            perception.updateLaneTrafficRight();
            if (perception.getParallelGTUsRight().isEmpty())
            {
                Collection<HeadwayGTU> sameLaneTraffic = new HashSet<>();
                if (perception.getForwardHeadwayGTU() != null && perception.getForwardHeadwayGTU().getGtuId() != null)
                {
                    sameLaneTraffic.add(perception.getForwardHeadwayGTU());
                }
                if (perception.getBackwardHeadwayGTU() != null && perception.getBackwardHeadwayGTU().getGtuId() != null)
                {
                    sameLaneTraffic.add(perception.getBackwardHeadwayGTU());
                }
                DirectedLaneChangeModel dlcm = new DirectedAltruistic();
                DirectedLaneMovementStep dlms =
                    dlcm.computeLaneChangeAndAcceleration(laneBasedGTU, LateralDirectionality.RIGHT, sameLaneTraffic,
                        perception.getNeighboringGTUsRight(), laneBasedGTU.getDrivingCharacteristics()
                            .getForwardHeadwayDistance(), perception.getSpeedLimit(), new Acceleration(1.0,
                            AccelerationUnit.SI), new Acceleration(0.5, AccelerationUnit.SI), new Time.Rel(
                            0.5, TimeUnit.SECOND));
                if (dlms.getLaneChange() != null)
                {
                    if (canChange(laneBasedGTU, perception, lanePathInfo, LateralDirectionality.RIGHT))
                    {
                        DirectedPoint newLocation = changeLane(laneBasedGTU, LateralDirectionality.RIGHT);
                        lanePathInfo = buildLaneListForward(laneBasedGTU, forwardHeadway);
                        return currentLanePlan(laneBasedGTU, startTime, newLocation, lanePathInfo);
                    }
                }
            }
        }

        return currentLanePlan(laneBasedGTU, startTime, locationAtStartTime, lanePathInfo);
    }

    /**
     * Make a plan for the current lane.
     * @param laneBasedGTU the gtu to generate the plan for
     * @param startTime the time from which the new operational plan has to be operational
     * @param locationAtStartTime the location of the GTU at the start time of the new plan
     * @param lanePathInfo the lane path for the current lane.
     * @return An operation plan for staying in the current lane.
     * @throws OperationalPlanException when there is a problem planning a path in the network
     * @throws GTUException when there is a problem with the state of the GTU when planning a path
     */
    private OperationalPlan currentLanePlan(final LaneBasedGTU laneBasedGTU, final Time.Abs startTime,
        final DirectedPoint locationAtStartTime, final LanePathInfo lanePathInfo) throws OperationalPlanException,
        GTUException
    {
        LanePerception perception = laneBasedGTU.getPerception();
        GTUFollowingModel gfm = laneBasedGTU.getDrivingCharacteristics().getGTUFollowingModel();

        // No lane change. Continue on current lane.
        AccelerationStep accelerationStep;
        if (perception.getForwardHeadwayGTU().getGtuId() == null)
        {
            accelerationStep =
                gfm.computeAccelerationStepWithNoLeader(laneBasedGTU,
                    lanePathInfo.getPath().getLength().minus(laneBasedGTU.getLength().multiplyBy(2.0)),
                    perception.getSpeedLimit());
        }
        else
        {
            accelerationStep =
                gfm.computeAccelerationStep(laneBasedGTU, perception.getForwardHeadwayGTU().getGtuSpeed(), perception
                    .getForwardHeadwayGTU().getDistance(),
                    lanePathInfo.getPath().getLength().minus(laneBasedGTU.getLength().multiplyBy(2.0)), perception
                        .getSpeedLimit());
        }

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6
            && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
        {
            return new OperationalPlan(locationAtStartTime, startTime, accelerationStep.getDuration());
        }

        // build a list of lanes forward, with a maximum headway.
        List<Segment> operationalPlanSegmentList = new ArrayList<>();
        if (accelerationStep.getAcceleration().si == 0.0)
        {
            Segment segment = new OperationalPlan.SpeedSegment(accelerationStep.getDuration());
            operationalPlanSegmentList.add(segment);
        }
        else
        {
            Segment segment =
                new OperationalPlan.AccelerationSegment(accelerationStep.getDuration(),
                    accelerationStep.getAcceleration());
            operationalPlanSegmentList.add(segment);
        }
        OperationalPlan op =
            new OperationalPlan(lanePathInfo.getPath(), startTime, laneBasedGTU.getVelocity(),
                operationalPlanSegmentList);
        return op;
    }

    /**
     * We are not on a lane that leads to our destination. Determine whether the lateral direction to go is left or right.
     * @param laneBasedGTU the gtu
     * @param nextSplitInfo the information about the next split
     * @return the lateral direction to go, or null if this cannot be determined
     */
    private LateralDirectionality
        determineLeftRight(final LaneBasedGTU laneBasedGTU, final NextSplitInfo nextSplitInfo)
    {
        // are the lanes in nextSplitInfo.getCorrectCurrentLanes() left or right of the current lane(s) of the GTU?
        for (Lane correctLane : nextSplitInfo.getCorrectCurrentLanes())
        {
            for (Lane currentLane : laneBasedGTU.getLanes().keySet())
            {
                if (correctLane.getParentLink().equals(currentLane.getParentLink()))
                {
                    double deltaOffset =
                        correctLane.getDesignLineOffsetAtBegin().si - currentLane.getDesignLineOffsetAtBegin().si;
                    if (laneBasedGTU.getLanes().get(currentLane).equals(GTUDirectionality.DIR_PLUS))
                    {
                        return deltaOffset > 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
                    }
                    else
                    {
                        return deltaOffset < 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT;
                    }
                }
            }
        }
        return null;
    }

    /**
     * See if a lane change in the given direction if possible.
     * @param gtu the GTU that has to make the lane change
     * @param perception the perception, where forward headway, accessible lanes and speed limit have been assessed
     * @param lanePathInfo the information for the path on the current lane
     * @param direction the lateral direction, either LEFT or RIGHT
     * @return whether a lane change is possible.
     * @throws NetworkException when there is a network inconsistency in updating the perception
     * @throws GTUException when there is an issue retrieving GTU information for the perception update
     */
    private boolean canChange(final LaneBasedGTU gtu, final LanePerception perception, final LanePathInfo lanePathInfo,
        final LateralDirectionality direction) throws GTUException, NetworkException
    {
        Collection<HeadwayGTU> otherLaneTraffic;
        perception.updateForwardHeadwayGTU();
        perception.updateBackwardHeadwayGTU();
        if (direction.isLeft())
        {
            perception.updateParallelGTUsLeft();
            perception.updateLaneTrafficLeft();
            otherLaneTraffic = perception.getNeighboringGTUsLeft();
        }
        else
        {
            perception.updateParallelGTUsRight();
            perception.updateLaneTrafficRight();
            otherLaneTraffic = perception.getNeighboringGTUsRight();
        }
        if (!perception.parallelGTUs(direction).isEmpty())
        {
            return false;
        }

        Collection<HeadwayGTU> sameLaneTraffic = new HashSet<>();
        if (perception.getForwardHeadwayGTU() != null && perception.getForwardHeadwayGTU().getGtuId() != null)
        {
            sameLaneTraffic.add(perception.getForwardHeadwayGTU());
        }
        if (perception.getBackwardHeadwayGTU() != null && perception.getBackwardHeadwayGTU().getGtuId() != null)
        {
            sameLaneTraffic.add(perception.getBackwardHeadwayGTU());
        }

        // TODO make type of plan (Egoistic, Altruistic) parameter of the class
        DirectedLaneChangeModel dlcm = new DirectedEgoistic();
        // TODO make the elasticities 2.0 and 0.1 parameters of the class
        DirectedLaneMovementStep dlms =
            dlcm.computeLaneChangeAndAcceleration(gtu, direction, sameLaneTraffic, otherLaneTraffic, gtu
                .getDrivingCharacteristics().getForwardHeadwayDistance(), perception.getSpeedLimit(), new Acceleration(
                2.0, AccelerationUnit.SI), new Acceleration(0.1, AccelerationUnit.SI), new Time.Rel(0.5,
                TimeUnit.SECOND));
        if (dlms.getLaneChange() == null)
        {
            return false;
        }

        return true;
    }

    /**
     * Change lanes instantaneously.
     * @param gtu the gtu
     * @param direction the direction
     * @return the new location of the reference point of the GTU
     * @throws GTUException in case the enterlane fails
     */
    private DirectedPoint changeLane(final LaneBasedGTU gtu,
        final LateralDirectionality direction) throws GTUException
    {
        DirectedPoint p = new DirectedPoint();
        Set<Lane> lanes = new HashSet<>(gtu.getLanes().keySet());
        for (Lane lane : lanes)
        {
            Set<Lane> adjacentLanes = lane.accessibleAdjacentLanes(direction, gtu.getGTUType());
            Length.Rel position = gtu.position(lane, gtu.getReference());
            double fraction = lane.fraction(position);
            for (Lane adjacentLane : adjacentLanes)
            {
                gtu.enterLane(adjacentLane, adjacentLane.getLength().multiplyBy(fraction), gtu.getLanes().get(lane));
                p = adjacentLane.getCenterLine().getLocationFractionExtended(fraction);
                System.out.println("gtu " + gtu.getId() + " entered lane " + adjacentLane + " at pos "
                    + adjacentLane.getLength().multiplyBy(fraction));
            }
            gtu.leaveLane(lane);
        }
        // stay at least 15 seconds in the current lane
        this.earliestNexLaneChangeTime =
                gtu.getSimulator().getSimulatorTime().getTime().plus(new Time.Rel(15, TimeUnit.SECOND));
        return p;
    }
}
