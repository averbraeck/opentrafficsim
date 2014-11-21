package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LinkLocation;
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
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
     * @param simulator the simulator.
     * @throws RemoteException in case the simulation time cannot be read.
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar.Rel<LengthUnit> width, final DoubleScalar.Abs<SpeedUnit> maximumVelocity,
            final GTUFollowingModel gtuFollowingModel,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed, final OTSDEVSSimulatorInterface simulator)
            throws RemoteException
    {
        super(id, gtuType, length, width, maximumVelocity, simulator);
        this.gtuFollowingModel = gtuFollowingModel;
        this.lastEvaluationTime = getSimulator().getSimulatorTime().get();
        this.longitudinalPositions = new HashMap<>(initialLongitudinalPositions);
        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.speed = new DoubleScalar.Abs<SpeedUnit>(initialSpeed);
        this.nextEvaluationTime = getSimulator().getSimulatorTime().get();
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
     * Set the new state.
     * @param cfmr GTUFollowingModelResult; the new state of this GTU
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    public final void setState(final GTUFollowingModelResult cfmr) throws NetworkException
    {
        // TODO: test when vehicle moves to next lane in the network.
        for (Lane lane : getLongitudinalPositions().keySet())
        {
            getLongitudinalPositions().put(lane, positionOfFront(lane, this.nextEvaluationTime));
        }
        this.speed = getLongitudinalVelocity(this.nextEvaluationTime);
        // TODO add a sanity check that time is increasing
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
    public final DoubleScalar.Abs<SpeedUnit> getCurrentLateralVelocity()
    {
        // TODO: change when lateral velocity is introduced.
        return new DoubleScalar.Abs<SpeedUnit>(0.0, SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> positionOfFront(final Lane lane) throws NetworkException
    {
        try
        {
            DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
            return positionOfFront(lane, when);
        }
        catch (RemoteException exception)
        {
            throw new NetworkException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> positionOfFront(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException
    {
        // TODO: link to the next lane if position > lane.getLength()
        if (!getLongitudinalPositions().containsKey(lane))
        {
            throw new NetworkException("GTU " + getId() + " not on lane " + lane);
        }
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = getLongitudinalPositions().get(lane);
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(
                DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final LinkLocation positionOfFront() throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        return positionOfFront(when);
    }

    /** {@inheritDoc} */
    @Override
    public final LinkLocation positionOfFront(final DoubleScalar.Abs<TimeUnit> when) throws RemoteException
    {
        // TODO: link to the previous or next link if fraction < 0.0 or fraction > 1.0
        Lane lane = getLongitudinalPositions().keySet().iterator().next();
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = getLongitudinalPositions().get(lane);
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        DoubleScalar.Rel<LengthUnit> loc =
                DoubleScalar.plus(
                        DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                        Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable();
        double fractionalLongitudinalPosition = DoubleScalar.divide(loc, lane.getLength()).doubleValue();
        return new LinkLocation(lane.getParentLink(), fractionalLongitudinalPosition);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> positionOfRear(final Lane lane) throws NetworkException
    {
        return DoubleScalar.minus(positionOfFront(lane), getLength()).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> positionOfRear(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException
    {
        // TODO: link to the next lane if position < 0 or position > lane.getLength()
        return DoubleScalar.minus(positionOfFront(lane, when), getLength()).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final LinkLocation positionOfRear() throws RemoteException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        return positionOfRear(when);
    }

    /** {@inheritDoc} */
    @Override
    public final LinkLocation positionOfRear(final DoubleScalar.Abs<TimeUnit> when) throws RemoteException
    {
        // TODO: link to the previous or next link if fraction < 0.0 or fraction > 1.0
        Lane lane = getLongitudinalPositions().keySet().iterator().next();
        DoubleScalar.Rel<LengthUnit> longitudinalPosition =
                DoubleScalar.minus(getLongitudinalPositions().get(lane), getLength()).immutable();
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        DoubleScalar.Rel<LengthUnit> loc =
                DoubleScalar.plus(
                        DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                        Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable();
        double fractionalLongitudinalPosition = DoubleScalar.divide(loc, lane.getLength()).doubleValue();
        return new LinkLocation(lane.getParentLink(), fractionalLongitudinalPosition);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headwayInCurrentLane(final DoubleScalar<LengthUnit> maxDistance)
            throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTUInCurrentLane(final DoubleScalar<LengthUnit> maxDistance)
            throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU) throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
            final DoubleScalar.Abs<TimeUnit> when) throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return this.positionOfFront().distance(otherGTU.positionOfFront());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headwayInLane(final Lane lane, final DoubleScalar<LengthUnit> maxDistance)
            throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTUInLane(final Lane lane, final DoubleScalar<LengthUnit> maxDistance)
            throws RemoteException
    {
        // TODO: link to the next lane if maxDistance < lane.getLength()
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> getLongitudinalPositions()
    {
        // System.out.println(getId() + ": Longitudinal positions: " + this.longitudinalPositions);
        return this.longitudinalPositions;
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
        Lane lane = getLongitudinalPositions().keySet().iterator().next();
        try
        {
            // TODO: solve problem when point is still on previous lane.
            double fraction = (positionOfFront(lane).getSI() - getLength().getSI() / 2.0) / lane.getLength().getSI();
            LineString line = lane.getOffsetLine();
            LengthIndexedLine lil = new LengthIndexedLine(line);
            Coordinate c = lil.extractPoint(fraction * line.getLength());
            Coordinate ca =
                    fraction <= 0.01 ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
            Coordinate cb =
                    fraction >= 0.99 ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
            double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
            return new DirectedPoint(c.x, c.y, c.z, 0.0, 0.0, angle);
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
            pos = this.positionOfFront(lane, when).getSI();
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", getId(),
                this.lastEvaluationTime.getSI(), this.nextEvaluationTime.getSI(), pos,
                this.getLongitudinalVelocity(when).getSI(), this.getAcceleration(when).getSI());
    }

}
