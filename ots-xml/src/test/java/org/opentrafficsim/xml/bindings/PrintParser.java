package org.opentrafficsim.xml.bindings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.djutils.io.ResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Prints the structure of the document that is parsed.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class PrintParser
{

    /**
     * Constructor.
     */
    private PrintParser()
    {
        //
    }

    /**
     * Main method.
     * @param args args
     * @throws ParserConfigurationException exception
     * @throws SAXException exception
     * @throws IOException exception
     * @throws URISyntaxException exception
     */
    public static void main(final String[] args)
            throws ParserConfigurationException, SAXException, IOException, URISyntaxException
    {
        URL res = ResourceResolver.resolve("/resources/").asUrl();
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

    /**
     * PrintParser.
     */
    static class PrintHandler extends DefaultHandler
    {

        /** Depth. */
        private int depth = 0;

        /**
         * Constructor.
         */
        PrintHandler()
        {
            //
        }

        @Override
        public void startDocument() throws SAXException
        {
            System.out.println("Start Document");
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                throws SAXException
        {
            this.depth++;
            for (int i = 0; i < this.depth; i++)
            {
                System.out.print("  ");
            }
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
        public void endElement(final String uri, final String localName, final String qName) throws SAXException
        {
            this.depth--;
        }

    }
}
