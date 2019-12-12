package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
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
class TypeTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Start position (s-coordinate). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length s = null;

    /** Road type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Maximum allowed speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed maxSpeed = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param roadTag RoadTag; the RoadTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseType(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final RoadTag roadTag)
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
            typeTag.s = new Length(Double.parseDouble(s.getNodeValue().trim()), LengthUnit.METER);

            Node type = attributes.getNamedItem("type");
            if (type == null)
                throw new SAXException("ROAD.TYPE: missing attribute type for ROAD.ID=" + roadTag.id);
            typeTag.type = type.getNodeValue().trim();

            roadTag.typeTag = typeTag;
        }

        if (typeCount > 1)
            throw new SAXException("ROAD: more than one TYPE tag for road id=" + roadTag.id);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TypeTag [s=" + this.s + ", type=" + this.type + ", maxSpeed=" + this.maxSpeed + "]";
    }
}
