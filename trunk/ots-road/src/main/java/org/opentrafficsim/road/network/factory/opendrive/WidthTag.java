package org.opentrafficsim.road.network.factory.opendrive;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.XMLParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
class WidthTag 
{
    /** sOffst. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel sOffst = null;

    /** a. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel a = null;
    
    /** b. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel b = null;
    
    /** c. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel c = null;
    
    /** d. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel d = null;

    /**
     * Parse the attributes of the road.type tag. The sub-elements are parsed in separate classes.
     * @param nodeList the list of subnodes of the road node
     * @param parser the parser with the lists of information
     * @param laneTag the LaneTag to which this element belongs
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseWidth(final NodeList nodeList, final OpenDriveNetworkLaneParser parser, final LaneTag laneTag)
        throws SAXException, NetworkException
    {
        int widthCount = 0;
        for (Node node : XMLParser.getNodes(nodeList, "width"))
        {
            widthCount++;
            WidthTag widthTag = new WidthTag();
            NamedNodeMap attributes = node.getAttributes();

            Node sOffst = attributes.getNamedItem("sOffst");
            if (sOffst != null)
                widthTag.sOffst = new Length.Rel(Double.parseDouble(sOffst.getNodeValue().trim()), LengthUnit.METER);
            
            Node a = attributes.getNamedItem("a");
            if (a != null)
                widthTag.a = new Length.Rel(Double.parseDouble(a.getNodeValue().trim()), LengthUnit.METER);
            
            Node b = attributes.getNamedItem("b");
            if (b != null)
                widthTag.b = new Length.Rel(Double.parseDouble(b.getNodeValue().trim()), LengthUnit.METER);
            
            Node c = attributes.getNamedItem("c");
            if (c != null)
                widthTag.c = new Length.Rel(Double.parseDouble(c.getNodeValue().trim()), LengthUnit.METER);
            
            Node d = attributes.getNamedItem("d");
            if (d != null)
                widthTag.d = new Length.Rel(Double.parseDouble(d.getNodeValue().trim()), LengthUnit.METER);

            laneTag.widthTag = widthTag;
        }

        if (widthCount > 1)
            throw new SAXException("ROAD: more than one width tag for road");
    }
}
