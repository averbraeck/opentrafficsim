package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;

import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Connector.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 aug. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ConnectorTag implements Serializable
{

    /** */
    private static final long serialVersionUID = 20180816L;

    /** Name. */
    String name = null;

    /** From node tag. */
    NodeTag nodeStartTag = null;

    /** To node tag. */
    NodeTag nodeEndTag = null;

    /** Demand weight. */
    Double demandWeight = null;

    /** Resulting connector link. */
    Link connector;

    /**
     * Parse the CONNECTOR tags.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseConnectors(final NodeList nodeList, final XmlNetworkLaneParserOld parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "CONNECTOR"))
        {
            NamedNodeMap attributes = node.getAttributes();
            ConnectorTag connectorTag = new ConnectorTag();

            if (attributes.getNamedItem("NAME") == null)
                throw new SAXException("CONNECTOR: missing attribute NAME");
            connectorTag.name = attributes.getNamedItem("NAME").getNodeValue().trim();
            if (parser.connectorTags.keySet().contains(connectorTag.name)
                    || parser.linkTags.keySet().contains(connectorTag.name))
                throw new SAXException("CONNECTOR: NAME " + connectorTag.name + " defined twice");

            if (attributes.getNamedItem("NODESTART") == null)
                throw new SAXException("CONNECTOR: missing attribute NODESTART for connector " + connectorTag.name);
            String fromNodeStr = attributes.getNamedItem("NODESTART").getNodeValue().trim();
            connectorTag.nodeStartTag = parser.nodeTags.get(fromNodeStr);
            if (connectorTag.nodeStartTag == null)
                throw new SAXException(
                        "CONNECTOR: NODESTART node " + fromNodeStr + " for link " + connectorTag.name + " not defined");

            if (attributes.getNamedItem("NODEEND") == null)
                throw new SAXException("CONNECTOR: missing attribute NODEEND for link " + connectorTag.name);
            String toNodeStr = attributes.getNamedItem("NODEEND").getNodeValue().trim();
            connectorTag.nodeEndTag = parser.nodeTags.get(toNodeStr);
            if (connectorTag.nodeEndTag == null)
                throw new SAXException(
                        "CONNECTOR: NODEEND node " + toNodeStr + " for connector " + connectorTag.name + " not defined");

            if (attributes.getNamedItem("DEMANDWEIGHT") != null)
                connectorTag.demandWeight = Double.valueOf(attributes.getNamedItem("DEMANDWEIGHT").getNodeValue());

            parser.connectorTags.put(connectorTag.name, connectorTag);
        }
    }

}
