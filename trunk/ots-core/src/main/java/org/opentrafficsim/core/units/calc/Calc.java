package org.opentrafficsim.core.units.calc;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.OTS_SCALAR;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://djunits.org/docs/license.html">DJUNITS License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Calc implements OTS_SCALAR
{
    /**
     * This class should never be instantiated.
     */
    private Calc()
    {
        // Prevent instantiation of this class
    }

    /**
     * Distance is speed times time. <br>
     * s(t) = v * t
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param time DoubleScalar.Rel&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the resulting distance
     */
    public static Length.Rel speedTimesTime(final Speed.Rel speed,
        final Time.Rel time)
    {
        return new Length.Rel(speed.getSI() * time.getSI(), LengthUnit.METER);
    }

    /**
     * Distance is speed times time. <br>
     * s(t) = v * t
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param time DoubleScalar.Rel&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the resulting distance
     */
    public static Length.Rel speedTimesTime(final Speed.Abs speed,
        final Time.Rel time)
    {
        return new Length.Rel(speed.getSI() * time.getSI(), LengthUnit.METER);
    }

    /**
     * Distance is 0.5 times acceleration times time squared. <br>
     * s(t) = 0.5 * a * t * t
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalar.Abs&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the resulting distance
     */
    public static Length.Rel accelerationTimesTimeSquaredDiv2(
        final Acceleration.Abs acceleration, final Time.Rel time)
    {
        double t = time.getSI();
        return new Length.Rel(0.5 * acceleration.getSI() * t * t, LengthUnit.METER);
    }

    /**
     * Speed is acceleration times time. <br>
     * v(t) = a * t
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalar.Rel&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;SpeedUnit&gt;; the resulting speed
     */
    public static Speed.Rel accelerationTimesTime(final Acceleration.Abs acceleration,
        final Time.Rel time)
    {
        return new Speed.Rel(acceleration.getSI() * time.getSI(), SpeedUnit.METER_PER_SECOND);
    }

    /**
     * Time is speed divided by acceleration. <br>
     * t = v / a
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time it takes to accelerate using the given acceleration from 0 to the
     *         indicated speed
     */
    public static Time.Rel speedDividedByAcceleration(final DoubleScalar<SpeedUnit> speed,
        final Acceleration.Rel acceleration)
    {
        return new Time.Rel(speed.getSI() / acceleration.getSI(), TimeUnit.SECOND);
    }

    /**
     * (Braking) distance is speed squared divided by two times the acceleration. <br>
     * t = v / a
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time it takes to accelerate using the given acceleration from 0 to the
     *         indicated speed
     */
    public static Length.Rel speedSquaredDividedByDoubleAcceleration(final DoubleScalar<SpeedUnit> speed,
        final Acceleration.Abs acceleration)
    {
        return new Length.Rel(speed.getSI() * speed.getSI() / (2.0 * acceleration.getSI()),
            LengthUnit.METER);
    }

    /**
     * Acceleration is speed difference divided by time difference. <br>
     * a = dv / dt
     * @param speedDifference DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed difference
     * @param timeDifference DoubleScalar.Rel&lt;TimeUnit&gt;; the time difference
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the average acceleration needed to match the given inputs
     */
    public static Acceleration.Abs deltaSpeedDividedByTime(
        final Speed.Rel speedDifference, final Time.Rel timeDifference)
    {
        return new Acceleration.Abs(speedDifference.getSI() / timeDifference.getSI(),
            AccelerationUnit.METER_PER_SECOND_2);
    }
}
