package org.opentrafficsim.draw;

import org.djutils.draw.Drawable2d;
import org.djutils.draw.Transform2d;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.point.Point2d;

/**
 * Bounds that can be transformed.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <B> bounds type
 */
public interface TransformableBounds<B extends TransformableBounds<B>> extends Bounds<B, Point2d, Drawable2d>
{

    /**
     * Returns a transformed copy of this bounds.
     * @param transformation Transform2d; transformation.
     * @return B; transformed copy of this bounds.
     */
    B transform(final Transform2d transformation);

}
