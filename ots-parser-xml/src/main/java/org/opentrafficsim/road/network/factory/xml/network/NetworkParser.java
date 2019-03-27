package org.opentrafficsim.road.network.factory.xml.network;

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
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
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
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.xml.bindings.types.ArcDirection;
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
        for (LINK xmlLink : network.getLINK())
        {
            Node startNode = otsNetwork.getNode(xmlLink.getNODESTART());
            Node endNode = otsNetwork.getNode(xmlLink.getNODEEND());
            double startDirection =
                    nodeDirections.containsKey(startNode.getId()) ? nodeDirections.get(startNode.getId()).getInUnit() : 0.0;
            double endDirection =
                    nodeDirections.containsKey(endNode.getId()) ? nodeDirections.get(endNode.getId()).getInUnit() : 0.0;
            OTSPoint3D startPoint = new OTSPoint3D(startNode.getPoint());
            OTSPoint3D endPoint = new OTSPoint3D(endNode.getPoint());

            if (!xmlLink.getOFFSETSTART().eq0())
            {
                // shift the start point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETSTART().si;
                startPoint = new OTSPoint3D(startPoint.x + offset * Math.cos(startDirection + Math.PI / 2.0),
                        startPoint.y + offset * Math.sin(startDirection + Math.PI / 2.0), startPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug("fc = " + startNode.getPoint() + ", sa = " + startDirection + ", so = "
                        + offset + ", sp = " + startPoint);
            }

            if (!xmlLink.getOFFSETEND().eq0())
            {
                // shift the end point perpendicular to the node direction or read from tag
                double offset = xmlLink.getOFFSETEND().si;
                endPoint = new OTSPoint3D(endPoint.x + offset * Math.cos(endDirection + Math.PI / 2.0),
                        endPoint.y + offset * Math.sin(endDirection + Math.PI / 2.0), endPoint.z);
                CategoryLogger.filter(Cat.PARSER).debug(
                        "tc = " + endNode.getPoint() + ", ea = " + endDirection + ", eo = " + offset + ", ep = " + endPoint);
            }

            OTSPoint3D[] coordinates = null;

            if (xmlLink.getSTRAIGHT() != null)
            {
                coordinates = new OTSPoint3D[2];
                coordinates[0] = startPoint;
                coordinates[1] = endPoint;
            }

            else if (xmlLink.getPOLYLINE() != null)
            {
                int intermediatePoints = xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().size();
                coordinates = new OTSPoint3D[intermediatePoints + 2];
                coordinates[0] = startPoint;
                coordinates[intermediatePoints + 1] = endPoint;
                for (int p = 0; p < intermediatePoints; p++)
                {
                    coordinates[p + 1] = new OTSPoint3D(xmlLink.getPOLYLINE().getINTERMEDIATEPOINTS().get(p));
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
                coordinates = Bezier
                        .cubic(numSegments, new DirectedPoint(startPoint.x, startPoint.y, startPoint.z, 0, 0, startDirection),
                                new DirectedPoint(endPoint.x, endPoint.y, endPoint.z, 0, 0, endDirection), 1.0, false)
                        .getPoints();

                // TODO: Bezier shape factor and weighted factor
            }

            else if (xmlLink.getCLOTHOID() != null)
            {
                int numSegments = xmlLink.getCLOTHOID().getNUMSEGMENTS().intValue();

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
            CrossSectionLink link = new CrossSectionLink(otsNetwork, xmlLink.getID(), startNode, endNode,
                    otsNetwork.getLinkType(LinkType.DEFAULTS.FREEWAY), designLine, simulator, laneKeepingPolicy);

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
     * @throws NetworkException when the objects cannot be inserted into the network due to inconsistencies
     * @throws OTSGeometryException when the design line is invalid
     * @throws XmlParserException when the stripe type cannot be recognized
     * @throws SimRuntimeException in case of simulation problems building the car generator
     * @throws GTUException when construction of the Strategical Planner failed
     */
    static void applyRoadLayout(final OTSRoadNetwork otsNetwork, final NETWORK network, OTSSimulatorInterface simulator,
            Map<String, ROADLAYOUT> roadLayoutMap)
            throws NetworkException, OTSGeometryException, XmlParserException, SimRuntimeException, GTUException
    {
        for (LINK xmlLink : network.getLINK())
        {
            CrossSectionLink csl = (CrossSectionLink) otsNetwork.getLink(xmlLink.getID());
            List<CrossSectionElement> cseList = new ArrayList<>();
            Map<String, Lane> lanes = new HashMap<>();

            CategoryLogger.filter(Cat.PARSER).trace("Parse link: {}", xmlLink.getID());

            // Get the ROADLAYOUT (wither defined here, or via pointer to DEFINITIONS)
            ROADLAYOUT roadLayoutTagBase;
            if (xmlLink.getLAYOUT() != null)
            {
                if (xmlLink.getROADLAYOUT() != null)
                {
                    throw new XmlParserException(
                            "Link " + xmlLink.getID() + " Ambiguous RoadLayout; both DEFINEDROADLAYOUT and ROADLAYOUT defined");
                }
                roadLayoutTagBase = roadLayoutMap.get(xmlLink.getLAYOUT());
                if (roadLayoutTagBase == null)
                {
                    throw new XmlParserException(
                            "Link " + xmlLink.getID() + " Could not find defined RoadLayout " + xmlLink.getLAYOUT());
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
            ROADLAYOUT roadLayoutTag = Cloner.cloneRoadLayout(roadLayoutTagBase);
            for (LANEOVERRIDE laneOverride : xmlLink.getLANEOVERRIDE())
            {
                for (CSELANE lane : Parser.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(), CSELANE.class))
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

            // STRIPE
            for (CSESTRIPE stripeTag : Parser.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(),
                    CSESTRIPE.class))
            {
                Length startOffset = (stripeTag.getCENTEROFFSETSTART() != null) ? stripeTag.getCENTEROFFSETSTART()
                        : stripeTag.getCENTEROFFSET();
                Length endOffset =
                        (stripeTag.getCENTEROFFSETEND() != null) ? stripeTag.getCENTEROFFSETEND() : stripeTag.getCENTEROFFSET();
                makeStripe(csl, startOffset, endOffset, stripeTag, cseList);
            }

            // Other CROSSECTIONELEMENT
            for (CROSSSECTIONELEMENT cseTag : Parser.getObjectsOfType(roadLayoutTag.getLANEOrNOTRAFFICLANEOrSHOULDER(),
                    CROSSSECTIONELEMENT.class))
            {
                Length startOffset =
                        (cseTag.getCENTEROFFSETSTART() != null) ? cseTag.getCENTEROFFSETSTART() : cseTag.getCENTEROFFSET();
                Length endOffset =
                        (cseTag.getCENTEROFFSETEND() != null) ? cseTag.getCENTEROFFSETEND() : cseTag.getCENTEROFFSET();
                Length startWidth = (cseTag.getWIDTHSTART() != null) ? cseTag.getWIDTHSTART() : cseTag.getWIDTH();
                Length endWidth = (cseTag.getWIDTHEND() != null) ? cseTag.getWIDTHEND() : cseTag.getWIDTH();

                // LANE
                if (cseTag instanceof CSELANE)
                {
                    CSELANE laneTag = (CSELANE) cseTag;
                    boolean direction = laneTag.isDESIGNDIRECTION();
                    LaneType laneType = otsNetwork.getLaneType(laneTag.getLANETYPE());
                    // TODO: Use the DESIGNDIRECTION
                    Map<GTUType, Speed> speedLimitMap = new HashMap<>();
                    List<SPEEDLIMIT> speedLimitTag = new ArrayList<>();
                    if (laneTag.getSPEEDLIMIT().size() > 0)
                        speedLimitTag.addAll(laneTag.getSPEEDLIMIT());
                    else if (roadLayoutTag.getSPEEDLIMIT().size() > 0)
                        speedLimitTag.addAll(roadLayoutTag.getSPEEDLIMIT());
                    Lane lane = new Lane(csl, laneTag.getID(), startOffset, endOffset, startWidth, endWidth, laneType,
                            speedLimitMap);
                    cseList.add(lane);
                    lanes.put(lane.getId(), lane);
                }

                // NOTRAFFICLANE
                else if (cseTag instanceof CSENOTRAFFICLANE)
                {
                    CSENOTRAFFICLANE ntlTag = (CSENOTRAFFICLANE) cseTag;
                    String id = ntlTag.getID() != null ? ntlTag.getID() : UUID.randomUUID().toString();
                    Lane lane = new NoTrafficLane(csl, id, startOffset, endOffset, startWidth, endWidth);
                    cseList.add(lane);
                }

                // SHOULDER
                else if (cseTag instanceof CSESHOULDER)
                {
                    CSESHOULDER shoulderTag = (CSESHOULDER) cseTag;
                    String id = shoulderTag.getID() != null ? shoulderTag.getID() : UUID.randomUUID().toString();
                    Shoulder shoulder = new Shoulder(csl, id, startOffset, endOffset, startWidth, endWidth);
                    cseList.add(shoulder);
                }
            }

            // SINK
            for (LINK.SINK sink : xmlLink.getSINK())
            {
                if (!lanes.containsKey(sink.getLANE()))
                    throw new NetworkException(
                            "LINK: " + xmlLink.getID() + ", Sink on Lane " + sink.getLANE() + " - Lane not found");
                Lane lane = lanes.get(sink.getLANE());
                Length position = Transformer.parseLengthBeginEnd(sink.getPOSITION(), lane.getLength());
                new SinkSensor(lane, position, simulator);
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
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(trafficLight.getCLASS(),
                            new Class[] {String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});
                    trafficLightConstructor.newInstance(new Object[] {trafficLight.getID(), lane, position, simulator});
                }
                catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException exception)
                {
                    throw new NetworkException("TRAFFICLIGHT: CLASS NAME " + trafficLight.getCLASS().getName()
                            + " for traffic light " + trafficLight.getID() + " on lane " + lane.toString()
                            + " -- class not found or constructor not right", exception);
                }
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
        Length width = stripeTag.getWIDTH() != null ? stripeTag.getWIDTH() : new Length(20.0, LengthUnit.CENTIMETER);
        switch (stripeTag.getTYPE())
        {
            case BLOCKED:
                Stripe blockedLine = new Stripe(csl, startOffset, endOffset,
                        stripeTag.getWIDTH() != null ? stripeTag.getWIDTH() : new Length(40.0, LengthUnit.CENTIMETER));
                blockedLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.BOTH);
                cseList.add(blockedLine);
                break;

            case DASHED:
                Stripe dashedLine = new Stripe(csl, startOffset, endOffset, width);
                dashedLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.BOTH);
                cseList.add(dashedLine);
                break;

            case DOUBLE:
                Stripe doubleLine = new Stripe(csl, startOffset, endOffset, width);
                cseList.add(doubleLine);
                break;

            case LEFTONLY:
                Stripe leftOnlyLine = new Stripe(csl, startOffset, endOffset, width);
                leftOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.LEFT);
                cseList.add(leftOnlyLine);
                break;

            case RIGHTONLY:
                Stripe rightOnlyLine = new Stripe(csl, startOffset, endOffset, width);
                rightOnlyLine.addPermeability(csl.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.RIGHT);
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

}
