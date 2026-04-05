package org.opentrafficsim.road.network.speed;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * Speed limit information.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param speed speed limit
 * @param enforced whether the speed limit is enforced (e.g. speed camera)
 */
public record SpeedLimit(Speed speed, boolean enforced)
{

    /**
     * Returns a standard non-enforced speed limit.
     * @param speed speed limit
     * @return standard non-enforced speed limit
     */
    public static SpeedLimit of(final Speed speed)
    {
        return new SpeedLimit(speed, false);
    }

}
