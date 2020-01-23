package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.opendrive.old.LinkTag.ContactPointEnum;
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
class ConnectionTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Id of the lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** Incoming Road id */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String incomingRoad = null;

    /** Connecting Road id */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String connectingRoad = null;

    /** Contact point. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContactPointEnum contactPoint = null;

    /** Lane Link From. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Integer laneLinkFrom = null;

    /** Lane Link To. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Integer laneLinkTo = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node Node; the top-level road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static ConnectionTag parseConnection(final Node node, final OpenDriveNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        ConnectionTag connectionTag = new ConnectionTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("LANE: missing attribute id");
        connectionTag.id = id.getNodeValue().trim();

        Node incomingRoad = attributes.getNamedItem("incomingRoad");
        if (incomingRoad == null)
            throw new SAXException("LANE: missing attribute incomingRoad");
        connectionTag.incomingRoad = incomingRoad.getNodeValue().trim();

        Node connectingRoad = attributes.getNamedItem("connectingRoad");
        if (connectingRoad == null)
            throw new SAXException("LANE: missing attribute connectingRoad");
        connectionTag.connectingRoad = connectingRoad.getNodeValue().trim();

        Node contactPoint = attributes.getNamedItem("contactPoint");
        if (contactPoint == null)
            throw new SAXException("LANE: missing attribute contactPoint");
        else
        {
            if ("start".equals(contactPoint.getNodeValue().trim()))
                connectionTag.contactPoint = ContactPointEnum.START;
            else if ("end".equals(contactPoint.getNodeValue().trim()))
                connectionTag.contactPoint = ContactPointEnum.END;
            else
                throw new SAXException("contactPoint is neither 'start' nor 'end' but: " + contactPoint.getNodeValue().trim());
        }

        for (Node laneLink : XMLParser.getNodes(node.getChildNodes(), "laneLink"))
        {
            NamedNodeMap attributes1 = laneLink.getAttributes();

            Node from = attributes1.getNamedItem("from");
            connectionTag.laneLinkFrom = Integer.parseInt(from.getNodeValue().trim());

            Node to = attributes1.getNamedItem("to");
            connectionTag.laneLinkTo = Integer.parseInt(to.getNodeValue().trim());
        }

        return connectionTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ConnectionTag [id=" + this.id + ", incomingRoad=" + this.incomingRoad + ", connectingRoad="
                + this.connectingRoad + ", contactPoint=" + this.contactPoint + ", laneLinkFrom=" + this.laneLinkFrom
                + ", laneLinkTo=" + this.laneLinkTo + "]";
    }
}
