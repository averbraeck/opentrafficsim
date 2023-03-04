package org.opentrafficsim.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.network.OtsNetwork;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * ParseXml.java.
 * <p>
 * Copyright (c) 2022-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ParseXml
{
    /**
     * Make a DOM tree and do some xpath.
     * @throws Exception
     */
    public ParseXml() throws Exception
    {
        domTree(URLResource.getResource("/resources/conflict/Simple.xml").getPath());
    }

    private void domTree(final String path) throws Exception
    {
        File file = new File(path);
        FileInputStream fileIS = new FileInputStream(file);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setXIncludeAware(true);
        builderFactory.setValidating(false); // no DTD validation
        // builderFactory.setFeature("http://apache.org/xml/features/validation/schema", true);
        // builderFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        // builderFactory.setFeature("http://apache.org/xml/features/validation/id-idref-checking", true);
        // builderFactory.setFeature("http://apache.org/xml/features/validation/identity-constraint-checking", true);
        // builderFactory.setFeature("http://apache.org/xml/features/xinclude", true);
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        builder.setEntityResolver(new DefaultsResolver());
        Document xmlDocument = builder.parse(fileIS);
        print(xmlDocument, "*");
        print(xmlDocument, "*/ots:OTS");
        print(xmlDocument, ".//ots:GTUTYPE");
    }

    private void print(final Document xmlDocument, final String expression) throws Exception
    {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        System.out.print(expression + " = [ ");
        boolean first = true;
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                System.out.print(", ");
            }
            Node n = nodeList.item(i);
            System.out.print("{" + n.getNodeName());
            NamedNodeMap attMap = n.getAttributes();
            if (attMap != null)
            {
                boolean f2 = true;
                for (int j = 0; j < attMap.getLength(); j++)
                {
                    if (f2)
                    {
                        f2 = false;
                    }
                    else
                    {
                        System.out.print(":");
                    }
                    System.out.print(" " + attMap.item(j).getNodeName() + "=" + attMap.item(j).getNodeValue());
                }
            }
            System.out.print("}");
        }
        System.out.println(" ]");
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        new ParseXml();
    }

    static class DefaultsResolver implements EntityResolver
    {
        /** {@inheritDoc} */
        @Override
        public InputSource resolveEntity(final String publicId, final String systemId)
        {
            if (systemId.contains("defaults/"))
            {
                System.out.println("\nINCLUDING " + systemId);
                String location = "/resources/xsd/defaults" + systemId.substring(systemId.lastIndexOf('/'));
                InputStream stream = URLResource.getResourceAsStream(location);
                return new InputSource(stream);
            }
            else
            {
                return new InputSource(URLResource.getResourceAsStream(systemId));
            }
        }
    }

}
