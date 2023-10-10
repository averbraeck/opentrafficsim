package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.djutils.event.Event;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeUtil;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;

/**
 * Validates that children nodes are not duplicate within their parent, this node.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class NoDuplicateChildrenValidator extends AbstractNodeDecoratorRemove implements Function<XsdTreeNode, String>
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Path location of nodes to attach to. */
    private final String path;

    /** First attribute to compare. */
    private final List<String> children;

    /** All nodes this validator validates. */
    private Set<XsdTreeNode> nodes = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path location of nodes to attach to.
     * @param children String...; children each of which may not have duplicates. Use none to check all children.
     */
    public NoDuplicateChildrenValidator(final OtsEditor editor, final String path, final String... children)
    {
        super(editor);
        this.path = path;
        this.children = Arrays.asList(children);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (this.children.isEmpty())
        {
            XsdTreeNode parent = node.getParent();
            if (parent != null && parent.isType(this.path))
            {
                node.addNodeValidator(NoDuplicateChildrenValidator.this);
            }
        }
        else
        {
            for (String child : this.children)
            {
                if (node.isType(this.path + "." + child))
                {
                    node.addNodeValidator(NoDuplicateChildrenValidator.this);
                }
            }
        }
        this.nodes.add(node);
        node.addListener(this, XsdTreeNode.VALUE_CHANGED);
        node.addListener(this, XsdTreeNode.OPTION_CHANGED);
        node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        this.nodes.forEach((n) -> n.invalidate());
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRemoved(final XsdTreeNode node)
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
     * @param node1 XsdTreeNode; node 1.
     * @param node2 XsdTreeNode; node 2.
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
        if (XsdTreeNode.VALUE_CHANGED.equals(event.getType()) || XsdTreeNode.OPTION_CHANGED.equals(event.getType())
                || XsdTreeNode.ATTRIBUTE_CHANGED.equals(event.getType())
                || XsdTreeNode.ACTIVATION_CHANGED.equals(event.getType()))
        {
            this.nodes.forEach((n) -> n.invalidate());
            return;
        }
        super.notify(event);
    }

}
