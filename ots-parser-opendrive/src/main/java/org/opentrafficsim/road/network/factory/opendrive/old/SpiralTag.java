package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class SpiralTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Degree of the curve at the start(s-coordinate?). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length curvStart = null;

    /** Degree of the curve at the end(s-coordinate?). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length curvEnd = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param geometryTag GeometryTag; the GeometryTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseSpiral(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final GeometryTag geometryTag)
            throws SAXException
    {
        int typeCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "spiral"))
        {
            typeCount++;

            SpiralTag spiralTag = new SpiralTag();
            NamedNodeMap attributes = node.getAttributes();

            Node curvStart = attributes.getNamedItem("curvStart");
            if (curvStart != null)
                spiralTag.curvStart = new Length(Double.parseDouble(curvStart.getNodeValue().trim()), LengthUnit.SI);

            Node curvEnd = attributes.getNamedItem("curvEnd");
            if (curvEnd != null)
                spiralTag.curvEnd = new Length(Double.parseDouble(curvEnd.getNodeValue().trim()), LengthUnit.SI);

            geometryTag.spiralTag = spiralTag;
        }

        if (typeCount > 1)
            throw new SAXException("ROAD: more than one spiral tag!");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SpiralTag [curvStart=" + this.curvStart + ", curvEnd=" + this.curvEnd + "]";
    }
}
