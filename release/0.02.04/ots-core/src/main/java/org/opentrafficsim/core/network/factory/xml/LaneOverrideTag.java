package org.opentrafficsim.core.network.factory.xml;

import java.awt.Color;

import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Colors;
import org.opentrafficsim.core.network.factory.xml.units.Directions;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LaneOverrideTag
{
    /** speed limit. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    DoubleScalar.Abs<SpeedUnit> speed = null;

    /** direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LongitudinalDirectionality direction;

    /** animation color. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Color color;

    /**
     * Parse the LINK.LANEOVERRIDE tag.
     * @param node the XML-node to parse
     * @param parser the parser with the lists of information
     * @param linkTag the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLaneOverride(final Node node, final XmlNetworkLaneParser parser, final LinkTag linkTag)
        throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        LaneOverrideTag laneOverrideTag = new LaneOverrideTag();

        if (attributes.getNamedItem("LANE") == null)
            throw new SAXException("LANEOVERRIDE: missing attribute LANE" + " for link " + linkTag.name);
        String name = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.roadTypeTag == null)
            throw new NetworkException("LANEOVERRIDE: LANE " + name.trim() + " no ROADTYPE for link " + linkTag.name);
        CrossSectionElementTag laneTag = linkTag.roadTypeTag.cseTags.get(name.trim());
        if (laneTag == null)
            throw new NetworkException("LANEOVERRIDE: Lane with LANE " + name.trim() + " not found in elements of link "
                + linkTag.name + " - roadtype " + linkTag.roadTypeTag.name);
        if (linkTag.laneOverrideTags.containsKey(name))
            throw new SAXException("LANEOVERRIDE: LANE OVERRIDE with LANE " + name + " defined twice");

        if (attributes.getNamedItem("SPEED") != null)
            laneOverrideTag.speed = SpeedUnits.parseSpeedAbs(attributes.getNamedItem("SPEED").getNodeValue().trim());

        if (attributes.getNamedItem("DIRECTION") != null)
            laneOverrideTag.direction =
                Directions.parseDirection(attributes.getNamedItem("DIRECTION").getNodeValue().trim());

        if (attributes.getNamedItem("COLOR") != null)
            laneOverrideTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue().trim());

        linkTag.laneOverrideTags.put(name.trim(), laneOverrideTag);

    }

}
