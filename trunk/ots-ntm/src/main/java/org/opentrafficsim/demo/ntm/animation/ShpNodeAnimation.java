package org.opentrafficsim.demo.ntm.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.ntm.Node;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionSep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShpNodeAnimation extends Renderable2D
{
    /**
     * @param source
     * @param simulator
     * @throws NamingException
     * @throws RemoteException
     */
    public ShpNodeAnimation(Node source, OTSSimulatorInterface simulator) throws NamingException, RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public void paint(Graphics2D graphics, ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.ORANGE);
        graphics.setBackground(Color.ORANGE);
        graphics.draw(new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0));
    }

}
