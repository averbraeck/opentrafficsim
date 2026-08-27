package org.opentrafficsim.road.network.factory.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.djutils.exceptions.Throw;
import org.djutils.io.ResourceResolver;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.types.ExpressionType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Loads adapter definitions from bindings.xml. They can be obtained through {@link #getAdapter} for node values or attribute
 * values.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public final class AdapterRegistry
{

    /** Global bindings: xmlType to adapter class. */
    private static final Map<String, ExpressionAdapter<?, ?>> GLOBAL_ADAPTERS = new HashMap<>();

    /** Specific bindings: binding XPath to adapter class. */
    private static final Map<SpecificAdapterKey, ExpressionAdapter<?, ?>> SPECIFIC_ADAPTERS = new HashMap<>();

    static
    {
        // Loads the bindings
        final String resource = "/xsd/bindings.xml";
        try (InputStream stream = ResourceResolver.resolve(resource).openStream())
        {
            if (stream == null)
            {
                throw new IllegalStateException("Cannot find resource: " + resource);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(stream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            // Global bindings <xjc:javaType xmlType="ots:LengthType" adapter="org.opentrafficsim.xml.bindings.LengthAdapter"/>
            NodeList globalJavaTypes =
                    (NodeList) xpath.evaluate("//*[local-name()='globalBindings']//*[local-name()='javaType']", document,
                            XPathConstants.NODESET);
            for (int i = 0; i < globalJavaTypes.getLength(); i++)
            {
                Node javaType = globalJavaTypes.item(i);
                Node xmlTypeAttr = javaType.getAttributes().getNamedItem("xmlType");
                Node adapterAttr = javaType.getAttributes().getNamedItem("adapter");
                if (xmlTypeAttr == null || adapterAttr == null)
                {
                    continue;
                }

                String xmlType = xmlTypeAttr.getNodeValue();
                String adapterClassName = adapterAttr.getNodeValue();

                registerAdapter(xmlType, GLOBAL_ADAPTERS, adapterClassName);
            }

            // Specific bindings
            // <jaxb:bindings node="//xsd:complexType[@name='LmrsModel']/xsd:attribute[@name='GapAcceptance']">
            // ... <xjc:javaType adapter="...GapAcceptanceAdapter"/>
            NodeList bindings =
                    (NodeList) xpath.evaluate("//*[local-name()='bindings'][@node]", document, XPathConstants.NODESET);
            for (int i = 0; i < bindings.getLength(); i++)
            {
                Node binding = bindings.item(i);
                Node nodeAttr = binding.getAttributes().getNamedItem("node");
                if (nodeAttr == null) // defensive check, @node should already guarantee it
                {
                    continue;
                }
                Node javaType = (Node) xpath.evaluate(".//*[local-name()='javaType'][@adapter]", binding, XPathConstants.NODE);
                if (javaType == null)
                {
                    continue;
                }
                Node adapterAttr = javaType.getAttributes().getNamedItem("adapter");
                if (adapterAttr == null) // defensive check, @adapter should already guarantee it
                {
                    continue;
                }

                String bindingXPath = nodeAttr.getNodeValue().replaceAll("\\s+", "");
                SpecificAdapterKey key = SpecificAdapterKey.fromXPath(bindingXPath);
                String adapterClassName = adapterAttr.getNodeValue();

                registerAdapter(key, SPECIFIC_ADAPTERS, adapterClassName);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load bindings.xml", e);
        }
    }

    /**
     * Helper method to register an adapter with consistent type casting.
     * @param <T> value type
     * @param <E> expression type
     * @param <K> cache key type
     * @param key cache key
     * @param cache cache map
     * @param adapterClassName adapter of javaType in binding
     * @throws Exception adapterClassName is not a valid and accessible class with appropriate empty constructor
     */
    @SuppressWarnings("unchecked")
    private static <T, E extends ExpressionType<T>, K> void registerAdapter(final K key,
            final Map<K, ExpressionAdapter<?, ?>> cache, final String adapterClassName) throws Exception
    {
        Class<? extends ExpressionAdapter<T, E>> adapterClass =
                (Class<? extends ExpressionAdapter<T, E>>) Class.forName(adapterClassName);
        cache.put(key, adapterClass.getConstructor().newInstance());
    }

    /**
     * Returns adapter on element value.
     * @param elementName element name
     * @param elementNode element node representing an {@code <xsd:element>}
     * @param typeSupplier supplies type from name
     * @return adapter on element value
     */
    public static Optional<ExpressionAdapter<?, ?>> getElementAdapter(final String elementName, final Node elementNode,
            final Function<String, Node> typeSupplier)
    {
        return getAdapter(elementName, elementNode, null, elementNode, typeSupplier);
    }

    /**
     * Returns adapter on attribute value.
     * @param elementName element name
     * @param elementNode element node representing an {@code <xsd:attribute>}
     * @param attributeNode attribute name
     * @param typeSupplier supplies type from name
     * @return adapter on attribute value
     */
    public static Optional<ExpressionAdapter<?, ?>> getAttributeAdapter(final String elementName, final Node elementNode,
            final Node attributeNode, final Function<String, Node> typeSupplier)
    {
        return getAdapter(elementName, elementNode, getAttribute(attributeNode, "name"), attributeNode, typeSupplier);
    }

    /**
     * Returns adapter.
     * @param elementName element name
     * @param elementNode element node
     * @param attributeName attribute name
     * @param typeNode node that defines type, either same as elementNode, or the node of the attribute
     * @param typeSupplier supplies type from name
     * @return adapter
     */
    private static Optional<ExpressionAdapter<?, ?>> getAdapter(final String elementName, final Node elementNode,
            final String attributeName, final Node typeNode, final Function<String, Node> typeSupplier)
    {
        ExpressionAdapter<?, ?> specific = getSpecificAdapter(elementName, elementNode, attributeName);
        if (specific != null)
        {
            return Optional.of(specific);
        }
        String typeName = getAttribute(typeNode, "type");
        if (typeName == null)
        {
            return Optional.empty();
        }
        Set<String> visited = new LinkedHashSet<>();
        while (typeName != null && !visited.contains(typeName))
        {
            ExpressionAdapter<?, ?> adapter = GLOBAL_ADAPTERS.get(typeName);
            if (adapter != null)
            {
                visited.forEach((v) -> GLOBAL_ADAPTERS.putIfAbsent(v, adapter));
                return Optional.of(adapter);
            }
            // if not found, move up the type hierarchy, and eventually store a found adapter for all visited types
            visited.add(typeName);
            Node typeDefinition = typeSupplier.apply(typeName);
            if (typeDefinition == null)
            {
                break;
            }
            typeName = findBaseType(typeDefinition);
        }
        return Optional.empty();
    }

    /**
     * Finds the base type of the input node that defines a type.
     * @param typeNode type node
     * @return the name of the base type, or {@code null} if there is no base type
     */
    private static String findBaseType(final Node typeNode)
    {
        Node child = typeNode.getFirstChild();
        while (child != null)
        {
            String nodeName = child.getNodeName();
            if ("xsd:complexContent".equals(nodeName) || "xsd:simpleContent".equals(nodeName))
            {
                Node contentChild = child.getFirstChild();
                while (contentChild != null)
                {
                    String contentName = contentChild.getNodeName();
                    if ("xsd:extension".equals(contentName) || "xsd:restriction".equals(contentName))
                    {
                        return getAttribute(contentChild, "base");
                    }
                    contentChild = contentChild.getNextSibling();
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    /**
     * Get specific adapter that matches the element and attribute, or the type of the element node and the attribute. In both
     * cases the attribute may be {@code null}.
     * @param elementName element name
     * @param elementNode element node
     * @param attributeName attribute name, may be {@code null}
     * @return specific adapter that matches the element and attribute, or the type of the element node and the attribute
     */
    private static ExpressionAdapter<?, ?> getSpecificAdapter(final String elementName, final Node elementNode,
            final String attributeName)
    {
        String nameSpacedTypeName = getAttribute(elementNode, "type");
        String typeName = nameSpacedTypeName == null ? null
                : nameSpacedTypeName.startsWith("ots:") ? nameSpacedTypeName.substring(4) : nameSpacedTypeName;
        for (Entry<SpecificAdapterKey, ExpressionAdapter<?, ?>> entry : SPECIFIC_ADAPTERS.entrySet())
        {
            if (entry.getKey().matches(elementName, typeName, attributeName))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Returns {@code node.getAttributes().getNamedItem(name)}, or {@code null} if any is null.
     * @param node node
     * @param name name of attribute
     * @return {@code node.getAttributes().getNamedItem(name)}, or {@code null} if any is null
     */
    private static String getAttribute(final Node node, final String name)
    {
        if (!node.hasAttributes())
        {
            return null;
        }
        Node attrNode = node.getAttributes().getNamedItem(name);
        return attrNode == null ? null : attrNode.getNodeValue();
    }

    /**
     * Constructor.
     */
    private AdapterRegistry()
    {
        //
    }

    /**
     * Key for specific adapters.
     * @param element element name, should be {@code null} if {@code complexType} is given
     * @param type type, should be {@code null} if {@code element} is given
     * @param attribute attribute, use {@code null} to refer to the node value
     */
    private record SpecificAdapterKey(String element, String type, String attribute)
    {

        /** Pattern to find element in xpath. */
        private static final Pattern ELEMENT_PATTERN = Pattern.compile("xsd:element\\[@name='([^']+)'\\]");

        /** Pattern to find complexType in xpath. */
        private static final Pattern COMPLEXTYPE_PATTERN = Pattern.compile("xsd:complexType\\[@name='([^']+)'\\]");

        /** Pattern to find attribute in xpath. */
        private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("xsd:attribute\\[@name='([^']+)'\\]");

        /**
         * Constructs a key from xpath string.
         * @param xpath xpath string
         * @return key from xpath string
         */
        public static SpecificAdapterKey fromXPath(final String xpath)
        {
            String element = extract(ELEMENT_PATTERN, xpath);
            String complexType = extract(COMPLEXTYPE_PATTERN, xpath);
            Throw.when(element == null && complexType == null, IllegalArgumentException.class,
                    "XPath %s does not have a valid element or complexType.", xpath);
            Throw.when(element != null && complexType != null, IllegalArgumentException.class,
                    "XPath %s has a valid element and complexType, it should have only one.", xpath);
            String attribute = extract(ATTRIBUTE_PATTERN, xpath);
            return new SpecificAdapterKey(element, complexType, attribute);
        }

        /**
         * Extract pattern from xpath.
         * @param pattern pattern
         * @param xpath xpath string
         * @return string extracted from xpath with pattern
         */
        private static String extract(final Pattern pattern, final String xpath)
        {
            Matcher matcher = pattern.matcher(xpath);
            return matcher.find() ? matcher.group(1) : null;
        }

        /**
         * Returns whether this key matches the given input, by either matching the element name or type name, with optionally
         * an attribute in the element or type.
         * @param elementName element name
         * @param typeName type name
         * @param attributeName attribute, use {@code null} to refer to the node value
         * @return whether this key matches the given input
         */
        public boolean matches(final String elementName, final String typeName, final String attributeName)
        {
            return (this.element == null ? this.type.equals(typeName) : this.element.equals(elementName))
                    && Objects.equals(this.attribute, attributeName);
        }

    }

}
