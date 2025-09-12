package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.PositionUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.PositionVector;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.logger.CategoryLogger;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.geometry.OtsLine2d.FractionalFallback;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;
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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneBasedGtu extends Gtu implements LaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * Margin to add to plan length to check if the path will enter the next section. This is because the plan might follow a
     * shorter path than the lane center line.
     */
    private static final Length EVENT_MARGIN = Length.instantiateSI(50.0);

    /** Lane. */
    private final HistoricalValue<Lane> lane;

    /** Time of reference position cache. */
    private Time cachedPositionTime = null;

    /** Cached reference position. */
    private LanePosition cachedPosition = null;

    /** Time of roaming position cache. */
    private Time cachedRoamingPositionTime = null;

    /** Cached roaming position. */
    private LanePosition cachedRoamingPosition = null;

    /** Lanes for which enter events are scheduled. */
    private NavigableMap<Time, Lane> pendingLanesToEnter = new TreeMap<>();

    /** Pending enter events. */
    private Map<Lane, SimEventInterface<Duration>> pendingEnterEvents = new LinkedHashMap<>();

    /** Event to leave lane and start roaming. */
    private SimEventInterface<Duration> roamEvent;

    /** Detector triggers (detector and odometer at trigger time). */
    private Map<LaneDetector, Length> detectorTriggers = new LinkedHashMap<>();

    /** Detector events. */
    private Set<SimEventInterface<Duration>> detectorEvents = new LinkedHashSet<>();

    /** Cached desired speed. */
    private Speed cachedDesiredSpeed;

    /** Time desired speed was cached. */
    private Time desiredSpeedTime;

    /** Cached car-following acceleration. */
    private Acceleration cachedCarFollowingAcceleration;

    /** Time car-following acceleration was cached. */
    private Time carFollowingAccelerationTime;

    /** Turn indicator status. */
    private final Historical<TurnIndicatorStatus> turnIndicatorStatus;

    /** Vehicle model. */
    private VehicleModel vehicleModel = VehicleModel.MINMAX;

    /** Lane bookkeeping. */
    private LaneBookkeeping bookkeeping = LaneBookkeeping.EDGE;

    /** Distance over which the GTU should not change lane after being created. */
    private Length noLaneChangeDistance;

    /** Lane change direction. */
    private final Historical<LateralDirectionality> laneChangeDirection;

    /**
     * The lane-based event type for pub/sub indicating a move.<br>
     * Payload: [String gtuId, PositionVector currentPosition, Direction currentDirection, Speed speed, Acceleration
     * acceleration, TurnIndicatorStatus turnIndicatorStatus, Length odometer, String linkId, String laneId, Length
     * positionOnLane]
     */
    public static final EventType LANEBASED_MOVE_EVENT = new EventType("LANEBASEDGTU.MOVE", new MetaData("Lane based GTU moved",
            "Lane based GTU moved",
            new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Position", "Position", PositionVector.class),
                    new ObjectDescriptor("Direction", "Direction", Direction.class),
                    new ObjectDescriptor("Speed", "Speed", Speed.class),
                    new ObjectDescriptor("Acceleration", "Acceleration", Acceleration.class),
                    new ObjectDescriptor("TurnIndicatorStatus", "Turn indicator status", String.class),
                    new ObjectDescriptor("Odometer", "Odometer value", Length.class),
                    new ObjectDescriptor("Link id", "Link id", String.class),
                    new ObjectDescriptor("Lane id", "Lane id", String.class),
                    new ObjectDescriptor("Longitudinal position on lane", "Longitudinal position on lane", Length.class)}));

    /**
     * The lane-based event type for pub/sub indicating destruction of the GTU.<br>
     * Payload: [String gtuId, PositionVector finalPosition, Direction finalDirection, Length finalOdometer, String linkId,
     * String laneId, Length positionOnLane]
     */
    public static final EventType LANEBASED_DESTROY_EVENT = new EventType("LANEBASEDGTU.DESTROY", new MetaData(
            "Lane based GTU destroyed", "Lane based GTU destroyed",
            new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Position", "Position", PositionVector.class),
                    new ObjectDescriptor("Direction", "Direction", Direction.class),
                    new ObjectDescriptor("Odometer", "Odometer value", Length.class),
                    new ObjectDescriptor("Link id", "Link id", String.class),
                    new ObjectDescriptor("Lane id", "Lane id", String.class),
                    new ObjectDescriptor("Longitudinal position on lane", "Longitudinal position on lane", Length.class)}));

    /**
     * The event type for pub/sub indicating that the GTU entered a lane in either the lateral or longitudinal direction.<br>
     * Payload: [String gtuId, String link id, String lane id]
     */
    public static final EventType LANE_ENTER_EVENT = new EventType("LANE.ENTER",
            new MetaData("Lane based GTU entered lane", "Front of lane based GTU entered lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class)}));

    /**
     * The event type for pub/sub indicating that the GTU exited a lane in either the lateral or longitudinal direction.<br>
     * Payload: [String gtuId, String link id, String lane id]
     */
    public static final EventType LANE_EXIT_EVENT = new EventType("LANE.EXIT",
            new MetaData("Lane based GTU exited lane", "Rear of lane based GTU exited lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class)}));

    /**
     * The event type for pub/sub indicating that the GTU changed lane, laterally only.<br>
     * Payload: [String gtuId, LateralDirectionality direction, String linkId, String fromLaneId, Length position]
     */
    public static final EventType LANE_CHANGE_EVENT = new EventType("LANE.CHANGE",
            new MetaData("Lane based GTU changes lane", "Lane based GTU changes lane",
                    new ObjectDescriptor[] {new ObjectDescriptor("GTU id", "GTU id", String.class),
                            new ObjectDescriptor("Lateral direction of lane change", "Lateral direction of lane change",
                                    String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id of exited lane", "Lane id of exited lane", String.class),
                            new ObjectDescriptor("Position along exited lane", "Position along exited lane", Length.class)}));

    /**
     * Construct a Lane Based GTU.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param length the maximum length of the GTU (parallel with driving direction)
     * @param width the maximum width of the GTU (perpendicular to driving direction)
     * @param maximumSpeed the maximum speed of the GTU (in the driving direction)
     * @param front front distance relative to the reference position
     * @param network the network that the GTU is initially registered in
     * @throws GtuException when initial values are not correct
     */
    public LaneBasedGtu(final String id, final GtuType gtuType, final Length length, final Length width,
            final Speed maximumSpeed, final Length front, final RoadNetwork network) throws GtuException
    {
        super(id, gtuType, network.getSimulator(), network, length, width, front, maximumSpeed);
        HistoryManager historyManager = network.getSimulator().getReplication().getHistoryManager(network.getSimulator());
        this.lane = new HistoricalValue<>(historyManager, this);
        this.turnIndicatorStatus = new HistoricalValue<>(historyManager, this, TurnIndicatorStatus.NOTPRESENT);
        this.laneChangeDirection = new HistoricalValue<>(historyManager, this, LateralDirectionality.NONE);
    }

    /**
     * Initializes the GTU.
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param initialLocation initial location
     * @param initialSpeed the initial speed of the car on the lane
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GtuException when initial values are not correct
     */
    @SuppressWarnings("checkstyle:designforextension")
    public synchronized void init(final LaneBasedStrategicalPlanner strategicalPlanner, final DirectedPoint2d initialLocation,
            final Speed initialSpeed) throws NetworkException, SimRuntimeException, GtuException
    {
        Throw.when(null == initialLocation, GtuException.class, "InitialLongitudinalPositions is null");

        // TODO: move this to super.init(...), and remove setOperationalPlan(...) method
        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move and events will work
        Time now = getSimulator().getSimulatorAbsTime();
        if (initialSpeed.lt(OperationalPlan.DRIFTING_SPEED))
        {
            setOperationalPlan(OperationalPlan.standStill(this, initialLocation, now, Duration.instantiateSI(1E-6)));
        }
        else
        {
            Point2d p2 = new Point2d(initialLocation.x + 1E-6 * Math.cos(initialLocation.getDirZ()),
                    initialLocation.y + 1E-6 * Math.sin(initialLocation.getDirZ()));
            OtsLine2d path = new OtsLine2d(initialLocation, p2);
            setOperationalPlan(new OperationalPlan(this, path, now,
                    Segments.off(initialSpeed, path.getTypedLength().divide(initialSpeed), Acceleration.ZERO)));
        }

        LanePosition longitudinalPosition = getRoamingPosition(initialLocation);
        endRoaming(longitudinalPosition); // enters lane if sufficiently close
        this.cachedPositionTime = null; // endRoaming() -> enterLane() -> getPosition() caches cachedPosition = null

        // initiate the actual move
        super.init(strategicalPlanner, initialLocation, initialSpeed);

        this.cachedPositionTime = null; // remove cache, it may be invalid as the above init results in a lane change
    }

    /**
     * {@inheritDoc} The lane the GTU is on will be left.
     */
    @Override
    public synchronized void setParent(final Gtu gtu) throws GtuException
    {
        exitLane();
        super.setParent(gtu);
    }

    /**
     * Removes the registration between this GTU and the lane.
     */
    protected synchronized void exitLane()
    {
        LanePosition exitLanePosition = getPosition();
        if (exitLanePosition != null)
        {
            exitLanePosition.lane().removeGtu(this, true, exitLanePosition.position());
        }
        this.lane.set(null);
        fireTimedEvent(LaneBasedGtu.LANE_EXIT_EVENT,
                new Object[] {getId(), exitLanePosition.lane().getLink().getId(), exitLanePosition.lane().getId()},
                getSimulator().getSimulatorTime());
    }

    /**
     * Enters a new lane, and removes the GTU from the previous lane.
     * @param lane lane
     * @param fraction fractional position
     */
    @SuppressWarnings("hiddenfield")
    protected synchronized void enterLane(final Lane lane, final double fraction)
    {
        // The reason this method does not use exitLane() is that we do not want to set the lane to null in the historical.
        LanePosition exitLanePosition = getPosition();
        this.lane.set(lane);
        Try.execute(() -> lane.addGtu(this, fraction), "Entering lane where the GTU is already at.");

        fireTimedEvent(LaneBasedGtu.LANE_ENTER_EVENT, new Object[] {getId(), lane.getLink().getId(), lane.getId()},
                getSimulator().getSimulatorTime());

        // First enter, then exit, as e.g. TrafficLightDetector checks whether a collection is empty to trigger an event that
        // the detector is empty. However, the GTU might have entered the next lane where the detector continues.
        if (exitLanePosition != null)
        {
            exitLanePosition.lane().removeGtu(this, true, exitLanePosition.position());
            this.pendingLanesToEnter.values().remove(lane);
            this.pendingEnterEvents.remove(lane);
            fireTimedEvent(LaneBasedGtu.LANE_EXIT_EVENT,
                    new Object[] {getId(), exitLanePosition.lane().getLink().getId(), exitLanePosition.lane().getId()},
                    getSimulator().getSimulatorTime());
            if (exitLanePosition.lane().getLink().equals(lane.getLink()))
            {
                // Same link, so must be a lane change
                setLaneChangeDirection(LateralDirectionality.NONE);
                String direction = lane.equals(exitLanePosition.lane().getLeft(getType())) ? LateralDirectionality.LEFT.name()
                        : LateralDirectionality.RIGHT.name();
                fireTimedEvent(LaneBasedGtu.LANE_CHANGE_EVENT,
                        new Object[] {getId(), direction, exitLanePosition.lane().getLink().getId(),
                                exitLanePosition.lane().getId(), exitLanePosition.position()},
                        getSimulator().getSimulatorTime());
            }
        }
    }

    @Override
    public synchronized Lane getLane()
    {
        return this.lane.get();
    }

    /**
     * Returns the lane at the given time. This may be in the future during the plan, in which case it is a prospective lane.
     * @param when time to get the lane for
     * @return lane at given time
     */
    public synchronized Lane getLane(final Time when)
    {
        return this.pendingLanesToEnter.isEmpty() || this.pendingLanesToEnter.firstKey().ge(when) ? this.lane.get(when)
                : this.pendingLanesToEnter.floorEntry(when).getValue();
    }

    /**
     * Returns the lane and reference position on the lane of the GTU.
     * @return lane position at time, or {@code null} if the GTU is not at a lane
     */
    public synchronized LanePosition getPosition()
    {
        if (!getSimulator().getSimulatorAbsTime().equals(this.cachedPositionTime))
        {
            this.cachedPositionTime = getSimulator().getSimulatorAbsTime();
            this.cachedPosition = getPosition(getReference(), this.cachedPositionTime);
        }
        return this.cachedPosition;
    }

    /**
     * Returns the lane and reference position on the lane of the GTU.
     * @param when time to get the position for
     * @return lane position at time, or {@code null} if the GTU is not at a lane
     */
    public synchronized LanePosition getPosition(final Time when)
    {
        return getPosition(getReference(), when);
    }

    /**
     * Returns the lane and relative position on the lane of the GTU. The relative position is calculated by shifting the
     * position of the reference by {@code dx} of the relative position.
     * @param relativePosition relative position
     * @return lane position, or {@code null} if the GTU is not at a lane
     */
    public synchronized LanePosition getPosition(final RelativePosition relativePosition)
    {
        LanePosition ref = getPosition();
        return new LanePosition(ref.lane(), ref.position().plus(relativePosition.dx()));
    }

    /**
     * Returns the lane and relative position on the lane of the GTU. The relative position is calculated by shifting the
     * position of the reference by {@code dx} of the relative position.
     * @param relativePosition relative position
     * @param when time to get the position for
     * @return lane position at time, or {@code null} if the GTU is not at a lane
     */
    public synchronized LanePosition getPosition(final RelativePosition relativePosition, final Time when)
    {
        Lane laneAtTime = getLane(when);
        if (laneAtTime == null)
        {
            return null;
        }
        return new LanePosition(laneAtTime, getPosition(laneAtTime, relativePosition, when));
    }

    /**
     * Returns the projected position of the GTU on the given lane, which should be on the same link.
     * @param lane lane
     * @return projected position of the GTU on the given lane
     * @throws IllegalStateException when the GTU is not on a lane
     * @throws IllegalArgumentException when the lane is not in the link the GTU is on
     */
    @SuppressWarnings("hiddenfield")
    public synchronized Length getPosition(final Lane lane)
    {
        return getPosition(lane, getReference(), getSimulator().getSimulatorAbsTime());
    }

    /**
     * Returns the projected position of the GTU on the given lane, which should be on the same link.
     * @param lane lane
     * @param when time
     * @return projected position of the GTU on the given lane
     * @throws IllegalStateException when the GTU is not on a lane
     * @throws IllegalArgumentException when the lane is not in the link the GTU is on
     */
    @SuppressWarnings("hiddenfield")
    public synchronized Length getPosition(final Lane lane, final Time when)
    {
        return getPosition(lane, getReference(), when);
    }

    /**
     * Returns the projected position of the GTU on the given lane, which should be on the same link. The relative position is
     * calculated by shifting the position of the reference by {@code dx} of the relative position.
     * @param lane lane
     * @param relativePosition relative position
     * @return projected position of the GTU on the given lane
     * @throws IllegalStateException when the GTU is not on a lane
     * @throws IllegalArgumentException when the lane is not in the link the GTU is on
     */
    @SuppressWarnings("hiddenfield")
    public synchronized Length getPosition(final Lane lane, final RelativePosition relativePosition)
    {
        return getPosition(lane, relativePosition, getSimulator().getSimulatorAbsTime());
    }

    @Override
    public synchronized Length getLongitudinalPosition()
    {
        return getPosition().position();
    }

    /**
     * Returns the projected position of the GTU on the given lane, which should be on the same link. The relative position is
     * calculated by shifting the position of the reference by {@code dx} of the relative position.
     * @param lane lane
     * @param relativePosition relative position
     * @param when time
     * @return projected position of the GTU on the given lane
     * @throws IllegalStateException when the GTU is not on a lane
     * @throws IllegalArgumentException when the lane is not in the link the GTU is on
     */
    @SuppressWarnings("hiddenfield")
    public synchronized Length getPosition(final Lane lane, final RelativePosition relativePosition, final Time when)
    {
        Throw.when(getLane() == null, IllegalStateException.class, "Requesting position on lane but GTU has no lane.");
        Throw.when(!lane.getLink().equals(getLane(when).getLink()), IllegalArgumentException.class,
                "Requesting position on lane on link %s but the GTU is on link %s.", lane.getLink().getId(),
                getLane().getLink().getId());
        DirectedPoint2d p = Try.assign(() -> getOperationalPlan(when).getLocation(when, getReference()),
                "Operational plan at time is not valid at time.");
        double f = lane.getCenterLine().projectFractional(lane.getLink().getStartNode().getHeading(),
                lane.getLink().getEndNode().getHeading(), p.x, p.y, FractionalFallback.ORTHOGONAL_EXTENDED);
        return lane.getLength().times(f).plus(relativePosition.dx());
    }

    /**
     * Returns the nearest lane position on the route / network. It is not strictly guaranteed that the position is closest, as
     * this method will only search on links where either of the nodes is the closest node.
     * @return nearest lane position on the route / network
     */
    public synchronized LanePosition getRoamingPosition()
    {
        Throw.when(getLane() != null, IllegalStateException.class, "GTU that is on a lane does not have a roaming position.");
        if (!getSimulator().getSimulatorAbsTime().equals(this.cachedRoamingPositionTime))
        {
            this.cachedRoamingPositionTime = getSimulator().getSimulatorAbsTime();
            this.cachedRoamingPosition = getRoamingPosition(getLocation());
        }
        return this.cachedRoamingPosition;
    }

    /**
     * Returns the nearest lane position on the route / network. It is not strictly guaranteed that the position is closest, as
     * this method will only search on links where either of the nodes is the closest node.
     * @param location location to find the nearest lane position for
     * @return nearest lane position on the route / network
     */
    protected LanePosition getRoamingPosition(final Point2d location)
    {
        Route route = getStrategicalPlanner() == null ? null : getStrategicalPlanner().getRoute();
        // TODO instead of getNetwork().getNodeMap().values(), using spatial tree would be a good alternative
        // perhaps even a findClosest() method.
        Iterable<Node> nodes =
                route == null ? getNetwork().getNodeMap().values() : getStrategicalPlanner().getRoute().getNodes();
        List<CrossSectionLink> nearestLinks = new ArrayList<>(2);
        double minDist = Double.POSITIVE_INFINITY;
        for (Node node : nodes)
        {
            double dist = node.getPoint().distance(location);
            if (dist < minDist)
            {
                nearestLinks.clear();
                for (Link link : node.getLinks())
                {
                    if (link instanceof CrossSectionLink cLink && (route == null || route.containsLink(link)))
                    {
                        nearestLinks.add(cLink);
                        minDist = dist;
                    }
                }
            }
        }
        Throw.when(nearestLinks.isEmpty(), IllegalStateException.class, "No lane in the route or in the network.");
        LanePosition roamingPosition = null;
        minDist = Double.POSITIVE_INFINITY;
        for (CrossSectionLink nearestLink : nearestLinks)
        {
            for (Lane checkLane : nearestLink.getLanesAndShoulders())
            {
                double fraction = checkLane.getCenterLine().projectOrthogonalSnap(location.x, location.y);
                DirectedPoint2d point = checkLane.getCenterLine().getLocationFraction(fraction);
                double dist = point.distance(location);
                if (dist < minDist)
                {
                    roamingPosition =
                            new LanePosition(checkLane, Length.instantiateSI(checkLane.getCenterLine().getLength() * fraction));
                    minDist = dist;
                }
            }
        }
        return roamingPosition;
    }

    /**
     * Deviation from lane center. Positive values are left, negative values are right.
     * @return deviation from lane center line, positive values are left, negative values are right
     */
    public synchronized Length getDeviation()
    {
        return getDeviation(getLane(), getLocation());
    }

    /**
     * Deviation from lane center at time. Positive values are left, negative values are right.
     * @param when time
     * @return deviation from lane center line, positive values are left, negative values are right
     */
    public synchronized Length getDeviation(final Time when)
    {
        return getDeviation(getLane(when), getLocation(when));
    }

    /**
     * Returns the deviation from the center line of the given lane, using extension if the GTU is not on the lane. Positive
     * values are left, negative values are right.
     * @param lane lane
     * @param location location
     * @return deviation from lane center line, positive values are left, negative values are right
     */
    @SuppressWarnings("hiddenfield")
    protected Length getDeviation(final Lane lane, final Point2d location)
    {
        double fraction = lane.getCenterLine().projectFractional(lane.getLink().getStartNode().getHeading(),
                lane.getLink().getEndNode().getHeading(), location.x, location.y, FractionalFallback.ORTHOGONAL_EXTENDED);
        DirectedPoint2d a = lane.getCenterLine().getLocationFractionExtended(fraction);
        Point2d b = new Point2d(a.x + Math.cos(a.dirZ), a.y + Math.sin(a.dirZ));
        double sign = (b.x - a.x) * (location.y - a.y) - (b.y - a.y) * (location.x - a.x) > 0.0 ? 1.0 : -1.0;
        return Length.instantiateSI(sign * lane.getCenterLine().getLocationFractionExtended(fraction).distance(location));
    }

    /**
     * Change lanes instantaneously.
     * @param laneChangeDirection the direction to change to
     */
    @SuppressWarnings("hiddenfield")
    public synchronized void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection)
    {
        LanePosition from = getPosition();
        Set<Lane> adjLanes = from.lane().accessibleAdjacentLanesPhysical(laneChangeDirection, getType());
        Lane adjLane = adjLanes.iterator().next();
        Length position = getPosition(adjLane);
        cancelAllEvents();
        enterLane(adjLane, position.si / adjLane.getLength().si);
        this.cachedPositionTime = null;
        this.cachedPosition = null;

        // fire event
        this.fireTimedEvent(
                LaneBasedGtu.LANE_CHANGE_EVENT, new Object[] {getId(), laneChangeDirection.name(),
                        from.lane().getLink().getId(), from.lane().getId(), from.position()},
                getSimulator().getSimulatorTime());
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected synchronized boolean move(final DirectedPoint2d fromLocation)
            throws SimRuntimeException, GtuException, NetworkException, ParameterException
    {
        if (this.isDestroyed())
        {
            return false;
        }
        try
        {
            // cancel events, if any
            cancelAllEvents();

            // generate the next operational plan and carry it out
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
                destroy();
                cancelAllEvents();
                return true;
            }

            scheduleLaneEvents();
            findDetectorTriggers(true);

            LanePosition position = getPosition();
            fireTimedEvent(LaneBasedGtu.LANEBASED_MOVE_EVENT,
                    new Object[] {getId(),
                            new PositionVector(new double[] {fromLocation.x, fromLocation.y}, PositionUnit.METER),
                            new Direction(fromLocation.getDirZ(), DirectionUnit.EAST_RADIAN), getSpeed(), getAcceleration(),
                            getTurnIndicatorStatus().name(), getOdometer(), position.lane().getLink().getId(),
                            position.lane().getId(), position.position()},
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
    protected void cancelAllEvents()
    {
        if (this.roamEvent != null)
        {
            getSimulator().cancelEvent(this.roamEvent);
            this.roamEvent = null;
        }
        this.pendingLanesToEnter.clear();
        this.pendingEnterEvents.values().forEach((event) -> getSimulator().cancelEvent(event));
        this.pendingEnterEvents.clear();
        // we should clear all detector events as triggers that remain in this.detectorTriggers will be rescheduled in move
        this.detectorEvents.forEach((event) -> getSimulator().cancelEvent(event));
        this.detectorEvents.clear();
        findDetectorTriggers(false);
    }

    /**
     * Schedules when a lane is entered (and a previous one is left). Also schedules start of roaming (GTU not having a lane),
     * or ends roaming if the GTU is on a lane.
     */
    protected void scheduleLaneEvents()
    {
        /*
         * Implementation note: this method cannot use the getPosition() methods without lane input, as those depend on
         * this.pendingLanesToEnter which this method is responsible for filling.
         */
        Lane laneOnPath = getLane();
        if (laneOnPath == null)
        {
            // Check whether the GTU is on the network and stops roaming
            endRoaming(getRoamingPosition());
            laneOnPath = getLane();
        }
        // Add distance as plan path may be shorter than lane center line path
        Length remain = getOperationalPlan().getTotalLength().plus(EVENT_MARGIN);
        Length planStartPositionAtLaneOnPath = getLongitudinalPosition();
        boolean checkLaneChange = getOperationalPlan() instanceof LaneBasedOperationalPlan lbop && lbop.isDeviative()
                && !this.bookkeeping.equals(LaneBookkeeping.INSTANT);
        while (true)
        {
            Time enterTime;
            if (laneOnPath.getLength().minus(planStartPositionAtLaneOnPath).lt(remain))
            {
                CrossSectionLink link = laneOnPath.getLink();
                Link nextLink =
                        Try.assign(() -> getStrategicalPlanner().nextLink(link, getType()), "Network issue during scheduling.");
                PolyLine2d enterLine = nextLink != null && nextLink instanceof CrossSectionLink
                        ? ((CrossSectionLink) nextLink).getStartLine() : link.getEndLine();
                enterTime = timeAtLine(enterLine, getReference());
            }
            else
            {
                enterTime = null;
            }
            Time lastTimeOnLane = enterTime == null ? getOperationalPlan().getEndTime() : enterTime;

            // Check whether a lane is entered laterally before longitudinally
            if (checkLaneChange && (enterTime == null || !Double.isNaN(enterTime.si)))
            {
                Time firstTimeOnLane = this.pendingLanesToEnter.isEmpty() ? getSimulator().getSimulatorAbsTime()
                        : this.pendingLanesToEnter.lastKey();
                Length overshoot = laneLateralOvershoot(lastTimeOnLane);
                if (overshoot.gt0() && laneLateralOvershoot(firstTimeOnLane).le0())
                {
                    Length deviation = getDeviation(lastTimeOnLane);
                    boolean noAdjacentLane =
                            (deviation.gt0() ? laneOnPath.getLeft(getType()) : laneOnPath.getRight(getType())) == null;
                    boolean willRoam = noAdjacentLane && overshoot.gt(getWidth().times(0.5));

                    Time lateralCrossingTime = getTimeOfLateralCrossing(firstTimeOnLane, lastTimeOnLane, willRoam);
                    if (willRoam)
                    {
                        this.roamEvent = getSimulator().scheduleEventAbs(Duration.instantiateSI(lateralCrossingTime.si),
                                () -> exitLane());
                        return; // no further lanes to check when roaming
                    }
                    else
                    {
                        // Regular lane change
                        LateralDirectionality lcDirection = getDeviation(lateralCrossingTime).ge0() ? LateralDirectionality.LEFT
                                : LateralDirectionality.RIGHT;
                        Length distanceTillLaneChange =
                                getPosition(laneOnPath, lateralCrossingTime).minus(planStartPositionAtLaneOnPath);
                        laneOnPath = laneOnPath.getAdjacentLane(lcDirection, getType());
                        if (laneOnPath != null)
                        {
                            // no lane to change to, curve back or roam
                            Length positionOnTargetLane = getPosition(laneOnPath, lateralCrossingTime);
                            double fractionOnTargetLane = positionOnTargetLane.si / laneOnPath.getLength().si;
                            planStartPositionAtLaneOnPath = positionOnTargetLane.minus(distanceTillLaneChange);
                            this.pendingLanesToEnter.put(lateralCrossingTime, laneOnPath);
                            Lane finalLane = laneOnPath;
                            this.pendingEnterEvents.put(laneOnPath,
                                    getSimulator().scheduleEventAbs(Duration.instantiateSI(lateralCrossingTime.si),
                                            () -> enterLane(finalLane, fractionOnTargetLane)));
                        }
                    }
                }
            }

            if (enterTime != null)
            {
                planStartPositionAtLaneOnPath = planStartPositionAtLaneOnPath.minus(laneOnPath.getLength());
                laneOnPath = getNextLaneForRoute(laneOnPath);
                if (laneOnPath == null && !Double.isNaN(enterTime.si))
                {
                    // Check longitudinal roaming
                    Time timeRefLeaving = enterTime; // next link but no lane change, or determined at end of current link
                    Length distanceRearLeaving = Try.assign(() -> getOperationalPlan().getTraveledDistance(timeRefLeaving),
                            "Time link is left is beyond plan.").minus(getRear().dx());
                    if (distanceRearLeaving.le(getOperationalPlan().getTotalLength()))
                    {
                        Time timeRearLeaving = Try.assign(() -> getOperationalPlan().getTimeAtDistance(distanceRearLeaving),
                                "Distance till rear leaves link is beyond plan.");
                        this.roamEvent =
                                getSimulator().scheduleEventAbs(Duration.instantiateSI(timeRearLeaving.si), () -> exitLane());
                    }
                    return; // no further lanes to check
                }
                else
                {
                    if (Double.isNaN(enterTime.si))
                    {
                        // NaN indicates we just missed it between moves, due to curvature and small gaps
                        enterTime = getSimulator().getSimulatorAbsTime();
                        CategoryLogger.always().error("GTU {} enters lane through hack.", getId());
                    }
                    this.pendingLanesToEnter.put(enterTime, laneOnPath);
                    Lane finalLane = laneOnPath;
                    this.pendingEnterEvents.put(laneOnPath, getSimulator()
                            .scheduleEventAbs(Duration.instantiateSI(enterTime.si), () -> enterLane(finalLane, 0.0)));
                }
            }
            else
            {
                return; // no next link within plan, possible lane change on current link already checked
            }
        }
    }

    /**
     * Estimates when the path crosses a lateral lane boundary assuming the GTU is within the boundary. This is estimated
     * through linear interpolation between the start and end deviation values of a line segment of the path. The line segment
     * is found through a binary search.
     * @param fromTime first time to consider on the lane
     * @param toTime last time to consider on the lane
     * @param roam when {@code true} the full width of the GTU is considered, when {@code false} only the reference position
     * @return when the path crosses a lateral lane boundary
     */
    protected Time getTimeOfLateralCrossing(final Time fromTime, final Time toTime, final boolean roam)
    {
        try
        {
            Length startPosition = getOperationalPlan().getTraveledDistance(fromTime);
            Length endPosition = getOperationalPlan().getTraveledDistance(toTime);
            Length lateralMargin = roam ? getWidth().times(0.5) : Length.ZERO;
            OtsLine2d path = getOperationalPlan().getPath();
            int low = 0;
            while (path.size() > low + 1 && path.lengthAtIndex(low + 1) <= startPosition.si)
            {
                low++;
            }
            int high = path.size() - 1;
            while (high > 0 && path.lengthAtIndex(high - 1) > endPosition.si)
            {
                high--;
            }
            int mid = 0;
            Length position0 = null;
            Length overshoot0 = null;
            // based on Collections.indexedBinarySearch
            while (low <= high)
            {
                mid = (low + high) / 2;
                position0 = Length.max(startPosition, Length.min(Length.instantiateSI(path.lengthAtIndex(mid)), endPosition));
                Time time0 = getOperationalPlan().getTimeAtDistance(position0);
                overshoot0 = laneLateralOvershoot(time0).minus(lateralMargin);
                if (overshoot0.le0())
                {
                    low = mid + 1;
                }
                else
                {
                    high = mid - 1;
                }
            }
            if (mid == low)
            {
                position0 =
                        Length.max(startPosition, Length.min(Length.instantiateSI(path.lengthAtIndex(low - 1)), endPosition));
                Time time0 = getOperationalPlan().getTimeAtDistance(position0);
                overshoot0 = laneLateralOvershoot(time0).minus(lateralMargin);
            }
            Length position1 = Length.min(endPosition, Length.instantiateSI(path.lengthAtIndex(low)));
            Time time1 = getOperationalPlan().getTimeAtDistance(position1);
            Length overshoot1 = laneLateralOvershoot(time1);
            double factor = overshoot0.neg().si / (overshoot1.si - overshoot0.si);
            return getOperationalPlan().getTimeAtDistance(Length.interpolate(position0, position1, factor));
        }
        catch (OperationalPlanException ex)
        {
            throw new RuntimeException("Lateral crossing time or distance beyond plan.", ex);
        }
    }

    /**
     * Ends roaming if the roaming position is sufficiently close to enter the network.
     * @param roamingPosition roaming position
     */
    protected void endRoaming(final LanePosition roamingPosition)
    {
        if (roamingPosition.getLocation().distance(
                getLocation()) < roamingPosition.lane().getWidth(roamingPosition.getFraction()).plus(getWidth()).times(0.5).si)
        {
            enterLane(roamingPosition.lane(), roamingPosition.getFraction());
        }
    }

    /**
     * This method applies a detector finding algorithm that guarantees that detectors at the same location, triggered for
     * different relative positions, are all always triggered in combination. As detectors might be triggered by the front,
     * detectors beyond the current plan path may need to be triggered in the current plan duration. To achieve this, all
     * detectors are found between the path start position + dx, up to the path end position + dx, where dx is the distance the
     * front is before the reference point. Start and end position and dx are applied along the lane center lines. In case of a
     * lane change dx is also applied on both lanes, meaning that all detectors overlapping the vehicle on the from lane are
     * found, but at the target lane only detectors downstream of the front are found.<br>
     * <br>
     * This method stores all found detectors as detector triggers. This includes for each detector the odometer value of the
     * reference point at which the detector should be triggered. The odometer value is adjusted for the relative position that
     * should trigger the detector, along the lane center lines.<br>
     * <br>
     * Finally, this method schedules trigger events for all stored detector triggers when the reference point reaches the
     * relevant odometer value in the current plan. This may include detector triggers that were stored in a previous time step
     * as the front reached the detector, but no event was scheduled in a previous time step as the relevant relative position
     * of the detector, e.g. the rear, did not reach the detector.<br>
     * <br>
     * Alternatively when {@code schedule = false} this method finds all detector triggers downstream of the current front
     * location during the current plan using the same search algorithm, and removes them from the stored detector triggers. No
     * events will be scheduled (nor removed by this method). Removing detector triggers is relevant when a plan is cancelled.
     * Any downstream detectors may be found again and rescheduled depending on a new plan by a new move.
     * @param schedule {@code true} adds downstream triggers and schedules them, {@code false} removes downstream triggers
     */
    protected void findDetectorTriggers(final boolean schedule)
    {
        Lane laneOnPath = getLane();
        if (laneOnPath == null)
        {
            return;
        }

        // Find detectors reached with the nose in the current plan
        Time time0 = getSimulator().getSimulatorAbsTime();
        LanePosition position0 = getPosition();
        Length searchedDistanceAtFrom = Length.ZERO;
        while (time0.lt(getOperationalPlan().getEndTime()))
        {
            /*
             * This loop loops over the current lane and all future pending lanes (i.e. episodes). At these lanes detectors are
             * found. The relevant range [from ... to] on the lane is bounded by the start and end of the whole plan, and the
             * position of lane changes if that is the cause of a next pending lane.
             */
            Time time1 = this.pendingLanesToEnter.higherKey(time0) == null ? getOperationalPlan().getEndTime()
                    : this.pendingLanesToEnter.higherKey(time0);
            LanePosition position1 = getPosition(time1); // note: could be on adjacent lane due to a lane change
            searchedDistanceAtFrom = findDetectorTriggersInEpisode(searchedDistanceAtFrom, position0, position1, schedule);
            time0 = time1;
            position0 = position1;
        }

        // Schedule odometer values crossed in current plan
        if (schedule)
        {
            for (Entry<LaneDetector, Length> trigger : this.detectorTriggers.entrySet())
            {
                Length toDetector = trigger.getValue().minus(getOdometer());
                if (toDetector.le(getOperationalPlan().getTotalLength()))
                {
                    Time triggerTime = Try.assign(() -> getOperationalPlan().getTimeAtDistance(toDetector),
                            "Distance to detector beyond plan length.");
                    this.detectorEvents.add(getSimulator().scheduleEventAbs(Duration.instantiateSI(triggerTime.si),
                            () -> triggerDetector(trigger.getKey())));
                }
            }
        }
    }

    /**
     * Finds all detectors within an episode, i.e. one lane in the plan, possibly amended by downstream lanes not in the plan
     * but within reach of the nose (downstream of the end of the plan, or downstream of a lane change location on from lane).
     * @param searchedDistance distance searched in earlier episodes up to {@code position0}
     * @param position0 start position of episode
     * @param position1 start position of next episode (or end of plan)
     * @param schedule {@code true} adds downstream triggers and schedules them, {@code false} removes downstream triggers
     * @return increased searched distance up to {@code position1}
     */
    private Length findDetectorTriggersInEpisode(final Length searchedDistance, final LanePosition position0,
            final LanePosition position1, final boolean schedule)
    {
        // The following values all apply to the reference point
        Lane searchLane = position0.lane();
        Length from = position0.position();
        Length to = searchLane.getLength();
        Length searchedDistanceAtFrom = searchedDistance;
        Length searchedDistanceAtFromOnPendingLink = Length.ZERO; // value will be overwritten
        boolean encounteredPendingLink = false;
        while (searchLane != null)
        {
            // Bound 'to' by enter position of next pending lane
            if (!encounteredPendingLink && searchLane.getLink().equals(position1.lane().getLink()))
            {
                encounteredPendingLink = true;
                searchedDistanceAtFromOnPendingLink = searchedDistanceAtFrom;
                if (searchLane.equals(position1.lane()))
                {
                    // Same link, same lane: use position of next pending lane
                    to = position1.position();
                }
                else
                {
                    // Same link, different lane: lane change so project position on target lane (position1) to searchLane
                    Point2d point = position1.getLocation();
                    double fraction = searchLane.getCenterLine().projectFractional(
                            searchLane.getLink().getStartNode().getHeading(), searchLane.getLink().getEndNode().getHeading(),
                            point.x, point.y, FractionalFallback.ENDPOINT);
                    to = searchLane.getLength().times(fraction);
                }
            }
            // Find all detectors in range [from ... to] + dx
            for (LaneDetector detector : searchLane.getDetectors(from.plus(getFront().dx()), to.plus(getFront().dx()),
                    getType()))
            {
                if (schedule)
                {
                    Length dxTrigger = getRelativePositions().get(detector.getPositionType()).dx();
                    Length detectorLocation = detector.getLongitudinalPosition();
                    Length deltaOdometer = searchedDistanceAtFrom.plus(detectorLocation).minus(from).minus(dxTrigger);
                    this.detectorTriggers.put(detector, getOdometer().plus(deltaOdometer));
                }
                else
                {
                    this.detectorTriggers.remove(detector);
                }
            }
            // Search further as the GTU up to the nose is possibly on downstream lanes, but adjust relevant range
            searchedDistanceAtFrom = searchedDistanceAtFrom.plus(to.minus(from));
            if (encounteredPendingLink)
            {
                to = to.minus(searchLane.getLength());
            }
            searchLane = to.le0() ? null : getNextLaneForRoute(searchLane);
            from = Length.ZERO;
        }
        return searchedDistanceAtFromOnPendingLink;
    }

    /**
     * Trigger detector and remove it from detectors that need to be triggered.
     * @param detector detector
     */
    protected void triggerDetector(final LaneDetector detector)
    {
        this.detectorTriggers.remove(detector);
        detector.trigger(this);
    }

    /**
     * Returns the lateral overshoot at give time. This is the lateral distance by which the reference point exceeds either the
     * left or right edge of the lane. Negative values indicate the reference point is still on the lane.
     * @param when time
     * @return lateral overshoot
     */
    protected Length laneLateralOvershoot(final Time when)
    {
        Lane laneAtTime = getLane(when);
        Point2d location = getLocation(when);
        Length deviation = getDeviation(laneAtTime, location);
        LanePosition position = getPosition(when);
        Length laneWidth = position.lane().getWidth(position.position());
        return deviation.abs().minus(laneWidth.times(0.5));
    }

    /**
     * Returns the next lane for a given lane to stay on the route.
     * @param lane the lane for which we want to know the next Lane
     * @return next lane, {@code null} if none
     */
    @SuppressWarnings("hiddenfield")
    public synchronized Lane getNextLaneForRoute(final Lane lane)
    {
        // ask strategical planner
        Set<Lane> set = getNextLanesForRoute(lane);
        if (set == null || set.isEmpty())
        {
            return null;
        }
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
                lane.getLink().getId(), getId());
    }

    /**
     * Returns a set of {@code Lane}s that can be followed considering the route.
     * @param lane the lane for which we want to know the next Lane
     * @return set of {@code Lane}s that can be followed considering the route
     */
    @SuppressWarnings("hiddenfield")
    private Set<Lane> getNextLanesForRoute(final Lane lane)
    {
        Set<Lane> out = new LinkedHashSet<>();
        Set<Lane> nextPhysical = lane.nextLanes(null);

        Link link = Try.assign(() -> getStrategicalPlanner().nextLink(lane.getLink(), getType()),
                "Strategical planner experiences exception on network.");

        if (nextPhysical.isEmpty())
        {
            // ignore gap and just return closest lane on next link for the GTU type
            if (link instanceof CrossSectionLink cLink)
            {
                double minDistance = Double.POSITIVE_INFINITY;
                Lane closest = null;
                for (Lane next : cLink.getLanesAndShoulders())
                {
                    double distance = Math.hypot(next.getCenterLine().getFirst().x - lane.getCenterLine().getLast().x,
                            next.getCenterLine().getFirst().y - lane.getCenterLine().getLast().y);
                    if (distance < minDistance)
                    {
                        closest = next;
                        minDistance = distance;
                    }
                }
                if (closest != null)
                {
                    out.add(closest);
                }
            }
            return out;
        }

        Set<Lane> next = lane.nextLanes(getType());
        if (next.isEmpty())
        {
            next = nextPhysical;
        }
        for (Lane l : next)
        {
            if (l.getLink().equals(link))
            {
                out.add(l);
            }
        }
        return out;
    }

    /**
     * Returns an estimation of when the relative position will reach the line. Returns {@code null} if this does not occur
     * during the current operational plan.
     * @param line line, i.e. lateral line at link start or lateral entrance of sensor
     * @param relativePosition position to cross the line
     * @return estimation of when the relative position will reach the line, {@code null} if this does not occur during the
     *         current operational plan
     */
    private Time timeAtLine(final PolyLine2d line, final RelativePosition relativePosition)
    {
        Throw.when(line.size() != 2, IllegalArgumentException.class, "Line to cross with path should have 2 points.");
        OtsLine2d path = getOperationalPlan().getPath();
        List<Point2d> points = new ArrayList<>(path.size() + 1);
        points.addAll(path.getPointList());
        double adjust;
        if (relativePosition.dx().gt0())
        {
            // as the position is downstream of the reference, we need to attach some distance at the end
            points.add(path.getLocationExtendedSI(path.getLength() + relativePosition.dx().si));
            adjust = -relativePosition.dx().si;
        }
        else if (relativePosition.dx().lt0())
        {
            points.add(0, path.getLocationExtendedSI(relativePosition.dx().si));
            adjust = 0.0;
        }
        else
        {
            adjust = 0.0;
        }

        // find intersection
        double cumul = 0.0;
        for (int i = 0; i < points.size() - 1; i++)
        {
            Point2d intersect = Point2d.intersectionOfLineSegments(points.get(i), points.get(i + 1), line.get(0), line.get(1));

            /*
             * SKL 31-07-2023: Using the djunits code rather than the older OTS point and line code, causes an intersection on a
             * polyline to sometimes not be found, if the path has a point that is essentially on the line to cross. Clearly,
             * when entering a next lane/link, this is often the case as the GTU path is made from lane center lines that have
             * the endpoint of the lanes in it.
             */
            if (intersect == null)
            {
                double projectionFraction = line.projectOrthogonalFractionalExtended(points.get(i));
                if (0.0 <= projectionFraction && projectionFraction <= 1.0)
                {
                    // try
                    // {
                    Point2d projection = line.getLocationFraction(projectionFraction);
                    double distance = projection.distance(points.get(i));
                    if (distance < 1e-6)
                    {
                        intersect = projection;
                    }
                    // }
                    // catch (Exception e)
                    // {
                    // Point2d projection = line.getLocationFraction(projectionFraction);
                    // }
                }
            }

            if (intersect != null)
            {
                cumul += points.get(i).distance(intersect);
                cumul += adjust;
                // return time at distance
                if (cumul < 0.0)
                {
                    // return getSimulator().getSimulatorAbsTime(); // this was a mistake...
                    // relative position already crossed the point, e.g. FRONT
                    // SKL 08-02-2023: if the nose did not trigger at end of last move by mm's and due to vehicle rotation
                    // having been assumed straight, we should trigger it now. However, we should not double-trigger e.g.
                    // detectors. Let's return NaN to indicate this problem.
                    return Time.instantiateSI(Double.NaN);
                }
                if (cumul <= getOperationalPlan().getTotalLength().si)
                {
                    return getOperationalPlan().timeAtDistance(Length.instantiateSI(cumul));
                }
                // ref will cross the line, but GTU will not travel enough for rear to cross
                return null;
            }
            else if (i < points.size() - 2)
            {
                cumul += points.get(i).distance(points.get(i + 1));
            }
        }
        // no intersect
        return null;
    }

    /**
     * Sets a vehicle model.
     * @param vehicleModel vehicle model
     */
    public void setVehicleModel(final VehicleModel vehicleModel)
    {
        this.vehicleModel = vehicleModel;
    }

    /**
     * Returns the vehicle model.
     * @return vehicle model
     */
    public VehicleModel getVehicleModel()
    {
        return this.vehicleModel;
    }

    @Override
    public LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner();
    }

    @Override
    public LaneBasedStrategicalPlanner getStrategicalPlanner(final Time time)
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner(time);
    }

    /**
     * Returns the network.
     * @return the road network to which the LaneBasedGtu belongs
     */
    public RoadNetwork getNetwork()
    {
        return (RoadNetwork) super.getPerceivableContext();
    }

    /**
     * This method returns the current desired speed of the GTU. This value is required often, so implementations can cache it.
     * @return current desired speed
     */
    public synchronized Speed getDesiredSpeed()
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

    /**
     * This method returns the current car-following acceleration of the GTU. This value is required often, so implementations
     * can cache it.
     * @return current car-following acceleration
     */
    public synchronized Acceleration getCarFollowingAcceleration()
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
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            // obtain
            this.cachedCarFollowingAcceleration =
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().followingAcceleration(getParameters(), speed,
                            speedInfo, leaders), "Parameter exception while obtaining the desired speed.");
            this.carFollowingAccelerationTime = simTime;
        }
        return this.cachedCarFollowingAcceleration;
    }

    /**
     * Returns the turn indicator status.
     * @return the status of the turn indicator
     */
    public TurnIndicatorStatus getTurnIndicatorStatus()
    {
        return this.turnIndicatorStatus.get();
    }

    /**
     * Returns the turn indicator status at time.
     * @param time time to obtain the turn indicator status at
     * @return the status of the turn indicator at the given time
     */
    public TurnIndicatorStatus getTurnIndicatorStatus(final Time time)
    {
        return this.turnIndicatorStatus.get(time);
    }

    /**
     * Set the status of the turn indicator.
     * @param turnIndicatorStatus the new status of the turn indicator.
     */
    public void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus)
    {
        this.turnIndicatorStatus.set(turnIndicatorStatus);
    }

    @Override
    public Length getHeight()
    {
        return Length.ZERO;
    }

    @Override
    public String getFullId()
    {
        return getId();
    }

    /**
     * Sets how lane bookkeeping at lane changes is done.
     * @param bookkeeping how lane bookkeeping at lane changes is done
     */
    public void setBookkeeping(final LaneBookkeeping bookkeeping)
    {
        this.bookkeeping = bookkeeping;
    }

    /**
     * Returns how lane bookkeeping at lane changes is done.
     * @return how lane bookkeeping at lane changes is done
     */
    public LaneBookkeeping getBookkeeping()
    {
        return this.bookkeeping;
    }

    @Override
    public LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return getStrategicalPlanner().getTacticalPlanner();
    }

    @Override
    public LaneBasedTacticalPlanner getTacticalPlanner(final Time time)
    {
        return getStrategicalPlanner(time).getTacticalPlanner(time);
    }

    /**
     * Set distance over which the GTU should not change lane after being created.
     * @param distance distance over which the GTU should not change lane after being created
     */
    public void setNoLaneChangeDistance(final Length distance)
    {
        this.noLaneChangeDistance = distance;
    }

    /**
     * Returns whether a lane change is allowed.
     * @return whether a lane change is allowed
     */
    public boolean laneChangeAllowed()
    {
        return this.noLaneChangeDistance == null ? true : getOdometer().gt(this.noLaneChangeDistance);
    }

    /**
     * Set lane change direction. This should only be set by a controller of the GTU, e.g. the tactical planner.
     * @param direction lane change direction
     */
    public void setLaneChangeDirection(final LateralDirectionality direction)
    {
        this.laneChangeDirection.set(direction);
    }

    /**
     * Returns the lane change direction.
     * @return lane change direction
     */
    public LateralDirectionality getLaneChangeDirection()
    {
        return this.laneChangeDirection.get();
    }

    /**
     * Returns the lane change direction at the given time.
     * @param time time
     * @return lane change direction at the given time
     */
    public LateralDirectionality getLaneChangeDirection(final Time time)
    {
        return this.laneChangeDirection.get(time);
    }

    /**
     * Returns whether the braking lights are on.
     * @return whether the braking lights are on
     */
    public boolean isBrakingLightsOn()
    {
        return isBrakingLightsOn(getSimulator().getSimulatorAbsTime());
    }

    /**
     * Returns whether the braking lights are on.
     * @param when time
     * @return whether the braking lights are on
     */
    public boolean isBrakingLightsOn(final Time when)
    {
        return getVehicleModel().isBrakingLightsOn(getSpeed(when), getAcceleration(when));
    }

    /**
     * Get projected length on the lane.
     * @param lane lane to project the vehicle on
     * @return the length on the lane, which is different from the actual length during deviative tactical plans
     */
    @SuppressWarnings("hiddenfield")
    public Length getProjectedLength(final Lane lane)
    {
        Length front = getPosition(lane, getFront());
        Length rear = getPosition(lane, getRear());
        return front.minus(rear);
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        LanePosition dlp = getPosition();
        DirectedPoint2d location = this.getOperationalPlan() == null ? new DirectedPoint2d(0.0, 0.0, 0.0) : getLocation();
        synchronized (this)
        {
            if (dlp.lane() != null)
            {
                dlp.lane().removeGtu(this, true, dlp.position());
            }
        }
        if (dlp != null && dlp.lane() != null)
        {
            Lane referenceLane = dlp.lane();
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), new PositionVector(new double[] {location.x, location.y}, PositionUnit.METER),
                            new Direction(location.getDirZ(), DirectionUnit.EAST_RADIAN), getOdometer(),
                            referenceLane.getLink().getId(), referenceLane.getId(), dlp.position()},
                    getSimulator().getSimulatorTime());
        }
        else
        {
            fireTimedEvent(LaneBasedGtu.LANEBASED_DESTROY_EVENT,
                    new Object[] {getId(), new PositionVector(new double[] {location.x, location.y}, PositionUnit.METER),
                            new Direction(location.getDirZ(), DirectionUnit.EAST_RADIAN), getOdometer(), null, null, null},
                    getSimulator().getSimulatorTime());
        }
        cancelAllEvents();

        super.destroy();
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "GTU " + getId();
    }

}
