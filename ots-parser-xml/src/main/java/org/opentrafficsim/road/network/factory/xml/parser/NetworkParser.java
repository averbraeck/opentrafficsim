package org.opentrafficsim.road.network.factory.xml.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousClothoid;
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
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets;
import org.opentrafficsim.road.network.factory.xml.utils.RoadLayoutOffsets.CseData;
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
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder.FixedWidthGenerator;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder.RelativeWidthGenerator;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder.WidthGenerator;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
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
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * @param eval Eval; expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final RoadNetwork otsNetwork, final Network network,
            final Map<String, Direction> nodeDirections, final Eval eval) throws NetworkException
    {
        for (org.opentrafficsim.xml.generated.Centroid xmlCentroid : network.getCentroid())
        {
            new Centroid(otsNetwork, xmlCentroid.getId(), xmlCentroid.getCoordinate().get(eval));
        }
        for (org.opentrafficsim.xml.generated.Node xmlNode : network.getNode())
        {
            new Node(otsNetwork, xmlNode.getId(), xmlNode.getCoordinate().get(eval), nodeDirections.get(xmlNode.getId()));
        }
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the Straight Link elements in
     * the XML file.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param network Network; the Network tag
     * @param eval Eval; expression evaluator.
     * @return a map of nodes and their default direction
     */
    public static Map<String, Direction> calculateNodeAngles(final RoadNetwork otsNetwork, final Network network,
            final Eval eval)
    {
        Map<String, Direction> nodeDirections = new LinkedHashMap<>();
        Map<String, Point2d> points = new LinkedHashMap<>();
        for (org.opentrafficsim.xml.generated.Node xmlNode : network.getNode())
        {
            if (xmlNode.getDirection() != null)
            {
                nodeDirections.put(xmlNode.getId(), xmlNode.getDirection().get(eval));
            }
            points.put(xmlNode.getId(), xmlNode.getCoordinate().get(eval));
        }

        for (Link xmlLink : network.getLink())
        {
            if (xmlLink.getStraight() != null)
            {
                Point2d startPoint = points.get(xmlLink.getNodeStart().get(eval));
                Point2d endPoint = points.get(xmlLink.getNodeEnd().get(eval));
                double direction = Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);
                if (!nodeDirections.containsKey(xmlLink.getNodeStart().get(eval)))
                {
                    nodeDirections.put(xmlLink.getNodeStart().get(eval), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(xmlLink.getNodeEnd().get(eval)))
                {
                    nodeDirections.put(xmlLink.getNodeEnd().get(eval), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }

        for (org.opentrafficsim.xml.generated.Node xmlNode : network.getNode())
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
     * @param eval Eval; expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     */
    static void parseLinks(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final Map<String, Direction> nodeDirections, final OtsSimulatorInterface simulator,
            final Map<String, ContinuousLine> designLines, final Eval eval) throws NetworkException, OtsGeometryException
    {
        for (org.opentrafficsim.xml.generated.Connector xmlConnector : network.getConnector())
        {
            String nodeId = xmlConnector.getNode().get(eval);
            Node node = (Node) otsNetwork.getNode(nodeId);
            if (null == node)
            {
                simulator.getLogger().always().debug("No node (" + nodeId + ") for Connector " + xmlConnector.getId());
            }
            String centroidId = xmlConnector.getCentroid().get(eval);
            Node centroid = (Node) otsNetwork.getNode(centroidId);
            if (null == centroid)
            {
                simulator.getLogger().always().debug("No centroid (" + centroidId + ") for Connector " + xmlConnector.getId());
            }
            String id = xmlConnector.getId();
            double demandWeight = xmlConnector.getDemandWeight().get(eval);
            LinkType linkType = definitions.get(LinkType.class, xmlConnector.getType().get(eval));
            Connector link = xmlConnector.getOutbound().get(eval) ? new Connector(otsNetwork, id, centroid, node, linkType)
                    : new Connector(otsNetwork, id, node, centroid, linkType);
            link.setDemandWeight(demandWeight);
        }

        for (Link xmlLink : network.getLink())
        {
            Node startNode = (Node) otsNetwork.getNode(xmlLink.getNodeStart().get(eval));
            Node endNode = (Node) otsNetwork.getNode(xmlLink.getNodeEnd().get(eval));
            Point2d startPoint = startNode.getPoint();
            Point2d endPoint = endNode.getPoint();
            Point2d[] coordinates = null;

            // start and end with heading, adjusted with offset
            double startHeading = startNode.getHeading().si;
            OrientedPoint2d start = new OrientedPoint2d(startPoint.x, startPoint.y, startHeading);
            if (xmlLink.getOffsetStart() != null)
            {
                start = OtsGeometryUtil.offsetPoint(start, xmlLink.getOffsetStart().get(eval).si);
            }
            double endHeading = endNode.getHeading().si;
            OrientedPoint2d end = new OrientedPoint2d(endPoint.x, endPoint.y, endHeading);
            if (xmlLink.getOffsetEnd() != null)
            {
                end = OtsGeometryUtil.offsetPoint(end, xmlLink.getOffsetEnd().get(eval).si);
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
                    coordinates[p + 1] = xmlLink.getPolyline().getCoordinate().get(p).get(eval);
                }
                designLine = new ContinuousPolyLine(new PolyLine2d(true, coordinates));
            }
            else if (xmlLink.getArc() != null)
            {
                if (xmlLink.getArc().getNumSegments() != null)
                {
                    numSegments = xmlLink.getArc().getNumSegments().intValue();
                }
                double radius = xmlLink.getArc().getRadius().get(eval).si;
                boolean left = xmlLink.getArc().getDirection().get(eval).equals(ArcDirection.LEFT);
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
                double shape = xmlLink.getBezier().getShape().get(eval);
                boolean weighted = xmlLink.getBezier().isWeighted();
                Point2d[] designPoints = Bezier.cubicControlPoints(start, end, shape, weighted);
                designLine = new ContinuousBezierCubic(designPoints[0], designPoints[1], designPoints[2], designPoints[3]);
            }
            else if (xmlLink.getClothoid() != null)
            {
                if (xmlLink.getClothoid().getNumSegments() != null)
                {
                    numSegments = xmlLink.getClothoid().getNumSegments().intValue();
                }
                // fields in getClothoid() appear as lists as StartCurvature and EndCurvature appear in multiple options
                if (!xmlLink.getClothoid().getInterpolated().isEmpty())
                {
                    designLine = new ContinuousClothoid(start, end);
                }
                else
                {
                    LinearDensity startCurvature = xmlLink.getClothoid().getStartCurvature().get(0).get(eval);
                    LinearDensity endCurvature = xmlLink.getClothoid().getEndCurvature().get(0).get(eval);
                    if (!xmlLink.getClothoid().getLength().isEmpty())
                    {
                        Length length = xmlLink.getClothoid().getLength().get(0).get(eval);
                        designLine = ContinuousClothoid.withLength(start, length.si, startCurvature.si, endCurvature.si);
                    }
                    else
                    {
                        Throw.when(xmlLink.getClothoid().getA().isEmpty(), NetworkException.class,
                                "Clothoid for link %s is not correctly specified.", xmlLink.getId());
                        Length a = xmlLink.getClothoid().getA().get(0).get(eval);
                        designLine = new ContinuousClothoid(start, a.si, startCurvature.si, endCurvature.si);
                    }
                }
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getId()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }
            designLines.put(xmlLink.getId(), designLine);

            // TODO: take flattening defaults from network when not defined for link
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

            LaneKeepingPolicy laneKeepingPolicy = xmlLink.getLaneKeeping().get(eval);
            LinkType linkType = definitions.get(LinkType.class, xmlLink.getType().get(eval));
            // TODO: elevation data
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getId(), startNode, endNode, linkType,
                    new OtsLine2d(flattenedLine), null, laneKeepingPolicy);

            if (xmlLink.getPriority() != null)
            {
                Priority priority = xmlLink.getPriority().get(eval);
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
     * @param eval Eval; expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     */
    static void applyRoadLayout(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final OtsSimulatorInterface simulator, final Map<String, RoadLayout> roadLayoutMap,
            final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap, final Map<String, ContinuousLine> designLines,
            final Eval eval)
            throws NetworkException, OtsGeometryException, XmlParserException, SimRuntimeException, GtuException
    {
        for (Link xmlLink : network.getLink())
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
                String definedLayoutId = xmlLink.getDefinedLayout().get(eval);
                RoadLayout roadLayoutTagBase = roadLayoutMap.get(definedLayoutId);
                Throw.when(roadLayoutTagBase == null, XmlParserException.class, "Link %s Could not find defined RoadLayout %s",
                        xmlLink.getId(), definedLayoutId);
                // Process LaneOverrides
                roadLayoutTag = Cloner.cloneRoadLayout(roadLayoutTagBase);
                for (LaneOverride laneOverride : xmlLink.getLaneOverride())
                {
                    for (CseLane lane : ParseUtil.getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), CseLane.class))
                    {
                        if (lane.getId().equals(laneOverride.getLane().get(eval)))
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
            RoadLayoutOffsets.calculateOffsets(roadLayoutTag, cseDataList, cseTagMap, eval);

            // Stripe
            ContinuousLine designLine = designLines.get(xmlLink.getId());
            for (CseStripe stripeTag : ParseUtil.getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), CseStripe.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, designLine, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList, eval);
            }

            // Other CrossSectionElement
            for (org.opentrafficsim.xml.generated.CrossSectionElement cseTag : ParseUtil.getObjectsOfType(
                    roadLayoutTag.getStripeOrLaneOrShoulder(), org.opentrafficsim.xml.generated.CrossSectionElement.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(cseTag));

                List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, cseData.centerOffsetStart,
                        cseData.centerOffsetEnd, cseData.widthStart, cseData.widthEnd);
                // TODO: use flattening of link, or global; probably need to create a map in parseLinks(...)
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
                    LaneType laneType = definitions.get(LaneType.class, laneTag.getLaneType().get(eval));
                    Map<GtuType, Speed> speedLimitMap = new LinkedHashMap<>();
                    LinkType linkType = csl.getType();
                    speedLimitMap.putAll(linkTypeSpeedLimitMap.computeIfAbsent(linkType, (l) -> new LinkedHashMap<>()));
                    for (SpeedLimit speedLimitTag : roadLayoutTag.getSpeedLimit())
                    {
                        GtuType gtuType = definitions.get(GtuType.class, speedLimitTag.getGtuType().get(eval));
                        speedLimitMap.put(gtuType, speedLimitTag.getLegalSpeedLimit().get(eval));
                    }
                    for (SpeedLimit speedLimitTag : laneTag.getSpeedLimit())
                    {
                        GtuType gtuType = definitions.get(GtuType.class, speedLimitTag.getGtuType().get(eval));
                        speedLimitMap.put(gtuType, speedLimitTag.getLegalSpeedLimit().get(eval));
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
                String laneId = trafficLight.getLane().get(eval);
                Throw.when(!lanes.containsKey(laneId), NetworkException.class,
                        "Link: %s, TrafficLight with id %s on Lane %s - Lane not found", xmlLink.getId(), trafficLight.getId(),
                        laneId);
                Lane lane = lanes.get(laneId);
                Length position = ParseUtil.parseLengthBeginEnd(trafficLight.getPosition().get(eval), lane.getLength());
                try
                {
                    @SuppressWarnings("unchecked")
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getClazz().get(eval),
                            new Class[] {String.class, Lane.class, Length.class, OtsSimulatorInterface.class});
                    trafficLightConstructor.newInstance(new Object[] {trafficLight.getId(), lane, position, simulator});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException("TrafficLight: Class Name " + trafficLight.getClazz().get(eval).getName()
                            + " for traffic light " + trafficLight.getId() + " on lane " + lane.toString() + " at position "
                            + position + " -- class not found or constructor not right", exception);
                    // TODO: this discards too much information; e.g. Network already contains an object with the name ...
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
     * @param eval Eval; expression evaluator.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final ContinuousLine designLine, final Length startOffset,
            final Length endOffset, final CseStripe stripeTag, final List<CrossSectionElement> cseList, final Eval eval)
            throws OtsGeometryException, NetworkException, XmlParserException
    {
        Type type = stripeTag.getType().get(eval);
        Length width = stripeTag.getDrawingWidth() != null ? stripeTag.getDrawingWidth().get(eval) : type.defaultWidth();
        List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, startOffset, endOffset, width, width);

        // TODO: use flattening of link, or global; probably need to create a map in parseLinks(...)
        NumSegments numSegments64 = new NumSegments(64);
        PolyLine2d centerLine = designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), numSegments64);
        PolyLine2d leftEdge = designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), numSegments64);
        PolyLine2d rightEdge =
                designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), numSegments64);
        Polygon2d contour = LaneGeometryUtil.getContour(leftEdge, rightEdge);

        cseList.add(new Stripe(type, csl, new OtsLine2d(centerLine), contour, slices));
    }

    /**
     * Build conflicts.
     * @param otsNetwork RoadNetwork; the network to insert the parsed objects in
     * @param network Network; the Network tag
     * @param eval Eval; expression evaluator.
     * @throws OtsGeometryException if building conflicts fails
     * @throws XmlParserException if Conflicts tag contains no valid element
     */
    static void buildConflicts(final RoadNetwork otsNetwork, final Network network, final Eval eval)
            throws OtsGeometryException, XmlParserException
    {
        if (network.getConflicts() != null && network.getConflicts().getNone() == null)
        {
            WidthGenerator widthGenerator;
            if (network.getConflicts().getFixedWidth() != null)
            {
                widthGenerator = new FixedWidthGenerator(network.getConflicts().getFixedWidth().get(eval));
            }
            else if (network.getConflicts().getRelativeWidth() != null)
            {
                widthGenerator = new RelativeWidthGenerator(network.getConflicts().getRelativeWidth().get(eval));
            }
            else if (network.getConflicts().getDefaultWidth() != null)
            {
                widthGenerator = new FixedWidthGenerator(Length.instantiateSI(2.0));
            }
            else
            {
                throw new XmlParserException("Conflicts tag contains no valid element.");
            }

            otsNetwork.getSimulator().getLogger().always().info("Generating conflicts");
            Map<String, Set<org.opentrafficsim.core.network.Link>> conflictCandidateMap = new LinkedHashMap<>();
            for (Link link : network.getLink())
            {
                if (link.getConflictId() != null)
                {
                    if (!conflictCandidateMap.containsKey(link.getConflictId().get(eval)))
                    {
                        conflictCandidateMap.put(link.getConflictId().get(eval), new LinkedHashSet<>());
                    }
                    conflictCandidateMap.get(link.getConflictId().get(eval)).add(otsNetwork.getLink(link.getId()));
                }
            }
            otsNetwork.getSimulator().getLogger().always().info("Map size of conflict candidate regions = {}",
                    conflictCandidateMap.size());

            // TODO: if there is any conflict ID specified, conflictCandidateMap is filled, and no other conflict anywhere will
            // be generated. How can we combine generation and specifying conflict IDs?
            // TODO: specify where conflicts are directly?
            if (conflictCandidateMap.size() == 0)
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, otsNetwork.getSimulator(), widthGenerator);
            }
            else
            {
                ConflictBuilder.buildConflictsParallel(otsNetwork, conflictCandidateMap, otsNetwork.getSimulator(),
                        widthGenerator);
            }
            otsNetwork.getSimulator().getLogger().always().info("Object map size = {}", otsNetwork.getObjectMap().size());
        }
    }

}
