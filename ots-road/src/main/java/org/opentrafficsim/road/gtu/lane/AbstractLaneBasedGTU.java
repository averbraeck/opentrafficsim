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
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedDrivingCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
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
    private final Map<Link, Double> fractionalLinkPositions = new LinkedHashMap<>();

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
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws SimRuntimeException when the move method cannot be scheduled
     * @throws GTUException when gtuFollowingModel is null
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractLaneBasedGTU(final String id, final GTUType gtuType,
        final Set<DirectedLanePosition> initialLongitudinalPositions, final Speed initialSpeed,
        final OTSDEVSSimulatorInterface simulator, final LaneBasedStrategicalPlanner strategicalPlanner,
        final LanePerception perception) throws NetworkException, SimRuntimeException, GTUException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLongitudinalPositions.iterator().next()
            .getLocation());

        // register the GTU in the perception module
        getPerception().setGtu(this);

        // register the GTU on the lanes
        for (DirectedLanePosition directedLanePosition : initialLongitudinalPositions)
        {
            Lane lane = directedLanePosition.getLane();
            if (lane == null || directedLanePosition.getGtuDirection() == null
                || directedLanePosition.getPosition() == null)
            {
                throw new GTUException("Constructing GTU - one of the fields of directedLanePosition is null: "
                    + directedLanePosition.toString());
            }
            this.lanes.put(lane, directedLanePosition.getGtuDirection());
            this.fractionalLinkPositions.put(lane.getParentLink(), lane.fraction(directedLanePosition.getPosition()));
            lane.addGTU(this, directedLanePosition.getPosition());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void enterLane(final Lane lane, final Length.Rel position, final GTUDirectionality gtuDirection)
        throws NetworkException
    {
        if (this.lanes.containsKey(lane))
        {
            System.err.println("GTU " + toString() + " is already registered on this lane: " + lane);
            return;
        }
        if (gtuDirection == null)
        {
            throw new NetworkException("GTU " + this + " Entering lane " + lane + " - gtuDirection is null");
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
        return this.lanes;
    }

    /** {@inheritDoc} */
    @Override
    protected final void move(final DirectedPoint fromLocation) throws SimRuntimeException, NetworkException,
        GTUException
    {
        // Only carry out move() if we still have lane(s) to drive on.
        // Note: a (Sink) trigger can have 'destroyed' us between the previous evaluation step and this one.
        if (this.lanes.isEmpty())
        {
            destroy();
            return; // Done; do not re-schedule execution of this move method.
        }

        // generate the next operational plan and carry it out
        super.move(fromLocation);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition) throws NetworkException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException
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
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition) throws NetworkException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    public final Length.Rel projectedPosition(final Lane projectionLane, final RelativePosition relativePosition,
        final Time.Abs when) throws NetworkException
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
        throw new NetworkException("GTU " + this + " is not on any lane of Link " + link);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException
    {
        if (null == lane)
        {
            throw new NetworkException("lane is null");
        }
        synchronized (this.lock)
        {
            if (!this.lanes.containsKey(lane))
            {
                throw new NetworkException("position() : GTU " + toString() + " is not on lane " + lane);
            }
            if (!this.fractionalLinkPositions.containsKey(lane.getParentLink()))
            {
                // DO NOT USE toString() here, as it will cause an endless loop...
                throw new NetworkException("GTU " + getId() + " does not have a fractional position on "
                    + lane.toString());
            }
            Length.Rel longitudinalPosition = lane.position(this.fractionalLinkPositions.get(lane.getParentLink()));
            if (longitudinalPosition == null)
            {
                // According to FindBugs; this cannot happen; PK is unsure whether FindBugs is correct.
                throw new NetworkException("position(): GTU " + toString() + " no position for lane " + lane);
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
                throw e;
            }
            if (Double.isNaN(loc.getSI()))
            {
                System.out.println("loc is NaN");
            }
            return loc;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws NetworkException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime().getTime());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException
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
            throws NetworkException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition)
        throws NetworkException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public LanePerception getPerception()
    {
        return (LanePerception) super.getPerception();
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
