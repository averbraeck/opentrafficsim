package org.opentrafficsim.draw;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * This class returns a line that represent the object.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface LineLocatable extends OtsShape
{

    /**
     * Returns line representation of this object in object coordinates.
     * @return line representation of this object in object coordinates
     */
    PolyLine2d getLine();

    /**
     * Signed distance function. The point must be relative. As this is a line object, only positive values are returned.
     * @param point point for which distance is returned
     * @return distance from point to these bounds
     */
    @Override
    default double signedDistance(final Point2d point)
    {
        return getLine().closestPointOnPolyLine(point).distance(point);
    }

    @Override
    default boolean contains(final Point2d point)
    {
        return signedDistance(point) < WORLD_MARGIN_LINE;
    }

}
