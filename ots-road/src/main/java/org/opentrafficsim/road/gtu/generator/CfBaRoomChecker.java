package org.opentrafficsim.road.gtu.generator;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Extends car-following placement with a first-order bounded acceleration (BA) principle. This principle comes down to
 * reduction of efficiency by increasing headways, as the generated GTU speed is lower than the desired speed. The increased
 * headways allow for acceleration to occur, allowing faster flow recovery over time.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CfBaRoomChecker extends CfRoomChecker
{

    /** {@inheritDoc} */
    @Override
    protected double headwayFactor(final Speed desiredSpeed, final Length desiredHeadway, final Speed generationSpeed,
            final Length generationHeadway, final Length leaderLength)
    {
        if (desiredSpeed.eq(generationSpeed))
        {
            return 1.0;
        }
        // following state at desired speed (capacity)
        double k0 = 1.0 / (desiredHeadway.si + leaderLength.si);
        double q0 = k0 * desiredSpeed.si;
        // actual state
        double k = 1.0 / (generationHeadway.si + leaderLength.si);
        double q = k * generationSpeed.si;
        // recovery flow qr
        double qr = q; // if already on free flow branch
        if (k > k0) // on congestion branch
        {
            // recovery wave speed assuming theta = 1 / q0 (theoretically sound and tested to be robust)
            double rho = generationSpeed.si - q0 / k;
            // recovery free flow state
            double kr = (q - rho * k) / (desiredSpeed.si - rho);
            qr = kr * desiredSpeed.si;
        }
        // efficiency factor, > 1 is larger headway, so less efficient
        return q0 / qr;
    }

}
