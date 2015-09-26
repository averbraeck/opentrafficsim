package org.opentrafficsim.road.network.factory.opendrive;

import java.util.Map;
import java.util.TreeMap;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class RoadTag 
{
    /** unique ID within database (preferably an integer number, uint32_t). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String id = null;

    /** name of the road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** total length of the reference line in the xy-plane, as indicated in the XML document. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel length = null;

    /** ID of the junction to which the road belongs as a connecting road (= -1 for none). */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String junctionId = null;

    /** Link Tag containing predecessor, successor and neighbor info. Can be absent for isolated roads. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LinkTag linkTag = null;

    /** Type Tags containing road type and maximum speed information for stretches of road. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<Double, TypeTag> typeTags = new TreeMap<Double, TypeTag>();

    /**
     * Parse the attributes of the road tag. The sub-elements are parsed in separate classes.
     * @param node the top-level road node
     * @param parser the parser with the lists of information
     * @return the generated RoadTag for further reference
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadTag parseRoad(final Node node, final OpenDriveNetworkLaneParser parser) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        RoadTag roadTag = new RoadTag();

        Node id = attributes.getNamedItem("id");
        if (id == null)
            throw new SAXException("ROAD: missing attribute ID");
        roadTag.id = id.getNodeValue().trim();
        if (parser.roadTags.keySet().contains(roadTag.id))
            throw new SAXException("ROAD: ID " + roadTag.id + " defined twice");

        Node name = attributes.getNamedItem("name");
        if (name == null)
            throw new SAXException("ROAD: missing attribute ID for road with ID=" + roadTag.id);
        roadTag.name = name.getNodeValue().trim();

        Node length = attributes.getNamedItem("length");
        if (length == null)
            throw new SAXException("ROAD: missing attribute LENGTH");
        roadTag.length = new Length.Rel(Double.parseDouble(length.getNodeValue().trim()), LengthUnit.METER);

        Node junctionId = attributes.getNamedItem("junction");
        if (junctionId == null)
            throw new SAXException("ROAD: missing attribute junction for road id=" + roadTag.id);
        if (!junctionId.getNodeValue().trim().equals("-1"))
        {
            roadTag.junctionId = junctionId.getNodeValue().trim();
            if (!parser.junctionTags.keySet().contains(roadTag.junctionId))
                throw new SAXException("ROAD: junction id=" + roadTag.junctionId + " for road id=" + roadTag.id
                    + " not defined as a junction in the XML-file");
        }

        return roadTag;
    }
}
