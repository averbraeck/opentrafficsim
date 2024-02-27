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
 */
public class SpeedInfoCurvature implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;

    /** Curvature radius. */
    private final Length radius;

    /**
     * Constructor with curvature radius.
     * @param radius Length; curvature radius
     * @throws NullPointerException if radius is null
     */
    public SpeedInfoCurvature(final Length radius)
    {
        Throw.whenNull(radius, "Radius may not be null.");
        this.radius = radius;
    }

    /**
     * Returns the curvature radius.
     * @return curvature radius
     */
    public final Length getRadius()
    {
        return this.radius;
    }

    /**
     * Returns the speed for which the current lateral acceleration follows in the corner.
     * @param acceleration Acceleration; acceleration to result from speed in corner
     * @return speed for which the current lateral acceleration follows in the corner
     * @throws NullPointerException if acceleration is null
     */
    public final Speed getSpeedForLateralAcceleration(final Acceleration acceleration)
    {
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        // a=v*v/r => v=sqrt(a*r)
        return new Speed(Math.sqrt(acceleration.si * this.radius.si), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        return this.radius.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SpeedInfoCurvature other = (SpeedInfoCurvature) obj;
        if (!this.radius.equals(other.radius))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpeedInfoCurvature [radius=" + this.radius + "]";
    }

}
