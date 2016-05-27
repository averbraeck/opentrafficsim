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
import org.opentrafficsim.road.gtu.lane.object.LaneBlock;

/**
 * Draw a road block.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 29 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DefaultBlockAnimation extends Renderable2D implements Serializable
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
    public DefaultBlockAnimation(final LaneBlock source, final OTSSimulatorInterface simulator) throws NamingException,
        RemoteException
    {
        super(source, simulator);
        this.halfWidth = 0.4 * source.getLane().getWidth(0.0).getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLACK);
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
