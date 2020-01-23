package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.vissim.units.LaneAttributes;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ROADLAYOUT tag parser.
 *
 * <pre>
 * {@code
  <xsd:element name="ROADLAYOUT">
    <xsd:complexType>
      <xsd:sequence>

        <xsd:element name="SPEEDLIMIT" minOccurs="0" maxOccurs="unbounded">
          <xsd:complexType>
            <xsd:attribute name="GTUTYPE" type="xsd:string" use="required" />
            <xsd:attribute name="LEGALSPEEDLIMIT" type="SPEEDTYPE" use="optional" />
          </xsd:complexType>
        </xsd:element>

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
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
      <xsd:attribute name="ROADTYPE" type="xsd:string" use="required" />
      <xsd:attribute name="WIDTH" type="LENGTHTYPE" use="optional" />
      <xsd:attribute name="LANEKEEPING" type="LANEKEEPINGTYPE" use="optional" />
      <xsd:attribute name="OVERTAKING" type="OVERTAKINGTYPE" use="optional" />
      <xsd:attribute ref="xml:base" />
    </xsd:complexType>
  </xsd:element>
 * }
 * </pre>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class RoadLayoutTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Road Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    RoadTypeTag roadTypeTag = null;

    /** Speed limits. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<GTUType, Speed> legalSpeedLimits = null;

    /** The lane keeping policy, i.e., keep left, keep right or keep lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneKeepingPolicy laneKeepingPolicy = null;

    /** The overtaking conditions for the lanes of this road type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions overtakingConditions = null;

    /** Default lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length defaultLaneWidth = null;

    /** CrossSectionElementTags, order is important, so a LinkedHashMap. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, CrossSectionElementTag> cseTags = new LinkedHashMap<>();

    /**
     * Parse the ROADLAYOUT tags. Delegates to a separate method because the RoadTypeTag can also occur inside a LINK tag. In
     * the latter case, it should not be stored in the central map. When this parseRoadTypes method is called, the tags
     * <b>are</b> stored in the central map in the parser class.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoadTypes(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "ROADLAYOUT"))
        {
            RoadLayoutTag roadLayoutTag = parseRoadType(node, parser);
            parser.getRoadLayoutTags().put(roadLayoutTag.name, roadLayoutTag);
        }
    }

    /**
     * Parse the ROADLAYOUT tags.
     * @param node Node; the ROADLAYOUT nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @return the parsed RoadTypeTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadLayoutTag parseRoadType(final Node node, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        RoadLayoutTag roadLayoutTag = new RoadLayoutTag();

        Node name = attributes.getNamedItem("NAME");
        if (name == null)
        {
            throw new SAXException("ROADLAYOUT: missing attribute NAME");
        }
        roadLayoutTag.name = name.getNodeValue().trim();
        if (parser.getRoadLayoutTags().keySet().contains(roadLayoutTag.name))
        {
            throw new SAXException("ROADLAYOUT: NAME " + roadLayoutTag.name + " defined twice");
        }

        Node roadType = attributes.getNamedItem("ROADTYPE");
        if (roadType == null)
        {
            throw new SAXException("ROADLAYOUT: missing attribute ROADTYPE");
        }
        if (!parser.getRoadTypeTags().containsKey(roadType.getNodeValue().trim()))
        {
            throw new SAXException("ROADLAYOUT: ROADTYPE " + roadType.getNodeValue().trim() + " not defined");
        }
        roadLayoutTag.roadTypeTag = parser.getRoadTypeTags().get(roadType.getNodeValue().trim());

        Node width = attributes.getNamedItem("WIDTH");
        if (width != null)
        {
            roadLayoutTag.defaultLaneWidth = Length.valueOf(width.getNodeValue());
        }

        Node lkp = attributes.getNamedItem("LANEKEEPING");
        if (lkp != null)
        {
            roadLayoutTag.laneKeepingPolicy = LaneAttributes.parseLaneKeepingPolicy(lkp.getNodeValue().trim());
        }

        Node oc = attributes.getNamedItem("OVERTAKING");
        if (oc != null)
        {
            roadLayoutTag.overtakingConditions = LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(), parser);
        }

        List<Node> speedLimitList = XMLParser.getNodes(node.getChildNodes(), "SPEEDLIMIT");
        if (speedLimitList.size() > 0)
        {
            roadLayoutTag.legalSpeedLimits = new LinkedHashMap<>();
        }
        for (Node speedLimitNode : speedLimitList)
        {
            NamedNodeMap speedLimitAttributes = speedLimitNode.getAttributes();

            Node gtuTypeName = speedLimitAttributes.getNamedItem("GTUTYPE");
            if (gtuTypeName == null)
            {
                throw new NetworkException("ROADLAYOUT.SPEEDLIMIT: No GTUTYPE defined");
            }
            if (!parser.getGtuTypes().containsKey(gtuTypeName.getNodeValue().trim()))
            {
                throw new NetworkException("ROADLAYOUT.SPEEDLIMIT: " + roadLayoutTag.name + " GTUTYPE "
                        + gtuTypeName.getNodeValue().trim() + " not defined");
            }
            GTUType gtuType = parser.getGtuTypes().get(gtuTypeName.getNodeValue().trim());

            Node speedNode = speedLimitAttributes.getNamedItem("LEGALSPEEDLIMIT");
            if (speedNode == null)
            {
                throw new NetworkException("ROADLAYOUT.SPEEDLIMIT: " + roadLayoutTag.name + " GTUTYPE " + gtuType.getId()
                        + ": LEGALSPEEDLIMIT not defined");
            }
            Speed speed = Speed.valueOf(speedNode.getNodeValue().trim());

            roadLayoutTag.legalSpeedLimits.put(gtuType, speed);
        }

        int cseCount = 0;

        for (Node laneNode : XMLParser.getNodes(node.getChildNodes(), "LANE"))
        {
            CrossSectionElementTag.parseLane(laneNode, parser, roadLayoutTag);
            cseCount++;
        }

        for (Node ntlNode : XMLParser.getNodes(node.getChildNodes(), "NOTRAFFICLANE"))
        {
            CrossSectionElementTag.parseNoTrafficLane(ntlNode, parser, roadLayoutTag);
            cseCount++;
        }

        for (Node stripeNode : XMLParser.getNodes(node.getChildNodes(), "STRIPE"))
        {
            CrossSectionElementTag.parseStripe(stripeNode, parser, roadLayoutTag);
            cseCount++;
        }

        for (Node shoulderNode : XMLParser.getNodes(node.getChildNodes(), "SHOULDER"))
        {
            CrossSectionElementTag.parseShoulder(shoulderNode, parser, roadLayoutTag);
            cseCount++;
        }

        if (cseCount == 0)
        {
            throw new NetworkException("ROADLAYOUT: No elements defined for road type " + roadLayoutTag.name);
        }

        return roadLayoutTag;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RoadLayoutTag [name=" + this.name + ", roadTypeTag=" + this.roadTypeTag + ", legalSpeedLimits="
                + this.legalSpeedLimits + ", laneKeepingPolicy=" + this.laneKeepingPolicy + ", overtakingConditions="
                + this.overtakingConditions + ", defaultLaneWidth=" + this.defaultLaneWidth + ", cseTags=" + this.cseTags + "]";
    }

}
