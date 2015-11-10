package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Dimensionless;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.ShoulderAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
import org.opentrafficsim.road.network.factory.opendrive.LinkTag.ContactPointEnum;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck
 * $, initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class RoadTag
{
    /** unique ID within database (preferably an integer number, uint32_t). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** name of the road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** total length of the reference line in the xy-plane, as indicated in the XML document. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel length = null;

    /** ID of the junction to which the road belongs as a connecting road (= -1 for none). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String junctionId = null;

    /** Link Tag containing predecessor, successor and neighbor info. Can be absent for isolated roads. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LinkTag linkTag = null;

    /** PlanView Tag containing a list of geometries. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    PlanViewTag planViewTag = null;

    /** ElevationProfile Tag containing a list of elevations. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ElevationProfileTag elevationProfileTag = null;

    /** lateralProfile Tag containing a list of superElevations. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LateralProfileTag lateralProfileTag = null;

    /** lanes Tag containing a list of laneSections. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LanesTag lanesTag = null;

    /** signals Tag containing a list of signals. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    SignalsTag signalsTag = null;

    /** Type Tags containing road type and maximum speed information for stretches of road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    TypeTag typeTag = null;

    /** the calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    CrossSectionLink link = null;

    /** the calculated designLine. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSLine3D designLine = null;

    /** the calculated startNode. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSNode startNode = null;

    /** the calculated endNode. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSNode endNode = null;

    /** the calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<CrossSectionLink> subLinks = new ArrayList<>();

    /** the lanetype that allows all traffic. */
    static LaneType LANETYPE_ALL = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node the top-level road node
     * @param parser the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadTag parseRoad(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException,
            NetworkException
    {
        if (LANETYPE_ALL == null)
        {
            LANETYPE_ALL = new LaneType("ALL");
            LANETYPE_ALL.addCompatibility(GTUType.ALL);
        }
        NamedNodeMap attributes = node.getAttributes();
        RoadTag roadTag = new RoadTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("ROAD: missing attribute ID");
        roadTag.id = id.getNodeValue().trim();
        if (parser.roadTags.keySet().contains(roadTag.id))
            throw new SAXException("ROAD: ID " + roadTag.id + " defined twice");

        Node name = attributes.getNamedItem("name");
        if (name == null)
            throw new SAXException("ROAD: missing attribute ID for road with ID=" + roadTag.id);
        roadTag.name = name.getNodeValue().trim();

        Node length = attributes.getNamedItem("length");
        if (length == null)
            throw new SAXException("ROAD: missing attribute LENGTH");
        roadTag.length = new Length.Rel(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);

        Node junctionId = attributes.getNamedItem("junction");
        if (junctionId == null)
            throw new SAXException("ROAD: missing attribute junction for road id=" + roadTag.id);

        roadTag.junctionId = junctionId.getNodeValue().trim();

        /*
         * if (!junctionId.getNodeValue().trim().equals("-1")) { roadTag.junctionId = junctionId.getNodeValue().trim();
         * if(roadTag.junctionId == null) throw new SAXException("ROAD: junction id=" + roadTag.junctionId +
         * " for road id=" + roadTag.id + " not defined as a junction in the XML-file"); if
         * (!parser.junctionTags.keySet().contains(roadTag.junctionId)) throw new SAXException("ROAD: junction id=" +
         * roadTag.junctionId + " for road id=" + roadTag.id + " not defined as a junction in the XML-file"); }
         */

        parser.roadTags.put(roadTag.id, roadTag);

        return roadTag;
    }

    /**
     * @param roadTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     */
    static void buildSubLinks(RoadTag roadTag, OTSDEVSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws OTSGeometryException, NetworkException,
            NamingException
    {
        OTSNetwork otsNetwork = openDriveNetworkLaneParser.network;
        if (roadTag.lanesTag.laneSectionTags.size() == 1)// no sub links
        {
            roadTag.subLinks.add(roadTag.link);
            if (!otsNetwork.containsNode(roadTag.link.getStartNode()))
                otsNetwork.addNode(roadTag.link.getStartNode());
            if (!otsNetwork.containsNode(roadTag.link.getEndNode()))
                otsNetwork.addNode(roadTag.link.getEndNode());
            if (!otsNetwork.containsLink(roadTag.link))
                otsNetwork.addLink(roadTag.link);
        } else
        {
            // build fist several sub links
            List<GeometryTag> tempGeometryTags = new ArrayList<GeometryTag>();
            tempGeometryTags = roadTag.planViewTag.geometryTags;

            int currentIndex = 0;
            for (Integer laneSecIndex = 1; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
            {
                LaneSectionTag laneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);
                Length.Rel laneSecLength = laneSec.s;

                List<OTSPoint3D> points = new ArrayList<OTSPoint3D>();

                GeometryTag from = tempGeometryTags.get(currentIndex);
                GeometryTag to = tempGeometryTags.get(currentIndex);

                for (int indexGeometryTag = currentIndex; indexGeometryTag < tempGeometryTags.size(); indexGeometryTag++)
                {
                    GeometryTag currentGeometryTag = tempGeometryTags.get(indexGeometryTag);
                    if (currentGeometryTag.s.doubleValue() < laneSecLength.doubleValue())
                    {
                        OTSPoint3D point =
                                new OTSPoint3D(currentGeometryTag.x.doubleValue(), currentGeometryTag.y.doubleValue(),
                                        currentGeometryTag.hdg.doubleValue());
                        points.add(point);
                        to = tempGeometryTags.get(currentIndex);
                        currentIndex++;
                        continue;
                    } else
                    {
                        OTSPoint3D point =
                                new OTSPoint3D(currentGeometryTag.x.doubleValue(), currentGeometryTag.y.doubleValue(),
                                        currentGeometryTag.hdg.doubleValue());
                        points.add(point);
                        // currentIndex++;
                        to = tempGeometryTags.get(currentIndex);
                        // OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
                        // coordinates = (OTSPoint3D[]) points.toArray();
                        OTSLine3D designLine = new OTSLine3D(points);
                        String sublinkId = roadTag.id + "." + laneSecIndex.toString();
                        CrossSectionLink sublink =
                                new CrossSectionLink(sublinkId, from.node, to.node, LinkType.ALL, designLine,
                                        LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);

                        roadTag.subLinks.add(sublink);

                        if (!otsNetwork.containsNode(from.node))
                            otsNetwork.addNode(from.node);
                        if (!otsNetwork.containsNode(to.node))
                            otsNetwork.addNode(to.node);

                        otsNetwork.addLink(sublink);

                        break;
                    }
                }
            }

            // build last sub link
            List<OTSPoint3D> points = new ArrayList<OTSPoint3D>();

            GeometryTag from = tempGeometryTags.get(currentIndex);
            GeometryTag to = tempGeometryTags.get(tempGeometryTags.size() - 1);

            for (int indexGeometryTag = currentIndex; indexGeometryTag < tempGeometryTags.size(); indexGeometryTag++)
            {
                GeometryTag currentGeometryTag = tempGeometryTags.get(indexGeometryTag);

                OTSPoint3D point =
                        new OTSPoint3D(currentGeometryTag.x.doubleValue(), currentGeometryTag.y.doubleValue(),
                                currentGeometryTag.hdg.doubleValue());
                points.add(point);
            }

            // OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
            // coordinates = (OTSPoint3D[]) points.toArray();
            OTSLine3D designLine = new OTSLine3D(points);
            String sublinkId = roadTag.id + "." + Integer.toString(roadTag.lanesTag.laneSectionTags.size());
            CrossSectionLink sublink =
                    new CrossSectionLink(sublinkId, from.node, to.node, LinkType.ALL, designLine,
                            LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);

            roadTag.subLinks.add(sublink);

            if (!otsNetwork.containsNode(from.node))
                otsNetwork.addNode(from.node);
            if (!otsNetwork.containsNode(to.node))
                otsNetwork.addNode(to.node);

            otsNetwork.addLink(sublink);

        }

    }

    /**
     * @param roadTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     */
    static void generateRegularRoads(RoadTag roadTag, OTSDEVSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws OTSGeometryException, NetworkException,
            NamingException
    {
        if (roadTag.junctionId.equals("-1"))
            for (int laneSecIndex = 0; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
            {
                LaneSectionTag currentLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);

                CrossSectionLink currentLink = roadTag.subLinks.get(laneSecIndex);

                Length.Rel ds = new Length.Rel(0.0, LengthUnit.METER);
                LaneSectionTag nextLaneSec;
                if (laneSecIndex != roadTag.lanesTag.laneSectionTags.size() - 1)
                {
                    nextLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex + 1);
                    ds = nextLaneSec.s.minus(currentLaneSec.s);
                } else
                {
                    ds = roadTag.length.minus(currentLaneSec.s);
                }

                // show left lanes
                int leftLaneSize = currentLaneSec.leftLaneTags.size();
                Length.Rel leftOffset_start = new Length.Rel(0.0, LengthUnit.METER);
                Length.Rel leftOffset_end = new Length.Rel(0.0, LengthUnit.METER);

                for (int leftLaneIndex = 1; leftLaneIndex <= leftLaneSize; leftLaneIndex++)
                {
                    LaneTag leftLane = currentLaneSec.leftLaneTags.get(leftLaneIndex);

                    leftLane.widthTag.sOffst =
                            leftLane.widthTag.a.plus(leftLane.widthTag.b.multiplyBy(ds.doubleValue()))
                                    .plus(leftLane.widthTag.c.multiplyBy(Math.pow(ds.doubleValue(), 2)))
                                    .plus(leftLane.widthTag.d.multiplyBy(Math.pow(ds.doubleValue(), 3)));

                    Length.Rel laneWidth_start = leftLane.widthTag.a;
                    Length.Rel laneWidth_end = leftLane.widthTag.sOffst;

                    leftOffset_start = leftOffset_start.plus(laneWidth_start.multiplyBy(0.5));
                    leftOffset_end = leftOffset_end.plus(laneWidth_end.multiplyBy(0.5));

                    OvertakingConditions overtakingConditions = null;

                    Speed speed = null;
                    if (leftLane.speedTags.size() > 0)
                        speed = leftLane.speedTags.get(0).max;
                    if (speed == null)
                    {
                        // System.err.println("speed.max == null for " + leftLane.id.toString());
                        speed = new Speed(30.0, SpeedUnit.MILE_PER_HOUR);
                    }

                    Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                    speedLimit.put(GTUType.ALL, speed);

                    if (leftLane.type.equals("driving"))
                    {
                        LongitudinalDirectionality direction = LongitudinalDirectionality.BACKWARD;
                        Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                        directionality.put(GTUType.ALL, direction);
                        Color color = Color.gray;

                        Lane lane =
                                new Lane(currentLink, leftLane.id.toString(), leftOffset_start, leftOffset_end,
                                        laneWidth_start, laneWidth_end, LANETYPE_ALL, directionality, speedLimit,
                                        overtakingConditions);
                        currentLaneSec.lanes.put(leftLane.id, lane);
                        try
                        {
                            new LaneAnimationOD(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (leftLane.type.equals("sidewalk"))
                    {
                        Color color = Color.darkGray;
                        Lane lane =
                                new NoTrafficLane(currentLink, leftLane.id.toString(), leftOffset_start,
                                        leftOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(leftLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (leftLane.type.equals("border"))
                    {
                        Stripe solidLine = new Stripe(currentLink, leftOffset_start, laneWidth_start);
                        try
                        {
                            new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (leftLane.type.equals("shoulder"))
                    {
                        Color color = Color.green;
                        Shoulder shoulder =
                                new Shoulder(currentLink, leftLane.id.toString(), leftOffset_start, leftOffset_end,
                                        laneWidth_start, laneWidth_end);
                        try
                        {
                            new ShoulderAnimation(shoulder, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else
                    {
                        /*
                         * Stripe solidLine = new Stripe(currentLink, leftOffset, laneWidth); try { new
                         * StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID); } catch (RemoteException
                         * exception) { exception.printStackTrace(); }
                         */

                        Color color = Color.green;
                        Lane lane =
                                new NoTrafficLane(currentLink, leftLane.id.toString(), leftOffset_start,
                                        leftOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(leftLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }

                    leftOffset_start = leftOffset_start.plus(laneWidth_start.multiplyBy(0.5));
                    leftOffset_end = leftOffset_end.plus(laneWidth_end.multiplyBy(0.5));
                }

                // show right lanes
                int rightLaneSize = currentLaneSec.rightLaneTags.size();
                Length.Rel rightOffset_start = new Length.Rel(0.0, LengthUnit.METER);
                Length.Rel rightOffset_end = new Length.Rel(0.0, LengthUnit.METER);

                for (int rightLaneIndex = 1; rightLaneIndex <= rightLaneSize; rightLaneIndex++)
                {
                    LaneTag rightLane = currentLaneSec.rightLaneTags.get(-rightLaneIndex);

                    rightLane.widthTag.sOffst =
                            rightLane.widthTag.a.plus(rightLane.widthTag.b.multiplyBy(ds.doubleValue()))
                                    .plus(rightLane.widthTag.c.multiplyBy(Math.pow(ds.doubleValue(), 2)))
                                    .plus(rightLane.widthTag.d.multiplyBy(Math.pow(ds.doubleValue(), 3)));

                    Length.Rel laneWidth_start = rightLane.widthTag.a;
                    Length.Rel laneWidth_end = rightLane.widthTag.sOffst;

                    rightOffset_start = rightOffset_start.minus(laneWidth_start.multiplyBy(0.5));
                    rightOffset_end = rightOffset_end.minus(laneWidth_end.multiplyBy(0.5));

                    OvertakingConditions overtakingConditions = null;

                    Speed speed = null;
                    if (rightLane.speedTags.size() > 0)
                        speed = rightLane.speedTags.get(0).max;
                    if (speed == null)
                    {
                        // System.err.println("speed.max == null for " + rightLane.id.toString());
                        speed = new Speed(30.0, SpeedUnit.MILE_PER_HOUR);
                    }

                    Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                    speedLimit.put(GTUType.ALL, speed);

                    if (rightLane.type.equals("driving"))
                    {
                        LongitudinalDirectionality direction = LongitudinalDirectionality.FORWARD;
                        // if(roadTag.link.getEndNode().getLinksOut().size() == 0)
                        // direction = LongitudinalDirectionality.BACKWARD;
                        Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                        directionality.put(GTUType.ALL, direction);
                        Color color = Color.gray;

                        try
                        {
                            Lane lane =
                                    new Lane(currentLink, rightLane.id.toString(), rightOffset_start, rightOffset_end,
                                            laneWidth_start, laneWidth_end, LANETYPE_ALL, directionality, speedLimit,
                                            overtakingConditions);
                            currentLaneSec.lanes.put(rightLane.id, lane);

                            new LaneAnimationOD(lane, simulator, color);
                        } catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("sidewalk"))
                    {
                        Color color = Color.darkGray;
                        Lane lane =
                                new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset_start,
                                        rightOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(rightLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("border"))
                    {
                        Stripe solidLine = new Stripe(currentLink, rightOffset_start, laneWidth_start);
                        try
                        {
                            new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("shoulder"))
                    {
                        Color color = Color.green;
                        Shoulder shoulder =
                                new Shoulder(currentLink, rightLane.id.toString(), rightOffset_start, laneWidth_start,
                                        laneWidth_end);
                        try
                        {
                            new ShoulderAnimation(shoulder, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else
                    {
                        Color color = Color.green;
                        Lane lane =
                                new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset_start,
                                        rightOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(rightLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }

                    rightOffset_start = rightOffset_start.minus(laneWidth_start.multiplyBy(0.5));
                    rightOffset_end = rightOffset_end.minus(laneWidth_end.multiplyBy(0.5));

                }

                // show center lanes
                int centerLaneSize = currentLaneSec.centerLaneTags.size();
                if (centerLaneSize != 1)
                    System.err.println("Sth is wrong in center lane");
                Length.Rel centerOffset = new Length.Rel(0.0, LengthUnit.METER);

                LaneTag centerLane = currentLaneSec.centerLaneTags.get(0);
                Length.Rel laneWidth = new Length.Rel(0.0, LengthUnit.METER);
                if (centerLane.widthTag != null)
                    laneWidth = centerLane.widthTag.a;

                Stripe solidLine = new Stripe(currentLink, centerOffset, laneWidth);
                try
                {
                    new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                } catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }

            }
    }

    /**
     * @param roadTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     */
    static void generateJunctionRoads(RoadTag roadTag, OTSDEVSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws OTSGeometryException, NetworkException,
            NamingException
    {
        if (!roadTag.junctionId.equals("-1"))
        {
            LaneTag currentLaneTag = roadTag.lanesTag.laneSectionTags.get(0).rightLaneTags.get(-1);

            //connecting start of the lane
            JunctionTag junctionTag = openDriveNetworkLaneParser.junctionTags.get(roadTag.junctionId);
            ConnectionTag connectionTag = junctionTag.connectionTags.get(roadTag.id);
            RoadTag incomingRoadTag = openDriveNetworkLaneParser.roadTags.get(connectionTag.incomingRoad);
            
            Lane inComingLane = null;                   
            
            OTSPoint3D point1 = null;
            OTSPoint3D point2 = null;
            Length.Rel halfWidth_Start = null;
            Length.Rel offset_Start = null;
            Length.Rel center_Start = null;
            
            if(Integer.parseInt(currentLaneTag.predecessorId) != connectionTag.laneLinkFrom)
                System.err.println("Sth is wrong in junction lane link from");
            
            if (incomingRoadTag.linkTag.successorType != null && incomingRoadTag.linkTag.successorType.equals("junction")
                    && incomingRoadTag.linkTag.successorId.equals(junctionTag.id))
            {
                inComingLane = incomingRoadTag.lanesTag.laneSectionTags.get(incomingRoadTag.lanesTag.laneSectionTags.size() - 1).lanes.get(connectionTag.laneLinkFrom);
                point1 = inComingLane.getParentLink().getDesignLine().get(inComingLane.getParentLink().getDesignLine().size()-1);
                point2 = inComingLane.getCenterLine().get(inComingLane.getCenterLine().size()-1);
                halfWidth_Start = inComingLane.getEndWidth().multiplyBy(0.5);
                offset_Start = inComingLane.getDesignLineOffsetAtEnd();
                center_Start = inComingLane.getLateralCenterPosition(1.0);
            }
            else
            {
                inComingLane = incomingRoadTag.lanesTag.laneSectionTags.get(0).lanes.get(connectionTag.laneLinkFrom);
                point1 = inComingLane.getParentLink().getDesignLine().get(0);
                point2 = inComingLane.getCenterLine().get(0);
                halfWidth_Start = inComingLane.getBeginWidth().multiplyBy(0.5);
                offset_Start = inComingLane.getDesignLineOffsetAtBegin();
                center_Start = inComingLane.getLateralCenterPosition(0.0);
            }
            
            if(offset_Start.minus(center_Start).doubleValue()>0.1)
                System.out.println("Sth is wrong in offset of junction road");
            
            double dis = Math.sqrt((point2.x - point1.x) * (point2.x - point1.x) + (point2.y - point1.y) * (point2.y - point1.y) );
            
            if((center_Start.doubleValue()-dis) > 0.1)
                System.out.println("Sth is wrong in offset of junction road");

            
            double factor = halfWidth_Start.divideBy(dis).doubleValue();
            OTSPoint3D newPoint_Start = new OTSPoint3D(point2.x - (point2.x - point1.x) * factor, point2.y - (point2.y - point1.y) * factor);
            
          //connecting end of the lane
            RoadTag outgoingRoadTag = openDriveNetworkLaneParser.roadTags.get(roadTag.linkTag.successorId);
            
            Lane outGoingLane = null;                   
            
            OTSPoint3D point3 = null;
            OTSPoint3D point4 = null;
            Length.Rel halfWidth_End = null;
            Length.Rel offset_End = null;
            Length.Rel center_End = null;


            if(roadTag.linkTag.successorContactPoint.equals(ContactPointEnum.START))
            {
                outGoingLane = outgoingRoadTag.lanesTag.laneSectionTags.get(0).lanes.get(Integer.parseInt(currentLaneTag.successorId));
                point3 = outGoingLane.getParentLink().getDesignLine().get(0);
                point4 = outGoingLane.getCenterLine().get(0);
                halfWidth_End = outGoingLane.getBeginWidth().multiplyBy(0.5);
                offset_End = outGoingLane.getDesignLineOffsetAtBegin();
                center_End = outGoingLane.getLateralCenterPosition(0.0);
            }
            else
            {
                outGoingLane = outgoingRoadTag.lanesTag.laneSectionTags.get(outgoingRoadTag.lanesTag.laneSectionTags.size() - 1).lanes.get(Integer.parseInt(currentLaneTag.successorId));
                point3 = outGoingLane.getParentLink().getDesignLine().get(outGoingLane.getParentLink().getDesignLine().size()-1);
                point4 = outGoingLane.getCenterLine().get(outGoingLane.getCenterLine().size()-1);
                halfWidth_End = outGoingLane.getEndWidth().multiplyBy(0.5);
                offset_End = outGoingLane.getDesignLineOffsetAtEnd();
                center_End = outGoingLane.getLateralCenterPosition(1.0);
            }
            
            if(offset_End.minus(center_End).doubleValue()>0.1)
                System.out.println("Sth is wrong in offset of junction road");

            
            double dis2 = Math.sqrt((point4.x - point3.x) * (point4.x - point3.x) + (point4.y - point3.y) * (point4.y - point3.y) );
            
            
            if((offset_End.doubleValue()-dis2) > 0.1)
                System.out.println("Sth is wrong in offset of junction road");
            
            double factor2 = halfWidth_End.divideBy(dis2).doubleValue();
            OTSPoint3D newPoint_End = new OTSPoint3D(point4.x - (point4.x - point3.x) * factor2, point4.y - (point4.y - point3.y) * factor2);

            
            for (int laneSecIndex = 0; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
            {
                LaneSectionTag currentLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);

                CrossSectionLink currentLink = roadTag.subLinks.get(laneSecIndex);

                Length.Rel ds = new Length.Rel(0.0, LengthUnit.METER);
                LaneSectionTag nextLaneSec;
                if (laneSecIndex != roadTag.lanesTag.laneSectionTags.size() - 1)
                {
                    nextLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex + 1);
                    ds = nextLaneSec.s.minus(currentLaneSec.s);
                } else
                {
                    ds = roadTag.length.minus(currentLaneSec.s);
                }

                // no left lanes
                int leftLaneSize = currentLaneSec.leftLaneTags.size();
                
                if(leftLaneSize!=0)
                    System.out.println("Sth is wrong in left lanes of junction road");


                // one right lane
                int rightLaneSize = currentLaneSec.rightLaneTags.size();
                
                if(rightLaneSize > 1)
                    System.out.println("Sth is wrong in right lanes of junction road");
                
                Length.Rel rightOffset_start = new Length.Rel(0.0, LengthUnit.METER);
                Length.Rel rightOffset_end = new Length.Rel(0.0, LengthUnit.METER);

                for (int rightLaneIndex = 1; rightLaneIndex <= rightLaneSize; rightLaneIndex++)
                {
                    LaneTag rightLane = currentLaneSec.rightLaneTags.get(-rightLaneIndex);

                    rightLane.widthTag.sOffst =
                            rightLane.widthTag.a.plus(rightLane.widthTag.b.multiplyBy(ds.doubleValue()))
                                    .plus(rightLane.widthTag.c.multiplyBy(Math.pow(ds.doubleValue(), 2)))
                                    .plus(rightLane.widthTag.d.multiplyBy(Math.pow(ds.doubleValue(), 3)));

                    Length.Rel laneWidth_start = rightLane.widthTag.a;
                    Length.Rel laneWidth_end = rightLane.widthTag.sOffst;

                    rightOffset_start = rightOffset_start.minus(laneWidth_start.multiplyBy(0.5));
                    rightOffset_end = rightOffset_end.minus(laneWidth_end.multiplyBy(0.5));

                    OvertakingConditions overtakingConditions = null;

                    Speed speed = null;
                    if (rightLane.speedTags.size() > 0)
                        speed = rightLane.speedTags.get(0).max;
                    if (speed == null)
                    {
                        // System.err.println("speed.max == null for " + rightLane.id.toString());
                        speed = new Speed(30.0, SpeedUnit.MILE_PER_HOUR);
                    }

                    Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                    speedLimit.put(GTUType.ALL, speed);

                    if (rightLane.type.equals("driving"))
                    {
                        LongitudinalDirectionality direction = LongitudinalDirectionality.FORWARD;
                        Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                        directionality.put(GTUType.ALL, direction);
                        Color color = Color.gray;
                        
                        Lane lane = null;

                        try
                        {
                            if(-rightLaneIndex == -1 && newPoint_Start.distance(currentLink.getDesignLine().get(0)).doubleValue()>0.5)
                            {
                                OTSPoint3D[] currentLine = currentLink.getDesignLine().getPoints();
                                List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();
                                coordinates.add(newPoint_Start);
                                for (OTSPoint3D point : currentLine)
                                {
                                    coordinates.add(point);
                                }
                                coordinates.add(newPoint_End);
                                
                                OTSLine3D designLine = new OTSLine3D(coordinates);
                                CrossSectionLink newlink =
                                        new CrossSectionLink(currentLink.getId(), currentLink.getStartNode(), currentLink.getEndNode(), LinkType.ALL, designLine,
                                                LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);
                                
                                openDriveNetworkLaneParser.network.removeLink(currentLink);
                                roadTag.subLinks.remove(currentLink);

                                roadTag.subLinks.add(newlink);
                                openDriveNetworkLaneParser.network.addLink(newlink);
                                
                                lane =
                                        new Lane(newlink, rightLane.id.toString(), rightOffset_start, rightOffset_end,
                                                laneWidth_start, laneWidth_end, LANETYPE_ALL, directionality, speedLimit,
                                                overtakingConditions);
                            }
                            else    
                                lane =
                                        new Lane(currentLink, rightLane.id.toString(), rightOffset_start,
                                                rightOffset_end, laneWidth_start, laneWidth_end, LANETYPE_ALL,
                                                directionality, speedLimit, overtakingConditions);
                            currentLaneSec.lanes.put(rightLane.id, lane);

                            new LaneAnimationOD(lane, simulator, color);
                        } catch (Exception exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("sidewalk"))
                    {
                        Color color = Color.darkGray;
                        Lane lane =
                                new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset_start,
                                        rightOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(rightLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("border"))
                    {
                        Stripe solidLine = new Stripe(currentLink, rightOffset_start, laneWidth_start);
                        try
                        {
                            new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else if (rightLane.type.equals("shoulder"))
                    {
                        Color color = Color.green;
                        Shoulder shoulder =
                                new Shoulder(currentLink, rightLane.id.toString(), rightOffset_start, laneWidth_start,
                                        laneWidth_end);
                        try
                        {
                            new ShoulderAnimation(shoulder, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    } else
                    {
                        Color color = Color.green;
                        Lane lane =
                                new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset_start,
                                        rightOffset_end, laneWidth_start, laneWidth_end);
                        currentLaneSec.lanes.put(rightLane.id, lane);
                        try
                        {
                            new LaneAnimation(lane, simulator, color);
                        } catch (RemoteException exception)
                        {
                            exception.printStackTrace();
                        }
                    }

                    rightOffset_start = rightOffset_start.minus(laneWidth_start.multiplyBy(0.5));
                    rightOffset_end = rightOffset_end.minus(laneWidth_end.multiplyBy(0.5));

                }

                // show center lanes
                int centerLaneSize = currentLaneSec.centerLaneTags.size();
                if (centerLaneSize > 1)
                    System.err.println("Sth is wrong in center lane");
                Length.Rel centerOffset = new Length.Rel(0.0, LengthUnit.METER);

                LaneTag centerLane = currentLaneSec.centerLaneTags.get(0);
                Length.Rel laneWidth = new Length.Rel(0.0, LengthUnit.METER);
                if (centerLane.widthTag != null)
                    laneWidth = centerLane.widthTag.a;

                Stripe solidLine = new Stripe(currentLink, centerOffset, laneWidth);
                try
                {
                    new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                } catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }

            }
        }
    }

    /**
     * @param roadTag
     * @param openDriveNetworkLaneParser
     */
    public static void buildLink(RoadTag roadTag, OpenDriveNetworkLaneParser openDriveNetworkLaneParser)
    {
        if (roadTag.junctionId == null)
            System.out.println("sth is wrong in building links");

        if (!roadTag.junctionId.equals("-1"))
        {
            RoadTag predecessorRoadTag = openDriveNetworkLaneParser.roadTags.get(roadTag.linkTag.predecessorId);
            RoadTag successorRoadTag = openDriveNetworkLaneParser.roadTags.get(roadTag.linkTag.successorId);

            OTSNode from = null;

            if (roadTag.linkTag.predecessorContactPoint.equals(ContactPointEnum.START))
                from = predecessorRoadTag.startNode;
            else if (roadTag.linkTag.predecessorContactPoint.equals(ContactPointEnum.END))
                from = predecessorRoadTag.endNode;
            else
                System.out.println("sth is wrong in building links");

            OTSNode to = null;

            if (roadTag.linkTag.successorContactPoint.equals(ContactPointEnum.START))
                to = successorRoadTag.startNode;
            else if (roadTag.linkTag.successorContactPoint.equals(ContactPointEnum.END))
                to = successorRoadTag.endNode;
            else
                System.out.println("sth is wrong in building links");

            CrossSectionLink newlink =
                    new CrossSectionLink(roadTag.id, from, to, LinkType.ALL, roadTag.designLine,
                            LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);

            roadTag.link = newlink;
        } else
        {
            OTSNode from = roadTag.startNode;
            OTSNode to = roadTag.endNode;
            CrossSectionLink newlink =
                    new CrossSectionLink(roadTag.id, from, to, LinkType.ALL, roadTag.designLine,
                            LongitudinalDirectionality.BOTH, LaneKeepingPolicy.KEEP_LANE);

            roadTag.link = newlink;
        }
    }

}
