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
public class DrawingInfoLine implements DrawingInfo
{
    /** lineColor. */
    private Color lineColor = Color.BLACK;

    /** lineWidth, to be interpreted by the animation package; could be relative. */
    private float lineWidth = 1.0f;

    /**
     * Create an empty DrawingInfo object for a line.
     */
    public DrawingInfoLine()
    {
        super();
    }
    
    /**
     * Create a DrawingInfo object for a line.
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoLine(final Color lineColor, final float lineWidth)
    {
        super();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth;
    }

    /**
     * @return lineColor
     */
    public final Color getLineColor()
    {
        return this.lineColor;
    }

    /**
     * @param lineColor set lineColor
     */
    public final void setLineColor(final Color lineColor)
    {
        this.lineColor = lineColor;
    }

    /**
     * @return lineWidth
     */
    public final float getLineWidth()
    {
        return this.lineWidth;
    }

    /**
     * @param lineWidth set lineWidth
     */
    public final void setLineWidth(final float lineWidth)
    {
        this.lineWidth = lineWidth;
    }
}
