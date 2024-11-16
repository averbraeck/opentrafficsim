package org.opentrafficsim.draw.road;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.draw.ClickableLineLocatable;
import org.opentrafficsim.draw.DrawLevel;
import org.opentrafficsim.draw.OtsRenderable;
import org.opentrafficsim.draw.PaintPolygons;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw road stripes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeAnimation extends OtsRenderable<StripeData>
{
    /** */
    private static final long serialVersionUID = 20141017L;

    /** Drawable paths. */
    private final Set<Path2D.Float> paths;

    /**
     * @param source stripe data
     * @param contextualized context provider
     */
    public StripeAnimation(final StripeData source, final Contextualized contextualized)
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
     * Generate the points needed to draw the stripe pattern.
     * @param stripe the stripe
     * @return array of Coordinate
     */
    private List<Point2d> makePoints(final StripeData stripe)
    {
        // TODO implement changing width along the length, when offset line with function for offset is supported
        double width = stripe.getWidth(Length.ZERO).si;
        double offset = .5 * width;
        double w = width / ((stripe.getDashes().size() - 1) * 2 + 1); // width per line
        PolyLine2d centerLine = stripe.getCenterLine();
        List<Point2d> result = new ArrayList<>();
        for (int i = 0; i < stripe.getDashes().size(); i++)
        {
            if (i > 0)
            {
                result.add(PaintPolygons.NEWPATH);
            }
            if (stripe.getDashes() == null || stripe.getDashes().get(i) == null)
            {
                // continuous
                
                centerLine.offsetLine(offset).getPoints().forEachRemaining(result::add);
                centerLine.offsetLine(offset - w).reverse().getPoints().forEachRemaining(result::add);
            }
            else
            {
                double[] dashes = stripe.getDashes().get(i).getValuesSI();
                result.addAll(makeDashes(centerLine.offsetLine(offset + .5 * w), w, stripe.getDashOffset().si, dashes));
            }
            offset -= (2 * w);
        }
        return result;
    }

    /**
     * Generate the drawing commands for a dash pattern.
     * @param center the design line of the striped pattern
     * @param width width of the stripes in meters
     * @param startOffset shift the starting point in the pattern by this length in meters
     * @param onOffLengths one or more lengths of the dashes and the gaps between those dashes. The first value in
     *            <cite>onOffLengths</cite> is the length of a gap. If the number of values in <cite>onOffLengths</cite> is odd,
     *            the pattern repeats inverted (gaps become dashes, dashes become gaps).
     * @return the coordinates of the dashes separated and terminated by a <cite>NEWPATH</cite> Coordinate
     */
    private List<Point2d> makeDashes(final PolyLine2d center, final double width, final double startOffset,
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
        double position = -startOffset + onOffLengths[0];
        int phase = 1;
        ArrayList<Point2d> result = new ArrayList<>();
        boolean first = true;
        while (position < length)
        {
            double nextBoundary = position + onOffLengths[phase++ % onOffLengths.length];
            if (nextBoundary > 0) // Skip this one; this entire dash lies within the startOffset
            {
                if (!first)
                {
                    result.add(PaintPolygons.NEWPATH);
                }
                first = false;
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
            }
            position = nextBoundary + onOffLengths[phase++ % onOffLengths.length];
        }
        return result;
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.paths != null)
        {
            setRendering(graphics);
            graphics.setStroke(new BasicStroke(2.0f));
            PaintPolygons.paintPaths(graphics, Color.WHITE, this.paths, true);
            resetRendering(graphics);
        }
    }

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
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public interface StripeData extends ClickableLineLocatable
    {
        @Override
        OrientedPoint2d getLocation();

        /**
         * Returns the center line in world coordinates.
         * @return the center line in world coordinates
         */
        PolyLine2d getCenterLine();

        /**
         * Return dashes for each line in the stripe. Use {@code null} for a continuous line.
         * @return dashes
         */
        List<LengthVector> getDashes();

        /**
         * Return dash offset.
         * @return dash offset
         */
        Length getDashOffset();

        /**
         * Returns the line width.
         * @param position where to obtain width
         * @return line width
         */
        Length getWidth(Length position);

        @Override
        default double getZ()
        {
            return DrawLevel.MARKING.getZ();
        }
    }
}
