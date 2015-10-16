package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.animation.ShoulderAnimation;
import org.opentrafficsim.road.network.animation.StripeAnimation;
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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
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

    /** the calculated Link. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<CrossSectionLink> subLinks = new ArrayList<>();

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
        if (!junctionId.getNodeValue().trim().equals("-1"))
        {
            roadTag.junctionId = junctionId.getNodeValue().trim();
            if (!parser.junctionTags.keySet().contains(roadTag.junctionId))
                throw new SAXException("ROAD: junction id=" + roadTag.junctionId + " for road id=" + roadTag.id
                    + " not defined as a junction in the XML-file");
        }

        return roadTag;
    }

    /**
     * @param roadTag
     * @param simulator
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     */
    static void showLanes(RoadTag roadTag, OTSDEVSSimulatorInterface simulator) throws OTSGeometryException,
        NetworkException, NamingException
    {
        if (roadTag.lanesTag.laneSectionTags.size() == 1)// no sub links
            roadTag.subLinks.add(roadTag.link);
        else
        {
            // build fist several sub links
            List<GeometryTag> tempGeometryTags = new ArrayList<GeometryTag>();
            tempGeometryTags = roadTag.planViewTag.geometryTags;

            int currentIndex = 0;
            for (Integer laneSecIndex = 1; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
            {
                LaneSectionTag laneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);
                Length.Rel endNode = laneSec.s;

                List<OTSPoint3D> points = new ArrayList<OTSPoint3D>();

                GeometryTag from = tempGeometryTags.get(currentIndex);
                GeometryTag to = tempGeometryTags.get(currentIndex);

                for (int indexGeometryTag = currentIndex; indexGeometryTag < tempGeometryTags.size(); indexGeometryTag++)
                {
                    GeometryTag currentGeometryTag = tempGeometryTags.get(indexGeometryTag);
                    if (currentGeometryTag.s.doubleValue() < endNode.doubleValue())
                    {
                        OTSPoint3D point =
                            new OTSPoint3D(currentGeometryTag.x.doubleValue(), currentGeometryTag.y.doubleValue(),
                                currentGeometryTag.hdg.doubleValue());
                        points.add(point);
                        currentIndex++;
                        to = tempGeometryTags.get(currentIndex);
                    }
                    else
                    {
                        OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
                        coordinates = (OTSPoint3D[]) points.toArray();
                        OTSLine3D designLine = new OTSLine3D(coordinates);
                        CrossSectionLink sublink =
                            new CrossSectionLink(laneSecIndex.toString(), from.node, to.node, LinkType.ALL, designLine,
                                LaneKeepingPolicy.KEEP_LANE);
                        roadTag.subLinks.add(sublink);
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

            OTSPoint3D[] coordinates = new OTSPoint3D[points.size()];
            coordinates = (OTSPoint3D[]) points.toArray();
            OTSLine3D designLine = new OTSLine3D(coordinates);
            CrossSectionLink sublink =
                new CrossSectionLink(Integer.toString(roadTag.lanesTag.laneSectionTags.size()), from.node, to.node,
                    LinkType.ALL, designLine, LaneKeepingPolicy.KEEP_LANE);
            roadTag.subLinks.add(sublink);

        }

        for (int laneSecIndex = 0; laneSecIndex < roadTag.lanesTag.laneSectionTags.size(); laneSecIndex++)
        {
            LaneSectionTag laneSec = roadTag.lanesTag.laneSectionTags.get(laneSecIndex);

            CrossSectionLink currentLink = roadTag.subLinks.get(laneSecIndex);

            // show left lanes
            int leftLaneSize = laneSec.leftLaneTags.size();
            Length.Rel leftOffset = new Length.Rel(0.0, LengthUnit.METER);
            for (int leftLaneIndex = 1; leftLaneIndex <= leftLaneSize; leftLaneIndex++)
            {
                LaneTag leftLane = laneSec.leftLaneTags.get(leftLaneIndex);
                Length.Rel laneWidth = leftLane.widthTag.a;

                leftOffset = leftOffset.minus(laneWidth.multiplyBy(0.5));

                OvertakingConditions overtakingConditions = null;

                Speed speed = null;
                if (leftLane.speedTags.size() > 0)
                    speed = leftLane.speedTags.get(0).max;

                Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                speedLimit.put(GTUType.ALL, speed);

                if (leftLane.type.equals("driving"))
                {
                    LongitudinalDirectionality direction = LongitudinalDirectionality.BACKWARD;
                    Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                    directionality.put(GTUType.ALL, direction);
                    Color color = Color.gray;

                    Lane lane =
                        new Lane(currentLink, leftLane.id.toString(), leftOffset, leftOffset, laneWidth, laneWidth,
                            LaneType.NONE, directionality, speedLimit, overtakingConditions);
                    try
                    {
                        new LaneAnimation(lane, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("sidewalk"))
                {
                    Color color = Color.darkGray;
                    Lane lane =
                        new NoTrafficLane(currentLink, leftLane.id.toString(), leftOffset, leftOffset, laneWidth,
                            laneWidth);
                    try
                    {
                        new LaneAnimation(lane, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("border"))
                {
                    Stripe solidLine = new Stripe(currentLink, leftOffset, laneWidth);
                    try
                    {
                        new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (leftLane.type.equals("shoulder"))
                {
                    Color color = Color.green;
                    Shoulder shoulder =
                        new Shoulder(currentLink, leftLane.id.toString(), leftOffset, laneWidth, laneWidth);
                    try
                    {
                        new ShoulderAnimation(shoulder, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    Stripe solidLine = new Stripe(currentLink, leftOffset, laneWidth);
                    try
                    {
                        new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }

                leftOffset = leftOffset.minus(laneWidth.multiplyBy(0.5));
            }

            // show right lanes
            int rightLaneSize = laneSec.rightLaneTags.size();
            Length.Rel rightOffset = new Length.Rel(0.0, LengthUnit.METER);
            for (int rightLaneIndex = 1; rightLaneIndex <= rightLaneSize; rightLaneIndex++)
            {
                LaneTag rightLane = laneSec.rightLaneTags.get(-rightLaneIndex);
                Length.Rel laneWidth = rightLane.widthTag.a;

                rightOffset = rightOffset.plus(laneWidth.multiplyBy(0.5));

                OvertakingConditions overtakingConditions = null;

                Speed speed = null;
                if (rightLane.speedTags.size() > 0)
                    speed = rightLane.speedTags.get(0).max;

                Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
                speedLimit.put(GTUType.ALL, speed);

                if (rightLane.type.equals("driving"))
                {
                    LongitudinalDirectionality direction = LongitudinalDirectionality.FORWARD;
                    Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
                    directionality.put(GTUType.ALL, direction);
                    Color color = Color.gray;

                    try
                    {
                        Lane lane =
                            new Lane(currentLink, rightLane.id.toString(), rightOffset, rightOffset, laneWidth,
                                laneWidth, LaneType.NONE, directionality, speedLimit, overtakingConditions);
                        new LaneAnimation(lane, simulator, color);
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("sidewalk"))
                {
                    Color color = Color.darkGray;
                    Lane lane =
                        new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset, rightOffset, laneWidth,
                            laneWidth);
                    try
                    {
                        new LaneAnimation(lane, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("border"))
                {
                    Stripe solidLine = new Stripe(currentLink, rightOffset, laneWidth);
                    try
                    {
                        new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else if (rightLane.type.equals("shoulder"))
                {
                    Color color = Color.green;
                    Shoulder shoulder =
                        new Shoulder(currentLink, rightLane.id.toString(), rightOffset, laneWidth, laneWidth);
                    try
                    {
                        new ShoulderAnimation(shoulder, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    Color color = Color.LIGHT_GRAY;
                    Lane lane =
                        new NoTrafficLane(currentLink, rightLane.id.toString(), rightOffset, rightOffset, laneWidth,
                            laneWidth);
                    try
                    {
                        new LaneAnimation(lane, simulator, color);
                    }
                    catch (RemoteException exception)
                    {
                        exception.printStackTrace();
                    }
                }

                rightOffset = rightOffset.plus(laneWidth.multiplyBy(0.5));
            }

            // show center lanes
            int centerLaneSize = laneSec.centerLaneTags.size();
            if (centerLaneSize != 1)
                System.err.println("Sth is wrong in center lane");
            Length.Rel centerOffset = new Length.Rel(0.0, LengthUnit.METER);

            LaneTag centerLane = laneSec.centerLaneTags.get(0);
            Length.Rel laneWidth = new Length.Rel(0.0, LengthUnit.METER);
            if (centerLane.widthTag != null)
                laneWidth = centerLane.widthTag.a;

            Stripe solidLine = new Stripe(currentLink, centerOffset, laneWidth);
            try
            {
                new StripeAnimation(solidLine, simulator, StripeAnimation.TYPE.SOLID);
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }

        }
    }
}
