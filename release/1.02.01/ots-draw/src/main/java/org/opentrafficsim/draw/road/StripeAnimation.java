package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.NamingException;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.draw.core.ClonableRenderable2DInterface;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.Stripe;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Draw road stripes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StripeAnimation extends Renderable2D<Stripe> implements ClonableRenderable2DInterface<Stripe>, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The line type. */
    private final TYPE type;

    /** The points for the outline of the Stripe. */
    private final OTSLine3D line;

    /** Precision of buffer operations. */
    private static final int QUADRANTSEGMENTS = 8;

    /**
     * Generate the drawing commands for a dash pattern.
     * @param center LengthIndexedLine; the design line of the striped pattern
     * @param width double; width of the stripes in meters
     * @param startOffset double; shift the starting point in the pattern by this length in meters
     * @param onOffLengths double[]; one or more lengths of the dashes and the gaps between those dashes. If the number of
     *            values in <cite>onOffLengths</cite> is odd, the pattern repeats inverted. The first value in
     *            <cite>onOffLengths</cite> is the length of a dash.
     * @return ArrayList&lt;Coordinate&gt;; the coordinates of the dashes separated and terminated by a <cite>NEWPATH</cite>
     *         Coordinate
     */
    // TODO startOffset does not work if a dash falls inside of it (so below the offset is 2.99m, rather than 3m)
    private ArrayList<OTSPoint3D> makeDashes(final LengthIndexedLine center, final double width, final double startOffset,
            final double[] onOffLengths)
    {
        double period = 0;
        for (double length : onOffLengths)
        {
            if (length < 0)
            {
                throw new Error("Bad pattern - on or off length is < 0");
            }
            period += length;
        }
        if (period <= 0)
        {
            throw new Error("Bad pattern - repeat period length is 0");
        }
        double length = center.getEndIndex();
        double position = -startOffset;
        int phase = 0;
        ArrayList<OTSPoint3D> result = new ArrayList<>();
        while (position < length)
        {
            double nextBoundary = position + onOffLengths[phase++ % onOffLengths.length];
            if (nextBoundary > 0) // Skip this one; this entire dash lies within the startOffset
            {
                if (position < 0)
                {
                    position = 0; // Draw a partial dash, starting at 0 (begin of the center line)
                }
                double endPosition = nextBoundary;
                if (endPosition > length)
                {
                    endPosition = length; // Draw a partial dash, ending at length (end of the center line)
                }
                Coordinate[] oneDash = center.extractLine(position, endPosition)
                        .buffer(width / 2, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                for (int i = 0; i < oneDash.length; i++)
                {
                    result.add(new OTSPoint3D(oneDash[i]));
                }
                result.add(PaintPolygons.NEWPATH);
            }
            position = nextBoundary + onOffLengths[phase++ % onOffLengths.length];
        }
        return result;
    }

    /**
     * Generate the points needed to draw the stripe pattern.
     * @param stripe Stripe; the stripe
     * @param stripeType TYPE; the stripe type
     * @return Coordinate[]; array of Coordinate
     * @throws NamingException when <cite>type</cite> is not supported
     */
    private ArrayList<OTSPoint3D> makePoints(final Stripe stripe, final TYPE stripeType) throws NamingException
    {
        switch (this.type)
        {
            case DASHED:// ¦ - Draw a 3-9 dash pattern on the center line
                return makeDashes(new LengthIndexedLine(stripe.getCenterLine().getLineString()), 0.2, 3.0, new double[] {3, 9});

            case BLOCK:// : - Draw a 1-3 dash pattern on the center line
                return makeDashes(new LengthIndexedLine(stripe.getCenterLine().getLineString()), 0.45, 1.0,
                        new double[] {1, 3});

            case DOUBLE:// ||- Draw two solid lines
            {
                OTSLine3D centerLine = stripe.getCenterLine();
                Coordinate[] leftLine = centerLine.offsetLine(0.2).getLineString()
                        .buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                Coordinate[] rightLine = centerLine.offsetLine(-0.2).getLineString()
                        .buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                ArrayList<OTSPoint3D> result = new ArrayList<>(leftLine.length + rightLine.length);
                for (int i = 0; i < leftLine.length; i++)
                {
                    result.add(new OTSPoint3D(leftLine[i]));
                }
                for (int i = 0; i < rightLine.length; i++)
                {
                    result.add(new OTSPoint3D(rightLine[i]));
                }
                return result;
            }

            case LEFTONLY: // |¦ - Draw left solid, right 3-9 dashed
            {
                OTSLine3D centerLine = stripe.getCenterLine();
                Geometry rightDesignLine = centerLine.offsetLine(-0.2).getLineString();
                ArrayList<OTSPoint3D> result =
                        makeDashes(new LengthIndexedLine(rightDesignLine), 0.2, 0.0, new double[] {3, 9});
                Coordinate[] leftCoordinates = centerLine.offsetLine(0.2).getLineString()
                        .buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                for (int i = 0; i < leftCoordinates.length; i++)
                {
                    result.add(new OTSPoint3D(leftCoordinates[i]));
                }
                result.add(PaintPolygons.NEWPATH);
                return result;
            }

            case RIGHTONLY: // ¦| - Draw left 3-9 dashed, right solid
            {
                OTSLine3D centerLine = stripe.getCenterLine();
                Geometry leftDesignLine = centerLine.offsetLine(0.2).getLineString();
                ArrayList<OTSPoint3D> result = makeDashes(new LengthIndexedLine(leftDesignLine), 0.2, 0.0, new double[] {3, 9});
                Coordinate[] rightCoordinates = centerLine.offsetLine(-0.2).getLineString()
                        .buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                for (int i = 0; i < rightCoordinates.length; i++)
                {
                    result.add(new OTSPoint3D(rightCoordinates[i]));
                }
                result.add(PaintPolygons.NEWPATH);
                return result;
            }

            case SOLID:// | - Draw single solid line. This (regretfully) involves copying everything twice...
                return new ArrayList<>(Arrays.asList(stripe.getContour().getPoints()));

            default:
                throw new NamingException("Unsupported stripe type: " + stripeType);
        }

    }

    /**
     * @param source Stripe; s
     * @param simulator SimulatorInterface.TimeDoubleUnit; s
     * @param type TYPE; t
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     * @throws OTSGeometryException when something is very wrong with the geometry of the line
     */
    public StripeAnimation(final Stripe source, final SimulatorInterface.TimeDoubleUnit simulator, final TYPE type)
            throws NamingException, RemoteException, OTSGeometryException
    {
        super(source, simulator);
        this.type = type;
        ArrayList<OTSPoint3D> list = makePoints(source, type);
        if (!list.isEmpty())
        {
            this.line = new OTSLine3D(list);
        }
        else
        {
            // no dash within length
            this.line = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.line != null)
        {
            graphics.setStroke(new BasicStroke(2.0f));
            PaintPolygons.paintMultiPolygon(graphics, Color.WHITE, getSource().getLocation(), this.line, true);
        }
    }

    /**
     * Stripe type.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     */
    public enum TYPE
    {
        /** Single solid line. */
        SOLID,

        /** Line |¦ allow to go to left, but not to right. */
        LEFTONLY,

        /** Line ¦| allow to go to right, but not to left. */
        RIGHTONLY,

        /** Dashes ¦ allow to cross in both directions. */
        DASHED,

        /** Double solid line ||, don't cross. */
        DOUBLE,

        /** Block : allow to cross in both directions. */
        BLOCK
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<Stripe> clone(final Stripe newSource,
            final SimulatorInterface.TimeDoubleUnit newSimulator) throws NamingException, RemoteException
    {
        try
        {
            return new StripeAnimation(newSource, newSimulator, this.type);
        }
        catch (OTSGeometryException exception)
        {
            throw new RemoteException("Stripe animation clone failed", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "StripeAnimation [source = " + getSource().toString() + ", type=" + this.type + ", line=" + this.line + "]";
    }
}
