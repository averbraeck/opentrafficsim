package org.opentrafficsim.road.gtu.lane.object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.immutablecollections.Immutable;
import org.opentrafficsim.core.immutablecollections.ImmutableHashMap;
import org.opentrafficsim.core.immutablecollections.ImmutableHashSet;
import org.opentrafficsim.core.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.immutablecollections.ImmutableSet;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.language.d3.BoundingBox;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AbstractTrafficLight extends AbstractGTU implements LaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The lane of the block. */
    private final Lane laneTL;

    /** The position of the block on the lane. */
    private final Length positionTL;

    /** Light blocked or not? */
    private boolean blocked = true;

    /** Blocking GTU type. */
    private static final GTUType BLOCK_GTU;

    /** the dummy contour positions. */
    private static final Set<RelativePosition> CONTOUR_POINTS = new HashSet<>();

    /** Relative position (0,0,0). */
    public static final Map<RelativePosition.TYPE, RelativePosition> RELATIVE_POSITIONS = new HashMap<>();

    static
    {
        BLOCK_GTU = new GTUType("BLOCK");
        RELATIVE_POSITIONS.put(RelativePosition.FRONT, new RelativePosition(Length.ZERO, Length.ZERO, Length.ZERO,
                RelativePosition.FRONT));
        RELATIVE_POSITIONS.put(RelativePosition.REAR, new RelativePosition(Length.ZERO, Length.ZERO, Length.ZERO,
                RelativePosition.REAR));
        RELATIVE_POSITIONS.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);
        RELATIVE_POSITIONS.put(RelativePosition.CENTER, RelativePosition.REFERENCE_POSITION);
    }

    /**
     * @param name the name or id of the traffic light
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @param network the network that the GTU is initially registered in
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     * @throws OTSGeometryException x
     * @throws SimRuntimeException x
     */
    public AbstractTrafficLight(final String name, final Lane lane, final Length position,
            final OTSDEVSSimulatorInterface simulator, final OTSNetwork network) throws GTUException, NetworkException,
            NamingException, SimRuntimeException, OTSGeometryException
    {
        super(name, BLOCK_GTU, simulator, network);
        this.positionTL = position;
        this.laneTL = lane;

        // register the block on the lanes
        lane.addGTU(this, position);
    }

    /**
     * @param blocked set blocked
     */
    public final void setBlocked(final boolean blocked)
    {
        try
        {
            if (this.blocked && !blocked)
            {
                // remove ourselves from the lane
                this.laneTL.removeGTU(this);
            }
            else if (!this.blocked && blocked)
            {
                // add ourselves to the lane
                this.laneTL.addGTU(this, this.positionTL);
            }
            this.blocked = blocked;
        }
        catch (GTUException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return blocked
     */
    public final boolean isBlocked()
    {
        return this.blocked;
    }

    /**
     * @return lane
     */
    public final Lane getLane()
    {
        return this.laneTL;
    }

    /**
     * @return positionTL
     */
    public final Length getPositionTL()
    {
        return this.positionTL;
    }
    
    /* ========================================================================================================= */

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return Length.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getWidth()
    {
        return Length.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getMaximumSpeed()
    {
        return Speed.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getFront()
    {
        return RELATIVE_POSITIONS.get(RelativePosition.FRONT);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getRear()
    {
        return RELATIVE_POSITIONS.get(RelativePosition.REAR);
    }

    /** {@inheritDoc} */
    @Override
    public final RelativePosition getCenter()
    {
        return RELATIVE_POSITIONS.get(RelativePosition.CENTER);
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableMap<TYPE, RelativePosition> getRelativePositions()
    {
        return new ImmutableHashMap<>(RELATIVE_POSITIONS, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        double dx = 2;
        double dy = 1;
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /** {@inheritDoc} */
    @Override
    public final BehavioralCharacteristics getBehavioralCharacteristics()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, GTUDirectionality> getLanes()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void enterLane(final Lane lane, final Length position, final GTUDirectionality gtuDirection) throws GTUException
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void leaveLane(final Lane lane)
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition) throws GTUException
    {
        Map<Lane, Length> map = new HashMap<Lane, Length>();
        map.put(this.laneTL, this.positionTL);
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length> positions(final RelativePosition relativePosition, final Time when) throws GTUException
    {
        return positions(relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        if (this.laneTL.equals(lane))
        {
            return this.positionTL;
        }
        throw new GTUException("TrafficLight " + this.getId() + " not on lane " + lane);
    }

    /** {@inheritDoc} */
    @Override
    public final Length position(final Lane lane, final RelativePosition relativePosition, final Time when) throws GTUException
    {
        return position(lane, relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws GTUException
    {
        Map<Lane, Double> map = new HashMap<Lane, Double>();
        map.put(this.laneTL, this.positionTL.getSI() / this.laneTL.getLength().getSI());
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        return fractionalPositions(relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition) throws GTUException
    {
        if (this.laneTL.equals(lane))
        {
            return this.positionTL.getSI() / this.laneTL.getLength().getSI();
        }
        throw new GTUException("TrafficLight " + this.getId() + " not on lane " + lane);
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        return fractionalPosition(lane, relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Length projectedPosition(final Lane projectionLane, final RelativePosition relativePosition, final Time when)
            throws GTUException
    {
        CrossSectionLink link = projectionLane.getParentLink();
        for (CrossSectionElement cse : link.getCrossSectionElementList())
        {
            if (cse instanceof Lane)
            {
                Lane cseLane = (Lane) cse;
                if (cseLane.equals(projectionLane))
                {
                    double fractionalPosition = fractionalPosition(cseLane, relativePosition, when);
                    return new Length(projectionLane.getLength().getSI() * fractionalPosition, LengthUnit.SI);
                }
            }
        }
        throw new GTUException("TrafficLight " + getId() + " is not on any lane of Link " + link);
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
    public final void addTrigger(final Lane lane, final SimEvent<OTSSimTimeDouble> event)
    {
        // Nothing to do as this is not really a GTU.
    }

    /** {@inheritDoc} */
    @Override
    public final ImmutableSet<RelativePosition> getContourPoints()
    {
        return new ImmutableHashSet<>(CONTOUR_POINTS, Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "AbstractTrafficLight [laneTL=" + this.laneTL + ", positionTL=" + this.positionTL + ", blocked=" + this.blocked
                + "]";
    }
}
