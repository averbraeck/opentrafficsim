package org.opentrafficsim.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility class to read XSD or XML from URI. There are also methods to obtain certain information from a node.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param file file.
     * @return document, i.e. the root of the XSD file.
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
     * Returns the attribute of a node. This is short for:
     *
     * <pre>
     * Optional.ofNullable(node.hasAttributes() &amp;&amp; node.getAttributes().getNamedItem(name) != null
     *         ? node.getAttributes().getNamedItem(name).getNodeValue() : null);
     * </pre>
     *
     * @param node node.
     * @param name attribute name.
     * @return value of the attribute in the node.
     */
    public static Optional<String> getAttribute(final Node node, final String name)
    {
        return Optional.ofNullable(node.hasAttributes() && node.getAttributes().getNamedItem(name) != null
                ? node.getAttributes().getNamedItem(name).getNodeValue() : null);
    }

    /**
     * Returns a child node of specified type. It should be a type of which there may be only one.
     * @param node node
     * @param type child type, e.g. xsd:complexType.
     * @return child node of specified type, empty if no such child.
     */
    public static Optional<Node> getChild(final Node node, final String type)
    {
        if (node.hasChildNodes())
        {
            for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
            {
                Node child = node.getChildNodes().item(childIndex);
                if (child.getNodeName().equals(type))
                {
                    return Optional.of(child);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns child nodes of specified type.
     * @param node node
     * @param type child type, e.g. xsd:field.
     * @return child nodes of specified type, empty {@code List} of no such child.
     */
    public static List<Node> getChildren(final Node node, final String type)
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

    /**
     * Remove HTML tags from string.
     * @param string input string
     * @return string with HTML tags removed, or {@code null} if the input is {@code null}
     */
    public static String filterHtml(final String string)
    {
        return string == null ? null : string.replaceAll("\\<[^>]*>", "");
    }

    /**
     * Types of annotation elements the {@code DocumentReader} can read. These are defined as below.
     *
     * <pre>
         * &lt;xsd:sequence&gt;
         *   &lt;xsd:annotation&gt;
         *     &lt;xsd:description&gt;describes the sequence&lt;/xsd:description&gt;
         *     &lt;xsd:appinfo&gt;
         *       &lt;ots:name&gt;annotates the sequence&lt;/ots:name&gt;
         *     &lt;/xsd:appinfo&gt;
         *   &lt;/xsd:annotation&gt;
         * &lt;/xsd:sequence&gt;
         * </pre>
     */
    public enum NodeAnnotation
    {
        /** Element xsd:documentation. */
        DESCRIPTION("xsd:documentation", null),

        /** Element xsd:appinfo/ots:name. */
        APPINFO_NAME("xsd:appinfo", "ots:name"),

        /** Element xsd:appinfo/ots:pattern. */
        APPINFO_PATTERN("xsd:appinfo", "ots:pattern"),

        /** Element xsd:appinfo/ots:defaultValue. */
        APPINFO_DEFAULT_VALUE("xsd:appinfo", "ots:defaultValue");

        /** Element name. */
        private final String annotationName;

        /** Tag. */
        private final String tag;

        /**
         * Constructor.
         * @param annotationName annotation name
         * @param tag tag name
         */
        NodeAnnotation(final String annotationName, final String tag)
        {
            this.annotationName = annotationName;
            this.tag = tag;
        }

        /**
         * Returns an annotation value. These are defined as below. All space-like characters are replaced by blanks, and
         * consecutive blanks are removed.
         *
         * <pre>
         * &lt;xsd:sequence&gt;
         *   &lt;xsd:annotation&gt;
         *     &lt;xsd:description&gt;describes the sequence&lt;/xsd:description&gt;
         *     &lt;xsd:appinfo&gt;
         *       &lt;ots:name&gt;annotates the sequence&lt;/ots:name&gt;
         *     &lt;/xsd:appinfo&gt;
         *   &lt;/xsd:annotation&gt;
         * &lt;/xsd:sequence&gt;
         * </pre>
         *
         * @param node node, either xsd:element or xsd:attribute.
         * @return annotation value, empty if not found.
         */
        public Optional<String> get(final Node node)
        {
            for (Node child : DocumentReader.getChildren(node, "xsd:annotation"))
            {
                for (Node annotation : DocumentReader.getChildren(child, this.annotationName))
                {
                    if (this.annotationName.equals(annotation.getNodeName()))
                    {
                        if (this.tag == null)
                        {
                            return getNodeValue(annotation);
                        }
                        for (Node tagChild : DocumentReader.getChildren(annotation, this.tag))
                        {
                            // As in Highlander, there can be only one.
                            return getNodeValue(tagChild);
                        }
                    }
                }
            }
            return Optional.empty();
        }

        /**
         * Returns the combined textual content of the node.
         * @param annotation node
         * @return combined textual content
         */
        private Optional<String> getNodeValue(final Node annotation)
        {
            StringBuilder str = new StringBuilder();
            for (int appIndex = 0; appIndex < annotation.getChildNodes().getLength(); appIndex++)
            {
                Node appInfo = annotation.getChildNodes().item(appIndex);
                if (appInfo.getNodeName().equals("#text") || appInfo.getNodeName().equals("#cdata-section"))
                {
                    str.append(appInfo.getNodeValue());
                }
            }
            // tabs, line break, etc. to blanks, then remove consecutive blanks, then trailing/leading blanks
            return Optional.of(str.toString().replaceAll("\\s", " ").replaceAll("\\s{2,}", " ").trim());
        }
    }

}
