package org.opentrafficsim.core.math;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.vdouble.scalar.Direction;

/**
 * Calculate between Polar (spherical) and Cartesian (xyz) coordinates.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Scalar3D
{
    /** Utility class, don't instantiate. */
    private Scalar3D()
    {
    }

    /**
     * @param r double; the radius
     * @param theta double; the angle from the z direction
     * @param phi double; the projected angle in the xy-plane from the x direction
     * @return a double array [x, y, z] with cartesian coordinates
     */
    public static double[] polarToCartesian(final double r, final double theta, final double phi)
    {
        // double x = r * Math.sin(theta) * Math.cos(phi);
        // double y = r * Math.sin(theta) * Math.sin(phi);
        // double z = r * Math.cos(theta);

        double mst = Math.sin(theta);
        double msp = Math.sin(phi);
        double mct = Math.sqrt(1.0 - mst * mst);
        double mcp = Math.sqrt(1.0 - msp * msp);
        return new double[] { r * mst * mcp, r * mst * msp, r * mct };
    }

    /**
     * Get the (polar) radius based on Cartesian coordinates.
     * @param x double; the x-coordinate
     * @param y double; the y-coordinate
     * @param z double; the z-coordinate
     * @return the radius, which is the distance from (0,0,0)
     */
    public static double cartesianToRadius(final double x, final double y, final double z)
    {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Get the (polar) theta angle, which is the angle from the z-direction, from Cartesian coordinates.
     * @param x double; the x-coordinate
     * @param y double; the y-coordinate
     * @param z double; the z-coordinate
     * @return the radius, which is the distance from (0,0,0)
     */
    public static Direction cartesianToTheta(final double x, final double y, final double z)
    {
        double r = Math.sqrt(x * x + y * y + z * z);
        return new Direction(Math.acos(z / r), DirectionUnit.NORTH_RADIAN);
    }

    /**
     * Get the (polar) phi angle, which is the projected angle in the xy-plane from the x direction.
     * @param x double; the x-coordinate
     * @param y double; the y-coordinate
     * @return the projected angle of direction in the xy-plane
     */
    public static Direction cartesianToPhi(final double x, final double y)
    {
        return new Direction(Math.atan2(y, x), DirectionUnit.NORTH_RADIAN);
    }

}
