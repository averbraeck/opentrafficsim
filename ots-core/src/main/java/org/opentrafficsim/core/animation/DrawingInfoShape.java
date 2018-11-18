package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoLine stores the drawing information about a shape. This can be interpreted by a visualization or animation class.
 * <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class DrawingInfoShape implements DrawingInfo
{
    /** fillColor. */
    private Color fillColor = Color.GRAY;

    /** lineColor. */
    private Color lineColor = Color.BLACK;

    /** lineWidth, to be interpreted by the animation package; could be relative. */
    private float lineWidth = 1.0f;

    /** fill? */
    private boolean filled = true;

    /** draw contour line? */
    private boolean stroked = true;

    /**
     * Create an empty DrawingInfo object for a shape.
     */
    public DrawingInfoShape()
    {
        super();
    }

    /**
     * Create a DrawingInfo object for a shape that is not filled.
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoShape(final Color lineColor, final float lineWidth)
    {
        super();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth;
        this.filled = false;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, with a contour line.
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     * @param fillColor Color; the fill color
     */
    public DrawingInfoShape(final Color lineColor, final float lineWidth, final Color fillColor)
    {
        super();
        this.lineColor = lineColor;
        this.lineWidth = lineWidth;
        this.fillColor = fillColor;
        this.filled = true;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, without a contour line.
     * @param fillColor Color; the fill color
     */
    public DrawingInfoShape(final Color fillColor)
    {
        super();
        this.fillColor = fillColor;
        this.filled = true;
        this.stroked = false;
    }

    /**
     * @return fillColor
     */
    public final Color getFillColor()
    {
        return this.fillColor;
    }

    /**
     * @param fillColor set fillColor
     */
    public final void setFillColor(final Color fillColor)
    {
        this.fillColor = fillColor;
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

    /**
     * @return filled
     */
    public final boolean isFilled()
    {
        return this.filled;
    }

    /**
     * @param filled set filled
     */
    public final void setFilled(final boolean filled)
    {
        this.filled = filled;
    }

    /**
     * @return stroked
     */
    public final boolean isStroked()
    {
        return this.stroked;
    }

    /**
     * @param stroked set stroked
     */
    public final void setStroked(final boolean stroked)
    {
        this.stroked = stroked;
    }

}
