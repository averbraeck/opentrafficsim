package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoLine stores the drawing information about a line. This can be interpreted by a visualization or animation class.
 * <br>
 * <br>
 * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> The drawable type for which this is the DrawingInfo
 */
public class DrawingInfoLine<D extends Drawable> implements DrawingInfo
{
    /** lineColorer. */
    private Colorer<D> lineColorer = FixedColorer.black();

    /** lineWidth, to be interpreted by the animation package; could be relative. */
    private float lineWidth = 1.0f;

    /**
     * Create an empty DrawingInfo object for a line.
     */
    public DrawingInfoLine()
    {
    }

    /**
     * Create a DrawingInfo object for a line.
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoLine(final Color lineColor, final float lineWidth)
    {
        setLineColor(lineColor);
        this.lineWidth = lineWidth;
    }

    /**
     * Create a DrawingInfo object for a line.
     * @param lineColorer Colorer&lt;D&gt;; the line colorer
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoLine(final Colorer<D> lineColorer, final float lineWidth)
    {
        setLineColorer(lineColorer);
        this.lineWidth = lineWidth;
    }

    /**
     * The returned color could be dependent on the state of the object representing the line. E.g., it turns red under certain
     * circumstances.
     * @param drawable D; the object that could influence the color of the line
     * @return the color of the line
     */
    public final Color getLineColor(final D drawable)
    {
        return this.lineColorer.getColor(drawable);
    }

    /**
     * Set the line color using a FixedColorer.
     * @param lineColor Color; lineColor to set
     */
    public final void setLineColor(final Color lineColor)
    {
        this.lineColorer = FixedColorer.create(lineColor);
    }

    /**
     * Set the line colorer.
     * @param lineColorer Colorer&lt;D&gt;; lineColorer to set
     */
    public final void setLineColorer(final Colorer<D> lineColorer)
    {
        this.lineColorer = lineColorer;
    }

    /**
     * @return lineWidth
     */
    public final float getLineWidth()
    {
        return this.lineWidth;
    }

    /**
     * @param lineWidth float; lineWidth to set
     */
    public final void setLineWidth(final float lineWidth)
    {
        this.lineWidth = lineWidth;
    }
}
