package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.ArrayList;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.xml.old.CrossSectionElementTag.ElementType;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
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
class SensorTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** Class name of the Sensor. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    /** Trigger position of the GTU (FRONT, REAR, etc.). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RelativePosition.TYPE triggerPosition;

    /**
     * Parse the SENSOR tag.
     * @param node Node; the SENSOR node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSensor(final Node node, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        SensorTag sensorTag = new SensorTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("SENSOR: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
            throw new NetworkException("SENSOR: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("SENSOR: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("SENSOR: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
            throw new NetworkException("SENSOR: POSITION element not found in elements of link " + linkTag.name + " - roadtype "
                    + linkTag.roadLayoutTag.name);
        sensorTag.positionStr = position.getNodeValue().trim();

        Node name = attributes.getNamedItem("NAME");
        if (name != null)
            sensorTag.name = name.getNodeValue().trim();

        Node classNode = attributes.getNamedItem("CLASS");
        if (classNode == null)
            throw new SAXException("SENSOR: missing attribute CLASS for sensor " + sensorTag.name + " on lane " + laneName);
        sensorTag.className = classNode.getNodeValue().trim();
        try
        {
            Class<?> clazz = Class.forName(sensorTag.className);
            if (!AbstractSensor.class.isAssignableFrom(clazz))
                throw new SAXException("SENSOR: CLASS NAME " + sensorTag.className + " for sensor " + sensorTag.name
                        + " on lane " + laneName + " does not extend the AbstractSensor class");

            try
            {
                ClassUtil.resolveConstructor(clazz, new Class[] {String.class, Lane.class, Length.class,
                        RelativePosition.TYPE.class, DEVSSimulatorInterface.TimeDoubleUnit.class, Compatible.class});
            }
            catch (NoSuchMethodException nsme)
            {
                throw new SAXException(
                        "SENSOR: CLASS NAME " + sensorTag.className + " for sensor " + sensorTag.name + " on lane " + laneName
                                + " -- no constructor with arguments (String, Lane, Length, RelativePosition.TYPE,"
                                + " SimulatorInterface.TimeDoubleUnit, Length, OTSLine3D)");
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new SAXException("SENSOR: CLASS NAME " + sensorTag.className + " for sensor " + sensorTag.name + " on lane "
                    + laneName + " could not be loaded.");
        }

        Node triggerNode = attributes.getNamedItem("TRIGGER");
        if (triggerNode == null)
            throw new SAXException("SENSOR: missing attribute TRIGGER for sensor " + sensorTag.name + " on lane " + laneName);
        sensorTag.triggerPosition = parseTriggerPosition(triggerNode.getNodeValue().trim(), sensorTag, laneName);

        if (!linkTag.sensorTags.containsKey(laneName))
            linkTag.sensorTags.put(laneName, new ArrayList<SensorTag>());
        linkTag.sensorTags.get(laneName).add(sensorTag);
    }

    /**
     * @param trigger String; String of the trigger position such as FRONT
     * @param sensorTag SensorTag; the sensor tag for error messages
     * @param laneName String; the lane name for error messages
     * @return a relative position type, such as RelatievPostition.FRONT
     * @throws SAXException when the trigger position did not exist
     */
    static RelativePosition.TYPE parseTriggerPosition(final String trigger, final SensorTag sensorTag, final String laneName)
            throws SAXException
    {
        switch (trigger)
        {
            case "FRONT":
                return RelativePosition.FRONT;

            case "CENTER":
                return RelativePosition.CENTER;

            case "DRIVER":
                return RelativePosition.DRIVER;

            case "REAR":
                return RelativePosition.REAR;

            case "REFERENCE":
                return RelativePosition.REFERENCE;

            default:
                throw new SAXException("SENSOR: wrong type of TRIGGER for sensor " + sensorTag.name + " on lane " + laneName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SensorTag [name=" + this.name + ", positionStr=" + this.positionStr + ", className=" + this.className
                + ", triggerPosition=" + this.triggerPosition + "]";
    }
}
