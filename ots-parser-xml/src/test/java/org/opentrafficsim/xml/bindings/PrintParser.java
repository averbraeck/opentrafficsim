package org.opentrafficsim.xml.bindings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.djutils.io.URLResource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Prints the structure of the document that is parsed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PrintParser
{

    /**
     * 
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, URISyntaxException
    {
        URL res = URLResource.getResource("/resources/");
        if (res == null)
        {
            System.out.println("Cannot find file");
            System.exit(-1);
        }
        String file = res.toURI().getPath() + "../../src/main/resources/example.xml";

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(file, new PrintHandler());
    }

    static class PrintHandler extends DefaultHandler
    {
        private int depth = 0;

        @Override
        public void startDocument() throws SAXException
        {
            System.out.println("Start Document");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            this.depth++;
            for (int i = 0; i < this.depth; i++)
                System.out.print("  ");
            System.out.print(qName);
            for (int i = 0; i < attributes.getLength(); i++)
            {
                System.out.print("  " + attributes.getQName(i) + "=" + attributes.getValue(i));
            }
            System.out.println();
        }

        @Override
        public void endDocument() throws SAXException
        {
            System.out.println("End Document");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            this.depth--;
        }

    }
}
