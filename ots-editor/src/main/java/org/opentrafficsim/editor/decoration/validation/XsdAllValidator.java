package org.opentrafficsim.editor.decoration.validation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Validates that children nodes are not duplicate within their parent. Nodes are considered equal if their names are equal.
 * This class is assigned automatically by {@code XsdTreeNode} to {@code xsd:all} contexts and should not be created during
 * editor decoration. This class is different from {@code NoDuplicateChildrenValidator} in that it is assigned automatically,
 * and that the value of child nodes is not relevant in considering child equality.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdAllValidator implements Function<XsdTreeNode, String>, EventListener
{

    /** All nodes this validator validates. */
    private Set<XsdTreeNode> nodes = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param root root.
     */
    public XsdAllValidator(final XsdTreeNodeRoot root)
    {
        root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
    }

    /**
     * Add node to this validator. This is done by {@code XsdTreeNode} automatically.
     * @param node node.
     */
    public void addNode(final XsdTreeNode node)
    {
        this.nodes.add(node);
        node.addListener(this, XsdTreeNode.OPTION_CHANGED);
        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        this.nodes.forEach((n) -> n.invalidate());
    }

    /**
     * Remove node from this validator. This is done by listening to NODE_REMOVED events.
     * @param node node.
     */
    private void removeNode(final XsdTreeNode node)
    {
        if (this.nodes.remove(node))
        {
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            this.nodes.forEach((n) -> n.invalidate());
        }
    }

    @Override
    public String apply(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        List<XsdTreeNode> childs = node.getParent().getChildren();
        if (childs.stream().filter((n) -> node.getNodeName().equals(n.getNodeName())).count() > 1)
        {
            return "Duplicate elements " + node.getNodeName() + " is not allowed.";
        }
        return null;
    }

    @Override
    public void notify(final Event event)
    {
        if (XsdTreeNodeRoot.NODE_REMOVED.equals(event.getType()))
        {
            removeNode((XsdTreeNode) ((Object[]) event.getContent())[0]);
            return;
        }
        if (XsdTreeNode.OPTION_CHANGED.equals(event.getType()) || XsdTreeNode.ACTIVATION_CHANGED.equals(event.getType()))
        {
            this.nodes.forEach((n) -> n.invalidate());
        }
    }

}
