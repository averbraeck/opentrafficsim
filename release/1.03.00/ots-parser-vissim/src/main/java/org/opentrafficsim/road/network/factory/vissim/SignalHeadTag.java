package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

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
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SignalHeadTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** No, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String no = "";

    /** No, cannot be null in implementation of signal head. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String sg = "";

    /** is the signalHead really located on this link? */
    Boolean activeOnThisLink = true;

    /**
     * Position of the signalHead on the link, relative to the design line, stored as a string to parse when the length is
     * known.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /**
     * Position of the signalHead on the link, relative to the design line, stored as a string to parse when the length is
     * known.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String signalGroup = null;

    /** Class name of the TrafficLight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String className = null;

    String linkName;

    String laneName;

    /**
     * @param signalHeadTag SignalHeadTag;
     */
    public SignalHeadTag(SignalHeadTag signalHeadTag)
    {
        this.no = signalHeadTag.no;
        this.sg = signalHeadTag.sg;
        this.activeOnThisLink = signalHeadTag.activeOnThisLink;
        this.positionStr = signalHeadTag.positionStr;
        this.signalGroup = signalHeadTag.signalGroup;
        this.className = signalHeadTag.className;
        this.linkName = signalHeadTag.linkName;
        this.laneName = signalHeadTag.laneName;
    }

    /**
     *
     */
    public SignalHeadTag()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * Parse the TRAFFICLIGHT tag.
     * @param nodeList NodeList; the SignalHead nodes to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSignalHead(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node linksNode : XMLParser.getNodes(nodeList, "signalHeads"))
        {

            for (Node node : XMLParser.getNodes(linksNode.getChildNodes(), "signalHead"))
            {
                NamedNodeMap attributes = node.getAttributes();
                // make a link with its attributes
                SignalHeadTag signalHeadTag = new SignalHeadTag();

                if (attributes.getNamedItem("lane") == null)
                {
                    throw new SAXException("SignalHead: missing attribute: lane");
                }
                String laneLink = attributes.getNamedItem("lane").getNodeValue().trim();
                String[] laneLinkInfo = laneLink.split("\\s+");
                signalHeadTag.linkName = laneLinkInfo[0];
                signalHeadTag.laneName = laneLinkInfo[1];

                if (attributes.getNamedItem("no") == null)
                {
                    throw new SAXException("SignalHead: missing attribute: no");
                }
                signalHeadTag.no = attributes.getNamedItem("no").getNodeValue().trim();

                if (attributes.getNamedItem("pos") == null)
                {
                    throw new SAXException("SignalHead: missing attribute: pos");
                }
                signalHeadTag.positionStr = attributes.getNamedItem("pos").getNodeValue().trim();

                if (attributes.getNamedItem("sg") == null)
                {
                    throw new SAXException("SignalHead: missing attribute: sg");
                }
                signalHeadTag.signalGroup = attributes.getNamedItem("sg").getNodeValue().trim();

                parser.getSignalHeadTags().put(signalHeadTag.no, signalHeadTag);

            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TrafficLightTag [name=" + this.no + ", positionStr=" + this.positionStr + ", className=" + this.className + "]";
    }

}
