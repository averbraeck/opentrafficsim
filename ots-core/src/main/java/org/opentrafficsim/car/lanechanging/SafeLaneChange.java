package org.opentrafficsim.car.lanechanging;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.FollowAcceleration;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * This utility class implements the <i>Safety Criterion</i> as described in Traffic Flow Dynamics by Martin Treiber and
 * Arne Kesting, ISBN 978-3-642-32459-8 ISBN 978-3-642-32460-4 (eBook), 2013, Chapter 14.3.1, pp 242-243.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class SafeLaneChange
{
    /**
     * This class should never be instantiated.
     */
    private SafeLaneChange()
    {
        // This class should never be instantiated.
    }

    /**
     * Determine if dangerous decelerations are incurred if a Car changes into a lane where another Car is driving. <br>
     * This implements the <i>Safety Criterion</i> as described in Traffic Flow Dynamics by Martin Treiber and Arne
     * Kesting, ISBN 978-3-642-32459-8 ISBN 978-3-642-32460-4 (eBook), 2013, Chapter 14.3.1, pp 242-243.
     * @param referenceCar Car; the car following model of this car is used to determine the needed acceleration
     *            (deceleration).
     * @param otherCar Car; the car driving in the other lane
     * @param maximumDeceleration DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum (considered safe) deceleration
     *            (must be positive; something on the order of 2m/s/s)
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return Boolean; true if the resulting deceleration is safe; false if the resulting deceleration is unsafe
     */
    public static boolean safe(final Car referenceCar, final Car otherCar,
            final DoubleScalar.Rel<AccelerationUnit> maximumDeceleration, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        DoubleScalar.Abs<TimeUnit> when = referenceCar.getNextEvaluationTime();
        CarFollowingModel carFollowingModel = referenceCar.getCarFollowingModel();
        if (referenceCar.getPosition(when).getValueSI() > otherCar.getPosition(when).getValueSI())
        { // The referenceCar is ahead of the otherCar
            return FollowAcceleration.acceleration(otherCar, referenceCar, when, carFollowingModel, speedLimit)
                    .getValueSI() >= -maximumDeceleration.getValueSI();
        }
        // The otherCar is exactly parallel or ahead of the referenceCar
        return FollowAcceleration.acceleration(referenceCar, otherCar, when, carFollowingModel, speedLimit)
                .getValueSI() >= -maximumDeceleration.getValueSI();
    }
}
