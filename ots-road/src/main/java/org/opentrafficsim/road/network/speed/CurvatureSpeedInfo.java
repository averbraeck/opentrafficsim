package org.opentrafficsim.road.network.speed;

import java.io.Serializable;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Class with curvature info for curvature speed limit type.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 30, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class CurvatureSpeedInfo implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160501L;
    
    /** Curvature radius. */
    private final Length radius;

    /**
     * Constructor with curvature radius.
     * @param radius curvature radius
     */
    public CurvatureSpeedInfo(final Length radius)
    {
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
     * @param acceleration acceleration to result from speed in corner.
     * @return speed for which the current lateral acceleration follows in the corner
     */
    public final Speed getSpeedForLateralAcceleration(final Acceleration acceleration)
    {
        // a=v*v/r => v=sqrt(a*r)
        return new Speed(Math.sqrt(acceleration.si * this.radius.si), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CurvatureSpeedInfo [radius=" + this.radius + "]";
    }
    
}
