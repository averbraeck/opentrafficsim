package org.opentrafficsim.draw.object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.object.StaticObjectAnimation.StaticObjectData;
import org.opentrafficsim.draw.road.OtsRenderable;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Generic animation of a static object.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class StaticObjectAnimation extends OtsRenderable<StaticObjectData>
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** */
    private float width;

    /** */
    private Color color;

    /** */
    private boolean fill;

    /** Drawable paths. */
    private final Set<Path2D.Double> paths;

    /**
     * @param source StaticObjectData; Static Object
     * @param contextualized Contextualized; context provider
     * @param width float; width of the contour line to draw
     * @param color Color; color of the contour line / fill
     * @param fill boolean; fill internal or not
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
        this.paths = PaintPolygons.getPaths(getSource().getLocation(), getSource().getGeometry());
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.width > 0.0f)
        {
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(this.width));
            PaintPolygons.paintPaths(graphics, this.color, this.paths, this.fill);
            graphics.setStroke(oldStroke);
        }
    }

    /**
     * @return width
     */
    public final float getWidth()
    {
        return this.width;
    }

    /**
     * @param width float; set width
     */
    public final void setWidth(final float width)
    {
        this.width = width;
    }

    /**
     * @return color
     */
    public final Color getColor()
    {
        return this.color;
    }

    /**
     * @param color Color; set color
     */
    public final void setColor(final Color color)
    {
        this.color = color;
    }

    /**
     * @return fill
     */
    public final boolean isFill()
    {
        return this.fill;
    }

    /**
     * @param fill boolean; set fill
     */
    public final void setFill(final boolean fill)
    {
        this.fill = fill;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "StaticObjectAnimation [width=" + this.width + ", color=" + this.color + ", fill=" + this.fill + "]";
    }

    /**
     * StaticObjectData provides the information required to draw a static object.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface StaticObjectData extends Locatable
    {
        /**
         * Returns the geometry of the object.
         * @return List&lt;Point2d&gt; list of points of the geometry.
         */
        List<Point2d> getGeometry();

        /** {@inheritDoc} */
        @Override
        Point2d getLocation();
    }

}
