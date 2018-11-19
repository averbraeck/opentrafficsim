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
        super();
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
        super();
        setFillColor(fillColor);
        this.filled = true;
        this.stroked = false;
    }

    /**
     * Create a DrawingInfo object for a shape that is not filled.
     * @param lineColorer Colorer; the line colorer
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     */
    public DrawingInfoShape(final Colorer<D> lineColorer, final float lineWidth)
    {
        super();
        setLineColorer(lineColorer);
        this.lineWidth = lineWidth;
        this.filled = false;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, with a contour line.
     * @param lineColorer Colorer; the line colorer
     * @param lineWidth float; the line width, which could be relative and is open for interpretation by the visualization or
     *            animation class
     * @param fillColorer Colorer; the fill colorer
     */
    public DrawingInfoShape(final Colorer<D> lineColorer, final float lineWidth, final Colorer<D> fillColorer)
    {
        super();
        setLineColorer(lineColorer);
        this.lineWidth = lineWidth;
        setFillColorer(fillColorer);
        this.filled = true;
        this.stroked = true;
    }

    /**
     * Create a DrawingInfo object for a shape that is filled, without a contour line.
     * @param fillColorer Colorer; the fill colorer
     */
    public DrawingInfoShape(final Colorer<D> fillColorer)
    {
        super();
        setFillColorer(fillColorer);
        this.filled = true;
        this.stroked = false;
    }

    /**
     * The returned color could be dependent on the state of the object for the fill color used. E.g., it turns red under
     * certain circumstances.
     * @param drawable the object that could influence the fill color
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
     * @param fillColorer Colorer; fillColorer to set
     */
    public final void setFillColorer(final Colorer<D> fillColorer)
    {
        this.fillColorer = fillColorer;
    }

    /**
     * The returned color could be dependent on the state of the object representing the line. E.g., it turns red under certain
     * circumstances.
     * @param drawable the object that could influence the color of the line
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
     * @param lineColorer Colorer; lineColorer to set
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
