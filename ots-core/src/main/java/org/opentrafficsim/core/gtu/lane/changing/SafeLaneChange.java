package org.opentrafficsim.core.gtu.lane.changing;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * This utility class implements the <i>Safety Criterion</i> as described in Traffic Flow Dynamics by Martin Treiber and Arne
 * Kesting, ISBN 978-3-642-32459-8 ISBN 978-3-642-32460-4 (eBook), 2013, Chapter 14.3.1, pp 242-243.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * Determine if dangerous decelerations are incurred if a GTU changes into a lane where another GTU is driving. <br>
     * This implements the <i>Safety Criterion</i> as described in Traffic Flow Dynamics by Martin Treiber and Arne Kesting,
     * ISBN 978-3-642-32459-8 ISBN 978-3-642-32460-4 (eBook), 2013, Chapter 14.3.1, pp 242-243.
     * @param referenceGTU GTU; the gtu following model of this gtu is used to determine the needed acceleration (deceleration).
     * @param otherGTU GTU; the gtu driving in the other lane
     * @param maximumDeceleration DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum (considered safe) deceleration (must be
     *            positive; something on the order of 2m/s/s)
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return Boolean; true if the resulting deceleration is safe; false if the resulting deceleration is unsafe
     */
    public static boolean safe(final GTU<?> referenceGTU, final GTU<?> otherGTU,
        final DoubleScalar.Rel<AccelerationUnit> maximumDeceleration, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        /*-
        DoubleScalar.Abs<TimeUnit> when = referenceGTU.getSimulator().getSimulatorTime().get();
        GTUFollowingModel gtuFollowingModel = referenceGTU.getGTUFollowingModel();
        if (referenceGTU.getPosition(when).gt(otherGTU.getPosition(when)))
        { // The referenceGTU is ahead of the otherGTU
            return FollowAcceleration.acceleration(otherGTU, referenceGTU, when, gtuFollowingModel, speedLimit).getSI() 
                    >= -maximumDeceleration.getSI();
        }
        // The otherGTU is exactly parallel or ahead of the referenceGTU
        return FollowAcceleration.acceleration(referenceGTU, otherGTU, when, gtuFollowingModel, speedLimit).getSI() 
                >= -maximumDeceleration.getSI();
         */
        return false;
    }
}
