package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Stripe;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StripeAnimation extends Renderable2D
{
    /** the line type. */
    private final TYPE type;

    /**
     * @param source s
     * @param simulator s
     * @param type t
     * @throws NamingException ne
     * @throws RemoteException re
     */
    public StripeAnimation(final Stripe source, final OTSSimulatorInterface simulator, final TYPE type)
        throws NamingException, RemoteException
    {
        super(source, simulator);
        this.type = type;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.WHITE);
        Stripe stripe = (Stripe) getSource();
        DirectedPoint p = stripe.getLocation();
        Geometry g = stripe.getContour();
        Coordinate[] coordinates = g.getCoordinates();
        Path2D.Double path = new Path2D.Double();
        boolean start = false;
        for (Coordinate c : coordinates)
        {
            if (!start)
            {
                start = true;
                path.moveTo(c.x - p.x, -c.y + p.y);
            }
            else
            {
                path.lineTo(c.x - p.x, -c.y + p.y);
            }
        }
        path.closePath();
        graphics.fill(path);
    }

    /**
     * Stripe type.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     */
    public static enum TYPE
    {
        /** solid line. */
        SOLID,

        /** line |: allow to go to left, but not to right. */
        LEFTONLY,

        /** line :| allow to go to right, but not to left. */
        RIGHTONLY,

        /** line : allow to cross in both directions. */
        DASHED,

        /** double solid line ||, don't cross. */
        DOUBLE
    }
}
