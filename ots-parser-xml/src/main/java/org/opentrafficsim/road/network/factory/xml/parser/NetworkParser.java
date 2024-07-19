package org.opentrafficsim.road.network.factory.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.djunits.unit.DirectionUnit;
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
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.ContinuousBezierCubic;
import org.opentrafficsim.core.geometry.ContinuousClothoid;
import org.opentrafficsim.core.geometry.ContinuousLine;
import org.opentrafficsim.core.geometry.ContinuousPolyLine;
import org.opentrafficsim.core.geometry.ContinuousStraight;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.Flattener.MaxAngle;
import org.opentrafficsim.core.geometry.Flattener.MaxDeviation;
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
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
import org.opentrafficsim.xml.bindings.types.StringType;
import org.opentrafficsim.xml.generated.BasicRoadLayout;
import org.opentrafficsim.xml.generated.CseLane;
import org.opentrafficsim.xml.generated.CseShoulder;
import org.opentrafficsim.xml.generated.CseStripe;
import org.opentrafficsim.xml.generated.FlattenerType;
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
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param designLines Map&lt;String, ContinuousLine&gt;; map to store created design lines.
     * @param flatteners Map&lt;String, Flattener&gt;; flattener per link id.
     * @param eval Eval; expression evaluator.
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     */
    static void parseLinks(final RoadNetwork otsNetwork, final Definitions definitions, final Network network,
            final Map<String, Direction> nodeDirections, final OtsSimulatorInterface simulator,
            final Map<String, ContinuousLine> designLines, final Map<String, Flattener> flatteners, final Eval eval)
            throws NetworkException, OtsGeometryException
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

        Flattener defaultFlattener =
                network.getFlattener() == null ? new NumSegments(64) : getFlattener(null, network.getFlattener(), eval);
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
            Flattener flattener = defaultFlattener;
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
                flattener = getFlattener(defaultFlattener, xmlLink.getArc().getFlattener(), eval);
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
                flattener = getFlattener(defaultFlattener, xmlLink.getBezier().getFlattener(), eval);
                double shape = xmlLink.getBezier().getShape().get(eval);
                boolean weighted = xmlLink.getBezier().isWeighted();
                Point2d[] designPoints = Bezier.cubicControlPoints(start, end, shape, weighted);
                designLine = new ContinuousBezierCubic(designPoints[0], designPoints[1], designPoints[2], designPoints[3]);
            }
            else if (xmlLink.getClothoid() != null)
            {
                FlattenerType flattenerType =
                        xmlLink.getClothoid().getFlattener().isEmpty() ? null : xmlLink.getClothoid().getFlattener().get(0);
                flattener = getFlattener(defaultFlattener, flattenerType, eval);
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
            flatteners.put(xmlLink.getId(), flattener);

            PolyLine2d flattenedLine = designLine.flatten(flattener);
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
     * @param flatteners Map&lt;String, Flattener&gt;; flattener per link id.
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
            final Map<String, Flattener> flatteners, final Eval eval)
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
            Flattener flattener = flatteners.get(xmlLink.getId());
            for (CseStripe stripeTag : ParseUtil.getObjectsOfType(roadLayoutTag.getStripeOrLaneOrShoulder(), CseStripe.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, designLine, flattener, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList,
                        eval);
            }

            // Other CrossSectionElement
            for (org.opentrafficsim.xml.generated.CrossSectionElement cseTag : ParseUtil.getObjectsOfType(
                    roadLayoutTag.getStripeOrLaneOrShoulder(), org.opentrafficsim.xml.generated.CrossSectionElement.class))
            {
                CseData cseData = cseDataList.get(cseTagMap.get(cseTag));

                List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, cseData.centerOffsetStart,
                        cseData.centerOffsetEnd, cseData.widthStart, cseData.widthEnd);
                PolyLine2d centerLine =
                        designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), flattener);
                PolyLine2d leftEdge =
                        designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), flattener);
                PolyLine2d rightEdge =
                        designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), flattener);
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

                // Shoulder
                else if (cseTag instanceof CseShoulder)
                {
                    CseShoulder shoulderTag = (CseShoulder) cseTag;
                    // FIXME:  laneType is a mandatory field, but parsing or eclipse is not taking the new field
                    // although specified in XML, the value is always null
                    LaneType laneType = shoulderTag.getLaneType() == null ? SHOULDER
                            : definitions.get(LaneType.class, shoulderTag.getLaneType().get(eval));
                    String id = shoulderTag.getId() != null ? shoulderTag.getId() : UUID.randomUUID().toString();
                    CrossSectionElement shoulder = new Shoulder(csl, id, new OtsLine2d(centerLine), contour, slices, laneType);
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
                TrafficLight obj = new TrafficLight(trafficLight.getId(), lane, position, simulator);
                for (StringType nodeId : trafficLight.getTurnOnRed())
                {
                    obj.addTurnOnRed(otsNetwork.getNode(nodeId.get(eval)));
                }
            }
        }
    }
    
    /** Temporary fix for CseShoulder.getLaneType() always being null. */
    // FIXME
    private static final LaneType SHOULDER = new LaneType("Shoulder");

    /**
     * Parse a stripe on a road.
     * @param csl CrossSectionLink; the CrossSectionLine
     * @param designLine ContinuousLine; design line.
     * @param flattener Flattener; flattener.
     * @param startOffset Length; the offset of the start node
     * @param endOffset Length; the offset of the end node
     * @param stripeTag CseStripe; the CseStripe tag in the XML file
     * @param cseList List&lt;CrossSectionElement&gt;; the list of CrossSectionElements to which the stripes should be added
     * @param eval Eval; expression evaluator.
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final ContinuousLine designLine, final Flattener flattener,
            final Length startOffset, final Length endOffset, final CseStripe stripeTag,
            final List<CrossSectionElement> cseList, final Eval eval)
            throws OtsGeometryException, NetworkException, XmlParserException
    {
        Type type = stripeTag.getType().get(eval);
        Length width = stripeTag.getDrawingWidth() != null ? stripeTag.getDrawingWidth().get(eval) : type.defaultWidth();
        List<CrossSectionSlice> slices = LaneGeometryUtil.getSlices(designLine, startOffset, endOffset, width, width);

        PolyLine2d centerLine = designLine.flattenOffset(LaneGeometryUtil.getCenterOffsets(designLine, slices), flattener);
        PolyLine2d leftEdge = designLine.flattenOffset(LaneGeometryUtil.getLeftEdgeOffsets(designLine, slices), flattener);
        PolyLine2d rightEdge = designLine.flattenOffset(LaneGeometryUtil.getRightEdgeOffsets(designLine, slices), flattener);
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

    /**
     * Returns a flattener. If the flattener tag is empty, the flattener is obtained from the network.
     * @param defaultFlattener Flattener; default flattener from the network, may be {@code null} to parse default flattener.
     * @param flattenerType FlattenerType; XML tag of flattener.
     * @param eval Eval; evaluator.
     * @return Flattener.
     * @throws NetworkException if the flattener is not correctly defined.
     */
    private static Flattener getFlattener(final Flattener defaultFlattener, final FlattenerType flattenerType, final Eval eval)
            throws NetworkException
    {
        if (flattenerType == null)
        {
            return defaultFlattener;
        }
        if (flattenerType.getNumSegments() != null)
        {
            return new NumSegments(flattenerType.getNumSegments().get(eval));
        }
        else if (flattenerType.getDeviationAndAngle() != null)
        {
            if (flattenerType.getDeviationAndAngle().getMaxDeviation() != null)
            {
                if (flattenerType.getDeviationAndAngle().getMaxAngle() != null)
                {
                    return new MaxDeviationAndAngle(
                            getDeviation(flattenerType.getDeviationAndAngle().getMaxDeviation().get(eval)),
                            getAngle(flattenerType.getDeviationAndAngle().getMaxAngle().get(eval)));
                }
                return new MaxDeviation(getDeviation(flattenerType.getDeviationAndAngle().getMaxDeviation().get(eval)));
            }
            else if (flattenerType.getDeviationAndAngle().getMaxAngle() != null)
            {
                return new MaxAngle(getAngle(flattenerType.getDeviationAndAngle().getMaxAngle().get(eval)));
            }
            throw new NetworkException("No deviation and/or angle for flattener specified.");
        }
        throw new NetworkException("No flattener specified.");
    }

    /**
     * Returns a safe deviation value (>=0.001).
     * @param length Length; deviation.
     * @return double; safe deviation.
     */
    private static double getDeviation(final Length length)
    {
        return length.si < 0.001 ? 0.001 : length.si;
    }

    /**
     * Returns a safe angle value (>=0.01).
     * @param angle Length; angle.
     * @return double; safe angle.
     */
    private static double getAngle(final Angle angle)
    {
        return angle.si < 0.01 ? 0.01 : angle.si;
    }

}
