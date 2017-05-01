package org.opentrafficsim.road.network.factory.xml;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class BezierTag
{

    /** The shape factor. Will be filled after parsing. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    double shape;

    /**
     * Parse the LINK.BEZIER tag.
     * @param bezierNode the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseBezier(final Node bezierNode, final XmlNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap bezierAttributes = bezierNode.getAttributes();
        linkTag.bezierTag = new BezierTag();

        Node shape = bezierAttributes.getNamedItem("SHAPE");
        if (shape == null)
        {
            linkTag.bezierTag.shape = 1.0;
        }
        else
        {
            double s = Double.parseDouble(shape.getNodeValue());
            if (s <= 0)
            {
                throw new SAXException("BEZIER: SHAPE attribute negative or zero");
            }
            linkTag.bezierTag.shape = s;
        }

    }
}
