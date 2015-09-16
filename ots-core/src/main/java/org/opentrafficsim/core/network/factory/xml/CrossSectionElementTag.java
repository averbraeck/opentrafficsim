package org.opentrafficsim.core.network.factory.xml;

import java.awt.Color;
import java.util.UUID;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Colors;
import org.opentrafficsim.core.network.factory.xml.units.Directions;
import org.opentrafficsim.core.network.factory.xml.units.LaneAttributes;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.changing.OvertakingConditions;
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
class CrossSectionElementTag implements OTS_SCALAR
{
    /** element types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    enum ElementType
    {
        LANE, NOTRAFFICLANE, SHOULDER, STRIPE
    };

    /** stripe types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    enum StripeType
    {
        SOLID, DASHED, BLOCKED, DOUBLE, LEFTONLY, RIGHTONLY
    };

    /** type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ElementType elementType = null;

    /** name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** lane type name in case elementType is a LANE. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneTypeString = null;

    /** lane type in case elementType is a LANE. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneType laneType = XmlNetworkLaneParser.noTrafficLaneType;

    /** stripe type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    StripeType stripeType = null;

    /** offset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel offset = null;

    /** speed limit. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed.Abs speed = null;

    /** lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length.Rel width = null;

    /** direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LongitudinalDirectionality direction;

    /** animation color. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Color color;

    /** overtaking conditions. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions overtakingConditions = new OvertakingConditions.LeftAndRight();

    /**
     * Parse the ROADTYPE.LANE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadTypeTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseLane(final Node node, final XmlNetworkLaneParser parser,
        final RoadTypeTag roadTypeTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        if (attributes.getNamedItem("NAME") == null)
            throw new SAXException("ROADTYPE.LANE: missing attribute NAME for ROADTYPE " + roadTypeTag.name);
        String name = attributes.getNamedItem("NAME").getNodeValue().trim();
        if (roadTypeTag.cseTags.containsKey(name))
            throw new SAXException("ROADTYPE.LANE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.LANE;

        if (attributes.getNamedItem("TYPE") == null)
            throw new SAXException("ROADTYPE.LANE: missing attribute TYPE for lane " + roadTypeTag.name + "." + name);
        cseTag.laneTypeString = attributes.getNamedItem("TYPE").getNodeValue().trim();
        if (!parser.laneTypes.containsKey(cseTag.laneTypeString))
            throw new SAXException("ROADTYPE.LANE: TYPE " + cseTag.laneTypeString + " for lane " + roadTypeTag.name
                + "." + name + " does not have compatible GTUs defined in a COMPATIBILITY element");
        cseTag.laneType = parser.laneTypes.get(cseTag.laneTypeString);

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADTYPE.LANE: missing attribute OFFSET for lane " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadTypeTag.width != null)
            cseTag.width = roadTypeTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADTYPE.LANE: cannot determine WIDTH for lane: " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("SPEED") != null)
            cseTag.speed = SpeedUnits.parseSpeedAbs(attributes.getNamedItem("SPEED").getNodeValue());
        else if (roadTypeTag.speed != null)
            cseTag.speed = roadTypeTag.speed;
        else if (parser.globalTag.defaultMaxSpeed != null)
            cseTag.speed = parser.globalTag.defaultMaxSpeed;
        else
            throw new SAXException("ROADTYPE.LANE: cannot determine SPEED for lane: " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("DIRECTION") == null)
            throw new SAXException("ROADTYPE.LANE: missing attribute DIRECTION for lane " + roadTypeTag.name + "."
                + name);
        cseTag.direction = Directions.parseDirection(attributes.getNamedItem("DIRECTION").getNodeValue());

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.LIGHT_GRAY;

        Node oc = attributes.getNamedItem("OVERTAKING");
        if (oc != null)
            cseTag.overtakingConditions = LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(), parser);
        else if (roadTypeTag.overtakingConditions != null)
            cseTag.overtakingConditions = roadTypeTag.overtakingConditions;
        else if (parser.globalTag.defaultOvertakingConditions != null)
            cseTag.overtakingConditions = parser.globalTag.defaultOvertakingConditions;
        else
            throw new SAXException("ROADTYPE.LANE: cannot determine OVERTAKING for lane: " + roadTypeTag.name + "."
                + name);

        roadTypeTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADTYPE.NOTRAFFICLANE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadTypeTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseNoTrafficLane(final Node node, final XmlNetworkLaneParser parser,
        final RoadTypeTag roadTypeTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadTypeTag.cseTags.containsKey(name))
            throw new SAXException("ROADTYPE.NOTRAFFICLANE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.NOTRAFFICLANE;

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADTYPE.LANE: missing attribute OFFSET for lane " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadTypeTag.width != null)
            cseTag.width = roadTypeTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADTYPE.NOTRAFFICLANE: cannot determine WIDTH for NOTRAFFICLANE: "
                + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GRAY;

        roadTypeTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADTYPE.SHOULDER tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadTypeTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseShoulder(final Node node, final XmlNetworkLaneParser parser,
        final RoadTypeTag roadTypeTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadTypeTag.cseTags.containsKey(name))
            throw new SAXException("ROADTYPE.SHOULDER: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.SHOULDER;

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADTYPE.LANE: missing attribute OFFSET for lane " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadTypeTag.width != null)
            cseTag.width = roadTypeTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADTYPE.SHOULDER: cannot determine WIDTH for NOTRAFFICLANE: " + roadTypeTag.name
                + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GREEN;

        roadTypeTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADTYPE.STRIPE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadTypeTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseStripe(final Node node, final XmlNetworkLaneParser parser,
        final RoadTypeTag roadTypeTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadTypeTag.cseTags.containsKey(name))
            throw new SAXException("ROADTYPE.STRIPE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.STRIPE;

        if (attributes.getNamedItem("TYPE") != null)
            cseTag.stripeType = parseStripeType(attributes.getNamedItem("TYPE").getNodeValue());

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADTYPE.LANE: missing attribute OFFSET for lane " + roadTypeTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else
            cseTag.width = new Length.Rel(0.2, METER);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.WHITE;

        roadTypeTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * @param stripeStr the stripe string.
     * @return the stripe type.
     * @throws NetworkException in case of unknown model.
     */
    private static StripeType parseStripeType(final String stripeStr) throws NetworkException
    {
        if (stripeStr.equals("SOLID"))
        {
            return StripeType.SOLID;
        }
        else if (stripeStr.equals("DASHED"))
        {
            return StripeType.DASHED;
        }
        else if (stripeStr.equals("BLOCKED"))
        {
            return StripeType.BLOCKED;
        }
        else if (stripeStr.equals("DOUBLE"))
        {
            return StripeType.DOUBLE;
        }
        else if (stripeStr.equals("LEFTONLY"))
        {
            return StripeType.LEFTONLY;
        }
        else if (stripeStr.equals("RIGHTONLY"))
        {
            return StripeType.RIGHTONLY;
        }
        throw new NetworkException("Unknown stripe type: " + stripeStr);
    }

}
