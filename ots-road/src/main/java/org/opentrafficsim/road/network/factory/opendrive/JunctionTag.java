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
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.lane.AbstractTrafficLight;
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

    /**
     * @param juncTag
     * @param simulator
     * @param openDriveNetworkLaneParser
     * @throws NamingException 
     * @throws NetworkException 
     * @throws GTUException 
     */
    public static void createController(JunctionTag juncTag, OTSDEVSSimulatorInterface simulator,
            OpenDriveNetworkLaneParser openDriveNetworkLaneParser) throws GTUException, NetworkException, NamingException
    {
        if(juncTag.controllerTags.size() > 0)
        {
            Controller controller = new Controller(juncTag.id, simulator);
            
            for(ControllerTag controllerTag: juncTag.controllerTags.values())
            {
                int sequence = controllerTag.sequence;
                String id = controllerTag.id;
                String signalId = openDriveNetworkLaneParser.controllerTags.get(id).controlSignalID;
                
                //AbstractTrafficLight trafficLight = openDriveNetworkLaneParser.trafficLightsBySignals.get(signalId);
                
                for(AbstractTrafficLight trafficLight: openDriveNetworkLaneParser.trafficLightsBySignals.get(signalId))
                    controller.addTrafficLight(sequence, trafficLight);
                
/*                String refId = signalId + ".ref";
                if(openDriveNetworkLaneParser.trafficLightsBySignals.containsKey(refId))
                {
                    AbstractTrafficLight trafficLightRef = openDriveNetworkLaneParser.trafficLightsBySignals.get(refId);
                    controller.addTrafficLight(sequence, trafficLightRef);
                } */              
            }
        }
    }


}
