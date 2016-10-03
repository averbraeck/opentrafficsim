package org.opentrafficsim.road.network.factory.xml;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.road.network.factory.xml.units.LaneAttributes;
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
 * <xsd:element name="ROADTYPE">
 *   <xsd:complexType>
 *     <xsd:sequence>
 *       <xsd:element name="GTUTYPE" minOccurs="1" maxOccurs="unbounded">
 *         <xsd:complexType>
 *           <xsd:attribute name="NAME" type="xsd:string" use="required" />
 *           <xsd:attribute name="LEGALSPEEDLIMIT" type="SPEEDTYPE" use="required" />
 *         </xsd:complexType>
 *       </xsd:element>
 *     </xsd:sequence>
 *     <xsd:attribute name="NAME" type="xsd:string" use="required" />
 *     <xsd:attribute name="DEFAULTLANEWIDTH" type="LENGTHTYPE" use="required" />
 *     <xsd:attribute name="DEFAULTLANEKEEPING" type="LANEKEEPINGTYPE" use="required" />
 *     <xsd:attribute name="DEFAULTOVERTAKING" type="OVERTAKINGTYPE" use="required" />
 *     <xsd:attribute ref="xml:base" />
 *   </xsd:complexType>
 * </xsd:element>
 * }
 * </pre>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
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

    /** CrossSectionElementTags, order is important, so a LinkedHashMap. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Map<String, Speed> legalSpeedLimitTags = new LinkedHashMap<>();

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
     * @param nodeList nodeList the top-level nodes of the XML-file
     * @param parser the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseRoadTypes(final NodeList nodeList, final XmlNetworkLaneParser parser) throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "ROADTYPE"))
        {
            RoadTypeTag roadTypeTag = parseRoadType(node, parser);
            parser.roadTypeTags.put(roadTypeTag.name, roadTypeTag);
        }
    }

    /**
     * Parse the ROADTYPE tags.
     * @param node the ROADTYPE nodes of the XML-file
     * @param parser the parser with the lists of information
     * @return the parsed RoadTypeTag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static RoadTypeTag parseRoadType(final Node node, final XmlNetworkLaneParser parser) throws SAXException, NetworkException
    {
        NamedNodeMap attributes = node.getAttributes();
        RoadTypeTag roadTypeTag = new RoadTypeTag();

        Node name = attributes.getNamedItem("NAME");
        if (name == null)
            throw new SAXException("ROADTYPE: missing attribute NAME");
        roadTypeTag.name = name.getNodeValue().trim();
        if (parser.roadTypeTags.keySet().contains(roadTypeTag.name))
            throw new SAXException("ROADTYPE: NAME " + roadTypeTag.name + " defined twice");

        Node width = attributes.getNamedItem("DEFAULTLANEWIDTH");
        if (width != null)
            roadTypeTag.defaultLaneWidth = LengthUnits.parseLengthRel(width.getNodeValue());

        Node lkp = attributes.getNamedItem("DEFAULTLANEKEEPING");
        if (lkp != null)
            roadTypeTag.defaultLaneKeepingPolicy = LaneAttributes.parseLaneKeepingPolicy(lkp.getNodeValue().trim());

        Node oc = attributes.getNamedItem("DEFAULTOVERTAKING");
        if (oc != null)
            roadTypeTag.defaultOvertakingConditions =
                    LaneAttributes.parseOvertakingConditions(oc.getNodeValue().trim(), parser);

        return roadTypeTag;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RoadTypeTag [name=" + this.name + ", default width=" + this.defaultLaneWidth + ", default laneKeepingPolicy="
                + this.defaultLaneKeepingPolicy + ", default overtakingConditions=" + this.defaultOvertakingConditions + "]";
    }
}
