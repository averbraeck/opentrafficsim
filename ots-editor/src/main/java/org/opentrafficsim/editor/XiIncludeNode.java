package org.opentrafficsim.editor;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * Singleton at {@code XiIncludeNode.XI_INCLUDE} to use for xi:include nodes in an {@code XsdTreeNode}. Most methods return
 * {@code null} or do nothing. The attributes define one attribute named 'File' and one named 'Fallback'.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class XiIncludeNode implements Node
{

    /** Singleton instance for xi:include nodes. */
    public static final Node XI_INCLUDE = new XiIncludeNode();

    /** Child node that represents the 'File' attribute. */
    private static final Node FILE_CHILD = new FileNode(new Attributes("File"));

    /** Child node that represents the 'Fallback' attribute. */
    private static final Node FALLBACK_CHILD = new FileNode(new Attributes("Fallback"));

    /**
     * Private constructor.
     */
    private XiIncludeNode()
    {

    }

    @Override
    public String getNodeName()
    {
        return "xi:include";
    }

    @Override
    public String getNodeValue() throws DOMException
    {
        return null;
    }

    @Override
    public void setNodeValue(final String nodeValue) throws DOMException
    {
    }

    @Override
    public short getNodeType()
    {
        return Node.TEXT_NODE;
    }

    @Override
    public Node getParentNode()
    {
        return null;
    }

    @Override
    public NodeList getChildNodes()
    {
        return new NodeList()
        {
            @Override
            public Node item(final int index)
            {
                if (index == 0)
                {
                    return FILE_CHILD;
                }
                if (index == 1)
                {
                    return FALLBACK_CHILD;
                }
                return null;
            }

            @Override
            public int getLength()
            {
                return 2;
            }
        };
    }

    @Override
    public Node getFirstChild()
    {
        return FILE_CHILD;
    }

    @Override
    public Node getLastChild()
    {
        return FALLBACK_CHILD;
    }

    @Override
    public Node getPreviousSibling()
    {
        return null;
    }

    @Override
    public Node getNextSibling()
    {
        return null;
    }

    @Override
    public NamedNodeMap getAttributes()
    {
        return null;
    }

    @Override
    public Document getOwnerDocument()
    {
        return null;
    }

    @Override
    public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
    {
        return null;
    }

    @Override
    public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
    {
        return null;
    }

    @Override
    public Node removeChild(final Node oldChild) throws DOMException
    {
        return null;
    }

    @Override
    public Node appendChild(final Node newChild) throws DOMException
    {
        return null;
    }

    @Override
    public boolean hasChildNodes()
    {
        return true;
    }

    @Override
    public Node cloneNode(final boolean deep)
    {
        return null;
    }

    @Override
    public void normalize()
    {
    }

    @Override
    public boolean isSupported(final String feature, final String version)
    {
        return false;
    }

    @Override
    public String getNamespaceURI()
    {
        return null;
    }

    @Override
    public String getPrefix()
    {
        return null;
    }

    @Override
    public void setPrefix(final String prefix) throws DOMException
    {
    }

    @Override
    public String getLocalName()
    {
        return null;
    }

    @Override
    public boolean hasAttributes()
    {
        return false;
    }

    @Override
    public String getBaseURI()
    {
        return null;
    }

    @Override
    public short compareDocumentPosition(final Node other) throws DOMException
    {
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException
    {
        return null;
    }

    @Override
    public void setTextContent(final String textContent) throws DOMException
    {
    }

    @Override
    public boolean isSameNode(final Node other)
    {
        return false;
    }

    @Override
    public String lookupPrefix(final String namespaceURI)
    {
        return null;
    }

    @Override
    public boolean isDefaultNamespace(final String namespaceURI)
    {
        return false;
    }

    @Override
    public String lookupNamespaceURI(final String prefix)
    {
        return null;
    }

    @Override
    public boolean isEqualNode(final Node arg)
    {
        return false;
    }

    @Override
    public Object getFeature(final String feature, final String version)
    {
        return null;
    }

    @Override
    public Object setUserData(final String key, final Object data, final UserDataHandler handler)
    {
        return null;
    }

    @Override
    public Object getUserData(final String key)
    {
        return null;
    }

    /**
     * Implementation of {@code Node} to provide the 'File' attribute child node.
     */
    private static class FileNode implements Node
    {

        /** Attributes. */
        private final Attributes attributes;

        /**
         * Constructor.
         * @param attributes attributes.
         */
        FileNode(final Attributes attributes)
        {
            this.attributes = attributes;
        }

        @Override
        public String getNodeName()
        {
            return "xsd:attribute";
        }

        @Override
        public String getNodeValue() throws DOMException
        {
            return null;
        }

        @Override
        public void setNodeValue(final String nodeValue) throws DOMException
        {
        }

        @Override
        public short getNodeType()
        {
            return Node.ATTRIBUTE_NODE;
        }

        @Override
        public Node getParentNode()
        {
            return null;
        }

        @Override
        public NodeList getChildNodes()
        {
            return null;
        }

        @Override
        public Node getFirstChild()
        {
            return null;
        }

        @Override
        public Node getLastChild()
        {
            return null;
        }

        @Override
        public Node getPreviousSibling()
        {
            return null;
        }

        @Override
        public Node getNextSibling()
        {
            return null;
        }

        @Override
        public NamedNodeMap getAttributes()
        {
            return this.attributes;
        }

        @Override
        public Document getOwnerDocument()
        {
            return null;
        }

        @Override
        public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
        {
            return null;
        }

        @Override
        public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
        {
            return null;
        }

        @Override
        public Node removeChild(final Node oldChild) throws DOMException
        {
            return null;
        }

        @Override
        public Node appendChild(final Node newChild) throws DOMException
        {
            return null;
        }

        @Override
        public boolean hasChildNodes()
        {
            return false;
        }

        @Override
        public Node cloneNode(final boolean deep)
        {
            return null;
        }

        @Override
        public void normalize()
        {
        }

        @Override
        public boolean isSupported(final String feature, final String version)
        {
            return false;
        }

        @Override
        public String getNamespaceURI()
        {
            return null;
        }

        @Override
        public String getPrefix()
        {
            return null;
        }

        @Override
        public void setPrefix(final String prefix) throws DOMException
        {
        }

        @Override
        public String getLocalName()
        {
            return null;
        }

        @Override
        public boolean hasAttributes()
        {
            return true;
        }

        @Override
        public String getBaseURI()
        {
            return null;
        }

        @Override
        public short compareDocumentPosition(final Node other) throws DOMException
        {
            return 0;
        }

        @Override
        public String getTextContent() throws DOMException
        {
            return null;
        }

        @Override
        public void setTextContent(final String textContent) throws DOMException
        {
        }

        @Override
        public boolean isSameNode(final Node other)
        {
            return false;
        }

        @Override
        public String lookupPrefix(final String namespaceURI)
        {
            return null;
        }

        @Override
        public boolean isDefaultNamespace(final String namespaceURI)
        {
            return false;
        }

        @Override
        public String lookupNamespaceURI(final String prefix)
        {
            return null;
        }

        @Override
        public boolean isEqualNode(final Node arg)
        {
            return false;
        }

        @Override
        public Object getFeature(final String feature, final String version)
        {
            return null;
        }

        @Override
        public Object setUserData(final String key, final Object data, final UserDataHandler handler)
        {
            return null;
        }

        @Override
        public Object getUserData(final String key)
        {
            return null;
        }

    }

    /**
     * Implementation of {@code NamedNodeMap} to provide the 'File' attribute.
     */
    private static class Attributes implements NamedNodeMap
    {

        /** Attribute name in GUI. */
        private final String name;

        /**
         * Constructor.
         * @param name attribute name in GUI.
         */
        Attributes(final String name)
        {
            this.name = name;
        }

        /** File node. */
        private Node file = new Node()
        {

            @Override
            public String getNodeName()
            {
                return null;
            }

            @Override
            public String getNodeValue() throws DOMException
            {
                return Attributes.this.name;
            }

            @Override
            public void setNodeValue(final String nodeValue) throws DOMException
            {
            }

            @Override
            public short getNodeType()
            {
                return 0;
            }

            @Override
            public Node getParentNode()
            {
                return null;
            }

            @Override
            public NodeList getChildNodes()
            {
                return null;
            }

            @Override
            public Node getFirstChild()
            {
                return null;
            }

            @Override
            public Node getLastChild()
            {
                return null;
            }

            @Override
            public Node getPreviousSibling()
            {
                return null;
            }

            @Override
            public Node getNextSibling()
            {
                return null;
            }

            @Override
            public NamedNodeMap getAttributes()
            {
                return null;
            }

            @Override
            public Document getOwnerDocument()
            {
                return null;
            }

            @Override
            public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
            {
                return null;
            }

            @Override
            public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
            {
                return null;
            }

            @Override
            public Node removeChild(final Node oldChild) throws DOMException
            {
                return null;
            }

            @Override
            public Node appendChild(final Node newChild) throws DOMException
            {
                return null;
            }

            @Override
            public boolean hasChildNodes()
            {
                return false;
            }

            @Override
            public Node cloneNode(final boolean deep)
            {
                return null;
            }

            @Override
            public void normalize()
            {
            }

            @Override
            public boolean isSupported(final String feature, final String version)
            {
                return false;
            }

            @Override
            public String getNamespaceURI()
            {
                return null;
            }

            @Override
            public String getPrefix()
            {
                return null;
            }

            @Override
            public void setPrefix(final String prefix) throws DOMException
            {
            }

            @Override
            public String getLocalName()
            {
                return null;
            }

            @Override
            public boolean hasAttributes()
            {
                return false;
            }

            @Override
            public String getBaseURI()
            {
                return null;
            }

            @Override
            public short compareDocumentPosition(final Node other) throws DOMException
            {
                return 0;
            }

            @Override
            public String getTextContent() throws DOMException
            {
                return null;
            }

            @Override
            public void setTextContent(final String textContent) throws DOMException
            {
            }

            @Override
            public boolean isSameNode(final Node other)
            {
                return false;
            }

            @Override
            public String lookupPrefix(final String namespaceURI)
            {
                return null;
            }

            @Override
            public boolean isDefaultNamespace(final String namespaceURI)
            {
                return false;
            }

            @Override
            public String lookupNamespaceURI(final String prefix)
            {
                return null;
            }

            @Override
            public boolean isEqualNode(final Node arg)
            {
                return false;
            }

            @Override
            public Object getFeature(final String feature, final String version)
            {
                return null;
            }

            @Override
            public Object setUserData(final String key, final Object data, final UserDataHandler handler)
            {
                return null;
            }

            @Override
            public Object getUserData(final String key)
            {
                return null;
            }

        };

        @Override
        @SuppressWarnings("hiddenfield")
        public Node getNamedItem(final String name)
        {
            if (name.equals("name"))
            {
                return this.file;
            }
            return null;
        }

        @Override
        public Node setNamedItem(final Node arg) throws DOMException
        {
            return null;
        }

        @Override
        @SuppressWarnings("hiddenfield")
        public Node removeNamedItem(final String name) throws DOMException
        {
            return null;
        }

        @Override
        public Node item(final int index)
        {
            if (index == 0)
            {
                return this.file;
            }
            return null;
        }

        @Override
        public int getLength()
        {
            return 1;
        }

        @Override
        public Node getNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            return null;
        }

        @Override
        public Node setNamedItemNS(final Node arg) throws DOMException
        {
            return null;
        }

        @Override
        public Node removeNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            return null;
        }

    }

}
