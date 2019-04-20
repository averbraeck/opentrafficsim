package org.opentrafficsim.road.network.factory.xml.old;

import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Colors;
import org.opentrafficsim.core.network.factory.xml.units.Directions;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * CrossSectionElement tags as part of the ROADLAYOUT tag.
 * 
 * <pre>
 * {@code
  <xsd:element name="ROADLAYOUT">
    <xsd:complexType>
      <xsd:sequence>
        ...
        <xsd:choice minOccurs="1" maxOccurs="unbounded">

          <xsd:element name="LANE" minOccurs="0" maxOccurs="unbounded">
            <xsd:complexType>
              <xsd:sequence minOccurs="0" maxOccurs="unbounded">
                <xsd:element name="SPEEDLIMIT" minOccurs="1" maxOccurs="unbounded">
                  <xsd:complexType>
                    <xsd:attribute name="GTUTYPE" type="xsd:string" use="required" />
                    <xsd:attribute name="LEGALSPEEDLIMIT" type="SPEEDTYPE" use="optional" />
                  </xsd:complexType>
                </xsd:element>
              </xsd:sequence>
              <xsd:attribute name="NAME" type="xsd:string" use="required" />
              <xsd:attribute name="LANETYPE" type="xsd:string" use="optional" />
              <xsd:attribute name="OFFSET" type="SIGNEDLENGTHTYPE" use="required" />
              <xsd:attribute name="WIDTH" type="LENGTHTYPE" use="optional" />
              <xsd:attribute name="DIRECTION" type="DIRECTIONTYPE" use="required" />
              <xsd:attribute name="COLOR" type="COLORTYPE" use="optional" />
              <xsd:attribute name="OVERTAKING" type="OVERTAKINGTYPE" use="optional" />
            </xsd:complexType>
          </xsd:element>

          <xsd:element name="NOTRAFFICLANE" minOccurs="0" maxOccurs="unbounded">
            <xsd:complexType>
              <xsd:attribute name="NAME" type="xsd:string" use="optional" />
              <xsd:attribute name="OFFSET" type="SIGNEDLENGTHTYPE" use="required" />
              <xsd:attribute name="WIDTH" type="LENGTHTYPE" use="optional" />
              <xsd:attribute name="COLOR" type="COLORTYPE" use="optional" />
            </xsd:complexType>
          </xsd:element>

          <xsd:element name="SHOULDER" minOccurs="0" maxOccurs="unbounded">
            <xsd:complexType>
              <xsd:attribute name="NAME" type="xsd:string" use="optional" />
              <xsd:attribute name="OFFSET" type="SIGNEDLENGTHTYPE" use="required" />
              <xsd:attribute name="WIDTH" type="LENGTHTYPE" use="optional" />
              <xsd:attribute name="COLOR" type="COLORTYPE" use="optional" />
            </xsd:complexType>
          </xsd:element>

          <xsd:element name="STRIPE" minOccurs="0" maxOccurs="unbounded">
            <xsd:complexType>
              <xsd:attribute name="NAME" type="xsd:string" use="optional" />
              <xsd:attribute name="TYPE" type="STRIPETYPE" use="required" />
              <xsd:attribute name="OFFSET" type="SIGNEDLENGTHTYPE" use="required" />
              <xsd:attribute name="WIDTH" type="LENGTHTYPE" use="optional" />
              <xsd:attribute name="COLOR" type="COLORTYPE" use="optional" />
            </xsd:complexType>
          </xsd:element>

        </xsd:choice>
      </xsd:sequence>
      ...
    </xsd:complexType>
  </xsd:element>
 * }
 * </pre>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        LANE,
        NOTRAFFICLANE,
        SHOULDER,
        STRIPE
    };

    /** Stripe types. */
    @SuppressWarnings({"javadoc", "checkstyle:javadocvariable"})
    enum StripeType
    {
        SOLID,
        DASHED,
        BLOCKED,
        DOUBLE,
        LEFTONLY,
        RIGHTONLY
    };

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ElementType elementType = null;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Lane type in case elementType is a LANE. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneTypeTag laneTypeTag = null;

    /** Stripe type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    StripeType stripeType = null;

    /** Offset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offset = null;

    /** Start offset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offSetStart = null;

    /** End offset. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length offSetEnd = null;

    /** Speed limits. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<GTUType, Speed> legalSpeedLimits = null;

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
    OvertakingConditions overtakingConditions = null;

    /**
     * Parse the ROADLAYOUT.LANE tag.
     * @param node Node; the node of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param roadLayoutTag RoadLayoutTag; the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseLane(final Node node, final XmlNetworkLaneParserOld parser,
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

        if (attributes.getNamedItem("LANETYPE") != null)
        {
            String laneTypeString = attributes.getNamedItem("LANETYPE").getNodeValue().trim();
            if (!parser.laneTypeTags.containsKey(laneTypeString))
                throw new SAXException("ROADLAYOUT.LANE: LANETYPE " + laneTypeString + " for lane " + roadLayoutTag.name + "."
                        + name + " not defined");
            cseTag.laneTypeTag = parser.laneTypeTags.get(laneTypeString);
        }

        // if (attributes.getNamedItem("OFFSET") != null)
        // cseTag.offset = LengthUnits.parseLength(attributes.getNamedItem("OFFSET").getNodeValue());
        // else
        // throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);
        parseOffset(cseTag, attributes, roadLayoutTag);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLength(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.defaultLaneWidth;
        else if (roadLayoutTag.roadTypeTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.roadTypeTag.defaultLaneWidth;
        else
            throw new SAXException("ROADLAYOUT.LANE: cannot determine WIDTH for lane: " + roadLayoutTag.name + "." + name);

        List<Node> speedLimitList = XMLParser.getNodes(node.getChildNodes(), "SPEEDLIMIT");
        if (speedLimitList.size() > 0)
            cseTag.legalSpeedLimits = new LinkedHashMap<>();
        for (Node speedLimitNode : speedLimitList)
        {
            NamedNodeMap speedLimitAttributes = speedLimitNode.getAttributes();

            Node gtuTypeName = speedLimitAttributes.getNamedItem("GTUTYPE");
            if (gtuTypeName == null)
                throw new NetworkException("ROADLAYOUT.LANE.SPEEDLIMIT: No GTUTYPE defined");
            if (!parser.gtuTypes.containsKey(gtuTypeName.getNodeValue().trim()))
                throw new NetworkException("ROADLAYOUT.LANE.SPEEDLIMIT: " + roadLayoutTag.name + " GTUTYPE "
                        + gtuTypeName.getNodeValue().trim() + " not defined");
            GTUType gtuType = parser.gtuTypes.get(gtuTypeName.getNodeValue().trim());

            Node speedNode = speedLimitAttributes.getNamedItem("LEGALSPEEDLIMIT");
            if (speedNode == null)
                throw new NetworkException("ROADLAYOUT.LANE.SPEEDLIMIT: " + roadLayoutTag.name + " GTUTYPE " + gtuType.getId()
                        + ": LEGALSPEEDLIMIT not defined");
            Speed speed = SpeedUnits.parseSpeed(speedNode.getNodeValue().trim());

            cseTag.legalSpeedLimits.put(gtuType, speed);
        }

        if (cseTag.legalSpeedLimits == null)
        {
            if (cseTag.laneTypeTag != null && cseTag.laneTypeTag.legalSpeedLimits != null)
                cseTag.legalSpeedLimits = new LinkedHashMap<>(cseTag.laneTypeTag.legalSpeedLimits);
            else if (roadLayoutTag.legalSpeedLimits != null)
                cseTag.legalSpeedLimits = new LinkedHashMap<>(roadLayoutTag.legalSpeedLimits);
            else if (roadLayoutTag.roadTypeTag.legalSpeedLimits != null)
                cseTag.legalSpeedLimits = new LinkedHashMap<>(roadLayoutTag.roadTypeTag.legalSpeedLimits);
            else
                throw new SAXException("ROADLAYOUT.LANE: cannot determine SPEED for lane: " + roadLayoutTag.name + "." + name);
        }

        if (attributes.getNamedItem("DIRECTION") == null)
            throw new SAXException("ROADLAYOUT.LANE: missing attribute DIRECTION for lane " + roadLayoutTag.name + "." + name);
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
        else if (roadLayoutTag.roadTypeTag.defaultOvertakingConditions != null)
            cseTag.overtakingConditions = roadLayoutTag.roadTypeTag.defaultOvertakingConditions;
        else
            throw new SAXException("ROADLAYOUT.LANE: cannot determine OVERTAKING for lane: " + roadLayoutTag.name + "." + name);

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.NOTRAFFICLANE tag.
     * @param node Node; the node of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param roadLayoutTag RoadLayoutTag; the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseNoTrafficLane(final Node node, final XmlNetworkLaneParserOld parser,
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

        // if (attributes.getNamedItem("OFFSET") != null)
        // cseTag.offset = LengthUnits.parseLength(attributes.getNamedItem("OFFSET").getNodeValue());
        // else
        // throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);
        parseOffset(cseTag, attributes, roadLayoutTag);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLength(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.defaultLaneWidth;
        else if (roadLayoutTag.roadTypeTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.roadTypeTag.defaultLaneWidth;
        else
            throw new SAXException(
                    "ROADLAYOUT.NOTRAFFICLANE: cannot determine WIDTH for NOTRAFFICLANE: " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GRAY;

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.SHOULDER tag.
     * @param node Node; the node of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param roadLayoutTag RoadLayoutTag; the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseShoulder(final Node node, final XmlNetworkLaneParserOld parser,
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

        // if (attributes.getNamedItem("OFFSET") != null)
        // cseTag.offset = LengthUnits.parseLength(attributes.getNamedItem("OFFSET").getNodeValue());
        // else
        // throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);
        parseOffset(cseTag, attributes, roadLayoutTag);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLength(attributes.getNamedItem("WIDTH").getNodeValue());
        else if (roadLayoutTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.defaultLaneWidth;
        else if (roadLayoutTag.roadTypeTag.defaultLaneWidth != null)
            cseTag.width = roadLayoutTag.roadTypeTag.defaultLaneWidth;
        else
            throw new SAXException(
                    "ROADLAYOUT.SHOULDER: cannot determine WIDTH for NOTRAFFICLANE: " + roadLayoutTag.name + "." + name);

        if (attributes.getNamedItem("COLOR") != null)
            cseTag.color = Colors.parseColor(attributes.getNamedItem("COLOR").getNodeValue());
        else
            cseTag.color = Color.GREEN;

        roadLayoutTag.cseTags.put(cseTag.name, cseTag);
        return cseTag;
    }

    /**
     * Parse the ROADLAYOUT.STRIPE tag.
     * @param node Node; the node of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @param roadLayoutTag RoadLayoutTag; the tag with the enclosing information
     * @return the cross section element for this part of the road
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static CrossSectionElementTag parseStripe(final Node node, final XmlNetworkLaneParserOld parser,
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

        // if (attributes.getNamedItem("OFFSET") != null)
        // cseTag.offset = LengthUnits.parseLength(attributes.getNamedItem("OFFSET").getNodeValue());
        // else
        // throw new SAXException("ROADLAYOUT.LANE: missing attribute OFFSET for lane " + roadLayoutTag.name + "." + name);
        parseOffset(cseTag, attributes, roadLayoutTag);

        if (attributes.getNamedItem("WIDTH") != null)
            cseTag.width = LengthUnits.parseLength(attributes.getNamedItem("WIDTH").getNodeValue());
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
     * @param stripeStr String; the stripe string.
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

    /**
     * @param cseTag CrossSectionElementTag; element tag
     * @param attributes NamedNodeMap; attributes
     * @param roadLayoutTag RoadLayoutTag; road layout tag
     * @throws SAXException on xml exception
     * @throws DOMException on xml exception
     * @throws NetworkException on network exception
     */
    private static void parseOffset(final CrossSectionElementTag cseTag, final NamedNodeMap attributes,
            final RoadLayoutTag roadLayoutTag) throws SAXException, DOMException, NetworkException
    {
        if (attributes.getNamedItem("OFFSET") != null)
            cseTag.offset = LengthUnits.parseLength(attributes.getNamedItem("OFFSET").getNodeValue());

        if (attributes.getNamedItem("OFFSETSTART") != null)
            cseTag.offSetStart = LengthUnits.parseLength(attributes.getNamedItem("OFFSETSTART").getNodeValue().trim());

        if (attributes.getNamedItem("OFFSETEND") != null)
            cseTag.offSetEnd = LengthUnits.parseLength(attributes.getNamedItem("OFFSETEND").getNodeValue().trim());

        if ((cseTag.offset == null && (cseTag.offSetStart == null || cseTag.offSetEnd == null))
                || (cseTag.offset != null && (cseTag.offSetStart != null || cseTag.offSetEnd != null)))
        {
            String namedPart = attributes.getNamedItem("NAME") == null ? "on " + roadLayoutTag.name
                    : roadLayoutTag.name + "." + cseTag.name;
            throw new SAXException("ROADLAYOUT." + cseTag.elementType
                    + ": missing attribute OFFSET or both STARTOFFSET and ENDOFFSET for cross-section element " + namedPart);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CrossSectionElementTag [elementType=" + this.elementType + ", name=" + this.name + ", laneTypeTag="
                + this.laneTypeTag + ", stripeType=" + this.stripeType + ", offset=" + this.offset + ", legalSpeedLimits="
                + this.legalSpeedLimits + ", width=" + this.width + ", direction=" + this.direction + ", color=" + this.color
                + ", overtakingConditions=" + this.overtakingConditions + "]";
    }

}
