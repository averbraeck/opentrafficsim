package org.opentrafficsim.core.network.factory.xml;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.CrossSectionElementTag.ElementType;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class BlockTag
{
    /** position of the block on the link, relative to the design line. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Rel<LengthUnit> position = null;

    /**
     * Parse the BLOCK tag.
     * @param node the BLOCK node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent LINK tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseBlock(final Node node, final XmlNetworkLaneParser parser, final LinkTag linkTag) throws SAXException,
        NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        BlockTag blockTag = new BlockTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("BLOCK: missing attribute LANE" + " for link " + linkTag.name);
        String laneName = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadTypeTag == null)
            throw new NetworkException("BLOCK: LANE " + laneName + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag cseTag = linkTag.roadTypeTag.cseTags.get(laneName);
        if (cseTag == null)
            throw new NetworkException("BLOCK: LANE " + laneName + " not found in elements of link " + linkTag.name
                + " - roadtype " + linkTag.roadTypeTag.name);
        if (cseTag.elementType != ElementType.LANE)
            throw new NetworkException("BLOCK: LANE " + laneName + " not a real GTU lane for link " + linkTag.name
                + " - roadtype " + linkTag.roadTypeTag.name);
        if (linkTag.generatorTags.containsKey(laneName))
            throw new SAXException("BLOCK for LANE with NAME " + laneName + " defined twice");

        Node position = attributes.getNamedItem("POSITION");
        blockTag.position =
            LinkTag.parseBeginEndPosition(position == null ? "END" : position.getNodeValue().trim(), linkTag);

        linkTag.blockTags.put(laneName, blockTag);
    }
}
