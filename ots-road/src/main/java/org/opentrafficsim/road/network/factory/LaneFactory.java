package org.opentrafficsim.road.network.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class LaneFactory
{

    /** Stripe width. */
    private static final Length STRIPE_WIDTH = Length.instantiateSI(0.2);

    /** Angle above which a Bezier curve is used over a straight line. */
    private static final double BEZIER_MARGIN = Math.toRadians(0.5);

    /** Link. */
    private final CrossSectionLink link;

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

    /** created lanes. */
    private final List<Lane> lanes = new ArrayList<>();

    /**
     * @param network OTSRoadNetwork; network
     * @param from OTSRoadNode; from node
     * @param to OTSRoadNode; to node
     * @param type LinkType; link type
     * @param simulator OTSSimulatorInterface; simulator
     * @param policy LaneKeepingPolicy; lane keeping policy
     * @throws OTSGeometryException if no valid line can be created
     * @throws NetworkException if the link exists, or a node does not exist, in the network
     */
    public LaneFactory(final OTSRoadNetwork network, final OTSRoadNode from, final OTSRoadNode to, final LinkType type,
            final OtsSimulatorInterface simulator, final LaneKeepingPolicy policy) throws OTSGeometryException, NetworkException
    {
        this(network, from, to, type, simulator, policy, makeLine(from, to));
    }

    /**
     * @param network OTSRoadNetwork; network
     * @param from OTSRoadNode; from node
     * @param to OTSRoadNode; to node
     * @param type LinkType; link type
     * @param simulator OTSSimulatorInterface; simulator
     * @param policy LaneKeepingPolicy; lane keeping policy
     * @param line OTSLine3D; line
     * @throws NetworkException if the link exists, or a node does not exist, in the network
     */
    public LaneFactory(final OTSRoadNetwork network, final OTSRoadNode from, final OTSRoadNode to, final LinkType type,
            final OtsSimulatorInterface simulator, final LaneKeepingPolicy policy, final OTSLine3D line) throws NetworkException
    {
        this.link = new CrossSectionLink(network, from.getId() + to.getId(), from, to, type, line, policy);
    }

    /**
     * Creates a line between two nodes. If the nodes and their directions are on a straight line, a straight line is created.
     * Otherwise a default Bezier curve is created.
     * @param from OTSNode; from node
     * @param to OTSNode; to node
     * @return OTSLine3D; line
     * @throws OTSGeometryException if no valid line can be created
     */
    private static OTSLine3D makeLine(final OTSNode from, final OTSNode to) throws OTSGeometryException
    {
        // Straight or bezier?
        double rotCrow = Math.atan2(to.getLocation().y - from.getLocation().y, to.getLocation().x - from.getLocation().x);
        double dRot = from.getLocation().getRotZ() - rotCrow;
        while (dRot < -Math.PI)
        {
            dRot += 2.0 * Math.PI;
        }
        while (dRot > Math.PI)
        {
            dRot -= 2.0 * Math.PI;
        }
        OTSLine3D line;
        if (from.getLocation().getRotZ() != to.getLocation().getRotZ() || Math.abs(dRot) > BEZIER_MARGIN)
        {
            line = Bezier.cubic(from.getLocation(), to.getLocation());
        }
        else
        {
            line = new OTSLine3D(from.getPoint(), to.getPoint());
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
        Try.execute(
                () -> new Stripe(this.link, this.offset.plus(this.offsetStart), this.offset.plus(this.offsetEnd), STRIPE_WIDTH),
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
        Try.execute(() -> new Stripe(this.link, this.offset, this.offset, STRIPE_WIDTH),
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
     * Adds a lane pair for each permeable, where the permeable determines the right-hand side line when building from left to
     * right and vice versa. The left-most line is created in {@code leftToRight()}, meaning that each permeable describes
     * permeablility between a lane and it's right-hand neighbor, when building left to right (and vice versa). For no allowed
     * lane changes use {@code null}. This method internally adds {@code null} to create the final continuous stripe.
     * @param permeable Permeable...; permeable per lane pair, for N lanes N-1 should be provided
     * @return this LaneFactory this lane factory for method chaining
     */
    public LaneFactory addLanes(final Permeable... permeable)
    {
        List<Permeable> list = new ArrayList<>(Arrays.asList(permeable));
        list.add(null);
        for (Permeable perm : list)
        {
            Length startOffset = this.offset.plus(this.laneWidth0.times(0.5)).plus(this.offsetStart);
            Length endOffset = this.offset.plus(this.laneWidth0.times(0.5)).plus(this.offsetEnd);
            this.lanes.add(Try.assign(
                    () -> new Lane(this.link, "Lane " + (this.lanes.size() + 1), startOffset, endOffset, this.laneWidth0.abs(),
                            this.laneWidth0.abs(), this.laneType0, this.speedLimit0),
                    "Unexpected exception while building link."));
            this.offset = this.offset.plus(this.laneWidth0);
            Stripe stripe = Try.assign(() -> new Stripe(this.link, this.offset.plus(this.offsetStart),
                    this.offset.plus(this.offsetEnd), STRIPE_WIDTH), "Unexpected exception while building link.");
            if (perm != null)
            {
                stripe.addPermeability(this.link.getNetwork().getGtuType(GtuType.DEFAULTS.VEHICLE), perm);
            }
        }
        return this;
    }

    /**
     * Adds 1 or 2 shoulders to the current set of lanes.
     * @param width Length; width of the shoulder
     * @param lat LateralDirectionality; side of shoulder, use {@code null} or {@code NONE} for both
     * @return LaneFactory this lane factory for method chaining
     * @throws IllegalStateException if no lanes are defined
     */
    public LaneFactory addShoulder(final Length width, final LateralDirectionality lat)
    {
        Throw.when(this.lanes.isEmpty(), IllegalStateException.class, "Lanes should be defined before adding shoulder(s).");
        if (lat == null || lat.isNone() || lat.isLeft())
        {
            Length startOffset = null;
            Length endOffset = null;
            for (Lane lane : this.lanes)
            {
                if (startOffset == null
                        || lane.getDesignLineOffsetAtBegin().plus(lane.getBeginWidth().times(0.5)).gt(startOffset))
                {
                    startOffset = lane.getDesignLineOffsetAtBegin().plus(lane.getBeginWidth().times(0.5));
                }
                if (endOffset == null || lane.getDesignLineOffsetAtEnd().plus(lane.getEndWidth().times(0.5)).gt(endOffset))
                {
                    endOffset = lane.getDesignLineOffsetAtEnd().plus(lane.getEndWidth().times(0.5));
                }
            }
            Length start = startOffset.plus(width.times(0.5));
            Length end = endOffset.plus(width.times(0.5));
            Try.assign(() -> new Shoulder(this.link, "Left shoulder", start, end, width, width),
                    "Unexpected exception while building link.");
        }
        if (lat == null || lat.isNone() || lat.isRight())
        {
            Length startOffset = null;
            Length endOffset = null;
            for (Lane lane : this.lanes)
            {
                if (startOffset == null
                        || lane.getDesignLineOffsetAtBegin().minus(lane.getBeginWidth().times(0.5)).lt(startOffset))
                {
                    startOffset = lane.getDesignLineOffsetAtBegin().minus(lane.getBeginWidth().times(0.5));
                }
                if (endOffset == null || lane.getDesignLineOffsetAtEnd().minus(lane.getEndWidth().times(0.5)).lt(endOffset))
                {
                    endOffset = lane.getDesignLineOffsetAtEnd().minus(lane.getEndWidth().times(0.5));
                }
            }
            Length start = startOffset.minus(width.times(0.5));
            Length end = endOffset.minus(width.times(0.5));
            Try.assign(() -> new Shoulder(this.link, "Right shoulder", start, end, width, width),
                    "Unexpected exception while building link.");
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
     * @param from OTSRoadNode; start Node of the new Link
     * @param to OTSRoadNode; end Node of the new Link
     * @param intermediatePoints OTSPoint3D[]; array of intermediate coordinates (may be null); the intermediate points may
     *            contain the coordinates of the from node and to node
     * @param simulator OTSSimulatorInterface; the simulator for this network
     * @return Link; the newly constructed Link
     * @throws OTSGeometryException when the design line is degenerate (only one point or duplicate point)
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public static CrossSectionLink makeLink(final OTSRoadNetwork network, final String name, final OTSRoadNode from,
            final OTSRoadNode to, final OTSPoint3D[] intermediatePoints, final OtsSimulatorInterface simulator)
            throws OTSGeometryException, NetworkException
    {
        List<OTSPoint3D> pointList =
                intermediatePoints == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(intermediatePoints));
        if (pointList.size() == 0 || !from.getPoint().equals(pointList.get(0)))
        {
            pointList.add(0, from.getPoint());
        }
        if (pointList.size() == 0 || !to.getPoint().equals(pointList.get(pointList.size() - 1)))
        {
            pointList.add(to.getPoint());
        }

        /*-
        // see if an intermediate point needs to be created to the start of the link in the right direction
        OTSPoint3D s1 = pointList.get(0);
        OTSPoint3D s2 = pointList.get(1);
        double dy = s2.y - s1.y;
        double dx = s2.x - s1.x;
        double a = from.getLocation().getRotZ();
        if (Math.abs(a - Math.atan2(dy, dx)) > 1E-6)
        {
            double r = Math.min(1.0, Math.sqrt(dy * dy + dx * dx) / 4.0); 
            OTSPoint3D extra = new OTSPoint3D(s1.x + r * Math.cos(a), s1.y + r * Math.sin(a), s1.z);
            pointList.add(1, extra);
        }
        
        // see if an intermediate point needs to be created to the end of the link in the right direction
        s1 = pointList.get(pointList.size() - 2);
        s2 = pointList.get(pointList.size() - 1);
        dy = s2.y - s1.y;
        dx = s2.x - s1.x;
        a = to.getLocation().getRotZ() - Math.PI;
        if (Math.abs(a - Math.atan2(dy, dx)) > 1E-6)
        {
            double r = Math.min(1.0, Math.sqrt(dy * dy + dx * dx) / 4.0); 
            OTSPoint3D extra = new OTSPoint3D(s2.x + r * Math.cos(a), s2.y + r * Math.sin(a), s2.z);
            pointList.add(pointList.size() - 2, extra);
        }
         */

        OTSLine3D designLine = new OTSLine3D(pointList);
        CrossSectionLink link = new CrossSectionLink(network, name, from, to, network.getLinkType(LinkType.DEFAULTS.ROAD),
                designLine, LaneKeepingPolicy.KEEPRIGHT);
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
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private static Lane makeLane(final CrossSectionLink link, final String id, final LaneType laneType,
            final Length latPosAtStart, final Length latPosAtEnd, final Length width, final Speed speedLimit,
            final OtsSimulatorInterface simulator) throws NetworkException, OTSGeometryException
    {
        Map<GtuType, Speed> speedMap = new LinkedHashMap<>();
        speedMap.put(link.getNetwork().getGtuType(GtuType.DEFAULTS.VEHICLE), speedLimit);
        Lane result = new Lane(link, id, latPosAtStart, latPosAtEnd, width, width, laneType, speedMap);
        return result;
    }

    /**
     * Create a simple Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Lane (and also of the Link that owns it)
     * @param from OTSRoadNode; starting node of the new Lane
     * @param to OTSRoadNode; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneType LaneType; type of the new Lane
     * @param speedLimit Speed; the speed limit on the new Lane
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane; the new Lane
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    public static Lane makeLane(final OTSRoadNetwork network, final String name, final OTSRoadNode from, final OTSRoadNode to,
            final OTSPoint3D[] intermediatePoints, final LaneType laneType, final Speed speedLimit,
            final OtsSimulatorInterface simulator) throws NetworkException, OTSGeometryException
    {
        Length width = new Length(4.0, LengthUnit.METER);
        final CrossSectionLink link = makeLink(network, name, from, to, intermediatePoints, simulator);
        Length latPos = new Length(0.0, LengthUnit.METER);
        return makeLane(link, "lane", laneType, latPos, latPos, width, speedLimit, simulator);
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param from OTSNode; starting node of the new Lane
     * @param to OTSNode; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final OTSRoadNetwork network, final String name, final OTSRoadNode from,
            final OTSRoadNode to, final OTSPoint3D[] intermediatePoints, final int laneCount, final int laneOffsetAtStart,
            final int laneOffsetAtEnd, final LaneType laneType, final Speed speedLimit, final OtsSimulatorInterface simulator)
            throws NetworkException, OTSGeometryException
    {
        final CrossSectionLink link = makeLink(network, name, from, to, intermediatePoints, simulator);
        Lane[] result = new Lane[laneCount];
        Length width = new Length(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length latPosAtStart = new Length((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length latPosAtEnd = new Length((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            result[laneIndex] =
                    makeLane(link, "lane." + laneIndex, laneType, latPosAtStart, latPosAtEnd, width, speedLimit, simulator);
        }
        return result;
    }

    /**
     * Create a simple road with the specified number of Lanes.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param from OTSRoadNode; starting node of the new Lane
     * @param to OTSRoadNode; ending node of the new Lane
     * @param intermediatePoints OTSPoint3D[]; intermediate coordinates or null to create a straight road; the intermediate
     *            points may contain the coordinates of the from node and to node
     * @param laneCount int; number of lanes in the road
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; Speed the speed limit (applies to all generated lanes)
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLane(final OTSRoadNetwork network, final String name, final OTSRoadNode from,
            final OTSRoadNode to, final OTSPoint3D[] intermediatePoints, final int laneCount, final LaneType laneType,
            final Speed speedLimit, final OtsSimulatorInterface simulator)
            throws NamingException, NetworkException, OTSGeometryException
    {
        return makeMultiLane(network, name, from, to, intermediatePoints, laneCount, 0, 0, laneType, speedLimit, simulator);
    }

    /**
     * Create a simple road with the specified number of Lanes, based on a Bezier curve.<br>
     * This method returns an array of Lane. These lanes are embedded in a Link that can be accessed through the getParentLink
     * method of the Lane.
     * @param network RoadNetwork; the network
     * @param name String; name of the Link
     * @param n1 OTSRoadNode; control node for the start direction
     * @param n2 OTSRoadNode; starting node of the new Lane
     * @param n3 OTSRoadNode; ending node of the new Lane
     * @param n4 OTSRoadNode; control node for the end direction
     * @param laneCount int; number of lanes in the road
     * @param laneOffsetAtStart int; extra offset from design line in lane widths at start of link
     * @param laneOffsetAtEnd int; extra offset from design line in lane widths at end of link
     * @param laneType LaneType; type of the new Lanes
     * @param speedLimit Speed; the speed limit on all lanes
     * @param simulator OTSSimulatorInterface; the simulator
     * @return Lane&lt;String, String&gt;[]; array containing the new Lanes
     * @throws NamingException when names cannot be registered for animation
     * @throws NetworkException on topological problems
     * @throws OTSGeometryException when creation of center line or contour fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static Lane[] makeMultiLaneBezier(final OTSRoadNetwork network, final String name, final OTSRoadNode n1,
            final OTSRoadNode n2, final OTSRoadNode n3, final OTSRoadNode n4, final int laneCount, final int laneOffsetAtStart,
            final int laneOffsetAtEnd, final LaneType laneType, final Speed speedLimit, final OtsSimulatorInterface simulator)
            throws NamingException, NetworkException, OTSGeometryException
    {
        OTSLine3D bezier = makeBezier(n1, n2, n3, n4);
        final CrossSectionLink link = makeLink(network, name, n2, n3, bezier.getPoints(), simulator);
        Lane[] result = new Lane[laneCount];
        Length width = new Length(4.0, LengthUnit.METER);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            // Be ware! LEFT is lateral positive, RIGHT is lateral negative.
            Length latPosAtStart = new Length((-0.5 - laneIndex - laneOffsetAtStart) * width.getSI(), LengthUnit.SI);
            Length latPosAtEnd = new Length((-0.5 - laneIndex - laneOffsetAtEnd) * width.getSI(), LengthUnit.SI);
            result[laneIndex] =
                    makeLane(link, "lane." + laneIndex, laneType, latPosAtStart, latPosAtEnd, width, speedLimit, simulator);
        }
        return result;
    }

    /**
     * @param n1 OTSNode; node 1
     * @param n2 OTSNode; node 2
     * @param n3 OTSNode; node 3
     * @param n4 OTSNode; node 4
     * @return line between n2 and n3 with start-direction n1--&gt;n2 and end-direction n3--&gt;n4
     * @throws OTSGeometryException on failure of Bezier curve creation
     */
    public static OTSLine3D makeBezier(final OTSNode n1, final OTSNode n2, final OTSNode n3, final OTSNode n4)
            throws OTSGeometryException
    {
        OTSPoint3D p1 = n1.getPoint();
        OTSPoint3D p2 = n2.getPoint();
        OTSPoint3D p3 = n3.getPoint();
        OTSPoint3D p4 = n4.getPoint();
        DirectedPoint dp1 = new DirectedPoint(p2.x, p2.y, p2.z, 0.0, 0.0, Math.atan2(p2.y - p1.y, p2.x - p1.x));
        DirectedPoint dp2 = new DirectedPoint(p3.x, p3.y, p3.z, 0.0, 0.0, Math.atan2(p4.y - p3.y, p4.x - p3.x));
        return Bezier.cubic(dp1, dp2);
    }
}
