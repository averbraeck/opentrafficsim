package org.opentrafficsim.road.network.factory.xml;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class StraightTag implements OTS_SCALAR
{
    /** length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel length = null;

    /**
     * Parse the LINK.STRAIGHT tag.
     * @param straightNode the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseStraight(final Node straightNode, final XmlNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException
    {
        NamedNodeMap straightAttributes = straightNode.getAttributes();
        linkTag.straightTag = new StraightTag();

        if (straightAttributes.getNamedItem("LENGTH") != null)
            linkTag.straightTag.length =
                LengthUnits.parseLengthRel(straightAttributes.getNamedItem("LENGTH").getNodeValue());
    }
}
