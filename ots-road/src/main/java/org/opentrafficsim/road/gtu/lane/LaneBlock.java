package org.opentrafficsim.road.gtu.lane;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.CompleteRouteNavigator;
import org.opentrafficsim.road.gtu.animation.DefaultBlockAnimation;
import org.opentrafficsim.road.gtu.animation.LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.following.HeadwayGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Special GTU that cannot move, but it can be seen by other GTUs.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 15 jul. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneBlock extends AbstractGTU implements LaneBasedGTU
{
    /** */
    private static final long serialVersionUID = 20150624L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** animation. */
    private Renderable2D animation;

    /** the lane of the block. */
    private final Lane lane;

    /** the position of the block on the lane. */
    private final Length.Rel position;

    /** the cached location for animation. */
    private DirectedPoint location = null;

    /** the cached bounds for animation. */
    private Bounds bounds = null;

    /** blocking GTU type. */
    public static final GTUType BLOCK_GTU;

    /** null length. */
    private static final Length.Rel LENGTH_REL_0 = new Length.Rel(0.0, LengthUnit.METER);

    /** null length. */
    private static final Length.Abs LENGTH_ABS_0 = new Length.Abs(0.0, LengthUnit.METER);

    /** null speed. */
    private static final Speed SPEED_ABS_0 = new Speed(0.0, SpeedUnit.METER_PER_SECOND);

    /** null time. */
    private static Time.Abs TIME_ABS_0 = new Time.Abs(0.0, TimeUnit.SECOND);

    /** null acceleration. */
    private static final Acceleration ACCELERATION_ABS_0 = new Acceleration(0.0, AccelerationUnit.METER_PER_SECOND_2);

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
     * @throws NetworkException when the GTU cannot be placed on the given lane
     */
    public LaneBlock(final Lane lane, final Length.Rel position, final OTSDEVSSimulatorInterface simulator,
        final Class<? extends Renderable2D> animationClass) throws GTUException, NetworkException, NamingException
    {
        super(UUID.randomUUID().toString(), BLOCK_GTU, new CompleteRouteNavigator(new CompleteRoute("")));
        this.simulator = simulator;
        this.position = position;
        this.lane = lane;

        // register the block on the lanes
        lane.addGTU(this, position);

        // animation
        try
        {
            new DefaultBlockAnimation(this, this.simulator);
            if (simulator instanceof OTSAnimatorInterface && animationClass != null)
            {
                // TODO use animationClass
            }
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
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
    public final Speed getMaximumVelocity()
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
    public final Speed getVelocity()
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
    public final Acceleration getAcceleration()
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        if (this.location == null)
        {
            try
            {
                this.location = this.lane.getCenterLine().getLocation(this.position);
                this.location.z = this.lane.getLocation().z + 0.01;
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
                return null;
            }
        }
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        if (this.bounds == null)
        {
            this.bounds =
                new BoundingBox(new Point3d(-0.4, -this.lane.getWidth(0.0).getSI() * 0.4, 0.0), new Point3d(0.4,
                    this.lane.getWidth(0.0).getSI() * 0.4, this.lane.getLocation().z + 0.01));
        }
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public final Length.Abs getOdometer()
    {
        return LENGTH_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getLongitudinalVelocity()
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getLongitudinalVelocity(final Time.Abs when)
    {
        return SPEED_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration(final Time.Abs when)
    {
        return ACCELERATION_ABS_0;
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getLateralVelocity()
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
        return "LaneBlock [lane=" + this.lane + ", position=" + this.position + "]";
    }

}
