package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LinkTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20150723L;

    /** Predecessor id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String predecessorId = null;

    /** Predecessor Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String predecessorType = null;

    /** Predecessor contact point. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContactPointEnum predecessorContactPoint = null;

    /** Successor id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String successorId = null;

    /** Successor Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String successorType = null;

    /** Successor contact point. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContactPointEnum successorContactPoint = null;

    /** Left neighbor road id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String leftNeighborRoadId = null;

    /** Left neighbor direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NeighborDirection leftNeighborDirection = null;

    /** Right neighbor road id. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String rightNeighborRoadId = null;

    /** Right neighbor direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    NeighborDirection rightNeighborDirection = null;

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLink(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
            throws SAXException, NetworkException
    {
        int linkCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "link"))
        {
            linkCount++;
            LinkTag linkTag = new LinkTag();
            roadTag.linkTag = linkTag;

            /* PREDECESSOR */

            List<Node> predecessorNodes = XMLParser.getNodes(node.getChildNodes(), "predecessor");
            if (predecessorNodes.size() > 1)
                throw new SAXException("ROAD: more than one LINK.PREDECESSOR tag for road id=" + roadTag.id);
            if (predecessorNodes.size() == 1)
            {
                Node predecessorNode = predecessorNodes.get(0);
                NamedNodeMap attributes = predecessorNode.getAttributes();

                Node elementId = attributes.getNamedItem("elementId");
                if (elementId == null)
                    throw new SAXException("ROAD.LINK.PREDECESSOR: missing attribute elementId for ROAD.ID=" + roadTag.id);

                Node elementType = attributes.getNamedItem("elementType");
                if (elementType == null)
                    throw new SAXException("ROAD.LINK.PREDECESSOR: missing attribute elementType for ROAD.ID=" + roadTag.id);
                if ("road".equals(elementType.getNodeValue().trim()) || "junction".equals(elementType.getNodeValue().trim()))
                {
                    linkTag.predecessorType = elementType.getNodeValue().trim();
                    linkTag.predecessorId = elementId.getNodeValue().trim();
                }
                else
                    throw new SAXException("ROAD.LINK.PREDECESSOR: elementType for ROAD.ID=" + roadTag.id
                            + " is neither 'road' nor 'junction' but: " + elementType.getNodeValue().trim());

                Node contactPoint = attributes.getNamedItem("contactPoint");
                if (contactPoint != null)
                // System.out.println("ROAD " + roadTag.id + " has no contactPoint for PREDECESSOR " +
                // elementType.getNodeValue().trim() + elementId.getNodeValue().trim());
                // else
                {
                    if ("start".equals(contactPoint.getNodeValue().trim()))
                        linkTag.predecessorContactPoint = ContactPointEnum.START;
                    else if ("end".equals(contactPoint.getNodeValue().trim()))
                        linkTag.predecessorContactPoint = ContactPointEnum.END;
                    else
                        throw new SAXException("ROAD.LINK.PREDECESSOR: contactPoint for ROAD.ID=" + roadTag.id
                                + " is neither 'start' nor 'end' but: " + contactPoint.getNodeValue().trim());
                }
            }

            /* SUCCESSOR */

            List<Node> successorNodes = XMLParser.getNodes(node.getChildNodes(), "successor");
            if (successorNodes.size() > 1)
                throw new SAXException("ROAD: more than one LINK.SUCCESSOR tag for road id=" + roadTag.id);
            if (successorNodes.size() == 1)
            {
                Node successorNode = successorNodes.get(0);
                NamedNodeMap attributes = successorNode.getAttributes();

                Node elementId = attributes.getNamedItem("elementId");
                if (elementId == null)
                    throw new SAXException("ROAD.LINK.SUCCESSOR: missing attribute elementId for ROAD.ID=" + roadTag.id);

                Node elementType = attributes.getNamedItem("elementType");
                if (elementType == null)
                    throw new SAXException("ROAD.LINK.SUCCESSOR: missing attribute elementType for ROAD.ID=" + roadTag.id);
                if ("road".equals(elementType.getNodeValue().trim()) || "junction".equals(elementType.getNodeValue().trim()))
                {
                    linkTag.successorType = elementType.getNodeValue().trim();
                    linkTag.successorId = elementId.getNodeValue().trim();
                }
                else
                    throw new SAXException("ROAD.LINK.SUCCESSOR: elementType for ROAD.ID=" + roadTag.id
                            + " is neither 'road' nor 'junction' but: " + elementType.getNodeValue().trim());

                Node contactPoint = attributes.getNamedItem("contactPoint");
                if (contactPoint != null)
                // System.out.println("ROAD " + roadTag.id + " has no contactPoint for SUCCESSOR " +
                // elementType.getNodeValue().trim() + elementId.getNodeValue().trim());
                // else
                {
                    if ("start".equals(contactPoint.getNodeValue().trim()))
                        linkTag.successorContactPoint = ContactPointEnum.START;
                    else if ("end".equals(contactPoint.getNodeValue().trim()))
                        linkTag.successorContactPoint = ContactPointEnum.END;
                    else
                        throw new SAXException("ROAD.LINK.SUCCESSOR: contactPoint for ROAD.ID=" + roadTag.id
                                + " is neither 'start' nor 'end' but: " + contactPoint.getNodeValue().trim());
                }
            }

            /* NEIGHBOR */

            List<Node> neighborNodes = XMLParser.getNodes(node.getChildNodes(), "neighbor");
            if (neighborNodes.size() > 2)
                throw new SAXException("ROAD: more than two LINK.NEIGHBOR tags for road id=" + roadTag.id);
            boolean left = false;
            boolean right = false;
            for (Node neighborNode : neighborNodes)
            {
                NamedNodeMap attributes = neighborNode.getAttributes();

                Node elementId = attributes.getNamedItem("elementId");
                if (elementId == null)
                    throw new SAXException("ROAD.LINK.NEIGHBOR: missing attribute elementId for ROAD.ID=" + roadTag.id);

                Node direction = attributes.getNamedItem("direction");
                NeighborDirection ndir = null;
                if (direction == null)
                    throw new SAXException("ROAD.LINK.NEIGHBOR: missing attribute direction for ROAD.ID=" + roadTag.id);
                if ("same".equals(direction.getNodeValue().trim()))
                    ndir = NeighborDirection.SAME;
                else if ("opposite".equals(direction.getNodeValue().trim()))
                    ndir = NeighborDirection.OPPOSITE;
                else
                    throw new SAXException("ROAD.LINK.NEIGHBOR: contactPoint for ROAD.ID=" + roadTag.id
                            + " is neither 'same' nor 'opposite' but: " + direction.getNodeValue().trim());

                Node side = attributes.getNamedItem("side");
                if (side == null)
                    throw new SAXException("ROAD.LINK.NEIGHBOR: missing attribute side for ROAD.ID=" + roadTag.id);
                if ("left".equals(side.getNodeValue().trim()))
                {
                    if (left)
                        throw new SAXException("ROAD.LINK.NEIGHBOR: left side defined twice for ROAD.ID=" + roadTag.id);
                    left = true;
                    linkTag.leftNeighborRoadId = elementId.getNodeValue().trim();
                    linkTag.leftNeighborDirection = ndir;
                }
                else if ("right".equals(side.getNodeValue().trim()))
                {
                    if (right)
                        throw new SAXException("ROAD.LINK.NEIGHBOR: right side defined twice for ROAD.ID=" + roadTag.id);
                    right = true;
                    linkTag.rightNeighborRoadId = elementId.getNodeValue().trim();
                    linkTag.rightNeighborDirection = ndir;
                }
                else
                    throw new SAXException("ROAD.LINK.NEIGHBOR: side for ROAD.ID=" + roadTag.id
                            + " is neither 'left' nor 'right' but: " + side.getNodeValue().trim());
            }

        }

        if (linkCount > 1)
            throw new SAXException("ROAD: more than one LINK tag for road id=" + roadTag.id);
    }

    /** Enum for road linking contact points: is the link with the start or the end? */
    enum ContactPointEnum
    {
        /** Start. */
        START,
        /** End. */
        END;
    }

    /** Enum for neighbor direction: same or opposite direction? */
    enum NeighborDirection
    {
        /** Same. */
        SAME,
        /** Opposite. */
        OPPOSITE;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LinkTag [predecessorId=" + this.predecessorId + ", predecessorType=" + this.predecessorType
                + ", predecessorContactPoint=" + this.predecessorContactPoint + ", successorId=" + this.successorId
                + ", successorType=" + this.successorType + ", successorContactPoint=" + this.successorContactPoint
                + ", leftNeighborRoadId=" + this.leftNeighborRoadId + ", leftNeighborDirection=" + this.leftNeighborDirection
                + ", rightNeighborRoadId=" + this.rightNeighborRoadId + ", rightNeighborDirection="
                + this.rightNeighborDirection + "]";
    }
}
