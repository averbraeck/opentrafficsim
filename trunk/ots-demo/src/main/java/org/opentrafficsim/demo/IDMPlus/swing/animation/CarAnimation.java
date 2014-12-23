package org.opentrafficsim.demo.IDMPlus.swing.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CarAnimation extends Renderable2D
{
    /** Color of this car. */
    final Color color;
    
    /** Length of this car. */
    final double length;
    
    /**
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public CarAnimation(final AnimatedCar source, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.length = source.getLength().getSI();
        this.color = colorTable[source.getId() % colorTable.length];
    }

    /**
     * Colors for the cars.
     */
    static final Color[] colorTable = {Color.BLACK, new Color(0xa5, 0x2a, 0x2a), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY};
    
    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(this.color);
        graphics.draw(new Rectangle2D.Double(-this.length / 2, -1.0d, this.length, 2.0d));
        graphics.setColor(Color.WHITE);
        graphics.draw(new Ellipse2D.Double(this.length / 2 -1.5d, -0.5d, 1d, 1d));
    }

}
