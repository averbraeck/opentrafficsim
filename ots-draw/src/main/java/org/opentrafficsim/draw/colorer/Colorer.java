package org.opentrafficsim.draw.colorer;

import java.awt.Color;

/**
 * Colorer is the generic interface for anything that can get a static or dynamic color in an animation within OTS.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to be colored
 */
public interface Colorer<T>
{

    /**
     * Get the color, based on the object.
     * @param object the object to determine the color for
     * @return the (fill, line) color of the object
     */
    Color getColor(T object);

    /**
     * Returns the name of the colorer.
     * @return name of the colorer
     */
    String getName();

}
