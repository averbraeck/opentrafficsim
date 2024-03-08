package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.List;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Common functionality between a key/unique validator, and a keyref validator.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class XPathValidator implements ValueValidator
{

    /** The node defining the xsd:key, xsd:keyref or xsd:unique. */
    protected final Node keyNode;

    /** Path where the key was defined. */
    protected final String keyPath;

    /** Name of the attribute the key points to, {@code null} if it points to a child element. */
    protected final List<String> attributeNames = new ArrayList<>();

    /** Name of the child element the key points to, {@code null} if it points to an argument. */
    protected final List<String> childNames = new ArrayList<>();

    /** Whether to include the value of the node itself (i.e. field reference "."). */
    protected boolean includeSelfValue;

    /**
     * Constructor.
     * @param keyNode Node; node defining the xsd:key, xsd:unique or xsd:keyref.
     * @param keyPath String; path where the key was defined.
     */
    public XPathValidator(final Node keyNode, final String keyPath)
    {
        this.keyNode = keyNode;
        this.keyPath = keyPath;
        List<Node> fields = DocumentReader.getChildren(keyNode, "xsd:field");
        for (Node field : fields)
        {
            String value = DocumentReader.getAttribute(field, "xpath");
            if (value.startsWith("@"))
            {
                this.attributeNames.add(value.substring(1));
                continue;
            }
            else if (value.startsWith("ots:"))
            {
                this.childNames.add(value.substring(4));
                continue;
            }
            else if (value.equals("."))
            {
                this.includeSelfValue = true;
                continue;
            }
            throw new UnsupportedOperationException(
                    "Unable to validate keyref that does not point to an attribute (@) or OTS type (ots:).");
        }
    }

    /**
     * Returns the name of the key, i.e. {@code <xsd:keyref name="Name">}.
     * @return String; name of the key.
     */
    public String getKeyName()
    {
        return DocumentReader.getAttribute(this.keyNode, "name");
    }

    /**
     * Returns the type {@code String} for which the xsd:key, xsd:unique or xsd:keyref applies, i.e. "GtuTypes.GtuType" for
     * {@code <xsd:selector xpath=".//ots:GtuTypes/ots:GtuType" />}. Note that multiple paths may be defined separated by "|".
     * @return String[]; type for which the xsd:key or xsd:keyref applies.
     */
    public String[] getTypeString()
    {
        return DocumentReader.getAttribute(DocumentReader.getChild(this.keyNode, "xsd:selector"), "xpath")
                .replace(".//ots:", "").replace("ots:", "").replace("/", ".").split("\\|");
    }

    /**
     * Gathers all the field values, i.e. attribute, child element value, or own value. As validators are registered with the
     * node that has the value, attributes are gathered from the given node, while element values are taken from the correctly
     * named children of the parent. Empty values are returned as {@code null}.
     * @param node XsdTreeNode; node for which to get the information.
     * @return List&lt;String&gt;; field values.
     */
    protected List<String> gatherFields(final XsdTreeNode node)
    {
        List<String> nodeList = new ArrayList<>();
        if (node.getPathString().endsWith("DefaultInputParameters.String"))
        {
            Throw.when(this.attributeNames.size() + this.childNames.size() + (this.includeSelfValue ? 1 : 0) != 1,
                    IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but it has multiple fields.", getKeyName());
            nodeList.add(node.getId());
        }
        else
        {
            for (String attribute : this.attributeNames)
            {
                nodeList.add(node.getAttributeValue(attribute));
            }
            // a child calls this method to validate its value, need to gather all children's values via parent
            XsdTreeNode parent = node.getParent();
            if (parent != null)
            {
                for (String child : this.childNames)
                {
                    for (XsdTreeNode treeChild : parent.getChildren())
                    {
                        if (treeChild.getNodeName().equals(child))
                        {
                            nodeList.add(treeChild.getValue());
                        }
                    }
                }
            }
            if (this.includeSelfValue)
            {
                nodeList.add(node.getValue());
            }
        }
        nodeList.replaceAll((v) -> "".equals(v) ? null : v);
        return nodeList;
    }

    /**
     * Returns a node that represent the proper context. This is a parent node of the given node, at the level where the
     * xsd:key, xsd:unique or xsd:keyref was defined.
     * @param node XsdTreeNode; any node somewhere in the context, i.e. subtree.
     * @return XsdTreeNode; node that represents the proper context.
     */
    protected XsdTreeNode getContext(final XsdTreeNode node)
    {
        XsdTreeNode context = null;
        List<XsdTreeNode> path = node.getPath();
        for (int index = path.size() - 1; index >= 0; index--)
        {
            if (path.get(index).getPathString().endsWith(getPath()))
            {
                context = path.get(index);
                break;
            }
        }
        return context;
    }

    /**
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined.
     * @return String; path at which the xsd:key or xsd:keyref is defined.
     */
    public String getPath()
    {
        return this.keyPath;
    }

    /**
     * Adds node to this key, if applicable. Nodes are stored per parent instance that defines the context at the level of the
     * path at which the key was defined. This method is called by a listener that the root node has set up, for every created
     * node.
     * @param node XsdTreeNode; node to add.
     */
    abstract public void addNode(XsdTreeNode node);

    /**
     * Remove node. It is removed from all contexts and listening keyrefs. This method is called by a listener that the root
     * node has set up, for every removed node.
     * @param node XsdTreeNode; node to remove.
     */
    abstract public void removeNode(XsdTreeNode node);

}
