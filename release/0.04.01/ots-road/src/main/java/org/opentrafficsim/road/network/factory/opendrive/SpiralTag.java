package org.opentrafficsim.road.network.factory.opendrive;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck
 * $, initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SpiralTag
{
    /** degree of the curve at the start(s-coordinate?). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel curvStart = null;

    /** degree of the curve at the end(s-coordinate?). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Angle.Rel curvEnd = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList the list of subnodes of the road node
     * @param parser the parser with the lists of information
     * @param geometryTag the GeometryTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSpiral(final NodeList nodeList, final OpenDriveNetworkLaneParser parser, final GeometryTag geometryTag)
            throws SAXException, NetworkException
    {
        int typeCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "spiral"))
        {
            typeCount++;

            SpiralTag spiralTag = new SpiralTag();
            NamedNodeMap attributes = node.getAttributes();

            Node curvStart = attributes.getNamedItem("curvStart");
            if (curvStart != null)
                spiralTag.curvStart = new Angle.Rel(Double.parseDouble(curvStart.getNodeValue().trim()), AngleUnit.DEGREE);

            Node curvEnd = attributes.getNamedItem("curvEnd");
            if (curvEnd != null)
                spiralTag.curvEnd = new Angle.Rel(Double.parseDouble(curvEnd.getNodeValue().trim()), AngleUnit.DEGREE);

            geometryTag.spiralTag = spiralTag;
        }

        if (typeCount > 1)
            throw new SAXException("ROAD: more than one spiral tag!");
    }
}