package org.opentrafficsim.core.object.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintPolygons;
import org.opentrafficsim.core.object.StaticObject;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StaticObjectAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** */
    private float width;

    /** */
    private Color color;

    /** */
    private boolean fill;

    /**
     * @param source Static Object
     * @param simulator simulator
     * @param width width of the contour line to draw
     * @param color color of the contour line / fill
     * @param fill fill internal or not
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public StaticObjectAnimation(final StaticObject source, final OTSSimulatorInterface simulator, final float width,
            final Color color, final boolean fill) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
        this.color = color;
        this.fill = fill;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        if (this.width > 0.0f)
        {
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(this.width));
            PaintPolygons.paintMultiPolygon(graphics, this.color, getSource().getLocation(),
                    ((StaticObject) getSource()).getGeometry(), this.fill);
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
     * @param width set width
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
     * @param color set color
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
     * @param fill set fill
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

}
