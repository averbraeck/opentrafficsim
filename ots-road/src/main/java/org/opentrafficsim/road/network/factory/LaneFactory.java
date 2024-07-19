package org.opentrafficsim.road.network.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousPolyLine;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsGeometryUtil;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionSlice;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class LaneFactory
{

    /** Angle above which a Bezier curve is used over a straight line. */
    private static final double BEZIER_MARGIN = Math.toRadians(0.5);

    /** Number of segments to use. */
    private static final NumSegments SEGMENTS = new NumSegments(64);

    /** Link. */
    private final CrossSectionLink link;

    /** Design line. */
    private final ContinuousLine line;

    /** Offset for next cross section elements. Left side of lane when building left to right, and vice versa. */
    private Length offset;

    /** Lane width to use (negative when building left to right). */
    private Length laneWidth0;

    /** Start offset. */
    private Length offsetStart = Length.ZERO;

    /** End offset. */
    private Length offsetEnd = Length.ZERO;

    /** Lane type to use. */
    private LaneType laneType0;

    /** Speed limit to use. */
    private Speed speedLimit0;

    /** Parent GTU type of relevant GTUs. */
    private GtuType gtuType;

    /** Created lanes. */
    private final List<Lane> lanes = new ArrayList<>();

    /** Stored stripes, so we can return it to the user on the addLanes() call. */
    private Stripe firstStripe;

    /**
     * @param network RoadNetwork; network
     * @param from Node; from node
     * @param to Node; to node
     * @param type LinkType; link type
     * @param simulator OtsSimulatorInterface; simulator
     * @param policy LaneKeepingPolicy; lane keeping policy
     * @param gtuType GtuType; parent GTU type of relevant GTUs.
     * @throws OtsGeometryException if no valid line can be created
     * @throws NetworkException if the link exists, or a node does not exist, in the network
     */
    public LaneFactory(final RoadNetwork network, final Node from, final Node to, final LinkType type,
            final OtsSimulatorInterface simulator, final LaneKeepingPolicy policy, final GtuType gtuType)
            throws OtsGeometryException, NetworkException
    {
        this(network, from, to, type, simulator, policy, gtuType, makeLine(from, to));
    }

    /**
     * @param network RoadNetwork; network
     * @param from Node; from node
     * @param to Node; to node
     * @param type LinkType; link type
     * @param simulator OtsSimulatorInterface; simulator
     * @param policy LaneKeepingPolicy; lane keeping policy
     * @param gtuType GtuType; parent GTU type of relevant GTUs.
     * @param line ContinuousLine; line
     * @throws NetworkException if the link exists, or a node does not exist, in the network
     */
    public LaneFactory(final RoadNetwork network, final Node from, final Node to, final LinkType type,
            final OtsSimulatorInterface simulator, final LaneKeepingPolicy policy, final GtuType gtuType,
            final ContinuousLine line) throws NetworkException
    {
        this.link = new CrossSectionLink(network, from.getId() + to.getId(), from, to, type,
                new OtsLine2d(line.flatten(SEGMENTS)), null, policy);
        this.line = line;
        this.gtuType = gtuType;
    }

    /**
     * Creates a line between two nodes. If the nodes and their directions are on a straight line, a straight line is created.
     * Otherwise a default Bezier curve is created.
     * @param from Node; from node
     * @param to Node; to node
     * @return ContinuousLine; design line
     */
    private static ContinuousLine makeLine(final Node from, final Node to)
    {
        // Straight or bezier?
        double rotCrow = Math.atan2(to.getLocation().y - from.getLocation().y, to.getLocation().x - from.getLocation().x);
        double dRot = from.getLocation().getDirZ() - rotCrow;
        while (dRot < -Math.PI)
        {
            dRot += 2.0 * Math.PI;
        }
        while (dRot > Math.PI)
        {
            dRot -= 2.0 * Math.PI;
        }
        ContinuousLine line;
        if (from.getLocation().getDirZ() != to.getLocation().getDirZ() || Math.abs(dRot) > BEZIER_MARGIN)
        {
            Point2d[] points = Bezier.cubicControlPoints(from.getLocation(), to.getLocation(), 1.0, false);
            line = new ContinuousBezierCubic(points[0], points[1], points[2], points[3]);
        }
        else
        {
            line = new ContinuousStraight(from.getLocation(), from.getPoint().distance(to.getPoint()));
        }
        return line;
    }

    /**
     * Prepare the factory to add lanes from left to right.
     * @param leftLanes double; number of lanes left from the link design line
     * @param laneWidth Length; lane width
     * @param laneType LaneType; lane type
     * @param speedLimit Speed; speed limit
     * @return LaneFactory this lane factory for method chaining
     */
    public LaneFactory leftToRight(final double leftLanes, final Length laneWidth, final LaneType laneType,
            final Speed speedLimit)
    {
        this.offset = laneWidth.times(leftLanes);
        this.laneWidth0 = laneWidth.neg();
        this.laneType0 = laneType;
        this.speedLimit0 = speedLimit;
        Length width = getWidth(Type.SOLID);
        List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(this.line, this.offset.plus(this.offsetStart), width);
        PolyLine2d centerLine = this.line.flattenOffset(LaneGeometryUtil.getCenterOffsets(this.line, slices), SEGMENTS);
        PolyLine2d leftEdge = this.line.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(this.line, slices), SEGMENTS);
        PolyLine2d rightEdge = this.line.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(this.line, slices), SEGMENTS);
        Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);
        this.firstStripe = Try.assign(() -> new Stripe(Type.SOLID, this.link, new OtsLine2d(centerLine), contour, slices),
                "Unexpected exception while building link.");
        return this;
    }

    /**
     * Prepare the factory to add lanes from right to left.
     * @param rightLanes double; number of lanes right from the link design line
     * @param laneWidth Length; lane width
     * @param laneType LaneType; lane type
     * @param speedLimit Speed; speed limit
     * @return LaneFactory this lane factory for method chaining
     */
    public LaneFactory rightToLeft(final double rightLanes, final Length laneWidth, final LaneType laneType,
            final Speed speedLimit)
    {
        this.offset = laneWidth.times(-rightLanes);
        this.laneWidth0 = laneWidth;
        this.laneType0 = laneType;
        this.speedLimit0 = speedLimit;
        Length width = getWidth(Type.SOLID);
        List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(this.line, this.offset.plus(this.offsetStart), width);
        PolyLine2d centerLine = this.line.flattenOffset(LaneGeometryUtil.getCenterOffsets(this.line, slices), SEGMENTS);
        PolyLine2d leftEdge = this.line.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(this.line, slices), SEGMENTS);
        PolyLine2d rightEdge = this.line.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(this.line, slices), SEGMENTS);
        Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);
        this.firstStripe = Try.assign(() -> new Stripe(Type.SOLID, this.link, new OtsLine2d(centerLine), contour, slices),
                "Unexpected exception while building link.");
        return this;
    }

    /**
     * Set start offset.
     * @param startOffset Length; offset
     * @return LaneFactory this lane factory for method chaining
     */
    public LaneFactory setOffsetStart(final Length startOffset)
    {
        this.offsetStart = startOffset;
        return this;
    }

    /**
     * Set end offset.
     * @param endOffset Length; offset
     * @return LaneFactory this lane factory for method chaining
     */
    public LaneFactory setOffsetEnd(final Length endOffset)
    {
        this.offsetEnd = endOffset;
        return this;
    }

    /**
     * Adds a lane pair for each stripe type, where the type determines the right-hand side stripe when building from left to
     * right and vice versa. The left-most stripe is created in {@code leftToRight()}, meaning that each type describes
     * permeablility between a lane and it's right-hand neighbor, when building left to right (and vice versa). This method
     * internally adds {@code SOLID} to create the final continuous stripe.
     * @param types Type...; type per lane pair, for N lanes N-1 should be provided
     * @return this LaneFactory this lane factory for method chaining
     */
    public LaneFactory addLanes(final Type... types)
    {
        return addLanes(new ArrayList<>(), types);
    }

    /**
     * Adds a lane pair for each stripe type, where the type determines the right-hand side stripe when building from left to
     * right and vice versa. The left-most stripe is created in {@code leftToRight()}, meaning that each type describes
     * permeablility between a lane and it's right-hand neighbor, when building left to right (and vice versa). This method
     * internally adds {@code SOLID} to create the final continuous stripe. All generated stripes, including the one generated
     * in leftToRight() or rightToLeft(), is returned in the provided list for custom permeability.
     * @param stripeList List&lt;? super Stripe&gt;; list in to which the generated stripes are placed.
     * @param types Type...; type per lane pair, for N lanes N-1 should be provided
     * @return this LaneFactory this lane factory for method chaining
     */
    public LaneFactory addLanes(final List<? super Stripe> stripeList, final Type... types)
    {
        stripeList.add(this.firstStripe);
        List<Type> typeList = new ArrayList<>(Arrays.asList(types));
        typeList.add(Type.SOLID);
        for (Type type : typeList)
        {
            Length startOffset = this.offset.plus(this.laneWidth0.times(0.5)).plus(this.offsetStart);
            Length endOffset = this.offset.plus(this.laneWidth0.times(0.5)).plus(this.offsetEnd);

            List<CrossSectionSlice> slices =
                    LaneGeometryUtil.getSlices(this.line, startOffset, endOffset, this.laneWidth0.abs(), this.laneWidth0.abs());
            PolyLine2d centerLine = this.line.flattenOffset(LaneGeometryUtil.getCenterOffsets(this.line, slices), SEGMENTS);
            PolyLine2d leftEdge = this.line.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(this.line, slices), SEGMENTS);
            PolyLine2d rightEdge = this.line.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(this.line, slices), SEGMENTS);
            Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);

            this.lanes.add(Try.assign(
                    () -> new Lane(this.link, "Lane " + (this.lanes.size() + 1), new OtsLine2d(centerLine), contour, slices,
                            this.laneType0, Map.of(this.gtuType, this.speedLimit0)),
                    "Unexpected exception while building link."));
            this.offset = this.offset.plus(this.laneWidth0);

            Length width = getWidth(type);
            startOffset = this.offset.plus(this.offsetStart);
            endOffset = this.offset.plus(this.offsetEnd);
            List<CrossSectionSlice> slices2 = LaneGeometryUtil.getSlices(this.line, startOffset, endOffset, width, width);
            PolyLine2d centerLine2 = this.line.flattenOffset(LaneGeometryUtil.getCenterOffsets(this.line, slices2), SEGMENTS);
            leftEdge = this.line.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(this.line, slices2), SEGMENTS);
            rightEdge = this.line.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(this.line, slices2), SEGMENTS);
            Polygon2d contour2 = LaneGeometryUtil.getContour(leftEdge, rightEdge);
            stripeList.add(Try.assign(() -> new Stripe(type, this.link, new OtsLine2d(centerLine2), contour2, slices2),
                    "Unexpected exception while building link."));
        }
        return this;
    }

    /**
     * Return width to use for different stripe types.
     * @param type Type; stripe type.
     * @return Length; width.
     */
    private Length getWidth(final Type type)
    {
        switch (type)
        {
            case DASHED:
            case SOLID:
                return Length.instantiateSI(0.2);
            case LEFT:
            case RIGHT:
            case DOUBLE:
                return Length.instantiateSI(0.6);
            case BLOCK:
                return Length.instantiateSI(0.45);
            default:
                return Length.instantiateSI(0.2);
        }
    }

    /**
     * Adds 1 or 2 shoulders to the current set of lanes.
     * @param width Length; width of the shoulder
     * @param lat LateralDirectionality; side of shoulder, use {@code null} or {@code NONE} for both
     * @param laneType LaneType; lane type.
     * @return LaneFactory this lane factory for method chaining
     * @throws IllegalStateException if no lanes are defined
     */
    public LaneFactory addShoulder(final Length width, final LateralDirectionality lat, final LaneType laneType)
    {
        Throw.when(this.lanes.isEmpty(), IllegalStateException.class, "Lanes should be defined before adding shoulder(s).");
        if (lat == null || lat.isNone() || lat.isLeft())
        {
            Length startOffset = null;
            Length endOffset = null;
            for (Lane lane : this.lanes)
            {
                if (startOffset == null || lane.getOffsetAtBegin().plus(lane.getBeginWidth().times(0.5)).gt(startOffset))
                {
                    startOffset = lane.getOffsetAtBegin().plus(lane.getBeginWidth().times(0.5));
                }
                if (endOffset == null || lane.getOffsetAtEnd().plus(lane.getEndWidth().times(0.5)).gt(endOffset))
                {
                    endOffset = lane.getOffsetAtEnd().plus(lane.getEndWidth().times(0.5));
                }
            }
            Length start = startOffset.plus(width.times(0.5));
            Length end = endOffset.plus(width.times(0.5));
            Try.assign(() -> LaneGeometryUtil.createStraightShoulder(this.link, "Left shoulder", start, end, width, width,
                    laneType), "Unexpected exception while building link.");
        }
        if (lat == null || lat.isNone() || lat.isRight())
        {
            Length startOffset = null;
            Length endOffset = null;
            for (Lane lane : this.lanes)
            {
                if (startOffset == null || lane.getOffsetAtBegin().minus(lane.getBeginWidth().times(0.5)).lt(startOffset))
                {
                    startOffset = lane.getOffsetAtBegin().minus(lane.getBeginWidth().times(0.5));
                }
                if (endOffset == null || lane.getOffsetAtEnd().minus(lane.getEndWidth().times(0.5)).lt(endOffset))
                {
                    endOffset = lane.getOffsetAtEnd().minus(lane.getEndWidth().times(0.5));
                }
            }
            Length start = startOffset.minus(width.times(0.5));
            Length end = endOffset.minus(width.times(0.5));
            Try.assign(() -> LaneGeometryUtil.createStraightShoulder(this.link, "Right shoulder", start, end, width, width,
                    laneType), "Unexpected exception while building link.");
        }
        return this;
    }

    /**
     * Returns the created lanes in build order.
     * @return List&lt;Lane&gt; created lanes in build order
     */
    public List<Lane> getLanes()
    {
        return this.lanes;
    }

    /**
     * Create a Link along intermediate coordinates from one Node to another.
     * @param network RoadNetwork; the network
     * @param name String; name of the new Link
     * @param from Node; start Node of the new Link
     * @param to Node; end Node of the new Link
     * @param intermediatePoints Point2d[]; array of intermediate coordinates (may be null in which case the node points are
     *            used)
     * @param simulator OtsSimulatorInterface; the simulator for this network
     * @return Link; the newly constructed Link
     * @throws OtsGeometryException when the design line is degenerate (only one point or duplicate point)
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public static CrossSectionLink makeLink(final RoadNetwork network, final String name, final Node from, final Node to,
            final Point2d[] intermediatePoints, final OtsSimulatorInterface simulator)
            throws OtsGeometryException, NetworkException
    {
        List<Point2d> pointList = intermediatePoints == null ? List.of(from.getPoint(), to.getPoint())
                : new ArrayList<>(Arrays.asList(intermediatePoints));
        OtsLine2d designLine = new OtsLine2d(pointList);
        CrossSectionLink link =
                new CrossSectionLink(network, name, from, to, DefaultsNl.ROAD, designLine, null, LaneKeepingPolicy.KEEPRIGHT);
        return link;
    }

    /**
     * Create one Lane.
     * @param link CrossSectionLink; the link that owns the new Lane
     * @param id String; the id of this lane, should be unique within the link
     * @param laneType LaneType; the type of the new Lane
     * @param latPosAtStart Length; the lateral position of the new Lane with respect to the design line of the link at the
     *            start of the link
     * @param latPosAtEnd Length; the lateral position of the new Lane with respect to the design line of the link at the end of
     *            the link
     * @param width Length; the width of the new Lane
     * @param speedLimit Speed; the speed limit on the new Lane
     * @param simulator OtsSimulatorInterface; the simulator
     * @param gtuType GtuType; parent GTU type of relevant GTUs
     * @return Lane
     * @throws NetworkException on network inconsistency
     * @throws OtsGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType,
            final Length latPosAtStart, final Length latPosAtEnd, final Length width, final Speed speedLimit,
            final OtsSimulatorInterface simulator, final GtuType gtuType) throws NetworkException, OtsGeometryException
    {
        ContinuousLine line = new ContinuousPolyLine(link.getDesignLine().getLine2d(), link.getStartNode().getLocation(),
                link.getEndNode().getLocation());
        List<CrossSectionSlice> slices = new ArrayList<>();
        slices.add(new CrossSectionSlice(Length.ZERO, latPosAtStart, width));
        slices.add(new CrossSectionSlice(link.getLength(), latPosAtEnd, width));
        PolyLine2d center = line.flattenOffset(LaneGeometryUtil.getCenterOffsets(line, slices), SEGMENTS);
        PolyLine2d left = line.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(line, slices), SEGMENTS);
        PolyLine2d right = line.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(line, slices), SEGMENTS);

        List<Point2d> points = new ArrayList<>();
        left.getPoints().forEachRemaining(points::add);
        right.reverse().getPoints().forEachRemaining(points::add);
        Polygon2d contour = new Polygon2d(points);

        return new Lane(link, id, new OtsLine2d(center), contour, slices, laneType, Map.of(gtuType, speedLimit));
    }

    /**
     * Create a simple Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints Point2d[]; intermediate coordinates or null to create a straight road; the intermediate points
     *            may contain the coordinates of the from node and to node
     * @param laneType LaneType; type of the new Lane
     * @param speedLimit Speed; the speed limit on the new Lane
     * @param simulator OtsSimulatorInterface; the simulator
     * @param gtuType GtuType; parent GTU type of relevant GTUs
     * @return Lane; the new Lane
     * @throws NetworkException on network inconsistency
     * @throws OtsGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane makeLane(final RoadNetwork network, final String name, final Node from, final Node to,
            final Point2d[] intermediatePoints, final LaneType laneType, final Speed speedLimit,
            final OtsSimulatorInterface simulator, final GtuType gtuType) throws NetworkException, OtsGeometryException
    {
        Length width = new Length(4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(network, name, from, to, intermediatePoints, simulator);
        Length latPos = new Length(0.0, LengthUnit.METER);
        return makeLane(link, "lane", laneType, latPos, latPos, width, speedLimit, simulator, gtuType);
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints Point2d[]; intermediate coordinates or null to create a straight road; the intermediate points
     *            may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OtsSimulatorInterface; the simulator
     * @param gtuType GtuType; parent GTU type of relevant GTUs
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NetworkException on topological problems
     * @throws OtsGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final RoadNetwork network, final String name, final Node from, final Node to,
            final Point2d[] intermediatePoints, final int laneCount, final int laneOffsetAtStart, final int laneOffsetAtEnd,
            final LaneType laneType, final Speed speedLimit, final OtsSimulatorInterface simulator, final GtuType gtuType)
            throws NetworkException, OtsGeometryException
    {
        final CrossSectionLink link = makeLink(network, name, from, to, intermediatePoints, simulator);
        Lane[] result = new Lane[laneCount];
        Length width = new Length(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length latPosAtStart = new Length((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length latPosAtEnd = new Length((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            result[laneIndex] = makeLane(link, "lane." + laneIndex, laneType, latPosAtStart, latPosAtEnd, width, speedLimit,
                    simulator, gtuType);
        }
        return result;
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param from Node; starting node of the new Lane
     * @param to Node; ending node of the new Lane
     * @param intermediatePoints Point2d[]; intermediate coordinates or null to create a straight road; the intermediate points
     *            may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; Speed the speed limit (applies to all generated lanes)
     * @param simulator OtsSimulatorInterface; the simulator
     * @param gtuType GtuType; parent GTU type of relevant GTUs
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OtsGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final RoadNetwork network, final String name, final Node from, final Node to,
            final Point2d[] intermediatePoints, final int laneCount, final LaneType laneType, final Speed speedLimit,
            final OtsSimulatorInterface simulator, final GtuType gtuType)
            throws NamingException, NetworkException, OtsGeometryException
    {
        return makeMultiLane(network, name, from, to, intermediatePoints, laneCount, 0, 0, laneType, speedLimit, simulator,
                gtuType);
    }

    /**
     * Create a simple road with the specified number of Lanes, based on a Bezier curve.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param n1 Node; control node for the start direction
     * @param n2 Node; starting node of the new Lane
     * @param n3 Node; ending node of the new Lane
     * @param n4 Node; control node for the end direction
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OtsSimulatorInterface; the simulator
     * @param gtuType GtuType; parent GTU type of relevant GTUs
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OtsGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLaneBezier(final RoadNetwork network, final String name, final Node n1, final Node n2,
            final Node n3, final Node n4, final int laneCount, final int laneOffsetAtStart, final int laneOffsetAtEnd,
            final LaneType laneType, final Speed speedLimit, final OtsSimulatorInterface simulator, final GtuType gtuType)
            throws NamingException, NetworkException, OtsGeometryException
    {
        OrientedPoint2d dp1 = new OrientedPoint2d(n2.getPoint().x, n2.getPoint().y,
                Math.atan2(n2.getPoint().y - n1.getPoint().y, n2.getPoint().x - n1.getPoint().x));
        OrientedPoint2d dp2 = new OrientedPoint2d(n3.getPoint().x, n3.getPoint().y,
                Math.atan2(n4.getPoint().y - n3.getPoint().y, n4.getPoint().x - n3.getPoint().x));

        Length width = new Length(4.0, LengthUnit.METER);
        dp1 = OtsGeometryUtil.offsetPoint(dp1, (-0.5 - laneOffsetAtStart) * width.getSI());
        dp2 = OtsGeometryUtil.offsetPoint(dp2, (-0.5 - laneOffsetAtStart) * width.getSI());

        Point2d[] controlPoints = Bezier.cubicControlPoints(dp1, dp2, 0.5, false);
        ContinuousBezierCubic designLine =
                new ContinuousBezierCubic(controlPoints[0], controlPoints[1], controlPoints[2], controlPoints[3]);
        final CrossSectionLink link = makeLink(network, name, n2, n3,
                designLine.flatten(SEGMENTS).getPointList().toArray(new Point2d[65]), simulator);
        Lane[] result = new Lane[laneCount];

        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            // Length latPosAtStart = new Length((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            // Length latPosAtEnd = new Length((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            Length latPosAtStart = new Length(-laneIndex * width.getSI(), LengthUnit.SI);
            Length latPosAtEnd = new Length(-laneIndex * width.getSI(), LengthUnit.SI);
            List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, latPosAtStart, latPosAtEnd, width, width);
            PolyLine2d centerLine = designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), SEGMENTS);
            PolyLine2d leftEdge = designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), SEGMENTS);
            PolyLine2d rightEdge = designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), SEGMENTS);
            Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);
            result[laneIndex] = new Lane(link, "lane." + laneIndex, new OtsLine2d(centerLine), contour, slices, laneType,
                    Map.of(gtuType, speedLimit));
        }
        return result;
    }

    /**
     * @param n1 Node; node 1
     * @param n2 Node; node 2
     * @param n3 Node; node 3
     * @param n4 Node; node 4
     * @return line between n2 and n3 with start-direction n1--&gt;n2 and end-direction n3--&gt;n4
     * @throws OtsGeometryException on failure of Bezier curve creation
     */
    public static OtsLine2d makeBezier(final Node n1, final Node n2, final Node n3, final Node n4) throws OtsGeometryException
    {
        Point2d p1 = n1.getPoint();
        Point2d p2 = n2.getPoint();
        Point2d p3 = n3.getPoint();
        Point2d p4 = n4.getPoint();
        OrientedPoint2d dp1 = new OrientedPoint2d(p2.x, p2.y, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        OrientedPoint2d dp2 = new OrientedPoint2d(p3.x, p3.y, Math.atan2(p4.y - p3.y, p4.x - p3.x));
        return Bezier.cubic(dp1, dp2);
    }
}
