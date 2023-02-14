package org.opentrafficsim.draw.road;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.CrossSectionElement;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class CrossSectionElementAnimation extends Renderable2D<CrossSectionElement> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The animation color. */
    private final Color color;

    /**
     * @param source CrossSectionElement; cross section element
     * @param simulator OtsSimulatorInterface; simulator
     * @param color Color; the color to draw the shoulder with
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public CrossSectionElementAnimation(final CrossSectionElement source, final OtsSimulatorInterface simulator,
            final Color color) throws NamingException, RemoteException
    {
        super(source, simulator);
        this.color = color;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        CrossSectionElement shoulder = getSource();
        PaintPolygons.paintMultiPolygon(graphics, this.color, shoulder.getLocation(), shoulder.getContour(), true);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionElementAnimation [source = " + getSource().toString() + ", color=" + this.color + "]";
    }
}
