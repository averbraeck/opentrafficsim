package org.opentrafficsim.road.gtu.lane;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

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
public abstract class AbstractLaneBasedGTU extends AbstractGTU implements LaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /**
     * Fractional longitudinal positions of the reference point of the GTU on one or more links at the lastEvaluationTime.
     * Because the reference point of the GTU might not be on all the links the GTU is registered on, the fractional
     * longitudinal positions can be more than one, or less than zero.
     */
    private Map<Link, Double> fractionalLinkPositions = new LinkedHashMap<>();

    /**
     * The lanes the GTU is registered on. Each lane has to have its link registered in the fractionalLinkPositions as well to
     * keep consistency. Each link from the fractionalLinkPositions can have one or more Lanes on which the vehicle is
     * registered. This is a list to improve reproducibility: The 'oldest' lanes on which the vehicle is registered are at the
     * front of the list, the later ones more to the back.
     */
    private final Map<Lane, GTUDirectionality> lanes = new HashMap<>();

    /** the object to lock to make the GTU thread safe. */
    private Object lock = new Object();

    /**
     * Construct a Lane Based GTU.
     * @param id the id of the GTU
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes with their directions
     * @param initialSpeed the initial speed of the car on the lane
     * @param simulator to initialize the move method and to get the current time
     * @param strategicalPlanner the strategical planner (e.g., route determination) to use
     * @param perception the lane-based perception model of the GTU
     * @param network the network that the GTU is initially registered in
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     * @throws OTSGeometryException when the initial path is wrong
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedGTU(final String id, final GTUType gtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
        final LanePerceptionFull perception, final OTSNetwork network) throws NetworkException, SimRuntimeException,
        GTUException, OTSGeometryException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, checkInitialLongitudinalPositions(
            initialLongitudinalPositions).iterator().next().getLocation(), initialSpeed, network);

        // register the GTU in the perception module
        getPerception().setGTU(this);

        // register the GTU on the lanes
        for (DirectedLanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            enterLane(lane, directedLanePosition.getPosition(), directedLanePosition.getGtuDirection());
        }
    }

    /**
     * Throw a GTUException if the provided set of initial longitudinal positions is null or empty.
     * @param initialLongitudinalPositions Set&lt;DirectedLanePosition&gt;; the set of initial longitudinal positions to check
     * @return Set&lt;DirectedLanePosition&gt;; the argument of this method
     * @throws GTUException when the provided set is null or empty
     */
    private static Set<DirectedLanePosition> checkInitialLongitudinalPositions(
        Set<DirectedLanePosition> initialLongitudinalPositions) throws GTUException
    {
        if (null == initialLongitudinalPositions)
        {
            throw new GTUException("InitialLongitudinalPositions is null");
        }
        if (0 == initialLongitudinalPositions.size())
        {
            throw new GTUException("InitialLongitudinalPositions is empty set");
        }
        return initialLongitudinalPositions;
    }

    /** {@inheritDoc} */
    @Override
    public final void enterLane(final Lane lane, final Length.Rel position, final GTUDirectionality gtuDirection)
        throws GTUException
    {
        if (lane == null || gtuDirection == null || position == null)
        {
            throw new GTUException("enterLane - one of the arguments is null");
        }
        if (this.lanes.containsKey(lane))
        {
            System.err.println("GTU " + toString() + " is already registered on this lane: " + lane);
            return;
        }

        // if the GTU is already registered on a lane of the same link, do not change its fractional position, as
        // this might lead to a "jump".
        if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
        {
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(position));
        }
        this.lanes.put(lane, gtuDirection);
        lane.addGTU(this, position);
    }

    /** {@inheritDoc} */
    @Override
    public final void leaveLane(final Lane lane)
    {
        leaveLane(lane, false);
    }

    /**
     * Leave a lane but do not complain about having no lanes left when beingDestroyed is true.
     * @param lane the lane to leave
     * @param beingDestroyed if true, no complaints about having no lanes left
     */
    public final void leaveLane(final Lane lane, final boolean beingDestroyed)
    {
        // System.out.println("GTU " + toString() + " to be removed from lane: " + lane);
        this.lanes.remove(lane);
        // check of there are any lanes for this link left. If not, remove the link.
        boolean found = false;
        for (Lane l : this.lanes.keySet())
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
        lane.removeGTU(this);
        if (this.lanes.size() == 0 && !beingDestroyed)
        {
            System.err.println("lanes.size() = 0 for GTU " + getId());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, GTUDirectionality> getLanes()
    {
        return new HashMap<Lane, GTUDirectionality>(this.lanes);
    }

    /** {@inheritDoc} */
    @Override
    protected final void move(final DirectedPoint fromLocation) throws SimRuntimeException, GTUException,
        OperationalPlanException, NetworkException
    {
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanes.isEmpty())
        {
            destroy();
            return; // Done; do not re-schedule execution of this move method.
        }

        // store the new positions, and sample statistics
        Map<Link, Double> newLinkPositions = new HashMap<>();
        for (Lane lane : this.lanes.keySet())
        {
            lane.sample(this);
            newLinkPositions.put(lane.getParentLink(), lane.fraction(position(lane, getReference())));
        }

        // generate the next operational plan and carry it out
        super.move(fromLocation);

        // update the positions on the lanes we are registered on
        this.fractionalLinkPositions = newLinkPositions;

        // schedule triggers and determine when to enter lanes with front and leave lanes with rear
        scheduleTriggers();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition) throws GTUException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition, final Time.Abs when)
        throws GTUException
    {
        Map<Lane, Length.Rel> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes.keySet())
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    public final Length.Rel projectedPosition(final Lane projectionLane, final RelativePosition relativePosition,
        final Time.Abs when) throws GTUException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (null != this.lanes.get(cseLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new Length.Rel(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                }
            }
        }
        throw new GTUException("GTU " + this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
        throws GTUException
    {
        if (null == lane)
        {
            throw new GTUException("lane is null");
        }
        synchronized (this.lock)
        {
            if (!this.lanes.containsKey(lane))
            {
                throw new GTUException("position() : GTU " + toString() + " is not on lane " + lane);
            }
            if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
            {
                // DO NOT USE toString() here, as it will cause an endless loop...
                throw new GTUException("GTU " + getId() + " does not have a fractional position on " + lane.toString());
            }
            Length.Rel longitudinalPosition = lane.position(this.fractionalLinkPositions.get(lane.getParentLink()));
            if (longitudinalPosition == null)
            {
                // According to FindBugs; this cannot happen; PK is unsure whether FindBugs is correct.
                throw new GTUException("position(): GTU " + toString() + " no position for lane " + lane);
            }
            if (getOperationalPlan() == null)
            {
                // no valid operational plan, e.g. during generation of a new plan
                return longitudinalPosition.plus(relativePosition.getDx());
            }
            Length.Rel loc;
            try
            {
                if (this.lanes.get(lane).equals(GTUDirectionality.DIR_PLUS))
                {
                    loc =
                        longitudinalPosition.plus(getOperationalPlan().getTraveledDistance(when)).plus(
                            relativePosition.getDx());
                }
                else
                {
                    loc =
                        longitudinalPosition.minus(getOperationalPlan().getTraveledDistance(when)).plus(
                            relativePosition.getDx());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println(toString());
                System.err.println(this.lanes);
                System.err.println(this.fractionalLinkPositions);
                throw new GTUException(e);
            }
            if (Double.isNaN(loc.getSI()))
            {
                System.out.println("loc is NaN");
            }
            return loc;
        }
    }

    /**
     * Schedule the triggers for this GTU that are going to happen until the next evaluation time. Also schedule the
     * registration and deregistration of lanes when the vehicle enters or leaves them, at the exact right time. <br>
     * Note: when the GTU makes a lane change, the vehicle will be registered for both lanes during the entire maneuver.
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException should never happen
     * @throws GTUException when a branch is reached where the GTU does not know where to go next
     */
    private void scheduleTriggers() throws NetworkException, SimRuntimeException, GTUException
    {
        /*
         * Move the vehicle into any new lanes with the front, and schedule entrance during this time step and calculate the
         * current position based on the fractional position, because THE POSITION METHOD DOES NOT WORK FOR THIS. IT CALCULATES
         * THE POSITION BASED ON THE NEWLY CALCULATED ACCELERATION AND VELOCITY AND CAN THEREFORE MAKE AN ERROR.
         */
        double timestep = getOperationalPlan().getTotalDuration().si;
        // TODO WRONG - should be based on timeAtPosition() as the plan can have acc/dec/const segments
        double moveSI = getVelocity().getSI() * timestep + 0.5 * getAcceleration().getSI() * timestep * timestep;
        Map<Lane, GTUDirectionality> lanesCopy = new LinkedHashMap<Lane, GTUDirectionality>(this.lanes);
        for (Lane lane : lanesCopy.keySet()) // use a copy because this.lanes can change
        {
            // schedule triggers on this lane
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            double sign = lanesCopy.get(lane).equals(GTUDirectionality.DIR_PLUS) ? 1.0 : -1.0;
            if (lanesCopy.get(lane).equals(GTUDirectionality.DIR_PLUS))
            {
                lane.scheduleTriggers(this, referenceStartSI, moveSI);
            }
            else
            {
                // TODO extra argument for DIR_MINUS driving direction?
                lane.scheduleTriggers(this, referenceStartSI - moveSI, moveSI);
            }

            // determine when our FRONT will pass the end of this registered lane in case of driving DIR_PLUS or
            // the start of this registered lane when driving DIR_MINUS.
            // if the time is earlier than the end of the timestep: schedule the enterLane method.
            // TODO look if more lanes are entered in one timestep, and continue the algorithm with the remainder of the time...
            double frontPosSI = referenceStartSI + sign * getFront().getDx().getSI();
            double nextFrontPosSI = frontPosSI + sign * moveSI;

            // LANE WE COME FROM IS IN PLUS DIRECTION
            if (lanesCopy.get(lane).equals(GTUDirectionality.DIR_PLUS))
            {
                if (frontPosSI <= lane.getLength().si && nextFrontPosSI > lane.getLength().si)
                {
                    Lane nextLane = determineNextLane(lane);
                    GTUDirectionality direction = lane.nextLanes(getGTUType()).get(nextLane);
                    /*
                     * We have to register the position at the previous timestep to keep calculations consistent. And we have to
                     * correct for the position of the reference point. The idea is that we register the vehicle 'before' the
                     * entrance of the new lane at the time of the last timestep, so for a DIR_PLUS on a negative position, and
                     * for a DIR_MINUS on a position beyond the length of the next lane.
                     */
                    if (direction.equals(GTUDirectionality.DIR_PLUS))
                    {
                        Length.Rel refPosAtLastTimestep =
                            new Length.Rel(-(lane.getLength().si - frontPosSI) - getFront().getDx().si, LengthUnit.SI);
                        enterLane(nextLane, refPosAtLastTimestep, direction);
                        // schedule any sensor triggers on this lane for the remainder time
                        nextLane.scheduleTriggers(this, refPosAtLastTimestep.getSI(), moveSI);
                    }
                    else if (direction.equals(GTUDirectionality.DIR_MINUS))
                    {
                        Length.Rel refPosAtLastTimestep =
                            new Length.Rel(nextLane.getLength().si + (lane.getLength().si - frontPosSI)
                                + getFront().getDx().si, LengthUnit.SI);
                        enterLane(nextLane, refPosAtLastTimestep, direction);
                        // schedule any sensor triggers on this lane for the remainder time
                        // TODO extra argument for DIR_MINUS driving direction?
                        nextLane.scheduleTriggers(this, refPosAtLastTimestep.getSI() - moveSI, moveSI);
                    }
                    else
                    {
                        throw new NetworkException("scheduleTriggers DIR_PLUS for GTU " + toString() + ", nextLane "
                            + nextLane + ", direction not DIR_PLUS or DIR_MINUS");
                    }
                }
            }

            // LANE WE COME FROM IS IN MINUS DIRECTION
            else if (lanesCopy.get(lane).equals(GTUDirectionality.DIR_MINUS))
            {
                if (frontPosSI >= 0.0 && nextFrontPosSI < 0.0)
                {
                    Lane prevLane = determinePrevLane(lane);
                    GTUDirectionality direction = lane.prevLanes(getGTUType()).get(prevLane);
                    /*
                     * We have to register the position at the previous timestep to keep calculations consistent. And we have to
                     * correct for the position of the reference point. The idea is that we register the vehicle 'before' the
                     * entrance of the new lane at the time of the last timestep, so for a DIR_MINUS on a negative position, and
                     * for a DIR_PLUS on a position beyond the length of the next lane.
                     */
                    if (direction.equals(GTUDirectionality.DIR_MINUS))
                    {
                        Length.Rel refPosAtLastTimestep =
                            new Length.Rel(prevLane.getLength().si + frontPosSI + getFront().getDx().si, LengthUnit.SI);
                        enterLane(prevLane, refPosAtLastTimestep, direction);
                        // schedule any sensor triggers on this lane for the remainder time
                        prevLane.scheduleTriggers(this, refPosAtLastTimestep.getSI() - moveSI, moveSI);
                    }
                    else if (direction.equals(GTUDirectionality.DIR_PLUS))
                    {
                        Length.Rel refPosAtLastTimestep =
                            new Length.Rel(-frontPosSI - getFront().getDx().si, LengthUnit.SI);
                        enterLane(prevLane, refPosAtLastTimestep, direction);
                        // schedule any sensor triggers on this lane for the remainder time
                        // TODO extra argument for DIR_MINUS driving direction?
                        prevLane.scheduleTriggers(this, refPosAtLastTimestep.getSI(), moveSI);
                    }
                    else
                    {
                        throw new NetworkException("scheduleTriggers DIR_MINUS for GTU " + toString() + ", prevLane "
                            + prevLane + ", direction not DIR_PLUS or DIR_MINUS");
                    }
                }
            }

            else
            {
                throw new NetworkException("scheduleTriggers for GTU " + toString() + ", lane " + lane
                    + ", direction not DIR_PLUS or DIR_MINUS");
            }
        }

        // move the vehicle out of any lanes with the BACK, and schedule exit during this time step
        for (Lane lane : this.lanes.keySet())
        {
            // determine when our REAR will pass the end of this registered lane.
            // if the time is earlier than the end of the timestep: schedule the exitLane method at the END of this timestep
            // TODO look if more lanes are exited in one timestep, and continue the algorithm with the remainder of the time...
            double referenceStartSI = this.fractionalLinkPositions.get(lane.getParentLink()) * lane.getLength().getSI();
            double sign = this.lanes.get(lane).equals(GTUDirectionality.DIR_PLUS) ? 1.0 : -1.0;
            double rearPosSI = referenceStartSI + sign * getRear().getDx().getSI();

            if (this.lanes.get(lane).equals(GTUDirectionality.DIR_PLUS))
            {
                if (rearPosSI <= lane.getLength().si && rearPosSI + moveSI > lane.getLength().si)
                {
                    getSimulator().scheduleEventRel(new Time.Rel(timestep - Math.ulp(timestep), TimeUnit.SI), this,
                        this, "leaveLane", new Object[]{lane, new Boolean(true)}); // TODO should be false?
                }
            }
            else
            {
                if (rearPosSI >= 0.0 && rearPosSI - moveSI < 0.0)
                {
                    getSimulator().scheduleEventRel(new Time.Rel(timestep - Math.ulp(timestep), TimeUnit.SI), this,
                        this, "leaveLane", new Object[]{lane, new Boolean(true)}); // XXX: should be false?
                }
            }
        }
    }

    /**
     * XXX: direction dependent... <br>
     * @param lane the lane to find the successor for
     * @return the next lane for this GTU
     * @throws NetworkException when no next lane exists or the route branches into multiple next lanes
     * @throws GTUException when no route could be found or the routeNavigator returns null
     */
    private Lane determineNextLane(final Lane lane) throws NetworkException, GTUException
    {
        Lane nextLane = null;
        if (lane.nextLanes(getGTUType()).size() == 0)
        {
            throw new NetworkException(this + " - lane " + lane + " does not have a successor");
        }
        if (lane.nextLanes(getGTUType()).size() == 1)
        {
            nextLane = lane.nextLanes(getGTUType()).keySet().iterator().next();
        }
        else
        {
            if (!(getStrategicalPlanner() instanceof LaneBasedStrategicalRoutePlanner))
            {
                throw new GTUException(this + " reaches branch but has no route navigator");
            }
            Node nextNode =
                ((LaneBasedStrategicalRoutePlanner) getStrategicalPlanner()).nextNode(lane.getParentLink(),
                    GTUDirectionality.DIR_PLUS, getGTUType());
            if (null == nextNode)
            {
                throw new GTUException(this + " reaches branch and the route returns null as nextNodeToVisit");
            }
            int continuingLaneCount = 0;
            for (Lane candidateLane : lane.nextLanes(getGTUType()).keySet())
            {
                if (null != this.lanes.get(candidateLane))
                {
                    continue; // Already on this lane
                }
                // XXX Hack - this should be done more considerate -- fails at loops...
                if (nextNode == candidateLane.getParentLink().getEndNode()
                    || nextNode == candidateLane.getParentLink().getStartNode())
                {
                    nextLane = candidateLane;
                    continuingLaneCount++;
                }
            }
            if (continuingLaneCount == 0)
            {
                throw new NetworkException(this + " reached branch and the route specifies a nextNodeToVisit ("
                    + nextNode + ") that is not a next node " + "at this branch at ("
                    + lane.getParentLink().getEndNode() + ")");
            }
            if (continuingLaneCount > 1)
            {
                throw new NetworkException(this
                    + " reached branch and the route specifies multiple lanes to continue on at this branch ("
                    + lane.getParentLink().getEndNode() + "). This is not yet supported");
            }
        }
        return nextLane;
    }

    /**
     * XXX: direction dependent... <br>
     * @param lane the lane to find the predecessor for
     * @return the next lane for this GTU
     * @throws NetworkException when no next lane exists or the route branches into multiple next lanes
     * @throws GTUException when no route could be found or the routeNavigator returns null
     */
    private Lane determinePrevLane(final Lane lane) throws NetworkException, GTUException
    {
        Lane prevLane = null;
        if (lane.prevLanes(getGTUType()).size() == 0)
        {
            throw new NetworkException(this + " - lane " + lane + " does not have a predecessor");
        }
        if (lane.prevLanes(getGTUType()).size() == 1)
        {
            prevLane = lane.prevLanes(getGTUType()).keySet().iterator().next();
        }
        else
        {
            if (!(getStrategicalPlanner() instanceof LaneBasedStrategicalRoutePlanner))
            {
                throw new GTUException(this + " reaches branch but has no route navigator");
            }
            Node prevNode =
                ((LaneBasedStrategicalRoutePlanner) getStrategicalPlanner()).nextNode(lane.getParentLink(),
                    GTUDirectionality.DIR_MINUS, getGTUType());
            if (null == prevNode)
            {
                throw new GTUException(this + " reaches branch and the route returns null as nextNodeToVisit");
            }
            int continuingLaneCount = 0;
            for (Lane candidateLane : lane.prevLanes(getGTUType()).keySet())
            {
                if (null != this.lanes.get(candidateLane))
                {
                    continue; // Already on this lane
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
                throw new NetworkException(this + " reached branch and the route specifies a nextNodeToVisit ("
                    + prevNode + ") that is not a next node " + "at this branch at ("
                    + lane.getParentLink().getStartNode() + ")");
            }
            if (continuingLaneCount > 1)
            {
                throw new NetworkException(this
                    + " reached branch and the route specifies multiple lanes to continue on at this branch ("
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
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time.Abs when)
        throws GTUException
    {
        Map<Lane, Double> positions = new LinkedHashMap<>();
        for (Lane lane : this.lanes.keySet())
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double
        fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
            throws GTUException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition)
        throws GTUException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public LanePerceptionFull getPerception()
    {
        return (LanePerceptionFull) super.getPerception();
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedStrategicalPlanner getStrategicalPlanner()
    {
        return (LaneBasedStrategicalPlanner) super.getStrategicalPlanner();
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedDrivingCharacteristics getDrivingCharacteristics()
    {
        return getStrategicalPlanner().getDrivingCharacteristics();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public void destroy()
    {
        synchronized (this.lock)
        {
            Set<Lane> laneSet = new HashSet<>(this.lanes.keySet()); // Operate on a copy of the key set
            for (Lane lane : laneSet)
            {
                leaveLane(lane, true);
            }
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
    public String toString()
    {
        return String.format("GTU " + getId());
    }
}
