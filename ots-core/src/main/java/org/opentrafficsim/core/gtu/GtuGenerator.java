package org.opentrafficsim.core.gtu;

import java.util.Set;

import org.djutils.base.Identifiable;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.object.NonLocatedObject;

/**
 * Gtu generator in its most basic form, which is able to report a queue count at one or more positions.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GtuGenerator extends NonLocatedObject
{

    /**
     * Returns the positions.
     * @return set of positions.
     */
    Set<GtuGeneratorPosition> getPositions();

    /**
     * Interface for a position that is reported on. This is a Locatable, with a queue count added to is.
     * <p>
     * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    interface GtuGeneratorPosition extends OtsShape, Identifiable
    {
        /**
         * Returns the number of GTUs in the queue.
         * @return number of GTUs in the queue.
         */
        int getQueueSize();

        @Override
        DirectedPoint2d getLocation();

        @Override
        default Polygon2d getAbsoluteContour()
        {
            throw new UnsupportedOperationException(
                    "A GtuGeneratorPosition does not have geometry. Geometry should be defined in animation.");
        }

        @Override
        default Polygon2d getRelativeContour()
        {
            throw new UnsupportedOperationException(
                    "A GtuGeneratorPosition does not have geometry. Geometry should be defined in animation.");
        }
    }

}
