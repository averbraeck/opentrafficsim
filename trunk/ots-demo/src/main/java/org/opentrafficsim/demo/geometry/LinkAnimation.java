package org.opentrafficsim.demo.geometry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D
{
    /** stroke width for drawing links. */
    private final float width;

    /**
     * @param source s
     * @param simulator s
     * @param width w
     * @throws NamingException ne
     * @throws RemoteException re
     */
    public LinkAnimation(final Link source, final OTSSimulatorInterface simulator, final float width)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.width = width;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.ORANGE);
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(this.width));
        Coordinate a = ((Link) getSource()).getStartNode().getPoint();
        Coordinate b = ((Link) getSource()).getEndNode().getPoint();
        // draw relative to point A (getLocation)
        graphics.draw(new Line2D.Double(0.0, 0.0, b.x - a.x, a.y - b.y));
        graphics.setStroke(oldStroke);
    }
}
