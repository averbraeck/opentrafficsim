package org.opentrafficsim.core.gtu.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.LaneChangeUrgeGTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneMovementStep;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedGTU<ID> extends AbstractGTU<ID> implements LaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Time of last evaluation. */
    private DoubleScalar.Abs<TimeUnit> lastEvaluationTime;

    /** Time of next evaluation. */
    private DoubleScalar.Abs<TimeUnit> nextEvaluationTime;

    /** Distance to the next required lane change and lateral direction thereof. */
    private LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection lastLaneChangeDistanceAndDirection =
            new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(new DoubleScalar.Rel<LengthUnit>(
                    Double.MAX_VALUE, LengthUnit.SI), null);

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the
     * lastEvaluationTime. Because the reference point of the GTU might not be on all the links the GTU is registered
     * on, the fractional longitudinal positions can be more than one, or less than zero.
     */
    private final Map<Link<?, ?>, Double> fractionalLinkPositions = new LinkedHashMap<>();

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as
     * well to keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the
     * vehicle is registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is
     * registered are at the front of the list, the later ones more to the back.
     */
    private final List<Lane> lanes = new ArrayList<>();

    /**
     * @return lanes.
     */
    public final List<Lane> getLanes()
    {
        return this.lanes;
    }

    /** Speed at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> speed;

    /** lateral velocity at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> lateralVelocity;

    /** acceleration (negative values indicate deceleration) at the lastEvaluationTime. */
    private DoubleScalar.Abs<AccelerationUnit> acceleration = new DoubleScalar.Abs<AccelerationUnit>(0,
            AccelerationUnit.METER_PER_SECOND_2);

    /** CarFollowingModel used by this GTU. */
    private final GTUFollowingModel gtuFollowingModel;

    /** LaneChangeModel used by this GTU. */
    private final LaneChangeModel laneChangeModel;

    /**
     * Construct a Lane Based GTU.
     * @param id the id of the GTU, could be String or Integer
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes
     * @param initialSpeed the initial speed of the car on the lane
     * @param route Route; the route that the GTU will take
     * @param simulator to initialize the move method and to get the current time
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
            final LaneChangeModel laneChangeModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final Route route, final OTSDEVSSimulatorInterface simulator)
            throws RemoteException, NetworkException, SimRuntimeException, GTUException
    {
        super(id, gtuType, route);
        if (null == gtuFollowingModel)
        {
            throw new GTUException("gtuFollowingModel may not be null");
        }
        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
        this.lateralVelocity = new DoubleScalar.Abs<SpeedUnit>(0.0, SpeedUnit.METER_PER_SECOND);

        // register the GTU on the lanes
        for (Lane lane : initialLongitudinalPositions.keySet())
        {
            this.lanes.add(lane);
            this.fractionalLinkPositions.put(lane.getParentLink(),
                    lane.fraction(initialLongitudinalPositions.get(lane)));
            lane.addGTU(this, initialLongitudinalPositions.get(lane));
        }

        this.lastEvaluationTime = simulator.getSimulatorTime().get();
        this.speed = initialSpeed;
        this.nextEvaluationTime = this.lastEvaluationTime;

        // start the movement of the GTU
        simulator.scheduleEventNow(this, this, "move", null);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(this.speed, Calc.accelerationTimesTime(this.getAcceleration(when), dT)).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity() throws RemoteException
    {
        return getLongitudinalVelocity(getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getVelocity() throws RemoteException
    {
        return getLongitudinalVelocity();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getLastEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.lastEvaluationTime);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getNextEvaluationTime()
    {
        return this.nextEvaluationTime;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration(final DoubleScalar.Abs<TimeUnit> when)
    {
        // Currently the acceleration is independent of when; it is constant during the evaluation interval
        return new DoubleScalar.Abs<AccelerationUnit>(this.acceleration);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration() throws RemoteException
    {
        return getAcceleration(getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<LengthUnit> getOdometer() throws RemoteException
    {
        return DoubleScalar.plus(this.odometer, deltaX(getSimulator().getSimulatorTime().get())).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLateralVelocity()
    {
        return new DoubleScalar.Abs<SpeedUnit>(this.lateralVelocity);
    }

    @Override
    public final void addFrontToSubsequentLane(final Lane lane) throws RemoteException, NetworkException
    {
        Lane prevLane = this.lanes.get(0); // TODO exceptions en zo.
        double positionPrevTimeStepSI = position(prevLane, getReference(), this.lastEvaluationTime).getSI();
        double positionNowSI = position(prevLane, getReference()).getSI();
        DoubleScalar.Rel<LengthUnit> position =
                new DoubleScalar.Rel<>(positionPrevTimeStepSI - positionNowSI + getReference().getDx().getSI()
                        - getFront().getDx().getSI(), LengthUnit.SI);
        addLane(lane, position);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane, final DoubleScalar.Rel<LengthUnit> position) throws NetworkException
    {
        if (this.lanes.contains(lane))
        {
            throw new NetworkException("GTU " + toString() + " is already registered on this lane: " + lane);
        }
        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as this
        // might lead to a "jump".
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
        {
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position));
        }
        this.lanes.add(lane);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLane(final Lane lane)
    {
        this.lanes.remove(lane);
        // check of there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.lanes)
        {
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                found = true;
            }
        }
        if (!found)
        {
            this.fractionalLinkPositions.remove(lane.getParentLink());
        }
    }

    /** Standard incentive to stay in the current lane. */
    private static final DoubleScalar.Rel<AccelerationUnit> STAYINCURRENTLANEINCENTIVE =
            new DoubleScalar.Rel<AccelerationUnit>(0.1, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final DoubleScalar.Rel<AccelerationUnit> PREFERREDLANEINCENTIVE =
            new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard incentive to stay in the current lane. */
    private static final DoubleScalar.Rel<AccelerationUnit> NONPREFERREDLANEINCENTIVE =
            new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2);

    /** Standard time horizon for route choices. */
    private static final DoubleScalar.Rel<TimeUnit> TIMEHORIZON = new DoubleScalar.Rel<TimeUnit>(90, TimeUnit.SECOND);

    /**
     * Move this GTU to it's current location, then compute (and commit to) the next movement step.
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     * @throws GTUException when GTU has not lane change model
     * @throws SimRuntimeException on not being able to reschedule this move() method.
     * @throws ValueException cannot happen
     */
    protected final void move() throws RemoteException, NetworkException, GTUException, SimRuntimeException,
            ValueException
    {
        if (getLongitudinalVelocity().getSI() < 0)
        {
            System.out.println("negative velocity: " + this + " " + getLateralVelocity().getSI() + "m/s");
        }
        /*-
        if (getId().toString().equals("4") && getSimulator().getSimulatorTime().get().getSI() > 129.4)
        {
            System.out.println("Debug me: " + getSimulator().getSimulatorTime() + " " + this + " " + this.getRoute()
                    + " " + this.getLongitudinalVelocity().getSI());
        }
         */
        // Quick sanity check
        if (getSimulator().getSimulatorTime().get().getSI() != getNextEvaluationTime().getSI())
        {
            throw new Error("move called at wrong time: expected time " + getNextEvaluationTime()
                    + " simulator time is : " + getSimulator().getSimulatorTime().get());
        }
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanes.isEmpty())
        {
            return; // Done; do not re-schedule execution of this move method.
        }
        DoubleScalar.Rel<LengthUnit> maximumForwardHeadway = new DoubleScalar.Rel<LengthUnit>(500.0, LengthUnit.METER);
        // TODO 500?
        DoubleScalar.Rel<LengthUnit> maximumReverseHeadway = new DoubleScalar.Rel<LengthUnit>(-200.0, LengthUnit.METER);
        // TODO 200?
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100.0, SpeedUnit.KM_PER_HOUR);
        // TODO should be the local speed limit and based on the maximum lane speed and the maximum GTU speed
        if (null == this.laneChangeModel)
        {
            throw new GTUException("All LaneBasedGTUs should have a LaneChangeModel");
        }
        Collection<HeadwayGTU> sameLaneTraffic = new ArrayList<HeadwayGTU>();
        HeadwayGTU leader = headway(maximumForwardHeadway);
        if (null != leader.getOtherGTU())
        {
            sameLaneTraffic.add(leader);
        }
        HeadwayGTU follower = headway(maximumReverseHeadway);
        if (null != follower.getOtherGTU())
        {
            sameLaneTraffic.add(new HeadwayGTU(follower.getOtherGTU(), -follower.getDistanceSI()));
        }
        DoubleScalar.Abs<TimeUnit> now = getSimulator().getSimulatorTime().get();
        Collection<HeadwayGTU> leftLaneTraffic =
                collectNeighborLaneTraffic(LateralDirectionality.LEFT, now, maximumForwardHeadway,
                        maximumReverseHeadway);
        Collection<HeadwayGTU> rightLaneTraffic =
                collectNeighborLaneTraffic(LateralDirectionality.RIGHT, now, maximumForwardHeadway,
                        maximumReverseHeadway);
        // FIXME: whether we drive on the right should be stored in some central place.
        final LateralDirectionality preferred = LateralDirectionality.RIGHT;
        final DoubleScalar.Rel<AccelerationUnit> defaultLeftLaneIncentive =
                LateralDirectionality.LEFT == preferred ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;
        final DoubleScalar.Rel<AccelerationUnit> defaultRightLaneIncentive =
                LateralDirectionality.RIGHT == preferred ? PREFERREDLANEINCENTIVE : NONPREFERREDLANEINCENTIVE;
        DoubleVector.Rel.Dense<AccelerationUnit> defaultLaneIncentives =
                new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{defaultLeftLaneIncentive.getSI(),
                        STAYINCURRENTLANEINCENTIVE.getSI(), defaultRightLaneIncentive.getSI()}, AccelerationUnit.SI);
        DoubleVector.Rel.Dense<AccelerationUnit> laneIncentives = laneIncentives(defaultLaneIncentives);
        LaneMovementStep lcmr =
                this.laneChangeModel.computeLaneChangeAndAcceleration(this, sameLaneTraffic, rightLaneTraffic,
                        leftLaneTraffic, speedLimit,
                        laneIncentives.get(preferred == LateralDirectionality.RIGHT ? 2 : 0), laneIncentives.get(1),
                        laneIncentives.get(preferred == LateralDirectionality.RIGHT ? 0 : 2));
        // TODO: detect that a required lane change was blocked and, if it was, do something to find/create a gap.
        if (lcmr.getGfmr().getAcceleration().getSI() < -9999)
        {
            System.out.println("Problem");
        }
        // First move this GTU forward (to its current location) as determined in the PREVIOUS move step.
        // GTUs move based on their fractional position to stay aligned when registered in parallel lanes.
        // The "oldest" lane of parallel lanes takes preference when updating the fractional position.
        // So we work from back to front.
        for (int i = this.lanes.size() - 1; i >= 0; i--)
        {
            Lane lane = this.lanes.get(i);
            this.fractionalLinkPositions.put(lane.getParentLink(),
                    lane.fraction(position(lane, getReference(), this.nextEvaluationTime)));
        }
        // Update the odometer value
        this.odometer = DoubleScalar.plus(this.odometer, deltaX(this.nextEvaluationTime)).immutable();
        // Compute and set the current speed using the "old" nextEvaluationTime and acceleration
        this.speed = getLongitudinalVelocity(this.nextEvaluationTime);
        // Now update last evaluation time
        this.lastEvaluationTime = this.nextEvaluationTime;
        // Set the next evaluation time
        this.nextEvaluationTime = lcmr.getGfmr().getValidUntil();
        // Set the acceleration (this totally defines the longitudinal motion until the next evaluation time)
        this.acceleration = lcmr.getGfmr().getAcceleration();
        // Execute all samplers
        for (Lane lane : this.lanes)
        {
            lane.sample(this);
        }
        // Change onto laterally adjacent lane(s) if the LaneMovementStep indicates a lane change
        if (lcmr.getLaneChange() != null)
        {
            synchronized (this.lanes)
            {
                // TODO: make lane changes gradual (not instantaneous; like now)
                Collection<Lane> oldLaneSet = new ArrayList<Lane>(this.lanes);
                Collection<Lane> newLaneSet = adjacentLanes(lcmr.getLaneChange());
                // Remove this GTU from all of the Lanes that it is on and remember the fractional position on each
                // one
                Map<Lane, Double> oldFractionalPositions = new LinkedHashMap<Lane, Double>();
                for (Lane l : this.lanes)
                {
                    oldFractionalPositions.put(l, fractionalPosition(l, getReference(), getLastEvaluationTime()));
                    this.fractionalLinkPositions.remove(l.getParentLink());
                }
                for (Lane l : oldFractionalPositions.keySet())
                {
                    l.removeGTU(this);
                    removeLane(l);
                }
                ArrayList<Lane> replacementLanes = new ArrayList<Lane>(); // for DEBUG
                // Add this GTU to the lanes in newLaneSet.
                // This could be rewritten to be more efficient.
                for (Lane newLane : newLaneSet)
                {
                    Double fractionalPosition = null;
                    // find ONE lane in oldLaneSet that has l as neighbor
                    for (Lane oldLane : oldLaneSet)
                    {
                        if (oldLane.accessibleAdjacentLanes(lcmr.getLaneChange(), getGTUType()).contains(newLane))
                        {
                            fractionalPosition = oldFractionalPositions.get(oldLane);
                            break;
                        }
                    }
                    if (null == fractionalPosition)
                    {
                        throw new Error("Program error: Cannot find an oldLane that has newLane " + newLane + " as "
                                + lcmr.getLaneChange() + " neighbor");
                    }
                    newLane.addGTU(this, fractionalPosition);
                    addLane(newLane, newLane.getLength().mutable().multiplyBy(fractionalPosition).immutable());
                    replacementLanes.add(newLane);
                }
                System.out.println("GTU " + this + " changed lanes from: " + oldLaneSet + " to " + replacementLanes);
                checkConsistency();
            }
        }
        // The GTU is now committed to executed the entire movement stored in the LaneChangeModelResult
        // Schedule all sensor triggers that are going to happen until the next evaluation time.
        scheduleTriggers();
        // Re-schedule this move method at the end of the committed time step.
        getSimulator().scheduleEventAbs(this.getNextEvaluationTime(), this, this, "move", null);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU on the
     * intended route.
     * @param defaultLaneIncentives DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives for the
     *            next left adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     */
    private DoubleVector.Rel.Dense<AccelerationUnit> laneIncentives(
            final DoubleVector.Rel.Dense<AccelerationUnit> defaultLaneIncentives) throws RemoteException,
            NetworkException, ValueException
    {
        DoubleScalar.Rel<LengthUnit> leftSuitability = suitability(LateralDirectionality.LEFT);
        DoubleScalar.Rel<LengthUnit> currentSuitability = suitability(null);
        DoubleScalar.Rel<LengthUnit> rightSuitability = suitability(LateralDirectionality.RIGHT);
        if (currentSuitability == Route.NOLANECHANGENEEDED)
        {
            this.lastLaneChangeDistanceAndDirection =
                    new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(currentSuitability, null);
        }
        else
        {
            this.lastLaneChangeDistanceAndDirection =
                    new LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection(currentSuitability,
                            rightSuitability.getSI() == 0 ? true : leftSuitability.gt(rightSuitability));
        }
        if ((leftSuitability == Route.NOLANECHANGENEEDED || leftSuitability == Route.GETOFFTHISLANENOW)
                && currentSuitability == Route.NOLANECHANGENEEDED
                && (rightSuitability == Route.NOLANECHANGENEEDED || rightSuitability == Route.GETOFFTHISLANENOW))
        {
            return checkLaneDrops(defaultLaneIncentives);
        }
        if (currentSuitability == Route.NOLANECHANGENEEDED)
        {
            return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{acceleration(leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(rightSuitability)}, AccelerationUnit.SI);
        }
        return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{acceleration(leftSuitability),
                acceleration(currentSuitability), acceleration(rightSuitability)}, AccelerationUnit.SI);
    }

    /**
     * Figure out if the default lane incentives are OK, or override them with values that should keep this GTU from
     * running out of road at an upcoming lane drop.
     * @param defaultLaneIncentives DoubleVector.Rel.Dense&lt;AccelerationUnit&gt; the three lane incentives for the
     *            next left adjacent lane, the current lane and the next right adjacent lane
     * @return DoubleVector.Rel.Dense&lt;AccelerationUnit&gt;; the (possibly adjusted) lane incentives
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     * @throws ValueException cannot happen
     */
    private DoubleVector.Rel.Dense<AccelerationUnit> checkLaneDrops(
            final DoubleVector.Rel.Dense<AccelerationUnit> defaultLaneIncentives) throws RemoteException,
            NetworkException, ValueException
    {
        DoubleScalar.Rel<LengthUnit> leftSuitability = laneDrop(LateralDirectionality.LEFT);
        DoubleScalar.Rel<LengthUnit> currentSuitability = laneDrop(null);
        DoubleScalar.Rel<LengthUnit> rightSuitability = laneDrop(LateralDirectionality.RIGHT);
        if ((leftSuitability == Route.NOLANECHANGENEEDED || leftSuitability == Route.GETOFFTHISLANENOW)
                && currentSuitability == Route.NOLANECHANGENEEDED
                && (rightSuitability == Route.NOLANECHANGENEEDED || rightSuitability == Route.GETOFFTHISLANENOW))
        {
            return defaultLaneIncentives;
        }
        if (currentSuitability == Route.NOLANECHANGENEEDED)
        {
            return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{acceleration(leftSuitability),
                    defaultLaneIncentives.get(1).getSI(), acceleration(rightSuitability)}, AccelerationUnit.SI);
        }
        if (currentSuitability.le(leftSuitability))
        {
            return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{PREFERREDLANEINCENTIVE.getSI(),
                    NONPREFERREDLANEINCENTIVE.getSI(), Route.GETOFFTHISLANENOW.getSI()}, AccelerationUnit.SI);
        }
        if (currentSuitability.le(rightSuitability))
        {
            return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{Route.GETOFFTHISLANENOW.getSI(),
                    NONPREFERREDLANEINCENTIVE.getSI(), PREFERREDLANEINCENTIVE.getSI()}, AccelerationUnit.SI);
        }
        return new DoubleVector.Rel.Dense<AccelerationUnit>(new double[]{acceleration(leftSuitability),
                acceleration(currentSuitability), acceleration(rightSuitability)}, AccelerationUnit.SI);
    }

    /**
     * Return the distance until the next lane drop in the specified (nearby) lane.
     * @param direction LateralDirectionality; one of the values <cite>LateralDirectionality.LEFT</cite> (use the
     *            left-adjacent lane), or <cite>LateralDirectionality.RIGHT</cite> (use the right-adjacent lane), or
     *            <cite>null</cite> (use the current lane)
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; distance until the next lane drop if it occurs within the
     *         TIMEHORIZON, or Route.NOLANECHANGENEEDED if this lane can be followed until the next split junction or
     *         until beyond the TIMEHORIZON
     * @throws NetworkException on network inconsistency
     * @throws RemoteException on communications failure
     */
    private DoubleScalar.Rel<LengthUnit> laneDrop(final LateralDirectionality direction) throws NetworkException,
            RemoteException
    {
        Lane lane = null;
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = null;
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction)
        {
            for (Lane l : getLanes())
            {
                if (l.getLaneType().isCompatible(getGTUType()))
                {
                    lane = l;
                }
            }
            if (null == lane)
            {
                throw new NetworkException("GTU " + this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        }
        else
        {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            lane = lane.bestAccessibleAdjacentLane(direction, longitudinalPosition, getGTUType());
        }
        if (null == lane)
        {
            return Route.GETOFFTHISLANENOW;
        }
        double remainingLength = lane.getLength().getSI() - longitudinalPosition.getSI();
        double remainingTimeSI = TIMEHORIZON.getSI() - remainingLength / lane.getSpeedLimit().getSI();
        while (remainingTimeSI >= 0)
        {
            if (lane instanceof SinkLane)
            {
                return Route.NOLANECHANGENEEDED;
            }
            int branching = lane.nextLanes().size();
            if (branching == 0)
            {
                return new DoubleScalar.Rel<LengthUnit>(remainingLength, LengthUnit.SI);
            }
            if (branching > 1)
            {
                return Route.NOLANECHANGENEEDED;
            }
            lane = lane.nextLanes().iterator().next();
            remainingTimeSI -= lane.getLength().getSI() / lane.getSpeedLimit().getSI();
            remainingLength += lane.getLength().getSI();
        }
        return Route.NOLANECHANGENEEDED;
    }

    /**
     * Compute deceleration needed to stop at a specified distance.
     * @param stopDistance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return double; the acceleration (deceleration) needed to stop at the specified distance in m/s/s
     * @throws RemoteException on communications failure
     */
    private double acceleration(final DoubleScalar.Rel<LengthUnit> stopDistance) throws RemoteException
    {
        // What is the deceleration that will bring this GTU to a stop at exactly the suitability distance?
        // Answer: a = -v^2 / 2 / suitabilityDistance
        double v = getLongitudinalVelocity().getSI();
        double a = -v * v / 2 / stopDistance.getSI();
        return a;
    }

    /**
     * Return the suitability for the current lane, left adjacent lane or right adjacent lane.
     * @param direction LateralDirectionality; one of the values <cite>null</cite>,
     *            <cite>LateralDirectionality.LEFT</cite>, or <cite>LateralDirectionality.RIGHT</cite>
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the suitability of the lane for reaching the (next) destination
     * @throws NetworkException on network inconsistency
     * @throws RemoteException on communications failure
     */
    private DoubleScalar.Rel<LengthUnit> suitability(final LateralDirectionality direction) throws NetworkException,
            RemoteException
    {
        Lane lane = null;
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = null;
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = positions(RelativePosition.REFERENCE_POSITION);
        if (null == direction)
        {
            for (Lane l : getLanes())
            {
                if (l.getLaneType().isCompatible(getGTUType()))
                {
                    lane = l;
                }
            }
            if (null == lane)
            {
                throw new NetworkException("GTU " + this + " is not on any compatible lane");
            }
            longitudinalPosition = positions.get(lane);
        }
        else
        {
            lane = positions.keySet().iterator().next();
            longitudinalPosition = positions.get(lane);
            lane = lane.bestAccessibleAdjacentLane(direction, longitudinalPosition, getGTUType());
        }
        if (null == lane)
        {
            return Route.GETOFFTHISLANENOW;
        }
        return getRoute().suitability(lane, longitudinalPosition, getGTUType(), TIMEHORIZON);
    }

    /**
     * Verify that all the lanes registered in this GTU have this GTU registered as well and vice versa.
     */
    private void checkConsistency()
    {
        for (Lane l : this.lanes)
        {
            if (!this.fractionalLinkPositions.containsKey(l.getParentLink()))
            {
                System.err.println("GTU " + this + " is in lane " + l
                        + " but that GTU has no fractional position on the link of that lane");
            }
        }
        for (Link<?, ?> csl : this.fractionalLinkPositions.keySet())
        {
            boolean found = false;
            for (Lane l : this.lanes)
            {
                if (l.getParentLink().equals(csl))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                System.err.println("GTU " + this + " has a fractional position "
                        + this.fractionalLinkPositions.get(csl) + " on link " + csl
                        + " but this GTU is not on any lane(s) of that link");
            }
        }
    }

    /**
     * Schedule the triggers for this GTU during the new time period.
     * @throws NetworkException on network inconsistency
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException should never happen
     * @throws GTUException when a branch is reached where the GTU does not know where to go next
     */
    private void scheduleTriggers() throws NetworkException, RemoteException, SimRuntimeException, GTUException
    {
        // Does our front reference point enter new lane(s) during the next time step?
        // Note: the trigger at the start of the lane will add the vehicle to that lane at the exact right time.
        for (Lane lane : this.lanes)
        {
            double frontPosSI = position(lane, getFront(), this.lastEvaluationTime).getSI();
            double frontPosAtNextEvalSI = position(lane, getFront(), this.nextEvaluationTime).getSI();
            double remainingDistanceSI = lane.getLength().getSI() - frontPosSI;
            // double excessNextLaneSI = frontPosAtNextEvalSI - lane.getLength().getSI();
            double moveSI = frontPosAtNextEvalSI - frontPosSI;
            if (lane.fractionSI(frontPosSI) <= 1.0 && lane.fractionSI(frontPosAtNextEvalSI) > 1.0)
            {
                int branching = lane.nextLanes().size();
                if (1 == branching)
                {
                    lane.nextLanes().iterator().next()
                            .scheduleTriggers(this, -remainingDistanceSI - getFront().getDx().getSI(), moveSI);
                }
                else if (branching > 1)
                {
                    if (null == this.getRoute())
                    {
                        throw new GTUException(this + " reaches branch and but has not route");
                    }
                    Node<?, ?> nextNode = this.getRoute().nextNodeToVisit();
                    if (null == nextNode)
                    {
                        throw new GTUException(this + " reaches branch and the route returns null as nextNodeToVisit");
                    }
                    int continuingLaneCount = 0;
                    for (Lane nextLane : lane.nextLanes())
                    {
                        if (this.lanes.contains(nextLane))
                        {
                            continue; // Already on this lane
                        }
                        if (nextNode == nextLane.getParentLink().getEndNode())
                        {
                            nextLane.scheduleTriggers(this, -remainingDistanceSI - getFront().getDx().getSI(), moveSI);
                            continuingLaneCount++;
                        }
                    }
                    if (0 == continuingLaneCount)
                    {
                        throw new NetworkException(this
                                + " reached branch and the route specifies a nextNodeToVisit that is not a next node "
                                + "at this branch (" + lane.getParentLink().getEndNode() + ")");
                    }
                }
            }
            // also schedule any triggers for the current lane(s)
            lane.scheduleTriggers(this, lane.positionSI(this.fractionalLinkPositions.get(lane.getParentLink())), moveSI);
        }

        // for (Lane lane : triggerLanes)
        // {
        // double dt = this.nextEvaluationTime.getSI() - this.getLastEvaluationTime().getSI();
        //
        // double frontPosSI = position(lane, getFront(), this.lastEvaluationTime).getSI();
        // double positionSI = frontPosSI - lane.getLength().getSI() - getFront().getDx().getSI();
        // fractionalLinkPosition = lane.fractionSI(positionSI);
        // }
    }

    /**
     * Collect relevant traffic in adjacent lanes. Parallel traffic is included with headway equal to Double.NaN.
     * @param directionality LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the (current) time
     * @param maximumForwardHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum forward search distance
     * @param maximumReverseHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum reverse search distance
     * @return Collection&lt;LaneBasedGTU&lt;?&gt;&gt;;
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     */
    private Collection<HeadwayGTU> collectNeighborLaneTraffic(final LateralDirectionality directionality,
            final DoubleScalar.Abs<TimeUnit> when, final DoubleScalar.Rel<LengthUnit> maximumForwardHeadway,
            final DoubleScalar.Rel<LengthUnit> maximumReverseHeadway) throws RemoteException, NetworkException
    {
        Collection<HeadwayGTU> result = new LinkedHashSet<HeadwayGTU>();
        for (LaneBasedGTU<?> p : parallel(directionality, when))
        {
            result.add(new HeadwayGTU(p, Double.NaN));
        }
        for (Lane adjacentLane : adjacentLanes(directionality))
        {
            HeadwayGTU leader = headway(adjacentLane, maximumForwardHeadway);
            if (null != leader.getOtherGTU() && !result.contains(leader))
            {
                result.add(leader);
            }
            HeadwayGTU follower = headway(adjacentLane, maximumReverseHeadway);
            if (null != follower.getOtherGTU() && !result.contains(follower))
            {
                result.add(new HeadwayGTU(follower.getOtherGTU(), -follower.getDistanceSI()));
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition)
            throws NetworkException, RemoteException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes)
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane lane, final RelativePosition relativePosition)
            throws NetworkException, RemoteException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    public final DoubleScalar.Rel<LengthUnit> projectedPosition(final Lane projectionLane,
            final RelativePosition relativePosition, final DoubleScalar.Abs<TimeUnit> when) throws NetworkException,
            RemoteException
    {
        CrossSectionLink<?, ?> link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (this.lanes.contains(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new DoubleScalar.Rel<LengthUnit>(projectionLane.getLength().getSI() * fractionalPosition,
                            LengthUnit.SI);
                }
            }
        }
        throw new NetworkException("GTU " + this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane lane, final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        if (null == lane)
        {
            throw new NetworkException("lane is null");
        }
        if (!this.lanes.contains(lane))
        {
            throw new NetworkException("GTU is not on lane " + lane.toString());
        }
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
        {
            throw new NetworkException("GTU does not have a fractional position on " + lane.toString());
        }
        DoubleScalar.Rel<LengthUnit> longitudinalPosition =
                lane.position(this.fractionalLinkPositions.get(lane.getParentLink()));
        if (longitudinalPosition == null)
        {
            // According to FindBugs; this cannot happen; PK is unsure whether FindBugs is correct.
            throw new NetworkException("GetPosition: GTU " + toString() + " not in lane " + lane);
        }
        DoubleScalar.Rel<LengthUnit> loc =
                DoubleScalar.plus(DoubleScalar.plus(longitudinalPosition, deltaX(when)).immutable(),
                        relativePosition.getDx()).immutable();
        if (Double.isNaN(loc.getSI()))
        {
            System.out.println("loc is NaN");
        }
        return loc;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition)
            throws NetworkException, RemoteException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes)
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition)
            throws NetworkException, RemoteException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the
     *            GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE
     *         meters when no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI,
            final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException
    {
        LaneBasedGTU<?> otherGTU =
                lane.getGtuAfter(new DoubleScalar.Rel<LengthUnit>(lanePositionSI, LengthUnit.METER),
                        RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceM =
                    cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = lane.nextLanes();
            if (nextLanes.size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a "real" Route
                    if (this.getRoute() == null || this.getRoute().size() == 0 /* XXXXX STUB dummy route */
                            || this.getRoute().containsLink(lane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        HeadwayGTU closest =
                                headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when);
                        if (closest.getDistanceSI() < maxDistanceSI
                                && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                        {
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
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the
     * other GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the
     *            rear of the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength()
     *            for each subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE
     *            number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this
     *            is a POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE
     *         meters when no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
            final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException
    {
        LaneBasedGTU<?> otherGTU =
                lane.getGtuBefore(new DoubleScalar.Rel<LengthUnit>(lanePositionSI, LengthUnit.METER),
                        RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM =
                    cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            Set<Lane> prevLanes = lane.prevLanes();
            if (prevLanes.size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane prevLane : prevLanes)
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    HeadwayGTU closest =
                            headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), traveledDistanceSI,
                                    maxDistanceSI, when);
                    if (closest.getDistanceSI() < maxDistanceSI
                            && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                    {
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
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU
     *         with a distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI
     *         meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayGTUSI(final double maxDistanceSI) throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        if (maxDistanceSI > 0.0)
        {
            // look forward.
            for (Lane lane : positions(getFront()).keySet())
            {
                HeadwayGTU closest =
                        headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(), when).getSI(), 0.0,
                                maxDistanceSI, when);
                if (closest.getDistanceSI() < maxDistanceSI
                        && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        else
        {
            // look backward.
            for (Lane lane : positions(getRear()).keySet())
            {
                HeadwayGTU closest =
                        headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(), when).getSI(), 0.0,
                                -maxDistanceSI, when);
                if (closest.getDistanceSI() < -maxDistanceSI
                        && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        return foundMaxGTUDistanceSI;
    }

    /** {@inheritDoc} */
    @Override
    public final HeadwayGTU headway(final DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
            NetworkException
    {
        return headwayGTUSI(maxDistance.getSI());
    }

    /** {@inheritDoc} */
    @Override
    public final HeadwayGTU headway(final Lane lane, final DoubleScalar.Rel<LengthUnit> maxDistance)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        if (maxDistance.getSI() > 0.0)
        {
            return headwayRecursiveForwardSI(lane, this.projectedPosition(lane, this.getFront(), when).getSI(), 0.0,
                    maxDistance.getSI(), when);
        }
        else
        {
            return headwayRecursiveBackwardSI(lane, this.projectedPosition(lane, this.getRear(), when).getSI(), 0.0,
                    -maxDistance.getSI(), when);
        }
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the
     *            (front of the) GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each
     *            subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be
     *         found within maxDistanceSI
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI,
            final LaneBasedGTU<?> otherGTU, final double cumDistanceSI, final double maxDistanceSI,
            final DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM =
                    cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = lane.nextLanes();
            if (nextLanes.size() > 0)
            {
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a Route
                    if (this.getRoute() == null || this.getRoute().size() == 0 /* XXXXX STUB dummy route */
                            || this.getRoute().containsLink(lane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        double headwaySuccessor =
                                headwayRecursiveForwardSI(nextLane, 0.0, otherGTU, traveledDistanceSI, maxDistanceSI,
                                        when);
                        if (headwaySuccessor < maxDistanceSI)
                        {
                            return headwaySuccessor;
                        }
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
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the
     *            (back of) the GTU when we measure in the lane where the original GTU is positioned, and the length of
     *            the lane for each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes, as a POSITIVE number
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls, as a POSITIVE number
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be
     *         found within maxDistanceSI
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
            final LaneBasedGTU<?> otherGTU, final double cumDistanceSI, final double maxDistanceSI,
            final DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM =
                    cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> prevLanes = lane.prevLanes();
            if (prevLanes.size() > 0)
            {
                for (Lane prevLane : prevLanes)
                {
                    // Routes are NOT IMPORTANT when we look backward.
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    // PK: This looks like a bug; replacement code below this comment.
                    // double headwayPredecessor =
                    // headwayRecursiveForwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
                    // traveledDistanceSI, maxDistanceSI, when);
                    double headwayPredecessor =
                            headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
                                    traveledDistanceSI, maxDistanceSI, when);
                    if (headwayPredecessor < maxDistanceSI)
                    {
                        return headwayPredecessor;
                    }
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU<?>> parallel(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException
    {
        Set<LaneBasedGTU<?>> gtuSet = new LinkedHashSet<LaneBasedGTU<?>>();
        for (Lane l : this.lanes)
        {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                // compare based on fractional positions.
                double posFractionFront = Math.max(0.0, this.fractionalPosition(l, getFront(), when));
                double posFractionRear = Math.min(1.0, this.fractionalPosition(l, getRear(), when));
                for (LaneBasedGTU<?> gtu : lane.getGtuList())
                {
                    if (!gtu.equals(this))
                    {
                        double gtuFractionFront = Math.max(0.0, gtu.fractionalPosition(lane, gtu.getFront(), when));
                        double gtuFractionRear = Math.min(1.0, gtu.fractionalPosition(lane, gtu.getRear(), when));
                        if (gtuFractionFront >= posFractionRear && gtuFractionRear <= posFractionFront)
                        {
                            gtuSet.add(gtu);
                        }
                    }
                }
            }
        }
        return gtuSet;
    }

    /**
     * Build a set of Lanes that is adjacent to the lanes that this GTU is in, in the specified lateral direction.
     * @param lateralDirection LateralDirectionality; the lateral direction.
     * @return Set&lt;Lane&gt;
     */
    private Set<Lane> adjacentLanes(final LateralDirectionality lateralDirection)
    {
        Set<Lane> result = new LinkedHashSet<Lane>();
        for (Lane lane : this.lanes)
        {
            result.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU<?>> parallel(final LateralDirectionality lateralDirection,
            final DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException
    {
        Set<Lane> adjacentLanes = adjacentLanes(lateralDirection);
        /*-                       new LinkedHashSet<Lane>();
        for (Lane lane : this.lanes)
        {
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
        }
         */
        Set<LaneBasedGTU<?>> gtuSet = new LinkedHashSet<LaneBasedGTU<?>>();
        for (Lane adjacentLane : adjacentLanes)
        {
            gtuSet.addAll(parallel(adjacentLane, when));
        }
        return gtuSet;
    }

    /** {@inheritDoc} */
    public final DoubleScalar.Abs<TimeUnit> timeAtDistance(final DoubleScalar.Rel<LengthUnit> distance)
    {
        Double result = solveTimeForDistance(distance);
        if (null == result)
        {
            return null;
        }
        return new DoubleScalar.Abs<TimeUnit>(this.lastEvaluationTime.getSI() + result, TimeUnit.SECOND);
    }

    /** {@inheritDoc} */
    public final DoubleScalar.Rel<TimeUnit> deltaTimeForDistance(final DoubleScalar.Rel<LengthUnit> distance)
    {
        Double result = solveTimeForDistance(distance);
        if (null == result)
        {
            return null;
        }
        return new DoubleScalar.Rel<TimeUnit>(result, TimeUnit.SECOND);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        while (!this.lanes.isEmpty())
        {
            Lane lane = this.lanes.get(0);
            removeLane(lane);
            lane.removeGTU(this);
        }
    }

    /**
     * Determine longitudinal displacement.
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the displacement since last move evaluation
     * @throws RemoteException on communications failure
     */
    private DoubleScalar.Rel<LengthUnit> deltaX(final DoubleScalar.Abs<TimeUnit> when) throws RemoteException
    {
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(Calc.speedTimesTime(this.speed, dT),
                Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(), dT)).immutable();
    }

    /**
     * Determine show long it will take for this GTU to cover the specified distance (both time and distance since the
     * last evaluation time).
     * @param distance double; the distance
     * @return Double; the relative time, or null when this GTU stops before covering the specified distance
     */
    private Double solveTimeForDistance(final DoubleScalar.Rel<LengthUnit> distance)
    {
        /*
         * Currently (!) a (Lane based) GTU commits to a constant acceleration until the next evaluation time. When/If
         * that is changed, this method will have to be re-written.
         */
        double c = -distance.getSI();
        double a = this.acceleration.getSI() / 2;
        double b = this.speed.getSI();
        if (0 == a)
        {
            if (b > 0)
            {
                return -c / b;
            }
            return null;
        }
        // Solve a * t^2 + b * t + c = 0
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0)
        {
            return null;
        }
        // The solutions are (-b +/- sqrt(discriminant)) / 2 / a
        double solution1 = (-b - Math.sqrt(discriminant)) / 2 / a;
        double solution2 = (-b + Math.sqrt(discriminant)) / 2 / a;
        if (solution1 < 0 && solution2 < 0)
        {
            return null;
        }
        if (solution1 < 0)
        {
            return solution2;
        }
        if (solution2 < 0)
        {
            return solution1;
        }
        // Both are >= 0; return the smallest one
        if (solution1 < solution2)
        {
            return solution1;
        }
        return solution2;
    }

    /**
     * Retrieve the GTUFollowingModel of this GTU.
     * @return GTUFollowingModel
     */
    public final GTUFollowingModel getGTUFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        synchronized (this.lanes)
        {
            try
            {
                if (this.lanes.size() == 0)
                {
                    // This happens temporarily when a GTU is moved to another Lane
                    return new DirectedPoint(Double.MAX_VALUE, Double.MAX_VALUE, 0);
                }
                Lane lane = this.lanes.get(0);
                // TODO solve problem when point is still on previous lane.
                DoubleScalar.Rel<LengthUnit> longitudinalPos = position(lane, getReference());
                double fraction = (longitudinalPos.getSI() + getLength().getSI() / 2.0) / lane.getLength().getSI();
                LineString line = lane.getCenterLine();
                LengthIndexedLine lil = new LengthIndexedLine(line);
                // if (fraction > 1)
                // {
                // System.out.println("fraction is " + fraction);
                // }
                double useFraction = fraction;
                boolean fractionAdjusted = false;
                if (fraction < 0)
                {
                    useFraction = 0;
                    fractionAdjusted = true;
                }
                if (fraction > 0.99)
                {
                    useFraction = 0.99;
                    fractionAdjusted = true;
                }
                // DO NOT MODIFY THE RESULT OF extractPoint (it may be one of the coordinates in line).
                Coordinate c = new Coordinate(lil.extractPoint(useFraction * line.getLength()));
                c.z = 0d;
                Coordinate cb = lil.extractPoint((useFraction + 0.01) * line.getLength());
                double angle = Math.atan2(cb.y - c.y, cb.x - c.x);
                if (fractionAdjusted)
                {
                    c =
                            new Coordinate(c.x + (fraction - useFraction) * 100 * (cb.x - c.x), c.y
                                    + (fraction - useFraction) * 100 * (cb.y - c.y), c.z);
                }
                if (Double.isNaN(c.x))
                {
                    System.out.println("Bad");
                }
                return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0,
                        angle);
            }
            catch (Exception ne)
            {
                System.err.println(this.getId());
                ne.printStackTrace();
                return new DirectedPoint(0, 0, 0);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    public LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection getLaneChangeDistanceAndDirection()
    {
        return this.lastLaneChangeDistanceAndDirection;
    }

    /**
     * Description of Car at specified time.
     * @param lane the position on this lane will be returned.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
    {
        double pos;
        try
        {
            pos = this.position(lane, getFront(), when).getSI();
        }
        catch (NetworkException | RemoteException exception)
        {
            pos = Double.NaN;
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s^2", getId(),
                this.lastEvaluationTime.getSI(), getNextEvaluationTime().getSI(), pos,
                this.getLongitudinalVelocity(when).getSI(), this.getAcceleration(when).getSI());
    }

}
