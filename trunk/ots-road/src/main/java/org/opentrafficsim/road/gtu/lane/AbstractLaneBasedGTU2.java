package org.opentrafficsim.road.gtu.lane;

import java.io.IOException;
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

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.logger.CategoryLogger;
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
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import traceverifier.TraceVerifier;

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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1408 $, $LastChangedDate: 2015-09-24 15:17:25 +0200 (Thu, 24 Sep 2015) $, by $Author: pknoppers $,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedGTU2 extends AbstractGTU implements LaneBasedGTU
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
    private DirectedLanePosition cachedReferencePosition = null;

    /** Pending leave triggers for each lane. */
    private SimEventInterface<SimTimeDoubleUnit> pendingLeaveTrigger;

    /** Pending enter triggers for each lane. */
    private SimEventInterface<SimTimeDoubleUnit> pendingEnterTrigger;

    /** Event to finalize lane change. */
    private SimEventInterface<SimTimeDoubleUnit> finalizeLaneChangeEvent;

    /** Sensor events. */
    private Set<SimEventInterface<SimTimeDoubleUnit>> sensorEvents = new LinkedHashSet<>();

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
     * @param gtuType GTUType; the type of GTU, e.g. TruckType, CarType, BusType
     * @param network OTSRoadNetwork; the network that the GTU is initially registered in
     * @throws GTUException when initial values are not correct
     */
    public AbstractLaneBasedGTU2(final String id, final GTUType gtuType, final OTSRoadNetwork network) throws GTUException
    {
        super(id, gtuType, network.getSimulator(), network);
        OTSSimulatorInterface simulator = network.getSimulator();
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
        List<DirectedLanePosition> inits = new ArrayList<>(); // need to sort them
        inits.addAll(initialLongitudinalPositions);
        Collections.sort(inits, new Comparator<DirectedLanePosition>()
        {
            @Override
            public int compare(final DirectedLanePosition o1, final DirectedLanePosition o2)
            {
                Length length1 =
                        o1.getGtuDirection().isPlus() ? o1.getPosition() : o1.getLane().getLength().minus(o1.getPosition());
                Length length2 =
                        o2.getGtuDirection().isPlus() ? o2.getPosition() : o2.getLane().getLength().minus(o2.getPosition());
                return length1.compareTo(length2);
            }
        });
        for (DirectedLanePosition directedLanePosition : inits)
        {
            List<Lane> lanes = new ArrayList<>();
            lanes.add(directedLanePosition.getLane());
            this.crossSections.add(new CrossSection(lanes, directedLanePosition.getGtuDirection())); // enter lane part 1
        }

        // init event
        DirectedLanePosition referencePosition = getReferencePosition();
        fireTimedEvent(LaneBasedGTU.LANEBASED_INIT_EVENT,
                new Object[] { getId(), new OTSPoint3D(initialLocation).doubleVector(PositionUnit.METER),
                        OTSPoint3D.direction(initialLocation, DirectionUnit.EAST_RADIAN), getLength(), getWidth(),
                        referencePosition.getLane().getParentLink().getId(), referencePosition.getLane().getId(),
                        referencePosition.getPosition(), referencePosition.getGtuDirection().name(), getGTUType().getId() },
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
    public synchronized void setParent(final GTU gtu) throws GTUException
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
     * @throws GTUException when initial values are not correct
     * @throws OTSGeometryException when the initial path is wrong
     */
    public void reinit(final Set<DirectedLanePosition> initialLongitudinalPositions)
            throws NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        init(getStrategicalPlanner(), initialLongitudinalPositions, Speed.ZERO);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection) throws GTUException
    {

        // from info
        DirectedLanePosition from = getReferencePosition();

        // obtain position on lane adjacent to reference lane and enter lanes upstream/downstream from there
        GTUDirectionality direction = getDirection(from.getLane());
        Set<Lane> adjLanes = from.getLane().accessibleAdjacentLanesPhysical(laneChangeDirection, getGTUType(), direction);
        Lane adjLane = adjLanes.iterator().next();
        Length position = adjLane.position(from.getLane().fraction(from.getPosition()));
        leaveAllLanes();
        enterLaneRecursive(new LaneDirection(adjLane, direction), position, 0);

        // stored positions no longer valid
        this.referencePositionTime = Double.NaN;
        this.cachedPositions.clear();

        // fire event
        this.fireTimedEvent(
                LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] { getId(), laneChangeDirection.name(),
                        from.getLane().getParentLink().getId(), from.getLane().getId(), from.getPosition() },
                getSimulator().getSimulatorTime());

    }

    /**
     * Enters lanes upstream and downstream of the new location after an instantaneous lane change.
     * @param lane LaneDirection; considered lane
     * @param position Length; position to add GTU at
     * @param dir int; below 0 for upstream, above 0 for downstream, 0 for both
     * @throws GTUException on exception
     */
    private void enterLaneRecursive(final LaneDirection lane, final Length position, final int dir) throws GTUException
    {
        List<Lane> lanes = new ArrayList<>();
        lanes.add(lane.getLane());
        int index = dir > 0 ? this.crossSections.size() : 0;
        this.crossSections.add(index, new CrossSection(lanes, lane.getDirection()));
        lane.getLane().addGTU(this, position);

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
                        upLane = upstream.keySet().iterator().next();
                    }
                    upDir = upstream.get(upLane);
                    LaneDirection next = new LaneDirection(upLane, upDir);
                    Length nextPos = upDir.isPlus() ? next.getLength().minus(before).minus(getRear().getDx())
                            : before.plus(getRear().getDx());
                    enterLaneRecursive(next, nextPos, -1);
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
                Length nextPos = next.getDirection().isPlus() ? passed.minus(getFront().getDx())
                        : next.getLength().minus(passed).plus(getFront().getDx());
                enterLaneRecursive(next, nextPos, 1);
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
    public synchronized void initLaneChange(final LateralDirectionality laneChangeDirection) throws GTUException
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
            Set<Lane> laneSet = lane.accessibleAdjacentLanesLegal(laneChangeDirection, getGTUType(), getDirection(lane));
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
                    f = crossSection.getDirection().isPlus() ? f : 1.0 - f;
                    addToLanes.put(adjacentLane, adjacentLane.getLength().times(f).si / adjacentLane.getLength().si);
                }
                resultingLanes.add(index, adjacentLane);
            }
            newLanes.add(new CrossSection(resultingLanes, crossSection.getDirection()));
        }
        Throw.when(numRegistered == 0, GTUException.class, "Gtu %s starting %s lane change, but no adjacent lane found.",
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
     * @throws GTUException if position or direction could not be obtained
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected synchronized void finalizeLaneChange(final LateralDirectionality laneChangeDirection) throws GTUException
    {
        List<CrossSection> newLanes = new ArrayList<>();
        Lane fromLane = null;
        Length fromPosition = null;
        GTUDirectionality fromDirection = null;
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
                    fromDirection = getDirection(lane);
                }
                lane.removeGTU(this, false, pos);
            }
            List<Lane> remainingLane = new ArrayList<>();
            remainingLane.add(crossSection.getLanes().get(1 - this.referenceLaneIndex));
            newLanes.add(new CrossSection(remainingLane, crossSection.getDirection()));
        }
        this.crossSections.clear();
        this.crossSections.addAll(newLanes);
        this.referenceLaneIndex = 0;

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

        // XXX: WRONG: this.fireTimedEvent(LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection, from},
        // XXX: WRONG: getSimulator().getSimulatorTime());
        this.fireTimedEvent(
                LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] { getId(), laneChangeDirection.name(),
                        from.getLane().getParentLink().getId(), from.getLane().getId(), from.getPosition() },
                getSimulator().getSimulatorTime());

        this.finalizeLaneChangeEvent = null;
    }

    /** {@inheritDoc} */
    @Override
    public void setFinalizeLaneChangeEvent(final SimEventInterface<SimTimeDoubleUnit> event)
    {
        this.finalizeLaneChangeEvent = event;
    }

    /** {@inheritDoc} */
    @Override
    public final synchronized GTUDirectionality getDirection(final Lane lane) throws GTUException
    {
        for (CrossSection crossSection : this.crossSections)
        {
            if (crossSection.getLanes().contains(lane))
            {
                return crossSection.getDirection();
            }
        }
        throw new GTUException("getDirection: GTU does not contain " + lane);
    }

    /** Verify that runs are totally predictable. */
    private static TraceVerifier traceVerifier = null;

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected synchronized boolean move(final DirectedPoint fromLocation)
            throws SimRuntimeException, GTUException, OperationalPlanException, NetworkException, ParameterException
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
            cancelAllEvents();

            // generate the next operational plan and carry it out
            // in case of an instantaneous lane change, fractionalLinkPositions will be accordingly adjusted to the new lane
            boolean error = super.move(fromLocation);
            if (error)
            {
                return error;
            }

            DirectedLanePosition dlp = getReferencePosition();

            scheduleEnterEvent();
            scheduleLeaveEvent();

            // sensors
            for (CrossSection crossSection : this.crossSections)
            {
                for (Lane lane : crossSection.getLanes())
                {
                    scheduleTriggers(lane, crossSection.getDirection());
                }
            }
            try
            {
                if (null == traceVerifier)
                {
                    traceVerifier = new TraceVerifier("c:/Temp/moveTrace.txt");
                }
                traceVerifier.sample(String.format("%s GTU %s", getSimulator().getSimulatorTime().toString(), getId()),
                        String.format("%s", this.getOperationalPlan().getEndLocation()));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            fireTimedEvent(LaneBasedGTU.LANEBASED_MOVE_EVENT,
                    new Object[] { getId(), new OTSPoint3D(fromLocation).doubleVector(PositionUnit.METER),
                            OTSPoint3D.direction(fromLocation, DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
                            getTurnIndicatorStatus().name(), getOdometer(), dlp.getLane().getParentLink().getId(),
                            dlp.getLane().getId(), dlp.getPosition(), dlp.getGtuDirection().name() },
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
                throw new GTUException(exception);
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
        for (SimEventInterface<SimTimeDoubleUnit> event : this.sensorEvents)
        {
            if (event.getAbsoluteExecutionTime().gt(getSimulator().getSimTime()))
            {
                getSimulator().cancelEvent(event);
            }
        }
        this.sensorEvents.clear();
    }

    /**
     * Checks whether the GTU will enter a next cross-section during the (remainder of) the tactical plan. Only one event will
     * be scheduled. Possible additional events are scheduled upon entering the cross-section.
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleEnterEvent() throws GTUException, OperationalPlanException, SimRuntimeException
    {
        CrossSection lastCrossSection = this.crossSections.get(this.crossSections.size() - 1);
        // heuristic to prevent geometric calculation if the next section is quite far away anyway
        Length remain = remainingEventDistance();
        Lane lane = lastCrossSection.getLanes().get(this.referenceLaneIndex);
        Length position = position(lane, getFront());
        boolean possiblyNearNextSection =
                lastCrossSection.getDirection().isPlus() ? lane.getLength().minus(position).lt(remain) : position.lt(remain);
        if (possiblyNearNextSection)
        {
            CrossSectionLink link = lastCrossSection.getLanes().get(0).getParentLink();
            OTSLine3D enterLine = lastCrossSection.getDirection().isPlus() ? link.getEndLine() : link.getStartLine();
            Time enterTime = timeAtLine(enterLine, getFront());
            if (enterTime != null)
            {
                if (enterTime.lt(getSimulator().getSimulatorTime()))
                {
                    System.err.println(
                            "Time travel? enterTime=" + enterTime + "; simulator time=" + getSimulator().getSimulatorTime());
                    enterTime = getSimulator().getSimulatorTime();
                }
                this.pendingEnterTrigger = getSimulator().scheduleEventAbs(enterTime, this, this, "enterCrossSection", null);
            }
        }
    }

    /**
     * Appends a new cross-section at the downstream end. Possibly schedules a next enter event.
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected synchronized void enterCrossSection() throws GTUException, OperationalPlanException, SimRuntimeException
    {
        CrossSection lastCrossSection = this.crossSections.get(this.crossSections.size() - 1);
        LaneDirection laneDirection =
                new LaneDirection(lastCrossSection.getLanes().get(this.referenceLaneIndex), lastCrossSection.getDirection());
        LaneDirection nextLaneDirection = laneDirection.getNextLaneDirection(this);
        if (nextLaneDirection == null)
        {
            forceLaneChangeFinalization();
            return;
        }
        double insertFraction = nextLaneDirection.getDirection().isPlus() ? 0.0 : 1.0;
        List<Lane> nextLanes = new ArrayList<>();
        for (int i = 0; i < lastCrossSection.getLanes().size(); i++)
        {
            if (i == this.referenceLaneIndex)
            {
                nextLanes.add(nextLaneDirection.getLane());
            }
            else
            {
                Lane lane = lastCrossSection.getLanes().get(i);
                ImmutableMap<Lane, GTUDirectionality> lanes = lane.downstreamLanes(laneDirection.getDirection(), getGTUType());
                if (lanes.size() == 1)
                {
                    Lane nextLane = lanes.keySet().iterator().next();
                    nextLanes.add(nextLane);
                }
                else
                {
                    boolean added = false;
                    for (Lane nextLane : lanes.keySet())
                    {
                        if (nextLane.getParentLink().equals(nextLaneDirection.getLane().getParentLink())
                                && nextLane.accessibleAdjacentLanesPhysical(
                                        this.referenceLaneIndex == 0 ? LateralDirectionality.LEFT : LateralDirectionality.RIGHT,
                                        getGTUType(), nextLaneDirection.getDirection()).contains(nextLaneDirection.getLane()))
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
        this.crossSections.add(new CrossSection(nextLanes, nextLaneDirection.getDirection()));
        for (Lane lane : nextLanes)
        {
            lane.addGTU(this, insertFraction);
        }
        this.pendingEnterTrigger = null;
        scheduleEnterEvent();
        for (Lane lane : nextLanes)
        {
            scheduleTriggers(lane, nextLaneDirection.getDirection());
        }
    }

    /**
     * Helper method for {@code enterCrossSection}. In some cases the GTU should first finalize the lane change. This method
     * checks whether such an event is scheduled, and performs it. This method then re-attempts to enter the cross-section. So
     * the calling method should return after calling this.
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    private void forceLaneChangeFinalization() throws GTUException, OperationalPlanException, SimRuntimeException
    {
        if (this.finalizeLaneChangeEvent != null)
        {
            // a lane change should be finalized at this time, but the event is later in the queue, force it now
            SimEventInterface<SimTimeDoubleUnit> tmp = this.finalizeLaneChangeEvent;
            finalizeLaneChange(this.referenceLaneIndex == 0 ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT);
            getSimulator().cancelEvent(tmp);
            enterCrossSection();
        }
        // or a sink sensor should delete us
    }

    /**
     * Checks whether the GTU will leave a cross-section during the (remainder of) the tactical plan. Only one event will be
     * scheduled. Possible additional events are scheduled upon leaving the cross-section.
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleLeaveEvent() throws GTUException, OperationalPlanException, SimRuntimeException
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
            possiblyNearNextSection = firstCrossSection.getDirection().isPlus() ? lane.getLength().minus(position).lt(remain)
                    : position.lt(remain);
        }
        if (possiblyNearNextSection)
        {
            CrossSectionLink link = firstCrossSection.getLanes().get(0).getParentLink();
            OTSLine3D leaveLine = firstCrossSection.getDirection().isPlus() ? link.getEndLine() : link.getStartLine();
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
                if (leaveTime.lt(getSimulator().getSimulatorTime()))
                {
                    System.err.println(
                            "Time travel? leaveTime=" + leaveTime + "; simulator time=" + getSimulator().getSimulatorTime());
                    leaveTime = getSimulator().getSimulatorTime();
                }
                this.pendingLeaveTrigger = getSimulator().scheduleEventAbs(leaveTime, this, this, "leaveCrossSection", null);
            }
        }
    }

    /**
     * Removes registration between the GTU and the lanes in the most upstream cross-section. Possibly schedules a next leave
     * event.
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected synchronized void leaveCrossSection() throws GTUException, OperationalPlanException, SimRuntimeException
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
     * @param direction GTUDirectionality; direction
     * @throws GTUException exception
     * @throws OperationalPlanException exception
     * @throws SimRuntimeException exception
     */
    protected void scheduleTriggers(final Lane lane, final GTUDirectionality direction)
            throws GTUException, OperationalPlanException, SimRuntimeException
    {
        double min;
        double max;
        Length remain = remainingEventDistance();
        if (direction.isPlus())
        {
            min = position(lane, getRear()).si;
            max = min + remain.si + getLength().si;
        }
        else
        {
            max = position(lane, getRear()).si;
            min = max - remain.si - getLength().si;
        }
        SortedMap<Double, List<SingleSensor>> sensors = lane.getSensorMap(getGTUType(), direction).subMap(min, max);
        for (List<SingleSensor> list : sensors.values())
        {
            for (SingleSensor sensor : list)
            {
                RelativePosition pos = this.getRelativePositions().get(sensor.getPositionType());
                Time time = timeAtLine(sensor.getGeometry(), pos);
                if (time != null)
                {
                    this.sensorEvents
                            .add(getSimulator().scheduleEventAbs(time, this, sensor, "trigger", new Object[] { this }));
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
            return plan.getTotalLength().minus(plan.getTraveledDistance(getSimulator().getSimulatorTime())).plus(eventMargin);
        }
        return getOperationalPlan().getTotalLength().plus(eventMargin);
    }

    /**
     * Returns an estimation of when the relative position will reach the line. Returns {@code null} if this does not occur
     * during the current operational plan.
     * @param line OTSLine3D; line, i.e. lateral line at link start or lateral entrance of sensor
     * @param relativePosition RelativePosition; position to cross the line
     * @return estimation of when the relative position will reach the line, {@code null} if this does not occur during the
     *         current operational plan
     * @throws GTUException position error
     */
    private Time timeAtLine(final OTSLine3D line, final RelativePosition relativePosition) throws GTUException
    {
        Throw.when(line.size() != 2, IllegalArgumentException.class, "Line to cross with path should have 2 points.");
        OTSLine3D path = getOperationalPlan().getPath();
        OTSPoint3D[] points;
        double adjust;
        if (relativePosition.getDx().gt0())
        {
            // as the position is downstream of the reference, we need to attach some distance at the end
            points = new OTSPoint3D[path.size() + 1];
            System.arraycopy(path.getPoints(), 0, points, 0, path.size());
            points[path.size()] = new OTSPoint3D(path.getLocationExtendedSI(path.getLengthSI() + relativePosition.getDx().si));
            adjust = -relativePosition.getDx().si;
        }
        else if (relativePosition.getDx().lt0())
        {
            points = new OTSPoint3D[path.size() + 1];
            System.arraycopy(path.getPoints(), 0, points, 1, path.size());
            points[0] = new OTSPoint3D(path.getLocationExtendedSI(relativePosition.getDx().si));
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
            OTSPoint3D intersect;
            try
            {
                intersect = OTSPoint3D.intersectionOfLineSegments(points[i], points[i + 1], line.get(0), line.get(1));
            }
            catch (OTSGeometryException exception)
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
                    return getSimulator().getSimulatorTime();
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
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GTUException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GTUException
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
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime());
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
        synchronized (this)
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
                    Throw.when(lateralIndex == -1, GTUException.class, "GTU %s is not on lane %s.", this, lane);

                    OperationalPlan plan = getOperationalPlan(when);
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
                                f = whenCrossSections.get(i).getDirection() == GTUDirectionality.DIR_PLUS ? 1 - f : f;
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
                                    f = whenCrossSections.get(i).getDirection() == GTUDirectionality.DIR_PLUS ? f : 1 - f;
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
                    throw new GTUException(e);
                }

                Length length = Length.instantiateSI(loc);
                if (CACHING)
                {
                    this.cachedPositions.put(cacheIndex, length);
                }
                return length;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public DirectedLanePosition getReferencePosition() throws GTUException
    {
        synchronized (this)
        {
            if (this.referencePositionTime == getSimulator().getSimulatorTime().si)
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
                this.cachedReferencePosition =
                        new DirectedLanePosition(refLane, position(refLane, getReference()), this.getDirection(refLane));
                this.referencePositionTime = getSimulator().getSimulatorTime().si;
                return this.cachedReferencePosition;
            }
            throw new GTUException(
                    "The reference point of GTU " + this + " is not on any of the lanes on which it is registered");
        }
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
                    catch (GTUException exception)
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
            fireTimedEvent(LaneBasedGTU.LANEBASED_DESTROY_EVENT,
                    new Object[] { getId(), new OTSPoint3D(location).doubleVector(PositionUnit.METER),
                            OTSPoint3D.direction(location, DirectionUnit.EAST_RADIAN), getOdometer(),
                            referenceLane.getParentLink().getId(), referenceLane.getId(), dlp.getPosition(),
                            dlp.getGtuDirection().name() },
                    getSimulator().getSimulatorTime());
        }
        else
        {
            fireTimedEvent(LaneBasedGTU.LANEBASED_DESTROY_EVENT,
                    new Object[] { getId(), location, getOdometer(), null, Length.ZERO, null },
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
        synchronized (this)
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
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getCarFollowingAcceleration()
    {
        synchronized (this)
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
    public Length getLateralPosition(final Lane lane) throws GTUException
    {
        OperationalPlan plan = getOperationalPlan();
        if (plan instanceof LaneBasedOperationalPlan && !((LaneBasedOperationalPlan) plan).isDeviative())
        {
            return Length.ZERO;
        }
        DirectedLanePosition ref = getReferencePosition();
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
        Throw.when(latIndex == -1 || longIndex == -1, GTUException.class, "GTU %s is not on %s", getId(), lane);
        Lane refCrossSectionLane = this.crossSections.get(longIndex).getLanes().get(latIndex);
        DirectedPoint loc = getLocation();
        double f = refCrossSectionLane.getCenterLine().projectOrthogonal(loc.x, loc.y);
        DirectedPoint p = Try.assign(() -> refCrossSectionLane.getCenterLine().getLocationFraction(f), GTUException.class,
                "GTU %s is not orthogonal to the reference lane.", getId());
        double d = p.distance(loc);
        d = ref.getGtuDirection().isPlus() ? d : -d;
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

        /** GTU directionality. */
        private final GTUDirectionality direction;

        /**
         * @param lanes List&lt;Lane&gt;; lanes
         * @param direction GTUDirectionality; GTU directionality
         */
        protected CrossSection(final List<Lane> lanes, final GTUDirectionality direction)
        {
            this.lanes = lanes;
            this.direction = direction;
        }

        /**
         * @return lanes.
         */
        protected List<Lane> getLanes()
        {
            return this.lanes;
        }

        /**
         * @return direction.
         */
        protected GTUDirectionality getDirection()
        {
            return this.direction;
        }

    }

}
