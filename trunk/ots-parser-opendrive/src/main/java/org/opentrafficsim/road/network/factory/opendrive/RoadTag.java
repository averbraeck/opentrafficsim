package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.ShoulderAnimation;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.road.network.factory.opendrive.LinkTag.ContactPointEnum;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.CrossSectionSlice;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.NoTrafficLane;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class RoadTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Unique ID within database (preferably an integer number, uint32_t). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Name of the road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Total length of the reference line in the xy-plane, as indicated in the XML document. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length length = null;

    /** Id of the junction to which the road belongs as a connecting road (= -1 for none). */
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

    /** LateralProfile Tag containing a list of superElevations. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LateralProfileTag lateralProfileTag = null;

    /** Lanes Tag containing a list of laneSections. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LanesTag lanesTag = null;

    /** Signals Tag containing a list of signals. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    SignalsTag signalsTag = null;

    /** Objects Tag containing a list of objects. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ObjectsTag objectsTag = null;

    /** Type Tags containing road type and maximum speed information for stretches of road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    TypeTag typeTag = null;

    /** The calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    CrossSectionLink link = null;

    /** The calculated designLine. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSLine3D designLine = null;

    /** The calculated startNode. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSRoadNode startNode = null;

    /** The calculated endNode. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OTSRoadNode endNode = null;

    /** The calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<CrossSectionLink> subLinks = new ArrayList<>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadTag parseRoad(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException, NetworkException
    {
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
        roadTag.length = new Length(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);

        Node junctionId = attributes.getNamedItem("junction");
        if (junctionId == null)
            throw new SAXException("ROAD: missing attribute junction for road id=" + roadTag.id);

        roadTag.junctionId = junctionId.getNodeValue().trim();

        /*
         * if (!junctionId.getNodeValue().trim().equals("-1")) { roadTag.junctionId = junctionId.getNodeValue().trim();
         * if(roadTag.junctionId == null) throw new SAXException("ROAD: junction id=" + roadTag.junctionId + " for road id=" +
         * roadTag.id + " not defined as a junction in the XML-file"); if
         * (!parser.junctionTags.keySet().contains(roadTag.junctionId)) throw new SAXException("ROAD: junction id=" +
         * roadTag.junctionId + " for road id=" + roadTag.id + " not defined as a junction in the XML-file"); }
         */

        parser.roadTags.put(roadTag.id, roadTag);

        return roadTag;
    }

    /**
     * @param roadTag RoadTag; the road tag
     * @param simulator OTSSimulatorInterface; the simulator
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NetworkException on network error
     * @throws OTSGeometryException on geometry or position error
     * @throws NamingException on error registering the animation
     */
    static void buildSubLinks(RoadTag roadTag, OTSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser)
            throws OTSGeometryException, NetworkException, NamingException
    {
        OTSNetwork otsNetwork = openDriveNetworkLaneParser.network;
        if (roadTag.lanesTag.laneSectionTags.size() == 1)// no sub links
        {
            // if (roadTag.junctionId.equals("-1"))
            {
                roadTag.subLinks.add(roadTag.link);
                if (!otsNetwork.containsNode(roadTag.link.getStartNode()))
                    otsNetwork.addNode(roadTag.link.getStartNode());
                if (!otsNetwork.containsNode(roadTag.link.getEndNode()))
                    otsNetwork.addNode(roadTag.link.getEndNode());
                if (!otsNetwork.containsLink(roadTag.link))
                    otsNetwork.addLink(roadTag.link);
            }

        }
        else
        {
            // build fist several sub links
            List<GeometryTag> tempGeometryTags = new ArrayList<GeometryTag>();
            tempGeometryTags = roadTag.planViewTag.geometryTags;

            /*
             * if(roadTag.id.equals("3766070")) System.out.println();
             */
            int currentIndex = 0;
            for (Integer laneSecIndex = 1; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
            {
                LaneSectionTag laneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);
                Length laneSecLength = laneSec.s;

                List<OTSPoint3D> points = new ArrayList<OTSPoint3D>();

                GeometryTag from = tempGeometryTags.get(currentIndex);
                GeometryTag to = tempGeometryTags.get(currentIndex);

                for (int indexGeometryTag = currentIndex; indexGeometryTag < tempGeometryTags.size(); indexGeometryTag++)
                {
                    GeometryTag currentGeometryTag = tempGeometryTags.get(indexGeometryTag);
                    if (currentGeometryTag.s.doubleValue() < laneSecLength.doubleValue())
                    {
                        OTSPoint3D point = new OTSPoint3D(currentGeometryTag.x.doubleValue(),
                                currentGeometryTag.y.doubleValue(), currentGeometryTag.z.doubleValue());

                        if (points.size() == 0)
                            points.add(point);
                        else
                        {
                            if (point.x != points.get(points.size() - 1).x && point.y != points.get(points.size() - 1).y)
                                points.add(point);
                        }

                        OTSPoint3D lastPoint = new OTSPoint3D(points.get(points.size() - 1));

                        if (currentGeometryTag.interLine != null)
                        {
                            for (OTSPoint3D point1 : currentGeometryTag.interLine.getPoints())
                            {
                                // double xDiff = lastPoint.x - point.x;
                                // double yDiff = lastPoint.y - point.y;
                                // double distance = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);

                                if (lastPoint.x != point1.x && lastPoint.y != point1.y)
                                {
                                    points.add(point1);
                                    lastPoint = point1;
                                }
                            }
                        }

                        to = tempGeometryTags.get(currentIndex);
                        currentIndex++;
                        continue;
                    }
                    else
                    {
                        OTSPoint3D point = new OTSPoint3D(currentGeometryTag.x.doubleValue(),
                                currentGeometryTag.y.doubleValue(), currentGeometryTag.z.doubleValue());

                        if (points.size() == 0)
                            points.add(point);
                        else
                        {
                            if (point.x != points.get(points.size() - 1).x && point.y != points.get(points.size() - 1).y)
                                points.add(point);
                        }

                        OTSPoint3D lastPoint = new OTSPoint3D(points.get(points.size() - 1));

                        if (currentGeometryTag.interLine != null)
                        {
                            for (OTSPoint3D point1 : currentGeometryTag.interLine.getPoints())
                            {

                                /*
                                 * double xDiff = lastPoint.x - point.x; double yDiff = lastPoint.y - point.y; double distance =
                                 * (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                                 */

                                if (lastPoint.x != point.x && lastPoint.y != point.y)
                                {
                                    points.add(point1);
                                    lastPoint = point1;
                                }
                            }
                        }

                        // currentIndex++;
                        to = tempGeometryTags.get(currentIndex);
                        // OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
                        // coordinates = (OTSPoint3D[]) points.toArray();
                        OTSLine3D designLine = new OTSLine3D(points);
                        String sublinkId = roadTag.id + "." + laneSecIndex.toString();
                        CrossSectionLink sublink = new CrossSectionLink(openDriveNetworkLaneParser.network, sublinkId,
                                from.node, to.node, openDriveNetworkLaneParser.network.getLinkType(LinkType.DEFAULTS.ROAD),
                                designLine, simulator, LaneKeepingPolicy.KEEPLANE);

                        roadTag.subLinks.add(sublink);

                        if (!otsNetwork.containsNode(from.node))
                            otsNetwork.addNode(from.node);
                        if (!otsNetwork.containsNode(to.node))
                            otsNetwork.addNode(to.node);

                        if (!otsNetwork.containsLink(sublink))
                            otsNetwork.addLink(sublink);
                        else
                            System.err.println("Sublink already registered: " + sublink);

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

                OTSPoint3D point = new OTSPoint3D(currentGeometryTag.x.doubleValue(), currentGeometryTag.y.doubleValue(),
                        currentGeometryTag.z.doubleValue());
                // points.add(point);

                if (points.size() == 0)
                    points.add(point);
                else
                {
                    if (point.x != points.get(points.size() - 1).x && point.y != points.get(points.size() - 1).y)
                        points.add(point);
                }

                OTSPoint3D lastPoint = new OTSPoint3D(points.get(points.size() - 1));

                if (currentGeometryTag.interLine != null)
                {
                    for (OTSPoint3D point1 : currentGeometryTag.interLine.getPoints())
                    {
                        /*
                         * OTSPoint3D lastPoint = coordinates.get(coordinates.size()-1); double xDiff = lastPoint.x - point.x;
                         * double yDiff = lastPoint.y - point.y; double distance = (float) Math.sqrt(xDiff * xDiff + yDiff *
                         * yDiff);
                         */
                        // if(distance > 0.01)
                        // points.add(point1);
                        if (lastPoint.x != point.x && lastPoint.y != point.y)
                        {
                            points.add(point1);
                            lastPoint = point1;
                        }
                    }
                }
            }

            // OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
            // coordinates = (OTSPoint3D[]) points.toArray();
            OTSLine3D designLine = new OTSLine3D(points);
            String sublinkId = roadTag.id + "." + Integer.toString(roadTag.lanesTag.laneSectionTags.size());
            CrossSectionLink sublink = new CrossSectionLink(openDriveNetworkLaneParser.network, sublinkId, from.node, to.node,
                    openDriveNetworkLaneParser.network.getLinkType(LinkType.DEFAULTS.ROAD), designLine, simulator,
                    LaneKeepingPolicy.KEEPLANE);

            roadTag.subLinks.add(sublink);

            if (!otsNetwork.containsNode(from.node))
                otsNetwork.addNode(from.node);
            if (!otsNetwork.containsNode(to.node))
                otsNetwork.addNode(to.node);

            if (!otsNetwork.containsLink(sublink))
                otsNetwork.addLink(sublink);
            else
                System.err.println("Sublink already registered: " + sublink);
        }

    }

    /**
     * @param roadTag RoadTag; the road tag
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NetworkException on network error
     * @throws OTSGeometryException on geometry or position error
     * @throws NamingException on error registering the animation
     * @throws RemoteException on error reaching the animation or simulator
     */
    static void generateRegularRoads(RoadTag roadTag, DEVSSimulatorInterface.TimeDoubleUnit simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser)
            throws OTSGeometryException, NetworkException, NamingException, RemoteException
    {
        // if (roadTag.junctionId.equals("-1"))
        for (int laneSecIndex = 0; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
        {
            LaneSectionTag currentLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);

            CrossSectionLink currentLink = roadTag.subLinks.get(laneSecIndex);

            Length ds = new Length(0.0, LengthUnit.METER);
            LaneSectionTag nextLaneSec;
            if (laneSecIndex != roadTag.lanesTag.laneSectionTags.size() - 1)
            {
                nextLaneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex + 1);
                ds = nextLaneSec.s.minus(currentLaneSec.s);
            }
            else
            {
                ds = roadTag.length.minus(currentLaneSec.s);
            }

            CrossSectionElement lastLane = null;

            // show center lanes
            int centerLaneSize = currentLaneSec.centerLaneTags.size();
            if (centerLaneSize != 1)
                System.err.println("Sth is wrong in center lane");
            Length centerOffset = new Length(0.0, LengthUnit.METER);

            LaneTag centerLane = currentLaneSec.centerLaneTags.get(0);
            Length laneWidth = new Length(0.0, LengthUnit.METER);
            if (centerLane.widthTags.size() != 0)
                System.err.println("error in show center stripe!");

            Stripe centerStripe = new Stripe(currentLink, centerOffset, centerOffset, laneWidth);
            try
            {
                Renderable2D<Stripe> animation = new StripeAnimation(centerStripe, simulator, StripeAnimation.TYPE.SOLID);
                openDriveNetworkLaneParser.animationMap.put(centerStripe, animation);
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }

            lastLane = centerStripe;

            // show left lanes
            int leftLaneSize = currentLaneSec.leftLaneTags.size();
            // Length leftOffset_start = lastLane.getDesignLineOffsetAtBegin();
            // Length leftOffset_end = lastLane.getDesignLineOffsetAtEnd();

            for (int leftLaneIndex = 1; leftLaneIndex <= leftLaneSize; leftLaneIndex++)
            {
                LaneTag leftLane = currentLaneSec.leftLaneTags.get(leftLaneIndex);

                List<CrossSectionSlice> crossSectionSlices = new ArrayList<CrossSectionSlice>();
                if (leftLane.widthTags.size() == 1)
                {
                    leftLane.widthTags.get(0).sOffst =
                            leftLane.widthTags.get(0).a.plus(leftLane.widthTags.get(0).b.multiplyBy(ds.doubleValue()))
                                    .plus(leftLane.widthTags.get(0).c.multiplyBy(Math.pow(ds.doubleValue(), 2)))
                                    .plus(leftLane.widthTags.get(0).d.multiplyBy(Math.pow(ds.doubleValue(), 3)));

                    Length laneWidth_start = leftLane.widthTags.get(0).a;
                    Length laneWidth_end = leftLane.widthTags.get(0).sOffst;

                    Length leftOffset_start = lastLane.getDesignLineOffsetAtBegin()
                            .plus(lastLane.getBeginWidth().multiplyBy(0.5)).plus(laneWidth_start.multiplyBy(0.5));
                    Length leftOffset_end = lastLane.getDesignLineOffsetAtEnd().plus(lastLane.getEndWidth().multiplyBy(0.5))
                            .plus(laneWidth_end.multiplyBy(0.5));

                    Length length = currentLink.getLength();

                    CrossSectionSlice startSlice =
                            new CrossSectionSlice(new Length(0.0, LengthUnit.METER), leftOffset_start, laneWidth_start);
                    CrossSectionSlice endSlice = new CrossSectionSlice(length, leftOffset_end, laneWidth_end);
                    crossSectionSlices.add(startSlice);
                    crossSectionSlices.add(endSlice);

                }
                else
                {
                    // if(roadTag.id.equals("54048"))
                    // System.out.println();
                    Length lengthofLane = leftLane.widthTags.get(leftLane.widthTags.size() - 1).sOffst;
                    for (WidthTag widthTag : leftLane.widthTags)
                    {
                        Length relativeLength = widthTag.sOffst;
                        double factor = relativeLength.divideBy(lengthofLane).doubleValue();

                        if (factor < 0.98)
                        {
                            Length width = widthTag.a.plus(widthTag.b.multiplyBy(relativeLength.doubleValue()))
                                    .plus(widthTag.c.multiplyBy(Math.pow(relativeLength.doubleValue(), 2)))
                                    .plus(widthTag.d.multiplyBy(Math.pow(relativeLength.doubleValue(), 3)));

                            Length offSet = lastLane.getLateralCenterPosition(factor)
                                    .plus(lastLane.getWidth(factor).multiplyBy(0.5)).plus(width.multiplyBy(0.5));

                            relativeLength = currentLink.getLength().multiplyBy(factor);

                            CrossSectionSlice slice = new CrossSectionSlice(relativeLength, offSet, width);
                            crossSectionSlices.add(slice);
                        }
                        else
                        {
                            CrossSectionSlice lastSlice = crossSectionSlices.get(crossSectionSlices.size() - 1);
                            Length width = lastSlice.getWidth();
                            Length offSet = lastSlice.getDesignLineOffset();
                            relativeLength = currentLink.getLength();

                            CrossSectionSlice slice = new CrossSectionSlice(relativeLength, offSet, width);
                            crossSectionSlices.add(slice);
                            break;
                        }
                    }
                }

                Speed speed = null;
                if (leftLane.speedTags.size() > 0)
                    speed = leftLane.speedTags.get(0).max;
                if (speed == null)
                {
                    // System.err.println("speed.max == null for " + leftLane.id.toString());
                    speed = new Speed(30.0, SpeedUnit.MILE_PER_HOUR);
                }

                Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                speedLimit.put(openDriveNetworkLaneParser.network.getGtuType(GTUType.DEFAULTS.VEHICLE), speed);

                if (leftLane.type.equals("driving"))
                {
                    LongitudinalDirectionality direction = LongitudinalDirectionality.DIR_MINUS;
                    Color color = Color.gray;

                    /*
                     * Lane lane = new Lane(currentLink, leftLane.id.toString(), leftOffset_start, leftOffset_end,
                     * laneWidth_start, laneWidth_end, LANETYPE_ALL, directionality, speedLimit, overtakingConditions);
                     */

                    Lane lane = new Lane(currentLink, leftLane.id.toString(), crossSectionSlices,
                            openDriveNetworkLaneParser.network.getLaneType(LaneType.DEFAULTS.FREEWAY), speedLimit);
                    currentLaneSec.lanes.put(leftLane.id, lane);

                    lastLane = lane;

                    try
                    {
                        Renderable2D animation = new LaneAnimationOD(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (RemoteException exception)
                    {
                        Renderable2D animation =
                                new org.opentrafficsim.draw.network.LinkAnimation(currentLink, simulator, 0.01f);
                        openDriveNetworkLaneParser.animationMap.put(currentLink, animation);
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("sidewalk"))
                {
                    Color color = Color.darkGray;
                    /*
                     * Lane lane = new NoTrafficLane(currentLink, leftLane.id.toString(), leftOffset_start, leftOffset_end,
                     * laneWidth_start, laneWidth_end);
                     */
                    Lane lane = new NoTrafficLane(currentLink, leftLane.id.toString(), crossSectionSlices);

                    currentLaneSec.lanes.put(leftLane.id, lane);

                    lastLane = lane;

                    try
                    {
                        Renderable2D animation = new LaneAnimation(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("border"))
                {
                    Stripe solidLine = new Stripe(currentLink, crossSectionSlices, Permeable.BOTH);

                    lastLane = solidLine;

                    try
                    {
                        Renderable2D animation = new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                        openDriveNetworkLaneParser.animationMap.put(solidLine, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("shoulder"))
                {
                    Color color = Color.green;
                    Shoulder shoulder = new Shoulder(currentLink, leftLane.id.toString(), crossSectionSlices);
                    lastLane = shoulder;

                    try
                    {
                        Renderable2D animation = new ShoulderAnimation(shoulder, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(shoulder, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else

                {
                    /*
                     * Stripe solidLine = new Stripe(currentLink, leftOffset, laneWidth); try { new StripeAnimation(solidLine,
                     * simulator, StripeAnimation.TYPE.SOLID); } catch (RemoteException exception) {
                     * exception.printStackTrace(); }
                     */

                    Color color = Color.green;

                    try
                    {
                        Lane lane = new NoTrafficLane(currentLink, leftLane.id.toString(), crossSectionSlices);

                        currentLaneSec.lanes.put(leftLane.id, lane);

                        lastLane = lane;
                        Renderable2D animation = new LaneAnimation(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (Exception exception)
                    {
                        Renderable2D animation = new LinkAnimation(currentLink, simulator, 0.01f);
                        openDriveNetworkLaneParser.animationMap.put(currentLink, animation);
                        exception.printStackTrace();
                    }
                }

            }

            lastLane = centerStripe;

            // show right lanes
            int rightLaneSize = currentLaneSec.rightLaneTags.size();
            // Length rightOffset_start = new Length(0.0, LengthUnit.METER);
            // Length rightOffset_end = new Length(0.0, LengthUnit.METER);

            for (int rightLaneIndex = 1; rightLaneIndex <= rightLaneSize; rightLaneIndex++)
            {
                LaneTag rightLane = currentLaneSec.rightLaneTags.get(-rightLaneIndex);

                List<CrossSectionSlice> crossSectionSlices = new ArrayList<CrossSectionSlice>();
                if (rightLane.widthTags.size() == 1)
                {
                    rightLane.widthTags.get(0).sOffst =
                            rightLane.widthTags.get(0).a.plus(rightLane.widthTags.get(0).b.multiplyBy(ds.doubleValue()))
                                    .plus(rightLane.widthTags.get(0).c.multiplyBy(Math.pow(ds.doubleValue(), 2)))
                                    .plus(rightLane.widthTags.get(0).d.multiplyBy(Math.pow(ds.doubleValue(), 3)));

                    Length laneWidth_start = rightLane.widthTags.get(0).a;
                    Length laneWidth_end = rightLane.widthTags.get(0).sOffst;

                    Length leftOffset_start = lastLane.getDesignLineOffsetAtBegin()
                            .minus(lastLane.getBeginWidth().multiplyBy(0.5)).minus(laneWidth_start.multiplyBy(0.5));
                    Length leftOffset_end = lastLane.getDesignLineOffsetAtEnd().minus(lastLane.getEndWidth().multiplyBy(0.5))
                            .minus(laneWidth_end.multiplyBy(0.5));

                    Length length = currentLink.getLength();

                    CrossSectionSlice startSlice =
                            new CrossSectionSlice(new Length(0.0, LengthUnit.METER), leftOffset_start, laneWidth_start);
                    CrossSectionSlice endSlice = new CrossSectionSlice(length, leftOffset_end, laneWidth_end);
                    crossSectionSlices.add(startSlice);
                    crossSectionSlices.add(endSlice);

                }
                else
                {
                    // if(roadTag.id.equals("54072"))
                    // System.out.println();
                    Length lengthofLane = rightLane.widthTags.get(rightLane.widthTags.size() - 1).sOffst;
                    for (WidthTag widthTag : rightLane.widthTags)
                    {
                        Length relativeLength = widthTag.sOffst;
                        double factor = relativeLength.divideBy(lengthofLane).doubleValue();

                        if (factor < 0.98)
                        {
                            Length width = widthTag.a.plus(widthTag.b.multiplyBy(relativeLength.doubleValue()))
                                    .plus(widthTag.c.multiplyBy(Math.pow(relativeLength.doubleValue(), 2)))
                                    .plus(widthTag.d.multiplyBy(Math.pow(relativeLength.doubleValue(), 3)));

                            Length offSet = lastLane.getLateralCenterPosition(factor)
                                    .minus(lastLane.getWidth(factor).multiplyBy(0.5)).minus(width.multiplyBy(0.5));

                            relativeLength = currentLink.getLength().multiplyBy(factor);

                            CrossSectionSlice slice = new CrossSectionSlice(relativeLength, offSet, width);
                            crossSectionSlices.add(slice);
                        }
                        else
                        {
                            CrossSectionSlice lastSlice = crossSectionSlices.get(crossSectionSlices.size() - 1);
                            Length width = lastSlice.getWidth();
                            Length offSet = lastSlice.getDesignLineOffset();
                            relativeLength = currentLink.getLength();

                            CrossSectionSlice slice = new CrossSectionSlice(relativeLength, offSet, width);
                            crossSectionSlices.add(slice);
                            break;
                        }

                    }
                }

                Speed speed = null;
                if (rightLane.speedTags.size() > 0)
                    speed = rightLane.speedTags.get(0).max;
                if (speed == null)
                {
                    // System.err.println("speed.max == null for " + rightLane.id.toString());
                    speed = new Speed(30.0, SpeedUnit.MILE_PER_HOUR);
                }

                Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                speedLimit.put(openDriveNetworkLaneParser.network.getGtuType(GTUType.DEFAULTS.VEHICLE), speed);

                if (rightLane.type.equals("driving"))
                {
                    LongitudinalDirectionality direction = LongitudinalDirectionality.DIR_PLUS;
                    // if(roadTag.link.getEndNode().getLinksOut().size() == 0)
                    // direction = LongitudinalDirectionality.BACKWARD;
                    Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                    directionality.put(openDriveNetworkLaneParser.network.getGtuType(GTUType.DEFAULTS.VEHICLE), direction);
                    Color color = Color.gray;

                    try
                    {
                        // if(roadTag.id.equals("385351")||roadTag.id.equals("385359"))
                        // System.out.println();

                        Lane lane = new Lane(currentLink, rightLane.id.toString(), crossSectionSlices,
                                openDriveNetworkLaneParser.network.getLaneType(LaneType.DEFAULTS.FREEWAY), speedLimit);

                        currentLaneSec.lanes.put(rightLane.id, lane);

                        lastLane = lane;

                        Renderable2D animation = new LaneAnimationOD(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (Exception exception)
                    {
                        Renderable2D animation = new LinkAnimation(currentLink, simulator, 0.01f);
                        openDriveNetworkLaneParser.animationMap.put(currentLink, animation);
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("sidewalk"))
                {
                    Color color = Color.darkGray;
                    Lane lane = new NoTrafficLane(currentLink, rightLane.id.toString(), crossSectionSlices);

                    currentLaneSec.lanes.put(rightLane.id, lane);

                    lastLane = lane;

                    try
                    {
                        Renderable2D animation = new LaneAnimation(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("border"))
                {
                    Stripe solidLine = new Stripe(currentLink, crossSectionSlices, Permeable.BOTH);

                    lastLane = solidLine;
                    try
                    {
                        Renderable2D animation = new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                        openDriveNetworkLaneParser.animationMap.put(solidLine, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("shoulder"))
                {
                    Color color = Color.green;
                    Shoulder shoulder = new Shoulder(currentLink, rightLane.id.toString(), crossSectionSlices);
                    lastLane = shoulder;

                    try
                    {
                        Renderable2D animation = new ShoulderAnimation(shoulder, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(shoulder, animation);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    Color color = Color.green;

                    try
                    {
                        Lane lane = new NoTrafficLane(currentLink, rightLane.id.toString(), crossSectionSlices);

                        currentLaneSec.lanes.put(rightLane.id, lane);
                        lastLane = lane;
                        Renderable2D animation = new LaneAnimation(lane, simulator, color);
                        openDriveNetworkLaneParser.animationMap.put(lane, animation);
                    }
                    catch (Exception exception)
                    {
                        Renderable2D animation = new LinkAnimation(currentLink, simulator, 0.01f);
                        openDriveNetworkLaneParser.animationMap.put(currentLink, animation);
                        exception.printStackTrace();
                    }
                }

            }

        }
    }

    /**
     * @param roadTag RoadTag; the road tag
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public static void buildLink(RoadTag roadTag, OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws NetworkException
    {
        if (roadTag.junctionId == null)
            System.out.println("sth is wrong in building links");

        if (!roadTag.junctionId.equals("-1"))
        {
            RoadTag predecessorRoadTag = openDriveNetworkLaneParser.roadTags.get(roadTag.linkTag.predecessorId);
            RoadTag successorRoadTag = openDriveNetworkLaneParser.roadTags.get(roadTag.linkTag.successorId);

            OTSRoadNode from = null;

            if (roadTag.linkTag.predecessorContactPoint.equals(ContactPointEnum.START))
                from = predecessorRoadTag.startNode;
            else if (roadTag.linkTag.predecessorContactPoint.equals(ContactPointEnum.END))
                from = predecessorRoadTag.endNode;
            else
                System.out.println("sth is wrong in building links");

            OTSRoadNode to = null;

            if (roadTag.linkTag.successorContactPoint.equals(ContactPointEnum.START))
                to = successorRoadTag.startNode;
            else if (roadTag.linkTag.successorContactPoint.equals(ContactPointEnum.END))
                to = successorRoadTag.endNode;
            else
                System.out.println("sth is wrong in building links");

            roadTag.startNode = from;
            roadTag.endNode = to;

            CrossSectionLink newlink = new CrossSectionLink(openDriveNetworkLaneParser.network, roadTag.id, from, to,
                    openDriveNetworkLaneParser.network.getLinkType(LinkType.DEFAULTS.ROAD), roadTag.designLine,
                    openDriveNetworkLaneParser.simulator, LaneKeepingPolicy.KEEPLANE);
            roadTag.link = newlink;

            roadTag.link = newlink;

        }
        else
        {
            OTSRoadNode from = roadTag.startNode;
            OTSRoadNode to = roadTag.endNode;
            CrossSectionLink newlink = new CrossSectionLink(openDriveNetworkLaneParser.network, roadTag.id, from, to,
                    openDriveNetworkLaneParser.network.getLinkType(LinkType.DEFAULTS.ROAD), roadTag.designLine,
                    openDriveNetworkLaneParser.simulator, LaneKeepingPolicy.KEEPLANE);

            roadTag.link = newlink;
        }
    }

    /**
     * @param roadTag RoadTag; the road tag
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NetworkException on network error
     */
    public static void generateTrafficLightsbySignal(final RoadTag roadTag,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator, final OpenDriveNetworkLaneParser openDriveNetworkLaneParser)
            throws NetworkException
    {
        for (SignalTag signalTag : roadTag.signalsTag.signalTags)
        {
            // Length sOffset = signalTag.s;
            // Length tOffset = signalTag.t;
            // String id = signalTag.id;

            LaneSectionTag laneSec = roadTag.lanesTag.findDrivingLaneSec(signalTag.s);
            Lane lane = laneSec.findLanes(signalTag.orientation).get(0);

            if (signalTag.type.equals("1000001") && signalTag.dynamic.equals("yes"))// generate traffic lights
            {
                try
                {

                    Length sOffset = null;

                    if (!openDriveNetworkLaneParser.trafficLightsByLanes.containsKey(roadTag.id))
                        sOffset = signalTag.s.minus(laneSec.s);
                    else
                        sOffset = signalTag.s.minus(laneSec.s).plus(new Length(0.5, LengthUnit.METER));

                    Class<?> clazz = Class.forName(SimpleTrafficLight.class.getName());
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(clazz,
                            new Class[] {String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});

                    SimpleTrafficLight trafficLight = (SimpleTrafficLight) trafficLightConstructor
                            .newInstance(new Object[] {signalTag.id, lane, sOffset, simulator});

                    if (!openDriveNetworkLaneParser.trafficLightsBySignals.containsKey(signalTag.id))
                    {
                        Set<SimpleTrafficLight> lights = new LinkedHashSet<SimpleTrafficLight>();
                        openDriveNetworkLaneParser.trafficLightsBySignals.put(signalTag.id, lights);
                    }

                    if (!openDriveNetworkLaneParser.trafficLightsByLanes.containsKey(roadTag.id))
                    {
                        Set<SimpleTrafficLight> lights = new LinkedHashSet<SimpleTrafficLight>();
                        openDriveNetworkLaneParser.trafficLightsByLanes.put(roadTag.id, lights);
                    }

                    openDriveNetworkLaneParser.trafficLightsBySignals.get(signalTag.id).add(trafficLight);
                    openDriveNetworkLaneParser.trafficLightsByLanes.get(roadTag.id).add(trafficLight);

                }
                catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException exception)
                {
                    throw new NetworkException(
                            "Traffic Light: CLASS NAME " + SimpleTrafficLight.class.getName() + " for " + signalTag.id
                                    + " on lane " + lane.toString() + " -- class not found or constructor not right",
                            exception);
                }
            }
            else if (signalTag.type.equals("206") && signalTag.dynamic.equals("no"))// generate stop sign
            {

            }
            else
                System.err.println("Unknown signals");
        }
    }

    /**
     * @param roadTag RoadTag; the road tag
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NetworkException on network error
     */
    public static void generateTrafficLightsbySignalReference(RoadTag roadTag, DEVSSimulatorInterface.TimeDoubleUnit simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws NetworkException
    {
        for (SignalReferenceTag signalReferenceTag : roadTag.signalsTag.signalReferenceTag)
        {
            LaneSectionTag laneSec = roadTag.lanesTag.findDrivingLaneSec(signalReferenceTag.s);
            Lane lane = laneSec.findLanes(signalReferenceTag.orientation).get(0);

            SignalTag signalTag = openDriveNetworkLaneParser.signalTags.get(signalReferenceTag.id);

            if (signalTag.type.equals("1000001") && signalTag.dynamic.equals("yes"))// generate traffic lights
            {
                try
                {
                    Length sOffset = null;

                    if (!openDriveNetworkLaneParser.trafficLightsByLanes.containsKey(roadTag.id))
                        sOffset = signalReferenceTag.s.minus(laneSec.s);
                    else
                        sOffset = signalReferenceTag.s.minus(laneSec.s).plus(new Length(0.5, LengthUnit.METER));

                    Class<?> clazz = Class.forName(SimpleTrafficLight.class.getName());
                    Constructor<?> trafficLightConstructor = ClassUtil.resolveConstructor(clazz,
                            new Class[] {String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});

                    SimpleTrafficLight trafficLight = (SimpleTrafficLight) trafficLightConstructor
                            .newInstance(new Object[] {signalTag.id + ".ref", lane, sOffset, simulator});

                    if (!openDriveNetworkLaneParser.trafficLightsByLanes.containsKey(roadTag.id))
                    {
                        Set<SimpleTrafficLight> lights = new LinkedHashSet<SimpleTrafficLight>();
                        openDriveNetworkLaneParser.trafficLightsByLanes.put(roadTag.id, lights);
                    }

                    // openDriveNetworkLaneParser.trafficLightsBySignals.put(trafficLight.getId(), trafficLight);
                    openDriveNetworkLaneParser.trafficLightsBySignals.get(signalTag.id).add(trafficLight);
                    openDriveNetworkLaneParser.trafficLightsByLanes.get(roadTag.id).add(trafficLight);

                }
                catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException exception)
                {
                    throw new NetworkException(
                            "Traffic Light: CLASS NAME " + SimpleTrafficLight.class.getName() + " for " + signalTag.id
                                    + " on lane " + lane.toString() + " -- class not found or constructor not right",
                            exception);
                }
            }
            else if (signalTag.type.equals("206") && signalTag.dynamic.equals("no"))// generate stop sign
            {

            }
            else
                System.err.println("Unknown signal references");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RoadTag [id=" + this.id + ", name=" + this.name + ", length=" + this.length + ", junctionId=" + this.junctionId
                + ", linkTag=" + this.linkTag + ", planViewTag=" + this.planViewTag + ", elevationProfileTag="
                + this.elevationProfileTag + ", lateralProfileTag=" + this.lateralProfileTag + ", lanesTag=" + this.lanesTag
                + ", signalsTag=" + this.signalsTag + ", objectsTag=" + this.objectsTag + ", typeTag=" + this.typeTag
                + ", link=" + this.link + ", designLine=" + this.designLine + ", startNode=" + this.startNode + ", endNode="
                + this.endNode + ", subLinks=" + this.subLinks + "]";
    }

}
