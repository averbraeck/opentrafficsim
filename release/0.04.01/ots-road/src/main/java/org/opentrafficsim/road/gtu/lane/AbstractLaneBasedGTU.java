package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.AccelerationVector;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.animation.LaneChangeUrgeGTUColorer;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.road.gtu.lane.changing.LaneMovementStep;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.route.AbstractLaneBasedRouteNavigator;
import org.opentrafficsim.road.network.route.LaneBasedRouteNavigator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * This class contains most of the code that is needed to run a lane based GTU. <br>
 * The starting point of a LaneBasedTU is that it can be in <b>multiple lanes</b> at the same time. This can be due to a lane
 * change (lateral), or due to crossing a link (front of the GTU is on another Lane than rear of the GTU). If a Lane is shorter
 * than the length of the GTU (e.g. when we do node expansion on a crossing, this is very well possible), a GTU could occupy
 * dozens of Lanes at the same time.
 * <p>
 * When calculating a headway, the GTU has to look in successive lanes. When Lanes (or underlying CrossSectionLinks) diverge,
 * the headway algorithms have to look at multiple Lanes and return the minimum headway in each of the Lanes. When the Lanes (or
 * underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in the headway calculation. Instead, gap
 * acceptance algorithms or their equivalent should guide the merging behavior.
 * <p>
 * To decide its movement, an AbstractLaneBasedGTU applies its car following algorithm and lane change algorithm to set the
 * acceleration and any lane change operation to perform. It then schedules the triggers that will add it to subsequent lanes
 * and remove it from current lanes as needed during the time step that is has committed to. Finally, it re-schedules its next
 * movement evaluation with the simulator.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1408 $, $LastChangedDate: 2015-09-24 15:17:25 +0200 (Thu, 24 Sep 2015) $, by $Author: pknoppers $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedGTU extends AbstractGTU implements LaneBasedGTU {
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Time of last evaluation. */
    private Time.Abs lastEvaluationTime;

    /** Time of next evaluation. */
    private Time.Abs nextEvaluationTime;

    /** Total traveled distance. */
    private Length.Abs odometer = new Length.Abs(0, LengthUnit.SI);

    /** Distance to the next required lane change and lateral direction thereof. */
    private LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection lastLaneChangeDistanceAndDirection =
        new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(new Length.Rel(Double.MAX_VALUE, LengthUnit.SI), null);

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the lastEvaluationTime.
     * Because the reference point of the GTU might not be on all the links the GTU is registered on, the fractional
     * longitudinal positions can be more than one, or less than zero.
     */
    private final Map<Link, Double> fractionalLinkPositions = new LinkedHashMap<>();

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as well to
     * keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the vehicle is
     * registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is registered are at the
     * front of the list, the later ones more to the back.
     */
    private final List<Lane> lanes = new ArrayList<>();

    /**
     * The adjacent lanes that are accessible for this GTU per lane where the GTU drives. This information is cached, because it
     * is used multiple times per timestep. The set of lanes is stored per LateralDirectionality (LEFT, RIGHT).
     */
    private final Map<Lane, EnumMap<LateralDirectionality, Set<Lane>>> accessibleAdjacentLanes = new HashMap<>();

    /** Speed at lastEvaluationTime. */
    private Speed speed;

    /** lateral velocity at lastEvaluationTime. */
    private final Speed lateralVelocity;

    /** acceleration (negative values indicate deceleration) at the lastEvaluationTime. */
    private Acceleration acceleration = new Acceleration(0, AccelerationUnit.SI);

    /** CarFollowingModel used by this GTU. */
    private final GTUFollowingModel gtuFollowingModel;

    /** LaneChangeModel used by this GTU. */
    private final LaneChangeModel laneChangeModel;

    /** the object to lock to make the GTU thread safe. */
    private final Object lock = new Object();

    /**
     * Construct a Lane Based GTU.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param routeNavigator Route; the route that the GTU will take
     * @param simulator to initialize the move method and to get the current time
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedGTU(final String id, final GTUType gtuType, final GTUFollowingModel gtuFollowingModel,
        final LaneChangeModel laneChangeModel, final Map<Lane, Length.Rel> initialLongitudinalPositions,
        final Speed initialSpeed, final LaneBasedRouteNavigator routeNavigator, final OTSDEVSSimulatorInterface simulator)
            throws NetworkException, SimRuntimeException, GTUException {
        super(id, gtuType, routeNavigator);
        setRouteNavigator(routeNavigator);
        if (null == gtuFollowingModel) {
            throw new GTUException("gtuFollowingModel may not be null");
        }

        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
        this.lateralVelocity = new Speed(0.0, SpeedUnit.SI);

        // register the GTU on the lanes
        for (Lane lane : initialLongitudinalPositions.keySet()) {
            this.lanes.add(lane);
            addAccessibleAdjacentLanes(lane);
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(initialLongitudinalPositions.get(lane)));
            lane.addGTU(this, initialLongitudinalPositions.get(lane));
        }

        this.lastEvaluationTime = simulator.getSimulatorTime().getTime();
        this.speed = initialSpeed;
        this.nextEvaluationTime = this.lastEvaluationTime;

        // start the movement of the GTU
        simulator.scheduleEventNow(this, this, "move", null);
    }

    /** very small speed to use for testing with rounding errors. */
    private static final Speed DRIFTINGSPEED = new Speed(1E-10, SpeedUnit.SI);

    /** {@inheritDoc} */
    @Override
    public final Speed getLongitudinalVelocity(final Time.Abs when) {
        Time.Rel dT = when.minus(this.lastEvaluationTime);
        Speed velocity = this.speed.plus(this.getAcceleration(when).multiplyBy(dT));
        if (velocity.abs().lt(DRIFTINGSPEED)) {
            velocity = new Speed(0.0, SpeedUnit.SI);
        }
        return velocity;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getLongitudinalVelocity() {
        return getLongitudinalVelocity(getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getVelocity() {
        return getLongitudinalVelocity();
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Abs getLastEvaluationTime() {
        return this.lastEvaluationTime;
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Abs getNextEvaluationTime() {
        return this.nextEvaluationTime;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration(final Time.Abs when) {
        // Currently the acceleration is independent of when; it is constant during the evaluation interval
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration() {
        return getAcceleration(getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Abs getOdometer() {
        return this.odometer.plus(deltaX(getSimulator().getSimulatorTime().getTime()));
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getLateralVelocity() {
        return this.lateralVelocity;
    }

    /** {@inheritDoc} */
    @Override
    public final void enterLane(final Lane lane, final Length.Rel position) throws NetworkException {
        if (this.lanes.contains(lane)) {
            System.err.println("GTU " + toString() + " is already registered on this lane: " + lane);
            return;
        }
        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as
        // this might lead to a "jump".
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink())) {
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position));
        }
        this.lanes.add(lane);
        addAccessibleAdjacentLanes(lane);
        lane.addGTU(this, position);
    }

    /** {@inheritDoc} */
    @Override
    public final void leaveLane(final Lane lane) {
        leaveLane(lane, false);
    }

    /**
     * Leave a lane but do not complain about having no lanes left when beingDestroyed is true.
     * @param lane the lane to leave
     * @param beingDestroyed if true, no complaints about having no lanes left
     */
    public final void leaveLane(final Lane lane, final boolean beingDestroyed) {
        // System.out.println("GTU " + toString() + " to be removed from lane: " + lane);
        this.lanes.remove(lane);
        removeAccessibleAdjacentLanes(lane);
        // check of there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.lanes) {
            if (l.getParentLink().equals(lane.getParentLink())) {
                found = true;
            }
        }
        if (!found) {
            this.fractionalLinkPositions.remove(lane.getParentLink());
        }
        lane.removeGTU(this);
        if (this.lanes.size() == 0 && !beingDestroyed) {
            System.err.println("lanes.size() = 0 for GTU " + getId());
        }
    }

    /**
     * @return lanes.
     */
    public final List<Lane> getLanes() {
        return this.lanes;
    }

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration STAYINCURRENTLANEINCENTIVE = new Acceleration(0.1,
        AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration PREFERREDLANEINCENTIVE = new Acceleration(0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final Acceleration NONPREFERREDLANEINCENTIVE = new Acceleration(-0.3,
        AccelerationUnit.METER_PER_SECOND_2);

    /** Standard time horizon for route choices. */
    private static final Time.Rel TIMEHORIZON = new Time.Rel(90, TimeUnit.SECOND);

    /**
     * Move this GTU to it's current location, then compute (and commit to) the next movement step.
     * @throws NetworkException on network inconsistency
     * @throws GTUException when GTU has not lane change model
     * @throws SimRuntimeException on not being able to reschedule this move() method.
     * @throws ValueException cannot happen
     */
    protected final void move() throws NetworkException, GTUException, SimRuntimeException, ValueException {
        if (getLongitudinalVelocity().getSI() < 0) {
            System.out.println("negative velocity: " + this + " " + getLongitudinalVelocity().getSI() + "m/s");
        }

        // Quick sanity check
        if (getSimulator().getSimulatorTime().getTime().getSI() != getNextEvaluationTime().getSI()) {
            throw new Error("move called at wrong time: expected time " + getNextEvaluationTime() + " simulator time is : "
                + getSimulator().getSimulatorTime().getTime());
        }
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanes.isEmpty()) {
            destroy();
            return; // Done; do not re-schedule execution of this move method.
        }
        Length.Rel maximumForwardHeadway = new Length.Rel(500.0, LengthUnit.METER);
        // TODO 500?
        Length.Rel maximumReverseHeadway = new Length.Rel(200.0, LengthUnit.METER);
        // TODO 200?
        Speed speedLimit = this.getMaximumVelocity();
        for (Lane lane : this.lanes) {
            if (lane.getSpeedLimit(getGTUType()).lt(speedLimit)) {
                speedLimit = lane.getSpeedLimit(getGTUType());
            }
        }
        if (null == this.laneChangeModel) {
            throw new GTUException("LaneBasedGTUs MUST have a LaneChangeModel");
        }
        // TODO Collecting information about nearby traffic should be done in a separate class. This is a very basic
        // operation in OTS.
        Collection<HeadwayGTU> sameLaneTraffic = new ArrayList<HeadwayGTU>();
        HeadwayGTU leader = headway(maximumForwardHeadway);
        if (null != leader.getOtherGTU()) {
            sameLaneTraffic.add(leader);
        }
        HeadwayGTU follower = headway(maximumReverseHeadway);
        if (null != follower.getOtherGTU()) {
            sameLaneTraffic.add(new HeadwayGTU(follower.getOtherGTU(), -follower.getDistanceSI()));
        }
        Time.Abs now = getSimulator().getSimulatorTime().getTime();
        Collection<HeadwayGTU> leftLaneTraffic = collectNeighborLaneTraffic(LateralDirectionality.LEFT, now,
            maximumForwardHeadway, maximumReverseHeadway);
        Collection<HeadwayGTU> rightLaneTraffic = collectNeighborLaneTraffic(LateralDirectionality.RIGHT, now,
            maximumForwardHeadway, maximumReverseHeadway);
        // FIXME: whether we drive on the right should be stored in some central place.
        final LateralDirectionality preferred = LateralDirectionality.RIGHT;
        final Acceleration defaultLeftLaneIncentive = LateralDirectionality.LEFT == preferred ? PREFERREDLANEINCENTIVE
            : NONPREFERREDLANEINCENTIVE;
        final Acceleration defaultRightLaneIncentive = LateralDirectionality.RIGHT == preferred ? PREFERREDLANEINCENTIVE
            : NONPREFERREDLANEINCENTIVE;
        AccelerationVector defaultLaneIncentives = new AccelerationVector(new double[] {defaultLeftLaneIncentive.getSI(),
            STAYINCURRENTLANEINCENTIVE.getSI(), defaultRightLaneIncentive.getSI()}, AccelerationUnit.SI, StorageType.DENSE);
        AccelerationVector laneIncentives = laneIncentives(defaultLaneIncentives);
        LaneMovementStep lcmr = this.laneChangeModel.computeLaneChangeAndAcceleration(this, sameLaneTraffic,
            rightLaneTraffic, leftLaneTraffic, speedLimit, new Acceleration(laneIncentives.get(
                preferred == LateralDirectionality.RIGHT ? 2 : 0)), new Acceleration(laneIncentives.get(1)),
            new Acceleration(laneIncentives.get(preferred == LateralDirectionality.RIGHT ? 0 : 2)));
        // TODO detect that a required lane change was blocked and, if it was, do something to find/create a gap.
        if (lcmr.getGfmr().getAcceleration().getSI() < -9999) {
            System.out.println("Problem");
        }
        // First move this GTU forward (to its current location) as determined in the PREVIOUS move step.
        // GTUs move based on their fractional position to stay aligned when registered in parallel lanes.
        // The "oldest" lane of parallel lanes takes preference when updating the fractional position.
        // So we work from back to front.
        // TODO Put the "update state to current time" code in a separate method and call that method at the start of
        // this (move) method.
        synchronized (this.lock) {
            for (int i = this.lanes.size() - 1; i >= 0; i--) {
                Lane lane = this.lanes.get(i);
                this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position(lane, getReference(),
                    this.nextEvaluationTime)));
            }
            // Update the odometer value
            this.odometer = this.odometer.plus(deltaX(this.nextEvaluationTime));
            // Compute and set the current speed using the "old" nextEvaluationTime and acceleration
            this.speed = getLongitudinalVelocity(this.nextEvaluationTime);
            // Now update last evaluation time
            this.lastEvaluationTime = this.nextEvaluationTime;
            // Set the next evaluation time
            this.nextEvaluationTime = lcmr.getGfmr().getValidUntil();
            // Set the acceleration (this totally defines the longitudinal motion until the next evaluation time)
            this.acceleration = lcmr.getGfmr().getAcceleration();
            // Execute all samplers
            for (Lane lane : this.lanes) {
                lane.sample(this);
            }
            // Change onto laterally adjacent lane(s) if the LaneMovementStep indicates a lane change
            if (lcmr.getLaneChange() != null) {
                // TODO make lane changes gradual (not instantaneous; like now)
                Collection<Lane> oldLaneSet = new HashSet<Lane>(this.lanes);
                Collection<Lane> newLaneSet = new HashSet<Lane>(2);
                // Prepare the remove of this GTU from all of the Lanes that it is on and remember the fractional
                // position on each one
                Map<Lane, Double> oldFractionalPositions = new LinkedHashMap<Lane, Double>();
                for (Lane l : this.lanes) {
                    oldFractionalPositions.put(l, fractionalPosition(l, getReference(), getLastEvaluationTime()));
                    this.fractionalLinkPositions.remove(l.getParentLink());
                    newLaneSet.addAll(this.accessibleAdjacentLanes.get(l).get(lcmr.getLaneChange()));
                }
                // Add this GTU to the lanes in newLaneSet.
                // This could be rewritten to be more efficient.
                for (Lane newLane : newLaneSet) {
                    Double fractionalPosition = null;
                    // find ONE lane in oldLaneSet that has l as neighbor
                    for (Lane oldLane : oldLaneSet) {
                        if (this.accessibleAdjacentLanes.get(oldLane).get(lcmr.getLaneChange()).contains(newLane)) {
                            fractionalPosition = oldFractionalPositions.get(oldLane);
                            break;
                        }
                    }
                    if (null == fractionalPosition) {
                        throw new Error("Program error: Cannot find an oldLane that has newLane " + newLane + " as " + lcmr
                            .getLaneChange() + " neighbor");
                    }
                    enterLane(newLane, newLane.getLength().multiplyBy(fractionalPosition));
                }

                // Remove this GTU from all of the Lanes that it is on and remember the fractional position on each
                // one
                for (Lane l : oldFractionalPositions.keySet()) {
                    leaveLane(l);
                }
                // System.out.println("GTU " + this + " changed lanes from: " + oldLaneSet + " to " + replacementLanes);
                checkConsistency();
            }
            // The GTU is now committed to executed the entire movement stored in the LaneChangeModelResult
            // Schedule all sensor triggers that are going to happen until the next evaluation time.
            // Also schedule the registration and unregistration of lanes when the vehicle enters them.
            scheduleTriggers();
        }
        // Re-schedule this move method at the end of the committed time step.
        getSimulator().scheduleEventAbs(this.getNextEvaluationTime(), this, this, "move", null);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU on the intended
     * route.
     * @param defaultLaneIncentives DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives for the next left
     *            adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     */
    private AccelerationVector laneIncentives(final AccelerationVector defaultLaneIncentives) throws NetworkException,
        ValueException {
        Length.Rel leftSuitability = suitability(LateralDirectionality.LEFT);
        Length.Rel currentSuitability = suitability(null);
        Length.Rel rightSuitability = suitability(LateralDirectionality.RIGHT);
        if (currentSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED) {
            this.lastLaneChangeDistanceAndDirection = new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(
                currentSuitability, null);
        } else {
            this.lastLaneChangeDistanceAndDirection = new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(
                currentSuitability, rightSuitability.getSI() == 0 ? false : leftSuitability.gt(rightSuitability));
        }
        if ((leftSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
            || leftSuitability == AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW)
            && currentSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
            && (rightSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
                || rightSuitability == AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW)) {
            return checkLaneDrops(defaultLaneIncentives);
        }
        if (currentSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED) {
            return new AccelerationVector(new double[] {acceleration(leftSuitability), defaultLaneIncentives.get(1).getSI(),
                acceleration(rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
        }
        return new AccelerationVector(new double[] {acceleration(leftSuitability), acceleration(currentSuitability),
            acceleration(rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU from running out
     * of road at an upcoming lane drop.
     * @param defaultLaneIncentives DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives for the next left
     *            adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     */
    private AccelerationVector checkLaneDrops(final AccelerationVector defaultLaneIncentives) throws NetworkException,
        ValueException {
        Length.Rel leftSuitability = laneDrop(LateralDirectionality.LEFT);
        Length.Rel currentSuitability = laneDrop(null);
        Length.Rel rightSuitability = laneDrop(LateralDirectionality.RIGHT);
        // @formatter:off
        if ((leftSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
            || leftSuitability == AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW)
            && currentSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
            && (rightSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED
                || rightSuitability == AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW)) {
            return defaultLaneIncentives;
        }
        // @formatter:on
        if (currentSuitability == AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED) {
            return new AccelerationVector(new double[] {acceleration(leftSuitability), defaultLaneIncentives.get(1).getSI(),
                acceleration(rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
        }
        if (currentSuitability.le(leftSuitability)) {
            return new AccelerationVector(new double[] {PREFERREDLANEINCENTIVE.getSI(), NONPREFERREDLANEINCENTIVE.getSI(),
                AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW.getSI()}, AccelerationUnit.SI, StorageType.DENSE);
        }
        if (currentSuitability.le(rightSuitability)) {
            return new AccelerationVector(new double[] {AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW.getSI(),
                NONPREFERREDLANEINCENTIVE.getSI(), PREFERREDLANEINCENTIVE.getSI()}, AccelerationUnit.SI, StorageType.DENSE);
        }
        return new AccelerationVector(new double[] {acceleration(leftSuitability), acceleration(currentSuitability),
            acceleration(rightSuitability)}, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Return the distance until the next lane drop in the specified (nearby) lane.
     * @param direction LateralDirectionality; one of the values <cite>LateralDirectionality.LEFT</cite> (use the left-adjacent
     *            lane), or <cite>LateralDirectionality.RIGHT</cite> (use the right-adjacent lane), or <cite>null</cite> (use
     *            the current lane)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; distance until the next lane drop if it occurs within the TIMEHORIZON, or
     *         LaneBasedRouteNavigator.NOLANECHANGENEEDED if this lane can be followed until the next split junction or until
     *         beyond the TIMEHORIZON
     * @throws NetworkException on network inconsistency
     */
    private Length.Rel laneDrop(final LateralDirectionality direction) throws NetworkException {
        Lane lane = null;
        Length.Rel longitudinalPosition = null;
        Map<Lane, Length.Rel> positions = positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction) {
            for (Lane l : getLanes()) {
                if (l.getLaneType().isCompatible(getGTUType())) {
                    lane = l;
                }
            }
            if (null == lane) {
                throw new NetworkException("GTU " + this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        } else {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            lane = bestAccessibleAdjacentLane(lane, direction, longitudinalPosition); // XXX correct??
        }
        if (null == lane) {
            return AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW;
        }
        double remainingLength = lane.getLength().getSI() - longitudinalPosition.getSI();
        double remainingTimeSI = TIMEHORIZON.getSI() - remainingLength / lane.getSpeedLimit(getGTUType()).getSI();
        while (remainingTimeSI >= 0) {
            // TODO: if (lane.getSensors() contains SinkSensor => return LaneBasedRouteNavigator.NOLANECHANGENEEDED
            int branching = lane.nextLanes(getGTUType()).size();
            if (branching == 0) {
                return new Length.Rel(remainingLength, LengthUnit.SI);
            }
            if (branching > 1) {
                return AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED;
            }
            lane = lane.nextLanes(getGTUType()).iterator().next();
            remainingTimeSI -= lane.getLength().getSI() / lane.getSpeedLimit(getGTUType()).getSI();
            remainingLength += lane.getLength().getSI();
        }
        return AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED;
    }

    /**
     * Compute deceleration needed to stop at a specified distance.
     * @param stopDistance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return double; the acceleration (deceleration) needed to stop at the specified distance in m/s/s
     */
    private double acceleration(final Length.Rel stopDistance) {
        // What is the deceleration that will bring this GTU to a stop at exactly the suitability distance?
        // Answer: a = -v^2 / 2 / suitabilityDistance
        double v = getLongitudinalVelocity().getSI();
        double a = -v * v / 2 / stopDistance.getSI();
        return a;
    }

    /**
     * Return the suitability for the current lane, left adjacent lane or right adjacent lane.
     * @param direction LateralDirectionality; one of the values <cite>null</cite>, <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the suitability of the lane for reaching the (next) destination
     * @throws NetworkException on network inconsistency
     */
    private Length.Rel suitability(final LateralDirectionality direction) throws NetworkException {
        Lane lane = null;
        Length.Rel longitudinalPosition = null;
        Map<Lane, Length.Rel> positions = positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction) {
            for (Lane l : getLanes()) {
                if (l.getLaneType().isCompatible(getGTUType())) {
                    lane = l;
                }
            }
            if (null == lane) {
                throw new NetworkException("GTU " + this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        } else {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            lane = bestAccessibleAdjacentLane(lane, direction, longitudinalPosition); // XXX correct??
        }
        if (null == lane) {
            return AbstractLaneBasedRouteNavigator.GETOFFTHISLANENOW;
        }
        try {
            LaneBasedRouteNavigator navigator = getRouteNavigator();

            return navigator.suitability(lane, longitudinalPosition, this, TIMEHORIZON);
        } catch (NetworkException ne) {
            System.err.println("GTU " + this.getId() + " has a route problem in suitability: " + ne.getMessage());
            return AbstractLaneBasedRouteNavigator.NOLANECHANGENEEDED;
        }
    }

    /**
     * Verify that all the lanes registered in this GTU have this GTU registered as well and vice versa.
     */
    private void checkConsistency() {
        for (Lane l : this.lanes) {
            if (!this.fractionalLinkPositions.containsKey(l.getParentLink())) {
                System.err.println("GTU " + this + " is in lane " + l
                    + " but that GTU has no fractional position on the link of that lane");
            }
        }
        for (Link csl : this.fractionalLinkPositions.keySet()) {
            boolean found = false;
            for (Lane l : this.lanes) {
                if (l.getParentLink().equals(csl)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.err.println("GTU " + this + " has a fractional position " + this.fractionalLinkPositions.get(csl)
                    + " on link " + csl + " but this GTU is not on any lane(s) of that link");
            }
        }
    }

    /**
     * Schedule the triggers for this GTU that are going to happen until the next evaluation time. Also schedule the
     * registration and unregistration of lanes when the vehicle enters them, at the exact right time. When the method is
     * called, the acceleration and velocity of this GTU are set to the right values for the coming timestep.
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException should never happen
     * @throws GTUException when a branch is reached where the GTU does not know where to go next
     */
    private void scheduleTriggers() throws NetworkException, SimRuntimeException, GTUException {
        // move the vehicle into any new lanes with the front, and schedule entrance during this time step
        // and calculate the current position based on the fractional position, because THE POSITION METHOD DOES NOT WORK. IT
        // CALCULATES THE POSITION BASED ON THE NEWLY CALCULATED ACCELERATION AND VELOCITY AND CAN THEREFORE MAKE AN ERROR.
        double timestep = this.nextEvaluationTime.getSI() - this.lastEvaluationTime.getSI();
        double moveSI = getVelocity().getSI() * timestep + 0.5 * getAcceleration().getSI() * timestep * timestep;
        for (Lane lane : new ArrayList<Lane>(this.lanes)) // use a copy because this.lanes can change
        {
            // schedule triggers on this lane
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            lane.scheduleTriggers(this, referenceStartSI, moveSI);

            // determine when our FRONT will pass the end of this registered lane.
            // if the time is earlier than the end of the timestep: schedule the enterLane method.
            // TODO look if more lanes are entered in one timestep, and continue the algorithm with the remainder of the time...
            double frontPosSI = referenceStartSI + getFront().getDx().getSI();
            double moveFrontOnNextLane = moveSI - (lane.getLength().getSI() - frontPosSI);
            if (frontPosSI < lane.getLength().getSI() && moveFrontOnNextLane > 0) {
                Lane nextLane = determineNextLane(lane);
                if (!this.lanes.contains(nextLane)) // XXX: this happens -- how can that be?
                {
                    // we have to register the position at the previous timestep to keep calculations consistent.
                    // And we have to correct for the position of the reference point.
                    Length.Rel refPosAtLastTimestep = new Length.Rel(-(lane.getLength().getSI() - frontPosSI) - getFront()
                        .getDx().getSI(), LengthUnit.SI); // XXX:
                                                          // should
                                                          // be
                                                          // based
                                                          // on
                                                          // fractional?
                    enterLane(nextLane, refPosAtLastTimestep);
                    // schedule any sensor triggers on this lane for the remainder time
                    nextLane.scheduleTriggers(this, refPosAtLastTimestep.getSI(), moveSI);

                    // XXX DO THE ROUTING -- ADDED 28-10-2015
                    try {
                        getRouteNavigator().visitNextNode();
                    } catch (NetworkException ne) {
                        System.err.println("GTU " + this.getId() + " has a route problem: " + ne.getMessage());
                    }

                }
            }
        }

        // move the vehicle out of any lanes with the back, and schedule exit during this time step
        for (Lane lane : this.lanes) {
            // determine when our REAR will pass the end of this registered lane.
            // if the time is earlier than the end of the timestep: schedule the exitLane method at the END of this timestep
            // TODO look if more lanes are exited in one timestep, and continue the algorithm with the remainder of the time...
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            double rearPosSI = referenceStartSI + getRear().getDx().getSI();
            if (rearPosSI < lane.getLength().getSI() && rearPosSI + moveSI > lane.getLength().getSI()) {
                getSimulator().scheduleEventRel(new Time.Rel(timestep - Math.ulp(timestep), TimeUnit.SI), this, this,
                    "leaveLane", new Object[] {lane, new Boolean(true)}); // XXX: should be false?
            }
        }
    }

    /**
     * @param lane the lane to find the successor for
     * @return the next lane for this GTU
     * @throws NetworkException when no next lane exists or the route branches into multiple next lanes
     * @throws GTUException when no route could be found or the routeNavigator returns null
     */
    private Lane determineNextLane(final Lane lane) throws NetworkException, GTUException {
        Lane nextLane = null;
        if (lane.nextLanes(getGTUType()).size() == 0) {
            throw new NetworkException(this + " - lane " + lane + " does not have a successor");
        }
        if (lane.nextLanes(getGTUType()).size() == 1) {
            nextLane = lane.nextLanes(getGTUType()).iterator().next();
        } else {
            if (null == this.getRouteNavigator()) {
                throw new GTUException(this + " reaches branch but has no route navigator");
            }
            Node nextNode = getRouteNavigator().nextNodeToVisit();
            if (null == nextNode) {
                throw new GTUException(this + " reaches branch and the route returns null as nextNodeToVisit");
            }
            int continuingLaneCount = 0;
            for (Lane candidateLane : lane.nextLanes(getGTUType())) {
                if (this.lanes.contains(candidateLane)) {
                    continue; // Already on this lane
                }
                if (nextNode == candidateLane.getParentLink().getEndNode()) {
                    nextLane = candidateLane;
                    continuingLaneCount++;
                }
            }
            if (continuingLaneCount == 0) {
                throw new NetworkException(this + " reached branch and the route specifies a nextNodeToVisit (" + nextNode
                    + ") that is not a next node " + "at this branch at (" + lane.getParentLink().getEndNode() + ")");
            }
            if (continuingLaneCount > 1) {
                throw new NetworkException(this
                    + " reached branch and the route specifies multiple lanes to continue on at this branch (" + lane
                        .getParentLink().getEndNode() + "). This is not yet supported");
            }
        }
        return nextLane;
    }

    /**
     * Collect relevant traffic in adjacent lanes. Parallel traffic is included with headway equal to Double.NaN.
     * @param directionality LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the (current) time
     * @param maximumForwardHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum forward search distance
     * @param maximumReverseHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum reverse search distance
     * @return Collection&lt;LaneBasedGTU&gt;;
     * @throws NetworkException on network inconsistency
     */
    private Collection<HeadwayGTU> collectNeighborLaneTraffic(final LateralDirectionality directionality,
        final Time.Abs when, final Length.Rel maximumForwardHeadway, final Length.Rel maximumReverseHeadway)
            throws NetworkException {
        Collection<HeadwayGTU> result = new HashSet<HeadwayGTU>();
        for (LaneBasedGTU p : parallel(directionality, when)) {
            result.add(new HeadwayGTU(p, Double.NaN));
        }
        for (Lane lane : this.lanes) {
            for (Lane adjacentLane : this.accessibleAdjacentLanes.get(lane).get(directionality)) {
                HeadwayGTU leader = headway(adjacentLane, maximumForwardHeadway);
                if (null != leader.getOtherGTU() && !result.contains(leader)) {
                    result.add(leader);
                }
                HeadwayGTU follower = headway(adjacentLane, maximumReverseHeadway);
                if (null != follower.getOtherGTU() && !result.contains(follower)) {
                    result.add(new HeadwayGTU(follower.getOtherGTU(), -follower.getDistanceSI()));
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition) throws NetworkException {
        return positions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException {
        Map<Lane, Length.Rel> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes) {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition) throws NetworkException {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel projectedPosition(final Lane projectionLane, final RelativePosition relativePosition,
        final Time.Abs when) throws NetworkException {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList()) {
            if (cse instanceof Lane) {
                Lane cseLane = (Lane) cse;
                if (this.lanes.contains(cseLane)) {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new Length.Rel(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                }
            }
        }
        throw new NetworkException("GTU " + this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException {
        if (null == lane) {
            throw new NetworkException("lane is null");
        }
        synchronized (this.lock) {
            if (!this.lanes.contains(lane)) {
                throw new NetworkException("position() : GTU " + toString() + " is not on lane " + lane);
            }
            if (!this.fractionalLinkPositions.containsKey(lane.getParentLink())) {
                // DO NOT USE toString() here, as it will cause an endless loop...
                throw new NetworkException("GTU " + getId() + " does not have a fractional position on " + lane.toString());
            }
            Length.Rel longitudinalPosition = lane.position(this.fractionalLinkPositions.get(lane.getParentLink()));
            if (longitudinalPosition == null) {
                // According to FindBugs; this cannot happen; PK is unsure whether FindBugs is correct.
                throw new NetworkException("position(): GTU " + toString() + " no position for lane " + lane);
            }
            Length.Rel loc = longitudinalPosition.plus(deltaX(when)).plus(relativePosition.getDx());
            if (Double.isNaN(loc.getSI())) {
                System.out.println("loc is NaN");
            }
            return loc;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws NetworkException {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes) {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition)
        throws NetworkException {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the GTU
     *            when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI, final double cumDistanceSI,
        final double maxDistanceSI, final Time.Abs when) throws NetworkException {
        LaneBasedGTU otherGTU = lane.getGtuAfter(new Length.Rel(lanePositionSI, LengthUnit.SI), RelativePosition.REAR, when);
        if (otherGTU != null) {
            double distanceM = cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI) {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI) {
            // is there a successor link?
            if (lane.nextLanes(getGTUType()).size() > 0) {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane nextLane : lane.nextLanes(getGTUType())) {
                    // TODO Only follow links on the Route if there is a "real" Route
                    // TODO use new functions of the Navigator
                    // if (this.getRoute() == null || this.getRoute().size() == 0 /* XXX STUB dummy route */
                    // || (this.routeNavigator.getRoute().containsLink((Link) lane.getParentLink())))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        HeadwayGTU closest = headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI,
                            when);
                        if (closest.getDistanceSI() < maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI
                            .getDistanceSI()) {
                            foundMaxGTUDistanceSI = closest;
                        }
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayGTU(null, Double.MAX_VALUE);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the rear of
     *            the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for each
     *            subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI, final double cumDistanceSI,
        final double maxDistanceSI, final Time.Abs when) throws NetworkException {
        LaneBasedGTU otherGTU = lane.getGtuBefore(new Length.Rel(lanePositionSI, LengthUnit.SI), RelativePosition.FRONT,
            when);
        if (otherGTU != null) {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI) {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI) {
            // is there a predecessor link?
            if (lane.prevLanes(getGTUType()).size() > 0) {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane prevLane : lane.prevLanes(getGTUType())) {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    HeadwayGTU closest = headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(),
                        traveledDistanceSI, maxDistanceSI, when);
                    if (closest.getDistanceSI() < maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI
                        .getDistanceSI()) {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayGTU(null, Double.MAX_VALUE);
    }

    /**
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayGTUSI(final double maxDistanceSI) throws NetworkException {
        Time.Abs when = getSimulator().getSimulatorTime().getTime();
        HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        if (maxDistanceSI > 0.0) {
            // look forward.
            for (Lane lane : positions(getFront()).keySet()) {
                HeadwayGTU closest = headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(), when).getSI(), 0.0,
                    maxDistanceSI, when);
                if (closest.getDistanceSI() < maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI
                    .getDistanceSI()) {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        } else {
            // look backward.
            for (Lane lane : positions(getRear()).keySet()) {
                HeadwayGTU closest = headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(), when).getSI(), 0.0,
                    -maxDistanceSI, when);
                if (closest.getDistanceSI() < -maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI
                    .getDistanceSI()) {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        return foundMaxGTUDistanceSI;
    }

    /** {@inheritDoc} */
    @Override
    public final HeadwayGTU headway(final Length.Rel maxDistance) throws NetworkException {
        return headwayGTUSI(maxDistance.getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final HeadwayGTU headway(final Lane lane, final Length.Rel maxDistance) throws NetworkException {
        Time.Abs when = getSimulator().getSimulatorTime().getTime();
        if (maxDistance.getSI() > 0.0) {
            return headwayRecursiveForwardSI(lane, this.projectedPosition(lane, this.getFront(), when).getSI(), 0.0,
                maxDistance.getSI(), when);
        } else {
            return headwayRecursiveBackwardSI(lane, this.projectedPosition(lane, this.getRear(), when).getSI(), 0.0,
                -maxDistance.getSI(), when);
        }
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (front
     *            of the) GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws NetworkException {
        if (lane.getGtuList().contains(otherGTU)) {
            double distanceM = cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI) {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI) {
            // is there a successor link?
            for (Lane nextLane : lane.nextLanes(getGTUType())) {
                // TODO Only follow links on the Route if there is a Route
                // if (this.getRoute() == null || this.getRoute().size() == 0) /* XXX STUB dummy route */
                // || this.routeNavigator.getRoute().containsLink((Link) lane.getParentLink()))
                {
                    double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                    double headwaySuccessor = headwayRecursiveForwardSI(nextLane, 0.0, otherGTU, traveledDistanceSI,
                        maxDistanceSI, when);
                    if (headwaySuccessor < maxDistanceSI) {
                        return headwaySuccessor;
                    }
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in backward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (back
     *            of) the GTU when we measure in the lane where the original GTU is positioned, and the length of the lane for
     *            each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes, as a POSITIVE number
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls, as a POSITIVE number
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws NetworkException {
        if (lane.getGtuList().contains(otherGTU)) {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI) {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI) {
            // is there a predecessor link?
            for (Lane prevLane : lane.prevLanes(getGTUType())) {
                // Routes are NOT IMPORTANT when we look backward.
                double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                // PK: This looks like a bug; replacement code below this comment.
                // double headwayPredecessor =
                // headwayRecursiveForwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
                // traveledDistanceSI, maxDistanceSI, when);
                double headwayPredecessor = headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
                    traveledDistanceSI, maxDistanceSI, when);
                if (headwayPredecessor < maxDistanceSI) {
                    return headwayPredecessor;
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /**
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for both lateral directions.
     * @param lane Lane; the lane for which to add the accessible lanes.
     */
    private void addAccessibleAdjacentLanes(final Lane lane) {
        EnumMap<LateralDirectionality, Set<Lane>> adjacentMap = new EnumMap<>(LateralDirectionality.class);
        for (LateralDirectionality lateralDirection : LateralDirectionality.values()) {
            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
            adjacentMap.put(lateralDirection, adjacentLanes);
        }
        this.accessibleAdjacentLanes.put(lane, adjacentMap);
    }

    /**
     * Remove the set of adjacent lanes when we leave the lane.
     * @param lane Lane; the lane for which to remove the accessible lanes.
     */
    private void removeAccessibleAdjacentLanes(final Lane lane) {
        this.accessibleAdjacentLanes.remove(lane);
    }

    /** {@inheritDoc} */
    @Override
    public final Lane bestAccessibleAdjacentLane(final Lane currentLane, final LateralDirectionality lateralDirection,
        final Length.Rel longitudinalPosition) {
        Set<Lane> candidates = this.accessibleAdjacentLanes.get(currentLane).get(lateralDirection);
        if (candidates.isEmpty()) {
            return null; // There is no adjacent Lane that this GTU type can cross into
        }
        if (candidates.size() == 1) {
            return candidates.iterator().next(); // There is exactly one adjacent Lane that this GTU type can cross into
        }
        // There are several candidates; find the one that is widest at the beginning.
        Lane bestLane = null;
        double widthM = -1.0;
        for (Lane lane : candidates) {
            if (lane.getWidth(longitudinalPosition).getSI() > widthM) {
                widthM = lane.getWidth(longitudinalPosition).getSI();
                bestLane = lane;
            }
        }
        return bestLane;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU> parallel(final Lane lane, final Time.Abs when) throws NetworkException {
        Set<LaneBasedGTU> gtuSet = new LinkedHashSet<LaneBasedGTU>();
        for (Lane l : this.lanes) {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink())) {
                // compare based on fractional positions.
                double posFractionFront = Math.max(0.0, this.fractionalPosition(l, getFront(), when));
                double posFractionRear = Math.min(1.0, this.fractionalPosition(l, getRear(), when));
                for (LaneBasedGTU gtu : lane.getGtuList()) {
                    if (!gtu.equals(this)) {
                        double gtuFractionFront = Math.max(0.0, gtu.fractionalPosition(lane, gtu.getFront(), when));
                        double gtuFractionRear = Math.min(1.0, gtu.fractionalPosition(lane, gtu.getRear(), when));
                        // TODO is this formula for parallel() okay?
                        // TODO should it not be extended with several || clauses?
                        if (gtuFractionFront >= posFractionRear && gtuFractionRear <= posFractionFront) {
                            gtuSet.add(gtu);
                        }
                    }
                }
            }
        }
        return gtuSet;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU> parallel(final LateralDirectionality lateralDirection, final Time.Abs when)
        throws NetworkException {
        Set<LaneBasedGTU> gtuSet = new LinkedHashSet<LaneBasedGTU>();
        for (Lane lane : this.lanes) {
            for (Lane adjacentLane : this.accessibleAdjacentLanes.get(lane).get(lateralDirection)) {
                gtuSet.addAll(parallel(adjacentLane, when));
            }
        }
        return gtuSet;
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Abs timeAtDistance(final Length.Rel distance) {
        Double result = solveTimeForDistance(distance);
        if (null == result) {
            return null;
        }
        return new Time.Abs(this.lastEvaluationTime.getSI() + result, TimeUnit.SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Rel deltaTimeForDistance(final Length.Rel distance) {
        Double result = solveTimeForDistance(distance);
        if (null == result) {
            return null;
        }
        return new Time.Rel(result, TimeUnit.SECOND);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy() {
        synchronized (this.lock) {
            while (!this.lanes.isEmpty()) {
                Lane lane = this.lanes.get(0);
                leaveLane(lane, true);
            }
        }
    }

    /**
     * Determine longitudinal displacement.
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the displacement since last move evaluation
     */
    private Length.Rel deltaX(final Time.Abs when) {
        Time.Rel dT = when.minus(this.lastEvaluationTime);
        return this.speed.multiplyBy(dT).plus(this.getAcceleration().multiplyBy(dT).multiplyBy(dT).divideBy(2));
        /*-
        return Calc.speedTimesTime(this.speed, dT).plus(
            Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(), dT));
         */
    }

    /**
     * Determine show long it will take for this GTU to cover the specified distance (both time and distance since the last
     * evaluation time).
     * @param distance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return Double; the relative time, or null when this GTU stops before covering the specified distance
     */
    private Double solveTimeForDistance(final Length.Rel distance) {
        return solveTimeForDistanceSI(distance.getSI());
    }

    /**
     * Determine show long it will take for this GTU to cover the specified distance (both time and distance since the last
     * evaluation time).
     * @param distanceSI double; the distance in SI units
     * @return Double; the relative time, or null when this GTU stops before covering the specified distance
     */
    private Double solveTimeForDistanceSI(final double distanceSI) {
        /*
         * Currently (!) a (Lane based) GTU commits to a constant acceleration until the next evaluation time. When/If that is
         * changed, this method will have to be re-written.
         */
        double c = -distanceSI;
        double a = this.acceleration.getSI() / 2;
        double b = this.speed.getSI();
        if (Math.abs(a) < 0.001) {
            if (b > 0) {
                return -c / b;
            }
            return null;
        }
        // Solve a * t^2 + b * t + c = 0
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return null;
        }
        // The solutions are (-b +/- sqrt(discriminant)) / 2 / a
        double solution1 = (-b - Math.sqrt(discriminant)) / (2 * a);
        double solution2 = (-b + Math.sqrt(discriminant)) / (2 * a);
        if (solution1 < 0 && solution2 < 0) {
            return null;
        }
        if (solution1 < 0) {
            return solution2;
        }
        if (solution2 < 0) {
            return solution1;
        }
        // Both are >= 0; return the smallest one
        if (solution1 < solution2) {
            return solution1;
        }
        return solution2;
    }

    /**
     * Retrieve the GTUFollowingModel of this GTU.
     * @return GTUFollowingModel
     */
    @Override
    public final GTUFollowingModel getGTUFollowingModel() {
        return this.gtuFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedRouteNavigator getRouteNavigator() {
        return (LaneBasedRouteNavigator) super.getRouteNavigator();
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() {
        synchronized (this.lock) {
            try {
                if (this.lanes.size() == 0) {
                    // This happens temporarily when a GTU is moved to another Lane
                    return new DirectedPoint(0, 0, 0);
                }
                Lane lane = this.lanes.get(0);
                DirectedPoint location = lane.getCenterLine().getLocationExtended(position(lane, getReference()));
                location.z += 0.01; // raise the location a bit above the lane
                return location;
            } catch (NetworkException exception) {
                return null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    @Override
    public LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection getLaneChangeDistanceAndDirection() {
        return this.lastLaneChangeDistanceAndDirection;
    }

    /**
     * Description of Car at specified time.
     * @param lane the position on this lane will be returned.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final Lane lane, final Time.Abs when) {
        double pos;
        try {
            pos = this.position(lane, getFront(), when).getSI();
        } catch (NetworkException exception) {
            pos = Double.NaN;
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s^2", getId(),
            this.lastEvaluationTime.getSI(), getNextEvaluationTime().getSI(), pos, this.getLongitudinalVelocity(when)
                .getSI(), this.getAcceleration(when).getSI());
    }

}
