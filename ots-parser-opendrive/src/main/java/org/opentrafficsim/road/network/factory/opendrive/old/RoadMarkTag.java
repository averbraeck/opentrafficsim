package org.opentrafficsim.road.network.factory.opendrive.old;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
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
class RoadMarkTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** The sOffst. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length sOffst = null;

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String type = null;

    /** Weight. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String weight = null;

    /** Color. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String color = null;

    /** Width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length width = null;

    /** Lane change. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneChange = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param laneTag LaneTag; the LaneTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoadMark(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final LaneTag laneTag)
            throws SAXException, NetworkException
    {
        int roadMarkCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "roadMark"))
        {

            RoadMarkTag roadMarkTag = new RoadMarkTag();
            NamedNodeMap attributes = node.getAttributes();

            Node sOffst = attributes.getNamedItem("sOffst");
            if (sOffst != null)
                roadMarkTag.sOffst = new Length(Double.parseDouble(sOffst.getNodeValue().trim()), LengthUnit.METER);

            Node type = attributes.getNamedItem("type");
            if (type != null)
                roadMarkTag.type = type.getNodeValue().trim();

            Node weight = attributes.getNamedItem("weight");
            if (weight != null)
                roadMarkTag.weight = weight.getNodeValue().trim();

            Node color = attributes.getNamedItem("color");
            if (color != null)
                roadMarkTag.color = color.getNodeValue().trim();

            Node width = attributes.getNamedItem("width");
            if (width != null)
                roadMarkTag.width = new Length(Double.parseDouble(width.getNodeValue().trim()), LengthUnit.METER);

            Node laneChange = attributes.getNamedItem("laneChange");
            if (laneChange != null)
                roadMarkTag.laneChange = laneChange.getNodeValue().trim();

            laneTag.roadMarkTags.add(roadMarkCount, roadMarkTag);
            roadMarkCount++;
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RoadMarkTag [sOffst=" + this.sOffst + ", type=" + this.type + ", weight=" + this.weight + ", color="
                + this.color + ", width=" + this.width + ", laneChange=" + this.laneChange + "]";
    }
}
