package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.lane.LaneBlock;
import org.opentrafficsim.core.gtu.lane.LaneBlockOnOff;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultBlockOnOffAnimation extends Renderable2D
{
    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public DefaultBlockOnOffAnimation(final LaneBlockOnOff source, final OTSSimulatorInterface simulator)
        throws NamingException, RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        if (((LaneBlockOnOff) source).isBlocked())
        {
            graphics.setColor(Color.RED);
        }
        else
        {
            graphics.setColor(Color.GREEN);
        }
        Rectangle2D rectangle = new Rectangle2D.Double(-0.5, -1.8, 0.5, 1.8);
        graphics.draw(rectangle);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DefaultBlockAnimation [getSource()=" + this.getSource() + "]";
    }

}
