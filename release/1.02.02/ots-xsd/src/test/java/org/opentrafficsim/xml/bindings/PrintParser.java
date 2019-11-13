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
 * Prints the structure of the document that is parsed. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class PrintParser
{

    /**
     * 
     */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, URISyntaxException
    {
        URL res = URLResource.getResource("/");
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

        /** {@inheritDoc} */
        @Override
        public void startDocument() throws SAXException
        {
            System.out.println("Start Document");
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
        @Override
        public void endDocument() throws SAXException
        {
            System.out.println("End Document");
        }

        /** {@inheritDoc} */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            this.depth--;
        }

    }
}
