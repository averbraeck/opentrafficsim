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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class HeightTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** The sOffst. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length sOffst = null;

    /** Inner. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length inner = null;

    /** Outer. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length outer = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList NodeList; the list of subnodes of the road node
     * @param parser OpenDriveNetworkLaneParser; the parser with the lists of information
     * @param laneTag LaneTag; the LaneTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseHeight(final NodeList nodeList, final OpenDriveNetworkLaneParserOld parser, final LaneTag laneTag)
            throws SAXException, NetworkException
    {
        int heightCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "height"))
        {
            heightCount++;
            HeightTag heightTag = new HeightTag();
            NamedNodeMap attributes = node.getAttributes();

            Node sOffst = attributes.getNamedItem("sOffst");
            if (sOffst != null)
                heightTag.sOffst = new Length(Double.parseDouble(sOffst.getNodeValue().trim()), LengthUnit.METER);

            Node inner = attributes.getNamedItem("inner");
            if (inner != null)
                heightTag.inner = new Length(Double.parseDouble(inner.getNodeValue().trim()), LengthUnit.METER);

            Node outer = attributes.getNamedItem("outer");
            if (outer != null)
                heightTag.outer = new Length(Double.parseDouble(outer.getNodeValue().trim()), LengthUnit.METER);

            laneTag.heightTag = heightTag;
        }

        if (heightCount > 1)
            System.err.println("ROAD: more than one height tag for road");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeightTag [sOffst=" + this.sOffst + ", inner=" + this.inner + ", outer=" + this.outer + "]";
    }
}
