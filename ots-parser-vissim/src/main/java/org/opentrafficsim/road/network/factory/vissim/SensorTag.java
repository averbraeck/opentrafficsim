package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
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

    /** length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String length = null;

    /** laneLocation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneName = null;

    /** linkLocation. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String linkName = null;

    /** No. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String no = null;

    /** PortNo. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String portNo = null;

    /** sc. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String sc = null;

    /** type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /** Class name of the Sensor. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    /** Trigger position of the GTU (FRONT, REAR, etc.). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RelativePosition.TYPE triggerPosition;

    /** is the sensor really located on this link? */
    Boolean activeOnThisLink = true;

    /**
     * @param sensorTag SensorTag;
     */
    public SensorTag(SensorTag sensorTag)
    {
        this.name = sensorTag.name;
        this.length = sensorTag.length;
        this.laneName = sensorTag.laneName;
        this.linkName = sensorTag.linkName;
        this.no = sensorTag.no;
        this.portNo = sensorTag.portNo;
        this.sc = sensorTag.sc;
        this.type = sensorTag.type;
        this.positionStr = sensorTag.positionStr;
        this.className = sensorTag.className;
        this.triggerPosition = sensorTag.triggerPosition;
    }

    /**
     *
     */
    public SensorTag()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Parse the SENSOR tag.
     * @param nodeList NodeList; the SENSOR node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSensor(final NodeList nodeList, final VissimNetworkLaneParser parser) throws SAXException, NetworkException
    {

        for (Node linksNode : XMLParser.getNodes(nodeList, "detectors"))
        {

            for (Node node : XMLParser.getNodes(linksNode.getChildNodes(), "detector"))
            {
                NamedNodeMap attributes = node.getAttributes();
                // make a link with its attributes
                SensorTag sensorTag = new SensorTag();

                if (attributes.getNamedItem("name") == null)
                {
                    throw new SAXException("Sensor: missing attribute: name");
                }
                sensorTag.name = attributes.getNamedItem("name").getNodeValue().trim();

                if (attributes.getNamedItem("lane") == null)
                {
                    throw new SAXException("Sensor: missing attribute: lane");
                }
                String laneLink = attributes.getNamedItem("lane").getNodeValue().trim();
                String[] laneLinkInfo = laneLink.split("\\s+");
                sensorTag.linkName = laneLinkInfo[0];
                sensorTag.laneName = laneLinkInfo[1];

                if (attributes.getNamedItem("length") == null)
                {
                    throw new SAXException("Sensor: missing attribute: length");
                }
                sensorTag.length = attributes.getNamedItem("length").getNodeValue().trim();

                if (attributes.getNamedItem("no") == null)
                {
                    throw new SAXException("Sensor: missing attribute: no");
                }
                sensorTag.no = attributes.getNamedItem("no").getNodeValue().trim();

                if (attributes.getNamedItem("portNo") == null)
                {
                    throw new SAXException("Sensor: missing attribute: portNo");
                }
                sensorTag.portNo = attributes.getNamedItem("portNo").getNodeValue().trim();

                if (attributes.getNamedItem("sc") == null)
                {
                    throw new SAXException("Sensor: missing attribute: sc");
                }
                sensorTag.sc = attributes.getNamedItem("sc").getNodeValue().trim();

                if (attributes.getNamedItem("type") == null)
                {
                    throw new SAXException("Sensor: missing attribute: type");
                }
                sensorTag.type = attributes.getNamedItem("type").getNodeValue().trim();

                if (attributes.getNamedItem("pos") == null)
                {
                    throw new SAXException("Sensor: missing attribute: pos");
                }
                sensorTag.positionStr = attributes.getNamedItem("pos").getNodeValue().trim();

                parser.getSensorTags().put(sensorTag.no, sensorTag);

            }
        }

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
