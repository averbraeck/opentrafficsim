package org.opentrafficsim.road.network.lane;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.FractionalLengthData;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.network.lane.Stripe.StripeType;

/**
 * This class is an extension (conceptually, not an actual java extension) of {@code OtsGeometryUtil}. This utility has access
 * to classes that are specific to the ots-road project, and required to define geometry of objects in this context.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LaneGeometryUtil
{

    /**
     * Utility class.
     */
    private LaneGeometryUtil()
    {
        //
    }

    /**
     * Create cross-section slices from constant values.
     * @param designLine design line.
     * @param offset offset.
     * @param width width.
     * @return list of cross-section slices.
     */
    public static List<CrossSectionSlice> getSlices(final ContinuousLine designLine, final Length offset, final Length width)
    {
        return List.of(new CrossSectionSlice(Length.ZERO, offset, width),
                new CrossSectionSlice(Length.instantiateSI(designLine.getLength()), offset, width));
    }

    /**
     * Create cross-section slices from start and end values.
     * @param designLine design line.
     * @param startOffset start offset.
     * @param endOffset end offset.
     * @param startWidth start width.
     * @param endWidth end width.
     * @return list of cross-section slices.
     */
    public static List<CrossSectionSlice> getSlices(final ContinuousLine designLine, final Length startOffset,
            final Length endOffset, final Length startWidth, final Length endWidth)
    {
        return List.of(new CrossSectionSlice(Length.ZERO, startOffset, startWidth),
                new CrossSectionSlice(Length.instantiateSI(designLine.getLength()), endOffset, endWidth));
    }

    /**
     * Returns the offsets to use on a {@code ContinuousLine} for the left-hand edge.
     * @param designLine design line.
     * @param crossSectionSlices cross-section slices.
     * @return offsets at fractional lengths to use on a {@code ContinuousLine}.
     */
    public static FractionalLengthData getCenterOffsets(final ContinuousLine designLine,
            final List<CrossSectionSlice> crossSectionSlices)
    {
        return getOffsets(crossSectionSlices, designLine.getLength(), 0.0);
    }

    /**
     * Returns the offsets to use on a {@code ContinuousLine} for the left-hand edge.
     * @param designLine design line.
     * @param crossSectionSlices cross-section slices.
     * @return offsets at fractional lengths to use on a {@code ContinuousLine}.
     */
    public static FractionalLengthData getLeftEdgeOffsets(final ContinuousLine designLine,
            final List<CrossSectionSlice> crossSectionSlices)
    {
        return getOffsets(crossSectionSlices, designLine.getLength(), 0.5);
    }

    /**
     * Returns the offsets to use on a {@code ContinuousLine} for the right-hand edge.
     * @param designLine design line.
     * @param crossSectionSlices cross-section slices.
     * @return offsets at fractional lengths to use on a {@code ContinuousLine}.
     */
    public static FractionalLengthData getRightEdgeOffsets(final ContinuousLine designLine,
            final List<CrossSectionSlice> crossSectionSlices)
    {
        return getOffsets(crossSectionSlices, designLine.getLength(), -0.5);
    }

    /**
     * Returns the offsets to use on a {@code ContinuousLine}.
     * @param crossSectionSlices cross-section slices.
     * @param length length of the design line.
     * @param widthFactor factor to use, typically -0.5 for right-hand, 0.0 for center, and 0.5 for left-hand.
     * @return offsets at fractional lengths to use on a {@code ContinuousLine}.
     */
    private static FractionalLengthData getOffsets(final List<CrossSectionSlice> crossSectionSlices, final double length,
            final double widthFactor)
    {
        NavigableMap<Double, Double> map = new TreeMap<>();
        crossSectionSlices.forEach((slice) -> map.put(slice.getRelativeLength().si / length,
                slice.getOffset().si + widthFactor * slice.getWidth().si));
        return new FractionalLengthData(map);
    }

    /**
     * Returns the contour based on left and right edge.
     * @param leftEdge left edge, in design line direction.
     * @param rightEdge right edge, in design line direction.
     * @return a closed loop of both edges.
     */
    public static Polygon2d getContour(final PolyLine2d leftEdge, final PolyLine2d rightEdge)
    {
        Point2d[] points = new Point2d[leftEdge.size() + rightEdge.size() + 1];
        System.arraycopy(leftEdge.getPointList().toArray(), 0, points, 0, leftEdge.size());
        System.arraycopy(rightEdge.reverse().getPointList().toArray(), 0, points, leftEdge.size(), rightEdge.size());
        points[points.length - 1] = points[0]; // close loop
        return new Polygon2d(true, points);
    }

    /**
     * Creates a simple straight lane. This method exists to create lanes for simple tests.
     * @param link link.
     * @param id id.
     * @param offset end offset.
     * @param width end width.
     * @param laneType lane type.
     * @param speedLimits speed limit map.
     * @return lane.
     */
    public static Lane createStraightLane(final CrossSectionLink link, final String id, final Length offset, final Length width,
            final LaneType laneType, final Map<GtuType, Speed> speedLimits)
    {
        return createStraightLane(link, id, offset, offset, width, width, laneType, speedLimits);
    }

    /**
     * Creates a simple straight lane. This method exists to create lanes for simple tests.
     * @param link link.
     * @param id id.
     * @param startOffset start offset.
     * @param endOffset end offset.
     * @param startWidth start width.
     * @param endWidth end width.
     * @param laneType lane type.
     * @param speedLimits speed limit map.
     * @return lane.
     */
    public static Lane createStraightLane(final CrossSectionLink link, final String id, final Length startOffset,
            final Length endOffset, final Length startWidth, final Length endWidth, final LaneType laneType,
            final Map<GtuType, Speed> speedLimits)
    {
        ContinuousLine designLine = new ContinuousStraight(
                Try.assign(() -> link.getDesignLine().getLocationPointFraction(0.0), "Link should have a valid design line."),
                link.getLength().si);
        List<CrossSectionSlice> slices = getSlices(designLine, startOffset, endOffset, startWidth, endWidth);
        return createStraightLane(link, id, slices, laneType, speedLimits);
    }

    /**
     * Creates a simple straight lane. This method exists to create lanes for simple tests.
     * @param link link.
     * @param id id.
     * @param slices slices.
     * @param laneType lane type.
     * @param speedLimits speed limit map.
     * @return lane.
     */
    public static Lane createStraightLane(final CrossSectionLink link, final String id, final List<CrossSectionSlice> slices,
            final LaneType laneType, final Map<GtuType, Speed> speedLimits)
    {
        ContinuousLine designLine = new ContinuousStraight(
                Try.assign(() -> link.getDesignLine().getLocationPointFraction(0.0), "Link should have a valid design line."),
                link.getLength().si);
        PolyLine2d centerLine = designLine.flattenOffset(getCenterOffsets(designLine, slices), null);
        PolyLine2d leftEdge = designLine.flattenOffset(getLeftEdgeOffsets(designLine, slices), null);
        PolyLine2d rightEdge = designLine.flattenOffset(getRightEdgeOffsets(designLine, slices), null);
        Polygon2d contour = getContour(leftEdge, rightEdge);
        return Try.assign(() -> new Lane(link, id, new OtsLine2d(centerLine), contour, slices, laneType, speedLimits),
                "Network exception.");
    }

    /**
     * Creates a simple straight lane. This method exists to create lanes for simple tests.
     * @param type stripe type.
     * @param link link.
     * @param offset end offset.
     * @param width end width.
     * @return lane.
     */
    public static Stripe createStraightStripe(final StripeType type, final CrossSectionLink link, final Length offset,
            final Length width)
    {
        ContinuousLine designLine = new ContinuousStraight(
                Try.assign(() -> link.getDesignLine().getLocationPointFraction(0.0), "Link should have a valid design line."),
                link.getLength().si);
        List<CrossSectionSlice> slices = getSlices(designLine, offset, width);
        PolyLine2d centerLine = designLine.flattenOffset(getCenterOffsets(designLine, slices), null);
        PolyLine2d leftEdge = designLine.flattenOffset(getLeftEdgeOffsets(designLine, slices), null);
        PolyLine2d rightEdge = designLine.flattenOffset(getRightEdgeOffsets(designLine, slices), null);
        Polygon2d contour = getContour(leftEdge, rightEdge);
        return Try.assign(() -> new Stripe(type, link, new OtsLine2d(centerLine), contour, slices), "Network exception.");
    }

    /**
     * Creates a simple straight shoulder. This method exists to create shoulders for simple tests.
     * @param link link.
     * @param id id.
     * @param startOffset start offset.
     * @param endOffset end offset.
     * @param startWidth start width.
     * @param endWidth end width.
     * @param laneType lane type.
     * @return lane.
     */
    public static Object createStraightShoulder(final CrossSectionLink link, final String id, final Length startOffset,
            final Length endOffset, final Length startWidth, final Length endWidth, final LaneType laneType)
    {
        ContinuousLine designLine = new ContinuousStraight(
                Try.assign(() -> link.getDesignLine().getLocationPointFraction(0.0), "Link should have a valid design line."),
                link.getLength().si);
        List<CrossSectionSlice> slices = getSlices(designLine, startOffset, endOffset, startWidth, endWidth);
        PolyLine2d centerLine = designLine.flattenOffset(getCenterOffsets(designLine, slices), null);
        PolyLine2d leftEdge = designLine.flattenOffset(getLeftEdgeOffsets(designLine, slices), null);
        PolyLine2d rightEdge = designLine.flattenOffset(getRightEdgeOffsets(designLine, slices), null);
        Polygon2d contour = getContour(leftEdge, rightEdge);
        return Try.assign(() -> new Shoulder(link, id, new OtsLine2d(centerLine), contour, slices, laneType),
                "Network exception.");
    }
}
