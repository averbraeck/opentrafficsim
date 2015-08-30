package org.opentrafficsim.core.gtu.following;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Storage for the result of a GTU following model. <br>
 * Currently the result is restricted to a constant acceleration during the period of validity (the time slot) of the result.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AccelerationStep
{
    /** Acceleration that will be maintained during the current time slot. */
    private final DoubleScalar.Abs<AccelerationUnit> acceleration;

    /** Time when the current time slot ends. */
    private final DoubleScalar.Abs<TimeUnit> validUntil;

    /**
     * Create a new GTUFollowingModelResult.
     * @param acceleration DoubleScalarAbs&lt;AccelerationUnit&gt;; computed acceleration
     * @param validUntil DoubleScalarAbs&lt;TimeUnit&gt;; time when this result expires
     */
    public AccelerationStep(final DoubleScalar.Abs<AccelerationUnit> acceleration,
        final DoubleScalar.Abs<TimeUnit> validUntil)
    {
        this.acceleration = acceleration;
        this.validUntil = validUntil;
    }

    /**
     * @return acceleration.
     */
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration()
    {
        return this.acceleration;
    }

    /**
     * @return validUntil.
     */
    public final DoubleScalar.Abs<TimeUnit> getValidUntil()
    {
        return this.validUntil;
    }

    /**
     * {@inheritDoc}
     */
    public final String toString()
    {
        return String.format("a=%s, valid until %s", this.acceleration, this.validUntil);
    }

}
