package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneLocation;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
    private final Map<Lane, DoubleScalar.Abs<LengthUnit>> longitudinalPositions;

    /** Speed at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> speed;

    /** Current acceleration (negative values indicate deceleration). */
    private DoubleScalar.Abs<AccelerationUnit> acceleration = new DoubleScalar.Abs<AccelerationUnit>(0,
            AccelerationUnit.METER_PER_SECOND_2);;

    /** CarFollowingModel used by this Car. */
    private final GTUFollowingModel gtuFollowingModel;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param length the maximum length of the GTU (parallel with driving direction).
     * @param width the maximum width of the GTU (perpendicular to driving direction).
     * @param maximumVelocity the maximum speed of the GTU (in the driving direction).
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes.
     * @param initialSpeed the initial speed of the car on the lane.
     * @throws RemoteException in case the simulation time cannot be read.
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final GTUFollowingModel gtuFollowingModel,
            final Map<Lane, DoubleScalar.Abs<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException
    {
        super(id, gtuType, length, width, maximumVelocity);
        this.gtuFollowingModel = gtuFollowingModel;
        this.lastEvaluationTime = getSimulator().getSimulatorTime().get();
        this.longitudinalPositions = new HashMap<>(initialLongitudinalPositions);
        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.speed = new DoubleScalar.Abs<SpeedUnit>(initialSpeed);
        this.nextEvaluationTime = getSimulator().getSimulatorTime().get();
    }

    /**
     * Return the speed of this Car at the specified time. <br>
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the speed at the specified time
     */
    public final DoubleScalar.Abs<SpeedUnit> getVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(this.speed, Calc.accelerationTimesTime(this.getAcceleration(when), dT)).immutable();
    }

    /**
     * Return the position of this Car at the specified time. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param lane the position on this lane will be returned.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time on the specified lane (could be longer than
     *         the length of the lane).
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    public final DoubleScalar.Abs<LengthUnit> getPosition(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException
    {
        if (!this.longitudinalPositions.containsKey(lane))
        {
            throw new NetworkException("GTU " + getId() + " not on lane " + lane);
        }
        DoubleScalar.Abs<LengthUnit> longitudinalPosition = this.longitudinalPositions.get(lane);
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<LengthUnit> positionOfFront(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException
    {
        return getPosition(lane, when);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<LengthUnit> positionOfRear(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException
    {
        return DoubleScalar.minus(getPosition(lane, when), getLength()).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getCurrentLongitudinalVelocity() throws RemoteException
    {
        return getVelocity(getSimulator().getSimulatorTime().get());
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
            pos = this.getPosition(lane, when).getSI();
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", getId(),
                this.lastEvaluationTime.getSI(), this.nextEvaluationTime.getSI(), pos, this.getVelocity(when).getSI(), this
                        .getAcceleration(when).getSI());
    }

    /**
     * Set the new state.
     * @param cfmr GTUFollowingModelResult; the new state of this GTU
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    public final void setState(final GTUFollowingModelResult cfmr) throws NetworkException
    {
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            this.longitudinalPositions.put(lane, getPosition(lane, this.nextEvaluationTime));
        }
        this.speed = getVelocity(this.nextEvaluationTime);
        // TODO add a check that time is increasing
        this.lastEvaluationTime = this.nextEvaluationTime;
        this.nextEvaluationTime = cfmr.getValidUntil();
        this.acceleration = cfmr.getAcceleration();
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
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU) throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        return DoubleScalar.minus(positionOfFront(lane, when), otherGTU.positionOfFront(lane, when)).immutable();
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
        Set<LaneLocation> laneLocations = getCurrentLocation(new GTUReferencePoint(0.0, 0.0, 0.0));
        if (laneLocations.isEmpty())
        {
            return null;
        }
        LaneLocation laneLocation = laneLocations.iterator().next();
        LineString line = laneLocation.getLane().getOffsetLine();
        double fraction = laneLocation.getFractionalLongitudinalPosition();
        LengthIndexedLine lil = new LengthIndexedLine(line);
        Coordinate c = lil.extractPoint(fraction * line.getLength());
        Coordinate ca = (fraction <= 0.01) ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
        Coordinate cb = (fraction >= 0.99) ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
        double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
        return new DirectedPoint(c.x, c.y, c.z, 0.0, 0.0, angle);
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        DirectedPoint l = getLocation();
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(l.x - dx, l.y - dy, 0.0), new Point3d(l.x + dx, l.y + dy, l.z));
    }

    /** {@inheritDoc} */
    @Override
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.gtuFollowingModel.getSimulator();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Abs<LengthUnit>> getLongitudinalPositions()
    {
        return this.longitudinalPositions;
    }

}
