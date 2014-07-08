package org.opentrafficsim.car;

import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

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
public class Car implements GTU<Integer, LocationRelative<Line>, DoubleScalarRel<SpeedUnit>> 
{
    // FIXME: this class should be extending GTU

    /** Time of last evaluation */
    protected DoubleScalarAbs<TimeUnit> lastEvaluationTime;

    /** Longitudinal position */
    protected DoubleScalarAbs<LengthUnit> longitudinalPosition;

    /** Speed at lastEvaluationTime */
    protected DoubleScalarRel<SpeedUnit> speed;

    /** Current acceleration (negative values indicate deceleration) */
    protected DoubleScalarRel<AccelerationUnit> acceleration;
    
    /** Maximum speed that this car can drive at */
    protected final DoubleScalarRel<SpeedUnit> vMax = new DoubleScalarRel<SpeedUnit> (180, SpeedUnit.KM_PER_HOUR);
    
    /** Length of this car */
    protected final DoubleScalarRel<LengthUnit> length = new DoubleScalarRel<LengthUnit> (4, LengthUnit.METER);
    
    /** ID of this car */
    private final int ID;
    
    /** SimulatorInterface "running" this Car */
    private final SimulatorInterface simulator;

    /** 
     * Create a new Car 
     * @param ID 
     * @param simulator 
     * */
    public Car(final int ID, final SimulatorInterface simulator)
    {
        this.ID = ID;
        this.simulator = simulator;
    }
    /**
     * Return the speed of this Car at the specified time. <br />
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the speed at the specified time
     */
    public DoubleScalarRel<SpeedUnit> speed(DoubleScalarAbs<TimeUnit> when)
    {
        DoubleScalarRel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime);
        return DoubleScalar.plus(SpeedUnit.METER_PER_SECOND, this.speed, Calc.accelerationTimesTime(this.acceleration, dT));
    }

    /**
     * Return the position of this Car at the specified time. <br />
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> position(DoubleScalarAbs<TimeUnit> when)
    {
        DoubleScalarRel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime);
        return DoubleScalar.plus(this.longitudinalPosition, Calc.speedTimesTime(this.speed, dT),
                Calc.accelerationTimesTimeSquaredDiv(this.acceleration, dT));
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
     * Return the position of the front bumper of this Car
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> positionOfFront(DoubleScalarAbs<TimeUnit> when)
    {
        return position(when);
    }
    /**
     * Return the position of the rear bumper of this Car
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     */
    public DoubleScalarAbs<LengthUnit> positionOfRear(DoubleScalarAbs<TimeUnit> when)
    {
        return DoubleScalar.minus(position(when), this.length);
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getID()
     */
    @Override
    public Integer getID()
    {
        return this.ID;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getLocation()
     */
    @Override
    public LocationRelative<Line> getLocation()
    {
        return null;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getVelocity()
     */
    @Override
    public DoubleScalarRel<SpeedUnit> getVelocity()
    {
        try
        {
            DoubleScalarAbs<TimeUnit> when = new DoubleScalarAbs<TimeUnit>(this.simulator.getSimulatorTime(), TimeUnit.SECOND);
            return speed(when);
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
            return null;    // TODO: STUB
        }
    }
}
