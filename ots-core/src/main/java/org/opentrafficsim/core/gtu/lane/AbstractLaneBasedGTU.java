package org.opentrafficsim.core.gtu.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.AccelerationStep;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneMovementStep;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.CrossSectionElement;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the
     * lastEvaluationTime. Because the reference point of the GTU might not be on all the links the GTU is registered
     * on, the fractional longitudinal positions can be more than one, or less than zero.
     */
    private final Map<Link<?, ?>, Double> fractionalLinkPositions = new HashMap<>();

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as
     * well to keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the
     * vehicle is registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is
     * registered are at the front of the list, the later ones more to the back.
     */
    private final List<Lane> lanes = new ArrayList<>();

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
     * @param simulator to initialize the move method and to get the current time
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
            final LaneChangeModel laneChangeModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final OTSDEVSSimulatorInterface simulator)
            throws RemoteException, NetworkException, SimRuntimeException
    {
        super(id, gtuType);
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

        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.lastEvaluationTime = new DoubleScalar.Abs<TimeUnit>(simulator.getSimulatorTime().get());
        this.speed = new DoubleScalar.Abs<SpeedUnit>(initialSpeed);
        this.nextEvaluationTime = new DoubleScalar.Abs<TimeUnit>(simulator.getSimulatorTime().get());

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
    public final DoubleScalar.Abs<SpeedUnit> getLateralVelocity()
    {
        return new DoubleScalar.Abs<SpeedUnit>(this.lateralVelocity);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane)
    {
        addLane(lane, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER));
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane, final DoubleScalar.Rel<LengthUnit> position)
    {
        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as this
        // might
        // lead to a "jump".
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

    /**
     * Set the new state.
     * @param cfmr AccelerationStep; the new state of this GTU
     * @throws RemoteException when simulator time could not be retrieved or sensor trigger scheduling fails.
     * @throws NetworkException when the vehicle is not on the given lane.
     * @throws SimRuntimeException when sensor trigger(s) cannot be scheduled on the simulator.
     */
    private void setState(final AccelerationStep cfmr) throws RemoteException, NetworkException, SimRuntimeException
    {
        // GTUs move based on their fractional position to stay aligned when registered in parallel lanes.
        // The "oldest" lane of parallel lanes takes preference when updating the fractional position.
        // So we work from back to front.
        for (int i = this.lanes.size() - 1; i >= 0; i--)
        {
            Lane lane = this.lanes.get(i);
            this.fractionalLinkPositions.put(lane.getParentLink(),
                    lane.fraction(position(lane, getReference(), this.nextEvaluationTime)));
        }
        // Compute and set the current speed using the "old" nextEvaluationTime and acceleration
        this.speed = getLongitudinalVelocity(this.nextEvaluationTime);
        // Update lastEvaluationTime and then set the new nextEvaluationTime
        this.lastEvaluationTime = this.nextEvaluationTime;
        this.nextEvaluationTime = cfmr.getValidUntil();
        this.acceleration = cfmr.getAcceleration();

        // Does our front reference point enter new lane(s) during the next time step? If so, add to our lane list!
        // Note: the trigger at the start of the lane will add the vehicle to that lane at the exact right time.
        List<Lane> lanesToCheck = new ArrayList<Lane>(this.lanes);
        while (!lanesToCheck.isEmpty())
        {
            Lane lane = lanesToCheck.remove(0);
            double frontPosSI = position(lane, getFront(), this.lastEvaluationTime).getSI();
            // TODO speed this up by using SI units, caching, etc.
            if (lane.fractionSI(frontPosSI) <= 1.0
                    && lane.fraction(position(lane, getFront(), this.nextEvaluationTime)) > 1.0)
            {
                for (Lane nextLane : lane.nextLanes())
                {
                    // Only follow links on the Route if there is a Route, and we haven't added this lane already
                    if (!this.lanes.contains(nextLane)
                            && (this.getRoute() == null || this.getRoute() != null
                                    && this.getRoute().containsLink(nextLane.getParentLink())))
                    {
                        this.lanes.add(nextLane);
                        if (!this.fractionalLinkPositions.containsKey(nextLane.getParentLink()))
                        {
                            double positionSI = frontPosSI - lane.getLength().getSI() - getFront().getDx().getSI();
                            this.fractionalLinkPositions.put(nextLane.getParentLink(), nextLane.fractionSI(positionSI));
                        }
                        lanesToCheck.add(nextLane);
                    }
                }
            }
        }

        // Execute all samplers
        for (Lane lane : this.lanes)
        {
            lane.sample(this);
        }

        // Schedule all sensor triggers that are going to happen until the next evaluation time.
        for (Lane lane : this.lanes)
        {
            double dt = this.nextEvaluationTime.getSI() - this.getLastEvaluationTime().getSI();
            double moveSI = this.speed.getSI() * dt + 0.5 * getAcceleration().getSI() * dt * dt;
            lane.scheduleTriggers(this, lane.positionSI(this.fractionalLinkPositions.get(lane.getParentLink())), moveSI);
        }
        getSimulator().scheduleEventAbs(cfmr.getValidUntil(), this, this, "move", null);
        // System.out.println("setState: " + cfmr + " " + this + " next evaluation is " + cfmr.getValidUntil());
    }

    /**
     * @throws RemoteException RemoteException
     * @throws NamingException on ???
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException on not being able to reschedule the move() method.
     */
    protected final void move() throws RemoteException, NamingException, NetworkException, SimRuntimeException
    {
        // Sanity check
        if (getSimulator().getSimulatorTime().get().getSI() != getNextEvaluationTime().getSI())
        {
            throw new Error("move called at wrong time: expected time " + getNextEvaluationTime()
                    + " simulator time is : " + getSimulator().getSimulatorTime().get());
        }
        // only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanes.isEmpty())
        {
            return;
        }
        DoubleScalar.Rel<LengthUnit> maximumForwardHeadway = new DoubleScalar.Rel<LengthUnit>(500.0, LengthUnit.METER);
        // TODO 500?
        DoubleScalar.Rel<LengthUnit> maximumReverseHeadway = new DoubleScalar.Rel<LengthUnit>(-200.0, LengthUnit.METER);
        // TODO 200?
        DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100.0, SpeedUnit.KM_PER_HOUR);
        // TODO should be the local speed limit
        if (null != this.laneChangeModel)
        {
            Collection<LaneBasedGTU<?>> sameLaneTraffic = new ArrayList<LaneBasedGTU<?>>();
            LaneBasedGTU<?> leader = headwayGTU(maximumForwardHeadway);
            if (null != leader)
            {
                sameLaneTraffic.add(leader);
            }
            LaneBasedGTU<?> follower = headwayGTU(maximumReverseHeadway);
            if (null != follower)
            {
                sameLaneTraffic.add(follower);
            }
            DoubleScalar.Abs<TimeUnit> now = getSimulator().getSimulatorTime().get();
            Collection<LaneBasedGTU<?>> leftLaneTraffic =
                    collectNeighborLaneTraffic(LateralDirectionality.LEFT, now, maximumForwardHeadway,
                            maximumReverseHeadway);
            Collection<LaneBasedGTU<?>> rightLaneTraffic =
                    collectNeighborLaneTraffic(LateralDirectionality.RIGHT, now, maximumForwardHeadway,
                            maximumReverseHeadway);
            LaneMovementStep lcmr =
                    this.laneChangeModel.computeLaneChangeAndAcceleration(this, sameLaneTraffic, rightLaneTraffic,
                            leftLaneTraffic, speedLimit, new DoubleScalar.Rel<AccelerationUnit>(0.3,
                                    AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(0.1,
                                    AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<AccelerationUnit>(-0.3,
                                    AccelerationUnit.METER_PER_SECOND_2));
            if (lcmr.getLaneChange() != null)
            {
                // TODO: make lane changes gradual (not instantaneous; like now)
                Collection<Lane> oldLaneSet = new ArrayList<Lane>(this.lanes);
                Collection<Lane> newLaneSet = adjacentLanes(lcmr.getLaneChange());
                // Remove this GTU from all of the Lanes that it is on and remember the fractional position on each one
                Map<Lane, Double> oldFractionalPositions = new HashMap<Lane, Double>();
                for (Lane l : this.lanes)
                {
                    /* !!! Must use the fractional positions at the last evaluation time !!! */
                    oldFractionalPositions.put(l, fractionalPosition(l, getReference(), getLastEvaluationTime()));
                    l.removeGTU(this);
                    // TODO: remove the triggers on Lane l
                }
                ArrayList<Lane> replacementLanes = new ArrayList<Lane>();
                // Add this GTU to the lanes in newLaneSet
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
                    replacementLanes.add(newLane);
                    addLane(newLane);
                }
                this.lanes.clear();
                this.lanes.addAll(replacementLanes);
            }
            // Move this GTU forward
            setState(lcmr.getGfmr());
            return;
        }
        Collection<LaneBasedGTU<?>> leaders = new ArrayList<>();
        leaders.add(headwayGTU(maximumForwardHeadway));
        // TODO calculate lowest speed limit
        AccelerationStep cfmr = getGTUFollowingModel().computeAcceleration(this, leaders, speedLimit);
        setState(cfmr);
    }

    /**
     * Collect relevant traffic in adjacent lanes.
     * @param directionality LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the (current) time
     * @param maximumForwardHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum forward search distance
     * @param maximumReverseHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum reverse search distance
     * @return Collection&lt;LaneBasedGTU&lt;?&gt;&gt;;
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     */
    private Collection<LaneBasedGTU<?>> collectNeighborLaneTraffic(final LateralDirectionality directionality,
            final DoubleScalar.Abs<TimeUnit> when, final DoubleScalar.Rel<LengthUnit> maximumForwardHeadway,
            final DoubleScalar.Rel<LengthUnit> maximumReverseHeadway) throws RemoteException, NetworkException
    {
        Collection<LaneBasedGTU<?>> result = parallel(directionality, when);
        for (Lane adjacentLane : adjacentLanes(directionality))
        {
            LaneBasedGTU<?> leader = headwayGTU(adjacentLane, maximumForwardHeadway);
            if (null != leader && !result.contains(leader))
            {
                result.add(leader);
            }
            LaneBasedGTU<?> follower = headwayGTU(adjacentLane, maximumReverseHeadway);
            if (null != follower && !result.contains(follower))
            {
                result.add(follower);
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
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = new HashMap<>();
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
            final RelativePosition relativePosition, final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
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
                    return new DoubleScalar.Rel<LengthUnit>(cseLane.getLength().getSI() * fractionalPosition,
                            LengthUnit.SI);
                }
            }
        }
        throw new NetworkException("GTU " + this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane lane, final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        if (null == lane)
        {
            throw new NetworkException("lane is null");
        }
        if (!this.lanes.contains(lane))
        {
            throw new NetworkException("GTU is not on lane " + lane.toString());
        }
        DoubleScalar.Rel<LengthUnit> longitudinalPosition =
                lane.position(this.fractionalLinkPositions.get(lane.getParentLink()));
        if (longitudinalPosition == null)
        {
            throw new NetworkException("GetPosition: GTU " + toString() + " not in lane " + lane);
        }
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        DoubleScalar.Rel<LengthUnit> loc =
                DoubleScalar.plus(
                        DoubleScalar.plus(
                                DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT))
                                        .immutable(),
                                Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable(),
                        relativePosition.getDx()).immutable();
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
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        Map<Lane, Double> positions = new HashMap<>();
        for (Lane lane : this.lanes)
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition,
            final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
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
    private GTUDistanceSI headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI,
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
                return new GTUDistanceSI(otherGTU, distanceM);
            }
            return new GTUDistanceSI(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = lane.nextLanes();
            if (nextLanes.size() > 0)
            {
                GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a Route
                    if (this.getRoute() == null || this.getRoute() != null
                            && this.getRoute().containsLink(lane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        GTUDistanceSI closest =
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
        return new GTUDistanceSI(null, Double.MAX_VALUE);
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
    private GTUDistanceSI headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
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
                return new GTUDistanceSI(otherGTU, distanceM);
            }
            return new GTUDistanceSI(null, Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            Set<Lane> prevLanes = lane.prevLanes();
            if (prevLanes.size() > 0)
            {
                GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
                for (Lane prevLane : prevLanes)
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    GTUDistanceSI closest =
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
        return new GTUDistanceSI(null, Double.MAX_VALUE);
    }

    /**
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU
     *         with a distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI
     *         meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private GTUDistanceSI headwayGTUSI(final double maxDistanceSI) throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        if (maxDistanceSI > 0.0)
        {
            // look forward.
            for (Lane lane : positions(getFront()).keySet())
            {
                GTUDistanceSI closest =
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
                GTUDistanceSI closest =
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
    public final DoubleScalar.Rel<LengthUnit> headway(final DoubleScalar.Rel<LengthUnit> maxDistance)
            throws RemoteException, NetworkException
    {
        return new DoubleScalar.Rel<LengthUnit>(headwayGTUSI(maxDistance.getSI()).getDistanceSI(), LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final Lane lane, final DoubleScalar.Rel<LengthUnit> maxDistance)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        if (maxDistance.getSI() > 0.0)
        {
            return new DoubleScalar.Rel<LengthUnit>(
                    headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(), when).getSI(), 0.0,
                            maxDistance.getSI(), when).getDistanceSI(), LengthUnit.METER);
        }
        else
        {
            return new DoubleScalar.Rel<LengthUnit>(
                    headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(), when).getSI(), 0.0,
                            -maxDistance.getSI(), when).getDistanceSI(), LengthUnit.METER);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final Lane lane, final DoubleScalar.Rel<LengthUnit> maxDistance)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        if (maxDistance.getSI() > 0.0)
        {
            return headwayRecursiveForwardSI(lane, this.projectedPosition(lane, this.getFront(), when).getSI(), 0.0,
                    maxDistance.getSI(), when).getOtherGTU();
        }
        else
        {
            return headwayRecursiveBackwardSI(lane, this.projectedPosition(lane, this.getRear(), when).getSI(), 0.0,
                    -maxDistance.getSI(), when).getOtherGTU();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
            NetworkException
    {
        return headwayGTUSI(maxDistance.getSI()).getOtherGTU();
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
                    if (this.getRoute() == null || this.getRoute() != null
                            && this.getRoute().containsLink(lane.getParentLink()))
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

        // Continue search on successor lanes.
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
                    double headwayPredecessor =
                            headwayRecursiveForwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
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
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
            final DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException, NetworkException
    {
        return headway(otherGTU, maxDistance, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
            final DoubleScalar.Rel<LengthUnit> maxDistance, final DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException
    {
        if (otherGTU == null)
        {
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }

        // search for the otherGTU on the current lanes we are registered on.
        if (maxDistance.getSI() > 0.0)
        {
            for (Lane lane : positions(getFront()).keySet())
            {
                // call an internal recursive method
                return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveForwardSI(lane,
                        this.position(lane, this.getFront(), when).getSI(), otherGTU, 0.0, maxDistance.getSI(), when),
                        LengthUnit.METER);
            }
            // other GTU not found within maxDistance
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }
        else
        {
            for (Lane lane : positions(getRear()).keySet())
            {
                // call an internal recursive method
                return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveBackwardSI(lane,
                        this.position(lane, this.getRear(), when).getSI(), otherGTU, 0.0, maxDistance.getSI(), when),
                        LengthUnit.METER);
            }
            // other GTU not found within maxDistance
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU<?>> parallel(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException
    {
        Set<LaneBasedGTU<?>> gtuSet = new HashSet<LaneBasedGTU<?>>();
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
        Set<Lane> result = new HashSet<Lane>();
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
        /*-                       new HashSet<Lane>();
        for (Lane lane : this.lanes)
        {
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
        }
         */
        Set<LaneBasedGTU<?>> gtuSet = new HashSet<LaneBasedGTU<?>>();
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
        try
        {
            if (this.lanes.size() == 0)
            {
                System.out.println("GTU " + this.getId() + " is not on any lane");
                getSimulator().stop();
            }
            Lane lane = this.lanes.get(0);
            // TODO solve problem when point is still on previous lane.
            double fraction =
                    (position(lane, getFront()).getSI() - getLength().getSI() / 2.0) / lane.getLength().getSI();
            LineString line = lane.getCenterLine();
            LengthIndexedLine lil = new LengthIndexedLine(line);
            Coordinate c = lil.extractPoint(fraction * line.getLength());
            // HACK (FIXME)
            c.z = 0d;
            Coordinate ca =
                    fraction <= 0.01 ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
            Coordinate cb =
                    fraction >= 0.99 ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
            double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
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

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * Description of Car at specified time.
     * @param lane the position on this lane will be returned.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
    {
        double pos = Double.NaN;
        try
        {
            pos = this.position(lane, getFront(), when).getSI();
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s^2", getId(),
                this.lastEvaluationTime.getSI(), getNextEvaluationTime().getSI(), pos,
                this.getLongitudinalVelocity(when).getSI(), this.getAcceleration(when).getSI());
    }

    /**
     * Helper class to store another GTU and the distance of this GTU to the other GTU.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 21, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    private class GTUDistanceSI
    {
        /** the other GTU. */
        private final LaneBasedGTU<?> otherGTU;

        /** the distance to the GTU in meters. */
        private final double distanceSI;

        /**
         * @param otherGTU the other GTU
         * @param distanceSI the distance to the other GTU in meters
         */
        public GTUDistanceSI(final LaneBasedGTU<?> otherGTU, final double distanceSI)
        {
            super();
            this.otherGTU = otherGTU;
            this.distanceSI = distanceSI;
        }

        /**
         * @return the other GTU.
         */
        public final LaneBasedGTU<?> getOtherGTU()
        {
            return this.otherGTU;
        }

        /**
         * @return distanceSI the distance to the other GTU in meters.
         */
        public final double getDistanceSI()
        {
            return this.distanceSI;
        }
    }

}
