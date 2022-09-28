package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * DrawingInfoLine stores the drawing information about a shape. This can be interpreted by a visualization or animation class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @param <D> The drawable type for which this is the DrawingInfo
 */
public class DrawingInfoShape<D extends Drawable> implements DrawingInfo
{
    /** fillColorer. */
    private Colorer<D> fillColorer = FixedColorer.gray();

    /** lineColorer. */
    private Colorer<D> lineColorer = FixedColorer.black();

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
    }

    /**
     * Create a DrawingInfo object for a shape that is not filled.
     * @param lineColor Color; the line color
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoShape(final Color lineColor, final float lineWidth)
    {
        setLineColor(lineColor);
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
        setLineColor(lineColor);
        this.lineWidth = lineWidth;
        setFillColor(fillColor);
        this.filled = true;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, without a contour line.
     * @param fillColor Color; the fill color
     */
    public DrawingInfoShape(final Color fillColor)
    {
        setFillColor(fillColor);
        this.filled = true;
        this.stroked = false;
    }

    /**
     * Create a DrawingInfo object for a shape that is not filled.
     * @param lineColorer Colorer&lt;D&gt;; the line colorer
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoShape(final Colorer<D> lineColorer, final float lineWidth)
    {
        setLineColorer(lineColorer);
        this.lineWidth = lineWidth;
        this.filled = false;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, with a contour line.
     * @param lineColorer Colorer&lt;D&gt;; the line colorer
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     * @param fillColorer Colorer&lt;D&gt;; the fill colorer
     */
    public DrawingInfoShape(final Colorer<D> lineColorer, final float lineWidth, final Colorer<D> fillColorer)
    {
        setLineColorer(lineColorer);
        this.lineWidth = lineWidth;
        setFillColorer(fillColorer);
        this.filled = true;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, without a contour line.
     * @param fillColorer Colorer&lt;D&gt;; the fill colorer
     */
    public DrawingInfoShape(final Colorer<D> fillColorer)
    {
        setFillColorer(fillColorer);
        this.filled = true;
        this.stroked = false;
    }

    /**
     * The returned color could be dependent on the state of the object for the fill color used. E.g., it turns red under
     * certain circumstances.
     * @param drawable D; the object that could influence the fill color
     * @return the color of the fill
     */
    public final Color getFillColor(final D drawable)
    {
        return this.fillColorer.getColor(drawable);
    }

    /**
     * Set the fill color using a FixedColorer.
     * @param fillColor Color; fillColor to set
     */
    public final void setFillColor(final Color fillColor)
    {
        this.fillColorer = FixedColorer.create(fillColor);
    }

    /**
     * Set the fill colorer.
     * @param fillColorer Colorer&lt;D&gt;; fillColorer to set
     */
    public final void setFillColorer(final Colorer<D> fillColorer)
    {
        this.fillColorer = fillColorer;
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
     * @param lineWidth float; set lineWidth
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
     * @param filled boolean; set filled
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
     * @param stroked boolean; set stroked
     */
    public final void setStroked(final boolean stroked)
    {
        this.stroked = stroked;
    }

}
