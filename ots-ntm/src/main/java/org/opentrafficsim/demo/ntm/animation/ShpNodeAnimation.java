package org.opentrafficsim.demo.ntm.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.demo.ntm.NTMNode;

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
public class ShpNodeAnimation extends Renderable2D
{
    /**
     * @param source NTMNode;
     * @param simulator SimulatorInterface.TimeDoubleUnit;
     * @throws NamingException
     * @throws RemoteException
     */
    public ShpNodeAnimation(NTMNode source, SimulatorInterface.TimeDoubleUnit simulator) throws NamingException, RemoteException
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
