package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneAnimation extends Renderable2D
{
    /** color of the lane. */
    private final Color color;

    /**
     * @param source s
     * @param simulator s
     * @param color color of the lane.
     * @throws NamingException ne
     * @throws RemoteException re
     */
    public LaneAnimation(final Lane<?, ?> source, final OTSSimulatorInterface simulator, final Color color)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        Lane<?, ?> lane = (Lane<?, ?>) getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, lane.getLocation(), lane.getContour());
    }
}
