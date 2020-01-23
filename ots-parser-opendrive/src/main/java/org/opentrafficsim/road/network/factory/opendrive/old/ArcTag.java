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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class ArcTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Degree of the curve at the start(s-coordinate?). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length curvature = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param geometryTag GeometryTag; the GeometryTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseArc(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final GeometryTag geometryTag)
            throws SAXException
    {
        int typeCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "arc"))
        {
            typeCount++;
            ArcTag arcTag = new ArcTag();
            NamedNodeMap attributes = node.getAttributes();

            Node curvature = attributes.getNamedItem("curvature");
            if (curvature != null)
                arcTag.curvature = new Length(Double.parseDouble(curvature.getNodeValue().trim()), LengthUnit.SI);

            geometryTag.arcTag = arcTag;
        }

        if (typeCount > 1)
            throw new SAXException("ROAD: more than one arc tag!");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ArcTag [curvature=" + this.curvature + "]";
    }
}
