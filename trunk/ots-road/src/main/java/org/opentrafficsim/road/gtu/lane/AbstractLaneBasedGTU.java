package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedHashMap;
import org.opentrafficsim.core.perception.collections.HistoricalMap;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1408 $, $LastChangedDate: 2015-09-24 15:17:25 +0200 (Thu, 24 Sep 2015) $, by $Author: pknoppers $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedGTU extends AbstractGTU implements LaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Collision detector. */
    private final CollisionDetector collisionDetector;

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the start of the current
     * operational plan. Because the reference point of the GTU might not be on all the links the GTU is registered on, the
     * fractional longitudinal positions can be more than one, or less than zero.
     */
    private HistoricalMap<Link, Double> fractionalLinkPositions;

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as well to
     * keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the vehicle is
     * registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is registered are at the
     * front of the list, the later ones more to the back.
     */
    private final HistoricalMap<Lane, GTUDirectionality> currentLanes;

    /** Maps that we enter when initiating a lane change, but we may not actually enter given a deviative plan. */
    private final Set<Lane> enteredLanes = new LinkedHashSet<>();

    /** Pending leave triggers for each lane. */
    private Map<Lane, List<SimEventInterface<SimTimeDoubleUnit>>> pendingLeaveTriggers = new LinkedHashMap<>();

    /** Pending enter triggers for each lane. */
    private Map<Lane, List<SimEventInterface<SimTimeDoubleUnit>>> pendingEnterTriggers = new LinkedHashMap<>();

    /** Event to finalize lane change. */
    private SimEventInterface<SimTimeDoubleUnit> finalizeLaneChangeEvent = null;

    /** Cached desired speed. */
    private Speed cachedDesiredSpeed;

    /** Time desired speed was cached. */
    private Time desiredSpeedTime;

    /** Cached car-following acceleration. */
    private Acceleration cachedCarFollowingAcceleration;

    /** Time car-following acceleration was cached. */
    private Time carFollowingAccelerationTime;

    /** The object to lock to make the GTU thread safe. */
    private Object lock = new Object();

    /** The threshold distance for differences between initial locations of the GTU on different lanes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public static Length initialLocationThresholdDifference = new Length(1.0, LengthUnit.MILLIMETER);

    /** Turn indicator status. */
    private final Historical<TurnIndicatorStatus> turnIndicatorStatus;

    /** Caching on or off. */
    // TODO: should be indicated with a Parameter
    public static boolean CACHING = true;

    /** cached position count. */
    // TODO: can be removed after testing period
    public static int CACHED_POSITION = 0;

    /** cached position count. */
    // TODO: can be removed after testing period
    public static int NON_CACHED_POSITION = 0;

    /** Vehicle model. */
    private VehicleModel vehicleModel = VehicleModel.MINMAX;

    /**
     * Construct a Lane Based GTU.
     * @param id String; the id of the GTU
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator OTSSimulatorInterface; to initialize the move method and to get the current time
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GTUException when initial values are not correct
     */
    public AbstractLaneBasedGTU(final String id, final GTUType gtuType, final OTSSimulatorInterface simulator,
            final OTSRoadNetwork network) throws GTUException
    {
        super(id, gtuType, simulator, network);
        HistoryManager historyManager = simulator.getReplication().getHistoryManager(simulator);
        this.fractionalLinkPositions = new HistoricalLinkedHashMap<>(historyManager);
        this.currentLanes = new HistoricalLinkedHashMap<>(historyManager);
        this.turnIndicatorStatus = new HistoricalValue<>(historyManager, TurnIndicatorStatus.NOTPRESENT);
        this.collisionDetector = new CollisionDetector(id);
    }

    /**
     * @param strategicalPlanner LaneBasedStrategicalPlanner; the strategical planner (e.g., route determination) to use
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the initial positions of the car on one or more
     *            lanes with their directions
     * @param initialSpeed Speed; the initial speed of the car on the lane
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when initial values are not correct
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void init(final LaneBasedStrategicalPlanner strategicalPlanner,
            final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed)
            throws NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        Throw.when(null == initialLongitudinalPositions, GTUException.class, "InitialLongitudinalPositions is null");
        Throw.when(0 == initialLongitudinalPositions.size(), GTUException.class, "InitialLongitudinalPositions is empty set");

        DirectedPoint lastPoint = null;
        for (DirectedLanePosition pos : initialLongitudinalPositions)
        {
            // Throw.when(lastPoint != null && pos.getLocation().distance(lastPoint) > initialLocationThresholdDifference.si,
            // GTUException.class, "initial locations for GTU have distance > " + initialLocationThresholdDifference);
            lastPoint = pos.getLocation();
        }
        DirectedPoint initialLocation = lastPoint;

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move and events will work
        Time now = getSimulator().getSimulatorTime();
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan
                        .set(new OperationalPlan(this, initialLocation, now, new Duration(1E-6, DurationUnit.SECOND)));
            }
            else
            {
                OTSPoint3D p2 = new OTSPoint3D(initialLocation.x + 1E-6 * Math.cos(initialLocation.getRotZ()),
                        initialLocation.y + 1E-6 * Math.sin(initialLocation.getRotZ()), initialLocation.z);
                OTSLine3D path = new OTSLine3D(new OTSPoint3D(initialLocation), p2);
                this.operationalPlan.set(OperationalPlanBuilder.buildConstantSpeedPlan(this, path, now, initialSpeed));
            }
        }
        catch (OperationalPlanException e)
        {
            throw new RuntimeException("Initial operational plan could not be created.", e);
        }

        // register the GTU on the lanes
        for (DirectedLanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            addLaneToGtu(lane, directedLanePosition.getPosition(), directedLanePosition.getGtuDirection()); // enter lane part 1
        }

        // init event
        DirectedLanePosition referencePosition = getReferencePosition();
        fireTimedEvent(LaneBasedGTU.LANEBASED_INIT_EVENT,
                new Object[] { getId(), initialLocation, getLength(), getWidth(), referencePosition.getLane(),
                        referencePosition.getPosition(), referencePosition.getGtuDirection(), getGTUType() },
                getSimulator().getSimulatorTime());

        // register the GTU on the lanes
        for (DirectedLanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            lane.addGTU(this, directedLanePosition.getPosition()); // enter lane part 2
        }

        // initiate the actual move
        super.init(strategicalPlanner, initialLocation, initialSpeed);

        this.referencePositionTime = Double.NaN; // remove cache, it may be invalid as the above init results in a lane change

    }

    /**
     * {@inheritDoc} All lanes the GTU is on will be left.
     */
    @Override
    public void setParent(final GTU gtu) throws GTUException
    {
        for (Lane lane : new LinkedHashSet<>(this.currentLanes.keySet())) // copy for concurrency problems
        {
            leaveLane(lane);
        }
        super.setParent(gtu);
    }

    /**
     * Reinitializes the GTU on the network using the existing strategical planner and zero speed.
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; initial position
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when initial values are not correct
     * @throws OTSGeometryException when the initial path is wrong
     */
    public void reinit(final Set<DirectedLanePosition> initialLongitudinalPositions)
            throws NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        init(getStrategicalPlanner(), initialLongitudinalPositions, Speed.ZERO);
    }

    /**
     * Hack method. TODO remove and solve better
     * @return safe to change
     * @throws GTUException on error
     */
    public final boolean isSafeToChange() throws GTUException
    {
        return this.fractionalLinkPositions.get(getReferencePosition().getLane().getParentLink()) > 0.0;
    }

    /**
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the GTU.
     * The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and rear positions
     * are relative towards this position.
     * @param lane Lane; the lane to add to the list of lanes on which the GTU is registered.
     * @param gtuDirection GTUDirectionality; the direction of the GTU on the lane (which can be bidirectional). If the GTU has
     *            a positive speed, it is moving in this direction.
     * @param position Length; the position on the lane.
     * @throws GTUException when positioning the GTU on the lane causes a problem
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void enterLane(final Lane lane, final Length position, final GTUDirectionality gtuDirection) throws GTUException
    {
        if (lane == null || gtuDirection == null || position == null)
        {
            throw new GTUException("enterLane - one of the arguments is null");
        }
        addLaneToGtu(lane, position, gtuDirection);
        addGtuToLane(lane, position);
    }

    /**
     * Registers the lane at the GTU. Only works at the start of a operational plan.
     * @param lane Lane; the lane to add to the list of lanes on which the GTU is registered.
     * @param gtuDirection GTUDirectionality; the direction of the GTU on the lane (which can be bidirectional). If the GTU has
     *            a positive speed, it is moving in this direction.
     * @param position Length; the position on the lane.
     * @throws GTUException when positioning the GTU on the lane causes a problem
     */
    private void addLaneToGtu(final Lane lane, final Length position, final GTUDirectionality gtuDirection) throws GTUException
    {
        if (this.currentLanes.containsKey(lane))
        {
            System.err.println(this + " is already registered on lane: " + lane + " at fractional position "
                    + this.fractionalPosition(lane, RelativePosition.REFERENCE_POSITION) + " intended position is " + position
                    + " length of lane is " + lane.getLength());
            return;
        }
        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as
        // this might lead to a "jump".
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
        {
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position));
        }
        this.currentLanes.put(lane, gtuDirection);
    }

    /**
     * Part of 'enterLane' which registers the GTU with the lane so the lane can report its GTUs.
     * @param lane Lane; lane
     * @param position Length; position
     * @throws GTUException on exception
     */
    protected void addGtuToLane(final Lane lane, final Length position) throws GTUException
    {
        List<SimEventInterface<SimTimeDoubleUnit>> pending = this.pendingEnterTriggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<SimTimeDoubleUnit> event : pending)
            {
                if (event.getAbsoluteExecutionTime().get().ge(getSimulator().getSimulatorTime()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().get().ne(getSimulator().getSimulatorTime()))
                    {
                        System.err.println("addLaneToGtu, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime() + ", eventTime="
                                + event.getAbsoluteExecutionTime().get());
                    }
                }
            }
            this.pendingEnterTriggers.remove(lane);
        }
        lane.addGTU(this, position);
    }

    /**
     * Unregister the GTU from a lane.
     * @param lane Lane; the lane to remove from the list of lanes on which the GTU is registered.
     * @throws GTUException when leaveLane should not be called
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane) throws GTUException
    {
        leaveLane(lane, false);
    }

    /**
     * Leave a lane but do not complain about having no lanes left when beingDestroyed is true.
     * @param lane Lane; the lane to leave
     * @param beingDestroyed boolean; if true, no complaints about having no lanes left
     * @throws GTUException in case leaveLane should not be called
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane, final boolean beingDestroyed) throws GTUException
    {
        Length position = position(lane, getReference());
        this.currentLanes.remove(lane);
        removePendingEvents(lane, this.pendingLeaveTriggers);
        removePendingEvents(lane, this.pendingEnterTriggers);
        // check if there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.currentLanes.keySet())
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
        lane.removeGTU(this, !found, position);
        if (this.currentLanes.size() == 0 && !beingDestroyed)
        {
            System.err.println("leaveLane: lanes.size() = 0 for GTU " + getId());
        }
    }

    /**
     * Removes and cancels events for the given lane.
     * @param lane Lane; lane
     * @param triggers Map&lt;Lane, List&lt;SimEventInterface&lt;SimTimeDoubleUnit&gt;&gt;&gt;; map to use
     */
    private void removePendingEvents(final Lane lane, final Map<Lane, List<SimEventInterface<SimTimeDoubleUnit>>> triggers)
    {
        List<SimEventInterface<SimTimeDoubleUnit>> pending = triggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<SimTimeDoubleUnit> event : pending)
            {
                if (event.getAbsoluteExecutionTime().get().ge(getSimulator().getSimulatorTime()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().get().ne(getSimulator().getSimulatorTime()))
                    {
                        System.err.println("leaveLane, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime() + ", eventTime="
                                + event.getAbsoluteExecutionTime().get());
                    }
                }
            }
            triggers.remove(lane);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection) throws GTUException
    {

        // from info
        DirectedLanePosition from = getReferencePosition();

        // keep a copy of the lanes and directions (!)
        Set<Lane> lanesToBeRemoved = new LinkedHashSet<>(this.currentLanes.keySet());

        // store the new positions
        // start with current link position, these will be overwritten, except if from a lane no adjacent lane is found, i.e.
        // changing over a continuous line when probably the reference point is past the line
        Map<Link, Double> newLinkPositionsLC = new LinkedHashMap<>(this.fractionalLinkPositions);

        // obtain position on lane adjacent to reference lane and enter lanes upstream/downstream from there
        Set<Lane> adjLanes = from.getLane().accessibleAdjacentLanesPhysical(laneChangeDirection, getGTUType(),
                this.currentLanes.get(from.getLane()));
        Lane adjLane = adjLanes.iterator().next();
        Length position = adjLane.position(from.getLane().fraction(from.getPosition()));
        GTUDirectionality direction = getDirection(from.getLane());
        Length planLength = Try.assign(() -> getOperationalPlan().getTraveledDistance(getSimulator().getSimulatorTime()),
                "Exception while determining plan length.");
        enterLaneRecursive(new LaneDirection(adjLane, direction), position, newLinkPositionsLC, planLength, lanesToBeRemoved,
                0);

        // update the positions on the lanes we are registered on
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkPositionsLC);

        // leave the from lanes
        for (Lane lane : lanesToBeRemoved)
        {
            leaveLane(lane);
        }

        // stored positions no longer valid
        this.referencePositionTime = Double.NaN;
        this.cachedPositions.clear();

        // fire event
        this.fireTimedEvent(LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] { getId(), laneChangeDirection, from },
                getSimulator().getSimulatorTime());

    }

    /**
     * Enters lanes upstream and downstream of the new location after an instantaneous lane change.
     * @param lane LaneDirection; considered lane
     * @param position Length; position to add GTU at
     * @param newLinkPositionsLC Map&lt;Link, Double&gt;; new link fractions to store
     * @param planLength Length; length of plan, to consider fractions at start
     * @param lanesToBeRemoved Set&lt;Lane&gt;; lanes to leave, from which lanes are removed when entered (such that they arent
     *            then left)
     * @param dir int; below 0 for upstream, above 0 for downstream, 0 for both
     * @throws GTUException on exception
     */
    private void enterLaneRecursive(final LaneDirection lane, final Length position, final Map<Link, Double> newLinkPositionsLC,
            final Length planLength, final Set<Lane> lanesToBeRemoved, final int dir) throws GTUException
    {
        enterLane(lane.getLane(), position, lane.getDirection());
        lanesToBeRemoved.remove(lane);
        Length adjusted = lane.getDirection().isPlus() ? position.minus(planLength) : position.plus(planLength);
        newLinkPositionsLC.put(lane.getLane().getParentLink(), adjusted.si / lane.getLength().si);

        // upstream
        if (dir < 1)
        {
            Length rear = lane.getDirection().isPlus() ? position.plus(getRear().getDx()) : position.minus(getRear().getDx());
            Length before = null;
            if (lane.getDirection().isPlus() && rear.si < 0.0)
            {
                before = rear.neg();
            }
            else if (lane.getDirection().isMinus() && rear.si > lane.getLength().si)
            {
                before = rear.minus(lane.getLength());
            }
            if (before != null)
            {
                GTUDirectionality upDir = lane.getDirection();
                ImmutableMap<Lane, GTUDirectionality> upstream = lane.getLane().upstreamLanes(upDir, getGTUType());
                if (!upstream.isEmpty())
                {
                    Lane upLane = null;
                    for (Lane nextUp : upstream.keySet())
                    {
                        if (newLinkPositionsLC.containsKey(nextUp.getParentLink()))
                        {
                            // multiple upstream lanes could belong to the same link, we pick an arbitrary lane
                            // (a conflict should solve this)
                            upLane = nextUp;
                            break;
                        }
                    }
                    if (upLane == null)
                    {
                        // the rear is on an upstream section we weren't before the lane change, due to curvature, we pick an
                        // arbitrary lane (a conflict should solve this)
                        upLane = upstream.keySet().iterator().next();
                    }
                    if (!this.currentLanes.containsKey(upLane))
                    {
                        upDir = upstream.get(upLane);
                        LaneDirection next = new LaneDirection(upLane, upDir);
                        Length nextPos = upDir.isPlus() ? next.getLength().minus(before).minus(getRear().getDx())
                                : before.plus(getRear().getDx());
                        enterLaneRecursive(next, nextPos, newLinkPositionsLC, planLength, lanesToBeRemoved, -1);
                    }
                }
            }
        }

        // downstream
        if (dir > -1)
        {
            Length front =
                    lane.getDirection().isPlus() ? position.plus(getFront().getDx()) : position.minus(getFront().getDx());
            Length passed = null;
            if (lane.getDirection().isPlus() && front.si > lane.getLength().si)
            {
                passed = front.minus(lane.getLength());
            }
            else if (lane.getDirection().isMinus() && front.si < 0.0)
            {
                passed = front.neg();
            }
            if (passed != null)
            {
                LaneDirection next = lane.getNextLaneDirection(this);
                if (!this.currentLanes.containsKey(next.getLane()))
                {
                    Length nextPos = next.getDirection().isPlus() ? passed.minus(getFront().getDx())
                            : next.getLength().minus(passed).plus(getFront().getDx());
                    enterLaneRecursive(next, nextPos, newLinkPositionsLC, planLength, lanesToBeRemoved, 1);
                }
            }
        }
    }

    /**
     * Register on lanes in target lane.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     * @throws GTUException exception
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void initLaneChange(final LateralDirectionality laneChangeDirection) throws GTUException
    {
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.currentLanes);
        Map<Lane, Double> fractionalLanePositions = new LinkedHashMap<>();
        for (Lane lane : lanesCopy.keySet())
        {
            fractionalLanePositions.put(lane, fractionalPosition(lane, getReference()));
        }
        int numRegistered = 0;
        for (Lane lane : lanesCopy.keySet())
        {
            Set<Lane> laneSet = lane.accessibleAdjacentLanesLegal(laneChangeDirection, getGTUType(), getDirection(lane));
            if (laneSet.size() > 0)
            {
                numRegistered++;
                Lane adjacentLane = laneSet.iterator().next();
                Length position = adjacentLane.getLength().multiplyBy(fractionalLanePositions.get(lane));
                if (lanesCopy.get(lane).isPlus() ? position.lt(lane.getLength().minus(getRear().getDx()))
                        : position.gt(getFront().getDx().neg()))
                {
                    this.enteredLanes.add(adjacentLane);
                    enterLane(adjacentLane, position, lanesCopy.get(lane));
                }
                else
                {
                    System.out.println("Skipping enterLane for GTU " + getId() + " on lane " + lane.getFullId() + " at "
                            + position + ", lane length = " + lane.getLength() + " rear = " + getRear().getDx() + " front = "
                            + getFront().getDx());
                }
            }
        }
        Throw.when(numRegistered == 0, GTUException.class, "Gtu %s starting %s lane change, but no adjacent lane found.",
                getId(), laneChangeDirection);
    }

    /**
     * Performs the finalization of a lane change by leaving the from lanes.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void finalizeLaneChange(final LateralDirectionality laneChangeDirection)
    {
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.currentLanes);
        Set<Lane> lanesToBeRemoved = new LinkedHashSet<>();
        Lane fromLane = null;
        Length fromPosition = null;
        GTUDirectionality fromDirection = null;
        try
        {
            // find lanes to leave as they have an adjacent lane the GTU is also on in the lane change direction
            for (Lane lane : lanesCopy.keySet())
            {
                Iterator<Lane> iterator =
                        lane.accessibleAdjacentLanesPhysical(laneChangeDirection, getGTUType(), getDirection(lane)).iterator();
                if (iterator.hasNext() && lanesCopy.keySet().contains(iterator.next()))
                {
                    lanesToBeRemoved.add(lane);
                }
            }
            // some lanes registered to the GTU may be downstream of a split and have no adjacent lane, find longitudinally
            boolean added = true;
            while (added)
            {
                added = false;
                Set<Lane> lanesToAlsoBeRemoved = new LinkedHashSet<>();
                for (Lane lane : lanesToBeRemoved)
                {
                    GTUDirectionality direction = getDirection(lane);
                    for (Lane nextLane : direction.isPlus() ? lane.nextLanes(getGTUType()).keySet()
                            : lane.prevLanes(getGTUType()).keySet())
                    {
                        if (lanesCopy.containsKey(nextLane) && !lanesToBeRemoved.contains(nextLane))
                        {
                            added = true;
                            lanesToAlsoBeRemoved.add(nextLane);
                        }
                    }
                }
                lanesToBeRemoved.addAll(lanesToAlsoBeRemoved);
            }
            double nearest = Double.POSITIVE_INFINITY;
            for (Lane lane : lanesToBeRemoved)
            {
                Length pos = position(lane, RelativePosition.REFERENCE_POSITION);
                if (0.0 <= pos.si && pos.si <= lane.getLength().si)
                {
                    fromLane = lane;
                    fromPosition = pos;
                    fromDirection = getDirection(lane);
                }
                else if (fromLane == null && (getDirection(lane).isPlus() ? pos.si > lane.getLength().si : pos.le0()))
                {
                    // if the reference point is in between two lanes, this recognizes the lane upstream of the gap
                    double distance = getDirection(lane).isPlus() ? pos.si - lane.getLength().si : -pos.si;
                    if (distance < nearest)
                    {
                        nearest = distance;
                        fromLane = lane;
                        fromPosition = pos;
                        fromDirection = getDirection(lane);
                    }
                }
                leaveLane(lane);
            }
            this.referencePositionTime = Double.NaN;
            this.finalizeLaneChangeEvent = null;
        }
        catch (GTUException exception)
        {
            // should not happen, lane was obtained from GTU
            throw new RuntimeException("position on lane not possible", exception);
        }
        Throw.when(fromLane == null, RuntimeException.class, "No from lane for lane change event.");
        DirectedLanePosition from;
        try
        {
            from = new DirectedLanePosition(fromLane, fromPosition, fromDirection);
        }
        catch (GTUException exception)
        {
            throw new RuntimeException(exception);
        }
        this.fireTimedEvent(LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] { getId(), laneChangeDirection, from },
                getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public void setFinalizeLaneChangeEvent(final SimEventInterface<SimTimeDoubleUnit> event)
    {
        this.finalizeLaneChangeEvent = event;
    }

    /** {@inheritDoc} */
    @Override
    public final GTUDirectionality getDirection(final Lane lane) throws GTUException
    {
        Throw.when(!this.currentLanes.containsKey(lane), GTUException.class, "getDirection: Lanes %s does not contain %s",
                this.currentLanes.keySet(), lane);
        return this.currentLanes.get(lane);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void move(final DirectedPoint fromLocation)
            throws SimRuntimeException, GTUException, OperationalPlanException, NetworkException, ParameterException
    {
        // DirectedPoint currentPoint = getLocation(); // used for "jump" detection that is also commented out
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.currentLanes.isEmpty())
        {
            destroy();
            return; // Done; do not re-schedule execution of this move method.
        }

        // remove enter events
        // WS: why?
        // for (Lane lane : this.pendingEnterTriggers.keySet())
        // {
        // System.out.println("GTU " + getId() + " is canceling event on lane " + lane.getFullId());
        // List<SimEventInterface<SimTimeDoubleUnit>> events = this.pendingEnterTriggers.get(lane);
        // for (SimEventInterface<SimTimeDoubleUnit> event : events)
        // {
        // // also unregister from lane
        // this.currentLanes.remove(lane);
        // getSimulator().cancelEvent(event);
        // }
        // }
        // this.pendingEnterTriggers.clear();

        // get distance covered in previous plan, to aid a shift in link fraction (from which a plan moves onwards)
        Length covered;
        if (getOperationalPlan() instanceof LaneBasedOperationalPlan
                && ((LaneBasedOperationalPlan) getOperationalPlan()).isDeviative())
        {
            // traveled distance as difference between start and current position on reference lane
            // note that for a deviative plan the traveled distance along the path is not valuable here
            LaneBasedOperationalPlan plan = (LaneBasedOperationalPlan) getOperationalPlan();
            DirectedLanePosition ref = getReferencePosition();
            covered = ref.getGtuDirection().isPlus()
                    ? position(ref.getLane(), getReference())
                            .minus(position(ref.getLane(), getReference(), plan.getStartTime()))
                    : position(ref.getLane(), getReference(), plan.getStartTime())
                            .minus(position(ref.getLane(), getReference()));
            // Note that distance is valid as the reference lane can not change (and location of previous plan is start location
            // of current plan). Only instantaneous lane changes can do that, which do not result in deviative plans.
        }
        else
        {
            covered = getOperationalPlan().getTraveledDistance(getSimulator().getSimulatorTime());
        }

        // generate the next operational plan and carry it out
        // in case of an instantaneous lane change, fractionalLinkPositions will be accordingly adjusted to the new lane
        super.move(fromLocation);

        // update the positions on the lanes we are registered on
        // WS: this was previously done using fractions calculated before super.move() based on the GTU position, but an
        // instantaneous lane change while e.g. the nose is on the next lane which is curved, results in a different fraction on
        // the next link (the GTU doesn't stretch or shrink)
        Map<Link, Double> newLinkFractions = new LinkedHashMap<>(this.fractionalLinkPositions);
        Set<Link> done = new LinkedHashSet<>();
        // WS: this used to be on all current lanes, skipping links already processed, but 'covered' regards the reference lane
        updateLinkFraction(getReferencePosition().getLane(), newLinkFractions, done, false, covered, true);
        updateLinkFraction(getReferencePosition().getLane(), newLinkFractions, done, true, covered, true);
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkFractions);

        DirectedLanePosition dlp = getReferencePosition();
        fireTimedEvent(
                LaneBasedGTU.LANEBASED_MOVE_EVENT, new Object[] { getId(), fromLocation, getSpeed(), getAcceleration(),
                        getTurnIndicatorStatus(), getOdometer(), dlp.getLane(), dlp.getPosition(), dlp.getGtuDirection() },
                getSimulator().getSimulatorTime());

        if (getOperationalPlan().getAcceleration(Duration.ZERO).si < -10
                && getOperationalPlan().getSpeed(Duration.ZERO).si > 2.5)
        {
            System.err.println("GTU: " + getId() + " - getOperationalPlan().getAcceleration(Duration.ZERO).si < -10)");
            System.err.println("Lanes in current plan: " + this.currentLanes.keySet());
            if (getTacticalPlanner().getPerception().contains(DefaultSimplePerception.class))
            {
                DefaultSimplePerception p =
                        getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class);
                System.err.println("HeadwayGTU: " + p.getForwardHeadwayGTU());
                System.err.println("HeadwayObject: " + p.getForwardHeadwayObject());
            }
        }
        // DirectedPoint currentPointAfterMove = getLocation();
        // if (currentPoint.distance(currentPointAfterMove) > 0.1)
        // {
        // System.err.println(this.getId() + " jumped");
        // }
        // schedule triggers and determine when to enter lanes with front and leave lanes with rear
        scheduleEnterLeaveTriggers();
    }

    /**
     * Recursive update of link fractions based on a moved distance.
     * @param lane Lane; current lane, start with reference lane
     * @param newLinkFractions Map&lt;Link, Double&gt;; map to put new fractions in
     * @param done Set&lt;Link&gt;; links to skip as link are already done
     * @param prevs boolean; whether to loop to the previous or next lanes, regardless of driving direction
     * @param covered Length; covered distance along the reference lane
     * @param isReferenceLane boolean; whether this lane is the reference lane (to skip in second call)
     */
    private void updateLinkFraction(final Lane lane, final Map<Link, Double> newLinkFractions, final Set<Link> done,
            final boolean prevs, final Length covered, final boolean isReferenceLane)
    {
        if (!prevs || !isReferenceLane)
        {
            if (done.contains(lane.getParentLink()) || !this.currentLanes.containsKey(lane))
            {
                return;
            }
            double sign;
            try
            {
                sign = getDirection(lane).isPlus() ? 1.0 : -1.0;
            }
            catch (GTUException exception)
            {
                // can not happen as we check that the lane is in the currentLanes
                throw new RuntimeException("Unexpected exception: trying to obtain direction on lane.", exception);
            }
            newLinkFractions.put(lane.getParentLink(),
                    this.fractionalLinkPositions.get(lane.getParentLink()) + sign * covered.si / lane.getLength().si);
            done.add(lane.getParentLink());
        }
        for (Lane nextLane : (prevs ? lane.prevLanes(getGTUType()) : lane.nextLanes(getGTUType())).keySet())
        {
            updateLinkFraction(nextLane, newLinkFractions, done, prevs, covered, false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GTUException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GTUException
    {
        Map<Lane, Length> positions = new LinkedHashMap<>();
        for (Lane lane : this.currentLanes.keySet())
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime());
    }

    /**
     * Return the longitudinal position that the indicated relative position of this GTU would have if it were to change to
     * another Lane with a / the current CrossSectionLink. This point may be before the begin or after the end of the link of
     * the projection lane of the GTU. This preserves the length of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GTUException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Length translatedPosition(final Lane projectionLane, final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (null != this.currentLanes.get(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, RelativePosition.REFERENCE_POSITION, when);
                    Length pos = new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                    if (this.currentLanes.get(cseLane).isPlus())
                    {
                        return pos.plus(relativePosition.getDx());
                    }
                    return pos.minus(relativePosition.getDx());
                }
            }
        }
        throw new GTUException(this + " is not on any lane of Link " + link);
    }

    /**
     * Return the longitudinal position on the projection lane that has the same fractional position on one of the current lanes
     * of the indicated relative position. This preserves the fractional positions of all relative positions of the GTU.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when Time; the time for which to project the position of this GTU
     * @return Length; the position of this GTU in the projectionLane
     * @throws GTUException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Length projectedPosition(final Lane projectionLane, final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (null != this.currentLanes.get(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                }
            }
        }
        throw new GTUException(this + " is not on any lane of Link " + link);
    }

    /** caching of time field for last stored position(s). */
    private double cachePositionsTime = Double.NaN;

    /** caching of last stored position(s). */
    private Map<Integer, Length> cachedPositions = new LinkedHashMap<>();

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Length position(final Lane lane, final RelativePosition relativePosition, final Time when) throws GTUException
    {
        int cacheIndex = 0;
        if (CACHING)
        {
            cacheIndex = 17 * lane.hashCode() + relativePosition.hashCode();
            Length l;
            if (when.si == this.cachePositionsTime && (l = this.cachedPositions.get(cacheIndex)) != null)
            {
                // PK verify the result; uncomment if you don't trust the cache
                // this.cachedPositions.clear();
                // Length difficultWay = position(lane, relativePosition, when);
                // if (Math.abs(l.si - difficultWay.si) > 0.00001)
                // {
                // System.err.println("Whoops: cache returns bad value for GTU " + getId());
                // }
                CACHED_POSITION++;
                return l;
            }
            if (when.si != this.cachePositionsTime)
            {
                this.cachedPositions.clear();
                this.cachePositionsTime = when.si;
            }
        }
        NON_CACHED_POSITION++;

        synchronized (this.lock)
        {
            double loc = Double.NaN;
            try
            {
                OperationalPlan plan = getOperationalPlan(when);
                if (!(plan instanceof LaneBasedOperationalPlan) || !((LaneBasedOperationalPlan) plan).isDeviative())
                {
                    double longitudinalPosition;
                    try
                    {
                        longitudinalPosition = lane.positionSI(this.fractionalLinkPositions.get(when).get(lane.getParentLink()));
                    }
                    catch (NullPointerException exception)
                    {
                        throw exception;
                    }
                    if (this.currentLanes.get(when).get(lane).isPlus())
                    {
                        loc = longitudinalPosition + plan.getTraveledDistanceSI(when) + relativePosition.getDx().si;
                    }
                    else
                    {
                        loc = longitudinalPosition - plan.getTraveledDistanceSI(when) - relativePosition.getDx().si;
                    }
                }
                else
                {
                    // deviative LaneBasedOperationalPlan, i.e. the GTU is not on a center line
                    DirectedPoint p = plan.getLocation(when, relativePosition);
                    double f = lane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.NaN);
                    if (!Double.isNaN(f))
                    {
                        loc = f * lane.getLength().si;
                    }
                    else
                    {
                        // the point does not project fractionally to this lane, it has to be up- or downstream of the lane

                        // simple heuristic to decide if we first look upstream or downstream
                        boolean upstream = this.fractionalLinkPositions.get(lane.getParentLink()) < 0.0 ? true : false;

                        // use loop up to 2 times (for loop creates 'loc not initialized' warning)
                        int i = 0;
                        while (true)
                        {
                            Set<Lane> otherLanesToConsider = new LinkedHashSet<>();
                            otherLanesToConsider.addAll(this.currentLanes.keySet());
                            double distance = getDistanceAtOtherLane(lane, when, upstream, 0.0, p, otherLanesToConsider);
                            // distance can be positive on an upstream lane due to a loop
                            if (!Double.isNaN(distance))
                            {
                                if (i == 1 && !Double.isNaN(loc))
                                {
                                    // loc was determined in both loops, this constitutes a lane-loop, select nearest
                                    double loc2 = upstream ? -distance : distance + lane.getLength().si;
                                    double d1 = loc < 0.0 ? -loc : loc - lane.getLength().si;
                                    double d2 = loc2 < 0.0 ? -loc2 : loc2 - lane.getLength().si;
                                    loc = d1 < d2 ? loc : loc2;
                                    break;
                                }
                                else
                                {
                                    // loc was determined in second loop
                                    loc = upstream ? -distance : distance + lane.getLength().si;
                                }
                            }
                            else if (!Double.isNaN(loc))
                            {
                                // loc was determined in first loop
                                break;
                            }
                            else if (i == 1)
                            {
                                // loc was determined in neither loop
                                // Lane change ended while moving to next link. The source lanes are left and for a leave-lane
                                // event the position is required. This may depend on upstream or downstream lanes as the
                                // reference position is projected to that lane. But if we already left that lane, we can't use
                                // it. We thus use ENDPOINT fallback instead.
                                loc = lane.getLength().si * lane.getCenterLine().projectFractional(null, null, p.x, p.y,
                                        FractionalFallback.ENDPOINT);
                                break;
                            }
                            // try other direction
                            i++;
                            upstream = !upstream;
                        }
                    }
                }
            }
            catch (NullPointerException e)
            {
                throw new GTUException("lanesCurrentOperationalPlan or fractionalLinkPositions is null", e);
            }
            catch (Exception e)
            {
                System.err.println(toString());
                System.err.println(this.currentLanes.get(when));
                System.err.println(this.fractionalLinkPositions.get(when));
                throw new GTUException(e);
            }
            if (Double.isNaN(loc))
            {
                System.out.println("loc is NaN");
            }
            Length length = Length.createSI(loc);
            if (CACHING)
            {
                this.cachedPositions.put(cacheIndex, length);
            }
            return length;
        }
    }

    /** Set of lane to attempt when determining the location with a deviative lane change. */
    // private Set<Lane> otherLanesToConsider;

    /**
     * In case of a deviative operational plan (not on the center lines), positions are projected fractionally to the center
     * lines. For points upstream or downstream of a lane, fractional projection is not valid. In such cases we need to project
     * the position to an upstream or downstream lane instead, and adjust length along the center lines.
     * @param lane Lane; lane to determine the position on
     * @param when Time; time
     * @param upstream boolean; whether to check upstream (or downstream)
     * @param distance double; cumulative distance in recursive search, starts at 0.0
     * @param point DirectedPoint; absolute point of GTU to be projected to center line
     * @param otherLanesToConsider Set&lt;Lane&gt;; lanes to consider
     * @return Length; position on lane being &lt;0 or &gt;{@code lane.getLength()}
     * @throws GTUException if GTU is not on the lane
     */
    private double getDistanceAtOtherLane(final Lane lane, final Time when, final boolean upstream, final double distance,
            final DirectedPoint point, final Set<Lane> otherLanesToConsider) throws GTUException
    {
        Set<Lane> nextLanes = new LinkedHashSet<>(upstream == getDirection(lane).isPlus() ? lane.prevLanes(getGTUType()).keySet()
                : lane.nextLanes(getGTUType()).keySet()); // safe copy
        nextLanes.retainAll(otherLanesToConsider); // as we delete here
        if (!upstream && nextLanes.size() > 1)
        {
            LaneDirection laneDir = new LaneDirection(lane, getDirection(lane)).getNextLaneDirection(this);
            if (nextLanes.contains(laneDir.getLane()))
            {
                nextLanes.clear();
                nextLanes.add(laneDir.getLane());
            }
            else
            {
                SimLogger.always().error("Distance on downstream lane could not be determined.");
            }
        }
        // TODO When requesting the position at the end of the plan, which will be on a further lane, this lane is not yet
        // part of the lanes in the current operational plan. This can be upstream or downstream depending on the direction of
        // travel. We might check whether getDirection(lane)=DIR_PLUS and upstream=false, or getDirection(lane)=DIR_MINUS and
        // upstream=true, to then use LaneDirection.getNextLaneDirection(this) to obtain the next lane. This is only required if
        // nextLanes originally had more than 1 lane, otherwise we can simply use that one lane. Problem is that the search
        // might go on far or even eternally (on a circular network), as projection simply keeps failing because the GTU is
        // actually towards the other longitudinal direction. Hence, the heuristic used before this method is called should
        // change and first always search against the direction of travel, and only consider lanes in currentLanes, while the
        // consecutive search in the direction of travel should then always find a point. We could build in a counter to prevent
        // a hanging software.
        if (nextLanes.size() == 0)
        {
            return Double.NaN; // point must be in the other direction
        }
        Throw.when(nextLanes.size() > 1, IllegalStateException.class,
                "A position (%s) of GTU %s is not on any of the current registered lanes.", point, this.getId());
        Lane nextLane = nextLanes.iterator().next();
        otherLanesToConsider.remove(lane);
        double f = nextLane.getCenterLine().projectFractional(null, null, point.x, point.y, FractionalFallback.NaN);
        if (Double.isNaN(f))
        {
            return getDistanceAtOtherLane(nextLane, when, upstream, distance + nextLane.getLength().si, point,
                    otherLanesToConsider);
        }
        return distance + (upstream == this.currentLanes.get(nextLane).isPlus() ? 1.0 - f : f) * nextLane.getLength().si;
    }

    /** Time of reference position cache. */
    private double referencePositionTime = Double.NaN;

    /** Cached reference position. */
    private DirectedLanePosition cachedReferencePosition = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedLanePosition getReferencePosition() throws GTUException
    {
        if (this.referencePositionTime == getSimulator().getSimulatorTime().si)
        {
            return this.cachedReferencePosition;
        }
        boolean anyOnLink = false;
        Lane refLane = null;
        double closest = Double.POSITIVE_INFINITY;
        double minEps = Double.POSITIVE_INFINITY;
        for (Lane lane : this.currentLanes.keySet())
        {
            double fraction = fractionalPosition(lane, getReference());
            if (fraction >= 0.0 && fraction <= 1.0)
            {
                // TODO widest lane in case we are registered on more than one lane with the reference point?
                // TODO lane that leads to our location or not if we are registered on parallel lanes?
                if (!anyOnLink)
                {
                    refLane = lane;
                }
                else
                {
                    DirectedPoint loc = getLocation();
                    double f = lane.getCenterLine().projectFractional(null, null, loc.x, loc.y, FractionalFallback.ENDPOINT);
                    double distance = loc.distance(lane.getCenterLine().getLocationFractionExtended(f));
                    if (refLane != null && Double.isInfinite(closest))
                    {
                        f = refLane.getCenterLine().projectFractional(null, null, loc.x, loc.y, FractionalFallback.ENDPOINT);
                        closest = loc.distance(refLane.getCenterLine().getLocationFractionExtended(f));
                    }
                    if (distance < closest)
                    {
                        refLane = lane;
                        closest = distance;
                    }
                }
                anyOnLink = true;
            }
            else if (!anyOnLink && Double.isInfinite(closest))// && getOperationalPlan() instanceof LaneBasedOperationalPlan
            // && ((LaneBasedOperationalPlan) getOperationalPlan()).isDeviative())
            {
                double eps = (fraction > 1.0 ? lane.getCenterLine().getLast() : lane.getCenterLine().getFirst())
                        .distanceSI(new OTSPoint3D(getLocation()));
                if (eps < minEps)
                {
                    minEps = eps;
                    refLane = lane;
                }
            }
        }
        if (refLane != null)
        {
            this.cachedReferencePosition =
                    new DirectedLanePosition(refLane, position(refLane, getReference()), this.getDirection(refLane));
            this.referencePositionTime = getSimulator().getSimulatorTime().si;
            return this.cachedReferencePosition;
        }
        // for (Lane lane : this.currentLanes.keySet())
        // {
        // Length relativePosition = position(lane, RelativePosition.REFERENCE_POSITION);
        // System.err
        // .println(String.format("Lane %s of Link %s: absolute position %s, relative position %5.1f%%", lane.getId(),
        // lane.getParentLink().getId(), relativePosition, relativePosition.si * 100 / lane.getLength().si));
        // }
        throw new GTUException("The reference point of GTU " + this + " is not on any of the lanes on which it is registered");
    }

    /**
     * Schedule the triggers for this GTU that are going to happen until the next evaluation time. Also schedule the
     * registration and deregistration of lanes when the vehicle enters or leaves them, at the exact right time. <br>
     * Note: when the GTU makes a lane change, the vehicle will be registered for both lanes during the entire maneuver.
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException should never happen
     * @throws GTUException when a branch is reached where the GTU does not know where to go next
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void scheduleEnterLeaveTriggers() throws NetworkException, SimRuntimeException, GTUException
    {

        LaneBasedOperationalPlan plan = null;
        double moveSI;
        if (getOperationalPlan() instanceof LaneBasedOperationalPlan)
        {
            plan = (LaneBasedOperationalPlan) getOperationalPlan();
            moveSI = plan.getTotalLengthAlongLane(this).si;
        }
        else
        {
            moveSI = getOperationalPlan().getTotalLength().si;
        }

        // Figure out which lanes this GTU will enter with its FRONT, and schedule the lane enter events
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.currentLanes);
        Iterator<Lane> it = lanesCopy.keySet().iterator();
        Lane enteredLane = null;
        // LateralDirectionality forceSide = LateralDirectionality.NONE;
        while (it.hasNext() || enteredLane != null) // use a copy because this.currentLanes can change
        {
            // next lane from 'lanesCopy', or asses the lane we just entered as it may be very short and also passed fully
            Lane lane;
            GTUDirectionality laneDir;
            if (enteredLane == null)
            {
                lane = it.next();
                laneDir = lanesCopy.get(lane);
            }
            else
            {
                lane = enteredLane;
                laneDir = this.currentLanes.get(lane);
            }
            double sign = laneDir.isPlus() ? 1.0 : -1.0;
            enteredLane = null;

            // skip if already on next lane
            if (!Collections.disjoint(this.currentLanes.keySet(),
                    lane.downstreamLanes(laneDir, getGTUType()).keySet().toCollection()))
            {
                continue;
            }

            // schedule triggers on this lane
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            // referenceStartSI is position of reference of GTU on current lane
            if (laneDir.isPlus())
            {
                lane.scheduleSensorTriggers(this, referenceStartSI, moveSI);
            }
            else
            {
                lane.scheduleSensorTriggers(this, referenceStartSI - moveSI, moveSI);
            }

            double nextFrontPosSI = referenceStartSI + sign * (moveSI + getFront().getDx().si);
            Lane nextLane = null;
            GTUDirectionality nextDirection = null;
            Length refPosAtLastTimestep = null;
            DirectedPoint end = null;
            if (laneDir.isPlus() ? nextFrontPosSI > lane.getLength().si : nextFrontPosSI < 0.0)
            {
                LaneDirection next = new LaneDirection(lane, laneDir).getNextLaneDirection(this);
                if (null == next)
                {
                    // A sink should delete the GTU, or a lane change should end, before reaching the end of the lane
                    continue;
                }
                nextLane = next.getLane();
                nextDirection = next.getDirection();
                double endPos = laneDir.isPlus() ? lane.getLength().si - getFront().getDx().si : getFront().getDx().si;
                Lane endLane = lane;
                GTUDirectionality endLaneDir = laneDir;
                while (endLaneDir.isPlus() ? endPos < 0.0 : endPos > endLane.getLength().si)
                {
                    // GTU front is more than lane length, so end location can not be extracted from the lane, let's move then
                    Map<Lane, GTUDirectionality> map = endLane.upstreamLanes(endLaneDir, getGTUType()).toMap();
                    map.keySet().retainAll(this.currentLanes.keySet());
                    double remain = endLaneDir.isPlus() ? -endPos : endPos - endLane.getLength().si;
                    endLane = map.keySet().iterator().next();
                    endLaneDir = map.get(endLane);
                    endPos = endLaneDir.isPlus() ? endLane.getLength().si - remain : remain;
                }
                end = endLane.getCenterLine().getLocationExtendedSI(endPos);
                if (laneDir.isPlus())
                {
                    refPosAtLastTimestep = nextDirection.isPlus() ? Length.createSI(referenceStartSI - lane.getLength().si)
                            : Length.createSI(nextLane.getLength().si - referenceStartSI + lane.getLength().si);
                }
                else
                {
                    refPosAtLastTimestep = nextDirection.isPlus() ? Length.createSI(-referenceStartSI)
                            : Length.createSI(nextLane.getLength().si + referenceStartSI);
                }
            }

            if (end != null)
            {
                Time enterTime = getOperationalPlan().timeAtPoint(end, false);
                if (enterTime != null)
                {
                    if (Double.isNaN(enterTime.si))
                    {
                        // TODO: this escape was in timeAtPoint, where it was changed to return null for leave lane events
                        enterTime = Time.createSI(getOperationalPlan().getEndTime().si - 1e-9);
                        // -1e-9 prevents that next move() reschedules enter
                    }
                    addLaneToGtu(nextLane, refPosAtLastTimestep, nextDirection);
                    enteredLane = nextLane;
                    Length coveredDistance;
                    if (plan == null || !plan.isDeviative())
                    {
                        try
                        {
                            coveredDistance = getOperationalPlan().getTraveledDistance(enterTime);
                        }
                        catch (OperationalPlanException exception)
                        {
                            throw new RuntimeException("Enter time of lane beyond plan.", exception);
                        }
                    }
                    else
                    {
                        coveredDistance = plan.getDistanceAlongLane(this, end);
                    }
                    SimEventInterface<SimTimeDoubleUnit> event = getSimulator().scheduleEventAbs(enterTime, this, this,
                            "addGtuToLane", new Object[] { nextLane, refPosAtLastTimestep.plus(coveredDistance) });
                    addEnterTrigger(nextLane, event);
                }
            }
        }

        // Figure out which lanes this GTU will exit with its BACK, and schedule the lane exit events
        for (Lane lane : this.currentLanes.keySet())
        {

            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            Time exitTime = null;

            GTUDirectionality laneDir = getDirection(lane);

            if (plan == null || !plan.isDeviative())
            {
                double sign = laneDir.isPlus() ? 1.0 : -1.0;
                double nextRearPosSI = referenceStartSI + sign * (getRear().getDx().si + moveSI);
                if (laneDir.isPlus() ? nextRearPosSI > lane.getLength().si : nextRearPosSI < 0.0)
                {
                    exitTime = getOperationalPlan().timeAtDistance(
                            Length.createSI((laneDir.isPlus() ? lane.getLength().si - referenceStartSI : referenceStartSI)
                                    - getRear().getDx().si));
                }
            }
            else
            {
                DirectedPoint end = null;
                double endPos = laneDir.isPlus() ? lane.getLength().si - getRear().getDx().si : getRear().getDx().si;
                Lane endLane = lane;
                GTUDirectionality endLaneDir = laneDir;
                while (endLaneDir.isPlus() ? endPos > endLane.getLength().si : endPos < 0.0)
                {
                    Map<Lane, GTUDirectionality> map = endLane.downstreamLanes(endLaneDir, getGTUType()).toMap();
                    map.keySet().retainAll(this.currentLanes.keySet());
                    if (!map.isEmpty())
                    {
                        double remain = endLaneDir.isPlus() ? endPos - endLane.getLength().si : -endPos;
                        endLane = map.keySet().iterator().next();
                        endLaneDir = map.get(endLane);
                        endPos = endLaneDir.isPlus() ? remain : endLane.getLength().si - remain;
                    }
                    else
                    {
                        endPos = endLaneDir.isPlus() ? endLane.getLength().si - getRear().getDx().si : getRear().getDx().si;
                        break;
                    }
                }
                end = endLane.getCenterLine().getLocationExtendedSI(endPos);
                if (end != null)
                {
                    exitTime = getOperationalPlan().timeAtPoint(end, false);
                    if (Double.isNaN(exitTime.si))
                    {
                        // code below will leave entered lanes if exitTime is null, make this so if NaN results due to the lane
                        // end being beyond the plan (rather than the GTU never having been there, but being registered there
                        // upon lane change initiation)
                        double sign = laneDir.isPlus() ? 1.0 : -1.0;
                        double nextRearPosSI = referenceStartSI + sign * (getRear().getDx().si + moveSI);
                        if (laneDir.isPlus() ? nextRearPosSI < lane.getLength().si : nextRearPosSI > 0.0)
                        {
                            exitTime = null;
                        }
                    }
                }
            }

            if (exitTime != null && !Double.isNaN(exitTime.si))
            {
                SimEvent<SimTimeDoubleUnit> event = new SimEvent<>(new SimTimeDoubleUnit(exitTime), this, this, "leaveLane",
                        new Object[] { lane, new Boolean(false) });
                getSimulator().scheduleEvent(event);
                addTrigger(lane, event);
            }
            else if (exitTime != null && this.enteredLanes.contains(lane))
            {
                // This lane was entered when initiating the lane change due to a fractional calculation. Now, the deviative
                // plan indicates we will never reach this location.
                SimEvent<SimTimeDoubleUnit> event = new SimEvent<>(getSimulator().getSimTime(), this, this, "leaveLane",
                        new Object[] { lane, new Boolean(false) });
                getSimulator().scheduleEvent(event);
                addTrigger(lane, event);
            }
        }

        this.enteredLanes.clear();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws GTUException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.currentLanes.keySet())
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void addTrigger(final Lane lane, final SimEventInterface<SimTimeDoubleUnit> event)
    {
        List<SimEventInterface<SimTimeDoubleUnit>> list = this.pendingLeaveTriggers.get(lane);
        if (null == list)
        {
            list = new ArrayList<>();
        }
        list.add(event);
        this.pendingLeaveTriggers.put(lane, list);
    }

    /**
     * Add enter trigger.
     * @param lane Lane; lane
     * @param event SimEventInterface&lt;SimTimeDoubleUnit&gt;; event
     */
    private void addEnterTrigger(final Lane lane, final SimEventInterface<SimTimeDoubleUnit> event)
    {
        List<SimEventInterface<SimTimeDoubleUnit>> list = this.pendingEnterTriggers.get(lane);
        if (null == list)
        {
            list = new ArrayList<>();
        }
        list.add(event);
        this.pendingEnterTriggers.put(lane, list);
    }

    /**
     * Sets a vehicle model.
     * @param vehicleModel VehicleModel; vehicle model
     */
    public void setVehicleModel(final VehicleModel vehicleModel)
    {
        this.vehicleModel = vehicleModel;
    }

    /** {@inheritDoc} */
    @Override
    public VehicleModel getVehicleModel()
    {
        return this.vehicleModel;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        DirectedLanePosition dlp = null;
        try
        {
            dlp = getReferencePosition();
        }
        catch (GTUException e)
        {
            // ignore. not important at destroy
        }
        DirectedPoint location = this.getOperationalPlan() == null ? new DirectedPoint(0.0, 0.0, 0.0) : getLocation();

        synchronized (this.lock)
        {
            Set<Lane> laneSet = new LinkedHashSet<>(this.currentLanes.keySet()); // Operate on a copy of the key
                                                                                 // set
            for (Lane lane : laneSet)
            {
                try
                {
                    leaveLane(lane, true);
                }
                catch (GTUException e)
                {
                    // ignore. not important at destroy
                }
            }
        }

        if (dlp != null && dlp.getLane() != null)
        {
            Lane referenceLane = dlp.getLane();
            fireTimedEvent(LaneBasedGTU.LANEBASED_DESTROY_EVENT,
                    new Object[] { getId(), location, getOdometer(), referenceLane, dlp.getPosition(), dlp.getGtuDirection() },
                    getSimulator().getSimulatorTime());
        }
        else
        {
            fireTimedEvent(LaneBasedGTU.LANEBASED_DESTROY_EVENT,
                    new Object[] { getId(), location, getOdometer(), null, Length.ZERO, null },
                    getSimulator().getSimulatorTime());
        }
        if (this.finalizeLaneChangeEvent != null)
        {
            getSimulator().cancelEvent(this.finalizeLaneChangeEvent);
        }

        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedStrategicalPlanner getStrategicalPlanner(final Time time)
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner(time);
    }

    /** {@inheritDoc} */
    @Override
    public RoadNetwork getNetwork()
    {
        return (RoadNetwork) super.getPerceivableContext();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getDesiredSpeed()
    {
        Time simTime = getSimulator().getSimulatorTime();
        if (this.desiredSpeedTime == null || this.desiredSpeedTime.si < simTime.si)
        {
            InfrastructurePerception infra =
                    getTacticalPlanner().getPerception().getPerceptionCategoryOrNull(InfrastructurePerception.class);
            SpeedLimitInfo speedInfo;
            if (infra == null)
            {
                speedInfo = new SpeedLimitInfo();
                speedInfo.addSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED, getMaximumSpeed());
            }
            else
            {
                // Throw.whenNull(infra, "InfrastructurePerception is required to determine the desired speed.");
                speedInfo = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
            }
            this.cachedDesiredSpeed =
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().desiredSpeed(getParameters(), speedInfo),
                            "Parameter exception while obtaining the desired speed.");
            this.desiredSpeedTime = simTime;
        }
        return this.cachedDesiredSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getCarFollowingAcceleration()
    {
        Time simTime = getSimulator().getSimulatorTime();
        if (this.carFollowingAccelerationTime == null || this.carFollowingAccelerationTime.si < simTime.si)
        {
            LanePerception perception = getTacticalPlanner().getPerception();
            // speed
            EgoPerception<?, ?> ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
            Throw.whenNull(ego, "EgoPerception is required to determine the speed.");
            Speed speed = ego.getSpeed();
            // speed limit info
            InfrastructurePerception infra = perception.getPerceptionCategoryOrNull(InfrastructurePerception.class);
            Throw.whenNull(infra, "InfrastructurePerception is required to determine the desired speed.");
            SpeedLimitInfo speedInfo = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
            // leaders
            NeighborsPerception neighbors = perception.getPerceptionCategoryOrNull(NeighborsPerception.class);
            Throw.whenNull(neighbors, "NeighborsPerception is required to determine the car-following acceleration.");
            PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            // check collision
            if (!leaders.isEmpty())
            {
                leaders.collect(this.collisionDetector);
            }
            // obtain
            this.cachedCarFollowingAcceleration =
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().followingAcceleration(getParameters(), speed,
                            speedInfo, leaders), "Parameter exception while obtaining the desired speed.");
            this.carFollowingAccelerationTime = simTime;
        }
        return this.cachedCarFollowingAcceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus()
    {
        return this.turnIndicatorStatus.get();
    }

    /** {@inheritDoc} */
    @Override
    public final TurnIndicatorStatus getTurnIndicatorStatus(final Time time)
    {
        return this.turnIndicatorStatus.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus)
    {
        this.turnIndicatorStatus.set(turnIndicatorStatus);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("GTU " + getId());
    }

}
