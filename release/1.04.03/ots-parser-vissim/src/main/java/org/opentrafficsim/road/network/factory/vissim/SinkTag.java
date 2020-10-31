package org.opentrafficsim.road.network.factory.vissim;

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
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SinkTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Position of the sink on the link, relative to the design line, stored as a string to parse when the length is known. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String positionStr = null;

    /**
     * Parse the SINK tag.
     * @param node Node; the SINK node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param linkTag LinkTag; the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSink(final Node node, final VissimNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        SinkTag sinkTag = new SinkTag();

        if (attributes.getNamedItem("LANE") == null)
        {
            throw new SAXException("SINK: missing attribute LANE" + " for link " + linkTag.name);
        }
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadLayoutTag == null)
        {
            throw new NetworkException("SINK: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        }
        CrossSectionElementTag cseTag = linkTag.roadLayoutTag.cseTags.get(laneName);
        if (cseTag == null)
        {
            throw new NetworkException("SINK: LANE " + laneName + " not found in elements of link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        }
        if (cseTag.elementType != org.opentrafficsim.road.network.factory.vissim.CrossSectionElementTag.ElementType.LANE)
        {
            throw new NetworkException("SINK: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                    + " - roadtype " + linkTag.roadLayoutTag.name);
        }
        if (linkTag.generatorTags.containsKey(laneName))
        {
            throw new SAXException("SINK for LANE with NAME " + laneName + " defined twice");
        }

        Node position = attributes.getNamedItem("POSITION");
        if (position == null)
        {
            throw new NetworkException("SINK: POSITION element not found in elements of link " + linkTag.name + " - roadtype "
                    + linkTag.roadLayoutTag.name);
        }
        sinkTag.positionStr = position.getNodeValue().trim();

        linkTag.sinkTags.put(laneName, sinkTag);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SinkTag [positionStr=" + this.positionStr + "]";
    }
}
