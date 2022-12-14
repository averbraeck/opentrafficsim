package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point3d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableLinkedHashSet;
import org.djutils.immutablecollections.ImmutableSet;
import org.djutils.logger.CategoryLogger;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.AbstractGtu;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
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
import org.opentrafficsim.core.perception.collections.HistoricalArrayList;
import org.opentrafficsim.core.perception.collections.HistoricalList;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

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
 * To decide its movement, an AbstractLaneBasedGtu applies its car following algorithm and lane change algorithm to set the
 * acceleration and any lane change operation to perform. It then schedules the triggers that will add it to subsequent lanes
 * and remove it from current lanes as needed during the time step that is has committed to. Finally, it re-schedules its next
 * movement evaluation with the simulator.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedGtu extends AbstractGtu implements LaneBasedGtu
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Lanes. */
    private final HistoricalList<CrossSection> crossSections;

    /** Reference lane index (0 = left or only lane, 1 = right lane). */
    private int referenceLaneIndex = 0;

    /** Time of reference position cache. */
    private double referencePositionTime = Double.NaN;

    /** Cached reference position. */
    private LanePosition cachedReferencePosition = null;

    /** Pending leave triggers for each lane. */
    private SimEventInterface<Duration> pendingLeaveTrigger;

    /** Pending enter triggers for each lane. */
    private SimEventInterface<Duration> pendingEnterTrigger;

    /** Event to finalize lane change. */
    private SimEventInterface<Duration> finalizeLaneChangeEvent;

    /** Sensor events. */
    private Set<SimEventInterface<Duration>> sensorEvents = new LinkedHashSet<>();

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

    /** Margin to add to plan to check of the path will enter the next section. */
    public static Length eventMargin = Length.instantiateSI(50.0);

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

    /** Whether the GTU perform lane changes instantaneously or not. */
    private boolean instantaneousLaneChange = false;

    /**
     * Construct a Lane Based GTU.
     * @param id String; the id of the GTU
     * @param gtuType GtuType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GtuException when initial values are not correct
     */
    public AbstractLaneBasedGtu(final String id, final GtuType gtuType, final OtsRoadNetwork network) throws GtuException
    {
        super(id, gtuType, network.getSimulator(), network);
        OtsSimulatorInterface simulator = network.getSimulator();
        HistoryManager historyManager = simulator.getReplication().getHistoryManager(simulator);
        this.crossSections = new HistoricalArrayList<>(historyManager);
        this.turnIndicatorStatus = new HistoricalValue<>(historyManager, TurnIndicatorStatus.NOTPRESENT);
    }

    /**
     * @param strategicalPlanner LaneBasedStrategicalPlanner; the strategical planner (e.g., route determination) to use
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the initial positions of the car on one or more
     *            lanes with their directions
     * @param initialSpeed Speed; the initial speed of the car on the lane
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GtuException when initial values are not correct
     * @throws OtsGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void init(final LaneBasedStrategicalPlanner strategicalPlanner, final Set<LanePosition> initialLongitudinalPositions,
            final Speed initialSpeed) throws NetworkException, SimRuntimeException, GtuException, OtsGeometryException
    {
        Throw.when(null == initialLongitudinalPositions, GtuException.class, "InitialLongitudinalPositions is null");
        Throw.when(0 == initialLongitudinalPositions.size(), GtuException.class, "InitialLongitudinalPositions is empty set");

        for (LanePosition pos : new LinkedHashSet<LanePosition>(initialLongitudinalPositions)) // copy
        {
            double fracFront = (pos.getPosition().si + getFront().getDx().si) / pos.getLane().getLength().si;
            if (fracFront > 1.0)
            {
                System.err.println("GTU " + toString() + " has been destroyed at init since it occupied multiple lanes");
                this.destroy();
                return;
            }
            double fracRear = (pos.getPosition().si - getRear().getDx().si) / pos.getLane().getLength().si;
            if (fracRear < 0.0)
            {
                System.err.println("GTU " + toString() + " has been destroyed at init since it occupied multiple lanes");
                this.destroy();
                return;
            }

        }

        DirectedPoint lastPoint = null;
        for (LanePosition pos : initialLongitudinalPositions)
        {
            // Throw.when(lastPoint != null && pos.getLocation().distance(lastPoint) > initialLocationThresholdDifference.si,
            // GTUException.class, "initial locations for GTU have distance > " + initialLocationThresholdDifference);
            lastPoint = pos.getLocation();
        }
        DirectedPoint initialLocation = lastPoint;

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move and events will work
        Time now = getSimulator().getSimulatorAbsTime();
        try
        {
            if (initialSpeed.si < OperationalPlan.DRIFTING_SPEED_SI)
            {
                this.operationalPlan
                        .set(new OperationalPlan(this, initialLocation, now, new Duration(1E-6, DurationUnit.SECOND)));
            }
            else
            {
                OtsPoint3D p2 = new OtsPoint3D(initialLocation.x + 1E-6 * Math.cos(initialLocation.getRotZ()),
                        initialLocation.y + 1E-6 * Math.sin(initialLocation.getRotZ()), initialLocation.z);
                OtsLine3D path = new OtsLine3D(new OtsPoint3D(initialLocation), p2);
                this.operationalPlan.set(OperationalPlanBuilder.buildConstantSpeedPlan(this, path, now, initialSpeed));
            }
        }
        catch (OperationalPlanException e)
        {
            throw new RuntimeException("Initial operational plan could not be created.", e);
        }

        // XXX: Added code AV 20220903
        // Check if the gtu has been registered on all lanes where it resides
        // Could not make the code work, so destroy the vehicles that are residing in multiple lanes...
        /*-
        for (DirectedLanePosition pos : new LinkedHashSet<DirectedLanePosition>(initialLongitudinalPositions)) // copy
        {
            DirectedLanePosition dlp = null;
            DirectedLanePosition posRef = pos;
            double fracFront = (posRef.getPosition().si + getFront().getDx().si) / posRef.getLane().getLength().si;
            while (fracFront > 1.0) // only take DIR_PLUS into account for now
            {
                Set<Lane> nextLanes = posRef.getLane().nextLanes(getGtuType());
                Throw.when(nextLanes.size() > 1, GTUException.class, "next lane for GTU %s: init has multiple choices", this);
                Lane nextLane = nextLanes.keySet().iterator().next();
                boolean alreadyPresent = false;
                for (DirectedLanePosition pos2 : initialLongitudinalPositions)
                {
                    if (pos2.getLane().equals(nextLane))
                    {
                        alreadyPresent = true;
                        dlp = pos2;
                        break;
                    }
                }
                if (!alreadyPresent)
                {
                    // calculate relative position for reference
                    Length len2 = posRef.getPosition().minus(posRef.getLane().getLength());
                    dlp = new DirectedLanePosition(nextLane, len2, GTUDirectionality.DIR_PLUS);
                    initialLongitudinalPositions.add(dlp);
                }
                fracFront = 0.0; // TODO: Change this code for a full while loop for multiple lanes
                posRef = dlp;
            }
            
            dlp = null;
            posRef = pos;
            double fracRear = (posRef.getPosition().si - getRear().getDx().si) / posRef.getLane().getLength().si;
            if (fracRear < 0.0) 
            {
                System.err.println("GTU " + toString() + " has been destroyed at init since it occupied multiple lanes");
                this.destroy();
            }
            while (fracRear < 0.0) // only take DIR_PLUS into account for now
            {
                Set<Lane> prevLanes = posRef.getLane().prevLanes(getGtuType());
                Throw.when(prevLanes.size() > 1, GTUException.class, "prev lane for GTU %s: init has multiple choices", this);
                Lane prevLane = prevLanes.keySet().iterator().next();
                boolean alreadyPresent = false;
                for (DirectedLanePosition pos2 : initialLongitudinalPositions)
                {
                    if (pos2.getLane().equals(prevLane))
                    {
                        alreadyPresent = true;
                        dlp = pos2;
                        break;
                    }
                }
                if (!alreadyPresent)
                {
                    // calculate relative position for reference
                    Length len2 = prevLane.getLength().plus(posRef.getPosition());
                    dlp = new DirectedLanePosition(prevLane, len2, GTUDirectionality.DIR_PLUS);
                    initialLongitudinalPositions.add(dlp);
                }
                fracRear = 1.0; // TODO: change this code for a full while loop for multiple lanes
                posRef = dlp;
            }
        }
        */

        // register the GTU on the lanes
        List<LanePosition> inits = new ArrayList<>(); // need to sort them
        inits.addAll(initialLongitudinalPositions);
        Collections.sort(inits, new Comparator<LanePosition>()
        {
            @Override
            public int compare(final LanePosition o1, final LanePosition o2)
            {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });
        for (LanePosition directedLanePosition : inits)
        {
            List<Lane> lanes = new ArrayList<>();
            lanes.add(directedLanePosition.getLane());
            this.crossSections.add(new CrossSection(lanes)); // enter lane part 1
        }

        // init event
        LanePosition referencePosition = getReferencePosition();
        fireTimedEvent(LaneBasedGtu.LANEBASED_INIT_EVENT,
                new Object[] {getId(), new OtsPoint3D(initialLocation).doubleVector(PositionUnit.METER),
                        OtsPoint3D.direction(initialLocation, DirectionUnit.EAST_RADIAN), getLength(), getWidth(),
                        referencePosition.getLane().getParentLink().getId(), referencePosition.getLane().getId(),
                        referencePosition.getPosition(), getType().getId()},
                getSimulator().getSimulatorTime());

        // register the GTU on the lanes
        for (LanePosition directedLanePosition : initialLongitudinalPositions)
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
    public synchronized void setParent(final Gtu gtu) throws GtuException
    {
        leaveAllLanes();
        super.setParent(gtu);
    }

    /**
     * Removes the registration between this GTU and all the lanes.
     */
    private void leaveAllLanes()
    {
        for (CrossSection crossSection : this.crossSections)
        {
            boolean removeFromParentLink = true;
            for (Lane lane : crossSection.getLanes())
            {
                // GTU should be on this lane as we loop the registration
                Length pos = Try.assign(() -> position(lane, getReference()), "Unexpected exception.");
                lane.removeGTU(this, removeFromParentLink, pos);
                removeFromParentLink = false;
            }
        }
        this.crossSections.clear();
    }

    /**
     * Reinitializes the GTU on the network using the existing strategical planner and zero speed.
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; initial position
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GtuException when initial values are not correct
     * @throws OtsGeometryException when the initial path is wrong
     */
    public void reinit(final Set<LanePosition> initialLongitudinalPositions)
            throws NetworkException, SimRuntimeException, GtuException, OtsGeometryException
    {
        init(getStrategicalPlanner(), initialLongitudinalPositions, Speed.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection) throws GtuException
    {

        // from info
        LanePosition from = getReferencePosition();

        // obtain position on lane adjacent to reference lane and enter lanes upstream/downstream from there
        Set<Lane> adjLanes = from.getLane().accessibleAdjacentLanesPhysical(laneChangeDirection, getType());
        Lane adjLane = adjLanes.iterator().next();
        Length position = adjLane.position(from.getLane().fraction(from.getPosition()));
        leaveAllLanes();
        enterLaneRecursive(adjLane, position, 0);

        // stored positions no longer valid
        this.referencePositionTime = Double.NaN;
        this.cachedPositions.clear();

        // fire event
        this.fireTimedEvent(
                LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection.name(),
                        from.getLane().getParentLink().getId(), from.getLane().getId(), from.getPosition()},
                getSimulator().getSimulatorTime());

    }

    /**
     * Enters lanes upstream and downstream of the new location after an instantaneous lane change.
     * @param lane LaneDirection; considered lane
     * @param position Length; position to add GTU at
     * @param dir int; below 0 for upstream, above 0 for downstream, 0 for both<br>
     *            TODO: the below 0 and above 0 is NOT what is tested
     * @throws GtuException on exception
     */
    private void enterLaneRecursive(final Lane lane, final Length position, final int dir) throws GtuException
    {
        List<Lane> lanes = new ArrayList<>();
        lanes.add(lane);
        int index = dir > 0 ? this.crossSections.size() : 0;
        this.crossSections.add(index, new CrossSection(lanes));
        lane.addGTU(this, position);

        // upstream
        if (dir < 1)
        {
            Length rear = position.plus(getRear().getDx());
            Length before = null;
            if (rear.si < 0.0)
            {
                before = rear.neg();
            }
            if (before != null)
            {
                ImmutableSet<Lane> upstream = new ImmutableLinkedHashSet<>(lane.prevLanes(getType()));
                if (!upstream.isEmpty())
                {
                    Lane upLane = null;
                    for (Lane nextUp : upstream)
                    {
                        for (CrossSection crossSection : this.crossSections)
                        {
                            if (crossSection.getLanes().contains(nextUp))
                            {
                                // multiple upstream lanes could belong to the same link, we pick an arbitrary lane
                                // (a conflict should solve this)
                                upLane = nextUp;
                                break;
                            }
                        }
                    }
                    if (upLane == null)
                    {
                        // the rear is on an upstream section we weren't before the lane change, due to curvature, we pick an
                        // arbitrary lane (a conflict should solve this)
                        upLane = upstream.iterator().next();
                    }
                    Lane next = upLane;
                    Length nextPos = next.getLength().minus(before).minus(getRear().getDx());
                    enterLaneRecursive(next, nextPos, -1);
                }
            }
        }

        // downstream
        if (dir > -1)
        {
            Length front = position.plus(getFront().getDx());
            Length passed = null;
            if (front.si > lane.getLength().si)
            {
                passed = front.minus(lane.getLength());
            }
            if (passed != null)
            {
                Lane next = getNextLaneForRoute(lane);
                Length nextPos = passed.minus(getFront().getDx());
                enterLaneRecursive(next, nextPos, 1);
            }
        }
    }

    /**
     * Register on lanes in target lane.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     * @throws GtuException exception
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public synchronized void initLaneChange(final LateralDirectionality laneChangeDirection) throws GtuException
    {
        List<CrossSection> newLanes = new ArrayList<>();
        int index = laneChangeDirection.isLeft() ? 0 : 1;
        int numRegistered = 0;
        DirectedPoint point = getLocation();
        Map<Lane, Double> addToLanes = new LinkedHashMap<>();
        for (CrossSection crossSection : this.crossSections)
        {
            List<Lane> resultingLanes = new ArrayList<>();
            Lane lane = crossSection.getLanes().get(0);
            resultingLanes.add(lane);
            Set<Lane> laneSet = lane.accessibleAdjacentLanesLegal(laneChangeDirection, getType());
            if (laneSet.size() > 0)
            {
                numRegistered++;
                Lane adjacentLane = laneSet.iterator().next();
                double f = adjacentLane.getCenterLine().projectFractional(null, null, point.x, point.y, FractionalFallback.NaN);
                if (Double.isNaN(f))
                {
                    // the GTU is upstream or downstream of the lane, or on the edge and we have rounding problems
                    // in either case we add the GTU at an extreme
                    // (this is only for ordering on the lane, the position is not used otherwise)
                    Length pos = position(lane, getReference());
                    addToLanes.put(adjacentLane, pos.si < lane.getLength().si / 2 ? 0.0 : 1.0);
                }
                else
                {
                    addToLanes.put(adjacentLane, adjacentLane.getLength().times(f).si / adjacentLane.getLength().si);
                }
                resultingLanes.add(index, adjacentLane);
            }
            newLanes.add(new CrossSection(resultingLanes));
        }
        Throw.when(numRegistered == 0, GtuException.class, "Gtu %s starting %s lane change, but no adjacent lane found.",
                getId(), laneChangeDirection);
        this.crossSections.clear();
        this.crossSections.addAll(newLanes);
        for (Entry<Lane, Double> entry : addToLanes.entrySet())
        {
            entry.getKey().addGTU(this, entry.getValue());
        }
        this.referenceLaneIndex = 1 - index;
    }

    /**
     * Performs the finalization of a lane change by leaving the from lanes.
     * @param laneChangeDirection LateralDirectionality; direction of lane change
     * @throws GtuException if position or direction could not be obtained
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected synchronized void finalizeLaneChange(final LateralDirectionality laneChangeDirection) throws GtuException
    {
        List<CrossSection> newLanes = new ArrayList<>();
        Lane fromLane = null;
        Length fromPosition = null;
        for (CrossSection crossSection : this.crossSections)
        {
            Lane lane = crossSection.getLanes().get(this.referenceLaneIndex);
            if (lane != null)
            {
                Length pos = position(lane, RelativePosition.REFERENCE_POSITION);
                if (0.0 <= pos.si && pos.si <= lane.getLength().si)
                {
                    fromLane = lane;
                    fromPosition = pos;
                }
                lane.removeGTU(this, false, pos);
            }
            List<Lane> remainingLane = new ArrayList<>();
            remainingLane.add(crossSection.getLanes().get(1 - this.referenceLaneIndex));
            newLanes.add(new CrossSection(remainingLane));
        }
        this.crossSections.clear();
        this.crossSections.addAll(newLanes);
        this.referenceLaneIndex = 0;

        Throw.when(fromLane == null, RuntimeException.class, "No from lane for lane change event.");
        LanePosition from = new LanePosition(fromLane, fromPosition);

        // XXX: WRONG: this.fireTimedEvent(LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection, from},
        // XXX: WRONG: getSimulator().getSimulatorTime());
        this.fireTimedEvent(
                LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection.name(),
                        from.getLane().getParentLink().getId(), from.getLane().getId(), from.getPosition()},
                getSimulator().getSimulatorTime());

        this.finalizeLaneChangeEvent = null;
    }

    /** {@inheritDoc} */
    @Override
    public void setFinalizeLaneChangeEvent(final SimEventInterface<Duration> event)
    {
        this.finalizeLaneChangeEvent = event;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected synchronized boolean move(final DirectedPoint fromLocation)
            throws SimRuntimeException, GtuException, OperationalPlanException, NetworkException, ParameterException
    {
        if (this.isDestroyed())
        {
            return false;
        }
        try
        {
            if (this.crossSections.isEmpty())
            {
                destroy();
                return false; // Done; do not re-schedule execution of this move method.
            }

            // cancel events, if any
            // FIXME: If there are still events left, clearly something went wrong?
            // XXX: Added boolean to indicate whether warnings need to be given when events were found
            cancelAllEvents();

            // generate the next operational plan and carry it out
            // in case of an instantaneous lane change, fractionalLinkPositions will be accordingly adjusted to the new lane
            try
            {
                boolean error = super.move(fromLocation);
                if (error)
                {
                    return error;
                }
            }
            catch (Exception exception)
            {
                System.err.println(exception.getMessage());
                System.err.println("  GTU " + this + " DESTROYED AND REMOVED FROM THE SIMULATION");
                this.destroy();
                this.cancelAllEvents();
                return true;
            }

            LanePosition dlp = getReferencePosition();

            scheduleEnterEvent();
            scheduleLeaveEvent();

            // sensors
            for (CrossSection crossSection : this.crossSections)
            {
                for (Lane lane : crossSection.getLanes())
                {
                    scheduleTriggers(lane);
                }
            }

            fireTimedEvent(LaneBasedGtu.LANEBASED_MOVE_EVENT,
                    new Object[] {getId(), new OtsPoint3D(fromLocation).doubleVector(PositionUnit.METER),
                            OtsPoint3D.direction(fromLocation, DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
                            getTurnIndicatorStatus().name(), getOdometer(), dlp.getLane().getParentLink().getId(),
                            dlp.getLane().getId(), dlp.getPosition()},
                    getSimulator().getSimulatorTime());

            return false;

        }
        catch (Exception ex)
        {
            try
            {
                getErrorHandler().handle(this, ex);
            }
            catch (Exception exception)
            {
                throw new GtuException(exception);
            }
            return true;
        }

    }

    /**
     * Cancels all future events.
     */
    private void cancelAllEvents()
    {
        if (this.pendingEnterTrigger != null)
        {
            getSimulator().cancelEvent(this.pendingEnterTrigger);
        }
        if (this.pendingLeaveTrigger != null)
        {
            getSimulator().cancelEvent(this.pendingLeaveTrigger);
        }
        if (this.finalizeLaneChangeEvent != null)
        {
            getSimulator().cancelEvent(this.finalizeLaneChangeEvent);
        }
        for (SimEventInterface<Duration> event : this.sensorEvents)
        {
            if (event.getAbsoluteExecutionTime().gt(getSimulator().getSimulatorTime()))
            {
                getSimulator().cancelEvent(event);
            }
        }
        this.sensorEvents.clear();
    }

    /**
     * Checks whether the GTU will enter a next cross-section during the (remainder of) the tactical plan. Only one event will
     * be scheduled. Possible additional events are scheduled upon entering the cross-section.
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleEnterEvent() throws GtuException, OperationalPlanException, SimRuntimeException
    {
        CrossSection lastCrossSection = this.crossSections.get(this.crossSections.size() - 1);
        // heuristic to prevent geometric calculation if the next section is quite far away anyway
        Length remain = remainingEventDistance();
        Lane lane = lastCrossSection.getLanes().get(this.referenceLaneIndex);
        Length position = position(lane, getFront());
        boolean possiblyNearNextSection = lane.getLength().minus(position).lt(remain);
        if (possiblyNearNextSection)
        {
            CrossSectionLink link = lastCrossSection.getLanes().get(0).getParentLink();
            OtsLine3D enterLine = link.getEndLine();
            Time enterTime = timeAtLine(enterLine, getFront());
            if (enterTime != null)
            {
                if (enterTime.lt(getSimulator().getSimulatorAbsTime()))
                {
                    System.err.println(
                            "Time travel? enterTime=" + enterTime + "; simulator time=" + getSimulator().getSimulatorAbsTime());
                    enterTime = getSimulator().getSimulatorAbsTime();
                }
                this.pendingEnterTrigger =
                        getSimulator().scheduleEventAbsTime(enterTime, this, this, "enterCrossSection", null);
            }
        }
    }

    /**
     * Appends a new cross-section at the downstream end. Possibly schedules a next enter event.
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected synchronized void enterCrossSection() throws GtuException, OperationalPlanException, SimRuntimeException
    {
        CrossSection lastCrossSection = this.crossSections.get(this.crossSections.size() - 1);
        Lane lcsLane = lastCrossSection.getLanes().get(this.referenceLaneIndex);
        Lane nextLcsLane = getNextLaneForRoute(lcsLane);
        if (nextLcsLane == null)
        {
            forceLaneChangeFinalization();
            return;
        }
        List<Lane> nextLanes = new ArrayList<>();
        for (int i = 0; i < lastCrossSection.getLanes().size(); i++)
        {
            if (i == this.referenceLaneIndex)
            {
                nextLanes.add(nextLcsLane);
            }
            else
            {
                Lane lane = lastCrossSection.getLanes().get(i);
                Set<Lane> lanes = lane.nextLanes(getType());
                if (lanes.size() == 1)
                {
                    Lane nextLane = lanes.iterator().next();
                    nextLanes.add(nextLane);
                }
                else
                {
                    boolean added = false;
                    for (Lane nextLane : lanes)
                    {
                        if (nextLane.getParentLink().equals(nextLcsLane.getParentLink())
                                && nextLane
                                        .accessibleAdjacentLanesPhysical(this.referenceLaneIndex == 0
                                                ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT, getType())
                                        .contains(nextLcsLane))
                        {
                            nextLanes.add(nextLane);
                            added = true;
                            break;
                        }
                    }
                    if (!added)
                    {
                        forceLaneChangeFinalization();
                        return;
                    }
                }
            }
        }
        this.crossSections.add(new CrossSection(nextLanes));
        for (Lane lane : nextLanes)
        {
            lane.addGTU(this, 0.0);
        }
        this.pendingEnterTrigger = null;
        scheduleEnterEvent();
        for (Lane lane : nextLanes)
        {
            scheduleTriggers(lane);
        }
    }

    /**
     * Helper method for {@code enterCrossSection}. In some cases the GTU should first finalize the lane change. This method
     * checks whether such an event is scheduled, and performs it. This method then re-attempts to enter the cross-section. So
     * the calling method should return after calling this.
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    private void forceLaneChangeFinalization() throws GtuException, OperationalPlanException, SimRuntimeException
    {
        if (this.finalizeLaneChangeEvent != null)
        {
            // a lane change should be finalized at this time, but the event is later in the queue, force it now
            SimEventInterface<Duration> tmp = this.finalizeLaneChangeEvent;
            finalizeLaneChange(this.referenceLaneIndex == 0 ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT);
            getSimulator().cancelEvent(tmp);
            enterCrossSection();
        }
        // or a sink sensor should delete us
    }

    /**
     * Checks whether the GTU will leave a cross-section during the (remainder of) the tactical plan. Only one event will be
     * scheduled. Possible additional events are scheduled upon leaving the cross-section.
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleLeaveEvent() throws GtuException, OperationalPlanException, SimRuntimeException
    {
        if (this.crossSections.isEmpty())
        {
            CategoryLogger.always().error("GTU {} has empty crossSections", this);
            return;
        }
        CrossSection firstCrossSection = this.crossSections.get(0);
        // check, if reference lane is not in first cross section
        boolean possiblyNearNextSection =
                !getReferencePosition().getLane().equals(firstCrossSection.getLanes().get(this.referenceLaneIndex));
        if (!possiblyNearNextSection)
        {
            Length remain = remainingEventDistance();
            Lane lane = firstCrossSection.getLanes().get(this.referenceLaneIndex);
            Length position = position(lane, getRear());
            possiblyNearNextSection = lane.getLength().minus(position).lt(remain);
        }
        if (possiblyNearNextSection)
        {
            CrossSectionLink link = firstCrossSection.getLanes().get(0).getParentLink();
            OtsLine3D leaveLine = link.getEndLine();
            Time leaveTime = timeAtLine(leaveLine, getRear());
            if (leaveTime == null)
            {
                // no intersect, let's do a check on the rear
                Lane lane = this.crossSections.get(0).getLanes().get(this.referenceLaneIndex);
                Length pos = position(lane, getRear());
                if (pos.gt(lane.getLength()))
                {
                    pos = position(lane, getRear());
                    this.pendingLeaveTrigger = getSimulator().scheduleEventNow(this, this, "leaveCrossSection", null);
                    getSimulator().getLogger().always().info("Forcing leave for GTU {} on lane {}", getId(), lane.getFullId());
                }
            }
            if (leaveTime != null)
            {
                if (leaveTime.lt(getSimulator().getSimulatorAbsTime()))
                {
                    System.err.println(
                            "Time travel? leaveTime=" + leaveTime + "; simulator time=" + getSimulator().getSimulatorAbsTime());
                    leaveTime = getSimulator().getSimulatorAbsTime();
                }
                this.pendingLeaveTrigger =
                        getSimulator().scheduleEventAbsTime(leaveTime, this, this, "leaveCrossSection", null);
            }
        }
    }

    /**
     * Removes registration between the GTU and the lanes in the most upstream cross-section. Possibly schedules a next leave
     * event.
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected synchronized void leaveCrossSection() throws GtuException, OperationalPlanException, SimRuntimeException
    {

        List<Lane> lanes = this.crossSections.get(0).getLanes();
        for (int i = 0; i < lanes.size(); i++)
        {
            Lane lane = lanes.get(i);
            if (lane != null)
            {
                lane.removeGTU(this, i == lanes.size() - 1, position(lane, getReference()));
            }
        }
        this.crossSections.remove(0);
        this.pendingLeaveTrigger = null;
        scheduleLeaveEvent();
    }

    /**
     * Schedules all trigger events during the current operational plan on the lane.
     * @param lane Lane; lane
     * @throws GtuException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleTriggers(final Lane lane) throws GtuException, OperationalPlanException, SimRuntimeException
    {
        Length remain = remainingEventDistance();
        double min = position(lane, getRear()).si;
        double max = min + remain.si + getLength().si;
        SortedMap<Double, List<SingleSensor>> sensors = lane.getSensorMap(getType()).subMap(min, max);
        for (List<SingleSensor> list : sensors.values())
        {
            for (SingleSensor sensor : list)
            {
                RelativePosition pos = this.getRelativePositions().get(sensor.getPositionType());
                Time time = timeAtLine(sensor.getGeometry(), pos);
                if (time != null)
                {
                    this.sensorEvents
                            .add(getSimulator().scheduleEventAbsTime(time, this, sensor, "trigger", new Object[] {this}));
                }
            }
        }
    }

    /**
     * Returns a safe distance beyond which a line will definitely not be crossed during the current operational plan.
     * @return Length; safe distance beyond which a line will definitely not be crossed during the current operational plan
     * @throws OperationalPlanException exception
     */
    private Length remainingEventDistance() throws OperationalPlanException
    {
        if (getOperationalPlan() instanceof LaneBasedOperationalPlan)
        {
            LaneBasedOperationalPlan plan = (LaneBasedOperationalPlan) getOperationalPlan();
            return plan.getTotalLength().minus(plan.getTraveledDistance(getSimulator().getSimulatorAbsTime()))
                    .plus(eventMargin);
        }
        return getOperationalPlan().getTotalLength().plus(eventMargin);
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getNextLaneForRoute(final Lane lane)
    {
        Set<Lane> next = lane.nextLanes(getType());
        if (next.isEmpty())
        {
            return null;
        }
        // ask strategical planner
        Set<Lane> set = getNextLanesForRoute(lane);
        if (set.size() == 1)
        {
            return set.iterator().next();
        }
        // check if the GTU is registered on any
        for (Lane l : set)
        {
            if (l.getGtuList().contains(this))
            {
                return l;
            }
        }
        // ask tactical planner
        return Try.assign(() -> getTacticalPlanner().chooseLaneAtSplit(lane, set),
                "Could not find suitable lane at split after lane %s of link %s for GTU %s.", lane.getId(),
                lane.getParentLink().getId(), getId());
    }

    /** {@inheritDoc} */
    @Override
    public Set<Lane> getNextLanesForRoute(final Lane lane)
    {
        Set<Lane> next = lane.nextLanes(getType());
        if (next.isEmpty())
        {
            return null;
        }
        Link link;
        try
        {
            link = getStrategicalPlanner().nextLink(lane.getParentLink(), getType());
        }
        catch (NetworkException exception)
        {
            throw new RuntimeException("Strategical planner experiences exception on network.", exception);
        }
        Set<Lane> out = new LinkedHashSet<>();
        for (Lane l : next)
        {
            if (l.getParentLink().equals(link))
            {
                out.add(l);
            }
        }
        return out;
    }

    /**
     * Returns an estimation of when the relative position will reach the line. Returns {@code null} if this does not occur
     * during the current operational plan.
     * @param line OTSLine3D; line, i.e. lateral line at link start or lateral entrance of sensor
     * @param relativePosition RelativePosition; position to cross the line
     * @return estimation of when the relative position will reach the line, {@code null} if this does not occur during the
     *         current operational plan
     * @throws GtuException position error
     */
    private Time timeAtLine(final OtsLine3D line, final RelativePosition relativePosition) throws GtuException
    {
        Throw.when(line.size() != 2, IllegalArgumentException.class, "Line to cross with path should have 2 points.");
        OtsLine3D path = getOperationalPlan().getPath();
        OtsPoint3D[] points;
        double adjust;
        if (relativePosition.getDx().gt0())
        {
            // as the position is downstream of the reference, we need to attach some distance at the end
            points = new OtsPoint3D[path.size() + 1];
            System.arraycopy(path.getPoints(), 0, points, 0, path.size());
            points[path.size()] = new OtsPoint3D(path.getLocationExtendedSI(path.getLengthSI() + relativePosition.getDx().si));
            adjust = -relativePosition.getDx().si;
        }
        else if (relativePosition.getDx().lt0())
        {
            points = new OtsPoint3D[path.size() + 1];
            System.arraycopy(path.getPoints(), 0, points, 1, path.size());
            points[0] = new OtsPoint3D(path.getLocationExtendedSI(relativePosition.getDx().si));
            adjust = 0.0;
        }
        else
        {
            points = path.getPoints();
            adjust = 0.0;
        }

        // find intersection
        double cumul = 0.0;
        for (int i = 0; i < points.length - 1; i++)
        {
            OtsPoint3D intersect;
            try
            {
                intersect = OtsPoint3D.intersectionOfLineSegments(points[i], points[i + 1], line.get(0), line.get(1));
            }
            catch (OtsGeometryException exception)
            {
                // should not occur, we check line.size() == 2
                throw new RuntimeException("Unexpected exception while obtaining points from line to cross.", exception);
            }
            if (intersect != null)
            {
                cumul += points[i].distanceSI(intersect);
                cumul += adjust; // , 0.0); // possible rear is already considered in first segment
                // return time at distance
                if (cumul < 0.0)
                {
                    // return getSimulator().getSimulatorAbsTime(); // this was a mistake...
                    // relative position already crossed the point, e.g. FRONT
                    return null;
                }
                if (cumul <= getOperationalPlan().getTotalLength().si)
                {
                    return getOperationalPlan().timeAtDistance(Length.instantiateSI(cumul));
                }
                // ref will cross the line, but GTU will not travel enough for rear to cross
                return null;
            }
            else if (i < points.length - 2)
            {
                cumul += points[i].distanceSI(points[i + 1]);
            }
        }
        // no intersect
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GtuException
    {
        return positions(relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GtuException
    {
        Map<Lane, Length> positions = new LinkedHashMap<>();
        for (CrossSection crossSection : this.crossSections.get(when))
        {
            for (Lane lane : crossSection.getLanes())
            {
                positions.put(lane, position(lane, relativePosition, when));
            }
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GtuException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /** Caching of time field for last stored position(s). */
    private double cachePositionsTime = Double.NaN;

    /** Caching of operation plan for last stored position(s). */
    private OperationalPlan cacheOperationalPlan = null;

    /** caching of last stored position(s). */
    private MultiKeyMap<Length> cachedPositions = new MultiKeyMap<>(Lane.class, RelativePosition.class);

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Length position(final Lane lane, final RelativePosition relativePosition, final Time when) throws GtuException
    {
        synchronized (this)
        {
            OperationalPlan plan = getOperationalPlan(when);
            if (CACHING)
            {
                if (when.si == this.cachePositionsTime && plan == this.cacheOperationalPlan)
                {
                    Length l = this.cachedPositions.get(lane, relativePosition);
                    if (l != null && (!Double.isNaN(l.si)))
                    {
                        CACHED_POSITION++;
                        // PK verify the result; uncomment if you don't trust the cache
                        // this.cachedPositions.clear();
                        // Length difficultWay = position(lane, relativePosition, when);
                        // if (Math.abs(l.si - difficultWay.si) > 0.00001)
                        // {
                        // System.err.println("Whoops: cache returns bad value for GTU " + getId() + " cache returned " + l
                        // + ", re-computing yielded " + difficultWay);
                        // l = null; // Invalidate; to debug and try again
                        // }
                        // }
                        // if (l != null)
                        // {
                        return l;
                    }
                }
                if (when.si != this.cachePositionsTime || plan != this.cacheOperationalPlan)
                {
                    this.cachePositionsTime = Double.NaN;
                    this.cacheOperationalPlan = null;
                    this.cachedPositions.clear();
                }
            }
            NON_CACHED_POSITION++;

            synchronized (this.lock)
            {
                List<CrossSection> whenCrossSections = this.crossSections.get(when);
                double loc = Double.NaN;

                try
                {
                    int crossSectionIndex = -1;
                    int lateralIndex = -1;
                    for (int i = 0; i < whenCrossSections.size(); i++)
                    {
                        if (whenCrossSections.get(i).getLanes().contains(lane))
                        {
                            crossSectionIndex = i;
                            lateralIndex = whenCrossSections.get(i).getLanes().indexOf(lane);
                            break;
                        }
                    }
                    Throw.when(lateralIndex == -1, GtuException.class, "GTU %s is not on lane %s.", this, lane);

                    DirectedPoint p = plan.getLocation(when, relativePosition);
                    double f = lane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.NaN);
                    if (!Double.isNaN(f))
                    {
                        loc = f * lane.getLength().si;
                    }
                    else
                    {
                        // the point does not project fractionally to this lane, it has to be up- or downstream of the lane
                        // try upstream
                        double distance = 0.0;
                        for (int i = crossSectionIndex - 1; i >= 0; i--)
                        {
                            Lane tryLane = whenCrossSections.get(i).getLanes().get(lateralIndex);
                            f = tryLane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.NaN);
                            if (!Double.isNaN(f))
                            {
                                f = 1 - f;
                                loc = distance - f * tryLane.getLength().si;
                                break;
                            }
                            distance -= tryLane.getLength().si;
                        }
                        // try downstream
                        if (Double.isNaN(loc))
                        {
                            distance = lane.getLength().si;
                            for (int i = crossSectionIndex + 1; i < whenCrossSections.size(); i++)
                            {
                                Lane tryLane = whenCrossSections.get(i).getLanes().get(lateralIndex);
                                f = tryLane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.NaN);
                                if (!Double.isNaN(f))
                                {
                                    loc = distance + f * tryLane.getLength().si;
                                    break;
                                }
                                distance += tryLane.getLength().si;
                            }
                        }

                    }

                    if (Double.isNaN(loc))
                    {
                        // the GTU is not on the lane with the relativePosition, nor is it registered on next/previous lanes
                        // this can occur as the GTU was generated with the rear upstream of the lane, or due to rounding errors
                        // use different fraction projection fallback
                        f = lane.getCenterLine().projectFractional(null, null, p.x, p.y, FractionalFallback.ENDPOINT);
                        if (Double.isNaN(f))
                        {
                            CategoryLogger.always().error("GTU {} at location {} cannot project itself onto {}; p is {}", this,
                                    getLocation(), lane.getCenterLine(), p);
                            plan.getLocation(when, relativePosition);
                        }
                        loc = lane.getLength().si * f;

                        // if (CACHING)
                        // {
                        // this.cachedPositions.put(cacheIndex, null);
                        // }
                        // return null;
                        // if (getOdometer().lt(getLength()))
                        // {
                        // // this occurs when the GTU is generated with the rear upstream of the lane, which we often do
                        // loc = position(lane, getFront(), when).si + relativePosition.getDx().si - getFront().getDx().si;
                        // }
                        // else
                        // {
                        // System.out.println("loc is NaN");
                        // }
                    }
                }
                catch (Exception e)
                {
                    // System.err.println(toString() + ": " + e.getMessage());
                    throw new GtuException(e);
                }

                Length length = Length.instantiateSI(loc);
                if (CACHING)
                {
                    this.cachedPositions.put(length, lane, relativePosition);
                    this.cachePositionsTime = when.si;
                    this.cacheOperationalPlan = plan;
                }
                return length;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public LanePosition getReferencePosition() throws GtuException
    {
        synchronized (this)
        {
            if (this.referencePositionTime == getSimulator().getSimulatorAbsTime().si)
            {
                return this.cachedReferencePosition;
            }
            Lane refLane = null;
            for (CrossSection crossSection : this.crossSections)
            {
                Lane lane = crossSection.getLanes().get(this.referenceLaneIndex);
                double fraction = fractionalPosition(lane, getReference());
                if (fraction >= 0.0 && fraction <= 1.0)
                {
                    refLane = lane;
                    break;
                }
            }
            if (refLane != null)
            {
                this.cachedReferencePosition = new LanePosition(refLane, position(refLane, getReference()));
                this.referencePositionTime = getSimulator().getSimulatorAbsTime().si;
                return this.cachedReferencePosition;
            }
            CategoryLogger.always().error("The reference point of GTU {} is not on any of the lanes on which it is registered",
                    this);
            for (CrossSection crossSection : this.crossSections)
            {
                Lane lane = crossSection.getLanes().get(this.referenceLaneIndex);
                double fraction = fractionalPosition(lane, getReference());
                CategoryLogger.always().error("\tGTU is on lane \"{}\" at fraction {}", lane, fraction);
            }
            throw new GtuException(
                    "The reference point of GTU " + this + " is not on any of the lanes on which it is registered");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws GtuException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorAbsTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (CrossSection crossSection : this.crossSections)
        {
            for (Lane lane : crossSection.getLanes())
            {
                positions.put(lane, fractionalPosition(lane, relativePosition, when));
            }
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time when)
            throws GtuException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition) throws GtuException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void addTrigger(final Lane lane, final SimEventInterface<Duration> event)
    {
        throw new UnsupportedOperationException("Method addTrigger is not supported.");
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
        LanePosition dlp = null;
        try
        {
            dlp = getReferencePosition();
        }
        catch (GtuException e)
        {
            // ignore. not important at destroy
        }
        DirectedPoint location = this.getOperationalPlan() == null ? new DirectedPoint(0.0, 0.0, 0.0) : getLocation();
        synchronized (this.lock)
        {
            for (CrossSection crossSection : this.crossSections)
            {
                boolean removeFromParentLink = true;
                for (Lane lane : crossSection.getLanes())
                {
                    Length position;
                    try
                    {
                        position = position(lane, getReference());
                    }
                    catch (GtuException exception)
                    {
                        // TODO: hard remove over whole network
                        // TODO: logger notification
                        throw new RuntimeException(exception);
                    }
                    lane.removeGTU(this, removeFromParentLink, position);
                    removeFromParentLink = false;
                }
            }
        }
        if (dlp != null && dlp.getLane() != null)
        {
            Lane referenceLane = dlp.getLane();
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), new OtsPoint3D(location).doubleVector(PositionUnit.METER),
                            OtsPoint3D.direction(location, DirectionUnit.EAST_RADIAN), getOdometer(),
                            referenceLane.getParentLink().getId(), referenceLane.getId(), dlp.getPosition()},
                    getSimulator().getSimulatorTime());
        }
        else
        {
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), new OtsPoint3D(location).doubleVector(PositionUnit.METER),
                            OtsPoint3D.direction(location, DirectionUnit.EAST_RADIAN), getOdometer(), null, null, null},
                    getSimulator().getSimulatorTime());
        }
        cancelAllEvents();

        super.destroy();
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new Bounds(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
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
        synchronized (this)
        {
            Time simTime = getSimulator().getSimulatorAbsTime();
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
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getCarFollowingAcceleration()
    {
        synchronized (this)
        {
            Time simTime = getSimulator().getSimulatorAbsTime();
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
                PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
                // obtain
                this.cachedCarFollowingAcceleration =
                        Try.assign(() -> getTacticalPlanner().getCarFollowingModel().followingAcceleration(getParameters(),
                                speed, speedInfo, leaders), "Parameter exception while obtaining the desired speed.");
                this.carFollowingAccelerationTime = simTime;
            }
            return this.cachedCarFollowingAcceleration;
        }
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
    public Length getLateralPosition(final Lane lane) throws GtuException
    {
        OperationalPlan plan = getOperationalPlan();
        if (plan instanceof LaneBasedOperationalPlan && !((LaneBasedOperationalPlan) plan).isDeviative())
        {
            return Length.ZERO;
        }
        LanePosition ref = getReferencePosition();
        int latIndex = -1;
        int longIndex = -1;
        for (int i = 0; i < this.crossSections.size(); i++)
        {
            List<Lane> lanes = this.crossSections.get(i).getLanes();
            if (lanes.contains(lane))
            {
                latIndex = lanes.indexOf(lane);
            }
            if (lanes.contains(ref.getLane()))
            {
                longIndex = i;
            }
        }
        Throw.when(latIndex == -1 || longIndex == -1, GtuException.class, "GTU %s is not on %s", getId(), lane);
        Lane refCrossSectionLane = this.crossSections.get(longIndex).getLanes().get(latIndex);
        DirectedPoint loc = getLocation();
        double f = refCrossSectionLane.getCenterLine().projectOrthogonal(loc.x, loc.y);
        DirectedPoint p = Try.assign(() -> refCrossSectionLane.getCenterLine().getLocationFraction(f), GtuException.class,
                "GTU %s is not orthogonal to the reference lane.", getId());
        double d = p.distance(loc);
        if (this.crossSections.get(0).getLanes().size() > 1)
        {
            return Length.instantiateSI(latIndex == 0 ? -d : d);
        }
        double x2 = p.x + Math.cos(p.getRotZ());
        double y2 = p.y + Math.sin(p.getRotZ());
        double det = (loc.x - p.x) * (y2 - p.y) - (loc.y - p.y) * (x2 - p.x);
        return Length.instantiateSI(det < 0.0 ? -d : d);
    }

    /** {@inheritDoc} */
    @Override
    public void setInstantaneousLaneChange(final boolean instantaneous)
    {
        this.instantaneousLaneChange = instantaneous;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInstantaneousLaneChange()
    {
        return this.instantaneousLaneChange;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("GTU " + getId());
    }

    /** Cross section of lanes. */
    private static class CrossSection
    {

        /** Lanes. */
        private final List<Lane> lanes;

        /**
         * @param lanes List&lt;Lane&gt;; lanes
         */
        protected CrossSection(final List<Lane> lanes)
        {
            this.lanes = lanes;
        }

        /**
         * @return lanes.
         */
        protected List<Lane> getLanes()
        {
            return this.lanes;
        }

    }

}
