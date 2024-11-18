package org.opentrafficsim.road.network.lane;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousLine.ContinuousDoubleFunction;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.FractionalLengthData;
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
public final class LaneGeometryUtil
{

    /**
     * Utility class.
     */
    private LaneGeometryUtil()
    {
        //
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
        ContinuousDoubleFunction offset = FractionalLengthData.of(0.0, startOffset.si, 1.0, endOffset.si);
        ContinuousDoubleFunction width = FractionalLengthData.of(0.0, startWidth.si, 1.0, endWidth.si);
        return createStraightLane(link, id, offset, width, laneType, speedLimits);
    }

    /**
     * Creates a simple straight lane. This method exists to create lanes for simple tests.
     * @param link link
     * @param id id
     * @param offset offset information
     * @param width offset information
     * @param laneType lane type
     * @param speedLimits speed limit map
     * @return lane
     */
    public static Lane createStraightLane(final CrossSectionLink link, final String id, final ContinuousDoubleFunction offset,
            final ContinuousDoubleFunction width, final LaneType laneType, final Map<GtuType, Speed> speedLimits)
    {
        ContinuousLine designLine = new ContinuousStraight(
                Try.assign(() -> link.getDesignLine().getLocationPointFraction(0.0), "Link should have a valid design line."),
                link.getLength().si);
        return Try.assign(
                () -> new Lane(link, id, CrossSectionGeometry.of(designLine, null, offset, width), laneType, speedLimits),
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
        ContinuousDoubleFunction offsetFunc = FractionalLengthData.of(0.0, offset.si, 1.0, offset.si);
        ContinuousDoubleFunction widthFunc = FractionalLengthData.of(0.0, width.si, 1.0, width.si);
        return Try.assign(() -> new Stripe(type, link, CrossSectionGeometry.of(designLine, null, offsetFunc, widthFunc)),
                "Network exception.");
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
        ContinuousDoubleFunction offset = FractionalLengthData.of(0.0, startOffset.si, 1.0, endOffset.si);
        ContinuousDoubleFunction width = FractionalLengthData.of(0.0, startWidth.si, 1.0, endWidth.si);
        return Try.assign(() -> new Shoulder(link, id, CrossSectionGeometry.of(designLine, null, offset, width), laneType),
                "Network exception.");
    }
}
