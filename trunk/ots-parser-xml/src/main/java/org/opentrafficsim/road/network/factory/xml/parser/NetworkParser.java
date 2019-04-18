package org.opentrafficsim.road.network.factory.xml.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.base.logger.Cat;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.OTSRoadNetwork;
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
import org.opentrafficsim.xml.generated.ROUTE;
import org.opentrafficsim.xml.generated.SHORTESTROUTE;
import org.opentrafficsim.xml.generated.SPEEDLIMIT;
import org.opentrafficsim.xml.generated.TRAFFICLIGHTTYPE;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * NetworkParser parses the NETWORK tag of the OTS network. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    public static void parseNodes(final OTSRoadNetwork otsNetwork, final NETWORK network) throws NetworkException
    {
        for (NODE xmlNode : network.getNODE())
            new OTSNode(otsNetwork, xmlNode.getID(), new OTSPoint3D(xmlNode.getCOORDINATE()));
    }

    /**
     * Calculate the default angles of the Nodes, in case they have not been set. This is based on the STRAIGHT LINK elements in
     * the XML file.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @return a map of nodes and their default direction
     */
    public static Map<String, Direction> calculateNodeAngles(final OTSRoadNetwork otsNetwork, final NETWORK network)
    {
        Map<String, Direction> nodeDirections = new HashMap<>();
        for (NODE xmlNode : network.getNODE())
        {
            if (xmlNode.getDIRECTION() != null)
            {
                nodeDirections.put(xmlNode.getID(), xmlNode.getDIRECTION());
            }
        }

        for (LINK xmlLink : network.getLINK())
        {
            if (xmlLink.getSTRAIGHT() != null)
            {
                Node startNode = otsNetwork.getNode(xmlLink.getNODESTART());
                Node endNode = otsNetwork.getNode(xmlLink.getNODEEND());
                double direction = Math.atan2(endNode.getPoint().y - startNode.getPoint().y,
                        endNode.getPoint().x - startNode.getPoint().x);
                if (!nodeDirections.containsKey(startNode.getId()))
                {
                    nodeDirections.put(startNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
                if (!nodeDirections.containsKey(endNode.getId()))
                {
                    nodeDirections.put(endNode.getId(), new Direction(direction, DirectionUnit.EAST_RADIAN));
                }
            }
        }

        for (NODE xmlNode : network.getNODE())
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
     * @throws OTSGeometryException when the design line is invalid
     */
    static void parseLinks(final OTSRoadNetwork otsNetwork, final NETWORK network, Map<String, Direction> nodeDirections,
            OTSSimulatorInterface simulator) throws NetworkException, OTSGeometryException
    {
        for (CONNECTOR xmlConnector : network.getCONNECTOR())
        {
            Node startNode = otsNetwork.getNode(xmlConnector.getNODESTART());
            Node endNode = otsNetwork.getNode(xmlConnector.getNODEEND());
            String id = xmlConnector.getID();
            double demandWeight = xmlConnector.getDEMANDWEIGHT();
            OTSLine3D designLine = new OTSLine3D(startNode.getPoint(), endNode.getPoint());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, id, startNode, endNode,
                    otsNetwork.getLinkType(LinkType.DEFAULTS.CONNECTOR), designLine, simulator, null);
            link.setDemandWeight(demandWeight);
        }

        for (LINK xmlLink : network.getLINK())
        {
            Node startNode = otsNetwork.getNode(xmlLink.getNODESTART());
            Node endNode = otsNetwork.getNode(xmlLink.getNODEEND());
            double startDirection =
                    nodeDirections.containsKey(startNode.getId()) ? nodeDirections.get(startNode.getId()).getSI() : 0.0;
            double endDirection =
                    nodeDirections.containsKey(endNode.getId()) ? nodeDirections.get(endNode.getId()).getSI() : 0.0;
            OTSPoint3D startPoint = new OTSPoint3D(startNode.getPoint());
            OTSPoint3D endPoint = new OTSPoint3D(endNode.getPoint());
            OTSPoint3D[] coordinates = null;

            if (xmlLink.getSTRAIGHT() != null)
            {
                coordinates = new OTSPoint3D[2];
                coordinates[0] = startPoint;
                coordinates[1] = endPoint;
            }

            else if (xmlLink.getPOLYLINE() != null)
            {
                int intermediatePoints = xmlLink.getPOLYLINE().getCOORDINATE().size();
                coordinates = new OTSPoint3D[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = new OTSPoint3D(xmlLink.getPOLYLINE().getCOORDINATE().get(p));
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
                List<OTSPoint3D> centerList = OTSPoint3D.circleIntersections(startNode.getPoint(), radiusSI + offsetStart,
                        endNode.getPoint(), radiusSI + offsetEnd);
                OTSPoint3D center =
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
                coordinates = new OTSPoint3D[numSegments];
                coordinates[0] = new OTSPoint3D(startNode.getPoint().x + Math.cos(sa) * offsetStart,
                        startNode.getPoint().y + Math.sin(sa) * offsetStart, startNode.getPoint().z);
                coordinates[coordinates.length - 1] = new OTSPoint3D(endNode.getPoint().x + Math.cos(ea) * offsetEnd,
                        endNode.getPoint().y + Math.sin(ea) * offsetEnd, endNode.getPoint().z);
                double angleStep = Math.abs((ea - sa)) / numSegments;
                double slopeStep = (endNode.getPoint().z - startNode.getPoint().z) / numSegments;

                if (xmlLink.getARC().getDIRECTION().equals(ArcDirection.RIGHT))
                {
                    for (int p = 1; p < numSegments - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / numSegments;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa - angleStep * p),
                                center.y + (radiusSI + dRad) * Math.sin(sa - angleStep * p),
                                startNode.getPoint().z + slopeStep * p);
                    }
                }
                else
                {
                    for (int p = 1; p < numSegments - 1; p++)
                    {
                        double dRad = offsetStart + (offsetEnd - offsetStart) * p / numSegments;
                        coordinates[p] = new OTSPoint3D(center.x + (radiusSI + dRad) * Math.cos(sa + angleStep * p),
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

            OTSLine3D designLine = OTSLine3D.createAndCleanOTSLine3D(coordinates);

            // TODO: Directionality has to be added later when the lanes and their direction are known.
            LaneKeepingPolicy laneKeepingPolicy = LaneKeepingPolicy.valueOf(xmlLink.getLANEKEEPING().name());
            LinkType linkType = otsNetwork.getLinkType(xmlLink.getTYPE());
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getID(), startNode, endNode, linkType, designLine,
                    simulator, laneKeepingPolicy);

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
     * @throws OTSGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    static void applyRoadLayout(final OTSRoadNetwork otsNetwork, final NETWORK network, OTSSimulatorInterface simulator,
            Map<String, ROADLAYOUT> roadLayoutMap, Map<LinkType, Map<GTUType, Speed>> linkTypeSpeedLimitMap)
            throws NetworkException, OTSGeometryException, XmlParserException, SimRuntimeException, GTUException
    {
        for (LINK xmlLink : network.getLINK())
        {
            CrossSectionLink csl = (CrossSectionLink) otsNetwork.getLink(xmlLink.getID());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new HashMap<>();

            CategoryLogger.filter(Cat.PARSER).trace("Parse link: {}", xmlLink.getID());

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
            Map<Object, Integer> cseTagMap = new HashMap<>();
            calculateOffsets(roadLayoutTag, xmlLink, cseDataList, cseTagMap);

            // STRIPE
            for (CSESTRIPE stripeTag : ParseUtil.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(),
                    CSESTRIPE.class))
            {
                CSEData cseData = cseDataList.get(cseTagMap.get(stripeTag));
                makeStripe(csl, cseData.centerOffsetStart, cseData.centerOffsetEnd, stripeTag, cseList);
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
                    Map<GTUType, Speed> speedLimitMap = new HashMap<>();
                    LinkType linkType = csl.getLinkType();
                    if (!linkTypeSpeedLimitMap.containsKey(linkType))
                        linkTypeSpeedLimitMap.put(linkType, new HashMap<>());
                    speedLimitMap.putAll(linkTypeSpeedLimitMap.get(linkType));
                    for (SPEEDLIMIT speedLimitTag : roadLayoutTag.getSPEEDLIMIT())
                    {
                        GTUType gtuType = otsNetwork.getGtuType(speedLimitTag.getGTUTYPE());
                        speedLimitMap.put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                    }
                    for (SPEEDLIMIT speedLimitTag : laneTag.getSPEEDLIMIT())
                    {
                        GTUType gtuType = otsNetwork.getGtuType(speedLimitTag.getGTUTYPE());
                        speedLimitMap.put(gtuType, speedLimitTag.getLEGALSPEEDLIMIT());
                    }
                    Lane lane = new Lane(csl, laneTag.getID(), cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd, laneType, speedLimitMap);
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                }

                // NOTRAFFICLANE
                else if (cseTag instanceof CSENOTRAFFICLANE)
                {
                    CSENOTRAFFICLANE ntlTag = (CSENOTRAFFICLANE) cseTag;
                    String id = ntlTag.getID() != null ? ntlTag.getID() : UUID.randomUUID().toString();
                    Lane lane = new NoTrafficLane(csl, id, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd);
                    cseList.add(lane);
                }

                // SHOULDER
                else if (cseTag instanceof CSESHOULDER)
                {
                    CSESHOULDER shoulderTag = (CSESHOULDER) cseTag;
                    String id = shoulderTag.getID() != null ? shoulderTag.getID() : UUID.randomUUID().toString();
                    Shoulder shoulder = new Shoulder(csl, id, cseData.centerOffsetStart, cseData.centerOffsetEnd,
                            cseData.widthStart, cseData.widthEnd);
                    cseList.add(shoulder);
                }
            }

            // TRAFFICLIGHT
            for (TRAFFICLIGHTTYPE trafficLight : xmlLink.getTRAFFICLIGHT())
            {
                if (!lanes.containsKey(trafficLight.getLANE()))
                    throw new NetworkException("LINK: " + xmlLink.getID() + ", TrafficLight with id " + trafficLight.getID()
                            + " on Lane " + trafficLight.getLANE() + " - Lane not found");
                Lane lane = lanes.get(trafficLight.getLANE());
                Length position = Transformer.parseLengthBeginEnd(trafficLight.getPOSITION(), lane.getLength());
                try
                {
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getCLASS(), new Class[] {
                            String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class });
                    trafficLightConstructor.newInstance(new Object[] { trafficLight.getID(), lane, position, simulator });
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
    private static void calculateOffsets(BASICROADLAYOUT roadLayoutTag, LINK xmlLink, List<CSEData> cseDataList,
            Map<Object, Integer> cseTagMap)
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
                Length halfWidthStart = cseData.widthStart.multiplyBy(0.5);
                totalWidthStart = totalWidthStart.plus(cseData.widthStart);
                cseData.widthEnd = cse.getWIDTH() == null ? cse.getWIDTHEND() : cse.getWIDTH();
                Length halfWidthEnd = cseData.widthEnd.multiplyBy(0.5);
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
            cseDataList.get(0).centerOffsetStart =
                    totalWidthStart.multiplyBy(-0.5).minus(cseDataList.get(0).widthStart.multiplyBy(-0.5));
        }
        if (!endOffset)
        {
            cseDataList.get(0).centerOffsetEnd =
                    totalWidthEnd.multiplyBy(-0.5).minus(cseDataList.get(0).widthEnd.multiplyBy(-0.5));
        }

        // forward pass
        Length cs = null;
        Length es = null;
        for (CSEData cseData : cseDataList)
        {
            if (cseData.centerOffsetStart != null)
            {
                cs = cseData.centerOffsetStart.plus(cseData.widthStart.multiplyBy(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.plus(cseData.widthStart.multiplyBy(0.5));
                    cs = cs.plus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.plus(cseData.widthEnd.multiplyBy(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.plus(cseData.widthEnd.multiplyBy(0.5));
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
                cs = cseData.centerOffsetStart.minus(cseData.widthStart.multiplyBy(0.5));
            }
            else
            {
                if (cs != null)
                {
                    cseData.centerOffsetStart = cs.minus(cseData.widthStart.multiplyBy(0.5));
                    cs = cs.minus(cseData.widthStart);
                }
            }
            if (cseData.centerOffsetEnd != null)
            {
                es = cseData.centerOffsetEnd.minus(cseData.widthEnd.multiplyBy(0.5));
            }
            else
            {
                if (es != null)
                {
                    cseData.centerOffsetEnd = es.minus(cseData.widthEnd.multiplyBy(0.5));
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
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id of the stripe not unique
     * @throws XmlParserException when the stripe type cannot be recognized
     */
    private static void makeStripe(final CrossSectionLink csl, final Length startOffset, final Length endOffset,
            final CSESTRIPE stripeTag, final List<CrossSectionElement> cseList)
            throws OTSGeometryException, NetworkException, XmlParserException
    {
        Length width =
                stripeTag.getDRAWINGWIDTH() != null ? stripeTag.getDRAWINGWIDTH() : new Length(20.0, LengthUnit.CENTIMETER);
        switch (stripeTag.getTYPE())
        {
            case BLOCKED:
                Stripe blockedLine = new Stripe(csl, startOffset, endOffset, stripeTag.getDRAWINGWIDTH() != null
                        ? stripeTag.getDRAWINGWIDTH() : new Length(40.0, LengthUnit.CENTIMETER));
                blockedLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.ROAD_USER), Permeable.BOTH);
                cseList.add(blockedLine);
                break;

            case DASHED:
                Stripe dashedLine = new Stripe(csl, startOffset, endOffset, width);
                dashedLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.ROAD_USER), Permeable.BOTH);
                cseList.add(dashedLine);
                break;

            case DOUBLE:
                Stripe doubleLine = new Stripe(csl, startOffset, endOffset, width);
                cseList.add(doubleLine);
                break;

            case LEFTONLY:
                Stripe leftOnlyLine = new Stripe(csl, startOffset, endOffset, width);
                leftOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.ROAD_USER), Permeable.LEFT);
                cseList.add(leftOnlyLine);
                break;

            case RIGHTONLY:
                Stripe rightOnlyLine = new Stripe(csl, startOffset, endOffset, width);
                rightOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.ROAD_USER), Permeable.RIGHT);
                cseList.add(rightOnlyLine);
                break;

            case SOLID:
                Stripe solidLine = new Stripe(csl, startOffset, endOffset, width);
                cseList.add(solidLine);
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

    /**
     * Parse the ROUTE tags.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static void parseRoutes(final OTSRoadNetwork otsNetwork, final NETWORK network) throws NetworkException
    {
        for (ROUTE routeTag : network.getROUTE())
        {
            Route route = new Route(routeTag.getID());
            GTUType gtuType = otsNetwork.getGtuType(routeTag.getGTUTYPE());
            if (gtuType == null)
                throw new NetworkException("GTUTYPE " + routeTag.getGTUTYPE() + " not found in ROUTE " + routeTag.getID());
            for (ROUTE.NODE nodeTag : routeTag.getNODE())
            {
                Node node = otsNetwork.getNode(nodeTag.getID());
                if (node == null)
                    throw new NetworkException("NODE " + nodeTag.getID() + " not found in ROUTE " + routeTag.getID());
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

    /**
     * Parse the SHORTESTROUTE tags.
     * @param otsNetwork OTSRoadNetwork; the network to insert the parsed objects in
     * @param network NETWORK; the NETWORK tag
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     */
    static void parseShortestRoutes(final OTSRoadNetwork otsNetwork, final NETWORK network) throws NetworkException
    {
        for (SHORTESTROUTE shortestRouteTag : network.getSHORTESTROUTE())
        {
            Route route = new Route(shortestRouteTag.getID());
            GTUType gtuType = otsNetwork.getGtuType(shortestRouteTag.getGTUTYPE());
            if (gtuType == null)
                throw new NetworkException(
                        "GTUTYPE " + shortestRouteTag.getGTUTYPE() + " not found in SHORTESTROUTE " + shortestRouteTag.getID());
            Node nodeFrom = otsNetwork.getNode(shortestRouteTag.getFROM().getNODE());
            if (nodeFrom == null)
                throw new NetworkException("FROM NODE " + shortestRouteTag.getFROM().getNODE() + " not found in SHORTESTROUTE "
                        + shortestRouteTag.getID());
            Node nodeTo = otsNetwork.getNode(shortestRouteTag.getTO().getNODE());
            if (nodeTo == null)
                throw new NetworkException("TO NODE " + shortestRouteTag.getTO().getNODE() + " not found in SHORTESTROUTE "
                        + shortestRouteTag.getID());
            List<Node> nodesVia = new ArrayList<>();
            for (SHORTESTROUTE.VIA nodeViaTag : shortestRouteTag.getVIA())
            {
                Node nodeVia = otsNetwork.getNode(nodeViaTag.getNODE());
                if (nodeTo == null)
                    throw new NetworkException(
                            "VIA NODE " + nodeViaTag.getNODE() + " not found in SHORTESTROUTE " + shortestRouteTag.getID());
                nodesVia.add(nodeVia);
            }
            Route shortestRoute = otsNetwork.getShortestRouteBetween(gtuType, nodeFrom, nodeTo, nodesVia);
            for (Node node : shortestRoute.getNodes())
            {
                route.addNode(node);
            }
            otsNetwork.addRoute(gtuType, route);
        }
    }

}
