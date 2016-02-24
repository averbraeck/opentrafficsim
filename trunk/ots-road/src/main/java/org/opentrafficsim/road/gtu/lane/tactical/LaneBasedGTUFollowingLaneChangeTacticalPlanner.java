package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
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
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.AccelerationStep;
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
                    OperationalPlan laneChangePlan = makeLaneChangePlan(laneBasedGTU, perception, lanePathInfo, direction);
                    if (laneChangePlan != null)
                    {
                        return laneChangePlan;
                    }
                }
            }
        }

        // TODO Step 2. Do we want to change lanes because of our predecessor on the current lane?

        // TODO Step 3. Do we want to change lanes because of traffic rules?

        // No lane change. Continue on current lane.
        AccelerationStep accelerationStep;
        if (perception.getForwardHeadwayGTU().getGtuId() == null)
        {
            accelerationStep =
                    laneBasedGTU
                            .getDrivingCharacteristics()
                            .getGTUFollowingModel()
                            .computeAccelerationStepWithNoLeader(laneBasedGTU, lanePathInfo.getPath().getLength(),
                                    perception.getSpeedLimit());
        }
        else
        {
            accelerationStep =
                    laneBasedGTU
                            .getDrivingCharacteristics()
                            .getGTUFollowingModel()
                            .computeAccelerationStep(laneBasedGTU, perception.getForwardHeadwayGTU().getGtuSpeed(),
                                    perception.getForwardHeadwayGTU().getDistance(), lanePathInfo.getPath().getLength(),
                                    perception.getSpeedLimit());
        }

        // see if we have to continue standing still. In that case, generate a stand still plan
        if (accelerationStep.getAcceleration().si < 1E-6 && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
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
                    new OperationalPlan.AccelerationSegment(accelerationStep.getDuration(), accelerationStep.getAcceleration());
            operationalPlanSegmentList.add(segment);
        }
        OperationalPlan op =
                new OperationalPlan(lanePathInfo.getPath(), startTime, gtu.getVelocity(), operationalPlanSegmentList);
        return op;
    }

    /**
     * We are not on a lane that leads to our destination. Determine whether the lateral direction to go is left or right.
     * @param laneBasedGTU the gtu
     * @param nextSplitInfo the information about the next split
     * @return the lateral direction to go, or null if this cannot be determined
     */
    private LateralDirectionality determineLeftRight(final LaneBasedGTU laneBasedGTU, final NextSplitInfo nextSplitInfo)
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
     * Make a lane change in the given direction if possible, and return the operational plan, or null if a lane change is not
     * possible.
     * @param gtu the GTU that has to make the lane change
     * @param perception the perception, where forward headway, accessible lanes and speed limit have been assessed
     * @param lanePathInfo the information for the path on the current lane
     * @param direction the lateral direction, either LEFT or RIGHT
     * @return the operational plan for the required lane change, or null if a lane change is not possible.
     * @throws NetworkException when there is a network inconsistency in updating the perception
     * @throws GTUException when there is an issue retrieving GTU information for the perception update
     */
    private OperationalPlan makeLaneChangePlan(final LaneBasedGTU gtu, final LanePerception perception,
            final LanePathInfo lanePathInfo, final LateralDirectionality direction) throws GTUException, NetworkException
    {
        if (direction.isLeft())
        {
            perception.updateParallelGTUsLeft();
            perception.updateLaneTrafficLeft();
        }
        else
        {
            perception.updateParallelGTUsRight();
            perception.updateLaneTrafficRight();
        }
        if (!perception.parallelGTUs(direction).isEmpty())
        {
            return null;
        }

        // how big is the headway on our lane and what about the gap on the target lane?
        HeadwayGTU followerNextLane = null;
        HeadwayGTU leaderNextLane = null;
        Collection<HeadwayGTU> nextLaneTraffic = perception.neighboringGTUCollection(direction);
        for (HeadwayGTU headwayGTU : nextLaneTraffic)
        {
            if (headwayGTU.getDistance().si < 0)
            {
                followerNextLane = headwayGTU;
            }
            if (headwayGTU.getDistance().si > 0)
            {
                leaderNextLane = headwayGTU;
            }
        }
        double followerDistanceSI = followerNextLane == null ? -Double.MAX_VALUE : followerNextLane.getDistance().si;
        double leaderDistanceSI = leaderNextLane == null ? Double.MAX_VALUE : leaderNextLane.getDistance().si;

        // enough? Take 1 second speed + 10 meters
        if (Math.abs(followerDistanceSI) < 10 + gtu.getVelocity().si * 1.0)
        {
            return null; // too dangerous
        }
        if (leaderDistanceSI < 10 + gtu.getVelocity().si * 1.0)
        {
            return null; // too dangerous
        }

        // suppose we spend 2 seconds doing the lane change.

        Lane startLane = getReferenceLane(gtu);
        Set<Lane> adjacentLanes = startLane.accessibleAdjacentLanes(direction, gtu.getGTUType());
        // TODO take the widest (now a random one)
        Lane adjacentLane = adjacentLanes.iterator().next();
        Length.Rel startPosition = gtu.position(startLane, gtu.getReference());
        double fraction2 = startLane.fraction(startPosition);
        LanePathInfo lanePathInfo2 =
                buildLaneListForward(gtu, gtu.getDrivingCharacteristics().getForwardHeadwayDistance(), adjacentLane, fraction2,
                        gtu.getLanes().get(startLane));

        // make a 2 second acc/dec plan on the current lane.
        Speed currentLaneLeaderSpeed =
                perception.getForwardHeadwayGTU() == null || perception.getForwardHeadwayGTU().getGtuSpeed() == null
                        ? perception.getSpeedLimit() : perception.getForwardHeadwayGTU().getGtuSpeed();
        Length.Rel currentLaneLeaderHeadway =
                perception.getForwardHeadwayGTU() == null || perception.getForwardHeadwayGTU().getDistance() == null ? gtu
                        .getDrivingCharacteristics().getForwardHeadwayDistance() : perception.getForwardHeadwayGTU()
                        .getDistance();
        if (currentLaneLeaderHeadway.si > lanePathInfo.getPath().getLengthSI())
        {
            currentLaneLeaderHeadway = lanePathInfo.getPath().getLength();
            currentLaneLeaderSpeed = Speed.ZERO;
        }
        AccelerationStep currentLaneStep =
                gtu.getDrivingCharacteristics()
                        .getGTUFollowingModel()
                        .computeAccelerationStep(gtu, currentLaneLeaderSpeed, currentLaneLeaderHeadway,
                                gtu.getDrivingCharacteristics().getForwardHeadwayDistance(), perception.getSpeedLimit(),
                                new Time.Rel(2.0, TimeUnit.SECOND));

        // make a 2 second acc/dec plan on the alternative lane.
        Speed alternativeLaneLeaderSpeed =
                leaderNextLane == null || leaderNextLane.getGtuSpeed() == null ? perception.getSpeedLimit() : leaderNextLane
                        .getGtuSpeed();
        Length.Rel alternativeLaneLeaderHeadway =
                leaderNextLane == null || leaderNextLane.getDistance() == null ? gtu.getDrivingCharacteristics()
                        .getForwardHeadwayDistance() : leaderNextLane.getDistance();
        AccelerationStep alternativeLaneStep =
                gtu.getDrivingCharacteristics()
                        .getGTUFollowingModel()
                        .computeAccelerationStep(gtu, alternativeLaneLeaderSpeed, alternativeLaneLeaderHeadway,
                                gtu.getDrivingCharacteristics().getForwardHeadwayDistance(), perception.getSpeedLimit(),
                                new Time.Rel(2.0, TimeUnit.SECOND));

        // XXX
        if (gtu.getId().contains("241"))
        {
            System.out.println("241");
        }
        // XXX

        // choose the most conservative one
        AccelerationStep accelerationStep =
                currentLaneStep.getAcceleration().si < alternativeLaneStep.getAcceleration().si ? currentLaneStep
                        : alternativeLaneStep;

        // interpolate the path for the most conservative one
        Speed v0 = gtu.getVelocity();
        double t = accelerationStep.getDuration().si;
        double distanceSI = v0.si * t + 0.5 * accelerationStep.getAcceleration().si * t * t;
        Speed vt = v0.plus(accelerationStep.getAcceleration().multiplyBy(accelerationStep.getDuration()));
        OTSLine3D path;
        try
        {
            path = interpolate(lanePathInfo.getPath(), lanePathInfo2.getPath(), distanceSI);
        }
        catch (OTSGeometryException exception)
        {
            throw new GTUException(exception);
        }

        try
        {
            // enter the other lane(s) at the same fractional position as the current position on the lane(s)
            // schedule leaving the current lane(s) that do not overlap with the target lane(s)
            for (Lane lane : gtu.getLanes().keySet())
            {
                gtu.getSimulator().scheduleEventRel(accelerationStep.getDuration(), this, gtu, "leaveLane",
                        new Object[] { lane });
            }

            gtu.enterLane(adjacentLane, adjacentLane.getLength().multiplyBy(fraction2), gtu.getLanes().get(startLane));
            System.out.println(gtu + " entered lane " + adjacentLane + " at pos "
                    + adjacentLane.getLength().multiplyBy(fraction2));

            return OperationalPlanBuilder.buildGradualAccelerationPlan(path, gtu.getSimulator().getSimulatorTime().getTime(),
                    v0, vt);
        }
        catch (OperationalPlanException | SimRuntimeException exception)
        {
            throw new GTUException(exception);
        }
    }

    /**
     * Linearly interpolate between two lines (can later become S-curve if needed).
     * @param line1 first line
     * @param line2 second line
     * @param lengthSI length of the interpolation (at this point 100% line 2)
     * @return a line between line 1 and line 2
     * @throws OTSGeometryException when interpolation fails
     */
    private static OTSLine3D interpolate(OTSLine3D line1, OTSLine3D line2, final double lengthSI) throws OTSGeometryException
    {
        OTSLine3D l1 = line1.extract(0, lengthSI);
        OTSLine3D l2 = line2.extract(0, lengthSI);
        List<OTSPoint3D> line = new ArrayList<>();
        int num = 32;
        for (int i = 0; i <= num; i++)
        {
            double f0 = 1.0 * i / num;
            double f1 = 1.0 - f0;
            DirectedPoint p1 = l1.getLocationFraction(f0);
            DirectedPoint p2 = l2.getLocationFraction(f0);
            line.add(new OTSPoint3D(p1.x * f1 + p2.x * f0, p1.y * f1 + p2.y * f0, p1.z * f1 + p2.z * f0));
        }
        System.out.println(l1);
        System.out.println(l2);
        System.out.println(line);
        return new OTSLine3D(line);
    }
}
