package org.opentrafficsim.road.network.speed;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;

/**
 * Class with curvature info for curvature speed limit type.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param radius curvature radius
 */
public record SpeedInfoCurvature(Length radius)
{

    /**
     * Constructor with curvature radius.
     * @param radius curvature radius
     * @throws NullPointerException if radius is null
     */
    public SpeedInfoCurvature
    {
        Throw.whenNull(radius, "radius");
    }

}
