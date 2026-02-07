package org.opentrafficsim.draw.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.Set;

import javax.naming.NamingException;

import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.object.StaticObjectAnimation.StaticObjectData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Generic animation of a static object.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StaticObjectAnimation extends OtsRenderable<StaticObjectData>
{
    /** Width. */
    private float width;

    /** Color. */
    private Color color;

    /** Fill. */
    private boolean fill;

    /** Drawable paths. */
    private final Set<Path2D.Float> paths;

    /**
     * Constructor.
     * @param source Static Object
     * @param contextualized context provider
     * @param width width of the contour line to draw
     * @param color color of the contour line / fill
     * @param fill fill internal or not
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public StaticObjectAnimation(final StaticObjectData source, final Contextualized contextualized, final float width,
            final Color color, final boolean fill) throws NamingException, RemoteException
    {
        super(source, contextualized);
        this.width = width;
        this.color = color;
        this.fill = fill;
        this.paths = PaintPolygons.getPaths(getSource().getLocation(), getSource().getAbsoluteContour().getPointList());
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.width > 0.0f)
        {
            Stroke oldStroke = graphics.getStroke();
            setRendering(graphics);
            graphics.setStroke(new BasicStroke(this.width));
            PaintPolygons.paintPaths(graphics, this.color, this.paths, this.fill);
            graphics.setStroke(oldStroke);
            resetRendering(graphics);
        }
    }

    /**
     * Returns width.
     * @return width
     */
    public final float getWidth()
    {
        return this.width;
    }

    /**
     * Sets width.
     * @param width set width
     */
    public final void setWidth(final float width)
    {
        this.width = width;
    }

    /**
     * Returns color.
     * @return color
     */
    public final Color getColor()
    {
        return this.color;
    }

    /**
     * Sets color.
     * @param color set color
     */
    public final void setColor(final Color color)
    {
        this.color = color;
    }

    /**
     * Returns whether to fill.
     * @return fill
     */
    public final boolean isFill()
    {
        return this.fill;
    }

    /**
     * Sets whether to fill.
     * @param fill set fill
     */
    public final void setFill(final boolean fill)
    {
        this.fill = fill;
    }

    @Override
    public final String toString()
    {
        return "StaticObjectAnimation [width=" + this.width + ", color=" + this.color + ", fill=" + this.fill + "]";
    }

    /**
     * StaticObjectData provides the information required to draw a static object.
     * <p>
     * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface StaticObjectData extends OtsShape
    {
    }

}
