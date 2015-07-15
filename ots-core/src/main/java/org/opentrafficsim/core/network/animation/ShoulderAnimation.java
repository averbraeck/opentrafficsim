package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Shoulder;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version ct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShoulderAnimation extends Renderable2D
{
    /**
     * @param source s
     * @param simulator s
     * @throws NamingException ne
     * @throws RemoteException re
     */
    public ShoulderAnimation(final Shoulder source, final OTSSimulatorInterface simulator) throws NamingException,
            RemoteException
    {
        super(source, simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        Shoulder shoulder = (Shoulder) getSource();
        PaintPolygons.paintMultiPolygon(graphics, Color.GREEN, shoulder.getLocation(), shoulder.getContour()
                .getCoordinates());
    }
}
