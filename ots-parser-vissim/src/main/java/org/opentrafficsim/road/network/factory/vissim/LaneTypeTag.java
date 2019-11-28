package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * LANETYPE Tag.
 *
 * <pre>
 * {@code
  <xsd:element name="LANETYPE">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="GTUTYPE" minOccurs="1" maxOccurs="unbounded">
          <xsd:complexType>
            <xsd:attribute name="NAME" type="xsd:string" use="required" />
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
class LaneTypeTag implements Serializable
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

    /**
     * Parse the LANETYPE tags.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseLaneTypes(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "LANETYPE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            LaneTypeTag laneTypeTag = new LaneTypeTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
            {
                throw new SAXException("LANETYPE: missing attribute NAME");
            }
            laneTypeTag.name = name.getNodeValue().trim();
            if (parser.getLaneTypeTags().keySet().contains(laneTypeTag.name))
            {
                throw new SAXException("LANETYPE: NAME " + laneTypeTag.name + " defined twice");
            }

            Node width = attributes.getNamedItem("DEFAULTLANEWIDTH");
            if (width != null)
            {
                laneTypeTag.defaultLaneWidth = Length.valueOf(width.getNodeValue());
            }

            Node lkp = attributes.getNamedItem("DEFAULTLANEKEEPING");
            if (lkp != null)
            {
                laneTypeTag.defaultLaneKeepingPolicy = org.opentrafficsim.road.network.factory.vissim.units.LaneAttributes
                        .parseLaneKeepingPolicy(lkp.getNodeValue().trim());
            }

            List<Node> speedLimitList = XMLParser.getNodes(node.getChildNodes(), "SPEEDLIMIT");
            if (speedLimitList.size() == 0)
            {
                throw new SAXException("LANETYPE: missing tag SPEEDLIMIT");
            }
            for (Node speedLimitNode : speedLimitList)
            {
                NamedNodeMap speedLimitAttributes = speedLimitNode.getAttributes();

                Node gtuTypeName = speedLimitAttributes.getNamedItem("GTUTYPE");
                if (gtuTypeName == null)
                {
                    throw new NetworkException("LANETYPE: No GTUTYPE defined");
                }
                if (!parser.getGtuTypes().containsKey(gtuTypeName.getNodeValue().trim()))
                {
                    throw new NetworkException(
                            "LANETYPE: " + laneTypeTag.name + " GTUTYPE " + gtuTypeName.getNodeValue().trim() + " not defined");
                }
                GTUType gtuType = parser.getGtuTypes().get(gtuTypeName.getNodeValue().trim());

                Node speedNode = speedLimitAttributes.getNamedItem("LEGALSPEEDLIMIT");
                if (speedNode == null)
                {
                    throw new NetworkException(
                            "LANETYPE: " + laneTypeTag.name + " GTUTYPE " + gtuType.getId() + ": LEGALSPEEDLIMIT not defined");
                }
                Speed speed = Speed.valueOf(speedNode.getNodeValue().trim());

                laneTypeTag.legalSpeedLimits.put(gtuType, speed);
            }
            parser.getLaneTypeTags().put(laneTypeTag.name, laneTypeTag);
        }
    }

}
