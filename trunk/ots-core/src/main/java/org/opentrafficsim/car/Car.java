package org.opentrafficsim.car;

import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.location.LocationRelative;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * This is a quick hack attempting to test-implement IDM+.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Car implements GTU<Integer, LocationRelative<Line<String>>, DoubleScalar.Rel<SpeedUnit>>
{
    // TODO lots of these fields should probably be moved int the GTU class...

    /** Time of last evaluation. */
    protected DoubleScalar.Abs<TimeUnit> lastEvaluationTime;

    /** Time of next evaluation. */
    protected DoubleScalar.Abs<TimeUnit> nextEvaluationTime;

    /** Longitudinal position. */
    protected DoubleScalar.Abs<LengthUnit> longitudinalPosition;

    /** Speed at lastEvaluationTime. */
    protected DoubleScalar.Rel<SpeedUnit> speed;

    /** Current acceleration (negative values indicate deceleration). */
    protected DoubleScalar.Abs<AccelerationUnit> acceleration = new DoubleScalar.Abs<AccelerationUnit>(0,
            AccelerationUnit.METER_PER_SECOND_2);;

    /** Maximum speed that this car can drive at. */
    protected final DoubleScalar.Rel<SpeedUnit> vMax = new DoubleScalar.Rel<SpeedUnit>(180, SpeedUnit.KM_PER_HOUR);

    /** Length of this car. */
    protected final DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER);

    /** ID of this car. */
    private final int id;

    /** Simulator "running" this Car. */
    private final OTSDEVSSimulator simulator;

    /** CarFollowingModel used by this Car. */
    private final CarFollowingModel carFollowingModel;

    /**
     * Create a new Car.
     * @param id integer; the id of the new Car
     * @param simulator OTSDEVSSimulator
     * @param carFollowingModel CarFollowingModel; the car following model used by the new Car
     * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the first evaluation time of the new Car
     * @param initialPosition DoubleScalar.Abs&lt;LengthUnit&gt;; the initial position of the new Car
     * @param initialSpeed DoubleScalar.Rel&lt;SpeedUnit&gt;; the initial speed of the new Car
     */
    public Car(final int id, final OTSDEVSSimulator simulator, final CarFollowingModel carFollowingModel,
            final DoubleScalar.Abs<TimeUnit> initialTime, final DoubleScalar.Abs<LengthUnit> initialPosition,
            final DoubleScalar.Rel<SpeedUnit> initialSpeed)
    {
        this.id = id;
        this.simulator = simulator;
        this.carFollowingModel = carFollowingModel;
        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.lastEvaluationTime = new DoubleScalar.Abs<TimeUnit>(initialTime);
        this.longitudinalPosition = new DoubleScalar.Abs<LengthUnit>(initialPosition);
        this.speed = new DoubleScalar.Rel<SpeedUnit>(initialSpeed);
        this.nextEvaluationTime = new DoubleScalar.Abs<TimeUnit>(initialTime);
    }

    /**
     * Return the speed of this Car at the specified time. <br>
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the speed at the specified time
     */
    public final DoubleScalar.Rel<SpeedUnit> getVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = MutableDoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return MutableDoubleScalar.plus(SpeedUnit.METER_PER_SECOND, this.speed,
                Calc.accelerationTimesTime(this.acceleration, dT)).immutable();
    }

    /**
     * Return the position of this Car at the specified time. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public final DoubleScalar.Abs<LengthUnit> getPosition(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = MutableDoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        // System.out.println("dT is " + dT);
        return MutableDoubleScalar.plus(this.longitudinalPosition, Calc.speedTimesTime(this.speed, dT),
                Calc.accelerationTimesTimeSquaredDiv2(this.acceleration, dT)).immutable();
    }

    /**
     * Return the maximum speed that this Car can drive on a horizontal, straight road.
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the maximum driving speed
     */
    public final DoubleScalar.Rel<SpeedUnit> vMax()
    {
        return new DoubleScalar.Rel<SpeedUnit>(this.vMax);
    }

    /**
     * Return the length of this Car.
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the length of this Car
     */
    public final DoubleScalar.Rel<LengthUnit> length()
    {
        return new DoubleScalar.Rel<LengthUnit>(this.length);
    }

    /**
     * Return the position of the front bumper of this Car.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public final DoubleScalar.Abs<LengthUnit> positionOfFront(final DoubleScalar.Abs<TimeUnit> when)
    {
        return getPosition(when);
    }

    /**
     * Return the position of the rear bumper of this Car.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public final DoubleScalar.Abs<LengthUnit> positionOfRear(final DoubleScalar.Abs<TimeUnit> when)
    {
        return MutableDoubleScalar.minus(getPosition(when), this.length).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final Integer getID()
    {
        return this.id;
    }

    /**
     * Retrieve the simulator of this Car.
     * @return OTSDEVSSimulatorInterface; the simulator of this Car
     */
    public final OTSDEVSSimulator getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public LocationRelative<Line<String>> getLocation()
    {
        return null; // FIXME: STUB
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<SpeedUnit> getVelocity()
    {
        return getVelocity(this.simulator.getSimulatorTime().get());
    }

    /**
     * Return the last evaluation time.
     * @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation
     */
    public final DoubleScalar.Abs<TimeUnit> getLastEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.lastEvaluationTime);
    }

    @Override
    public final String toString()
    {
        return toString(this.simulator.getSimulatorTime().get());
    }

    /**
     * Description of Car at specified time.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final DoubleScalar.Abs<TimeUnit> when)
    {
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", this.id,
                this.lastEvaluationTime.getValueSI(), this.nextEvaluationTime.getValueSI(),
                this.getPosition(when).getValueSI(), this.getVelocity(when).getValueSI(), this.acceleration.getValueSI());
    }

    /**
     * @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation.
     */
    public final DoubleScalar.Abs<TimeUnit> getNextEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.nextEvaluationTime);
    }

    /**
     * Set the new state.
     * @param cfmr CarFollowingModelResult; the new state of this Car
     */
    public final void setState(final CarFollowingModelResult cfmr)
    {
        // System.out.println("Moving car from " + position(this.lastEvaluationTime) + " to " +
        // position(this.nextEvaluationTime));
        // System.out.println("Updating lastEvaluationTime from " + this.lastEvaluationTime + " to " +
        // this.nextEvaluationTime);
        this.longitudinalPosition = getPosition(this.nextEvaluationTime);
        this.speed = getVelocity(this.nextEvaluationTime);
        // TODO add a check that time is increasing
        this.lastEvaluationTime = this.nextEvaluationTime;
        this.nextEvaluationTime = cfmr.validUntil;
        this.acceleration = cfmr.acceleration;
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

    /**
     * Determine by what distance the front of this Car is ahead or behind the front an other Car. <br>
     * Positive values indicate that this Car is ahead, negative values indicate behind.
     * @param otherCar Car; the car to which the headway must be returned
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     */
    public final DoubleScalar.Rel<LengthUnit> headway(final Car otherCar)
    {
        DoubleScalar.Abs<TimeUnit> when = this.simulator.getSimulatorTime().get();
        return MutableDoubleScalar.minus(positionOfFront(when), otherCar.positionOfFront(when)).immutable();
    }

    // TODO: we need something like headway that returns the lateral offset in lanes or meters

    /**
     * Retrieve the CarFollowingModel of this Car.
     * @return CarFollowingModel
     */
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

}
