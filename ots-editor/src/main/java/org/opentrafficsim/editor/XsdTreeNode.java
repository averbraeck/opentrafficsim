package org.opentrafficsim.editor;

import java.util.ArrayList;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.w3c.dom.Node;

/**
 * Underlying data structure object of the editor. Starting with the root node "OTS", all the information is stored in a tree.
 * The tree follows the XSD structure, e.g. "OTS.NETWORK.LINK". XsdTreeNode's may have a {@code Node} object from the XSD DOM
 * tree. From this information it can be derived what the child nodes should be. Some nodes are utility nodes are not part of
 * the XML structure. Typically these are nodes with which nodes can be added to the XML structure. The {@code Node} is then
 * used to know what nodes to create.
 * @author wjschakel
 */
public class XsdTreeNode
{

    private XsdTreeNode parent;

    private final Node xsdNode;

    private Node complexType;

    private Boolean isComplex;

    private List<XsdTreeNode> children;

    private List<Node> attributeNodes;

    private List<String> attributeValues;

    private String id;

    private Boolean isIdentifiable;

    private String value;

    private Boolean isEditable;

    public XsdTreeNode(final XsdTreeNode parent, final Node xsdNode)
    {
        this.parent = parent;
        this.xsdNode = xsdNode;
    }

    public int getChildCount()
    {
        assureChildren();
        return this.children.size();
    }

    public XsdTreeNode getChild(final int index)
    {
        assureChildren();
        return this.children.get(index);
    }

    public XsdTreeNode getParent()
    {
        return this.parent;
    }

    private void assureChildren()
    {
        if (this.children != null)
        {
            return;
        }
        this.children = new ArrayList<>();
        if (!this.xsdNode.hasChildNodes())
        {
            return;
        }
        for (int childIndex = 0; childIndex < this.xsdNode.getChildNodes().getLength(); childIndex++)
        {
            Node node = this.xsdNode.getChildNodes().item(childIndex);
            switch (node.getNodeName())
            {
                case "#document":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:schema":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:element":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:complexType":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:sequence":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:choice":
                    this.children.add(new XsdTreeNode(this, node));
                    break;
                case "xsd:key":

                case "xsd:unique":

                case "xsd:keyref":
            }
        }

    }

    public final void remove()
    {
        this.parent.children.remove(this);
        this.parent = null;
    }

    public void reExpand()
    {
        this.children = null;
        assureChildren();
    }

    public int attributeCount()
    {
        assureAttributes();
        return this.attributeNodes.size();
    }

    public Node getAttributeNode(final int index)
    {
        assureAttributes();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        return this.attributeNodes.get(index);
    }

    public void setAttributeValue(final int index, final String value)
    {
        assureAttributes();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        this.attributeValues.set(index, value);
    }
    
    public String getAttributeValue(final int index)
    {
        assureAttributes();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        return this.attributeValues.get(index);
    }

    /**
     * Finds all attributes that meet the following structure. If the attribute also specifies a {@code default}, it is also
     * stored.
     *
     * <pre>
     * &lt;xsd:element ...&gt;
     *   ...
     *   &lt;xsd:complexType&gt;
     *     ...
     *     &lt;xsd:attribute name="NAME" use="USE" ...&gt;
     *     ...
     *   &lt;/xsd:complexType&gt;
     *   ...
     * &lt;/xsd:element&gt;
     * </pre>
     */
    private synchronized void assureAttributes()
    {
        if (this.attributeNodes != null)
        {
            return;
        }
        this.attributeNodes = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        if (!this.xsdNode.hasChildNodes() || !isComplexType())
        {
            return;
        }
        for (int childIndex = 0; childIndex < this.complexType.getChildNodes().getLength(); childIndex++)
        {
            Node child = this.complexType.getChildNodes().item(childIndex);
            // <xsd:attribute name="LENGTH" use="required" default="1.75m">
            if (child.getNodeName().equals("xsd:attribute") && child.hasAttributes()
                    && child.getAttributes().getNamedItem("name") != null && child.getAttributes().getNamedItem("use") != null)
            {
                this.attributeNodes.add(child);
                this.attributeValues.add(child.hasAttributes() && child.getAttributes().getNamedItem("default") != null
                        ? child.getAttributes().getNamedItem("default").getNodeValue() : null);
            }
        }
    }

    public boolean isIdentifiable()
    {
        if (this.isIdentifiable == null)
        {
            assureAttributes();
            for (Node node : this.attributeNodes)
            {
                if (node.getAttributes().getNamedItem("name").getNodeValue().equals("ID"))
                {
                    this.isIdentifiable = true;
                    break;
                }
            }
        }
        return this.isIdentifiable;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public boolean isEditable()
    {
        // TODO: when precisely can a node have a value?
        if (this.isEditable == null)
        {
            if (isComplexType())
            {
                // 'mixed = true' can have text, if mixed not specified, default is false
                if (!this.complexType.hasAttributes() || this.complexType.getAttributes().getNamedItem("mixed") == null
                        || this.complexType.getAttributes().getNamedItem("mixed").getNodeValue().equals("false"))
                {
                    this.isEditable = false;
                }
                else
                {
                    this.isEditable = true;
                }
            }
            else
            {
                this.isEditable = true;
            }
        }
        return this.isEditable;
    }

    public void setValue(final String value)
    {
        Throw.when(!isEditable(), IllegalStateException.class,
                "Node is not a xsd:simpleType or mixed xsd:complexType, hence no value is allowed.");
        this.value = value;
    }

    public String getValue()
    {
        return this.value;
    }

    private boolean isComplexType()
    {
        if (this.isComplex == null)
        {
            for (int childIndex = 0; childIndex < this.xsdNode.getChildNodes().getLength(); childIndex++)
            {
                if (this.xsdNode.getChildNodes().item(childIndex).getNodeName().equals("xsd:complexType"))
                {
                    this.complexType = this.xsdNode.getChildNodes().item(childIndex);
                    break;
                }
            }
            this.isComplex = this.complexType != null;
        }
        return this.isComplex;
    }

    @Override
    public String toString()
    {
        // TODO: temporary implementation
        if (this.xsdNode.hasAttributes() && this.xsdNode.getAttributes().getNamedItem("name") != null)
        {
            return this.xsdNode.getAttributes().getNamedItem("name").getNodeValue();
        }
        if (this.xsdNode.hasAttributes() && this.xsdNode.getAttributes().getNamedItem("ref") != null)
        {
            return this.xsdNode.getAttributes().getNamedItem("ref").getNodeValue();
        }
        return this.xsdNode.getNodeName();
    }

    public boolean isDeletable()
    {
        // TODO: temporary implementation
        if (this.xsdNode.hasAttributes() && this.xsdNode.getAttributes().getNamedItem("name") != null)
        {
            return true;
        }
        if (this.xsdNode.hasAttributes() && this.xsdNode.getAttributes().getNamedItem("ref") != null)
        {
            return true;
        }
        return false;
    }

}
