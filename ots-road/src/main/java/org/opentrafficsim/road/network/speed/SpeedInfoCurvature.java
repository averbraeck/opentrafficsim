package org.opentrafficsim.road.network.speed;

import java.io.Serializable;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;

/**
 * Class with curvature info for curvature speed limit type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param radius curvature radius
 */
public record SpeedInfoCurvature(Length radius) implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /**
     * Constructor with curvature radius.
     * @param radius curvature radius
     * @throws NullPointerException if radius is null
     */
    public SpeedInfoCurvature
    {
        Throw.whenNull(radius, "Radius may not be null.");
    }

    /**
     * Returns the speed for which the current lateral acceleration follows in the corner.
     * @param acceleration acceleration to result from speed in corner
     * @return speed for which the current lateral acceleration follows in the corner
     * @throws NullPointerException if acceleration is null
     */
    public final Speed getSpeedForLateralAcceleration(final Acceleration acceleration)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        // a=v*v/r => v=sqrt(a*r)
        return new Speed(Math.sqrt(acceleration.si * this.radius.si), SpeedUnit.SI);
    }

}
