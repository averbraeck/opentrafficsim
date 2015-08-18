package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.ntm.Link;
import org.opentrafficsim.gui.OTSRenderable2D;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends OTSRenderable2D
{
    /** stroke width for drawing links. */
    private final float width;

    /**
     * @param link
     * @param simulator
     * @param width
     * @throws NamingException
     * @throws RemoteException
     */
    public LinkAnimation(final Link link, final OTSSimulatorInterface simulator, final float width)
            throws NamingException, RemoteException
    {
        super(link, simulator);
        this.width = width;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.BLACK);
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(this.width));
        Coordinate a = ((Link) getSource()).getStartNode().getPoint().getCoordinate();
        Coordinate b = ((Link) getSource()).getEndNode().getPoint().getCoordinate();
        // draw relative to point A (getLocation)
        graphics.draw(new Line2D.Double(0.0, 0.0, b.x - a.x, a.y - b.y));
        graphics.setStroke(oldStroke);
    }

}
