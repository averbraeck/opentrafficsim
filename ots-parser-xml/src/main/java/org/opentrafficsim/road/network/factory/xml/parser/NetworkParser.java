package org.opentrafficsim.road.network.factory.xml.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousPolyLine;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.Flattener.MaxDeviationAndAngle;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsGeometryUtil;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Centroid;
import org.opentrafficsim.core.network.Connector;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Cloner;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.CrossSectionSlice;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneGeometryUtil;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.xml.bindings.types.ArcDirection;
import org.opentrafficsim.xml.generated.BasicRoadLayout;
import org.opentrafficsim.xml.generated.CseLane;
import org.opentrafficsim.xml.generated.CseNoTrafficLane;
import org.opentrafficsim.xml.generated.CseShoulder;
import org.opentrafficsim.xml.generated.CseStripe;
import org.opentrafficsim.xml.generated.Link;
import org.opentrafficsim.xml.generated.Link.LaneOverride;
import org.opentrafficsim.xml.generated.Network;
import org.opentrafficsim.xml.generated.RoadLayout;
import org.opentrafficsim.xml.generated.SpeedLimit;
import org.opentrafficsim.xml.generated.TrafficLightType;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * NetworkParser parses the Network tag of the OTS network.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class NetworkParser
{
    /** */
    private NetworkParser()
    {
        // utility class
    }

    /**
     * Parse the Nodes.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param network Network; the Network tag
     * @param nodeDirections Map&lt;String,Direction&gt;; a map of the node ids and their default directions
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final RoadNetwork otsNetwork, final Network network,
            final Map<String, Direction> nodeDirections) throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Centroid xmlCentroid : ParseUtil
                .getObjectsOfType(network.getNodeOrLinkOrCentroid(), org.opentrafficsim.xml.generated.Centroid.class))
        {
            new Centroid(otsNetwork, xmlCentroid.getId(),
                    new Point2d(xmlCentroid.getCoordinate().x, xmlCentroid.getCoordinate().y));
        }
        for (org.opentrafficsim.xml.generated.Node xmlNode : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(),
                org.opentrafficsim.xml.generated.Node.class))
        {
            new Node(otsNetwork, xmlNode.getId(), new Point2d(xmlNode.getCoordinate().x, xmlNode.getCoordinate().y),
                    nodeDirections.get(xmlNode.getId()));
        }
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the Straight Link elements in
     * the XML file.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param network Network; the Network tag
     * @return a map of nodes and their default direction
     */
    public static Map<String, Direction> calculateNodeAngles(final RoadNetwork otsNetwork, final Network network)
    {
        Map<String, Direction> nodeDirections = new LinkedHashMap<>();
        Map<String, Point2d> points = new LinkedHashMap<>();
        for (org.opentrafficsim.xml.generated.Node xmlNode : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(),
                org.opentrafficsim.xml.generated.Node.class))
        {
            if (xmlNode.getDirection() != null)
            {
                nodeDirections.put(xmlNode.getId(), xmlNode.getDirection());
            }
            points.put(xmlNode.getId(), xmlNode.getCoordinate());
        }

        for (Link xmlLink : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(), Link.class))
        {
            if (xmlLink.getStraight() != null)
            {
                Point2d startPoint = points.get(xmlLink.getNodeStart());
                Point2d endPoint = points.get(xmlLink.getNodeEnd());
                double direction = Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);
                if (!nodeDirections.containsKey(xmlLink.getNodeStart()))
                {
                    nodeDirections.put(xmlLink.getNodeStart(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(xmlLink.getNodeEnd()))
                {
                    nodeDirections.put(xmlLink.getNodeEnd(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }

        for (org.opentrafficsim.xml.generated.Node xmlNode : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(),
                org.opentrafficsim.xml.generated.Node.class))
        {
            if (!nodeDirections.containsKey(xmlNode.getId()))
            {
                System.err.println("Warning: Node " + xmlNode.getId() + " does not have a (calculated) direction");
            }
        }

        return nodeDirections;
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions.
     * @param network Network; the Network tag
     * @param nodeDirections Map&lt;String,Direction&gt;; a map of the node ids and their default directions
     * @param simulator OtsSimulatorInterface; the simulator
     * @param designLines Map&lt;String, ContinuousLine&gt;; map t store created design lines.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     */
    static void parseLinks(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final Map<String, Direction> nodeDirections, final OtsSimulatorInterface simulator,
            final Map<String, ContinuousLine> designLines) throws NetworkException, OtsGeometryException
    {
        for (org.opentrafficsim.xml.generated.Connector xmlConnector : ParseUtil
                .getObjectsOfType(network.getNodeOrLinkOrCentroid(), org.opentrafficsim.xml.generated.Connector.class))
        {
            Node node = (Node) otsNetwork.getNode(xmlConnector.getNode());
            if (null == node)
            {
                simulator.getLogger().always()
                        .debug("No node (" + xmlConnector.getNode() + ") for Connector " + xmlConnector.getId());
            }
            Node centroid = (Node) otsNetwork.getNode(xmlConnector.getCentroid());
            if (null == centroid)
            {
                simulator.getLogger().always()
                        .debug("No centroid (" + xmlConnector.getCentroid() + ") for Connector " + xmlConnector.getId());
            }
            String id = xmlConnector.getId();
            double demandWeight = xmlConnector.getDemandWeight();
            LinkType linkType = definitions.get(LinkType.class, xmlConnector.getType());
            Connector link = xmlConnector.isOutbound() ? new Connector(otsNetwork, id, centroid, node, linkType)
                    : new Connector(otsNetwork, id, node, centroid, linkType);
            link.setDemandWeight(demandWeight);
        }

        for (Link xmlLink : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(), Link.class))
        {
            Node startNode = (Node) otsNetwork.getNode(xmlLink.getNodeStart());
            Node endNode = (Node) otsNetwork.getNode(xmlLink.getNodeEnd());
            Point2d startPoint = startNode.getPoint();
            Point2d endPoint = endNode.getPoint();
            Point2d[] coordinates = null;

            // start and end with heading, adjusted with offset
            double startHeading = startNode.getHeading().si;
            OrientedPoint2d start = new OrientedPoint2d(startPoint.x, startPoint.y, startHeading);
            if (xmlLink.getOffsetStart() != null)
            {
                start = OtsGeometryUtil.offsetPoint(start, xmlLink.getOffsetStart().si);
            }
            double endHeading = endNode.getHeading().si;
            OrientedPoint2d end = new OrientedPoint2d(endPoint.x, endPoint.y, endHeading);
            if (xmlLink.getOffsetEnd() != null)
            {
                end = OtsGeometryUtil.offsetPoint(end, xmlLink.getOffsetEnd().si);
            }

            ContinuousLine designLine;
            Integer numSegments = null;
            Angle maxAngleError = null;
            Length maxSpatialError = null;
            if (xmlLink.getStraight() != null)
            {
                designLine = new ContinuousStraight(start, Math.hypot(end.x - start.x, end.y - start.y));
            }
            else if (xmlLink.getPolyline() != null)
            {
                int intermediatePoints = xmlLink.getPolyline().getCoordinate().size();
                coordinates = new Point2d[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = xmlLink.getPolyline().getCoordinate().get(p);
                }
                designLine = new ContinuousPolyLine(new PolyLine2d(true, coordinates));
            }
            else if (xmlLink.getArc() != null)
            {
                if (xmlLink.getArc().getNumSegments() != null)
                {
                    numSegments = xmlLink.getArc().getNumSegments().intValue();
                }
                double radius = xmlLink.getArc().getRadius().getSI();
                boolean left = xmlLink.getArc().getDirection().equals(ArcDirection.LEFT);
                while (left && endHeading < startHeading)
                {
                    endHeading += 2.0 * Math.PI;
                }
                while (!left && endHeading > startHeading)
                {
                    endHeading -= 2.0 * Math.PI;
                }
                designLine = new ContinuousArc(start, radius, left, Angle.instantiateSI(Math.abs(endHeading) - startHeading));
            }
            else if (xmlLink.getBezier() != null)
            {
                if (xmlLink.getBezier().getNumSegments() != null)
                {
                    numSegments = xmlLink.getBezier().getNumSegments().intValue();
                }
                double shape = xmlLink.getBezier().getShape();
                boolean weighted = xmlLink.getBezier().isWeighted();
                Point2d[] designPoints = Bezier.cubicControlPoints(start, end, shape, weighted);
                designLine = new ContinuousBezierCubic(designPoints[0], designPoints[1], designPoints[2], designPoints[3]);
            }
            else if (xmlLink.getClothoid() != null)
            {
                // int numSegments = xmlLink.getCLOTHOID().getNumSegments().intValue();

                // TODO: Clothoid parsing
                designLine = null;
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getId()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }
            designLines.put(xmlLink.getId(), designLine);

            // TODO: take defaults from network when not defined for link
            PolyLine2d flattenedLine;
            if (maxAngleError != null)
            {
                flattenedLine = designLine.flatten(new MaxDeviationAndAngle(maxSpatialError.si, maxAngleError.si));
            }
            else
            {
                numSegments = numSegments == null ? 64 : numSegments;
                flattenedLine = designLine.flatten(new NumSegments(numSegments));
            }

            // TODO: Directionality has to be added later when the lanes and their direction are known.
            LaneKeepingPolicy laneKeepingPolicy = LaneKeepingPolicy.valueOf(xmlLink.getLaneKeeping().name());
            LinkType linkType = definitions.get(LinkType.class, xmlLink.getType());
            // TODO: elevation data
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getId(), startNode, endNode, linkType,
                    new OtsLine2d(flattenedLine), null, laneKeepingPolicy);

            if (xmlLink.getPriority() != null)
            {
                Priority priority = Priority.valueOf(xmlLink.getPriority().toString());
                link.setPriority(priority);
            }
        }
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param definitions Definitions; parsed definitions.
     * @param network Network; the Network tag
     * @param simulator OtsSimulatorInterface; the simulator
     * @param roadLayoutMap the map of the tags of the predefined RoadLayout tags in Definitions
     * @param linkTypeSpeedLimitMap map of speed limits per link type
     * @param designLines Map&lt;String, ContinuousLine&gt;; design lines per link id.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     */
    static void applyRoadLayout(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final OtsSimulatorInterface simulator, final Map<String, RoadLayout> roadLayoutMap,
            final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap, final Map<String, ContinuousLine> designLines)
            throws NetworkException, OtsGeometryException, XmlParserException, SimRuntimeException, GtuException
    {
        for (Link xmlLink : ParseUtil.getObjectsOfType(network.getNodeOrLinkOrCentroid(), Link.class))
        {
            CrossSectionLink csl = (CrossSectionLink) otsNetwork.getLink(xmlLink.getId());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new LinkedHashMap<>();

            // Get the RoadLayout (either defined here, or via pointer to Definitions)
            BasicRoadLayout roadLayoutTag;
            if (xmlLink.getDefinedLayout() != null)
            {
                Throw.when(xmlLink.getRoadLayout() != null, XmlParserException.class,
                        "Link %s Ambiguous RoadLayout; both DefinedRoadLayout and RoadLayout defined", xmlLink.getId());
                RoadLayout roadLayoutTagBase = roadLayoutMap.get(xmlLink.getDefinedLayout());
                Throw.when(roadLayoutTagBase == null, XmlParserException.class, "Link %s Could not find defined RoadLayout %s",
                        xmlLink.getId(), xmlLink.getDefinedLayout());
                // Process LaneOverrides
                roadLayoutTag = Cloner.cloneRoadLayout(roadLayoutTagBase);
                for (LaneOverride laneOverride : xmlLink.getLaneOverride())
                {
                    for (CseLane lane : ParseUtil.getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), CseLane.class))
                    {
                        if (lane.getId().equals(laneOverride.getLane()))
                        {
                            if (laneOverride.getSpeedLimit().size() > 0)
                            {
                                lane.getSpeedLimit().clear();
                                lane.getSpeedLimit().addAll(laneOverride.getSpeedLimit());
                            }
                        }
                    }
                }
            }
            else
            {
                roadLayoutTag = xmlLink.getRoadLayout();
                if (roadLayoutTag == null)
                {
                    throw new XmlParserException("Link " + xmlLink.getId() + " No RoadLayout defined");
                }
            }

            // calculate for each lane and stripe what the start and end offset is
            List<CseData> cseDataList = new ArrayList<>();
            Map<Object, Integer> cseTagMap = new LinkedHashMap<>();
            calculateOffsets(roadLayoutTag, xmlLink, cseDataList, cseTagMap);

            // Stripe
            ContinuousLine designLine = designLines.get(xmlLink.getId());
            for (CseStripe stripeTag : ParseUtil.getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), CseStripe.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, designLine, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList);
            }

            // Other CrossSectionElement
            for (org.opentrafficsim.xml.generated.CrossSectionElement cseTag : ParseUtil.getObjectsOfType(
                    roadLayoutTag.getStripeOrLaneOrShoulder(), org.opentrafficsim.xml.generated.CrossSectionElement.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(cseTag));

                List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, cseData.centerOffsetStart,
                        cseData.centerOffsetEnd, cseData.widthStart, cseData.widthEnd);
                NumSegments numSegments64 = new NumSegments(64);
                PolyLine2d centerLine =
                        designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), numSegments64);
                PolyLine2d leftEdge =
                        designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), numSegments64);
                PolyLine2d rightEdge =
                        designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), numSegments64);
                Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);

                // Lane
                if (cseTag instanceof CseLane)
                {
                    CseLane laneTag = (CseLane) cseTag;
                    LaneType laneType = definitions.get(LaneType.class, laneTag.getLaneType());
                    Map<GtuType, Speed> speedLimitMap = new LinkedHashMap<>();
                    LinkType linkType = csl.getType();
                    if (!linkTypeSpeedLimitMap.containsKey(linkType))
                    {
                        linkTypeSpeedLimitMap.put(linkType, new LinkedHashMap<>());
                    }
                    speedLimitMap.putAll(linkTypeSpeedLimitMap.get(linkType));
                    for (SpeedLimit speedLimitTag : roadLayoutTag.getSpeedLimit())
                    {
                        GtuType gtuType = definitions.get(GtuType.class, speedLimitTag.getGtuType());
                        speedLimitMap.put(gtuType, speedLimitTag.getLegalSpeedLimit());
                    }
                    for (SpeedLimit speedLimitTag : laneTag.getSpeedLimit())
                    {
                        GtuType gtuType = definitions.get(GtuType.class, speedLimitTag.getGtuType());
                        speedLimitMap.put(gtuType, speedLimitTag.getLegalSpeedLimit());
                    }
                    Lane lane =
                            new Lane(csl, laneTag.getId(), new OtsLine2d(centerLine), contour, slices, laneType, speedLimitMap);
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                }

                // NoTrafficLane
                else if (cseTag instanceof CseNoTrafficLane)
                {
                    CseNoTrafficLane ntlTag = (CseNoTrafficLane) cseTag;
                    String id = ntlTag.getId() != null ? ntlTag.getId() : UUID.randomUUID().toString();
                    Lane lane = Lane.noTrafficLane(csl, id, new OtsLine2d(centerLine), contour, slices);
                    cseList.add(lane);
                }

                // Shoulder
                else if (cseTag instanceof CseShoulder)
                {
                    CseShoulder shoulderTag = (CseShoulder) cseTag;
                    String id = shoulderTag.getId() != null ? shoulderTag.getId() : UUID.randomUUID().toString();
                    CrossSectionElement shoulder = new Shoulder(csl, id, new OtsLine2d(centerLine), contour, slices);
                    cseList.add(shoulder);
                }
            }

            // TrafficLight
            for (TrafficLightType trafficLight : xmlLink.getTrafficLight())
            {
                Throw.when(!lanes.containsKey(trafficLight.getLane()), NetworkException.class,
                        "Link: %s, TrafficLight with id %s on Lane %s - Lane not found", xmlLink.getId(), trafficLight.getId(),
                        trafficLight.getLane());
                Lane lane = lanes.get(trafficLight.getLane());
                Length position = Transformer.parseLengthBeginEnd(trafficLight.getPosition(), lane.getLength());
                try
                {
                    @SuppressWarnings("unchecked")
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getClazz(),
                            new Class[] {String.class, Lane.class, Length.class, OtsSimulatorInterface.class});
                    trafficLightConstructor.newInstance(new Object[] {trafficLight.getId(), lane, position, simulator});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException("TrafficLight: Class Name " + trafficLight.getClazz().getName()
                            + " for traffic light " + trafficLight.getId() + " on lane " + lane.toString() + " at position "
                            + position + " -- class not found or constructor not right", exception);
                    // TODO: this discards too much information; e.g. Network already contains an object with the name ...
                }
            }
        }
    }

    /**
     * Calculate the offsets for the RoadLlayout. Note that offsets can be different for begin and end, and that they can be
     * specified from the right, left or center of the lane/stripe. Start width and end width can be different. The overall Link
     * can have an additional start offset and end offset that has to be added to the already calculated offsets.
     * @param roadLayoutTag the tag for the road layout containing all lanes and stripes
     * @param xmlLink the Link tag containing the overall offsets
     * @param cseDataList the list of offsets and widths for each tag, in order of definition in the RoadLayout tag
     * @param cseTagMap the map of the tags to the index in the list, to be able to find them quickly
     */
    @SuppressWarnings("checkstyle:methodlength")
    private static void calculateOffsets(final BasicRoadLayout roadLayoutTag, final Link xmlLink,
            final List<CseData> cseDataList, final Map<Object, Integer> cseTagMap)
    {
        int nr = 0;
        Length totalWidthStart = Length.ZERO;
        Length totalWidthEnd = Length.ZERO;
        boolean startOffset = false;
        boolean endOffset = false;
        for (Object o : roadLayoutTag.getStripeOrLaneOrShoulder())
        {
            if (o instanceof CseStripe)
            {
                CseStripe stripe = (CseStripe) o;
                CseData cseData = new CseData();
                cseData.widthStart = Length.ZERO;
                cseData.widthEnd = Length.ZERO;
                if (stripe.getCenterOffset() != null)
                {
                    cseData.centerOffsetStart = stripe.getCenterOffset();
                    cseData.centerOffsetEnd = stripe.getCenterOffset();
                    startOffset = true;
                    endOffset = true;
                }
                else
                {
                    if (stripe.getCenterOffsetStart() != null)
                    {
                        cseData.centerOffsetStart = stripe.getCenterOffsetStart();
                        startOffset = true;
                    }
                    if (stripe.getCenterOffsetEnd() != null)
                    {
                        cseData.centerOffsetEnd = stripe.getCenterOffsetEnd();
                        endOffset = true;
                    }
                }
                cseDataList.add(cseData);
            }
            else
            {
                org.opentrafficsim.xml.generated.CrossSectionElement cse =
                        (org.opentrafficsim.xml.generated.CrossSectionElement) o;
                CseData cseData = new CseData();
                cseData.widthStart = cse.getWidth() == null ? cse.getWidthStart() : cse.getWidth();
                Length halfWidthStart = cseData.widthStart.times(0.5);
                totalWidthStart = totalWidthStart.plus(cseData.widthStart);
                cseData.widthEnd = cse.getWidth() == null ? cse.getWidthEnd() : cse.getWidth();
                Length halfWidthEnd = cseData.widthEnd.times(0.5);
                totalWidthEnd = totalWidthEnd.plus(cseData.widthStart);

                if (cse.getCenterOffset() != null)
                {
                    cseData.centerOffsetStart = cse.getCenterOffset();
                    cseData.centerOffsetEnd = cse.getCenterOffset();
                    startOffset = true;
                    endOffset = true;
                }
                else if (cse.getLeftOffset() != null)
                {
                    cseData.centerOffsetStart = cse.getLeftOffset().minus(halfWidthStart);
                    cseData.centerOffsetEnd = cse.getLeftOffset().minus(halfWidthEnd);
                    startOffset = true;
                    endOffset = true;
                }
                else if (cse.getRightOffset() != null)
                {
                    cseData.centerOffsetStart = cse.getRightOffset().plus(halfWidthStart);
                    cseData.centerOffsetEnd = cse.getRightOffset().plus(halfWidthEnd);
                    startOffset = true;
                    endOffset = true;
                }

                if (cse.getCenterOffsetStart() != null)
                {
                    cseData.centerOffsetStart = cse.getCenterOffsetStart();
                    startOffset = true;
                }
                else if (cse.getLeftOffsetStart() != null)
                {
                    cseData.centerOffsetStart = cse.getLeftOffsetStart().minus(halfWidthStart);
                    startOffset = true;
                }
                else if (cse.getRightOffsetStart() != null)
                {
                    cseData.centerOffsetStart = cse.getRightOffsetStart().plus(halfWidthStart);
                    startOffset = true;
                }

                if (cse.getCenterOffsetEnd() != null)
                {
                    cseData.centerOffsetEnd = cse.getCenterOffsetEnd();
                    endOffset = true;
                }
                else if (cse.getLeftOffsetEnd() != null)
                {
                    cseData.centerOffsetEnd = cse.getLeftOffsetEnd().minus(halfWidthEnd);
                    endOffset = true;
                }
                else if (cse.getRightOffsetEnd() != null)
                {
                    cseData.centerOffsetEnd = cse.getRightOffsetEnd().plus(halfWidthEnd);
                    endOffset = true;
                }
                cseDataList.add(cseData);
            }
            cseTagMap.put(o, nr);
            nr++;
        }

        if (!startOffset)
        {
            cseDataList.get(0).centerOffsetStart = totalWidthStart.times(-0.5).minus(cseDataList.get(0).widthStart.times(-0.5));
        }
        if (!endOffset)
        {
            cseDataList.get(0).centerOffsetEnd = totalWidthEnd.times(-0.5).minus(cseDataList.get(0).widthEnd.times(-0.5));
        }

        // forward pass
        Length cs = null;
        Length es = null;
        for (CseData cseData : cseDataList)
        {
            if (cseData.centerOffsetStart != null)
            {
                cs = cseData.centerOffsetStart.plus(cseData.widthStart.times(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.plus(cseData.widthStart.times(0.5));
                    cs = cs.plus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.plus(cseData.widthEnd.times(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.plus(cseData.widthEnd.times(0.5));
                    es = es.plus(cseData.widthEnd);
                }
            }
        }

        // backward pass
        cs = null;
        es = null;
        for (int i = cseDataList.size() - 1; i >= 0; i--)
        {
            CseData cseData = cseDataList.get(i);
            if (cseData.centerOffsetStart != null)
            {
                cs = cseData.centerOffsetStart.minus(cseData.widthStart.times(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.minus(cseData.widthStart.times(0.5));
                    cs = cs.minus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.minus(cseData.widthEnd.times(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.minus(cseData.widthEnd.times(0.5));
                    es = es.minus(cseData.widthEnd);
                }
            }
        }

    }

    /**
     * Parse a stripe on a road.
     * @param csl CrossSectionLink; the CrossSectionLine
     * @param designLine ContinuousLine; design line.
     * @param startOffset Length; the offset of the start node
     * @param endOffset Length; the offset of the end node
     * @param stripeTag CseStripe; the CseStripe tag in the XML file
     * @param cseList List&lt;CrossSectionElement&gt;; the list of CrossSectionElements to which the stripes should be added
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final ContinuousLine designLine, final Length startOffset,
            final Length endOffset, final CseStripe stripeTag, final List<CrossSectionElement> cseList)
            throws OtsGeometryException, NetworkException, XmlParserException
    {
        Length width =
                stripeTag.getDrawingWidth() != null ? stripeTag.getDrawingWidth() : (stripeTag.getType().equals(Type.BLOCK)
                        ? new Length(40.0, LengthUnit.CENTIMETER) : new Length(20.0, LengthUnit.CENTIMETER));
        List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, startOffset, endOffset, width, width);

        NumSegments numSegments64 = new NumSegments(64);
        PolyLine2d centerLine = designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), numSegments64);
        PolyLine2d leftEdge = designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), numSegments64);
        PolyLine2d rightEdge =
                designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), numSegments64);
        Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);

        cseList.add(new Stripe(stripeTag.getType(), csl, new OtsLine2d(centerLine), contour, slices));
    }

    /** contains information about the lanes and stripes to calculate the offset. */
    protected static class CseData
    {
        /** the start width of the element (stripes are defined as 0). */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length widthStart;

        /** the end width of the element (stripes are defined as 0). */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length widthEnd;

        /** the start offset of the element. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length centerOffsetStart;

        /** the end offset of the element. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        public Length centerOffsetEnd;

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "CSEData [widthStart=" + this.widthStart + ", widthEnd=" + this.widthEnd + ", centerOffsetStart="
                    + this.centerOffsetStart + ", centerOffsetEnd=" + this.centerOffsetEnd + "]";
        }
    }

}
