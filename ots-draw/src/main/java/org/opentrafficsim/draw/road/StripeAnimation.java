package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.draw.core.PaintPolygons;
import org.opentrafficsim.road.network.lane.Stripe;

import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2dInterface;

/**
 * Draw road stripes.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public class StripeAnimation extends Renderable2d<Stripe> implements Renderable2dInterface<Stripe>, Serializable
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** The points for the outline of the Stripe. */
    private final List<Point2d> line;

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
    private ArrayList<Point2d> makeDashes(final OtsLine2d center, final double width, final double startOffset,
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
        double length = center.getLength().si;
        double position = -startOffset;
        int phase = 0;
        ArrayList<Point2d> result = new ArrayList<>();
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

                OtsLine2d dashCenter;
                dashCenter = center.extract(position, endPosition);
                dashCenter.offsetLine(width / 2).getLine2d().getPoints().forEachRemaining(result::add);
                dashCenter.offsetLine(-width / 2).getLine2d().reverse().getPoints().forEachRemaining(result::add);
                result.add(PaintPolygons.NEWPATH);
            }
            position = nextBoundary + onOffLengths[phase++ % onOffLengths.length];
        }
        return result;
    }

    /**
     * Generate the points needed to draw the stripe pattern.
     * @param stripe Stripe; the stripe
     * @return Coordinate[]; array of Coordinate
     * @throws NamingException when <cite>type</cite> is not supported
     */
    private List<Point2d> makePoints(final Stripe stripe) throws NamingException
    {
        double width = stripe.getWidth(0.5).si;
        switch (stripe.getType())
        {
            case DASHED:// ¦ - Draw a 3-9 dash pattern on the center line
                return makeDashes(stripe.getCenterLine(), width, 3.0, new double[] {3, 9});

            case BLOCK:// : - Draw a 1-3 dash pattern on the center line
                return makeDashes(stripe.getCenterLine(), width, 1.0, new double[] {1, 3});

            case DOUBLE:// ||- Draw two solid lines
            {
                OtsLine2d centerLine = stripe.getCenterLine();
                List<Point2d> result = new ArrayList<>(centerLine.size() * 2);
                centerLine.offsetLine(width / 2).getLine2d().getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(-width / 2).getLine2d().reverse().getPoints().forEachRemaining(result::add);
                return result;
            }

            case LEFT: // |¦ - Draw left solid, right 3-9 dashed
            {
                OtsLine2d centerLine = stripe.getCenterLine();
                List<Point2d> result = makeDashes(centerLine.offsetLine(-width / 3), width / 3, 0.0, new double[] {3, 9});
                centerLine.offsetLine(width / 3).getLine2d().getPoints().forEachRemaining(result::add);
                return result;
            }

            case RIGHT: // ¦| - Draw left 3-9 dashed, right solid
            {
                OtsLine2d centerLine = stripe.getCenterLine();
                ArrayList<Point2d> result = makeDashes(centerLine.offsetLine(width / 3), width / 3, 0.0, new double[] {3, 9});
                centerLine.offsetLine(-width / 3).getLine2d().getPoints().forEachRemaining(result::add);
                return result;
            }

            case SOLID:// | - Draw single solid line. This (regretfully) involves copying everything twice...
                List<Point2d> result = new ArrayList<>(stripe.getContour().size());
                stripe.getContour().getPoints().forEachRemaining(result::add);
                return result;

            default:
                throw new NamingException("Unsupported stripe type: " + stripe.getType());
        }

    }

    /**
     * @param source Stripe; s
     * @param simulator OtsSimulatorInterface; s
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     * @throws OtsGeometryException when something is very wrong with the geometry of the line
     */
    public StripeAnimation(final Stripe source, final OtsSimulatorInterface simulator)
            throws NamingException, RemoteException, OtsGeometryException
    {
        super(source, simulator);
        List<Point2d> list = makePoints(source);
        if (!list.isEmpty())
        {
            this.line = list;
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "StripeAnimation [source = " + getSource().toString() + ", line=" + this.line + "]";
    }
}
