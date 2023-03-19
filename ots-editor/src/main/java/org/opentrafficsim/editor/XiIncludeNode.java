package org.opentrafficsim.editor;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * Singleton at {@code XiIncludeNode.XI_INCLUDE} to use for xi:include nodes in an {@code XsdTreeNode}. Most methods return
 * {@code null} or do nothing. The attributes contain one attribute named 'File'.
 * @author wjschakel
 */
public final class XiIncludeNode implements Node
{

    /** Singleton instance for xi:include nodes. */
    public static final Node XI_INCLUDE = new XiIncludeNode();

    /** Child node that represents the 'File' attribute. */
    private static final Node FILE_CHILD = new FileNode();
    
    /** Attributes to contain 'File'. */
    private static final Attributes ATTRIBUTES = new Attributes();
    
    /**
     * Private constructor.
     */
    private XiIncludeNode()
    {

    }

    /** {@inheritDoc} */
    @Override
    public String getNodeName()
    {
        return "xi:include";
    }

    /** {@inheritDoc} */
    @Override
    public String getNodeValue() throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setNodeValue(final String nodeValue) throws DOMException
    {
    }

    /** {@inheritDoc} */
    @Override
    public short getNodeType()
    {
        return Node.TEXT_NODE;
    }

    /** {@inheritDoc} */
    @Override
    public Node getParentNode()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public NodeList getChildNodes()
    {
        return new NodeList()
        {
            /** {@inheritDoc} */
            @Override
            public Node item(final int index)
            {
                if (index == 0)
                {
                    return FILE_CHILD;
                }
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public int getLength()
            {
                return 1;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Node getFirstChild()
    {
        return FILE_CHILD;
    }

    /** {@inheritDoc} */
    @Override
    public Node getLastChild()
    {
        return FILE_CHILD;
    }

    /** {@inheritDoc} */
    @Override
    public Node getPreviousSibling()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node getNextSibling()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public NamedNodeMap getAttributes()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Document getOwnerDocument()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node removeChild(final Node oldChild) throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Node appendChild(final Node newChild) throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildNodes()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Node cloneNode(final boolean deep)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void normalize()
    {
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSupported(final String feature, final String version)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getNamespaceURI()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getPrefix()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setPrefix(final String prefix) throws DOMException
    {
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalName()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasAttributes()
    {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getBaseURI()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public short compareDocumentPosition(final Node other) throws DOMException
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getTextContent() throws DOMException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setTextContent(final String textContent) throws DOMException
    {
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSameNode(final Node other)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String lookupPrefix(final String namespaceURI)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefaultNamespace(final String namespaceURI)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String lookupNamespaceURI(final String prefix)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEqualNode(final Node arg)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Object getFeature(final String feature, final String version)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Object setUserData(final String key, final Object data, final UserDataHandler handler)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Object getUserData(final String key)
    {
        return null;
    }

    /**
     * Implementation of {@code Node} to provide the 'File' attribute child node.
     * @author wjschakel
     */
    private static class FileNode implements Node
    {

        /** {@inheritDoc} */
        @Override
        public String getNodeName()
        {
            return "xsd:attribute";
        }

        /** {@inheritDoc} */
        @Override
        public String getNodeValue() throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void setNodeValue(final String nodeValue) throws DOMException
        {
        }

        /** {@inheritDoc} */
        @Override
        public short getNodeType()
        {
            return Node.ATTRIBUTE_NODE;
        }

        /** {@inheritDoc} */
        @Override
        public Node getParentNode()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public NodeList getChildNodes()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node getFirstChild()
        {
            return null;
        }
        
        /** {@inheritDoc} */
        @Override
        public Node getLastChild()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node getPreviousSibling()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node getNextSibling()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public NamedNodeMap getAttributes()
        {
            return ATTRIBUTES;
        }

        /** {@inheritDoc} */
        @Override
        public Document getOwnerDocument()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node removeChild(final Node oldChild) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node appendChild(final Node newChild) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasChildNodes()
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Node cloneNode(final boolean deep)
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void normalize()
        {
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSupported(final String feature, final String version)
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public String getNamespaceURI()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public String getPrefix()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void setPrefix(final String prefix) throws DOMException
        {
        }

        /** {@inheritDoc} */
        @Override
        public String getLocalName()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasAttributes()
        {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public String getBaseURI()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public short compareDocumentPosition(final Node other) throws DOMException
        {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public String getTextContent() throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void setTextContent(final String textContent) throws DOMException
        {
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSameNode(final Node other)
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public String lookupPrefix(final String namespaceURI)
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isDefaultNamespace(final String namespaceURI)
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public String lookupNamespaceURI(final String prefix)
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEqualNode(final Node arg)
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Object getFeature(final String feature, final String version)
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Object setUserData(final String key, final Object data, final UserDataHandler handler)
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Object getUserData(final String key)
        {
            return null;
        }
        
    }
    
    /**
     * Implementation of {@code NamedNodeMap} to provide the 'File' attribute.
     * @author wjschakel
     */
    private static class Attributes implements NamedNodeMap
    {

        /** File node. */
        private Node file = new Node()
        {

            /** {@inheritDoc} */
            @Override
            public String getNodeName()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public String getNodeValue() throws DOMException
            {
                return "File";
            }

            /** {@inheritDoc} */
            @Override
            public void setNodeValue(final String nodeValue) throws DOMException
            {
            }

            /** {@inheritDoc} */
            @Override
            public short getNodeType()
            {
                return 0;
            }

            /** {@inheritDoc} */
            @Override
            public Node getParentNode()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public NodeList getChildNodes()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node getFirstChild()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node getLastChild()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node getPreviousSibling()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node getNextSibling()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public NamedNodeMap getAttributes()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Document getOwnerDocument()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node insertBefore(final Node newChild, final Node refChild) throws DOMException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node replaceChild(final Node newChild, final Node oldChild) throws DOMException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node removeChild(final Node oldChild) throws DOMException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Node appendChild(final Node newChild) throws DOMException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public boolean hasChildNodes()
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public Node cloneNode(final boolean deep)
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public void normalize()
            {
            }

            /** {@inheritDoc} */
            @Override
            public boolean isSupported(final String feature, final String version)
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public String getNamespaceURI()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public String getPrefix()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public void setPrefix(final String prefix) throws DOMException
            {
            }

            /** {@inheritDoc} */
            @Override
            public String getLocalName()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public boolean hasAttributes()
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public String getBaseURI()
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public short compareDocumentPosition(final Node other) throws DOMException
            {
                return 0;
            }

            /** {@inheritDoc} */
            @Override
            public String getTextContent() throws DOMException
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public void setTextContent(final String textContent) throws DOMException
            {
            }

            /** {@inheritDoc} */
            @Override
            public boolean isSameNode(final Node other)
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public String lookupPrefix(final String namespaceURI)
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isDefaultNamespace(final String namespaceURI)
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public String lookupNamespaceURI(final String prefix)
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isEqualNode(final Node arg)
            {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public Object getFeature(final String feature, final String version)
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Object setUserData(final String key, final Object data, final UserDataHandler handler)
            {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public Object getUserData(final String key)
            {
                return null;
            }

        };

        /** {@inheritDoc} */
        @Override
        public Node getNamedItem(final String name)
        {
            if (name.equals("name"))
            {
                return this.file;
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node setNamedItem(final Node arg) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node removeNamedItem(final String name) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node item(final int index)
        {
            if (index == 0)
            {
                return this.file;
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public int getLength()
        {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public Node getNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node setNamedItemNS(final Node arg) throws DOMException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Node removeNamedItemNS(final String namespaceURI, final String localName) throws DOMException
        {
            return null;
        }

    }

}
