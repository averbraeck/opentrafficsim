package org.opentrafficsim.core.network.factory.opendrive;

import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.XMLParser;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
class TypeTag
{
    /** start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Rel<LengthUnit> s = null;

    /** road type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** maximum allowed speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar<SpeedUnit> maxSpeed = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList the list of subnodes of the road node
     * @param parser the parser with the lists of information
     * @param roadTag the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseType(final NodeList nodeList, final OpenDriveNetworkLaneParser parser, final RoadTag roadTag)
        throws SAXException, NetworkException
    {
        int typeCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "type"))
        {
            typeCount++;
            TypeTag typeTag = new TypeTag();
            NamedNodeMap attributes = node.getAttributes();

            Node s = attributes.getNamedItem("s");
            if (s == null)
                throw new SAXException("ROAD.TYPE: missing attribute s for ROAD.ID=" + roadTag.id);
            typeTag.s = new DoubleScalar.Rel<LengthUnit>(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);
            

            roadTag.typeTags.put(typeTag.s.doubleValue(), typeTag);
        }

        if (typeCount > 1)
            throw new SAXException("ROAD: more than one TYPE tag for road id=" + roadTag.id);
    }
}
