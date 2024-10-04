package org.opentrafficsim.road.network.speed;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * Implementation of SpeedLimitType suitable for the most common speed info class {@code Speed}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SpeedLimitTypeSpeed extends SpeedLimitType<Speed>
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /**
     * Constructor.
     * @param id id of this speed limit type, which must be unique
     * @throws NullPointerException if id is null
     */
    public SpeedLimitTypeSpeed(final String id)
    {
        super(id, Speed.class);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SpeedLimitTypeSpeed [" + getId() + "]";
    }

}
