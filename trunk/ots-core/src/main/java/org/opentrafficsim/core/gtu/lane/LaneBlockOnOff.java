package org.opentrafficsim.core.gtu.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.animation.DefaultBlockOnOffAnimation;
import org.opentrafficsim.core.gtu.animation.LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

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
public class LaneBlockOnOff extends AbstractGTU<Integer> implements LaneBasedGTU<Integer>
{
    /** */
    private static final long serialVersionUID = 20150624L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** animation. */
    private Renderable2D animation;

    /** the lane of the block. */
    final Lane<?, ?> lane;

    /** the position of the block on the lane. */
    final DoubleScalar.Rel<LengthUnit> position;

    /** blocking GTU type. */
    public static final GTUType<String> BLOCK_GTU;

    /** null length. */
    private static final DoubleScalar.Rel<LengthUnit> LENGTH_REL_0 = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);

    /** null length. */
    private static final DoubleScalar.Abs<LengthUnit> LENGTH_ABS_0 = new DoubleScalar.Abs<LengthUnit>(0.0, LengthUnit.METER);

    /** null speed. */
    private static final DoubleScalar.Abs<SpeedUnit> SPEED_ABS_0 = new DoubleScalar.Abs<SpeedUnit>(0.0,
        SpeedUnit.METER_PER_SECOND);

    /** null time. */
    private static DoubleScalar.Abs<TimeUnit> TIME_ABS_0 = new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND);

    /** null acceleration. */
    private static final DoubleScalar.Abs<AccelerationUnit> ACCELERATION_ABS_0 = new DoubleScalar.Abs<AccelerationUnit>(0.0,
        AccelerationUnit.METER_PER_SECOND_2);

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
     * @param lane The lane where the block has to be put
     * @param position the position on the lane as a length
     * @param simulator the simulator to avoid NullPointerExceptions
     * @param animationClass Class&lt;? extends Renderable2D&gt;; the class for animation or null if no animation
     * @throws GTUException when GTU cannot be created.
     * @throws NamingException if an error occurs when adding the animation handler
     * @throws RemoteException when the simulator cannot be reached
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public LaneBlockOnOff(final Lane<?, ?> lane, final DoubleScalar.Rel<LengthUnit> position,
        final OTSDEVSSimulatorInterface simulator, final Class<? extends Renderable2D> animationClass) throws GTUException,
        RemoteException, NetworkException, NamingException
    {
        super(0, BLOCK_GTU, new Route(""));
        this.simulator = simulator;
        this.position = position;
        this.lane = lane;

        // register the block on the lanes
        lane.addGTU(this, position);

        new DefaultBlockOnOffAnimation(this, this.simulator);
        // animation
        if (simulator instanceof OTSAnimatorInterface && animationClass != null)
        {
            // TODO
        }
        try
        {
            this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(60.0, TimeUnit.SECOND), this, this,
                "changeColor", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    private boolean blocked = true;

    /**
     * @return blocked
     */
    public final boolean isBlocked()
    {
        return this.blocked;
    }

    protected void changeColor()
    {
        this.blocked = !this.blocked;

        try
        {
            if (this.blocked)
            {
                // add ourselves to the lane
                this.lane.addGTU(this, this.position);
            }
            else
            {
                // remove ourselves from the lane
                this.lane.removeGTU(this);

            }
            this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(60.0, TimeUnit.SECOND), this, this,
                "changeColor", null);
        }
        catch (SimRuntimeException | RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return lane
     */
    public final Lane<?, ?> getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getLength()
    {
        return LENGTH_REL_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getWidth()
    {
        return LENGTH_REL_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getMaximumVelocity()
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
    public final DoubleScalar.Abs<SpeedUnit> getVelocity() throws RemoteException
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
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration() throws RemoteException
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        // TODO solve problem when point is still on previous lane.
        DoubleScalar.Rel<LengthUnit> longitudinalPos;
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
                    new Coordinate(c.x + (fraction - useFraction) * 100 * (cb.x - c.x), c.y + (fraction - useFraction) * 100
                        * (cb.y - c.y), c.z);
            }
            if (Double.isNaN(c.x))
            {
                System.out.println("Bad");
            }
            return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0, angle);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        double dx = 2;
        double dy = 2;
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<LengthUnit> getOdometer() throws RemoteException
    {
        return LENGTH_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity() throws RemoteException
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration(final DoubleScalar.Abs<TimeUnit> when)
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLateralVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getLastEvaluationTime()
    {
        return TIME_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getNextEvaluationTime()
    {
        return TIME_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final void addFrontToSubsequentLane(Lane<?, ?> lane) throws RemoteException, NetworkException
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane<?, ?> lane, final DoubleScalar.Rel<LengthUnit> position) throws NetworkException
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLane(final Lane<?, ?> lane)
    {
        // do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> map = new HashMap<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>>();
        map.put(this.lane, this.position);
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane<?, ?>, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        return positions(relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane<?, ?> lane, final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        if (this.lane.equals(lane))
        {
            return this.position;
        }
        throw new NetworkException("BLOCK GTU not on lane " + lane);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane<?, ?> lane, final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        return position(lane, relativePosition);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane<?, ?>, Double> fractionalPositions(final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        Map<Lane<?, ?>, Double> map = new HashMap<Lane<?, ?>, Double>();
        map.put(this.lane, this.position.getSI() / this.lane.getLength().getSI());
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Lane<?, ?>, Double> fractionalPositions(RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
        throws NetworkException, RemoteException
    {
        Map<Lane<?, ?>, Double> result = new HashMap<Lane<?, ?>, Double>();
        result.put(this.lane, this.position.getSI() / this.lane.getLength().getSI());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public double fractionalPosition(Lane<?, ?> lane, RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
        throws NetworkException, RemoteException
    {
        return this.position.getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public double fractionalPosition(Lane<?, ?> lane, RelativePosition relativePosition) throws NetworkException,
        RemoteException
    {
        return this.position.getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Rel<LengthUnit> projectedPosition(Lane<?, ?> projectionLane, RelativePosition relativePosition,
        DoubleScalar.Abs<TimeUnit> when) throws NetworkException, RemoteException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU headway(Rel<LengthUnit> maxDistance) throws RemoteException, NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU headway(Lane<?, ?> lane, DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
        NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneBasedGTU<?>> parallel(Lane<?, ?> lane, DoubleScalar.Abs<TimeUnit> when) throws RemoteException,
        NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<LaneBasedGTU<?>> parallel(LateralDirectionality lateralDirection, DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Abs<TimeUnit> timeAtDistance(Rel<LengthUnit> distance)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Rel<TimeUnit> deltaTimeForDistance(Rel<LengthUnit> distance)
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
        return "LaneBlock [lane=" + this.lane + ", position=" + this.position + "]";
    }

}
