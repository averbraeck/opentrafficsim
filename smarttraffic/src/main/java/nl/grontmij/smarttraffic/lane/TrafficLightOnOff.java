package nl.grontmij.smarttraffic.lane;

import java.awt.Color;
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
import org.opentrafficsim.core.gtu.animation.LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
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
public class TrafficLightOnOff extends AbstractGTU<Integer> implements LaneBasedGTU<Integer>
{
    /** */
    private static final long serialVersionUID = 20150624L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** animation. */
    private Renderable2D animation;

    /** the lane of the block. */
    final Lane<?, ?> lane;
    
    /**   */
    private boolean blocked;

    /** the cached location for animation. */
    private DirectedPoint location = null;
    
    /** the cached bounds for animation. */
    private Bounds bounds = null;

	/** the position of the block on the lane. */
    final DoubleScalar.Rel<LengthUnit> position;

	private StopLineLane stopLine;


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
    public TrafficLightOnOff(final Lane<?, ?> lane, final DoubleScalar.Rel<LengthUnit> position,
        final Class<? extends Renderable2D> animationClass, StopLineLane stopLine) throws GTUException,
        RemoteException, NetworkException, NamingException
    {
        super(0, BLOCK_GTU, new Route(""));
        //this.simulator = simulator;
        this.position = position;
        this.lane = lane;
        this.stopLine = stopLine;

        // register the block on the lanes
        //lane.addGTU(this, position);

        new DefaultTrafficLightOnOffAnimation(this, this.simulator);
        // animation
        if (simulator instanceof OTSAnimatorInterface && animationClass != null)
        {
            // TODO
        }
    }



    public StopLineLane getStopLine() {
		return stopLine;
	}
    /**
     * @return blocked
     */
    public boolean isBlocked()
    {
        return this.blocked;
    }


    public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}
    
    public void changeFromColor(DoubleScalar.Abs<TimeUnit> when) throws RemoteException {
        try
        {
            this.simulator.scheduleEventAbs(when, this, this,
                "changeColor", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }
    
    protected void changeColor(DoubleScalar.Abs<TimeUnit> when)
    {

        try
        {
            if (this.blocked)
            {
                // add ourselves to the lane
                this.lane.addGTU(this, this.position);
                this.getStopLine().setColorTrafficLight(Color.RED);
            }
            else
            {
                // remove ourselves from the lane
                this.lane.removeGTU(this);
                this.getStopLine().setColorTrafficLight(Color.GREEN);

            }
        }
        catch (RemoteException | NetworkException exception)
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
    public final Bounds getBounds() throws RemoteException
    {
        if (this.bounds == null)
        {
            this.bounds = new BoundingBox(new Point3d(-0.4, -this.lane.getWidth(0.0).getSI() * 0.4, 0.0), 
                new Point3d(0.4, this.lane.getWidth(0.0).getSI() * 0.4, this.lane.getLocation().z + 0.01));
        }
        return this.bounds;
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
