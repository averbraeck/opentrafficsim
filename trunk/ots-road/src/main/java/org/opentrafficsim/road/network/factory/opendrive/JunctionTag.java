package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.network.animation.LaneAnimation;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.opentrafficsim.road.network.factory.opendrive.LinkTag.ContactPointEnum;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
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
class JunctionTag
{
    /** name of the junction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** unique ID within database. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** a map of connections in the junction */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, ConnectionTag> connectionTags = new HashMap<String, ConnectionTag>();

    /** a map of controller in the junction */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, ControllerTag> controllerTags = new HashMap<String, ControllerTag>();

    /**
     * Parse the attributes of the junction tag. The sub-elements are parsed in separate classes.
     * @param node the junction node to parse
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseJunction(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException,
        NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        JunctionTag junctionTag = new JunctionTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("JUNCTION: missing attribute ID");
        junctionTag.id = id.getNodeValue().trim();
        if (parser.roadTags.keySet().contains(junctionTag.id))
            throw new SAXException("JUNCTION: ID " + junctionTag.id + " defined twice");

        Node name = attributes.getNamedItem("name");
        if (name == null)
            throw new SAXException("JUNCTION: missing attribute NAME for ID=" + junctionTag.id);
        junctionTag.name = name.getNodeValue().trim();

        for (Node connectionNode : XMLParser.getNodes(node.getChildNodes(), "connection"))
        {
            ConnectionTag connectionTag = ConnectionTag.parseConnection(connectionNode, parser);
            junctionTag.connectionTags.put(connectionTag.connectingRoad, connectionTag);
        }

        for (Node connectionNode : XMLParser.getNodes(node.getChildNodes(), "controller"))
        {
            ControllerTag controllerTag = ControllerTag.parseController(connectionNode, parser);
            junctionTag.controllerTags.put(controllerTag.id, controllerTag);
        }

        parser.junctionTags.put(junctionTag.id, junctionTag);
    }

/*    *//**
     * @param juncTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NetworkException
     * @throws OTSGeometryException
     * @throws NamingException
     *//*
    public static void showJunctions(JunctionTag juncTag, OTSDEVSSimulatorInterface simulator,
        OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws NetworkException, OTSGeometryException,
        NamingException
    {
        for (ConnectionTag connectionTag : juncTag.connectionTags.values())
        {
            RoadTag inComing = openDriveNetworkLaneParser.roadTags.get(connectionTag.incomingRoad);
            RoadTag connecting = openDriveNetworkLaneParser.roadTags.get(connectionTag.connectingRoad);

            Lane inComingLane = null;
            Lane connectingLane = null;
            String sublinkId = juncTag.id + "." + connectionTag.id;
            CrossSectionLink sublink = null;

            List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();

            OTSLine3D designLine = null;

            if (inComing.linkTag.successorType != null && inComing.linkTag.successorType.equals("junction")
                && inComing.linkTag.successorId.equals(juncTag.id))
            {
                inComingLane =
                    inComing.lanesTag.laneSectionTags.get(inComing.lanesTag.laneSectionTags.size() - 1).lanes
                        .get(connectionTag.laneLinkFrom);
                connectingLane = connecting.lanesTag.laneSectionTags.get(0).lanes.get(connectionTag.laneLinkTo);
            }
            else if (inComing.linkTag.predecessorType.equals("junction")
                && inComing.linkTag.predecessorId.equals(juncTag.id))
            {
                inComingLane =
                    connecting.lanesTag.laneSectionTags.get(connecting.lanesTag.laneSectionTags.size() - 1).lanes
                        .get(connectionTag.laneLinkFrom);
                connectingLane = inComing.lanesTag.laneSectionTags.get(0).lanes.get(connectionTag.laneLinkTo);
            }
            else
            {
                System.err.println("err in junctions!");
            }

            OTSNode from1 = inComing.link.getStartNode();
            OTSNode from2 = inComing.link.getEndNode();
            OTSNode from = null;

            OTSNode to1 = connecting.link.getStartNode();
            OTSNode to2 = connecting.link.getEndNode();
            OTSNode to = null;

            double dis1 = from1.getPoint().getCoordinate().distance(to1.getPoint().getCoordinate());
            double dis2 = from1.getPoint().getCoordinate().distance(to2.getPoint().getCoordinate());

            double dis3 = from2.getPoint().getCoordinate().distance(to1.getPoint().getCoordinate());
            double dis4 = from2.getPoint().getCoordinate().distance(to2.getPoint().getCoordinate());

            if (dis1 < dis2 && dis3 < dis4)
            {
                if (dis1 < dis3)
                {
                    from = from1;
                    to = to1;
                }
                else
                {
                    from = from2;
                    to = to1;
                }

            }
            else if (dis1 > dis2 && dis3 < dis4)
            {
                if (dis2 < dis3)
                {
                    from = from1;
                    to = to2;
                }
                else
                {
                    from = from2;
                    to = to1;
                }
            }
            else if (dis1 < dis2 && dis3 > dis4)
            {
                if (dis1 < dis4)
                {
                    from = from1;
                    to = to1;
                }
                else
                {
                    from = from2;
                    to = to2;
                }
            }
            else if (dis1 > dis2 && dis3 > dis4)
            {
                if (dis2 < dis4)
                {
                    from = from1;
                    to = to2;
                }
                else
                {
                    from = from2;
                    to = to2;
                }
            }

            coordinates.add(from.getPoint());

            coordinates.add(to.getPoint());

            if (from.equals(to))
                return;

        }
    }*/
}
