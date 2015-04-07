package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.lane.Stripe;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StripeAnimation extends Renderable2D
{
    /** the line type. */
    private final TYPE type;

    /** The Coordinates that for the outline of the Stripe. */
    private final Coordinate[] coordinates;

    /** Precision of buffer operations. */
    private static final int QUADRANTSEGMENTS = 8;
    
    /** Dummy coordinate that forces the drawing operation to start a new path. */
    private static final Coordinate NEWPATH = new Coordinate(Double.NaN, Double.NaN);

    /**
     * Generate the coordinates needed to draw the stripe
     * @param stripe Stripe; the stripe
     * @param stripeType TYPE; the stripe type
     * @return Coordinate[]; array of Coordinate
     * @throws NamingException when <cite>type</cite> is not supported
     */
    private Coordinate[] makeCoordinates(final Stripe stripe, final TYPE stripeType) throws NamingException
    {
        switch (this.type)
        {
            case DASHED:
            {
                // Draw a 3-9 stripe pattern
                double onLength = 3;
                double width = 0.15;// m
                LengthIndexedLine centerLine = new LengthIndexedLine(stripe.getCenterLine());
                double period = 12;// meter
                double position = 0;// TODO should be initialized from a field in the Stripe
                double length = stripe.getLength().getSI();
                ArrayList<Coordinate> coordList = new ArrayList<Coordinate>();
                while (position < length)
                {
                    double endIndex = position + onLength;
                    if (endIndex > length)
                    {
                        endIndex = length;
                    }
                    Geometry stripeCenterGeometry = centerLine.extractLine(position, endIndex);
                    Coordinate[] bufferCoordinates =
                            stripeCenterGeometry.buffer(width / 2, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT)
                                    .getCoordinates();
                    if (coordList.size() > 0)
                    {
                        coordList.add(NEWPATH);
                    }
                    for (int i = 0; i < bufferCoordinates.length; i++)
                    {
                        coordList.add(bufferCoordinates[i]);
                    }
                    position += period;
                }
                Coordinate[] result = new Coordinate[coordList.size()];
                coordList.toArray(result);
                return result;
            }
            case DOUBLE:
                return stripe.getContour().getCoordinates();// FIXME STUB
            case LEFTONLY:
                return stripe.getContour().getCoordinates();// FIXME STUB
            case RIGHTONLY:
                return stripe.getContour().getCoordinates();// FIXME STUB
            case SOLID:
                return stripe.getContour().getCoordinates();
            default:
                throw new NamingException("Unsupported stripe type: " + stripeType);
        }

    }

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
        this.coordinates = makeCoordinates(source, type);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        graphics.setColor(Color.WHITE);
        Stripe stripe = (Stripe) getSource();
        DirectedPoint p = stripe.getLocation();
        Path2D.Double path = new Path2D.Double();
        boolean continuation = false;
        for (Coordinate c : this.coordinates)
        {
            if (c == NEWPATH)
            {
                path.closePath();
                graphics.fill(path);
                path = new Path2D.Double();
                continuation = false;
            }
            else if (!continuation)
            {
                continuation = true;
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
    public static enum TYPE {
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
