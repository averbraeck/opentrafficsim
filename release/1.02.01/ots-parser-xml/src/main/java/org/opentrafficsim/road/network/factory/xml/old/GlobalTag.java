package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.AccelerationUnits;
import org.opentrafficsim.core.network.factory.xml.units.LengthUnits;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * GlobalTag parser.
 * 
 * <pre>
 * {@code
  <xsd:element name="GLOBAL">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="SPEEDGTUCOLORER" minOccurs="0" maxOccurs="1">
          <xsd:complexType>
            <xsd:attribute name="MAXSPEED" type="SPEEDTYPE" use="required" />
          </xsd:complexType>
        </xsd:element>
        <xsd:element name="ACCELERATIONGTUCOLORER" minOccurs="0" maxOccurs="1">
          <xsd:complexType>
            <xsd:attribute name="MAXDECELERATION" type="ACCELERATIONTYPE" use="required" />
            <xsd:attribute name="MAXACCELERATION" type="ACCELERATIONTYPE" use="required" />
          </xsd:complexType>
        </xsd:element>
        <xsd:element name="LANECHANGEURGEGTUCOLORER" minOccurs="0" maxOccurs="1">
          <xsd:complexType>
            <xsd:attribute name="MINLANECHANGEDISTANCE" type="LENGTHTYPE" use="required" />
            <xsd:attribute name="HORIZON" type="LENGTHTYPE" use="required" />
          </xsd:complexType>
        </xsd:element>
      </xsd:sequence>
      <xsd:attribute ref="xml:base" />
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
class GlobalTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Default SpeedGTUColorer.maxSpeed. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Speed speedGTUColorerMaxSpeed = null;

    /** Default AccelerationGTUColorer.maxDeceleration. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Acceleration accelerationGTUColorerMaxDeceleration = null;

    /** Default AccelerationGTUColorer.maxAcceleration. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Acceleration accelerationGTUColorerMaxAcceleration = null;

    /** Default LaneChangeUrgeGTUColorer.minLaneChangeDistance. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length laneChangeUrgeGTUColorerMinLaneChangeDistance = null;

    /** Default LaneChangeUrgeGTUColorer.horizon. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Length laneChangeUrgeGTUColorerHorizon = null;

    /**
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @throws NetworkException when parsing of units fails
     * @throws SAXException when parsing of GLOBAL tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGlobal(final NodeList nodeList, final XmlNetworkLaneParserOld parser) throws NetworkException, SAXException
    {
        List<Node> nodes = XMLParser.getNodes(nodeList, "GLOBAL");
        if (nodes.size() > 1)
            throw new SAXException("GLOBAL: More than one tag GLOBAL in the XML-file");
        if (nodes.size() == 1)
        {
            Node node = nodes.get(0);

            parser.globalTag = new GlobalTag();

            // SPEEDGTUCOLORER attributes
            List<Node> speedGTUColorerNodes = XMLParser.getNodes(node.getChildNodes(), "SPEEDGTUCOLORER");
            if (speedGTUColorerNodes.size() > 1)
                throw new SAXException("GLOBAL: More than one tag SPEEDGTUCOLORER in the XML-file");
            if (speedGTUColorerNodes.size() == 1)
            {
                Node speedGTUColorerNode = nodes.get(0);
                NamedNodeMap speedGTUColorerAttributes = speedGTUColorerNode.getAttributes();
                if (speedGTUColorerAttributes.getNamedItem("MAXSPEED") == null)
                    throw new SAXException("GLOBAL: No attribute MAXSPEED for the tag SPEEDGTUCOLORER");
                parser.globalTag.speedGTUColorerMaxSpeed =
                        SpeedUnits.parseSpeed(speedGTUColorerAttributes.getNamedItem("MAXSPEED").getNodeValue());
            }

            // ACCELERATIONGTUCOLORER attributes
            List<Node> accelerationGTUColorerNodes = XMLParser.getNodes(node.getChildNodes(), "ACCELERATIONGTUCOLORER");
            if (accelerationGTUColorerNodes.size() > 1)
                throw new SAXException("GLOBAL: More than one tag ACCELERATIONGTUCOLORER in the XML-file");
            if (accelerationGTUColorerNodes.size() == 1)
            {
                Node accelerationGTUColorerNode = nodes.get(0);
                NamedNodeMap accelerationGTUColorerAttributes = accelerationGTUColorerNode.getAttributes();
                if (accelerationGTUColorerAttributes.getNamedItem("MAXDECELERATION") == null)
                    throw new SAXException("GLOBAL: No attribute MAXDECELERATION for the tag ACCELERATIONGTUCOLORER");
                parser.globalTag.accelerationGTUColorerMaxDeceleration = AccelerationUnits
                        .parseAcceleration(accelerationGTUColorerAttributes.getNamedItem("MAXDECELERATION").getNodeValue());
                if (accelerationGTUColorerAttributes.getNamedItem("MAXACCELERATION") == null)
                    throw new SAXException("GLOBAL: No attribute MAXACCELERATION for the tag ACCELERATIONGTUCOLORER");
                parser.globalTag.accelerationGTUColorerMaxAcceleration = AccelerationUnits
                        .parseAcceleration(accelerationGTUColorerAttributes.getNamedItem("MAXACCELERATION").getNodeValue());
            }

            // LANECHANGEURGEGTUCOLORER attributes
            List<Node> lcuGTUColorerNodes = XMLParser.getNodes(node.getChildNodes(), "LANECHANGEURGEGTUCOLORER");
            if (lcuGTUColorerNodes.size() > 1)
                throw new SAXException("GLOBAL: More than one tag LANECHANGEURGEGTUCOLORER in the XML-file");
            if (lcuGTUColorerNodes.size() == 1)
            {
                Node lcuGTUColorerNode = nodes.get(0);
                NamedNodeMap lcuGTUColorerAttributes = lcuGTUColorerNode.getAttributes();
                if (lcuGTUColorerAttributes.getNamedItem("MINLANECHANGEDISTANCE") == null)
                    throw new SAXException("GLOBAL: No attribute MINLANECHANGEDISTANCE for the tag LANECHANGEURGEGTUCOLORER");
                parser.globalTag.laneChangeUrgeGTUColorerMinLaneChangeDistance =
                        LengthUnits.parseLength(lcuGTUColorerAttributes.getNamedItem("MINLANECHANGEDISTANCE").getNodeValue());
                if (lcuGTUColorerAttributes.getNamedItem("HORIZON") == null)
                    throw new SAXException("GLOBAL: No attribute HORIZON for the tag LANECHANGEURGEGTUCOLORER");
                parser.globalTag.laneChangeUrgeGTUColorerHorizon =
                        LengthUnits.parseLength(lcuGTUColorerAttributes.getNamedItem("HORIZON").getNodeValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GlobalTag [speedGTUColorerMaxSpeed=" + this.speedGTUColorerMaxSpeed + ", accelerationGTUColorerMaxDeceleration="
                + this.accelerationGTUColorerMaxDeceleration + ", accelerationGTUColorerMaxAcceleration="
                + this.accelerationGTUColorerMaxAcceleration + ", laneChangeUrgeGTUColorerMinLaneChangeDistance="
                + this.laneChangeUrgeGTUColorerMinLaneChangeDistance + ", laneChangeUrgeGTUColorerHorizon="
                + this.laneChangeUrgeGTUColorerHorizon + "]";
    }

}
