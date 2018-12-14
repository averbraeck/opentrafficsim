package org.opentrafficsim.core.animation;

import java.awt.Paint;

/**
 * PaintScale provides a source for paint instances, based on the PaintScale class in JFreeChart. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public interface PaintScale
{
    /**
     * Returns the lower bound for the scale.
     * @return the lower bound.
     */
    public double getLowerBound();

    /**
     * Returns the upper bound for the scale.
     * @return the upper bound.
     */
    public double getUpperBound();

    /**
     * Returns a Paint instance for the specified value.
     * @param value the value.
     * @return Paint; a Paint instance for the specified value
     */
    public Paint getPaint(double value);

}
