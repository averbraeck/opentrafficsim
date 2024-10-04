package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.XsdTreeNodeUtil;

/**
 * Validates that children nodes are not duplicate within their parent. Nodes are considered equal if their names are equal.
 * This class is assigned automatically to xsd:all contexts and should not be created during editor decoration.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdAllValidator implements Function<XsdTreeNode, String>, EventListener
{

    /** */
    private static final long serialVersionUID = 20240314L;

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
     * Add node to this validator.
     * @param node node.
     */
    public void addNode(final XsdTreeNode node)
    {
        this.nodes.add(node);
        node.addListener(this, XsdTreeNode.VALUE_CHANGED);
        node.addListener(this, XsdTreeNode.OPTION_CHANGED);
        node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        this.nodes.forEach((n) -> n.invalidate());
    }

    /**
     * Remove node from this validator.
     * @param node node.
     */
    private void removeNode(final XsdTreeNode node)
    {
        if (this.nodes.remove(node))
        {
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            this.nodes.forEach((n) -> n.invalidate());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String apply(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        List<XsdTreeNode> childs = node.getParent().getChildren();
        if (childs.stream().filter((n) -> nodesEqual(node, n)).count() > 1)
        {
            return "Duplicate elements " + node.getNodeName() + " is not allowed.";
        }
        return null;
    }

    /**
     * Returns whether the nodes are equal by path string and value.
     * @param node1 node 1.
     * @param node2 node 2.
     * @return whether the nodes are equal by path string and value.
     */
    private final static boolean nodesEqual(final XsdTreeNode node1, final XsdTreeNode node2)
    {
        if (!node1.getPathString().equals(node2.getPathString()))
        {
            return false;
        }
        if (!XsdTreeNodeUtil.valuesAreEqual(node1.getValue(), node2.getValue()))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (XsdTreeNodeRoot.NODE_REMOVED.equals(event.getType()))
        {
            removeNode((XsdTreeNode) ((Object[]) event.getContent())[0]);
            return;
        }
        if (XsdTreeNode.VALUE_CHANGED.equals(event.getType()) || XsdTreeNode.OPTION_CHANGED.equals(event.getType())
                || XsdTreeNode.ATTRIBUTE_CHANGED.equals(event.getType())
                || XsdTreeNode.ACTIVATION_CHANGED.equals(event.getType()))
        {
            this.nodes.forEach((n) -> n.invalidate());
        }
    }

}
