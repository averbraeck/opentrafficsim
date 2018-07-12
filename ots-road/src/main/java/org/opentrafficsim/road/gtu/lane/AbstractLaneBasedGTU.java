package org.opentrafficsim.road.gtu.lane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanBuilder;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedHashMap;
import org.opentrafficsim.core.perception.collections.HistoricalMap;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.language.Throw;
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
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final HistoricalMap<Lane, GTUDirectionality> lanesCurrentOperationalPlan;

    /** Pending leave triggers for each lane. */
    private Map<Lane, List<SimEventInterface<OTSSimTimeDouble>>> pendingLeaveTriggers = new HashMap<>();

    /** Pending enter triggers for each lane. */
    private Map<Lane, List<SimEventInterface<OTSSimTimeDouble>>> pendingEnterTriggers = new HashMap<>();

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
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param simulator to initialize the move method and to get the current time
     * @param network the network that the GTU is initially registered in
     * @throws GTUException when initial values are not correct
     */
    public AbstractLaneBasedGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final OTSNetwork network) throws GTUException
    {
        super(id, gtuType, simulator, network);
        this.fractionalLinkPositions = new HistoricalLinkedHashMap<>(HistoryManager.get(simulator));
        this.lanesCurrentOperationalPlan = new HistoricalLinkedHashMap<>(HistoryManager.get(simulator));
    }

    /**
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes with their directions
     * @param initialSpeed the initial speed of the car on the lane
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
//            Throw.when(lastPoint != null && pos.getLocation().distance(lastPoint) > initialLocationThresholdDifference.si,
//                    GTUException.class, "initial locations for GTU have distance > " + initialLocationThresholdDifference);
            lastPoint = pos.getLocation();
        }
        DirectedPoint initialLocation = lastPoint;

        // Give the GTU a 1 micrometer long operational plan, or a stand-still plan, so the first move and events will work
        Time now = getSimulator().getSimulatorTime().getTime();
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
                new Object[] { getId(), initialLocation, getLength(), getWidth(), getBaseColor(), referencePosition.getLane(),
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
     * Hack method. TODO remove and solve better
     * @return safe to change
     * @throws GTUException on error
     */
    public final boolean isSafeToChange() throws GTUException
    {
        return this.fractionalLinkPositions.get(getReferencePosition().getLane().getParentLink()) > 0.0;
    }

    /** {@inheritDoc} */
    @Override
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
     * Registers the lane at the GTU.
     * @param lane the lane to add to the list of lanes on which the GTU is registered.
     * @param gtuDirection the direction of the GTU on the lane (which can be bidirectional). If the GTU has a positive speed,
     *            it is moving in this direction.
     * @param position the position on the lane.
     * @throws GTUException when positioning the GTU on the lane causes a problem
     */
    private void addLaneToGtu(final Lane lane, final Length position, final GTUDirectionality gtuDirection) throws GTUException
    {
        if (this.lanesCurrentOperationalPlan.containsKey(lane))
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
        this.lanesCurrentOperationalPlan.put(lane, gtuDirection);
    }

    /**
     * Part of 'enterLane' which register the lane at the GTU so the GTU can report it's position on the lane.
     * @param lane Lane; lane
     * @param position Length; position
     * @throws GTUException on exception
     */
    protected void addGtuToLane(final Lane lane, final Length position) throws GTUException
    {
        List<SimEventInterface<OTSSimTimeDouble>> pending = this.pendingEnterTriggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<OTSSimTimeDouble> event : pending)
            {
                if (event.getAbsoluteExecutionTime().get().ge(getSimulator().getSimulatorTime().get()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().get().ne(getSimulator().getSimulatorTime().get()))
                    {
                        System.err.println("addLaneToGtu, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime().get() + ", eventTime="
                                + event.getAbsoluteExecutionTime().get());
                    }
                }
            }
            this.pendingEnterTriggers.remove(lane);
        }
        lane.addGTU(this, position);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane) throws GTUException
    {
        leaveLane(lane, false);
    }

    /**
     * Leave a lane but do not complain about having no lanes left when beingDestroyed is true.
     * @param lane the lane to leave
     * @param beingDestroyed if true, no complaints about having no lanes left
     * @throws GTUException in case leaveLane should not be called
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void leaveLane(final Lane lane, final boolean beingDestroyed) throws GTUException
    {
        Length position = position(lane, getReference());
        this.lanesCurrentOperationalPlan.remove(lane);
        removePendingEvents(lane, this.pendingLeaveTriggers);
        removePendingEvents(lane, this.pendingEnterTriggers);
        // check if there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.lanesCurrentOperationalPlan.keySet())
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
        if (this.lanesCurrentOperationalPlan.size() == 0 && !beingDestroyed)
        {
            System.err.println("leaveLane: lanes.size() = 0 for GTU " + getId());
        }
    }

    /**
     * Removes and cancels events for the given lane.
     * @param lane Lane; lane
     * @param triggers Map; map to use
     */
    private void removePendingEvents(final Lane lane, final Map<Lane, List<SimEventInterface<OTSSimTimeDouble>>> triggers)
    {
        List<SimEventInterface<OTSSimTimeDouble>> pending = triggers.get(lane);
        if (null != pending)
        {
            for (SimEventInterface<OTSSimTimeDouble> event : pending)
            {
                if (event.getAbsoluteExecutionTime().get().ge(getSimulator().getSimulatorTime().get()))
                {
                    boolean result = getSimulator().cancelEvent(event);
                    if (!result && event.getAbsoluteExecutionTime().get().ne(getSimulator().getSimulatorTime().get()))
                    {
                        System.err.println("leaveLane, trying to remove event: NOTHING REMOVED -- result=" + result
                                + ", simTime=" + getSimulator().getSimulatorTime().get() + ", eventTime="
                                + event.getAbsoluteExecutionTime().get());
                    }
                }
            }
            triggers.remove(lane);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void changeLaneInstantaneously(final LateralDirectionality laneChangeDirection) throws GTUException
    {

        // from info
        DirectedLanePosition from = getReferencePosition();

        // keep a copy of the lanes and directions (!)
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.lanesCurrentOperationalPlan);
        Set<Lane> lanesToBeRemoved = new LinkedHashSet<>();
        Map<Lane, Double> fractionalLanePositions = new HashMap<>();

        for (Lane lane : lanesCopy.keySet())
        {
            fractionalLanePositions.put(lane, fractionalPosition(lane, getReference()));
            lanesToBeRemoved.add(lane);
        }

        // store the new positions, and sample statistics
        // start with current link position, these will be overwritten, except if from a lane no adjacent lane is found, i.e.
        // changing over a continuous line when probably the reference point is past the line
        Map<Link, Double> newLinkPositionsLC = new LinkedHashMap<>(this.fractionalLinkPositions);
        Set<DirectedLanePosition> sidewaysEnter = new LinkedHashSet<>();

        for (Lane lane : lanesCopy.keySet())
        {
            Set<Lane> laneSet = lane.accessibleAdjacentLanesPhysical(laneChangeDirection, getGTUType(), getDirection(lane));
            if (laneSet.size() > 0)
            {
                Lane adjacentLane = laneSet.iterator().next();
                Length pos = null;
                try
                {
                    pos = adjacentLane.position(lane.fraction(position(lane, getReference())));
                    if (pos.si < -getFront().getDx().si || pos.si > adjacentLane.getLength().si - getRear().getDx().si)
                    {
                        // due to curvature, the GTU is not on the adjacent lane, scheduleEnterLeaveTriggers registers next lane
                        continue;
                    }
                    Length adjustedStart =
                            pos.minus(getOperationalPlan().getTraveledDistance(getSimulator().getSimulatorTime().getTime()));
                    newLinkPositionsLC.put(lane.getParentLink(), adjustedStart.si / adjacentLane.getLength().si);

                }
                catch (OperationalPlanException exception)
                {
                    exception.printStackTrace();
                }
                enterLane(adjacentLane, adjacentLane.getLength().multiplyBy(fractionalLanePositions.get(lane)),
                        lanesCopy.get(lane));

                // due to curvature, the GTU is on the next lane of the adjacent lane
                if (pos != null && pos.si + getFront().getDx().si > adjacentLane.getLength().si)
                {
                    // scheduleEnterLeaveTriggers won't trigger this lane as the nose is already on it
                    Lane nextLane = Try.assign(() -> determineNextLane(adjacentLane), "Exception while determining next lane.");
                    GTUDirectionality dir = (lanesCopy.get(lane).isPlus() ? adjacentLane.nextLanes(getGTUType())
                            : adjacentLane.prevLanes(getGTUType())).get(nextLane);
                    // need to postpone scheduling until we know no other current lane has this lane as adjacent lane
                    // position is current position at start of new plan, as we schedule for right after the move method
                    sidewaysEnter.add(new DirectedLanePosition(nextLane, pos.minus(adjacentLane.getLength()), dir));
                }

                lanesToBeRemoved.remove(adjacentLane); // don't remove lanes we might end up on
            }

        }

        // update the positions on the lanes we are registered on
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkPositionsLC);

        // schedule lane that are entered sideways at link boundaries
        for (DirectedLanePosition dirLanePos : sidewaysEnter)
        {
            // only if not already entered sideways from another current lane
            if (!this.lanesCurrentOperationalPlan.containsKey(dirLanePos.getLane()))
            {
                // need to schedule as fractionalLinkPositions will be overwritten in AbstractLaneBasedGTU.move() without this
                try
                {
                    getSimulator().scheduleEventNow(this, this, "enterLane",
                            new Object[] { dirLanePos.getLane(), dirLanePos.getPosition(), dirLanePos.getGtuDirection() });
                }
                catch (SimRuntimeException exception)
                {
                    throw new GTUException(exception);
                }
            }
        }

        // leave the from lanes 
        for (Lane lane : lanesToBeRemoved)
        {
            leaveLane(lane);
        }

        // stored positions no longer valid
        this.referencePositionTime = Double.NaN;

        // fire event
        this.fireTimedEvent(LaneBasedGTU.LANE_CHANGE_EVENT, new Object[] { getId(), laneChangeDirection, from },
                getSimulator().getSimulatorTime());

    }

    /**
     * Register on lanes in target lane.
     * @param laneChangeDirection direction of lane change
     * @throws GTUException exception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public void initLaneChange(final LateralDirectionality laneChangeDirection) throws GTUException
    {
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.lanesCurrentOperationalPlan);
        Map<Lane, Double> fractionalLanePositions = new HashMap<>();
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
                enterLane(adjacentLane, adjacentLane.getLength().multiplyBy(fractionalLanePositions.get(lane)),
                        lanesCopy.get(lane));
            }
        }
        Throw.when(numRegistered == 0, GTUException.class, "Gtu %s starting %s lane change, but no adjacent lane found.",
                getId(), laneChangeDirection);
    }

    /**
     * Performs the finalization of a lane change by leaving the from lanes.
     * @param laneChangeDirection direction of lane change
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected void finalizeLaneChange(final LateralDirectionality laneChangeDirection)
    {
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.lanesCurrentOperationalPlan);
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
            for (Lane lane : lanesToBeRemoved)
            {
                double fractionalPosition = this.fractionalLinkPositions.get(lane.getParentLink());
                if (0.0 <= fractionalPosition && fractionalPosition <= 1.0)
                {
                    fromLane = lane;
                    fromPosition = lane.getLength().multiplyBy(fractionalPosition);
                    fromDirection = getDirection(lane);
                }
                leaveLane(lane);
            }
        }
        catch (GTUException exception)
        {
            // should not happen, lane was obtained from GTU
            throw new RuntimeException("fractionalPosition on lane not possible", exception);
        }
        this.referencePositionTime = Double.NaN;
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
    public final GTUDirectionality getDirection(final Lane lane) throws GTUException
    {
        Throw.when(!this.lanesCurrentOperationalPlan.containsKey(lane), GTUException.class,
                "getDirection: Lanes %s does not contain %s", this.lanesCurrentOperationalPlan.keySet(), lane);
        return this.lanesCurrentOperationalPlan.get(lane);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected void move(final DirectedPoint fromLocation)
            throws SimRuntimeException, GTUException, OperationalPlanException, NetworkException, ParameterException
    {
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanesCurrentOperationalPlan.isEmpty())
        {
            destroy();
            return; // Done; do not re-schedule execution of this move method.
        }

        // remove enter events
        for (Lane lane : this.pendingEnterTriggers.keySet())
        {
            List<SimEventInterface<OTSSimTimeDouble>> events = this.pendingEnterTriggers.get(lane);
            for (SimEventInterface<OTSSimTimeDouble> event : events)
            {
                // also unregister from lane
                this.lanesCurrentOperationalPlan.remove(lane);
                getSimulator().cancelEvent(event);
            }
        }
        this.pendingEnterTriggers.clear();

        // store the new positions
        Map<Link, Double> newLinkPositions = new LinkedHashMap<>();
        for (Lane lane : this.lanesCurrentOperationalPlan.keySet())
        {
            newLinkPositions.put(lane.getParentLink(), lane.fraction(position(lane, getReference())));
        }

        // generate the next operational plan and carry it out
        super.move(fromLocation);

        // update the positions on the lanes we are registered on
        this.fractionalLinkPositions.clear();
        this.fractionalLinkPositions.putAll(newLinkPositions);

        DirectedLanePosition dlp = getReferencePosition();
        fireTimedEvent(
                LaneBasedGTU.LANEBASED_MOVE_EVENT, new Object[] { getId(), fromLocation, getSpeed(), getAcceleration(),
                        getTurnIndicatorStatus(), getOdometer(), dlp.getLane(), dlp.getPosition(), dlp.getGtuDirection() },
                getSimulator().getSimulatorTime());

        if (getOperationalPlan().getAcceleration(Duration.ZERO).si < -10
                && getOperationalPlan().getSpeed(Duration.ZERO).si > 2.5)
        {
            System.err.println("GTU: " + getId() + " - getOperationalPlan().getAcceleration(Duration.ZERO).si < -10)");
            System.err.println("Lanes in current plan: " + this.lanesCurrentOperationalPlan.keySet());
            if (getTacticalPlanner().getPerception().contains(DefaultSimplePerception.class))
            {
                DefaultSimplePerception p =
                        getTacticalPlanner().getPerception().getPerceptionCategory(DefaultSimplePerception.class);
                System.err.println("HeadwayGTU: " + p.getForwardHeadwayGTU());
                System.err.println("HeadwayObject: " + p.getForwardHeadwayObject());
            }
        }

        // schedule triggers and determine when to enter lanes with front and leave lanes with rear
        scheduleEnterLeaveTriggers();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GTUException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GTUException
    {
        Map<Lane, Length> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanesCurrentOperationalPlan.keySet())
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
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
                if (null != this.lanesCurrentOperationalPlan.get(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, RelativePosition.REFERENCE_POSITION, when);
                    Length pos = new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                    if (this.lanesCurrentOperationalPlan.get(cseLane).isPlus())
                    {
                        return pos.plus(relativePosition.getDx());
                    }
                    return pos.minus(relativePosition.getDx());
                }
            }
        }
        throw new GTUException(this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
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
                if (null != this.lanesCurrentOperationalPlan.get(cseLane))
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
    private Map<Integer, Length> cachedPositions = new HashMap<>();

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
            double longitudinalPosition;
            try
            {
                longitudinalPosition = lane.positionSI(this.fractionalLinkPositions.get(when).get(lane.getParentLink()));
            }
            catch (NullPointerException exception)
            {
                throw exception;
            }
            double loc;
            try
            {
                OperationalPlan plan = getOperationalPlan(when);
                if (!(plan instanceof LaneBasedOperationalPlan) || !((LaneBasedOperationalPlan) plan).isDeviative())
                {
                    if (this.lanesCurrentOperationalPlan.get(when).get(lane).isPlus())
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
                            double distance = getDistanceAtOtherLane(lane, when, upstream, 0.0, p);
                            if (!Double.isNaN(distance))
                            {
                                loc = upstream ? -distance : distance + lane.getLength().si;
                                break;
                            }
                            else if (i == 1)
                            {
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
                System.err.println(this.lanesCurrentOperationalPlan.get(when));
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

    /**
     * In case of a deviative operational plan (not on the center lines), positions are projected fractionally to the center
     * lines. For points upstream or downstream of a lane, fractional projection is not valid. In such cases we need to project
     * the position to an upstream or downstream lane instead, and adjust length along the center lines.
     * @param lane Lane; lane to determine the position on
     * @param when Time; time
     * @param upstream boolean; whether to check upstream (or downstream)
     * @param distance double; cumulative distance in recursive search, starts at 0.0
     * @param point DirectedPoint; absolute point of GTU to be projected to center line
     * @return Length; position on lane being &lt;0 or &gt;{@code lane.getLength()}
     */
    private double getDistanceAtOtherLane(final Lane lane, final Time when, final boolean upstream, final double distance,
            final DirectedPoint point)
    {
        Set<Lane> nextLanes = new HashSet<>(upstream == this.lanesCurrentOperationalPlan.get(lane).isPlus()
                ? lane.prevLanes(getGTUType()).keySet() : lane.nextLanes(getGTUType()).keySet()); // safe copy
        nextLanes.retainAll(this.lanesCurrentOperationalPlan.keySet()); // as we delete here
        if (nextLanes.size() == 0)
        {
            return Double.NaN; // point must be in the other direction
        }
        Throw.when(nextLanes.size() > 1, IllegalStateException.class,
                "A position (%s) of GTU % is not on any of the current registered lanes.", point, this.getId());
        Lane nextLane = nextLanes.iterator().next();
        double f = nextLane.getCenterLine().projectFractional(null, null, point.x, point.y, FractionalFallback.NaN);
        if (Double.isNaN(f))
        {
            getDistanceAtOtherLane(nextLane, when, upstream, distance + nextLane.getLength().si, point);
        }
        return distance
                + (upstream == this.lanesCurrentOperationalPlan.get(nextLane).isPlus() ? 1.0 - f : f) * nextLane.getLength().si;
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
        if (this.referencePositionTime == getSimulator().getSimulatorTime().getTime().si)
        {
            return this.cachedReferencePosition;
        }
        Lane refLane = null;
        Double closest = Double.POSITIVE_INFINITY;
        for (Lane lane : this.lanesCurrentOperationalPlan.keySet())
        {
            double fraction = fractionalPosition(lane, getReference());
            if (fraction >= 0.0 && fraction <= 1.0)
            {
                // TODO widest lane in case we are registered on more than one lane with the reference point?
                // TODO lane that leads to our location or not if we are registered on parallel lanes?
                if (refLane == null)
                {
                    refLane = lane;
                }
                else
                {
                    DirectedPoint loc = getLocation();
                    try
                    {
                        // FIXME: should this not be a test for Double.infinity? Or should closest be initialized to null?
                        if (closest == null)
                        {
                            double f = refLane.getCenterLine().projectFractional(null, null, loc.x, loc.y,
                                    FractionalFallback.ENDPOINT);
                            closest = loc.distance(refLane.getCenterLine().getLocationFraction(f));
                        }
                        double f =
                                lane.getCenterLine().projectFractional(null, null, loc.x, loc.y, FractionalFallback.ENDPOINT);
                        double distance = loc.distance(lane.getCenterLine().getLocationFraction(f));
                        if (distance < closest)
                        {
                            refLane = lane;
                            closest = distance;
                        }
                    }
                    catch (OTSGeometryException exception)
                    {
                        throw new RuntimeException("Exception while determining reference position between lanes.", exception);
                    }
                }
            }
        }
        if (refLane != null)
        {
            this.cachedReferencePosition =
                    new DirectedLanePosition(refLane, position(refLane, getReference()), this.getDirection(refLane));
            this.referencePositionTime = getSimulator().getSimulatorTime().getTime().si;
            return this.cachedReferencePosition;
        }
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
        DirectedLanePosition ref = getReferencePosition();
        double endPosition = position(ref.getLane(), getReference(), getOperationalPlan().getEndTime()).si;
        double moveSI = endPosition - ref.getPosition().si; // getOperationalPlan().getTotalLength().si;

        // Figure out which lanes this GTU will enter with its FRONT, and schedule the lane enter events
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<>(this.lanesCurrentOperationalPlan);
        Iterator<Lane> it = lanesCopy.keySet().iterator();
        Lane enteredLane = null;
        while (it.hasNext() || enteredLane != null) // use a copy because this.lanesCurrentOperationalPlan can change
        {
            // next lane from 'lanesCopy', or asses the lane we just entered as it may be very short and also passed fully
            Lane lane;
            double sign;
            if (enteredLane == null)
            {
                lane = it.next();
                sign = lanesCopy.get(lane).isPlus() ? 1.0 : -1.0;
            }
            else
            {
                lane = enteredLane;
                sign = this.lanesCurrentOperationalPlan.get(lane).isPlus() ? 1.0 : -1.0;
            }
            enteredLane = null;

            // schedule triggers on this lane
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            // referenceStartSI is position of reference of GTU on current lane
            if (sign > 0.0)
            {
                lane.scheduleSensorTriggers(this, referenceStartSI, moveSI);
            }
            else
            {
                lane.scheduleSensorTriggers(this, referenceStartSI - moveSI, moveSI);
            }

            // TODO work with getNextLaneDirection() and remove determineNextLane() and determinePrevLane()

            // check if we exceed the lane with the front during this operational plan
            double nextFrontPosSI = referenceStartSI + sign * (moveSI + getFront().getDx().si);
            Lane nextLane = null;
            GTUDirectionality direction = null;
            Length refPosAtLastTimestep = null;
            DirectedPoint end = null;
            try
            {
                if (sign > 0.0 && referenceStartSI + getFront().getDx().si <= lane.getLength().si
                        && nextFrontPosSI > lane.getLength().si)
                {

                    nextLane = determineNextLane(lane);
                    if (nextLane == null)
                    {
                        // already on the lane, can happen at a lane change
                        continue;
                        // TODO lane change needs to be more accurate, this can happen the other way around too (GTU was not
                        // registered during the lane change, and this scheduling is also not triggered as the GTU is too far
                    }
                    direction = lane.nextLanes(getGTUType()).get(nextLane);
                    end = lane.getCenterLine().getLocationExtendedSI(lane.getLength().si - getFront().getDx().si);
                    // end is the location where the center (reference) of the GTU is when its front moves out of the lane
                    if (direction.isPlus())
                    {
                        // o----L1-r->o----L2--->o
                        refPosAtLastTimestep = Length.createSI(referenceStartSI - lane.getLength().si);
                    }
                    else
                    {
                        // o----L1-r->o<---L2----o
                        refPosAtLastTimestep =
                                Length.createSI(nextLane.getLength().si - referenceStartSI + lane.getLength().si);
                    }

                }
                else if (sign < 0.0 && referenceStartSI - getFront().getDx().si >= 0.0 && nextFrontPosSI < 0.0)
                {
                    nextLane = determinePrevLane(lane);
                    direction = lane.prevLanes(getGTUType()).get(nextLane);
                    end = lane.getCenterLine().getLocationExtendedSI(getFront().getDx().si);
                    end.setRotZ(end.getRotZ() + Math.PI); // probably doesn't matter: intersection with perpendicular line
                    if (direction.isPlus())
                    {
                        // o<---L1-r--o----L2--->o
                        refPosAtLastTimestep = Length.createSI(-referenceStartSI);
                    }
                    else
                    {
                        // o<---L1-r--o<---L2----o
                        refPosAtLastTimestep = Length.createSI(nextLane.getLength().si + referenceStartSI);
                    }
                }
            }
            catch (@SuppressWarnings("unused") NetworkException exception)
            {
                // plan goes beyond the lane, but we might (finish a) lane change during the plan
            }
            if (end != null)
            {
                Time t = getOperationalPlan().timeAtPoint(end, false);
                if (!Double.isNaN(t.si))
                {
                    addLaneToGtu(nextLane, refPosAtLastTimestep, direction);
                    enteredLane = nextLane;
                    SimEventInterface<OTSSimTimeDouble> event = getSimulator().scheduleEventAbs(t, this, this, "addGtuToLane",
                            new Object[] { nextLane, refPosAtLastTimestep }); // pos at plan start
                    addEnterTrigger(nextLane, event);
                    // schedule any sensor triggers on this lane for the remainder time
                    // nextLane.scheduleSensorTriggers(this, refPosAtLastTimestep.si, direction.isPlus() ? moveSI : -moveSI);
                }
            }
        }

        // Figure out which lanes this GTU will exit with its BACK, and schedule the lane exit events
        for (Lane lane : this.lanesCurrentOperationalPlan.keySet())
        {
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            Time exitTime = null;
            // heuristic to perform accurate timeAtPoint check: reference position beyond lane
            if (this.lanesCurrentOperationalPlan.get(lane).equals(GTUDirectionality.DIR_PLUS))
            {
                if (referenceStartSI + getRear().getDx().si + moveSI > lane.getLength().si)
                {
                    try
                    {
                        exitTime = getOperationalPlan().timeAtPoint(lane.getCenterLine().getLocationFraction(1.0),
                                this.fractionalLinkPositions.get(lane.getParentLink()) > 1.0);
                    }
                    catch (OTSGeometryException exception)
                    {
                        throw new RuntimeException(exception);
                    }
                }
            }
            else
            {
                if (referenceStartSI - getRear().getDx().si - moveSI < 0.0)
                {
                    try
                    {
                        exitTime = getOperationalPlan().timeAtPoint(lane.getCenterLine().getLocationFraction(0.0),
                                this.fractionalLinkPositions.get(lane.getParentLink()) < 0.0);
                    }
                    catch (OTSGeometryException exception)
                    {
                        throw new RuntimeException(exception);
                    }
                }
            }
            if (exitTime != null && !Double.isNaN(exitTime.si))
            {
                SimEvent<OTSSimTimeDouble> event = new SimEvent<>(new OTSSimTimeDouble(exitTime), this, this, "leaveLane",
                        new Object[] { lane, new Boolean(false) });
                getSimulator().scheduleEvent(event);
                addTrigger(lane, event);
            }
        }
    }

    /**
     * Get the next lane.
     * @param lane the lane to find the successor for
     * @return the next lane for this GTU
     * @throws NetworkException when no next lane exists or the route branches into multiple next lanes
     * @throws GTUException when no route could be found or the routeNavigator returns null
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Lane determineNextLane(final Lane lane) throws NetworkException, GTUException
    {
        Lane nextLane = null;
        Map<Lane, GTUDirectionality> nextLanes = lane.nextLanes(getGTUType());
        Throw.when(nextLanes.size() == 0, NetworkException.class, "%s - lane %s does not have a successor", this, lane);
        // if (nextLanes.size() == 1)
        // {
        // nextLane = nextLanes.keySet().iterator().next();
        // Throw.when(this.lanesCurrentOperationalPlan.containsKey(nextLane), NetworkException.class,
        // "%s - lane %s is already registered to the GTU", this, lane);
        // }
        // else
        // {
        Node nextNode = getStrategicalPlanner().nextNode(lane.getParentLink(), GTUDirectionality.DIR_PLUS, getGTUType());
        Throw.when(nextNode == null, GTUException.class, "%s reaches branch and the route returns null as nextNodeToVisit",
                this);
        int continuingLaneCount = 0;
        for (Lane candidateLane : nextLanes.keySet())
        {
            if (null != this.lanesCurrentOperationalPlan.get(candidateLane))
            {
                return null; // Already on this lane
            }
            // XXX Hack - this should be done more considerate -- fails at loops...
            if (nextNode == candidateLane.getParentLink().getEndNode()
                    || nextNode == candidateLane.getParentLink().getStartNode())
            {
                // XXX Hack: use the one that has lexicographic lower parent link id.
                if (null == nextLane || (nextLane.getParentLink().getId().compareTo(candidateLane.getParentLink().getId()) > 0))
                {
                    nextLane = candidateLane;
                }
                continuingLaneCount++;
            }
        }
        if (continuingLaneCount == 0)
        {
            throw new NetworkException(this + " reached branch and the route specifies a nextNodeToVisit (" + nextNode
                    + ") that is not a next node at this branch at (" + lane.getParentLink().getEndNode() + ")");
        }
        if (continuingLaneCount > 1)
        {
            System.err.println(this + " reached branch and the route specifies multiple lanes to continue on at this branch ("
                            + lane.getParentLink().getEndNode() + "). Selecting lane " + nextLane.getId());
            // throw new NetworkException(
            // this + " reached branch and the route specifies multiple lanes to continue on at this branch ("
            // + lane.getParentLink().getEndNode() + "). This is not yet supported");
        }
        // }
        return nextLane;
    }

    /**
     * Get the previous lane, that is next for the GTU.
     * @param lane the lane to find the predecessor for
     * @return the next lane for this GTU
     * @throws NetworkException when no next lane exists or the route branches into multiple next lanes
     * @throws GTUException when no route could be found or the routeNavigator returns null
     */
    @SuppressWarnings("checkstyle:designforextension")
    protected Lane determinePrevLane(final Lane lane) throws NetworkException, GTUException
    {
        Lane prevLane = null;
        Map<Lane, GTUDirectionality> prevLanes = lane.prevLanes(getGTUType());
        if (prevLanes.size() == 0)
        {
            return null; // throw new NetworkException(this + " - lane " + lane + " does not have a predecessor");
        }
        if (prevLanes.size() == 1)
        {
            prevLane = lane.prevLanes(getGTUType()).keySet().iterator().next();
        }
        else
        {
            if (!(getStrategicalPlanner() instanceof LaneBasedStrategicalRoutePlanner))
            {
                throw new GTUException(this + " reaches branch but has no route navigator");
            }
            Node prevNode = ((LaneBasedStrategicalRoutePlanner) getStrategicalPlanner()).nextNode(lane.getParentLink(),
                    GTUDirectionality.DIR_MINUS, getGTUType());
            if (null == prevNode)
            {
                throw new GTUException(this + " reaches branch and the route returns null as nextNodeToVisit");
            }
            int continuingLaneCount = 0;
            for (Lane candidateLane : prevLanes.keySet())
            {
                if (null != this.lanesCurrentOperationalPlan.get(candidateLane))
                {
                    return null; // Already on this lane
                }
                // XXX Hack - this should be done more considerate -- fails at loops...
                if (prevNode == candidateLane.getParentLink().getEndNode()
                        || prevNode == candidateLane.getParentLink().getStartNode())
                {
                    prevLane = candidateLane;
                    continuingLaneCount++;
                }
            }
            if (continuingLaneCount == 0)
            {
                throw new NetworkException(this + " reached branch and the route specifies a nextNodeToVisit (" + prevNode
                        + ") that is not a next node at this branch at (" + lane.getParentLink().getStartNode() + ")");
            }
            if (continuingLaneCount > 1)
            {
                throw new NetworkException(
                        this + " reached branch and the route specifies multiple lanes to continue on at this branch ("
                                + lane.getParentLink().getStartNode() + "). This is not yet supported");
            }
        }
        return prevLane;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws GTUException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanesCurrentOperationalPlan.keySet())
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
    public final void addTrigger(final Lane lane, final SimEventInterface<OTSSimTimeDouble> event)
    {
        List<SimEventInterface<OTSSimTimeDouble>> list = this.pendingLeaveTriggers.get(lane);
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
     * @param event SimEvent; event
     */
    private final void addEnterTrigger(final Lane lane, final SimEventInterface<OTSSimTimeDouble> event)
    {
        List<SimEventInterface<OTSSimTimeDouble>> list = this.pendingEnterTriggers.get(lane);
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
        catch (@SuppressWarnings("unused") GTUException e)
        {
            // ignore. not important at destroy
        }
        DirectedPoint location = this.getOperationalPlan() == null ? new DirectedPoint(0.0, 0.0, 0.0) : getLocation();

        synchronized (this.lock)
        {
            Set<Lane> laneSet = new LinkedHashSet<>(this.lanesCurrentOperationalPlan.keySet()); // Operate on a copy of the key
                                                                                                // set
            for (Lane lane : laneSet)
            {
                try
                {
                    leaveLane(lane, true);
                }
                catch (@SuppressWarnings("unused") GTUException e)
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
    public final LaneBasedTacticalPlanner getTacticalPlanner()
    {
        return (LaneBasedTacticalPlanner) super.getTacticalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    public Speed getDesiredSpeed()
    {
        Time simTime = getSimulator().getSimulatorTime().getTime();
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
        Time simTime = getSimulator().getSimulatorTime().getTime();
        if (this.carFollowingAccelerationTime == null || this.carFollowingAccelerationTime.si < simTime.si)
        {
            LanePerception perception = getTacticalPlanner().getPerception();
            // speed
            EgoPerception ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
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
                    Try.assign(() -> getTacticalPlanner().getCarFollowingModel().followingAcceleration(getParameters(), speed,
                            speedInfo, leaders), "Parameter exception while obtaining the desired speed.");
            this.carFollowingAccelerationTime = simTime;
        }
        return this.cachedCarFollowingAcceleration;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return String.format("GTU " + getId());
    }

}
