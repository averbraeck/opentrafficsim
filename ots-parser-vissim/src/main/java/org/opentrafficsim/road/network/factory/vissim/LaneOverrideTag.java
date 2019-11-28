package org.opentrafficsim.road.network.factory.vissim;

import java.awt.Color;
import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Colors;
import org.opentrafficsim.core.network.factory.xml.units.Directions;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 24, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class LaneOverrideTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150724L;

    /** Speed limit. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed speed = null;

    /** Direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LongitudinalDirectionality direction;

    /** Animation color. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Color color;

    /** The overtaking conditions for this lane, i.e., overtake on the left, and on the right under 25 km/h. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions overtakingConditions = null;

    /**
     * Parse the LINK.LANEOVERRIDE tag.
     * @param node Node; the XML-node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param linkTag LinkTag; the parent link tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLaneOverride(final Node node, final VissimNetworkLaneParser parser, final LinkTag linkTag)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        LaneOverrideTag laneOverrideTag = new LaneOverrideTag();

        if (attributes.getNamedItem("LANE") == null)
        {
            throw new SAXException("LANEOVERRIDE: missing attribute LANE" + " for link " + linkTag.name);
        }
        String name = attributes.getNamedItem("LANE").getNodeValue().trim();
        if (linkTag.laneOverrideTags.containsKey(name))
        {
            throw new SAXException("LANEOVERRIDE: LANE OVERRIDE with LANE " + name + " defined twice");
        }

        if (attributes.getNamedItem("SPEED") != null)
        {
            laneOverrideTag.speed = Speed.valueOf(attributes.getNamedItem("SPEED").getNodeValue().trim());
        }

        if (attributes.getNamedItem("DIRECTION") != null)
        {
            laneOverrideTag.direction = Directions.parseDirection(attributes.getNamedItem("DIRECTION").getNodeValue().trim());
        }

        if (attributes.getNamedItem("COLOR") != null)
        {
            laneOverrideTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue().trim());
        }

        // Node oc = attributes.getNamedItem("OVERTAKING");
        // if (oc != null) {
        // laneOverrideTag.overtakingConditions = LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(),
        // parser);
        // }

        linkTag.laneOverrideTags.put(name.trim(), laneOverrideTag);

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneOverrideTag [speed=" + this.speed + ", direction=" + this.direction + ", color=" + this.color
                + ", overtakingConditions=" + this.overtakingConditions + "]";
    }

}
