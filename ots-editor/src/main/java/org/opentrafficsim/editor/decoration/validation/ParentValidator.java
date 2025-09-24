package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;

/**
 * Validates that the Parent attribute of a node does not refer to self, either directly or indirectly.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ParentValidator extends AbstractNodeDecoratorRemove implements ValueValidator
{

    /** */
    private static final long serialVersionUID = 20230319L;

    /** Context level within which the parents may be found. */
    private String contextPath;

    /** Attribute defining a parent. */
    private String idAttribute;

    /** Attribute referring to a parent. */
    private String parentAttribute;

    /** All nodes created under the path. */
    private Map<XsdTreeNode, Set<XsdTreeNode>> nodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param editor editor.
     * @param path path of the nodes that have a Parent attribute referring to another node under the same path.
     */
    public ParentValidator(final OtsEditor editor, final String path)
    {
        super(editor, (n) -> n.isType(path));
        this.contextPath = "Ots";
        this.idAttribute = "Id";
        this.parentAttribute = "Parent";
    }

    /**
     * Sets the context path, which should refer to a higher level node relative to the nodes at the regular path. Default is
     * "Ots".
     * @param contextPath context level within which the parents may be found.
     * @return this validator for method chaining.
     */
    public ParentValidator setContext(final String contextPath)
    {
        this.contextPath = contextPath;
        return this;
    }

    /**
     * Sets the attribute that is the defining attribute of a parent. Default is "Id".
     * @param idAttribute attribute defining a parent.
     * @return this validator for method chaining.
     */
    public ParentValidator setIdAttribute(final String idAttribute)
    {
        this.idAttribute = idAttribute;
        return this;
    }

    /**
     * Sets the attribute that refers to a parent. Default is "Parent".
     * @param parentAttribute attribute defining a parent.
     * @return this validator for method chaining.
     */
    public ParentValidator setParentAttribute(final String parentAttribute)
    {
        this.parentAttribute = parentAttribute;
        return this;
    }

    @Override
    public String validate(final XsdTreeNode node)
    {
        String value = node.getAttributeValue(this.parentAttribute);
        if (value == null || value.isEmpty() || !node.isActive())
        {
            return null;
        }
        List<XsdTreeNode> list = new ArrayList<>();
        list.add(node);
        return validateParent(getContext(node), value, list);
    }

    /**
     * Finds the parent node, checks whether it is referring to an exiting node in the list, and moves to the next parent.
     * @param context context, i.e. all relevant possible parent nodes.
     * @param parentId id of next parent node to find.
     * @param nodeList list of nodes so far, every next node is the parent of the previous.
     * @return message if the parent refers to self, directly or indirectly, or {@code null} otherwise.
     */
    private String validateParent(final Set<XsdTreeNode> context, final String parentId, final List<XsdTreeNode> nodeList)
    {
        for (XsdTreeNode otherNode : context)
        {
            if (otherNode.isActive() && parentId.equals(otherNode.getAttributeValue(this.idAttribute)))
            {
                int index = nodeList.indexOf(otherNode);
                if (index == 0)
                {
                    StringBuilder str = new StringBuilder("Parent refers to self: ");
                    String separator = "";
                    for (XsdTreeNode node : nodeList)
                    {
                        str.append(separator).append(node.getAttributeValue(this.idAttribute));
                        separator = " > ";
                    }
                    str.append(separator).append(otherNode.getAttributeValue(this.idAttribute));
                    return str.toString();
                }
                else if (index > 0)
                {
                    return null; // A > B > C > C > C ... report at C, not at A
                }
                String value = otherNode.getAttributeValue(this.parentAttribute);
                if (value == null || value.isEmpty())
                {
                    return null;
                }
                nodeList.add(otherNode);
                return validateParent(context, value, nodeList);
            }
        }
        return null;
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        node.addAttributeValidator(this.parentAttribute, ParentValidator.this);
        getContext(node).add(node);
    }

    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        getContext(node).remove(node);
    }

    /**
     * Returns the context of the given node, i.e. all relevant possible parent nodes.
     * @param node node.
     * @return Context of the given node, i.e. all relevant possible parent nodes.
     */
    private Set<XsdTreeNode> getContext(final XsdTreeNode node)
    {
        XsdTreeNode parent = node;
        while (!parent.getPathString().equals(this.contextPath) && parent != null)
        {
            parent = parent.getParent();
        }
        return this.nodes.computeIfAbsent(parent, (p) -> new LinkedHashSet<>());
    }

}
