package org.opentrafficsim.base.geometry;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Locatable that specifies return types.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface OtsLocatable extends Locatable
{

    /** {@inheritDoc} */
    @Override
    Point2d getLocation();

    /**
     * {@inheritDoc}
     */
    @Override
    default Bounds2d getBounds()
    {
        OtsBounds2d b = getOtsBounds();
        return new Bounds2d(b.getMinX(), b.getMaxX(), b.getMinY(), b.getMaxY());
    }

    /**
     * Returns the bounds relative to the location.
     * @return OtsBounds2d; bounds relative to the location.
     */
    OtsBounds2d getOtsBounds();

}
