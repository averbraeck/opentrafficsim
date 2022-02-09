package org.opentrafficsim.core.geometry;

import org.djutils.draw.point.OrientedPoint3d;
import org.djutils.draw.point.Point3d;

/**
 * DirectedPoint.java.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectedPoint extends OrientedPoint3d
{

    public DirectedPoint(final double x, final double y, final double z, final double dirX, final double dirY,
            final double dirZ) throws IllegalArgumentException
    {
        super(x, y, z, dirX, dirY, dirZ);
    }

    public DirectedPoint(final double x, final double y, final double z, final double[] orientation)
            throws NullPointerException, IllegalArgumentException
    {
        super(x, y, z, orientation);
    }

    public DirectedPoint(final double x, final double y, final double z) throws IllegalArgumentException
    {
        super(x, y, z);
    }

    public DirectedPoint(final double[] xyz, final double dirX, final double dirY, final double dirZ)
            throws NullPointerException, IllegalArgumentException
    {
        super(xyz, dirX, dirY, dirZ);
    }

    public DirectedPoint(final double[] xyz, final double[] orientation) throws NullPointerException, IllegalArgumentException
    {
        super(xyz, orientation);
    }

    public DirectedPoint(final double[] xyz) throws NullPointerException, IllegalArgumentException
    {
        super(xyz);
    }

    public DirectedPoint(final Point3d point, final double dirX, final double dirY, final double dirZ)
            throws IllegalArgumentException
    {
        super(point, dirX, dirY, dirZ);
    }

    public double getRotX()
    {
        return getDirX();
    }

    public double getRotY()
    {
        return getDirY();
    }

    public double getRotZ()
    {
        return getDirZ();
    }
}
