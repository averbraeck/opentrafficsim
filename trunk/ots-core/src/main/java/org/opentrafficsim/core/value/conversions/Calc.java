package org.opentrafficsim.core.value.conversions;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Calc
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
    public static DoubleScalar.Rel<LengthUnit> speedTimesTime(final DoubleScalar.Rel<SpeedUnit> speed,
        final DoubleScalar.Rel<TimeUnit> time)
    {
        return new DoubleScalar.Rel<LengthUnit>(speed.getSI() * time.getSI(), LengthUnit.METER);
    }

    /**
     * Distance is speed times time. <br>
     * s(t) = v * t
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param time DoubleScalar.Rel&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the resulting distance
     */
    public static DoubleScalar.Rel<LengthUnit> speedTimesTime(final DoubleScalar.Abs<SpeedUnit> speed,
        final DoubleScalar.Rel<TimeUnit> time)
    {
        return new DoubleScalar.Rel<LengthUnit>(speed.getSI() * time.getSI(), LengthUnit.METER);
    }

    /**
     * Distance is 0.5 times acceleration times time squared. <br>
     * s(t) = 0.5 * a * t * t
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalar.Abs&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the resulting distance
     */
    public static DoubleScalar.Rel<LengthUnit> accelerationTimesTimeSquaredDiv2(
        final DoubleScalar.Abs<AccelerationUnit> acceleration, final DoubleScalar.Rel<TimeUnit> time)
    {
        double t = time.getSI();
        return new DoubleScalar.Rel<LengthUnit>(0.5 * acceleration.getSI() * t * t, LengthUnit.METER);
    }

    /**
     * Speed is acceleration times time. <br>
     * v(t) = a * t
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalar.Rel&lt;TimeUnit&gt;; the time
     * @return DoubleScalar.Rel&lt;SpeedUnit&gt;; the resulting speed
     */
    public static DoubleScalar.Rel<SpeedUnit> accelerationTimesTime(final DoubleScalar.Abs<AccelerationUnit> acceleration,
        final DoubleScalar.Rel<TimeUnit> time)
    {
        return new DoubleScalar.Rel<SpeedUnit>(acceleration.getSI() * time.getSI(), SpeedUnit.METER_PER_SECOND);
    }

    /**
     * Time is speed divided by acceleration. <br>
     * t = v / a
     * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed
     * @param acceleration DoubleScalar.Rel&lt;AccelerationUnit&gt;; the acceleration
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time it takes to accelerate using the given acceleration from 0 to the
     *         indicated speed
     */
    public static DoubleScalar.Rel<TimeUnit> speedDividedByAcceleration(final DoubleScalar<SpeedUnit> speed,
        final DoubleScalar.Rel<AccelerationUnit> acceleration)
    {
        return new DoubleScalar.Rel<TimeUnit>(speed.getSI() / acceleration.getSI(), TimeUnit.SECOND);
    }

    /**
     * Acceleration is speed difference divided by time difference. <br>
     * a = dv / dt
     * @param speedDifference DoubleScalar.Rel&lt;SpeedUnit&gt;; the speed difference
     * @param timeDifference DoubleScalar.Rel&lt;TimeUnit&gt;; the time difference
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the average acceleration needed to match the given inputs
     */
    public static DoubleScalar.Abs<AccelerationUnit> deltaSpeedDividedByTime(
        final DoubleScalar.Rel<SpeedUnit> speedDifference, final DoubleScalar.Rel<TimeUnit> timeDifference)
    {
        return new DoubleScalar.Abs<AccelerationUnit>(speedDifference.getSI() / timeDifference.getSI(),
            AccelerationUnit.METER_PER_SECOND_2);
    }
}
