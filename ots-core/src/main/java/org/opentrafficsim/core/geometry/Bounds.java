package org.opentrafficsim.core.geometry;

import java.util.Collection;
import java.util.Iterator;

import org.djutils.draw.Drawable3d;
import org.djutils.draw.bounds.Bounds3d;
import org.djutils.draw.point.Point3d;

/**
 * Bounds.java.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Bounds extends Bounds3d
{

    public Bounds()
    {
        this(0.5, 0.5, 0.5);
    }

    public Bounds(final Collection<Drawable3d> drawableCollection) throws NullPointerException, IllegalArgumentException
    {
        super(drawableCollection);
    }

    public Bounds(final double minX, final double maxX, final double minY, final double maxY, final double minZ,
            final double maxZ)
    {
        super(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public Bounds(final double deltaX, final double deltaY, final double deltaZ)
    {
        super(deltaX, deltaY, deltaZ);
    }

    public Bounds(final Drawable3d... drawable3d) throws NullPointerException, IllegalArgumentException
    {
        super(drawable3d);
    }

    public Bounds(final Drawable3d drawable3d) throws NullPointerException
    {
        super(drawable3d);
    }

    public Bounds(final Iterator<? extends Point3d> points)
    {
        super(points);
    }

    public Bounds(final Point3d[] points) throws NullPointerException, IllegalArgumentException
    {
        super(points);
    }

}
