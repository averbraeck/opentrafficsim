package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class JunctionTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name of the junction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Unique ID within database. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** A map of connections in the junction */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, ConnectionTag> connectionTags = new LinkedHashMap<String, ConnectionTag>();

    /** A map of controller in the junction */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, ControllerTag> controllerTags = new LinkedHashMap<String, ControllerTag>();

    /**
     * Parse the attributes of the junction tag. The sub-elements are parsed in separate classes.
     * @param node Node; the junction node to parse
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseJunction(final Node node, final OpenDriveNetworkLaneParserOld parser) throws SAXException, NetworkException
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
     * @param juncTag JunctionTag; junction tag
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator
     * @param openDriveNetworkLaneParser OpenDriveNetworkLaneParser; the parser
     * @throws NamingException when an animation registration fails
     * @throws NetworkException when the network is inconsistent
     * @throws GTUException when the traffic light (a GTU at the moment) has an error
     */
    public static void createController(JunctionTag juncTag, DEVSSimulatorInterface.TimeDoubleUnit simulator,
            OpenDriveNetworkLaneParserOld openDriveNetworkLaneParser) throws GTUException, NetworkException, NamingException
    {
        if (true)
            return;
        
        if (juncTag.controllerTags.size() > 0)
        {
            Controller controller = new Controller(juncTag.id, simulator);

            for (ControllerTag controllerTag : juncTag.controllerTags.values())
            {
                int sequence = controllerTag.sequence;
                String id = controllerTag.id;
                String signalId = openDriveNetworkLaneParser.controllerTags.get(id).controlSignalID;

                // AbstractTrafficLight trafficLight = openDriveNetworkLaneParser.trafficLightsBySignals.get(signalId);

                for (SimpleTrafficLight trafficLight : openDriveNetworkLaneParser.trafficLightsBySignals.get(signalId))
                    controller.addTrafficLight(sequence, trafficLight);

                /*
                 * String refId = signalId + ".ref"; if(openDriveNetworkLaneParser.trafficLightsBySignals.containsKey(refId)) {
                 * AbstractTrafficLight trafficLightRef = openDriveNetworkLaneParser.trafficLightsBySignals.get(refId);
                 * controller.addTrafficLight(sequence, trafficLightRef); }
                 */
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "JunctionTag [name=" + this.name + ", id=" + this.id + ", connectionTags=" + this.connectionTags
                + ", controllerTags=" + this.controllerTags + "]";
    }

}
