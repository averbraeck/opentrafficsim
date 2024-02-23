package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw road stripes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class StripeAnimation extends OtsRenderable<StripeData>
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Drawable paths. */
    private final Set<Path2D.Double> paths;

    /**
     * @param source StripeData; stripe data
     * @param contextualized Contextualized; context provider
     * @throws NamingException ne
     * @throws RemoteException on communication failure
     */
    public StripeAnimation(final StripeData source, final Contextualized contextualized) throws NamingException, RemoteException
    {
        super(source, contextualized);
        List<Point2d> list = makePoints(source);
        if (!list.isEmpty())
        {
            this.paths = PaintPolygons.getPaths(getSource().getLocation(), list);
        }
        else
        {
            // no dash within length
            this.paths = null;
        }
    }

    /**
     * Generate the drawing commands for a dash pattern.
     * @param center PolyLine2d; the design line of the striped pattern
     * @param width double; width of the stripes in meters
     * @param startOffset double; shift the starting point in the pattern by this length in meters
     * @param onOffLengths double[]; one or more lengths of the dashes and the gaps between those dashes. If the number of
     *            values in <cite>onOffLengths</cite> is odd, the pattern repeats inverted. The first value in
     *            <cite>onOffLengths</cite> is the length of a dash.
     * @return ArrayList&lt;Coordinate&gt;; the coordinates of the dashes separated and terminated by a <cite>NEWPATH</cite>
     *         Coordinate
     */
    private ArrayList<Point2d> makeDashes(final PolyLine2d center, final double width, final double startOffset,
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
        double length = center.getLength();
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

                PolyLine2d dashCenter;
                dashCenter = center.extract(position, endPosition);
                dashCenter.offsetLine(width / 2).getPoints().forEachRemaining(result::add);
                dashCenter.offsetLine(-width / 2).reverse().getPoints().forEachRemaining(result::add);
                result.add(PaintPolygons.NEWPATH);
            }
            position = nextBoundary + onOffLengths[phase++ % onOffLengths.length];
        }
        return result;
    }

    /**
     * Generate the points needed to draw the stripe pattern.
     * @param stripe StripeData; the stripe
     * @return Coordinate[]; array of Coordinate
     * @throws NamingException when <cite>type</cite> is not supported
     */
    private List<Point2d> makePoints(final StripeData stripe) throws NamingException
    {
        double width = stripe.getWidth().si;
        switch (stripe.getType())
        {
            case DASHED:// ¦ - Draw a 3-9 dash pattern on the center line
                return makeDashes(stripe.getCenterLine(), width, 3.0, new double[] {3, 9});

            case BLOCK:// : - Draw a 1-3 dash pattern on the center line
                return makeDashes(stripe.getCenterLine(), width, 1.0, new double[] {1, 3});

            case DOUBLE:// ||- Draw two solid lines
            {
                PolyLine2d centerLine = stripe.getCenterLine();
                List<Point2d> result = new ArrayList<>(centerLine.size() * 4 + 1);
                centerLine.offsetLine(width / 2).getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(width / 6).reverse().getPoints().forEachRemaining(result::add);
                result.add(PaintPolygons.NEWPATH);
                centerLine.offsetLine(-width / 2).getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(-width / 6).reverse().getPoints().forEachRemaining(result::add);
                return result;
            }

            case LEFT: // |¦ - Draw left solid, right 3-9 dashed
            {
                PolyLine2d centerLine = stripe.getCenterLine();
                List<Point2d> result = makeDashes(centerLine.offsetLine(-width / 3), width / 3, 0.0, new double[] {3, 9});
                result.add(PaintPolygons.NEWPATH);
                centerLine.offsetLine(width / 2).getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(width / 6).reverse().getPoints().forEachRemaining(result::add);
                return result;
            }

            case RIGHT: // ¦| - Draw left 3-9 dashed, right solid
            {
                PolyLine2d centerLine = stripe.getCenterLine();
                ArrayList<Point2d> result = makeDashes(centerLine.offsetLine(width / 3), width / 3, 0.0, new double[] {3, 9});
                result.add(PaintPolygons.NEWPATH);
                centerLine.offsetLine(-width / 2).getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(-width / 6).reverse().getPoints().forEachRemaining(result::add);
                return result;
            }

            case SOLID: // | - Draw single solid line
                List<Point2d> result = new ArrayList<>(stripe.getContour().size());
                stripe.getContour().iterator().forEachRemaining(result::add);
                return result;

            default:
                throw new NamingException("Unsupported stripe type: " + stripe.getType());
        }

    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.paths != null)
        {
            graphics.setStroke(new BasicStroke(2.0f));
            PaintPolygons.paintPaths(graphics, Color.WHITE, this.paths, true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "StripeAnimation [source = " + getSource().toString() + ", paths=" + this.paths + "]";
    }

    /**
     * StripeData provides the information required to draw a stripe.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public interface StripeData extends Locatable
    {
        /**
         * Returns the center line.
         * @return PolyLine2d; center line.
         */
        PolyLine2d getCenterLine();

        /** {@inheritDoc} */
        @Override
        Point2d getLocation();

        /**
         * Returns the stripe type.
         * @return Type; stripe type.
         */
        Type getType();

        /**
         * Returns the line width.
         * @return Length; line width.
         */
        Length getWidth();

        /**
         * Returns the contour.
         * @return PolyLine2d; contour.
         */
        List<Point2d> getContour();

        /** {@inheritDoc} */
        @Override
        default double getZ()
        {
            return DrawLevel.MARKING.getZ();
        }

        /**
         * Stripe type (same fields as org.opentrafficsim.road.network.lane.Stripe.Type).
         * <p>
         * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
         * </p>
         * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
         */
        public enum Type
        {
            /** Single solid line. */
            SOLID,

            /** Line |¦ allow to go to left, but not to right. */
            LEFT,

            /** Line ¦| allow to go to right, but not to left. */
            RIGHT,

            /** Dashes ¦ allow to cross in both directions. */
            DASHED,

            /** Double solid line ||, don't cross. */
            DOUBLE,

            /** Block : allow to cross in both directions. */
            BLOCK;
        }
    }
}
