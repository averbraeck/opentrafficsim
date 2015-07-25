package org.opentrafficsim.core.network.factory.xml;

import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class GlobalTag
{
    /** default speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<SpeedUnit> speed = null;

    /** default lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Rel<LengthUnit> width = null;

    /**
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws NetworkException when parsing of GLOBAL tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGlobal(final NodeList nodeList, final XmlNetworkLaneParser parser) throws NetworkException
    {
        List<Node> nodes = XMLParser.getNodes(nodeList, "GLOBAL");
        if (nodes.size() > 1)
            throw new NetworkException("More than one tag GLOBAL in the XML-file");
        if (nodes.size() == 1)
        {
            Node node = nodes.get(0);
            NamedNodeMap attributes = node.getAttributes();

            parser.globalTag = new GlobalTag();

            Node speed = attributes.getNamedItem("SPEED");
            if (speed != null)
                parser.globalTag.speed = SpeedUnits.parseSpeedAbs(speed.getNodeValue().trim());

            Node width = attributes.getNamedItem("WIDTH");
            if (width != null)
                parser.globalTag.width = LengthUnits.parseLengthRel(width.getNodeValue().trim());
        }
    }
}
