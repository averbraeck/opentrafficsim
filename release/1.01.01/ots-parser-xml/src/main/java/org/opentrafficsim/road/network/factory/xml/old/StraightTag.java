package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class StraightTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /**
     * Parse the LINK.STRAIGHT tag.
     * @param straightNode Node; the XML-node to parse
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param linkTag LinkTag; the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseStraight(final Node straightNode, final XmlNetworkLaneParserOld parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        linkTag.straightTag = new StraightTag();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "StraightTag";
    }
}
