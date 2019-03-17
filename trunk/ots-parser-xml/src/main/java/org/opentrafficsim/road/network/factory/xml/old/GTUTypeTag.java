package org.opentrafficsim.road.network.factory.xml.old;

import java.io.Serializable;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The XML tag for the GTUType.
 * 
 * <pre>
 * {@code
  <xsd:element name="GTUTYPE">
    <xsd:complexType>
      <xsd:attribute name="NAME" type="xsd:string" use="required" />
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
class GTUTypeTag implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160925L;

    /** Name. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    String name = null;

    /**
     * @param nodeList NodeList; nodeList the top-level nodes of the XML-file
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @throws SAXException when parsing of GTU tag fails
     * @throws NetworkException when parsing of GTU tag fails
     * @throws GTUException if GTUType defined twice
     */
    @SuppressWarnings("checkstyle:needbraces")
    static void parseGTUTypes(final NodeList nodeList, final XmlNetworkLaneParserOld parser)
            throws SAXException, NetworkException, GTUException
    {
        for (Node node : XMLParser.getNodes(nodeList, "GTUTYPE"))
        {
            NamedNodeMap attributes = node.getAttributes();
            GTUTypeTag gtuTypeTag = new GTUTypeTag();

            Node name = attributes.getNamedItem("NAME");
            if (name == null)
                throw new SAXException("GTUTYPE: missing attribute NAME");
            gtuTypeTag.name = name.getNodeValue().trim();
            if (parser.gtuTypes.keySet().contains(gtuTypeTag.name))
                throw new SAXException("GTUTYPE: NAME " + gtuTypeTag.name + " defined twice");
            // TODO super type (car, bus, truck) in xml definition
            GTUType gtuType = new GTUType(gtuTypeTag.name, parser.network.getGtuType(GTUType.DEFAULTS.VEHICLE));
            parser.gtuTypes.put(gtuTypeTag.name, gtuType);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GTUTypeTag [name=" + this.name + "]";
    }

}
