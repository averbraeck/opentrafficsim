package org.opentrafficsim.core.gtu.lane;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.CompleteRouteNavigator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * Special GTU that cannot move, but it can be seen by other GTUs.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 15 jul. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractTrafficLight extends AbstractGTU implements LaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20150624L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** animation. */
    private Renderable2D animation;

    /** the lane of the block. */
    final Lane lane;

    /** the position of the block on the lane. */
    final Length.Rel position;

    /** blocking GTU type. */
    public static final GTUType BLOCK_GTU;

    /** null length. */
    private static final Length.Rel LENGTH_REL_0 = new Length.Rel(0.0, METER);

    /** null length. */
    private static final Length.Abs LENGTH_ABS_0 = new Length.Abs(0.0, METER);

    /** null speed. */
    private static final Speed.Abs SPEED_ABS_0 = new Speed.Abs(0.0, METER_PER_SECOND);

    /** null time. */
    private static Time.Abs TIME_ABS_0 = new Time.Abs(0.0, SECOND);

    /** null acceleration. */
    private static final Acceleration.Abs ACCELERATION_ABS_0 = new Acceleration.Abs(0.0, METER_PER_SECOND_2);

    /** the front, back, and reference positions; all at the same place. */
    private static final Map<RelativePosition.TYPE, RelativePosition> RELATIVE_POSITIONS = new LinkedHashMap<>();

    static
    {
        BLOCK_GTU = GTUType.makeGTUType("BLOCK");

        RELATIVE_POSITIONS.put(RelativePosition.FRONT, new RelativePosition(LENGTH_REL_0, LENGTH_REL_0, LENGTH_REL_0,
            RelativePosition.FRONT));
        RELATIVE_POSITIONS.put(RelativePosition.REAR, new RelativePosition(LENGTH_REL_0, LENGTH_REL_0, LENGTH_REL_0,
            RelativePosition.REAR));
        RELATIVE_POSITIONS.put(RelativePosition.REFERENCE, RelativePosition.REFERENCE_POSITION);
    }

    /**
     * @param name the name or id of the traffic light
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public AbstractTrafficLight(final String name, final Lane lane, final Length.Rel position,
        final OTSDEVSSimulatorInterface simulator) throws GTUException, NetworkException, NamingException
    {
        super(name, BLOCK_GTU, new CompleteRouteNavigator(new CompleteRoute("")));
        this.simulator = simulator;
        this.position = position;
        this.lane = lane;

        // register the block on the lanes
        lane.addGTU(this, position);
    }

    private boolean blocked = true;

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
                this.lane.removeGTU(this);
            }
            else if (!this.blocked && blocked)
            {
                // add ourselves to the lane
                this.lane.addGTU(this, this.position);
            }
            this.blocked = blocked;
        }
        catch (NetworkException exception)
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
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getLength()
    {
        return LENGTH_REL_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel getWidth()
    {
        return LENGTH_REL_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed.Abs getMaximumVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
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
        return RELATIVE_POSITIONS.get(RelativePosition.FRONT);
    }

    /** {@inheritDoc} */
    @Override
    public final Speed.Abs getVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<TYPE, RelativePosition> getRelativePositions()
    {
        return RELATIVE_POSITIONS;
    }

    /** {@inheritDoc} */
    @Override
    public final void destroy()
    {
        // nothing to do.
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs getAcceleration()
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        // TODO solve problem when point is still on previous lane.
        Length.Rel longitudinalPos;
        try
        {
            longitudinalPos = position(lane, getReference());
            double fraction = (longitudinalPos.getSI() + getLength().getSI() / 2.0) / lane.getLength().getSI();
            LineString line = lane.getCenterLine().getLineString();
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
                    new Coordinate(c.x + (fraction - useFraction) * 100 * (cb.x - c.x), c.y + (fraction - useFraction)
                        * 100 * (cb.y - c.y), c.z);
            }
            if (Double.isNaN(c.x))
            {
                System.out.println("Bad");
            }
            return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0,
                angle);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        double dx = 2;
        double dy = 2;
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Abs getOdometer()
    {
        return LENGTH_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed.Abs getLongitudinalVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed.Abs getLongitudinalVelocity(final Time.Abs when)
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs getAcceleration(final Time.Abs when)
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed.Abs getLateralVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Abs getLastEvaluationTime()
    {
        return TIME_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Abs getNextEvaluationTime()
    {
        return TIME_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final void enterLane(final Lane lane, final Length.Rel position) throws NetworkException
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void leaveLane(final Lane lane)
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition) throws NetworkException
    {
        Map<Lane, Length.Rel> map = new HashMap<Lane, Length.Rel>();
        map.put(this.lane, this.position);
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Length.Rel> positions(final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException
    {
        return positions(relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition) throws NetworkException
    {
        if (this.lane.equals(lane))
        {
            return this.position;
        }
        throw new NetworkException("BLOCK GTU not on lane " + lane);
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Rel position(final Lane lane, final RelativePosition relativePosition, final Time.Abs when)
        throws NetworkException
    {
        return position(lane, relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws NetworkException
    {
        Map<Lane, Double> map = new HashMap<Lane, Double>();
        map.put(this.lane, this.position.getSI() / this.lane.getLength().getSI());
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, Time.Abs when)
        throws NetworkException
    {
        Map<Lane, Double> result = new HashMap<Lane, Double>();
        result.put(this.lane, this.position.getSI() / this.lane.getLength().getSI());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public double fractionalPosition(Lane lane, RelativePosition relativePosition, Time.Abs when)
        throws NetworkException
    {
        return this.position.getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public double fractionalPosition(Lane lane, RelativePosition relativePosition) throws NetworkException
    {
        return this.position.getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public Length.Rel projectedPosition(Lane projectionLane, RelativePosition relativePosition, Time.Abs when)
        throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU headway(Length.Rel maxDistance) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU headway(Lane lane, Length.Rel maxDistance) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneBasedGTU> parallel(Lane lane, Time.Abs when) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneBasedGTU> parallel(LateralDirectionality lateralDirection, Time.Abs when) throws NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Lane bestAccessibleAdjacentLane(Lane currentLane, LateralDirectionality lateralDirection,
        Length.Rel longitudinalPosition)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Time.Abs timeAtDistance(Length.Rel distance)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Time.Rel deltaTimeForDistance(Length.Rel distance)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GTUFollowingModel getGTUFollowingModel()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LaneChangeDistanceAndDirection getLaneChangeDistanceAndDirection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "AbstractTrafficLight [lane=" + this.lane + ", position=" + this.position + "]";
    }

}
