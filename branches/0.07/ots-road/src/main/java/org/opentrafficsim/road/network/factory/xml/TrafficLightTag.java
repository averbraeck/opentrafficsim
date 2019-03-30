package org.opentrafficsim.road.network.factory.xml;

import java.util.ArrayList;

import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.object.AbstractTrafficLight;
import org.opentrafficsim.road.network.factory.xml.CrossSectionElementTag.ElementType;
import org.opentrafficsim.road.network.lane.Lane;
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
class TrafficLightTag
{
    /** name, cannot be null in implementation of traffic light. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = "";

    /** position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** class name of the TrafficLight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    /**
     * Parse the TRAFFICLIGHT tag.
     * @param node the TRAFFICLIGHT node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseTrafficLight(final Node node, final XmlNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        TrafficLightTag trafficLightTag = new TrafficLightTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("TRAFFICLIGHT: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadTypeTag == null)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " not found in elements of link "
                + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " not a real GTU lane for link "
                + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
            throw new NetworkException("TRAFFICLIGHT: POSITION element not found in elements of link " + linkTag.name
                + " - roadtype " + linkTag.roadTypeTag.name);
        trafficLightTag.positionStr = position.getNodeValue().trim();

        Node name = attributes.getNamedItem("NAME");
        if (name != null)
            trafficLightTag.name = name.getNodeValue().trim();

        Node classNode = attributes.getNamedItem("CLASS");
        if (classNode == null)
            throw new SAXException("TRAFFICLIGHT: missing attribute CLASS for traffic light " + trafficLightTag.name
                + " on lane " + laneName);
        trafficLightTag.className = classNode.getNodeValue().trim();
        try
        {
            Class<?> clazz = Class.forName(trafficLightTag.className);
            if (!AbstractTrafficLight.class.isAssignableFrom(clazz))
                throw new SAXException("TRAFFICLIGHT: CLASS NAME " + trafficLightTag.className + " for trafficLight "
                    + trafficLightTag.name + " on lane " + laneName + " does not extend the AbstractTrafficLight class");

            try
            {
                ClassUtil.resolveConstructor(clazz, new Class[]{String.class, Lane.class, Length.Rel.class,
                    OTSDEVSSimulatorInterface.class, OTSNetwork.class});
            }
            catch (NoSuchMethodException nsme)
            {
                throw new SAXException(
                    "TRAFFICLIGHT: CLASS NAME "
                        + trafficLightTag.className
                        + " for trafficLight "
                        + trafficLightTag.name
                        + " on lane "
                        + laneName
                        + " -- no constructor with arguments (String, Lane, Length.Rel, OTSDEVSSimulatorInterface, OTSNetwork)");
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new SAXException("TRAFFICLIGHT: CLASS NAME " + trafficLightTag.className + " for trafficLight "
                + trafficLightTag.name + " on lane " + laneName + " could not be loaded.");
        }

        if (!linkTag.trafficLightTags.containsKey(laneName))
            linkTag.trafficLightTags.put(laneName, new ArrayList<TrafficLightTag>());
        linkTag.trafficLightTags.get(laneName).add(trafficLightTag);
    }

}