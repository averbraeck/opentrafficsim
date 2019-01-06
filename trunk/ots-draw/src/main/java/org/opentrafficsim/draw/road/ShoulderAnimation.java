package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.Shoulder;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ShoulderAnimation extends Renderable2D<Shoulder> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The animation color. */
    private final Color color;

    /**
     * @param source Shoulder; s
     * @param simulator SimulatorInterface.TimeDoubleUnit; s
     * @param color Color; the color to draw the shoulder with
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public ShoulderAnimation(final Shoulder source, final SimulatorInterface.TimeDoubleUnit simulator, final Color color)
            throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Shoulder shoulder = getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, shoulder.getLocation(), shoulder.getContour(), true);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ShoulderAnimation [source = " + getSource().toString() + ", color=" + this.color + "]";
    }
}
