package org.opentrafficsim.core.geometry;

import org.djutils.draw.point.Point2d;

/**
 * Line representation that a {@code Flattener} can flatten in to a polyline.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface FlattableLine
{

    /**
     * Returns the point at the given fraction. The fraction may represent any parameter, such as <i>t</i> in a Bezier, <i>s</i>
     * in a Clothoid, or simply the fraction of length.
     * @param fraction double; fraction.
     * @return double; point at the given fraction.
     */
    Point2d get(double fraction);

    /**
     * Returns the direction at the given fraction. The fraction may represent any parameter, such as <i>t</i> in a Bezier,
     * <i>s</i> in a Clothoid, or simply the fraction of length. The default implementation performs a numerical approach by
     * looking at the direction between the points at fraction, and a point 1e-6 away.
     * @param fraction double; fraction.
     * @return double; direction at the given fraction.
     */
    default double getDirection(final double fraction)
    {
        Point2d p1, p2;
        if (fraction < 0.5) // to prevent going above 1.0
        {
            p1 = get(fraction);
            p2 = get(fraction + 1e-6);
        }
        else
        {
            p1 = get(fraction - 1e-6);
            p2 = get(fraction);
        }
        return p1.directionTo(p2);
    }

}
