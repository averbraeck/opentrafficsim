package org.opentrafficsim.demo.ntm.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.ntm.AreaNode;
import org.opentrafficsim.demo.ntm.AreaNodeNTM;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class NodeAnimation extends Renderable2D
{
    /**
     * @param node
     * @param simulator
     * @throws NamingException
     * @throws RemoteException
     */
    public NodeAnimation(AreaNode node, OTSSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(node, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics2D graphics, ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.BLACK);
        graphics.draw(new Ellipse2D.Double(-10.0, -10.0, 20.0, 20.0));
    }

}
