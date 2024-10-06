package org.opentrafficsim.draw;

import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * This class returns bounds that respond to {@code contains(x, y)} by checking the actual shape, while also accounting for a
 * minimum clickable expanse.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ClickableLocatable extends OtsLocatable
{

    /** Minimum expanse to click on object. */
    double EXPANSE = 2.0;
    
    /** {@inheritDoc} */
    @Override
    default Bounds2d getBounds()
    {
        /*
         * TODO: expanse does not work on thin diagonal objects. For instance a center line at 45 degrees. It has a xExpand and
         * yExpand beyond 2m, yet is not clickable as the OtsShape will not see points included.
         */
        OtsShape shape = getShape();
        double deltaX = shape.getMaxX() - shape.getMinX();
        double deltaY = shape.getMaxY() - shape.getMinY();
        boolean xExpand = deltaX < EXPANSE;
        boolean yExpand = deltaY < EXPANSE;
        return new Bounds2d(xExpand ? EXPANSE : deltaX, yExpand ? EXPANSE : deltaY)
        {
            /** */
            private static final long serialVersionUID = 20241006L;

            /** {@inheritDoc} */
            @Override
            public boolean contains(final double x, final double y)
            {
                if (xExpand || yExpand)
                {
                    return getMinX() <= x && x <= getMaxX() && getMinY() <= y && y <= getMaxY();
                }
                return shape.contains(x, y);
            }
        };
    }

}
