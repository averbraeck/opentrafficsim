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
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.Point3d;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.utils.Cloner;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.road.network.factory.xml.utils.Transformer;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OtsRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.xml.bindings.types.ArcDirection;
import org.opentrafficsim.xml.generated.BASICROADLAYOUT;
import org.opentrafficsim.xml.generated.CONNECTOR;
import org.opentrafficsim.xml.generated.CROSSSECTIONELEMENT;
import org.opentrafficsim.xml.generated.CSELANE;
import org.opentrafficsim.xml.generated.CSENOTRAFFICLANE;
import org.opentrafficsim.xml.generated.CSESHOULDER;
import org.opentrafficsim.xml.generated.CSESTRIPE;
import org.opentrafficsim.xml.generated.LINK;
import org.opentrafficsim.xml.generated.LINK.LANEOVERRIDE;
import org.opentrafficsim.xml.generated.NETWORK;
import org.opentrafficsim.xml.generated.NODE;
import org.opentrafficsim.xml.generated.ROADLAYOUT;
import org.opentrafficsim.xml.generated.SPEEDLIMIT;
import org.opentrafficsim.xml.generated.TRAFFICLIGHTTYPE;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * NetworkParser parses the NETWORK tag of the OTS network.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @param nodeDirections Map&lt;String,Direction&gt;; a map of the node ids and their default directions
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final OtsRoadNetwork otsNetwork, final NETWORK network,
            final Map<String, Direction> nodeDirections) throws NetworkException
    {
        for (NODE xmlNode : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), NODE.class))
        {
            new OtsRoadNode(otsNetwork, xmlNode.getID(),
                    new OtsPoint3D(xmlNode.getCOORDINATE().x, xmlNode.getCOORDINATE().y, xmlNode.getCOORDINATE().z),
                    nodeDirections.get(xmlNode.getID()));
        }
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the STRAIGHT LINK elements in
     * the XML file.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @return a map of nodes and their default direction
     */
    public static Map<String, Direction> calculateNodeAngles(final OtsRoadNetwork otsNetwork, final NETWORK network)
    {
        Map<String, Direction> nodeDirections = new LinkedHashMap<>();
        Map<String, Point3d> points = new LinkedHashMap<>();
        for (NODE xmlNode : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), NODE.class))
        {
            if (xmlNode.getDIRECTION() != null)
            {
                nodeDirections.put(xmlNode.getID(), xmlNode.getDIRECTION());
            }
            points.put(xmlNode.getID(), xmlNode.getCOORDINATE());
        }

        for (LINK xmlLink : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), LINK.class))
        {
            if (xmlLink.getSTRAIGHT() != null)
            {
                Point3d startPoint = points.get(xmlLink.getNODESTART());
                Point3d endPoint = points.get(xmlLink.getNODEEND());
                double direction = Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);
                if (!nodeDirections.containsKey(xmlLink.getNODESTART()))
                {
                    nodeDirections.put(xmlLink.getNODESTART(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(xmlLink.getNODEEND()))
                {
                    nodeDirections.put(xmlLink.getNODEEND(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }

        for (NODE xmlNode : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), NODE.class))
        {
            if (!nodeDirections.containsKey(xmlNode.getID()))
            {
                System.err.println("Warning: Node " + xmlNode.getID() + " does not have a (calculated) direction");
            }
        }

        return nodeDirections;
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @param nodeDirections Map&lt;String,Direction&gt;; a map of the node ids and their default directions
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     */
    static void parseLinks(final OtsRoadNetwork otsNetwork, final NETWORK network, final Map<String, Direction> nodeDirections,
            final OtsSimulatorInterface simulator) throws NetworkException, OtsGeometryException
    {
        for (CONNECTOR xmlConnector : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), CONNECTOR.class))
        {
            OtsRoadNode startNode = (OtsRoadNode) otsNetwork.getNode(xmlConnector.getNODESTART());
            if (null == startNode)
            {
                simulator.getLogger().always()
                        .debug("No start node (" + xmlConnector.getNODESTART() + ") for CONNECTOR " + xmlConnector.getID());
            }
            OtsRoadNode endNode = (OtsRoadNode) otsNetwork.getNode(xmlConnector.getNODEEND());
            if (null == endNode)
            {
                simulator.getLogger().always()
                        .debug("No end node (" + xmlConnector.getNODEEND() + ")for CONNECTOR " + xmlConnector.getID());
            }
            String id = xmlConnector.getID();
            double demandWeight = xmlConnector.getDEMANDWEIGHT();
            OtsLine3D designLine = new OtsLine3D(startNode.getPoint(), endNode.getPoint());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, id, startNode, endNode,
                    otsNetwork.getLinkType(LinkType.DEFAULTS.CONNECTOR), designLine, null);
            link.setDemandWeight(demandWeight);
        }

        for (LINK xmlLink : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), LINK.class))
        {
            OtsRoadNode startNode = (OtsRoadNode) otsNetwork.getNode(xmlLink.getNODESTART());
            OtsRoadNode endNode = (OtsRoadNode) otsNetwork.getNode(xmlLink.getNODEEND());
            double startDirection =
                    nodeDirections.containsKey(startNode.getId()) ? nodeDirections.get(startNode.getId()).getSI() : 0.0;
            double endDirection =
                    nodeDirections.containsKey(endNode.getId()) ? nodeDirections.get(endNode.getId()).getSI() : 0.0;
            OtsPoint3D startPoint = new OtsPoint3D(startNode.getPoint());
            OtsPoint3D endPoint = new OtsPoint3D(endNode.getPoint());
            OtsPoint3D[] coordinates = null;

            if (xmlLink.getSTRAIGHT() != null)
            {
                coordinates = new OtsPoint3D[2];
                coordinates[0] = startPoint;
                coordinates[1] = endPoint;
            }

            else if (xmlLink.getPOLYLINE() != null)
            {
                int intermediatePoints = xmlLink.getPOLYLINE().getCOORDINATE().size();
                coordinates = new OtsPoint3D[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = new OtsPoint3D(xmlLink.getPOLYLINE().getCOORDINATE().get(p));
                }

            }
            else if (xmlLink.getARC() != null)
            {
                // calculate the center position
                double radiusSI = xmlLink.getARC().getRADIUS().getSI();
                double offsetStart = 0.0;
                if (xmlLink.getOFFSETSTART() != null)
                {
                    offsetStart = xmlLink.getOFFSETSTART().si;
                }
                double offsetEnd = 0.0;
                if (xmlLink.getOFFSETEND() != null)
                {
                    offsetEnd = xmlLink.getOFFSETEND().si;
                }
                List<OtsPoint3D> centerList = OtsPoint3D.circleIntersections(startNode.getPoint(), radiusSI + offsetStart,
                        endNode.getPoint(), radiusSI + offsetEnd);
                OtsPoint3D center =
                        (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT)) ? centerList.get(0) : centerList.get(1);

                // calculate start angle and end angle
                double sa = Math.atan2(startNode.getPoint().y - center.y, startNode.getPoint().x - center.x);
                double ea = Math.atan2(endNode.getPoint().y - center.y, endNode.getPoint().x - center.x);
                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    // right -> negative direction, ea should be less than sa
                    ea = (sa < ea) ? ea + Math.PI * 2.0 : ea;
                }
                else
                {
                    // left -> positive direction, sa should be less than ea
                    ea = (ea < sa) ? ea + Math.PI * 2.0 : ea;
                }

                int numSegments = xmlLink.getARC().getNUMSEGMENTS().intValue();
                coordinates = new OtsPoint3D[numSegments];
                coordinates[0] = new OtsPoint3D(startNode.getPoint().x + Math.cos(sa) * offsetStart,
                        startNode.getPoint().y + Math.sin(sa) * offsetStart, startNode.getPoint().z);
                coordinates[coordinates.length - 1] = new OtsPoint3D(endNode.getPoint().x + Math.cos(ea) * offsetEnd,
                        endNode.getPoint().y + Math.sin(ea) * offsetEnd, endNode.getPoint().z);
                double angleStep = Math.abs((ea - sa)) / numSegments;
                double slopeStep = (endNode.getPoint().z - startNode.getPoint().z) / numSegments;

                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < numSegments - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / numSegments;
                        coordinates[p] = new OtsPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa - angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa - angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < numSegments - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / numSegments;
                        coordinates[p] = new OtsPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa + angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa + angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
            }

            else if (xmlLink.getBEZIER() != null)
            {
                int numSegments = xmlLink.getBEZIER().getNUMSEGMENTS().intValue();
                double shape = xmlLink.getBEZIER().getSHAPE().doubleValue();
                boolean weighted = xmlLink.getBEZIER().isWEIGHTED();
                if (xmlLink.getBEZIER().getSTARTDIRECTION() != null)
                {
                    startDirection = xmlLink.getBEZIER().getSTARTDIRECTION().getSI();
                }
                if (xmlLink.getBEZIER().getENDDIRECTION() != null)
                {
                    endDirection = xmlLink.getBEZIER().getENDDIRECTION().getSI();
                }
                coordinates = Bezier
                        .cubic(numSegments, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startDirection),
                                new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endDirection), shape, weighted)
                        .getPoints();
            }

            else if (xmlLink.getCLOTHOID() != null)
            {
                // int numSegments = xmlLink.getCLOTHOID().getNUMSEGMENTS().intValue();

                // TODO: Clothoid parsing
            }

            else
            {
                throw new NetworkException("Making link, but link " + xmlLink.getID()
                        + " has no filled straight, arc, bezier, polyline, or clothoid definition");
            }

            OtsLine3D designLine = OtsLine3D.createAndCleanOTSLine3D(coordinates);

            // TODO: Directionality has to be added later when the lanes and their direction are known.
            LaneKeepingPolicy laneKeepingPolicy = LaneKeepingPolicy.valueOf(xmlLink.getLANEKEEPING().name());
            LinkType linkType = otsNetwork.getLinkType(xmlLink.getTYPE());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getID(), startNode, endNode, linkType, designLine,
                    laneKeepingPolicy);

            if (xmlLink.getPRIORITY() != null)
            {
                Priority priority = Priority.valueOf(xmlLink.getPRIORITY());
                link.setPriority(priority);
            }
        }
    }

    /**
     * Build the links with the correct design line.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @param simulator OTSSimulatorInterface; the simulator
     * @param roadLayoutMap the map of the tags of the predefined ROADLAYOUT tags in DEFINITIONS
     * @param linkTypeSpeedLimitMap map of speed limits per link type
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OtsGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GtuException when construction of the Strategical Planner failed
     */
    static void applyRoadLayout(final OtsRoadNetwork otsNetwork, final NETWORK network, final OtsSimulatorInterface simulator,
            final Map<String, ROADLAYOUT> roadLayoutMap, final Map<LinkType, Map<GtuType, Speed>> linkTypeSpeedLimitMap)
            throws NetworkException, OtsGeometryException, XmlParserException, SimRuntimeException, GtuException
    {
        for (LINK xmlLink : ParseUtil.getObjectsOfType(network.getIncludeOrNODEOrCONNECTOR(), LINK.class))
        {
            CrossSectionLink csl = (CrossSectionLink) otsNetwork.getLink(xmlLink.getID());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new LinkedHashMap<>();

            // CategoryLogger.filter(Cat.PARSER).trace("Parse link: {}", xmlLink.getID());

            // Get the ROADLAYOUT (either defined here, or via pointer to DEFINITIONS)
            BASICROADLAYOUT roadLayoutTagBase;
            if (xmlLink.getDEFINEDLAYOUT() != null)
            {
                if (xmlLink.getROADLAYOUT() != null)
                {
                    throw new XmlParserException(
                            "Link " + xmlLink.getID() + " Ambiguous RoadLayout; both DEFINEDROADLAYOUT and ROADLAYOUT defined");
                }
                roadLayoutTagBase = roadLayoutMap.get(xmlLink.getDEFINEDLAYOUT());
                if (roadLayoutTagBase == null)
                {
                    throw new XmlParserException(
                            "Link " + xmlLink.getID() + " Could not find defined RoadLayout " + xmlLink.getDEFINEDLAYOUT());
                }
            }
            else
            {
                roadLayoutTagBase = xmlLink.getROADLAYOUT();
                if (roadLayoutTagBase == null)
                {
                    throw new XmlParserException("Link " + xmlLink.getID() + " No RoadLayout defined");
                }
            }

            // Process LANEOVERRIDEs
            BASICROADLAYOUT roadLayoutTag = Cloner.cloneRoadLayout(roadLayoutTagBase);
            for (LANEOVERRIDE laneOverride : xmlLink.getLANEOVERRIDE())
            {
                for (CSELANE lane : ParseUtil.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSELANE.class))
                {
                    if (lane.getID().equals(laneOverride.getLANE()))
                    {
                        if (laneOverride.getSPEEDLIMIT().size() > 0)
                        {
                            lane.getSPEEDLIMIT().clear();
                            lane.getSPEEDLIMIT().addAll(laneOverride.getSPEEDLIMIT());
                        }
                    }
                }
            }

            // calculate for each lane and stripe what the start and end offset is
            List<CSEData> cseDataList = new ArrayList<>();
            Map<Object, Integer> cseTagMap = new LinkedHashMap<>();
            calculateOffsets(roadLayoutTag, xmlLink, cseDataList, cseTagMap);
            boolean fixGradualLateralOffset = xmlLink.isFIXGRADUALOFFSET();

            // STRIPE
            for (CSESTRIPE stripeTag : ParseUtil.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(),
                    CSESTRIPE.class))
            {
                CSEData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList,
                        fixGradualLateralOffset);
            }

            // Other CROSSECTIONELEMENT
            for (CROSSSECTIONELEMENT cseTag : ParseUtil.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(),
                    CROSSSECTIONELEMENT.class))
            {
                CSEData cseData = cseDataList.get(cseTagMap.get(cseTag));

                // LANE
                if (cseTag instanceof CSELANE)
                {
                    CSELANE laneTag = (CSELANE) cseTag;
                    boolean direction = laneTag.isDESIGNDIRECTION();
                    LaneType laneType = otsNetwork.getLaneType(laneTag.getLANETYPE());
                    // TODO: Use the DESIGNDIRECTION
                    Map<GtuType, Speed> speedLimitMap = new LinkedHashMap<>();
                    LinkType linkType = csl.getLinkType();
                    if (!linkTypeSpeedLimitMap.containsKey(linkType))
                    {
                        linkTypeSpeedLimitMap.put(linkType, new LinkedHashMap<>());
                    }
                    speedLimitMap.putAll(linkTypeSpeedLimitMap.get(linkType));
                    for (SPEEDLIMIT speedLimitTag : roadLayoutTag.getSPEEDLIMIT())
                    {
                        GtuType gtuType = otsNetwork.getGtuType(speedLimitTag.getGTUTYPE());
                        speedLimitMap.put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                    }
                    for (SPEEDLIMIT speedLimitTag : laneTag.getSPEEDLIMIT())
                    {
                        GtuType gtuType = otsNetwork.getGtuType(speedLimitTag.getGTUTYPE());
                        speedLimitMap.put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                    }
                    Lane lane = new Lane(csl, laneTag.getID(), cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd, laneType, speedLimitMap, fixGradualLateralOffset);
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                }

                // NOTRAFFICLANE
                else if (cseTag instanceof CSENOTRAFFICLANE)
                {
                    CSENOTRAFFICLANE ntlTag = (CSENOTRAFFICLANE) cseTag;
                    String id = ntlTag.getID() != null ? ntlTag.getID() : UUID.randomUUID().toString();
                    Lane lane = new NoTrafficLane(csl, id, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd, fixGradualLateralOffset);
                    cseList.add(lane);
                }

                // SHOULDER
                else if (cseTag instanceof CSESHOULDER)
                {
                    CSESHOULDER shoulderTag = (CSESHOULDER) cseTag;
                    String id = shoulderTag.getID() != null ? shoulderTag.getID() : UUID.randomUUID().toString();
                    Shoulder shoulder = new Shoulder(csl, id, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd, fixGradualLateralOffset);
                    cseList.add(shoulder);
                }
            }

            // TRAFFICLIGHT
            for (TRAFFICLIGHTTYPE trafficLight : xmlLink.getTRAFFICLIGHT())
            {
                if (!lanes.containsKey(trafficLight.getLANE()))
                {
                    throw new NetworkException("LINK: " + xmlLink.getID() + ", TrafficLight with id " + trafficLight.getID()
                            + " on Lane " + trafficLight.getLANE() + " - Lane not found");
                }
                Lane lane = lanes.get(trafficLight.getLANE());
                Length position = Transformer.parseLengthBeginEnd(trafficLight.getPOSITION(), lane.getLength());
                try
                {
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getCLASS(),
                            new Class[] {String.class, Lane.class, Length.class, OtsSimulatorInterface.class});
                    trafficLightConstructor.newInstance(new Object[] {trafficLight.getID(), lane, position, simulator});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException("TRAFFICLIGHT: CLASS NAME " + trafficLight.getCLASS().getName()
                            + " for traffic light " + trafficLight.getID() + " on lane " + lane.toString() + " at position "
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
     * @param xmlLink the LINK tag containing the overall offsets
     * @param cseDataList the list of offsets and widths for each tag, in order of definition in the ROADLAYOUT tag
     * @param cseTagMap the map of the tags to the index in the list, to be able to find them quickly
     */
    @SuppressWarnings("checkstyle:methodlength")
    private static void calculateOffsets(final BASICROADLAYOUT roadLayoutTag, final LINK xmlLink,
            final List<CSEData> cseDataList, final Map<Object, Integer> cseTagMap)
    {
        int nr = 0;
        Length totalWidthStart = Length.ZERO;
        Length totalWidthEnd = Length.ZERO;
        boolean startOffset = false;
        boolean endOffset = false;
        for (Object o : roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER())
        {
            if (o instanceof CSESTRIPE)
            {
                CSESTRIPE stripe = (CSESTRIPE) o;
                CSEData cseData = new CSEData();
                cseData.widthStart = Length.ZERO;
                cseData.widthEnd = Length.ZERO;
                if (stripe.getCENTEROFFSET() != null)
                {
                    cseData.centerOffsetStart = stripe.getCENTEROFFSET();
                    cseData.centerOffsetEnd = stripe.getCENTEROFFSET();
                    startOffset = true;
                    endOffset = true;
                }
                else
                {
                    if (stripe.getCENTEROFFSETSTART() != null)
                    {
                        cseData.centerOffsetStart = stripe.getCENTEROFFSETSTART();
                        startOffset = true;
                    }
                    if (stripe.getCENTEROFFSETEND() != null)
                    {
                        cseData.centerOffsetEnd = stripe.getCENTEROFFSETEND();
                        endOffset = true;
                    }
                }
                cseDataList.add(cseData);
            }
            else
            {
                CROSSSECTIONELEMENT cse = (CROSSSECTIONELEMENT) o;
                CSEData cseData = new CSEData();
                cseData.widthStart = cse.getWIDTH() == null ? cse.getWIDTHSTART() : cse.getWIDTH();
                Length halfWidthStart = cseData.widthStart.times(0.5);
                totalWidthStart = totalWidthStart.plus(cseData.widthStart);
                cseData.widthEnd = cse.getWIDTH() == null ? cse.getWIDTHEND() : cse.getWIDTH();
                Length halfWidthEnd = cseData.widthEnd.times(0.5);
                totalWidthEnd = totalWidthEnd.plus(cseData.widthStart);

                if (cse.getCENTEROFFSET() != null)
                {
                    cseData.centerOffsetStart = cse.getCENTEROFFSET();
                    cseData.centerOffsetEnd = cse.getCENTEROFFSET();
                    startOffset = true;
                    endOffset = true;
                }
                else if (cse.getLEFTOFFSET() != null)
                {
                    cseData.centerOffsetStart = cse.getLEFTOFFSET().minus(halfWidthStart);
                    cseData.centerOffsetEnd = cse.getLEFTOFFSET().minus(halfWidthEnd);
                    startOffset = true;
                    endOffset = true;
                }
                else if (cse.getRIGHTOFFSET() != null)
                {
                    cseData.centerOffsetStart = cse.getRIGHTOFFSET().plus(halfWidthStart);
                    cseData.centerOffsetEnd = cse.getRIGHTOFFSET().plus(halfWidthEnd);
                    startOffset = true;
                    endOffset = true;
                }

                if (cse.getCENTEROFFSETSTART() != null)
                {
                    cseData.centerOffsetStart = cse.getCENTEROFFSETSTART();
                    startOffset = true;
                }
                else if (cse.getLEFTOFFSETSTART() != null)
                {
                    cseData.centerOffsetStart = cse.getLEFTOFFSETSTART().minus(halfWidthStart);
                    startOffset = true;
                }
                else if (cse.getRIGHTOFFSETSTART() != null)
                {
                    cseData.centerOffsetStart = cse.getRIGHTOFFSETSTART().plus(halfWidthStart);
                    startOffset = true;
                }

                if (cse.getCENTEROFFSETEND() != null)
                {
                    cseData.centerOffsetEnd = cse.getCENTEROFFSETEND();
                    endOffset = true;
                }
                else if (cse.getLEFTOFFSETEND() != null)
                {
                    cseData.centerOffsetEnd = cse.getLEFTOFFSETEND().minus(halfWidthEnd);
                    endOffset = true;
                }
                else if (cse.getRIGHTOFFSETEND() != null)
                {
                    cseData.centerOffsetEnd = cse.getRIGHTOFFSETEND().plus(halfWidthEnd);
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
        for (CSEData cseData : cseDataList)
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
            CSEData cseData = cseDataList.get(i);
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

        // add the link offset
        if (xmlLink.getOFFSETSTART() != null && xmlLink.getOFFSETSTART().ne0())
        {
            for (CSEData cseData : cseDataList)
            {
                cseData.centerOffsetStart = cseData.centerOffsetStart.plus(xmlLink.getOFFSETSTART());
            }
        }
        if (xmlLink.getOFFSETEND() != null && xmlLink.getOFFSETEND().ne0())
        {
            for (CSEData cseData : cseDataList)
            {
                cseData.centerOffsetEnd = cseData.centerOffsetEnd.plus(xmlLink.getOFFSETEND());
            }
        }
    }

    /**
     * Parse a stripe on a road.
     * @param csl CrossSectionLink; the CrossSectionLine
     * @param startOffset Length; the offset of the start node
     * @param endOffset Length; the offset of the end node
     * @param stripeTag CSESTRIPE; the CSESTRIPE tag in the XML file
     * @param cseList List&lt;CrossSectionElement&gt;; the list of CrossSectionElements to which the stripes should be added
     * @param fixGradualLateralOffset boolean; true if gradualLateralOffset needs to be fixed
     * @throws OtsGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final Length startOffset, final Length endOffset,
            final CSESTRIPE stripeTag, final List<CrossSectionElement> cseList, final boolean fixGradualLateralOffset)
            throws OtsGeometryException, NetworkException, XmlParserException
    {
        Length width =
                stripeTag.getDRAWINGWIDTH() != null ? stripeTag.getDRAWINGWIDTH() : new Length(20.0, LengthUnit.CENTIMETER);
        switch (stripeTag.getTYPE())
        {
            case BLOCKED:
                Stripe blockedLine = new Stripe(csl, startOffset, endOffset, stripeTag.getDRAWINGWIDTH() != null
                        ? stripeTag.getDRAWINGWIDTH() : new Length(40.0, LengthUnit.CENTIMETER), fixGradualLateralOffset);
                blockedLine.addPermeability(csl.getNetwork().getGtuType(GtuType.DEFAULTS.ROAD_USER), Permeable.BOTH);
                cseList.add(blockedLine);
                break;

            case DASHED:
                Stripe dashedLine = new Stripe(csl, startOffset, endOffset, width, fixGradualLateralOffset);
                dashedLine.addPermeability(csl.getNetwork().getGtuType(GtuType.DEFAULTS.ROAD_USER), Permeable.BOTH);
                cseList.add(dashedLine);
                break;

            case DOUBLE:
                Stripe doubleLine = new Stripe(csl, startOffset, endOffset, width, fixGradualLateralOffset);
                cseList.add(doubleLine);
                break;

            case LEFTONLY:
                Stripe leftOnlyLine = new Stripe(csl, startOffset, endOffset, width, fixGradualLateralOffset);
                leftOnlyLine.addPermeability(csl.getNetwork().getGtuType(GtuType.DEFAULTS.ROAD_USER), Permeable.LEFT);
                cseList.add(leftOnlyLine);
                break;

            case RIGHTONLY:
                Stripe rightOnlyLine = new Stripe(csl, startOffset, endOffset, width, fixGradualLateralOffset);
                rightOnlyLine.addPermeability(csl.getNetwork().getGtuType(GtuType.DEFAULTS.ROAD_USER), Permeable.RIGHT);
                cseList.add(rightOnlyLine);
                break;

            case SOLID:
                try
                {
                    Stripe solidLine = new Stripe(csl, startOffset, endOffset, width, fixGradualLateralOffset);
                    cseList.add(solidLine);
                }
                catch (OtsGeometryException oge)
                {
                    System.out.println("Caught OTSGeometryException constructing a stripe on " + csl);
                }
                break;

            default:
                throw new XmlParserException("Unknown Stripe type: " + stripeTag.getTYPE().toString());
        }
    }

    /** contains information about the lanes and stripes to calculate the offset. */
    protected static class CSEData
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
