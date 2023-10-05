package org.opentrafficsim.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility class to read XSD or XML from URI. There are also methods to obtain certain information from a node.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class DocumentReader
{

    /**
     * Private constructor.
     */
    private DocumentReader()
    {

    }

    /**
     * Opens an XSD or XML file.
     * @param file URI; file.
     * @return Document; document, i.e. the root of the XSD file.
     * @throws SAXException exception
     * @throws IOException exception
     * @throws ParserConfigurationException exception
     */
    public static Document open(final URI file) throws SAXException, IOException, ParserConfigurationException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // dbf.setXIncludeAware(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(file));
        return doc;
    }

    /**
     * Returns an annotation value. These are defined as below, for either xsd:appinfo or xsd:documentation. All space-like
     * characters are replaced by blanks, and consecutive blanks are removed.
     * 
     * <pre>
     * &lt;xsd:sequence&gt;
     *   &lt;xsd:annotation&gt;
     *     &lt;xsd:appinfo source="name"&gt;annotates the sequence&lt;/xsd:appinfo&gt;
     *   &lt;/xsd:annotation&gt;
     * &lt;/xsd:sequence&gt;
     * </pre>
     * 
     * @param node Node; node, either xsd:element or xsd:attribute.
     * @param element String; either "xsd:documentation" or "xsd:appinfo".
     * @param source String; name that the source attribute of the annotation should have.
     * @return String; annotation value, {@code null} if not found.
     */
    public static String getAnnotation(final Node node, final String element, final String source)
    {
        for (Node child : DocumentReader.getChildren(node, "xsd:annotation"))
        {
            for (Node annotation : DocumentReader.getChildren(child, element))
            {
                String appInfoSource = DocumentReader.getAttribute(annotation, "source");
                if (appInfoSource != null && appInfoSource.equals(source))
                {
                    StringBuilder str = new StringBuilder();
                    for (int appIndex = 0; appIndex < annotation.getChildNodes().getLength(); appIndex++)
                    {
                        Node appInfo = annotation.getChildNodes().item(appIndex);
                        if (appInfo.getNodeName().equals("#text"))
                        {
                            str.append(appInfo.getNodeValue());
                        }
                    }
                    // tabs, line break, etc. to blanks, then remove consecutive blanks, then trailing/leading blanks
                    return str.toString().replaceAll("\\s", " ").replaceAll("\\s{2,}", " ").trim();
                }
            }
        }
        return null;
    }

    /**
     * Returns the attribute of a node. This is short for:
     * 
     * <pre>
     * String value = node.hasAttributes() &amp;&amp; node.getAttributes().getNamedItem(name) != null
     *         ? node.getAttributes().getNamedItem(name).getNodeValue() : null;
     * </pre>
     * 
     * @param node Node; node.
     * @param name String; attribute name.
     * @return String; value of the attribute in the node.
     */
    public static String getAttribute(final Node node, final String name)
    {
        return node.hasAttributes() && node.getAttributes().getNamedItem(name) != null
                ? node.getAttributes().getNamedItem(name).getNodeValue() : null;
    }

    /**
     * Returns a child node of specified type. It should be a type of which there may be only one.
     * @param node Node node;
     * @param type String; child type, e.g. xsd:complexType.
     * @return Node; child node of specified type.
     */
    public static Node getChild(final Node node, final String type)
    {
        if (node.hasChildNodes())
        {
            for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
            {
                Node child = node.getChildNodes().item(childIndex);
                if (child.getNodeName().equals(type))
                {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Returns child nodes of specified type.
     * @param node Node node;
     * @param type String; child type, e.g. xsd:field.
     * @return ArayList&lt;Node&gt;; child nodes of specified type.
     */
    public static ArrayList<Node> getChildren(final Node node, final String type)
    {
        ArrayList<Node> children = new ArrayList<>();
        if (node.hasChildNodes())
        {
            for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
            {
                Node child = node.getChildNodes().item(childIndex);
                if (child.getNodeName().equals(type))
                {
                    children.add(child);
                }
            }
        }
        return children;
    }

}
