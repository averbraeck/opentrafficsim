package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Shoulder;
import org.opentrafficsim.gui.OTSRenderable2D;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShoulderAnimation extends OTSRenderable2D
{
    /** the animation color. */
    private final Color color;
    
    /**
     * @param source s
     * @param simulator s
     * @param color the color to draw the shoulder with
     * @throws NamingException ne
     * @throws RemoteException re
     */
    public ShoulderAnimation(final Shoulder source, final OTSSimulatorInterface simulator, final Color color) throws NamingException,
        RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        Shoulder shoulder = (Shoulder) getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, shoulder.getLocation(), shoulder.getContour());
    }
}
