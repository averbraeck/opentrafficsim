package org.opentrafficsim.draw;

import org.djutils.draw.bounds.Bounds2d;

/**
 * This class returns bounds that respond to {@code contains(x, y)} by checking a clickable expanse around a point (radius).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ClickablePointLocatable extends ClickableLocatable
{

    /** {@inheritDoc} */
    @Override
    default Bounds2d getBounds()
    {
        return new Bounds2d(EXPANSE, EXPANSE)
        {
            /** */
            private static final long serialVersionUID = 20241006L;

            /** {@inheritDoc} */
            @Override
            public boolean contains(final double x, final double y)
            {
                return Math.hypot(x, y) < 0.5 * EXPANSE; // 0.5 as on both sides of line
            }
        };
    }

}
