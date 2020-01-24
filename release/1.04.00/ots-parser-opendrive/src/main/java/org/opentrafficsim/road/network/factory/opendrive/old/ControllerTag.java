package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ControllerTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Id of the lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Sequence. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Integer sequence = null;

    /** Id of the control signal. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String controlSignalID = null;

    /** Control type */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String controlType = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static ControllerTag parseController(final Node node, final OpenDriveNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        ControllerTag controllerTag = new ControllerTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("LANE: missing attribute id");
        controllerTag.id = id.getNodeValue().trim();

        Node type = attributes.getNamedItem("type");
        if (type != null)
            controllerTag.type = type.getNodeValue().trim();

        Node name = attributes.getNamedItem("name");
        if (name != null)
            controllerTag.name = name.getNodeValue().trim();

        Node sequence = attributes.getNamedItem("sequence");
        if (sequence == null)
            throw new SAXException("LANE: missing attribute sequence");
        controllerTag.sequence = Integer.parseInt(sequence.getNodeValue().trim());

        for (Node control : XMLParser.getNodes(node.getChildNodes(), "control"))
        {
            NamedNodeMap attributes1 = control.getAttributes();

            Node signalId = attributes1.getNamedItem("signalId");
            controllerTag.controlSignalID = signalId.getNodeValue().trim();

            Node controlType = attributes1.getNamedItem("type");
            controllerTag.controlType = controlType.getNodeValue().trim();
        }

        return controllerTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ControllerTag [id=" + this.id + ", type=" + this.type + ", name=" + this.name + ", sequence=" + this.sequence
                + ", controlSignalID=" + this.controlSignalID + ", controlType=" + this.controlType + "]";
    }
}
