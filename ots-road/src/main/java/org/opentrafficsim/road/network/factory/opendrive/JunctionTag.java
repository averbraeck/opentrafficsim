package org.opentrafficsim.road.network.factory.opendrive;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

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
import org.opentrafficsim.road.network.factory.XMLParser;
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
            junctionTag.connectionTags.put(connectionTag.id, connectionTag);
        }
        
        for (Node connectionNode : XMLParser.getNodes(node.getChildNodes(), "controller"))
        {
            ControllerTag controllerTag = ControllerTag.parseController(connectionNode, parser);
            junctionTag.controllerTags.put(controllerTag.id, controllerTag);
        }
        
        parser.junctionTags.put(junctionTag.id, junctionTag);
    }

    /**
     * @param juncTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NetworkException 
     * @throws OTSGeometryException 
     * @throws NamingException 
     */
    public static void showJunctions(JunctionTag juncTag, OTSDEVSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws NetworkException, OTSGeometryException, NamingException
    {
        for(ConnectionTag connectionTag: juncTag.connectionTags.values())
        {
            RoadTag inComing = openDriveNetworkLaneParser.roadTags.get(connectionTag.incomingRoad);
            RoadTag connecting = openDriveNetworkLaneParser.roadTags.get(connectionTag.connectingRoad);
            
            Lane inComingLane = null;
            Lane connectingLane = null;
            String sublinkId = juncTag.id + "." + connectionTag.id;
            CrossSectionLink sublink = null;
            
            if(inComing.linkTag.successorType !=null && inComing.linkTag.successorType.equals("junction")&&inComing.linkTag.successorId.equals(juncTag.id))
            {
                List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();
                coordinates.add(inComing.link.getEndNode().getPoint());
                coordinates.add(connecting.link.getStartNode().getPoint());
                
                OTSLine3D designLine = new OTSLine3D(coordinates);                           
                
                sublink =
                    new CrossSectionLink(sublinkId, inComing.link.getEndNode(), connecting.link.getStartNode(), LinkType.ALL, designLine,
                        LaneKeepingPolicy.KEEP_LANE);

                openDriveNetworkLaneParser.network.addLink(sublink);                
                
                inComingLane = inComing.lanesTag.laneSectionTags.get(inComing.lanesTag.laneSectionTags.size()-1).lanes.get(connectionTag.laneLinkFrom);
                connectingLane = connecting.lanesTag.laneSectionTags.get(0).lanes.get(connectionTag.laneLinkTo);
            }
            else if(inComing.linkTag.predecessorType.equals("junction")&&inComing.linkTag.predecessorId.equals(juncTag.id))
            {
                List<OTSPoint3D> coordinates = new ArrayList<OTSPoint3D>();
                coordinates.add( connecting.link.getEndNode().getPoint());
                coordinates.add(inComing.link.getStartNode().getPoint());
                
                OTSLine3D designLine = new OTSLine3D(coordinates);                           
                
                sublink =
                    new CrossSectionLink(sublinkId, connecting.link.getEndNode(), inComing.link.getStartNode(), LinkType.ALL, designLine,
                        LaneKeepingPolicy.KEEP_LANE);

                openDriveNetworkLaneParser.network.addLink(sublink);                
                
                inComingLane = connecting.lanesTag.laneSectionTags.get(connecting.lanesTag.laneSectionTags.size()-1).lanes.get(connectionTag.laneLinkFrom);
                connectingLane = inComing.lanesTag.laneSectionTags.get(0).lanes.get(connectionTag.laneLinkTo);
            }
            else
            {
                System.err.println("err in junctions!");
            }
            

            
            OvertakingConditions overtakingConditions = null;

            Speed speed = null;            

/*            if (connectingLane.getSpeedLimit(GTUType.ALL) != null)
                speed = connectingLane.getSpeedLimit(GTUType.ALL);
            if (inComingLane.getSpeedLimit(GTUType.ALL) != null)
                speed = inComingLane.getSpeedLimit(GTUType.ALL);*/

            Map<GTUType, Speed> speedLimit = new LinkedHashMap<>();
            speedLimit.put(GTUType.ALL, speed);
            
            LongitudinalDirectionality direction = LongitudinalDirectionality.FORWARD;
            Map<GTUType, LongitudinalDirectionality> directionality = new LinkedHashMap<>();
            directionality.put(GTUType.ALL, direction);
            Color color = Color.red;

            if(inComingLane != null && connectingLane != null)
                try
                {
                    Lane lane =
                            new Lane(sublink, sublinkId, inComingLane.getDesignLineOffsetAtEnd(),
                                    connectingLane.getDesignLineOffsetAtBegin(), inComingLane.getEndWidth(),
                                    connectingLane.getBeginWidth(), LaneType.NONE, directionality, speedLimit,
                                    overtakingConditions);
                    new LaneAnimation(lane, simulator, color);
                    
                    //new LinkAnimation(sublink, simulator, 1000.0f);
                } catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }
            
        }
    }
}
