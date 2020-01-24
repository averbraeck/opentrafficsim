package org.opentrafficsim.road.network.factory.vissim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * GTUMIX Tag.
 *
 * <pre>
 * {@code
  <xsd:element name="GTUMIX">
    <xsd:complexType>
      <xsd:sequence>
        <xsd:element name="GTU" minOccurs="1" maxOccurs="unbounded">
          <xsd:complexType>
            <xsd:attribute name="NAME" type="xsd:string" use="required" />
            <xsd:attribute name="WEIGHT" type="xsd:double" use="required" />
          </xsd:complexType>
        </xsd:element>
      </xsd:sequence>
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
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
class GTUMixTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150723L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /** GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<GTUTag> gtus = new ArrayList<GTUTag>();

    /** Weights. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<Double> weights = new ArrayList<Double>();

    /**
     * Parse the GTUMIX tag.
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGTUMix(final NodeList nodeList, final VissimNetworkLaneParser parser) throws SAXException, NetworkException
    {
        for (Node node : XMLParser.getNodes(nodeList, "GTUMIX"))
        {
            NamedNodeMap attributes = node.getAttributes();
            GTUMixTag gtuMixTag = new GTUMixTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
            {
                throw new SAXException("GTUMIX: missing attribute NAME");
            }
            gtuMixTag.name = name.getNodeValue().trim();
            if (parser.getGtuMixTags().keySet().contains(gtuMixTag.name))
            {
                throw new SAXException("GTUMIX: NAME " + gtuMixTag.name + " defined twice");
            }

            List<Node> gtuList = XMLParser.getNodes(node.getChildNodes(), "GTU");
            if (gtuList.size() == 0)
            {
                throw new SAXException("GTUMIX: missing tag GTU");
            }
            for (Node gtuNode : gtuList)
            {
                parseGTUMixGTUTag(gtuNode, parser, gtuMixTag);
            }

            parser.getGtuMixTags().put(gtuMixTag.name, gtuMixTag);
        }
    }

    /**
     * Parse the GTUMIX's GTU tag.
     * @param gtuNode Node; the GTU node to parse
     * @param parser VissimNetworkLaneParser; the parser with the lists of information
     * @param gtuMixTag GTUMixTag; the parent tag
     * @throws SAXException when parsing of the tag fails
     * @throws NetworkException when parsing of the tag fails
     */
    @SuppressWarnings("checkstyle:needbraces")
    private static void parseGTUMixGTUTag(final Node gtuNode, final VissimNetworkLaneParser parser, final GTUMixTag gtuMixTag)
            throws NetworkException, SAXException
    {
        NamedNodeMap attributes = gtuNode.getAttributes();

        Node gtuName = attributes.getNamedItem("NAME");
        if (gtuName == null)
        {
            throw new NetworkException("GTUMIX: No GTU NAME defined");
        }
        if (!parser.getGtuTags().containsKey(gtuName.getNodeValue().trim()))
        {
            throw new NetworkException("GTUMIX: " + gtuMixTag.name + " GTU " + gtuName.getNodeValue().trim() + " not defined");
        }
        gtuMixTag.gtus.add(parser.getGtuTags().get(gtuName.getNodeValue().trim()));

        Node weight = attributes.getNamedItem("WEIGHT");
        if (weight == null)
        {
            throw new NetworkException(
                    "GTUMIX: " + gtuMixTag.name + " GTU " + gtuName.getNodeValue().trim() + ": weight not defined");
        }
        gtuMixTag.weights.add(Double.parseDouble(weight.getNodeValue()));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUMixTag [name=" + this.name + ", gtus=" + this.gtus + ", weights=" + this.weights + "]";
    }

}
