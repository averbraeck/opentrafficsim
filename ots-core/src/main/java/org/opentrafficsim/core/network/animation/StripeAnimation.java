package org.opentrafficsim.core.network.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSBuffering;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.lane.Stripe;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class StripeAnimation extends Renderable2D
{
    /** the line type. */
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
                Coordinate[] oneDash =
                    center.extractLine(position, endPosition).buffer(width / 2, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT)
                        .getCoordinates();
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
            case DASHED:// : - Draw a 3-9 dash pattern on the center line
                return makeDashes(new LengthIndexedLine(stripe.getCenterLine().getLineString()), 0.2, 0, new double[]{3, 9});

            case DOUBLE:// ||- Draw two solid lines
                try
                {
                    OTSLine3D centerLine = stripe.getCenterLine();
                    Coordinate[] leftLine =
                        OTSBuffering.offsetGeometry(centerLine, 0.2).getLineString().buffer(0.1, QUADRANTSEGMENTS,
                            BufferParameters.CAP_FLAT).getCoordinates();
                    Coordinate[] rightLine =
                        OTSBuffering.offsetGeometry(centerLine, -0.2).getLineString().buffer(0.1, QUADRANTSEGMENTS,
                            BufferParameters.CAP_FLAT).getCoordinates();
                    ArrayList<OTSPoint3D> result = new ArrayList<OTSPoint3D>(leftLine.length + rightLine.length);
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
                catch (OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
                return new ArrayList<OTSPoint3D>(); // Return an empty ArrayList after an error occurred

            case LEFTONLY: // |: - Draw left solid, right 3-9 dashed
                try
                {
                    OTSLine3D centerLine = stripe.getCenterLine();
                    Geometry rightDesignLine = OTSBuffering.offsetGeometry(centerLine, -0.2).getLineString();
                    ArrayList<OTSPoint3D> result =
                        makeDashes(new LengthIndexedLine(rightDesignLine), 0.2, 0, new double[]{3, 9});
                    Geometry leftDesignLine =
                        OTSBuffering.offsetGeometry(centerLine, 0.2).getLineString().buffer(0.1, QUADRANTSEGMENTS,
                            BufferParameters.CAP_FLAT);
                    Coordinate[] leftCoordinates =
                        leftDesignLine.buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                    for (int i = 0; i < leftCoordinates.length; i++)
                    {
                        result.add(new OTSPoint3D(leftCoordinates[i]));
                    }
                    result.add(PaintPolygons.NEWPATH);
                    return result;
                }
                catch (OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
                return new ArrayList<OTSPoint3D>(); // Return an empty ArrayList after an error occurred

            case RIGHTONLY: // :| - Draw left 3-9 dashed, right solid
                try
                {
                    OTSLine3D centerLine = stripe.getCenterLine();
                    Geometry leftDesignLine = OTSBuffering.offsetGeometry(centerLine, 0.2).getLineString();
                    ArrayList<OTSPoint3D> result =
                        makeDashes(new LengthIndexedLine(leftDesignLine), 0.2, 0, new double[]{3, 9});
                    Geometry rightDesignLine =
                        OTSBuffering.offsetGeometry(centerLine, -0.2).getLineString().buffer(0.1, QUADRANTSEGMENTS,
                            BufferParameters.CAP_FLAT);
                    Coordinate[] rightCoordinates =
                        rightDesignLine.buffer(0.1, QUADRANTSEGMENTS, BufferParameters.CAP_FLAT).getCoordinates();
                    for (int i = 0; i < rightCoordinates.length; i++)
                    {
                        result.add(new OTSPoint3D(rightCoordinates[i]));
                    }
                    result.add(PaintPolygons.NEWPATH);
                    return result;
                }
                catch (OTSGeometryException exception)
                {
                    exception.printStackTrace();
                }
                return new ArrayList<OTSPoint3D>(); // Return an empty ArrayList after an error occurred

            case SOLID:// | - Draw single solid line. This (regretfully) involves copying everything twice...
                return new ArrayList<OTSPoint3D>(Arrays.asList(stripe.getContour().getPoints()));

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
        this.line = new OTSLine3D(makePoints(source, type));
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        PaintPolygons.paintMultiPolygon(graphics, Color.WHITE, ((Stripe) getSource()).getLocation(), this.line);
    }

    /**
     * Stripe type.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     */
    public static enum TYPE
    {
        /** Single solid line. */
        SOLID,

        /** Line |: allow to go to left, but not to right. */
        LEFTONLY,

        /** Line :| allow to go to right, but not to left. */
        RIGHTONLY,

        /** Dashes : allow to cross in both directions. */
        DASHED,

        /** Double solid line ||, don't cross. */
        DOUBLE
    }
}
