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
 * ROADTYPE Tag.
 *
 * <pre>
 * {@code
  <xsd:element name="LANETYPE">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="SPEEDLIMIT" minOccurs="1" maxOccurs="unbounded">
          <xsd:complexType>
            <xsd:attribute name="GTUTYPE" type="xsd:string" use="required" />
            <xsd:attribute name="LEGALSPEEDLIMIT" type="SPEEDTYPE" use="optional" />
          </xsd:complexType>
        </xsd:element>
      </xsd:sequence>
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
      <xsd:attribute name="DEFAULTLANEWIDTH" type="LENGTHTYPE" use="optional" />
      <xsd:attribute name="DEFAULTLANEKEEPING" type="LANEKEEPINGTYPE" use="optional" />
      <xsd:attribute ref="xml:base" />
    </xsd:complexType>
  </xsd:element>
 * }
 * </pre>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
class RoadTypeTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Speed limits. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<GTUType, Speed> legalSpeedLimits = new LinkedHashMap<>();

    /** Default lane width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length defaultLaneWidth = null;

    /** The lane keeping policy, i.e., keep left, keep right or keep lane. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    LaneKeepingPolicy defaultLaneKeepingPolicy = null;

    /** The overtaking conditions for the lanes of this road type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    OvertakingConditions defaultOvertakingConditions = null;

    /**
     * Parse the ROADTYPE tags. Delegates to a separate method because the RoadTypeTag can also occur inside a LINK tag. In the
     * latter case, it should not be stored in the central map. When this parseRoadTypes method is called, the tags <b>are</b>
     * stored in the central map in the parser class.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoadTypes(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "ROADTYPE"))
        {
            RoadTypeTag roadTypeTag = parseRoadType(node, parser);
            parser.getRoadTypeTags().put(roadTypeTag.name, roadTypeTag);
        }
    }

    /**
     * Parse the ROADTYPE tags.
     * @param node Node; the ROADTYPE nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @return the parsed RoadTypeTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadTypeTag parseRoadType(final Node node, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        RoadTypeTag roadTypeTag = new RoadTypeTag();

        Node name = attributes.getNamedItem("NAME");
        if (name == null)
        {
            throw new SAXException("ROADTYPE: missing attribute NAME");
        }
        roadTypeTag.name = name.getNodeValue().trim();
        if (parser.getRoadTypeTags().keySet().contains(roadTypeTag.name))
        {
            throw new SAXException("ROADTYPE: NAME " + roadTypeTag.name + " defined twice");
        }

        Node width = attributes.getNamedItem("DEFAULTLANEWIDTH");
        if (width != null)
        {
            roadTypeTag.defaultLaneWidth = Length.valueOf(width.getNodeValue());
        }

        Node lkp = attributes.getNamedItem("DEFAULTLANEKEEPING");
        if (lkp != null)
        {
            roadTypeTag.defaultLaneKeepingPolicy = LaneAttributes.parseLaneKeepingPolicy(lkp.getNodeValue().trim());
        }

        // Node oc = attributes.getNamedItem("DEFAULTOVERTAKING");
        // if (oc != null) {
        // roadTypeTag.defaultOvertakingConditions = LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(),
        // parser);
        // }

        List<Node> speedLimitList = XMLParser.getNodes(node.getChildNodes(), "SPEEDLIMIT");
        if (speedLimitList.size() == 0)
        {
            throw new SAXException("ROADTYPE: missing tag SPEEDLIMIT");
        }
        for (Node speedLimitNode : speedLimitList)
        {
            NamedNodeMap speedLimitAttributes = speedLimitNode.getAttributes();

            Node gtuTypeName = speedLimitAttributes.getNamedItem("GTUTYPE");
            if (gtuTypeName == null)
            {
                throw new NetworkException("ROADTYPE: No GTUTYPE defined");
            }
            if (!parser.getGtuTypes().containsKey(gtuTypeName.getNodeValue().trim()))
            {
                throw new NetworkException(
                        "ROADTYPE: " + roadTypeTag.name + " GTUTYPE " + gtuTypeName.getNodeValue().trim() + " not defined");
            }
            GTUType gtuType = parser.getGtuTypes().get(gtuTypeName.getNodeValue().trim());

            Node speedNode = speedLimitAttributes.getNamedItem("LEGALSPEEDLIMIT");
            if (speedNode == null)
            {
                throw new NetworkException(
                        "ROADTYPE: " + roadTypeTag.name + " GTUTYPE " + gtuType.getId() + ": LEGALSPEEDLIMIT not defined");
            }
            Speed speed = Speed.valueOf(speedNode.getNodeValue().trim());

            roadTypeTag.legalSpeedLimits.put(gtuType, speed);
        }

        return roadTypeTag;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RoadTypeTag [name=" + this.name + ", legalSpeedLimits=" + this.legalSpeedLimits + ", defaultLaneWidth="
                + this.defaultLaneWidth + ", defaultLaneKeepingPolicy=" + this.defaultLaneKeepingPolicy
                + ", defaultOvertakingConditions=" + this.defaultOvertakingConditions + "]";
    }

}
