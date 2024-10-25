package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.draw.ClickableLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.road.SpeedSignAnimation.SpeedSignData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class SpeedSignAnimation extends OtsRenderable<SpeedSignData>
{

    /** */
    private static final long serialVersionUID = 20170420L;

    /** Radius in meters. */
    private static final double RADIUS = 1.6;

    /** Radius in meters. */
    private static final double EDGE = 1.3;

    /**
     * @param source speed sign
     * @param contextualized context provider
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public SpeedSignAnimation(final SpeedSignData source, final Contextualized contextualized)
            throws NamingException, RemoteException
    {
        super(source, contextualized);
        setRotate(false);
    }

    @Override
    public final void paint(final Graphics2D g, final ImageObserver arg1)
    {
        setRendering(g);
        Ellipse2D ellipse = new Ellipse2D.Double(-RADIUS, -RADIUS, 2 * RADIUS, 2 * RADIUS);
        g.setColor(Color.RED);
        g.fill(ellipse);
        ellipse = new Ellipse2D.Double(-EDGE, -EDGE, 2 * EDGE, 2 * EDGE);
        g.setColor(Color.WHITE);
        g.fill(ellipse);
        g.setColor(Color.BLACK);
        int speed = (int) getSource().getSpeed().getInUnit(SpeedUnit.KM_PER_HOUR);
        if (speed < 100)
        {
            g.setFont(new Font("Arial", 0, -1).deriveFont(2.0f));
        }
        else
        {
            g.setFont(new Font("Arial narrow", 0, -1).deriveFont(1.85f));
        }
        String str = Integer.toString(speed);
        Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(str, g);
        g.drawString(str, (float) -stringBounds.getCenterX(), (float) -stringBounds.getCenterY());
        resetRendering(g);
    }

    @Override
    public final String toString()
    {
        return "SpeedSignAnimation";
    }

    /**
     * LinkData provides the information required to draw a link.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface SpeedSignData extends ClickableLocatable
    {
        /**
         * Returns the speed.
         * @return speed.
         */
        Speed getSpeed();

        @Override
        default double getZ()
        {
            return DrawLevel.OBJECT.getZ();
        }
    }

}
