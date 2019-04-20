package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.AccelerationVector;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.Segment;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneMovementStep;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Lane-based tactical planner that implements car following and lane change behavior. This lane-based tactical planner makes
 * decisions based on headway (GTU following model) and lane change (Lane Change model), and will generate an operational plan
 * for the GTU. It can ask the strategic planner for assistance on the route to take when the network splits.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Look back parameter type. */
    protected static final ParameterTypeLength LOOKBACKOLD = ParameterTypes.LOOKBACKOLD;

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration STAYINCURRENTLANEINCENTIVE = new Acceleration(0.1, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration PREFERREDLANEINCENTIVE = new Acceleration(0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration NONPREFERREDLANEINCENTIVE = new Acceleration(-0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Return value of suitability when no lane change is required within the time horizon. */
    public static final Length NOLANECHANGENEEDED = new Length(Double.MAX_VALUE, LengthUnit.SI);

    /** Return value of suitability when a lane change is required <i>right now</i>. */
    public static final Length GETOFFTHISLANENOW = Length.ZERO;

    /** Standard time horizon for route choices. */
    private static final Duration TIMEHORIZON = new Duration(90, DurationUnit.SECOND);

    /** Lane change model for this tactical planner. */
    private LaneChangeModel laneChangeModel;

    /**
     * Instantiated a tactical planner with GTU following and lane change behavior.
     * @param carFollowingModel GTUFollowingModelOld; Car-following model.
     * @param laneChangeModel LaneChangeModel; Lane change model.
     * @param gtu LaneBasedGTU; GTU
     */
    public LaneBasedCFLCTacticalPlanner(final GTUFollowingModelOld carFollowingModel, final LaneChangeModel laneChangeModel,
            final LaneBasedGTU gtu)
    {
        super(carFollowingModel, gtu, new CategoricalLanePerception(gtu));
        this.laneChangeModel = laneChangeModel;
        getPerception().addPerceptionCategory(new DirectDefaultSimplePerception(getPerception()));
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, NetworkException, GTUException, ParameterException
    {
        try
        {
            // define some basic variables
            LaneBasedGTU laneBasedGTU = getGtu();
            LanePerception perception = getPerception();

            // if the GTU's maximum speed is zero (block), generate a stand still plan for one second
            if (laneBasedGTU.getMaximumSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                return new OperationalPlan(getGtu(), locationAtStartTime, startTime, new Duration(1.0, DurationUnit.SECOND));
            }

            Length maximumForwardHeadway = laneBasedGTU.getParameters().getParameter(LOOKAHEAD);
            DefaultSimplePerception simplePerception = perception.getPerceptionCategory(DefaultSimplePerception.class);
            Speed speedLimit = simplePerception.getSpeedLimit();

            // look at the conditions for headway on the current lane
            Headway sameLaneLeader = simplePerception.getForwardHeadwayGTU();
            // TODO how to handle objects on this lane or another lane???
            Headway sameLaneFollower = simplePerception.getBackwardHeadway();
            Collection<Headway> sameLaneTraffic = new ArrayList<>();
            if (sameLaneLeader.getObjectType().isGtu())
            {
                sameLaneTraffic.add(sameLaneLeader);
            }
            if (sameLaneFollower.getObjectType().isGtu())
            {
                sameLaneTraffic.add(sameLaneFollower);
            }

            // Are we in the right lane for the route?
            LanePathInfo lanePathInfo = buildLanePathInfo(laneBasedGTU, maximumForwardHeadway);

            // TODO these two info's are not used
            NextSplitInfo nextSplitInfo = determineNextSplit(laneBasedGTU, maximumForwardHeadway);
            boolean currentLaneFine = nextSplitInfo.getCorrectCurrentLanes().contains(lanePathInfo.getReferenceLane());

            // calculate the lane change step
            // TODO skip if:
            // - we are in the right lane and drive at max speed or we accelerate maximally
            // - there are no other lanes
            Collection<Headway> leftLaneTraffic = simplePerception.getNeighboringHeadwaysLeft();
            Collection<Headway> rightLaneTraffic = simplePerception.getNeighboringHeadwaysRight();

            // FIXME: whether we drive on the right should be stored in some central place.
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final Acceleration defaultLeftLaneIncentive =
                    preferred.isLeft() ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;
            final Acceleration defaultRightLaneIncentive =
                    preferred.isRight() ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;

            AccelerationVector defaultLaneIncentives =
                    new AccelerationVector(new double[] {defaultLeftLaneIncentive.getSI(), STAYINCURRENTLANEINCENTIVE.getSI(),
                            defaultRightLaneIncentive.getSI()}, AccelerationUnit.SI, StorageType.DENSE);
            AccelerationVector laneIncentives = laneIncentives(laneBasedGTU, defaultLaneIncentives);
            LaneMovementStep lcmr = this.laneChangeModel.computeLaneChangeAndAcceleration(laneBasedGTU, sameLaneTraffic,
                    rightLaneTraffic, leftLaneTraffic, speedLimit,
                    new Acceleration(laneIncentives.get(preferred.isRight() ? 2 : 0)), new Acceleration(laneIncentives.get(1)),
                    new Acceleration(laneIncentives.get(preferred.isRight() ? 0 : 2)));
            Duration duration = lcmr.getGfmr().getValidUntil().minus(getGtu().getSimulator().getSimulatorTime());
            if (lcmr.getLaneChangeDirection() != null)
            {
                laneBasedGTU.changeLaneInstantaneously(lcmr.getLaneChangeDirection());

                // create the path to drive in this timestep.
                lanePathInfo = buildLanePathInfo(laneBasedGTU, maximumForwardHeadway);
            }

            // incorporate traffic light
            Headway object = simplePerception.getForwardHeadwayObject();
            Acceleration a = lcmr.getGfmr().getAcceleration();
            if (object instanceof HeadwayTrafficLight)
            {
                // if it was perceived, it was red, or yellow and judged as requiring to stop
                a = Acceleration.min(a, ((GTUFollowingModelOld) getCarFollowingModel()).computeAcceleration(getGtu().getSpeed(),
                        getGtu().getMaximumSpeed(), Speed.ZERO, object.getDistance(), speedLimit));
            }

            // incorporate dead-end/split
            Length dist = lanePathInfo.getPath().getLength().minus(getGtu().getFront().getDx());
            a = Acceleration.min(a, ((GTUFollowingModelOld) getCarFollowingModel()).computeAcceleration(getGtu().getSpeed(),
                    getGtu().getMaximumSpeed(), Speed.ZERO, dist, speedLimit));

            // build a list of lanes forward, with a maximum headway.
            OTSLine3D path = lanePathInfo.getPath();
            if (a.si < 1E-6 && laneBasedGTU.getSpeed().si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                try
                {
                    return new OperationalPlan(getGtu(), path.getLocationFraction(0.0), startTime, duration);
                }
                catch (OTSGeometryException exception)
                {
                    // should not happen as 0.0 should be accepted
                    throw new RuntimeException(exception);
                }
            }
            List<Segment> operationalPlanSegmentList = new ArrayList<>();

            if (a.si == 0.0)
            {
                Segment segment = new OperationalPlan.SpeedSegment(duration);
                operationalPlanSegmentList.add(segment);
            }
            else
            {
                Segment segment = new OperationalPlan.AccelerationSegment(duration, a);
                operationalPlanSegmentList.add(segment);
            }
            OperationalPlan op =
                    new OperationalPlan(getGtu(), path, startTime, getGtu().getSpeed(), operationalPlanSegmentList);
            return op;
        }
        catch (ValueException exception)
        {
            throw new GTUException(exception);
        }
    }

    /**
     * TODO: move laneIncentives to LanePerception? Figure out if the default lane incentives are OK, or override them with
     * values that should keep this GTU on the intended route.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the incentives
     * @param defaultLaneIncentives AccelerationVector; the three lane incentives for the next left adjacent lane, the current
     *            lane and the next right adjacent lane
     * @return AccelerationVector; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     * @throws GTUException when the position of the GTU cannot be correctly determined
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    private AccelerationVector laneIncentives(final LaneBasedGTU gtu, final AccelerationVector defaultLaneIncentives)
            throws NetworkException, ValueException, GTUException, OperationalPlanException
    {
        Length leftSuitability = suitability(gtu, LateralDirectionality.LEFT);
        Length currentSuitability = suitability(gtu, null);
        Length rightSuitability = suitability(gtu, LateralDirectionality.RIGHT);
        if (leftSuitability == NOLANECHANGENEEDED && currentSuitability == NOLANECHANGENEEDED
                && rightSuitability == NOLANECHANGENEEDED)
        {
            return checkLaneDrops(gtu, defaultLaneIncentives);
        }
        if ((leftSuitability == NOLANECHANGENEEDED || leftSuitability == GETOFFTHISLANENOW)
                && currentSuitability == NOLANECHANGENEEDED
                && (rightSuitability == NOLANECHANGENEEDED || rightSuitability == GETOFFTHISLANENOW))
        {
            return checkLaneDrops(gtu,
                    new AccelerationVector(new double[] {acceleration(gtu, leftSuitability),
                            defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability)}, AccelerationUnit.SI,
                            StorageType.DENSE));
        }
        if (currentSuitability == NOLANECHANGENEEDED)
        {
            return new AccelerationVector(new double[] {acceleration(gtu, leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability)}, AccelerationUnit.SI,
                    StorageType.DENSE);
        }
        return new AccelerationVector(new double[] {acceleration(gtu, leftSuitability), acceleration(gtu, currentSuitability),
                acceleration(gtu, rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU from running out
     * of road at an upcoming lane drop.
     * @param gtu LaneBasedGTU; the GTU for which to check the lane drops
     * @param defaultLaneIncentives AccelerationVector; DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives
     *            for the next left adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     * @throws GTUException when the positions of the GTU cannot be determined
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    private AccelerationVector checkLaneDrops(final LaneBasedGTU gtu, final AccelerationVector defaultLaneIncentives)
            throws NetworkException, ValueException, GTUException, OperationalPlanException
    {
        // FIXME: these comparisons to -10 is ridiculous.
        Length leftSuitability = Double.isNaN(defaultLaneIncentives.get(0).si) || defaultLaneIncentives.get(0).si < -10
                ? GETOFFTHISLANENOW : laneDrop(gtu, LateralDirectionality.LEFT);
        Length currentSuitability = laneDrop(gtu, null);
        Length rightSuitability = Double.isNaN(defaultLaneIncentives.get(2).si) || defaultLaneIncentives.get(2).si < -10
                ? GETOFFTHISLANENOW : laneDrop(gtu, LateralDirectionality.RIGHT);
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
            return new AccelerationVector(new double[] {acceleration(gtu, leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(gtu, rightSuitability)}, AccelerationUnit.SI,
                    StorageType.DENSE);
        }
        if (currentSuitability.le(leftSuitability))
        {
            return new AccelerationVector(
                    new double[] {PREFERREDLANEINCENTIVE.getSI(), NONPREFERREDLANEINCENTIVE.getSI(), GETOFFTHISLANENOW.getSI()},
                    AccelerationUnit.SI, StorageType.DENSE);
        }
        if (currentSuitability.le(rightSuitability))
        {
            return new AccelerationVector(
                    new double[] {GETOFFTHISLANENOW.getSI(), NONPREFERREDLANEINCENTIVE.getSI(), PREFERREDLANEINCENTIVE.getSI()},
                    AccelerationUnit.SI, StorageType.DENSE);
        }
        return new AccelerationVector(new double[] {acceleration(gtu, leftSuitability), acceleration(gtu, currentSuitability),
                acceleration(gtu, rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Return the distance until the next lane drop in the specified (nearby) lane.
     * @param gtu LaneBasedGTU; the GTU to determine the next lane drop for
     * @param direction LateralDirectionality; one of the values <cite>LateralDirectionality.LEFT</cite> (use the left-adjacent
     *            lane), or <cite>LateralDirectionality.RIGHT</cite> (use the right-adjacent lane), or <cite>null</cite> (use
     *            the current lane)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; distance until the next lane drop if it occurs within the TIMEHORIZON, or
     *         LaneBasedRouteNavigator.NOLANECHANGENEEDED if this lane can be followed until the next split junction or until
     *         beyond the TIMEHORIZON
     * @throws NetworkException on network inconsistency
     * @throws GTUException when the positions of the GTU cannot be determined
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    private Length laneDrop(final LaneBasedGTU gtu, final LateralDirectionality direction)
            throws NetworkException, GTUException, OperationalPlanException
    {
        DirectedLanePosition dlp = gtu.getReferencePosition();
        Lane lane = dlp.getLane();
        Length longitudinalPosition = dlp.getPosition();
        if (null != direction)
        {
            lane = getPerception().getPerceptionCategory(DefaultSimplePerception.class).bestAccessibleAdjacentLane(lane,
                    direction, longitudinalPosition);
        }
        if (null == lane)
        {
            return GETOFFTHISLANENOW;
        }
        double remainingLength = lane.getLength().getSI() - longitudinalPosition.getSI();
        double remainingTimeSI = TIMEHORIZON.getSI() - remainingLength / lane.getSpeedLimit(gtu.getGTUType()).getSI();
        while (remainingTimeSI >= 0)
        {
            for (SingleSensor s : lane.getSensors())
            {
                if (s instanceof SinkSensor)
                {
                    return NOLANECHANGENEEDED;
                }
            }
            int branching = lane.nextLanes(gtu.getGTUType()).size();
            if (branching == 0)
            {
                return new Length(remainingLength, LengthUnit.SI);
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
     * @param gtu LaneBasedGTU; the GTU for which to calculate the incentives
     * @param direction LateralDirectionality; one of the values <cite>null</cite>, <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the suitability of the lane for reaching the (next) destination
     * @throws NetworkException on network inconsistency
     * @throws GTUException when position cannot be determined
     * @throws OperationalPlanException if DefaultAlexander perception category is not present
     */
    private Length suitability(final LaneBasedGTU gtu, final LateralDirectionality direction)
            throws NetworkException, GTUException, OperationalPlanException
    {
        DirectedLanePosition dlp = gtu.getReferencePosition();
        Lane lane = dlp.getLane();
        Length longitudinalPosition = dlp.getPosition().plus(gtu.getFront().getDx());
        if (null != direction)
        {
            lane = getPerception().getPerceptionCategory(DefaultSimplePerception.class).bestAccessibleAdjacentLane(lane,
                    direction, longitudinalPosition);
        }
        if (null == lane)
        {
            return GETOFFTHISLANENOW;
        }
        try
        {
            return suitability(lane, longitudinalPosition, gtu, TIMEHORIZON);
            // return suitability(lane, lane.getLength().minus(longitudinalPosition), gtu, TIMEHORIZON);
        }
        catch (NetworkException ne)
        {
            System.err.println(gtu + " has a route problem in suitability: " + ne.getMessage());
            return NOLANECHANGENEEDED;
        }
    }

    /**
     * Compute deceleration needed to stop at a specified distance.
     * @param gtu LaneBasedGTU; the GTU for which to calculate the acceleration to come to a full stop at the distance
     * @param stopDistance Length; the distance
     * @return double; the acceleration (deceleration) needed to stop at the specified distance in m/s/s
     */
    private double acceleration(final LaneBasedGTU gtu, final Length stopDistance)
    {
        // What is the deceleration that will bring this GTU to a stop at exactly the suitability distance?
        // Answer: a = -v^2 / 2 / suitabilityDistance
        double v = gtu.getSpeed().getSI();
        double a = -v * v / 2 / stopDistance.getSI();
        return a;
    }

    /**
     * Determine the suitability of being at a particular longitudinal position in a particular Lane for following this Route.
     * <br>
     * TODO this code assumes GTUDirectionality.DIR_PLUS on all lanes of interest
     * @param lane Lane; the lane to consider
     * @param longitudinalPosition Length; the longitudinal position in the lane
     * @param gtu LaneBasedGTU; the GTU (used to check lane compatibility of lanes, and current lane the GTU is on)
     * @param timeHorizon Duration; the maximum time that a driver may want to look ahead
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; a value that indicates within what distance the GTU should try to vacate this
     *         lane.
     * @throws NetworkException on network inconsistency, or when the continuation Link at a branch cannot be determined
     */
    private Length suitability(final Lane lane, final Length longitudinalPosition, final LaneBasedGTU gtu,
            final Duration timeHorizon) throws NetworkException
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
            int laneCount = countCompatibleLanes(linkBeforeBranch, gtu.getGTUType(), GTUDirectionality.DIR_PLUS);
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
                    GTUDirectionality drivingDirection =
                            nextNode.equals(nextLink.getStartNode()) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
                    nextNode = nextLink.getEndNode();
                    // Oops: wrong code added the length of linkBeforeBranch in stead of length of nextLink
                    remainingDistance += nextLink.getLength().getSI();
                    linkBeforeBranch = (CrossSectionLink) nextLink;
                    // Figure out the new currentLane
                    if (currentLane.nextLanes(gtu.getGTUType()).size() == 0)
                    {
                        // Lane drop; our lane disappears. This is a compulsory lane change; which is not controlled
                        // by the Route. Perform the forced lane change.
                        if (currentLane
                                .accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtu.getGTUType(), drivingDirection)
                                .size() > 0)
                        {
                            for (Lane adjacentLane : currentLane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT,
                                    gtu.getGTUType(), drivingDirection))
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
                        for (Lane adjacentLane : currentLane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT,
                                gtu.getGTUType(), drivingDirection))
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
                            throw new NetworkException(
                                    "Lane ends and there is not a compatible adjacent lane that does " + "not end");
                        }
                    }
                    // Any compulsory lane change(s) have been performed and there is guaranteed a compatible next lane.
                    for (Lane nextLane : currentLane.nextLanes(gtu.getGTUType()).keySet())
                    {
                        if (nextLane.getLaneType().getDirectionality(gtu.getGTUType()).getDirectionalities()
                                .contains(drivingDirection))
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
        Map<Lane, Length> suitabilityOfLanesBeforeBranch = new HashMap<>();
        Link linkAfterBranch =
                gtu.getStrategicalPlanner().nextLinkDirection(nextSplitNode, lastLink, gtu.getGTUType()).getLink();
        GTUDirectionality drivingDirectionOnNextLane =
                linkAfterBranch.getStartNode().equals(nextSplitNode) ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
        for (CrossSectionElement cse : linkBeforeBranch.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().getDirectionality(gtu.getGTUType()).getDirectionalities()
                        .contains(GTUDirectionality.DIR_PLUS))
                {
                    for (Lane connectingLane : l.nextLanes(gtu.getGTUType()).keySet())
                    {
                        if (connectingLane.getParentLink() == linkAfterBranch
                                && connectingLane.getLaneType().isCompatible(gtu.getGTUType(), drivingDirectionOnNextLane))
                        {
                            Length currentValue = suitabilityOfLanesBeforeBranch.get(l);
                            // Use recursion to find out HOW suitable this continuation lane is, but don't revert back
                            // to the maximum time horizon (or we could end up in infinite recursion when there are
                            // loops in the network).
                            Length value = suitability(connectingLane, new Length(0, LengthUnit.SI), gtu,
                                    new Duration(spareTime, DurationUnit.SI));
                            // This line was missing...
                            value = value.plus(new Length(remainingDistance, LengthUnit.SI));
                            // Use the minimum of the value computed for the first split junction (if there is one)
                            // and the value computed for the second split junction.
                            suitabilityOfLanesBeforeBranch.put(l,
                                    null == currentValue || value.le(currentValue) ? value : currentValue);
                        }
                    }
                }
            }
        }
        if (suitabilityOfLanesBeforeBranch.size() == 0)
        {
            throw new NetworkException("No lanes available on Link " + linkBeforeBranch);
        }
        Length currentLaneSuitability = suitabilityOfLanesBeforeBranch.get(currentLane);
        if (null != currentLaneSuitability)
        {
            return currentLaneSuitability; // Following the current lane will keep us on the Route
        }
        // Performing one or more lane changes (left or right) is required.
        int totalLanes = countCompatibleLanes(currentLane.getParentLink(), gtu.getGTUType(), GTUDirectionality.DIR_PLUS);
        Length leftSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                        LateralDirectionality.LEFT, gtu.getGTUType(), GTUDirectionality.DIR_PLUS);
        Length rightSuitability =
                computeSuitabilityWithLaneChanges(currentLane, remainingDistance, suitabilityOfLanesBeforeBranch, totalLanes,
                        LateralDirectionality.RIGHT, gtu.getGTUType(), GTUDirectionality.DIR_PLUS);
        if (leftSuitability.ge(rightSuitability))
        {
            return leftSuitability;
        }
        else if (rightSuitability.ge(leftSuitability))
        {
            // TODO
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
     * @param suitabilities Map&lt;Lane, Length&gt;; the set of suitable lanes and their suitability
     * @param totalLanes int; total number of lanes compatible with the GTU type
     * @param direction LateralDirectionality; the direction of the lane changes to attempt
     * @param gtuType GTUType; the type of the GTU
     * @param drivingDirection GTUDirectionality; the driving direction of the GTU
     * @return double; the suitability of the <cite>startLane</cite> for following the Route
     */
    protected final Length computeSuitabilityWithLaneChanges(final Lane startLane, final double remainingDistance,
            final Map<Lane, Length> suitabilities, final int totalLanes, final LateralDirectionality direction,
            final GTUType gtuType, final GTUDirectionality drivingDirection)
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
        Length currentSuitability = null;
        while (null == currentSuitability)
        {
            laneChangesUsed++;
            if (currentLane.accessibleAdjacentLanesLegal(direction, gtuType, drivingDirection).size() == 0)
            {
                return GETOFFTHISLANENOW;
            }
            currentLane = currentLane.accessibleAdjacentLanesLegal(direction, gtuType, drivingDirection).iterator().next();
            currentSuitability = suitabilities.get(currentLane);
        }
        double fraction = currentSuitability == NOLANECHANGENEEDED ? 0 : 0.5;
        int notSuitableLaneCount = totalLanes - suitabilities.size();
        return new Length(
                remainingDistance * (notSuitableLaneCount - laneChangesUsed + 1 + fraction) / (notSuitableLaneCount + fraction),
                LengthUnit.SI);
    }

    /**
     * Determine how many lanes on a CrossSectionLink are compatible with a particular GTU type.<br>
     * TODO: this method should probably be moved into the CrossSectionLink class
     * @param link CrossSectionLink; the link
     * @param gtuType GTUType; the GTU type
     * @param drivingDirection GTUDirectionality; the driving direction on the link
     * @return integer; the number of lanes on the link that are compatible with the GTU type
     */
    protected final int countCompatibleLanes(final CrossSectionLink link, final GTUType gtuType,
            final GTUDirectionality drivingDirection)
    {
        int result = 0;
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane l = (Lane) cse;
                if (l.getLaneType().isCompatible(gtuType, drivingDirection))
                {
                    result++;
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneBasedCFLCTacticalPlanner [laneChangeModel=" + this.laneChangeModel + "]";
    }

}
