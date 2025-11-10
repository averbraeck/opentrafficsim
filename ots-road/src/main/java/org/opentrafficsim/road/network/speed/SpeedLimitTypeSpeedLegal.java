package org.opentrafficsim.road.network.speed;

/**
 * Similar to {@code SpeedLimitTypeSpeed} but implements the marker interface {@code LegalSpeedLimit}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class SpeedLimitTypeSpeedLegal extends SpeedLimitTypeSpeed implements LegalSpeedLimit
{

    /**
     * Constructor.
     * @param id id of this speed limit type, which must be unique
     * @throws NullPointerException if id is null
     */
    public SpeedLimitTypeSpeedLegal(final String id)
    {
        super(id);
    }

    @Override
    public final String toString()
    {
        return "SpeedLimitTypeSpeedLegal [" + getId() + "]";
    }

}
