package org.opentrafficsim.base.geometry;

import org.djutils.draw.point.Point2d;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Locatable that specifies return types.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface OtsLocatable extends Locatable
{

    /** {@inheritDoc} */
    @Override
    Point2d getLocation();

    /**
     * Returns the bounds relative to the location.
     * @return OtsBounds2d; bounds relative to the location.
     */
    @Override
    OtsBounds2d getBounds();

}
