package org.opentrafficsim.demo.ntm.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.locationtech.jts.geom.Coordinate;
import org.opentrafficsim.demo.ntm.NTMLink;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D
{
    /** Stroke width for drawing links. */
    private final float width;

    /**
     * @param link NTMLink;
     * @param simulator SimulatorInterface.TimeDoubleUnit;
     * @param width float;
     * @throws NamingException
     * @throws RemoteException
     */
    public LinkAnimation(final NTMLink link, final SimulatorInterface.TimeDoubleUnit simulator, final float width)
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
        Coordinate a = ((NTMLink) getSource()).getStartNode().getPoint().getCoordinate();
        Coordinate b = ((NTMLink) getSource()).getEndNode().getPoint().getCoordinate();
        // draw relative to point A (getLocation)
        graphics.draw(new Line2D.Double(0.0, 0.0, b.x - a.x, a.y - b.y));
        graphics.setStroke(oldStroke);
    }

}
