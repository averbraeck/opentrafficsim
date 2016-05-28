package org.opentrafficsim.road.gtu.lane.object.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.gtu.lane.object.LaneBlockOnOff;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultBlockOnOffAnimation extends Renderable2D implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;
    
    /** The half width left and right of the center line that is used to draw the block. */
    private final double halfWidth;

    /**
     * Construct the DefaultCarAnimation for a LaneBlock (road block).
     * @param source the Car to draw
     * @param simulator the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException on communication failure
     */
    public DefaultBlockOnOffAnimation(final LaneBlockOnOff source, final OTSSimulatorInterface simulator)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.halfWidth = 0.4 * source.getLane().getWidth(0.0).getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) 
    {
        if (((LaneBlockOnOff) this.source).isBlocked())
        {
            graphics.setColor(Color.RED);
        }
        else
        {
            graphics.setColor(Color.GREEN);
        }
        Rectangle2D rectangle = new Rectangle2D.Double(-0.4, -this.halfWidth, 0.8, 2 * this.halfWidth);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DefaultBlockOnOffAnimation [getSource()=" + this.getSource() + "]";
    }

}
