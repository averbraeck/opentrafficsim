package org.opentrafficsim.draw;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * This class returns bounds that respond to {@code contains(x, y)} by checking a clickable expanse around a line.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ClickableLineLocatable extends ClickableLocatable
{

    /** {@inheritDoc} */
    @Override
    default Bounds2d getBounds()
    {
        OtsShape shape = getShape();
        return new Bounds2d(shape.getMaxX() - shape.getMinX(), shape.getMaxY() - shape.getMinY())
        {
            /** */
            private static final long serialVersionUID = 20241006L;

            /** {@inheritDoc} */
            @Override
            public boolean contains(final double x, final double y)
            {
                Point2d point = new Point2d(x, y);
                return getLine().closestPointOnPolyLine(point).distance(point) < 0.5 * EXPANSE; // 0.5 as on both sides of line
            }
        };
    }

    /**
     * 
     * Returns line representation of this object in object coordinates.
     * @return line representation of this object in object coordinates.
     */
    PolyLine2d getLine();

}
