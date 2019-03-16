package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.ArrayList;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.xml.old.CrossSectionElementTag.ElementType;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
class TrafficLightTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name, cannot be null in implementation of traffic light. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = "";

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** Class name of the TrafficLight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    /**
     * Parse the TRAFFICLIGHT tag.
     * @param node Node; the TRAFFICLIGHT node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseTrafficLight(final Node node, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        TrafficLightTag trafficLightTag = new TrafficLightTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("TRAFFICLIGHT: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("TRAFFICLIGHT: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
            throw new NetworkException("TRAFFICLIGHT: POSITION element not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        trafficLightTag.positionStr = position.getNodeValue().trim();

        Node name = attributes.getNamedItem("NAME");
        if (name != null)
            trafficLightTag.name = name.getNodeValue().trim();

        Node classNode = attributes.getNamedItem("CLASS");
        if (classNode == null)
            throw new SAXException(
                    "TRAFFICLIGHT: missing attribute CLASS for traffic light " + trafficLightTag.name + " on lane " + laneName);
        trafficLightTag.className = classNode.getNodeValue().trim();
        try
        {
            Class<?> clazz = Class.forName(trafficLightTag.className);
            if (!TrafficLight.class.isAssignableFrom(clazz))
                throw new SAXException("TRAFFICLIGHT: CLASS NAME " + trafficLightTag.className + " for trafficLight "
                        + trafficLightTag.name + " on lane " + laneName + " does not implement the TrafficLight interface");

            try
            {
                ClassUtil.resolveConstructor(clazz,
                        new Class[] {String.class, Lane.class, Length.class, DEVSSimulatorInterface.TimeDoubleUnit.class});
            }
            catch (NoSuchMethodException nsme)
            {
                throw new SAXException("TRAFFICLIGHT: CLASS NAME " + trafficLightTag.className + " for trafficLight "
                        + trafficLightTag.name + " on lane " + laneName
                        + " -- no constructor with arguments (String, Lane, Length, DEVSSimulatorInterface.TimeDoubleUnit)");
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrafficLightTag [name=" + this.name + ", positionStr=" + this.positionStr + ", className=" + this.className
                + "]";
    }

}
