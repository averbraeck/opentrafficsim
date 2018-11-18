package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoLine stores the drawing information about a line. This can be interpreted by a visualization or animation class.
 * <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DrawingInfoLine
{
    /** lineColor. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public Color lineColor = Color.BLACK;
    
    /** lineWidth, to be interpreted by the animation package; could be relative. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public float lineWidth = 1.0f;
}

