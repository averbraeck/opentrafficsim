package org.opentrafficsim.core.car;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * Draw a car.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class DefaultCarAnimation extends Renderable2D
{
    /** Color of this car. */
    private final Color color;

    /** Counter used to cycle through the colors in colorTable. */
    private static int nextIndex = 0;

    /**
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public DefaultCarAnimation(final LaneBasedIndividualCar<?> source, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = COLORTABLE[++nextIndex % COLORTABLE.length];
    }

    /**
     * Colors for the cars.
     */
    private static final Color[] COLORTABLE = {Color.BLACK, new Color(0xa5, 0x2a, 0x2a), Color.RED, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY};

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        final LaneBasedIndividualCar<?> car = (LaneBasedIndividualCar<?>) getSource();
        final double length = car.getLength().getSI();
        final double width = car.getWidth().getSI();
        graphics.setColor(this.color);
        graphics.draw(new Rectangle2D.Double(-length / 2, -width / 2, length, width));
        // Draw a 1m diameter white disk about 1m before the front to indicate which side faces forward
        graphics.setColor(Color.WHITE);
        graphics.draw(new Ellipse2D.Double(length / 2 - 1.5d, -0.5d, 1d, 1d));
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DefaultCarAnimation [getSource()=" + this.getSource() + "]";
    }

}
