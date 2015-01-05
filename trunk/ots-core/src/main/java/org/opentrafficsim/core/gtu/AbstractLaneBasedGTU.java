package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedGTU<ID> extends AbstractGTU<ID> implements LaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Time of last evaluation. */
    private DoubleScalar.Abs<TimeUnit> lastEvaluationTime;

    /** Time of next evaluation. */
    private DoubleScalar.Abs<TimeUnit> nextEvaluationTime;

    /** Longitudinal positions on one or more lanes. */
    private final Map<Lane, DoubleScalar.Rel<LengthUnit>> longitudinalPositions;

    /**
     * FIXME: temp for debugging purposes.
     * @return longitudinalPositions.
     */
    public final String getLanePositions()
    {
        return this.longitudinalPositions.toString();
    }

    /** Speed at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> speed;

    /** lateral velocity at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> lateralVelocity;

    /** Current acceleration (negative values indicate deceleration). */
    private DoubleScalar.Abs<AccelerationUnit> acceleration = new DoubleScalar.Abs<AccelerationUnit>(0,
        AccelerationUnit.METER_PER_SECOND_2);

    /** CarFollowingModel used by this Car. */
    private final GTUFollowingModel gtuFollowingModel;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes.
     * @param initialSpeed the initial speed of the car on the lane.
     * @param currentTime to initialize the evaluation. getSimulator() does not work yet, because the super constructors have
     *            not finished yet.
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
        final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
        final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Abs<TimeUnit> currentTime)
    {
        super(id, gtuType);
        this.gtuFollowingModel = gtuFollowingModel;
        this.lastEvaluationTime = currentTime;
        this.longitudinalPositions = new HashMap<>(initialLongitudinalPositions);
        
        // register the GTUs on the lane
        for (Lane lane : initialLongitudinalPositions.keySet())
        {
            lane.addGTU(this);
        }
        
        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.speed = new DoubleScalar.Abs<SpeedUnit>(initialSpeed);
        this.lateralVelocity = new DoubleScalar.Abs<SpeedUnit>(0.0, SpeedUnit.METER_PER_SECOND);
        this.nextEvaluationTime = currentTime;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(this.speed, Calc.accelerationTimesTime(this.getAcceleration(when), dT)).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity() throws RemoteException
    {
        return getLongitudinalVelocity(getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getLastEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.lastEvaluationTime);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getNextEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.nextEvaluationTime);
    }

    /**
     * Return the acceleration at a specified time.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time for which the acceleration must be returned
     * @return DoubleScalarAbs&lt;AccelerationUnit&gt;; the acceleration at the given time
     */
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration(final DoubleScalar.Abs<TimeUnit> when)
    {
        // Currently the acceleration is independent of when; it is constant during the evaluation interval
        return new DoubleScalar.Abs<AccelerationUnit>(this.acceleration);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLateralVelocity()
    {
        return new DoubleScalar.Abs<SpeedUnit>(this.lateralVelocity);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane)
    {
        this.longitudinalPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER));
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLane(final Lane lane)
    {
        this.longitudinalPositions.remove(lane);
    }

    /**
     * Set the new state.
     * @param cfmr GTUFollowingModelResult; the new state of this GTU
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    public final void setState(final GTUFollowingModelResult cfmr) throws NetworkException
    {
        // TODO test when vehicle moves to next lane in the network; has to be done with sensors.

        for (Lane lane : this.longitudinalPositions.keySet())
        {
            this.longitudinalPositions.put(lane, getPosition(lane, getFront(), this.nextEvaluationTime));
        }

        this.speed = getLongitudinalVelocity(this.nextEvaluationTime);
        // TODO add a sanity check that time is increasing
        this.lastEvaluationTime = this.nextEvaluationTime;
        this.nextEvaluationTime = cfmr.getValidUntil();
        this.acceleration = cfmr.getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Rel<LengthUnit>> getPositions(final RelativePosition relativePosition) throws NetworkException,
        RemoteException
    {
        return getPositions(relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> getPositions(final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = new HashMap<>();
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            positions.put(lane, getPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getPosition(final Lane lane, final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        return getPosition(lane, relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> getPosition(final Lane lane, final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = this.longitudinalPositions.get(lane);
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        DoubleScalar.Rel<LengthUnit> loc =
            DoubleScalar.minus(
                DoubleScalar.plus(DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                    Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable(),
                relativePosition.getDx()).immutable();
        return loc;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final DoubleScalar.Abs<LengthUnit> maxDistance) throws RemoteException
    {
        // TODO headway(maxDistance)
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final Lane lane, final DoubleScalar.Abs<LengthUnit> maxDistance)
        throws RemoteException
    {
        // TODO headway(lane, maxDistance)
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final Lane lane, final DoubleScalar.Abs<LengthUnit> maxDistance)
        throws RemoteException
    {
        // TODO headwayGTU(lane, maxDistance)
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final DoubleScalar.Abs<LengthUnit> maxDistance) throws RemoteException
    {
        // TODO headwayGTU(maxDistance)
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
        final DoubleScalar.Abs<LengthUnit> maxDistance) throws RemoteException, NetworkException
    {
        return headway(otherGTU, maxDistance, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
        final DoubleScalar.Abs<LengthUnit> maxDistance, final DoubleScalar.Abs<TimeUnit> when) throws RemoteException,
        NetworkException
    {
        if (otherGTU == null)
        {
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }

        // search for the otherGTU on the current lanes we are registered on.
        for (Lane lane : getPositions(getFront()).keySet())
        {
            if (lane.getGtuList().contains(otherGTU))
            {
                double distanceM =
                    otherGTU.getPosition(lane, otherGTU.getFront(), when).getSI()
                        - this.getPosition(lane, this.getFront(), when).getSI();
                double maxD = maxDistance.getSI();
                if ((maxD > 0.0 && distanceM > 0.0) || (maxD < 0.0 && distanceM < 0.0))
                {
                    return new DoubleScalar.Rel<LengthUnit>(distanceM, LengthUnit.METER);
                }
                return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
            }
        }

        // TODO the otherGTU was not on one of the current lanes.
        return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
    }

    /**
     * Retrieve the GTUFollowingModel of this GTU.
     * @return GTUFollowingModel
     */
    public final GTUFollowingModel getGTUFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        Lane lane = this.longitudinalPositions.keySet().iterator().next();
        try
        {
            // TODO solve problem when point is still on previous lane.
            double fraction = (getPosition(lane, getFront()).getSI() - getLength().getSI() / 2.0) / lane.getLength().getSI();
            LineString line = lane.getOffsetLine();
            LengthIndexedLine lil = new LengthIndexedLine(line);
            Coordinate c = lil.extractPoint(fraction * line.getLength());
            // HACK (FIXME)
            c.z = 0d;
            Coordinate ca =
                fraction <= 0.01 ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
            Coordinate cb =
                fraction >= 0.99 ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
            double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
            return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0, angle);
        }
        catch (NetworkException ne)
        {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * Description of Car at specified time.
     * @param lane the position on this lane will be returned.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
    {
        double pos = Double.NaN;
        try
        {
            pos = this.getPosition(lane, getFront(), when).getSI();
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", getId(),
            this.lastEvaluationTime.getSI(), this.nextEvaluationTime.getSI(), pos, this.getLongitudinalVelocity(when)
                .getSI(), this.getAcceleration(when).getSI());
    }

}
