package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.AccelerationVector;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneMovementStep;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Sensor;
import org.opentrafficsim.road.network.lane.SinkSensor;

/**
 * Lane-based tactical planner that implements car following and lane change behavior. This lane-based tactical planner makes
 * decisions based on headway (GTU following model) and lane change (Lane Change model), and will generate an operational plan
 * for the GTU. It can ask the strategic planner for assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 25, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBasedCFLCTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{
    /** */
    private static final long serialVersionUID = 20151125L;

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration STAYINCURRENTLANEINCENTIVE = new Acceleration(0.1, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration PREFERREDLANEINCENTIVE = new Acceleration(0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration NONPREFERREDLANEINCENTIVE = new Acceleration(-0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Return value of suitability when no lane change is required within the time horizon. */
    public static final Length.Rel NOLANECHANGENEEDED = new Length.Rel(Double.MAX_VALUE, LengthUnit.SI);

    /** Return value of suitability when a lane change is required <i>right now</i>. */
    public static final Length.Rel GETOFFTHISLANENOW = Length.Rel.ZERO;

    /** Standard time horizon for route choices. */
    private static final Time.Rel TIMEHORIZON = new Time.Rel(90, TimeUnit.SECOND);

    /**
     * Instantiated a tactical planner with GTU following and lane change behavior.
     */
    public LaneBasedCFLCTacticalPlanner()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public OperationalPlan generateOperationalPlan(final GTU gtu, final Time.Abs startTime,
            final DirectedPoint locationAtStartTime) throws OperationalPlanException, NetworkException, GTUException
    {
        try
        {
            // define some basic variables
            LaneBasedGTU laneBasedGTU = (LaneBasedGTU) gtu;
            LanePerceptionFull perception = laneBasedGTU.getPerception();

            // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
            if (laneBasedGTU.getMaximumVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                return new OperationalPlan(gtu, locationAtStartTime, startTime, new Time.Rel(1.0, TimeUnit.SECOND));
            }

            // perceive every time step... This is the 'classical' way of tactical planning.
            // NOTE: delete this if perception takes place independent of the tactical planning (different frequency)
            perception.perceive();

            Length.Rel maximumForwardHeadway = laneBasedGTU.getBehavioralCharacteristics().getForwardHeadwayDistance();
            Length.Rel maximumReverseHeadway = laneBasedGTU.getBehavioralCharacteristics().getBackwardHeadwayDistance();
            Time.Abs now = gtu.getSimulator().getSimulatorTime().getTime();
            Speed speedLimit = perception.getSpeedLimit();

            // get some models to help us make a plan
            GTUFollowingModelOld gtuFollowingModel =
                    laneBasedGTU.getStrategicalPlanner().getDrivingCharacteristics().getGTUFollowingModel();
            LaneChangeModel laneChangeModel =
                    laneBasedGTU.getStrategicalPlanner().getDrivingCharacteristics().getLaneChangeModel();

            // look at the conditions for headway on the current lane
            HeadwayGTU sameLaneLeader = perception.getForwardHeadwayGTU();
            HeadwayGTU sameLaneFollower = perception.getBackwardHeadwayGTU();
            Collection<HeadwayGTU> sameLaneTraffic = new ArrayList<HeadwayGTU>();
            if (null != sameLaneLeader.getGtuId())
            {
                sameLaneTraffic.add(sameLaneLeader);
            }
            if (null != sameLaneFollower.getGtuId())
            {
                sameLaneTraffic.add(new HeadwayGTU(sameLaneFollower.getGtuId(), sameLaneFollower.getGtuSpeed(),
                        sameLaneFollower.getDistance().si, sameLaneFollower.getGtuType()));
            }

            // Are we in the right lane for the route?
            LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, maximumForwardHeadway);
            NextSplitInfo nextSplitInfo = determineNextSplit(laneBasedGTU, maximumForwardHeadway);
            boolean currentLaneFine = nextSplitInfo.getCorrectCurrentLanes().contains(lanePathInfo.getReferenceLane());

            // calculate the lane change step
            // TODO skip if:
            // - we are in the right lane and drive at max speed or we accelerate maximally
            // - there are no other lanes
            Collection<HeadwayGTU> leftLaneTraffic = perception.getNeighboringGTUsLeft();
            Collection<HeadwayGTU> rightLaneTraffic = perception.getNeighboringGTUsRight();

            // FIXME: whether we drive on the right should be stored in some central place.
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final Acceleration defaultLeftLaneIncentive =
                    LateralDirectionality.LEFT == preferred ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;
            final Acceleration defaultRightLaneIncentive =
                    LateralDirectionality.RIGHT == preferred ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;
            
            AccelerationVector defaultLaneIncentives =
                    new AccelerationVector(new double[] { defaultLeftLaneIncentive.getSI(), STAYINCURRENTLANEINCENTIVE.getSI(),
                            defaultRightLaneIncentive.getSI() }, AccelerationUnit.SI, StorageType.DENSE);
            AccelerationVector laneIncentives = laneIncentives(laneBasedGTU, defaultLaneIncentives);
            LaneMovementStep lcmr =
                    laneChangeModel.computeLaneChangeAndAcceleration(laneBasedGTU, sameLaneTraffic, rightLaneTraffic,
                            leftLaneTraffic, speedLimit,
                            new Acceleration(laneIncentives.get(preferred == LateralDirectionality.RIGHT ? 2 : 0)),
                            new Acceleration(laneIncentives.get(1)),
                            new Acceleration(laneIncentives.get(preferred == LateralDirectionality.RIGHT ? 0 : 2)));
            Time.Rel duration = lcmr.getGfmr().getValidUntil().minus(gtu.getSimulator().getSimulatorTime().getTime());
            // if ("1".equals(gtu.getId()))
            // {
            // System.out.println(String.format("%s: laneIncentives %s, lcmr %s", gtu, laneIncentives, lcmr));
            // }
            if (lcmr.getLaneChange() != null)
            {
                Collection<Lane> oldLaneSet = new HashSet<Lane>(laneBasedGTU.getLanes().keySet());
                Collection<Lane> newLaneSet = new HashSet<Lane>(2);
                Map<Lane, Double> oldFractionalPositions = new LinkedHashMap<Lane, Double>();
                for (Lane l : laneBasedGTU.getLanes().keySet())
                {
                    oldFractionalPositions.put(l, laneBasedGTU.fractionalPosition(l, gtu.getReference(), now));
                    if (lcmr.getLaneChange().equals(LateralDirectionality.LEFT))
                    {
                        newLaneSet.addAll(laneBasedGTU.getPerception().getAccessibleAdjacentLanesLeft().get(l));
                    }
                    else
                    {
                        newLaneSet.addAll(laneBasedGTU.getPerception().getAccessibleAdjacentLanesRight().get(l));
                    }
                }
                // Add this GTU to the lanes in newLaneSet.
                // This could be rewritten to be more efficient.
                for (Lane newLane : newLaneSet)
                {
                    Double fractionalPosition = null;
                    // find ONE lane in oldLaneSet that has l as neighbor
                    Lane foundOldLane = null;
                    for (Lane oldLane : oldLaneSet)
                    {
                        if ((lcmr.getLaneChange().equals(LateralDirectionality.LEFT) && laneBasedGTU.getPerception()
                                .getAccessibleAdjacentLanesLeft().get(oldLane).contains(newLane))
                                || (lcmr.getLaneChange().equals(LateralDirectionality.RIGHT) && laneBasedGTU.getPerception()
                                        .getAccessibleAdjacentLanesRight().get(oldLane).contains(newLane)))
                        {
                            fractionalPosition = oldFractionalPositions.get(oldLane);
                            foundOldLane = oldLane;
                            break;
                        }
                    }
                    if (null == fractionalPosition)
                    {
                        throw new Error("Program error: Cannot find an oldLane that has newLane " + newLane + " as "
                                + lcmr.getLaneChange() + " neighbor");
                    }
                    laneBasedGTU.enterLane(newLane, newLane.getLength().multiplyBy(fractionalPosition), laneBasedGTU.getLanes()
                            .get(foundOldLane));
                }
                // if ("41".equals(gtu.getId()) && oldLaneSet.size() > 1)
                // {
                // System.out.println("Problem");
                // AbstractLaneBasedGTU lbg = (AbstractLaneBasedGTU) gtu;
                // for (Lane l : lbg.getLanes().keySet())
                // {
                // System.out.println("reference on lane " + l + " is "
                // + lbg.position(l, RelativePosition.REFERENCE_POSITION) + " length of lane is " + l.getLength());
                // }
                // }
                System.out.println(gtu + " changes lane from " + oldLaneSet + " to " + newLaneSet + " at time "
                        + gtu.getSimulator().getSimulatorTime().get());
                // Remove this GTU from all of the Lanes that it is on and remember the fractional position on each
                // one
                for (Lane l : oldFractionalPositions.keySet())
                {
                    laneBasedGTU.leaveLane(l);
                }
                // create the path to drive in this timestep.
                lanePathInfo = buildLanePathInfo(laneBasedGTU, maximumForwardHeadway);

                // System.out.println("lane incentives: " + laneIncentives);
                // build a list of lanes forward, with a maximum headway.
                if (lcmr.getGfmr().getAcceleration().si < 1E-6
                        && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
                {
                    // TODO Make a 100% lateral move from standing still...
                    return new OperationalPlan(gtu, locationAtStartTime, startTime, duration);
                }

                // TODO make a gradual lateral move
                OTSLine3D path = lanePathInfo.getPath();
                List<Segment> operationalPlanSegmentList = new ArrayList<>();
                if (lcmr.getGfmr().getAcceleration().si == 0.0)
                {
                    Segment segment = new OperationalPlan.SpeedSegment(duration);
                    operationalPlanSegmentList.add(segment);
                }
                else
                {
                    Segment segment = new OperationalPlan.AccelerationSegment(duration, lcmr.getGfmr().getAcceleration());
                    operationalPlanSegmentList.add(segment);
                }
                OperationalPlan op = new OperationalPlan(gtu, path, startTime, gtu.getVelocity(), operationalPlanSegmentList);
                return op;
            }
            else
            // NO LANE CHANGE
            {
                // see if we have to continue standing still. In that case, generate a stand still plan
                if (lcmr.getGfmr().getAcceleration().si < 1E-6
                        && laneBasedGTU.getVelocity().si < OperationalPlan.DRIFTING_SPEED_SI)
                {
                    return new OperationalPlan(gtu, locationAtStartTime, startTime, duration);
                }
                // build a list of lanes forward, with a maximum headway.
                OTSLine3D path = lanePathInfo.getPath();
                List<Segment> operationalPlanSegmentList = new ArrayList<>();
                if (lcmr.getGfmr().getAcceleration().si == 0.0)
                {
                    Segment segment = new OperationalPlan.SpeedSegment(duration);
                    operationalPlanSegmentList.add(segment);
                }
                else
                {
                    Segment segment = new OperationalPlan.AccelerationSegment(duration, lcmr.getGfmr().getAcceleration());
                    operationalPlanSegmentList.add(segment);
                }
                OperationalPlan op = new OperationalPlan(gtu, path, startTime, gtu.getVelocity(), operationalPlanSegmentList);
                return op;
            }
        }
        catch (ValueException exception)
        {
            throw new GTUException(exception);
        }
    }

    /**
     * TODO: move laneIncentives to LanePerception? Figure out if the default lane incentives are OK, or override them with
     * values that should keep this GTU on the intended route.
     * @param gtu the GTU for which to calculate the incentives
     * @param defaultLaneIncentives AccelerationVector; the three lane incentives for the next left adjacent lane, the current
     *            lane and the next right adjacent lane
     * @return AccelerationVector; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     * @throws GTUException when the position of the GTU cannot be correctly determined
     */
    private AccelerationVector laneIncentives(final LaneBasedGTU gtu, final AccelerationVector defaultLaneIncentives)
            throws NetworkException, ValueException, GTUException
    {
        Length.Rel leftSuitability = suitability(gtu, LateralDirectionality.LEFT);
        Length.Rel currentSuitability = suitability(gtu, null);
        Length.Rel rightSuitability = suitability(gtu, LateralDirectionality.RIGHT);
        if (leftSuitability == NOLANECHANGENEEDED && currentSuitability == NOLANECHANGENEEDED
                && rightSuitability == NOLANECHANGENEEDED)
        {
            return checkLaneDrops(gtu, defaultLaneIncentives);
        }
        if ((leftSuitability == NOLANECHANGENEEDED || leftSuitability == GETOFFTHISLANENOW)
                && currentSuitability == NOLANECHANGENEEDED
                && (rightSuitability == NOLANECHANGENEEDED || rightSuitability == GETOFFTHISLANENOW))
        {
            return checkLaneDrops(gtu, new AccelerationVector(new double[] { acceleration(gtu, leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability) }, AccelerationUnit.SI,
                    StorageType.DENSE));
        }
        if (currentSuitability == NOLANECHANGENEEDED)
        {
            return new AccelerationVector(new double[] { acceleration(gtu, leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability) }, AccelerationUnit.SI,
                    StorageType.DENSE);
        }
        return new AccelerationVector(new double[] { acceleration(gtu, leftSuitability), acceleration(gtu, currentSuitability),
                acceleration(gtu, rightSuitability) }, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU from running out
     * of road at an upcoming lane drop.
     * @param gtu the GTU for which to check the lane drops
     * @param defaultLaneIncentives DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives for the next left
     *            adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     * @throws GTUException when the positions of the GTU cannot be determined
     */
    private AccelerationVector checkLaneDrops(final LaneBasedGTU gtu, final AccelerationVector defaultLaneIncentives)
            throws NetworkException, ValueException, GTUException
    {
        // FIXME: these comparisons to -10 is ridiculous.
        Length.Rel leftSuitability =
                Double.isNaN(defaultLaneIncentives.get(0).si) || defaultLaneIncentives.get(0).si < -10 ? GETOFFTHISLANENOW
                        : laneDrop(gtu, LateralDirectionality.LEFT);
        Length.Rel currentSuitability = laneDrop(gtu, null);
        Length.Rel rightSuitability =
                Double.isNaN(defaultLaneIncentives.get(2).si) || defaultLaneIncentives.get(2).si < -10 ? GETOFFTHISLANENOW
                        : laneDrop(gtu, LateralDirectionality.RIGHT);
        // @formatter:off
        if ((leftSuitability == NOLANECHANGENEEDED || leftSuitability == GETOFFTHISLANENOW)
                && currentSuitability == NOLANECHANGENEEDED
                && (rightSuitability == NOLANECHANGENEEDED || rightSuitability == GETOFFTHISLANENOW))
        {
            return defaultLaneIncentives;
        }
        // @formatter:on
        if (currentSuitability == NOLANECHANGENEEDED)
        {
            return new AccelerationVector(new double[] { acceleration(gtu, leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability) }, AccelerationUnit.SI,
                    StorageType.DENSE);
        }
        if (currentSuitability.le(leftSuitability))
        {
            return new AccelerationVector(new double[] { PREFERREDLANEINCENTIVE.getSI(), NONPREFERREDLANEINCENTIVE.getSI(),
                    GETOFFTHISLANENOW.getSI() }, AccelerationUnit.SI, StorageType.DENSE);
        }
        if (currentSuitability.le(rightSuitability))
        {
            return new AccelerationVector(new double[] { GETOFFTHISLANENOW.getSI(), NONPREFERREDLANEINCENTIVE.getSI(),
                    PREFERREDLANEINCENTIVE.getSI() }, AccelerationUnit.SI, StorageType.DENSE);
        }
        return new AccelerationVector(new double[] { acceleration(gtu, leftSuitability), acceleration(gtu, currentSuitability),
                acceleration(gtu, rightSuitability) }, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Return the distance until the next lane drop in the specified (nearby) lane.
     * @param gtu the GTU to determine the next lane drop for
     * @param direction LateralDirectionality; one of the values <cite>LateralDirectionality.LEFT</cite> (use the left-adjacent
     *            lane), or <cite>LateralDirectionality.RIGHT</cite> (use the right-adjacent lane), or <cite>null</cite> (use
     *            the current lane)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; distance until the next lane drop if it occurs within the TIMEHORIZON, or
     *         LaneBasedRouteNavigator.NOLANECHANGENEEDED if this lane can be followed until the next split junction or until
     *         beyond the TIMEHORIZON
     * @throws NetworkException on network inconsistency
     * @throws GTUException when the positions of the GTU cannot be determined
     */
    private Length.Rel laneDrop(final LaneBasedGTU gtu, final LateralDirectionality direction) throws NetworkException,
            GTUException
    {
        Lane lane = null;
        Length.Rel longitudinalPosition = null;
        Map<Lane, Length.Rel> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction)
        {
            for (Lane l : gtu.getLanes().keySet())
            {
                if (l.getLaneType().isCompatible(gtu.getGTUType()))
                {
                    lane = l;
                }
            }
            if (null == lane)
            {
                throw new NetworkException(this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        }
        else
        {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            lane = gtu.getPerception().bestAccessibleAdjacentLane(lane, direction, longitudinalPosition); // XXX correct??
        }
        if (null == lane)
        {
            return GETOFFTHISLANENOW;
        }
        double remainingLength = lane.getLength().getSI() - longitudinalPosition.getSI();
        double remainingTimeSI = TIMEHORIZON.getSI() - remainingLength / lane.getSpeedLimit(gtu.getGTUType()).getSI();
        while (remainingTimeSI >= 0)
        {
            for (Sensor s : lane.getSensors())
            {
                if (s instanceof SinkSensor)
                {
                    return NOLANECHANGENEEDED;
                }
            }
            int branching = lane.nextLanes(gtu.getGTUType()).size();
            if (branching == 0)
            {
                return new Length.Rel(remainingLength, LengthUnit.SI);
            }
            if (branching > 1)
            {
                return NOLANECHANGENEEDED;
            }
            lane = lane.nextLanes(gtu.getGTUType()).keySet().iterator().next();
            remainingTimeSI -= lane.getLength().getSI() / lane.getSpeedLimit(gtu.getGTUType()).getSI();
            remainingLength += lane.getLength().getSI();
        }
        return NOLANECHANGENEEDED;
    }

    /**
     * TODO: move suitability to LanePerception? Return the suitability for the current lane, left adjacent lane or right
     * adjacent lane.
     * @param gtu the GTU for which to calculate the incentives
     * @param direction LateralDirectionality; one of the values <cite>null</cite>, <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the suitability of the lane for reaching the (next) destination
     * @throws NetworkException on network inconsistency
     * @throws GTUException when position cannot be determined
     */
    private Length.Rel suitability(final LaneBasedGTU gtu, final LateralDirectionality direction) throws NetworkException,
            GTUException
    {
        Lane lane = null;
        Length.Rel longitudinalPosition = null;
        Map<Lane, Length.Rel> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction)
        {
            for (Lane l : gtu.getLanes().keySet())
            {
                if (l.getLaneType().isCompatible(gtu.getGTUType()))
                {
                    lane = l;
                }
            }
            if (null == lane)
            {
                throw new NetworkException(this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        }
        else
        {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            // if ("1051".equals(gtu.getId()) && "Lane lane.1 of FirstVia to SecondVia".equals(lane.toString())
            // && longitudinalPosition.si >= -1.85699 && longitudinalPosition.si < 0)
            // {
            // System.out.println(gtu + " direction " + direction + " position on " + lane + " is " + longitudinalPosition);
            // }
            lane = gtu.getPerception().bestAccessibleAdjacentLane(lane, direction, longitudinalPosition); // XXX correct??
            // These nested if statements can be combined, but that would reduce readability of the code
            if (null != lane)
            {
                // Cancel lane change opportunity if front or rear of the GTU is not able to make the lane change
                if ((!canChangeLane(gtu.positions(gtu.getRear()), positions, gtu, direction))
                        || (!canChangeLane(gtu.positions(gtu.getFront()), positions, gtu, direction)))
                {
                    // System.out.println("Canceling " + direction + " lane change opportunity for " + gtu);
                    lane = null;
                }
            }
        }
        if (null == lane)
        {
            return GETOFFTHISLANENOW;
        }
        try
        {
            return suitability(lane, longitudinalPosition, gtu, TIMEHORIZON);
        }
        catch (NetworkException ne)
        {
            System.err.println(gtu + " has a route problem in suitability: " + ne.getMessage());
            return NOLANECHANGENEEDED;
        }
    }

    /**
     * Check that front or back of GTU can (also) change lane.
     * @param laneMap Map&lt;Lane, Length.Rel&gt;; Map of lanes that the front or back GTU is in
     * @param positions Map&lt;Lane, Length&gt;; Map of lanes that the reference point of the GTU is in
     * @param gtu LaneBasedGTU; the GTU
     * @param direction LateralDirectionality; direction of the intended lane change
     * @return boolean; true if the lane change appears possible; false if the lane change is not possible for the front or back
     *         of the GTU
     */
    private boolean canChangeLane(final Map<Lane, Length.Rel> laneMap, final Map<Lane, Length.Rel> positions,
            final LaneBasedGTU gtu, final LateralDirectionality direction)
    {
        for (Lane lane : laneMap.keySet())
        {
            Length.Rel positionInLane = laneMap.get(lane);
            if (positionInLane.si < 0 || positionInLane.gt(lane.getLength()))
            {
                continue;
            }
            if (null == gtu.getPerception().bestAccessibleAdjacentLane(lane, direction, positions.get(lane)))
            {
                // System.out.println("Front or back of " + gtu + " cannot change lane");
                return false;
            }
        }
        return true;
    }

    /**
     * Compute deceleration needed to stop at a specified distance.
     * @param gtu the GTU for which to calculate the acceleration to come to a full stop at the distance
     * @param stopDistance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return double; the acceleration (deceleration) needed to stop at the specified distance in m/s/s
     */
    private double acceleration(final LaneBasedGTU gtu, final Length.Rel stopDistance)
    {
        // What is the deceleration that will bring this GTU to a stop at exactly the suitability distance?
        // Answer: a = -v^2 / 2 / suitabilityDistance
        double v = gtu.getVelocity().getSI();
        double a = -v * v / 2 / stopDistance.getSI();
        return a;
    }

    /**
     * Determine the suitability of being at a particular longitudinal position in a particular Lane for following this Route.
     * @param lane Lane; the lane to consider
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position in the lane
     * @param gtu GTU; the GTU (used to check lane compatibility of lanes, and current lane the GTU is on)
     * @param timeHorizon DoubleScalar.Rel&lt;TimeUnit&gt;; the maximum time that a driver may want to look ahead
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; a value that indicates within what distance the GTU should try to vacate this
     *         lane.
     * @throws NetworkException on network inconsistency, or when the continuation Link at a branch cannot be determined
     */
    private Length.Rel suitability(final Lane lane, final Length.Rel longitudinalPosition, final LaneBasedGTU gtu,
            final Time.Rel timeHorizon) throws NetworkException
    {
        double remainingDistance = lane.getLength().getSI() - longitudinalPosition.getSI();
        double spareTime = timeHorizon.getSI() - remainingDistance / lane.getSpeedLimit(gtu.getGTUType()).getSI();
        // Find the first upcoming Node where there is a branch
        Node nextNode = lane.getParentLink().getEndNode();
        Link lastLink = lane.getParentLink();
        Node nextSplitNode = null;
        Lane currentLane = lane;
        CrossSectionLink linkBeforeBranch = lane.getParentLink();
        while (null != nextNode)
        {
            if (spareTime <= 0)
            {
                return NOLANECHANGENEEDED; // It is not yet time to worry; this lane will do as well as any other
            }
            int laneCount = countCompatibleLanes(linkBeforeBranch, gtu.getGTUType());
            if (0 == laneCount)
            {
                throw new NetworkException("No compatible Lanes on Link " + linkBeforeBranch);
            }
            if (1 == laneCount)
            {
                return NOLANECHANGENEEDED; // Only one compatible lane available; we'll get there "automatically";
                // i.e. without influence from the Route
            }
            int branching = nextNode.getLinks().size();
            if (branching > 2)
            { // Found a split
                nextSplitNode = nextNode;
                break;
            }
            else if (1 == branching)
            {
                return NOLANECHANGENEEDED; // dead end; no more choices to make
            }
            else
            { // Look beyond this nextNode
                Link nextLink = gtu.getStrategicalPlanner().nextLinkDirection(nextNode, lastLink, gtu.getGTUType()).getLink();
                if (nextLink instanceof CrossSectionLink)
                {
                    nextNode = nextLink.getEndNode();
                    // Oops: wrong code added the length of linkBeforeBranch in stead of length of nextLink
                    remainingDistance += nextLink.getLength().getSI();
                    linkBeforeBranch = (CrossSectionLink) nextLink;
                    // Figure out the new currentLane
                    if (currentLane.nextLanes(gtu.getGTUType()).size() == 0)
                    {
                        // Lane drop; our lane disappears. This is a compulsory lane change; which is not controlled
                        // by the Route. Perform the forced lane change.
                        if (currentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtu.getGTUType()).size() > 0)
                        {
                            for (Lane adjacentLane : currentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT,
                                    gtu.getGTUType()))
                            {
                                if (adjacentLane.nextLanes(gtu.getGTUType()).size() > 0)
                                {
                                    currentLane = adjacentLane;
                                    break;
                                }
                                // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                                // first in the set
                            }
                        }
                        for (Lane adjacentLane : currentLane.accessibleAdjacentLanes(LateralDirectionality.LEFT,
                                gtu.getGTUType()))
                        {
                            if (adjacentLane.nextLanes(gtu.getGTUType()).size() > 0)
                            {
                                currentLane = adjacentLane;
                                break;
                            }
                            // If there are several adjacent lanes that have non empty nextLanes, we simple take the
                            // first in the set
                        }
                        if (currentLane.nextLanes(gtu.getGTUType()).size() == 0)
                        {
                            throw new NetworkException("Lane ends and there is not a compatible adjacent lane that does "
                                    + "not end");
                        }
                    }
                    // Any compulsory lane change(s) have been performed and there is guaranteed a compatible next lane.
                    for (Lane nextLane : currentLane.nextLanes(gtu.getGTUType()).keySet())
                    {
                        if (nextLane.getLaneType().isCompatible(gtu.getGTUType()))
                        {
                            currentLane = currentLane.nextLanes(gtu.getGTUType()).keySet().iterator().next();
                            break;
                        }
                    }
                    spareTime -= currentLane.getLength().getSI() / currentLane.getSpeedLimit(gtu.getGTUType()).getSI();
                }
                else
                {
                    // There is a non-CrossSectionLink on the path to the next branch. A non-CrossSectionLink does not
                    // have identifiable Lanes, therefore we can't aim for a particular Lane
                    return NOLANECHANGENEEDED; // Any Lane will do equally well
                }
                lastLink = nextLink;
            }
        }
        if (null == nextNode)
        {
            throw new NetworkException("Cannot find the next branch or sink node");
        }
        // We have now found the first upcoming branching Node
        // Which continuing link is the one we need?
        Map<Lane, Length.Rel> suitabilityOfLanesBeforeBranch = new HashMap<Lane, Length.Rel>();
        Link linkAfterBranch =
                gtu.getStrategicalPlanner().nextLinkDirection(nextSplitNode, lastLink, gtu.getGTUType()).getLink();
        for (CrossSectionElement cse : linkBeforeBranch.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtu.getGTUType()))
                {
                    for (Lane connectingLane : l.nextLanes(gtu.getGTUType()).keySet())
                    {
                        if (connectingLane.getParentLink() == linkAfterBranch
                                && connectingLane.getLaneType().isCompatible(gtu.getGTUType()))
                        {
                            Length.Rel currentValue = suitabilityOfLanesBeforeBranch.get(l);
                            // Use recursion to find out HOW suitable this continuation lane is, but don't revert back
                            // to the maximum time horizon (or we could end up in infinite recursion when there are
                            // loops in the network).
                            Length.Rel value =
                                    suitability(connectingLane, new Length.Rel(0, LengthUnit.SI), gtu, new Time.Rel(spareTime,
                                            TimeUnit.SI));
                            // Use the minimum of the value computed for the first split junction (if there is one)
                            // and the value computed for the second split junction.
                            suitabilityOfLanesBeforeBranch.put(l, null == currentValue || value.le(currentValue) ? value
                                    : currentValue);
                        }
                    }
                }
            }
        }
        if (suitabilityOfLanesBeforeBranch.size() == 0)
        {
            throw new NetworkException("No lanes available on Link " + linkBeforeBranch);
        }
        Length.Rel currentLaneSuitability = suitabilityOfLanesBeforeBranch.get(currentLane);
        if (null != currentLaneSuitability)
        {
            return currentLaneSuitability; // Following the current lane will keep us on the Route
        }
        // Performing one or more lane changes (left or right) is required.
        int totalLanes = countCompatibleLanes(currentLane.getParentLink(), gtu.getGTUType());
        Length.Rel leftSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                        LateralDirectionality.LEFT, gtu.getGTUType());
        Length.Rel rightSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                        LateralDirectionality.RIGHT, gtu.getGTUType());
        if (leftSuitability.ge(rightSuitability))
        {
            return leftSuitability;
        }
        else if (rightSuitability.ge(leftSuitability))
        {
            return rightSuitability;
        }
        if (leftSuitability.le(GETOFFTHISLANENOW))
        {
            throw new NetworkException("Changing lanes in any direction does not get the GTU on a suitable lane");
        }
        return leftSuitability; // left equals right; this is odd but topologically possible
    }

    /**
     * Compute the suitability of a lane from which lane changes are required to get to the next point on the Route.<br>
     * This method weighs the suitability of the nearest suitable lane by (m - n) / m where n is the number of lane changes
     * required and m is the total number of lanes in the CrossSectionLink.
     * @param startLane Lane; the current lane of the GTU
     * @param remainingDistance double; distance in m of GTU to first branch
     * @param suitabilities Map&lt;Lane, Double&gt;; the set of suitable lanes and their suitability
     * @param totalLanes integer; total number of lanes compatible with the GTU type
     * @param direction LateralDirectionality; the direction of the lane changes to attempt
     * @param gtuType GTUType; the type of the GTU
     * @return double; the suitability of the <cite>startLane</cite> for following the Route
     */
    protected final Length.Rel computeSuitabilityWithLaneChanges(final Lane startLane, final double remainingDistance,
            final Map<Lane, Length.Rel> suitabilities, final int totalLanes, final LateralDirectionality direction,
            final GTUType gtuType)
    {
        /*-
         * The time per required lane change seems more relevant than distance per required lane change.
         * Total time required does not grow linearly with the number of required lane changes. Logarithmic, arc tangent 
         * is more like it.
         * Rijkswaterstaat appears to use a fixed time for ANY number of lane changes (about 60s). 
         * TomTom navigation systems give more time (about 90s).
         * In this method the returned suitability decreases linearly with the number of required lane changes. This
         * ensures that there is a gradient that coaches the GTU towards the most suitable lane.
         */
        int laneChangesUsed = 0;
        Lane currentLane = startLane;
        Length.Rel currentSuitability = null;
        while (null == currentSuitability)
        {
            laneChangesUsed++;
            if (currentLane.accessibleAdjacentLanes(direction, gtuType).size() == 0)
            {
                return GETOFFTHISLANENOW;
            }
            currentLane = currentLane.accessibleAdjacentLanes(direction, gtuType).iterator().next();
            currentSuitability = suitabilities.get(currentLane);
        }
        double fraction = currentSuitability == NOLANECHANGENEEDED ? 0 : 0.5;
        int notSuitableLaneCount = totalLanes - suitabilities.size();
        return new Length.Rel(remainingDistance * (notSuitableLaneCount - laneChangesUsed + 1 + fraction)
                / (notSuitableLaneCount + fraction), LengthUnit.SI);
    }

    /**
     * Determine how many lanes on a CrossSectionLink are compatible with a particular GTU type.<br>
     * TODO: this method should probably be moved into the CrossSectionLink class
     * @param link CrossSectionLink; the link
     * @param gtuType GTUType; the GTU type
     * @return integer; the number of lanes on the link that are compatible with the GTU type
     */
    protected final int countCompatibleLanes(final CrossSectionLink link, final GTUType gtuType)
    {
        int result = 0;
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtuType))
                {
                    result++;
                }
            }
        }
        return result;
    }
}
