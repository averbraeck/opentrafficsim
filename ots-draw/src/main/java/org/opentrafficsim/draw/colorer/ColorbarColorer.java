package org.opentrafficsim.draw.colorer;

import java.text.NumberFormat;

import org.opentrafficsim.draw.BoundsPaintScale;

/**
 * Interface of colorers that have a colorbar (as opposed to, or possibly additional to, a legend).
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public interface ColorbarColorer<T> extends Colorer<T>
{

    /**
     * Returns the bound paint scale to fill the color bar.
     * @return bound paint scale to fill the color bar
     */
    BoundsPaintScale getBoundsPaintScale();

    /**
     * Returns the number format for values along the colorbar.
     * @return number format for values along the colorbar
     */
    NumberFormat getNumberFormat();

}
