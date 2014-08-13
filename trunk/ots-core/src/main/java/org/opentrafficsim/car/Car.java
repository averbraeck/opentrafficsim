package org.opentrafficsim.car;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.location.LocationRelative;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * This is a quick hack attempting to test-implement IDM+.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Car implements GTU<Integer, LocationRelative<Line<String>>, DoubleScalarRel<SpeedUnit>>
{
    /** Time of last evaluation. */
    protected DoubleScalarAbs<TimeUnit> lastEvaluationTime;

    /** Time of next evaluation. */
    protected DoubleScalarAbs<TimeUnit> nextEvaluationTime;

    /** Longitudinal position. */
    protected DoubleScalarAbs<LengthUnit> longitudinalPosition;

    /** Speed at lastEvaluationTime. */
    protected DoubleScalarRel<SpeedUnit> speed;

    /** Current acceleration (negative values indicate deceleration). */
    protected DoubleScalarAbs<AccelerationUnit> acceleration = new DoubleScalarAbs<AccelerationUnit>(0,
            AccelerationUnit.METER_PER_SECOND_2);;

    /** Maximum speed that this car can drive at. */
    protected final DoubleScalarRel<SpeedUnit> vMax = new DoubleScalarRel<SpeedUnit>(180, SpeedUnit.KM_PER_HOUR);

    /** Length of this car. */
    protected final DoubleScalarRel<LengthUnit> length = new DoubleScalarRel<LengthUnit>(4, LengthUnit.METER);

    /** ID of this car. */
    private final int iD;

    /** SimulatorInterface "running" this Car. */
    private final SimulatorInterface simulator;

    /** CarFollowingModel used by this Car. */
    private final CarFollowingModel carFollowingModel;

    /**
     * Create a new Car.
     * @param iD
     * @param simulator
     * @param carFollowingModel
     * @param initialTime
     * @param initialPosition
     * @param initialSpeed
     */
    public Car(final int iD, final SimulatorInterface simulator, final CarFollowingModel carFollowingModel,
            final DoubleScalarAbs<TimeUnit> initialTime, final DoubleScalarAbs<LengthUnit> initialPosition,
            final DoubleScalarRel<SpeedUnit> initialSpeed)
    {
        this.iD = iD;
        this.simulator = simulator;
        this.carFollowingModel = carFollowingModel;
        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.lastEvaluationTime = new DoubleScalarAbs<TimeUnit>(initialTime);
        this.longitudinalPosition = new DoubleScalarAbs<LengthUnit>(initialPosition);
        this.speed = new DoubleScalarRel<SpeedUnit>(initialSpeed);
        this.nextEvaluationTime = new DoubleScalarAbs<TimeUnit>(initialTime);
    }

    /**
     * Return the speed of this Car at the specified time. <br />
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the speed at the specified time
     */
    public DoubleScalarRel<SpeedUnit> getVelocity(final DoubleScalarAbs<TimeUnit> when)
    {
        DoubleScalarRel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime);
        return DoubleScalar.plus(SpeedUnit.METER_PER_SECOND, this.speed,
                Calc.accelerationTimesTime(this.acceleration, dT));
    }

    /**
     * Return the position of this Car at the specified time. <br />
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> getPosition(final DoubleScalarAbs<TimeUnit> when)
    {
        DoubleScalarRel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime);
        // System.out.println("dT is " + dT);
        return DoubleScalar.plus(this.longitudinalPosition, Calc.speedTimesTime(this.speed, dT),
                Calc.accelerationTimesTimeSquaredDiv2(this.acceleration, dT));
    }

    /**
     * Return the maximum speed that this Car can drive on a horizontal, straight road.
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the maximum driving speed
     */
    public DoubleScalarRel<SpeedUnit> vMax()
    {
        return new DoubleScalarRel<SpeedUnit>(this.vMax);
    }

    /**
     * Return the length of this Car.
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the length of this Car
     */
    public DoubleScalarRel<LengthUnit> length()
    {
        return new DoubleScalarRel<LengthUnit>(this.length);
    }

    /**
     * Return the position of the front bumper of this Car.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> positionOfFront(final DoubleScalarAbs<TimeUnit> when)
    {
        return getPosition(when);
    }

    /**
     * Return the position of the rear bumper of this Car.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> positionOfRear(final DoubleScalarAbs<TimeUnit> when)
    {
        return DoubleScalar.minus(getPosition(when), this.length);
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getID()
     */
    @Override
    public Integer getID()
    {
        return this.iD;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getLocation()
     */
    @Override
    public LocationRelative<Line<String>> getLocation()
    {
        return null; // FIXME: STUB
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getVelocity()
     */
    @Override
    public DoubleScalarRel<SpeedUnit> getVelocity()
    {
        try
        {
            return getVelocity(new DoubleScalarAbs<TimeUnit>(this.simulator.getSimulatorTime(), TimeUnit.SECOND));
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
            return null; // TODO: STUB
        }
    }

    /**
     * Return the last evaluation time.
     * @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation
     */
    public DoubleScalarAbs<TimeUnit> getLastEvaluationTime()
    {
        return new DoubleScalarAbs<TimeUnit>(this.lastEvaluationTime);
    }

    public String toString()
    {
        try
        {
            return toString(new DoubleScalarAbs<TimeUnit>(this.simulator.getSimulatorTime(), TimeUnit.SECOND));
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
            return null; // TODO: STUB
        }
    }

    /**
     * Description of Car at specified time.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public String toString(final DoubleScalarAbs<TimeUnit> when)
    {
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", this.iD,
                this.lastEvaluationTime.getValueSI(), this.nextEvaluationTime.getValueSI(), this.getPosition(when)
                        .getValueSI(), this.getVelocity(when).getValueSI(), this.acceleration.getValueSI());
    }

    /**
     * @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation.
     */
    public DoubleScalarAbs<TimeUnit> getNextEvaluationTime()
    {
        return new DoubleScalarAbs<TimeUnit>(this.nextEvaluationTime);
    }

    /**
     * Set the new state.
     * @param cfmr CarFollowingModelResult; the new state of this Car
     */
    public void setState(final CarFollowingModelResult cfmr)
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
        // TODO schedule next evaluation in the scheduler.
    }

    /**
     * Return the acceleration at a specified time.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time for which the acceleration must be returned
     * @return DoubleScalarAbs&lt;AccelerationUnit&gt;; the acceleration at the given time
     */
    public DoubleScalarAbs<AccelerationUnit> getAcceleration(final DoubleScalarAbs<TimeUnit> when)
    {
        // Currently the acceleration is independent of when; it is constant during the evaluation interval
        return new DoubleScalarAbs<AccelerationUnit>(this.acceleration);
    }

}
