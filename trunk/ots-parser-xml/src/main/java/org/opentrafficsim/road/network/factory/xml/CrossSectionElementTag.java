package org.opentrafficsim.road.network.factory.xml;

import java.awt.Color;
import java.io.Serializable;
import java.util.UUID;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Colors;
import org.opentrafficsim.core.network.factory.xml.units.Directions;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.factory.xml.units.LaneAttributes;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class CrossSectionElementTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Element types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    enum ElementType
    {
        LANE, NOTRAFFICLANE, SHOULDER, STRIPE
    };

    /** Stripe types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    enum StripeType
    {
        SOLID, DASHED, BLOCKED, DOUBLE, LEFTONLY, RIGHTONLY
    };

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ElementType elementType = null;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Lane type name in case elementType is a LANE. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String laneTypeString = null;

    /** Lane type in case elementType is a LANE. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneType laneType = null;

    /** Stripe type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    StripeType stripeType = null;

    /** Offset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offset = null;

    /** Speed limit. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed speed = null;

    /** Lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length width = null;

    /** Direction. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LongitudinalDirectionality direction;

    /** Animation color. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Color color;

    /** Overtaking conditions. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions overtakingConditions = new OvertakingConditions.LeftAndRight();

    /**
     * Parse the ROADLAYOUT.LANE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadLayoutTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseLane(final Node node, final XmlNetworkLaneParser parser,
        final RoadLayoutTag roadLayoutTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        if (attributes.getNamedItem("NAME") == null)
            throw new SAXException("ROADLAYOUT.LANE: missing attribute NAME for ROADLAYOUT " + roadLayoutTag.name);
        String name = attributes.getNamedItem("NAME").getNodeValue().trim();
        if (roadLayoutTag.cseTags.containsKey(name))
            throw new SAXException("ROADLAYOUT.LANE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.LANE;

        if (attributes.getNamedItem("TYPE") == null)
            throw new SAXException("ROADLAYOUT.LANE: missing attribute TYPE for lane " + roadLayoutTag.name + "." + name);
        cseTag.laneTypeString = attributes.getNamedItem("TYPE").getNodeValue().trim();
        if (!parser.laneTypes.containsKey(cseTag.laneTypeString))
            throw new SAXException("ROADLAYOUT.LANE: TYPE " + cseTag.laneTypeString + " for lane " + roadLayoutTag.name
                + "." + name + " does not have compatible GTUs defined in a COMPATIBILITY element");
        cseTag.laneType = parser.laneTypes.get(cseTag.laneTypeString);

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.width != null)
            cseTag.width = roadLayoutTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADLAYOUT.LANE: cannot determine WIDTH for lane: " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("SPEED") != null)
            cseTag.speed = SpeedUnits.parseSpeedAbs(attributes.getNamedItem("SPEED").getNodeValue());
        else if (roadLayoutTag.speed != null)
            cseTag.speed = roadLayoutTag.speed;
        else if (parser.globalTag.defaultMaxSpeed != null)
            cseTag.speed = parser.globalTag.defaultMaxSpeed;
        else
            throw new SAXException("ROADLAYOUT.LANE: cannot determine SPEED for lane: " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("DIRECTION") == null)
            throw new SAXException("ROADLAYOUT.LANE: missing attribute DIRECTION for lane " + roadLayoutTag.name + "."
                + name);
        cseTag.direction = Directions.parseDirection(attributes.getNamedItem("DIRECTION").getNodeValue());

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.LIGHT_GRAY;

        Node oc = attributes.getNamedItem("OVERTAKING");
        if (oc != null)
            cseTag.overtakingConditions = LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(), parser);
        else if (roadLayoutTag.overtakingConditions != null)
            cseTag.overtakingConditions = roadLayoutTag.overtakingConditions;
        else if (parser.globalTag.defaultOvertakingConditions != null)
            cseTag.overtakingConditions = parser.globalTag.defaultOvertakingConditions;
        else
            throw new SAXException("ROADLAYOUT.LANE: cannot determine OVERTAKING for lane: " + roadLayoutTag.name + "."
                + name);

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.NOTRAFFICLANE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadLayoutTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseNoTrafficLane(final Node node, final XmlNetworkLaneParser parser,
        final RoadLayoutTag roadLayoutTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadLayoutTag.cseTags.containsKey(name))
            throw new SAXException("ROADLAYOUT.NOTRAFFICLANE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.NOTRAFFICLANE;

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.width != null)
            cseTag.width = roadLayoutTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADLAYOUT.NOTRAFFICLANE: cannot determine WIDTH for NOTRAFFICLANE: "
                + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GRAY;

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.SHOULDER tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadLayoutTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseShoulder(final Node node, final XmlNetworkLaneParser parser,
        final RoadLayoutTag roadLayoutTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadLayoutTag.cseTags.containsKey(name))
            throw new SAXException("ROADLAYOUT.SHOULDER: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.SHOULDER;

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.width != null)
            cseTag.width = roadLayoutTag.width;
        else if (parser.globalTag.defaultLaneWidth != null)
            cseTag.width = parser.globalTag.defaultLaneWidth;
        else
            throw new SAXException("ROADLAYOUT.SHOULDER: cannot determine WIDTH for NOTRAFFICLANE: " + roadLayoutTag.name
                + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GREEN;

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.STRIPE tag.
     * @param node the node of the XML-file
     * @param parser the parser with the lists of information
     * @param roadLayoutTag the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseStripe(final Node node, final XmlNetworkLaneParser parser,
        final RoadLayoutTag roadLayoutTag) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        CrossSectionElementTag cseTag = new CrossSectionElementTag();

        String name;
        if (attributes.getNamedItem("NAME") != null)
            name = attributes.getNamedItem("NAME").getNodeValue().trim();
        else
            name = UUID.randomUUID().toString();
        if (roadLayoutTag.cseTags.containsKey(name))
            throw new SAXException("ROADLAYOUT.STRIPE: LANE NAME " + name + " defined twice");
        cseTag.name = name;

        cseTag.elementType = ElementType.STRIPE;

        if (attributes.getNamedItem("TYPE") != null)
            cseTag.stripeType = parseStripeType(attributes.getNamedItem("TYPE").getNodeValue());

        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLengthRel(attributes.getNamedItem("OFFSET").getNodeValue());
        else
            throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLengthRel(attributes.getNamedItem("WIDTH").getNodeValue());
        else
            cseTag.width = new Length(0.2, LengthUnit.METER);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.WHITE;

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CrossSectionElementTag [elementType=" + this.elementType + ", name=" + this.name + ", laneTypeString="
                + this.laneTypeString + ", laneType=" + this.laneType + ", stripeType=" + this.stripeType + ", offset="
                + this.offset + ", speed=" + this.speed + ", width=" + this.width + ", direction=" + this.direction
                + ", color=" + this.color + ", overtakingConditions=" + this.overtakingConditions + "]";
    }

}
