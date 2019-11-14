package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.Distributions;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * GTU Tag.
 *
 * <pre>
 * {@code
  <xsd:element name="GTU">
    <xsd:complexType>
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
      <xsd:attribute name="GTUTYPE" type="xsd:string" use="required" />
      <xsd:attribute name="LENGTH" type="LENGTHDISTTYPE" use="required" />
      <xsd:attribute name="WIDTH" type="LENGTHDISTTYPE" use="required" />
      <xsd:attribute name="MAXSPEED" type="SPEEDDISTTYPE" use="required" />
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
class GTUTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** Type. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    GTUType gtuType = null;

    /** GTU length. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDist = null;

    /** GTU width. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDist = null;

    /** Maximum speed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maxSpeedDist = null;

    /**
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     * @throws GTUException if GTUType defined twice
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGTUs(final NodeList nodeList, final VissimNetworkLaneParser parser)
            throws SAXException, NetworkException, GTUException
    {
        for (Node node : XMLParser.getNodes(nodeList, "GTU"))
        {
            NamedNodeMap attributes = node.getAttributes();
            GTUTag gtuTag = new GTUTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
            {
                throw new SAXException("GTU: missing attribute NAME");
            }
            gtuTag.name = name.getNodeValue().trim();
            if (parser.getGtuTags().keySet().contains(gtuTag.name))
            {
                throw new SAXException("GTU: NAME " + gtuTag.name + " defined twice");
            }

            Node gtuType = attributes.getNamedItem("GTUTYPE");
            if (gtuType == null)
            {
                throw new SAXException("GTU: missing attribute GTUTYPE");
            }
            if (!parser.getGtuTypes().containsKey(gtuType.getNodeValue().trim()))
            {
                throw new SAXException("GTU: GTUTYPE " + gtuType.getNodeValue().trim() + " not defined");
            }
            gtuTag.gtuType = parser.getGtuTypes().get(gtuType.getNodeValue().trim());

            Node length = attributes.getNamedItem("LENGTH");
            if (length == null)
            {
                throw new SAXException("GTU: missing attribute LENGTH");
            }
            gtuTag.lengthDist = Distributions.parseLengthDist(length.getNodeValue());

            Node width = attributes.getNamedItem("WIDTH");
            if (width == null)
            {
                throw new SAXException("GTU: missing attribute WIDTH");
            }
            gtuTag.widthDist = Distributions.parseLengthDist(width.getNodeValue());

            Node maxSpeed = attributes.getNamedItem("MAXSPEED");
            if (maxSpeed == null)
            {
                throw new SAXException("GTU: missing attribute LENGTH");
            }
            gtuTag.maxSpeedDist = Distributions.parseSpeedDist(maxSpeed.getNodeValue());

            parser.getGtuTags().put(gtuTag.name, gtuTag);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUTag [name=" + this.name + ", gtuType=" + this.gtuType + ", lengthDist=" + this.lengthDist + ", widthDist="
                + this.widthDist + ", followingModel=" + ", maxSpeedDist=" + this.maxSpeedDist + "]";
    }

}
