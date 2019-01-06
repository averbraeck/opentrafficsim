package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * Colorer is the generic interface for anything that can get a static or dynamic color in an animation within OTS. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> the drawable type
 */
public interface Colorer<D extends Drawable>
{
    /**
     * Get the color, based on the Drawable object.
     * @param drawable D; the object to determine the color for
     * @return the (fill, line) color of the drawable object
     */
    Color getColor(D drawable);
}
