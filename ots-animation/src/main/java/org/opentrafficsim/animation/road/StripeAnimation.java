package org.opentrafficsim.animation.road;

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
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.LineLocatable;
import org.opentrafficsim.animation.OtsRenderable;
import org.opentrafficsim.animation.PaintPolygons;
import org.opentrafficsim.animation.road.StripeAnimation.StripeData;
import org.opentrafficsim.base.StripeElement;
import org.opentrafficsim.base.geometry.DirectionalPolyLine;
import org.opentrafficsim.base.geometry.FractionalProjectionHelper.FractionalFallback;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draw road stripes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class StripeAnimation extends OtsRenderable<StripeData>
{
    /** Drawable paths. */
    private List<PaintData> paintDatas;

    /** Offset that applied when paths were determined. */
    private Length pathOffset;

    /** Whether a partial dash at the start was skipped when paths were determined. */
    private boolean pathSkipPartialStart;

    /** Whether a partial dash at the end was skipped when paths were determined. */
    private boolean pathSkipPartialEnd;

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
                        stripe.getReferenceLine(), w, stripe.getDashOffset().si, dashes, stripe.isPartialDashAtStartSkipped(),
                        stripe.isPartialDashAtEndSkipped()));
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
     * Generate the drawing commands for a dash pattern. Dash center points are determined on the reference line (with only gaps
     * stretched to match the synchronized start and end phase), projected to the center line for lateral alignment, and each
     * dash is drawn with its original intended length around that center. Virtual dashes before and beyond the link are
     * considered so that partially visible dashes at link boundaries are included.
     * @param centerLine the design line of the striped pattern
     * @param referenceLine reference line to which dashes are applied
     * @param width width of the stripes in meters
     * @param startOffset shift the starting point in the pattern by this length in meters
     * @param dashes one or more lengths of the dashes and the gaps between those dashes. The first value in <cite>dashes</cite>
     *            is the length of a gap. If the number of values in <cite>dashes</cite> is odd, the pattern repeats inverted
     *            (gaps become dashes, dashes become gaps).
     * @param skipPartialStart whether to skip a partial dash at the start of the stripe
     * @param skipPartialEnd whether to skip a partial dash at the end of the stripe
     * @return the coordinates of the dashes separated and terminated by a <cite>NEWPATH</cite> Coordinate
     */
    private List<Point2d> makeDashes(final DirectionalPolyLine centerLine, final PolyLine2d referenceLine, final double width,
            final double startOffset, final double[] dashes, final boolean skipPartialStart, final boolean skipPartialEnd)
    {
        double period = 0;
        double maxDash = 0.0;
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
        // First value is a gap; dash elements are at odd indices in the base pattern.
        for (int i = 1; i < dashes.length; i += 2)
        {
            maxDash = Math.max(maxDash, dashes[i]);
        }

        double referenceLength = referenceLine.getLength();
        double centerLength = centerLine.getLength();
        if (referenceLength <= 0.0 || centerLength <= 0.0)
        {
            return new ArrayList<>();
        }

        // Collect all nominal dash instances in an extended range (virtual dashes before/beyond the link) so that any dash
        // whose fixed-length extent can reach into [0, referenceLength] is considered.
        double margin = 0.5 * maxDash + period;
        List<Segment> nominal = new ArrayList<>();
        double position = -startOffset + dashes[0];
        int phase = 1;
        while (position < referenceLength + margin)
        {
            double dashStart = position;
            double dashEnd = position + dashes[phase++ % dashes.length];
            if (dashEnd > -margin)
            {
                nominal.add(new Segment(dashStart, dashEnd));
            }
            position = dashEnd + dashes[phase++ % dashes.length];
        }

        ArrayList<Point2d> result = new ArrayList<>();
        if (nominal.isEmpty())
        {
            return result;
        }

        // Visible nominal dashes (overlapping [0, referenceLength]) determine gap stretching over the fitted range.
        List<Segment> visibleNominal = new ArrayList<>();
        for (Segment segment : nominal)
        {
            double start = Math.max(0.0, segment.start);
            double end = Math.min(referenceLength, segment.end);
            if (end > start)
            {
                visibleNominal.add(segment);
            }
        }

        double gapScale = 1.0;
        if (!visibleNominal.isEmpty())
        {
            double refDashSum = 0.0;
            for (Segment segment : visibleNominal)
            {
                refDashSum += Math.min(referenceLength, segment.end) - Math.max(0.0, segment.start);
            }
            double firstVisStart = Math.max(0.0, visibleNominal.get(0).start);
            double lastVisEnd = Math.min(referenceLength, visibleNominal.get(visibleNominal.size() - 1).end);
            double origGapSum = firstVisStart;
            for (int i = 1; i < visibleNominal.size(); i++)
            {
                double start = Math.min(referenceLength, Math.max(0.0, visibleNominal.get(i - 1).end));
                double end = Math.max(0.0, Math.min(referenceLength, visibleNominal.get(i).start));
                origGapSum += Math.max(0.0, end - start);
            }
            origGapSum += Math.max(0.0, referenceLength - lastVisEnd);
            double targetGapSum = Math.max(0.0, referenceLength - refDashSum);
            gapScale = origGapSum > 1e-9 ? targetGapSum / origGapSum : 1.0;
        }

        boolean sameLine = centerLine.getPointList().equals(referenceLine.getPointList());
        boolean first = true;
        for (Segment segment : nominal)
        {
            if ((skipPartialStart && segment.start < 0.0 && segment.end > 0.0)
                    || (skipPartialEnd && segment.start < referenceLength && segment.end > referenceLength))
            {
                // partial dash at a node with 3 or more links
                continue;
            }
            double nominalCenter = 0.5 * (segment.start + segment.end);
            double dashLen = segment.end - segment.start;
            double stretchedCenter = stretchedPosition(nominalCenter, visibleNominal, gapScale, referenceLength);

            // Dash centre on reference line, projected for lateral alignment; draw original dash length around it.
            double centerFraction;
            if (sameLine)
            {
                centerFraction = stretchedCenter / referenceLength;
            }
            else
            {
                // Project dash centre from (stretched) reference line to center line for lateral alignment.
                double refFraction = stretchedCenter / referenceLength;
                double clampedRef = Math.max(0.0, Math.min(1.0, refFraction));
                DirectedPoint2d p = referenceLine.getLocationFraction(clampedRef);
                double projected = centerLine.projectFractional(p.x, p.y, FractionalFallback.ENDPOINT);
                if (refFraction < 0.0)
                {
                    centerFraction = projected + (stretchedCenter / referenceLength);
                }
                else if (refFraction > 1.0)
                {
                    centerFraction = projected + (stretchedCenter - referenceLength) / referenceLength;
                }
                else
                {
                    centerFraction = projected;
                }
            }

            double centerPos = centerFraction * centerLength;
            double half = 0.5 * dashLen;
            double start = centerPos - half;
            double end = centerPos + half;
            if (end <= 0.0 || start >= centerLength)
            {
                continue;
            }
            start = Math.max(0.0, start);
            end = Math.min(centerLength, end);
            if (end <= start)
            {
                continue;
            }

            if (!first)
            {
                result.add(PaintPolygons.NEWPATH);
            }
            first = false;

            DirectionalPolyLine dashCenter = centerLine.extractFractional(start / centerLength, end / centerLength);
            dashCenter.directionalOffsetLine(width / 2).iterator().forEachRemaining(result::add);
            dashCenter.directionalOffsetLine(-width / 2).reverse().iterator().forEachRemaining(result::add);
        }
        return result;
    }

    /**
     * Returns the stretched position of a nominal reference-line coordinate, stretching only gaps to fit [0, referenceLength]
     * while preserving dash lengths. Uses nominal dashes that overlap [0, referenceLength] for in-link mapping and preserves
     * fixed dash length for virtual boundary dashes outside the range.
     * @param x nominal coordinate
     * @param visible visible nominal dash segments in order
     * @param gapScale scale factor for gaps
     * @param referenceLength reference length
     * @return stretched coordinate
     */
    private static double stretchedPosition(final double x, final List<Segment> visible, final double gapScale,
            final double referenceLength)
    {
        if (referenceLength <= 0.0)
        {
            return x;
        }
        if (visible.isEmpty())
        {
            return x * gapScale;
        }

        Segment first = visible.get(0);
        Segment last = visible.get(visible.size() - 1);

        // Virtual dash crossing the start boundary: preserve fixed dash length relative to nominal 0 -> stretched 0.
        if (x < 0.0)
        {
            if (first.start < 0.0 && x >= first.start && x <= first.end)
            {
                return x;
            }
            return x * gapScale;
        }
        // Virtual dash crossing the end boundary: preserve fixed dash length relative to nominal L -> stretched L.
        if (x > referenceLength)
        {
            if (last.end > referenceLength && x >= last.start && x <= last.end)
            {
                return x;
            }
            return referenceLength + (x - referenceLength) * gapScale;
        }

        double nominalCursor = 0.0;
        double stretchedCursor = 0.0;
        for (int i = 0; i < visible.size(); i++)
        {
            Segment s = visible.get(i);
            double dashStartIn = Math.max(0.0, s.start);
            double dashEndIn = Math.min(referenceLength, s.end);

            // Gap before this dash.
            if (x <= dashStartIn)
            {
                return stretchedCursor + (x - nominalCursor) * gapScale;
            }
            stretchedCursor += (dashStartIn - nominalCursor) * gapScale;
            nominalCursor = dashStartIn;

            // Dash: fixed length preserved.
            if (x <= dashEndIn)
            {
                return stretchedCursor + (x - dashStartIn);
            }
            stretchedCursor += (dashEndIn - dashStartIn);
            nominalCursor = dashEndIn;
        }

        // Trailing gap.
        return stretchedCursor + (x - nominalCursor) * gapScale;
    }

    /**
     * Nominal dash segment along the reference line.
     * @param start nominal start
     * @param end nominal end
     */
    private record Segment(double start, double end)
    {
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
     * Updates paths to draw when a new offset applies, or when partial boundary dashes should be skipped differently.
     */
    private void update()
    {
        boolean skipPartialStart = getSource().isPartialDashAtStartSkipped();
        boolean skipPartialEnd = getSource().isPartialDashAtEndSkipped();
        if (!getSource().getDashOffset().equals(this.pathOffset) || skipPartialStart != this.pathSkipPartialStart
                || skipPartialEnd != this.pathSkipPartialEnd)
        {
            this.paintDatas = makePaths(getSource());
            this.pathOffset = getSource().getDashOffset();
            this.pathSkipPartialStart = skipPartialStart;
            this.pathSkipPartialEnd = skipPartialEnd;
        }
    }

    @Override
    public final String toString()
    {
        return "StripeAnimation [source = " + getSource().toString() + ", paintDatas=" + this.paintDatas + "]";
    }

    /**
     * StripeData provides the information required to draw a stripe.
     */
    public interface StripeData extends LineLocatable
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

        /**
         * Returns whether a partial dash at the start of the stripe should be skipped. This is typically {@code true} when the
         * node at the start connects to 3 or more links, where dashes from the various links can clutter the node. A partial
         * dash at a node with 2 links is drawn to continue a clean pattern.
         * @return whether a partial dash at the start of the stripe should be skipped
         */
        boolean isPartialDashAtStartSkipped();

        /**
         * Returns whether a partial dash at the end of the stripe should be skipped. This is typically {@code true} when the
         * node at the end connects to 3 or more links, where dashes from the various links can clutter the node. A partial dash
         * at a node with 2 links is drawn to continue a clean pattern.
         * @return whether a partial dash at the end of the stripe should be skipped
         */
        boolean isPartialDashAtEndSkipped();

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
