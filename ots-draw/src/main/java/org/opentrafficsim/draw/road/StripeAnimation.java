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
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.base.geometry.OtsLine2d.FractionalFallback;
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
    private List<PaintData> paintDatas;

    /** Offset that applied when paths were determined. */
    private Length pathOffset;

    /**
     * Constructor.
     * @param source stripe data
     * @param contextualized context provider
     */
    public StripeAnimation(final StripeData source, final Contextualized contextualized)
    {
        super(source, contextualized);
    }

    /**
     * Generate the points needed to draw the stripe pattern.
     * @param stripe the stripe
     * @return list of paint data
     */
    private List<PaintData> makePaths(final StripeData stripe)
    {
        // TODO implement changing width along the length, when offset line with function for offset is supported
        List<PaintData> paintData = new ArrayList<>();
        double width = stripe.getWidth(Length.ZERO).si;
        double edgeOffset = .5 * width;
        for (StripeElement element : stripe.getElements())
        {
            double w = element.width().si;
            List<Point2d> path = new ArrayList<>();
            if (element.isContinuous())
            {
                stripe.getCenterLine().directionalOffsetLine(edgeOffset).iterator().forEachRemaining(path::add);
                stripe.getCenterLine().directionalOffsetLine(edgeOffset - w).reverse().iterator().forEachRemaining(path::add);
            }
            else if (!element.isGap())
            {
                double[] dashes = element.dashes().getValuesSI();
                path.addAll(makeDashes(stripe.getCenterLine().directionalOffsetLine(edgeOffset - .5 * w),
                        stripe.getReferenceLine(), w, stripe.getDashOffset().si, dashes));
            }
            edgeOffset -= w;
            // can be empty for a gap element, or when no dash is within the length
            if (!path.isEmpty())
            {
                paintData.add(new PaintData(PaintPolygons.getPaths(getSource().getLocation(), path), element.color()));
            }
        }
        return paintData;
    }

    /**
     * Generate the drawing commands for a dash pattern.
     * @param centerLine the design line of the striped pattern
     * @param referenceLine reference line to which dashes are applied
     * @param width width of the stripes in meters
     * @param startOffset shift the starting point in the pattern by this length in meters
     * @param dashes one or more lengths of the dashes and the gaps between those dashes. The first value in <cite>dashes</cite>
     *            is the length of a gap. If the number of values in <cite>dashes</cite> is odd, the pattern repeats inverted
     *            (gaps become dashes, dashes become gaps).
     * @return the coordinates of the dashes separated and terminated by a <cite>NEWPATH</cite> Coordinate
     */
    private List<Point2d> makeDashes(final DirectionalPolyLine centerLine, final PolyLine2d referenceLine, final double width,
            final double startOffset, final double[] dashes)
    {
        double period = 0;
        for (double length : dashes)
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
        // TODO link length when that is chosen
        double referenceLength = referenceLine.getLength();
        double position = -startOffset + dashes[0];
        int phase = 1;
        ArrayList<Point2d> result = new ArrayList<>();
        boolean first = true;
        boolean sameLine = centerLine.getPointList().equals(referenceLine.getPointList());
        while (position < referenceLength)
        {
            double nextBoundary = position + dashes[phase++ % dashes.length];
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
                if (endPosition > referenceLength)
                {
                    endPosition = referenceLength; // Draw a partial dash, ending at length (end of the center line)
                }

                double fraction1 = position / referenceLength;
                double fraction2 = endPosition / referenceLength;
                if (!sameLine)
                {
                    // project dash from reference line on the own center line, using fractional projection (i.e. pizza slices)
                    Ray2d p1 = referenceLine.getLocationFraction(fraction1);
                    Ray2d p2 = referenceLine.getLocationFraction(fraction2);
                    fraction1 = centerLine.projectFractional(p1.x, p1.y, FractionalFallback.ENDPOINT);
                    fraction2 = centerLine.projectFractional(p2.x, p2.y, FractionalFallback.ENDPOINT);
                }
                DirectionalPolyLine dashCenter = centerLine.extractFractional(fraction1, fraction2);

                // create offsets on dash center line to add dash contour line
                dashCenter.directionalOffsetLine(width / 2).iterator().forEachRemaining(result::add);
                dashCenter.directionalOffsetLine(-width / 2).reverse().iterator().forEachRemaining(result::add);
            }
            position = nextBoundary + dashes[phase++ % dashes.length];
        }
        return result;
    }

    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        update();
        if (this.paintDatas != null)
        {
            for (PaintData paintData : this.paintDatas)
            {
                setRendering(graphics);
                graphics.setStroke(new BasicStroke(2.0f));
                PaintPolygons.paintPaths(graphics, paintData.color(), paintData.path(), true);
                resetRendering(graphics);
            }
        }
    }

    /**
     * Updates paths to draw when new offset applies.
     */
    private void update()
    {
        if (!getSource().getDashOffset().equals(this.pathOffset))
        {
            this.paintDatas = makePaths(getSource());
            this.pathOffset = getSource().getDashOffset();
        }
    }

    @Override
    public final String toString()
    {
        return "StripeAnimation [source = " + getSource().toString() + ", paintDatas=" + this.paintDatas + "]";
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
        DirectedPoint2d getLocation();

        /**
         * Returns the center line in world coordinates, with directions of end-points.
         * @return the center line in world coordinates, with directions of end-points
         */
        DirectionalPolyLine getCenterLine();

        /**
         * Returns the line along which dashes are applied. At these fractions, parts of the centerline are taken.
         * @return line along which dashes are applied
         */
        PolyLine2d getReferenceLine();

        /**
         * Returns the stripe elements.
         * @return stripe elements
         */
        List<StripeElement> getElements();

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

    /**
     * Paint data.
     * @param path path
     * @param color color
     */
    private record PaintData(Set<Path2D.Float> path, Color color)
    {
    }
}
