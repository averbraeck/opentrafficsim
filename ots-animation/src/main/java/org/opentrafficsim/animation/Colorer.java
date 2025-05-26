package org.opentrafficsim.animation;

import java.awt.Color;

import org.opentrafficsim.base.geometry.OtsShape;

/**
 * Colorer is the generic interface for anything that can get a static or dynamic color in an animation within OTS.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> the drawable type
 */
public interface Colorer<D extends OtsShape>
{
    /**
     * Get the color, based on the OtsLocatable object.
     * @param drawable the object to determine the color for
     * @return the (fill, line) color of the locatable object
     */
    Color getColor(D drawable);
    
    /**
     * Returns the name of the colorer.
     * @return name of the colorer
     */
    String getName();
}
