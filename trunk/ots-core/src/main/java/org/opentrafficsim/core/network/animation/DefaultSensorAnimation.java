package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Sensor;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1178 $, $LastChangedDate: 2015-08-04 02:55:18 +0200 (Tue, 04 Aug 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultSensorAnimation extends Renderable2D
{
    /** the color of the sensor. */
    private final Color color;

    /** the half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @param color the color on the animation
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public DefaultSensorAnimation(final Sensor source, final OTSSimulatorInterface simulator, final Color color)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
        this.halfWidth = 0.4 * source.getLane().getWidth(0.0).getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(this.color);
        Rectangle2D rectangle = new Rectangle2D.Double(-0.4, -this.halfWidth, 0.8, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DefaultBlockAnimation [getSource()=" + this.getSource() + "]";
    }

}
