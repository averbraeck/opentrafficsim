package org.opentrafficsim.draw;

import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.bounds.Bounds2d;

/**
 * Creates bounds that are at least 2m of size in the x and y direction.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ClickableBounds
{
    
    /** Minimum distance from center to edge. */
    private static double R_MIN = 1.0;
    
    /**
     * Constructor.
     */
    private ClickableBounds()
    {
        // utility class
    }
    
    /**
     * Creates bounds that are at least 2m of size in the x and y direction.
     * @param bounds Bounds&lt;?, ?, ?&gt;; actual object bounds.
     * @return Bounds&lt;?, ?, ?&gt;; bounds that are at least 2m of size in the x and y direction.
     */
    public static Bounds2d get(final Bounds<?, ?, ?> bounds)
    {
        if (bounds.getDeltaX() < 2 * R_MIN)
        {
            double x = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
            if (bounds.getDeltaY() < 2 * R_MIN)
            {
                double y = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
                return new Bounds2d(x - R_MIN, x + R_MIN, y - R_MIN, y + R_MIN);
            }
            return new Bounds2d(x - R_MIN, x + R_MIN, bounds.getMinY(), bounds.getMaxY());
        }
        if (bounds.getDeltaY() < 2 * R_MIN)
        {
            double y = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
            return new Bounds2d(bounds.getMinX(), bounds.getMaxX(), y - R_MIN, y + R_MIN);
        }
        if (bounds instanceof Bounds2d)
        {
            return (Bounds2d) bounds;
        }
        return new Bounds2d(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

}
