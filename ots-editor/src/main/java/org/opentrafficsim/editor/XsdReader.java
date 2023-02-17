package org.opentrafficsim.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Reads XSD from URI. May be moved to other class. 
 * @author wjschakel
 */
public class XsdReader
{

    static Document open(final URI file) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setXIncludeAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder(); 
        Document doc = db.parse(new File(file));
        return doc;
    }
    
}
