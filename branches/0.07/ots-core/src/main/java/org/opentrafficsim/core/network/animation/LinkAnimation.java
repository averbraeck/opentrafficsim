package org.opentrafficsim.core.network.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Link;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LinkAnimation extends Renderable2D
{
    /** */
    private float width;

    /**
     * @param source Link
     * @param simulator simulator
     * @param width width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
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
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(this.width));
        PaintPolygons.paintMultiPolygon(graphics, Color.RED, getSource().getLocation(),
            ((Link) getSource()).getDesignLine(), true);
        graphics.setStroke(oldStroke);
    }

}
